package java.lang.invoke;

import sun.misc.Unsafe;
import java.lang.reflect.Method;
import java.util.Arrays;
import sun.invoke.util.VerifyAccess;
import static java.lang.invoke.MethodHandleNatives.Constants.*;
import static java.lang.invoke.LambdaForm.*;
import static java.lang.invoke.MethodTypeForm.*;
import static java.lang.invoke.MethodHandleStatics.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Objects;
import sun.invoke.util.ValueConversions;
import sun.invoke.util.VerifyType;
import sun.invoke.util.Wrapper;

class DirectMethodHandle extends MethodHandle {

    final MemberName member;

    private DirectMethodHandle(MethodType mtype, LambdaForm form, MemberName member) {
        super(mtype, form);
        if (!member.isResolved())
            throw new InternalError();
        if (member.getDeclaringClass().isInterface() && member.isMethod() && !member.isAbstract()) {
            MemberName m = new MemberName(Object.class, member.getName(), member.getMethodType(), member.getReferenceKind());
            m = MemberName.getFactory().resolveOrNull(m.getReferenceKind(), m, null);
            if (m != null && m.isPublic()) {
                assert (member.getReferenceKind() == m.getReferenceKind());
                member = m;
            }
        }
        this.member = member;
    }

    static DirectMethodHandle make(byte refKind, Class<?> receiver, MemberName member) {
        MethodType mtype = member.getMethodOrFieldType();
        if (!member.isStatic()) {
            if (!member.getDeclaringClass().isAssignableFrom(receiver) || member.isConstructor())
                throw new InternalError(member.toString());
            mtype = mtype.insertParameterTypes(0, receiver);
        }
        if (!member.isField()) {
            if (refKind == REF_invokeSpecial) {
                member = member.asSpecial();
                LambdaForm lform = preparedLambdaForm(member);
                return new Special(mtype, lform, member);
            } else {
                LambdaForm lform = preparedLambdaForm(member);
                return new DirectMethodHandle(mtype, lform, member);
            }
        } else {
            LambdaForm lform = preparedFieldLambdaForm(member);
            if (member.isStatic()) {
                long offset = MethodHandleNatives.staticFieldOffset(member);
                Object base = MethodHandleNatives.staticFieldBase(member);
                return new StaticAccessor(mtype, lform, member, base, offset);
            } else {
                long offset = MethodHandleNatives.objectFieldOffset(member);
                assert (offset == (int) offset);
                return new Accessor(mtype, lform, member, (int) offset);
            }
        }
    }

    static DirectMethodHandle make(Class<?> receiver, MemberName member) {
        byte refKind = member.getReferenceKind();
        if (refKind == REF_invokeSpecial)
            refKind = REF_invokeVirtual;
        return make(refKind, receiver, member);
    }

    static DirectMethodHandle make(MemberName member) {
        if (member.isConstructor())
            return makeAllocator(member);
        return make(member.getDeclaringClass(), member);
    }

    static DirectMethodHandle make(Method method) {
        return make(method.getDeclaringClass(), new MemberName(method));
    }

    static DirectMethodHandle make(Field field) {
        return make(field.getDeclaringClass(), new MemberName(field));
    }

    private static DirectMethodHandle makeAllocator(MemberName ctor) {
        assert (ctor.isConstructor() && ctor.getName().equals("<init>"));
        Class<?> instanceClass = ctor.getDeclaringClass();
        ctor = ctor.asConstructor();
        assert (ctor.isConstructor() && ctor.getReferenceKind() == REF_newInvokeSpecial) : ctor;
        MethodType mtype = ctor.getMethodType().changeReturnType(instanceClass);
        LambdaForm lform = preparedLambdaForm(ctor);
        MemberName init = ctor.asSpecial();
        assert (init.getMethodType().returnType() == void.class);
        return new Constructor(mtype, lform, ctor, init, instanceClass);
    }

    @Override
    BoundMethodHandle rebind() {
        return BoundMethodHandle.makeReinvoker(this);
    }

    @Override
    MethodHandle copyWith(MethodType mt, LambdaForm lf) {
        assert (this.getClass() == DirectMethodHandle.class);
        return new DirectMethodHandle(mt, lf, member);
    }

    @Override
    String internalProperties() {
        return "\n& DMH.MN=" + internalMemberName();
    }

    @Override
    @ForceInline
    MemberName internalMemberName() {
        return member;
    }

