package com.google.android.exoplayer2.source;

import static com.google.android.exoplayer2.C.BUFFER_FLAG_ENCRYPTED;
import static com.google.android.exoplayer2.C.BUFFER_FLAG_KEY_FRAME;
import static com.google.android.exoplayer2.C.RESULT_BUFFER_READ;
import static com.google.android.exoplayer2.C.RESULT_FORMAT_READ;
import static com.google.android.exoplayer2.C.RESULT_NOTHING_READ;
import static com.google.common.truth.Truth.assertThat;
import static java.lang.Long.MIN_VALUE;
import static java.util.Arrays.copyOfRange;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.when;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.FormatHolder;
import com.google.android.exoplayer2.decoder.DecoderInputBuffer;
import com.google.android.exoplayer2.drm.DrmInitData;
import com.google.android.exoplayer2.drm.DrmSession;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.ExoMediaCrypto;
import com.google.android.exoplayer2.extractor.TrackOutput;
import com.google.android.exoplayer2.testutil.TestUtil;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.ParsableByteArray;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

@RunWith(AndroidJUnit4.class)
public final class SampleQueueTest {

    private static final int ALLOCATION_SIZE = 16;

    private static final Format FORMAT_1 = Format.createSampleFormat("1", "mimeType", 0);

    private static final Format FORMAT_2 = Format.createSampleFormat("2", "mimeType", 0);

    private static final Format FORMAT_1_COPY = Format.createSampleFormat("1", "mimeType", 0);

    private static final Format FORMAT_SPLICED = Format.createSampleFormat("spliced", "mimeType", 0);

    private static final Format FORMAT_ENCRYPTED = Format.createSampleFormat("encrypted", "mimeType", null, Format.NO_VALUE, new DrmInitData());

    private static final byte[] DATA = TestUtil.buildTestData(ALLOCATION_SIZE * 10);

    private static final int[] SAMPLE_SIZES = new int[] { ALLOCATION_SIZE - 1, ALLOCATION_SIZE - 2, ALLOCATION_SIZE - 1, ALLOCATION_SIZE - 1, ALLOCATION_SIZE, ALLOCATION_SIZE * 2, ALLOCATION_SIZE * 2 - 2, ALLOCATION_SIZE };

    private static final int[] SAMPLE_OFFSETS = new int[] { ALLOCATION_SIZE * 9, ALLOCATION_SIZE * 8 + 1, ALLOCATION_SIZE * 7, ALLOCATION_SIZE * 6 + 1, ALLOCATION_SIZE * 5, ALLOCATION_SIZE * 3, ALLOCATION_SIZE + 1, 0 };

    private static final long[] SAMPLE_TIMESTAMPS = new long[] { 0, 1000, 2000, 3000, 4000, 5000, 6000, 7000 };

    private static final long LAST_SAMPLE_TIMESTAMP = SAMPLE_TIMESTAMPS[SAMPLE_TIMESTAMPS.length - 1];

    private static final int[] SAMPLE_FLAGS = new int[] { C.BUFFER_FLAG_KEY_FRAME, 0, 0, 0, C.BUFFER_FLAG_KEY_FRAME, 0, 0, 0 };

    private static final Format[] SAMPLE_FORMATS = new Format[] { FORMAT_1, FORMAT_1, FORMAT_1, FORMAT_1, FORMAT_2, FORMAT_2, FORMAT_2, FORMAT_2 };

    private static final int DATA_SECOND_KEYFRAME_INDEX = 4;

    private static final int[] ENCRYPTED_SAMPLES_FLAGS = new int[] { C.BUFFER_FLAG_KEY_FRAME, C.BUFFER_FLAG_ENCRYPTED, 0, C.BUFFER_FLAG_ENCRYPTED };

    private static final long[] ENCRYPTED_SAMPLE_TIMESTAMPS = new long[] { 0, 1000, 2000, 3000 };

    private static final Format[] ENCRYPTED_SAMPLE_FORMATS = new Format[] { FORMAT_ENCRYPTED, FORMAT_ENCRYPTED, FORMAT_1, FORMAT_ENCRYPTED };

    private static final int[] ENCRYPTED_SAMPLE_SIZES = new int[] { 1, 3, 1, 3 };

    private static final int[] ENCRYPTED_SAMPLE_OFFSETS = new int[] { 7, 4, 3, 0 };

    private static final byte[] ENCRYPTED_SAMPLE_DATA = new byte[] { 1, 1, 1, 1, 1, 1, 1, 1 };

    private static final TrackOutput.CryptoData DUMMY_CRYPTO_DATA = new TrackOutput.CryptoData(C.CRYPTO_MODE_AES_CTR, new byte[16], 0, 0);

    private Allocator allocator;

    private DrmSessionManager<ExoMediaCrypto> mockDrmSessionManager;

    private DrmSession<ExoMediaCrypto> mockDrmSession;

    private SampleQueue sampleQueue;

    private FormatHolder formatHolder;

