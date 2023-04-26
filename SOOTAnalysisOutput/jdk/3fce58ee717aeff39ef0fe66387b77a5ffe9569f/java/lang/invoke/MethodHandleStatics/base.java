package java.lang.invoke;

import java.util.Properties;
import jdk.internal.misc.Unsafe;
import sun.security.action.GetPropertyAction;

class MethodHandleStatics {

    private MethodHandleStatics() {
    }

    static final Unsafe UNSAFE = Unsafe.getUnsafe();

    static final boolean DEBUG_METHOD_HANDLE_NAMES;

    static final boolean DUMP_CLASS_FILES;

    static final boolean TRACE_INTERPRETER;

    static final boolean TRACE_METHOD_LINKAGE;

    static final int COMPILE_THRESHOLD;

    static final int DONT_INLINE_THRESHOLD;

    static final int PROFILE_LEVEL;

    static final boolean PROFILE_GWT;

    static final int CUSTOMIZE_THRESHOLD;

    static final boolean VAR_HANDLE_GUARDS;

    static {
        Properties props = GetPropertyAction.privilegedGetProperties();
        DEBUG_METHOD_HANDLE_NAMES = Boolean.parseBoolean(props.getProperty("java.lang.invoke.MethodHandle.DEBUG_NAMES"));
        DUMP_CLASS_FILES = Boolean.parseBoolean(props.getProperty("java.lang.invoke.MethodHandle.DUMP_CLASS_FILES"));
        TRACE_INTERPRETER = Boolean.parseBoolean(props.getProperty("java.lang.invoke.MethodHandle.TRACE_INTERPRETER"));
        TRACE_METHOD_LINKAGE = Boolean.parseBoolean(props.getProperty("java.lang.invoke.MethodHandle.TRACE_METHOD_LINKAGE"));
        COMPILE_THRESHOLD = Integer.parseInt(props.getProperty("java.lang.invoke.MethodHandle.COMPILE_THRESHOLD", "0"));
        DONT_INLINE_THRESHOLD = Integer.parseInt(props.getProperty("java.lang.invoke.MethodHandle.DONT_INLINE_THRESHOLD", "30"));
        PROFILE_LEVEL = Integer.parseInt(props.getProperty("java.lang.invoke.MethodHandle.PROFILE_LEVEL", "0"));
        PROFILE_GWT = Boolean.parseBoolean(props.getProperty("java.lang.invoke.MethodHandle.PROFILE_GWT", "true"));
        CUSTOMIZE_THRESHOLD = Integer.parseInt(props.getProperty("java.lang.invoke.MethodHandle.CUSTOMIZE_THRESHOLD", "127"));
        VAR_HANDLE_GUARDS = Boolean.parseBoolean(props.getProperty("java.lang.invoke.VarHandle.VAR_HANDLE_GUARDS", "true"));
        if (CUSTOMIZE_THRESHOLD < -1 || CUSTOMIZE_THRESHOLD > 127) {
            throw newInternalError("CUSTOMIZE_THRESHOLD should be in [-1...127] range");
        }
    }

    static boolean debugEnabled() {
        return (DEBUG_METHOD_HANDLE_NAMES | DUMP_CLASS_FILES | TRACE_INTERPRETER | TRACE_METHOD_LINKAGE);
    }

    static InternalError newInternalError(String message) {
        return new InternalError(message);
    }

    static InternalError newInternalError(String message, Throwable cause) {
        return new InternalError(message, cause);
    }

    static InternalError newInternalError(Throwable cause) {
        return new InternalError(cause);
    }

    static RuntimeException newIllegalStateException(String message) {
        return new IllegalStateException(message);
    }

    static RuntimeException newIllegalStateException(String message, Object obj) {
        return new IllegalStateException(message(message, obj));
    }

    static RuntimeException newIllegalArgumentException(String message) {
        return new IllegalArgumentException(message);
    }

    static RuntimeException newIllegalArgumentException(String message, Object obj) {
        return new IllegalArgumentException(message(message, obj));
    }

    static RuntimeException newIllegalArgumentException(String message, Object obj, Object obj2) {
        return new IllegalArgumentException(message(message, obj, obj2));
    }

    static Error uncaughtException(Throwable ex) {
        if (ex instanceof Error)
            throw (Error) ex;
        if (ex instanceof RuntimeException)
            throw (RuntimeException) ex;
        throw newInternalError("uncaught exception", ex);
    }

    private static String message(String message, Object obj) {
        if (obj != null)
            message = message + ": " + obj;
        return message;
    }

    private static String message(String message, Object obj, Object obj2) {
        if (obj != null || obj2 != null)
            message = message + ": " + obj + ", " + obj2;
        return message;
    }
}
