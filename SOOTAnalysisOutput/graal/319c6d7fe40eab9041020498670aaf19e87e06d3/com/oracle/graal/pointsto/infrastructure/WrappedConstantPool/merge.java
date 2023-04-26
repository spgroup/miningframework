package com.oracle.graal.pointsto.infrastructure;

import static jdk.vm.ci.common.JVMCIError.unimplemented;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.graalvm.compiler.debug.GraalError;
import com.oracle.graal.pointsto.constraints.UnresolvedElementException;
import com.oracle.graal.pointsto.util.AnalysisError.TypeNotFoundError;
import com.oracle.svm.util.ReflectionUtil;
import com.oracle.svm.util.ReflectionUtil.ReflectionUtilError;
import jdk.vm.ci.meta.ConstantPool;
import jdk.vm.ci.meta.JavaConstant;
import jdk.vm.ci.meta.JavaField;
import jdk.vm.ci.meta.JavaMethod;
import jdk.vm.ci.meta.JavaType;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import jdk.vm.ci.meta.ResolvedJavaType;

public class WrappedConstantPool implements ConstantPool, ConstantPoolPatch {

    private final Universe universe;

    protected final ConstantPool wrapped;

    private final WrappedJavaType defaultAccessingClass;

    public WrappedConstantPool(Universe universe, ConstantPool wrapped, WrappedJavaType defaultAccessingClass) {
        this.universe = universe;
        this.wrapped = wrapped;
        this.defaultAccessingClass = defaultAccessingClass;
    }

    @Override
    public int length() {
        return wrapped.length();
    }

    private static final Method hsLoadReferencedType;

    private static final Method hsLookupReferencedType;

    static {
        try {
            Class<?> hsConstantPool = Class.forName("jdk.vm.ci.hotspot.HotSpotConstantPool");
            hsLoadReferencedType = ReflectionUtil.lookupMethod(hsConstantPool, "loadReferencedType", int.class, int.class, boolean.class);
        } catch (ClassNotFoundException | ReflectionUtilError ex) {
            throw GraalError.shouldNotReachHere("JVMCI 0.47 or later, or JDK 11 is required for Substrate VM: could not find method HotSpotConstantPool.loadReferencedType");
        }
        Method lookupReferencedType = null;
        try {
            Class<?> hsConstantPool = Class.forName("jdk.vm.ci.hotspot.HotSpotConstantPool");
            lookupReferencedType = ReflectionUtil.lookupMethod(hsConstantPool, "lookupReferencedType", int.class, int.class);
        } catch (ClassNotFoundException | ReflectionUtilError ex) {
        }
        hsLookupReferencedType = lookupReferencedType;
    }

    public static void loadReferencedType(ConstantPool cp, int cpi, int opcode, boolean initialize) {
        ConstantPool root = cp;
        while (root instanceof WrappedConstantPool) {
            root = ((WrappedConstantPool) root).wrapped;
        }
        try {
            hsLoadReferencedType.invoke(root, cpi, opcode, initialize);
        } catch (Throwable ex) {
            Throwable cause = ex;
            if (ex instanceof InvocationTargetException && ex.getCause() != null) {
                cause = ex.getCause();
                if (cause instanceof BootstrapMethodError && cause.getCause() != null) {
                    cause = cause.getCause();
                }
            } else if (ex instanceof ExceptionInInitializerError && ex.getCause() != null) {
                cause = ex.getCause();
            }
            throw new UnresolvedElementException("Error loading a referenced type: " + cause.toString(), cause);
        }
    }

    @Override
    public void loadReferencedType(int cpi, int opcode) {
        loadReferencedType(wrapped, cpi, opcode, false);
    }

    @Override
    public JavaField lookupField(int cpi, ResolvedJavaMethod method, int opcode) {
        ResolvedJavaMethod substMethod = universe.resolveSubstitution(((WrappedJavaMethod) method).getWrapped());
        return universe.lookupAllowUnresolved(wrapped.lookupField(cpi, substMethod, opcode));
    }

    @Override
    public JavaMethod lookupMethod(int cpi, int opcode) {
        return universe.lookupAllowUnresolved(wrapped.lookupMethod(cpi, opcode));
    }

    public JavaMethod lookupMethodInWrapped(int cpi, int opcode) {
        if (wrapped instanceof WrappedConstantPool) {
            return ((WrappedConstantPool) wrapped).lookupMethodInWrapped(cpi, opcode);
        } else {
            return wrapped.lookupMethod(cpi, opcode);
        }
    }

    public JavaType lookupTypeInWrapped(int cpi, int opcode) {
        if (wrapped instanceof WrappedConstantPool) {
            return ((WrappedConstantPool) wrapped).lookupTypeInWrapped(cpi, opcode);
        } else {
            return wrapped.lookupType(cpi, opcode);
        }
    }

    public JavaField lookupFieldInWrapped(int cpi, ResolvedJavaMethod method, int opcode) {
        if (wrapped instanceof WrappedConstantPool) {
            return ((WrappedConstantPool) wrapped).lookupFieldInWrapped(cpi, method, opcode);
        } else {
            return wrapped.lookupField(cpi, method, opcode);
        }
    }

    @Override
    public JavaType lookupType(int cpi, int opcode) {
        try {
            return universe.lookupAllowUnresolved(wrapped.lookupType(cpi, opcode));
        } catch (TypeNotFoundError e) {
            return null;
        }
    }

    @Override
    public WrappedSignature lookupSignature(int cpi) {
        return universe.lookup(wrapped.lookupSignature(cpi), defaultAccessingClass);
    }

    @Override
    public JavaConstant lookupAppendix(int cpi, int opcode) {
        return universe.lookup(wrapped.lookupAppendix(cpi, opcode));
    }

    @Override
    public String lookupUtf8(int cpi) {
        return wrapped.lookupUtf8(cpi);
    }

    @Override
    public Object lookupConstant(int cpi) {
        Object con = wrapped.lookupConstant(cpi);
        if (con instanceof JavaType) {
            if (con instanceof ResolvedJavaType) {
                return universe.lookup((ResolvedJavaType) con);
            } else {
                return con;
            }
        } else if (con instanceof JavaConstant) {
            return universe.lookup((JavaConstant) con);
        } else {
            throw unimplemented();
        }
    }

    @Override
    public JavaType lookupReferencedType(int index, int opcode) {
        if (hsLookupReferencedType != null) {
            try {
                JavaType type = null;
                if (wrapped instanceof WrappedConstantPool) {
                    type = ((WrappedConstantPool) wrapped).lookupReferencedType(index, opcode);
                } else {
                    try {
                        type = (JavaType) hsLookupReferencedType.invoke(wrapped, index, opcode);
                    } catch (Throwable ex) {
                    }
                }
                if (type != null) {
                    return universe.lookupAllowUnresolved(type);
                }
            } catch (TypeNotFoundError e) {
            }
        }
        return null;
    }
}
