package java.util;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

public final class SplittableRandom {

    private static final long GOLDEN_GAMMA = 0x9e3779b97f4a7c15L;

    private static final double DOUBLE_UNIT = 0x1.0p-53;

    private long seed;

    private final long gamma;

    private SplittableRandom(long seed, long gamma) {
        this.seed = seed;
        this.gamma = gamma;
    }

    private static long mix64(long z) {
        z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
        return z ^ (z >>> 31);
    }

    private static int mix32(long z) {
        z = (z ^ (z >>> 33)) * 0x62a9d9ed799705f5L;
        return (int) (((z ^ (z >>> 28)) * 0xcb24d0a5c88c35b3L) >>> 32);
    }

    private static long mixGamma(long z) {
        z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdL;
        z = (z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53L;
        z = (z ^ (z >>> 33)) | 1L;
        int n = Long.bitCount(z ^ (z >>> 1));
        return (n < 24) ? z ^ 0xaaaaaaaaaaaaaaaaL : z;
    }

    private long nextSeed() {
        return seed += gamma;
    }

    static final String BAD_BOUND = "bound must be positive";

    static final String BAD_RANGE = "bound must be greater than origin";

    static final String BAD_SIZE = "size must be non-negative";

    private static final AtomicLong defaultGen = new AtomicLong(mix64(System.currentTimeMillis()) ^ mix64(System.nanoTime()));

    static {
        if (java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<Boolean>() {

            public Boolean run() {
                return Boolean.getBoolean("java.util.secureRandomSeed");
            }
        })) {
            byte[] seedBytes = java.security.SecureRandom.getSeed(8);
            long s = (long) seedBytes[0] & 0xffL;
            for (int i = 1; i < 8; ++i) s = (s << 8) | ((long) seedBytes[i] & 0xffL);
            defaultGen.set(s);
        }
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

    public SplittableRandom(long seed) {
        this(seed, GOLDEN_GAMMA);
    }

    public SplittableRandom() {
        long s = defaultGen.getAndAdd(2 * GOLDEN_GAMMA);
        this.seed = mix64(s);
        this.gamma = mixGamma(s + GOLDEN_GAMMA);
    }

    public SplittableRandom split() {
        return new SplittableRandom(nextLong(), mixGamma(nextSeed()));
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

    public IntStream ints(long streamSize) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BAD_SIZE);
        return StreamSupport.intStream(new RandomIntsSpliterator(this, 0L, streamSize, Integer.MAX_VALUE, 0), false);
    }

    public IntStream ints() {
        return StreamSupport.intStream(new RandomIntsSpliterator(this, 0L, Long.MAX_VALUE, Integer.MAX_VALUE, 0), false);
    }

