package java.lang;

import java.lang.annotation.Annotation;
import java.lang.module.ModuleReader;
import java.lang.ref.SoftReference;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamField;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import jdk.internal.HotSpotIntrinsicCandidate;
import jdk.internal.loader.BootLoader;
import jdk.internal.loader.BuiltinClassLoader;
import jdk.internal.misc.Unsafe;
import jdk.internal.misc.VM;
import jdk.internal.module.Resources;
import jdk.internal.reflect.CallerSensitive;
import jdk.internal.reflect.ConstantPool;
import jdk.internal.reflect.Reflection;
import jdk.internal.reflect.ReflectionFactory;
import jdk.internal.vm.annotation.ForceInline;
import sun.reflect.generics.factory.CoreReflectionFactory;
import sun.reflect.generics.factory.GenericsFactory;
import sun.reflect.generics.repository.ClassRepository;
import sun.reflect.generics.repository.MethodRepository;
import sun.reflect.generics.repository.ConstructorRepository;
import sun.reflect.generics.scope.ClassScope;
import sun.security.util.SecurityConstants;
import sun.reflect.annotation.*;
import sun.reflect.misc.ReflectUtil;

public final class Class<T> implements java.io.Serializable, GenericDeclaration, Type, AnnotatedElement {

    private static final int ANNOTATION = 0x00002000;

    private static final int ENUM = 0x00004000;

    private static final int SYNTHETIC = 0x00001000;

    private static native void registerNatives();

    static {
        registerNatives();
    }

    private Class(ClassLoader loader, Class<?> arrayComponentType) {
        classLoader = loader;
        componentType = arrayComponentType;
    }

    public String toString() {
        return (isInterface() ? "interface " : (isPrimitive() ? "" : "class ")) + getName();
    }

    public String toGenericString() {
        if (isPrimitive()) {
            return toString();
        } else {
            StringBuilder sb = new StringBuilder();
            Class<?> component = this;
            int arrayDepth = 0;
            if (isArray()) {
                do {
                    arrayDepth++;
                    component = component.getComponentType();
                } while (component.isArray());
                sb.append(component.getName());
            } else {
                int modifiers = getModifiers() & Modifier.classModifiers();
                if (modifiers != 0) {
                    sb.append(Modifier.toString(modifiers));
                    sb.append(' ');
                }
                if (isAnnotation()) {
                    sb.append('@');
                }
                if (isInterface()) {
                    sb.append("interface");
                } else {
                    if (isEnum())
                        sb.append("enum");
                    else
                        sb.append("class");
                }
                sb.append(' ');
                sb.append(getName());
            }
            TypeVariable<?>[] typeparms = component.getTypeParameters();
            if (typeparms.length > 0) {
                StringJoiner sj = new StringJoiner(",", "<", ">");
                for (TypeVariable<?> typeparm : typeparms) {
                    sj.add(typeparm.getTypeName());
                }
                sb.append(sj.toString());
            }
            for (int i = 0; i < arrayDepth; i++) sb.append("[]");
            return sb.toString();
        }
    }

    @CallerSensitive
    public static Class<?> forName(String className) throws ClassNotFoundException {
        Class<?> caller = Reflection.getCallerClass();
        return forName0(className, true, ClassLoader.getClassLoader(caller), caller);
    }

