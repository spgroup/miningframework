package compiler.jvmci.compilerToVM;

import jdk.vm.ci.hotspot.CompilerToVMHelper;
import jdk.vm.ci.hotspot.HotSpotResolvedJavaMethod;
import jdk.vm.ci.hotspot.PublicMetaspaceWrapperObject;
import jdk.test.lib.Asserts;
import jdk.test.lib.Utils;
import sun.hotspot.WhiteBox;
import sun.misc.Unsafe;
import java.lang.reflect.Field;

public class GetResolvedJavaMethodTest {

    private static enum TestCase {

        NULL_BASE {

            @Override
            HotSpotResolvedJavaMethod getResolvedJavaMethod() {
                return CompilerToVMHelper.getResolvedJavaMethod(null, getPtrToMethod());
            }
        }
        , JAVA_METHOD_BASE {

            @Override
            HotSpotResolvedJavaMethod getResolvedJavaMethod() {
                HotSpotResolvedJavaMethod methodInstance = CompilerToVMHelper.getResolvedJavaMethodAtSlot(TEST_CLASS, 0);
                try {
                    METASPACE_METHOD_FIELD.set(methodInstance, getPtrToMethod());
                } catch (ReflectiveOperationException e) {
                    throw new Error("TEST BUG : " + e, e);
                }
                return CompilerToVMHelper.getResolvedJavaMethod(methodInstance, 0L);
            }
        }
        , JAVA_METHOD_BASE_IN_TWO {

            @Override
            HotSpotResolvedJavaMethod getResolvedJavaMethod() {
                long ptr = getPtrToMethod();
                HotSpotResolvedJavaMethod methodInstance = CompilerToVMHelper.getResolvedJavaMethodAtSlot(TEST_CLASS, 0);
                try {
                    METASPACE_METHOD_FIELD.set(methodInstance, ptr / 2L);
                } catch (ReflectiveOperationException e) {
                    throw new Error("TESTBUG : " + e, e);
                }
                return CompilerToVMHelper.getResolvedJavaMethod(methodInstance, ptr - ptr / 2L);
            }
        }
        , JAVA_METHOD_BASE_ZERO {

            @Override
            HotSpotResolvedJavaMethod getResolvedJavaMethod() {
                long ptr = getPtrToMethod();
                HotSpotResolvedJavaMethod methodInstance = CompilerToVMHelper.getResolvedJavaMethodAtSlot(TEST_CLASS, 0);
                try {
                    METASPACE_METHOD_FIELD.set(methodInstance, 0L);
                } catch (ReflectiveOperationException e) {
                    throw new Error("TESTBUG : " + e, e);
                }
                return CompilerToVMHelper.getResolvedJavaMethod(methodInstance, ptr);
            }
        }
        ;

        abstract HotSpotResolvedJavaMethod getResolvedJavaMethod();
    }

    private static final Unsafe UNSAFE = Utils.getUnsafe();

    private static final WhiteBox WB = WhiteBox.getWhiteBox();

    private static final Field METASPACE_METHOD_FIELD;

    private static final Class<?> TEST_CLASS = GetResolvedJavaMethodTest.class;

    private static final long PTR;

    static {
        HotSpotResolvedJavaMethod method = CompilerToVMHelper.getResolvedJavaMethodAtSlot(TEST_CLASS, 0);
        try {
            METASPACE_METHOD_FIELD = method.getClass().getDeclaredField("metaspaceMethod");
            METASPACE_METHOD_FIELD.setAccessible(true);
            PTR = (long) METASPACE_METHOD_FIELD.get(method);
        } catch (ReflectiveOperationException e) {
            throw new Error("TESTBUG : " + e, e);
        }
    }

    private static long getPtrToMethod() {
        Field field;
        try {
            field = TEST_CLASS.getDeclaredField("PTR");
        } catch (NoSuchFieldException e) {
            throw new Error("TEST BUG : " + e, e);
        }
        Object base = UNSAFE.staticFieldBase(field);
        return WB.getObjectAddress(base) + UNSAFE.staticFieldOffset(field);
    }

    public void test(TestCase testCase) {
        System.out.println(testCase.name());
        HotSpotResolvedJavaMethod result = testCase.getResolvedJavaMethod();
        Asserts.assertNotNull(result, testCase + " : got null");
        Asserts.assertEQ(TEST_CLASS, CompilerToVMHelper.getMirror(result.getDeclaringClass()), testCase + " : unexpected declaring class");
    }

    public static void main(String[] args) {
        GetResolvedJavaMethodTest test = new GetResolvedJavaMethodTest();
        for (TestCase testCase : TestCase.values()) {
            test.test(testCase);
        }
        testObjectBase();
        testMetaspaceWrapperBase();
    }

    private static void testMetaspaceWrapperBase() {
        try {
            HotSpotResolvedJavaMethod method = CompilerToVMHelper.getResolvedJavaMethod(new PublicMetaspaceWrapperObject() {

                @Override
                public long getMetaspacePointer() {
                    return getPtrToMethod();
                }
            }, 0L);
            throw new AssertionError("Test METASPACE_WRAPPER_BASE." + " Expected IllegalArgumentException has not been caught");
        } catch (IllegalArgumentException e) {
        }
    }

    private static void testObjectBase() {
        try {
            HotSpotResolvedJavaMethod method = CompilerToVMHelper.getResolvedJavaMethod(new Object(), 0L);
            throw new AssertionError("Test OBJECT_BASE." + " Expected IllegalArgumentException has not been caught");
        } catch (IllegalArgumentException e) {
        }
    }
}