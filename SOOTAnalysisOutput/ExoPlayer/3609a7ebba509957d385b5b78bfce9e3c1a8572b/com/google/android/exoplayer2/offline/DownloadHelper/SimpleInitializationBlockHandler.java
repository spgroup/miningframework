package com.google.android.exoplayer2.offline;

import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import androidx.annotation.Nullable;
import android.util.SparseIntArray;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.source.MediaPeriod;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSource.MediaPeriodId;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.BaseTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.Parameters;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.SelectionOverride;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectorResult;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSource.Factory;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Util;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.checkerframework.checker.nullness.compatqual.NullableType;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;

public final class DownloadHelper {

    public static final DefaultTrackSelector.Parameters DEFAULT_TRACK_SELECTOR_PARAMETERS = new DefaultTrackSelector.ParametersBuilder().setForceHighestSupportedBitrate(true).build();

    public interface Callback {

        void onPrepared(DownloadHelper helper);

        void onPrepareError(DownloadHelper helper, IOException e);
    }

    private static final MediaSourceFactory DASH_FACTORY = getMediaSourceFactory("com.google.android.exoplayer2.source.dash.DashMediaSource$Factory");

    private static final MediaSourceFactory SS_FACTORY = getMediaSourceFactory("com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource$Factory");

    private static final MediaSourceFactory HLS_FACTORY = getMediaSourceFactory("com.google.android.exoplayer2.source.hls.HlsMediaSource$Factory");

    public static DownloadHelper forProgressive(Uri uri) {
        return forProgressive(uri, null);
    }

    public static DownloadHelper forProgressive(Uri uri, @Nullable String cacheKey) {
        return new DownloadHelper(DownloadRequest.TYPE_PROGRESSIVE, uri, cacheKey, null, DEFAULT_TRACK_SELECTOR_PARAMETERS, new RendererCapabilities[0]);
    }

    public static DownloadHelper forDash(Uri uri, DataSource.Factory dataSourceFactory, RenderersFactory renderersFactory) {
        return forDash(uri, dataSourceFactory, renderersFactory, null, DEFAULT_TRACK_SELECTOR_PARAMETERS);
    }

    public static DownloadHelper forDash(Uri uri, DataSource.Factory dataSourceFactory, RenderersFactory renderersFactory, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, DefaultTrackSelector.Parameters trackSelectorParameters) {
        return new DownloadHelper(DownloadRequest.TYPE_DASH, uri, null, DASH_FACTORY.createMediaSource(uri, dataSourceFactory, null), trackSelectorParameters, Util.getRendererCapabilities(renderersFactory, drmSessionManager));
    }

    public static DownloadHelper forHls(Uri uri, DataSource.Factory dataSourceFactory, RenderersFactory renderersFactory) {
        return forHls(uri, dataSourceFactory, renderersFactory, null, DEFAULT_TRACK_SELECTOR_PARAMETERS);
    }

    public static DownloadHelper forHls(Uri uri, DataSource.Factory dataSourceFactory, RenderersFactory renderersFactory, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, DefaultTrackSelector.Parameters trackSelectorParameters) {
        return new DownloadHelper(DownloadRequest.TYPE_HLS, uri, null, HLS_FACTORY.createMediaSource(uri, dataSourceFactory, null), trackSelectorParameters, Util.getRendererCapabilities(renderersFactory, drmSessionManager));
    }

    public static DownloadHelper forSmoothStreaming(Uri uri, DataSource.Factory dataSourceFactory, RenderersFactory renderersFactory) {
        return forSmoothStreaming(uri, dataSourceFactory, renderersFactory, null, DEFAULT_TRACK_SELECTOR_PARAMETERS);
    }

    public static DownloadHelper forSmoothStreaming(Uri uri, DataSource.Factory dataSourceFactory, RenderersFactory renderersFactory, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, DefaultTrackSelector.Parameters trackSelectorParameters) {
        return new DownloadHelper(DownloadRequest.TYPE_SS, uri, null, SS_FACTORY.createMediaSource(uri, dataSourceFactory, null), trackSelectorParameters, Util.getRendererCapabilities(renderersFactory, drmSessionManager));
    }

