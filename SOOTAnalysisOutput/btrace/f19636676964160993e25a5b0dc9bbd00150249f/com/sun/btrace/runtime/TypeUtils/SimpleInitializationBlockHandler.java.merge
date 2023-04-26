package com.sun.btrace.runtime;

import com.sun.btrace.AnyType;
import static com.sun.btrace.runtime.Constants.*;
import static com.sun.btrace.org.objectweb.asm.Opcodes.*;
import java.util.Map;
import java.util.HashMap;
import java.util.StringTokenizer;
import com.sun.btrace.org.objectweb.asm.Type;

public final class TypeUtils {

    private TypeUtils() {
    }

    public static final Type throwableType = Type.getType(Throwable.class);

    public static final Type objectArrayType = Type.getType(Object[].class);

    public static final Type anyTypeArray = Type.getType(AnyType[].class);

    public static boolean isPrimitive(Type t) {
        return t == Type.BOOLEAN_TYPE || t == Type.BYTE_TYPE || t == Type.CHAR_TYPE || t == Type.DOUBLE_TYPE || t == Type.FLOAT_TYPE || t == Type.INT_TYPE || t == Type.LONG_TYPE || t == Type.SHORT_TYPE;
    }

    public static boolean isAnyType(Type t) {
        return t.equals(ANYTYPE_TYPE);
    }

    public static boolean isAnyTypeArray(Type t) {
        return t.equals(anyTypeArray);
    }

    public static boolean isObject(Type t) {
        return t.equals(OBJECT_TYPE);
    }

    public static boolean isObjectOrAnyType(Type t) {
        return isObject(t) || isAnyType(t);
    }

    public static boolean isString(Type t) {
        return t.equals(STRING_TYPE);
    }

    public static boolean isArray(Type t) {
        return t.getSort() == Type.ARRAY;
    }

    public static boolean isThrowable(Type t) {
        return t.equals(throwableType);
    }

    public static boolean isVoid(Type t) {
        return t == Type.VOID_TYPE || VOIDREF_TYPE.equals(t);
    }

