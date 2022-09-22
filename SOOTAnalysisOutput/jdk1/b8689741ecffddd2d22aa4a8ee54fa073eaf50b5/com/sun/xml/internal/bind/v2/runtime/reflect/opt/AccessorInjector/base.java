package com.sun.xml.internal.bind.v2.runtime.reflect.opt;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.internal.bind.Util;
import com.sun.xml.internal.bind.v2.bytecode.ClassTailor;

class AccessorInjector {

    private static final Logger logger = Util.getClassLogger();

    protected static final boolean noOptimize = Util.getSystemProperty(ClassTailor.class.getName() + ".noOptimize") != null;

    static {
        if (noOptimize)
            logger.info("The optimized code generation is disabled");
    }

    public static Class<?> prepare(Class beanClass, String templateClassName, String newClassName, String... replacements) {
        if (noOptimize)
            return null;
        try {
            ClassLoader cl = SecureLoader.getClassClassLoader(beanClass);
            if (cl == null)
                return null;
            Class c = Injector.find(cl, newClassName);
            if (c == null) {
                byte[] image = tailor(templateClassName, newClassName, replacements);
                if (image == null) {
                    return null;
                }
                c = Injector.inject(cl, newClassName, image);
                if (c == null) {
                    Injector.find(cl, newClassName);
                }
            }
            return c;
        } catch (SecurityException e) {
            logger.log(Level.INFO, "Unable to create an optimized TransducedAccessor ", e);
            return null;
        }
    }

    private static byte[] tailor(String templateClassName, String newClassName, String... replacements) {
        InputStream resource;
        if (CLASS_LOADER != null)
            resource = CLASS_LOADER.getResourceAsStream(templateClassName + ".class");
        else
            resource = ClassLoader.getSystemResourceAsStream(templateClassName + ".class");
        if (resource == null)
            return null;
        return ClassTailor.tailor(resource, templateClassName, newClassName, replacements);
    }

    private static final ClassLoader CLASS_LOADER = SecureLoader.getClassClassLoader(AccessorInjector.class);
}
