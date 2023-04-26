package java.lang.invoke;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import static java.lang.invoke.MethodHandleNatives.Constants.*;
import static java.lang.invoke.MethodHandleStatics.*;
import static java.lang.invoke.MethodHandles.Lookup.IMPL_LOOKUP;

class MethodHandleNatives {

    private MethodHandleNatives() {
    }

    static native void init(MemberName self, Object ref);

    static native void expand(MemberName self);

    static native MemberName resolve(MemberName self, Class<?> caller) throws LinkageError;

    static native int getMembers(Class<?> defc, String matchName, String matchSig, int matchFlags, Class<?> caller, int skip, MemberName[] results);

    static native long objectFieldOffset(MemberName self);

    static native long staticFieldOffset(MemberName self);

    static native Object staticFieldBase(MemberName self);

    static native Object getMemberVMInfo(MemberName self);

    static native int getConstant(int which);

    static final boolean COUNT_GWT;

    static native void setCallSiteTargetNormal(CallSite site, MethodHandle target);

    static native void setCallSiteTargetVolatile(CallSite site, MethodHandle target);

    private static native void registerNatives();

    static {
        registerNatives();
        COUNT_GWT = getConstant(Constants.GC_COUNT_GWT) != 0;
        MethodHandleImpl.initStatics();
    }

    static class Constants {

        Constants() {
        }

        static final int GC_COUNT_GWT = 4, GC_LAMBDA_SUPPORT = 5;

        static final int MN_IS_METHOD = 0x00010000, MN_IS_CONSTRUCTOR = 0x00020000, MN_IS_FIELD = 0x00040000, MN_IS_TYPE = 0x00080000, MN_REFERENCE_KIND_SHIFT = 24, MN_REFERENCE_KIND_MASK = 0x0F000000 >> MN_REFERENCE_KIND_SHIFT, MN_SEARCH_SUPERCLASSES = 0x00100000, MN_SEARCH_INTERFACES = 0x00200000;

        static final int T_BOOLEAN = 4, T_CHAR = 5, T_FLOAT = 6, T_DOUBLE = 7, T_BYTE = 8, T_SHORT = 9, T_INT = 10, T_LONG = 11, T_OBJECT = 12, T_VOID = 14, T_ILLEGAL = 99;

        static final byte CONSTANT_Utf8 = 1, CONSTANT_Integer = 3, CONSTANT_Float = 4, CONSTANT_Long = 5, CONSTANT_Double = 6, CONSTANT_Class = 7, CONSTANT_String = 8, CONSTANT_Fieldref = 9, CONSTANT_Methodref = 10, CONSTANT_InterfaceMethodref = 11, CONSTANT_NameAndType = 12, CONSTANT_MethodHandle = 15, CONSTANT_MethodType = 16, CONSTANT_InvokeDynamic = 18, CONSTANT_LIMIT = 19;

        static final char ACC_PUBLIC = 0x0001, ACC_PRIVATE = 0x0002, ACC_PROTECTED = 0x0004, ACC_STATIC = 0x0008, ACC_FINAL = 0x0010, ACC_SYNCHRONIZED = 0x0020, ACC_VOLATILE = 0x0040, ACC_TRANSIENT = 0x0080, ACC_NATIVE = 0x0100, ACC_INTERFACE = 0x0200, ACC_ABSTRACT = 0x0400, ACC_STRICT = 0x0800, ACC_SYNTHETIC = 0x1000, ACC_ANNOTATION = 0x2000, ACC_ENUM = 0x4000, ACC_SUPER = ACC_SYNCHRONIZED, ACC_BRIDGE = ACC_VOLATILE, ACC_VARARGS = ACC_TRANSIENT;

        static final byte REF_NONE = 0, REF_getField = 1, REF_getStatic = 2, REF_putField = 3, REF_putStatic = 4, REF_invokeVirtual = 5, REF_invokeStatic = 6, REF_invokeSpecial = 7, REF_newInvokeSpecial = 8, REF_invokeInterface = 9, REF_LIMIT = 10;
    }

    static boolean refKindIsValid(int refKind) {
        return (refKind > REF_NONE && refKind < REF_LIMIT);
    }

    static boolean refKindIsField(byte refKind) {
        assert (refKindIsValid(refKind));
        return (refKind <= REF_putStatic);
    }