    @CallerSensitive
    public static Class<?> forName(String name, boolean initialize, ClassLoader loader) throws ClassNotFoundException {
        Class<?> caller = null;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            caller = Reflection.getCallerClass();
            if (loader == null) {
                ClassLoader ccl = ClassLoader.getClassLoader(caller);
                if (ccl != null) {
                    sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);
                }
            }
        }
        return forName0(name, initialize, loader, caller);
    }

    private static native Class<?> forName0(String name, boolean initialize, ClassLoader loader, Class<?> caller) throws ClassNotFoundException;

    @CallerSensitive
    public static Class<?> forName(Module module, String name) {
        Objects.requireNonNull(module);
        Objects.requireNonNull(name);
        Class<?> caller = Reflection.getCallerClass();
        if (caller != null && caller.getModule() != module) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);
            }
        }
        PrivilegedAction<ClassLoader> pa = module::getClassLoader;
        ClassLoader cl = AccessController.doPrivileged(pa);
        if (cl != null) {
            return cl.loadClass(module, name);
        } else {
            return BootLoader.loadClass(module, name);
        }
    }

    @CallerSensitive
    @Deprecated(since = "9")
    public T newInstance() throws InstantiationException, IllegalAccessException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkMemberAccess(sm, Member.PUBLIC, Reflection.getCallerClass(), false);
        }
        if (cachedConstructor == null) {
            if (this == Class.class) {
                throw new IllegalAccessException("Can not call newInstance() on the Class for java.lang.Class");
            }
            try {
                Class<?>[] empty = {};
                final Constructor<T> c = getReflectionFactory().copyConstructor(getConstructor0(empty, Member.DECLARED));
                java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<>() {

                    public Void run() {
                        c.setAccessible(true);
                        return null;
                    }
                });
                cachedConstructor = c;
            } catch (NoSuchMethodException e) {
                throw (InstantiationException) new InstantiationException(getName()).initCause(e);
            }
        }
        Constructor<T> tmpConstructor = cachedConstructor;
        Class<?> caller = Reflection.getCallerClass();
        if (newInstanceCallerCache != caller) {
            int modifiers = tmpConstructor.getModifiers();
            Reflection.ensureMemberAccess(caller, this, this, modifiers);
            newInstanceCallerCache = caller;
        }
        try {
            return tmpConstructor.newInstance((Object[]) null);
        } catch (InvocationTargetException e) {
            Unsafe.getUnsafe().throwException(e.getTargetException());
            return null;
        }
    }

    private transient volatile Constructor<T> cachedConstructor;

    private transient volatile Class<?> newInstanceCallerCache;

    @HotSpotIntrinsicCandidate
    public native boolean isInstance(Object obj);

    @HotSpotIntrinsicCandidate
    public native boolean isAssignableFrom(Class<?> cls);

    @HotSpotIntrinsicCandidate
    public native boolean isInterface();

    @HotSpotIntrinsicCandidate
    public native boolean isArray();

    @HotSpotIntrinsicCandidate
    public native boolean isPrimitive();

    public boolean isAnnotation() {
        return (getModifiers() & ANNOTATION) != 0;
    }

    public boolean isSynthetic() {
        return (getModifiers() & SYNTHETIC) != 0;
    }

    public String getName() {
        String name = this.name;
        if (name == null)
            this.name = name = getName0();
        return name;
    }

    private transient String name;

    private native String getName0();

    @CallerSensitive
    @ForceInline
    public ClassLoader getClassLoader() {
        ClassLoader cl = getClassLoader0();
        if (cl == null)
            return null;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            ClassLoader.checkClassLoaderPermission(cl, Reflection.getCallerClass());
        }
        return cl;
    }

    ClassLoader getClassLoader0() {
        return classLoader;
    }

    public Module getModule() {
        return module;
    }

    private transient Module module;

    private final ClassLoader classLoader;

    @SuppressWarnings("unchecked")
    public TypeVariable<Class<T>>[] getTypeParameters() {
        ClassRepository info = getGenericInfo();
        if (info != null)
            return (TypeVariable<Class<T>>[]) info.getTypeParameters();
        else
            return (TypeVariable<Class<T>>[]) new TypeVariable<?>[0];
    }

    @HotSpotIntrinsicCandidate
    public native Class<? super T> getSuperclass();

    public Type getGenericSuperclass() {
        ClassRepository info = getGenericInfo();
        if (info == null) {
            return getSuperclass();
        }
        if (isInterface()) {
            return null;
        }
        return info.getSuperclass();
    }

    public Package getPackage() {
        if (isPrimitive() || isArray()) {
            return null;
        }
        ClassLoader cl = getClassLoader0();
        return cl != null ? cl.definePackage(this) : BootLoader.definePackage(this);
    }

    public String getPackageName() {
        String pn = this.packageName;
        if (pn == null) {
            Class<?> c = this;
            while (c.isArray()) {
                c = c.getComponentType();
            }
            if (c.isPrimitive()) {
                pn = "java.lang";
            } else {
                String cn = c.getName();
                int dot = cn.lastIndexOf('.');
                pn = (dot != -1) ? cn.substring(0, dot).intern() : "";
            }
            this.packageName = pn;
        }
        return pn;
    }

    private transient String packageName;

    public Class<?>[] getInterfaces() {
        return getInterfaces(true);
    }

    private Class<?>[] getInterfaces(boolean cloneArray) {
        ReflectionData<T> rd = reflectionData();
        if (rd == null) {
            return getInterfaces0();
        } else {
            Class<?>[] interfaces = rd.interfaces;
            if (interfaces == null) {
                interfaces = getInterfaces0();
                rd.interfaces = interfaces;
            }
            return cloneArray ? interfaces.clone() : interfaces;
        }
    }

    private native Class<?>[] getInterfaces0();

    public Type[] getGenericInterfaces() {
        ClassRepository info = getGenericInfo();
        return (info == null) ? getInterfaces() : info.getSuperInterfaces();
    }

    public Class<?> getComponentType() {
        if (isArray()) {
            return componentType;
        } else {
            return null;
        }
    }

    private final Class<?> componentType;

    @HotSpotIntrinsicCandidate
    public native int getModifiers();

    public native Object[] getSigners();

    native void setSigners(Object[] signers);

    @CallerSensitive
    public Method getEnclosingMethod() throws SecurityException {
        EnclosingMethodInfo enclosingInfo = getEnclosingMethodInfo();
        if (enclosingInfo == null)
            return null;
        else {
            if (!enclosingInfo.isMethod())
                return null;
            MethodRepository typeInfo = MethodRepository.make(enclosingInfo.getDescriptor(), getFactory());
            Class<?> returnType = toClass(typeInfo.getReturnType());
            Type[] parameterTypes = typeInfo.getParameterTypes();
            Class<?>[] parameterClasses = new Class<?>[parameterTypes.length];
            for (int i = 0; i < parameterClasses.length; i++) parameterClasses[i] = toClass(parameterTypes[i]);
            final Class<?> enclosingCandidate = enclosingInfo.getEnclosingClass();
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                enclosingCandidate.checkMemberAccess(sm, Member.DECLARED, Reflection.getCallerClass(), true);
            }
            Method[] candidates = enclosingCandidate.privateGetDeclaredMethods(false);
            ReflectionFactory fact = getReflectionFactory();
            for (Method m : candidates) {
                if (m.getName().equals(enclosingInfo.getName()) && arrayContentsEq(parameterClasses, fact.getExecutableSharedParameterTypes(m))) {
                    if (m.getReturnType().equals(returnType)) {
                        return fact.copyMethod(m);
                    }
                }
            }
            throw new InternalError("Enclosing method not found");
        }
    }

    private native Object[] getEnclosingMethod0();

    private EnclosingMethodInfo getEnclosingMethodInfo() {
        Object[] enclosingInfo = getEnclosingMethod0();
        if (enclosingInfo == null)
            return null;
        else {
            return new EnclosingMethodInfo(enclosingInfo);
        }
    }

    private static final class EnclosingMethodInfo {

        private final Class<?> enclosingClass;

        private final String name;

        private final String descriptor;

        static void validate(Object[] enclosingInfo) {
            if (enclosingInfo.length != 3)
                throw new InternalError("Malformed enclosing method information");
            try {
                Class<?> enclosingClass = (Class<?>) enclosingInfo[0];
                assert (enclosingClass != null);
                String name = (String) enclosingInfo[1];
                String descriptor = (String) enclosingInfo[2];
                assert ((name != null && descriptor != null) || name == descriptor);
            } catch (ClassCastException cce) {
                throw new InternalError("Invalid type in enclosing method information", cce);
            }
        }

        EnclosingMethodInfo(Object[] enclosingInfo) {
            validate(enclosingInfo);
            this.enclosingClass = (Class<?>) enclosingInfo[0];
            this.name = (String) enclosingInfo[1];
            this.descriptor = (String) enclosingInfo[2];
        }

        boolean isPartial() {
            return enclosingClass == null || name == null || descriptor == null;
        }

        boolean isConstructor() {
            return !isPartial() && "<init>".equals(name);
        }

        boolean isMethod() {
            return !isPartial() && !isConstructor() && !"<clinit>".equals(name);
        }

        Class<?> getEnclosingClass() {
            return enclosingClass;
        }

        String getName() {
            return name;
        }

        String getDescriptor() {
            return descriptor;
        }
    }

    private static Class<?> toClass(Type o) {
        if (o instanceof GenericArrayType)
            return Array.newInstance(toClass(((GenericArrayType) o).getGenericComponentType()), 0).getClass();
        return (Class<?>) o;
    }

    @CallerSensitive
    public Constructor<?> getEnclosingConstructor() throws SecurityException {
        EnclosingMethodInfo enclosingInfo = getEnclosingMethodInfo();
        if (enclosingInfo == null)
            return null;
        else {
            if (!enclosingInfo.isConstructor())
                return null;
            ConstructorRepository typeInfo = ConstructorRepository.make(enclosingInfo.getDescriptor(), getFactory());
            Type[] parameterTypes = typeInfo.getParameterTypes();
            Class<?>[] parameterClasses = new Class<?>[parameterTypes.length];
            for (int i = 0; i < parameterClasses.length; i++) parameterClasses[i] = toClass(parameterTypes[i]);
            final Class<?> enclosingCandidate = enclosingInfo.getEnclosingClass();
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                enclosingCandidate.checkMemberAccess(sm, Member.DECLARED, Reflection.getCallerClass(), true);
            }
            Constructor<?>[] candidates = enclosingCandidate.privateGetDeclaredConstructors(false);
            ReflectionFactory fact = getReflectionFactory();
            for (Constructor<?> c : candidates) {
                if (arrayContentsEq(parameterClasses, fact.getExecutableSharedParameterTypes(c))) {
                    return fact.copyConstructor(c);
                }
            }
            throw new InternalError("Enclosing constructor not found");
        }
    }

    @CallerSensitive
    public Class<?> getDeclaringClass() throws SecurityException {
        final Class<?> candidate = getDeclaringClass0();
        if (candidate != null) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                candidate.checkPackageAccess(sm, ClassLoader.getClassLoader(Reflection.getCallerClass()), true);
            }
        }
        return candidate;
    }

    private native Class<?> getDeclaringClass0();

    @CallerSensitive
    public Class<?> getEnclosingClass() throws SecurityException {
        EnclosingMethodInfo enclosingInfo = getEnclosingMethodInfo();
        Class<?> enclosingCandidate;
        if (enclosingInfo == null) {
            enclosingCandidate = getDeclaringClass0();
        } else {
            Class<?> enclosingClass = enclosingInfo.getEnclosingClass();
            if (enclosingClass == this || enclosingClass == null)
                throw new InternalError("Malformed enclosing method information");
            else
                enclosingCandidate = enclosingClass;
        }
        if (enclosingCandidate != null) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                enclosingCandidate.checkPackageAccess(sm, ClassLoader.getClassLoader(Reflection.getCallerClass()), true);
            }
        }
        return enclosingCandidate;
    }

    public String getSimpleName() {
        if (isArray())
            return getComponentType().getSimpleName() + "[]";
        String simpleName = getSimpleBinaryName();
        if (simpleName == null) {
            simpleName = getName();
            return simpleName.substring(simpleName.lastIndexOf('.') + 1);
        }
        return simpleName;
    }

    public String getTypeName() {
        if (isArray()) {
            try {
                Class<?> cl = this;
                int dimensions = 0;
                while (cl.isArray()) {
                    dimensions++;
                    cl = cl.getComponentType();
                }
                StringBuilder sb = new StringBuilder();
                sb.append(cl.getName());
                for (int i = 0; i < dimensions; i++) {
                    sb.append("[]");
                }
                return sb.toString();
            } catch (Throwable e) {
            }
        }
        return getName();
    }

    public String getCanonicalName() {
        if (isArray()) {
            String canonicalName = getComponentType().getCanonicalName();
            if (canonicalName != null)
                return canonicalName + "[]";
            else
                return null;
        }
        if (isLocalOrAnonymousClass())
            return null;
        Class<?> enclosingClass = getEnclosingClass();
        if (enclosingClass == null) {
            return getName();
        } else {
            String enclosingName = enclosingClass.getCanonicalName();
            if (enclosingName == null)
                return null;
            return enclosingName + "." + getSimpleName();
        }
    }

    public boolean isAnonymousClass() {
        return !isArray() && isLocalOrAnonymousClass() && getSimpleBinaryName0() == null;
    }

    public boolean isLocalClass() {
        return isLocalOrAnonymousClass() && (isArray() || getSimpleBinaryName0() != null);
    }

    public boolean isMemberClass() {
        return !isLocalOrAnonymousClass() && getDeclaringClass0() != null;
    }

    private String getSimpleBinaryName() {
        if (isTopLevelClass())
            return null;
        String name = getSimpleBinaryName0();
        if (name == null)
            return "";
        return name;
    }

    private native String getSimpleBinaryName0();

    private boolean isTopLevelClass() {
        return !isLocalOrAnonymousClass() && getDeclaringClass0() == null;
    }

    private boolean isLocalOrAnonymousClass() {
        return hasEnclosingMethodInfo();
    }

    private boolean hasEnclosingMethodInfo() {
        Object[] enclosingInfo = getEnclosingMethod0();
        if (enclosingInfo != null) {
            EnclosingMethodInfo.validate(enclosingInfo);
            return true;
        }
        return false;
    }

    @CallerSensitive
    public Class<?>[] getClasses() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkMemberAccess(sm, Member.PUBLIC, Reflection.getCallerClass(), false);
        }
        return java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<>() {

            public Class<?>[] run() {
                List<Class<?>> list = new ArrayList<>();
                Class<?> currentClass = Class.this;
                while (currentClass != null) {
                    for (Class<?> m : currentClass.getDeclaredClasses()) {
                        if (Modifier.isPublic(m.getModifiers())) {
                            list.add(m);
                        }
                    }
                    currentClass = currentClass.getSuperclass();
                }
                return list.toArray(new Class<?>[0]);
            }
        });
    }

    @CallerSensitive
    public Field[] getFields() throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkMemberAccess(sm, Member.PUBLIC, Reflection.getCallerClass(), true);
        }
        return copyFields(privateGetPublicFields(null));
    }

    @CallerSensitive
    public Method[] getMethods() throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkMemberAccess(sm, Member.PUBLIC, Reflection.getCallerClass(), true);
        }
        return copyMethods(privateGetPublicMethods());
    }

    @CallerSensitive
    public Constructor<?>[] getConstructors() throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkMemberAccess(sm, Member.PUBLIC, Reflection.getCallerClass(), true);
        }
        return copyConstructors(privateGetDeclaredConstructors(true));
    }

    @CallerSensitive
    public Field getField(String name) throws NoSuchFieldException, SecurityException {
        Objects.requireNonNull(name);
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkMemberAccess(sm, Member.PUBLIC, Reflection.getCallerClass(), true);
        }
        Field field = getField0(name);
        if (field == null) {
            throw new NoSuchFieldException(name);
        }
        return getReflectionFactory().copyField(field);
    }

    @CallerSensitive
    public Method getMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        Objects.requireNonNull(name);
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkMemberAccess(sm, Member.PUBLIC, Reflection.getCallerClass(), true);
        }
        Method method = getMethod0(name, parameterTypes);
        if (method == null) {
            throw new NoSuchMethodException(methodToString(name, parameterTypes));
        }
        return getReflectionFactory().copyMethod(method);
    }

    @CallerSensitive
    public Constructor<T> getConstructor(Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkMemberAccess(sm, Member.PUBLIC, Reflection.getCallerClass(), true);
        }
        return getReflectionFactory().copyConstructor(getConstructor0(parameterTypes, Member.PUBLIC));
    }

    @CallerSensitive
    public Class<?>[] getDeclaredClasses() throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkMemberAccess(sm, Member.DECLARED, Reflection.getCallerClass(), false);
        }
        return getDeclaredClasses0();
    }

    @CallerSensitive
    public Field[] getDeclaredFields() throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkMemberAccess(sm, Member.DECLARED, Reflection.getCallerClass(), true);
        }
        return copyFields(privateGetDeclaredFields(false));
    }

    @CallerSensitive
    public Method[] getDeclaredMethods() throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkMemberAccess(sm, Member.DECLARED, Reflection.getCallerClass(), true);
        }
        return copyMethods(privateGetDeclaredMethods(false));
    }

    @CallerSensitive
    public Constructor<?>[] getDeclaredConstructors() throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkMemberAccess(sm, Member.DECLARED, Reflection.getCallerClass(), true);
        }
        return copyConstructors(privateGetDeclaredConstructors(false));
    }

    @CallerSensitive
    public Field getDeclaredField(String name) throws NoSuchFieldException, SecurityException {
        Objects.requireNonNull(name);
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkMemberAccess(sm, Member.DECLARED, Reflection.getCallerClass(), true);
        }
        Field field = searchFields(privateGetDeclaredFields(false), name);
        if (field == null) {
            throw new NoSuchFieldException(name);
        }
        return getReflectionFactory().copyField(field);
    }

    @CallerSensitive
    public Method getDeclaredMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        Objects.requireNonNull(name);
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkMemberAccess(sm, Member.DECLARED, Reflection.getCallerClass(), true);
        }
        Method method = searchMethods(privateGetDeclaredMethods(false), name, parameterTypes);
        if (method == null) {
            throw new NoSuchMethodException(methodToString(name, parameterTypes));
        }
        return getReflectionFactory().copyMethod(method);
    }

    List<Method> getDeclaredPublicMethods(String name, Class<?>... parameterTypes) {
        Method[] methods = privateGetDeclaredMethods(true);
        ReflectionFactory factory = getReflectionFactory();
        List<Method> result = new ArrayList<>();
        for (Method method : methods) {
            if (method.getName().equals(name) && Arrays.equals(factory.getExecutableSharedParameterTypes(method), parameterTypes)) {
                result.add(factory.copyMethod(method));
            }
        }
        return result;
    }

    @CallerSensitive
    public Constructor<T> getDeclaredConstructor(Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            checkMemberAccess(sm, Member.DECLARED, Reflection.getCallerClass(), true);
        }
        return getReflectionFactory().copyConstructor(getConstructor0(parameterTypes, Member.DECLARED));
    }

    @CallerSensitive
    public InputStream getResourceAsStream(String name) {
        name = resolveName(name);
        Module thisModule = getModule();
        if (thisModule.isNamed()) {
            if (Resources.canEncapsulate(name) && !isOpenToCaller(name, Reflection.getCallerClass())) {
                return null;
            }
            String mn = thisModule.getName();
            ClassLoader cl = getClassLoader0();
            try {
                if (cl == null) {
                    return BootLoader.findResourceAsStream(mn, name);
                } else if (cl instanceof BuiltinClassLoader) {
                    return ((BuiltinClassLoader) cl).findResourceAsStream(mn, name);
                } else {
                    URL url = cl.findResource(mn, name);
                    return (url != null) ? url.openStream() : null;
                }
            } catch (IOException | SecurityException e) {
                return null;
            }
        }
        ClassLoader cl = getClassLoader0();
        if (cl == null) {
            return ClassLoader.getSystemResourceAsStream(name);
        } else {
            return cl.getResourceAsStream(name);
        }
    }

    @CallerSensitive
    public URL getResource(String name) {
        name = resolveName(name);
        Module thisModule = getModule();
        if (thisModule.isNamed()) {
            if (Resources.canEncapsulate(name) && !isOpenToCaller(name, Reflection.getCallerClass())) {
                return null;
            }
            String mn = thisModule.getName();
            ClassLoader cl = getClassLoader0();
            try {
                if (cl == null) {
                    return BootLoader.findResource(mn, name);
                } else {
                    return cl.findResource(mn, name);
                }
            } catch (IOException ioe) {
                return null;
            }
        }
        ClassLoader cl = getClassLoader0();
        if (cl == null) {
            return ClassLoader.getSystemResource(name);
        } else {
            return cl.getResource(name);
        }
    }

    private boolean isOpenToCaller(String name, Class<?> caller) {
        Module thisModule = getModule();
        Module callerModule = (caller != null) ? caller.getModule() : null;
        if (callerModule != thisModule) {
            String pn = Resources.toPackageName(name);
            if (thisModule.getDescriptor().packages().contains(pn)) {
                if (callerModule == null && !thisModule.isOpen(pn)) {
                    return false;
                }
                if (!thisModule.isOpen(pn, callerModule)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static java.security.ProtectionDomain allPermDomain;

    public java.security.ProtectionDomain getProtectionDomain() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.GET_PD_PERMISSION);
        }
        java.security.ProtectionDomain pd = getProtectionDomain0();
        if (pd == null) {
            if (allPermDomain == null) {
                java.security.Permissions perms = new java.security.Permissions();
                perms.add(SecurityConstants.ALL_PERMISSION);
                allPermDomain = new java.security.ProtectionDomain(null, perms);
            }
            pd = allPermDomain;
        }
        return pd;
    }

    private native java.security.ProtectionDomain getProtectionDomain0();

    static native Class<?> getPrimitiveClass(String name);

    private void checkMemberAccess(SecurityManager sm, int which, Class<?> caller, boolean checkProxyInterfaces) {
        final ClassLoader ccl = ClassLoader.getClassLoader(caller);
        if (which != Member.PUBLIC) {
            final ClassLoader cl = getClassLoader0();
            if (ccl != cl) {
                sm.checkPermission(SecurityConstants.CHECK_MEMBER_ACCESS_PERMISSION);
            }
        }
        this.checkPackageAccess(sm, ccl, checkProxyInterfaces);
    }

    private void checkPackageAccess(SecurityManager sm, final ClassLoader ccl, boolean checkProxyInterfaces) {
        final ClassLoader cl = getClassLoader0();
        if (ReflectUtil.needsPackageAccessCheck(ccl, cl)) {
            String pkg = this.getPackageName();
            if (pkg != null && !pkg.isEmpty()) {
                if (!Proxy.isProxyClass(this) || ReflectUtil.isNonPublicProxyClass(this)) {
                    sm.checkPackageAccess(pkg);
                }
            }
        }
        if (checkProxyInterfaces && Proxy.isProxyClass(this)) {
            ReflectUtil.checkProxyPackageAccess(ccl, this.getInterfaces());
        }
    }

    private String resolveName(String name) {
        if (!name.startsWith("/")) {
            Class<?> c = this;
            while (c.isArray()) {
                c = c.getComponentType();
            }
            String baseName = c.getPackageName();
            if (baseName != null && !baseName.isEmpty()) {
                name = baseName.replace('.', '/') + "/" + name;
            }
        } else {
            name = name.substring(1);
        }
        return name;
    }

    private static class Atomic {

        private static final Unsafe unsafe = Unsafe.getUnsafe();

        private static final long reflectionDataOffset = unsafe.objectFieldOffset(Class.class, "reflectionData");

        private static final long annotationTypeOffset = unsafe.objectFieldOffset(Class.class, "annotationType");

        private static final long annotationDataOffset = unsafe.objectFieldOffset(Class.class, "annotationData");

        static <T> boolean casReflectionData(Class<?> clazz, SoftReference<ReflectionData<T>> oldData, SoftReference<ReflectionData<T>> newData) {
            return unsafe.compareAndSetObject(clazz, reflectionDataOffset, oldData, newData);
        }

        static <T> boolean casAnnotationType(Class<?> clazz, AnnotationType oldType, AnnotationType newType) {
            return unsafe.compareAndSetObject(clazz, annotationTypeOffset, oldType, newType);
        }

        static <T> boolean casAnnotationData(Class<?> clazz, AnnotationData oldData, AnnotationData newData) {
            return unsafe.compareAndSetObject(clazz, annotationDataOffset, oldData, newData);
        }
    }

    private static class ReflectionData<T> {

        volatile Field[] declaredFields;

        volatile Field[] publicFields;

        volatile Method[] declaredMethods;

        volatile Method[] publicMethods;

        volatile Constructor<T>[] declaredConstructors;

        volatile Constructor<T>[] publicConstructors;

        volatile Field[] declaredPublicFields;

        volatile Method[] declaredPublicMethods;

        volatile Class<?>[] interfaces;

        final int redefinedCount;

        ReflectionData(int redefinedCount) {
            this.redefinedCount = redefinedCount;
        }
    }

    private transient volatile SoftReference<ReflectionData<T>> reflectionData;

    private transient volatile int classRedefinedCount;

    private ReflectionData<T> reflectionData() {
        SoftReference<ReflectionData<T>> reflectionData = this.reflectionData;
        int classRedefinedCount = this.classRedefinedCount;
        ReflectionData<T> rd;
        if (reflectionData != null && (rd = reflectionData.get()) != null && rd.redefinedCount == classRedefinedCount) {
            return rd;
        }
        return newReflectionData(reflectionData, classRedefinedCount);
    }

    private ReflectionData<T> newReflectionData(SoftReference<ReflectionData<T>> oldReflectionData, int classRedefinedCount) {
        while (true) {
            ReflectionData<T> rd = new ReflectionData<>(classRedefinedCount);
            if (Atomic.casReflectionData(this, oldReflectionData, new SoftReference<>(rd))) {
                return rd;
            }
            oldReflectionData = this.reflectionData;
            classRedefinedCount = this.classRedefinedCount;
            if (oldReflectionData != null && (rd = oldReflectionData.get()) != null && rd.redefinedCount == classRedefinedCount) {
                return rd;
            }
        }
    }

    private native String getGenericSignature0();

    private transient volatile ClassRepository genericInfo;

    private GenericsFactory getFactory() {
        return CoreReflectionFactory.make(this, ClassScope.make(this));
    }

    private ClassRepository getGenericInfo() {
        ClassRepository genericInfo = this.genericInfo;
        if (genericInfo == null) {
            String signature = getGenericSignature0();
            if (signature == null) {
                genericInfo = ClassRepository.NONE;
            } else {
                genericInfo = ClassRepository.make(signature, getFactory());
            }
            this.genericInfo = genericInfo;
        }
        return (genericInfo != ClassRepository.NONE) ? genericInfo : null;
    }

    native byte[] getRawAnnotations();

    native byte[] getRawTypeAnnotations();

    static byte[] getExecutableTypeAnnotationBytes(Executable ex) {
        return getReflectionFactory().getExecutableTypeAnnotationBytes(ex);
    }

    native ConstantPool getConstantPool();

    private Field[] privateGetDeclaredFields(boolean publicOnly) {
        Field[] res;
        ReflectionData<T> rd = reflectionData();
        if (rd != null) {
            res = publicOnly ? rd.declaredPublicFields : rd.declaredFields;
            if (res != null)
                return res;
        }
        res = Reflection.filterFields(this, getDeclaredFields0(publicOnly));
        if (rd != null) {
            if (publicOnly) {
                rd.declaredPublicFields = res;
            } else {
                rd.declaredFields = res;
            }
        }
        return res;
    }

    private Field[] privateGetPublicFields(Set<Class<?>> traversedInterfaces) {
        Field[] res;
        ReflectionData<T> rd = reflectionData();
        if (rd != null) {
            res = rd.publicFields;
            if (res != null)
                return res;
        }
        List<Field> fields = new ArrayList<>();
        if (traversedInterfaces == null) {
            traversedInterfaces = new HashSet<>();
        }
        Field[] tmp = privateGetDeclaredFields(true);
        addAll(fields, tmp);
        for (Class<?> c : getInterfaces()) {
            if (!traversedInterfaces.contains(c)) {
                traversedInterfaces.add(c);
                addAll(fields, c.privateGetPublicFields(traversedInterfaces));
            }
        }
        if (!isInterface()) {
            Class<?> c = getSuperclass();
            if (c != null) {
                addAll(fields, c.privateGetPublicFields(traversedInterfaces));
            }
        }
        res = new Field[fields.size()];
        fields.toArray(res);
        if (rd != null) {
            rd.publicFields = res;
        }
        return res;
    }

    private static void addAll(Collection<Field> c, Field[] o) {
        for (Field f : o) {
            c.add(f);
        }
    }

    private Constructor<T>[] privateGetDeclaredConstructors(boolean publicOnly) {
        Constructor<T>[] res;
        ReflectionData<T> rd = reflectionData();
        if (rd != null) {
            res = publicOnly ? rd.publicConstructors : rd.declaredConstructors;
            if (res != null)
                return res;
        }
        if (isInterface()) {
            @SuppressWarnings("unchecked")
            Constructor<T>[] temporaryRes = (Constructor<T>[]) new Constructor<?>[0];
            res = temporaryRes;
        } else {
            res = getDeclaredConstructors0(publicOnly);
        }
        if (rd != null) {
            if (publicOnly) {
                rd.publicConstructors = res;
            } else {
                rd.declaredConstructors = res;
            }
        }
        return res;
    }

    private Method[] privateGetDeclaredMethods(boolean publicOnly) {
        Method[] res;
        ReflectionData<T> rd = reflectionData();
        if (rd != null) {
            res = publicOnly ? rd.declaredPublicMethods : rd.declaredMethods;
            if (res != null)
                return res;
        }
        res = Reflection.filterMethods(this, getDeclaredMethods0(publicOnly));
        if (rd != null) {
            if (publicOnly) {
                rd.declaredPublicMethods = res;
            } else {
                rd.declaredMethods = res;
            }
        }
        return res;
    }

    private Method[] privateGetPublicMethods() {
        Method[] res;
        ReflectionData<T> rd = reflectionData();
        if (rd != null) {
            res = rd.publicMethods;
            if (res != null)
                return res;
        }
        PublicMethods pms = new PublicMethods();
        for (Method m : privateGetDeclaredMethods(true)) {
            pms.merge(m);
        }
        Class<?> sc = getSuperclass();
        if (sc != null) {
            for (Method m : sc.privateGetPublicMethods()) {
                pms.merge(m);
            }
        }
        for (Class<?> intf : getInterfaces(false)) {
            for (Method m : intf.privateGetPublicMethods()) {
                if (!Modifier.isStatic(m.getModifiers())) {
                    pms.merge(m);
                }
            }
        }
        res = pms.toArray();
        if (rd != null) {
            rd.publicMethods = res;
        }
        return res;
    }

    private static Field searchFields(Field[] fields, String name) {
        for (Field field : fields) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    private Field getField0(String name) {
        Field res;
        if ((res = searchFields(privateGetDeclaredFields(true), name)) != null) {
            return res;
        }
        Class<?>[] interfaces = getInterfaces(false);
        for (Class<?> c : interfaces) {
            if ((res = c.getField0(name)) != null) {
                return res;
            }
        }
        if (!isInterface()) {
            Class<?> c = getSuperclass();
            if (c != null) {
                if ((res = c.getField0(name)) != null) {
                    return res;
                }
            }
        }
        return null;
    }

    private static Method searchMethods(Method[] methods, String name, Class<?>[] parameterTypes) {
        ReflectionFactory fact = getReflectionFactory();
        Method res = null;
        for (Method m : methods) {
            if (m.getName().equals(name) && arrayContentsEq(parameterTypes, fact.getExecutableSharedParameterTypes(m)) && (res == null || (res.getReturnType() != m.getReturnType() && res.getReturnType().isAssignableFrom(m.getReturnType()))))
                res = m;
        }
        return res;
    }

    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

    private Method getMethod0(String name, Class<?>[] parameterTypes) {
        PublicMethods.MethodList res = getMethodsRecursive(name, parameterTypes == null ? EMPTY_CLASS_ARRAY : parameterTypes, true);
        return res == null ? null : res.getMostSpecific();
    }

    private PublicMethods.MethodList getMethodsRecursive(String name, Class<?>[] parameterTypes, boolean includeStatic) {
        Method[] methods = privateGetDeclaredMethods(true);
        PublicMethods.MethodList res = PublicMethods.MethodList.filter(methods, name, parameterTypes, includeStatic);
        if (res != null) {
            return res;
        }
        Class<?> sc = getSuperclass();
        if (sc != null) {
            res = sc.getMethodsRecursive(name, parameterTypes, includeStatic);
        }
        for (Class<?> intf : getInterfaces(false)) {
            res = PublicMethods.MethodList.merge(res, intf.getMethodsRecursive(name, parameterTypes, false));
        }
        return res;
    }

    private Constructor<T> getConstructor0(Class<?>[] parameterTypes, int which) throws NoSuchMethodException {
        ReflectionFactory fact = getReflectionFactory();
        Constructor<T>[] constructors = privateGetDeclaredConstructors((which == Member.PUBLIC));
        for (Constructor<T> constructor : constructors) {
            if (arrayContentsEq(parameterTypes, fact.getExecutableSharedParameterTypes(constructor))) {
                return constructor;
            }
        }
        throw new NoSuchMethodException(methodToString("<init>", parameterTypes));
    }

    private static boolean arrayContentsEq(Object[] a1, Object[] a2) {
        if (a1 == null) {
            return a2 == null || a2.length == 0;
        }
        if (a2 == null) {
            return a1.length == 0;
        }
        if (a1.length != a2.length) {
            return false;
        }
        for (int i = 0; i < a1.length; i++) {
            if (a1[i] != a2[i]) {
                return false;
            }
        }
        return true;
    }

    private static Field[] copyFields(Field[] arg) {
        Field[] out = new Field[arg.length];
        ReflectionFactory fact = getReflectionFactory();
        for (int i = 0; i < arg.length; i++) {
            out[i] = fact.copyField(arg[i]);
        }
        return out;
    }

    private static Method[] copyMethods(Method[] arg) {
        Method[] out = new Method[arg.length];
        ReflectionFactory fact = getReflectionFactory();
        for (int i = 0; i < arg.length; i++) {
            out[i] = fact.copyMethod(arg[i]);
        }
        return out;
    }

    private static <U> Constructor<U>[] copyConstructors(Constructor<U>[] arg) {
        Constructor<U>[] out = arg.clone();
        ReflectionFactory fact = getReflectionFactory();
        for (int i = 0; i < out.length; i++) {
            out[i] = fact.copyConstructor(out[i]);
        }
        return out;
    }

    private native Field[] getDeclaredFields0(boolean publicOnly);

    private native Method[] getDeclaredMethods0(boolean publicOnly);

    private native Constructor<T>[] getDeclaredConstructors0(boolean publicOnly);

    private native Class<?>[] getDeclaredClasses0();

    private String methodToString(String name, Class<?>[] argTypes) {
        StringJoiner sj = new StringJoiner(", ", getName() + "." + name + "(", ")");
        if (argTypes != null) {
            for (int i = 0; i < argTypes.length; i++) {
                Class<?> c = argTypes[i];
                sj.add((c == null) ? "null" : c.getName());
            }
        }
        return sj.toString();
    }

    private static final long serialVersionUID = 3206093459760846163L;

    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[0];

    public boolean desiredAssertionStatus() {
        ClassLoader loader = getClassLoader0();
        if (loader == null)
            return desiredAssertionStatus0(this);
        synchronized (loader.assertionLock) {
            if (loader.classAssertionStatus != null) {
                return loader.desiredAssertionStatus(getName());
            }
        }
        return desiredAssertionStatus0(this);
    }

    private static native boolean desiredAssertionStatus0(Class<?> clazz);

    public boolean isEnum() {
        return (this.getModifiers() & ENUM) != 0 && this.getSuperclass() == java.lang.Enum.class;
    }

    private static ReflectionFactory getReflectionFactory() {
        if (reflectionFactory == null) {
            reflectionFactory = java.security.AccessController.doPrivileged(new ReflectionFactory.GetReflectionFactoryAction());
        }
        return reflectionFactory;
    }

    private static ReflectionFactory reflectionFactory;

    public T[] getEnumConstants() {
        T[] values = getEnumConstantsShared();
        return (values != null) ? values.clone() : null;
    }

    T[] getEnumConstantsShared() {
        T[] constants = enumConstants;
        if (constants == null) {
            if (!isEnum())
                return null;
            try {
                final Method values = getMethod("values");
                java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<>() {

                    public Void run() {
                        values.setAccessible(true);
                        return null;
                    }
                });
                @SuppressWarnings("unchecked")
                T[] temporaryConstants = (T[]) values.invoke(null);
                enumConstants = constants = temporaryConstants;
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
                return null;
            }
        }
        return constants;
    }

    private transient volatile T[] enumConstants;

    Map<String, T> enumConstantDirectory() {
        Map<String, T> directory = enumConstantDirectory;
        if (directory == null) {
            T[] universe = getEnumConstantsShared();
            if (universe == null)
                throw new IllegalArgumentException(getName() + " is not an enum type");
            directory = new HashMap<>(2 * universe.length);
            for (T constant : universe) {
                directory.put(((Enum<?>) constant).name(), constant);
            }
            enumConstantDirectory = directory;
        }
        return directory;
    }

    private transient volatile Map<String, T> enumConstantDirectory;

    @SuppressWarnings("unchecked")
    @HotSpotIntrinsicCandidate
    public T cast(Object obj) {
        if (obj != null && !isInstance(obj))
            throw new ClassCastException(cannotCastMsg(obj));
        return (T) obj;
    }

    private String cannotCastMsg(Object obj) {
        return "Cannot cast " + obj.getClass().getName() + " to " + getName();
    }

    @SuppressWarnings("unchecked")
    public <U> Class<? extends U> asSubclass(Class<U> clazz) {
        if (clazz.isAssignableFrom(this))
            return (Class<? extends U>) this;
        else
            throw new ClassCastException(this.toString());
    }

    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        Objects.requireNonNull(annotationClass);
        return (A) annotationData().annotations.get(annotationClass);
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return GenericDeclaration.super.isAnnotationPresent(annotationClass);
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationClass) {
        Objects.requireNonNull(annotationClass);
        AnnotationData annotationData = annotationData();
        return AnnotationSupport.getAssociatedAnnotations(annotationData.declaredAnnotations, this, annotationClass);
    }

    public Annotation[] getAnnotations() {
        return AnnotationParser.toArray(annotationData().annotations);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getDeclaredAnnotation(Class<A> annotationClass) {
        Objects.requireNonNull(annotationClass);
        return (A) annotationData().declaredAnnotations.get(annotationClass);
    }

    @Override
    public <A extends Annotation> A[] getDeclaredAnnotationsByType(Class<A> annotationClass) {
        Objects.requireNonNull(annotationClass);
        return AnnotationSupport.getDirectlyAndIndirectlyPresent(annotationData().declaredAnnotations, annotationClass);
    }

    public Annotation[] getDeclaredAnnotations() {
        return AnnotationParser.toArray(annotationData().declaredAnnotations);
    }

    private static class AnnotationData {

        final Map<Class<? extends Annotation>, Annotation> annotations;

        final Map<Class<? extends Annotation>, Annotation> declaredAnnotations;

        final int redefinedCount;

        AnnotationData(Map<Class<? extends Annotation>, Annotation> annotations, Map<Class<? extends Annotation>, Annotation> declaredAnnotations, int redefinedCount) {
            this.annotations = annotations;
            this.declaredAnnotations = declaredAnnotations;
            this.redefinedCount = redefinedCount;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    private transient volatile AnnotationData annotationData;

    private AnnotationData annotationData() {
        while (true) {
            AnnotationData annotationData = this.annotationData;
            int classRedefinedCount = this.classRedefinedCount;
            if (annotationData != null && annotationData.redefinedCount == classRedefinedCount) {
                return annotationData;
            }
            AnnotationData newAnnotationData = createAnnotationData(classRedefinedCount);
            if (Atomic.casAnnotationData(this, annotationData, newAnnotationData)) {
                return newAnnotationData;
            }
        }
    }

    private AnnotationData createAnnotationData(int classRedefinedCount) {
        Map<Class<? extends Annotation>, Annotation> declaredAnnotations = AnnotationParser.parseAnnotations(getRawAnnotations(), getConstantPool(), this);
        Class<?> superClass = getSuperclass();
        Map<Class<? extends Annotation>, Annotation> annotations = null;
        if (superClass != null) {
            Map<Class<? extends Annotation>, Annotation> superAnnotations = superClass.annotationData().annotations;
            for (Map.Entry<Class<? extends Annotation>, Annotation> e : superAnnotations.entrySet()) {
                Class<? extends Annotation> annotationClass = e.getKey();
                if (AnnotationType.getInstance(annotationClass).isInherited()) {
                    if (annotations == null) {
                        annotations = new LinkedHashMap<>((Math.max(declaredAnnotations.size(), Math.min(12, declaredAnnotations.size() + superAnnotations.size())) * 4 + 2) / 3);
                    }
                    annotations.put(annotationClass, e.getValue());
                }
            }
        }
        if (annotations == null) {
            annotations = declaredAnnotations;
        } else {
            annotations.putAll(declaredAnnotations);
        }
        return new AnnotationData(annotations, declaredAnnotations, classRedefinedCount);
    }

    @SuppressWarnings("UnusedDeclaration")
    private transient volatile AnnotationType annotationType;

    boolean casAnnotationType(AnnotationType oldType, AnnotationType newType) {
        return Atomic.casAnnotationType(this, oldType, newType);
    }

    AnnotationType getAnnotationType() {
        return annotationType;
    }

    Map<Class<? extends Annotation>, Annotation> getDeclaredAnnotationMap() {
        return annotationData().declaredAnnotations;
    }

    transient ClassValue.ClassValueMap classValueMap;

    public AnnotatedType getAnnotatedSuperclass() {
        if (this == Object.class || isInterface() || isArray() || isPrimitive() || this == Void.TYPE) {
            return null;
        }
        return TypeAnnotationParser.buildAnnotatedSuperclass(getRawTypeAnnotations(), getConstantPool(), this);
    }

    public AnnotatedType[] getAnnotatedInterfaces() {
        return TypeAnnotationParser.buildAnnotatedInterfaces(getRawTypeAnnotations(), getConstantPool(), this);
    }
}
