package com.sun.btrace.runtime;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.sun.btrace.org.objectweb.asm.Opcodes.*;
import static com.sun.btrace.runtime.Constants.*;
import com.sun.btrace.BTraceRuntime;
import com.sun.btrace.annotations.Export;
import com.sun.btrace.annotations.ServiceType;
import static com.sun.btrace.annotations.ServiceType.RUNTIME;
import static com.sun.btrace.annotations.ServiceType.SIMPLE;
import com.sun.btrace.annotations.TLS;
import com.sun.btrace.annotations.Property;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import com.sun.btrace.org.objectweb.asm.AnnotationVisitor;
import com.sun.btrace.org.objectweb.asm.Attribute;
import com.sun.btrace.org.objectweb.asm.ClassReader;
import com.sun.btrace.org.objectweb.asm.ClassVisitor;
import com.sun.btrace.org.objectweb.asm.ClassWriter;
import com.sun.btrace.org.objectweb.asm.FieldVisitor;
import com.sun.btrace.org.objectweb.asm.Label;
import com.sun.btrace.org.objectweb.asm.MethodVisitor;
import com.sun.btrace.org.objectweb.asm.Opcodes;
import com.sun.btrace.org.objectweb.asm.Type;
import com.sun.btrace.services.spi.RuntimeService;
import com.sun.btrace.services.spi.SimpleService;
import com.sun.btrace.util.LocalVariableHelperImpl;

public class Preprocessor extends ClassVisitor {

    public static final String JAVA_LANG_THREAD_LOCAL_DESC = "Ljava/lang/ThreadLocal;";

    public static final String BTRACE_EXPORT_DESC = Type.getDescriptor(Export.class);

    public static final String BTRACE_TLS_DESC = Type.getDescriptor(TLS.class);

    public static final String BTRACE_PROPERTY_DESC = Type.getDescriptor(Property.class);

    public static final String BTRACE_PROPERTY_NAME = "name";

    public static final String BTRACE_PROPERTY_DESCRIPTION = "description";

    public static final String BTRACE_RUNTIME = Type.getInternalName(BTraceRuntime.class);

    public static final String BTRACE_RUNTIME_DESC = Type.getDescriptor(BTraceRuntime.class);

    public static final String BTRACE_RUNTIME_FIELD_NAME = "runtime";

    public static final String BTRACE_FIELD_PREFIX = "$";

    public static final String BTRACE_RUNTIME_HANDLE_EXCEPTION;

    public static final String BTRACE_RUNTIME_HANDLE_EXCEPTION_DESC;

    public static final String BTRACE_RUNTIME_ENTER;

    public static final String BTRACE_RUNTIME_ENTER_DESC;

    public static final String BTRACE_RUNTIME_LEAVE;

    public static final String BTRACE_RUNTIME_LEAVE_DESC;

    public static final String BTRACE_RUNTIME_START;

    public static final String BTRACE_RUNTIME_START_DESC;

    public static final String BTRACE_RUNTIME_FOR_CLASS;

    public static final String BTRACE_RUNTIME_FOR_CLASS_DESC;

    public static final String BTRACE_RUNTIME_NEW_THREAD_LOCAL;

    public static final String BTRACE_RUNTIME_NEW_THREAD_LOCAL_DESC;

    public static final String BTRACE_RUNTIME_NEW_PERFCOUNTER;

    public static final String BTRACE_RUNTIME_NEW_PERFCOUNTER_DESC;

    public static final String BTRACE_RUNTIME_GET_PERFSTRING;

    public static final String BTRACE_RUNTIME_GET_PERFSTRING_DESC;

    public static final String BTRACE_RUNTIME_GET_PERFINT;

    public static final String BTRACE_RUNTIME_GET_PERFINT_DESC;

    public static final String BTRACE_RUNTIME_GET_PERFLONG;

    public static final String BTRACE_RUNTIME_GET_PERFLONG_DESC;

    public static final String BTRACE_RUNTIME_GET_PERFFLOAT;

    public static final String BTRACE_RUNTIME_GET_PERFFLOAT_DESC;

    public static final String BTRACE_RUNTIME_GET_PERFDOUBLE;

