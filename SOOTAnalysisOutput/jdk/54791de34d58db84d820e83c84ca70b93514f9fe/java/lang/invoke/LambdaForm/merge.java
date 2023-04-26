package java.lang.invoke;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import sun.invoke.util.Wrapper;
import static java.lang.invoke.MethodHandleStatics.*;
import static java.lang.invoke.MethodHandleNatives.Constants.*;
import java.lang.reflect.Field;
import java.util.Objects;

class LambdaForm {

    final int arity;

    final int result;

    @Stable
    final Name[] names;

    final String debugName;

    MemberName vmentry;

    private boolean isCompiled;

    LambdaForm[] bindCache;

    public static final int VOID_RESULT = -1, LAST_RESULT = -2;

    LambdaForm(String debugName, int arity, Name[] names, int result) {
        assert (namesOK(arity, names));
        this.arity = arity;
        this.result = fixResult(result, names);
        this.names = names.clone();
        this.debugName = debugName;
        normalize();
    }

    LambdaForm(String debugName, int arity, Name[] names) {
        this(debugName, arity, names, LAST_RESULT);
    }

    LambdaForm(String debugName, Name[] formals, Name[] temps, Name result) {
        this(debugName, formals.length, buildNames(formals, temps, result), LAST_RESULT);
    }

    private static Name[] buildNames(Name[] formals, Name[] temps, Name result) {
        int arity = formals.length;
        int length = arity + temps.length + (result == null ? 0 : 1);
        Name[] names = Arrays.copyOf(formals, length);
        System.arraycopy(temps, 0, names, arity, temps.length);
        if (result != null)
            names[length - 1] = result;
        return names;
    }

    private LambdaForm(String sig) {
        assert (isValidSignature(sig));
        this.arity = signatureArity(sig);
        this.result = (signatureReturn(sig) == 'V' ? -1 : arity);
        this.names = buildEmptyNames(arity, sig);
        this.debugName = "LF.zero";
        assert (nameRefsAreLegal());
        assert (isEmpty());
        assert (sig.equals(basicTypeSignature()));
    }

    private static Name[] buildEmptyNames(int arity, String basicTypeSignature) {
        assert (isValidSignature(basicTypeSignature));
        int resultPos = arity + 1;
        if (arity < 0 || basicTypeSignature.length() != resultPos + 1)
            throw new IllegalArgumentException("bad arity for " + basicTypeSignature);
        int numRes = (basicTypeSignature.charAt(resultPos) == 'V' ? 0 : 1);
        Name[] names = arguments(numRes, basicTypeSignature.substring(0, arity));
        for (int i = 0; i < numRes; i++) {
            names[arity + i] = constantZero(arity + i, basicTypeSignature.charAt(resultPos + i));
        }
        return names;
    }

    private static int fixResult(int result, Name[] names) {
        if (result >= 0) {
            if (names[result].type == 'V')
                return -1;
        } else if (result == LAST_RESULT) {
            return names.length - 1;
        }
        return result;
    }

    private static boolean namesOK(int arity, Name[] names) {
        for (int i = 0; i < names.length; i++) {
            Name n = names[i];
            assert (n != null) : "n is null";
            if (i < arity)
                assert (n.isParam()) : n + " is not param at " + i;
            else
                assert (!n.isParam()) : n + " is param at " + i;
        }
        return true;
    }

    private void normalize() {
        Name[] oldNames = null;
        int changesStart = 0;
        for (int i = 0; i < names.length; i++) {
            Name n = names[i];
            if (!n.initIndex(i)) {
                if (oldNames == null) {
                    oldNames = names.clone();
                    changesStart = i;
                }
                names[i] = n.cloneWithIndex(i);
            }
        }
        if (oldNames != null) {
            int startFixing = arity;
            if (startFixing <= changesStart)
                startFixing = changesStart + 1;
            for (int i = startFixing; i < names.length; i++) {
                Name fixed = names[i].replaceNames(oldNames, names, changesStart, i);
                names[i] = fixed.newIndex(i);
            }
        }
        assert (nameRefsAreLegal());
        int maxInterned = Math.min(arity, INTERNED_ARGUMENT_LIMIT);
        boolean needIntern = false;
        for (int i = 0; i < maxInterned; i++) {
            Name n = names[i], n2 = internArgument(n);
            if (n != n2) {
                names[i] = n2;
                needIntern = true;
            }
        }
        if (needIntern) {
            for (int i = arity; i < names.length; i++) {
                names[i].internArguments();
            }
            assert (nameRefsAreLegal());
        }
    }

    private boolean nameRefsAreLegal() {
        assert (arity >= 0 && arity <= names.length);
        assert (result >= -1 && result < names.length);
        for (int i = 0; i < arity; i++) {
            Name n = names[i];
            assert (n.index() == i) : Arrays.asList(n.index(), i);
            assert (n.isParam());
        }
        for (int i = arity; i < names.length; i++) {
            Name n = names[i];
            assert (n.index() == i);
            for (Object arg : n.arguments) {
                if (arg instanceof Name) {
                    Name n2 = (Name) arg;
                    int i2 = n2.index;
                    assert (0 <= i2 && i2 < names.length) : n.debugString() + ": 0 <= i2 && i2 < names.length: 0 <= " + i2 + " < " + names.length;
                    assert (names[i2] == n2) : Arrays.asList("-1-", i, "-2-", n.debugString(), "-3-", i2, "-4-", n2.debugString(), "-5-", names[i2].debugString(), "-6-", this);
                    assert (i2 < i);
                }
            }
        }
        return true;
    }

    char returnType() {
        if (result < 0)
            return 'V';
        Name n = names[result];
        return n.type;
    }