    public IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BAD_SIZE);
        if (randomNumberOrigin >= randomNumberBound)
            throw new IllegalArgumentException(BAD_RANGE);
        return StreamSupport.intStream(new RandomIntsSpliterator(this, 0L, streamSize, randomNumberOrigin, randomNumberBound), false);
    }

    public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
        if (randomNumberOrigin >= randomNumberBound)
            throw new IllegalArgumentException(BAD_RANGE);
        return StreamSupport.intStream(new RandomIntsSpliterator(this, 0L, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound), false);
    }

    public LongStream longs(long streamSize) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BAD_SIZE);
        return StreamSupport.longStream(new RandomLongsSpliterator(this, 0L, streamSize, Long.MAX_VALUE, 0L), false);
    }

    public LongStream longs() {
        return StreamSupport.longStream(new RandomLongsSpliterator(this, 0L, Long.MAX_VALUE, Long.MAX_VALUE, 0L), false);
    }

    public LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BAD_SIZE);
        if (randomNumberOrigin >= randomNumberBound)
            throw new IllegalArgumentException(BAD_RANGE);
        return StreamSupport.longStream(new RandomLongsSpliterator(this, 0L, streamSize, randomNumberOrigin, randomNumberBound), false);
    }

    public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
        if (randomNumberOrigin >= randomNumberBound)
            throw new IllegalArgumentException(BAD_RANGE);
        return StreamSupport.longStream(new RandomLongsSpliterator(this, 0L, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound), false);
    }

    public DoubleStream doubles(long streamSize) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BAD_SIZE);
        return StreamSupport.doubleStream(new RandomDoublesSpliterator(this, 0L, streamSize, Double.MAX_VALUE, 0.0), false);
    }

    public DoubleStream doubles() {
        return StreamSupport.doubleStream(new RandomDoublesSpliterator(this, 0L, Long.MAX_VALUE, Double.MAX_VALUE, 0.0), false);
    }

    public DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound) {
        if (streamSize < 0L)
            throw new IllegalArgumentException(BAD_SIZE);
        if (!(randomNumberOrigin < randomNumberBound))
            throw new IllegalArgumentException(BAD_RANGE);
        return StreamSupport.doubleStream(new RandomDoublesSpliterator(this, 0L, streamSize, randomNumberOrigin, randomNumberBound), false);
    }

    public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
        if (!(randomNumberOrigin < randomNumberBound))
            throw new IllegalArgumentException(BAD_RANGE);
        return StreamSupport.doubleStream(new RandomDoublesSpliterator(this, 0L, Long.MAX_VALUE, randomNumberOrigin, randomNumberBound), false);
    }

    private static final class RandomIntsSpliterator implements Spliterator.OfInt {

        final SplittableRandom rng;

        long index;

        final long fence;

        final int origin;

        final int bound;

        RandomIntsSpliterator(SplittableRandom rng, long index, long fence, int origin, int bound) {
            this.rng = rng;
            this.index = index;
            this.fence = fence;
            this.origin = origin;
            this.bound = bound;
        }

        public RandomIntsSpliterator trySplit() {
            long i = index, m = (i + fence) >>> 1;
            return (m <= i) ? null : new RandomIntsSpliterator(rng.split(), i, index = m, origin, bound);
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
                consumer.accept(rng.internalNextInt(origin, bound));
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
                SplittableRandom r = rng;
                int o = origin, b = bound;
                do {
                    consumer.accept(r.internalNextInt(o, b));
                } while (++i < f);
            }
        }
    }

    private static final class RandomLongsSpliterator implements Spliterator.OfLong {

        final SplittableRandom rng;

        long index;

        final long fence;

        final long origin;

        final long bound;

        RandomLongsSpliterator(SplittableRandom rng, long index, long fence, long origin, long bound) {
            this.rng = rng;
            this.index = index;
            this.fence = fence;
            this.origin = origin;
            this.bound = bound;
        }

        public RandomLongsSpliterator trySplit() {
            long i = index, m = (i + fence) >>> 1;
            return (m <= i) ? null : new RandomLongsSpliterator(rng.split(), i, index = m, origin, bound);
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
                consumer.accept(rng.internalNextLong(origin, bound));
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
                SplittableRandom r = rng;
                long o = origin, b = bound;
                do {
                    consumer.accept(r.internalNextLong(o, b));
                } while (++i < f);
            }
        }
    }

    private static final class RandomDoublesSpliterator implements Spliterator.OfDouble {

        final SplittableRandom rng;

        long index;

        final long fence;

        final double origin;

        final double bound;

        RandomDoublesSpliterator(SplittableRandom rng, long index, long fence, double origin, double bound) {
            this.rng = rng;
            this.index = index;
            this.fence = fence;
            this.origin = origin;
            this.bound = bound;
        }

        public RandomDoublesSpliterator trySplit() {
            long i = index, m = (i + fence) >>> 1;
            return (m <= i) ? null : new RandomDoublesSpliterator(rng.split(), i, index = m, origin, bound);
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
                consumer.accept(rng.internalNextDouble(origin, bound));
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
                SplittableRandom r = rng;
                double o = origin, b = bound;
                do {
                    consumer.accept(r.internalNextDouble(o, b));
                } while (++i < f);
            }
        }
    }
}