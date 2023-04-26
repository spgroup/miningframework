package java.lang.invoke;

import jdk.internal.misc.JavaLangInvokeAccess;
import jdk.internal.misc.SharedSecrets;
import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.reflect.CallerSensitive;
import jdk.internal.reflect.Reflection;
import jdk.internal.vm.annotation.ForceInline;
import jdk.internal.vm.annotation.Stable;
import sun.invoke.empty.Empty;
import sun.invoke.util.ValueConversions;
import sun.invoke.util.VerifyType;
import sun.invoke.util.Wrapper;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import static java.lang.invoke.LambdaForm.*;
import static java.lang.invoke.MethodHandleStatics.*;
import static java.lang.invoke.MethodHandles.Lookup.IMPL_LOOKUP;
import static jdk.internal.org.objectweb.asm.Opcodes.*;

abstract class MethodHandleImpl {

    static MethodHandle makeArrayElementAccessor(Class<?> arrayClass, ArrayAccess access) {
        if (arrayClass == Object[].class) {
            return ArrayAccess.objectAccessor(access);
        }
        if (!arrayClass.isArray())
            throw newIllegalArgumentException("not an array: " + arrayClass);
        MethodHandle[] cache = ArrayAccessor.TYPED_ACCESSORS.get(arrayClass);
        int cacheIndex = ArrayAccess.cacheIndex(access);
        MethodHandle mh = cache[cacheIndex];
        if (mh != null)
            return mh;
        mh = ArrayAccessor.getAccessor(arrayClass, access);
        MethodType correctType = ArrayAccessor.correctType(arrayClass, access);
        if (mh.type() != correctType) {
            assert (mh.type().parameterType(0) == Object[].class);
            assert (access != ArrayAccess.SET || mh.type().parameterType(2) == Object.class);
            assert (access != ArrayAccess.GET || (mh.type().returnType() == Object.class && correctType.parameterType(0).getComponentType() == correctType.returnType()));
            mh = mh.viewAsType(correctType, false);
        }
        mh = makeIntrinsic(mh, ArrayAccess.intrinsic(access));
        synchronized (cache) {
            if (cache[cacheIndex] == null) {
                cache[cacheIndex] = mh;
            } else {
                mh = cache[cacheIndex];
            }
        }
        return mh;
    }

    enum ArrayAccess {

        GET, SET, LENGTH;

        static String opName(ArrayAccess a) {
            switch(a) {
                case GET:
                    return "getElement";
                case SET:
                    return "setElement";
                case LENGTH:
                    return "length";
            }
            throw unmatchedArrayAccess(a);
        }

        static MethodHandle objectAccessor(ArrayAccess a) {
            switch(a) {
                case GET:
                    return ArrayAccessor.OBJECT_ARRAY_GETTER;
                case SET:
                    return ArrayAccessor.OBJECT_ARRAY_SETTER;
                case LENGTH:
                    return ArrayAccessor.OBJECT_ARRAY_LENGTH;
            }
            throw unmatchedArrayAccess(a);
        }

        static int cacheIndex(ArrayAccess a) {
            switch(a) {
                case GET:
                    return ArrayAccessor.GETTER_INDEX;
                case SET:
                    return ArrayAccessor.SETTER_INDEX;
                case LENGTH:
                    return ArrayAccessor.LENGTH_INDEX;
            }
            throw unmatchedArrayAccess(a);
        }

        static Intrinsic intrinsic(ArrayAccess a) {
            switch(a) {
                case GET:
                    return Intrinsic.ARRAY_LOAD;
                case SET:
                    return Intrinsic.ARRAY_STORE;
                case LENGTH:
                    return Intrinsic.ARRAY_LENGTH;
            }
            throw unmatchedArrayAccess(a);
        }
    }

    static InternalError unmatchedArrayAccess(ArrayAccess a) {
        return newInternalError("should not reach here (unmatched ArrayAccess: " + a + ")");
    }

    static final class ArrayAccessor {

        static final int GETTER_INDEX = 0, SETTER_INDEX = 1, LENGTH_INDEX = 2, INDEX_LIMIT = 3;

        static final ClassValue<MethodHandle[]> TYPED_ACCESSORS = new ClassValue<MethodHandle[]>() {

            @Override
            protected MethodHandle[] computeValue(Class<?> type) {
                return new MethodHandle[INDEX_LIMIT];
            }
        };

        static final MethodHandle OBJECT_ARRAY_GETTER, OBJECT_ARRAY_SETTER, OBJECT_ARRAY_LENGTH;

        static {
            MethodHandle[] cache = TYPED_ACCESSORS.get(Object[].class);
            cache[GETTER_INDEX] = OBJECT_ARRAY_GETTER = makeIntrinsic(getAccessor(Object[].class, ArrayAccess.GET), Intrinsic.ARRAY_LOAD);
            cache[SETTER_INDEX] = OBJECT_ARRAY_SETTER = makeIntrinsic(getAccessor(Object[].class, ArrayAccess.SET), Intrinsic.ARRAY_STORE);
            cache[LENGTH_INDEX] = OBJECT_ARRAY_LENGTH = makeIntrinsic(getAccessor(Object[].class, ArrayAccess.LENGTH), Intrinsic.ARRAY_LENGTH);
            assert (InvokerBytecodeGenerator.isStaticallyInvocable(ArrayAccessor.OBJECT_ARRAY_GETTER.internalMemberName()));
            assert (InvokerBytecodeGenerator.isStaticallyInvocable(ArrayAccessor.OBJECT_ARRAY_SETTER.internalMemberName()));
            assert (InvokerBytecodeGenerator.isStaticallyInvocable(ArrayAccessor.OBJECT_ARRAY_LENGTH.internalMemberName()));
        }

        static int getElementI(int[] a, int i) {
            return a[i];
        }

        static long getElementJ(long[] a, int i) {
            return a[i];
        }

        static float getElementF(float[] a, int i) {
            return a[i];
        }

        static double getElementD(double[] a, int i) {
            return a[i];
        }

        static boolean getElementZ(boolean[] a, int i) {
            return a[i];
        }

        static byte getElementB(byte[] a, int i) {
            return a[i];
        }

        static short getElementS(short[] a, int i) {
            return a[i];
        }

        static char getElementC(char[] a, int i) {
            return a[i];
        }

        static Object getElementL(Object[] a, int i) {
            return a[i];
        }

        static void setElementI(int[] a, int i, int x) {
            a[i] = x;
        }

        static void setElementJ(long[] a, int i, long x) {
            a[i] = x;
        }

        static void setElementF(float[] a, int i, float x) {
            a[i] = x;
        }

        static void setElementD(double[] a, int i, double x) {
            a[i] = x;
        }

        static void setElementZ(boolean[] a, int i, boolean x) {
            a[i] = x;
        }

        static void setElementB(byte[] a, int i, byte x) {
            a[i] = x;
        }

        static void setElementS(short[] a, int i, short x) {
            a[i] = x;
        }

        static void setElementC(char[] a, int i, char x) {
            a[i] = x;
        }

        static void setElementL(Object[] a, int i, Object x) {
            a[i] = x;
        }

        static int lengthI(int[] a) {
            return a.length;
        }

        static int lengthJ(long[] a) {
            return a.length;
        }

        static int lengthF(float[] a) {
            return a.length;
        }

        static int lengthD(double[] a) {
            return a.length;
        }

        static int lengthZ(boolean[] a) {
            return a.length;
        }

        static int lengthB(byte[] a) {
            return a.length;
        }

        static int lengthS(short[] a) {
            return a.length;
        }

        static int lengthC(char[] a) {
            return a.length;
        }

        static int lengthL(Object[] a) {
            return a.length;
        }

        static String name(Class<?> arrayClass, ArrayAccess access) {
            Class<?> elemClass = arrayClass.getComponentType();
            if (elemClass == null)
                throw newIllegalArgumentException("not an array", arrayClass);
            return ArrayAccess.opName(access) + Wrapper.basicTypeChar(elemClass);
        }

        static MethodType type(Class<?> arrayClass, ArrayAccess access) {
            Class<?> elemClass = arrayClass.getComponentType();
            Class<?> arrayArgClass = arrayClass;
            if (!elemClass.isPrimitive()) {
                arrayArgClass = Object[].class;
                elemClass = Object.class;
            }
            switch(access) {
                case GET:
                    return MethodType.methodType(elemClass, arrayArgClass, int.class);
                case SET:
                    return MethodType.methodType(void.class, arrayArgClass, int.class, elemClass);
                case LENGTH:
                    return MethodType.methodType(int.class, arrayArgClass);
            }
            throw unmatchedArrayAccess(access);
        }

        static MethodType correctType(Class<?> arrayClass, ArrayAccess access) {
            Class<?> elemClass = arrayClass.getComponentType();
            switch(access) {
                case GET:
                    return MethodType.methodType(elemClass, arrayClass, int.class);
                case SET:
                    return MethodType.methodType(void.class, arrayClass, int.class, elemClass);
                case LENGTH:
                    return MethodType.methodType(int.class, arrayClass);
            }
            throw unmatchedArrayAccess(access);
        }

        static MethodHandle getAccessor(Class<?> arrayClass, ArrayAccess access) {
            String name = name(arrayClass, access);
            MethodType type = type(arrayClass, access);
            try {
                return IMPL_LOOKUP.findStatic(ArrayAccessor.class, name, type);
            } catch (ReflectiveOperationException ex) {
                throw uncaughtException(ex);
            }
        }
    }

    static MethodHandle makePairwiseConvert(MethodHandle target, MethodType srcType, boolean strict, boolean monobox) {
        MethodType dstType = target.type();
        if (srcType == dstType)
            return target;
        return makePairwiseConvertByEditor(target, srcType, strict, monobox);
    }

    private static int countNonNull(Object[] array) {
        int count = 0;
        for (Object x : array) {
            if (x != null)
                ++count;
        }
        return count;
    }

