package com.google.android.exoplayer2.ext.cast;

import android.os.Looper;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.BasePlayer;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.gms.cast.CastStatusCodes;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.cast.framework.media.RemoteMediaClient.MediaChannelResult;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;

public final class CastPlayer extends BasePlayer {

    static {
        ExoPlayerLibraryInfo.registerModule("goog.exo.cast");
    }

    private static final String TAG = "CastPlayer";

    private static final int RENDERER_COUNT = 3;

    private static final int RENDERER_INDEX_VIDEO = 0;

    private static final int RENDERER_INDEX_AUDIO = 1;

    private static final int RENDERER_INDEX_TEXT = 2;

    private static final long PROGRESS_REPORT_PERIOD_MS = 1000;

    private static final TrackSelectionArray EMPTY_TRACK_SELECTION_ARRAY = new TrackSelectionArray(null, null, null);

    private static final long[] EMPTY_TRACK_ID_ARRAY = new long[0];

    private final CastContext castContext;

    private final CastTimelineTracker timelineTracker;

    private final Timeline.Period period;

    private final StatusListener statusListener;

    private final SeekResultCallback seekResultCallback;

    private final CopyOnWriteArrayList<ListenerHolder> listeners;

    private final ArrayList<ListenerNotificationTask> notificationsBatch;

    private final ArrayDeque<ListenerNotificationTask> ongoingNotificationsTasks;

    @Nullable
private SessionAvailabilityListener sessionAvailabilityListener;

    @Nullable
private RemoteMediaClient remoteMediaClient;

    private CastTimeline currentTimeline;

    private TrackGroupArray currentTrackGroups;

    private TrackSelectionArray currentTrackSelection;

        private int playbackState;

    private final StateHolder<Integer> repeatMode;

    private int currentWindowIndex;

    private final StateHolder<Boolean> playWhenReady;

    private long lastReportedPositionMs;

    private int pendingSeekCount;

    private int pendingSeekWindowIndex;

    private long pendingSeekPositionMs;

    private boolean waitingForInitialTimeline;

    public CastPlayer(CastContext castContext) {
        this.castContext = castContext;
        timelineTracker = new CastTimelineTracker();
        period = new Timeline.Period();
        statusListener = new StatusListener();
        seekResultCallback = new SeekResultCallback();
        listeners = new CopyOnWriteArrayList<>();
        notificationsBatch = new ArrayList<>();
        ongoingNotificationsTasks = new ArrayDeque<>();
<<<<<<< MINE
        playWhenReady = new StateHolder<>(false);
        repeatMode = new StateHolder<>(REPEAT_MODE_OFF);
=======
        SessionManager sessionManager = castContext.getSessionManager();
        sessionManager.addSessionManagerListener(statusListener, CastSession.class);
        CastSession session = sessionManager.getCurrentCastSession();
        remoteMediaClient = session != null ? session.getRemoteMediaClient() : null;
>>>>>>> YOURS
        playbackState = STATE_IDLE;
        currentTimeline = CastTimeline.EMPTY_CAST_TIMELINE;
        currentTrackGroups = TrackGroupArray.EMPTY;
        currentTrackSelection = EMPTY_TRACK_SELECTION_ARRAY;
        pendingSeekWindowIndex = C.INDEX_UNSET;
        pendingSeekPositionMs = C.TIME_UNSET;
        SessionManager sessionManager = castContext.getSessionManager();
        sessionManager.addSessionManagerListener(statusListener, CastSession.class);
        CastSession session = sessionManager.getCurrentCastSession();
        setRemoteMediaClient(session != null ? session.getRemoteMediaClient() : null);
        updateInternalStateAndNotifyIfChanged();
    }

    @Nullable
public PendingResult<MediaChannelResult> loadItem(MediaQueueItem item, long positionMs) {
        return loadItems(new MediaQueueItem[] { item }, 0, positionMs, REPEAT_MODE_OFF);
    }

    @Nullable
public PendingResult<MediaChannelResult> loadItems(MediaQueueItem[] items, int startIndex, long positionMs, @RepeatMode int repeatMode) {
        if (remoteMediaClient != null) {
            positionMs = positionMs != C.TIME_UNSET ? positionMs : 0;
            waitingForInitialTimeline = true;
            return remoteMediaClient.queueLoad(items, startIndex, getCastRepeatMode(repeatMode), positionMs, null);
        }
        return null;
    }

