package reactor.core.publisher;

import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.LockSupport;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;
import reactor.util.concurrent.Queues;
import reactor.util.concurrent.WaitStrategy;
import sun.misc.Unsafe;
import static java.util.Arrays.copyOf;

abstract class RingBuffer<E> implements LongSupplier {

    static <T> void addSequence(final T holder, final AtomicReferenceFieldUpdater<T, Sequence[]> updater, final Sequence sequence) {
        Sequence[] updatedSequences;
        Sequence[] currentSequences;
        do {
            currentSequences = updater.get(holder);
            updatedSequences = copyOf(currentSequences, currentSequences.length + 1);
            updatedSequences[currentSequences.length] = sequence;
        } while (!updater.compareAndSet(holder, currentSequences, updatedSequences));
    }

    private static <T> int countMatching(T[] values, final T toMatch) {
        int numToRemove = 0;
        for (T value : values) {
            if (value == toMatch) {
                numToRemove++;
            }
        }
        return numToRemove;
    }

    static <T> boolean removeSequence(final T holder, final AtomicReferenceFieldUpdater<T, Sequence[]> sequenceUpdater, final Sequence sequence) {
        int numToRemove;
        Sequence[] oldSequences;
        Sequence[] newSequences;
        do {
            oldSequences = sequenceUpdater.get(holder);
            numToRemove = countMatching(oldSequences, sequence);
            if (0 == numToRemove) {
                break;
            }
            final int oldSize = oldSequences.length;
            newSequences = new Sequence[oldSize - numToRemove];
            for (int i = 0, pos = 0; i < oldSize; i++) {
                final Sequence testSequence = oldSequences[i];
                if (sequence != testSequence) {
                    newSequences[pos++] = testSequence;
                }
            }
        } while (!sequenceUpdater.compareAndSet(holder, oldSequences, newSequences));
        return numToRemove != 0;
    }

    static final long INITIAL_CURSOR_VALUE = -1L;

    static <E> RingBuffer<E> createMultiProducer(Supplier<E> factory, int bufferSize, WaitStrategy waitStrategy, Runnable spinObserver) {
        if (hasUnsafe()) {
            MultiProducerRingBuffer sequencer = new MultiProducerRingBuffer(bufferSize, waitStrategy, spinObserver);
            return new UnsafeRingBuffer<>(factory, sequencer);
        } else {
            throw new IllegalStateException("This JVM does not support sun.misc.Unsafe");
        }
    }

    static <E> RingBuffer<E> createSingleProducer(Supplier<E> factory, int bufferSize, WaitStrategy waitStrategy) {
        return createSingleProducer(factory, bufferSize, waitStrategy, null);
    }

    static <E> RingBuffer<E> createSingleProducer(Supplier<E> factory, int bufferSize, WaitStrategy waitStrategy, @Nullable Runnable spinObserver) {
        SingleProducerSequencer sequencer = new SingleProducerSequencer(bufferSize, waitStrategy, spinObserver);
        if (hasUnsafe() && Queues.isPowerOfTwo(bufferSize)) {
            return new UnsafeRingBuffer<>(factory, sequencer);
        } else {
            return new NotFunRingBuffer<>(factory, sequencer);
        }
    }

    static long getMinimumSequence(final Sequence[] sequences, long minimum) {
        for (int i = 0, n = sequences.length; i < n; i++) {
            long value = sequences[i].getAsLong();
            minimum = Math.min(minimum, value);
        }
        return minimum;
    }

    static long getMinimumSequence(@Nullable Sequence excludeSequence, final Sequence[] sequences, long minimum) {
        for (int i = 0, n = sequences.length; i < n; i++) {
            if (excludeSequence == null || sequences[i] != excludeSequence) {
                long value = sequences[i].getAsLong();
                minimum = Math.min(minimum, value);
            }
        }
        return minimum;
    }

    @SuppressWarnings("unchecked")
    static <T> T getUnsafe() {
        return (T) UnsafeSupport.getUnsafe();
    }

    static int log2(int i) {
        int r = 0;
        while ((i >>= 1) != 0) {
            ++r;
        }
        return r;
    }

    static Sequence newSequence(long init) {
        if (hasUnsafe()) {
            return new UnsafeSequence(init);
        } else {
            return new AtomicSequence(init);
        }
    }