    static MethodHandle makePairwiseConvertByEditor(MethodHandle target, MethodType srcType, boolean strict, boolean monobox) {
        Object[] convSpecs = computeValueConversions(srcType, target.type(), strict, monobox);
        int convCount = countNonNull(convSpecs);
        if (convCount == 0)
            return target.viewAsType(srcType, strict);
        MethodType basicSrcType = srcType.basicType();
        MethodType midType = target.type().basicType();
        BoundMethodHandle mh = target.rebind();
        for (int i = 0; i < convSpecs.length - 1; i++) {
            Object convSpec = convSpecs[i];
            if (convSpec == null)
                continue;
            MethodHandle fn;
            if (convSpec instanceof Class) {
                fn = getConstantHandle(MH_cast).bindTo(convSpec);
            } else {
                fn = (MethodHandle) convSpec;
            }
            Class<?> newType = basicSrcType.parameterType(i);
            if (--convCount == 0)
                midType = srcType;
            else
                midType = midType.changeParameterType(i, newType);
            LambdaForm form2 = mh.editor().filterArgumentForm(1 + i, BasicType.basicType(newType));
            mh = mh.copyWithExtendL(midType, form2, fn);
            mh = mh.rebind();
        }
        Object convSpec = convSpecs[convSpecs.length - 1];
        if (convSpec != null) {
            MethodHandle fn;
            if (convSpec instanceof Class) {
                if (convSpec == void.class)
                    fn = null;
                else
                    fn = getConstantHandle(MH_cast).bindTo(convSpec);
            } else {
                fn = (MethodHandle) convSpec;
            }
            Class<?> newType = basicSrcType.returnType();
            assert (--convCount == 0);
            midType = srcType;
            if (fn != null) {
                mh = mh.rebind();
                LambdaForm form2 = mh.editor().filterReturnForm(BasicType.basicType(newType), false);
                mh = mh.copyWithExtendL(midType, form2, fn);
            } else {
                LambdaForm form2 = mh.editor().filterReturnForm(BasicType.basicType(newType), true);
                mh = mh.copyWith(midType, form2);
            }
        }
        assert (convCount == 0);
        assert (mh.type().equals(srcType));
        return mh;
    }

    static MethodHandle makePairwiseConvertIndirect(MethodHandle target, MethodType srcType, boolean strict, boolean monobox) {
        assert (target.type().parameterCount() == srcType.parameterCount());
        Object[] convSpecs = computeValueConversions(srcType, target.type(), strict, monobox);
        final int INARG_COUNT = srcType.parameterCount();
        int convCount = countNonNull(convSpecs);
        boolean retConv = (convSpecs[INARG_COUNT] != null);
        boolean retVoid = srcType.returnType() == void.class;
        if (retConv && retVoid) {
            convCount -= 1;
            retConv = false;
        }
        final int IN_MH = 0;
        final int INARG_BASE = 1;
        final int INARG_LIMIT = INARG_BASE + INARG_COUNT;
        final int NAME_LIMIT = INARG_LIMIT + convCount + 1;
        final int RETURN_CONV = (!retConv ? -1 : NAME_LIMIT - 1);
        final int OUT_CALL = (!retConv ? NAME_LIMIT : RETURN_CONV) - 1;
        final int RESULT = (retVoid ? -1 : NAME_LIMIT - 1);
        MethodType lambdaType = srcType.basicType().invokerType();
        Name[] names = arguments(NAME_LIMIT - INARG_LIMIT, lambdaType);
        final int OUTARG_BASE = 0;
        Object[] outArgs = new Object[OUTARG_BASE + INARG_COUNT];
        int nameCursor = INARG_LIMIT;
        for (int i = 0; i < INARG_COUNT; i++) {
            Object convSpec = convSpecs[i];
            if (convSpec == null) {
                outArgs[OUTARG_BASE + i] = names[INARG_BASE + i];
                continue;
            }
            Name conv;
            if (convSpec instanceof Class) {
                Class<?> convClass = (Class<?>) convSpec;
                conv = new Name(getConstantHandle(MH_cast), convClass, names[INARG_BASE + i]);
            } else {
                MethodHandle fn = (MethodHandle) convSpec;
                conv = new Name(fn, names[INARG_BASE + i]);
            }
            assert (names[nameCursor] == null);
            names[nameCursor++] = conv;
            assert (outArgs[OUTARG_BASE + i] == null);
            outArgs[OUTARG_BASE + i] = conv;
        }
        assert (nameCursor == OUT_CALL);
        names[OUT_CALL] = new Name(target, outArgs);
        Object convSpec = convSpecs[INARG_COUNT];
        if (!retConv) {
            assert (OUT_CALL == names.length - 1);
        } else {
            Name conv;
            if (convSpec == void.class) {
                conv = new Name(LambdaForm.constantZero(BasicType.basicType(srcType.returnType())));
            } else if (convSpec instanceof Class) {
                Class<?> convClass = (Class<?>) convSpec;
                conv = new Name(getConstantHandle(MH_cast), convClass, names[OUT_CALL]);
            } else {
                MethodHandle fn = (MethodHandle) convSpec;
                if (fn.type().parameterCount() == 0)
                    conv = new Name(fn);
                else
                    conv = new Name(fn, names[OUT_CALL]);
            }
            assert (names[RETURN_CONV] == null);
            names[RETURN_CONV] = conv;
            assert (RETURN_CONV == names.length - 1);
        }
        LambdaForm form = new LambdaForm(lambdaType.parameterCount(), names, RESULT, Kind.CONVERT);
        return SimpleMethodHandle.make(srcType, form);
    }

    static Object[] computeValueConversions(MethodType srcType, MethodType dstType, boolean strict, boolean monobox) {
        final int INARG_COUNT = srcType.parameterCount();
        Object[] convSpecs = new Object[INARG_COUNT + 1];
        for (int i = 0; i <= INARG_COUNT; i++) {
            boolean isRet = (i == INARG_COUNT);
            Class<?> src = isRet ? dstType.returnType() : srcType.parameterType(i);
            Class<?> dst = isRet ? srcType.returnType() : dstType.parameterType(i);
            if (!VerifyType.isNullConversion(src, dst, strict)) {
                convSpecs[i] = valueConversion(src, dst, strict, monobox);
            }
        }
        return convSpecs;
    }

    static MethodHandle makePairwiseConvert(MethodHandle target, MethodType srcType, boolean strict) {
        return makePairwiseConvert(target, srcType, strict, false);
    }

    static Object valueConversion(Class<?> src, Class<?> dst, boolean strict, boolean monobox) {
        assert (!VerifyType.isNullConversion(src, dst, strict));
        if (dst == void.class)
            return dst;
        MethodHandle fn;
        if (src.isPrimitive()) {
            if (src == void.class) {
                return void.class;
            } else if (dst.isPrimitive()) {
                fn = ValueConversions.convertPrimitive(src, dst);
            } else {
                Wrapper wsrc = Wrapper.forPrimitiveType(src);
                fn = ValueConversions.boxExact(wsrc);
                assert (fn.type().parameterType(0) == wsrc.primitiveType());
                assert (fn.type().returnType() == wsrc.wrapperType());
                if (!VerifyType.isNullConversion(wsrc.wrapperType(), dst, strict)) {
                    MethodType mt = MethodType.methodType(dst, src);
                    if (strict)
                        fn = fn.asType(mt);
                    else
                        fn = MethodHandleImpl.makePairwiseConvert(fn, mt, false);
                }
            }
        } else if (dst.isPrimitive()) {
            Wrapper wdst = Wrapper.forPrimitiveType(dst);
            if (monobox || src == wdst.wrapperType()) {
                fn = ValueConversions.unboxExact(wdst, strict);
            } else {
                fn = (strict ? ValueConversions.unboxWiden(wdst) : ValueConversions.unboxCast(wdst));
            }
        } else {
            return dst;
        }
        assert (fn.type().parameterCount() <= 1) : "pc" + Arrays.asList(src.getSimpleName(), dst.getSimpleName(), fn);
        return fn;
    }

    static MethodHandle makeVarargsCollector(MethodHandle target, Class<?> arrayType) {
        MethodType type = target.type();
        int last = type.parameterCount() - 1;
        if (type.parameterType(last) != arrayType)
            target = target.asType(type.changeParameterType(last, arrayType));
        target = target.asFixedArity();
        return new AsVarargsCollector(target, arrayType);
    }

    private static final class AsVarargsCollector extends DelegatingMethodHandle {

        private final MethodHandle target;

        private final Class<?> arrayType;

        @Stable
        private MethodHandle asCollectorCache;

        AsVarargsCollector(MethodHandle target, Class<?> arrayType) {
            this(target.type(), target, arrayType);
        }

        AsVarargsCollector(MethodType type, MethodHandle target, Class<?> arrayType) {
            super(type, target);
            this.target = target;
            this.arrayType = arrayType;
            this.asCollectorCache = target.asCollector(arrayType, 0);
        }

        @Override
        public boolean isVarargsCollector() {
            return true;
        }

        @Override
        protected MethodHandle getTarget() {
            return target;
        }

        @Override
        public MethodHandle asFixedArity() {
            return target;
        }

        @Override
        MethodHandle setVarargs(MemberName member) {
            if (member.isVarargs())
                return this;
            return asFixedArity();
        }

        @Override
        public MethodHandle withVarargs(boolean makeVarargs) {
            if (makeVarargs)
                return this;
            return asFixedArity();
        }

        @Override
        public MethodHandle asTypeUncached(MethodType newType) {
            MethodType type = this.type();
            int collectArg = type.parameterCount() - 1;
            int newArity = newType.parameterCount();
            if (newArity == collectArg + 1 && type.parameterType(collectArg).isAssignableFrom(newType.parameterType(collectArg))) {
                return asTypeCache = asFixedArity().asType(newType);
            }
            MethodHandle acc = asCollectorCache;
            if (acc != null && acc.type().parameterCount() == newArity)
                return asTypeCache = acc.asType(newType);
            int arrayLength = newArity - collectArg;
            MethodHandle collector;
            try {
                collector = asFixedArity().asCollector(arrayType, arrayLength);
                assert (collector.type().parameterCount() == newArity) : "newArity=" + newArity + " but collector=" + collector;
            } catch (IllegalArgumentException ex) {
                throw new WrongMethodTypeException("cannot build collector", ex);
            }
            asCollectorCache = collector;
            return asTypeCache = collector.asType(newType);
        }

