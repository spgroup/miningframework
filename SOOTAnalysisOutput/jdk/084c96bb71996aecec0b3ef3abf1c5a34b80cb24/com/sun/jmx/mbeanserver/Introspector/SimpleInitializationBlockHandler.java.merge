package com.sun.jmx.mbeanserver;

import java.lang.annotation.Annotation;
import java.lang.ref.SoftReference;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import javax.management.Descriptor;
import javax.management.DescriptorKey;
import javax.management.DynamicMBean;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanInfo;
import javax.management.NotCompliantMBeanException;
import com.sun.jmx.remote.util.EnvHelp;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import javax.management.AttributeNotFoundException;
import javax.management.openmbean.CompositeData;
import sun.reflect.misc.MethodUtil;
import sun.reflect.misc.ReflectUtil;

public class Introspector {

    final public static boolean ALLOW_NONPUBLIC_MBEAN;

    static {
        String val = AccessController.doPrivileged(new GetPropertyAction("jdk.jmx.mbeans.allowNonPublic"));
        ALLOW_NONPUBLIC_MBEAN = Boolean.parseBoolean(val);
    }

    private Introspector() {
    }

    public static final boolean isDynamic(final Class<?> c) {
        return javax.management.DynamicMBean.class.isAssignableFrom(c);
    }

    public static void testCreation(Class<?> c) throws NotCompliantMBeanException {
        final int mods = c.getModifiers();
        if (Modifier.isAbstract(mods) || Modifier.isInterface(mods)) {
            throw new NotCompliantMBeanException("MBean class must be concrete");
        }
        final Constructor<?>[] consList = c.getConstructors();
        if (consList.length == 0) {
            throw new NotCompliantMBeanException("MBean class must have public constructor");
        }
    }

    public static void checkCompliance(Class<?> mbeanClass) throws NotCompliantMBeanException {
        if (DynamicMBean.class.isAssignableFrom(mbeanClass))
            return;
        final Exception mbeanException;
        try {
            getStandardMBeanInterface(mbeanClass);
            return;
        } catch (NotCompliantMBeanException e) {
            mbeanException = e;
        }
        final Exception mxbeanException;
        try {
            getMXBeanInterface(mbeanClass);
            return;
        } catch (NotCompliantMBeanException e) {
            mxbeanException = e;
        }
        final String msg = "MBean class " + mbeanClass.getName() + " does not implement " + "DynamicMBean, and neither follows the Standard MBean conventions (" + mbeanException.toString() + ") nor the MXBean conventions (" + mxbeanException.toString() + ")";
        throw new NotCompliantMBeanException(msg);
    }

    public static <T> DynamicMBean makeDynamicMBean(T mbean) throws NotCompliantMBeanException {
        if (mbean instanceof DynamicMBean)
            return (DynamicMBean) mbean;
        final Class<?> mbeanClass = mbean.getClass();
        Class<? super T> c = null;
        try {
            c = Util.cast(getStandardMBeanInterface(mbeanClass));
        } catch (NotCompliantMBeanException e) {
        }
        if (c != null)
            return new StandardMBeanSupport(mbean, c);
        try {
            c = Util.cast(getMXBeanInterface(mbeanClass));
        } catch (NotCompliantMBeanException e) {
        }
        if (c != null)
            return new MXBeanSupport(mbean, c);
        checkCompliance(mbeanClass);
        throw new NotCompliantMBeanException("Not compliant");
    }

    public static MBeanInfo testCompliance(Class<?> baseClass) throws NotCompliantMBeanException {
        if (isDynamic(baseClass))
            return null;
        return testCompliance(baseClass, null);
    }

    public static void testComplianceMXBeanInterface(Class<?> interfaceClass) throws NotCompliantMBeanException {
        MXBeanIntrospector.getInstance().getAnalyzer(interfaceClass);
    }

    public static void testComplianceMBeanInterface(Class<?> interfaceClass) throws NotCompliantMBeanException {
        StandardMBeanIntrospector.getInstance().getAnalyzer(interfaceClass);
    }