    char parameterType(int n) {
        assert (n < arity);
        return names[n].type;
    }

    int arity() {
        return arity;
    }

    MethodType methodType() {
        return signatureType(basicTypeSignature());
    }

    final String basicTypeSignature() {
        StringBuilder buf = new StringBuilder(arity() + 3);
        for (int i = 0, a = arity(); i < a; i++) buf.append(parameterType(i));
        return buf.append('_').append(returnType()).toString();
    }

    static int signatureArity(String sig) {
        assert (isValidSignature(sig));
        return sig.indexOf('_');
    }

    static char signatureReturn(String sig) {
        return sig.charAt(signatureArity(sig) + 1);
    }

    static boolean isValidSignature(String sig) {
        int arity = sig.indexOf('_');
        if (arity < 0)
            return false;
        int siglen = sig.length();
        if (siglen != arity + 2)
            return false;
        for (int i = 0; i < siglen; i++) {
            if (i == arity)
                continue;
            char c = sig.charAt(i);
            if (c == 'V')
                return (i == siglen - 1 && arity == siglen - 2);
            if (ALL_TYPES.indexOf(c) < 0)
                return false;
        }
        return true;
    }

    static Class<?> typeClass(char t) {
        switch(t) {
            case 'I':
                return int.class;
            case 'J':
                return long.class;
            case 'F':
                return float.class;
            case 'D':
                return double.class;
            case 'L':
                return Object.class;
            case 'V':
                return void.class;
            default:
                assert false;
        }
        return null;
    }

    static MethodType signatureType(String sig) {
        Class<?>[] ptypes = new Class<?>[signatureArity(sig)];
        for (int i = 0; i < ptypes.length; i++) ptypes[i] = typeClass(sig.charAt(i));
        Class<?> rtype = typeClass(signatureReturn(sig));
        return MethodType.methodType(rtype, ptypes);
    }

    public void prepare() {
        if (COMPILE_THRESHOLD == 0) {
            compileToBytecode();
        }
        if (this.vmentry != null) {
            return;
        }
        LambdaForm prep = getPreparedForm(basicTypeSignature());
        this.vmentry = prep.vmentry;
    }

    MemberName compileToBytecode() {
        MethodType invokerType = methodType();
        assert (vmentry == null || vmentry.getMethodType().basicType().equals(invokerType));
        if (vmentry != null && isCompiled) {
            return vmentry;
        }
        try {
            vmentry = InvokerBytecodeGenerator.generateCustomizedCode(this, invokerType);
            if (TRACE_INTERPRETER)
                traceInterpreter("compileToBytecode", this);
            isCompiled = true;
            return vmentry;
        } catch (Error | Exception ex) {
            throw newInternalError("compileToBytecode", ex);
        }
    }

    private static final ConcurrentHashMap<String, LambdaForm> PREPARED_FORMS;

    static {
        int capacity = 512;
        float loadFactor = 0.75f;
        int writers = 1;
        PREPARED_FORMS = new ConcurrentHashMap<>(capacity, loadFactor, writers);
    }

    private static Map<String, LambdaForm> computeInitialPreparedForms() {
        HashMap<String, LambdaForm> forms = new HashMap<>();
        for (MemberName m : MemberName.getFactory().getMethods(LambdaForm.class, false, null, null, null)) {
            if (!m.isStatic() || !m.isPackage())
                continue;
            MethodType mt = m.getMethodType();
            if (mt.parameterCount() > 0 && mt.parameterType(0) == MethodHandle.class && m.getName().startsWith("interpret_")) {
                String sig = basicTypeSignature(mt);
                assert (m.getName().equals("interpret" + sig.substring(sig.indexOf('_'))));
                LambdaForm form = new LambdaForm(sig);
                form.vmentry = m;
                mt.form().setCachedLambdaForm(MethodTypeForm.LF_COUNTER, form);
                forms.put(sig, form);
            }
        }
        return forms;
    }

    private static final boolean USE_PREDEFINED_INTERPRET_METHODS = true;

    static Object interpret_L(MethodHandle mh) throws Throwable {
        Object[] av = { mh };
        String sig = null;
        assert (argumentTypesMatch(sig = "L_L", av));
        Object res = mh.form.interpretWithArguments(av);
        assert (returnTypesMatch(sig, av, res));
        return res;
    }

    static Object interpret_L(MethodHandle mh, Object x1) throws Throwable {
        Object[] av = { mh, x1 };
        String sig = null;
        assert (argumentTypesMatch(sig = "LL_L", av));
        Object res = mh.form.interpretWithArguments(av);
        assert (returnTypesMatch(sig, av, res));
        return res;
    }

    static Object interpret_L(MethodHandle mh, Object x1, Object x2) throws Throwable {
        Object[] av = { mh, x1, x2 };
        String sig = null;
        assert (argumentTypesMatch(sig = "LLL_L", av));
        Object res = mh.form.interpretWithArguments(av);
        assert (returnTypesMatch(sig, av, res));
        return res;
    }

    private static LambdaForm getPreparedForm(String sig) {
        MethodType mtype = signatureType(sig);
        LambdaForm prep = mtype.form().cachedLambdaForm(MethodTypeForm.LF_INTERPRET);
        if (prep != null)
            return prep;
        assert (isValidSignature(sig));
        prep = new LambdaForm(sig);
        prep.vmentry = InvokerBytecodeGenerator.generateLambdaFormInterpreterEntryPoint(sig);
        return mtype.form().setCachedLambdaForm(MethodTypeForm.LF_INTERPRET, prep);
    }