        @Override
        boolean viewAsTypeChecks(MethodType newType, boolean strict) {
            super.viewAsTypeChecks(newType, true);
            if (strict)
                return true;
            assert (type().lastParameterType().getComponentType().isAssignableFrom(newType.lastParameterType().getComponentType())) : Arrays.asList(this, newType);
            return true;
        }

        @Override
        public Object invokeWithArguments(Object... arguments) throws Throwable {
            MethodType type = this.type();
            int argc;
            final int MAX_SAFE = 127;
            if (arguments == null || (argc = arguments.length) <= MAX_SAFE || argc < type.parameterCount()) {
                return super.invokeWithArguments(arguments);
            }
            int uncollected = type.parameterCount() - 1;
            Class<?> elemType = arrayType.getComponentType();
            int collected = argc - uncollected;
            Object collArgs = (elemType == Object.class) ? new Object[collected] : Array.newInstance(elemType, collected);
            if (!elemType.isPrimitive()) {
                try {
                    System.arraycopy(arguments, uncollected, collArgs, 0, collected);
                } catch (ArrayStoreException ex) {
                    return super.invokeWithArguments(arguments);
                }
            } else {
                MethodHandle arraySetter = MethodHandles.arrayElementSetter(arrayType);
                try {
                    for (int i = 0; i < collected; i++) {
                        arraySetter.invoke(collArgs, i, arguments[uncollected + i]);
                    }
                } catch (WrongMethodTypeException | ClassCastException ex) {
                    return super.invokeWithArguments(arguments);
                }
            }
            Object[] newArgs = new Object[uncollected + 1];
            System.arraycopy(arguments, 0, newArgs, 0, uncollected);
            newArgs[uncollected] = collArgs;
            return asFixedArity().invokeWithArguments(newArgs);
        }
    }

    static MethodHandle makeSpreadArguments(MethodHandle target, Class<?> spreadArgType, int spreadArgPos, int spreadArgCount) {
        MethodType targetType = target.type();
        for (int i = 0; i < spreadArgCount; i++) {
            Class<?> arg = VerifyType.spreadArgElementType(spreadArgType, i);
            if (arg == null)
                arg = Object.class;
            targetType = targetType.changeParameterType(spreadArgPos + i, arg);
        }
        target = target.asType(targetType);
        MethodType srcType = targetType.replaceParameterTypes(spreadArgPos, spreadArgPos + spreadArgCount, spreadArgType);
        MethodType lambdaType = srcType.invokerType();
        Name[] names = arguments(spreadArgCount + 2, lambdaType);
        int nameCursor = lambdaType.parameterCount();
        int[] indexes = new int[targetType.parameterCount()];
        for (int i = 0, argIndex = 1; i < targetType.parameterCount() + 1; i++, argIndex++) {
            Class<?> src = lambdaType.parameterType(i);
            if (i == spreadArgPos) {
                MethodHandle aload = MethodHandles.arrayElementGetter(spreadArgType);
                Name array = names[argIndex];
                names[nameCursor++] = new Name(getFunction(NF_checkSpreadArgument), array, spreadArgCount);
                for (int j = 0; j < spreadArgCount; i++, j++) {
                    indexes[i] = nameCursor;
                    names[nameCursor++] = new Name(new NamedFunction(aload, Intrinsic.ARRAY_LOAD), array, j);
                }
            } else if (i < indexes.length) {
                indexes[i] = argIndex;
            }
        }
        assert (nameCursor == names.length - 1);
        Name[] targetArgs = new Name[targetType.parameterCount()];
        for (int i = 0; i < targetType.parameterCount(); i++) {
            int idx = indexes[i];
            targetArgs[i] = names[idx];
        }
        names[names.length - 1] = new Name(target, (Object[]) targetArgs);
        LambdaForm form = new LambdaForm(lambdaType.parameterCount(), names, Kind.SPREAD);
        return SimpleMethodHandle.make(srcType, form);
    }

    static void checkSpreadArgument(Object av, int n) {
        if (av == null && n == 0) {
            return;
        } else if (av == null) {
            throw new NullPointerException("null array reference");
        } else if (av instanceof Object[]) {
            int len = ((Object[]) av).length;
            if (len == n)
                return;
        } else {
            int len = java.lang.reflect.Array.getLength(av);
            if (len == n)
                return;
        }
        throw newIllegalArgumentException("array is not of length " + n);
    }

    static MethodHandle makeCollectArguments(MethodHandle target, MethodHandle collector, int collectArgPos, boolean retainOriginalArgs) {
        MethodType targetType = target.type();
        MethodType collectorType = collector.type();
        int collectArgCount = collectorType.parameterCount();
        Class<?> collectValType = collectorType.returnType();
        int collectValCount = (collectValType == void.class ? 0 : 1);
        MethodType srcType = targetType.dropParameterTypes(collectArgPos, collectArgPos + collectValCount);
        if (!retainOriginalArgs) {
            srcType = srcType.insertParameterTypes(collectArgPos, collectorType.parameterArray());
        }
        MethodType lambdaType = srcType.invokerType();
        Name[] names = arguments(2, lambdaType);
        final int collectNamePos = names.length - 2;
        final int targetNamePos = names.length - 1;
        Name[] collectorArgs = Arrays.copyOfRange(names, 1 + collectArgPos, 1 + collectArgPos + collectArgCount);
        names[collectNamePos] = new Name(collector, (Object[]) collectorArgs);
        Name[] targetArgs = new Name[targetType.parameterCount()];
        int inputArgPos = 1;
        int targetArgPos = 0;
        int chunk = collectArgPos;
        System.arraycopy(names, inputArgPos, targetArgs, targetArgPos, chunk);
        inputArgPos += chunk;
        targetArgPos += chunk;
        if (collectValType != void.class) {
            targetArgs[targetArgPos++] = names[collectNamePos];
        }
        chunk = collectArgCount;
        if (retainOriginalArgs) {
            System.arraycopy(names, inputArgPos, targetArgs, targetArgPos, chunk);
            targetArgPos += chunk;
        }
        inputArgPos += chunk;
        chunk = targetArgs.length - targetArgPos;
        System.arraycopy(names, inputArgPos, targetArgs, targetArgPos, chunk);
        assert (inputArgPos + chunk == collectNamePos);
        names[targetNamePos] = new Name(target, (Object[]) targetArgs);
        LambdaForm form = new LambdaForm(lambdaType.parameterCount(), names, Kind.COLLECT);
        return SimpleMethodHandle.make(srcType, form);
    }

    @LambdaForm.Hidden
    static MethodHandle selectAlternative(boolean testResult, MethodHandle target, MethodHandle fallback) {
        if (testResult) {
            return target;
        } else {
            return fallback;
        }
    }

    @LambdaForm.Hidden
    @jdk.internal.HotSpotIntrinsicCandidate
    static boolean profileBoolean(boolean result, int[] counters) {
        int idx = result ? 1 : 0;
        try {
            counters[idx] = Math.addExact(counters[idx], 1);
        } catch (ArithmeticException e) {
            counters[idx] = counters[idx] / 2;
        }
        return result;
    }

    @LambdaForm.Hidden
    @jdk.internal.HotSpotIntrinsicCandidate
    static boolean isCompileConstant(Object obj) {
        return false;
    }

    static MethodHandle makeGuardWithTest(MethodHandle test, MethodHandle target, MethodHandle fallback) {
        MethodType type = target.type();
        assert (test.type().equals(type.changeReturnType(boolean.class)) && fallback.type().equals(type));
        MethodType basicType = type.basicType();
        LambdaForm form = makeGuardWithTestForm(basicType);
        BoundMethodHandle mh;
        try {
            if (PROFILE_GWT) {
                int[] counts = new int[2];
                mh = (BoundMethodHandle) BoundMethodHandle.speciesData_LLLL().factory().invokeBasic(type, form, (Object) test, (Object) profile(target), (Object) profile(fallback), counts);
            } else {
                mh = (BoundMethodHandle) BoundMethodHandle.speciesData_LLL().factory().invokeBasic(type, form, (Object) test, (Object) profile(target), (Object) profile(fallback));
            }
        } catch (Throwable ex) {
            throw uncaughtException(ex);
        }
        assert (mh.type() == type);
        return mh;
    }

    static MethodHandle profile(MethodHandle target) {
        if (DONT_INLINE_THRESHOLD >= 0) {
            return makeBlockInliningWrapper(target);
        } else {
            return target;
        }
    }

    static MethodHandle makeBlockInliningWrapper(MethodHandle target) {
        LambdaForm lform;
        if (DONT_INLINE_THRESHOLD > 0) {
            lform = Makers.PRODUCE_BLOCK_INLINING_FORM.apply(target);
        } else {
            lform = Makers.PRODUCE_REINVOKER_FORM.apply(target);
        }
        return new CountingWrapper(target, lform, Makers.PRODUCE_BLOCK_INLINING_FORM, Makers.PRODUCE_REINVOKER_FORM, DONT_INLINE_THRESHOLD);
    }

    private final static class Makers {

        static final Function<MethodHandle, LambdaForm> PRODUCE_BLOCK_INLINING_FORM = new Function<MethodHandle, LambdaForm>() {

            @Override
            public LambdaForm apply(MethodHandle target) {
                return DelegatingMethodHandle.makeReinvokerForm(target, MethodTypeForm.LF_DELEGATE_BLOCK_INLINING, CountingWrapper.class, false, DelegatingMethodHandle.NF_getTarget, CountingWrapper.NF_maybeStopCounting);
            }
        };

        static final Function<MethodHandle, LambdaForm> PRODUCE_REINVOKER_FORM = new Function<MethodHandle, LambdaForm>() {

            @Override
            public LambdaForm apply(MethodHandle target) {
                return DelegatingMethodHandle.makeReinvokerForm(target, MethodTypeForm.LF_DELEGATE, DelegatingMethodHandle.class, DelegatingMethodHandle.NF_getTarget);
            }
        };

        static final ClassValue<MethodHandle[]> TYPED_COLLECTORS = new ClassValue<MethodHandle[]>() {

            @Override
            protected MethodHandle[] computeValue(Class<?> type) {
                return new MethodHandle[MAX_JVM_ARITY + 1];
            }
        };
    }

