package java.lang;
import jdk.internal.reflect.MethodAccessor;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.lang.annotation.Native;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import static java.lang.StackStreamFactory.WalkerState.*;

final class StackStreamFactory {

    private StackStreamFactory() {
    }

    private final static Set<Class<?>> stackWalkImplClasses = init();

    private static final int SMALL_BATCH = 8;

    private static final int BATCH_SIZE = 32;

    private static final int LARGE_BATCH_SIZE = 256;

    private static final int MIN_BATCH_SIZE = SMALL_BATCH;

    @Native
    private static final int DEFAULT_MODE = 0x0;

    @Native
    private static final int FILL_CLASS_REFS_ONLY = 0x2;

    @Native
    private static final int SHOW_HIDDEN_FRAMES = 0x20;

    @Native
    private static final int FILL_LIVE_STACK_FRAMES = 0x100;

    final static boolean isDebug = getProperty("stackwalk.debug", false);

    static <T> StackFrameTraverser<T> makeStackTraverser(StackWalker walker, Function<? super Stream<StackFrame>, ? extends T> function) {
        if (walker.hasLocalsOperandsOption())
            return new LiveStackInfoTraverser<>(walker, function);
        else
            return new StackFrameTraverser<>(walker, function);
    }

    static CallerClassFinder makeCallerFinder(StackWalker walker) {
        return new CallerClassFinder(walker);
    }

    enum WalkerState {

        NEW, OPEN, CLOSED
    }

    static abstract class AbstractStackWalker<R, T> {

        protected final StackWalker walker;

        protected final Thread thread;

        protected final int maxDepth;

        protected final long mode;

        protected int depth;

        protected FrameBuffer<? extends T> frameBuffer;

        protected long anchor;

        protected AbstractStackWalker(StackWalker walker, int mode) {
            this(walker, mode, Integer.MAX_VALUE);
        }

        protected AbstractStackWalker(StackWalker walker, int mode, int maxDepth) {
            this.thread = Thread.currentThread();
            this.mode = toStackWalkMode(walker, mode);
            this.walker = walker;
            this.maxDepth = maxDepth;
            this.depth = 0;
        }

        private int toStackWalkMode(StackWalker walker, int mode) {
            int newMode = mode;
            if (walker.hasOption(Option.SHOW_HIDDEN_FRAMES) && (mode & FILL_CLASS_REFS_ONLY) != FILL_CLASS_REFS_ONLY)
                newMode |= SHOW_HIDDEN_FRAMES;
            if (walker.hasLocalsOperandsOption())
                newMode |= FILL_LIVE_STACK_FRAMES;
            return newMode;
        }

        protected abstract R consumeFrames();

        protected abstract void initFrameBuffer();

        protected abstract int batchSize(int lastBatchFrameCount);

        protected int getNextBatchSize() {
            int lastBatchSize = depth == 0 ? 0 : frameBuffer.curBatchFrameCount();
            int nextBatchSize = batchSize(lastBatchSize);
            if (isDebug) {
                System.err.println("last batch size = " + lastBatchSize + " next batch size = " + nextBatchSize);
            }
            return nextBatchSize >= MIN_BATCH_SIZE ? nextBatchSize : MIN_BATCH_SIZE;
        }

        final void checkState(WalkerState state) {
            if (thread != Thread.currentThread()) {
                throw new IllegalStateException("Invalid thread walking this stack stream: " + Thread.currentThread().getName() + " " + thread.getName());
            }
            switch(state) {
                case NEW:
                    if (anchor != 0) {
                        throw new IllegalStateException("This stack stream is being reused.");
                    }
                    break;
                case OPEN:
                    if (anchor == 0 || anchor == -1L) {
                        throw new IllegalStateException("This stack stream is not valid for walking.");
                    }
                    break;
                case CLOSED:
                    if (anchor != -1L) {
                        throw new IllegalStateException("This stack stream is not closed.");
                    }
            }
        }

        private void close() {
            this.anchor = -1L;
        }

