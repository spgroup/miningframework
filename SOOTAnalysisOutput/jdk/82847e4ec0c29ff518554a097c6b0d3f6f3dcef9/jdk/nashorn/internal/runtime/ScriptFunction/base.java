package jdk.nashorn.internal.runtime;

import static jdk.nashorn.internal.codegen.CompilerConstants.virtualCallNoLookup;
import static jdk.nashorn.internal.lookup.Lookup.MH;
import static jdk.nashorn.internal.runtime.ECMAErrors.typeError;
import static jdk.nashorn.internal.runtime.ScriptRuntime.UNDEFINED;
import static jdk.nashorn.internal.runtime.UnwarrantedOptimismException.INVALID_PROGRAM_POINT;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.SwitchPoint;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import jdk.dynalink.CallSiteDescriptor;
import jdk.dynalink.SecureLookupSupplier;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.support.Guards;
import jdk.nashorn.internal.codegen.ApplySpecialization;
import jdk.nashorn.internal.codegen.Compiler;
import jdk.nashorn.internal.codegen.CompilerConstants.Call;
import jdk.nashorn.internal.ir.FunctionNode;
import jdk.nashorn.internal.objects.Global;
import jdk.nashorn.internal.objects.NativeFunction;
import jdk.nashorn.internal.objects.annotations.SpecializedFunction.LinkLogic;
import jdk.nashorn.internal.runtime.linker.Bootstrap;
import jdk.nashorn.internal.runtime.linker.NashornCallSiteDescriptor;
import jdk.nashorn.internal.runtime.logging.DebugLogger;

public class ScriptFunction extends ScriptObject {

    public static final MethodHandle G$PROTOTYPE = findOwnMH_S("G$prototype", Object.class, Object.class);

    public static final MethodHandle S$PROTOTYPE = findOwnMH_S("S$prototype", void.class, Object.class, Object.class);

    public static final MethodHandle G$LENGTH = findOwnMH_S("G$length", int.class, Object.class);

    public static final MethodHandle G$NAME = findOwnMH_S("G$name", Object.class, Object.class);

    public static final MethodHandle INVOKE_SYNC = findOwnMH_S("invokeSync", Object.class, ScriptFunction.class, Object.class, Object.class, Object[].class);

    static final MethodHandle ALLOCATE = findOwnMH_V("allocate", Object.class);

    private static final MethodHandle WRAPFILTER = findOwnMH_S("wrapFilter", Object.class, Object.class);

    private static final MethodHandle SCRIPTFUNCTION_GLOBALFILTER = findOwnMH_S("globalFilter", Object.class, Object.class);

    public static final Call GET_SCOPE = virtualCallNoLookup(ScriptFunction.class, "getScope", ScriptObject.class);

    private static final MethodHandle IS_FUNCTION_MH = findOwnMH_S("isFunctionMH", boolean.class, Object.class, ScriptFunctionData.class);

    private static final MethodHandle IS_APPLY_FUNCTION = findOwnMH_S("isApplyFunction", boolean.class, boolean.class, Object.class, Object.class);

    private static final MethodHandle IS_NONSTRICT_FUNCTION = findOwnMH_S("isNonStrictFunction", boolean.class, Object.class, Object.class, ScriptFunctionData.class);

    private static final MethodHandle ADD_ZEROTH_ELEMENT = findOwnMH_S("addZerothElement", Object[].class, Object[].class, Object.class);

    private static final MethodHandle WRAP_THIS = MH.findStatic(MethodHandles.lookup(), ScriptFunctionData.class, "wrapThis", MH.type(Object.class, Object.class));

    private static final PropertyMap anonmap$;

    private static final PropertyMap strictmodemap$;

    private static final PropertyMap boundfunctionmap$;

    private static final PropertyMap map$;

    private static final Object LAZY_PROTOTYPE = new Object();

    private static final AccessControlContext GET_LOOKUP_PERMISSION_CONTEXT = AccessControlContextFactory.createAccessControlContext(SecureLookupSupplier.GET_LOOKUP_PERMISSION_NAME);

    private static PropertyMap createStrictModeMap(final PropertyMap map) {
        final int flags = Property.NOT_ENUMERABLE | Property.NOT_CONFIGURABLE;
        PropertyMap newMap = map;
        newMap = newMap.addPropertyNoHistory(map.newUserAccessors("arguments", flags));
        newMap = newMap.addPropertyNoHistory(map.newUserAccessors("caller", flags));
        return newMap;
    }

