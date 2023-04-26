package org.graalvm.compiler.test;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.Set;

public class JLModule {

    static {
        if (GraalTest.Java8OrEarlier) {
            throw new AssertionError("Use of " + JLModule.class + " only allowed if " + GraalTest.class.getName() + ".JDK8OrEarlier is false");
        }
    }

    private final Object realModule;

    public JLModule(Object module) {
        this.realModule = module;
    }

    private static final Class<?> moduleClass;

    private static final Method getModuleMethod;

    private static final Method getUnnamedModuleMethod;

    private static final Method getPackagesMethod;

    private static final Method isExportedMethod;

    private static final Method isExported2Method;

    private static final Method addExportsMethod;

    private static final Method modulesAddExportsMethod;

    private static final Method modulesAddOpensMethod;

    static {
        try {
            moduleClass = Class.forName("java.lang.Module");
            Class<?> modulesClass = Class.forName("jdk.internal.module.Modules");
            getModuleMethod = Class.class.getMethod("getModule");
            getUnnamedModuleMethod = ClassLoader.class.getMethod("getUnnamedModule");
            getPackagesMethod = moduleClass.getMethod("getPackages");
            isExportedMethod = moduleClass.getMethod("isExported", String.class);
            isExported2Method = moduleClass.getMethod("isExported", String.class, moduleClass);
            addExportsMethod = moduleClass.getMethod("addExports", String.class, moduleClass);
            modulesAddExportsMethod = modulesClass.getDeclaredMethod("addExports", moduleClass, String.class, moduleClass);
            modulesAddOpensMethod = modulesClass.getDeclaredMethod("addOpens", moduleClass, String.class, moduleClass);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static JLModule fromClass(Class<?> cls) {
        try {
            return new JLModule(getModuleMethod.invoke(cls));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static JLModule getUnnamedModuleFor(ClassLoader cl) {
        try {
            return new JLModule(getUnnamedModuleMethod.invoke(cl));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public void exportAllPackagesTo(JLModule module) {
        if (this != module) {
            for (String pkg : getPackages()) {
                if (!isExported(pkg, module)) {
                    addExports(pkg, module);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Set<String> getPackages() {
        try {
            return (Set<String>) getPackagesMethod.invoke(realModule);
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

    public boolean isExported(String pn, JLModule other) {
        try {
            return (Boolean) isExported2Method.invoke(realModule, pn, other.realModule);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public void addExports(String pn, JLModule other) {
        try {
            addExportsMethod.invoke(realModule, pn, other.realModule);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static Object unbox(Object obj) {
        if (obj instanceof JLModule) {
            return ((JLModule) obj).realModule;
        }
        return obj;
    }

    public static void uncheckedAddExports(Object m1, String pn, Object m2) {
        try {
            modulesAddExportsMethod.invoke(null, unbox(m1), pn, unbox(m2));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static void openAllPackagesForReflectionTo(Class<?> moduleMember, Class<?> requestor) {
        try {
            Object moduleToOpen = getModuleMethod.invoke(moduleMember);
            Object requestorModule = getModuleMethod.invoke(requestor);
            if (moduleToOpen != requestorModule) {
                String[] packages = (String[]) getPackagesMethod.invoke(moduleToOpen);
                for (String pkg : packages) {
                    modulesAddOpensMethod.invoke(moduleToOpen, pkg, requestorModule);
                }
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static void openForReflectionTo(Class<?> declaringClass, Class<?> accessor) {
        try {
            Object moduleToOpen = getModuleMethod.invoke(declaringClass);
            Object accessorModule = getModuleMethod.invoke(accessor);
            if (moduleToOpen != accessorModule) {
                modulesAddOpensMethod.invoke(null, moduleToOpen, declaringClass.getPackage().getName(), accessorModule);
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static void exportPackageTo(Class<?> moduleMember, String packageName, Class<?> requestor) {
        try {
            Object moduleToExport = getModuleMethod.invoke(moduleMember);
            Object requestorModule = getModuleMethod.invoke(requestor);
            if (moduleToExport != requestorModule) {
                modulesAddExportsMethod.invoke(null, moduleToExport, packageName, requestorModule);
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