    private static boolean argumentTypesMatch(String sig, Object[] av) {
        int arity = signatureArity(sig);
        assert (av.length == arity) : "av.length == arity: av.length=" + av.length + ", arity=" + arity;
        assert (av[0] instanceof MethodHandle) : "av[0] not instace of MethodHandle: " + av[0];
        MethodHandle mh = (MethodHandle) av[0];
        MethodType mt = mh.type();
        assert (mt.parameterCount() == arity - 1);
        for (int i = 0; i < av.length; i++) {
            Class<?> pt = (i == 0 ? MethodHandle.class : mt.parameterType(i - 1));
            assert (valueMatches(sig.charAt(i), pt, av[i]));
        }
        return true;
    }

    private static boolean valueMatches(char tc, Class<?> type, Object x) {
        if (type == void.class)
            tc = 'V';
        assert tc == basicType(type) : tc + " == basicType(" + type + ")=" + basicType(type);
        switch(tc) {
            case 'I':
                assert checkInt(type, x) : "checkInt(" + type + "," + x + ")";
                break;
            case 'J':
                assert x instanceof Long : "instanceof Long: " + x;
                break;
            case 'F':
                assert x instanceof Float : "instanceof Float: " + x;
                break;
            case 'D':
                assert x instanceof Double : "instanceof Double: " + x;
                break;
            case 'L':
                assert checkRef(type, x) : "checkRef(" + type + "," + x + ")";
                break;
            case 'V':
                break;
            default:
                assert (false);
        }
        return true;
    }

    private static boolean returnTypesMatch(String sig, Object[] av, Object res) {
        MethodHandle mh = (MethodHandle) av[0];
        return valueMatches(signatureReturn(sig), mh.type().returnType(), res);
    }

    private static boolean checkInt(Class<?> type, Object x) {
        assert (x instanceof Integer);
        if (type == int.class)
            return true;
        Wrapper w = Wrapper.forBasicType(type);
        assert (w.isSubwordOrInt());
        Object x1 = Wrapper.INT.wrap(w.wrap(x));
        return x.equals(x1);
    }

    private static boolean checkRef(Class<?> type, Object x) {
        assert (!type.isPrimitive());
        if (x == null)
            return true;
        if (type.isInterface())
            return true;
        return type.isInstance(x);
    }

    private static final int COMPILE_THRESHOLD;

    static {
        if (MethodHandleStatics.COMPILE_THRESHOLD != null)
            COMPILE_THRESHOLD = MethodHandleStatics.COMPILE_THRESHOLD;
        else
            COMPILE_THRESHOLD = 30;
    }

    private int invocationCounter = 0;

    @Hidden
    @DontInline
    Object interpretWithArguments(Object... argumentValues) throws Throwable {
        if (TRACE_INTERPRETER)
            return interpretWithArgumentsTracing(argumentValues);
        checkInvocationCounter();
        assert (arityCheck(argumentValues));
        Object[] values = Arrays.copyOf(argumentValues, names.length);
        for (int i = argumentValues.length; i < values.length; i++) {
            values[i] = interpretName(names[i], values);
        }
        return (result < 0) ? null : values[result];
    }

    @Hidden
    @DontInline
    Object interpretName(Name name, Object[] values) throws Throwable {
        if (TRACE_INTERPRETER)
            traceInterpreter("| interpretName", name.debugString(), (Object[]) null);
        Object[] arguments = Arrays.copyOf(name.arguments, name.arguments.length, Object[].class);
        for (int i = 0; i < arguments.length; i++) {
            Object a = arguments[i];
            if (a instanceof Name) {
                int i2 = ((Name) a).index();
                assert (names[i2] == a);
                a = values[i2];
                arguments[i] = a;
            }
        }
        return name.function.invokeWithArguments(arguments);
    }

    private void checkInvocationCounter() {
        if (COMPILE_THRESHOLD != 0 && invocationCounter < COMPILE_THRESHOLD) {
            invocationCounter++;
            if (invocationCounter >= COMPILE_THRESHOLD) {
                compileToBytecode();
            }
        }
    }

    Object interpretWithArgumentsTracing(Object... argumentValues) throws Throwable {
        traceInterpreter("[ interpretWithArguments", this, argumentValues);
        if (invocationCounter < COMPILE_THRESHOLD) {
            int ctr = invocationCounter++;
            traceInterpreter("| invocationCounter", ctr);
            if (invocationCounter >= COMPILE_THRESHOLD) {
                compileToBytecode();
            }
        }
        Object rval;
        try {
            assert (arityCheck(argumentValues));
            Object[] values = Arrays.copyOf(argumentValues, names.length);
            for (int i = argumentValues.length; i < values.length; i++) {
                values[i] = interpretName(names[i], values);
            }
            rval = (result < 0) ? null : values[result];
        } catch (Throwable ex) {
            traceInterpreter("] throw =>", ex);
            throw ex;
        }
        traceInterpreter("] return =>", rval);
        return rval;
    }

    static void traceInterpreter(String event, Object obj, Object... args) {
        if (TRACE_INTERPRETER) {
            System.out.println("LFI: " + event + " " + (obj != null ? obj : "") + (args != null && args.length != 0 ? Arrays.asList(args) : ""));
        }
    }

    static void traceInterpreter(String event, Object obj) {
        traceInterpreter(event, obj, (Object[]) null);
    }

    private boolean arityCheck(Object[] argumentValues) {
        assert (argumentValues.length == arity) : arity + "!=" + Arrays.asList(argumentValues) + ".length";
        assert (argumentValues[0] instanceof MethodHandle) : "not MH: " + argumentValues[0];
        assert (((MethodHandle) argumentValues[0]).internalForm() == this);
        return true;
    }

