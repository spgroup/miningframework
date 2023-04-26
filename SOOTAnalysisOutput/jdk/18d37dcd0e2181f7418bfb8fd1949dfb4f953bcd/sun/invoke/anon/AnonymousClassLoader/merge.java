package sun.invoke.anon;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AnonymousClassLoader {

    final Class<?> hostClass;

    private AnonymousClassLoader(Class<?> hostClass) {
        this.hostClass = hostClass;
    }

    public static AnonymousClassLoader make(jdk.internal.misc.Unsafe unsafe, Class<?> hostClass) {
        if (unsafe == null)
            throw new NullPointerException();
        return new AnonymousClassLoader(hostClass);
    }

    public Class<?> loadClass(byte[] classFile) {
        if (defineAnonymousClass == null) {
            try {
                return fakeLoadClass(new ConstantPoolParser(classFile).createPatch());
            } catch (InvalidConstantPoolFormatException ee) {
                throw new IllegalArgumentException(ee);
            }
        }
        return loadClass(classFile, null);
    }

    public Class<?> loadClass(ConstantPoolPatch classPatch) {
        if (defineAnonymousClass == null) {
            return fakeLoadClass(classPatch);
        }
        Object[] patches = classPatch.patchArray;
        for (int i = 0; i < patches.length; i++) {
            Object value = patches[i];
            if (value != null) {
                byte tag = classPatch.getTag(i);
                switch(tag) {
                    case ConstantPoolVisitor.CONSTANT_Class:
                        if (value instanceof String) {
                            if (patches == classPatch.patchArray)
                                patches = patches.clone();
                            patches[i] = ((String) value).replace('.', '/');
                        }
                        break;
                    case ConstantPoolVisitor.CONSTANT_Fieldref:
                    case ConstantPoolVisitor.CONSTANT_Methodref:
                    case ConstantPoolVisitor.CONSTANT_InterfaceMethodref:
                    case ConstantPoolVisitor.CONSTANT_NameAndType:
                        break;
                }
            }
        }
        return loadClass(classPatch.outer.classFile, classPatch.patchArray);
    }

    private Class<?> loadClass(byte[] classFile, Object[] patchArray) {
        try {
            return (Class<?>) defineAnonymousClass.invoke(unsafe, hostClass, classFile, patchArray);
        } catch (Exception ex) {
            throwReflectedException(ex);
            throw new RuntimeException("error loading into " + hostClass, ex);
        }
    }

    private static void throwReflectedException(Exception ex) {
        if (ex instanceof InvocationTargetException) {
            Throwable tex = ((InvocationTargetException) ex).getTargetException();
            if (tex instanceof Error)
                throw (Error) tex;
            ex = (Exception) tex;
        }
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        }
    }

    private Class<?> fakeLoadClass(ConstantPoolPatch classPatch) {
        if (true)
            throw new UnsupportedOperationException("NYI");
        Object[] cpArray;
        try {
            cpArray = classPatch.getOriginalCP();
        } catch (InvalidConstantPoolFormatException ex) {
            throw new RuntimeException(ex);
        }
        int thisClassIndex = classPatch.getParser().getThisClassIndex();
        String thisClassName = (String) cpArray[thisClassIndex];
        synchronized (AnonymousClassLoader.class) {
            thisClassName = thisClassName + "\\|" + (++fakeNameCounter);
        }
        classPatch.putUTF8(thisClassIndex, thisClassName);
        byte[] classFile = null;
        return unsafe.defineClass(null, classFile, 0, classFile.length, hostClass.getClassLoader(), hostClass.getProtectionDomain());
    }

    private static int fakeNameCounter = 99999;

    private static jdk.internal.misc.Unsafe unsafe = jdk.internal.misc.Unsafe.getUnsafe();

    private static final Method defineAnonymousClass;

    static {
        Method dac = null;
        Class<? extends jdk.internal.misc.Unsafe> unsafeClass = unsafe.getClass();
        try {
            dac = unsafeClass.getMethod("defineAnonymousClass", Class.class, byte[].class, Object[].class);
        } catch (Exception ee) {
            dac = null;
        }
        defineAnonymousClass = dac;
    }

    private static void noJVMSupport() {
        throw new UnsupportedOperationException("no JVM support for anonymous classes");
    }

    private static native Class<?> loadClassInternal(Class<?> hostClass, byte[] classFile, Object[] patchArray);

    public static byte[] readClassFile(Class<?> templateClass) throws IOException {
        String templateName = templateClass.getName();
        int lastDot = templateName.lastIndexOf('.');
        java.net.URL url = templateClass.getResource(templateName.substring(lastDot + 1) + ".class");
        java.net.URLConnection connection = url.openConnection();
        int contentLength = connection.getContentLength();
        if (contentLength < 0)
            throw new IOException("invalid content length " + contentLength);
        byte[] b = connection.getInputStream().readAllBytes();
        if (b.length != contentLength)
            throw new EOFException("Expected:" + contentLength + ", read:" + b.length);
        return b;
    }
}
