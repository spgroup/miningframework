package java.lang.reflect;

import java.security.AccessController;
import java.util.StringJoiner;
import jdk.internal.reflect.LangReflectAccess;
import jdk.internal.reflect.ReflectionFactory;

public class Modifier {

    static {
        ReflectionFactory factory = AccessController.doPrivileged(new ReflectionFactory.GetReflectionFactoryAction());
        factory.setLangReflectAccess(new java.lang.reflect.ReflectAccess());
    }

    public static boolean isPublic(int mod) {
        return (mod & PUBLIC) != 0;
    }

    public static boolean isPrivate(int mod) {
        return (mod & PRIVATE) != 0;
    }

    public static boolean isProtected(int mod) {
        return (mod & PROTECTED) != 0;
    }

    public static boolean isStatic(int mod) {
        return (mod & STATIC) != 0;
    }

    public static boolean isFinal(int mod) {
        return (mod & FINAL) != 0;
    }

    public static boolean isSynchronized(int mod) {
        return (mod & SYNCHRONIZED) != 0;
    }

    public static boolean isVolatile(int mod) {
        return (mod & VOLATILE) != 0;
    }

    public static boolean isTransient(int mod) {
        return (mod & TRANSIENT) != 0;
    }

    public static boolean isNative(int mod) {
        return (mod & NATIVE) != 0;
    }

    public static boolean isInterface(int mod) {
        return (mod & INTERFACE) != 0;
    }

    public static boolean isAbstract(int mod) {
        return (mod & ABSTRACT) != 0;
    }

    public static boolean isStrict(int mod) {
        return (mod & STRICT) != 0;
    }

    public static String toString(int mod) {
        StringJoiner sj = new StringJoiner(" ");
        if ((mod & PUBLIC) != 0)
            sj.add("public");
        if ((mod & PROTECTED) != 0)
            sj.add("protected");
        if ((mod & PRIVATE) != 0)
            sj.add("private");
        if ((mod & ABSTRACT) != 0)
            sj.add("abstract");
        if ((mod & STATIC) != 0)
            sj.add("static");
        if ((mod & FINAL) != 0)
            sj.add("final");
        if ((mod & TRANSIENT) != 0)
            sj.add("transient");
        if ((mod & VOLATILE) != 0)
            sj.add("volatile");
        if ((mod & SYNCHRONIZED) != 0)
            sj.add("synchronized");
        if ((mod & NATIVE) != 0)
            sj.add("native");
        if ((mod & STRICT) != 0)
            sj.add("strictfp");
        if ((mod & INTERFACE) != 0)
            sj.add("interface");
        return sj.toString();
    }

    public static final int PUBLIC = 0x00000001;

    public static final int PRIVATE = 0x00000002;

    public static final int PROTECTED = 0x00000004;

    public static final int STATIC = 0x00000008;

    public static final int FINAL = 0x00000010;

    public static final int SYNCHRONIZED = 0x00000020;

    public static final int VOLATILE = 0x00000040;

    public static final int TRANSIENT = 0x00000080;

    public static final int NATIVE = 0x00000100;

    public static final int INTERFACE = 0x00000200;

    public static final int ABSTRACT = 0x00000400;

    public static final int STRICT = 0x00000800;

    static final int BRIDGE = 0x00000040;

    static final int VARARGS = 0x00000080;

    static final int SYNTHETIC = 0x00001000;

    static final int ANNOTATION = 0x00002000;

    static final int ENUM = 0x00004000;

    static final int MANDATED = 0x00008000;

    static boolean isSynthetic(int mod) {
        return (mod & SYNTHETIC) != 0;
    }

    static boolean isMandated(int mod) {
        return (mod & MANDATED) != 0;
    }

    private static final int CLASS_MODIFIERS = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE | Modifier.ABSTRACT | Modifier.STATIC | Modifier.FINAL | Modifier.STRICT;

    private static final int INTERFACE_MODIFIERS = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE | Modifier.ABSTRACT | Modifier.STATIC | Modifier.STRICT;

    private static final int CONSTRUCTOR_MODIFIERS = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE;

    private static final int METHOD_MODIFIERS = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE | Modifier.ABSTRACT | Modifier.STATIC | Modifier.FINAL | Modifier.SYNCHRONIZED | Modifier.NATIVE | Modifier.STRICT;

    private static final int FIELD_MODIFIERS = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL | Modifier.TRANSIENT | Modifier.VOLATILE;

    private static final int PARAMETER_MODIFIERS = Modifier.FINAL;

    static final int ACCESS_MODIFIERS = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE;

    public static int classModifiers() {
        return CLASS_MODIFIERS;
    }

    public static int interfaceModifiers() {
        return INTERFACE_MODIFIERS;
    }

    public static int constructorModifiers() {
        return CONSTRUCTOR_MODIFIERS;
    }

    public static int methodModifiers() {
        return METHOD_MODIFIERS;
    }

    public static int fieldModifiers() {
        return FIELD_MODIFIERS;
    }

    public static int parameterModifiers() {
        return PARAMETER_MODIFIERS;
    }
}