    private boolean isEmpty() {
        if (result < 0)
            return (names.length == arity);
        else if (result == arity && names.length == arity + 1)
            return names[arity].isConstantZero();
        else
            return false;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder(debugName + "=Lambda(");
        for (int i = 0; i < names.length; i++) {
            if (i == arity)
                buf.append(")=>{");
            Name n = names[i];
            if (i >= arity)
                buf.append("\n    ");
            buf.append(n);
            if (i < arity) {
                if (i + 1 < arity)
                    buf.append(",");
                continue;
            }
            buf.append("=").append(n.exprString());
            buf.append(";");
        }
        buf.append(result < 0 ? "void" : names[result]).append("}");
        if (TRACE_INTERPRETER) {
            buf.append(":").append(basicTypeSignature());
            buf.append("/").append(vmentry);
        }
        return buf.toString();
    }

    LambdaForm bindImmediate(int pos, char basicType, Object value) {
        assert pos > 0 && pos < arity && names[pos].type == basicType && Name.typesMatch(basicType, value);
        int arity2 = arity - 1;
        Name[] names2 = new Name[names.length - 1];
        for (int r = 0, w = 0; r < names.length; ++r, ++w) {
            Name n = names[r];
            if (n.isParam()) {
                if (n.index == pos) {
                    --w;
                } else {
                    names2[w] = new Name(w, n.type);
                }
            } else {
                Object[] arguments2 = new Object[n.arguments.length];
                for (int i = 0; i < n.arguments.length; ++i) {
                    Object arg = n.arguments[i];
                    if (arg instanceof Name) {
                        int ni = ((Name) arg).index;
                        if (ni == pos) {
                            arguments2[i] = value;
                        } else if (ni < pos) {
                            arguments2[i] = names2[ni];
                        } else {
                            arguments2[i] = names2[ni - 1];
                        }
                    } else {
                        arguments2[i] = arg;
                    }
                }
                names2[w] = new Name(n.function, arguments2);
                names2[w].initIndex(w);
            }
        }
        int result2 = result == -1 ? -1 : result - 1;
        return new LambdaForm(debugName, arity2, names2, result2);
    }

    LambdaForm bind(int namePos, BoundMethodHandle.SpeciesData oldData) {
        Name name = names[namePos];
        BoundMethodHandle.SpeciesData newData = oldData.extendWithType(name.type);
        return bind(name, newData.getterName(names[0], oldData.fieldCount()), oldData, newData);
    }

    LambdaForm bind(Name name, Name binding, BoundMethodHandle.SpeciesData oldData, BoundMethodHandle.SpeciesData newData) {
        int pos = name.index;
        assert (name.isParam());
        assert (!binding.isParam());
        assert (name.type == binding.type);
        assert (0 <= pos && pos < arity && names[pos] == name);
        assert (binding.function.memberDeclaringClassOrNull() == newData.clazz);
        assert (oldData.getters.length == newData.getters.length - 1);
        if (bindCache != null) {
            LambdaForm form = bindCache[pos];
            if (form != null) {
                assert (form.contains(binding)) : "form << " + form + " >> does not contain binding << " + binding + " >>";
                return form;
            }
        } else {
            bindCache = new LambdaForm[arity];
        }
        assert (nameRefsAreLegal());
        int arity2 = arity - 1;
        Name[] names2 = names.clone();
        names2[pos] = binding;
        int firstOldRef = -1;
        for (int i = 0; i < names2.length; i++) {
            Name n = names[i];
            if (n.function != null && n.function.memberDeclaringClassOrNull() == oldData.clazz) {
                MethodHandle oldGetter = n.function.resolvedHandle;
                MethodHandle newGetter = null;
                for (int j = 0; j < oldData.getters.length; j++) {
                    if (oldGetter == oldData.getters[j])
                        newGetter = newData.getters[j];
                }
                if (newGetter != null) {
                    if (firstOldRef < 0)
                        firstOldRef = i;
                    Name n2 = new Name(newGetter, n.arguments);
                    names2[i] = n2;
                }
            }
        }
        assert (firstOldRef < 0 || firstOldRef > pos);
        for (int i = pos + 1; i < names2.length; i++) {
            if (i <= arity2)
                continue;
            names2[i] = names2[i].replaceNames(names, names2, pos, i);
        }
        int insPos = pos;
        for (; insPos + 1 < names2.length; insPos++) {
            Name n = names2[insPos + 1];
            if (n.isSiblingBindingBefore(binding)) {
                names2[insPos] = n;
            } else {
                break;
            }
        }
        names2[insPos] = binding;
        int result2 = result;
        if (result2 == pos)
            result2 = insPos;
        else if (result2 > pos && result2 <= insPos)
            result2 -= 1;
        return bindCache[pos] = new LambdaForm(debugName, arity2, names2, result2);
    }

    boolean contains(Name name) {
        int pos = name.index();
        if (pos >= 0) {
            return pos < names.length && name.equals(names[pos]);
        }
        for (int i = arity; i < names.length; i++) {
            if (name.equals(names[i]))
                return true;
        }
        return false;
    }

    LambdaForm addArguments(int pos, char... types) {
        assert (pos <= arity);
        int length = names.length;
        int inTypes = types.length;
        Name[] names2 = Arrays.copyOf(names, length + inTypes);
        int arity2 = arity + inTypes;
        int result2 = result;
        if (result2 >= arity)
            result2 += inTypes;
        int argpos = pos + 1;
        System.arraycopy(names, argpos, names2, argpos + inTypes, length - argpos);
        for (int i = 0; i < inTypes; i++) {
            names2[argpos + i] = new Name(types[i]);
        }
        return new LambdaForm(debugName, arity2, names2, result2);
    }

