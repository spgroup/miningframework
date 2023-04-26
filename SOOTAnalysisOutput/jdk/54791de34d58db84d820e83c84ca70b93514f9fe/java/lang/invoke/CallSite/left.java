package java.lang.invoke;

import sun.invoke.empty.Empty;
import static java.lang.invoke.MethodHandleStatics.*;
import static java.lang.invoke.MethodHandles.Lookup.IMPL_LOOKUP;

abstract public class CallSite {

    static {
        MethodHandleImpl.initStatics();
    }

    MethodHandle target;

    CallSite(MethodType type) {
        target = type.invokers().uninitializedCallSite();
    }

    CallSite(MethodHandle target) {
        target.type();
        this.target = target;
    }

    CallSite(MethodType targetType, MethodHandle createTargetHook) throws Throwable {
        this(targetType);
        ConstantCallSite selfCCS = (ConstantCallSite) this;
        MethodHandle boundTarget = (MethodHandle) createTargetHook.invokeWithArguments(selfCCS);
        checkTargetChange(this.target, boundTarget);
        this.target = boundTarget;
    }

    public MethodType type() {
        return target.type();
    }

    public abstract MethodHandle getTarget();

    public abstract void setTarget(MethodHandle newTarget);

    void checkTargetChange(MethodHandle oldTarget, MethodHandle newTarget) {
        MethodType oldType = oldTarget.type();
        MethodType newType = newTarget.type();
        if (!newType.equals(oldType))
            throw wrongTargetType(newTarget, oldType);
    }

    private static WrongMethodTypeException wrongTargetType(MethodHandle target, MethodType type) {
        return new WrongMethodTypeException(String.valueOf(target) + " should be of type " + type);
    }

    public abstract MethodHandle dynamicInvoker();

    MethodHandle makeDynamicInvoker() {
        MethodHandle getTarget = GET_TARGET.bindReceiver(this);
        MethodHandle invoker = MethodHandles.exactInvoker(this.type());
        return MethodHandles.foldArguments(invoker, getTarget);
    }

    private static final MethodHandle GET_TARGET;

    static {
        try {
            GET_TARGET = IMPL_LOOKUP.findVirtual(CallSite.class, "getTarget", MethodType.methodType(MethodHandle.class));
        } catch (ReflectiveOperationException e) {
            throw newInternalError(e);
        }
    }

    static Empty uninitializedCallSite() {
        throw new IllegalStateException("uninitialized call site");
    }

    private static final long TARGET_OFFSET;

    static {
        try {
            TARGET_OFFSET = UNSAFE.objectFieldOffset(CallSite.class.getDeclaredField("target"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    void setTargetNormal(MethodHandle newTarget) {
        MethodHandleNatives.setCallSiteTargetNormal(this, newTarget);
    }

    MethodHandle getTargetVolatile() {
        return (MethodHandle) UNSAFE.getObjectVolatile(this, TARGET_OFFSET);
    }

    void setTargetVolatile(MethodHandle newTarget) {
        MethodHandleNatives.setCallSiteTargetVolatile(this, newTarget);
    }

    static CallSite makeSite(MethodHandle bootstrapMethod, String name, MethodType type, Object info, Class<?> callerClass) {
        MethodHandles.Lookup caller = IMPL_LOOKUP.in(callerClass);
        CallSite site;
        try {
            Object binding;
            info = maybeReBox(info);
            if (info == null) {
                binding = bootstrapMethod.invoke(caller, name, type);
            } else if (!info.getClass().isArray()) {
                binding = bootstrapMethod.invoke(caller, name, type, info);
            } else {
                Object[] argv = (Object[]) info;
                maybeReBoxElements(argv);
                switch(argv.length) {
                    case 0:
                        binding = bootstrapMethod.invoke(caller, name, type);
                        break;
                    case 1:
                        binding = bootstrapMethod.invoke(caller, name, type, argv[0]);
                        break;
                    case 2:
                        binding = bootstrapMethod.invoke(caller, name, type, argv[0], argv[1]);
                        break;
                    case 3:
                        binding = bootstrapMethod.invoke(caller, name, type, argv[0], argv[1], argv[2]);
                        break;
                    case 4:
                        binding = bootstrapMethod.invoke(caller, name, type, argv[0], argv[1], argv[2], argv[3]);
                        break;
                    case 5:
                        binding = bootstrapMethod.invoke(caller, name, type, argv[0], argv[1], argv[2], argv[3], argv[4]);
                        break;
                    case 6:
                        binding = bootstrapMethod.invoke(caller, name, type, argv[0], argv[1], argv[2], argv[3], argv[4], argv[5]);
                        break;
                    default:
                        final int NON_SPREAD_ARG_COUNT = 3;
                        if (NON_SPREAD_ARG_COUNT + argv.length > MethodType.MAX_MH_ARITY)
                            throw new BootstrapMethodError("too many bootstrap method arguments");
                        MethodType bsmType = bootstrapMethod.type();
                        MethodType invocationType = MethodType.genericMethodType(NON_SPREAD_ARG_COUNT + argv.length);
                        MethodHandle typedBSM = bootstrapMethod.asType(invocationType);
                        MethodHandle spreader = invocationType.invokers().spreadInvoker(NON_SPREAD_ARG_COUNT);
                        binding = spreader.invokeExact(typedBSM, (Object) caller, (Object) name, (Object) type, argv);
                }
            }
            if (binding instanceof CallSite) {
                site = (CallSite) binding;
            } else {
                throw new ClassCastException("bootstrap method failed to produce a CallSite");
            }
            if (!site.getTarget().type().equals(type))
                throw new WrongMethodTypeException("wrong type: " + site.getTarget());
        } catch (Throwable ex) {
            BootstrapMethodError bex;
            if (ex instanceof BootstrapMethodError)
                bex = (BootstrapMethodError) ex;
            else
                bex = new BootstrapMethodError("call site initialization exception", ex);
            throw bex;
        }
        return site;
    }

    private static Object maybeReBox(Object x) {
        if (x instanceof Integer) {
            int xi = (int) x;
            if (xi == (byte) xi)
                x = xi;
        }
        return x;
    }

    private static void maybeReBoxElements(Object[] xa) {
        for (int i = 0; i < xa.length; i++) {
            xa[i] = maybeReBox(xa[i]);
        }
    }
}