    public static boolean isCompatible(Type left, Type right) {
        if (left.equals(right)) {
            return true;
        } else if (isVoid(left)) {
            return isVoid(right);
        } else if (isArray(left)) {
            return false;
        } else if (isObjectOrAnyType(left)) {
            int sort2 = right.getSort();
            return (sort2 == Type.OBJECT || sort2 == Type.ARRAY || sort2 == Type.VOID || isPrimitive(right));
        } else if (isPrimitive(left)) {
            return left.equals(right);
        } else {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                cl = ClassLoader.getSystemClassLoader();
            }
            Class clzLeft, clzRight;
            try {
                clzLeft = cl.loadClass(left.getClassName());
            } catch (Throwable e) {
                clzLeft = Object.class;
            }
            if (clzLeft == Object.class) {
                return true;
            }
            try {
                clzRight = cl.loadClass(right.getClassName());
            } catch (Throwable e) {
                clzRight = Object.class;
            }
            return (clzLeft.isAssignableFrom(clzRight));
        }
    }

    public static boolean isCompatible(Type[] args1, Type[] args2) {
        if (args1.length != args2.length) {
            return false;
        }
        for (int i = 0; i < args1.length; i++) {
            if (!args1[i].equals(args2[i])) {
                int sort2 = args2[i].getSort();
                if (isAnyType(args1[i]) && (sort2 == Type.OBJECT || sort2 == Type.ARRAY || isPrimitive(args2[i]))) {
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    public static Type getArrayType(int arrayOpcode) {
        switch(arrayOpcode) {
            case IALOAD:
            case IASTORE:
                return Type.getType("[I");
            case BALOAD:
            case BASTORE:
                return Type.getType("[B");
            case AALOAD:
            case AASTORE:
                return objectArrayType;
            case CALOAD:
            case CASTORE:
                return Type.getType("[C");
            case FALOAD:
            case FASTORE:
                return Type.getType("[F");
            case SALOAD:
            case SASTORE:
                return Type.getType("[S");
            case LALOAD:
            case LASTORE:
                return Type.getType("[J");
            case DALOAD:
            case DASTORE:
                return Type.getType("[D");
            default:
                throw new RuntimeException("invalid array opcode");
        }
    }

    public static Type getElementType(int arrayOpcode) {
        switch(arrayOpcode) {
            case IALOAD:
            case IASTORE:
                return Type.INT_TYPE;
            case BALOAD:
            case BASTORE:
                return Type.BYTE_TYPE;
            case AALOAD:
            case AASTORE:
                return OBJECT_TYPE;
            case CALOAD:
            case CASTORE:
                return Type.CHAR_TYPE;
            case FALOAD:
            case FASTORE:
                return Type.FLOAT_TYPE;
            case SALOAD:
            case SASTORE:
                return Type.SHORT_TYPE;
            case LALOAD:
            case LASTORE:
                return Type.LONG_TYPE;
            case DALOAD:
            case DASTORE:
                return Type.DOUBLE_TYPE;
            default:
                throw new RuntimeException("invalid array opcode");
        }
    }

    private static final Map<String, String> primitives;

    static {
        primitives = new HashMap<>();
        primitives.put("void", "V");
        primitives.put("byte", "B");
        primitives.put("char", "C");
        primitives.put("double", "D");
        primitives.put("float", "F");
        primitives.put("int", "I");
        primitives.put("long", "J");
        primitives.put("short", "S");
        primitives.put("boolean", "Z");
    }

    public static String declarationToDescriptor(String decl) {
        int leftParen = decl.indexOf('(');
        int rightParen = decl.indexOf(')');
        if (leftParen == -1 || rightParen == -1) {
            throw new IllegalArgumentException();
        }
        StringBuilder buf = new StringBuilder();
        String descriptor;
        buf.append('(');
        String args = decl.substring(leftParen + 1, rightParen);
        StringTokenizer st = new StringTokenizer(args, ",");
        while (st.hasMoreTokens()) {
            String arg = st.nextToken().trim();
            descriptor = primitives.get(arg);
            if (arg.length() == 0) {
                throw new IllegalArgumentException();
            }
            if (descriptor == null) {
                descriptor = objectOrArrayType(arg);
            }
            buf.append(descriptor);
        }
        buf.append(')');
        String returnType = decl.substring(0, leftParen).trim();
        descriptor = primitives.get(returnType);
        if (returnType.length() == 0) {
            throw new IllegalArgumentException();
        }
        if (descriptor == null) {
            descriptor = objectOrArrayType(returnType);
        }
        buf.append(descriptor);
        return buf.toString();
    }

    public static String descriptorToSimplified(String desc, String owner, String name) {
        Type retType = Type.getReturnType(desc);
        Type[] args = desc.contains("(") ? Type.getArgumentTypes(desc) : new Type[0];
        StringBuilder sb = new StringBuilder();
        sb.append(getJavaType(retType.getDescriptor())).append(' ').append(owner.replace('/', '.')).append('#').append(name);
        if (args.length > 0) {
            sb.append("(");
            boolean more = false;
            for (Type t : args) {
                if (more) {
                    sb.append(", ");
                } else {
                    more = true;
                }
                sb.append(getJavaType(t.getDescriptor()));
            }
            sb.append(')');
        }
        return sb.toString();
    }

    public static String getJavaType(String desc) {
        int arrIndex = desc.lastIndexOf("[") + 1;
        desc = desc.substring(arrIndex);
        if (desc.startsWith("L")) {
            desc = desc.substring(1, desc.length() - 1).replace('/', '.');
        } else {
            for (Map.Entry<String, String> entry : primitives.entrySet()) {
                if (entry.getValue().equals(desc)) {
                    desc = entry.getKey();
                    break;
                }
            }
        }
        StringBuilder sb = new StringBuilder(desc);
        for (int i = 0; i < arrIndex; i++) {
            sb.append("[]");
        }
        return sb.toString();
    }

    public static String objectOrArrayType(String type) {
        StringBuilder buf = new StringBuilder();
        int index = 0;
        while ((index = type.indexOf("[]", index) + 1) > 0) {
            buf.append('[');
        }
        String t = type.substring(0, type.length() - buf.length() * 2);
        String desc = primitives.get(t);
        if (desc != null) {
            buf.append(desc);
        } else {
            buf.append('L');
            if (t.indexOf('.') < 0) {
                buf.append(t);
            } else {
                buf.append(t.replace('.', '/'));
            }
            buf.append(';');
        }
        return buf.toString();
    }
}