    private static final MemberName.Factory IMPL_NAMES = MemberName.getFactory();

    private static LambdaForm preparedLambdaForm(MemberName m) {
        assert (m.isInvocable()) : m;
        MethodType mtype = m.getInvocationType().basicType();
        assert (!m.isMethodHandleInvoke() || "invokeBasic".equals(m.getName())) : m;
        int which;
        switch(m.getReferenceKind()) {
            case REF_invokeVirtual:
                which = LF_INVVIRTUAL;
                break;
            case REF_invokeStatic:
                which = LF_INVSTATIC;
                break;
            case REF_invokeSpecial:
                which = LF_INVSPECIAL;
                break;
            case REF_invokeInterface:
                which = LF_INVINTERFACE;
                break;
            case REF_newInvokeSpecial:
                which = LF_NEWINVSPECIAL;
                break;
            default:
                throw new InternalError(m.toString());
        }
        if (which == LF_INVSTATIC && shouldBeInitialized(m)) {
            preparedLambdaForm(mtype, which);
            which = LF_INVSTATIC_INIT;
        }
        LambdaForm lform = preparedLambdaForm(mtype, which);
        maybeCompile(lform, m);
        assert (lform.methodType().dropParameterTypes(0, 1).equals(m.getInvocationType().basicType())) : Arrays.asList(m, m.getInvocationType().basicType(), lform, lform.methodType());
        return lform;
    }

    private static LambdaForm preparedLambdaForm(MethodType mtype, int which) {
        LambdaForm lform = mtype.form().cachedLambdaForm(which);
        if (lform != null)
            return lform;
        lform = makePreparedLambdaForm(mtype, which);
        return mtype.form().setCachedLambdaForm(which, lform);
    }

    private static LambdaForm makePreparedLambdaForm(MethodType mtype, int which) {
        boolean needsInit = (which == LF_INVSTATIC_INIT);
        boolean doesAlloc = (which == LF_NEWINVSPECIAL);
        String linkerName, lambdaName;
        switch(which) {
            case LF_INVVIRTUAL:
                linkerName = "linkToVirtual";
                lambdaName = "DMH.invokeVirtual";
                break;
            case LF_INVSTATIC:
                linkerName = "linkToStatic";
                lambdaName = "DMH.invokeStatic";
                break;
            case LF_INVSTATIC_INIT:
                linkerName = "linkToStatic";
                lambdaName = "DMH.invokeStaticInit";
                break;
            case LF_INVSPECIAL:
                linkerName = "linkToSpecial";
                lambdaName = "DMH.invokeSpecial";
                break;
            case LF_INVINTERFACE:
                linkerName = "linkToInterface";
                lambdaName = "DMH.invokeInterface";
                break;
            case LF_NEWINVSPECIAL:
                linkerName = "linkToSpecial";
                lambdaName = "DMH.newInvokeSpecial";
                break;
            default:
                throw new InternalError("which=" + which);
        }
        MethodType mtypeWithArg = mtype.appendParameterTypes(MemberName.class);
        if (doesAlloc)
            mtypeWithArg = mtypeWithArg.insertParameterTypes(0, Object.class).changeReturnType(void.class);
        MemberName linker = new MemberName(MethodHandle.class, linkerName, mtypeWithArg, REF_invokeStatic);
        try {
            linker = IMPL_NAMES.resolveOrFail(REF_invokeStatic, linker, null, NoSuchMethodException.class);
        } catch (ReflectiveOperationException ex) {
            throw newInternalError(ex);
        }
        final int DMH_THIS = 0;
        final int ARG_BASE = 1;
        final int ARG_LIMIT = ARG_BASE + mtype.parameterCount();
        int nameCursor = ARG_LIMIT;
        final int NEW_OBJ = (doesAlloc ? nameCursor++ : -1);
        final int GET_MEMBER = nameCursor++;
        final int LINKER_CALL = nameCursor++;
        Name[] names = arguments(nameCursor - ARG_LIMIT, mtype.invokerType());
        assert (names.length == nameCursor);
        if (doesAlloc) {
            names[NEW_OBJ] = new Name(Lazy.NF_allocateInstance, names[DMH_THIS]);
            names[GET_MEMBER] = new Name(Lazy.NF_constructorMethod, names[DMH_THIS]);
        } else if (needsInit) {
            names[GET_MEMBER] = new Name(Lazy.NF_internalMemberNameEnsureInit, names[DMH_THIS]);
        } else {
            names[GET_MEMBER] = new Name(Lazy.NF_internalMemberName, names[DMH_THIS]);
        }
        assert (findDirectMethodHandle(names[GET_MEMBER]) == names[DMH_THIS]);
        Object[] outArgs = Arrays.copyOfRange(names, ARG_BASE, GET_MEMBER + 1, Object[].class);
        assert (outArgs[outArgs.length - 1] == names[GET_MEMBER]);
        int result = LAST_RESULT;
        if (doesAlloc) {
            assert (outArgs[outArgs.length - 2] == names[NEW_OBJ]);
            System.arraycopy(outArgs, 0, outArgs, 1, outArgs.length - 2);
            outArgs[0] = names[NEW_OBJ];
            result = NEW_OBJ;
        }
        names[LINKER_CALL] = new Name(linker, outArgs);
        lambdaName += "_" + shortenSignature(basicTypeSignature(mtype));
        LambdaForm lform = new LambdaForm(lambdaName, ARG_LIMIT, names, result);
        lform.compileToBytecode();
        return lform;
    }