    static boolean refKindIsGetter(byte refKind) {
        assert (refKindIsValid(refKind));
        return (refKind <= REF_getStatic);
    }

    static boolean refKindIsSetter(byte refKind) {
        return refKindIsField(refKind) && !refKindIsGetter(refKind);
    }

    static boolean refKindIsMethod(byte refKind) {
        return !refKindIsField(refKind) && (refKind != REF_newInvokeSpecial);
    }

    static boolean refKindHasReceiver(byte refKind) {
        assert (refKindIsValid(refKind));
        return (refKind & 1) != 0;
    }

    static boolean refKindIsStatic(byte refKind) {
        return !refKindHasReceiver(refKind) && (refKind != REF_newInvokeSpecial);
    }

    static boolean refKindDoesDispatch(byte refKind) {
        assert (refKindIsValid(refKind));
        return (refKind == REF_invokeVirtual || refKind == REF_invokeInterface);
    }

    static {
        final int HR_MASK = ((1 << REF_getField) | (1 << REF_putField) | (1 << REF_invokeVirtual) | (1 << REF_invokeSpecial) | (1 << REF_invokeInterface));
        for (byte refKind = REF_NONE + 1; refKind < REF_LIMIT; refKind++) {
            assert (refKindHasReceiver(refKind) == (((1 << refKind) & HR_MASK) != 0)) : refKind;
        }
    }

    static String refKindName(byte refKind) {
        assert (refKindIsValid(refKind));
        return REFERENCE_KIND_NAME[refKind];
    }

    private static String[] REFERENCE_KIND_NAME = { null, "getField", "getStatic", "putField", "putStatic", "invokeVirtual", "invokeStatic", "invokeSpecial", "newInvokeSpecial", "invokeInterface" };

    private static native int getNamedCon(int which, Object[] name);

    static boolean verifyConstants() {
        Object[] box = { null };
        for (int i = 0; ; i++) {
            box[0] = null;
            int vmval = getNamedCon(i, box);
            if (box[0] == null)
                break;
            String name = (String) box[0];
            try {
                Field con = Constants.class.getDeclaredField(name);
                int jval = con.getInt(null);
                if (jval == vmval)
                    continue;
                String err = (name + ": JVM has " + vmval + " while Java has " + jval);
                if (name.equals("CONV_OP_LIMIT")) {
                    System.err.println("warning: " + err);
                    continue;
                }
                throw new InternalError(err);
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                String err = (name + ": JVM has " + vmval + " which Java does not define");
                continue;
            }
        }
        return true;
    }

    static {
        assert (verifyConstants());
    }

    static MemberName linkCallSite(Object callerObj, Object bootstrapMethodObj, Object nameObj, Object typeObj, Object staticArguments, Object[] appendixResult) {
        MethodHandle bootstrapMethod = (MethodHandle) bootstrapMethodObj;
        Class<?> caller = (Class<?>) callerObj;
        String name = nameObj.toString().intern();
        MethodType type = (MethodType) typeObj;
        appendixResult[0] = CallSite.makeSite(bootstrapMethod, name, type, staticArguments, caller);
        return Invokers.linkToCallSiteMethod(type);
    }

    static MethodType findMethodHandleType(Class<?> rtype, Class<?>[] ptypes) {
        return MethodType.makeImpl(rtype, ptypes, true);
    }

    static MemberName linkMethod(Class<?> callerClass, int refKind, Class<?> defc, String name, Object type, Object[] appendixResult) {
        if (!TRACE_METHOD_LINKAGE)
            return linkMethodImpl(callerClass, refKind, defc, name, type, appendixResult);
        return linkMethodTracing(callerClass, refKind, defc, name, type, appendixResult);
    }

    static MemberName linkMethodImpl(Class<?> callerClass, int refKind, Class<?> defc, String name, Object type, Object[] appendixResult) {
        try {
            if (defc == MethodHandle.class && refKind == REF_invokeVirtual) {
                switch(name) {
                    case "invoke":
                        return Invokers.genericInvokerMethod(fixMethodType(callerClass, type), appendixResult);
                    case "invokeExact":
                        return Invokers.exactInvokerMethod(fixMethodType(callerClass, type), appendixResult);
                }
            }
        } catch (Throwable ex) {
            if (ex instanceof LinkageError)
                throw (LinkageError) ex;
            else
                throw new LinkageError(ex.getMessage(), ex);
        }
        throw new LinkageError("no such method " + defc.getName() + "." + name + type);
    }

