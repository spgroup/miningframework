package java.lang;

import jdk.internal.misc.VM;
import java.io.PrintStream;
import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.lang.annotation.Native;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
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
    private static final int FILTER_FILL_IN_STACKTRACE = 0x10;

    @Native
    private static final int SHOW_HIDDEN_FRAMES = 0x20;

    @Native
    private static final int FILL_LIVE_STACK_FRAMES = 0x100;

    final static boolean useNewThrowable = getProperty("stackwalk.newThrowable", false);

    final static boolean isDebug = getProperty("stackwalk.debug", false);

    static <T> StackFrameTraverser<T> makeStackTraverser(StackWalker walker, Function<? super Stream<StackFrame>, ? extends T> function) {
        if (walker.hasLocalsOperandsOption())
            return new LiveStackInfoTraverser<T>(walker, function);
        else
            return new StackFrameTraverser<T>(walker, function);
    }

    static CallerClassFinder makeCallerFinder(StackWalker walker) {
        return new CallerClassFinder(walker);
    }

    static boolean useStackTrace(Throwable t) {
        if (t instanceof VirtualMachineError)
            return false;
        return VM.isBooted() && StackStreamFactory.useNewThrowable;
    }

    static StackTrace makeStackTrace(Throwable ex) {
        return StackTrace.dump(ex);
    }

    static StackTrace makeStackTrace() {
        return StackTrace.dump();
    }

    enum WalkerState {

        NEW, OPEN, CLOSED
    }

    static abstract class AbstractStackWalker<T> {

        protected final StackWalker walker;

        protected final Thread thread;

        protected final int maxDepth;

        protected final long mode;

        protected int depth;

        protected FrameBuffer frameBuffer;

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
            if (walker.hasOption(Option.SHOW_HIDDEN_FRAMES) && !fillCallerClassOnly(newMode))
                newMode |= SHOW_HIDDEN_FRAMES;
            if (walker.hasLocalsOperandsOption())
                newMode |= FILL_LIVE_STACK_FRAMES;
            return newMode;
        }

        private boolean fillCallerClassOnly(int mode) {
            return (mode | FILL_CLASS_REFS_ONLY) != FILL_CLASS_REFS_ONLY;
        }

        protected abstract T consumeFrames();

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

        final T walk() {
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
            frameBuffer.setBatch(bufStartIndex, bufEndIndex);
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

        private T beginStackWalk() {
            initFrameBuffer();
            return callStackWalk(mode, 0, frameBuffer.curBatchFrameCount(), frameBuffer.startIndex(), frameBuffer.classes, frameBuffer.stackFrames);
        }

        private int fetchStackFrames(int batchSize) {
            int startIndex = frameBuffer.startIndex();
            frameBuffer.resize(startIndex, batchSize);
            int endIndex = fetchStackFrames(mode, anchor, batchSize, startIndex, frameBuffer.classes, frameBuffer.stackFrames);
            if (isDebug) {
                System.out.format("  more stack walk requesting %d got %d to %d frames%n", batchSize, frameBuffer.startIndex(), endIndex);
            }
            int numFrames = endIndex - startIndex;
            if (numFrames == 0) {
                frameBuffer.freeze();
            } else {
                frameBuffer.setBatch(startIndex, endIndex);
            }
            return numFrames;
        }

        private native T callStackWalk(long mode, int skipframes, int batchSize, int startIndex, Class<?>[] classes, StackFrame[] frames);

        private native int fetchStackFrames(long mode, long anchor, int batchSize, int startIndex, Class<?>[] classes, StackFrame[] frames);

        class FrameBuffer {

            static final int START_POS = 2;

            int currentBatchSize;

            Class<?>[] classes;

            StackFrame[] stackFrames;

            int origin;

            int fence;

            FrameBuffer(int initialBatchSize) {
                if (initialBatchSize < MIN_BATCH_SIZE) {
                    throw new IllegalArgumentException(initialBatchSize + " < minimum batch size: " + MIN_BATCH_SIZE);
                }
                this.origin = START_POS;
                this.fence = 0;
                this.currentBatchSize = initialBatchSize;
                this.classes = new Class<?>[currentBatchSize];
            }

            int curBatchFrameCount() {
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
                Class<?> c = classes[origin++];
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
                return classes[origin];
            }

            final int getIndex() {
                return origin;
            }

            final void setBatch(int startIndex, int endIndex) {
                if (startIndex <= 0 || endIndex <= 0)
                    throw new IllegalArgumentException("startIndex=" + startIndex + " endIndex=" + endIndex);
                this.origin = startIndex;
                this.fence = endIndex;
                if (depth == 0 && fence > 0) {
                    for (int i = START_POS; i < fence; i++) {
                        Class<?> c = classes[i];
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

            void resize(int startIndex, int elements) {
                if (!isActive())
                    throw new IllegalStateException("inactive frame buffer can't be resized");
                int size = startIndex + elements;
                if (classes.length < size) {
                    Class<?>[] prev = classes;
                    classes = new Class<?>[size];
                    System.arraycopy(prev, 0, classes, 0, START_POS);
                }
                currentBatchSize = size;
            }

            int startIndex() {
                return START_POS;
            }

            StackFrame nextStackFrame() {
                throw new InternalError("should not reach here");
            }
        }
    }

    static class StackFrameTraverser<T> extends AbstractStackWalker<T> implements Spliterator<StackFrame> {

        static {
            stackWalkImplClasses.add(StackFrameTraverser.class);
        }

        private static final int CHARACTERISTICS = Spliterator.ORDERED | Spliterator.IMMUTABLE;

        class Buffer extends FrameBuffer {

            Buffer(int initialBatchSize) {
                super(initialBatchSize);
                this.stackFrames = new StackFrame[initialBatchSize];
                for (int i = START_POS; i < initialBatchSize; i++) {
                    stackFrames[i] = new StackFrameInfo(walker);
                }
            }

            @Override
            void resize(int startIndex, int elements) {
                super.resize(startIndex, elements);
                int size = startIndex + elements;
                if (stackFrames.length < size) {
                    stackFrames = new StackFrame[size];
                }
                for (int i = startIndex(); i < size; i++) {
                    stackFrames[i] = new StackFrameInfo(walker);
                }
            }

            @Override
            StackFrame nextStackFrame() {
                if (isEmpty()) {
                    throw new NoSuchElementException("origin=" + origin + " fence=" + fence);
                }
                StackFrame frame = stackFrames[origin];
                origin++;
                return frame;
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
            StackFrame frame = frameBuffer.nextStackFrame();
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
            this.frameBuffer = new Buffer(getNextBatchSize());
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

    static class CallerClassFinder extends AbstractStackWalker<Integer> {

        static {
            stackWalkImplClasses.add(CallerClassFinder.class);
        }

        private Class<?> caller;

        CallerClassFinder(StackWalker walker) {
            super(walker, FILL_CLASS_REFS_ONLY);
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
            if (frames[1] == null)
                throw new IllegalStateException("no caller frame");
            return n;
        }

        @Override
        protected void initFrameBuffer() {
            this.frameBuffer = new FrameBuffer(getNextBatchSize());
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

    static class StackTrace extends AbstractStackWalker<Integer> {

        static {
            stackWalkImplClasses.add(StackTrace.class);
        }

        class GrowableBuffer extends FrameBuffer {

            GrowableBuffer(int initialBatchSize) {
                super(initialBatchSize);
                this.stackFrames = new StackFrame[initialBatchSize];
                for (int i = START_POS; i < initialBatchSize; i++) {
                    stackFrames[i] = new StackFrameInfo(walker);
                }
            }

            @Override
            int startIndex() {
                return origin;
            }

            @Override
            void resize(int startIndex, int elements) {
                int size = startIndex + elements;
                if (classes.length < size) {
                    classes = Arrays.copyOf(classes, size);
                    stackFrames = Arrays.copyOf(stackFrames, size);
                }
                for (int i = startIndex; i < size; i++) {
                    stackFrames[i] = new StackFrameInfo(walker);
                }
                currentBatchSize = size;
            }

            StackTraceElement get(int index) {
                return new StackTraceElement(classes[index].getName(), "unknown", null, -1);
            }

            StackTraceElement[] toStackTraceElements() {
                int startIndex = START_POS;
                for (int i = startIndex; i < classes.length; i++) {
                    if (classes[i] != null && filterStackWalkImpl(classes[i])) {
                        startIndex++;
                    } else {
                        break;
                    }
                }
                StackFrameInfo.fillInStackFrames(0, stackFrames, startIndex, startIndex + depth);
                StackTraceElement[] stes = new StackTraceElement[depth];
                for (int i = startIndex, j = 0; i < classes.length && j < depth; i++, j++) {
                    if (isDebug) {
                        System.err.println("StackFrame: " + i + " " + stackFrames[i]);
                    }
                    stes[j] = stackFrames[i].toStackTraceElement();
                }
                return stes;
            }
        }

        private static final int MAX_STACK_FRAMES = 1024;

        private static final StackWalker STACKTRACE_WALKER = StackWalker.newInstanceNoCheck(EnumSet.of(Option.SHOW_REFLECT_FRAMES));

        private StackTraceElement[] stes;

        static StackTrace dump() {
            return new StackTrace();
        }

        static StackTrace dump(Throwable ex) {
            return new StackTrace(ex);
        }

        private StackTrace() {
            this(STACKTRACE_WALKER, DEFAULT_MODE);
        }

        private StackTrace(Throwable ex) {
            this(STACKTRACE_WALKER, FILTER_FILL_IN_STACKTRACE);
            if (isDebug) {
                System.err.println("dump stack for " + ex.getClass().getName());
            }
        }

        StackTrace(StackWalker walker, int mode) {
            super(walker, mode, MAX_STACK_FRAMES);
            walk();
        }

        @Override
        protected Integer consumeFrames() {
            int n = 0;
            while (n < maxDepth && nextFrame() != null) {
                n++;
            }
            return n;
        }

        @Override
        protected void initFrameBuffer() {
            this.frameBuffer = new GrowableBuffer(getNextBatchSize());
        }

        @Override
        protected int batchSize(int lastBatchFrameCount) {
            return lastBatchFrameCount == 0 ? 32 : 32;
        }

        synchronized StackTraceElement[] getStackTraceElements() {
            if (stes == null) {
                stes = ((GrowableBuffer) frameBuffer).toStackTraceElements();
                frameBuffer = null;
            }
            return stes;
        }

        void printStackTrace(PrintStream s) {
            StackTraceElement[] stes = getStackTraceElements();
            synchronized (s) {
                s.println("Stack trace");
                for (StackTraceElement traceElement : stes) s.println("\tat " + traceElement);
            }
        }
    }

    static class LiveStackInfoTraverser<T> extends StackFrameTraverser<T> {

        static {
            stackWalkImplClasses.add(LiveStackInfoTraverser.class);
        }

        class Buffer extends FrameBuffer {

            Buffer(int initialBatchSize) {
                super(initialBatchSize);
                this.stackFrames = new StackFrame[initialBatchSize];
                for (int i = START_POS; i < initialBatchSize; i++) {
                    stackFrames[i] = new LiveStackFrameInfo(walker);
                }
            }

            @Override
            void resize(int startIndex, int elements) {
                super.resize(startIndex, elements);
                int size = startIndex + elements;
                if (stackFrames.length < size) {
                    this.stackFrames = new StackFrame[size];
                }
                for (int i = startIndex(); i < size; i++) {
                    stackFrames[i] = new LiveStackFrameInfo(walker);
                }
            }

            @Override
            StackFrame nextStackFrame() {
                if (isEmpty()) {
                    throw new NoSuchElementException("origin=" + origin + " fence=" + fence);
                }
                StackFrame frame = stackFrames[origin];
                origin++;
                return frame;
            }
        }

        LiveStackInfoTraverser(StackWalker walker, Function<? super Stream<StackFrame>, ? extends T> function) {
            super(walker, function, DEFAULT_MODE);
        }

        @Override
        protected void initFrameBuffer() {
            this.frameBuffer = new Buffer(getNextBatchSize());
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
        if (c.getName().startsWith("sun.reflect") && !sun.reflect.MethodAccessor.class.isAssignableFrom(c)) {
            throw new InternalError("Not sun.reflect.MethodAccessor: " + c.toString());
        }
        return c == Method.class || sun.reflect.MethodAccessor.class.isAssignableFrom(c) || c.getName().startsWith("java.lang.invoke.LambdaForm");
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