    static Object findDirectMethodHandle(Name name) {
        if (name.function == Lazy.NF_internalMemberName || name.function == Lazy.NF_internalMemberNameEnsureInit || name.function == Lazy.NF_constructorMethod) {
            assert (name.arguments.length == 1);
            return name.arguments[0];
        }
        return null;
    }

    private static void maybeCompile(LambdaForm lform, MemberName m) {
        if (VerifyAccess.isSamePackage(m.getDeclaringClass(), MethodHandle.class))
            lform.compileToBytecode();
    }

    @ForceInline
    static Object internalMemberName(Object mh) {
        return ((DirectMethodHandle) mh).member;
    }

    static Object internalMemberNameEnsureInit(Object mh) {
        DirectMethodHandle dmh = (DirectMethodHandle) mh;
        dmh.ensureInitialized();
        return dmh.member;
    }

    static boolean shouldBeInitialized(MemberName member) {
        switch(member.getReferenceKind()) {
            case REF_invokeStatic:
            case REF_getStatic:
            case REF_putStatic:
            case REF_newInvokeSpecial:
                break;
            default:
                return false;
        }
        Class<?> cls = member.getDeclaringClass();
        if (cls == ValueConversions.class || cls == MethodHandleImpl.class || cls == Invokers.class) {
            return false;
        }
        if (VerifyAccess.isSamePackage(MethodHandle.class, cls) || VerifyAccess.isSamePackage(ValueConversions.class, cls)) {
            if (UNSAFE.shouldBeInitialized(cls)) {
                UNSAFE.ensureClassInitialized(cls);
            }
            return false;
        }
        return UNSAFE.shouldBeInitialized(cls);
    }

    private static class EnsureInitialized extends ClassValue<WeakReference<Thread>> {

        @Override
        protected WeakReference<Thread> computeValue(Class<?> type) {
            UNSAFE.ensureClassInitialized(type);
            if (UNSAFE.shouldBeInitialized(type))
                return new WeakReference<>(Thread.currentThread());
            return null;
        }

        static final EnsureInitialized INSTANCE = new EnsureInitialized();
    }

    private void ensureInitialized() {
        if (checkInitialized(member)) {
            if (member.isField())
                updateForm(preparedFieldLambdaForm(member));
            else
                updateForm(preparedLambdaForm(member));
        }
    }

    private static boolean checkInitialized(MemberName member) {
        Class<?> defc = member.getDeclaringClass();
        WeakReference<Thread> ref = EnsureInitialized.INSTANCE.get(defc);
        if (ref == null) {
            return true;
        }
        Thread clinitThread = ref.get();
        if (clinitThread == Thread.currentThread()) {
            if (UNSAFE.shouldBeInitialized(defc))
                return false;
        } else {
            UNSAFE.ensureClassInitialized(defc);
        }
        assert (!UNSAFE.shouldBeInitialized(defc));
        EnsureInitialized.INSTANCE.remove(defc);
        return true;
    }

    static void ensureInitialized(Object mh) {
        ((DirectMethodHandle) mh).ensureInitialized();
    }

    static class Special extends DirectMethodHandle {

        private Special(MethodType mtype, LambdaForm form, MemberName member) {
            super(mtype, form, member);
        }

