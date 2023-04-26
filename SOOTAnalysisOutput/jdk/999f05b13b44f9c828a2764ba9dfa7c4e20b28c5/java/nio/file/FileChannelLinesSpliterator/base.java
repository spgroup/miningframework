package java.nio.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

final class FileChannelLinesSpliterator implements Spliterator<String> {

    static final Set<String> SUPPORTED_CHARSET_NAMES;

    static {
        SUPPORTED_CHARSET_NAMES = new HashSet<>();
        SUPPORTED_CHARSET_NAMES.add(StandardCharsets.UTF_8.name());
        SUPPORTED_CHARSET_NAMES.add(StandardCharsets.ISO_8859_1.name());
        SUPPORTED_CHARSET_NAMES.add(StandardCharsets.US_ASCII.name());
    }

    private final FileChannel fc;

    private final Charset cs;

    private int index;

    private final int fence;

    private ByteBuffer buffer;

    private BufferedReader reader;

    FileChannelLinesSpliterator(FileChannel fc, Charset cs, int index, int fence) {
        this.fc = fc;
        this.cs = cs;
        this.index = index;
        this.fence = fence;
    }

    private FileChannelLinesSpliterator(FileChannel fc, Charset cs, int index, int fence, ByteBuffer buffer) {
        this.fc = fc;
        this.buffer = buffer;
        this.cs = cs;
        this.index = index;
        this.fence = fence;
    }

    @Override
    public boolean tryAdvance(Consumer<? super String> action) {
        String line = readLine();
        if (line != null) {
            action.accept(line);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void forEachRemaining(Consumer<? super String> action) {
        String line;
        while ((line = readLine()) != null) {
            action.accept(line);
        }
    }

    private BufferedReader getBufferedReader() {
        ReadableByteChannel rrbc = new ReadableByteChannel() {

            @Override
            public int read(ByteBuffer dst) throws IOException {
                int bytesToRead = fence - index;
                if (bytesToRead == 0)
                    return -1;
                int bytesRead;
                if (bytesToRead < dst.remaining()) {
                    int oldLimit = dst.limit();
                    dst.limit(dst.position() + bytesToRead);
                    bytesRead = fc.read(dst, index);
                    dst.limit(oldLimit);
                } else {
                    bytesRead = fc.read(dst, index);
                }
                if (bytesRead == -1) {
                    index = fence;
                    return bytesRead;
                }
                index += bytesRead;
                return bytesRead;
            }

            @Override
            public boolean isOpen() {
                return fc.isOpen();
            }

            @Override
            public void close() throws IOException {
                fc.close();
            }
        };
        return new BufferedReader(Channels.newReader(rrbc, cs.newDecoder(), -1));
    }

    private String readLine() {
        if (reader == null) {
            reader = getBufferedReader();
            buffer = null;
        }
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ByteBuffer getMappedByteBuffer() {
        try {
            return fc.map(FileChannel.MapMode.READ_ONLY, 0, fence);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Spliterator<String> trySplit() {
        if (reader != null)
            return null;
        ByteBuffer b;
        if ((b = buffer) == null) {
            b = buffer = getMappedByteBuffer();
        }
        final int hi = fence, lo = index;
        int mid = (lo + hi) >>> 1;
        int c = b.get(mid);
        if (c == '\n') {
            mid++;
        } else if (c == '\r') {
            if (++mid < hi && b.get(mid) == '\n') {
                mid++;
            }
        } else {
            int midL = mid - 1;
            int midR = mid + 1;
            mid = 0;
            while (midL > lo && midR < hi) {
                c = b.get(midL--);
                if (c == '\n' || c == '\r') {
                    mid = midL + 2;
                    break;
                }
                c = b.get(midR++);
                if (c == '\n' || c == '\r') {
                    mid = midR;
                    if (c == '\r' && mid < hi && b.get(mid) == '\n') {
                        mid++;
                    }
                    break;
                }
            }
        }
        return (mid > lo && mid < hi) ? new FileChannelLinesSpliterator(fc, cs, lo, index = mid, b) : null;
    }

    @Override
    public long estimateSize() {
        return fence - index;
    }

    @Override
    public long getExactSizeIfKnown() {
        return -1;
    }

    @Override
    public int characteristics() {
        return Spliterator.ORDERED | Spliterator.NONNULL;
    }
}