    LambdaForm addArguments(int pos, List<Class<?>> types) {
        char[] basicTypes = new char[types.size()];
        for (int i = 0; i < basicTypes.length; i++) basicTypes[i] = basicType(types.get(i));
        return addArguments(pos, basicTypes);
    }

    LambdaForm permuteArguments(int skip, int[] reorder, char[] types) {
        int length = names.length;
        int inTypes = types.length;
        int outArgs = reorder.length;
        assert (skip + outArgs == arity);
        assert (permutedTypesMatch(reorder, types, names, skip));
        int pos = 0;
        while (pos < outArgs && reorder[pos] == pos) pos += 1;
        Name[] names2 = new Name[length - outArgs + inTypes];
        System.arraycopy(names, 0, names2, 0, skip + pos);
        int bodyLength = length - arity;
        System.arraycopy(names, skip + outArgs, names2, skip + inTypes, bodyLength);
        int arity2 = names2.length - bodyLength;
        int result2 = result;
        if (result2 >= 0) {
            if (result2 < skip + outArgs) {
                result2 = reorder[result2 - skip];
            } else {
                result2 = result2 - outArgs + inTypes;
            }
        }
        for (int j = pos; j < outArgs; j++) {
            Name n = names[skip + j];
            int i = reorder[j];
            Name n2 = names2[skip + i];
            if (n2 == null)
                names2[skip + i] = n2 = new Name(types[i]);
            else
                assert (n2.type == types[i]);
            for (int k = arity2; k < names2.length; k++) {
                names2[k] = names2[k].replaceName(n, n2);
            }
        }
        for (int i = skip + pos; i < arity2; i++) {
            if (names2[i] == null)
                names2[i] = argument(i, types[i - skip]);
        }
        for (int j = arity; j < names.length; j++) {
            int i = j - arity + arity2;
            Name n = names[j];
            Name n2 = names2[i];
            if (n != n2) {
                for (int k = i + 1; k < names2.length; k++) {
                    names2[k] = names2[k].replaceName(n, n2);
                }
            }
        }
        return new LambdaForm(debugName, arity2, names2, result2);
    }

    static boolean permutedTypesMatch(int[] reorder, char[] types, Name[] names, int skip) {
        int inTypes = types.length;
        int outArgs = reorder.length;
        for (int i = 0; i < outArgs; i++) {
            assert (names[skip + i].isParam());
            assert (names[skip + i].type == types[reorder[i]]);
        }
        return true;
    }

    static class NamedFunction {

        final MemberName member;

        @Stable
        MethodHandle resolvedHandle;

        @Stable
        MethodHandle invoker;

        NamedFunction(MethodHandle resolvedHandle) {
            this(resolvedHandle.internalMemberName(), resolvedHandle);
        }

        NamedFunction(MemberName member, MethodHandle resolvedHandle) {
            this.member = member;
            this.resolvedHandle = resolvedHandle;
        }

        NamedFunction(MethodType basicInvokerType) {
            assert (basicInvokerType == basicInvokerType.basicType()) : basicInvokerType;
            if (basicInvokerType.parameterSlotCount() < MethodType.MAX_MH_INVOKER_ARITY) {
                this.resolvedHandle = basicInvokerType.invokers().basicInvoker();
                this.member = resolvedHandle.internalMemberName();
            } else {
                this.member = Invokers.invokeBasicMethod(basicInvokerType);
            }
        }

        NamedFunction(Method method) {
            this(new MemberName(method));
        }

        NamedFunction(Field field) {
            this(new MemberName(field));
        }

        NamedFunction(MemberName member) {
            this.member = member;
            this.resolvedHandle = null;
        }

        MethodHandle resolvedHandle() {
            if (resolvedHandle == null)
                resolve();
            return resolvedHandle;
        }

        void resolve() {
            resolvedHandle = DirectMethodHandle.make(member);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;
            if (other == null)
                return false;
            if (!(other instanceof NamedFunction))
                return false;
            NamedFunction that = (NamedFunction) other;
            return this.member != null && this.member.equals(that.member);
        }

        @Override
        public int hashCode() {
            if (member != null)
                return member.hashCode();
            return super.hashCode();
        }

        static void initializeInvokers() {
            for (MemberName m : MemberName.getFactory().getMethods(NamedFunction.class, false, null, null, null)) {
                if (!m.isStatic() || !m.isPackage())
                    continue;
                MethodType type = m.getMethodType();
                if (type.equals(INVOKER_METHOD_TYPE) && m.getName().startsWith("invoke_")) {
                    String sig = m.getName().substring("invoke_".length());
                    int arity = LambdaForm.signatureArity(sig);
                    MethodType srcType = MethodType.genericMethodType(arity);
                    if (LambdaForm.signatureReturn(sig) == 'V')
                        srcType = srcType.changeReturnType(void.class);
                    MethodTypeForm typeForm = srcType.form();
                    typeForm.namedFunctionInvoker = DirectMethodHandle.make(m);
                }
            }
        }

        @Hidden
        static Object invoke__V(MethodHandle mh, Object[] a) throws Throwable {
            assert (a.length == 0);
            mh.invokeBasic();
            return null;
        }

        @Hidden
        static Object invoke_L_V(MethodHandle mh, Object[] a) throws Throwable {
            assert (a.length == 1);
            mh.invokeBasic(a[0]);
            return null;
        }

        @Hidden
        static Object invoke_LL_V(MethodHandle mh, Object[] a) throws Throwable {
            assert (a.length == 2);
            mh.invokeBasic(a[0], a[1]);
            return null;
        }

        @Hidden
        static Object invoke_LLL_V(MethodHandle mh, Object[] a) throws Throwable {
            assert (a.length == 3);
            mh.invokeBasic(a[0], a[1], a[2]);
            return null;
        }