        @Override
        boolean isInvokeSpecial() {
            return true;
        }

        @Override
        MethodHandle copyWith(MethodType mt, LambdaForm lf) {
            return new Special(mt, lf, member);
        }
    }

    static class Constructor extends DirectMethodHandle {

        final MemberName initMethod;

        final Class<?> instanceClass;

        private Constructor(MethodType mtype, LambdaForm form, MemberName constructor, MemberName initMethod, Class<?> instanceClass) {
            super(mtype, form, constructor);
            this.initMethod = initMethod;
            this.instanceClass = instanceClass;
            assert (initMethod.isResolved());
        }

        @Override
        MethodHandle copyWith(MethodType mt, LambdaForm lf) {
            return new Constructor(mt, lf, member, initMethod, instanceClass);
        }
    }

    static Object constructorMethod(Object mh) {
        Constructor dmh = (Constructor) mh;
        return dmh.initMethod;
    }

    static Object allocateInstance(Object mh) throws InstantiationException {
        Constructor dmh = (Constructor) mh;
        return UNSAFE.allocateInstance(dmh.instanceClass);
    }

    static class Accessor extends DirectMethodHandle {

        final Class<?> fieldType;

        final int fieldOffset;

        private Accessor(MethodType mtype, LambdaForm form, MemberName member, int fieldOffset) {
            super(mtype, form, member);
            this.fieldType = member.getFieldType();
            this.fieldOffset = fieldOffset;
        }

        @Override
        Object checkCast(Object obj) {
            return fieldType.cast(obj);
        }

        @Override
        MethodHandle copyWith(MethodType mt, LambdaForm lf) {
            return new Accessor(mt, lf, member, fieldOffset);
        }
    }

    @ForceInline
    static long fieldOffset(Object accessorObj) {
        return ((Accessor) accessorObj).fieldOffset;
    }

    @ForceInline
    static Object checkBase(Object obj) {
        return Objects.requireNonNull(obj);
    }

    static class StaticAccessor extends DirectMethodHandle {

        private final Class<?> fieldType;

        private final Object staticBase;

        private final long staticOffset;

        private StaticAccessor(MethodType mtype, LambdaForm form, MemberName member, Object staticBase, long staticOffset) {
            super(mtype, form, member);
            this.fieldType = member.getFieldType();
            this.staticBase = staticBase;
            this.staticOffset = staticOffset;
        }

        @Override
        Object checkCast(Object obj) {
            return fieldType.cast(obj);
        }

        @Override
        MethodHandle copyWith(MethodType mt, LambdaForm lf) {
            return new StaticAccessor(mt, lf, member, staticBase, staticOffset);
        }
    }

    @ForceInline
    static Object nullCheck(Object obj) {
        return Objects.requireNonNull(obj);
    }

    @ForceInline
    static Object staticBase(Object accessorObj) {
        return ((StaticAccessor) accessorObj).staticBase;
    }

    @ForceInline
    static long staticOffset(Object accessorObj) {
        return ((StaticAccessor) accessorObj).staticOffset;
    }

    @ForceInline
    static Object checkCast(Object mh, Object obj) {
        return ((DirectMethodHandle) mh).checkCast(obj);
    }

    Object checkCast(Object obj) {
        return member.getReturnType().cast(obj);
    }

    private static byte AF_GETFIELD = 0, AF_PUTFIELD = 1, AF_GETSTATIC = 2, AF_PUTSTATIC = 3, AF_GETSTATIC_INIT = 4, AF_PUTSTATIC_INIT = 5, AF_LIMIT = 6;

    private static int FT_LAST_WRAPPER = Wrapper.values().length - 1, FT_UNCHECKED_REF = Wrapper.OBJECT.ordinal(), FT_CHECKED_REF = FT_LAST_WRAPPER + 1, FT_LIMIT = FT_LAST_WRAPPER + 2;

    private static int afIndex(byte formOp, boolean isVolatile, int ftypeKind) {
        return ((formOp * FT_LIMIT * 2) + (isVolatile ? FT_LIMIT : 0) + ftypeKind);
    }

    private static final LambdaForm[] ACCESSOR_FORMS = new LambdaForm[afIndex(AF_LIMIT, false, 0)];