    private static PropertyMap createBoundFunctionMap(final PropertyMap strictModeMap) {
        return strictModeMap.deleteProperty(strictModeMap.findProperty("prototype"));
    }

    static {
        anonmap$ = PropertyMap.newMap();
        final ArrayList<Property> properties = new ArrayList<>(3);
        properties.add(AccessorProperty.create("prototype", Property.NOT_ENUMERABLE | Property.NOT_CONFIGURABLE, G$PROTOTYPE, S$PROTOTYPE));
        properties.add(AccessorProperty.create("length", Property.NOT_ENUMERABLE | Property.NOT_CONFIGURABLE | Property.NOT_WRITABLE, G$LENGTH, null));
        properties.add(AccessorProperty.create("name", Property.NOT_ENUMERABLE | Property.NOT_CONFIGURABLE | Property.NOT_WRITABLE, G$NAME, null));
        map$ = PropertyMap.newMap(properties);
        strictmodemap$ = createStrictModeMap(map$);
        boundfunctionmap$ = createBoundFunctionMap(strictmodemap$);
    }

    private static boolean isStrict(final int flags) {
        return (flags & ScriptFunctionData.IS_STRICT) != 0;
    }

    private static PropertyMap getMap(final boolean strict) {
        return strict ? strictmodemap$ : map$;
    }

    private final ScriptObject scope;

    private final ScriptFunctionData data;

    protected PropertyMap allocatorMap;

    protected Object prototype;

    private ScriptFunction(final ScriptFunctionData data, final PropertyMap map, final ScriptObject scope, final Global global) {
        super(map);
        if (Context.DEBUG) {
            constructorCount.increment();
        }
        this.data = data;
        this.scope = scope;
        this.setInitialProto(global.getFunctionPrototype());
        this.prototype = LAZY_PROTOTYPE;
        assert objectSpill == null;
        if (isStrict() || isBoundFunction()) {
            final ScriptFunction typeErrorThrower = global.getTypeErrorThrower();
            initUserAccessors("arguments", Property.NOT_CONFIGURABLE | Property.NOT_ENUMERABLE, typeErrorThrower, typeErrorThrower);
            initUserAccessors("caller", Property.NOT_CONFIGURABLE | Property.NOT_ENUMERABLE, typeErrorThrower, typeErrorThrower);
        }
    }

    private ScriptFunction(final String name, final MethodHandle methodHandle, final PropertyMap map, final ScriptObject scope, final Specialization[] specs, final int flags, final Global global) {
        this(new FinalScriptFunctionData(name, methodHandle, specs, flags), map, scope, global);
    }

    private ScriptFunction(final String name, final MethodHandle methodHandle, final ScriptObject scope, final Specialization[] specs, final int flags) {
        this(name, methodHandle, getMap(isStrict(flags)), scope, specs, flags, Global.instance());
    }

    protected ScriptFunction(final String name, final MethodHandle invokeHandle, final Specialization[] specs) {
        this(name, invokeHandle, map$, null, specs, ScriptFunctionData.IS_BUILTIN_CONSTRUCTOR, Global.instance());
    }

    protected ScriptFunction(final String name, final MethodHandle invokeHandle, final PropertyMap map, final Specialization[] specs) {
        this(name, invokeHandle, map.addAll(map$), null, specs, ScriptFunctionData.IS_BUILTIN_CONSTRUCTOR, Global.instance());
    }

    public static ScriptFunction create(final Object[] constants, final int index, final ScriptObject scope) {
        final RecompilableScriptFunctionData data = (RecompilableScriptFunctionData) constants[index];
        return new ScriptFunction(data, getMap(data.isStrict()), scope, Global.instance());
    }

    public static ScriptFunction create(final Object[] constants, final int index) {
        return create(constants, index, null);
    }

    public static ScriptFunction createAnonymous() {
        return new ScriptFunction("", GlobalFunctions.ANONYMOUS, anonmap$, null);
    }

    private static ScriptFunction createBuiltin(final String name, final MethodHandle methodHandle, final Specialization[] specs, final int flags) {
        final ScriptFunction func = new ScriptFunction(name, methodHandle, null, specs, flags);
        func.setPrototype(UNDEFINED);
        func.deleteOwnProperty(func.getMap().findProperty("prototype"));
        return func;
    }