    static class CountingWrapper extends DelegatingMethodHandle {

        private final MethodHandle target;

        private int count;

        private Function<MethodHandle, LambdaForm> countingFormProducer;

        private Function<MethodHandle, LambdaForm> nonCountingFormProducer;

        private volatile boolean isCounting;

        private CountingWrapper(MethodHandle target, LambdaForm lform, Function<MethodHandle, LambdaForm> countingFromProducer, Function<MethodHandle, LambdaForm> nonCountingFormProducer, int count) {
            super(target.type(), lform);
            this.target = target;
            this.count = count;
            this.countingFormProducer = countingFromProducer;
            this.nonCountingFormProducer = nonCountingFormProducer;
            this.isCounting = (count > 0);
        }

        @Hidden
        @Override
        protected MethodHandle getTarget() {
            return target;
        }

        @Override
        public MethodHandle asTypeUncached(MethodType newType) {
            MethodHandle newTarget = target.asType(newType);
            MethodHandle wrapper;
            if (isCounting) {
                LambdaForm lform;
                lform = countingFormProducer.apply(newTarget);
                wrapper = new CountingWrapper(newTarget, lform, countingFormProducer, nonCountingFormProducer, DONT_INLINE_THRESHOLD);
            } else {
                wrapper = newTarget;
            }
            return (asTypeCache = wrapper);
        }

        private int invocations = CUSTOMIZE_THRESHOLD;

        private void maybeCustomizeTarget() {
            int c = invocations;
            if (c >= 0) {
                if (c == 1) {
                    target.customize();
                }
                invocations = c - 1;
            }
        }

        boolean countDown() {
            int c = count;
            maybeCustomizeTarget();
            if (c <= 1) {
                if (isCounting) {
                    isCounting = false;
                    return true;
                } else {
                    return false;
                }
            } else {
                count = c - 1;
                return false;
            }
        }

        @Hidden
        static void maybeStopCounting(Object o1) {
            CountingWrapper wrapper = (CountingWrapper) o1;
            if (wrapper.countDown()) {
                LambdaForm lform = wrapper.nonCountingFormProducer.apply(wrapper.target);
                lform.compileToBytecode();
                wrapper.updateForm(lform);
            }
        }

        static final NamedFunction NF_maybeStopCounting;

        static {
            Class<?> THIS_CLASS = CountingWrapper.class;
            try {
                NF_maybeStopCounting = new NamedFunction(THIS_CLASS.getDeclaredMethod("maybeStopCounting", Object.class));
            } catch (ReflectiveOperationException ex) {
                throw newInternalError(ex);
            }
        }
    }

    static LambdaForm makeGuardWithTestForm(MethodType basicType) {
        LambdaForm lform = basicType.form().cachedLambdaForm(MethodTypeForm.LF_GWT);
        if (lform != null)
            return lform;
        final int THIS_MH = 0;
        final int ARG_BASE = 1;
        final int ARG_LIMIT = ARG_BASE + basicType.parameterCount();
        int nameCursor = ARG_LIMIT;
        final int GET_TEST = nameCursor++;
        final int GET_TARGET = nameCursor++;
        final int GET_FALLBACK = nameCursor++;
        final int GET_COUNTERS = PROFILE_GWT ? nameCursor++ : -1;
        final int CALL_TEST = nameCursor++;
        final int PROFILE = (GET_COUNTERS != -1) ? nameCursor++ : -1;
        final int TEST = nameCursor - 1;
        final int SELECT_ALT = nameCursor++;
        final int CALL_TARGET = nameCursor++;
        assert (CALL_TARGET == SELECT_ALT + 1);
        MethodType lambdaType = basicType.invokerType();
        Name[] names = arguments(nameCursor - ARG_LIMIT, lambdaType);
        BoundMethodHandle.SpeciesData data = (GET_COUNTERS != -1) ? BoundMethodHandle.speciesData_LLLL() : BoundMethodHandle.speciesData_LLL();
        names[THIS_MH] = names[THIS_MH].withConstraint(data);
        names[GET_TEST] = new Name(data.getterFunction(0), names[THIS_MH]);
        names[GET_TARGET] = new Name(data.getterFunction(1), names[THIS_MH]);
        names[GET_FALLBACK] = new Name(data.getterFunction(2), names[THIS_MH]);
        if (GET_COUNTERS != -1) {
            names[GET_COUNTERS] = new Name(data.getterFunction(3), names[THIS_MH]);
        }
        Object[] invokeArgs = Arrays.copyOfRange(names, 0, ARG_LIMIT, Object[].class);
        MethodType testType = basicType.changeReturnType(boolean.class).basicType();
        invokeArgs[0] = names[GET_TEST];
        names[CALL_TEST] = new Name(testType, invokeArgs);
        if (PROFILE != -1) {
            names[PROFILE] = new Name(getFunction(NF_profileBoolean), names[CALL_TEST], names[GET_COUNTERS]);
        }
        names[SELECT_ALT] = new Name(new NamedFunction(getConstantHandle(MH_selectAlternative), Intrinsic.SELECT_ALTERNATIVE), names[TEST], names[GET_TARGET], names[GET_FALLBACK]);
        invokeArgs[0] = names[SELECT_ALT];
        names[CALL_TARGET] = new Name(basicType, invokeArgs);
        lform = new LambdaForm(lambdaType.parameterCount(), names, true, Kind.GUARD);
        return basicType.form().setCachedLambdaForm(MethodTypeForm.LF_GWT, lform);
    }

    private static LambdaForm makeGuardWithCatchForm(MethodType basicType) {
        MethodType lambdaType = basicType.invokerType();
        LambdaForm lform = basicType.form().cachedLambdaForm(MethodTypeForm.LF_GWC);
        if (lform != null) {
            return lform;
        }
        final int THIS_MH = 0;
        final int ARG_BASE = 1;
        final int ARG_LIMIT = ARG_BASE + basicType.parameterCount();
        int nameCursor = ARG_LIMIT;
        final int GET_TARGET = nameCursor++;
        final int GET_CLASS = nameCursor++;
        final int GET_CATCHER = nameCursor++;
        final int GET_COLLECT_ARGS = nameCursor++;
        final int GET_UNBOX_RESULT = nameCursor++;
        final int BOXED_ARGS = nameCursor++;
        final int TRY_CATCH = nameCursor++;
        final int UNBOX_RESULT = nameCursor++;
        Name[] names = arguments(nameCursor - ARG_LIMIT, lambdaType);
        BoundMethodHandle.SpeciesData data = BoundMethodHandle.speciesData_LLLLL();
        names[THIS_MH] = names[THIS_MH].withConstraint(data);
        names[GET_TARGET] = new Name(data.getterFunction(0), names[THIS_MH]);
        names[GET_CLASS] = new Name(data.getterFunction(1), names[THIS_MH]);
        names[GET_CATCHER] = new Name(data.getterFunction(2), names[THIS_MH]);
        names[GET_COLLECT_ARGS] = new Name(data.getterFunction(3), names[THIS_MH]);
        names[GET_UNBOX_RESULT] = new Name(data.getterFunction(4), names[THIS_MH]);
        MethodType collectArgsType = basicType.changeReturnType(Object.class);
        MethodHandle invokeBasic = MethodHandles.basicInvoker(collectArgsType);
        Object[] args = new Object[invokeBasic.type().parameterCount()];
        args[0] = names[GET_COLLECT_ARGS];
        System.arraycopy(names, ARG_BASE, args, 1, ARG_LIMIT - ARG_BASE);
        names[BOXED_ARGS] = new Name(new NamedFunction(invokeBasic, Intrinsic.GUARD_WITH_CATCH), args);
        Object[] gwcArgs = new Object[] { names[GET_TARGET], names[GET_CLASS], names[GET_CATCHER], names[BOXED_ARGS] };
        names[TRY_CATCH] = new Name(getFunction(NF_guardWithCatch), gwcArgs);
        MethodHandle invokeBasicUnbox = MethodHandles.basicInvoker(MethodType.methodType(basicType.rtype(), Object.class));
        Object[] unboxArgs = new Object[] { names[GET_UNBOX_RESULT], names[TRY_CATCH] };
        names[UNBOX_RESULT] = new Name(invokeBasicUnbox, unboxArgs);
        lform = new LambdaForm(lambdaType.parameterCount(), names, Kind.GUARD_WITH_CATCH);
        return basicType.form().setCachedLambdaForm(MethodTypeForm.LF_GWC, lform);
    }

    static MethodHandle makeGuardWithCatch(MethodHandle target, Class<? extends Throwable> exType, MethodHandle catcher) {
        MethodType type = target.type();
        LambdaForm form = makeGuardWithCatchForm(type.basicType());
        MethodType varargsType = type.changeReturnType(Object[].class);
        MethodHandle collectArgs = varargsArray(type.parameterCount()).asType(varargsType);
        MethodHandle unboxResult = unboxResultHandle(type.returnType());
        BoundMethodHandle.SpeciesData data = BoundMethodHandle.speciesData_LLLLL();
        BoundMethodHandle mh;
        try {
            mh = (BoundMethodHandle) data.factory().invokeBasic(type, form, (Object) target, (Object) exType, (Object) catcher, (Object) collectArgs, (Object) unboxResult);
        } catch (Throwable ex) {
            throw uncaughtException(ex);
        }
        assert (mh.type() == type);
        return mh;
    }

    @LambdaForm.Hidden
    static Object guardWithCatch(MethodHandle target, Class<? extends Throwable> exType, MethodHandle catcher, Object... av) throws Throwable {
        try {
            return target.asFixedArity().invokeWithArguments(av);
        } catch (Throwable t) {
            if (!exType.isInstance(t))
                throw t;
            return catcher.asFixedArity().invokeWithArguments(prepend(av, t));
        }
    }

    @LambdaForm.Hidden
    private static Object[] prepend(Object[] array, Object... elems) {
        int nArray = array.length;
        int nElems = elems.length;
        Object[] newArray = new Object[nArray + nElems];
        System.arraycopy(elems, 0, newArray, 0, nElems);
        System.arraycopy(array, 0, newArray, nElems, nArray);
        return newArray;
    }