    public static MediaSource createMediaSource(DownloadRequest downloadRequest, DataSource.Factory dataSourceFactory) {
        MediaSourceFactory factory;
        switch(downloadRequest.type) {
            case DownloadRequest.TYPE_DASH:
                factory = DASH_FACTORY;
                break;
            case DownloadRequest.TYPE_SS:
                factory = SS_FACTORY;
                break;
            case DownloadRequest.TYPE_HLS:
                factory = HLS_FACTORY;
                break;
            case DownloadRequest.TYPE_PROGRESSIVE:
                return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(downloadRequest.uri);
            default:
                throw new IllegalStateException("Unsupported type: " + downloadRequest.type);
        }
        return factory.createMediaSource(downloadRequest.uri, dataSourceFactory, downloadRequest.streamKeys);
    }

    private final String downloadType;

    private final Uri uri;

    @Nullable
    private final String cacheKey;

    @Nullable
    private final MediaSource mediaSource;

    private final DefaultTrackSelector trackSelector;

    private final RendererCapabilities[] rendererCapabilities;

    private final SparseIntArray scratchSet;

    private final Handler callbackHandler;

    private boolean isPreparedWithMedia;

    @MonotonicNonNull
    private Callback callback;

    @MonotonicNonNull
    private MediaPreparer mediaPreparer;

    private TrackGroupArray @MonotonicNonNull [] trackGroupArrays;

    private MappedTrackInfo @MonotonicNonNull [] mappedTrackInfos;

    private List<TrackSelection> @MonotonicNonNull [][] trackSelectionsByPeriodAndRenderer;

    private List<TrackSelection> @MonotonicNonNull [][] immutableTrackSelectionsByPeriodAndRenderer;

    public DownloadHelper(String downloadType, Uri uri, @Nullable String cacheKey, @Nullable MediaSource mediaSource, DefaultTrackSelector.Parameters trackSelectorParameters, RendererCapabilities[] rendererCapabilities) {
        this.downloadType = downloadType;
        this.uri = uri;
        this.cacheKey = cacheKey;
        this.mediaSource = mediaSource;
        this.trackSelector = new DefaultTrackSelector(new DownloadTrackSelection.Factory());
        this.rendererCapabilities = rendererCapabilities;
        this.scratchSet = new SparseIntArray();
        trackSelector.setParameters(trackSelectorParameters);
        trackSelector.init(() -> {
        }, new DummyBandwidthMeter());
        callbackHandler = new Handler(Util.getLooper());
    }

    public void prepare(Callback callback) {
        Assertions.checkState(this.callback == null);
        this.callback = callback;
        if (mediaSource != null) {
            mediaPreparer = new MediaPreparer(mediaSource, this);
        } else {
            callbackHandler.post(() -> callback.onPrepared(this));
        }
    }

    public void release() {
        if (mediaPreparer != null) {
            mediaPreparer.release();
        }
    }

    @Nullable
    public Object getManifest() {
        if (mediaSource == null) {
            return null;
        }
        assertPreparedWithMedia();
        return mediaPreparer.manifest;
    }

    public int getPeriodCount() {
        if (mediaSource == null) {
            return 0;
        }
        assertPreparedWithMedia();
        return trackGroupArrays.length;
    }

    public TrackGroupArray getTrackGroups(int periodIndex) {
        assertPreparedWithMedia();
        return trackGroupArrays[periodIndex];
    }

    public MappedTrackInfo getMappedTrackInfo(int periodIndex) {
        assertPreparedWithMedia();
        return mappedTrackInfos[periodIndex];
    }

    public List<TrackSelection> getTrackSelections(int periodIndex, int rendererIndex) {
        assertPreparedWithMedia();
        return immutableTrackSelectionsByPeriodAndRenderer[periodIndex][rendererIndex];
    }

    public void clearTrackSelections(int periodIndex) {
        assertPreparedWithMedia();
        for (int i = 0; i < rendererCapabilities.length; i++) {
            trackSelectionsByPeriodAndRenderer[periodIndex][i].clear();
        }
    }

    public void replaceTrackSelections(int periodIndex, DefaultTrackSelector.Parameters trackSelectorParameters) {
        clearTrackSelections(periodIndex);
        addTrackSelection(periodIndex, trackSelectorParameters);
    }

    public void addTrackSelection(int periodIndex, DefaultTrackSelector.Parameters trackSelectorParameters) {
        assertPreparedWithMedia();
        trackSelector.setParameters(trackSelectorParameters);
        runTrackSelection(periodIndex);
    }