    abstract void addGatingSequence(Sequence gatingSequence);

    abstract int bufferSize();

    abstract E get(long sequence);

    @Override
    public long getAsLong() {
        return getCursor();
    }

    abstract long getCursor();

    abstract long getMinimumGatingSequence();

    abstract long getMinimumGatingSequence(Sequence sequence);

    abstract int getPending();

    Sequence[] getSequenceReceivers() {
        return getSequencer().getGatingSequences();
    }

    abstract Reader newReader();

    abstract long next();

    abstract long next(int n);

    abstract void publish(long sequence);

    abstract boolean removeGatingSequence(Sequence sequence);

    abstract RingBufferProducer getSequencer();

    static boolean hasUnsafe() {
        return HAS_UNSAFE;
    }

    static boolean hasUnsafe0() {
        return UnsafeSupport.hasUnsafe();
    }

    private static final boolean HAS_UNSAFE = hasUnsafe0();

    interface Sequence extends LongSupplier {

        long INITIAL_VALUE = INITIAL_CURSOR_VALUE;

        void set(long value);

        boolean compareAndSet(long expectedValue, long newValue);
    }

    static final class Reader {

        private final WaitStrategy waitStrategy;

        private volatile boolean alerted = false;

        private final Sequence cursorSequence;

        private final RingBufferProducer sequenceProducer;

        Reader(final RingBufferProducer sequenceProducer, final WaitStrategy waitStrategy, final Sequence cursorSequence) {
            this.sequenceProducer = sequenceProducer;
            this.waitStrategy = waitStrategy;
            this.cursorSequence = cursorSequence;
        }

        long waitFor(final long sequence, Runnable consumer) throws InterruptedException {
            if (alerted) {
                WaitStrategy.alert();
            }
            long availableSequence = waitStrategy.waitFor(sequence, cursorSequence, consumer);
            if (availableSequence < sequence) {
                return availableSequence;
            }
            return sequenceProducer.getHighestPublishedSequence(sequence, availableSequence);
        }

        boolean isAlerted() {
            return alerted;
        }

        void alert() {
            alerted = true;
            waitStrategy.signalAllWhenBlocking();
        }

        void signal() {
            waitStrategy.signalAllWhenBlocking();
        }

        void clearAlert() {
            alerted = false;
        }
    }
}

enum UnsafeSupport {
    ;

    static final Logger logger = Loggers.getLogger(UnsafeSupport.class);

    static {
        String javaSpecVersion = System.getProperty("java.specification.version");
        logger.debug("Starting UnsafeSupport init in Java " + javaSpecVersion);
        ByteBuffer direct = ByteBuffer.allocateDirect(1);
        Unsafe unsafe;
        Object maybeUnsafe;
        try {
            final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            maybeUnsafe = unsafeField.get(null);
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
            maybeUnsafe = e;
        }
        if (maybeUnsafe instanceof Throwable) {
            unsafe = null;
            logger.debug("Unsafe unavailable - Could not get it via Field sun.misc.Unsafe.theUnsafe", (Throwable) maybeUnsafe);
        } else {
            unsafe = (Unsafe) maybeUnsafe;
            logger.trace("sun.misc.Unsafe.theUnsafe ok");
        }
        if (unsafe != null) {
            final Unsafe finalUnsafe = unsafe;
            Object maybeException;
            try {
                finalUnsafe.getClass().getDeclaredMethod("copyMemory", Object.class, long.class, Object.class, long.class, long.class);
                maybeException = null;
            } catch (NoSuchMethodException | SecurityException e) {
                maybeException = e;
            }
            if (maybeException == null) {
                logger.trace("sun.misc.Unsafe.copyMemory ok");
            } else {
                unsafe = null;
                logger.debug("Unsafe unavailable - failed on sun.misc.Unsafe.copyMemory", (Throwable) maybeException);
            }
        }
        if (unsafe != null) {
            final Unsafe finalUnsafe = unsafe;
            Object maybeAddressField;
            try {
                final Field field = Buffer.class.getDeclaredField("address");
                final long offset = finalUnsafe.objectFieldOffset(field);
                final long heapAddress = finalUnsafe.getLong(ByteBuffer.allocate(1), offset);
                final long directAddress = finalUnsafe.getLong(direct, offset);
                if (heapAddress != 0 && "1.8".equals(javaSpecVersion)) {
                    maybeAddressField = new IllegalStateException("A heap buffer must have 0 address in Java 8, got " + heapAddress);
                } else if (heapAddress == 0 && !"1.8".equals(javaSpecVersion)) {
                    maybeAddressField = new IllegalStateException("A heap buffer must have non-zero address in Java " + javaSpecVersion);
                } else if (directAddress == 0) {
                    maybeAddressField = new IllegalStateException("A direct buffer must have non-zero address");
                } else {
                    maybeAddressField = field;
                }
            } catch (NoSuchFieldException | SecurityException e) {
                maybeAddressField = e;
            }
            if (maybeAddressField instanceof Throwable) {
                logger.debug("Unsafe unavailable - failed on java.nio.Buffer.address", (Throwable) maybeAddressField);
                unsafe = null;
            } else {
                logger.trace("java.nio.Buffer.address ok");
                logger.debug("Unsafe is available");
            }
        }
        UNSAFE = unsafe;
    }

