package org.graalvm.compiler.core.test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import org.junit.Test;
import org.graalvm.compiler.code.CompilationResult;
import org.graalvm.compiler.test.ExportingClassLoader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.vm.ci.code.InstalledCode;
import jdk.vm.ci.meta.ResolvedJavaMethod;

public final class InterfaceMethodHandleTest extends GraalCompilerTest implements Opcodes {

    private static final MethodHandle INTERFACE_HANDLE_M;

    private static final MethodHandle INTERFACE_HANDLE_M2;

    public interface I {

        int m();

        int m2(int a, int b, int c, int d, int e, int f, int g, int h, int i, int j);
    }

    static class A implements I {

        @Override
        public int m() {
            return 0;
        }

        @Override
        public int m2(int a, int b, int c, int d, int e, int f, int g, int h, int i, int j) {
            return 1;
        }
    }

    static class M2Thrower implements I {

        @Override
        public int m() {
            return 0;
        }

        @Override
        public int m2(int a, int b, int c, int d, int e, int f, int g, int h, int i, int j) {
            throw new InternalError();
        }
    }

    static {
        try {
            MethodType type = MethodType.fromMethodDescriptorString("()I", I.class.getClassLoader());
            INTERFACE_HANDLE_M = MethodHandles.lookup().findVirtual(I.class, "m", type);
            MethodType type2 = MethodType.fromMethodDescriptorString("(IIIIIIIIII)I", I.class.getClassLoader());
            INTERFACE_HANDLE_M2 = MethodHandles.lookup().findVirtual(I.class, "m2", type2);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("unable to initialize method handle", e);
        }
    }

    public static Object invokeInterfaceHandle(I o) throws Throwable {
        return (int) INTERFACE_HANDLE_M.invokeExact(o);
    }

    @Test
    public void testInvokeInterface01() {
        test("invokeInterfaceHandle", new A());
    }

    @Test
    public void testInvokeInterface02() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        test("invokeInterfaceHandle", loader.findClass(NAME).newInstance());
    }

    public static Object invokeInterfaceHandle2(I o, int a, int b, int c, int d, int e, int f, int g, int h, int i, int j) throws Throwable {
        return (int) INTERFACE_HANDLE_M2.invokeExact(o, a, b, c, d, e, f, g, h, i, j);
    }

    @Override
    protected InstalledCode addMethod(ResolvedJavaMethod method, CompilationResult compResult) {
        if (method.getDeclaringClass().equals(getMetaAccess().lookupJavaType(M2Thrower.class))) {
            return getBackend().createDefaultInstalledCode(method, compResult);
        }
        return super.addMethod(method, compResult);
    }

    @Test
    public void testInvokeInterface03() throws Throwable {
        A goodInstance = new A();
        I badInstance = new M2Thrower();
        getCode(getMetaAccess().lookupJavaMethod(getMethod(M2Thrower.class, "m2")));
        for (int x = 0; x < 1000; x++) {
            final int limit = 20000;
            for (int i = 0; i <= limit; i++) {
                try {
                    invokeInterfaceHandle2(i < limit - 1 ? goodInstance : badInstance, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
                } catch (InternalError e) {
                }
            }
        }
    }

    private static final String BASENAME = InterfaceMethodHandleTest.class.getName();

    private static final String NAME = BASENAME + "_B";

    private AsmLoader loader = new AsmLoader(UnbalancedMonitorsTest.class.getClassLoader());

    public static byte[] bytesForB() {
        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;
        String jvmName = NAME.replace('.', '/');
        cw.visit(52, ACC_SUPER | ACC_PUBLIC, jvmName, null, "java/lang/Object", new String[] { BASENAME.replace('.', '/') + "$I" });
        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
        mv = cw.visitMethod(ACC_PRIVATE, "m", "()I", null, null);
        mv.visitCode();
        l0 = new Label();
        mv.visitLabel(l0);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
        l1 = new Label();
        mv.visitLabel(l1);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
        cw.visitEnd();
        mv = cw.visitMethod(ACC_PRIVATE, "m2", "(IIIIIIIIII)I", null, null);
        mv.visitCode();
        l0 = new Label();
        mv.visitLabel(l0);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
        l1 = new Label();
        mv.visitLabel(l1);
        mv.visitMaxs(1, 11);
        mv.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    public static class AsmLoader extends ExportingClassLoader {

        Class<?> loaded;

        public AsmLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (name.equals(NAME)) {
                if (loaded != null) {
                    return loaded;
                }
                byte[] bytes = bytesForB();
                return (loaded = defineClass(name, bytes, 0, bytes.length));
            } else {
                return super.findClass(name);
            }
        }
    }
}