    public void addAudioLanguagesToSelection(String... languages) {
        assertPreparedWithMedia();
        for (int periodIndex = 0; periodIndex < mappedTrackInfos.length; periodIndex++) {
            DefaultTrackSelector.ParametersBuilder parametersBuilder = DEFAULT_TRACK_SELECTOR_PARAMETERS.buildUpon();
            MappedTrackInfo mappedTrackInfo = mappedTrackInfos[periodIndex];
            int rendererCount = mappedTrackInfo.getRendererCount();
            for (int rendererIndex = 0; rendererIndex < rendererCount; rendererIndex++) {
                if (mappedTrackInfo.getRendererType(rendererIndex) != C.TRACK_TYPE_AUDIO) {
                    parametersBuilder.setRendererDisabled(rendererIndex, true);
                }
            }
            for (String language : languages) {
                parametersBuilder.setPreferredAudioLanguage(language);
                addTrackSelection(periodIndex, parametersBuilder.build());
            }
        }
    }

    public void addTextLanguagesToSelection(boolean selectUndeterminedTextLanguage, String... languages) {
        assertPreparedWithMedia();
        for (int periodIndex = 0; periodIndex < mappedTrackInfos.length; periodIndex++) {
            DefaultTrackSelector.ParametersBuilder parametersBuilder = DEFAULT_TRACK_SELECTOR_PARAMETERS.buildUpon();
            MappedTrackInfo mappedTrackInfo = mappedTrackInfos[periodIndex];
            int rendererCount = mappedTrackInfo.getRendererCount();
            for (int rendererIndex = 0; rendererIndex < rendererCount; rendererIndex++) {
                if (mappedTrackInfo.getRendererType(rendererIndex) != C.TRACK_TYPE_TEXT) {
                    parametersBuilder.setRendererDisabled(rendererIndex, true);
                }
            }
            parametersBuilder.setSelectUndeterminedTextLanguage(selectUndeterminedTextLanguage);
            for (String language : languages) {
                parametersBuilder.setPreferredTextLanguage(language);
                addTrackSelection(periodIndex, parametersBuilder.build());
            }
        }
    }

    public void addTrackSelectionForSingleRenderer(int periodIndex, int rendererIndex, DefaultTrackSelector.Parameters trackSelectorParameters, List<SelectionOverride> overrides) {
        assertPreparedWithMedia();
        DefaultTrackSelector.ParametersBuilder builder = trackSelectorParameters.buildUpon();
        for (int i = 0; i < mappedTrackInfos[periodIndex].getRendererCount(); i++) {
            builder.setRendererDisabled(i, i != rendererIndex);
        }
        if (overrides.isEmpty()) {
            addTrackSelection(periodIndex, builder.build());
        } else {
            TrackGroupArray trackGroupArray = mappedTrackInfos[periodIndex].getTrackGroups(rendererIndex);
            for (int i = 0; i < overrides.size(); i++) {
                builder.setSelectionOverride(rendererIndex, trackGroupArray, overrides.get(i));
                addTrackSelection(periodIndex, builder.build());
            }
        }
    }

    public DownloadRequest getDownloadRequest(@Nullable byte[] data) {
        return getDownloadRequest(uri.toString(), data);
    }

    public DownloadRequest getDownloadRequest(String id, @Nullable byte[] data) {
        if (mediaSource == null) {
            return new DownloadRequest(id, downloadType, uri, Collections.emptyList(), cacheKey, data);
        }
        assertPreparedWithMedia();
        List<StreamKey> streamKeys = new ArrayList<>();
        List<TrackSelection> allSelections = new ArrayList<>();
        int periodCount = trackSelectionsByPeriodAndRenderer.length;
        for (int periodIndex = 0; periodIndex < periodCount; periodIndex++) {
            allSelections.clear();
            int rendererCount = trackSelectionsByPeriodAndRenderer[periodIndex].length;
            for (int rendererIndex = 0; rendererIndex < rendererCount; rendererIndex++) {
                allSelections.addAll(trackSelectionsByPeriodAndRenderer[periodIndex][rendererIndex]);
            }
            streamKeys.addAll(mediaPreparer.mediaPeriods[periodIndex].getStreamKeys(allSelections));
        }
        return new DownloadRequest(id, downloadType, uri, streamKeys, cacheKey, data);
    }

