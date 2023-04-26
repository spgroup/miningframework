package java.lang.invoke;

import java.util.*;
import sun.invoke.util.*;
import sun.misc.Unsafe;
import static java.lang.invoke.MethodHandleStatics.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class MethodHandle {

    static {
        MethodHandleImpl.initStatics();
    }

    @java.lang.annotation.Target({ java.lang.annotation.ElementType.METHOD })
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @interface PolymorphicSignature {
    }

    private final MethodType type;

    final LambdaForm form;

    public MethodType type() {
        return type;
    }

    MethodHandle(MethodType type, LambdaForm form) {
        type.getClass();
        form.getClass();
        this.type = type;
        this.form = form;
        form.prepare();
    }

    @PolymorphicSignature
    public final native Object invokeExact(Object... args) throws Throwable;

    @PolymorphicSignature
    public final native Object invoke(Object... args) throws Throwable;

    @PolymorphicSignature
    final native Object invokeBasic(Object... args) throws Throwable;

    @PolymorphicSignature
    static native Object linkToVirtual(Object... args) throws Throwable;

    @PolymorphicSignature
    static native Object linkToStatic(Object... args) throws Throwable;

    @PolymorphicSignature
    static native Object linkToSpecial(Object... args) throws Throwable;

    @PolymorphicSignature
    static native Object linkToInterface(Object... args) throws Throwable;

    public Object invokeWithArguments(Object... arguments) throws Throwable {
        int argc = arguments == null ? 0 : arguments.length;
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        MethodType type = type();
        if (type.parameterCount() != argc || isVarargsCollector()) {
            return asType(MethodType.genericMethodType(argc)).invokeWithArguments(arguments);
        }
        MethodHandle invoker = type.invokers().varargsInvoker();
        return invoker.invokeExact(this, arguments);
    }

    public Object invokeWithArguments(java.util.List<?> arguments) throws Throwable {
        return invokeWithArguments(arguments.toArray());
    }

    public MethodHandle asType(MethodType newType) {
        if (!type.isConvertibleTo(newType)) {
            throw new WrongMethodTypeException("cannot convert " + this + " to " + newType);
        }
        return convertArguments(newType);
    }

    public MethodHandle asSpreader(Class<?> arrayType, int arrayLength) {
        asSpreaderChecks(arrayType, arrayLength);
        int spreadArgPos = type.parameterCount() - arrayLength;
        return MethodHandleImpl.makeSpreadArguments(this, arrayType, spreadArgPos, arrayLength);
    }

    private void asSpreaderChecks(Class<?> arrayType, int arrayLength) {
        spreadArrayChecks(arrayType, arrayLength);
        int nargs = type().parameterCount();
        if (nargs < arrayLength || arrayLength < 0)
            throw newIllegalArgumentException("bad spread array length");
        if (arrayType != Object[].class && arrayLength != 0) {
            boolean sawProblem = false;
            Class<?> arrayElement = arrayType.getComponentType();
            for (int i = nargs - arrayLength; i < nargs; i++) {
                if (!MethodType.canConvert(arrayElement, type().parameterType(i))) {
                    sawProblem = true;
                    break;
                }
            }
            if (sawProblem) {
                ArrayList<Class<?>> ptypes = new ArrayList<>(type().parameterList());
                for (int i = nargs - arrayLength; i < nargs; i++) {
                    ptypes.set(i, arrayElement);
                }
                this.asType(MethodType.methodType(type().returnType(), ptypes));
            }
        }
    }

    private void spreadArrayChecks(Class<?> arrayType, int arrayLength) {
        Class<?> arrayElement = arrayType.getComponentType();
        if (arrayElement == null)
            throw newIllegalArgumentException("not an array type", arrayType);
        if ((arrayLength & 0x7F) != arrayLength) {
            if ((arrayLength & 0xFF) != arrayLength)
                throw newIllegalArgumentException("array length is not legal", arrayLength);
            assert (arrayLength >= 128);
            if (arrayElement == long.class || arrayElement == double.class)
                throw newIllegalArgumentException("array length is not legal for long[] or double[]", arrayLength);
        }
    }

    public MethodHandle asCollector(Class<?> arrayType, int arrayLength) {
        asCollectorChecks(arrayType, arrayLength);
        int collectArgPos = type().parameterCount() - 1;
        MethodHandle target = this;
        if (arrayType != type().parameterType(collectArgPos))
            target = convertArguments(type().changeParameterType(collectArgPos, arrayType));
        MethodHandle collector = ValueConversions.varargsArray(arrayType, arrayLength);
        return MethodHandles.collectArguments(target, collectArgPos, collector);
    }

    private boolean asCollectorChecks(Class<?> arrayType, int arrayLength) {
        spreadArrayChecks(arrayType, arrayLength);
        int nargs = type().parameterCount();
        if (nargs != 0) {
            Class<?> lastParam = type().parameterType(nargs - 1);
            if (lastParam == arrayType)
                return true;
            if (lastParam.isAssignableFrom(arrayType))
                return false;
        }
        throw newIllegalArgumentException("array type not assignable to trailing argument", this, arrayType);
    }

    public MethodHandle asVarargsCollector(Class<?> arrayType) {
        Class<?> arrayElement = arrayType.getComponentType();
        boolean lastMatch = asCollectorChecks(arrayType, 0);
        if (isVarargsCollector() && lastMatch)
            return this;
        return MethodHandleImpl.makeVarargsCollector(this, arrayType);
    }

    public boolean isVarargsCollector() {
        return false;
    }

    public MethodHandle asFixedArity() {
        assert (!isVarargsCollector());
        return this;
    }

    public MethodHandle bindTo(Object x) {
        Class<?> ptype;
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        MethodType type = type();
        if (type.parameterCount() == 0 || (ptype = type.parameterType(0)).isPrimitive())
            throw newIllegalArgumentException("no leading reference parameter", x);
        x = ptype.cast(x);
        return bindReceiver(x);
    }

    @Override
    public String toString() {
        if (DEBUG_METHOD_HANDLE_NAMES)
            return debugString();
        return standardString();
    }

    String standardString() {
        return "MethodHandle" + type;
    }

    String debugString() {
        return standardString() + "/LF=" + internalForm() + internalProperties();
    }

    MethodHandle setVarargs(MemberName member) throws IllegalAccessException {
        if (!member.isVarargs())
            return this;
        int argc = type().parameterCount();
        if (argc != 0) {
            Class<?> arrayType = type().parameterType(argc - 1);
            if (arrayType.isArray()) {
                return MethodHandleImpl.makeVarargsCollector(this, arrayType);
            }
        }
        throw member.makeAccessException("cannot make variable arity", null);
    }

    MethodHandle viewAsType(MethodType newType) {
        return MethodHandleImpl.makePairwiseConvert(this, newType, 0);
    }

    LambdaForm internalForm() {
        return form;
    }

    MemberName internalMemberName() {
        return null;
    }

    MethodHandle withInternalMemberName(MemberName member) {
        if (member != null) {
            return MethodHandleImpl.makeWrappedMember(this, member);
        } else if (internalMemberName() == null) {
            return this;
        } else {
            MethodHandle result = rebind();
            assert (result.internalMemberName() == null);
            return result;
        }
    }

    boolean isInvokeSpecial() {
        return false;
    }

    Object internalValues() {
        return null;
    }

    Object internalProperties() {
        return "";
    }

    MethodHandle convertArguments(MethodType newType) {
        return MethodHandleImpl.makePairwiseConvert(this, newType, 1);
    }

    MethodHandle bindArgument(int pos, char basicType, Object value) {
        return rebind().bindArgument(pos, basicType, value);
    }

    MethodHandle bindReceiver(Object receiver) {
        return bindArgument(0, 'L', receiver);
    }

    MethodHandle bindImmediate(int pos, char basicType, Object value) {
        assert pos == 0 && basicType == 'L' && value instanceof Unsafe;
        MethodType type2 = type.dropParameterTypes(pos, pos + 1);
        LambdaForm form2 = form.bindImmediate(pos + 1, basicType, value);
        return copyWith(type2, form2);
    }

    MethodHandle copyWith(MethodType mt, LambdaForm lf) {
        throw new InternalError("copyWith: " + this.getClass());
    }

    MethodHandle dropArguments(MethodType srcType, int pos, int drops) {
        return rebind().dropArguments(srcType, pos, drops);
    }

    MethodHandle permuteArguments(MethodType newType, int[] reorder) {
        return rebind().permuteArguments(newType, reorder);
    }

    MethodHandle rebind() {
        MethodType type2 = type();
        LambdaForm form2 = reinvokerForm(this);
        return BoundMethodHandle.bindSingle(type2, form2, this);
    }

    MethodHandle reinvokerTarget() {
        throw new InternalError("not a reinvoker MH: " + this.getClass().getName() + ": " + this);
    }

    static LambdaForm reinvokerForm(MethodHandle target) {
        MethodType mtype = target.type().basicType();
        LambdaForm reinvoker = mtype.form().cachedLambdaForm(MethodTypeForm.LF_REINVOKE);
        if (reinvoker != null)
            return reinvoker;
        if (mtype.parameterSlotCount() >= MethodType.MAX_MH_ARITY)
            return makeReinvokerForm(target.type(), target);
        reinvoker = makeReinvokerForm(mtype, null);
        return mtype.form().setCachedLambdaForm(MethodTypeForm.LF_REINVOKE, reinvoker);
    }

    private static LambdaForm makeReinvokerForm(MethodType mtype, MethodHandle customTargetOrNull) {
        boolean customized = (customTargetOrNull != null);
        MethodHandle MH_invokeBasic = customized ? null : MethodHandles.basicInvoker(mtype);
        final int THIS_BMH = 0;
        final int ARG_BASE = 1;
        final int ARG_LIMIT = ARG_BASE + mtype.parameterCount();
        int nameCursor = ARG_LIMIT;
        final int NEXT_MH = customized ? -1 : nameCursor++;
        final int REINVOKE = nameCursor++;
        LambdaForm.Name[] names = LambdaForm.arguments(nameCursor - ARG_LIMIT, mtype.invokerType());
        Object[] targetArgs;
        MethodHandle targetMH;
        if (customized) {
            targetArgs = Arrays.copyOfRange(names, ARG_BASE, ARG_LIMIT, Object[].class);
            targetMH = customTargetOrNull;
        } else {
            names[NEXT_MH] = new LambdaForm.Name(NF_reinvokerTarget, names[THIS_BMH]);
            targetArgs = Arrays.copyOfRange(names, THIS_BMH, ARG_LIMIT, Object[].class);
            targetArgs[0] = names[NEXT_MH];
            targetMH = MethodHandles.basicInvoker(mtype);
        }
        names[REINVOKE] = new LambdaForm.Name(targetMH, targetArgs);
        return new LambdaForm("BMH.reinvoke", ARG_LIMIT, names);
    }

    private static final LambdaForm.NamedFunction NF_reinvokerTarget;

    static {
        try {
            NF_reinvokerTarget = new LambdaForm.NamedFunction(MethodHandle.class.getDeclaredMethod("reinvokerTarget"));
        } catch (ReflectiveOperationException ex) {
            throw newInternalError(ex);
        }
    }

    void updateForm(LambdaForm newForm) {
        if (form == newForm)
            return;
        UNSAFE.putObject(this, FORM_OFFSET, newForm);
        this.form.prepare();
    }

    private static final long FORM_OFFSET;

    static {
        try {
            FORM_OFFSET = UNSAFE.objectFieldOffset(MethodHandle.class.getDeclaredField("form"));
        } catch (ReflectiveOperationException ex) {
            throw newInternalError(ex);
        }
    }
}