    private DecoderInputBuffer inputBuffer;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        allocator = new DefaultAllocator(false, ALLOCATION_SIZE);
        mockDrmSessionManager = (DrmSessionManager<ExoMediaCrypto>) Mockito.mock(DrmSessionManager.class);
        mockDrmSession = (DrmSession<ExoMediaCrypto>) Mockito.mock(DrmSession.class);
        when(mockDrmSessionManager.acquireSession(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(mockDrmSession);
        sampleQueue = new SampleQueue(allocator, Assertions.checkNotNull(Looper.myLooper()), mockDrmSessionManager);
        formatHolder = new FormatHolder();
        inputBuffer = new DecoderInputBuffer(DecoderInputBuffer.BUFFER_REPLACEMENT_MODE_NORMAL);
    }

    @After
    public void tearDown() {
        allocator = null;
        sampleQueue = null;
        formatHolder = null;
        inputBuffer = null;
    }

    @Test
    public void testCapacityIncreases() {
        int numberOfSamplesToInput = 3 * SampleQueue.SAMPLE_CAPACITY_INCREMENT + 1;
        sampleQueue.format(FORMAT_1);
        sampleQueue.sampleData(new ParsableByteArray(numberOfSamplesToInput), numberOfSamplesToInput);
        for (int i = 0; i < numberOfSamplesToInput; i++) {
            sampleQueue.sampleMetadata(i * 1000, C.BUFFER_FLAG_KEY_FRAME, 1, numberOfSamplesToInput - i - 1, null);
        }
        assertReadFormat(false, FORMAT_1);
        for (int i = 0; i < numberOfSamplesToInput; i++) {
            assertReadSample(i * 1000, true, false, new byte[1], 0, 1);
        }
        assertReadNothing(false);
    }

    @Test
    public void testResetReleasesAllocations() {
        writeTestData();
        assertAllocationCount(10);
        sampleQueue.reset();
        assertAllocationCount(0);
    }

    @Test
    public void testReadWithoutWrite() {
        assertNoSamplesToRead(null);
    }

    @Test
    public void testEqualFormatsDeduplicated() {
        sampleQueue.format(FORMAT_1);
        assertReadFormat(false, FORMAT_1);
        sampleQueue.format(FORMAT_1);
        assertNoSamplesToRead(FORMAT_1);
        sampleQueue.format(FORMAT_1_COPY);
        assertNoSamplesToRead(FORMAT_1);
    }

    @Test
    public void testMultipleFormatsDeduplicated() {
        sampleQueue.format(FORMAT_1);
        sampleQueue.sampleData(new ParsableByteArray(DATA), ALLOCATION_SIZE);
        sampleQueue.sampleMetadata(0, C.BUFFER_FLAG_KEY_FRAME, ALLOCATION_SIZE, 0, null);
        sampleQueue.format(FORMAT_2);
        sampleQueue.format(FORMAT_1_COPY);
        sampleQueue.sampleData(new ParsableByteArray(DATA), ALLOCATION_SIZE);
        sampleQueue.sampleMetadata(1000, C.BUFFER_FLAG_KEY_FRAME, ALLOCATION_SIZE, 0, null);
        assertReadFormat(false, FORMAT_1);
        assertReadSample(0, true, false, DATA, 0, ALLOCATION_SIZE);
        assertReadSample(1000, true, false, DATA, 0, ALLOCATION_SIZE);
        sampleQueue.format(FORMAT_2);
        sampleQueue.format(FORMAT_1);
        sampleQueue.sampleData(new ParsableByteArray(DATA), ALLOCATION_SIZE);
        sampleQueue.sampleMetadata(2000, C.BUFFER_FLAG_KEY_FRAME, ALLOCATION_SIZE, 0, null);
        assertReadSample(2000, true, false, DATA, 0, ALLOCATION_SIZE);
    }

    @Test
    public void testReadSingleSamples() {
        sampleQueue.sampleData(new ParsableByteArray(DATA), ALLOCATION_SIZE);
        assertAllocationCount(1);
        assertNoSamplesToRead(null);
        sampleQueue.format(FORMAT_1);
        assertReadFormat(false, FORMAT_1);
        assertNoSamplesToRead(FORMAT_1);
        sampleQueue.sampleMetadata(1000, C.BUFFER_FLAG_KEY_FRAME, ALLOCATION_SIZE, 0, null);
        assertReadFormat(true, FORMAT_1);
        assertReadSample(1000, true, false, DATA, 0, ALLOCATION_SIZE);
        assertAllocationCount(1);
        sampleQueue.discardToRead();
        assertAllocationCount(0);
        assertNoSamplesToRead(FORMAT_1);
        sampleQueue.sampleData(new ParsableByteArray(DATA), ALLOCATION_SIZE);
        sampleQueue.sampleMetadata(2000, 0, ALLOCATION_SIZE - 1, 1, null);
        assertReadFormat(true, FORMAT_1);
        assertReadSample(2000, false, false, DATA, 0, ALLOCATION_SIZE - 1);
        assertAllocationCount(1);
        sampleQueue.discardToRead();
        assertAllocationCount(1);
        sampleQueue.sampleMetadata(3000, 0, 1, 0, null);
        assertReadFormat(true, FORMAT_1);
        assertReadSample(3000, false, false, DATA, ALLOCATION_SIZE - 1, 1);
        assertAllocationCount(1);
        sampleQueue.discardToRead();
        assertAllocationCount(0);
    }

    @Test
    public void testReadMultiSamples() {
        writeTestData();
        assertThat(sampleQueue.getLargestQueuedTimestampUs()).isEqualTo(LAST_SAMPLE_TIMESTAMP);
        assertAllocationCount(10);
        assertReadTestData();
        assertAllocationCount(10);
        sampleQueue.discardToRead();
        assertAllocationCount(0);
    }

    @Test
    public void testReadMultiSamplesTwice() {
        writeTestData();
        writeTestData();
        assertAllocationCount(20);
        assertReadTestData(FORMAT_2);
        assertReadTestData(FORMAT_2);
        assertAllocationCount(20);
        sampleQueue.discardToRead();
        assertAllocationCount(0);
    }

    @Test
    public void testReadMultiWithSeek() {
        writeTestData();
        assertReadTestData();
        assertThat(sampleQueue.getFirstIndex()).isEqualTo(0);
        assertThat(sampleQueue.getReadIndex()).isEqualTo(8);
        assertAllocationCount(10);
        sampleQueue.seekTo(0);
        assertAllocationCount(10);
        assertThat(sampleQueue.getFirstIndex()).isEqualTo(0);
        assertThat(sampleQueue.getReadIndex()).isEqualTo(0);
        assertReadTestData();
    }

    @Test
    public void testEmptyQueueReturnsLoadingFinished() {
        sampleQueue.sampleData(new ParsableByteArray(DATA), DATA.length);
        assertThat(sampleQueue.isReady(false)).isFalse();
        assertThat(sampleQueue.isReady(true)).isTrue();
    }

    @Test
    public void testIsReadyWithUpstreamFormatOnlyReturnsTrue() {
        sampleQueue.format(FORMAT_ENCRYPTED);
        assertThat(sampleQueue.isReady(false)).isTrue();
    }

    @Test
    public void testIsReadyReturnsTrueForValidDrmSession() {
        writeTestDataWithEncryptedSections();
        assertReadFormat(false, FORMAT_ENCRYPTED);
        assertThat(sampleQueue.isReady(false)).isFalse();
        when(mockDrmSession.getState()).thenReturn(DrmSession.STATE_OPENED_WITH_KEYS);
        assertThat(sampleQueue.isReady(false)).isTrue();
    }

    @Test
    public void testIsReadyReturnsTrueForClearSampleAndPlayClearSamplesWithoutKeysIsTrue() {
        when(mockDrmSession.playClearSamplesWithoutKeys()).thenReturn(true);
        sampleQueue = new SampleQueue(allocator, Assertions.checkNotNull(Looper.myLooper()), mockDrmSessionManager);
        writeTestDataWithEncryptedSections();
        assertThat(sampleQueue.isReady(false)).isTrue();
    }

    @Test
    public void testReadEncryptedSectionsWaitsForKeys() {
        when(mockDrmSession.getState()).thenReturn(DrmSession.STATE_OPENED);
        writeTestDataWithEncryptedSections();
        assertReadFormat(false, FORMAT_ENCRYPTED);
        assertReadNothing(false);
        assertThat(inputBuffer.waitingForKeys).isTrue();
        when(mockDrmSession.getState()).thenReturn(DrmSession.STATE_OPENED_WITH_KEYS);
        assertReadEncryptedSample(0);
        assertThat(inputBuffer.waitingForKeys).isFalse();
    }

    @Test
    public void testReadEncryptedSectionsPopulatesDrmSession() {
        when(mockDrmSession.getState()).thenReturn(DrmSession.STATE_OPENED_WITH_KEYS);
        writeTestDataWithEncryptedSections();
        int result = sampleQueue.read(formatHolder, inputBuffer, false, false, 0);
        assertThat(result).isEqualTo(RESULT_FORMAT_READ);
        assertThat(formatHolder.drmSession).isSameInstanceAs(mockDrmSession);
        assertReadEncryptedSample(0);
        assertReadEncryptedSample(1);
        formatHolder.clear();
        assertThat(formatHolder.drmSession).isNull();
        result = sampleQueue.read(formatHolder, inputBuffer, false, false, 0);
        assertThat(result).isEqualTo(RESULT_FORMAT_READ);
        assertThat(formatHolder.drmSession).isNull();
        assertReadEncryptedSample(2);
        result = sampleQueue.read(formatHolder, inputBuffer, false, false, 0);
        assertThat(result).isEqualTo(RESULT_FORMAT_READ);
        assertThat(formatHolder.drmSession).isSameInstanceAs(mockDrmSession);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAllowPlaceholderSessionPopulatesDrmSession() {
        when(mockDrmSession.getState()).thenReturn(DrmSession.STATE_OPENED_WITH_KEYS);
        DrmSession<ExoMediaCrypto> mockPlaceholderDrmSession = (DrmSession<ExoMediaCrypto>) Mockito.mock(DrmSession.class);
        when(mockPlaceholderDrmSession.getState()).thenReturn(DrmSession.STATE_OPENED_WITH_KEYS);
        when(mockDrmSessionManager.acquirePlaceholderSession(ArgumentMatchers.any(), ArgumentMatchers.anyInt())).thenReturn(mockPlaceholderDrmSession);
        writeTestDataWithEncryptedSections();
        int result = sampleQueue.read(formatHolder, inputBuffer, false, false, 0);
        assertThat(result).isEqualTo(RESULT_FORMAT_READ);
        assertThat(formatHolder.drmSession).isSameInstanceAs(mockDrmSession);
        assertReadEncryptedSample(0);
        assertReadEncryptedSample(1);
        formatHolder.clear();
        assertThat(formatHolder.drmSession).isNull();
        result = sampleQueue.read(formatHolder, inputBuffer, false, false, 0);
        assertThat(result).isEqualTo(RESULT_FORMAT_READ);
        assertThat(formatHolder.drmSession).isSameInstanceAs(mockPlaceholderDrmSession);
        assertReadEncryptedSample(2);
        result = sampleQueue.read(formatHolder, inputBuffer, false, false, 0);
        assertThat(result).isEqualTo(RESULT_FORMAT_READ);
        assertThat(formatHolder.drmSession).isSameInstanceAs(mockDrmSession);
        assertReadEncryptedSample(3);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTrailingCryptoInfoInitializationVectorBytesZeroed() {
        when(mockDrmSession.getState()).thenReturn(DrmSession.STATE_OPENED_WITH_KEYS);
        DrmSession<ExoMediaCrypto> mockPlaceholderDrmSession = (DrmSession<ExoMediaCrypto>) Mockito.mock(DrmSession.class);
        when(mockPlaceholderDrmSession.getState()).thenReturn(DrmSession.STATE_OPENED_WITH_KEYS);
        when(mockDrmSessionManager.acquirePlaceholderSession(ArgumentMatchers.any(), ArgumentMatchers.anyInt())).thenReturn(mockPlaceholderDrmSession);
        writeFormat(ENCRYPTED_SAMPLE_FORMATS[0]);
        byte[] sampleData = new byte[] { 0, 1, 2 };
        byte[] initializationVector = new byte[] { 7, 6, 5, 4, 3, 2, 1, 0 };
        byte[] encryptedSampleData = TestUtil.joinByteArrays(new byte[] { 0x08 }, initializationVector, sampleData);
        writeSample(encryptedSampleData, 0, BUFFER_FLAG_KEY_FRAME | BUFFER_FLAG_ENCRYPTED);
        int result = sampleQueue.read(formatHolder, inputBuffer, false, false, 0);
        assertThat(result).isEqualTo(RESULT_FORMAT_READ);
        inputBuffer.cryptoInfo.iv = new byte[16];
        Arrays.fill(inputBuffer.cryptoInfo.iv, (byte) 1);
        result = sampleQueue.read(formatHolder, inputBuffer, false, false, 0);
        assertThat(result).isEqualTo(RESULT_BUFFER_READ);
        byte[] expectedInitializationVector = Arrays.copyOf(initializationVector, 16);
        assertArrayEquals(expectedInitializationVector, inputBuffer.cryptoInfo.iv);
    }

    @Test
    public void testReadWithErrorSessionReadsNothingAndThrows() throws IOException {
        when(mockDrmSession.getState()).thenReturn(DrmSession.STATE_OPENED);
        writeTestDataWithEncryptedSections();
        assertReadFormat(false, FORMAT_ENCRYPTED);
        assertReadNothing(false);
        sampleQueue.maybeThrowError();
        when(mockDrmSession.getState()).thenReturn(DrmSession.STATE_ERROR);
        when(mockDrmSession.getError()).thenReturn(new DrmSession.DrmSessionException(new Exception()));
        assertReadNothing(false);
        try {
            sampleQueue.maybeThrowError();
            Assert.fail();
        } catch (IOException e) {
        }
        when(mockDrmSession.getState()).thenReturn(DrmSession.STATE_OPENED_WITH_KEYS);
        assertReadEncryptedSample(0);
    }

    @Test
    public void testAllowPlayClearSamplesWithoutKeysReadsClearSamples() {
        when(mockDrmSession.playClearSamplesWithoutKeys()).thenReturn(true);
        sampleQueue = new SampleQueue(allocator, Assertions.checkNotNull(Looper.myLooper()), mockDrmSessionManager);
        when(mockDrmSession.getState()).thenReturn(DrmSession.STATE_OPENED);
        writeTestDataWithEncryptedSections();
        assertReadFormat(false, FORMAT_ENCRYPTED);
        assertReadEncryptedSample(0);
    }

    @Test
    public void testSeekAfterDiscard() {
        writeTestData();
        assertReadTestData();
        sampleQueue.discardToRead();
        assertThat(sampleQueue.getFirstIndex()).isEqualTo(8);
        assertThat(sampleQueue.getReadIndex()).isEqualTo(8);
        assertAllocationCount(0);
        sampleQueue.seekTo(0);
        assertAllocationCount(0);
        assertThat(sampleQueue.getFirstIndex()).isEqualTo(8);
        assertThat(sampleQueue.getReadIndex()).isEqualTo(8);
        assertReadEndOfStream(false);
    }

    @Test
    public void testAdvanceToEnd() {
        writeTestData();
        sampleQueue.advanceToEnd();
        assertAllocationCount(10);
        sampleQueue.discardToRead();
        assertAllocationCount(0);
        assertReadFormat(false, FORMAT_2);
        assertNoSamplesToRead(FORMAT_2);
    }

    @Test
    public void testAdvanceToEndRetainsUnassignedData() {
        sampleQueue.format(FORMAT_1);
        sampleQueue.sampleData(new ParsableByteArray(DATA), ALLOCATION_SIZE);
        sampleQueue.advanceToEnd();
        assertAllocationCount(1);
        sampleQueue.discardToRead();
        assertAllocationCount(1);
        assertReadFormat(false, FORMAT_1);
        assertNoSamplesToRead(FORMAT_1);
        sampleQueue.sampleMetadata(0, C.BUFFER_FLAG_KEY_FRAME, ALLOCATION_SIZE, 0, null);
        assertReadSample(0, true, false, DATA, 0, ALLOCATION_SIZE);
        assertNoSamplesToRead(FORMAT_1);
        assertAllocationCount(1);
        sampleQueue.discardToRead();
        assertAllocationCount(0);
    }

    @Test
    public void testAdvanceToBeforeBuffer() {
        writeTestData();
        int skipCount = sampleQueue.advanceTo(SAMPLE_TIMESTAMPS[0] - 1);
        assertThat(skipCount).isEqualTo(0);
        assertReadTestData();
        assertNoSamplesToRead(FORMAT_2);
    }

    @Test
    public void testAdvanceToStartOfBuffer() {
        writeTestData();
        int skipCount = sampleQueue.advanceTo(SAMPLE_TIMESTAMPS[0]);
        assertThat(skipCount).isEqualTo(0);
        assertReadTestData();
        assertNoSamplesToRead(FORMAT_2);
    }

    @Test
    public void testAdvanceToEndOfBuffer() {
        writeTestData();
        int skipCount = sampleQueue.advanceTo(LAST_SAMPLE_TIMESTAMP);
        assertThat(skipCount).isEqualTo(4);
        assertReadTestData(null, DATA_SECOND_KEYFRAME_INDEX);
        assertNoSamplesToRead(FORMAT_2);
    }

    @Test
    public void testAdvanceToAfterBuffer() {
        writeTestData();
        int skipCount = sampleQueue.advanceTo(LAST_SAMPLE_TIMESTAMP + 1);
        assertThat(skipCount).isEqualTo(4);
        assertReadTestData(null, DATA_SECOND_KEYFRAME_INDEX);
        assertNoSamplesToRead(FORMAT_2);
    }

    @Test
    public void testSeekToBeforeBuffer() {
        writeTestData();
        boolean success = sampleQueue.seekTo(SAMPLE_TIMESTAMPS[0] - 1, false);
        assertThat(success).isFalse();
        assertThat(sampleQueue.getReadIndex()).isEqualTo(0);
        assertReadTestData();
        assertNoSamplesToRead(FORMAT_2);
    }

    @Test
    public void testSeekToStartOfBuffer() {
        writeTestData();
        boolean success = sampleQueue.seekTo(SAMPLE_TIMESTAMPS[0], false);
        assertThat(success).isTrue();
        assertThat(sampleQueue.getReadIndex()).isEqualTo(0);
        assertReadTestData();
        assertNoSamplesToRead(FORMAT_2);
    }

    @Test
    public void testSeekToEndOfBuffer() {
        writeTestData();
        boolean success = sampleQueue.seekTo(LAST_SAMPLE_TIMESTAMP, false);
        assertThat(success).isTrue();
        assertThat(sampleQueue.getReadIndex()).isEqualTo(4);
        assertReadTestData(null, DATA_SECOND_KEYFRAME_INDEX);
        assertNoSamplesToRead(FORMAT_2);
    }

    @Test
    public void testSeekToAfterBuffer() {
        writeTestData();
        boolean success = sampleQueue.seekTo(LAST_SAMPLE_TIMESTAMP + 1, false);
        assertThat(success).isFalse();
        assertThat(sampleQueue.getReadIndex()).isEqualTo(0);
        assertReadTestData();
        assertNoSamplesToRead(FORMAT_2);
    }

    @Test
    public void testSeekToAfterBufferAllowed() {
        writeTestData();
        boolean success = sampleQueue.seekTo(LAST_SAMPLE_TIMESTAMP + 1, true);
        assertThat(success).isTrue();
        assertThat(sampleQueue.getReadIndex()).isEqualTo(4);
        assertReadTestData(null, DATA_SECOND_KEYFRAME_INDEX);
        assertNoSamplesToRead(FORMAT_2);
    }

    @Test
    public void testSeekToEndAndBackToStart() {
        writeTestData();
        boolean success = sampleQueue.seekTo(LAST_SAMPLE_TIMESTAMP, false);
        assertThat(success).isTrue();
        assertThat(sampleQueue.getReadIndex()).isEqualTo(4);
        assertReadTestData(null, DATA_SECOND_KEYFRAME_INDEX);
        assertNoSamplesToRead(FORMAT_2);
        success = sampleQueue.seekTo(SAMPLE_TIMESTAMPS[0], false);
        assertThat(success).isTrue();
        assertThat(sampleQueue.getReadIndex()).isEqualTo(0);
        assertReadTestData();
        assertNoSamplesToRead(FORMAT_2);
    }

    @Test
    public void testDiscardToEnd() {
        writeTestData();
        sampleQueue.discardToEnd();
        assertThat(sampleQueue.getFirstIndex()).isEqualTo(8);
        assertThat(sampleQueue.getReadIndex()).isEqualTo(8);
        assertAllocationCount(0);
        assertReadFormat(false, FORMAT_2);
        writeTestData();
        assertReadTestData(FORMAT_2);
    }

    @Test
    public void testDiscardToStopAtReadPosition() {
        writeTestData();
        sampleQueue.discardTo(LAST_SAMPLE_TIMESTAMP, false, true);
        assertThat(sampleQueue.getFirstIndex()).isEqualTo(0);
        assertThat(sampleQueue.getReadIndex()).isEqualTo(0);
        assertAllocationCount(10);
        assertReadTestData(null, 0, 1);
        sampleQueue.discardTo(SAMPLE_TIMESTAMPS[1] - 1, false, true);
        assertThat(sampleQueue.getFirstIndex()).isEqualTo(0);
        assertThat(sampleQueue.getReadIndex()).isEqualTo(1);
        assertAllocationCount(10);
        sampleQueue.discardTo(SAMPLE_TIMESTAMPS[1], false, true);
        assertThat(sampleQueue.getFirstIndex()).isEqualTo(1);
        assertThat(sampleQueue.getReadIndex()).isEqualTo(1);
        assertAllocationCount(9);
        sampleQueue.discardTo(LAST_SAMPLE_TIMESTAMP, false, true);
        assertThat(sampleQueue.getFirstIndex()).isEqualTo(1);
        assertThat(sampleQueue.getReadIndex()).isEqualTo(1);
        assertAllocationCount(9);
        assertReadTestData(FORMAT_1, 1, 7);
        assertThat(sampleQueue.getFirstIndex()).isEqualTo(1);
        assertThat(sampleQueue.getReadIndex()).isEqualTo(8);
        sampleQueue.discardTo(LAST_SAMPLE_TIMESTAMP - 1, false, true);
        assertThat(sampleQueue.getFirstIndex()).isEqualTo(6);
        assertThat(sampleQueue.getReadIndex()).isEqualTo(8);
        assertAllocationCount(3);
        sampleQueue.discardTo(LAST_SAMPLE_TIMESTAMP, false, true);
        assertThat(sampleQueue.getFirstIndex()).isEqualTo(7);
        assertThat(sampleQueue.getReadIndex()).isEqualTo(8);
        assertAllocationCount(1);
    }

    @Test
    public void testDiscardToDontStopAtReadPosition() {
        writeTestData();
        sampleQueue.discardTo(SAMPLE_TIMESTAMPS[1] - 1, false, false);
        assertThat(sampleQueue.getFirstIndex()).isEqualTo(0);
        assertThat(sampleQueue.getReadIndex()).isEqualTo(0);
        assertAllocationCount(10);
        sampleQueue.discardTo(SAMPLE_TIMESTAMPS[1], false, false);
        assertThat(sampleQueue.getFirstIndex()).isEqualTo(1);
        assertThat(sampleQueue.getReadIndex()).isEqualTo(1);
        assertAllocationCount(9);
        assertReadTestData(FORMAT_1, 1, 7);
    }

    @Test
    public void testDiscardUpstream() {
        writeTestData();
        sampleQueue.discardUpstreamSamples(8);
        assertAllocationCount(10);
        sampleQueue.discardUpstreamSamples(7);
        assertAllocationCount(9);
        sampleQueue.discardUpstreamSamples(6);
        assertAllocationCount(7);
        sampleQueue.discardUpstreamSamples(5);
        assertAllocationCount(5);
        sampleQueue.discardUpstreamSamples(4);
        assertAllocationCount(4);
        sampleQueue.discardUpstreamSamples(3);
        assertAllocationCount(3);
        sampleQueue.discardUpstreamSamples(2);
        assertAllocationCount(2);
        sampleQueue.discardUpstreamSamples(1);
        assertAllocationCount(1);
        sampleQueue.discardUpstreamSamples(0);
        assertAllocationCount(0);
        assertReadFormat(false, FORMAT_2);
        assertNoSamplesToRead(FORMAT_2);
    }

    @Test
    public void testDiscardUpstreamMulti() {
        writeTestData();
        sampleQueue.discardUpstreamSamples(4);
        assertAllocationCount(4);
        sampleQueue.discardUpstreamSamples(0);
        assertAllocationCount(0);
        assertReadFormat(false, FORMAT_2);
        assertNoSamplesToRead(FORMAT_2);
    }

    @Test
    public void testDiscardUpstreamBeforeRead() {
        writeTestData();
        sampleQueue.discardUpstreamSamples(4);
        assertAllocationCount(4);
        assertReadTestData(null, 0, 4);
        assertReadFormat(false, FORMAT_2);
        assertNoSamplesToRead(FORMAT_2);
    }

    @Test
    public void testDiscardUpstreamAfterRead() {
        writeTestData();
        assertReadTestData(null, 0, 3);
        sampleQueue.discardUpstreamSamples(8);
        assertAllocationCount(10);
        sampleQueue.discardToRead();
        assertAllocationCount(7);
        sampleQueue.discardUpstreamSamples(7);
        assertAllocationCount(6);
        sampleQueue.discardUpstreamSamples(6);
        assertAllocationCount(4);
        sampleQueue.discardUpstreamSamples(5);
        assertAllocationCount(2);
        sampleQueue.discardUpstreamSamples(4);
        assertAllocationCount(1);
        sampleQueue.discardUpstreamSamples(3);
        assertAllocationCount(0);
        assertReadFormat(false, FORMAT_2);
        assertNoSamplesToRead(FORMAT_2);
    }

    @Test
    public void testLargestQueuedTimestampWithDiscardUpstream() {
        writeTestData();
        assertThat(sampleQueue.getLargestQueuedTimestampUs()).isEqualTo(LAST_SAMPLE_TIMESTAMP);
        sampleQueue.discardUpstreamSamples(SAMPLE_TIMESTAMPS.length - 1);
        assertThat(sampleQueue.getLargestQueuedTimestampUs()).isEqualTo(SAMPLE_TIMESTAMPS[SAMPLE_TIMESTAMPS.length - 2]);
        sampleQueue.discardUpstreamSamples(0);
        assertThat(sampleQueue.getLargestQueuedTimestampUs()).isEqualTo(MIN_VALUE);
    }

    @Test
    public void testLargestQueuedTimestampWithDiscardUpstreamDecodeOrder() {
        long[] decodeOrderTimestamps = new long[] { 0, 3000, 2000, 1000, 4000, 7000, 6000, 5000 };
        writeTestData(DATA, SAMPLE_SIZES, SAMPLE_OFFSETS, decodeOrderTimestamps, SAMPLE_FORMATS, SAMPLE_FLAGS);
        assertThat(sampleQueue.getLargestQueuedTimestampUs()).isEqualTo(7000);
        sampleQueue.discardUpstreamSamples(SAMPLE_TIMESTAMPS.length - 2);
        assertThat(sampleQueue.getLargestQueuedTimestampUs()).isEqualTo(7000);
        sampleQueue.discardUpstreamSamples(SAMPLE_TIMESTAMPS.length - 3);
        assertThat(sampleQueue.getLargestQueuedTimestampUs()).isEqualTo(4000);
        sampleQueue.discardUpstreamSamples(0);
        assertThat(sampleQueue.getLargestQueuedTimestampUs()).isEqualTo(MIN_VALUE);
    }

    @Test
    public void testLargestQueuedTimestampWithRead() {
        writeTestData();
        assertThat(sampleQueue.getLargestQueuedTimestampUs()).isEqualTo(LAST_SAMPLE_TIMESTAMP);
        assertReadTestData();
        assertThat(sampleQueue.getLargestQueuedTimestampUs()).isEqualTo(LAST_SAMPLE_TIMESTAMP);
    }

    @Test
    public void testSetSampleOffsetBeforeData() {
        long sampleOffsetUs = 1000;
        sampleQueue.setSampleOffsetUs(sampleOffsetUs);
        writeTestData();
        assertReadTestData(null, 0, 8, sampleOffsetUs);
        assertReadEndOfStream(false);
    }

    @Test
    public void testSetSampleOffsetBetweenSamples() {
        writeTestData();
        long sampleOffsetUs = 1000;
        sampleQueue.setSampleOffsetUs(sampleOffsetUs);
        long unadjustedTimestampUs = LAST_SAMPLE_TIMESTAMP + 1234;
        writeSample(DATA, unadjustedTimestampUs, 0);
        assertReadTestData();
        assertReadFormat(false, FORMAT_2.copyWithSubsampleOffsetUs(sampleOffsetUs));
        assertReadSample(unadjustedTimestampUs + sampleOffsetUs, false, false, DATA, 0, DATA.length);
        assertReadEndOfStream(false);
    }

    @Test
    public void testAdjustUpstreamFormat() {
        String label = "label";
        sampleQueue = new SampleQueue(allocator, Assertions.checkNotNull(Looper.myLooper()), mockDrmSessionManager) {

            @Override
            public Format getAdjustedUpstreamFormat(Format format) {
                return super.getAdjustedUpstreamFormat(format.copyWithLabel(label));
            }
        };
        writeFormat(FORMAT_1);
        assertReadFormat(false, FORMAT_1.copyWithLabel(label));
        assertReadEndOfStream(false);
    }

    @Test
    public void testInvalidateUpstreamFormatAdjustment() {
        AtomicReference<String> label = new AtomicReference<>("label1");
        sampleQueue = new SampleQueue(allocator, Assertions.checkNotNull(Looper.myLooper()), mockDrmSessionManager) {

            @Override
            public Format getAdjustedUpstreamFormat(Format format) {
                return super.getAdjustedUpstreamFormat(format.copyWithLabel(label.get()));
            }
        };
        writeFormat(FORMAT_1);
        writeSample(DATA, 0, BUFFER_FLAG_KEY_FRAME);
        label.set("label2");
        sampleQueue.invalidateUpstreamFormatAdjustment();
        writeSample(DATA, 1, 0);
        assertReadFormat(false, FORMAT_1.copyWithLabel("label1"));
        assertReadSample(0, true, false, DATA, 0, DATA.length);
        assertReadFormat(false, FORMAT_1.copyWithLabel("label2"));
        assertReadSample(1, false, false, DATA, 0, DATA.length);
        assertReadEndOfStream(false);
    }

    @Test
    public void testSplice() {
        writeTestData();
        sampleQueue.splice();
        long spliceSampleTimeUs = SAMPLE_TIMESTAMPS[4];
        writeFormat(FORMAT_SPLICED);
        writeSample(DATA, spliceSampleTimeUs, C.BUFFER_FLAG_KEY_FRAME);
        assertReadTestData(null, 0, 4);
        assertReadFormat(false, FORMAT_SPLICED);
        assertReadSample(spliceSampleTimeUs, true, false, DATA, 0, DATA.length);
        assertReadEndOfStream(false);
    }

    @Test
    public void testSpliceAfterRead() {
        writeTestData();
        assertReadTestData(null, 0, 4);
        sampleQueue.splice();
        long spliceSampleTimeUs = SAMPLE_TIMESTAMPS[3];
        writeFormat(FORMAT_SPLICED);
        writeSample(DATA, spliceSampleTimeUs, C.BUFFER_FLAG_KEY_FRAME);
        assertReadTestData(SAMPLE_FORMATS[3], 4, 4);
        assertReadEndOfStream(false);
        sampleQueue.seekTo(0);
        assertReadTestData(null, 0, 4);
        sampleQueue.splice();
        spliceSampleTimeUs = SAMPLE_TIMESTAMPS[3] + 1;
        writeFormat(FORMAT_SPLICED);
        writeSample(DATA, spliceSampleTimeUs, C.BUFFER_FLAG_KEY_FRAME);
        assertReadFormat(false, FORMAT_SPLICED);
        assertReadSample(spliceSampleTimeUs, true, false, DATA, 0, DATA.length);
        assertReadEndOfStream(false);
    }

    @Test
    public void testSpliceWithSampleOffset() {
        long sampleOffsetUs = 30000;
        sampleQueue.setSampleOffsetUs(sampleOffsetUs);
        writeTestData();
        sampleQueue.splice();
        long spliceSampleTimeUs = SAMPLE_TIMESTAMPS[4];
        writeFormat(FORMAT_SPLICED);
        writeSample(DATA, spliceSampleTimeUs, C.BUFFER_FLAG_KEY_FRAME);
        assertReadTestData(null, 0, 4, sampleOffsetUs);
        assertReadFormat(false, FORMAT_SPLICED.copyWithSubsampleOffsetUs(sampleOffsetUs));
        assertReadSample(spliceSampleTimeUs + sampleOffsetUs, true, false, DATA, 0, DATA.length);
        assertReadEndOfStream(false);
    }

    private void writeTestData() {
        writeTestData(DATA, SAMPLE_SIZES, SAMPLE_OFFSETS, SAMPLE_TIMESTAMPS, SAMPLE_FORMATS, SAMPLE_FLAGS);
    }

    private void writeTestDataWithEncryptedSections() {
        writeTestData(ENCRYPTED_SAMPLE_DATA, ENCRYPTED_SAMPLE_SIZES, ENCRYPTED_SAMPLE_OFFSETS, ENCRYPTED_SAMPLE_TIMESTAMPS, ENCRYPTED_SAMPLE_FORMATS, ENCRYPTED_SAMPLES_FLAGS);
    }

    @SuppressWarnings("ReferenceEquality")
    private void writeTestData(byte[] data, int[] sampleSizes, int[] sampleOffsets, long[] sampleTimestamps, Format[] sampleFormats, int[] sampleFlags) {
        sampleQueue.sampleData(new ParsableByteArray(data), data.length);
        Format format = null;
        for (int i = 0; i < sampleTimestamps.length; i++) {
            if (sampleFormats[i] != format) {
                sampleQueue.format(sampleFormats[i]);
                format = sampleFormats[i];
            }
            sampleQueue.sampleMetadata(sampleTimestamps[i], sampleFlags[i], sampleSizes[i], sampleOffsets[i], (sampleFlags[i] & C.BUFFER_FLAG_ENCRYPTED) != 0 ? DUMMY_CRYPTO_DATA : null);
        }
    }

    private void writeFormat(Format format) {
        sampleQueue.format(format);
    }

    private void writeSample(byte[] data, long timestampUs, int sampleFlags) {
        sampleQueue.sampleData(new ParsableByteArray(data), data.length);
        sampleQueue.sampleMetadata(timestampUs, sampleFlags, data.length, 0, (sampleFlags & C.BUFFER_FLAG_ENCRYPTED) != 0 ? DUMMY_CRYPTO_DATA : null);
    }

    private void assertReadTestData() {
        assertReadTestData(null, 0);
    }

    private void assertReadTestData(Format startFormat) {
        assertReadTestData(startFormat, 0);
    }

    private void assertReadTestData(Format startFormat, int firstSampleIndex) {
        assertReadTestData(startFormat, firstSampleIndex, SAMPLE_TIMESTAMPS.length - firstSampleIndex);
    }

    private void assertReadTestData(Format startFormat, int firstSampleIndex, int sampleCount) {
        assertReadTestData(startFormat, firstSampleIndex, sampleCount, 0);
    }

    private void assertReadTestData(Format startFormat, int firstSampleIndex, int sampleCount, long sampleOffsetUs) {
        Format format = adjustFormat(startFormat, sampleOffsetUs);
        for (int i = firstSampleIndex; i < firstSampleIndex + sampleCount; i++) {
            Format testSampleFormat = adjustFormat(SAMPLE_FORMATS[i], sampleOffsetUs);
            if (!testSampleFormat.equals(format)) {
                assertReadFormat(false, testSampleFormat);
                format = testSampleFormat;
            }
            assertReadFormat(true, testSampleFormat);
            assertReadSample(SAMPLE_TIMESTAMPS[i] + sampleOffsetUs, (SAMPLE_FLAGS[i] & C.BUFFER_FLAG_KEY_FRAME) != 0, false, DATA, DATA.length - SAMPLE_OFFSETS[i] - SAMPLE_SIZES[i], SAMPLE_SIZES[i]);
        }
    }

    private void assertNoSamplesToRead(Format endFormat) {
        assertReadNothing(false);
        if (endFormat == null) {
            assertReadNothing(true);
        } else {
            assertReadFormat(true, endFormat);
        }
        assertReadEndOfStream(false);
        assertReadEndOfStream(true);
        assertReadNothing(false);
        if (endFormat == null) {
            assertReadNothing(true);
        } else {
            assertReadFormat(true, endFormat);
        }
    }

    private void assertReadNothing(boolean formatRequired) {
        clearFormatHolderAndInputBuffer();
        int result = sampleQueue.read(formatHolder, inputBuffer, formatRequired, false, 0);
        assertThat(result).isEqualTo(RESULT_NOTHING_READ);
        assertThat(formatHolder.format).isNull();
        assertInputBufferContainsNoSampleData();
        assertInputBufferHasNoDefaultFlagsSet();
    }

    private void assertReadEndOfStream(boolean formatRequired) {
        clearFormatHolderAndInputBuffer();
        int result = sampleQueue.read(formatHolder, inputBuffer, formatRequired, true, 0);
        assertThat(result).isEqualTo(RESULT_BUFFER_READ);
        assertThat(formatHolder.format).isNull();
        assertInputBufferContainsNoSampleData();
        assertThat(inputBuffer.isEndOfStream()).isTrue();
        assertThat(inputBuffer.isDecodeOnly()).isFalse();
        assertThat(inputBuffer.isEncrypted()).isFalse();
    }

    private void assertReadFormat(boolean formatRequired, Format format) {
        clearFormatHolderAndInputBuffer();
        int result = sampleQueue.read(formatHolder, inputBuffer, formatRequired, false, 0);
        assertThat(result).isEqualTo(RESULT_FORMAT_READ);
        assertThat(formatHolder.format).isEqualTo(format);
        assertInputBufferContainsNoSampleData();
        assertInputBufferHasNoDefaultFlagsSet();
    }

    private void assertReadEncryptedSample(int sampleIndex) {
        byte[] sampleData = new byte[ENCRYPTED_SAMPLE_SIZES[sampleIndex]];
        Arrays.fill(sampleData, (byte) 1);
        boolean isKeyFrame = (ENCRYPTED_SAMPLES_FLAGS[sampleIndex] & C.BUFFER_FLAG_KEY_FRAME) != 0;
        boolean isEncrypted = (ENCRYPTED_SAMPLES_FLAGS[sampleIndex] & C.BUFFER_FLAG_ENCRYPTED) != 0;
        assertReadSample(ENCRYPTED_SAMPLE_TIMESTAMPS[sampleIndex], isKeyFrame, isEncrypted, sampleData, 0, ENCRYPTED_SAMPLE_SIZES[sampleIndex] - (isEncrypted ? 2 : 0));
    }

    private void assertReadSample(long timeUs, boolean isKeyFrame, boolean isEncrypted, byte[] sampleData, int offset, int length) {
        clearFormatHolderAndInputBuffer();
        int result = sampleQueue.read(formatHolder, inputBuffer, false, false, 0);
        assertThat(result).isEqualTo(RESULT_BUFFER_READ);
        assertThat(formatHolder.format).isNull();
        assertThat(inputBuffer.timeUs).isEqualTo(timeUs);
        assertThat(inputBuffer.isKeyFrame()).isEqualTo(isKeyFrame);
        assertThat(inputBuffer.isDecodeOnly()).isFalse();
        assertThat(inputBuffer.isEncrypted()).isEqualTo(isEncrypted);
        inputBuffer.flip();
        assertThat(inputBuffer.data.limit()).isEqualTo(length);
        byte[] readData = new byte[length];
        inputBuffer.data.get(readData);
        assertThat(readData).isEqualTo(copyOfRange(sampleData, offset, offset + length));
    }

    private void assertAllocationCount(int count) {
        assertThat(allocator.getTotalBytesAllocated()).isEqualTo(ALLOCATION_SIZE * count);
    }

    private void assertInputBufferContainsNoSampleData() {
        if (inputBuffer.data == null) {
            return;
        }
        inputBuffer.flip();
        assertThat(inputBuffer.data.limit()).isEqualTo(0);
    }

    private void assertInputBufferHasNoDefaultFlagsSet() {
        assertThat(inputBuffer.isEndOfStream()).isFalse();
        assertThat(inputBuffer.isDecodeOnly()).isFalse();
        assertThat(inputBuffer.isEncrypted()).isFalse();
    }

    private void clearFormatHolderAndInputBuffer() {
        formatHolder.format = null;
        inputBuffer.clear();
    }

    private static Format adjustFormat(@Nullable Format format, long sampleOffsetUs) {
        return format == null || sampleOffsetUs == 0 ? format : format.copyWithSubsampleOffsetUs(sampleOffsetUs);
    }
}