    @Nullable
public PendingResult<MediaChannelResult> addItems(MediaQueueItem... items) {
        return addItems(MediaQueueItem.INVALID_ITEM_ID, items);
    }

    @Nullable
public PendingResult<MediaChannelResult> addItems(int periodId, MediaQueueItem... items) {
        if (getMediaStatus() != null && (periodId == MediaQueueItem.INVALID_ITEM_ID || currentTimeline.getIndexOfPeriod(periodId) != C.INDEX_UNSET)) {
            return remoteMediaClient.queueInsertItems(items, periodId, null);
        }
        return null;
    }

    @Nullable
public PendingResult<MediaChannelResult> removeItem(int periodId) {
        if (getMediaStatus() != null && currentTimeline.getIndexOfPeriod(periodId) != C.INDEX_UNSET) {
            return remoteMediaClient.queueRemoveItem(periodId, null);
        }
        return null;
    }

    @Nullable
public PendingResult<MediaChannelResult> moveItem(int periodId, int newIndex) {
        Assertions.checkArgument(newIndex >= 0 && newIndex < currentTimeline.getPeriodCount());
        if (getMediaStatus() != null && currentTimeline.getIndexOfPeriod(periodId) != C.INDEX_UNSET) {
            return remoteMediaClient.queueMoveItemToNewIndex(periodId, newIndex, null);
        }
        return null;
    }

    @Nullable
public MediaQueueItem getItem(int periodId) {
        MediaStatus mediaStatus = getMediaStatus();
        return mediaStatus != null && currentTimeline.getIndexOfPeriod(periodId) != C.INDEX_UNSET ? mediaStatus.getItemById(periodId) : null;
    }

    public boolean isCastSessionAvailable() {
        return remoteMediaClient != null;
    }

    public void setSessionAvailabilityListener(@Nullable SessionAvailabilityListener listener) {
        sessionAvailabilityListener = listener;
    }

    @Override
    @Nullable
    public AudioComponent getAudioComponent() {
        return null;
    }

    @Override
    @Nullable
    public VideoComponent getVideoComponent() {
        return null;
    }

    @Override
    @Nullable
    public TextComponent getTextComponent() {
        return null;
    }

    @Override
    @Nullable
    public MetadataComponent getMetadataComponent() {
        return null;
    }

    @Override
    public Looper getApplicationLooper() {
        return Looper.getMainLooper();
    }

    @Override
    public void addListener(EventListener listener) {
        listeners.addIfAbsent(new ListenerHolder(listener));
    }

    @Override
    public void removeListener(EventListener listener) {
        for (ListenerHolder listenerHolder : listeners) {
            if (listenerHolder.listener.equals(listener)) {
                listenerHolder.release();
                listeners.remove(listenerHolder);
            }
        }
    }

    @Override
    public int getPlaybackState() {
        return playbackState;
    }

    @Override
    @PlaybackSuppressionReason
    public int getPlaybackSuppressionReason() {
        return Player.PLAYBACK_SUPPRESSION_REASON_NONE;
    }

    @Override
    @Nullable
    public ExoPlaybackException getPlaybackError() {
        return null;
    }

    @Override
    public void setPlayWhenReady(boolean playWhenReady) {
        if (remoteMediaClient == null) {
            return;
        }
        setPlayerStateAndNotifyIfChanged(playWhenReady, playbackState);
        flushNotifications();
        PendingResult<MediaChannelResult> pendingResult = playWhenReady ? remoteMediaClient.play() : remoteMediaClient.pause();
        this.playWhenReady.pendingResultCallback = new ResultCallback<MediaChannelResult>() {

            @Override
            public void onResult(MediaChannelResult mediaChannelResult) {
                if (remoteMediaClient != null) {
                    updatePlayerStateAndNotifyIfChanged(this);
                    flushNotifications();
        }
    }
        };
        pendingResult.setResultCallback(this.playWhenReady.pendingResultCallback);
    }

    @Override
    public boolean getPlayWhenReady() {
        return playWhenReady.value;
    }