    public static synchronized MBeanInfo testCompliance(final Class<?> baseClass, Class<?> mbeanInterface) throws NotCompliantMBeanException {
        if (mbeanInterface == null)
            mbeanInterface = getStandardMBeanInterface(baseClass);
        ReflectUtil.checkPackageAccess(mbeanInterface);
        MBeanIntrospector<?> introspector = StandardMBeanIntrospector.getInstance();
        return getClassMBeanInfo(introspector, baseClass, mbeanInterface);
    }

    private static <M> MBeanInfo getClassMBeanInfo(MBeanIntrospector<M> introspector, Class<?> baseClass, Class<?> mbeanInterface) throws NotCompliantMBeanException {
        PerInterface<M> perInterface = introspector.getPerInterface(mbeanInterface);
        return introspector.getClassMBeanInfo(baseClass, perInterface);
    }

    public static Class<?> getMBeanInterface(Class<?> baseClass) {
        if (isDynamic(baseClass))
            return null;
        try {
            return getStandardMBeanInterface(baseClass);
        } catch (NotCompliantMBeanException e) {
            return null;
        }
    }

    public static <T> Class<? super T> getStandardMBeanInterface(Class<T> baseClass) throws NotCompliantMBeanException {
        Class<? super T> current = baseClass;
        Class<? super T> mbeanInterface = null;
        while (current != null) {
            mbeanInterface = findMBeanInterface(current, current.getName());
            if (mbeanInterface != null)
                break;
            current = current.getSuperclass();
        }
        if (mbeanInterface != null) {
            return mbeanInterface;
        } else {
            final String msg = "Class " + baseClass.getName() + " is not a JMX compliant Standard MBean";
            throw new NotCompliantMBeanException(msg);
        }
    }

    public static <T> Class<? super T> getMXBeanInterface(Class<T> baseClass) throws NotCompliantMBeanException {
        try {
            return MXBeanSupport.findMXBeanInterface(baseClass);
        } catch (Exception e) {
            throw throwException(baseClass, e);
        }
    }

    private static <T> Class<? super T> findMBeanInterface(Class<T> aClass, String aName) {
        Class<? super T> current = aClass;
        while (current != null) {
            final Class<?>[] interfaces = current.getInterfaces();
            final int len = interfaces.length;
            for (int i = 0; i < len; i++) {
                Class<? super T> inter = Util.cast(interfaces[i]);
                inter = implementsMBean(inter, aName);
                if (inter != null)
                    return inter;
            }
            current = current.getSuperclass();
        }
        return null;
    }

    public static Descriptor descriptorForElement(final AnnotatedElement elmt) {
        if (elmt == null)
            return ImmutableDescriptor.EMPTY_DESCRIPTOR;
        final Annotation[] annots = elmt.getAnnotations();
        return descriptorForAnnotations(annots);
    }

    public static Descriptor descriptorForAnnotations(Annotation[] annots) {
        if (annots.length == 0)
            return ImmutableDescriptor.EMPTY_DESCRIPTOR;
        Map<String, Object> descriptorMap = new HashMap<String, Object>();
        for (Annotation a : annots) {
            Class<? extends Annotation> c = a.annotationType();
            Method[] elements = c.getMethods();
            boolean packageAccess = false;
            for (Method element : elements) {
                DescriptorKey key = element.getAnnotation(DescriptorKey.class);
                if (key != null) {
                    String name = key.value();
                    Object value;
                    try {
                        if (!packageAccess) {
                            ReflectUtil.checkPackageAccess(c);
                            packageAccess = true;
                        }
                        value = MethodUtil.invoke(element, a, null);
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new UndeclaredThrowableException(e);
                    }
                    value = annotationToField(value);
                    Object oldValue = descriptorMap.put(name, value);
                    if (oldValue != null && !equals(oldValue, value)) {
                        final String msg = "Inconsistent values for descriptor field " + name + " from annotations: " + value + " :: " + oldValue;
                        throw new IllegalArgumentException(msg);
                    }
                }
            }
        }
        if (descriptorMap.isEmpty())
            return ImmutableDescriptor.EMPTY_DESCRIPTOR;
        else
            return new ImmutableDescriptor(descriptorMap);
    }

