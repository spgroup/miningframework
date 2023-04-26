package com.google.android.exoplayer2.source.rtsp;

import static com.google.android.exoplayer2.util.Assertions.checkNotNull;
import android.net.Uri;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManagerProvider;
import com.google.android.exoplayer2.source.BaseMediaSource;
import com.google.android.exoplayer2.source.ForwardingTimeline;
import com.google.android.exoplayer2.source.MediaPeriod;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.SinglePeriodTimeline;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.LoadErrorHandlingPolicy;
import com.google.android.exoplayer2.upstream.TransferListener;
import java.io.IOException;

public final class RtspMediaSource extends BaseMediaSource {

    static {
        ExoPlayerLibraryInfo.registerModule("goog.exo.rtsp");
    }

    public static final class Factory implements MediaSourceFactory {

        private String userAgent;

        private boolean forceUseRtpTcp;

        public Factory() {
            userAgent = ExoPlayerLibraryInfo.VERSION_SLASHY;
        }

        public Factory setForceUseRtpTcp(boolean forceUseRtpTcp) {
            this.forceUseRtpTcp = forceUseRtpTcp;
            return this;
        }

        public Factory setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

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
            return new RtspMediaSource(mediaItem, forceUseRtpTcp ? new TransferRtpDataChannelFactory() : new UdpDataSourceRtpDataChannelFactory(), userAgent);
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

    private final String userAgent;

    private final Uri uri;

    private long timelineDurationUs;

    private boolean timelineIsSeekable;

    private boolean timelineIsLive;

    private boolean timelineIsPlaceholder;

    private RtspMediaSource(MediaItem mediaItem, RtpDataChannel.Factory rtpDataChannelFactory, String userAgent) {
        this.mediaItem = mediaItem;
        this.rtpDataChannelFactory = rtpDataChannelFactory;
        this.userAgent = userAgent;
        this.uri = checkNotNull(this.mediaItem.playbackProperties).uri;
        this.timelineDurationUs = C.TIME_UNSET;
        this.timelineIsPlaceholder = true;
    }

    @Override
    protected void prepareSourceInternal(@Nullable TransferListener mediaTransferListener) {
        notifySourceInfoRefreshed();
    }

    @Override
    protected void releaseSourceInternal() {
    }

    @Override
    public MediaItem getMediaItem() {
        return mediaItem;
    }

    @Override
    public void maybeThrowSourceInfoRefreshError() {
    }

    @Override
    public MediaPeriod createPeriod(MediaPeriodId id, Allocator allocator, long startPositionUs) {
        return new RtspMediaPeriod(allocator, rtpDataChannelFactory, uri, (timing) -> {
            timelineDurationUs = C.msToUs(timing.getDurationMs());
            timelineIsSeekable = !timing.isLive();
            timelineIsLive = timing.isLive();
            timelineIsPlaceholder = false;
            notifySourceInfoRefreshed();
        }, userAgent);
    }

    @Override
    public void releasePeriod(MediaPeriod mediaPeriod) {
        ((RtspMediaPeriod) mediaPeriod).release();
    }

    private void notifySourceInfoRefreshed() {
        Timeline timeline = new SinglePeriodTimeline(timelineDurationUs, timelineIsSeekable, false, timelineIsLive, null, mediaItem);
        if (timelineIsPlaceholder) {
            timeline = new ForwardingTimeline(timeline) {

                @Override
                public Window getWindow(int windowIndex, Window window, long defaultPositionProjectionUs) {
                    super.getWindow(windowIndex, window, defaultPositionProjectionUs);
                    window.isPlaceholder = true;
                    return window;
                }

                @Override
                public Period getPeriod(int periodIndex, Period period, boolean setIds) {
                    super.getPeriod(periodIndex, period, setIds);
                    period.isPlaceholder = true;
                    return period;
                }
            };
        }
        refreshSourceInfo(timeline);
    }
}
