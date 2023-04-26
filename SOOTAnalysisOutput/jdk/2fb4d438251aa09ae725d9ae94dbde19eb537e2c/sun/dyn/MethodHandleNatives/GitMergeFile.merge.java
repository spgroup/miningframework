package sun.dyn;

import java.dyn.CallSite;
import java.dyn.MethodHandle;
import java.dyn.MethodType;
import java.dyn.MethodHandles.Lookup;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import static sun.dyn.MethodHandleNatives.Constants.*;
import static sun.dyn.MethodHandleImpl.IMPL_LOOKUP;

class MethodHandleNatives {

    private MethodHandleNatives() {
    }

    static native void init(MemberName self, Object ref);

    static native void expand(MemberName self);

    static native void resolve(MemberName self, Class<?> caller);

    static native int getMembers(Class<?> defc, String matchName, String matchSig, int matchFlags, Class<?> caller, int skip, MemberName[] results);

    static native void init(AdapterMethodHandle self, MethodHandle target, int argnum);

    static native void init(BoundMethodHandle self, Object target, int argnum);

    static native void init(DirectMethodHandle self, Object ref, boolean doDispatch, Class<?> caller);

    static native void init(MethodType self);

    static native void registerBootstrap(Class<?> caller, MethodHandle bootstrapMethod);

    static native MethodHandle getBootstrap(Class<?> caller);

    static native void setCallSiteTarget(CallSite site, MethodHandle target);

    static native Object getTarget(MethodHandle self, int format);

    static MemberName getMethodName(MethodHandle self) {
        if (!JVM_SUPPORT)
            return null;
        return (MemberName) getTarget(self, ETF_METHOD_NAME);
    }

    static AccessibleObject getTargetMethod(MethodHandle self) {
        if (!JVM_SUPPORT)
            return null;
        return (AccessibleObject) getTarget(self, ETF_REFLECT_METHOD);
    }

    static Object getTargetInfo(MethodHandle self) {
        if (!JVM_SUPPORT)
            return null;
        return getTarget(self, ETF_HANDLE_OR_METHOD_NAME);
    }

    static Object[] makeTarget(Class<?> defc, String name, String sig, int mods, Class<?> refc) {
        return new Object[] { defc, name, sig, mods, refc };
    }

    static native int getConstant(int which);

    public static final boolean JVM_SUPPORT;

    static final int JVM_PUSH_LIMIT;

    static final int JVM_STACK_MOVE_UNIT;

    static final int CONV_OP_IMPLEMENTED_MASK;

    private static native void registerNatives();

    static {
        boolean JVM_SUPPORT_;
        int JVM_PUSH_LIMIT_;
        int JVM_STACK_MOVE_UNIT_;
        int CONV_OP_IMPLEMENTED_MASK_;
        try {
            registerNatives();
            JVM_SUPPORT_ = true;
            JVM_PUSH_LIMIT_ = getConstant(Constants.GC_JVM_PUSH_LIMIT);
            JVM_STACK_MOVE_UNIT_ = getConstant(Constants.GC_JVM_STACK_MOVE_UNIT);
            CONV_OP_IMPLEMENTED_MASK_ = getConstant(Constants.GC_CONV_OP_IMPLEMENTED_MASK);
        } catch (UnsatisfiedLinkError ee) {
            JVM_SUPPORT_ = false;
            JVM_PUSH_LIMIT_ = 3;
            JVM_STACK_MOVE_UNIT_ = -1;
            CONV_OP_IMPLEMENTED_MASK_ = 0;
            JVM_SUPPORT = JVM_SUPPORT_;
            JVM_PUSH_LIMIT = JVM_PUSH_LIMIT_;
            JVM_STACK_MOVE_UNIT = JVM_STACK_MOVE_UNIT_;
            throw ee;
        }
        JVM_SUPPORT = JVM_SUPPORT_;
        JVM_PUSH_LIMIT = JVM_PUSH_LIMIT_;
        JVM_STACK_MOVE_UNIT = JVM_STACK_MOVE_UNIT_;
        if (CONV_OP_IMPLEMENTED_MASK_ == 0)
            CONV_OP_IMPLEMENTED_MASK_ = DEFAULT_CONV_OP_IMPLEMENTED_MASK;
        CONV_OP_IMPLEMENTED_MASK = CONV_OP_IMPLEMENTED_MASK_;
    }

    static class Constants {

        Constants() {
        }

        static final int GC_JVM_PUSH_LIMIT = 0, GC_JVM_STACK_MOVE_UNIT = 1, GC_CONV_OP_IMPLEMENTED_MASK = 2;

        static final int ETF_HANDLE_OR_METHOD_NAME = 0, ETF_DIRECT_HANDLE = 1, ETF_METHOD_NAME = 2, ETF_REFLECT_METHOD = 3;

