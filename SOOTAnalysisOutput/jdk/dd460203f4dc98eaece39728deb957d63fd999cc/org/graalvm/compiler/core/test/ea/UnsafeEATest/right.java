package org.graalvm.compiler.core.test.ea;

import jdk.vm.ci.meta.JavaConstant;
import org.junit.Assert;
import org.junit.Test;
import org.graalvm.compiler.nodes.PhiNode;
import org.graalvm.compiler.nodes.ValuePhiNode;
import org.graalvm.compiler.nodes.java.LoadFieldNode;

public class UnsafeEATest extends EATestBase {

    public static int zero = 0;

    private static final long fieldOffset1;

    private static final long fieldOffset2;

    static {
        try {
            long localFieldOffset1 = UNSAFE.objectFieldOffset(TestClassInt.class.getField("x"));
            if (localFieldOffset1 % 8 == 0) {
                fieldOffset1 = localFieldOffset1;
                fieldOffset2 = UNSAFE.objectFieldOffset(TestClassInt.class.getField("y"));
            } else {
                fieldOffset1 = UNSAFE.objectFieldOffset(TestClassInt.class.getField("y"));
                fieldOffset2 = UNSAFE.objectFieldOffset(TestClassInt.class.getField("z"));
            }
            assert fieldOffset2 == fieldOffset1 + 4;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSimpleInt() {
        testEscapeAnalysis("testSimpleIntSnippet", JavaConstant.forInt(101), false);
    }

    public static int testSimpleIntSnippet() {
        TestClassInt x = new TestClassInt();
        UNSAFE.putInt(x, fieldOffset1, 101);
        return UNSAFE.getInt(x, fieldOffset1);
    }

    @Test
    public void testMaterializedInt() {
        test("testMaterializedIntSnippet");
    }

    public static TestClassInt testMaterializedIntSnippet() {
        TestClassInt x = new TestClassInt();
        UNSAFE.putInt(x, fieldOffset1, 101);
        return x;
    }

    @Test
    public void testSimpleDouble() {
        testEscapeAnalysis("testSimpleDoubleSnippet", JavaConstant.forDouble(10.1), false);
    }

    public static double testSimpleDoubleSnippet() {
        TestClassInt x = new TestClassInt();
        UNSAFE.putDouble(x, fieldOffset1, 10.1);
        return UNSAFE.getDouble(x, fieldOffset1);
    }

    @Test
    public void testMergedDouble() {
        testEscapeAnalysis("testMergedDoubleSnippet", null, false);
        Assert.assertEquals(1, returnNodes.size());
        Assert.assertTrue(returnNodes.get(0).result() instanceof ValuePhiNode);
        PhiNode phi = (PhiNode) returnNodes.get(0).result();
        Assert.assertTrue(phi.valueAt(0) instanceof LoadFieldNode);
        Assert.assertTrue(phi.valueAt(1) instanceof LoadFieldNode);
    }

    public static double testMergedDoubleSnippet(boolean a) {
        TestClassInt x;
        if (a) {
            x = new TestClassInt(0, 0);
            UNSAFE.putDouble(x, fieldOffset1, doubleField);
        } else {
            x = new TestClassInt();
            UNSAFE.putDouble(x, fieldOffset1, doubleField2);
        }
        return UNSAFE.getDouble(x, fieldOffset1);
    }

    @Test
    public void testMaterializedDouble() {
        test("testMaterializedDoubleSnippet");
    }

    public static TestClassInt testMaterializedDoubleSnippet() {
        TestClassInt x = new TestClassInt();
        UNSAFE.putDouble(x, fieldOffset1, 10.1);
        return x;
    }

    @Test
    public void testDeoptDoubleVar() {
        test("testDeoptDoubleVarSnippet");
    }

    public static double doubleField = 10.1e99;

    public static double doubleField2;

    public static TestClassInt testDeoptDoubleVarSnippet() {
        TestClassInt x = new TestClassInt();
        UNSAFE.putDouble(x, fieldOffset1, doubleField);
        doubleField2 = 123;
        try {
            doubleField = ((int) UNSAFE.getDouble(x, fieldOffset1)) / zero;
        } catch (RuntimeException e) {
            return x;
        }
        return x;
    }

    @Test
    public void testDeoptDoubleConstant() {
        test("testDeoptDoubleConstantSnippet");
    }

    public static TestClassInt testDeoptDoubleConstantSnippet() {
        TestClassInt x = new TestClassInt();
        UNSAFE.putDouble(x, fieldOffset1, 10.123);
        doubleField2 = 123;
        try {
            doubleField = ((int) UNSAFE.getDouble(x, fieldOffset1)) / zero;
        } catch (RuntimeException e) {
            return x;
        }
        return x;
    }

    @Test
    public void testDeoptLongVar() {
        test("testDeoptLongVarSnippet");
    }

    public static long longField = 0x133443218aaaffffL;

    public static long longField2;

    public static TestClassInt testDeoptLongVarSnippet() {
        TestClassInt x = new TestClassInt();
        UNSAFE.putLong(x, fieldOffset1, longField);
        longField2 = 123;
        try {
            longField = UNSAFE.getLong(x, fieldOffset1) / zero;
        } catch (RuntimeException e) {
            return x;
        }
        return x;
    }

    @Test
    public void testDeoptLongConstant() {
        test("testDeoptLongConstantSnippet");
    }

    public static TestClassInt testDeoptLongConstantSnippet() {
        TestClassInt x = new TestClassInt();
        UNSAFE.putLong(x, fieldOffset1, 0x2222222210123L);
        longField2 = 123;
        try {
            longField = UNSAFE.getLong(x, fieldOffset1) / zero;
        } catch (RuntimeException e) {
            return x;
        }
        return x;
    }
}