        final R walk() {
            checkState(NEW);
            try {
                return beginStackWalk();
            } finally {
                close();
            }
        }

        private boolean skipReflectionFrames() {
            return !walker.hasOption(Option.SHOW_REFLECT_FRAMES) && !walker.hasOption(Option.SHOW_HIDDEN_FRAMES);
        }

        final Class<?> peekFrame() {
            while (frameBuffer.isActive() && depth < maxDepth) {
                if (frameBuffer.isEmpty()) {
                    getNextBatch();
                } else {
                    Class<?> c = frameBuffer.get();
                    if (skipReflectionFrames() && isReflectionFrame(c)) {
                        if (isDebug)
                            System.err.println("  skip: frame " + frameBuffer.getIndex() + " " + c);
                        frameBuffer.next();
                        depth++;
                        continue;
                    } else {
                        return c;
                    }
                }
            }
            return null;
        }

        private Object doStackWalk(long anchor, int skipFrames, int batchSize, int bufStartIndex, int bufEndIndex) {
            checkState(NEW);
            frameBuffer.check(skipFrames);
            if (isDebug) {
                System.err.format("doStackWalk: skip %d start %d end %d%n", skipFrames, bufStartIndex, bufEndIndex);
            }
            this.anchor = anchor;
            frameBuffer.setBatch(depth, bufStartIndex, bufEndIndex);
            return consumeFrames();
        }

        private int getNextBatch() {
            int nextBatchSize = Math.min(maxDepth - depth, getNextBatchSize());
            if (!frameBuffer.isActive() || nextBatchSize <= 0) {
                if (isDebug) {
                    System.out.format("  more stack walk done%n");
                }
                frameBuffer.freeze();
                return 0;
            }
            return fetchStackFrames(nextBatchSize);
        }

        final Class<?> nextFrame() {
            if (!hasNext()) {
                return null;
            }
            Class<?> c = frameBuffer.next();
            depth++;
            return c;
        }

        final boolean hasNext() {
            return peekFrame() != null;
        }

        private R beginStackWalk() {
            initFrameBuffer();
            return callStackWalk(mode, 0, frameBuffer.curBatchFrameCount(), frameBuffer.startIndex(), frameBuffer.frames());
        }

        private int fetchStackFrames(int batchSize) {
            int startIndex = frameBuffer.startIndex();
            frameBuffer.resize(startIndex, batchSize);
            int endIndex = fetchStackFrames(mode, anchor, batchSize, startIndex, frameBuffer.frames());
            if (isDebug) {
                System.out.format("  more stack walk requesting %d got %d to %d frames%n", batchSize, frameBuffer.startIndex(), endIndex);
            }
            int numFrames = endIndex - startIndex;
            if (numFrames == 0) {
                frameBuffer.freeze();
            } else {
                frameBuffer.setBatch(depth, startIndex, endIndex);
            }
            return numFrames;
        }

        private native R callStackWalk(long mode, int skipframes, int batchSize, int startIndex, T[] frames);

        private native int fetchStackFrames(long mode, long anchor, int batchSize, int startIndex, T[] frames);
    }

    static class StackFrameTraverser<T> extends AbstractStackWalker<T, StackFrameInfo> implements Spliterator<StackFrame> {

        static {
            stackWalkImplClasses.add(StackFrameTraverser.class);
        }

        private static final int CHARACTERISTICS = Spliterator.ORDERED | Spliterator.IMMUTABLE;

        final class StackFrameBuffer extends FrameBuffer<StackFrameInfo> {

            private StackFrameInfo[] stackFrames;

            StackFrameBuffer(int initialBatchSize) {
                super(initialBatchSize);
                this.stackFrames = new StackFrameInfo[initialBatchSize];
                for (int i = START_POS; i < initialBatchSize; i++) {
                    stackFrames[i] = new StackFrameInfo(walker);
                }
            }

            @Override
            StackFrameInfo[] frames() {
                return stackFrames;
            }

