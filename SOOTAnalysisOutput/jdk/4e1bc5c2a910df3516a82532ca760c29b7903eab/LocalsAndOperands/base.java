import org.testng.annotations.*;
import java.lang.StackWalker.StackFrame;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;

public class LocalsAndOperands {

    static final boolean debug = true;

    static Class<?> liveStackFrameClass;

    static Class<?> primitiveValueClass;

    static StackWalker extendedWalker;

    static Method getLocals;

    static Method getOperands;

    static Method getMonitors;

    static Method primitiveType;

    static {
        try {
            liveStackFrameClass = Class.forName("java.lang.LiveStackFrame");
            primitiveValueClass = Class.forName("java.lang.LiveStackFrame$PrimitiveValue");
            getLocals = liveStackFrameClass.getDeclaredMethod("getLocals");
            getLocals.setAccessible(true);
            getOperands = liveStackFrameClass.getDeclaredMethod("getStack");
            getOperands.setAccessible(true);
            getMonitors = liveStackFrameClass.getDeclaredMethod("getMonitors");
            getMonitors.setAccessible(true);
            primitiveType = primitiveValueClass.getDeclaredMethod("type");
            primitiveType.setAccessible(true);
            Method method = liveStackFrameClass.getMethod("getStackWalker");
            method.setAccessible(true);
            extendedWalker = (StackWalker) method.invoke(null);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    static Object[] invokeGetLocals(StackFrame arg) {
        try {
            return (Object[]) getLocals.invoke(arg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DataProvider
    public static StackFrame[][] provider() {
        return new StackFrame[][] { new Tester().testLocals() };
    }

    @DataProvider
    public static StackFrame[][] keepAliveProvider() {
        return new StackFrame[][] { new Tester().testLocalsKeepAlive() };
    }

    @DataProvider
    public static StackFrame[][] noLocalsProvider() {
        return new StackFrame[][] { new Tester(StackWalker.getInstance(), true).testLocals() };
    }

    @DataProvider
    public static StackFrame[][] unfilteredProvider() {
        return new StackFrame[][] { new Tester(extendedWalker, false).testLocals() };
    }

    @Test(dataProvider = "keepAliveProvider")
    public static void checkLocalValues(StackFrame... frames) {
        if (debug) {
            System.out.println("Running checkLocalValues");
            dumpStackWithLocals(frames);
        }
        Arrays.stream(frames).filter(f -> f.getMethodName().equals("testLocalsKeepAlive")).forEach(f -> {
            Object[] locals = invokeGetLocals(f);
            for (int i = 0; i < locals.length; i++) {
                String expected = Tester.LOCAL_VALUES[i];
                Object observed = locals[i];
                if (expected != null && !expected.equals(observed.toString())) {
                    System.err.println("Local value mismatch:");
                    if (!debug) {
                        dumpStackWithLocals(frames);
                    }
                    throw new RuntimeException("local " + i + " value is " + observed + ", expected " + expected);
                }
                expected = Tester.LOCAL_TYPES[i];
                observed = type(locals[i]);
                if (expected != null && !expected.equals(observed)) {
                    System.err.println("Local type mismatch:");
                    if (!debug) {
                        dumpStackWithLocals(frames);
                    }
                    throw new RuntimeException("local " + i + " type is " + observed + ", expected " + expected);
                }
            }
        });
    }

    @Test(dataProvider = "provider")
    public static void sanityCheck(StackFrame... frames) {
        if (debug) {
            System.out.println("Running sanityCheck");
        }
        try {
            Stream<StackFrame> stream = Arrays.stream(frames);
            if (debug) {
                stream.forEach(LocalsAndOperands::printLocals);
            } else {
                System.out.println(stream.count() + " frames");
            }
        } catch (Throwable t) {
            dumpStackWithLocals(frames);
            throw t;
        }
    }

    @Test(dataProvider = "unfilteredProvider")
    public static void unfilteredSanityCheck(StackFrame... frames) {
        if (debug) {
            System.out.println("Running unfilteredSanityCheck");
        }
        try {
            Stream<StackFrame> stream = Arrays.stream(frames);
            if (debug) {
                stream.forEach(f -> {
                    System.out.println(f + ": " + invokeGetLocals(f).length + " locals");
                });
            } else {
                System.out.println(stream.count() + " frames");
            }
        } catch (Throwable t) {
            dumpStackWithLocals(frames);
            throw t;
        }
    }

    @Test(dataProvider = "noLocalsProvider")
    public static void withoutLocalsAndOperands(StackFrame... frames) {
        for (StackFrame frame : frames) {
            if (liveStackFrameClass.isInstance(frame)) {
                throw new RuntimeException("should not be LiveStackFrame");
            }
        }
    }

    static class Tester {

        private StackWalker walker;

        private boolean filter = true;

        Tester() {
            this.walker = extendedWalker;
        }

        Tester(StackWalker walker, boolean filter) {
            this.walker = walker;
            this.filter = filter;
        }

        private synchronized StackFrame[] testLocals() {
            int x = 10;
            char c = 'z';
            String hi = "himom";
            long l = 1000000L;
            double d = 3.1415926;
            if (filter) {
                return walker.walk(s -> s.filter(f -> TEST_METHODS.contains(f.getMethodName())).collect(Collectors.toList())).toArray(new StackFrame[0]);
            } else {
                return walker.walk(s -> s.collect(Collectors.toList())).toArray(new StackFrame[0]);
            }
        }

        private synchronized StackFrame[] testLocalsKeepAlive() {
            int x = 10;
            char c = 'z';
            String hi = "himom";
            long l = 1000000L;
            double d = 3.1415926;
            List<StackWalker.StackFrame> frames;
            if (filter) {
                frames = walker.walk(s -> s.filter(f -> TEST_METHODS.contains(f.getMethodName())).collect(Collectors.toList()));
            } else {
                frames = walker.walk(s -> s.collect(Collectors.toList()));
            }
            System.out.println("Stayin' alive: " + x + " " + c + " " + hi + " " + l + " " + d);
            return frames.toArray(new StackFrame[0]);
        }

        private final static String[] LOCAL_VALUES = new String[] { null, "10", "122", "himom", "0", null, null, null, "0" };

        private final static String[] LOCAL_TYPES = new String[] { null, "I", "I", "java.lang.String", "I", "I", "I", "I", "I" };

        final static Map NUM_LOCALS = Map.of("testLocals", 8, "testLocalsKeepAlive", LOCAL_VALUES.length);

        private final static Collection<String> TEST_METHODS = NUM_LOCALS.keySet();
    }

    public static void dumpStackWithLocals(StackFrame... frames) {
        Arrays.stream(frames).forEach(LocalsAndOperands::printLocals);
    }

    public static void printLocals(StackWalker.StackFrame frame) {
        try {
            System.out.println(frame);
            Object[] locals = (Object[]) getLocals.invoke(frame);
            for (int i = 0; i < locals.length; i++) {
                System.out.format("  local %d: %s type %s\n", i, locals[i], type(locals[i]));
            }
            Object[] operands = (Object[]) getOperands.invoke(frame);
            for (int i = 0; i < operands.length; i++) {
                System.out.format("  operand %d: %s type %s%n", i, operands[i], type(operands[i]));
            }
            Object[] monitors = (Object[]) getMonitors.invoke(frame);
            for (int i = 0; i < monitors.length; i++) {
                System.out.format("  monitor %d: %s%n", i, monitors[i]);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String type(Object o) {
        try {
            if (o == null) {
                return "null";
            } else if (primitiveValueClass.isInstance(o)) {
                char c = (char) primitiveType.invoke(o);
                return String.valueOf(c);
            } else {
                return o.getClass().getName();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
