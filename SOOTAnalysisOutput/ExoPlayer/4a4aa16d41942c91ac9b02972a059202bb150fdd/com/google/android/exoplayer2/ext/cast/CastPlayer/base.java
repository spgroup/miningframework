package com.google.android.exoplayer2.ext.cast;

import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.BasePlayer;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
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
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

public final class CastPlayer extends BasePlayer {

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

    private RemoteMediaClient remoteMediaClient;

    private final StatusListener statusListener;

    private final SeekResultCallback seekResultCallback;

    private final CopyOnWriteArraySet<EventListener> listeners;

    private SessionAvailabilityListener sessionAvailabilityListener;

    private CastTimeline currentTimeline;

    private TrackGroupArray currentTrackGroups;

    private TrackSelectionArray currentTrackSelection;

    @Player.State
    private int playbackState;

    private int repeatMode;

    private int currentWindowIndex;

    private boolean playWhenReady;

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
        listeners = new CopyOnWriteArraySet<>();
        SessionManager sessionManager = castContext.getSessionManager();
        sessionManager.addSessionManagerListener(statusListener, CastSession.class);
        CastSession session = sessionManager.getCurrentCastSession();
        remoteMediaClient = session != null ? session.getRemoteMediaClient() : null;
        playbackState = STATE_IDLE;
        repeatMode = REPEAT_MODE_OFF;
        currentTimeline = CastTimeline.EMPTY_CAST_TIMELINE;
        currentTrackGroups = TrackGroupArray.EMPTY;
        currentTrackSelection = EMPTY_TRACK_SELECTION_ARRAY;
        pendingSeekWindowIndex = C.INDEX_UNSET;
        pendingSeekPositionMs = C.TIME_UNSET;
        updateInternalState();
    }

    public PendingResult<MediaChannelResult> loadItem(MediaQueueItem item, long positionMs) {
        return loadItems(new MediaQueueItem[] { item }, 0, positionMs, REPEAT_MODE_OFF);
    }

    public PendingResult<MediaChannelResult> loadItems(MediaQueueItem[] items, int startIndex, long positionMs, @RepeatMode int repeatMode) {
        if (remoteMediaClient != null) {
            positionMs = positionMs != C.TIME_UNSET ? positionMs : 0;
            waitingForInitialTimeline = true;
            return remoteMediaClient.queueLoad(items, startIndex, getCastRepeatMode(repeatMode), positionMs, null);
        }
        return null;
    }

    public PendingResult<MediaChannelResult> addItems(MediaQueueItem... items) {
        return addItems(MediaQueueItem.INVALID_ITEM_ID, items);
    }

    public PendingResult<MediaChannelResult> addItems(int periodId, MediaQueueItem... items) {
        if (getMediaStatus() != null && (periodId == MediaQueueItem.INVALID_ITEM_ID || currentTimeline.getIndexOfPeriod(periodId) != C.INDEX_UNSET)) {
            return remoteMediaClient.queueInsertItems(items, periodId, null);
        }
        return null;
    }

    public PendingResult<MediaChannelResult> removeItem(int periodId) {
        if (getMediaStatus() != null && currentTimeline.getIndexOfPeriod(periodId) != C.INDEX_UNSET) {
            return remoteMediaClient.queueRemoveItem(periodId, null);
        }
        return null;
    }

    public PendingResult<MediaChannelResult> moveItem(int periodId, int newIndex) {
        Assertions.checkArgument(newIndex >= 0 && newIndex < currentTimeline.getPeriodCount());
        if (getMediaStatus() != null && currentTimeline.getIndexOfPeriod(periodId) != C.INDEX_UNSET) {
            return remoteMediaClient.queueMoveItemToNewIndex(periodId, newIndex, null);
        }
        return null;
    }

    public MediaQueueItem getItem(int periodId) {
        MediaStatus mediaStatus = getMediaStatus();
        return mediaStatus != null && currentTimeline.getIndexOfPeriod(periodId) != C.INDEX_UNSET ? mediaStatus.getItemById(periodId) : null;
    }

    public boolean isCastSessionAvailable() {
        return remoteMediaClient != null;
    }

    public void setSessionAvailabilityListener(SessionAvailabilityListener listener) {
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
        listeners.add(listener);
    }

    @Override
    public void removeListener(EventListener listener) {
        listeners.remove(listener);
    }

    @Override
    @Player.State
    public int getPlaybackState() {
        return playbackState;
    }

    @Override
    public ExoPlaybackException getPlaybackError() {
        return null;
    }

    @Override
    public void setPlayWhenReady(boolean playWhenReady) {
        if (remoteMediaClient == null) {
            return;
        }
        if (playWhenReady) {
            remoteMediaClient.play();
        } else {
            remoteMediaClient.pause();
        }
    }

    @Override
    public boolean getPlayWhenReady() {
        return playWhenReady;
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
            for (EventListener listener : listeners) {
                listener.onPositionDiscontinuity(Player.DISCONTINUITY_REASON_SEEK);
            }
        } else if (pendingSeekCount == 0) {
            for (EventListener listener : listeners) {
                listener.onSeekProcessed();
            }
        }
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
        if (remoteMediaClient != null) {
            remoteMediaClient.queueSetRepeatMode(getCastRepeatMode(repeatMode), null);
        }
    }

    @Override
    @RepeatMode
    public int getRepeatMode() {
        return repeatMode;
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
    @Nullable
    public Object getCurrentManifest() {
        return null;
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

    public void updateInternalState() {
        if (remoteMediaClient == null) {
            return;
        }
        int playbackState = fetchPlaybackState(remoteMediaClient);
        boolean playWhenReady = !remoteMediaClient.isPaused();
        if (this.playbackState != playbackState || this.playWhenReady != playWhenReady) {
            this.playbackState = playbackState;
            this.playWhenReady = playWhenReady;
            for (EventListener listener : listeners) {
                listener.onPlayerStateChanged(this.playWhenReady, this.playbackState);
            }
        }
        @RepeatMode
        int repeatMode = fetchRepeatMode(remoteMediaClient);
        if (this.repeatMode != repeatMode) {
            this.repeatMode = repeatMode;
            for (EventListener listener : listeners) {
                listener.onRepeatModeChanged(repeatMode);
            }
        }
        int currentWindowIndex = fetchCurrentWindowIndex(getMediaStatus());
        if (this.currentWindowIndex != currentWindowIndex && pendingSeekCount == 0) {
            this.currentWindowIndex = currentWindowIndex;
            for (EventListener listener : listeners) {
                listener.onPositionDiscontinuity(DISCONTINUITY_REASON_PERIOD_TRANSITION);
            }
        }
        if (updateTracksAndSelections()) {
            for (EventListener listener : listeners) {
                listener.onTracksChanged(currentTrackGroups, currentTrackSelection);
            }
        }
        maybeUpdateTimelineAndNotify();
    }

    private void maybeUpdateTimelineAndNotify() {
        if (updateTimeline()) {
            @Player.TimelineChangeReason
            int reason = waitingForInitialTimeline ? Player.TIMELINE_CHANGE_REASON_PREPARED : Player.TIMELINE_CHANGE_REASON_DYNAMIC;
            waitingForInitialTimeline = false;
            for (EventListener listener : listeners) {
                listener.onTimelineChanged(currentTimeline, null, reason);
            }
        }
    }

    private boolean updateTimeline() {
        CastTimeline oldTimeline = currentTimeline;
        MediaStatus status = getMediaStatus();
        currentTimeline = status != null ? timelineTracker.getCastTimeline(remoteMediaClient) : CastTimeline.EMPTY_CAST_TIMELINE;
        return !oldTimeline.equals(currentTimeline);
    }

    private boolean updateTracksAndSelections() {
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
            updateInternalState();
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

    private static int fetchCurrentWindowIndex(@Nullable MediaStatus mediaStatus) {
        Integer currentItemId = mediaStatus != null ? mediaStatus.getIndexById(mediaStatus.getCurrentItemId()) : null;
        return currentItemId != null ? currentItemId : 0;
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
            updateInternalState();
        }

        @Override
        public void onMetadataUpdated() {
        }

        @Override
        public void onQueueStatusUpdated() {
            maybeUpdateTimelineAndNotify();
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

    private final class SeekResultCallback implements ResultCallback<MediaChannelResult> {

        @Override
        public void onResult(@NonNull MediaChannelResult result) {
            int statusCode = result.getStatus().getStatusCode();
            if (statusCode != CastStatusCodes.SUCCESS && statusCode != CastStatusCodes.REPLACED) {
                Log.e(TAG, "Seek failed. Error code " + statusCode + ": " + CastUtils.getLogString(statusCode));
            }
            if (--pendingSeekCount == 0) {
                pendingSeekWindowIndex = C.INDEX_UNSET;
                pendingSeekPositionMs = C.TIME_UNSET;
                for (EventListener listener : listeners) {
                    listener.onSeekProcessed();
                }
            }
        }
    }
}