            @Override
            void resize(int startIndex, int elements) {
                if (!isActive())
                    throw new IllegalStateException("inactive frame buffer can't be resized");
                assert startIndex == START_POS : "bad start index " + startIndex + " expected " + START_POS;
                int size = startIndex + elements;
                if (stackFrames.length < size) {
                    StackFrameInfo[] newFrames = new StackFrameInfo[size];
                    System.arraycopy(stackFrames, 0, newFrames, 0, startIndex);
                    stackFrames = newFrames;
                }
                for (int i = startIndex; i < size; i++) {
                    stackFrames[i] = new StackFrameInfo(walker);
                }
                currentBatchSize = size;
            }

            @Override
            StackFrameInfo nextStackFrame() {
                if (isEmpty()) {
                    throw new NoSuchElementException("origin=" + origin + " fence=" + fence);
                }
                StackFrameInfo frame = stackFrames[origin];
                origin++;
                return frame;
            }

            @Override
            final Class<?> at(int index) {
                return stackFrames[index].declaringClass;
            }
        }

        final Function<? super Stream<StackFrame>, ? extends T> function;

        StackFrameTraverser(StackWalker walker, Function<? super Stream<StackFrame>, ? extends T> function) {
            this(walker, function, DEFAULT_MODE);
        }

        StackFrameTraverser(StackWalker walker, Function<? super Stream<StackFrame>, ? extends T> function, int mode) {
            super(walker, mode);
            this.function = function;
        }

        StackFrame nextStackFrame() {
            if (!hasNext()) {
                return null;
            }
            StackFrameInfo frame = frameBuffer.nextStackFrame();
            depth++;
            return frame;
        }

        @Override
        protected T consumeFrames() {
            checkState(OPEN);
            Stream<StackFrame> stream = StreamSupport.stream(this, false);
            if (function != null) {
                return function.apply(stream);
            } else
                throw new UnsupportedOperationException();
        }

        @Override
        protected void initFrameBuffer() {
            this.frameBuffer = new StackFrameBuffer(getNextBatchSize());
        }

        @Override
        protected int batchSize(int lastBatchFrameCount) {
            if (lastBatchFrameCount == 0) {
                int initialBatchSize = Math.max(walker.estimateDepth(), SMALL_BATCH);
                return Math.min(initialBatchSize, LARGE_BATCH_SIZE);
            } else {
                if (lastBatchFrameCount > BATCH_SIZE) {
                    return lastBatchFrameCount;
                } else {
                    return Math.min(lastBatchFrameCount * 2, BATCH_SIZE);
                }
            }
        }

        @Override
        public Spliterator<StackFrame> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return maxDepth;
        }

        @Override
        public int characteristics() {
            return CHARACTERISTICS;
        }

        @Override
        public void forEachRemaining(Consumer<? super StackFrame> action) {
            checkState(OPEN);
            for (int n = 0; n < maxDepth; n++) {
                StackFrame frame = nextStackFrame();
                if (frame == null)
                    break;
                action.accept(frame);
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super StackFrame> action) {
            checkState(OPEN);
            int index = frameBuffer.getIndex();
            if (hasNext()) {
                StackFrame frame = nextStackFrame();
                action.accept(frame);
                if (isDebug) {
                    System.err.println("tryAdvance: " + index + " " + frame);
                }
                return true;
            }
            if (isDebug) {
                System.err.println("tryAdvance: " + index + " NO element");
            }
            return false;
        }
    }

    static final class CallerClassFinder extends AbstractStackWalker<Integer, Class<?>> {

        static {
            stackWalkImplClasses.add(CallerClassFinder.class);
        }

        private Class<?> caller;

        CallerClassFinder(StackWalker walker) {
            super(walker, FILL_CLASS_REFS_ONLY);
            assert (mode & FILL_CLASS_REFS_ONLY) == FILL_CLASS_REFS_ONLY : "mode should contain FILL_CLASS_REFS_ONLY";
        }

