package org.graalvm.compiler.test;

import java.lang.reflect.Method;

public class JLRModule {

    static {
        if (GraalTest.Java8OrEarlier) {
            throw new AssertionError("Use of " + JLRModule.class + " only allowed if " + GraalTest.class.getName() + ".JDK8OrEarlier is false");
        }
    }

    private final Object realModule;

    public JLRModule(Object module) {
        this.realModule = module;
    }

    private static final Class<?> moduleClass;

    private static final Method getModuleMethod;

    private static final Method getUnnamedModuleMethod;

    private static final Method getPackagesMethod;

    private static final Method isExportedMethod;

    private static final Method isExported2Method;

    private static final Method addExportsMethod;

    static {
        try {
            moduleClass = Class.forName("java.lang.reflect.Module");
            getModuleMethod = Class.class.getMethod("getModule");
            getUnnamedModuleMethod = ClassLoader.class.getMethod("getUnnamedModule");
            getPackagesMethod = moduleClass.getMethod("getPackages");
            isExportedMethod = moduleClass.getMethod("isExported", String.class);
            isExported2Method = moduleClass.getMethod("isExported", String.class, moduleClass);
            addExportsMethod = moduleClass.getMethod("addExports", String.class, moduleClass);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static JLRModule fromClass(Class<?> cls) {
        try {
            return new JLRModule(getModuleMethod.invoke(cls));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static JLRModule getUnnamedModuleFor(ClassLoader cl) {
        try {
            return new JLRModule(getUnnamedModuleMethod.invoke(cl));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public void exportAllPackagesTo(JLRModule module) {
        if (this != module) {
            for (String pkg : getPackages()) {
                if (!isExported(pkg, module)) {
                    addExports(pkg, module);
                }
            }
        }
    }

    public String[] getPackages() {
        try {
            return (String[]) getPackagesMethod.invoke(realModule);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public boolean isExported(String pn) {
        try {
            return (Boolean) isExportedMethod.invoke(realModule, pn);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public boolean isExported(String pn, JLRModule other) {
        try {
            return (Boolean) isExported2Method.invoke(realModule, pn, other.realModule);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public void addExports(String pn, JLRModule other) {
        try {
            addExportsMethod.invoke(realModule, pn, other.realModule);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}