    public static ScriptFunction createBuiltin(final String name, final MethodHandle methodHandle, final Specialization[] specs) {
        return ScriptFunction.createBuiltin(name, methodHandle, specs, ScriptFunctionData.IS_BUILTIN);
    }

    public static ScriptFunction createBuiltin(final String name, final MethodHandle methodHandle) {
        return ScriptFunction.createBuiltin(name, methodHandle, null);
    }

    public static ScriptFunction createStrictBuiltin(final String name, final MethodHandle methodHandle) {
        return ScriptFunction.createBuiltin(name, methodHandle, null, ScriptFunctionData.IS_BUILTIN | ScriptFunctionData.IS_STRICT);
    }

    private static class Bound extends ScriptFunction {

        private final ScriptFunction target;

        Bound(final ScriptFunctionData boundData, final ScriptFunction target) {
            super(boundData, boundfunctionmap$, null, Global.instance());
            setPrototype(ScriptRuntime.UNDEFINED);
            this.target = target;
        }

        @Override
        protected ScriptFunction getTargetFunction() {
            return target;
        }
    }

    public final ScriptFunction createBound(final Object self, final Object[] args) {
        return new Bound(data.makeBoundFunctionData(this, self, args), getTargetFunction());
    }

    public final ScriptFunction createSynchronized(final Object sync) {
        final MethodHandle mh = MH.insertArguments(ScriptFunction.INVOKE_SYNC, 0, this, sync);
        return createBuiltin(getName(), mh);
    }

    @Override
    public String getClassName() {
        return "Function";
    }

    @Override
    public boolean isInstance(final ScriptObject instance) {
        final Object basePrototype = getTargetFunction().getPrototype();
        if (!(basePrototype instanceof ScriptObject)) {
            throw typeError("prototype.not.an.object", ScriptRuntime.safeToString(getTargetFunction()), ScriptRuntime.safeToString(basePrototype));
        }
        for (ScriptObject proto = instance.getProto(); proto != null; proto = proto.getProto()) {
            if (proto == basePrototype) {
                return true;
            }
        }
        return false;
    }

    protected ScriptFunction getTargetFunction() {
        return this;
    }

    final boolean isBoundFunction() {
        return getTargetFunction() != this;
    }

    public final void setArity(final int arity) {
        data.setArity(arity);
    }

    public final boolean isStrict() {
        return data.isStrict();
    }

    public boolean hasAllVarsInScope() {
        return data instanceof RecompilableScriptFunctionData && (((RecompilableScriptFunctionData) data).getFunctionFlags() & FunctionNode.HAS_ALL_VARS_IN_SCOPE) != 0;
    }

    public final boolean needsWrappedThis() {
        return data.needsWrappedThis();
    }

    private static boolean needsWrappedThis(final Object fn) {
        return fn instanceof ScriptFunction ? ((ScriptFunction) fn).needsWrappedThis() : false;
    }

    final Object invoke(final Object self, final Object... arguments) throws Throwable {
        if (Context.DEBUG) {
            invokes.increment();
        }
        return data.invoke(this, self, arguments);
    }

    final Object construct(final Object... arguments) throws Throwable {
        return data.construct(this, arguments);
    }

    @SuppressWarnings("unused")
    private Object allocate() {
        if (Context.DEBUG) {
            allocations.increment();
        }
        assert !isBoundFunction();
        final ScriptObject prototype = getAllocatorPrototype();
        final ScriptObject object = data.allocate(getAllocatorMap(prototype));
        if (object != null) {
            object.setInitialProto(prototype);
        }
        return object;
    }

    private PropertyMap getAllocatorMap(final ScriptObject prototype) {
        if (allocatorMap == null || allocatorMap.isInvalidSharedMapFor(prototype)) {
            allocatorMap = data.getAllocatorMap(prototype);
        }
        return allocatorMap;
    }

    private ScriptObject getAllocatorPrototype() {
        final Object prototype = getPrototype();
        if (prototype instanceof ScriptObject) {
            return (ScriptObject) prototype;
        }
        return Global.objectPrototype();
    }

    @Override
    public final String safeToString() {
        return toSource();
    }