    static MethodHandle throwException(MethodType type) {
        assert (Throwable.class.isAssignableFrom(type.parameterType(0)));
        int arity = type.parameterCount();
        if (arity > 1) {
            MethodHandle mh = throwException(type.dropParameterTypes(1, arity));
            mh = MethodHandles.dropArguments(mh, 1, Arrays.copyOfRange(type.parameterArray(), 1, arity));
            return mh;
        }
        return makePairwiseConvert(getFunction(NF_throwException).resolvedHandle(), type, false, true);
    }

    static <T extends Throwable> Empty throwException(T t) throws T {
        throw t;
    }

    static MethodHandle[] FAKE_METHOD_HANDLE_INVOKE = new MethodHandle[2];

    static MethodHandle fakeMethodHandleInvoke(MemberName method) {
        int idx;
        assert (method.isMethodHandleInvoke());
        switch(method.getName()) {
            case "invoke":
                idx = 0;
                break;
            case "invokeExact":
                idx = 1;
                break;
            default:
                throw new InternalError(method.getName());
        }
        MethodHandle mh = FAKE_METHOD_HANDLE_INVOKE[idx];
        if (mh != null)
            return mh;
        MethodType type = MethodType.methodType(Object.class, UnsupportedOperationException.class, MethodHandle.class, Object[].class);
        mh = throwException(type);
        mh = mh.bindTo(new UnsupportedOperationException("cannot reflectively invoke MethodHandle"));
        if (!method.getInvocationType().equals(mh.type()))
            throw new InternalError(method.toString());
        mh = mh.withInternalMemberName(method, false);
        mh = mh.withVarargs(true);
        assert (method.isVarargs());
        FAKE_METHOD_HANDLE_INVOKE[idx] = mh;
        return mh;
    }

    static MethodHandle fakeVarHandleInvoke(MemberName method) {
        MethodType type = MethodType.methodType(method.getReturnType(), UnsupportedOperationException.class, VarHandle.class, Object[].class);
        MethodHandle mh = throwException(type);
        mh = mh.bindTo(new UnsupportedOperationException("cannot reflectively invoke VarHandle"));
        if (!method.getInvocationType().equals(mh.type()))
            throw new InternalError(method.toString());
        mh = mh.withInternalMemberName(method, false);
        mh = mh.asVarargsCollector(Object[].class);
        assert (method.isVarargs());
        return mh;
    }

    static MethodHandle bindCaller(MethodHandle mh, Class<?> hostClass) {
        return BindCaller.bindCaller(mh, hostClass);
    }

    private static class BindCaller {

        private static MethodType INVOKER_MT = MethodType.methodType(Object.class, MethodHandle.class, Object[].class);

        static MethodHandle bindCaller(MethodHandle mh, Class<?> hostClass) {
            if (hostClass == null || (hostClass.isArray() || hostClass.isPrimitive() || hostClass.getName().startsWith("java.lang.invoke."))) {
                throw new InternalError();
            }
            MethodHandle vamh = prepareForInvoker(mh);
            MethodHandle bccInvoker = CV_makeInjectedInvoker.get(hostClass);
            return restoreToType(bccInvoker.bindTo(vamh), mh, hostClass);
        }

        private static MethodHandle makeInjectedInvoker(Class<?> hostClass) {
            try {
                Class<?> invokerClass = UNSAFE.defineAnonymousClass(hostClass, INJECTED_INVOKER_TEMPLATE, null);
                assert checkInjectedInvoker(hostClass, invokerClass);
                return IMPL_LOOKUP.findStatic(invokerClass, "invoke_V", INVOKER_MT);
            } catch (ReflectiveOperationException ex) {
                throw uncaughtException(ex);
            }
        }

        private static ClassValue<MethodHandle> CV_makeInjectedInvoker = new ClassValue<MethodHandle>() {

            @Override
            protected MethodHandle computeValue(Class<?> hostClass) {
                return makeInjectedInvoker(hostClass);
            }
        };

        private static MethodHandle prepareForInvoker(MethodHandle mh) {
            mh = mh.asFixedArity();
            MethodType mt = mh.type();
            int arity = mt.parameterCount();
            MethodHandle vamh = mh.asType(mt.generic());
            vamh.internalForm().compileToBytecode();
            vamh = vamh.asSpreader(Object[].class, arity);
            vamh.internalForm().compileToBytecode();
            return vamh;
        }

        private static MethodHandle restoreToType(MethodHandle vamh, MethodHandle original, Class<?> hostClass) {
            MethodType type = original.type();
            MethodHandle mh = vamh.asCollector(Object[].class, type.parameterCount());
            MemberName member = original.internalMemberName();
            mh = mh.asType(type);
            mh = new WrappedMember(mh, type, member, original.isInvokeSpecial(), hostClass);
            return mh;
        }

        private static boolean checkInjectedInvoker(Class<?> hostClass, Class<?> invokerClass) {
            assert (hostClass.getClassLoader() == invokerClass.getClassLoader()) : hostClass.getName() + " (CL)";
            try {
                assert (hostClass.getProtectionDomain() == invokerClass.getProtectionDomain()) : hostClass.getName() + " (PD)";
            } catch (SecurityException ex) {
            }
            try {
                MethodHandle invoker = IMPL_LOOKUP.findStatic(invokerClass, "invoke_V", INVOKER_MT);
                MethodHandle vamh = prepareForInvoker(MH_checkCallerClass);
                return (boolean) invoker.invoke(vamh, new Object[] { invokerClass });
            } catch (Throwable ex) {
                throw new InternalError(ex);
            }
        }

        private static final MethodHandle MH_checkCallerClass;

        static {
            final Class<?> THIS_CLASS = BindCaller.class;
            assert (checkCallerClass(THIS_CLASS));
            try {
                MH_checkCallerClass = IMPL_LOOKUP.findStatic(THIS_CLASS, "checkCallerClass", MethodType.methodType(boolean.class, Class.class));
                assert ((boolean) MH_checkCallerClass.invokeExact(THIS_CLASS));
            } catch (Throwable ex) {
                throw new InternalError(ex);
            }
        }

        @CallerSensitive
        @ForceInline
        private static boolean checkCallerClass(Class<?> expected) {
            Class<?> actual = Reflection.getCallerClass();
            if (actual != expected)
                throw new InternalError("found " + actual.getName() + ", expected " + expected.getName());
            return true;
        }

        private static final byte[] INJECTED_INVOKER_TEMPLATE = generateInvokerTemplate();