    @SuppressWarnings("unchecked")
    private void onMediaPrepared() {
        Assertions.checkNotNull(mediaPreparer);
        Assertions.checkNotNull(mediaPreparer.mediaPeriods);
        Assertions.checkNotNull(mediaPreparer.timeline);
        int periodCount = mediaPreparer.mediaPeriods.length;
        int rendererCount = rendererCapabilities.length;
        trackSelectionsByPeriodAndRenderer = (List<TrackSelection>[][]) new List<?>[periodCount][rendererCount];
        immutableTrackSelectionsByPeriodAndRenderer = (List<TrackSelection>[][]) new List<?>[periodCount][rendererCount];
        for (int i = 0; i < periodCount; i++) {
            for (int j = 0; j < rendererCount; j++) {
                trackSelectionsByPeriodAndRenderer[i][j] = new ArrayList<>();
                immutableTrackSelectionsByPeriodAndRenderer[i][j] = Collections.unmodifiableList(trackSelectionsByPeriodAndRenderer[i][j]);
            }
        }
        trackGroupArrays = new TrackGroupArray[periodCount];
        mappedTrackInfos = new MappedTrackInfo[periodCount];
        for (int i = 0; i < periodCount; i++) {
            trackGroupArrays[i] = mediaPreparer.mediaPeriods[i].getTrackGroups();
            TrackSelectorResult trackSelectorResult = runTrackSelection(i);
            trackSelector.onSelectionActivated(trackSelectorResult.info);
            mappedTrackInfos[i] = Assertions.checkNotNull(trackSelector.getCurrentMappedTrackInfo());
        }
        setPreparedWithMedia();
        Assertions.checkNotNull(callbackHandler).post(() -> Assertions.checkNotNull(callback).onPrepared(this));
    }

    private void onMediaPreparationFailed(IOException error) {
        Assertions.checkNotNull(callbackHandler).post(() -> Assertions.checkNotNull(callback).onPrepareError(this, error));
    }

    @RequiresNonNull({ "trackGroupArrays", "mappedTrackInfos", "trackSelectionsByPeriodAndRenderer", "immutableTrackSelectionsByPeriodAndRenderer", "mediaPreparer", "mediaPreparer.timeline", "mediaPreparer.mediaPeriods" })
    private void setPreparedWithMedia() {
        isPreparedWithMedia = true;
    }

    @EnsuresNonNull({ "trackGroupArrays", "mappedTrackInfos", "trackSelectionsByPeriodAndRenderer", "immutableTrackSelectionsByPeriodAndRenderer", "mediaPreparer", "mediaPreparer.timeline", "mediaPreparer.mediaPeriods" })
    @SuppressWarnings("nullness:contracts.postcondition.not.satisfied")
    private void assertPreparedWithMedia() {
        Assertions.checkState(isPreparedWithMedia);
    }