        final class ClassBuffer extends FrameBuffer<Class<?>> {

            Class<?>[] classes;

            ClassBuffer(int batchSize) {
                super(batchSize);
                classes = new Class<?>[batchSize];
            }

            @Override
            Class<?>[] frames() {
                return classes;
            }

            @Override
            final Class<?> at(int index) {
                return classes[index];
            }

            @Override
            void resize(int startIndex, int elements) {
                if (!isActive())
                    throw new IllegalStateException("inactive frame buffer can't be resized");
                assert startIndex == START_POS : "bad start index " + startIndex + " expected " + START_POS;
                int size = startIndex + elements;
                if (classes.length < size) {
                    Class<?>[] prev = classes;
                    classes = new Class<?>[size];
                    System.arraycopy(prev, 0, classes, 0, startIndex);
                }
                currentBatchSize = size;
            }
        }

        Class<?> findCaller() {
            walk();
            return caller;
        }

        @Override
        protected Integer consumeFrames() {
            checkState(OPEN);
            int n = 0;
            Class<?>[] frames = new Class<?>[2];
            while (n < 2 && (caller = nextFrame()) != null) {
                if (isMethodHandleFrame(caller))
                    continue;
                frames[n++] = caller;
            }
            if (frames[1] == null) {
                throw new IllegalStateException("no caller frame");
            }
            return n;
        }

        @Override
        protected void initFrameBuffer() {
            this.frameBuffer = new ClassBuffer(getNextBatchSize());
        }

        @Override
        protected int batchSize(int lastBatchFrameCount) {
            return MIN_BATCH_SIZE;
        }

        @Override
        protected int getNextBatchSize() {
            return MIN_BATCH_SIZE;
        }
    }

    static final class LiveStackInfoTraverser<T> extends StackFrameTraverser<T> {

        final class LiveStackFrameBuffer extends FrameBuffer<LiveStackFrameInfo> {

            private LiveStackFrameInfo[] stackFrames;

            LiveStackFrameBuffer(int initialBatchSize) {
                super(initialBatchSize);
                this.stackFrames = new LiveStackFrameInfo[initialBatchSize];
                for (int i = START_POS; i < initialBatchSize; i++) {
                    stackFrames[i] = new LiveStackFrameInfo(walker);
                }
            }

            @Override
            LiveStackFrameInfo[] frames() {
                return stackFrames;
            }

            @Override
            void resize(int startIndex, int elements) {
                if (!isActive()) {
                    throw new IllegalStateException("inactive frame buffer can't be resized");
                }
                assert startIndex == START_POS : "bad start index " + startIndex + " expected " + START_POS;
                int size = startIndex + elements;
                if (stackFrames.length < size) {
                    LiveStackFrameInfo[] newFrames = new LiveStackFrameInfo[size];
                    System.arraycopy(stackFrames, 0, newFrames, 0, startIndex);
                    stackFrames = newFrames;
                }
                for (int i = startIndex(); i < size; i++) {
                    stackFrames[i] = new LiveStackFrameInfo(walker);
                }
                currentBatchSize = size;
            }

            @Override
            LiveStackFrameInfo nextStackFrame() {
                if (isEmpty()) {
                    throw new NoSuchElementException("origin=" + origin + " fence=" + fence);
                }
                LiveStackFrameInfo frame = stackFrames[origin];
                origin++;
                return frame;
            }

            @Override
            final Class<?> at(int index) {
                return stackFrames[index].declaringClass;
            }
        }

        static {
            stackWalkImplClasses.add(LiveStackInfoTraverser.class);
        }

        LiveStackInfoTraverser(StackWalker walker, Function<? super Stream<StackFrame>, ? extends T> function) {
            super(walker, function, DEFAULT_MODE);
        }

        @Override
        protected void initFrameBuffer() {
            this.frameBuffer = new LiveStackFrameBuffer(getNextBatchSize());
        }
    }

    static abstract class FrameBuffer<F> {