    public static final String BTRACE_RUNTIME_GET_PERFDOUBLE_DESC;

    public static final String BTRACE_RUNTIME_PUT_PERFSTRING;

    public static final String BTRACE_RUNTIME_PUT_PERFSTRING_DESC;

    public static final String BTRACE_RUNTIME_PUT_PERFINT;

    public static final String BTRACE_RUNTIME_PUT_PERFINT_DESC;

    public static final String BTRACE_RUNTIME_PUT_PERFLONG;

    public static final String BTRACE_RUNTIME_PUT_PERFLONG_DESC;

    public static final String BTRACE_RUNTIME_PUT_PERFFLOAT;

    public static final String BTRACE_RUNTIME_PUT_PERFFLOAT_DESC;

    public static final String BTRACE_RUNTIME_PUT_PERFDOUBLE;

    public static final String BTRACE_RUNTIME_PUT_PERFDOUBLE_DESC;

    static {
        try {
            Method handleException = BTraceRuntime.class.getMethod("handleException", new Class[] { Throwable.class });
            BTRACE_RUNTIME_HANDLE_EXCEPTION = handleException.getName();
            BTRACE_RUNTIME_HANDLE_EXCEPTION_DESC = Type.getMethodDescriptor(handleException);
            Method enter = BTraceRuntime.class.getMethod("enter", new Class[] { BTraceRuntime.class });
            BTRACE_RUNTIME_ENTER = enter.getName();
            BTRACE_RUNTIME_ENTER_DESC = Type.getMethodDescriptor(enter);
            Method leave = BTraceRuntime.class.getMethod("leave", new Class[0]);
            BTRACE_RUNTIME_LEAVE = leave.getName();
            BTRACE_RUNTIME_LEAVE_DESC = Type.getMethodDescriptor(leave);
            Method start = BTraceRuntime.class.getMethod("start", new Class[0]);
            BTRACE_RUNTIME_START = start.getName();
            BTRACE_RUNTIME_START_DESC = Type.getMethodDescriptor(start);
            Method forClass = BTraceRuntime.class.getMethod("forClass", new Class[] { Class.class });
            BTRACE_RUNTIME_FOR_CLASS = forClass.getName();
            BTRACE_RUNTIME_FOR_CLASS_DESC = Type.getMethodDescriptor(forClass);
            Method newThreadLocal = BTraceRuntime.class.getMethod("newThreadLocal", new Class[] { Object.class });
            BTRACE_RUNTIME_NEW_THREAD_LOCAL = newThreadLocal.getName();
            BTRACE_RUNTIME_NEW_THREAD_LOCAL_DESC = Type.getMethodDescriptor(newThreadLocal);
            Method newPerfCounter = BTraceRuntime.class.getMethod("newPerfCounter", new Class[] { String.class, String.class, Object.class });
            BTRACE_RUNTIME_NEW_PERFCOUNTER = newPerfCounter.getName();
            BTRACE_RUNTIME_NEW_PERFCOUNTER_DESC = Type.getMethodDescriptor(newPerfCounter);
            Method getPerfString = BTraceRuntime.class.getMethod("getPerfString", new Class[] { String.class });
            BTRACE_RUNTIME_GET_PERFSTRING = getPerfString.getName();
            BTRACE_RUNTIME_GET_PERFSTRING_DESC = Type.getMethodDescriptor(getPerfString);
            Method getPerfInt = BTraceRuntime.class.getMethod("getPerfInt", new Class[] { String.class });
            BTRACE_RUNTIME_GET_PERFINT = getPerfInt.getName();
            BTRACE_RUNTIME_GET_PERFINT_DESC = Type.getMethodDescriptor(getPerfInt);
            Method getPerfLong = BTraceRuntime.class.getMethod("getPerfLong", new Class[] { String.class });
            BTRACE_RUNTIME_GET_PERFLONG = getPerfLong.getName();
            BTRACE_RUNTIME_GET_PERFLONG_DESC = Type.getMethodDescriptor(getPerfLong);
            Method getPerfFloat = BTraceRuntime.class.getMethod("getPerfFloat", new Class[] { String.class });
            BTRACE_RUNTIME_GET_PERFFLOAT = getPerfFloat.getName();
            BTRACE_RUNTIME_GET_PERFFLOAT_DESC = Type.getMethodDescriptor(getPerfFloat);
            Method getPerfDouble = BTraceRuntime.class.getMethod("getPerfDouble", new Class[] { String.class });
            BTRACE_RUNTIME_GET_PERFDOUBLE = getPerfDouble.getName();
            BTRACE_RUNTIME_GET_PERFDOUBLE_DESC = Type.getMethodDescriptor(getPerfDouble);
            Method putPerfString = BTraceRuntime.class.getMethod("putPerfString", new Class[] { String.class, String.class });
            BTRACE_RUNTIME_PUT_PERFSTRING = putPerfString.getName();
            BTRACE_RUNTIME_PUT_PERFSTRING_DESC = Type.getMethodDescriptor(putPerfString);
            Method putPerfInt = BTraceRuntime.class.getMethod("putPerfInt", new Class[] { int.class, String.class });
            BTRACE_RUNTIME_PUT_PERFINT = putPerfInt.getName();
            BTRACE_RUNTIME_PUT_PERFINT_DESC = Type.getMethodDescriptor(putPerfInt);
            Method putPerfLong = BTraceRuntime.class.getMethod("putPerfLong", new Class[] { long.class, String.class });
            BTRACE_RUNTIME_PUT_PERFLONG = putPerfLong.getName();
            BTRACE_RUNTIME_PUT_PERFLONG_DESC = Type.getMethodDescriptor(putPerfLong);
            Method putPerfFloat = BTraceRuntime.class.getMethod("putPerfFloat", new Class[] { float.class, String.class });
            BTRACE_RUNTIME_PUT_PERFFLOAT = putPerfFloat.getName();
            BTRACE_RUNTIME_PUT_PERFFLOAT_DESC = Type.getMethodDescriptor(putPerfFloat);
            Method putPerfDouble = BTraceRuntime.class.getMethod("putPerfDouble", new Class[] { double.class, String.class });
            BTRACE_RUNTIME_PUT_PERFDOUBLE = putPerfDouble.getName();
            BTRACE_RUNTIME_PUT_PERFDOUBLE_DESC = Type.getMethodDescriptor(putPerfDouble);
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    private String className;

    private String superName;

    private String externalClassName;

    private Type classType;

    private final List<FieldDescriptor> fields;

    private final Map<String, FieldDescriptor> threadLocalFields;

    private final Map<String, FieldDescriptor> exportFields;

    private final Map<String, FieldDescriptor> injectedFields;

    private boolean classInitializerFound;

    public Preprocessor(ClassVisitor cv) {
        super(Opcodes.ASM4, cv);
        fields = new ArrayList<FieldDescriptor>();
        threadLocalFields = new HashMap<String, FieldDescriptor>();
        exportFields = new HashMap<String, FieldDescriptor>();
        injectedFields = new HashMap<String, FieldDescriptor>();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        className = name;
        this.superName = superName;
        classType = Type.getObjectType(className);
        classInitializerFound = false;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        final AnnotationVisitor zupr = super.visitAnnotation(desc, visible);
        if ("Lcom/sun/btrace/annotations/BTrace;".equals(desc)) {
            return new AnnotationVisitor(Opcodes.ASM4) {

                @Override
                public void visit(String string, Object o) {
                    if ("name".equals(string)) {
                        externalClassName = (String) o;
                    }
                    zupr.visit(string, o);
                }

                @Override
                public void visitEnum(String string, String string1, String string2) {
                    zupr.visitEnum(string, string1, string2);
                }

                @Override
                public AnnotationVisitor visitAnnotation(String string, String string1) {
                    return zupr.visitAnnotation(string, string1);
                }

                @Override
                public AnnotationVisitor visitArray(String string) {
                    return zupr.visitArray(string);
                }

                @Override
                public void visitEnd() {
                    zupr.visitEnd();
                }
            };
        }
        return zupr;
    }

    private String externalClassName() {
        if (externalClassName == null) {
            externalClassName = className.replace('/', '.');
        }
        return externalClassName;
    }

    private static final String BTRACE_COUNTER_PREFIX = "btrace.";

    private String perfCounterName(String fieldName) {
        return BTRACE_COUNTER_PREFIX + externalClassName() + "." + fieldName;
    }

    private static class FieldDescriptor {

        int access;

        String name, desc, signature;

        Object value;

        List<Attribute> attributes;

        boolean isThreadLocal;

        boolean isExport;

        boolean isProperty;

        boolean isInjected;

        String propertyName;

        String propertyDescription;

        int var = -1;

        boolean initialized;

        FieldDescriptor(int acc, String n, String d, String sig, Object val, List<Attribute> attrs, boolean tls, boolean isExp, boolean isProp, boolean isInj, String propName, String propDescription) {
            access = acc;
            name = n;
            desc = d;
            signature = sig;
            value = val;
            attributes = attrs;
            isThreadLocal = tls;
            isExport = isExp;
            isProperty = isProp;
            isInjected = isInj;
            propertyName = propName;
            propertyDescription = propDescription;
        }
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {
        final List<Attribute> attrs = new ArrayList<Attribute>();
        return new FieldVisitor(Opcodes.ASM4) {

            boolean isExport;

            boolean isThreadLocal;

            boolean isProperty;

            boolean isInjected;

            String propName = "";

            String propDescription = "";

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (desc.equals(BTRACE_TLS_DESC)) {
                    isThreadLocal = true;
                } else if (desc.equals(BTRACE_EXPORT_DESC)) {
                    isExport = true;
                } else if (desc.equals(BTRACE_PROPERTY_DESC)) {
                    isProperty = true;
                    return new AnnotationVisitor(Opcodes.ASM5) {

                        @Override
                        public void visit(String name, Object value) {
                            super.visit(name, value);
                            if (name.equals(BTRACE_PROPERTY_NAME)) {
                                propName = value.toString();
                            } else if (name.equals(BTRACE_PROPERTY_DESCRIPTION)) {
                                propDescription = value.toString();
                            }
                        }
                    };
                } else if (desc.equals(INJECTED_DESC)) {
                    isInjected = true;
                    return new AnnotationVisitor(Opcodes.ASM5) {

                        @Override
                        public void visit(String name, Object value) {
                            super.visit(name, value);
                            if (name.equals("factoryMethod")) {
                                propName = value.toString();
                            }
                        }

                        @Override
                        public void visitEnum(String name, String desc, String value) {
                            super.visitEnum(name, desc, value);
                            if (name.equals("value")) {
                                ServiceType sk = ServiceType.valueOf((String) value);
                                switch(sk) {
                                    case SIMPLE:
                                        {
                                            propDescription = Type.getInternalName(SimpleService.class);
                                            break;
                                        }
                                    case RUNTIME:
                                        {
                                            propDescription = Type.getInternalName(RuntimeService.class);
                                            break;
                                        }
                                }
                            }
                        }
                    };
                }
                return new AnnotationVisitor(Opcodes.ASM5) {
                };
            }

            @Override
            public void visitAttribute(Attribute attr) {
                attrs.add(attr);
            }

            @Override
            public void visitEnd() {
                FieldDescriptor fd = new FieldDescriptor(access, name, desc, signature, value, attrs, isThreadLocal, isExport, isProperty, isInjected, propName, propDescription);
                fields.add(fd);
                if (isThreadLocal) {
                    threadLocalFields.put(name, fd);
                } else if (isExport) {
                    exportFields.put(name, fd);
                } else if (isInjected) {
                    injectedFields.put(name, fd);
                }
            }
        };
    }

    @Override
    public void visitEnd() {
        if (!classInitializerFound) {
            MethodVisitor clinit = visitMethod(ACC_STATIC | ACC_PUBLIC, CLASS_INITIALIZER, "()V", null, null);
            clinit.visitCode();
            clinit.visitInsn(RETURN);
            clinit.visitMaxs(0, 0);
            clinit.visitEnd();
        }
        addFields();
        super.visitEnd();
    }

    private void addFields() {
        for (FieldDescriptor fd : fields) {
            String fieldName = fd.name;
            if (fd.isExport) {
                continue;
            }
            int fieldAccess = fd.access;
            String fieldDesc = fd.desc;
            String fieldSignature = fd.signature;
            Object fieldValue = fd.value;
            if (fd.isThreadLocal) {
                fieldAccess &= ~ACC_FINAL;
                fieldDesc = JAVA_LANG_THREAD_LOCAL_DESC;
                fieldSignature = null;
                fieldValue = null;
            }
            fieldAccess &= ~ACC_PRIVATE;
            fieldAccess &= ~ACC_PROTECTED;
            fieldAccess |= ACC_PUBLIC;
            FieldVisitor fv = super.visitField(fieldAccess, BTRACE_FIELD_PREFIX + fieldName, fieldDesc, fieldSignature, fieldValue);
            if (fd.isProperty) {
                AnnotationVisitor av = fv.visitAnnotation(BTRACE_PROPERTY_DESC, true);
                if (av != null) {
                    av.visit(BTRACE_PROPERTY_NAME, fd.propertyName);
                    av.visit(BTRACE_PROPERTY_DESCRIPTION, fd.propertyDescription);
                }
            }
            for (Attribute attr : fd.attributes) {
                fv.visitAttribute(attr);
            }
            fv.visitEnd();
        }
        super.visitField(ACC_PUBLIC | ACC_STATIC, BTRACE_RUNTIME_FIELD_NAME, BTRACE_RUNTIME_DESC, null, null);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, final String desc, String signature, String[] exceptions) {
        if (name.equals(CONSTRUCTOR)) {
            return super.visitMethod(access, name, desc, signature, exceptions);
        } else {
            final boolean isClassInitializer = name.equals(CLASS_INITIALIZER);
            classInitializerFound = classInitializerFound || isClassInitializer;
            if (!isClassInitializer) {
                if ((access & Opcodes.ACC_PRIVATE) > 0)
                    access ^= Opcodes.ACC_PRIVATE;
                if ((access & Opcodes.ACC_PROTECTED) > 0)
                    access ^= Opcodes.ACC_PROTECTED;
                access |= Opcodes.ACC_PUBLIC;
            }
            MethodVisitor adaptee = super.visitMethod(access, name, desc, signature, exceptions);
            final LocalVariableHelperImpl lvh = new LocalVariableHelperImpl(adaptee, access, desc);
            final Assembler asm = new Assembler(lvh);
            return new MethodVisitor(Opcodes.ASM4, lvh) {

                private boolean isBTraceHandler = false;

                private final Label start = new Label();

                private final Label handler = new Label();

                private int nextVar = 0;

                private Type delayedClzLiteral = null;

                private String lastStringLiteral = null;

                private boolean ignoreNextCheckcast;

                private void generateExportGet(String name, String desc) {
                    int typeCode = desc.charAt(0);
                    switch(typeCode) {
                        case '[':
                            asm.loadNull();
                            break;
                        case 'L':
                            if (desc.equals(JAVA_LANG_STRING_DESC)) {
                                asm.ldc(name).invokeStatic(BTRACE_RUNTIME, BTRACE_RUNTIME_GET_PERFSTRING, BTRACE_RUNTIME_GET_PERFSTRING_DESC);
                            } else {
                                asm.loadNull();
                            }
                            break;
                        case 'Z':
                        case 'C':
                        case 'B':
                        case 'S':
                        case 'I':
                            asm.ldc(name).invokeStatic(BTRACE_RUNTIME, BTRACE_RUNTIME_GET_PERFINT, BTRACE_RUNTIME_GET_PERFINT_DESC);
                            break;
                        case 'J':
                            asm.ldc(name).invokeStatic(BTRACE_RUNTIME, BTRACE_RUNTIME_GET_PERFLONG, BTRACE_RUNTIME_GET_PERFLONG_DESC);
                            break;
                        case 'F':
                            asm.ldc(name).invokeStatic(BTRACE_RUNTIME, BTRACE_RUNTIME_GET_PERFFLOAT, BTRACE_RUNTIME_GET_PERFFLOAT_DESC);
                            break;
                        case 'D':
                            asm.ldc(name).invokeStatic(BTRACE_RUNTIME, BTRACE_RUNTIME_GET_PERFDOUBLE, BTRACE_RUNTIME_GET_PERFDOUBLE_DESC);
                            break;
                    }
                }

                private void generateExportPut(String name, String desc) {
                    int typeCode = desc.charAt(0);
                    switch(typeCode) {
                        case '[':
                            asm.pop();
                            break;
                        case 'L':
                            if (desc.equals(JAVA_LANG_STRING_DESC)) {
                                asm.ldc(name).invokeStatic(BTRACE_RUNTIME, BTRACE_RUNTIME_PUT_PERFSTRING, BTRACE_RUNTIME_PUT_PERFSTRING_DESC);
                            } else {
                                asm.pop();
                            }
                            break;
                        case 'Z':
                        case 'C':
                        case 'B':
                        case 'S':
                        case 'I':
                            asm.ldc(name).invokeStatic(BTRACE_RUNTIME, BTRACE_RUNTIME_PUT_PERFINT, BTRACE_RUNTIME_PUT_PERFINT_DESC);
                            break;
                        case 'J':
                            asm.ldc(name).invokeStatic(BTRACE_RUNTIME, BTRACE_RUNTIME_PUT_PERFLONG, BTRACE_RUNTIME_PUT_PERFLONG_DESC);
                            break;
                        case 'F':
                            asm.ldc(name).invokeStatic(BTRACE_RUNTIME, BTRACE_RUNTIME_PUT_PERFFLOAT, BTRACE_RUNTIME_PUT_PERFFLOAT_DESC);
                            break;
                        case 'D':
                            asm.ldc(name).invokeStatic(BTRACE_RUNTIME, BTRACE_RUNTIME_PUT_PERFDOUBLE, BTRACE_RUNTIME_PUT_PERFDOUBLE_DESC);
                            break;
                    }
                }

                private void generateThreadLocalGet(FieldDescriptor fd) {
                    if (isClassInitializer) {
                        if (fd.initialized) {
                            asm.loadLocal(Type.getType(fd.desc), fd.var);
                        } else if (fd.value != null) {
                            asm.ldc(fd.value);
                        } else {
                            asm.defaultValue(fd.desc);
                        }
                    } else {
                        String fieldName = BTRACE_FIELD_PREFIX + fd.name;
                        asm.getStatic(className, fieldName, JAVA_LANG_THREAD_LOCAL_DESC).invokeVirtual(JAVA_LANG_THREAD_LOCAL, JAVA_LANG_THREAD_LOCAL_GET, JAVA_LANG_THREAD_LOCAL_GET_DESC).unbox(fd.desc);
                    }
                }

                private void generateThreadLocalPut(FieldDescriptor fd) {
                    if (isClassInitializer) {
                        asm.storeLocal(Type.getType(fd.desc), fd.var);
                        fd.initialized = true;
                    } else {
                        String fieldName = BTRACE_FIELD_PREFIX + fd.name;
                        asm.box(fd.desc).getStatic(className, fieldName, JAVA_LANG_THREAD_LOCAL_DESC).swap().invokeVirtual(JAVA_LANG_THREAD_LOCAL, JAVA_LANG_THREAD_LOCAL_SET, JAVA_LANG_THREAD_LOCAL_SET_DESC);
                    }
                }

                @Override
                public AnnotationVisitor visitAnnotation(String name, boolean bln) {
                    isBTraceHandler = name.startsWith("Lcom/sun/btrace/annotations/");
                    return super.visitAnnotation(name, bln);
                }

                @Override
                public void visitCode() {
                    if (isClassInitializer || isBTraceHandler) {
                        visitTryCatchBlock(start, handler, handler, JAVA_LANG_THROWABLE);
                        if (isClassInitializer) {
                            asm.ldc(classType).invokeStatic(BTRACE_RUNTIME, BTRACE_RUNTIME_FOR_CLASS, BTRACE_RUNTIME_FOR_CLASS_DESC).putStatic(className, BTRACE_RUNTIME_FIELD_NAME, BTRACE_RUNTIME_DESC);
                        }
                        asm.getStatic(className, BTRACE_RUNTIME_FIELD_NAME, BTRACE_RUNTIME_DESC).invokeStatic(BTRACE_RUNTIME, BTRACE_RUNTIME_ENTER, BTRACE_RUNTIME_ENTER_DESC);
                        if (isClassInitializer) {
                            for (FieldDescriptor fd : threadLocalFields.values()) {
                                fd.var = nextVar;
                                nextVar += Type.getType(fd.desc).getSize();
                            }
                            for (FieldDescriptor fd : exportFields.values()) {
                                asm.ldc(perfCounterName(fd.name)).ldc(fd.desc);
                                if (fd.value == null) {
                                    asm.loadNull();
                                } else {
                                    asm.ldc(fd.value).box(fd.desc);
                                }
                                asm.invokeStatic(BTRACE_RUNTIME, BTRACE_RUNTIME_NEW_PERFCOUNTER, BTRACE_RUNTIME_NEW_PERFCOUNTER_DESC);
                            }
                        }
                        visitJumpInsn(IFNE, start);
                        super.visitInsn(RETURN);
                        visitLabel(start);
                    }
                    super.visitCode();
                }

                @Override
                public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                    if (owner.equals(className)) {
                        FieldDescriptor fd = exportFields.get(name);
                        if (fd != null) {
                            if (opcode == GETSTATIC) {
                                generateExportGet(perfCounterName(name), desc);
                            } else {
                                generateExportPut(perfCounterName(name), desc);
                            }
                            return;
                        }
                        fd = threadLocalFields.get(name);
                        if (fd != null) {
                            if (opcode == GETSTATIC) {
                                generateThreadLocalGet(fd);
                            } else {
                                generateThreadLocalPut(fd);
                            }
                            return;
                        }
                        fd = injectedFields.get(name);
                        if (fd != null) {
                            Type descType = Type.getType(desc);
                            if (fd.initialized) {
                                asm.loadLocal(descType, fd.var);
                                return;
                            } else {
                                String internal = descType.getInternalName();
                                if (fd.propertyName.isEmpty()) {
                                    asm.newInstance(descType).dup();
                                    if (fd.propertyDescription.equals(Type.getInternalName(RuntimeService.class))) {
                                        asm.getStatic(className, BTRACE_RUNTIME_FIELD_NAME, BTRACE_RUNTIME_DESC).invokeSpecial(internal, CONSTRUCTOR, "(" + BTRACE_RUNTIME_DESC + ")V");
                                    } else {
                                        asm.invokeSpecial(internal, CONSTRUCTOR, "()V");
                                    }
                                } else {
                                    if (fd.propertyDescription.equals(Type.getInternalName(RuntimeService.class))) {
                                        asm.getStatic(className, BTRACE_RUNTIME_FIELD_NAME, BTRACE_RUNTIME_DESC).invokeStatic(internal, fd.propertyName, "(" + BTRACE_RUNTIME_DESC + ")" + descType.getDescriptor());
                                    } else {
                                        asm.invokeStatic(internal, fd.propertyName, "()" + descType.getDescriptor());
                                    }
                                }
                                asm.dup();
                                fd.var = lvh.storeNewLocal(descType);
                                fd.initialized = true;
                                return;
                            }
                        }
                    }
                    super.visitFieldInsn(opcode, owner, name.equals(BTRACE_RUNTIME_FIELD_NAME) ? name : BTRACE_FIELD_PREFIX + name, desc);
                }

                @Override
                public void visitVarInsn(int opcode, int var) {
                    super.visitVarInsn(opcode, var + nextVar);
                }

                @Override
                public void visitInsn(int opcode) {
                    if (opcode == RETURN) {
                        if (isClassInitializer) {
                            for (FieldDescriptor fd : threadLocalFields.values()) {
                                generateThreadLocalGet(fd);
                                asm.box(fd.desc).invokeStatic(BTRACE_RUNTIME, BTRACE_RUNTIME_NEW_THREAD_LOCAL, BTRACE_RUNTIME_NEW_THREAD_LOCAL_DESC).putStatic(className, BTRACE_FIELD_PREFIX + fd.name, JAVA_LANG_THREAD_LOCAL_DESC);
                            }
                            asm.invokeStatic(BTRACE_RUNTIME, BTRACE_RUNTIME_START, BTRACE_RUNTIME_START_DESC);
                        } else {
                            if (isBTraceHandler) {
                                asm.invokeStatic(BTRACE_RUNTIME, BTRACE_RUNTIME_LEAVE, BTRACE_RUNTIME_LEAVE_DESC);
                            }
                        }
                    }
                    super.visitInsn(opcode);
                }

                @Override
                public void visitTypeInsn(int opcode, String desc) {
                    if (opcode != Opcodes.CHECKCAST || !ignoreNextCheckcast) {
                        super.visitTypeInsn(opcode, desc);
                    } else {
                        ignoreNextCheckcast = false;
                    }
                }

                @Override
                public void visitLdcInsn(Object o) {
                    if (o instanceof Type) {
                        delayedClzLiteral = (Type) o;
                    } else {
                        if (o instanceof String && delayedClzLiteral != null) {
                            lastStringLiteral = (String) o;
                        } else {
                            super.visitLdcInsn(o);
                        }
                    }
                }

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                    if (owner.equals(SERVICE)) {
                        if (name.equals("simple")) {
                            if (lastStringLiteral == null) {
                                asm.newInstance(delayedClzLiteral).dup().invokeSpecial(delayedClzLiteral.getInternalName(), CONSTRUCTOR, "()V");
                            } else {
                                asm.invokeStatic(delayedClzLiteral.getInternalName(), lastStringLiteral, "()" + delayedClzLiteral.getDescriptor());
                            }
                        } else if (name.equals("runtime")) {
                            asm.newInstance(delayedClzLiteral).dup().getStatic(className, BTRACE_RUNTIME_FIELD_NAME, BTRACE_RUNTIME_DESC).invokeSpecial(delayedClzLiteral.getInternalName(), CONSTRUCTOR, "(" + BTRACE_RUNTIME_DESC + ")V");
                        }
                        ignoreNextCheckcast = true;
                        delayedClzLiteral = null;
                        return;
                    }
                    if (owner.equals(Type.getInternalName(StringBuilder.class))) {
                        if (name.equals("append")) {
                            if (desc.equals(Type.getMethodDescriptor(Type.getType(StringBuilder.class), Type.getType(Object.class)))) {
                                super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(BTraceRuntime.class), "safeStr", Type.getMethodDescriptor(Type.getType(String.class), Type.getType(Object.class)), false);
                                desc = Type.getMethodDescriptor(Type.getType(StringBuilder.class), Type.getType(String.class));
                            }
                        }
                    }
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                }

                @Override
                public void visitMaxs(int maxStack, int maxLocals) {
                    visitLabel(handler);
                    if (isBTraceHandler) {
                        visitMethodInsn(INVOKESTATIC, BTRACE_RUNTIME, BTRACE_RUNTIME_HANDLE_EXCEPTION, BTRACE_RUNTIME_HANDLE_EXCEPTION_DESC, false);
                    }
                    super.visitInsn(RETURN);
                    super.visitMaxs(maxStack, maxLocals);
                }

                @Override
                public void visitEnd() {
                    for (FieldDescriptor fd : injectedFields.values()) {
                        fd.initialized = false;
                    }
                    super.visitEnd();
                }
            };
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || args.length > 2) {
            System.err.println("Usage: java com.sun.btrace.runtime.Preprocessor <class> [<new-class-name>]");
            System.exit(1);
        }
        boolean renamed = (args.length == 2);
        String className = args[0].replace('.', '/');
        String newName = className;
        if (renamed) {
            newName = args[1].replace('.', '/');
        }
        FileInputStream fis = new FileInputStream(className + ".class");
        ClassReader reader = new ClassReader(new BufferedInputStream(fis));
        FileOutputStream fos = new FileOutputStream(newName + ".class");
        ClassWriter writer = InstrumentUtils.newClassWriter();
        ClassVisitor cv;
        if (renamed) {
            cv = new ClassRenamer(args[1], new Preprocessor(writer));
        } else {
            cv = new Preprocessor(writer);
        }
        InstrumentUtils.accept(reader, cv);
        fos.write(writer.toByteArray());
    }
}