    private static int ftypeKind(Class<?> ftype) {
        if (ftype.isPrimitive())
            return Wrapper.forPrimitiveType(ftype).ordinal();
        else if (VerifyType.isNullReferenceConversion(Object.class, ftype))
            return FT_UNCHECKED_REF;
        else
            return FT_CHECKED_REF;
    }

    private static LambdaForm preparedFieldLambdaForm(MemberName m) {
        Class<?> ftype = m.getFieldType();
        boolean isVolatile = m.isVolatile();
        byte formOp;
        switch(m.getReferenceKind()) {
            case REF_getField:
                formOp = AF_GETFIELD;
                break;
            case REF_putField:
                formOp = AF_PUTFIELD;
                break;
            case REF_getStatic:
                formOp = AF_GETSTATIC;
                break;
            case REF_putStatic:
                formOp = AF_PUTSTATIC;
                break;
            default:
                throw new InternalError(m.toString());
        }
        if (shouldBeInitialized(m)) {
            preparedFieldLambdaForm(formOp, isVolatile, ftype);
            assert ((AF_GETSTATIC_INIT - AF_GETSTATIC) == (AF_PUTSTATIC_INIT - AF_PUTSTATIC));
            formOp += (AF_GETSTATIC_INIT - AF_GETSTATIC);
        }
        LambdaForm lform = preparedFieldLambdaForm(formOp, isVolatile, ftype);
        maybeCompile(lform, m);
        assert (lform.methodType().dropParameterTypes(0, 1).equals(m.getInvocationType().basicType())) : Arrays.asList(m, m.getInvocationType().basicType(), lform, lform.methodType());
        return lform;
    }

    private static LambdaForm preparedFieldLambdaForm(byte formOp, boolean isVolatile, Class<?> ftype) {
        int afIndex = afIndex(formOp, isVolatile, ftypeKind(ftype));
        LambdaForm lform = ACCESSOR_FORMS[afIndex];
        if (lform != null)
            return lform;
        lform = makePreparedFieldLambdaForm(formOp, isVolatile, ftypeKind(ftype));
        ACCESSOR_FORMS[afIndex] = lform;
        return lform;
    }