        static final int START_POS = 2;

        int currentBatchSize;

        int origin;

        int fence;

        FrameBuffer(int initialBatchSize) {
            if (initialBatchSize < MIN_BATCH_SIZE) {
                throw new IllegalArgumentException(initialBatchSize + " < minimum batch size: " + MIN_BATCH_SIZE);
            }
            this.origin = START_POS;
            this.fence = 0;
            this.currentBatchSize = initialBatchSize;
        }

        abstract F[] frames();

        abstract void resize(int startIndex, int elements);

        abstract Class<?> at(int index);

        int startIndex() {
            return START_POS;
        }

        F nextStackFrame() {
            throw new InternalError("should not reach here");
        }

        final int curBatchFrameCount() {
            return currentBatchSize - START_POS;
        }

        final boolean isEmpty() {
            return origin >= fence || (origin == START_POS && fence == 0);
        }

        final void freeze() {
            origin = 0;
            fence = 0;
        }

        final boolean isActive() {
            return origin > 0 && (fence == 0 || origin < fence || fence == currentBatchSize);
        }

        final Class<?> next() {
            if (isEmpty()) {
                throw new NoSuchElementException("origin=" + origin + " fence=" + fence);
            }
            Class<?> c = at(origin);
            origin++;
            if (isDebug) {
                int index = origin - 1;
                System.out.format("  next frame at %d: %s (origin %d fence %d)%n", index, Objects.toString(c), index, fence);
            }
            return c;
        }

        final Class<?> get() {
            if (isEmpty()) {
                throw new NoSuchElementException("origin=" + origin + " fence=" + fence);
            }
            return at(origin);
        }

        final int getIndex() {
            return origin;
        }

        final void setBatch(int depth, int startIndex, int endIndex) {
                if (startIndex <= 0 || endIndex <= 0)
                    throw new IllegalArgumentException("startIndex=" + startIndex + " endIndex=" + endIndex);
                this.origin = startIndex;
                this.fence = endIndex;
                if (depth == 0 && fence > 0) {
                    for (int i = START_POS; i < fence; i++) {
                    Class<?> c = at(i);
                        if (isDebug)
                            System.err.format("  frame %d: %s%n", i, c);
                        if (filterStackWalkImpl(c)) {
                            origin++;
                        } else {
                            break;
                        }
                    }
                }
            }

        final void check(int skipFrames) {
            int index = skipFrames + START_POS;
            if (origin != index) {
                throw new IllegalStateException("origin " + origin + " != " + index);
            }
        }
    }

    private static native boolean checkStackWalkModes();

    private static Set<Class<?>> init() {
        if (!checkStackWalkModes()) {
            throw new InternalError("StackWalker mode values do not match with JVM");
        }
        Set<Class<?>> classes = new HashSet<>();
        classes.add(StackWalker.class);
        classes.add(StackStreamFactory.class);
        classes.add(AbstractStackWalker.class);
        return classes;
    }

    private static boolean filterStackWalkImpl(Class<?> c) {
        return stackWalkImplClasses.contains(c) || c.getName().startsWith("java.util.stream.");
    }

    private static boolean isMethodHandleFrame(Class<?> c) {
        return c.getName().startsWith("java.lang.invoke.");
    }

    private static boolean isReflectionFrame(Class<?> c) {
        if (c.getName().startsWith("jdk.internal.reflect") && !MethodAccessor.class.isAssignableFrom(c)) {
            throw new InternalError("Not jdk.internal.reflect.MethodAccessor: " + c.toString());
        }
        return c == Method.class || MethodAccessor.class.isAssignableFrom(c) || c.getName().startsWith("java.lang.invoke.LambdaForm");
    }

    private static boolean getProperty(String key, boolean value) {
        String s = AccessController.doPrivileged(new PrivilegedAction<>() {

            @Override
            public String run() {
                return System.getProperty(key);
            }
        });
        if (s != null) {
            return Boolean.valueOf(s);
        }
        return value;
    }
}