    private static MethodType fixMethodType(Class<?> callerClass, Object type) {
        if (type instanceof MethodType)
            return (MethodType) type;
        else
            return MethodType.fromMethodDescriptorString((String) type, callerClass.getClassLoader());
    }

    static MemberName linkMethodTracing(Class<?> callerClass, int refKind, Class<?> defc, String name, Object type, Object[] appendixResult) {
        System.out.println("linkMethod " + defc.getName() + "." + name + type + "/" + Integer.toHexString(refKind));
        try {
            MemberName res = linkMethodImpl(callerClass, refKind, defc, name, type, appendixResult);
            System.out.println("linkMethod => " + res + " + " + appendixResult[0]);
            return res;
        } catch (Throwable ex) {
            System.out.println("linkMethod => throw " + ex);
            throw ex;
        }
    }

    static MethodHandle linkMethodHandleConstant(Class<?> callerClass, int refKind, Class<?> defc, String name, Object type) {
        try {
            Lookup lookup = IMPL_LOOKUP.in(callerClass);
            assert (refKindIsValid(refKind));
            return lookup.linkMethodHandleConstant((byte) refKind, defc, name, type);
        } catch (ReflectiveOperationException ex) {
            Error err = new IncompatibleClassChangeError();
            err.initCause(ex);
            throw err;
        }
    }

    static boolean isCallerSensitive(MemberName mem) {
        assert (mem.isInvocable());
        Class<?> defc = mem.getDeclaringClass();
        switch(mem.getName()) {
            case "doPrivileged":
                return defc == java.security.AccessController.class;
            case "getUnsafe":
                return defc == sun.misc.Unsafe.class;
            case "lookup":
                return defc == java.lang.invoke.MethodHandles.class;
            case "invoke":
                return defc == java.lang.reflect.Method.class;
            case "get":
            case "getBoolean":
            case "getByte":
            case "getChar":
            case "getShort":
            case "getInt":
            case "getLong":
            case "getFloat":
            case "getDouble":
            case "set":
            case "setBoolean":
            case "setByte":
            case "setChar":
            case "setShort":
            case "setInt":
            case "setLong":
            case "setFloat":
            case "setDouble":
                return defc == java.lang.reflect.Field.class;
            case "newInstance":
                if (defc == java.lang.reflect.Constructor.class)
                    return true;
                if (defc == java.lang.Class.class)
                    return true;
                break;
            case "forName":
            case "getClassLoader":
            case "getClasses":
            case "getFields":
            case "getMethods":
            case "getConstructors":
            case "getDeclaredClasses":
            case "getDeclaredFields":
            case "getDeclaredMethods":
            case "getDeclaredConstructors":
            case "getField":
            case "getMethod":
            case "getConstructor":
            case "getDeclaredField":
            case "getDeclaredMethod":
            case "getDeclaredConstructor":
                return defc == java.lang.Class.class;
            case "getConnection":
            case "getDriver":
            case "getDrivers":
            case "deregisterDriver":
                return defc == java.sql.DriverManager.class;
            case "newUpdater":
                if (defc == java.util.concurrent.atomic.AtomicIntegerFieldUpdater.class)
                    return true;
                if (defc == java.util.concurrent.atomic.AtomicLongFieldUpdater.class)
                    return true;
                if (defc == java.util.concurrent.atomic.AtomicReferenceFieldUpdater.class)
                    return true;
                break;
            case "getContextClassLoader":
                return defc == java.lang.Thread.class;
            case "getPackage":
            case "getPackages":
                return defc == java.lang.Package.class;
            case "getParent":
            case "getSystemClassLoader":
                return defc == java.lang.ClassLoader.class;
            case "load":
            case "loadLibrary":
                if (defc == java.lang.Runtime.class)
                    return true;
                if (defc == java.lang.System.class)
                    return true;
                break;
            case "getCallerClass":
                if (defc == sun.reflect.Reflection.class)
                    return true;
                if (defc == java.lang.System.class)
                    return true;
                break;
            case "getCallerClassLoader":
                return defc == java.lang.ClassLoader.class;
            case "getProxyClass":
            case "newProxyInstance":
                return defc == java.lang.reflect.Proxy.class;
            case "getBundle":
            case "clearCache":
                return defc == java.util.ResourceBundle.class;
        }
        return false;
    }
}