    static Unsafe getUnsafe() {
        return UNSAFE;
    }

    static boolean hasUnsafe() {
        return UNSAFE != null;
    }

    private static final Unsafe UNSAFE;
}

abstract class RingBufferProducer {

    static final AtomicReferenceFieldUpdater<RingBufferProducer, RingBuffer.Sequence[]> SEQUENCE_UPDATER = AtomicReferenceFieldUpdater.newUpdater(RingBufferProducer.class, RingBuffer.Sequence[].class, "gatingSequences");

    final Runnable spinObserver;

    final int bufferSize;

    final WaitStrategy waitStrategy;

    final RingBuffer.Sequence cursor = RingBuffer.newSequence(RingBuffer.INITIAL_CURSOR_VALUE);

    volatile RingBuffer.Sequence[] gatingSequences = new RingBuffer.Sequence[0];

    RingBufferProducer(int bufferSize, WaitStrategy waitStrategy, @Nullable Runnable spinObserver) {
        this.spinObserver = spinObserver;
        this.bufferSize = bufferSize;
        this.waitStrategy = waitStrategy;
    }

    final long getCursor() {
        return cursor.getAsLong();
    }

    final int getBufferSize() {
        return bufferSize;
    }

    final void addGatingSequence(RingBuffer.Sequence gatingSequence) {
        RingBuffer.addSequence(this, SEQUENCE_UPDATER, gatingSequence);
    }

    boolean removeGatingSequence(RingBuffer.Sequence sequence) {
        return RingBuffer.removeSequence(this, SEQUENCE_UPDATER, sequence);
    }

    long getMinimumSequence(@Nullable RingBuffer.Sequence excludeSequence) {
        return RingBuffer.getMinimumSequence(excludeSequence, gatingSequences, cursor.getAsLong());
    }

    RingBuffer.Reader newBarrier() {
        return new RingBuffer.Reader(this, waitStrategy, cursor);
    }

    abstract long getHighestPublishedSequence(long nextSequence, long availableSequence);

    abstract long getPending();

    abstract long next();

    abstract long next(int n);

    abstract void publish(long sequence);

    RingBuffer.Sequence[] getGatingSequences() {
        return gatingSequences;
    }
}

abstract class SingleProducerSequencerPad extends RingBufferProducer {

    protected long p1, p2, p3, p4, p5, p6, p7;

    SingleProducerSequencerPad(int bufferSize, WaitStrategy waitStrategy, @Nullable Runnable spinObserver) {
        super(bufferSize, waitStrategy, spinObserver);
    }
}

abstract class SingleProducerSequencerFields extends SingleProducerSequencerPad {

    SingleProducerSequencerFields(int bufferSize, WaitStrategy waitStrategy, @Nullable Runnable spinObserver) {
        super(bufferSize, waitStrategy, spinObserver);
    }

    protected long nextValue = RingBuffer.Sequence.INITIAL_VALUE;

    protected long cachedValue = RingBuffer.Sequence.INITIAL_VALUE;
}

final class SingleProducerSequencer extends SingleProducerSequencerFields {