        @Hidden
        static Object invoke_LLLL_V(MethodHandle mh, Object[] a) throws Throwable {
            assert (a.length == 4);
            mh.invokeBasic(a[0], a[1], a[2], a[3]);
            return null;
        }

        @Hidden
        static Object invoke_LLLLL_V(MethodHandle mh, Object[] a) throws Throwable {
            assert (a.length == 5);
            mh.invokeBasic(a[0], a[1], a[2], a[3], a[4]);
            return null;
        }

        @Hidden
        static Object invoke__L(MethodHandle mh, Object[] a) throws Throwable {
            assert (a.length == 0);
            return mh.invokeBasic();
        }

        @Hidden
        static Object invoke_L_L(MethodHandle mh, Object[] a) throws Throwable {
            assert (a.length == 1);
            return mh.invokeBasic(a[0]);
        }

        @Hidden
        static Object invoke_LL_L(MethodHandle mh, Object[] a) throws Throwable {
            assert (a.length == 2);
            return mh.invokeBasic(a[0], a[1]);
        }

        @Hidden
        static Object invoke_LLL_L(MethodHandle mh, Object[] a) throws Throwable {
            assert (a.length == 3);
            return mh.invokeBasic(a[0], a[1], a[2]);
        }

        @Hidden
        static Object invoke_LLLL_L(MethodHandle mh, Object[] a) throws Throwable {
            assert (a.length == 4);
            return mh.invokeBasic(a[0], a[1], a[2], a[3]);
        }

        @Hidden
        static Object invoke_LLLLL_L(MethodHandle mh, Object[] a) throws Throwable {
            assert (a.length == 5);
            return mh.invokeBasic(a[0], a[1], a[2], a[3], a[4]);
        }

        static final MethodType INVOKER_METHOD_TYPE = MethodType.methodType(Object.class, MethodHandle.class, Object[].class);

        private static MethodHandle computeInvoker(MethodTypeForm typeForm) {
            MethodHandle mh = typeForm.namedFunctionInvoker;
            if (mh != null)
                return mh;
            MemberName invoker = InvokerBytecodeGenerator.generateNamedFunctionInvoker(typeForm);
            mh = DirectMethodHandle.make(invoker);
            MethodHandle mh2 = typeForm.namedFunctionInvoker;
            if (mh2 != null)
                return mh2;
            if (!mh.type().equals(INVOKER_METHOD_TYPE))
                throw new InternalError(mh.debugString());
            return typeForm.namedFunctionInvoker = mh;
        }

        @Hidden
        Object invokeWithArguments(Object... arguments) throws Throwable {
            if (TRACE_INTERPRETER)
                return invokeWithArgumentsTracing(arguments);
            assert (checkArgumentTypes(arguments, methodType()));
            return invoker().invokeBasic(resolvedHandle(), arguments);
        }

        @Hidden
        Object invokeWithArgumentsTracing(Object[] arguments) throws Throwable {
            Object rval;
            try {
                traceInterpreter("[ call", this, arguments);
                if (invoker == null) {
                    traceInterpreter("| getInvoker", this);
                    invoker();
                }
                if (resolvedHandle == null) {
                    traceInterpreter("| resolve", this);
                    resolvedHandle();
                }
                assert (checkArgumentTypes(arguments, methodType()));
                rval = invoker().invokeBasic(resolvedHandle(), arguments);
            } catch (Throwable ex) {
                traceInterpreter("] throw =>", ex);
                throw ex;
            }
            traceInterpreter("] return =>", rval);
            return rval;
        }

        private MethodHandle invoker() {
            if (invoker != null)
                return invoker;
            return invoker = computeInvoker(methodType().form());
        }

        private static boolean checkArgumentTypes(Object[] arguments, MethodType methodType) {
            if (true)
                return true;
            MethodType dstType = methodType.form().erasedType();
            MethodType srcType = dstType.basicType().wrap();
            Class<?>[] ptypes = new Class<?>[arguments.length];
            for (int i = 0; i < arguments.length; i++) {
                Object arg = arguments[i];
                Class<?> ptype = arg == null ? Object.class : arg.getClass();
                ptypes[i] = dstType.parameterType(i).isPrimitive() ? ptype : Object.class;
            }
            MethodType argType = MethodType.methodType(srcType.returnType(), ptypes).wrap();
            assert (argType.isConvertibleTo(srcType)) : "wrong argument types: cannot convert " + argType + " to " + srcType;
            return true;
        }

        String basicTypeSignature() {
            return LambdaForm.basicTypeSignature(methodType());
        }

        MethodType methodType() {
            if (resolvedHandle != null)
                return resolvedHandle.type();
            else
                return member.getInvocationType();
        }

        MemberName member() {
            assert (assertMemberIsConsistent());
            return member;
        }

        private boolean assertMemberIsConsistent() {
            if (resolvedHandle instanceof DirectMethodHandle) {
                MemberName m = resolvedHandle.internalMemberName();
                assert (m.equals(member));
            }
            return true;
        }

        Class<?> memberDeclaringClassOrNull() {
            return (member == null) ? null : member.getDeclaringClass();
        }

        char returnType() {
            return basicType(methodType().returnType());
        }

        char parameterType(int n) {
            return basicType(methodType().parameterType(n));
        }

        int arity() {
            return methodType().parameterCount();
        }

        public String toString() {
            if (member == null)
                return String.valueOf(resolvedHandle);
            return member.getDeclaringClass().getSimpleName() + "." + member.getName();
        }
    }

    void resolve() {
        for (Name n : names) n.resolve();
    }

