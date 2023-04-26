package com.google.android.exoplayer2.testutil;

import static com.google.common.truth.Truth.assertThat;
import android.content.Context;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.extractor.Extractor;
import com.google.android.exoplayer2.extractor.ExtractorInput;
import com.google.android.exoplayer2.extractor.ExtractorOutput;
import com.google.android.exoplayer2.extractor.PositionHolder;
import com.google.android.exoplayer2.extractor.SeekMap;
import com.google.android.exoplayer2.testutil.FakeExtractorInput.SimulatedIOException;
import com.google.android.exoplayer2.util.Assertions;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;

public final class ExtractorAsserts {

    private static Context robolectricContext;

    static {
        try {
            Class<?> runtimeEnvironmentClass = Class.forName("org.robolectric.RuntimeEnvironment");
            Field applicationField = runtimeEnvironmentClass.getDeclaredField("application");
            robolectricContext = (Context) applicationField.get(null);
        } catch (ClassNotFoundException e) {
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
    }

    public interface ExtractorFactory {

        Extractor create();
    }

    private static final String DUMP_EXTENSION = ".dump";

    private static final String UNKNOWN_LENGTH_EXTENSION = ".unklen" + DUMP_EXTENSION;

    public static void assertBehavior(ExtractorFactory factory, String file) throws IOException, InterruptedException {
        Extractor extractor = factory.create();
        extractor.seek(0, 0);
        extractor.release();
        byte[] fileData = TestUtil.getByteArray(robolectricContext, file);
        assertOutput(factory, file, fileData, robolectricContext);
    }

    public static void assertBehavior(ExtractorFactory factory, String file, Context context) throws IOException, InterruptedException {
        Extractor extractor = factory.create();
        extractor.seek(0, 0);
        extractor.release();
        byte[] fileData = TestUtil.getByteArray(context, file);
        assertOutput(factory, file, fileData, context);
    }

    public static void assertOutput(ExtractorFactory factory, String file, byte[] data, Context context) throws IOException, InterruptedException {
        assertOutput(factory.create(), file, data, context, true, false, false, false);
        assertOutput(factory.create(), file, data, context, true, false, false, true);
        assertOutput(factory.create(), file, data, context, true, false, true, false);
        assertOutput(factory.create(), file, data, context, true, false, true, true);
        assertOutput(factory.create(), file, data, context, true, true, false, false);
        assertOutput(factory.create(), file, data, context, true, true, false, true);
        assertOutput(factory.create(), file, data, context, true, true, true, false);
        assertOutput(factory.create(), file, data, context, true, true, true, true);
        assertOutput(factory.create(), file, data, context, false, false, false, false);
    }

    private static FakeExtractorOutput assertOutput(Extractor extractor, String file, byte[] data, Context context, boolean sniffFirst, boolean simulateIOErrors, boolean simulateUnknownLength, boolean simulatePartialReads) throws IOException, InterruptedException {
        FakeExtractorInput input = new FakeExtractorInput.Builder().setData(data).setSimulateIOErrors(simulateIOErrors).setSimulateUnknownLength(simulateUnknownLength).setSimulatePartialReads(simulatePartialReads).build();
        if (sniffFirst) {
            assertThat(TestUtil.sniffTestData(extractor, input)).isTrue();
            input.resetPeekPosition();
        }
        FakeExtractorOutput extractorOutput = consumeTestData(extractor, input, 0, true);
        if (simulateUnknownLength && assetExists(context, file + UNKNOWN_LENGTH_EXTENSION)) {
            extractorOutput.assertOutput(context, file + UNKNOWN_LENGTH_EXTENSION);
        } else {
            extractorOutput.assertOutput(context, file + ".0" + DUMP_EXTENSION);
        }
        SeekMap seekMap = extractorOutput.seekMap;
        if (seekMap.isSeekable()) {
            long durationUs = seekMap.getDurationUs();
            for (int j = 0; j < 4; j++) {
                long timeUs = (durationUs * j) / 3;
                long position = seekMap.getSeekPoints(timeUs).first.position;
                input.setPosition((int) position);
                for (int i = 0; i < extractorOutput.numberOfTracks; i++) {
                    extractorOutput.trackOutputs.valueAt(i).clear();
                }
                consumeTestData(extractor, input, timeUs, extractorOutput, false);
                extractorOutput.assertOutput(context, file + '.' + j + DUMP_EXTENSION);
            }
        }
        return extractorOutput;
    }

    public static void assertThrows(ExtractorFactory factory, String sampleFile, Context context, Class<? extends Throwable> expectedThrowable) throws IOException, InterruptedException {
        byte[] fileData = TestUtil.getByteArray(context, sampleFile);
        assertThrows(factory, fileData, expectedThrowable);
    }

    private static void assertThrows(ExtractorFactory factory, byte[] fileData, Class<? extends Throwable> expectedThrowable) throws IOException, InterruptedException {
        assertThrows(factory.create(), fileData, expectedThrowable, false, false, false);
        assertThrows(factory.create(), fileData, expectedThrowable, true, false, false);
        assertThrows(factory.create(), fileData, expectedThrowable, false, true, false);
        assertThrows(factory.create(), fileData, expectedThrowable, true, true, false);
        assertThrows(factory.create(), fileData, expectedThrowable, false, false, true);
        assertThrows(factory.create(), fileData, expectedThrowable, true, false, true);
        assertThrows(factory.create(), fileData, expectedThrowable, false, true, true);
        assertThrows(factory.create(), fileData, expectedThrowable, true, true, true);
    }

    private static void assertThrows(Extractor extractor, byte[] fileData, Class<? extends Throwable> expectedThrowable, boolean simulateIOErrors, boolean simulateUnknownLength, boolean simulatePartialReads) throws IOException, InterruptedException {
        FakeExtractorInput input = new FakeExtractorInput.Builder().setData(fileData).setSimulateIOErrors(simulateIOErrors).setSimulateUnknownLength(simulateUnknownLength).setSimulatePartialReads(simulatePartialReads).build();
        try {
            consumeTestData(extractor, input, 0, true);
            throw new AssertionError(expectedThrowable.getSimpleName() + " expected but not thrown");
        } catch (Throwable throwable) {
            if (expectedThrowable.equals(throwable.getClass())) {
                return;
            }
            throw throwable;
        }
    }

    private ExtractorAsserts() {
    }

    private static FakeExtractorOutput consumeTestData(Extractor extractor, FakeExtractorInput input, long timeUs, boolean retryFromStartIfLive) throws IOException, InterruptedException {
        FakeExtractorOutput output = new FakeExtractorOutput();
        extractor.init(output);
        consumeTestData(extractor, input, timeUs, output, retryFromStartIfLive);
        return output;
    }

    private static void consumeTestData(Extractor extractor, FakeExtractorInput input, long timeUs, FakeExtractorOutput output, boolean retryFromStartIfLive) throws IOException, InterruptedException {
        extractor.seek(input.getPosition(), timeUs);
        PositionHolder seekPositionHolder = new PositionHolder();
        int readResult = Extractor.RESULT_CONTINUE;
        while (readResult != Extractor.RESULT_END_OF_INPUT) {
            try {
                seekPositionHolder.position = Long.MIN_VALUE;
                readResult = extractor.read(input, seekPositionHolder);
                if (readResult == Extractor.RESULT_SEEK) {
                    long seekPosition = seekPositionHolder.position;
                    Assertions.checkState(0 <= seekPosition && seekPosition <= Integer.MAX_VALUE);
                    input.setPosition((int) seekPosition);
                }
            } catch (SimulatedIOException e) {
                if (!retryFromStartIfLive) {
                    continue;
                }
                boolean isOnDemand = input.getLength() != C.LENGTH_UNSET || (output.seekMap != null && output.seekMap.getDurationUs() != C.TIME_UNSET);
                if (isOnDemand) {
                    continue;
                }
                input.setPosition(0);
                for (int i = 0; i < output.numberOfTracks; i++) {
                    output.trackOutputs.valueAt(i).clear();
                }
                extractor.seek(0, 0);
            }
        }
    }

    private static boolean assetExists(Context context, String fileName) throws IOException {
        int i = fileName.lastIndexOf('/');
        String path = i >= 0 ? fileName.substring(0, i) : "";
        String file = i >= 0 ? fileName.substring(i + 1) : fileName;
        return Arrays.asList(context.getResources().getAssets().list(path)).contains(file);
    }
}