    protected long p1, p2, p3, p4, p5, p6, p7;

    SingleProducerSequencer(int bufferSize, final WaitStrategy waitStrategy, @Nullable Runnable spinObserver) {
        super(bufferSize, waitStrategy, spinObserver);
    }

    @Override
    long next() {
        return next(1);
    }

    @Override
    long next(int n) {
        long nextValue = this.nextValue;
        long nextSequence = nextValue + n;
        long wrapPoint = nextSequence - bufferSize;
        long cachedGatingSequence = this.cachedValue;
        if (wrapPoint > cachedGatingSequence || cachedGatingSequence > nextValue) {
            long minSequence;
            while (wrapPoint > (minSequence = RingBuffer.getMinimumSequence(gatingSequences, nextValue))) {
                if (spinObserver != null) {
                    spinObserver.run();
                }
                LockSupport.parkNanos(1L);
            }
            this.cachedValue = minSequence;
        }
        this.nextValue = nextSequence;
        return nextSequence;
    }

    @Override
    public long getPending() {
        long nextValue = this.nextValue;
        long consumed = RingBuffer.getMinimumSequence(gatingSequences, nextValue);
        long produced = nextValue;
        return produced - consumed;
    }

    @Override
    void publish(long sequence) {
        cursor.set(sequence);
        waitStrategy.signalAllWhenBlocking();
    }

    @Override
    long getHighestPublishedSequence(long lowerBound, long availableSequence) {
        return availableSequence;
    }
}

abstract class NotFunRingBufferFields<E> extends RingBuffer<E> {

    private final long indexMask;

    private final Object[] entries;

    final int bufferSize;

    final RingBufferProducer sequenceProducer;

    NotFunRingBufferFields(Supplier<E> eventFactory, RingBufferProducer sequenceProducer) {
        this.sequenceProducer = sequenceProducer;
        this.bufferSize = sequenceProducer.getBufferSize();
        this.indexMask = bufferSize - 1;
        this.entries = new Object[sequenceProducer.getBufferSize()];
        fill(eventFactory);
    }

    private void fill(Supplier<E> eventFactory) {
        for (int i = 0; i < bufferSize; i++) {
            entries[i] = eventFactory.get();
        }
    }

    @SuppressWarnings("unchecked")
    final E elementAt(long sequence) {
        return (E) entries[(int) (sequence & indexMask)];
    }
}

final class NotFunRingBuffer<E> extends NotFunRingBufferFields<E> {

    NotFunRingBuffer(Supplier<E> eventFactory, RingBufferProducer sequenceProducer) {
        super(eventFactory, sequenceProducer);
    }

    @Override
    E get(long sequence) {
        return elementAt(sequence);
    }

    @Override
    long next() {
        return next(1);
    }

    @Override
    long next(int n) {
        return sequenceProducer.next(n);
    }

    @Override
    void addGatingSequence(Sequence gatingSequence) {
        sequenceProducer.addGatingSequence(gatingSequence);
    }

    @Override
    long getMinimumGatingSequence() {
        return getMinimumGatingSequence(null);
    }

    @Override
    long getMinimumGatingSequence(@Nullable Sequence sequence) {
        return sequenceProducer.getMinimumSequence(sequence);
    }

    @Override
    boolean removeGatingSequence(Sequence sequence) {
        return sequenceProducer.removeGatingSequence(sequence);
    }

    @Override
    Reader newReader() {
        return sequenceProducer.newBarrier();
    }

    @Override
    long getCursor() {
        return sequenceProducer.getCursor();
    }

    @Override
    int bufferSize() {
        return bufferSize;
    }

    @Override
    void publish(long sequence) {
        sequenceProducer.publish(sequence);
    }

    @Override
    int getPending() {
        return (int) sequenceProducer.getPending();
    }

    @Override
    RingBufferProducer getSequencer() {
        return sequenceProducer;
    }
}

final class AtomicSequence extends RhsPadding implements LongSupplier, RingBuffer.Sequence {

    private static final AtomicLongFieldUpdater<Value> UPDATER = AtomicLongFieldUpdater.newUpdater(Value.class, "value");

    AtomicSequence(final long initialValue) {
        UPDATER.lazySet(this, initialValue);
    }