    private static LambdaForm makePreparedFieldLambdaForm(byte formOp, boolean isVolatile, int ftypeKind) {
        boolean isGetter = (formOp & 1) == (AF_GETFIELD & 1);
        boolean isStatic = (formOp >= AF_GETSTATIC);
        boolean needsInit = (formOp >= AF_GETSTATIC_INIT);
        boolean needsCast = (ftypeKind == FT_CHECKED_REF);
        Wrapper fw = (needsCast ? Wrapper.OBJECT : Wrapper.values()[ftypeKind]);
        Class<?> ft = fw.primitiveType();
        assert (ftypeKind(needsCast ? String.class : ft) == ftypeKind);
        String tname = fw.primitiveSimpleName();
        String ctname = Character.toUpperCase(tname.charAt(0)) + tname.substring(1);
        if (isVolatile)
            ctname += "Volatile";
        String getOrPut = (isGetter ? "get" : "put");
        String linkerName = (getOrPut + ctname);
        MethodType linkerType;
        if (isGetter)
            linkerType = MethodType.methodType(ft, Object.class, long.class);
        else
            linkerType = MethodType.methodType(void.class, Object.class, long.class, ft);
        MemberName linker = new MemberName(Unsafe.class, linkerName, linkerType, REF_invokeVirtual);
        try {
            linker = IMPL_NAMES.resolveOrFail(REF_invokeVirtual, linker, null, NoSuchMethodException.class);
        } catch (ReflectiveOperationException ex) {
            throw newInternalError(ex);
        }
        MethodType mtype;
        if (isGetter)
            mtype = MethodType.methodType(ft);
        else
            mtype = MethodType.methodType(void.class, ft);
        mtype = mtype.basicType();
        if (!isStatic)
            mtype = mtype.insertParameterTypes(0, Object.class);
        final int DMH_THIS = 0;
        final int ARG_BASE = 1;
        final int ARG_LIMIT = ARG_BASE + mtype.parameterCount();
        final int OBJ_BASE = isStatic ? -1 : ARG_BASE;
        final int SET_VALUE = isGetter ? -1 : ARG_LIMIT - 1;
        int nameCursor = ARG_LIMIT;
        final int F_HOLDER = (isStatic ? nameCursor++ : -1);
        final int F_OFFSET = nameCursor++;
        final int OBJ_CHECK = (OBJ_BASE >= 0 ? nameCursor++ : -1);
        final int INIT_BAR = (needsInit ? nameCursor++ : -1);
        final int PRE_CAST = (needsCast && !isGetter ? nameCursor++ : -1);
        final int LINKER_CALL = nameCursor++;
        final int POST_CAST = (needsCast && isGetter ? nameCursor++ : -1);
        final int RESULT = nameCursor - 1;
        Name[] names = arguments(nameCursor - ARG_LIMIT, mtype.invokerType());
        if (needsInit)
            names[INIT_BAR] = new Name(Lazy.NF_ensureInitialized, names[DMH_THIS]);
        if (needsCast && !isGetter)
            names[PRE_CAST] = new Name(Lazy.NF_checkCast, names[DMH_THIS], names[SET_VALUE]);
        Object[] outArgs = new Object[1 + linkerType.parameterCount()];
        assert (outArgs.length == (isGetter ? 3 : 4));
        outArgs[0] = UNSAFE;
        if (isStatic) {
            outArgs[1] = names[F_HOLDER] = new Name(Lazy.NF_staticBase, names[DMH_THIS]);
            outArgs[2] = names[F_OFFSET] = new Name(Lazy.NF_staticOffset, names[DMH_THIS]);
        } else {
            outArgs[1] = names[OBJ_CHECK] = new Name(Lazy.NF_checkBase, names[OBJ_BASE]);
            outArgs[2] = names[F_OFFSET] = new Name(Lazy.NF_fieldOffset, names[DMH_THIS]);
        }
        if (!isGetter) {
            outArgs[3] = (needsCast ? names[PRE_CAST] : names[SET_VALUE]);
        }
        for (Object a : outArgs) assert (a != null);
        names[LINKER_CALL] = new Name(linker, outArgs);
        if (needsCast && isGetter)
            names[POST_CAST] = new Name(Lazy.NF_checkCast, names[DMH_THIS], names[LINKER_CALL]);
        for (Name n : names) assert (n != null);
        String fieldOrStatic = (isStatic ? "Static" : "Field");
        String lambdaName = (linkerName + fieldOrStatic);
        if (needsCast)
            lambdaName += "Cast";
        if (needsInit)
            lambdaName += "Init";
        return new LambdaForm(lambdaName, ARG_LIMIT, names, RESULT);
    }

    private static class Lazy {

        static final NamedFunction NF_internalMemberName, NF_internalMemberNameEnsureInit, NF_ensureInitialized, NF_fieldOffset, NF_checkBase, NF_staticBase, NF_staticOffset, NF_checkCast, NF_allocateInstance, NF_constructorMethod;

        static {
            try {
                NamedFunction[] nfs = { NF_internalMemberName = new NamedFunction(DirectMethodHandle.class.getDeclaredMethod("internalMemberName", Object.class)), NF_internalMemberNameEnsureInit = new NamedFunction(DirectMethodHandle.class.getDeclaredMethod("internalMemberNameEnsureInit", Object.class)), NF_ensureInitialized = new NamedFunction(DirectMethodHandle.class.getDeclaredMethod("ensureInitialized", Object.class)), NF_fieldOffset = new NamedFunction(DirectMethodHandle.class.getDeclaredMethod("fieldOffset", Object.class)), NF_checkBase = new NamedFunction(DirectMethodHandle.class.getDeclaredMethod("checkBase", Object.class)), NF_staticBase = new NamedFunction(DirectMethodHandle.class.getDeclaredMethod("staticBase", Object.class)), NF_staticOffset = new NamedFunction(DirectMethodHandle.class.getDeclaredMethod("staticOffset", Object.class)), NF_checkCast = new NamedFunction(DirectMethodHandle.class.getDeclaredMethod("checkCast", Object.class, Object.class)), NF_allocateInstance = new NamedFunction(DirectMethodHandle.class.getDeclaredMethod("allocateInstance", Object.class)), NF_constructorMethod = new NamedFunction(DirectMethodHandle.class.getDeclaredMethod("constructorMethod", Object.class)) };
                for (NamedFunction nf : nfs) {
                    assert (InvokerBytecodeGenerator.isStaticallyInvocable(nf.member)) : nf;
                    nf.resolve();
                }
            } catch (ReflectiveOperationException ex) {
                throw newInternalError(ex);
            }
        }
    }
}