    @Override
    public final String toString() {
        return data.toString();
    }

    public final String toSource() {
        return data.toSource();
    }

    public final Object getPrototype() {
        if (prototype == LAZY_PROTOTYPE) {
            prototype = new PrototypeObject(this);
        }
        return prototype;
    }

    public final void setPrototype(final Object newPrototype) {
        if (newPrototype instanceof ScriptObject && newPrototype != this.prototype && allocatorMap != null) {
            allocatorMap = null;
        }
        this.prototype = newPrototype;
    }

    public final MethodHandle getBoundInvokeHandle(final Object self) {
        return MH.bindTo(bindToCalleeIfNeeded(data.getGenericInvoker(scope)), self);
    }

    private MethodHandle bindToCalleeIfNeeded(final MethodHandle methodHandle) {
        return ScriptFunctionData.needsCallee(methodHandle) ? MH.bindTo(methodHandle, this) : methodHandle;
    }

    public final String getDocumentation() {
        return data.getDocumentation();
    }

    public final String getDocumentationKey() {
        return data.getDocumentationKey();
    }

    public final void setDocumentationKey(final String docKey) {
        data.setDocumentationKey(docKey);
    }

    public final String getName() {
        return data.getName();
    }

    public final ScriptObject getScope() {
        return scope;
    }

    public static Object G$prototype(final Object self) {
        return self instanceof ScriptFunction ? ((ScriptFunction) self).getPrototype() : UNDEFINED;
    }

    public static void S$prototype(final Object self, final Object prototype) {
        if (self instanceof ScriptFunction) {
            ((ScriptFunction) self).setPrototype(prototype);
        }
    }

    public static int G$length(final Object self) {
        if (self instanceof ScriptFunction) {
            return ((ScriptFunction) self).data.getArity();
        }
        return 0;
    }

    public static Object G$name(final Object self) {
        if (self instanceof ScriptFunction) {
            return ((ScriptFunction) self).getName();
        }
        return UNDEFINED;
    }

    public static ScriptObject getPrototype(final ScriptFunction constructor) {
        if (constructor != null) {
            final Object proto = constructor.getPrototype();
            if (proto instanceof ScriptObject) {
                return (ScriptObject) proto;
            }
        }
        return null;
    }

    private static LongAdder constructorCount;

    private static LongAdder invokes;

    private static LongAdder allocations;

    static {
        if (Context.DEBUG) {
            constructorCount = new LongAdder();
            invokes = new LongAdder();
            allocations = new LongAdder();
        }
    }

    public static long getConstructorCount() {
        return constructorCount.longValue();
    }

    public static long getInvokes() {
        return invokes.longValue();
    }

    public static long getAllocations() {
        return allocations.longValue();
    }

    @Override
    protected GuardedInvocation findNewMethod(final CallSiteDescriptor desc, final LinkRequest request) {
        final MethodType type = desc.getMethodType();
        assert desc.getMethodType().returnType() == Object.class && !NashornCallSiteDescriptor.isOptimistic(desc);
        final CompiledFunction cf = data.getBestConstructor(type, scope, CompiledFunction.NO_FUNCTIONS);
        final GuardedInvocation bestCtorInv = cf.createConstructorInvocation();
        return new GuardedInvocation(pairArguments(bestCtorInv.getInvocation(), type), getFunctionGuard(this, cf.getFlags()), bestCtorInv.getSwitchPoints(), null);
    }

    private static Object wrapFilter(final Object obj) {
        if (obj instanceof ScriptObject || !ScriptFunctionData.isPrimitiveThis(obj)) {
            return obj;
        }
        return Context.getGlobal().wrapAsObject(obj);
    }

    @SuppressWarnings("unused")
    private static Object globalFilter(final Object object) {
        return Context.getGlobal();
    }

    private static LinkLogic getLinkLogic(final Object self, final Class<? extends LinkLogic> linkLogicClass) {
        if (linkLogicClass == null) {
            return LinkLogic.EMPTY_INSTANCE;
        }
        if (!Context.getContextTrusted().getEnv()._optimistic_types) {
            return null;
        }
        final Object wrappedSelf = wrapFilter(self);
        if (wrappedSelf instanceof OptimisticBuiltins) {
            if (wrappedSelf != self && ((OptimisticBuiltins) wrappedSelf).hasPerInstanceAssumptions()) {
                return null;
            }
            return ((OptimisticBuiltins) wrappedSelf).getLinkLogic(linkLogicClass);
        }
        return null;
    }

