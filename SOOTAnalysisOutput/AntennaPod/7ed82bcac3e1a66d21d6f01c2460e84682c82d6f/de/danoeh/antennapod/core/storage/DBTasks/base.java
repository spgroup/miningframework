package de.danoeh.antennapod.core.storage;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import de.danoeh.antennapod.core.ClientConfig;
import de.danoeh.antennapod.core.asynctask.FlattrClickWorker;
import de.danoeh.antennapod.core.asynctask.FlattrStatusFetcher;
import de.danoeh.antennapod.core.feed.EventDistributor;
import de.danoeh.antennapod.core.feed.Feed;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.feed.FeedMedia;
import de.danoeh.antennapod.core.feed.FeedPreferences;
import de.danoeh.antennapod.core.service.GpodnetSyncService;
import de.danoeh.antennapod.core.service.download.DownloadStatus;
import de.danoeh.antennapod.core.service.playback.PlaybackService;
import de.danoeh.antennapod.core.util.DownloadError;
import de.danoeh.antennapod.core.util.LongList;
import de.danoeh.antennapod.core.util.comparator.FeedItemPubdateComparator;
import de.danoeh.antennapod.core.util.exception.MediaFileNotFoundException;
import de.danoeh.antennapod.core.util.flattr.FlattrUtils;

public final class DBTasks {

    private static final String TAG = "DBTasks";

    private static ExecutorService autodownloadExec;