    @Override
    public void seekTo(int windowIndex, long positionMs) {
        MediaStatus mediaStatus = getMediaStatus();
        positionMs = positionMs != C.TIME_UNSET ? positionMs : 0;
        if (mediaStatus != null) {
            if (getCurrentWindowIndex() != windowIndex) {
                remoteMediaClient.queueJumpToItem((int) currentTimeline.getPeriod(windowIndex, period).uid, positionMs, null).setResultCallback(seekResultCallback);
            } else {
                remoteMediaClient.seek(positionMs).setResultCallback(seekResultCallback);
            }
            pendingSeekCount++;
            pendingSeekWindowIndex = windowIndex;
            pendingSeekPositionMs = positionMs;
            notificationsBatch.add(new ListenerNotificationTask(listener -> listener.onPositionDiscontinuity(DISCONTINUITY_REASON_SEEK)));
        } else if (pendingSeekCount == 0) {
            notificationsBatch.add(new ListenerNotificationTask(EventListener::onSeekProcessed));
            }
        flushNotifications();
    }

    @Override
    public void setPlaybackParameters(@Nullable PlaybackParameters playbackParameters) {
    }

    @Override
    public PlaybackParameters getPlaybackParameters() {
        return PlaybackParameters.DEFAULT;
    }

    @Override
    public void stop(boolean reset) {
        playbackState = STATE_IDLE;
        if (remoteMediaClient != null) {
            remoteMediaClient.stop();
        }
    }

    @Override
    public void release() {
        SessionManager sessionManager = castContext.getSessionManager();
        sessionManager.removeSessionManagerListener(statusListener, CastSession.class);
        sessionManager.endCurrentSession(false);
    }

    @Override
    public int getRendererCount() {
        return RENDERER_COUNT;
    }