    @Override
    protected GuardedInvocation findCallMethod(final CallSiteDescriptor desc, final LinkRequest request) {
        final MethodType type = desc.getMethodType();
        final String name = getName();
        final boolean isUnstable = request.isCallSiteUnstable();
        final boolean scopeCall = NashornCallSiteDescriptor.isScope(desc);
        final boolean isCall = !scopeCall && data.isBuiltin() && "call".equals(name);
        final boolean isApply = !scopeCall && data.isBuiltin() && "apply".equals(name);
        final boolean isApplyOrCall = isCall | isApply;
        if (isUnstable && !isApplyOrCall) {
            final MethodHandle handle;
            if (type.parameterCount() == 3 && type.parameterType(2) == Object[].class) {
                handle = ScriptRuntime.APPLY.methodHandle();
            } else {
                handle = MH.asCollector(ScriptRuntime.APPLY.methodHandle(), Object[].class, type.parameterCount() - 2);
            }
            return new GuardedInvocation(handle, null, (SwitchPoint) null, ClassCastException.class);
        }
        MethodHandle boundHandle;
        MethodHandle guard = null;
        if (isApplyOrCall && !isUnstable) {
            final Object[] args = request.getArguments();
            if (Bootstrap.isCallable(args[1])) {
                return createApplyOrCallCall(isApply, desc, request, args);
            }
        }
        int programPoint = INVALID_PROGRAM_POINT;
        if (NashornCallSiteDescriptor.isOptimistic(desc)) {
            programPoint = NashornCallSiteDescriptor.getProgramPoint(desc);
        }
        CompiledFunction cf = data.getBestInvoker(type, scope, CompiledFunction.NO_FUNCTIONS);
        final Object self = request.getArguments()[1];
        final Collection<CompiledFunction> forbidden = new HashSet<>();
        final List<SwitchPoint> sps = new ArrayList<>();
        Class<? extends Throwable> exceptionGuard = null;
        while (cf.isSpecialization()) {
            final Class<? extends LinkLogic> linkLogicClass = cf.getLinkLogicClass();
            final LinkLogic linkLogic = getLinkLogic(self, linkLogicClass);
            if (linkLogic != null && linkLogic.checkLinkable(self, desc, request)) {
                final DebugLogger log = Context.getContextTrusted().getLogger(Compiler.class);
                if (log.isEnabled()) {
                    log.info("Linking optimistic builtin function: '", name, "' args=", Arrays.toString(request.getArguments()), " desc=", desc);
                }
                exceptionGuard = linkLogic.getRelinkException();
                break;
            }
            forbidden.add(cf);
            final CompiledFunction oldCf = cf;
            cf = data.getBestInvoker(type, scope, forbidden);
            assert oldCf != cf;
        }
        final GuardedInvocation bestInvoker = cf.createFunctionInvocation(type.returnType(), programPoint);
        final MethodHandle callHandle = bestInvoker.getInvocation();
        if (data.needsCallee()) {
            if (scopeCall && needsWrappedThis()) {
                boundHandle = MH.filterArguments(callHandle, 1, SCRIPTFUNCTION_GLOBALFILTER);
            } else {
                boundHandle = callHandle;
            }
        } else if (data.isBuiltin() && Global.isBuiltInJavaExtend(this)) {
            boundHandle = MH.dropArguments(MH.bindTo(callHandle, getLookupPrivileged(desc)), 0, type.parameterType(0), type.parameterType(1));
        } else if (data.isBuiltin() && Global.isBuiltInJavaTo(this)) {
            boundHandle = MH.dropArguments(MH.bindTo(callHandle, desc), 0, type.parameterType(0), type.parameterType(1));
        } else if (scopeCall && needsWrappedThis()) {
            boundHandle = MH.filterArguments(callHandle, 0, SCRIPTFUNCTION_GLOBALFILTER);
            boundHandle = MH.dropArguments(boundHandle, 0, type.parameterType(0));
        } else {
            boundHandle = MH.dropArguments(callHandle, 0, type.parameterType(0));
        }
        if (!scopeCall && needsWrappedThis()) {
            if (ScriptFunctionData.isPrimitiveThis(request.getArguments()[1])) {
                boundHandle = MH.filterArguments(boundHandle, 1, WRAPFILTER);
            } else {
                guard = getNonStrictFunctionGuard(this);
            }
        }
        if (isUnstable && NashornCallSiteDescriptor.isApplyToCall(desc)) {
            boundHandle = MH.asCollector(boundHandle, Object[].class, type.parameterCount() - 2);
        }
        boundHandle = pairArguments(boundHandle, type);
        if (bestInvoker.getSwitchPoints() != null) {
            sps.addAll(Arrays.asList(bestInvoker.getSwitchPoints()));
        }
        final SwitchPoint[] spsArray = sps.isEmpty() ? null : sps.toArray(new SwitchPoint[0]);
        return new GuardedInvocation(boundHandle, guard == null ? getFunctionGuard(this, cf.getFlags()) : guard, spsArray, exceptionGuard);
    }

