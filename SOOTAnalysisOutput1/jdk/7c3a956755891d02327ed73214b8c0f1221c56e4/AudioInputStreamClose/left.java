import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;
import javax.sound.sampled.spi.AudioFileWriter;
import static java.util.ServiceLoader.load;
import static javax.sound.sampled.AudioFileFormat.Type.AIFC;
import static javax.sound.sampled.AudioFileFormat.Type.AIFF;
import static javax.sound.sampled.AudioFileFormat.Type.AU;
import static javax.sound.sampled.AudioFileFormat.Type.SND;
import static javax.sound.sampled.AudioFileFormat.Type.WAVE;
import static javax.sound.sampled.AudioSystem.NOT_SPECIFIED;

public final class AudioInputStreamClose {

    static final class StreamWrapper extends BufferedInputStream {

        private boolean open = true;

        StreamWrapper(final InputStream in) {
            super(in);
        }

        @Override
        public void close() throws IOException {
            super.close();
            open = false;
        }

        boolean isOpen() {
            return open;
        }
    }

    private static final List<AudioFormat> formats = new ArrayList<>(23000);

    private static final AudioFormat.Encoding[] encodings = { AudioFormat.Encoding.ALAW, AudioFormat.Encoding.ULAW, AudioFormat.Encoding.PCM_SIGNED, AudioFormat.Encoding.PCM_UNSIGNED, AudioFormat.Encoding.PCM_FLOAT, new AudioFormat.Encoding("Test") };

    private static final int[] sampleBits = { 1, 4, 8, 11, 16, 20, 24, 32 };

    private static final int[] channels = { 1, 2, 3, 4, 5 };

    private static final AudioFileFormat.Type[] types = { WAVE, AU, AIFF, AIFC, SND, new AudioFileFormat.Type("TestName", "TestExt") };

    private static final int FRAME_LENGTH = 10;

    static {
        for (final int sampleSize : sampleBits) {
            for (final int channel : channels) {
                for (final AudioFormat.Encoding enc : encodings) {
                    final int frameSize = ((sampleSize + 7) / 8) * channel;
                    formats.add(new AudioFormat(enc, 44100, sampleSize, channel, frameSize, 44100, true));
                    formats.add(new AudioFormat(enc, 44100, sampleSize, channel, frameSize, 44100, false));
                }
            }
        }
    }

    public static void main(final String[] args) throws IOException {
        for (final AudioFileWriter afw : load(AudioFileWriter.class)) {
            for (final AudioFileReader afr : load(AudioFileReader.class)) {
                for (final AudioFileFormat.Type type : types) {
                    for (final AudioFormat from : formats) {
                        test(afw, afr, type, getStream(from, true));
                        test(afw, afr, type, getStream(from, false));
                    }
                }
            }
        }
    }

    private static void test(final AudioFileWriter afw, final AudioFileReader afr, final AudioFileFormat.Type type, final AudioInputStream ais) throws IOException {
        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            afw.write(ais, type, out);
            final InputStream input = new ByteArrayInputStream(out.toByteArray());
            final StreamWrapper wrapper = new StreamWrapper(input);
            afr.getAudioInputStream(wrapper).close();
            if (wrapper.isOpen()) {
                System.err.println("Writer = " + afw);
                System.err.println("Reader = " + afr);
                throw new RuntimeException("Stream was not closed");
            }
        } catch (IOException | IllegalArgumentException | UnsupportedAudioFileException ignored) {
        }
    }

    private static AudioInputStream getStream(final AudioFormat format, final boolean frameLength) {
        final int dataSize = FRAME_LENGTH * format.getFrameSize();
        byte[] buf = new byte[dataSize];
        final InputStream in = new ByteArrayInputStream(buf);
        if (frameLength) {
            return new AudioInputStream(in, format, FRAME_LENGTH);
        } else {
            return new AudioInputStream(in, format, NOT_SPECIFIED);
        }
    }
}