    static NotCompliantMBeanException throwException(Class<?> notCompliant, Throwable cause) throws NotCompliantMBeanException, SecurityException {
        if (cause instanceof SecurityException)
            throw (SecurityException) cause;
        if (cause instanceof NotCompliantMBeanException)
            throw (NotCompliantMBeanException) cause;
        final String classname = (notCompliant == null) ? "null class" : notCompliant.getName();
        final String reason = (cause == null) ? "Not compliant" : cause.getMessage();
        final NotCompliantMBeanException res = new NotCompliantMBeanException(classname + ": " + reason);
        res.initCause(cause);
        throw res;
    }

    private static Object annotationToField(Object x) {
        if (x == null)
            return null;
        if (x instanceof Number || x instanceof String || x instanceof Character || x instanceof Boolean || x instanceof String[])
            return x;
        Class<?> c = x.getClass();
        if (c.isArray()) {
            if (c.getComponentType().isPrimitive())
                return x;
            Object[] xx = (Object[]) x;
            String[] ss = new String[xx.length];
            for (int i = 0; i < xx.length; i++) ss[i] = (String) annotationToField(xx[i]);
            return ss;
        }
        if (x instanceof Class<?>)
            return ((Class<?>) x).getName();
        if (x instanceof Enum<?>)
            return ((Enum<?>) x).name();
        if (Proxy.isProxyClass(c))
            c = c.getInterfaces()[0];
        throw new IllegalArgumentException("Illegal type for annotation " + "element using @DescriptorKey: " + c.getName());
    }

    private static boolean equals(Object x, Object y) {
        return Arrays.deepEquals(new Object[] { x }, new Object[] { y });
    }