    @Override
    public int getRendererType(int index) {
        switch(index) {
            case RENDERER_INDEX_VIDEO:
                return C.TRACK_TYPE_VIDEO;
            case RENDERER_INDEX_AUDIO:
                return C.TRACK_TYPE_AUDIO;
            case RENDERER_INDEX_TEXT:
                return C.TRACK_TYPE_TEXT;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public void setRepeatMode(@RepeatMode int repeatMode) {
        if (remoteMediaClient == null) {
            return;
        }
        setRepeatModeAndNotifyIfChanged(repeatMode);
        flushNotifications();
        PendingResult<MediaChannelResult> pendingResult = remoteMediaClient.queueSetRepeatMode(getCastRepeatMode(repeatMode), null);
        this.repeatMode.pendingResultCallback = new ResultCallback<MediaChannelResult>() {

            @Override
            public void onResult(MediaChannelResult mediaChannelResult) {
                if (remoteMediaClient != null) {
                    updateRepeatModeAndNotifyIfChanged(this);
                    flushNotifications();
                }
            }
        };
        pendingResult.setResultCallback(this.repeatMode.pendingResultCallback);
    }

    @Override
    @RepeatMode
    public int getRepeatMode() {
        return repeatMode.value;
    }

    @Override
    public void setShuffleModeEnabled(boolean shuffleModeEnabled) {
    }

    @Override
    public boolean getShuffleModeEnabled() {
        return false;
    }

    @Override
    public TrackSelectionArray getCurrentTrackSelections() {
        return currentTrackSelection;
    }

    @Override
    public TrackGroupArray getCurrentTrackGroups() {
        return currentTrackGroups;
    }

    @Override
    public Timeline getCurrentTimeline() {
        return currentTimeline;
    }

    @Override
    public int getCurrentPeriodIndex() {
        return getCurrentWindowIndex();
    }

    @Override
    public int getCurrentWindowIndex() {
        return pendingSeekWindowIndex != C.INDEX_UNSET ? pendingSeekWindowIndex : currentWindowIndex;
    }

    @Override
    public long getDuration() {
        return getContentDuration();
    }

    @Override
    public long getCurrentPosition() {
        return pendingSeekPositionMs != C.TIME_UNSET ? pendingSeekPositionMs : remoteMediaClient != null ? remoteMediaClient.getApproximateStreamPosition() : lastReportedPositionMs;
    }

    @Override
    public long getBufferedPosition() {
        return getCurrentPosition();
    }

    @Override
    public long getTotalBufferedDuration() {
        long bufferedPosition = getBufferedPosition();
        long currentPosition = getCurrentPosition();
        return bufferedPosition == C.TIME_UNSET || currentPosition == C.TIME_UNSET ? 0 : bufferedPosition - currentPosition;
    }

    @Override
    public boolean isPlayingAd() {
        return false;
    }

    @Override
    public int getCurrentAdGroupIndex() {
        return C.INDEX_UNSET;
    }

    @Override
    public int getCurrentAdIndexInAdGroup() {
        return C.INDEX_UNSET;
    }

    @Override
    public boolean isLoading() {
        return false;
    }

    @Override
    public long getContentPosition() {
        return getCurrentPosition();
    }

    @Override
    public long getContentBufferedPosition() {
        return getBufferedPosition();
    }

    private void updateInternalStateAndNotifyIfChanged() {
        if (remoteMediaClient == null) {
            return;
        }
        boolean wasPlaying = playbackState == Player.STATE_READY && playWhenReady.value;
        updatePlayerStateAndNotifyIfChanged(null);
        boolean isPlaying = playbackState == Player.STATE_READY && playWhenReady.value;
        if (wasPlaying != isPlaying) {
            notificationsBatch.add(new ListenerNotificationTask(listener -> listener.onIsPlayingChanged(isPlaying)));
        }
        updateRepeatModeAndNotifyIfChanged(null);
        updateTimelineAndNotifyIfChanged();
        int currentWindowIndex = C.INDEX_UNSET;
        MediaQueueItem currentItem = remoteMediaClient.getCurrentItem();
        if (currentItem != null) {
            currentWindowIndex = currentTimeline.getIndexOfPeriod(currentItem.getItemId());
        }
        if (currentWindowIndex == C.INDEX_UNSET) {
            currentWindowIndex = 0;
        }
        if (this.currentWindowIndex != currentWindowIndex && pendingSeekCount == 0) {
            this.currentWindowIndex = currentWindowIndex;
            notificationsBatch.add(new ListenerNotificationTask(listener -> listener.onPositionDiscontinuity(DISCONTINUITY_REASON_PERIOD_TRANSITION)));
        }
        if (updateTracksAndSelectionsAndNotifyIfChanged()) {
            notificationsBatch.add(new ListenerNotificationTask(listener -> listener.onTracksChanged(currentTrackGroups, currentTrackSelection)));
        }
        flushNotifications();
    }

    @RequiresNonNull("remoteMediaClient")
    private void updatePlayerStateAndNotifyIfChanged(@Nullable ResultCallback<?> resultCallback) {
        boolean newPlayWhenReadyValue = playWhenReady.value;
        if (playWhenReady.acceptsUpdate(resultCallback)) {
            newPlayWhenReadyValue = !remoteMediaClient.isPaused();
            playWhenReady.clearPendingResultCallback();
        }
        setPlayerStateAndNotifyIfChanged(newPlayWhenReadyValue, fetchPlaybackState(remoteMediaClient));
    }

    @RequiresNonNull("remoteMediaClient")
    private void updateRepeatModeAndNotifyIfChanged(@Nullable ResultCallback<?> resultCallback) {
        if (repeatMode.acceptsUpdate(resultCallback)) {
            setRepeatModeAndNotifyIfChanged(fetchRepeatMode(remoteMediaClient));
            repeatMode.clearPendingResultCallback();
        }
    }

    private void updateTimelineAndNotifyIfChanged() {
        if (updateTimeline()) {
            @Player.TimelineChangeReason
            int reason = waitingForInitialTimeline ? Player.TIMELINE_CHANGE_REASON_PREPARED : Player.TIMELINE_CHANGE_REASON_DYNAMIC;
            waitingForInitialTimeline = false;
<<<<<<< MINE
            notificationsBatch.add(new ListenerNotificationTask(listener -> listener.onTimelineChanged(currentTimeline, reason)));
=======
            notificationsBatch.add(new ListenerNotificationTask(listener -> listener.onTimelineChanged(currentTimeline, null, reason)));
>>>>>>> YOURS
        }
    }<<<<<<< MINE
=======
private void updateInternalState() {
        if (remoteMediaClient == null) {
            return;
        }
        boolean wasPlaying = playbackState == Player.STATE_READY && playWhenReady;
        int playbackState = fetchPlaybackState(remoteMediaClient);
        boolean playWhenReady = !remoteMediaClient.isPaused();
        if (this.playbackState != playbackState || this.playWhenReady != playWhenReady) {
            this.playbackState = playbackState;
            this.playWhenReady = playWhenReady;
            notificationsBatch.add(new ListenerNotificationTask(listener -> listener.onPlayerStateChanged(this.playWhenReady, this.playbackState)));
        }
        boolean isPlaying = playbackState == Player.STATE_READY && playWhenReady;
        if (wasPlaying != isPlaying) {
            notificationsBatch.add(new ListenerNotificationTask(listener -> listener.onIsPlayingChanged(isPlaying)));
        }
        @RepeatMode
        int repeatMode = fetchRepeatMode(remoteMediaClient);
        if (this.repeatMode != repeatMode) {
            this.repeatMode = repeatMode;
            notificationsBatch.add(new ListenerNotificationTask(listener -> listener.onRepeatModeChanged(this.repeatMode)));
        }
        maybeUpdateTimelineAndNotify();
        int currentWindowIndex = C.INDEX_UNSET;
        MediaQueueItem currentItem = remoteMediaClient.getCurrentItem();
        if (currentItem != null) {
            currentWindowIndex = currentTimeline.getIndexOfPeriod(currentItem.getItemId());
        }
        if (currentWindowIndex == C.INDEX_UNSET) {
            currentWindowIndex = 0;
        }
        if (this.currentWindowIndex != currentWindowIndex && pendingSeekCount == 0) {
            this.currentWindowIndex = currentWindowIndex;
            notificationsBatch.add(new ListenerNotificationTask(listener -> listener.onPositionDiscontinuity(DISCONTINUITY_REASON_PERIOD_TRANSITION)));
        }
        if (updateTracksAndSelections()) {
            notificationsBatch.add(new ListenerNotificationTask(listener -> listener.onTracksChanged(currentTrackGroups, currentTrackSelection)));
        }
        flushNotifications();
    }
>>>>>>> YOURS


    private boolean updateTimeline() {
        CastTimeline oldTimeline = currentTimeline;
        MediaStatus status = getMediaStatus();
        currentTimeline = status != null ? timelineTracker.getCastTimeline(remoteMediaClient) : CastTimeline.EMPTY_CAST_TIMELINE;
        return !oldTimeline.equals(currentTimeline);
    }

    private boolean updateTracksAndSelectionsAndNotifyIfChanged() {
        if (remoteMediaClient == null) {
            return false;
        }
        MediaStatus mediaStatus = getMediaStatus();
        MediaInfo mediaInfo = mediaStatus != null ? mediaStatus.getMediaInfo() : null;
        List<MediaTrack> castMediaTracks = mediaInfo != null ? mediaInfo.getMediaTracks() : null;
        if (castMediaTracks == null || castMediaTracks.isEmpty()) {
            boolean hasChanged = !currentTrackGroups.isEmpty();
            currentTrackGroups = TrackGroupArray.EMPTY;
            currentTrackSelection = EMPTY_TRACK_SELECTION_ARRAY;
            return hasChanged;
        }
        long[] activeTrackIds = mediaStatus.getActiveTrackIds();
        if (activeTrackIds == null) {
            activeTrackIds = EMPTY_TRACK_ID_ARRAY;
        }
        TrackGroup[] trackGroups = new TrackGroup[castMediaTracks.size()];
        TrackSelection[] trackSelections = new TrackSelection[RENDERER_COUNT];
        for (int i = 0; i < castMediaTracks.size(); i++) {
            MediaTrack mediaTrack = castMediaTracks.get(i);
            trackGroups[i] = new TrackGroup(CastUtils.mediaTrackToFormat(mediaTrack));
            long id = mediaTrack.getId();
            int trackType = MimeTypes.getTrackType(mediaTrack.getContentType());
            int rendererIndex = getRendererIndexForTrackType(trackType);
            if (isTrackActive(id, activeTrackIds) && rendererIndex != C.INDEX_UNSET && trackSelections[rendererIndex] == null) {
                trackSelections[rendererIndex] = new FixedTrackSelection(trackGroups[i], 0);
            }
        }
        TrackGroupArray newTrackGroups = new TrackGroupArray(trackGroups);
        TrackSelectionArray newTrackSelections = new TrackSelectionArray(trackSelections);
        if (!newTrackGroups.equals(currentTrackGroups) || !newTrackSelections.equals(currentTrackSelection)) {
            currentTrackSelection = new TrackSelectionArray(trackSelections);
            currentTrackGroups = new TrackGroupArray(trackGroups);
            return true;
        }
        return false;
    }

    private void setRepeatModeAndNotifyIfChanged(@Player.RepeatMode int repeatMode) {
        if (this.repeatMode.value != repeatMode) {
            this.repeatMode.value = repeatMode;
            notificationsBatch.add(new ListenerNotificationTask(listener -> listener.onRepeatModeChanged(repeatMode)));
        }
    }

    private void setPlayerStateAndNotifyIfChanged(boolean playWhenReady, @Player.State int playbackState) {
        if (this.playWhenReady.value != playWhenReady || this.playbackState != playbackState) {
            this.playWhenReady.value = playWhenReady;
            this.playbackState = playbackState;
            notificationsBatch.add(new ListenerNotificationTask(listener -> listener.onPlayerStateChanged(playWhenReady, playbackState)));
        }
    }

    private void setRemoteMediaClient(@Nullable RemoteMediaClient remoteMediaClient) {
        if (this.remoteMediaClient == remoteMediaClient) {
            return;
        }
        if (this.remoteMediaClient != null) {
            this.remoteMediaClient.removeListener(statusListener);
            this.remoteMediaClient.removeProgressListener(statusListener);
        }
        this.remoteMediaClient = remoteMediaClient;
        if (remoteMediaClient != null) {
            if (sessionAvailabilityListener != null) {
                sessionAvailabilityListener.onCastSessionAvailable();
            }
            remoteMediaClient.addListener(statusListener);
            remoteMediaClient.addProgressListener(statusListener, PROGRESS_REPORT_PERIOD_MS);
            updateInternalStateAndNotifyIfChanged();
        } else {
            if (sessionAvailabilityListener != null) {
                sessionAvailabilityListener.onCastSessionUnavailable();
            }
        }
    }

    @Nullable
    private MediaStatus getMediaStatus() {
        return remoteMediaClient != null ? remoteMediaClient.getMediaStatus() : null;
    }

    private static int fetchPlaybackState(RemoteMediaClient remoteMediaClient) {
        int receiverAppStatus = remoteMediaClient.getPlayerState();
        switch(receiverAppStatus) {
            case MediaStatus.PLAYER_STATE_BUFFERING:
                return STATE_BUFFERING;
            case MediaStatus.PLAYER_STATE_PLAYING:
            case MediaStatus.PLAYER_STATE_PAUSED:
                return STATE_READY;
            case MediaStatus.PLAYER_STATE_IDLE:
            case MediaStatus.PLAYER_STATE_UNKNOWN:
            default:
                return STATE_IDLE;
        }
    }

    @RepeatMode
    private static int fetchRepeatMode(RemoteMediaClient remoteMediaClient) {
        MediaStatus mediaStatus = remoteMediaClient.getMediaStatus();
        if (mediaStatus == null) {
            return REPEAT_MODE_OFF;
        }
        int castRepeatMode = mediaStatus.getQueueRepeatMode();
        switch(castRepeatMode) {
            case MediaStatus.REPEAT_MODE_REPEAT_SINGLE:
                return REPEAT_MODE_ONE;
            case MediaStatus.REPEAT_MODE_REPEAT_ALL:
            case MediaStatus.REPEAT_MODE_REPEAT_ALL_AND_SHUFFLE:
                return REPEAT_MODE_ALL;
            case MediaStatus.REPEAT_MODE_REPEAT_OFF:
                return REPEAT_MODE_OFF;
            default:
                throw new IllegalStateException();
        }
    }

    private static boolean isTrackActive(long id, long[] activeTrackIds) {
        for (long activeTrackId : activeTrackIds) {
            if (activeTrackId == id) {
                return true;
            }
        }
        return false;
    }

    private static int getRendererIndexForTrackType(int trackType) {
        return trackType == C.TRACK_TYPE_VIDEO ? RENDERER_INDEX_VIDEO : trackType == C.TRACK_TYPE_AUDIO ? RENDERER_INDEX_AUDIO : trackType == C.TRACK_TYPE_TEXT ? RENDERER_INDEX_TEXT : C.INDEX_UNSET;
    }

    private static int getCastRepeatMode(@RepeatMode int repeatMode) {
        switch(repeatMode) {
            case REPEAT_MODE_ONE:
                return MediaStatus.REPEAT_MODE_REPEAT_SINGLE;
            case REPEAT_MODE_ALL:
                return MediaStatus.REPEAT_MODE_REPEAT_ALL;
            case REPEAT_MODE_OFF:
                return MediaStatus.REPEAT_MODE_REPEAT_OFF;
            default:
                throw new IllegalArgumentException();
        }
    }

    private final class StatusListener implements RemoteMediaClient.Listener, SessionManagerListener<CastSession>, RemoteMediaClient.ProgressListener {

        @Override
        public void onProgressUpdated(long progressMs, long unusedDurationMs) {
            lastReportedPositionMs = progressMs;
        }

        @Override
        public void onStatusUpdated() {
            updateInternalStateAndNotifyIfChanged();
        }

        @Override
        public void onMetadataUpdated() {
        }

        @Override
        public void onQueueStatusUpdated() {
            updateTimelineAndNotifyIfChanged();
        }

        @Override
        public void onPreloadStatusUpdated() {
        }

        @Override
        public void onSendingRemoteMediaRequest() {
        }

        @Override
        public void onAdBreakStatusUpdated() {
        }

        @Override
        public void onSessionStarted(CastSession castSession, String s) {
            setRemoteMediaClient(castSession.getRemoteMediaClient());
        }

        @Override
        public void onSessionResumed(CastSession castSession, boolean b) {
            setRemoteMediaClient(castSession.getRemoteMediaClient());
        }

        @Override
        public void onSessionEnded(CastSession castSession, int i) {
            setRemoteMediaClient(null);
        }

        @Override
        public void onSessionSuspended(CastSession castSession, int i) {
            setRemoteMediaClient(null);
        }

        @Override
        public void onSessionResumeFailed(CastSession castSession, int statusCode) {
            Log.e(TAG, "Session resume failed. Error code " + statusCode + ": " + CastUtils.getLogString(statusCode));
        }

        @Override
        public void onSessionStarting(CastSession castSession) {
        }

        @Override
        public void onSessionStartFailed(CastSession castSession, int statusCode) {
            Log.e(TAG, "Session start failed. Error code " + statusCode + ": " + CastUtils.getLogString(statusCode));
        }

        @Override
        public void onSessionEnding(CastSession castSession) {
        }

        @Override
        public void onSessionResuming(CastSession castSession, String s) {
        }
    }

    private void flushNotifications() {
        boolean recursiveNotification = !ongoingNotificationsTasks.isEmpty();
        ongoingNotificationsTasks.addAll(notificationsBatch);
        notificationsBatch.clear();
        if (recursiveNotification) {
            return;
        }
        while (!ongoingNotificationsTasks.isEmpty()) {
            ongoingNotificationsTasks.peekFirst().execute();
            ongoingNotificationsTasks.removeFirst();
        }
    }

    private final class SeekResultCallback implements ResultCallback<MediaChannelResult> {

        @Override
        public void onResult(MediaChannelResult result) {
            int statusCode = result.getStatus().getStatusCode();
            if (statusCode != CastStatusCodes.SUCCESS && statusCode != CastStatusCodes.REPLACED) {
                Log.e(TAG, "Seek failed. Error code " + statusCode + ": " + CastUtils.getLogString(statusCode));
            }
            if (--pendingSeekCount == 0) {
                pendingSeekWindowIndex = C.INDEX_UNSET;
                pendingSeekPositionMs = C.TIME_UNSET;
                notificationsBatch.add(new ListenerNotificationTask(EventListener::onSeekProcessed));
                flushNotifications();
            }
        }
    }

    private static final class StateHolder<T> {

        public T value;

        @Nullable
        public ResultCallback<MediaChannelResult> pendingResultCallback;

        public StateHolder(T initialValue) {
            value = initialValue;
        }

        public void clearPendingResultCallback() {
            pendingResultCallback = null;
        }

        public boolean acceptsUpdate(@Nullable ResultCallback<?> resultCallback) {
            return pendingResultCallback == resultCallback;
        }
    }

    private final class ListenerNotificationTask {

        private final Iterator<ListenerHolder> listenersSnapshot;

        private final ListenerInvocation listenerInvocation;

        private ListenerNotificationTask(ListenerInvocation listenerInvocation) {
            this.listenersSnapshot = listeners.iterator();
            this.listenerInvocation = listenerInvocation;
        }

        public void execute() {
            while (listenersSnapshot.hasNext()) {
                listenersSnapshot.next().invoke(listenerInvocation);
            }
        }
    }
}