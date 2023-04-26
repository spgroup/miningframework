package com.google.android.exoplayer2.source.rtsp;

import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.VERSION_SLASHY;
import static com.google.android.exoplayer2.util.Assertions.checkNotNull;
import static com.google.android.exoplayer2.util.Util.castNonNull;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManagerProvider;
import com.google.android.exoplayer2.source.BaseMediaSource;
import com.google.android.exoplayer2.source.MediaPeriod;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.SinglePeriodTimeline;
import com.google.android.exoplayer2.source.rtsp.RtspClient.SessionInfoListener;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.LoadErrorHandlingPolicy;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

public final class RtspMediaSource extends BaseMediaSource {

    public static final class Factory implements MediaSourceFactory {

        @Override
        public Factory setDrmSessionManagerProvider(@Nullable DrmSessionManagerProvider drmSessionManager) {
            return this;
        }

        @Deprecated
        @Override
        public Factory setDrmSessionManager(@Nullable DrmSessionManager drmSessionManager) {
            return this;
        }

        @Deprecated
        @Override
        public Factory setDrmHttpDataSourceFactory(@Nullable HttpDataSource.Factory drmHttpDataSourceFactory) {
            return this;
        }

        @Deprecated
        @Override
        public Factory setDrmUserAgent(@Nullable String userAgent) {
            return this;
        }

        @Override
        public Factory setLoadErrorHandlingPolicy(@Nullable LoadErrorHandlingPolicy loadErrorHandlingPolicy) {
            return this;
        }

        @Override
        public int[] getSupportedTypes() {
            return new int[] { C.TYPE_RTSP };
        }

        @Override
        public RtspMediaSource createMediaSource(MediaItem mediaItem) {
            checkNotNull(mediaItem.playbackProperties);
            return new RtspMediaSource(mediaItem);
        }
    }

    public static final class RtspPlaybackException extends IOException {

        public RtspPlaybackException(String message) {
            super(message);
        }

        public RtspPlaybackException(Throwable e) {
            super(e);
        }

        public RtspPlaybackException(String message, Throwable e) {
            super(message, e);
        }
    }

    private final MediaItem mediaItem;

    private final RtpDataChannel.Factory rtpDataChannelFactory;

    @MonotonicNonNull
    private RtspClient rtspClient;

    @Nullable
    private ImmutableList<RtspMediaTrack> rtspMediaTracks;

    @Nullable
    private IOException sourcePrepareException;

    private RtspMediaSource(MediaItem mediaItem) {
        this.mediaItem = mediaItem;
        rtpDataChannelFactory = new UdpDataSourceRtpDataChannelFactory();
    }

    @Override
    protected void prepareSourceInternal(@Nullable TransferListener mediaTransferListener) {
        checkNotNull(mediaItem.playbackProperties);
        try {
            rtspClient = new RtspClient(new SessionInfoListenerImpl(), VERSION_SLASHY, mediaItem.playbackProperties.uri);
            rtspClient.start();
        } catch (IOException e) {
            sourcePrepareException = new RtspPlaybackException("RtspClient not opened.", e);
        }
    }

    @Override
    protected void releaseSourceInternal() {
        Util.closeQuietly(rtspClient);
    }

    @Override
    public MediaItem getMediaItem() {
        return mediaItem;
    }

    @Override
    public void maybeThrowSourceInfoRefreshError() throws IOException {
        if (sourcePrepareException != null) {
            throw sourcePrepareException;
        }
    }

    @Override
    public MediaPeriod createPeriod(MediaPeriodId id, Allocator allocator, long startPositionUs) {
        return new RtspMediaPeriod(allocator, checkNotNull(rtspMediaTracks), checkNotNull(rtspClient), rtpDataChannelFactory);
    }

    @Override
    public void releasePeriod(MediaPeriod mediaPeriod) {
        ((RtspMediaPeriod) mediaPeriod).release();
    }

    private final class SessionInfoListenerImpl implements SessionInfoListener {

        @Override
        public void onSessionTimelineUpdated(RtspSessionTiming timing, ImmutableList<RtspMediaTrack> tracks) {
            rtspMediaTracks = tracks;
            refreshSourceInfo(new SinglePeriodTimeline(C.msToUs(timing.getDurationMs()), !timing.isLive(), false, timing.isLive(), null, mediaItem));
        }

        @Override
        public void onSessionTimelineRequestFailed(String message, @Nullable Throwable cause) {
            if (cause == null) {
                sourcePrepareException = new RtspPlaybackException(message);
            } else {
                sourcePrepareException = new RtspPlaybackException(message, castNonNull(cause));
            }
        }
    }
}