    private static <T> Class<? super T> implementsMBean(Class<T> c, String clName) {
        String clMBeanName = clName + "MBean";
        if (c.getName().equals(clMBeanName)) {
            return c;
        }
        Class<?>[] interfaces = c.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i].getName().equals(clMBeanName) && (Modifier.isPublic(interfaces[i].getModifiers()) || ALLOW_NONPUBLIC_MBEAN)) {
                return Util.cast(interfaces[i]);
            }
        }
        return null;
    }

    public static Object elementFromComplex(Object complex, String element) throws AttributeNotFoundException {
        try {
            if (complex.getClass().isArray() && element.equals("length")) {
                return Array.getLength(complex);
            } else if (complex instanceof CompositeData) {
                return ((CompositeData) complex).get(element);
            } else {
                Class<?> clazz = complex.getClass();
                Method readMethod = null;
                if (BeansHelper.isAvailable()) {
                    Object bi = BeansHelper.getBeanInfo(clazz);
                    Object[] pds = BeansHelper.getPropertyDescriptors(bi);
                    for (Object pd : pds) {
                        if (BeansHelper.getPropertyName(pd).equals(element)) {
                            readMethod = BeansHelper.getReadMethod(pd);
                            break;
                        }
                    }
                } else {
                    readMethod = SimpleIntrospector.getReadMethod(clazz, element);
                }
                if (readMethod != null) {
                    ReflectUtil.checkPackageAccess(readMethod.getDeclaringClass());
                    return MethodUtil.invoke(readMethod, complex, new Class[0]);
                }
                throw new AttributeNotFoundException("Could not find the getter method for the property " + element + " using the Java Beans introspector");
            }
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        } catch (AttributeNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw EnvHelp.initCause(new AttributeNotFoundException(e.getMessage()), e);
        }
    }

    private static class SimpleIntrospector {

        private SimpleIntrospector() {
        }

        private static final String GET_METHOD_PREFIX = "get";

        private static final String IS_METHOD_PREFIX = "is";

        private static final Map<Class<?>, SoftReference<List<Method>>> cache = Collections.synchronizedMap(new WeakHashMap<Class<?>, SoftReference<List<Method>>>());

        private static List<Method> getCachedMethods(Class<?> clazz) {
            SoftReference<List<Method>> ref = cache.get(clazz);
            if (ref != null) {
                List<Method> cached = ref.get();
                if (cached != null)
                    return cached;
            }
            return null;
        }

        static boolean isReadMethod(Method method) {
            int modifiers = method.getModifiers();
            if (Modifier.isStatic(modifiers))
                return false;
            String name = method.getName();
            Class<?>[] paramTypes = method.getParameterTypes();
            int paramCount = paramTypes.length;
            if (paramCount == 0 && name.length() > 2) {
                if (name.startsWith(IS_METHOD_PREFIX))
                    return (method.getReturnType() == boolean.class);
                if (name.length() > 3 && name.startsWith(GET_METHOD_PREFIX))
                    return (method.getReturnType() != void.class);
            }
            return false;
        }

        static List<Method> getReadMethods(Class<?> clazz) {
            List<Method> cachedResult = getCachedMethods(clazz);
            if (cachedResult != null)
                return cachedResult;
            List<Method> methods = StandardMBeanIntrospector.getInstance().getMethods(clazz);
            methods = MBeanAnalyzer.eliminateCovariantMethods(methods);
            List<Method> result = new LinkedList<Method>();
            for (Method m : methods) {
                if (isReadMethod(m)) {
                    if (m.getName().startsWith(IS_METHOD_PREFIX)) {
                        result.add(0, m);
                    } else {
                        result.add(m);
                    }
                }
            }
            cache.put(clazz, new SoftReference<List<Method>>(result));
            return result;
        }

        static Method getReadMethod(Class<?> clazz, String property) {
            property = property.substring(0, 1).toUpperCase(Locale.ENGLISH) + property.substring(1);
            String getMethod = GET_METHOD_PREFIX + property;
            String isMethod = IS_METHOD_PREFIX + property;
            for (Method m : getReadMethods(clazz)) {
                String name = m.getName();
                if (name.equals(isMethod) || name.equals(getMethod)) {
                    return m;
                }
            }
            return null;
        }
    }

    private static class BeansHelper {

        private static final Class<?> introspectorClass = getClass("java.beans.Introspector");

        private static final Class<?> beanInfoClass = (introspectorClass == null) ? null : getClass("java.beans.BeanInfo");

        private static final Class<?> getPropertyDescriptorClass = (beanInfoClass == null) ? null : getClass("java.beans.PropertyDescriptor");

        private static final Method getBeanInfo = getMethod(introspectorClass, "getBeanInfo", Class.class);

        private static final Method getPropertyDescriptors = getMethod(beanInfoClass, "getPropertyDescriptors");

        private static final Method getPropertyName = getMethod(getPropertyDescriptorClass, "getName");

        private static final Method getReadMethod = getMethod(getPropertyDescriptorClass, "getReadMethod");

        private static Class<?> getClass(String name) {
            try {
                return Class.forName(name, true, null);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        private static Method getMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
            if (clazz != null) {
                try {
                    return clazz.getMethod(name, paramTypes);
                } catch (NoSuchMethodException e) {
                    throw new AssertionError(e);
                }
            } else {
                return null;
            }
        }

        private BeansHelper() {
        }

        static boolean isAvailable() {
            return introspectorClass != null;
        }

        static Object getBeanInfo(Class<?> clazz) throws Exception {
            try {
                return getBeanInfo.invoke(null, clazz);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof Exception)
                    throw (Exception) cause;
                throw new AssertionError(e);
            } catch (IllegalAccessException iae) {
                throw new AssertionError(iae);
            }
        }

        static Object[] getPropertyDescriptors(Object bi) {
            try {
                return (Object[]) getPropertyDescriptors.invoke(bi);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException)
                    throw (RuntimeException) cause;
                throw new AssertionError(e);
            } catch (IllegalAccessException iae) {
                throw new AssertionError(iae);
            }
        }

        static String getPropertyName(Object pd) {
            try {
                return (String) getPropertyName.invoke(pd);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException)
                    throw (RuntimeException) cause;
                throw new AssertionError(e);
            } catch (IllegalAccessException iae) {
                throw new AssertionError(iae);
            }
        }

        static Method getReadMethod(Object pd) {
            try {
                return (Method) getReadMethod.invoke(pd);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException)
                    throw (RuntimeException) cause;
                throw new AssertionError(e);
            } catch (IllegalAccessException iae) {
                throw new AssertionError(iae);
            }
        }
    }
}