    public static char basicType(Class<?> type) {
        char c = Wrapper.basicTypeChar(type);
        if ("ZBSC".indexOf(c) >= 0)
            c = 'I';
        assert ("LIJFDV".indexOf(c) >= 0);
        return c;
    }

    public static char[] basicTypes(List<Class<?>> types) {
        char[] btypes = new char[types.size()];
        for (int i = 0; i < btypes.length; i++) {
            btypes[i] = basicType(types.get(i));
        }
        return btypes;
    }

    public static String basicTypeSignature(MethodType type) {
        char[] sig = new char[type.parameterCount() + 2];
        int sigp = 0;
        for (Class<?> pt : type.parameterList()) {
            sig[sigp++] = basicType(pt);
        }
        sig[sigp++] = '_';
        sig[sigp++] = basicType(type.returnType());
        assert (sigp == sig.length);
        return String.valueOf(sig);
    }

    static final class Name {

        final char type;

        private short index;

        final NamedFunction function;

        @Stable
        final Object[] arguments;

        private Name(int index, char type, NamedFunction function, Object[] arguments) {
            this.index = (short) index;
            this.type = type;
            this.function = function;
            this.arguments = arguments;
            assert (this.index == index);
        }

        Name(MethodHandle function, Object... arguments) {
            this(new NamedFunction(function), arguments);
        }

        Name(MethodType functionType, Object... arguments) {
            this(new NamedFunction(functionType), arguments);
            assert (arguments[0] instanceof Name && ((Name) arguments[0]).type == 'L');
        }

        Name(MemberName function, Object... arguments) {
            this(new NamedFunction(function), arguments);
        }

        Name(NamedFunction function, Object... arguments) {
            this(-1, function.returnType(), function, arguments = arguments.clone());
            assert (arguments.length == function.arity()) : "arity mismatch: arguments.length=" + arguments.length + " == function.arity()=" + function.arity() + " in " + debugString();
            for (int i = 0; i < arguments.length; i++) assert (typesMatch(function.parameterType(i), arguments[i])) : "types don't match: function.parameterType(" + i + ")=" + function.parameterType(i) + ", arguments[" + i + "]=" + arguments[i] + " in " + debugString();
        }

        Name(int index, char type) {
            this(index, type, null, null);
        }

        Name(char type) {
            this(-1, type);
        }

        char type() {
            return type;
        }

        int index() {
            return index;
        }

        boolean initIndex(int i) {
            if (index != i) {
                if (index != -1)
                    return false;
                index = (short) i;
            }
            return true;
        }

        void resolve() {
            if (function != null)
                function.resolve();
        }

        Name newIndex(int i) {
            if (initIndex(i))
                return this;
            return cloneWithIndex(i);
        }

        Name cloneWithIndex(int i) {
            Object[] newArguments = (arguments == null) ? null : arguments.clone();
            return new Name(i, type, function, newArguments);
        }

        Name replaceName(Name oldName, Name newName) {
            if (oldName == newName)
                return this;
            @SuppressWarnings("LocalVariableHidesMemberVariable")
            Object[] arguments = this.arguments;
            if (arguments == null)
                return this;
            boolean replaced = false;
            for (int j = 0; j < arguments.length; j++) {
                if (arguments[j] == oldName) {
                    if (!replaced) {
                        replaced = true;
                        arguments = arguments.clone();
                    }
                    arguments[j] = newName;
                }
            }
            if (!replaced)
                return this;
            return new Name(function, arguments);
        }

        Name replaceNames(Name[] oldNames, Name[] newNames, int start, int end) {
            @SuppressWarnings("LocalVariableHidesMemberVariable")
            Object[] arguments = this.arguments;
            boolean replaced = false;
            eachArg: for (int j = 0; j < arguments.length; j++) {
                if (arguments[j] instanceof Name) {
                    Name n = (Name) arguments[j];
                    int check = n.index;
                    if (check >= 0 && check < newNames.length && n == newNames[check])
                        continue eachArg;
                    for (int i = start; i < end; i++) {
                        if (n == oldNames[i]) {
                            if (n == newNames[i])
                                continue eachArg;
                            if (!replaced) {
                                replaced = true;
                                arguments = arguments.clone();
                            }
                            arguments[j] = newNames[i];
                            continue eachArg;
                        }
                    }
                }
            }
            if (!replaced)
                return this;
            return new Name(function, arguments);
        }

        void internArguments() {
            @SuppressWarnings("LocalVariableHidesMemberVariable")
            Object[] arguments = this.arguments;
            for (int j = 0; j < arguments.length; j++) {
                if (arguments[j] instanceof Name) {
                    Name n = (Name) arguments[j];
                    if (n.isParam() && n.index < INTERNED_ARGUMENT_LIMIT)
                        arguments[j] = internArgument(n);
                }
            }
        }

        boolean isParam() {
            return function == null;
        }

        boolean isConstantZero() {
            return !isParam() && arguments.length == 0 && function.equals(constantZero(0, type).function);
        }

        public String toString() {
            return (isParam() ? "a" : "t") + (index >= 0 ? index : System.identityHashCode(this)) + ":" + type;
        }

        public String debugString() {
            String s = toString();
            return (function == null) ? s : s + "=" + exprString();
        }

        public String exprString() {
            if (function == null)
                return "null";
            StringBuilder buf = new StringBuilder(function.toString());
            buf.append("(");
            String cma = "";
            for (Object a : arguments) {
                buf.append(cma);
                cma = ",";
                if (a instanceof Name || a instanceof Integer)
                    buf.append(a);
                else
                    buf.append("(").append(a).append(")");
            }
            buf.append(")");
            return buf.toString();
        }