        static final int MN_IS_METHOD = 0x00010000, MN_IS_CONSTRUCTOR = 0x00020000, MN_IS_FIELD = 0x00040000, MN_IS_TYPE = 0x00080000, MN_SEARCH_SUPERCLASSES = 0x00100000, MN_SEARCH_INTERFACES = 0x00200000, VM_INDEX_UNINITIALIZED = -99;

        static final int OP_RETYPE_ONLY = 0x0, OP_RETYPE_RAW = 0x1, OP_CHECK_CAST = 0x2, OP_PRIM_TO_PRIM = 0x3, OP_REF_TO_PRIM = 0x4, OP_PRIM_TO_REF = 0x5, OP_SWAP_ARGS = 0x6, OP_ROT_ARGS = 0x7, OP_DUP_ARGS = 0x8, OP_DROP_ARGS = 0x9, OP_COLLECT_ARGS = 0xA, OP_SPREAD_ARGS = 0xB, OP_FLYBY = 0xC, OP_RICOCHET = 0xD, CONV_OP_LIMIT = 0xE;

        static final int CONV_OP_MASK = 0xF00, CONV_VMINFO_MASK = 0x0FF, CONV_VMINFO_SHIFT = 0, CONV_OP_SHIFT = 8, CONV_DEST_TYPE_SHIFT = 12, CONV_SRC_TYPE_SHIFT = 16, CONV_STACK_MOVE_SHIFT = 20, CONV_STACK_MOVE_MASK = (1 << (32 - CONV_STACK_MOVE_SHIFT)) - 1;

        static final int DEFAULT_CONV_OP_IMPLEMENTED_MASK = ((1 << OP_RETYPE_ONLY) | (1 << OP_RETYPE_RAW) | (1 << OP_CHECK_CAST) | (1 << OP_PRIM_TO_PRIM) | (1 << OP_REF_TO_PRIM) | (1 << OP_SWAP_ARGS) | (1 << OP_ROT_ARGS) | (1 << OP_DUP_ARGS) | (1 << OP_DROP_ARGS));

        static final int T_BOOLEAN = 4, T_CHAR = 5, T_FLOAT = 6, T_DOUBLE = 7, T_BYTE = 8, T_SHORT = 9, T_INT = 10, T_LONG = 11, T_OBJECT = 12, T_VOID = 14;

        static final int REF_getField = 1, REF_getStatic = 2, REF_putField = 3, REF_putStatic = 4, REF_invokeVirtual = 5, REF_invokeStatic = 6, REF_invokeSpecial = 7, REF_newInvokeSpecial = 8, REF_invokeInterface = 9;
    }

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
                if (jval != vmval)
                    throw new InternalError(name + ": JVM has " + vmval + " while Java has " + jval);
            } catch (Exception ex) {
                throw new InternalError(name + ": access failed, got " + ex);
            }
        }
        return true;
    }

    static {
        if (JVM_SUPPORT)
            verifyConstants();
    }

    static CallSite makeDynamicCallSite(MethodHandle bootstrapMethod, String name, MethodType type, Object info, MemberName callerMethod, int callerBCI) {
        return CallSiteImpl.makeSite(bootstrapMethod, name, type, info, callerMethod, callerBCI);
    }

    static MethodType findMethodHandleType(Class<?> rtype, Class<?>[] ptypes) {
        MethodType.genericMethodType(0);
        return MethodTypeImpl.makeImpl(Access.TOKEN, rtype, ptypes, true);
    }

    static MethodHandle linkMethodHandleConstant(Class<?> callerClass, int refKind, Class<?> defc, String name, Object type) {
        Lookup lookup = IMPL_LOOKUP.in(callerClass);
        switch(refKind) {
            case REF_getField:
                return lookup.findGetter(defc, name, (Class<?>) type);
            case REF_getStatic:
                return lookup.findStaticGetter(defc, name, (Class<?>) type);
            case REF_putField:
                return lookup.findSetter(defc, name, (Class<?>) type);
            case REF_putStatic:
                return lookup.findStaticSetter(defc, name, (Class<?>) type);
            case REF_invokeVirtual:
                return lookup.findVirtual(defc, name, (MethodType) type);
            case REF_invokeStatic:
                return lookup.findStatic(defc, name, (MethodType) type);
            case REF_invokeSpecial:
                return lookup.findSpecial(defc, name, (MethodType) type, callerClass);
            case REF_newInvokeSpecial:
                return lookup.findConstructor(defc, (MethodType) type);
            case REF_invokeInterface:
                return lookup.findVirtual(defc, name, (MethodType) type);
        }
        throw new IllegalArgumentException("bad MethodHandle constant " + name + " : " + type);
    }
}