    private static Lookup getLookupPrivileged(final CallSiteDescriptor desc) {
        return AccessController.doPrivileged((PrivilegedAction<Lookup>) () -> desc.getLookup(), GET_LOOKUP_PERMISSION_CONTEXT);
    }

    private GuardedInvocation createApplyOrCallCall(final boolean isApply, final CallSiteDescriptor desc, final LinkRequest request, final Object[] args) {
        final MethodType descType = desc.getMethodType();
        final int paramCount = descType.parameterCount();
        if (descType.parameterType(paramCount - 1).isArray()) {
            return createVarArgApplyOrCallCall(isApply, desc, request, args);
        }
        final boolean passesThis = paramCount > 2;
        final boolean passesArgs = paramCount > 3;
        final int realArgCount = passesArgs ? paramCount - 3 : 0;
        final Object appliedFn = args[1];
        final boolean appliedFnNeedsWrappedThis = needsWrappedThis(appliedFn);
        CallSiteDescriptor appliedDesc = desc;
        final SwitchPoint applyToCallSwitchPoint = Global.getBuiltinFunctionApplySwitchPoint();
        final boolean isApplyToCall = NashornCallSiteDescriptor.isApplyToCall(desc);
        final boolean isFailedApplyToCall = isApplyToCall && applyToCallSwitchPoint.hasBeenInvalidated();
        MethodType appliedType = descType.dropParameterTypes(0, 1);
        if (!passesThis) {
            appliedType = appliedType.insertParameterTypes(1, Object.class);
        } else if (appliedFnNeedsWrappedThis) {
            appliedType = appliedType.changeParameterType(1, Object.class);
        }
        MethodType dropArgs = MH.type(void.class);
        if (isApply && !isFailedApplyToCall) {
            final int pc = appliedType.parameterCount();
            for (int i = 3; i < pc; i++) {
                dropArgs = dropArgs.appendParameterTypes(appliedType.parameterType(i));
            }
            if (pc > 3) {
                appliedType = appliedType.dropParameterTypes(3, pc);
            }
        }
        if (isApply || isFailedApplyToCall) {
            if (passesArgs) {
                appliedType = appliedType.changeParameterType(2, Object[].class);
                if (isFailedApplyToCall) {
                    appliedType = appliedType.dropParameterTypes(3, paramCount - 1);
                }
            } else {
                appliedType = appliedType.insertParameterTypes(2, Object[].class);
            }
        }
        appliedDesc = appliedDesc.changeMethodType(appliedType);
        final Object[] appliedArgs = new Object[isApply ? 3 : appliedType.parameterCount()];
        appliedArgs[0] = appliedFn;
        appliedArgs[1] = passesThis ? appliedFnNeedsWrappedThis ? ScriptFunctionData.wrapThis(args[2]) : args[2] : ScriptRuntime.UNDEFINED;
        if (isApply && !isFailedApplyToCall) {
            appliedArgs[2] = passesArgs ? NativeFunction.toApplyArgs(args[3]) : ScriptRuntime.EMPTY_ARRAY;
        } else {
            if (passesArgs) {
                if (isFailedApplyToCall) {
                    final Object[] tmp = new Object[args.length - 3];
                    System.arraycopy(args, 3, tmp, 0, tmp.length);
                    appliedArgs[2] = NativeFunction.toApplyArgs(tmp);
                } else {
                    assert !isApply;
                    System.arraycopy(args, 3, appliedArgs, 2, args.length - 3);
                }
            } else if (isFailedApplyToCall) {
                appliedArgs[2] = ScriptRuntime.EMPTY_ARRAY;
            }
        }
        final LinkRequest appliedRequest = request.replaceArguments(appliedDesc, appliedArgs);
        GuardedInvocation appliedInvocation;
        try {
            appliedInvocation = Bootstrap.getLinkerServices().getGuardedInvocation(appliedRequest);
        } catch (final RuntimeException | Error e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        assert appliedRequest != null;
        final Class<?> applyFnType = descType.parameterType(0);
        MethodHandle inv = appliedInvocation.getInvocation();
        MethodHandle guard = appliedInvocation.getGuard();
        if (isApply && !isFailedApplyToCall) {
            if (passesArgs) {
                inv = MH.filterArguments(inv, 2, NativeFunction.TO_APPLY_ARGS);
                if (guard.type().parameterCount() > 2) {
                    guard = MH.filterArguments(guard, 2, NativeFunction.TO_APPLY_ARGS);
                }
            } else {
                inv = MH.insertArguments(inv, 2, (Object) ScriptRuntime.EMPTY_ARRAY);
            }
        }
        if (isApplyToCall) {
            if (isFailedApplyToCall) {
                Context.getContextTrusted().getLogger(ApplySpecialization.class).info("Collection arguments to revert call to apply in " + appliedFn);
                inv = MH.asCollector(inv, Object[].class, realArgCount);
            } else {
                appliedInvocation = appliedInvocation.addSwitchPoint(applyToCallSwitchPoint);
            }
        }
        if (!passesThis) {
            inv = bindImplicitThis(appliedFnNeedsWrappedThis, inv);
            if (guard.type().parameterCount() > 1) {
                guard = bindImplicitThis(appliedFnNeedsWrappedThis, guard);
            }
        } else if (appliedFnNeedsWrappedThis) {
            inv = MH.filterArguments(inv, 1, WRAP_THIS);
            if (guard.type().parameterCount() > 1) {
                guard = MH.filterArguments(guard, 1, WRAP_THIS);
            }
        }
        final MethodType guardType = guard.type();
        inv = MH.dropArguments(inv, 0, applyFnType);
        guard = MH.dropArguments(guard, 0, applyFnType);
        for (int i = 0; i < dropArgs.parameterCount(); i++) {
            inv = MH.dropArguments(inv, 4 + i, dropArgs.parameterType(i));
        }
        MethodHandle applyFnGuard = MH.insertArguments(IS_APPLY_FUNCTION, 2, this);
        applyFnGuard = MH.dropArguments(applyFnGuard, 2, guardType.parameterArray());
        guard = MH.foldArguments(applyFnGuard, guard);
        return appliedInvocation.replaceMethods(inv, guard);
    }

    private GuardedInvocation createVarArgApplyOrCallCall(final boolean isApply, final CallSiteDescriptor desc, final LinkRequest request, final Object[] args) {
        final MethodType descType = desc.getMethodType();
        final int paramCount = descType.parameterCount();
        final Object[] varArgs = (Object[]) args[paramCount - 1];
        final int copiedArgCount = args.length - 1;
        final int varArgCount = varArgs.length;
        final Object[] spreadArgs = new Object[copiedArgCount + varArgCount];
        System.arraycopy(args, 0, spreadArgs, 0, copiedArgCount);
        System.arraycopy(varArgs, 0, spreadArgs, copiedArgCount, varArgCount);
        final MethodType spreadType = descType.dropParameterTypes(paramCount - 1, paramCount).appendParameterTypes(Collections.<Class<?>>nCopies(varArgCount, Object.class));
        final CallSiteDescriptor spreadDesc = desc.changeMethodType(spreadType);
        final LinkRequest spreadRequest = request.replaceArguments(spreadDesc, spreadArgs);
        final GuardedInvocation spreadInvocation = createApplyOrCallCall(isApply, spreadDesc, spreadRequest, spreadArgs);
        return spreadInvocation.replaceMethods(pairArguments(spreadInvocation.getInvocation(), descType), spreadGuardArguments(spreadInvocation.getGuard(), descType));
    }

    private static MethodHandle spreadGuardArguments(final MethodHandle guard, final MethodType descType) {
        final MethodType guardType = guard.type();
        final int guardParamCount = guardType.parameterCount();
        final int descParamCount = descType.parameterCount();
        final int spreadCount = guardParamCount - descParamCount + 1;
        if (spreadCount <= 0) {
            return guard;
        }
        final MethodHandle arrayConvertingGuard;
        if (guardType.parameterType(guardParamCount - 1).isArray()) {
            arrayConvertingGuard = MH.filterArguments(guard, guardParamCount - 1, NativeFunction.TO_APPLY_ARGS);
        } else {
            arrayConvertingGuard = guard;
        }
        return ScriptObject.adaptHandleToVarArgCallSite(arrayConvertingGuard, descParamCount);
    }

    private static MethodHandle bindImplicitThis(final boolean needsWrappedThis, final MethodHandle mh) {
        final MethodHandle bound;
        if (needsWrappedThis) {
            bound = MH.filterArguments(mh, 1, SCRIPTFUNCTION_GLOBALFILTER);
        } else {
            bound = mh;
        }
        return MH.insertArguments(bound, 1, ScriptRuntime.UNDEFINED);
    }

    MethodHandle getCallMethodHandle(final MethodType type, final String bindName) {
        return pairArguments(bindToNameIfNeeded(bindToCalleeIfNeeded(data.getGenericInvoker(scope)), bindName), type);
    }

    private static MethodHandle bindToNameIfNeeded(final MethodHandle methodHandle, final String bindName) {
        if (bindName == null) {
            return methodHandle;
        }
        final MethodType methodType = methodHandle.type();
        final int parameterCount = methodType.parameterCount();
        if (parameterCount < 2) {
            return methodHandle;
        }
        final boolean isVarArg = methodType.parameterType(parameterCount - 1).isArray();
        if (isVarArg) {
            return MH.filterArguments(methodHandle, 1, MH.insertArguments(ADD_ZEROTH_ELEMENT, 1, bindName));
        }
        return MH.insertArguments(methodHandle, 1, bindName);
    }

    private static MethodHandle getFunctionGuard(final ScriptFunction function, final int flags) {
        assert function.data != null;
        if (function.data.isBuiltin()) {
            return Guards.getIdentityGuard(function);
        }
        return MH.insertArguments(IS_FUNCTION_MH, 1, function.data);
    }

    private static MethodHandle getNonStrictFunctionGuard(final ScriptFunction function) {
        assert function.data != null;
        return MH.insertArguments(IS_NONSTRICT_FUNCTION, 2, function.data);
    }

    @SuppressWarnings("unused")
    private static boolean isFunctionMH(final Object self, final ScriptFunctionData data) {
        return self instanceof ScriptFunction && ((ScriptFunction) self).data == data;
    }

    @SuppressWarnings("unused")
    private static boolean isNonStrictFunction(final Object self, final Object arg, final ScriptFunctionData data) {
        return self instanceof ScriptFunction && ((ScriptFunction) self).data == data && arg instanceof ScriptObject;
    }

    @SuppressWarnings("unused")
    private static boolean isApplyFunction(final boolean appliedFnCondition, final Object self, final Object expectedSelf) {
        return appliedFnCondition && self == expectedSelf;
    }

    @SuppressWarnings("unused")
    private static Object[] addZerothElement(final Object[] args, final Object value) {
        final Object[] src = args == null ? ScriptRuntime.EMPTY_ARRAY : args;
        final Object[] result = new Object[src.length + 1];
        System.arraycopy(src, 0, result, 1, src.length);
        result[0] = value;
        return result;
    }

    @SuppressWarnings("unused")
    private static Object invokeSync(final ScriptFunction func, final Object sync, final Object self, final Object... args) throws Throwable {
        final Object syncObj = sync == UNDEFINED ? self : sync;
        synchronized (syncObj) {
            return func.invoke(self, args);
        }
    }

    private static MethodHandle findOwnMH_S(final String name, final Class<?> rtype, final Class<?>... types) {
        return MH.findStatic(MethodHandles.lookup(), ScriptFunction.class, name, MH.type(rtype, types));
    }

    private static MethodHandle findOwnMH_V(final String name, final Class<?> rtype, final Class<?>... types) {
        return MH.findVirtual(MethodHandles.lookup(), ScriptFunction.class, name, MH.type(rtype, types));
    }
}