    static {
        autodownloadExec = Executors.newSingleThreadExecutor(new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setPriority(Thread.MIN_PRIORITY);
                return t;
            }
        });
    }

    private DBTasks() {
    }

    public static void removeFeedWithDownloadUrl(Context context, String downloadUrl) {
        PodDBAdapter adapter = PodDBAdapter.getInstance();
        adapter.open();
        Cursor cursor = adapter.getFeedCursorDownloadUrls();
        long feedID = 0;
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(1).equals(downloadUrl)) {
                    feedID = cursor.getLong(0);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        adapter.close();
        if (feedID != 0) {
            try {
                DBWriter.deleteFeed(context, feedID).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            Log.w(TAG, "removeFeedWithDownloadUrl: Could not find feed with url: " + downloadUrl);
        }
    }

    public static void playMedia(final Context context, final FeedMedia media, boolean showPlayer, boolean startWhenPrepared, boolean shouldStream) {
        try {
            if (!shouldStream) {
                if (media.fileExists() == false) {
                    throw new MediaFileNotFoundException("No episode was found at " + media.getFile_url(), media);
                }
            }
            Intent launchIntent = new Intent(context, PlaybackService.class);
            launchIntent.putExtra(PlaybackService.EXTRA_PLAYABLE, media);
            launchIntent.putExtra(PlaybackService.EXTRA_START_WHEN_PREPARED, startWhenPrepared);
            launchIntent.putExtra(PlaybackService.EXTRA_SHOULD_STREAM, shouldStream);
            launchIntent.putExtra(PlaybackService.EXTRA_PREPARE_IMMEDIATELY, true);
            context.startService(launchIntent);
            if (showPlayer) {
                context.startActivity(PlaybackService.getPlayerActivityIntent(context, media));
            }
            DBWriter.addQueueItemAt(context, media.getItem().getId(), 0, false);
        } catch (MediaFileNotFoundException e) {
            e.printStackTrace();
            if (media.isPlaying()) {
                context.sendBroadcast(new Intent(PlaybackService.ACTION_SHUTDOWN_PLAYBACK_SERVICE));
            }
            notifyMissingFeedMediaFile(context, media);
        }
    }

    private static AtomicBoolean isRefreshing = new AtomicBoolean(false);

    public static void refreshAllFeeds(final Context context, final List<Feed> feeds) {
        if (isRefreshing.compareAndSet(false, true)) {
            new Thread() {

                public void run() {
                    if (feeds != null) {
                        refreshFeeds(context, feeds);
                    } else {
                        refreshFeeds(context, DBReader.getFeedList());
                    }
                    isRefreshing.set(false);
                    if (FlattrUtils.hasToken()) {
                        Log.d(TAG, "Flattring all pending things.");
                        new FlattrClickWorker(context).executeAsync();
                        Log.d(TAG, "Fetching flattr status.");
                        new FlattrStatusFetcher(context).start();
                    }
                    if (ClientConfig.gpodnetCallbacks.gpodnetEnabled()) {
                        GpodnetSyncService.sendSyncIntent(context);
                    }
                    Log.d(TAG, "refreshAllFeeds autodownload");
                    autodownloadUndownloadedItems(context);
                }
            }.start();
        } else {
            Log.d(TAG, "Ignoring request to refresh all feeds: Refresh lock is locked");
        }
    }

    private static void refreshFeeds(final Context context, final List<Feed> feedList) {
        for (Feed feed : feedList) {
            FeedPreferences prefs = feed.getPreferences();
            if (prefs.getKeepUpdated()) {
                try {
                    refreshFeed(context, feed);
                } catch (DownloadRequestException e) {
                    e.printStackTrace();
                    DBWriter.addDownloadStatus(new DownloadStatus(feed, feed.getHumanReadableIdentifier(), DownloadError.ERROR_REQUEST_ERROR, false, e.getMessage()));
                }
            }
        }
    }

    public static void refreshCompleteFeed(final Context context, final Feed feed) {
        try {
            refreshFeed(context, feed, true);
        } catch (DownloadRequestException e) {
            e.printStackTrace();
            DBWriter.addDownloadStatus(new DownloadStatus(feed, feed.getHumanReadableIdentifier(), DownloadError.ERROR_REQUEST_ERROR, false, e.getMessage()));
        }
    }

    public static void loadNextPageOfFeed(final Context context, Feed feed, boolean loadAllPages) throws DownloadRequestException {
        if (feed.isPaged() && feed.getNextPageLink() != null) {
            int pageNr = feed.getPageNr() + 1;
            Feed nextFeed = new Feed(feed.getNextPageLink(), new Date(), feed.getTitle() + "(" + pageNr + ")");
            nextFeed.setPageNr(pageNr);
            nextFeed.setPaged(true);
            nextFeed.setId(feed.getId());
            DownloadRequester.getInstance().downloadFeed(context, nextFeed, loadAllPages);
        } else {
            Log.e(TAG, "loadNextPageOfFeed: Feed was either not paged or contained no nextPageLink");
        }
    }

    public static void refreshFeed(Context context, Feed feed) throws DownloadRequestException {
        Log.d(TAG, "refreshFeed(feed.id: " + feed.getId() + ")");
        refreshFeed(context, feed, false);
    }

    private static void refreshFeed(Context context, Feed feed, boolean loadAllPages) throws DownloadRequestException {
        Feed f;
        Date lastUpdate = feed.hasLastUpdateFailed() ? new Date(0) : feed.getLastUpdate();
        if (feed.getPreferences() == null) {
            f = new Feed(feed.getDownload_url(), lastUpdate, feed.getTitle());
        } else {
            f = new Feed(feed.getDownload_url(), lastUpdate, feed.getTitle(), feed.getPreferences().getUsername(), feed.getPreferences().getPassword());
        }
        f.setId(feed.getId());
        DownloadRequester.getInstance().downloadFeed(context, f, loadAllPages);
    }

    public static void notifyMissingFeedMediaFile(final Context context, final FeedMedia media) {
        Log.i(TAG, "The feedmanager was notified about a missing episode. It will update its database now.");
        media.setDownloaded(false);
        media.setFile_url(null);
        DBWriter.setFeedMedia(media);
        EventDistributor.getInstance().sendFeedUpdateBroadcast();
    }

    public static void downloadAllItemsInQueue(final Context context) {
        new Thread() {

            public void run() {
                List<FeedItem> queue = DBReader.getQueue();
                if (!queue.isEmpty()) {
                    try {
                        downloadFeedItems(context, queue.toArray(new FeedItem[queue.size()]));
                    } catch (DownloadRequestException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public static void downloadFeedItems(final Context context, FeedItem... items) throws DownloadRequestException {
        downloadFeedItems(true, context, items);
    }

    static void downloadFeedItems(boolean performAutoCleanup, final Context context, final FeedItem... items) throws DownloadRequestException {
        final DownloadRequester requester = DownloadRequester.getInstance();
        if (performAutoCleanup) {
            new Thread() {

                @Override
                public void run() {
                    ClientConfig.dbTasksCallbacks.getEpisodeCacheCleanupAlgorithm().makeRoomForEpisodes(context, items.length);
                }
            }.start();
        }
        for (FeedItem item : items) {
            if (item.getMedia() != null && !requester.isDownloadingFile(item.getMedia()) && !item.getMedia().isDownloaded()) {
                if (items.length > 1) {
                    try {
                        requester.downloadMedia(context, item.getMedia());
                    } catch (DownloadRequestException e) {
                        e.printStackTrace();
                        DBWriter.addDownloadStatus(new DownloadStatus(item.getMedia(), item.getMedia().getHumanReadableIdentifier(), DownloadError.ERROR_REQUEST_ERROR, false, e.getMessage()));
                    }
                } else {
                    requester.downloadMedia(context, item.getMedia());
                }
            }
        }
    }

    public static Future<?> autodownloadUndownloadedItems(final Context context) {
        Log.d(TAG, "autodownloadUndownloadedItems");
        return autodownloadExec.submit(ClientConfig.dbTasksCallbacks.getAutomaticDownloadAlgorithm().autoDownloadUndownloadedItems(context));
    }

    public static void performAutoCleanup(final Context context) {
        ClientConfig.dbTasksCallbacks.getEpisodeCacheCleanupAlgorithm().performCleanup(context);
    }

    public static FeedItem getQueueSuccessorOfItem(final long itemId, List<FeedItem> queue) {
        FeedItem result = null;
        if (queue == null) {
            queue = DBReader.getQueue();
        }
        if (queue != null) {
            Iterator<FeedItem> iterator = queue.iterator();
            while (iterator.hasNext()) {
                FeedItem item = iterator.next();
                if (item.getId() == itemId) {
                    if (iterator.hasNext()) {
                        result = iterator.next();
                    }
                    break;
                }
            }
        }
        return result;
    }

    public static boolean isInQueue(Context context, final long feedItemId) {
        LongList queue = DBReader.getQueueIDList();
        return queue.contains(feedItemId);
    }

    private static Feed searchFeedByIdentifyingValueOrID(PodDBAdapter adapter, Feed feed) {
        if (feed.getId() != 0) {
            return DBReader.getFeed(feed.getId(), adapter);
        } else {
            List<Feed> feeds = DBReader.getFeedList();
            for (Feed f : feeds) {
                if (f.getIdentifyingValue().equals(feed.getIdentifyingValue())) {
                    f.setItems(DBReader.getFeedItemList(f));
                    return f;
                }
            }
        }
        return null;
    }

    private static FeedItem searchFeedItemByIdentifyingValue(Feed feed, String identifier) {
        for (FeedItem item : feed.getItems()) {
            if (item.getIdentifyingValue().equals(identifier)) {
                return item;
            }
        }
        return null;
    }

    public static synchronized Feed[] updateFeed(final Context context, final Feed... newFeeds) {
        List<Feed> newFeedsList = new ArrayList<Feed>();
        List<Feed> updatedFeedsList = new ArrayList<Feed>();
        Feed[] resultFeeds = new Feed[newFeeds.length];
        PodDBAdapter adapter = PodDBAdapter.getInstance();
        adapter.open();
        for (int feedIdx = 0; feedIdx < newFeeds.length; feedIdx++) {
            final Feed newFeed = newFeeds[feedIdx];
            final Feed savedFeed = searchFeedByIdentifyingValueOrID(adapter, newFeed);
            if (savedFeed == null) {
                Log.d(TAG, "Found no existing Feed with title " + newFeed.getTitle() + ". Adding as new one.");
                FeedItem mostRecent = newFeed.getMostRecentItem();
                if (mostRecent != null) {
                    mostRecent.setNew();
                }
                newFeedsList.add(newFeed);
                resultFeeds[feedIdx] = newFeed;
            } else {
                Log.d(TAG, "Feed with title " + newFeed.getTitle() + " already exists. Syncing new with existing one.");
                Collections.sort(newFeed.getItems(), new FeedItemPubdateComparator());
                if (newFeed.getPageNr() == savedFeed.getPageNr()) {
                    if (savedFeed.compareWithOther(newFeed)) {
                        Log.d(TAG, "Feed has updated attribute values. Updating old feed's attributes");
                        savedFeed.updateFromOther(newFeed);
                    }
                } else {
                    Log.d(TAG, "New feed has a higher page number.");
                    savedFeed.setNextPageLink(newFeed.getNextPageLink());
                }
                if (savedFeed.getPreferences().compareWithOther(newFeed.getPreferences())) {
                    Log.d(TAG, "Feed has updated preferences. Updating old feed's preferences");
                    savedFeed.getPreferences().updateFromOther(newFeed.getPreferences());
                }
                FeedItem priorMostRecent = savedFeed.getMostRecentItem();
                Date priorMostRecentDate = null;
                if (priorMostRecent != null) {
                    priorMostRecentDate = priorMostRecent.getPubDate();
                }
                for (int idx = 0; idx < newFeed.getItems().size(); idx++) {
                    final FeedItem item = newFeed.getItems().get(idx);
                    FeedItem oldItem = searchFeedItemByIdentifyingValue(savedFeed, item.getIdentifyingValue());
                    if (oldItem == null) {
                        item.setFeed(savedFeed);
                        item.setAutoDownload(savedFeed.getPreferences().getAutoDownload());
                        savedFeed.getItems().add(idx, item);
                        if (priorMostRecentDate == null || priorMostRecentDate.before(item.getPubDate())) {
                            Log.d(TAG, "Marking item published on " + item.getPubDate() + " new, prior most recent date = " + priorMostRecentDate);
                            item.setNew();
                        }
                    } else {
                        oldItem.updateFromOther(item);
                    }
                }
                savedFeed.setLastUpdate(newFeed.getLastUpdate());
                savedFeed.setType(newFeed.getType());
                savedFeed.setLastUpdateFailed(false);
                updatedFeedsList.add(savedFeed);
                resultFeeds[feedIdx] = savedFeed;
            }
        }
        adapter.close();
        try {
            DBWriter.addNewFeed(context, newFeedsList.toArray(new Feed[newFeedsList.size()])).get();
            DBWriter.setCompleteFeed(updatedFeedsList.toArray(new Feed[updatedFeedsList.size()])).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        EventDistributor.getInstance().sendFeedUpdateBroadcast();
        return resultFeeds;
    }

    public static FutureTask<List<FeedItem>> searchFeedItemTitle(final Context context, final long feedID, final String query) {
        return new FutureTask<List<FeedItem>>(new QueryTask<List<FeedItem>>(context) {

            @Override
            public void execute(PodDBAdapter adapter) {
                Cursor searchResult = adapter.searchItemTitles(feedID, query);
                List<FeedItem> items = DBReader.extractItemlistFromCursor(searchResult);
                DBReader.loadAdditionalFeedItemListData(items);
                setResult(items);
                searchResult.close();
            }
        });
    }

    public static FutureTask<List<FeedItem>> searchFeedItemDescription(final Context context, final long feedID, final String query) {
        return new FutureTask<List<FeedItem>>(new QueryTask<List<FeedItem>>(context) {

            @Override
            public void execute(PodDBAdapter adapter) {
                Cursor searchResult = adapter.searchItemDescriptions(feedID, query);
                List<FeedItem> items = DBReader.extractItemlistFromCursor(searchResult);
                DBReader.loadAdditionalFeedItemListData(items);
                setResult(items);
                searchResult.close();
            }
        });
    }

    public static FutureTask<List<FeedItem>> searchFeedItemContentEncoded(final Context context, final long feedID, final String query) {
        return new FutureTask<List<FeedItem>>(new QueryTask<List<FeedItem>>(context) {

            @Override
            public void execute(PodDBAdapter adapter) {
                Cursor searchResult = adapter.searchItemContentEncoded(feedID, query);
                List<FeedItem> items = DBReader.extractItemlistFromCursor(searchResult);
                DBReader.loadAdditionalFeedItemListData(items);
                setResult(items);
                searchResult.close();
            }
        });
    }

    public static FutureTask<List<FeedItem>> searchFeedItemChapters(final Context context, final long feedID, final String query) {
        return new FutureTask<List<FeedItem>>(new QueryTask<List<FeedItem>>(context) {

            @Override
            public void execute(PodDBAdapter adapter) {
                Cursor searchResult = adapter.searchItemChapters(feedID, query);
                List<FeedItem> items = DBReader.extractItemlistFromCursor(searchResult);
                DBReader.loadAdditionalFeedItemListData(items);
                setResult(items);
                searchResult.close();
            }
        });
    }

    static abstract class QueryTask<T> implements Callable<T> {

        private T result;

        private Context context;

        public QueryTask(Context context) {
            this.context = context;
        }

        @Override
        public T call() throws Exception {
            PodDBAdapter adapter = PodDBAdapter.getInstance();
            adapter.open();
            execute(adapter);
            adapter.close();
            return result;
        }

        public abstract void execute(PodDBAdapter adapter);

        protected void setResult(T result) {
            this.result = result;
        }
    }

    public static void flattrItemIfLoggedIn(Context context, FeedItem item) {
        if (FlattrUtils.hasToken()) {
            item.getFlattrStatus().setFlattrQueue();
            DBWriter.setFlattredStatus(context, item, true);
        } else {
            FlattrUtils.showNoTokenDialogOrRedirect(context, item.getPaymentLink());
        }
    }

    public static void flattrFeedIfLoggedIn(Context context, Feed feed) {
        if (FlattrUtils.hasToken()) {
            feed.getFlattrStatus().setFlattrQueue();
            DBWriter.setFlattredStatus(context, feed, true);
        } else {
            FlattrUtils.showNoTokenDialogOrRedirect(context, feed.getPaymentLink());
        }
    }
}