    @Override
    public long getAsLong() {
        return value;
    }

    @Override
    public void set(final long value) {
        UPDATER.set(this, value);
    }

    @Override
    public boolean compareAndSet(final long expectedValue, final long newValue) {
        return UPDATER.compareAndSet(this, expectedValue, newValue);
    }
}

abstract class RingBufferPad<E> extends RingBuffer<E> {

    protected long p1, p2, p3, p4, p5, p6, p7;
}

abstract class RingBufferFields<E> extends RingBufferPad<E> {

    private static final int BUFFER_PAD;

    private static final long REF_ARRAY_BASE;

    private static final int REF_ELEMENT_SHIFT;

    private static final Unsafe UNSAFE = RingBuffer.getUnsafe();

    static {
        final int scale = UNSAFE.arrayIndexScale(Object[].class);
        if (4 == scale) {
            REF_ELEMENT_SHIFT = 2;
        } else if (8 == scale) {
            REF_ELEMENT_SHIFT = 3;
        } else {
            throw new IllegalStateException("Unknown pointer size");
        }
        BUFFER_PAD = 128 / scale;
        REF_ARRAY_BASE = UNSAFE.arrayBaseOffset(Object[].class) + (BUFFER_PAD << REF_ELEMENT_SHIFT);
    }

    private final long indexMask;

    private final Object[] entries;

    protected final int bufferSize;

    protected final RingBufferProducer sequenceProducer;

    RingBufferFields(Supplier<E> eventFactory, RingBufferProducer sequenceProducer) {
        this.sequenceProducer = sequenceProducer;
        this.bufferSize = sequenceProducer.getBufferSize();
        this.indexMask = bufferSize - 1;
        this.entries = new Object[sequenceProducer.getBufferSize() + 2 * BUFFER_PAD];
        fill(eventFactory);
    }

    private void fill(Supplier<E> eventFactory) {
        for (int i = 0; i < bufferSize; i++) {
            entries[BUFFER_PAD + i] = eventFactory.get();
        }
    }

    @SuppressWarnings("unchecked")
    final E elementAt(long sequence) {
        return (E) UNSAFE.getObject(entries, REF_ARRAY_BASE + ((sequence & indexMask) << REF_ELEMENT_SHIFT));
    }
}

final class UnsafeRingBuffer<E> extends RingBufferFields<E> {

    protected long p1, p2, p3, p4, p5, p6, p7;

    UnsafeRingBuffer(Supplier<E> eventFactory, RingBufferProducer sequenceProducer) {
        super(eventFactory, sequenceProducer);
    }

    @Override
    E get(long sequence) {
        return elementAt(sequence);
    }

    @Override
    long next() {
        return sequenceProducer.next();
    }

    @Override
    long next(int n) {
        return sequenceProducer.next(n);
    }

    @Override
    void addGatingSequence(Sequence gatingSequence) {
        sequenceProducer.addGatingSequence(gatingSequence);
    }

    @Override
    long getMinimumGatingSequence() {
        return getMinimumGatingSequence(null);
    }

    @Override
    long getMinimumGatingSequence(@Nullable Sequence sequence) {
        return sequenceProducer.getMinimumSequence(sequence);
    }

    @Override
    boolean removeGatingSequence(Sequence sequence) {
        return sequenceProducer.removeGatingSequence(sequence);
    }

    @Override
    Reader newReader() {
        return sequenceProducer.newBarrier();
    }

    @Override
    long getCursor() {
        return sequenceProducer.getCursor();
    }

    @Override
    int bufferSize() {
        return bufferSize;
    }

    @Override
    void publish(long sequence) {
        sequenceProducer.publish(sequence);
    }

    @Override
    int getPending() {
        return (int) sequenceProducer.getPending();
    }

    @Override
    RingBufferProducer getSequencer() {
        return sequenceProducer;
    }
}

class LhsPadding {

    protected long p1, p2, p3, p4, p5, p6, p7;
}

class Value extends LhsPadding {

    protected volatile long value;
}

class RhsPadding extends Value {

    protected long p9, p10, p11, p12, p13, p14, p15;
}

final class UnsafeSequence extends RhsPadding implements RingBuffer.Sequence, LongSupplier {

    private static final Unsafe UNSAFE;