        private static boolean typesMatch(char parameterType, Object object) {
            if (object instanceof Name) {
                return ((Name) object).type == parameterType;
            }
            switch(parameterType) {
                case 'I':
                    return object instanceof Integer;
                case 'J':
                    return object instanceof Long;
                case 'F':
                    return object instanceof Float;
                case 'D':
                    return object instanceof Double;
            }
            assert (parameterType == 'L');
            return true;
        }

        boolean isSiblingBindingBefore(Name binding) {
            assert (!binding.isParam());
            if (isParam())
                return true;
            if (function.equals(binding.function) && arguments.length == binding.arguments.length) {
                boolean sawInt = false;
                for (int i = 0; i < arguments.length; i++) {
                    Object a1 = arguments[i];
                    Object a2 = binding.arguments[i];
                    if (!a1.equals(a2)) {
                        if (a1 instanceof Integer && a2 instanceof Integer) {
                            if (sawInt)
                                continue;
                            sawInt = true;
                            if ((int) a1 < (int) a2)
                                continue;
                        }
                        return false;
                    }
                }
                return sawInt;
            }
            return false;
        }

        public boolean equals(Name that) {
            if (this == that)
                return true;
            if (isParam())
                return false;
            return this.type == that.type && this.function.equals(that.function) && Arrays.equals(this.arguments, that.arguments);
        }

        @Override
        public boolean equals(Object x) {
            return x instanceof Name && equals((Name) x);
        }

        @Override
        public int hashCode() {
            if (isParam())
                return index | (type << 8);
            return function.hashCode() ^ Arrays.hashCode(arguments);
        }
    }

    static Name argument(int which, char type) {
        int tn = ALL_TYPES.indexOf(type);
        if (tn < 0 || which >= INTERNED_ARGUMENT_LIMIT)
            return new Name(which, type);
        return INTERNED_ARGUMENTS[tn][which];
    }

    static Name internArgument(Name n) {
        assert (n.isParam()) : "not param: " + n;
        assert (n.index < INTERNED_ARGUMENT_LIMIT);
        return argument(n.index, n.type);
    }

    static Name[] arguments(int extra, String types) {
        int length = types.length();
        Name[] names = new Name[length + extra];
        for (int i = 0; i < length; i++) names[i] = argument(i, types.charAt(i));
        return names;
    }

    static Name[] arguments(int extra, char... types) {
        int length = types.length;
        Name[] names = new Name[length + extra];
        for (int i = 0; i < length; i++) names[i] = argument(i, types[i]);
        return names;
    }

    static Name[] arguments(int extra, List<Class<?>> types) {
        int length = types.size();
        Name[] names = new Name[length + extra];
        for (int i = 0; i < length; i++) names[i] = argument(i, basicType(types.get(i)));
        return names;
    }

    static Name[] arguments(int extra, Class<?>... types) {
        int length = types.length;
        Name[] names = new Name[length + extra];
        for (int i = 0; i < length; i++) names[i] = argument(i, basicType(types[i]));
        return names;
    }

    static Name[] arguments(int extra, MethodType types) {
        int length = types.parameterCount();
        Name[] names = new Name[length + extra];
        for (int i = 0; i < length; i++) names[i] = argument(i, basicType(types.parameterType(i)));
        return names;
    }

    static final String ALL_TYPES = "LIJFD";

    static final int INTERNED_ARGUMENT_LIMIT = 10;

    private static final Name[][] INTERNED_ARGUMENTS = new Name[ALL_TYPES.length()][INTERNED_ARGUMENT_LIMIT];

    static {
        for (int tn = 0; tn < ALL_TYPES.length(); tn++) {
            for (int i = 0; i < INTERNED_ARGUMENTS[tn].length; i++) {
                char type = ALL_TYPES.charAt(tn);
                INTERNED_ARGUMENTS[tn][i] = new Name(i, type);
            }
        }
    }

    private static final MemberName.Factory IMPL_NAMES = MemberName.getFactory();

    static Name constantZero(int which, char type) {
        return CONSTANT_ZERO[ALL_TYPES.indexOf(type)].newIndex(which);
    }

    private static final Name[] CONSTANT_ZERO = new Name[ALL_TYPES.length()];

    static {
        for (int tn = 0; tn < ALL_TYPES.length(); tn++) {
            char bt = ALL_TYPES.charAt(tn);
            Wrapper wrap = Wrapper.forBasicType(bt);
            MemberName zmem = new MemberName(LambdaForm.class, "zero" + bt, MethodType.methodType(wrap.primitiveType()), REF_invokeStatic);
            try {
                zmem = IMPL_NAMES.resolveOrFail(REF_invokeStatic, zmem, null, NoSuchMethodException.class);
            } catch (IllegalAccessException | NoSuchMethodException ex) {
                throw newInternalError(ex);
            }
            NamedFunction zcon = new NamedFunction(zmem);
            Name n = new Name(zcon).newIndex(0);
            assert (n.type == ALL_TYPES.charAt(tn));
            CONSTANT_ZERO[tn] = n;
            assert (n.isConstantZero());
        }
    }

    private static int zeroI() {
        return 0;
    }

    private static long zeroJ() {
        return 0;
    }

    private static float zeroF() {
        return 0;
    }

    private static double zeroD() {
        return 0;
    }

    private static Object zeroL() {
        return null;
    }

    static {
        if (USE_PREDEFINED_INTERPRET_METHODS)
            PREPARED_FORMS.putAll(computeInitialPreparedForms());
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Compiled {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Hidden {
    }

    static {
        NamedFunction.initializeInvokers();
    }

    private static final boolean TRACE_INTERPRETER = MethodHandleStatics.TRACE_INTERPRETER;
}
