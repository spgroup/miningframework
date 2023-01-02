import java.util.*;
import java.lang.invoke.*;

public class TestCatchExceptionWithVarargs {

    private static final Class<?> CLASS = TestCatchExceptionWithVarargs.class;

    private static final int MAX_MH_ARITY = 254;

    public static MethodHandle target;

    public static MethodHandle handler;

    private static Object firstArg;

    static class MyException extends Exception {
    }

    public static Object target(Object... a) throws Exception {
        if (a[0] != firstArg) {
            throw new AssertionError("first argument different than expected: " + a[0] + " != " + firstArg);
        }
        throw new MyException();
    }

    public static Object handler(Object... a) {
        if (a[0] != firstArg) {
            throw new AssertionError("first argument different than expected: " + a[0] + " != " + firstArg);
        }
        return a[0];
    }

    static {
        try {
            MethodType mtype = MethodType.methodType(Object.class, Object[].class);
            target = MethodHandles.lookup().findStatic(CLASS, "target", mtype);
            handler = MethodHandles.lookup().findStatic(CLASS, "handler", mtype);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static void main(String[] args) throws Throwable {
        List<Class<?>> ptypes = new LinkedList<>();
        ptypes.add(Object[].class);
        for (int i = 1; i < MAX_MH_ARITY - 1; i++) {
            ptypes.add(0, Object.class);
            MethodHandle targetWithArgs = target.asType(MethodType.methodType(Object.class, ptypes));
            MethodHandle handlerWithArgs = handler.asType(MethodType.methodType(Object.class, ptypes));
            handlerWithArgs = MethodHandles.dropArguments(handlerWithArgs, 0, MyException.class);
            MethodHandle gwc1 = MethodHandles.catchException(targetWithArgs, MyException.class, handlerWithArgs);
            MethodHandle gwc2 = MethodHandles.catchException(gwc1, MyException.class, handlerWithArgs);
            firstArg = new Object();
            Object o = gwc2.asSpreader(Object[].class, ptypes.size() - 1).invoke(firstArg, new Object[i]);
            if (o != firstArg) {
                throw new AssertionError("return value different than expected: " + o + " != " + firstArg);
            }
        }
    }
}