    private static final long VALUE_OFFSET;

    static {
        UNSAFE = RingBuffer.getUnsafe();
        try {
            VALUE_OFFSET = UNSAFE.objectFieldOffset(Value.class.getDeclaredField("value"));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    UnsafeSequence(final long initialValue) {
        UNSAFE.putOrderedLong(this, VALUE_OFFSET, initialValue);
    }

    @Override
    public long getAsLong() {
        return value;
    }

    @Override
    public void set(final long value) {
        UNSAFE.putOrderedLong(this, VALUE_OFFSET, value);
    }

    @Override
    public boolean compareAndSet(final long expectedValue, final long newValue) {
        return UNSAFE.compareAndSwapLong(this, VALUE_OFFSET, expectedValue, newValue);
    }
}

final class MultiProducerRingBuffer extends RingBufferProducer {

    private static final Unsafe UNSAFE = RingBuffer.getUnsafe();

    private static final long BASE = UNSAFE.arrayBaseOffset(int[].class);

    private static final long SCALE = UNSAFE.arrayIndexScale(int[].class);

    private final RingBuffer.Sequence gatingSequenceCache = new UnsafeSequence(RingBuffer.INITIAL_CURSOR_VALUE);

    private final int[] availableBuffer;

    private final int indexMask;

    private final int indexShift;

    MultiProducerRingBuffer(int bufferSize, final WaitStrategy waitStrategy, Runnable spinObserver) {
        super(bufferSize, waitStrategy, spinObserver);
        availableBuffer = new int[bufferSize];
        indexMask = bufferSize - 1;
        indexShift = RingBuffer.log2(bufferSize);
        initialiseAvailableBuffer();
    }

    @Override
    long next() {
        return next(1);
    }

    @Override
    long next(int n) {
        long current;
        long next;
        do {
            current = cursor.getAsLong();
            next = current + n;
            long wrapPoint = next - bufferSize;
            long cachedGatingSequence = gatingSequenceCache.getAsLong();
            if (wrapPoint > cachedGatingSequence || cachedGatingSequence > current) {
                long gatingSequence = RingBuffer.getMinimumSequence(gatingSequences, current);
                if (wrapPoint > gatingSequence) {
                    if (spinObserver != null) {
                        spinObserver.run();
                    }
                    LockSupport.parkNanos(1);
                    continue;
                }
                gatingSequenceCache.set(gatingSequence);
            } else if (cursor.compareAndSet(current, next)) {
                break;
            }
        } while (true);
        return next;
    }

    @Override
    long getPending() {
        long consumed = RingBuffer.getMinimumSequence(gatingSequences, cursor.getAsLong());
        long produced = cursor.getAsLong();
        return produced - consumed;
    }

    private void initialiseAvailableBuffer() {
        for (int i = availableBuffer.length - 1; i != 0; i--) {
            setAvailableBufferValue(i, -1);
        }
        setAvailableBufferValue(0, -1);
    }

    @Override
    void publish(final long sequence) {
        setAvailable(sequence);
        waitStrategy.signalAllWhenBlocking();
    }

    private void setAvailable(final long sequence) {
        setAvailableBufferValue(calculateIndex(sequence), calculateAvailabilityFlag(sequence));
    }

    private void setAvailableBufferValue(int index, int flag) {
        long bufferAddress = (index * SCALE) + BASE;
        UNSAFE.putOrderedInt(availableBuffer, bufferAddress, flag);
    }

    boolean isAvailable(long sequence) {
        int index = calculateIndex(sequence);
        int flag = calculateAvailabilityFlag(sequence);
        long bufferAddress = (index * SCALE) + BASE;
        return UNSAFE.getIntVolatile(availableBuffer, bufferAddress) == flag;
    }

    @Override
    long getHighestPublishedSequence(long lowerBound, long availableSequence) {
        for (long sequence = lowerBound; sequence <= availableSequence; sequence++) {
            if (!isAvailable(sequence)) {
                return sequence - 1;
            }
        }
        return availableSequence;
    }

    private int calculateAvailabilityFlag(final long sequence) {
        return (int) (sequence >>> indexShift);
    }

    private int calculateIndex(final long sequence) {
        return ((int) sequence) & indexMask;
    }
}