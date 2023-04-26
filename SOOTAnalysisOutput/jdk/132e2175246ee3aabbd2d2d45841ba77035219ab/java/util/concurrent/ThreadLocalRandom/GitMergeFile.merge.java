package java.util.concurrent;

import java.io.ObjectStreamField;
import java.util.Random;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

public class ThreadLocalRandom extends Random {

    private static long mix64(long z) {
        z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL;
        z = (z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L;
        return z ^ (z >>> 33);
    }

    private static int mix32(long z) {
        z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL;
        return (int) (((z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L) >>> 32);
    }

    boolean initialized;

    private ThreadLocalRandom() {
        initialized = true;
    }

    static final void localInit() {
        int p = probeGenerator.addAndGet(PROBE_INCREMENT);
        int probe = (p == 0) ? 1 : p;
        long seed = mix64(seeder.getAndAdd(SEEDER_INCREMENT));
        Thread t = Thread.currentThread();
        U.putLong(t, SEED, seed);
        U.putInt(t, PROBE, probe);
    }

    public static ThreadLocalRandom current() {
        if (U.getInt(Thread.currentThread(), PROBE) == 0)
            localInit();
        return instance;
    }

    public void setSeed(long seed) {
        if (initialized)
            throw new UnsupportedOperationException();
    }

    final long nextSeed() {
        Thread t;
        long r;
        U.putLong(t = Thread.currentThread(), SEED, r = U.getLong(t, SEED) + GAMMA);
        return r;
    }

    protected int next(int bits) {
        return (int) (mix64(nextSeed()) >>> (64 - bits));
    }

    final long internalNextLong(long origin, long bound) {
        long r = mix64(nextSeed());
        if (origin < bound) {
            long n = bound - origin, m = n - 1;
            if ((n & m) == 0L)
                r = (r & m) + origin;
            else if (n > 0L) {
                for (long u = r >>> 1; u + m - (r = u % n) < 0L; u = mix64(nextSeed()) >>> 1) ;
                r += origin;
            } else {
                while (r < origin || r >= bound) r = mix64(nextSeed());
            }
        }
        return r;
    }

    final int internalNextInt(int origin, int bound) {
        int r = mix32(nextSeed());
        if (origin < bound) {
            int n = bound - origin, m = n - 1;
            if ((n & m) == 0)
                r = (r & m) + origin;
            else if (n > 0) {
                for (int u = r >>> 1; u + m - (r = u % n) < 0; u = mix32(nextSeed()) >>> 1) ;
                r += origin;
            } else {
                while (r < origin || r >= bound) r = mix32(nextSeed());
            }
        }
        return r;
    }

    final double internalNextDouble(double origin, double bound) {
        double r = (nextLong() >>> 11) * DOUBLE_UNIT;
        if (origin < bound) {
            r = r * (bound - origin) + origin;
            if (r >= bound)
                r = Double.longBitsToDouble(Double.doubleToLongBits(bound) - 1);
        }
        return r;
    }

    public int nextInt() {
        return mix32(nextSeed());
    }

    public int nextInt(int bound) {
        if (bound <= 0)
            throw new IllegalArgumentException(BAD_BOUND);
        int r = mix32(nextSeed());
        int m = bound - 1;
        if ((bound & m) == 0)
            r &= m;
        else {
            for (int u = r >>> 1; u + m - (r = u % bound) < 0; u = mix32(nextSeed()) >>> 1) ;
        }
        return r;
    }

    public int nextInt(int origin, int bound) {
        if (origin >= bound)
            throw new IllegalArgumentException(BAD_RANGE);
        return internalNextInt(origin, bound);
    }

    public long nextLong() {
        return mix64(nextSeed());
    }

    public long nextLong(long bound) {
        if (bound <= 0)
            throw new IllegalArgumentException(BAD_BOUND);
        long r = mix64(nextSeed());
        long m = bound - 1;
        if ((bound & m) == 0L)
            r &= m;
        else {
            for (long u = r >>> 1; u + m - (r = u % bound) < 0L; u = mix64(nextSeed()) >>> 1) ;
        }
        return r;
    }

    public long nextLong(long origin, long bound) {
        if (origin >= bound)
            throw new IllegalArgumentException(BAD_RANGE);
        return internalNextLong(origin, bound);
    }

    public double nextDouble() {
        return (mix64(nextSeed()) >>> 11) * DOUBLE_UNIT;
    }

    public double nextDouble(double bound) {
        if (!(bound > 0.0))
            throw new IllegalArgumentException(BAD_BOUND);
        double result = (mix64(nextSeed()) >>> 11) * DOUBLE_UNIT * bound;
        return (result < bound) ? result : Double.longBitsToDouble(Double.doubleToLongBits(bound) - 1);
    }

    public double nextDouble(double origin, double bound) {
        if (!(origin < bound))
            throw new IllegalArgumentException(BAD_RANGE);
        return internalNextDouble(origin, bound);
    }

    public boolean nextBoolean() {
        return mix32(nextSeed()) < 0;
    }

    public float nextFloat() {
        return (mix32(nextSeed()) >>> 8) * FLOAT_UNIT;
    }

    public double nextGaussian() {
        Double d = nextLocalGaussian.get();
        if (d != null) {
            nextLocalGaussian.set(null);
            return d.doubleValue();
        }
        double v1, v2, s;
        do {
            v1 = 2 * nextDouble() - 1;
            v2 = 2 * nextDouble() - 1;
            s = v1 * v1 + v2 * v2;
        } while (s >= 1 || s == 0);
        double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s);
        nextLocalGaussian.set(new Double(v2 * multiplier));
        return v1 * multiplier;
    }

    public IntStream ints(long streamSize) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BAD_SIZE);
        return StreamSupport.intStream(new RandomIntsSpliterator(0L, streamSize, Integer.MAX_VALUE, 0), false);
    }

    public IntStream ints() {
        return StreamSupport.intStream(new RandomIntsSpliterator(0L, Long.MAX_VALUE, Integer.MAX_VALUE, 0), false);
    }

    public IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BAD_SIZE);
        if (randomNumberOrigin >= randomNumberBound)
            throw new IllegalArgumentException(BAD_RANGE);
        return StreamSupport.intStream(new RandomIntsSpliterator(0L, streamSize, randomNumberOrigin, randomNumberBound), false);
    }

    public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
        if (randomNumberOrigin >= randomNumberBound)
            throw new IllegalArgumentException(BAD_RANGE);
        return StreamSupport.intStream(new RandomIntsSpliterator(0L, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound), false);
    }

    public LongStream longs(long streamSize) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BAD_SIZE);
        return StreamSupport.longStream(new RandomLongsSpliterator(0L, streamSize, Long.MAX_VALUE, 0L), false);
    }

    public LongStream longs() {
        return StreamSupport.longStream(new RandomLongsSpliterator(0L, Long.MAX_VALUE, Long.MAX_VALUE, 0L), false);
    }

    public LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BAD_SIZE);
        if (randomNumberOrigin >= randomNumberBound)
            throw new IllegalArgumentException(BAD_RANGE);
        return StreamSupport.longStream(new RandomLongsSpliterator(0L, streamSize, randomNumberOrigin, randomNumberBound), false);
    }

    public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
        if (randomNumberOrigin >= randomNumberBound)
            throw new IllegalArgumentException(BAD_RANGE);
        return StreamSupport.longStream(new RandomLongsSpliterator(0L, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound), false);
    }

    public DoubleStream doubles(long streamSize) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BAD_SIZE);
        return StreamSupport.doubleStream(new RandomDoublesSpliterator(0L, streamSize, Double.MAX_VALUE, 0.0), false);
    }

    public DoubleStream doubles() {
        return StreamSupport.doubleStream(new RandomDoublesSpliterator(0L, Long.MAX_VALUE, Double.MAX_VALUE, 0.0), false);
    }

    public DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BAD_SIZE);
        if (!(randomNumberOrigin < randomNumberBound))
            throw new IllegalArgumentException(BAD_RANGE);
        return StreamSupport.doubleStream(new RandomDoublesSpliterator(0L, streamSize, randomNumberOrigin, randomNumberBound), false);
    }

    public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
        if (!(randomNumberOrigin < randomNumberBound))
            throw new IllegalArgumentException(BAD_RANGE);
        return StreamSupport.doubleStream(new RandomDoublesSpliterator(0L, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound), false);
    }

    private static final class RandomIntsSpliterator implements Spliterator.OfInt {

        long index;

        final long fence;

        final int origin;

        final int bound;

        RandomIntsSpliterator(long index, long fence, int origin, int bound) {
            this.index = index;
            this.fence = fence;
            this.origin = origin;
            this.bound = bound;
        }

        public RandomIntsSpliterator trySplit() {
            long i = index, m = (i + fence) >>> 1;
            return (m <= i) ? null : new RandomIntsSpliterator(i, index = m, origin, bound);
        }

        public long estimateSize() {
            return fence - index;
        }

        public int characteristics() {
            return (Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.NONNULL | Spliterator.IMMUTABLE);
        }

        public boolean tryAdvance(IntConsumer consumer) {
            if (consumer == null)
                throw new NullPointerException();
            long i = index, f = fence;
            if (i < f) {
                consumer.accept(ThreadLocalRandom.current().internalNextInt(origin, bound));
                index = i + 1;
                return true;
            }
            return false;
        }

        public void forEachRemaining(IntConsumer consumer) {
            if (consumer == null)
                throw new NullPointerException();
            long i = index, f = fence;
            if (i < f) {
                index = f;
                int o = origin, b = bound;
                ThreadLocalRandom rng = ThreadLocalRandom.current();
                do {
                    consumer.accept(rng.internalNextInt(o, b));
                } while (++i < f);
            }
        }
    }

    private static final class RandomLongsSpliterator implements Spliterator.OfLong {

        long index;

        final long fence;

        final long origin;

        final long bound;

        RandomLongsSpliterator(long index, long fence, long origin, long bound) {
            this.index = index;
            this.fence = fence;
            this.origin = origin;
            this.bound = bound;
        }

        public RandomLongsSpliterator trySplit() {
            long i = index, m = (i + fence) >>> 1;
            return (m <= i) ? null : new RandomLongsSpliterator(i, index = m, origin, bound);
        }

        public long estimateSize() {
            return fence - index;
        }

        public int characteristics() {
            return (Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.NONNULL | Spliterator.IMMUTABLE);
        }

        public boolean tryAdvance(LongConsumer consumer) {
            if (consumer == null)
                throw new NullPointerException();
            long i = index, f = fence;
            if (i < f) {
                consumer.accept(ThreadLocalRandom.current().internalNextLong(origin, bound));
                index = i + 1;
                return true;
            }
            return false;
        }

        public void forEachRemaining(LongConsumer consumer) {
            if (consumer == null)
                throw new NullPointerException();
            long i = index, f = fence;
            if (i < f) {
                index = f;
                long o = origin, b = bound;
                ThreadLocalRandom rng = ThreadLocalRandom.current();
                do {
                    consumer.accept(rng.internalNextLong(o, b));
                } while (++i < f);
            }
        }
    }

    private static final class RandomDoublesSpliterator implements Spliterator.OfDouble {

        long index;

        final long fence;

        final double origin;

        final double bound;

        RandomDoublesSpliterator(long index, long fence, double origin, double bound) {
            this.index = index;
            this.fence = fence;
            this.origin = origin;
            this.bound = bound;
        }

        public RandomDoublesSpliterator trySplit() {
            long i = index, m = (i + fence) >>> 1;
            return (m <= i) ? null : new RandomDoublesSpliterator(i, index = m, origin, bound);
        }

        public long estimateSize() {
            return fence - index;
        }

        public int characteristics() {
            return (Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.NONNULL | Spliterator.IMMUTABLE);
        }

        public boolean tryAdvance(DoubleConsumer consumer) {
            if (consumer == null)
                throw new NullPointerException();
            long i = index, f = fence;
            if (i < f) {
                consumer.accept(ThreadLocalRandom.current().internalNextDouble(origin, bound));
                index = i + 1;
                return true;
            }
            return false;
        }

        public void forEachRemaining(DoubleConsumer consumer) {
            if (consumer == null)
                throw new NullPointerException();
            long i = index, f = fence;
            if (i < f) {
                index = f;
                double o = origin, b = bound;
                ThreadLocalRandom rng = ThreadLocalRandom.current();
                do {
                    consumer.accept(rng.internalNextDouble(o, b));
                } while (++i < f);
            }
        }
    }

    static final int getProbe() {
        return U.getInt(Thread.currentThread(), PROBE);
    }

    static final int advanceProbe(int probe) {
        probe ^= probe << 13;
        probe ^= probe >>> 17;
        probe ^= probe << 5;
        U.putInt(Thread.currentThread(), PROBE, probe);
        return probe;
    }

    static final int nextSecondarySeed() {
        int r;
        Thread t = Thread.currentThread();
        if ((r = U.getInt(t, SECONDARY)) != 0) {
            r ^= r << 13;
            r ^= r >>> 17;
            r ^= r << 5;
        } else if ((r = mix32(seeder.getAndAdd(SEEDER_INCREMENT))) == 0)
            r = 1;
        U.putInt(t, SECONDARY, r);
        return r;
    }

    private static final long serialVersionUID = -5851777807851030925L;

    private static final ObjectStreamField[] serialPersistentFields = { new ObjectStreamField("rnd", long.class), new ObjectStreamField("initialized", boolean.class) };

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        java.io.ObjectOutputStream.PutField fields = s.putFields();
        fields.put("rnd", U.getLong(Thread.currentThread(), SEED));
        fields.put("initialized", true);
        s.writeFields();
    }

    private Object readResolve() {
        return current();
    }

    private static final long GAMMA = 0x9e3779b97f4a7c15L;

    private static final int PROBE_INCREMENT = 0x9e3779b9;

    private static final long SEEDER_INCREMENT = 0xbb67ae8584caa73bL;

    private static final double DOUBLE_UNIT = 0x1.0p-53;

    private static final float FLOAT_UNIT = 0x1.0p-24f;

    static final String BAD_BOUND = "bound must be positive";

    static final String BAD_RANGE = "bound must be greater than origin";

    static final String BAD_SIZE = "size must be non-negative";

    private static final jdk.internal.misc.Unsafe U = jdk.internal.misc.Unsafe.getUnsafe();

    private static final long SEED;

    private static final long PROBE;

    private static final long SECONDARY;

    static {
        try {
            SEED = U.objectFieldOffset(Thread.class.getDeclaredField("threadLocalRandomSeed"));
            PROBE = U.objectFieldOffset(Thread.class.getDeclaredField("threadLocalRandomProbe"));
            SECONDARY = U.objectFieldOffset(Thread.class.getDeclaredField("threadLocalRandomSecondarySeed"));
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    private static final ThreadLocal<Double> nextLocalGaussian = new ThreadLocal<>();

    private static final AtomicInteger probeGenerator = new AtomicInteger();

    static final ThreadLocalRandom instance = new ThreadLocalRandom();

    private static final AtomicLong seeder = new AtomicLong(mix64(System.currentTimeMillis()) ^ mix64(System.nanoTime()));

    static {
        if (java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<Boolean>() {

            public Boolean run() {
                return Boolean.getBoolean("java.util.secureRandomSeed");
            }
        })) {
            byte[] seedBytes = java.security.SecureRandom.getSeed(8);
            long s = (long) seedBytes[0] & 0xffL;
            for (int i = 1; i < 8; ++i) s = (s << 8) | ((long) seedBytes[i] & 0xffL);
            seeder.set(s);
        }
    }
}