    @SuppressWarnings("ReferenceEquality")
    @RequiresNonNull({ "trackGroupArrays", "trackSelectionsByPeriodAndRenderer", "mediaPreparer", "mediaPreparer.timeline" })
    private TrackSelectorResult runTrackSelection(int periodIndex) {
        try {
            TrackSelectorResult trackSelectorResult = trackSelector.selectTracks(rendererCapabilities, trackGroupArrays[periodIndex], new MediaPeriodId(mediaPreparer.timeline.getUidOfPeriod(periodIndex)), mediaPreparer.timeline);
            for (int i = 0; i < trackSelectorResult.length; i++) {
                TrackSelection newSelection = trackSelectorResult.selections.get(i);
                if (newSelection == null) {
                    continue;
                }
                List<TrackSelection> existingSelectionList = trackSelectionsByPeriodAndRenderer[periodIndex][i];
                boolean mergedWithExistingSelection = false;
                for (int j = 0; j < existingSelectionList.size(); j++) {
                    TrackSelection existingSelection = existingSelectionList.get(j);
                    if (existingSelection.getTrackGroup() == newSelection.getTrackGroup()) {
                        scratchSet.clear();
                        for (int k = 0; k < existingSelection.length(); k++) {
                            scratchSet.put(existingSelection.getIndexInTrackGroup(k), 0);
                        }
                        for (int k = 0; k < newSelection.length(); k++) {
                            scratchSet.put(newSelection.getIndexInTrackGroup(k), 0);
                        }
                        int[] mergedTracks = new int[scratchSet.size()];
                        for (int k = 0; k < scratchSet.size(); k++) {
                            mergedTracks[k] = scratchSet.keyAt(k);
                        }
                        existingSelectionList.set(j, new DownloadTrackSelection(existingSelection.getTrackGroup(), mergedTracks));
                        mergedWithExistingSelection = true;
                        break;
                    }
                }
                if (!mergedWithExistingSelection) {
                    existingSelectionList.add(newSelection);
                }
            }
            return trackSelectorResult;
        } catch (ExoPlaybackException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static MediaSourceFactory getMediaSourceFactory(String className) {
        Constructor<?> constructor = null;
        Method setStreamKeysMethod = null;
        Method createMethod = null;
        try {
            Class<?> factoryClazz = Class.forName(className);
            constructor = factoryClazz.getConstructor(Factory.class);
            setStreamKeysMethod = factoryClazz.getMethod("setStreamKeys", List.class);
            createMethod = factoryClazz.getMethod("createMediaSource", Uri.class);
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException(e);
        }
        return new MediaSourceFactory(constructor, setStreamKeysMethod, createMethod);
    }

    private static final class MediaSourceFactory {

        @Nullable
        private final Constructor<?> constructor;

        @Nullable
        private final Method setStreamKeysMethod;

        @Nullable
        private final Method createMethod;

        public MediaSourceFactory(@Nullable Constructor<?> constructor, @Nullable Method setStreamKeysMethod, @Nullable Method createMethod) {
            this.constructor = constructor;
            this.setStreamKeysMethod = setStreamKeysMethod;
            this.createMethod = createMethod;
        }

        private MediaSource createMediaSource(Uri uri, Factory dataSourceFactory, @Nullable List<StreamKey> streamKeys) {
            if (constructor == null || setStreamKeysMethod == null || createMethod == null) {
                throw new IllegalStateException("Module missing to create media source.");
            }
            try {
                Object factory = constructor.newInstance(dataSourceFactory);
                if (streamKeys != null) {
                    setStreamKeysMethod.invoke(factory, streamKeys);
                }
                return (MediaSource) Assertions.checkNotNull(createMethod.invoke(factory, uri));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to instantiate media source.", e);
            }
        }
    }

    private static final class MediaPreparer implements MediaSource.SourceInfoRefreshListener, MediaPeriod.Callback, Handler.Callback {

        private static final int MESSAGE_PREPARE_SOURCE = 0;

        private static final int MESSAGE_CHECK_FOR_FAILURE = 1;

        private static final int MESSAGE_CONTINUE_LOADING = 2;

        private static final int MESSAGE_RELEASE = 3;

        private static final int DOWNLOAD_HELPER_CALLBACK_MESSAGE_PREPARED = 0;

        private static final int DOWNLOAD_HELPER_CALLBACK_MESSAGE_FAILED = 1;

        private final MediaSource mediaSource;

        private final DownloadHelper downloadHelper;

        private final Allocator allocator;

        private final HandlerThread mediaSourceThread;

        private final Handler mediaSourceHandler;

        private final Handler downloadHelperHandler;

        private final ArrayList<MediaPeriod> pendingMediaPeriods;

        @Nullable
        public Object manifest;

        @MonotonicNonNull
        public Timeline timeline;

        public MediaPeriod @MonotonicNonNull [] mediaPeriods;

        private boolean released;

        public MediaPreparer(MediaSource mediaSource, DownloadHelper downloadHelper) {
            this.mediaSource = mediaSource;
            this.downloadHelper = downloadHelper;
            allocator = new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE);
            @SuppressWarnings("methodref.receiver.bound.invalid")
            Handler downloadThreadHandler = Util.createHandler(this::handleDownloadHelperCallbackMessage);
            this.downloadHelperHandler = downloadThreadHandler;
            mediaSourceThread = new HandlerThread("DownloadHelper");
            mediaSourceThread.start();
            mediaSourceHandler = Util.createHandler(mediaSourceThread.getLooper(), this);
            mediaSourceHandler.sendEmptyMessage(MESSAGE_PREPARE_SOURCE);
            pendingMediaPeriods = new ArrayList<>();
        }

        public void release() {
            if (released) {
                return;
            }
            released = true;
            mediaSourceHandler.sendEmptyMessage(MESSAGE_RELEASE);
        }

        @Override
        public boolean handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_PREPARE_SOURCE:
                    mediaSource.prepareSource(this, null);
                    mediaSourceHandler.sendEmptyMessage(MESSAGE_CHECK_FOR_FAILURE);
                    return true;
                case MESSAGE_CHECK_FOR_FAILURE:
                    try {
                        if (mediaPeriods == null) {
                            mediaSource.maybeThrowSourceInfoRefreshError();
                        } else {
                            for (int i = 0; i < pendingMediaPeriods.size(); i++) {
                                pendingMediaPeriods.get(i).maybeThrowPrepareError();
                            }
                        }
                        mediaSourceHandler.sendEmptyMessageDelayed(MESSAGE_CHECK_FOR_FAILURE, 100);
                    } catch (IOException e) {
                        downloadHelperHandler.obtainMessage(DOWNLOAD_HELPER_CALLBACK_MESSAGE_FAILED, e).sendToTarget();
                    }
                    return true;
                case MESSAGE_CONTINUE_LOADING:
                    MediaPeriod mediaPeriod = (MediaPeriod) msg.obj;
                    if (pendingMediaPeriods.contains(mediaPeriod)) {
                        mediaPeriod.continueLoading(0);
                    }
                    return true;
                case MESSAGE_RELEASE:
                    if (mediaPeriods != null) {
                        for (MediaPeriod period : mediaPeriods) {
                            mediaSource.releasePeriod(period);
                        }
                    }
                    mediaSource.releaseSource(this);
                    mediaSourceHandler.removeCallbacksAndMessages(null);
                    mediaSourceThread.quit();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onSourceInfoRefreshed(MediaSource source, Timeline timeline, @Nullable Object manifest) {
            if (this.timeline != null) {
                return;
            }
            this.timeline = timeline;
            this.manifest = manifest;
            mediaPeriods = new MediaPeriod[timeline.getPeriodCount()];
            for (int i = 0; i < mediaPeriods.length; i++) {
                MediaPeriod mediaPeriod = mediaSource.createPeriod(new MediaPeriodId(timeline.getUidOfPeriod(i)), allocator, 0);
                mediaPeriods[i] = mediaPeriod;
                pendingMediaPeriods.add(mediaPeriod);
            }
            for (MediaPeriod mediaPeriod : mediaPeriods) {
                mediaPeriod.prepare(this, 0);
            }
        }

        @Override
        public void onPrepared(MediaPeriod mediaPeriod) {
            pendingMediaPeriods.remove(mediaPeriod);
            if (pendingMediaPeriods.isEmpty()) {
                mediaSourceHandler.removeMessages(MESSAGE_CHECK_FOR_FAILURE);
                downloadHelperHandler.sendEmptyMessage(DOWNLOAD_HELPER_CALLBACK_MESSAGE_PREPARED);
            }
        }

        @Override
        public void onContinueLoadingRequested(MediaPeriod mediaPeriod) {
            if (pendingMediaPeriods.contains(mediaPeriod)) {
                mediaSourceHandler.obtainMessage(MESSAGE_CONTINUE_LOADING, mediaPeriod).sendToTarget();
            }
        }

        private boolean handleDownloadHelperCallbackMessage(Message msg) {
            if (released) {
                return false;
            }
            switch(msg.what) {
                case DOWNLOAD_HELPER_CALLBACK_MESSAGE_PREPARED:
                    downloadHelper.onMediaPrepared();
                    return true;
                case DOWNLOAD_HELPER_CALLBACK_MESSAGE_FAILED:
                    downloadHelper.onMediaPreparationFailed((IOException) Util.castNonNull(msg.obj));
                    return true;
                default:
                    return false;
            }
        }
    }

    private static final class DownloadTrackSelection extends BaseTrackSelection {

        private static final class Factory implements TrackSelection.Factory {

            @Override
            @NullableType
            public TrackSelection[] createTrackSelections(@NullableType Definition[] definitions, BandwidthMeter bandwidthMeter) {
                @NullableType
                TrackSelection[] selections = new TrackSelection[definitions.length];
                for (int i = 0; i < definitions.length; i++) {
                    selections[i] = definitions[i] == null ? null : new DownloadTrackSelection(definitions[i].group, definitions[i].tracks);
                }
                return selections;
            }
        }

        public DownloadTrackSelection(TrackGroup trackGroup, int[] tracks) {
            super(trackGroup, tracks);
        }

        @Override
        public int getSelectedIndex() {
            return 0;
        }

        @Override
        public int getSelectionReason() {
            return C.SELECTION_REASON_UNKNOWN;
        }

        @Nullable
        @Override
        public Object getSelectionData() {
            return null;
        }
    }

    private static final class DummyBandwidthMeter implements BandwidthMeter {

        @Override
        public long getBitrateEstimate() {
            return 0;
        }

        @Nullable
        @Override
        public TransferListener getTransferListener() {
            return null;
        }

        @Override
        public void addEventListener(Handler eventHandler, EventListener eventListener) {
        }

        @Override
        public void removeEventListener(EventListener eventListener) {
        }
    }
}