        private static byte[] generateInvokerTemplate() {
            ClassWriter cw = new ClassWriter(0);
            cw.visit(52, ACC_PRIVATE | ACC_SUPER, "InjectedInvoker", null, "java/lang/Object", null);
            MethodVisitor mv = cw.visitMethod(ACC_STATIC, "invoke_V", "(Ljava/lang/invoke/MethodHandle;[Ljava/lang/Object;)Ljava/lang/Object;", null, null);
            AnnotationVisitor av0 = mv.visitAnnotation("Ljava/lang/invoke/LambdaForm$Hidden;", true);
            av0.visitEnd();
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "invokeExact", "([Ljava/lang/Object;)Ljava/lang/Object;", false);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
            cw.visitEnd();
            return cw.toByteArray();
        }
    }

    private static final class WrappedMember extends DelegatingMethodHandle {

        private final MethodHandle target;

        private final MemberName member;

        private final Class<?> callerClass;

        private final boolean isInvokeSpecial;

        private WrappedMember(MethodHandle target, MethodType type, MemberName member, boolean isInvokeSpecial, Class<?> callerClass) {
            super(type, target);
            this.target = target;
            this.member = member;
            this.callerClass = callerClass;
            this.isInvokeSpecial = isInvokeSpecial;
        }

        @Override
        MemberName internalMemberName() {
            return member;
        }

        @Override
        Class<?> internalCallerClass() {
            return callerClass;
        }

        @Override
        boolean isInvokeSpecial() {
            return isInvokeSpecial;
        }

        @Override
        protected MethodHandle getTarget() {
            return target;
        }

        @Override
        public MethodHandle asTypeUncached(MethodType newType) {
            return asTypeCache = target.asType(newType);
        }
    }

    static MethodHandle makeWrappedMember(MethodHandle target, MemberName member, boolean isInvokeSpecial) {
        if (member.equals(target.internalMemberName()) && isInvokeSpecial == target.isInvokeSpecial())
            return target;
        return new WrappedMember(target, target.type(), member, isInvokeSpecial, null);
    }

    enum Intrinsic {

        SELECT_ALTERNATIVE,
        GUARD_WITH_CATCH,
        TRY_FINALLY,
        LOOP,
        NEW_ARRAY,
        ARRAY_LOAD,
        ARRAY_STORE,
        ARRAY_LENGTH,
        IDENTITY,
        ZERO,
        NONE
    }

    static final class IntrinsicMethodHandle extends DelegatingMethodHandle {

        private final MethodHandle target;

        private final Intrinsic intrinsicName;

        IntrinsicMethodHandle(MethodHandle target, Intrinsic intrinsicName) {
            super(target.type(), target);
            this.target = target;
            this.intrinsicName = intrinsicName;
        }

        @Override
        protected MethodHandle getTarget() {
            return target;
        }

        @Override
        Intrinsic intrinsicName() {
            return intrinsicName;
        }

        @Override
        public MethodHandle asTypeUncached(MethodType newType) {
            return asTypeCache = target.asType(newType);
        }

        @Override
        String internalProperties() {
            return super.internalProperties() + "\n& Intrinsic=" + intrinsicName;
        }

        @Override
        public MethodHandle asCollector(Class<?> arrayType, int arrayLength) {
            if (intrinsicName == Intrinsic.IDENTITY) {
                MethodType resultType = type().asCollectorType(arrayType, type().parameterCount() - 1, arrayLength);
                MethodHandle newArray = MethodHandleImpl.varargsArray(arrayType, arrayLength);
                return newArray.asType(resultType);
            }
            return super.asCollector(arrayType, arrayLength);
        }
    }

    static MethodHandle makeIntrinsic(MethodHandle target, Intrinsic intrinsicName) {
        if (intrinsicName == target.intrinsicName())
            return target;
        return new IntrinsicMethodHandle(target, intrinsicName);
    }

    static MethodHandle makeIntrinsic(MethodType type, LambdaForm form, Intrinsic intrinsicName) {
        return new IntrinsicMethodHandle(SimpleMethodHandle.make(type, form), intrinsicName);
    }

    private static MethodHandle findCollector(String name, int nargs, Class<?> rtype, Class<?>... ptypes) {
        MethodType type = MethodType.genericMethodType(nargs).changeReturnType(rtype).insertParameterTypes(0, ptypes);
        try {
            return IMPL_LOOKUP.findStatic(MethodHandleImpl.class, name, type);
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }

    private static final Object[] NO_ARGS_ARRAY = {};

    private static Object[] makeArray(Object... args) {
        return args;
    }

    private static Object[] array() {
        return NO_ARGS_ARRAY;
    }

    private static Object[] array(Object a0) {
        return makeArray(a0);
    }

    private static Object[] array(Object a0, Object a1) {
        return makeArray(a0, a1);
    }

    private static Object[] array(Object a0, Object a1, Object a2) {
        return makeArray(a0, a1, a2);
    }

    private static Object[] array(Object a0, Object a1, Object a2, Object a3) {
        return makeArray(a0, a1, a2, a3);
    }

    private static Object[] array(Object a0, Object a1, Object a2, Object a3, Object a4) {
        return makeArray(a0, a1, a2, a3, a4);
    }

    private static Object[] array(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5) {
        return makeArray(a0, a1, a2, a3, a4, a5);
    }

    private static Object[] array(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6) {
        return makeArray(a0, a1, a2, a3, a4, a5, a6);
    }

    private static Object[] array(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7) {
        return makeArray(a0, a1, a2, a3, a4, a5, a6, a7);
    }

    private static Object[] array(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8) {
        return makeArray(a0, a1, a2, a3, a4, a5, a6, a7, a8);
    }

    private static Object[] array(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9) {
        return makeArray(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9);
    }

    private static final int ARRAYS_COUNT = 11;

    @Stable
    private static final MethodHandle[] ARRAYS = new MethodHandle[MAX_ARITY + 1];

    private static Object[] fillNewArray(Integer len, Object[] args) {
        Object[] a = new Object[len];
        fillWithArguments(a, 0, args);
        return a;
    }

    private static Object[] fillNewTypedArray(Object[] example, Integer len, Object[] args) {
        Object[] a = Arrays.copyOf(example, len);
        assert (a.getClass() != Object[].class);
        fillWithArguments(a, 0, args);
        return a;
    }

    private static void fillWithArguments(Object[] a, int pos, Object... args) {
        System.arraycopy(args, 0, a, pos, args.length);
    }

    private static Object[] fillArray(Integer pos, Object[] a, Object a0) {
        fillWithArguments(a, pos, a0);
        return a;
    }

    private static Object[] fillArray(Integer pos, Object[] a, Object a0, Object a1) {
        fillWithArguments(a, pos, a0, a1);
        return a;
    }

    private static Object[] fillArray(Integer pos, Object[] a, Object a0, Object a1, Object a2) {
        fillWithArguments(a, pos, a0, a1, a2);
        return a;
    }

    private static Object[] fillArray(Integer pos, Object[] a, Object a0, Object a1, Object a2, Object a3) {
        fillWithArguments(a, pos, a0, a1, a2, a3);
        return a;
    }

    private static Object[] fillArray(Integer pos, Object[] a, Object a0, Object a1, Object a2, Object a3, Object a4) {
        fillWithArguments(a, pos, a0, a1, a2, a3, a4);
        return a;
    }

    private static Object[] fillArray(Integer pos, Object[] a, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5) {
        fillWithArguments(a, pos, a0, a1, a2, a3, a4, a5);
        return a;
    }

    private static Object[] fillArray(Integer pos, Object[] a, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6) {
        fillWithArguments(a, pos, a0, a1, a2, a3, a4, a5, a6);
        return a;
    }

    private static Object[] fillArray(Integer pos, Object[] a, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7) {
        fillWithArguments(a, pos, a0, a1, a2, a3, a4, a5, a6, a7);
        return a;
    }

    private static Object[] fillArray(Integer pos, Object[] a, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8) {
        fillWithArguments(a, pos, a0, a1, a2, a3, a4, a5, a6, a7, a8);
        return a;
    }

    private static Object[] fillArray(Integer pos, Object[] a, Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9) {
        fillWithArguments(a, pos, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9);
        return a;
    }

    private static final int FILL_ARRAYS_COUNT = 11;

    @Stable
    private static final MethodHandle[] FILL_ARRAYS = new MethodHandle[FILL_ARRAYS_COUNT];

    private static MethodHandle getFillArray(int count) {
        assert (count > 0 && count < FILL_ARRAYS_COUNT);
        MethodHandle mh = FILL_ARRAYS[count];
        if (mh != null) {
            return mh;
        }
        mh = findCollector("fillArray", count, Object[].class, Integer.class, Object[].class);
        FILL_ARRAYS[count] = mh;
        return mh;
    }

    private static Object copyAsPrimitiveArray(Wrapper w, Object... boxes) {
        Object a = w.makeArray(boxes.length);
        w.copyArrayUnboxing(boxes, 0, a, 0, boxes.length);
        return a;
    }

    static MethodHandle varargsArray(int nargs) {
        MethodHandle mh = ARRAYS[nargs];
        if (mh != null) {
            return mh;
        }
        if (nargs < ARRAYS_COUNT) {
            mh = findCollector("array", nargs, Object[].class);
        } else {
            mh = buildVarargsArray(getConstantHandle(MH_fillNewArray), getConstantHandle(MH_arrayIdentity), nargs);
        }
        assert (assertCorrectArity(mh, nargs));
        mh = makeIntrinsic(mh, Intrinsic.NEW_ARRAY);
        return ARRAYS[nargs] = mh;
    }

    private static boolean assertCorrectArity(MethodHandle mh, int arity) {
        assert (mh.type().parameterCount() == arity) : "arity != " + arity + ": " + mh;
        return true;
    }

    static <T> T[] identity(T[] x) {
        return x;
    }

    private static MethodHandle buildVarargsArray(MethodHandle newArray, MethodHandle finisher, int nargs) {
        int leftLen = Math.min(nargs, LEFT_ARGS);
        int rightLen = nargs - leftLen;
        MethodHandle leftCollector = newArray.bindTo(nargs);
        leftCollector = leftCollector.asCollector(Object[].class, leftLen);
        MethodHandle mh = finisher;
        if (rightLen > 0) {
            MethodHandle rightFiller = fillToRight(LEFT_ARGS + rightLen);
            if (mh.equals(getConstantHandle(MH_arrayIdentity)))
                mh = rightFiller;
            else
                mh = MethodHandles.collectArguments(mh, 0, rightFiller);
        }
        if (mh.equals(getConstantHandle(MH_arrayIdentity)))
            mh = leftCollector;
        else
            mh = MethodHandles.collectArguments(mh, 0, leftCollector);
        return mh;
    }

    private static final int LEFT_ARGS = FILL_ARRAYS_COUNT - 1;

    @Stable
    private static final MethodHandle[] FILL_ARRAY_TO_RIGHT = new MethodHandle[MAX_ARITY + 1];

    private static MethodHandle fillToRight(int nargs) {
        MethodHandle filler = FILL_ARRAY_TO_RIGHT[nargs];
        if (filler != null)
            return filler;
        filler = buildFiller(nargs);
        assert (assertCorrectArity(filler, nargs - LEFT_ARGS + 1));
        return FILL_ARRAY_TO_RIGHT[nargs] = filler;
    }

    private static MethodHandle buildFiller(int nargs) {
        if (nargs <= LEFT_ARGS)
            return getConstantHandle(MH_arrayIdentity);
        final int CHUNK = LEFT_ARGS;
        int rightLen = nargs % CHUNK;
        int midLen = nargs - rightLen;
        if (rightLen == 0) {
            midLen = nargs - (rightLen = CHUNK);
            if (FILL_ARRAY_TO_RIGHT[midLen] == null) {
                for (int j = LEFT_ARGS % CHUNK; j < midLen; j += CHUNK) if (j > LEFT_ARGS)
                    fillToRight(j);
            }
        }
        if (midLen < LEFT_ARGS)
            rightLen = nargs - (midLen = LEFT_ARGS);
        assert (rightLen > 0);
        MethodHandle midFill = fillToRight(midLen);
        MethodHandle rightFill = getFillArray(rightLen).bindTo(midLen);
        assert (midFill.type().parameterCount() == 1 + midLen - LEFT_ARGS);
        assert (rightFill.type().parameterCount() == 1 + rightLen);
        if (midLen == LEFT_ARGS)
            return rightFill;
        else
            return MethodHandles.collectArguments(rightFill, 0, midFill);
    }

    static final int MAX_JVM_ARITY = 255;

    static MethodHandle varargsArray(Class<?> arrayType, int nargs) {
        Class<?> elemType = arrayType.getComponentType();
        if (elemType == null)
            throw new IllegalArgumentException("not an array: " + arrayType);
        if (nargs >= MAX_JVM_ARITY / 2 - 1) {
            int slots = nargs;
            final int MAX_ARRAY_SLOTS = MAX_JVM_ARITY - 1;
            if (slots <= MAX_ARRAY_SLOTS && elemType.isPrimitive())
                slots *= Wrapper.forPrimitiveType(elemType).stackSlots();
            if (slots > MAX_ARRAY_SLOTS)
                throw new IllegalArgumentException("too many arguments: " + arrayType.getSimpleName() + ", length " + nargs);
        }
        if (elemType == Object.class)
            return varargsArray(nargs);
        MethodHandle[] cache = Makers.TYPED_COLLECTORS.get(elemType);
        MethodHandle mh = nargs < cache.length ? cache[nargs] : null;
        if (mh != null)
            return mh;
        if (nargs == 0) {
            Object example = java.lang.reflect.Array.newInstance(arrayType.getComponentType(), 0);
            mh = MethodHandles.constant(arrayType, example);
        } else if (elemType.isPrimitive()) {
            MethodHandle builder = getConstantHandle(MH_fillNewArray);
            MethodHandle producer = buildArrayProducer(arrayType);
            mh = buildVarargsArray(builder, producer, nargs);
        } else {
            Class<? extends Object[]> objArrayType = arrayType.asSubclass(Object[].class);
            Object[] example = Arrays.copyOf(NO_ARGS_ARRAY, 0, objArrayType);
            MethodHandle builder = getConstantHandle(MH_fillNewTypedArray).bindTo(example);
            MethodHandle producer = getConstantHandle(MH_arrayIdentity);
            mh = buildVarargsArray(builder, producer, nargs);
        }
        mh = mh.asType(MethodType.methodType(arrayType, Collections.<Class<?>>nCopies(nargs, elemType)));
        mh = makeIntrinsic(mh, Intrinsic.NEW_ARRAY);
        assert (assertCorrectArity(mh, nargs));
        if (nargs < cache.length)
            cache[nargs] = mh;
        return mh;
    }

    private static MethodHandle buildArrayProducer(Class<?> arrayType) {
        Class<?> elemType = arrayType.getComponentType();
        assert (elemType.isPrimitive());
        return getConstantHandle(MH_copyAsPrimitiveArray).bindTo(Wrapper.forPrimitiveType(elemType));
    }

    static void assertSame(Object mh1, Object mh2) {
        if (mh1 != mh2) {
            String msg = String.format("mh1 != mh2: mh1 = %s (form: %s); mh2 = %s (form: %s)", mh1, ((MethodHandle) mh1).form, mh2, ((MethodHandle) mh2).form);
            throw newInternalError(msg);
        }
    }

    static final byte NF_checkSpreadArgument = 0, NF_guardWithCatch = 1, NF_throwException = 2, NF_tryFinally = 3, NF_loop = 4, NF_profileBoolean = 5, NF_LIMIT = 6;

    @Stable
    private static final NamedFunction[] NFS = new NamedFunction[NF_LIMIT];

    static NamedFunction getFunction(byte func) {
        NamedFunction nf = NFS[func];
        if (nf != null) {
            return nf;
        }
        return NFS[func] = createFunction(func);
    }

    private static NamedFunction createFunction(byte func) {
        try {
            switch(func) {
                case NF_checkSpreadArgument:
                    return new NamedFunction(MethodHandleImpl.class.getDeclaredMethod("checkSpreadArgument", Object.class, int.class));
                case NF_guardWithCatch:
                    return new NamedFunction(MethodHandleImpl.class.getDeclaredMethod("guardWithCatch", MethodHandle.class, Class.class, MethodHandle.class, Object[].class));
                case NF_tryFinally:
                    return new NamedFunction(MethodHandleImpl.class.getDeclaredMethod("tryFinally", MethodHandle.class, MethodHandle.class, Object[].class));
                case NF_loop:
                    return new NamedFunction(MethodHandleImpl.class.getDeclaredMethod("loop", BasicType[].class, LoopClauses.class, Object[].class));
                case NF_throwException:
                    return new NamedFunction(MethodHandleImpl.class.getDeclaredMethod("throwException", Throwable.class));
                case NF_profileBoolean:
                    return new NamedFunction(MethodHandleImpl.class.getDeclaredMethod("profileBoolean", boolean.class, int[].class));
                default:
                    throw new InternalError("Undefined function: " + func);
            }
        } catch (ReflectiveOperationException ex) {
            throw newInternalError(ex);
        }
    }

    static {
        SharedSecrets.setJavaLangInvokeAccess(new JavaLangInvokeAccess() {

            @Override
            public Object newMemberName() {
                return new MemberName();
            }

            @Override
            public String getName(Object mname) {
                MemberName memberName = (MemberName) mname;
                return memberName.getName();
            }

            @Override
            public Class<?> getDeclaringClass(Object mname) {
                MemberName memberName = (MemberName) mname;
                return memberName.getDeclaringClass();
            }

            @Override
            public MethodType getMethodType(Object mname) {
                MemberName memberName = (MemberName) mname;
                return memberName.getMethodType();
            }

            @Override
            public String getMethodDescriptor(Object mname) {
                MemberName memberName = (MemberName) mname;
                return memberName.getMethodDescriptor();
            }

            @Override
            public boolean isNative(Object mname) {
                MemberName memberName = (MemberName) mname;
                return memberName.isNative();
            }

            @Override
            public byte[] generateDirectMethodHandleHolderClassBytes(String className, MethodType[] methodTypes, int[] types) {
                return GenerateJLIClassesHelper.generateDirectMethodHandleHolderClassBytes(className, methodTypes, types);
            }

            @Override
            public byte[] generateDelegatingMethodHandleHolderClassBytes(String className, MethodType[] methodTypes) {
                return GenerateJLIClassesHelper.generateDelegatingMethodHandleHolderClassBytes(className, methodTypes);
            }

            @Override
            public Map.Entry<String, byte[]> generateConcreteBMHClassBytes(final String types) {
                return GenerateJLIClassesHelper.generateConcreteBMHClassBytes(types);
            }

            @Override
            public byte[] generateBasicFormsClassBytes(final String className) {
                return GenerateJLIClassesHelper.generateBasicFormsClassBytes(className);
            }

            @Override
            public byte[] generateInvokersHolderClassBytes(final String className, MethodType[] methodTypes) {
                return GenerateJLIClassesHelper.generateInvokersHolderClassBytes(className, methodTypes);
            }
        });
    }

    private static MethodHandle unboxResultHandle(Class<?> returnType) {
        if (returnType.isPrimitive()) {
            if (returnType == void.class) {
                return ValueConversions.ignore();
            } else {
                Wrapper w = Wrapper.forPrimitiveType(returnType);
                return ValueConversions.unboxExact(w);
            }
        } else {
            return MethodHandles.identity(Object.class);
        }
    }

    static MethodHandle makeLoop(Class<?> tloop, List<Class<?>> targs, List<MethodHandle> init, List<MethodHandle> step, List<MethodHandle> pred, List<MethodHandle> fini) {
        MethodType type = MethodType.methodType(tloop, targs);
        BasicType[] initClauseTypes = init.stream().map(h -> h.type().returnType()).map(BasicType::basicType).toArray(BasicType[]::new);
        LambdaForm form = makeLoopForm(type.basicType(), initClauseTypes);
        MethodType varargsType = type.changeReturnType(Object[].class);
        MethodHandle collectArgs = varargsArray(type.parameterCount()).asType(varargsType);
        MethodHandle unboxResult = unboxResultHandle(tloop);
        LoopClauses clauseData = new LoopClauses(new MethodHandle[][] { toArray(init), toArray(step), toArray(pred), toArray(fini) });
        BoundMethodHandle.SpeciesData data = BoundMethodHandle.speciesData_LLL();
        BoundMethodHandle mh;
        try {
            mh = (BoundMethodHandle) data.factory().invokeBasic(type, form, (Object) clauseData, (Object) collectArgs, (Object) unboxResult);
        } catch (Throwable ex) {
            throw uncaughtException(ex);
        }
        assert (mh.type() == type);
        return mh;
    }

    private static MethodHandle[] toArray(List<MethodHandle> l) {
        return l.toArray(new MethodHandle[0]);
    }

    private static LambdaForm makeLoopForm(MethodType basicType, BasicType[] localVarTypes) {
        MethodType lambdaType = basicType.invokerType();
        final int THIS_MH = 0;
        final int ARG_BASE = 1;
        final int ARG_LIMIT = ARG_BASE + basicType.parameterCount();
        int nameCursor = ARG_LIMIT;
        final int GET_CLAUSE_DATA = nameCursor++;
        final int GET_COLLECT_ARGS = nameCursor++;
        final int GET_UNBOX_RESULT = nameCursor++;
        final int BOXED_ARGS = nameCursor++;
        final int LOOP = nameCursor++;
        final int UNBOX_RESULT = nameCursor++;
        LambdaForm lform = basicType.form().cachedLambdaForm(MethodTypeForm.LF_LOOP);
        if (lform == null) {
            Name[] names = arguments(nameCursor - ARG_LIMIT, lambdaType);
            BoundMethodHandle.SpeciesData data = BoundMethodHandle.speciesData_LLL();
            names[THIS_MH] = names[THIS_MH].withConstraint(data);
            names[GET_CLAUSE_DATA] = new Name(data.getterFunction(0), names[THIS_MH]);
            names[GET_COLLECT_ARGS] = new Name(data.getterFunction(1), names[THIS_MH]);
            names[GET_UNBOX_RESULT] = new Name(data.getterFunction(2), names[THIS_MH]);
            MethodType collectArgsType = basicType.changeReturnType(Object.class);
            MethodHandle invokeBasic = MethodHandles.basicInvoker(collectArgsType);
            Object[] args = new Object[invokeBasic.type().parameterCount()];
            args[0] = names[GET_COLLECT_ARGS];
            System.arraycopy(names, ARG_BASE, args, 1, ARG_LIMIT - ARG_BASE);
            names[BOXED_ARGS] = new Name(new NamedFunction(invokeBasic, Intrinsic.LOOP), args);
            Object[] lArgs = new Object[] { null, names[GET_CLAUSE_DATA], names[BOXED_ARGS] };
            names[LOOP] = new Name(getFunction(NF_loop), lArgs);
            MethodHandle invokeBasicUnbox = MethodHandles.basicInvoker(MethodType.methodType(basicType.rtype(), Object.class));
            Object[] unboxArgs = new Object[] { names[GET_UNBOX_RESULT], names[LOOP] };
            names[UNBOX_RESULT] = new Name(invokeBasicUnbox, unboxArgs);
            lform = basicType.form().setCachedLambdaForm(MethodTypeForm.LF_LOOP, new LambdaForm(lambdaType.parameterCount(), names, Kind.LOOP));
        }
        return lform.editor().noteLoopLocalTypesForm(BOXED_ARGS, localVarTypes);
    }

    static class LoopClauses {

        @Stable
        final MethodHandle[][] clauses;

        LoopClauses(MethodHandle[][] clauses) {
            assert clauses.length == 4;
            this.clauses = clauses;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer("LoopClauses -- ");
            for (int i = 0; i < 4; ++i) {
                if (i > 0) {
                    sb.append("       ");
                }
                sb.append('<').append(i).append(">: ");
                MethodHandle[] hs = clauses[i];
                for (int j = 0; j < hs.length; ++j) {
                    if (j > 0) {
                        sb.append("          ");
                    }
                    sb.append('*').append(j).append(": ").append(hs[j]).append('\n');
                }
            }
            sb.append(" --\n");
            return sb.toString();
        }
    }

    @LambdaForm.Hidden
    static Object loop(BasicType[] localTypes, LoopClauses clauseData, Object... av) throws Throwable {
        final MethodHandle[] init = clauseData.clauses[0];
        final MethodHandle[] step = clauseData.clauses[1];
        final MethodHandle[] pred = clauseData.clauses[2];
        final MethodHandle[] fini = clauseData.clauses[3];
        int varSize = (int) Stream.of(init).filter(h -> h.type().returnType() != void.class).count();
        int nArgs = init[0].type().parameterCount();
        Object[] varsAndArgs = new Object[varSize + nArgs];
        for (int i = 0, v = 0; i < init.length; ++i) {
            MethodHandle ih = init[i];
            if (ih.type().returnType() == void.class) {
                ih.invokeWithArguments(av);
            } else {
                varsAndArgs[v++] = ih.invokeWithArguments(av);
            }
        }
        System.arraycopy(av, 0, varsAndArgs, varSize, nArgs);
        final int nSteps = step.length;
        for (; ; ) {
            for (int i = 0, v = 0; i < nSteps; ++i) {
                MethodHandle p = pred[i];
                MethodHandle s = step[i];
                MethodHandle f = fini[i];
                if (s.type().returnType() == void.class) {
                    s.invokeWithArguments(varsAndArgs);
                } else {
                    varsAndArgs[v++] = s.invokeWithArguments(varsAndArgs);
                }
                if (!(boolean) p.invokeWithArguments(varsAndArgs)) {
                    return f.invokeWithArguments(varsAndArgs);
                }
            }
        }
    }

    static boolean countedLoopPredicate(int limit, int counter) {
        return counter < limit;
    }

    static int countedLoopStep(int limit, int counter) {
        return counter + 1;
    }

    static Iterator<?> initIterator(Iterable<?> it) {
        return it.iterator();
    }

    static boolean iteratePredicate(Iterator<?> it) {
        return it.hasNext();
    }

    static Object iterateNext(Iterator<?> it) {
        return it.next();
    }

    static MethodHandle makeTryFinally(MethodHandle target, MethodHandle cleanup, Class<?> rtype, List<Class<?>> argTypes) {
        MethodType type = MethodType.methodType(rtype, argTypes);
        LambdaForm form = makeTryFinallyForm(type.basicType());
        MethodType varargsType = type.changeReturnType(Object[].class);
        MethodHandle collectArgs = varargsArray(type.parameterCount()).asType(varargsType);
        MethodHandle unboxResult = unboxResultHandle(rtype);
        BoundMethodHandle.SpeciesData data = BoundMethodHandle.speciesData_LLLL();
        BoundMethodHandle mh;
        try {
            mh = (BoundMethodHandle) data.factory().invokeBasic(type, form, (Object) target, (Object) cleanup, (Object) collectArgs, (Object) unboxResult);
        } catch (Throwable ex) {
            throw uncaughtException(ex);
        }
        assert (mh.type() == type);
        return mh;
    }

    private static LambdaForm makeTryFinallyForm(MethodType basicType) {
        MethodType lambdaType = basicType.invokerType();
        LambdaForm lform = basicType.form().cachedLambdaForm(MethodTypeForm.LF_TF);
        if (lform != null) {
            return lform;
        }
        final int THIS_MH = 0;
        final int ARG_BASE = 1;
        final int ARG_LIMIT = ARG_BASE + basicType.parameterCount();
        int nameCursor = ARG_LIMIT;
        final int GET_TARGET = nameCursor++;
        final int GET_CLEANUP = nameCursor++;
        final int GET_COLLECT_ARGS = nameCursor++;
        final int GET_UNBOX_RESULT = nameCursor++;
        final int BOXED_ARGS = nameCursor++;
        final int TRY_FINALLY = nameCursor++;
        final int UNBOX_RESULT = nameCursor++;
        Name[] names = arguments(nameCursor - ARG_LIMIT, lambdaType);
        BoundMethodHandle.SpeciesData data = BoundMethodHandle.speciesData_LLLL();
        names[THIS_MH] = names[THIS_MH].withConstraint(data);
        names[GET_TARGET] = new Name(data.getterFunction(0), names[THIS_MH]);
        names[GET_CLEANUP] = new Name(data.getterFunction(1), names[THIS_MH]);
        names[GET_COLLECT_ARGS] = new Name(data.getterFunction(2), names[THIS_MH]);
        names[GET_UNBOX_RESULT] = new Name(data.getterFunction(3), names[THIS_MH]);
        MethodType collectArgsType = basicType.changeReturnType(Object.class);
        MethodHandle invokeBasic = MethodHandles.basicInvoker(collectArgsType);
        Object[] args = new Object[invokeBasic.type().parameterCount()];
        args[0] = names[GET_COLLECT_ARGS];
        System.arraycopy(names, ARG_BASE, args, 1, ARG_LIMIT - ARG_BASE);
        names[BOXED_ARGS] = new Name(new NamedFunction(invokeBasic, Intrinsic.TRY_FINALLY), args);
        Object[] tfArgs = new Object[] { names[GET_TARGET], names[GET_CLEANUP], names[BOXED_ARGS] };
        names[TRY_FINALLY] = new Name(getFunction(NF_tryFinally), tfArgs);
        MethodHandle invokeBasicUnbox = MethodHandles.basicInvoker(MethodType.methodType(basicType.rtype(), Object.class));
        Object[] unboxArgs = new Object[] { names[GET_UNBOX_RESULT], names[TRY_FINALLY] };
        names[UNBOX_RESULT] = new Name(invokeBasicUnbox, unboxArgs);
        lform = new LambdaForm(lambdaType.parameterCount(), names, Kind.TRY_FINALLY);
        return basicType.form().setCachedLambdaForm(MethodTypeForm.LF_TF, lform);
    }

    @LambdaForm.Hidden
    static Object tryFinally(MethodHandle target, MethodHandle cleanup, Object... av) throws Throwable {
        Throwable t = null;
        Object r = null;
        try {
            r = target.invokeWithArguments(av);
        } catch (Throwable thrown) {
            t = thrown;
            throw t;
        } finally {
            Object[] args = target.type().returnType() == void.class ? prepend(av, t) : prepend(av, t, r);
            r = cleanup.invokeWithArguments(args);
        }
        return r;
    }

    static final int MH_cast = 0, MH_selectAlternative = 1, MH_copyAsPrimitiveArray = 2, MH_fillNewTypedArray = 3, MH_fillNewArray = 4, MH_arrayIdentity = 5, MH_countedLoopPred = 6, MH_countedLoopStep = 7, MH_initIterator = 8, MH_iteratePred = 9, MH_iterateNext = 10, MH_Array_newInstance = 11, MH_LIMIT = 12;

    static MethodHandle getConstantHandle(int idx) {
        MethodHandle handle = HANDLES[idx];
        if (handle != null) {
            return handle;
        }
        return setCachedHandle(idx, makeConstantHandle(idx));
    }

    private static synchronized MethodHandle setCachedHandle(int idx, final MethodHandle method) {
        MethodHandle prev = HANDLES[idx];
        if (prev != null) {
            return prev;
        }
        HANDLES[idx] = method;
        return method;
    }

    @Stable
    private static final MethodHandle[] HANDLES = new MethodHandle[MH_LIMIT];

    private static MethodHandle makeConstantHandle(int idx) {
        try {
            switch(idx) {
                case MH_cast:
                    return IMPL_LOOKUP.findVirtual(Class.class, "cast", MethodType.methodType(Object.class, Object.class));
                case MH_copyAsPrimitiveArray:
                    return IMPL_LOOKUP.findStatic(MethodHandleImpl.class, "copyAsPrimitiveArray", MethodType.methodType(Object.class, Wrapper.class, Object[].class));
                case MH_arrayIdentity:
                    return IMPL_LOOKUP.findStatic(MethodHandleImpl.class, "identity", MethodType.methodType(Object[].class, Object[].class));
                case MH_fillNewArray:
                    return IMPL_LOOKUP.findStatic(MethodHandleImpl.class, "fillNewArray", MethodType.methodType(Object[].class, Integer.class, Object[].class));
                case MH_fillNewTypedArray:
                    return IMPL_LOOKUP.findStatic(MethodHandleImpl.class, "fillNewTypedArray", MethodType.methodType(Object[].class, Object[].class, Integer.class, Object[].class));
                case MH_selectAlternative:
                    return IMPL_LOOKUP.findStatic(MethodHandleImpl.class, "selectAlternative", MethodType.methodType(MethodHandle.class, boolean.class, MethodHandle.class, MethodHandle.class));
                case MH_countedLoopPred:
                    return IMPL_LOOKUP.findStatic(MethodHandleImpl.class, "countedLoopPredicate", MethodType.methodType(boolean.class, int.class, int.class));
                case MH_countedLoopStep:
                    return IMPL_LOOKUP.findStatic(MethodHandleImpl.class, "countedLoopStep", MethodType.methodType(int.class, int.class, int.class));
                case MH_initIterator:
                    return IMPL_LOOKUP.findStatic(MethodHandleImpl.class, "initIterator", MethodType.methodType(Iterator.class, Iterable.class));
                case MH_iteratePred:
                    return IMPL_LOOKUP.findStatic(MethodHandleImpl.class, "iteratePredicate", MethodType.methodType(boolean.class, Iterator.class));
                case MH_iterateNext:
                    return IMPL_LOOKUP.findStatic(MethodHandleImpl.class, "iterateNext", MethodType.methodType(Object.class, Iterator.class));
                case MH_Array_newInstance:
                    return IMPL_LOOKUP.findStatic(Array.class, "newInstance", MethodType.methodType(Object.class, Class.class, int.class));
            }
        } catch (ReflectiveOperationException ex) {
            throw newInternalError(ex);
        }
        throw newInternalError("Unknown function index: " + idx);
    }
}
