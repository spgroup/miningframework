package compiler.jvmci.compilerToVM;

import compiler.jvmci.common.CTVMUtilities;
import compiler.testlibrary.CompilerUtils;
import compiler.whitebox.CompilerWhiteBoxTest;
import jdk.test.lib.Asserts;
import jdk.vm.ci.hotspot.CompilerToVMHelper;
import jdk.vm.ci.hotspot.HotSpotStackFrameReference;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import sun.hotspot.WhiteBox;
import java.lang.reflect.Method;

public class MaterializeVirtualObjectTest {

    private static final WhiteBox WB;

    private static final boolean INVALIDATE;

    private static final int COMPILE_THRESHOLD;

    private static final Method MATERIALIZED_METHOD;

    private static final Method NOT_MATERIALIZED_METHOD;

    private static final ResolvedJavaMethod MATERIALIZED_RESOLVED;

    private static final ResolvedJavaMethod NOT_MATERIALIZED_RESOLVED;

    private static final boolean MATERIALIZE_FIRST;

    static {
        Method method1;
        Method method2;
        WB = WhiteBox.getWhiteBox();
        try {
            method1 = MaterializeVirtualObjectTest.class.getDeclaredMethod("testFrame", String.class, int.class);
            method2 = MaterializeVirtualObjectTest.class.getDeclaredMethod("testFrame2", String.class, int.class);
        } catch (NoSuchMethodException e) {
            throw new Error("Can't get executable for test method", e);
        }
        ResolvedJavaMethod resolved1;
        ResolvedJavaMethod resolved2;
        resolved1 = CTVMUtilities.getResolvedMethod(method1);
        resolved2 = CTVMUtilities.getResolvedMethod(method2);
        INVALIDATE = Boolean.getBoolean("compiler.jvmci.compilerToVM.MaterializeVirtualObjectTest.invalidate");
        COMPILE_THRESHOLD = WB.getBooleanVMFlag("TieredCompilation") ? CompilerWhiteBoxTest.THRESHOLD : CompilerWhiteBoxTest.THRESHOLD * 2;
        MATERIALIZE_FIRST = Boolean.getBoolean("compiler.jvmci.compilerToVM.MaterializeVirtualObjectTest.materializeFirst");
        MATERIALIZED_RESOLVED = MATERIALIZE_FIRST ? resolved1 : resolved2;
        NOT_MATERIALIZED_RESOLVED = MATERIALIZE_FIRST ? resolved2 : resolved1;
        MATERIALIZED_METHOD = MATERIALIZE_FIRST ? method1 : method2;
        NOT_MATERIALIZED_METHOD = MATERIALIZE_FIRST ? method2 : method1;
    }

    public static void main(String[] args) {
        int[] levels = CompilerUtils.getAvailableCompilationLevels();
        if (levels.length < 1 || levels[levels.length - 1] != 4) {
            System.out.println("INFO: Test needs compilation level 4 to" + " be available. Skipping.");
        } else {
            new MaterializeVirtualObjectTest().test();
        }
    }

    private static String getName() {
        return "CASE: invalidate=" + INVALIDATE + ", materializedMethod=" + (MATERIALIZE_FIRST ? "testFrame" : "testFrame2") + ", notMaterializedMethod=" + (MATERIALIZE_FIRST ? "testFrame2" : "testFrame");
    }

    private void test() {
        System.out.println(getName());
        Asserts.assertFalse(WB.isMethodCompiled(MATERIALIZED_METHOD), getName() + " : materialized method is compiled");
        Asserts.assertFalse(WB.isMethodCompiled(NOT_MATERIALIZED_METHOD), getName() + " : not materialized method is compiled");
        for (int i = 0; i < CompilerWhiteBoxTest.THRESHOLD; i++) {
            testFrame("someString", i);
        }
        Asserts.assertTrue(WB.isMethodCompiled(MATERIALIZED_METHOD), getName() + " : materialized method not compiled");
        Asserts.assertTrue(WB.isMethodCompiled(NOT_MATERIALIZED_METHOD), getName() + " : not materialized method not compiled");
        testFrame("someString", CompilerWhiteBoxTest.THRESHOLD);
    }

    private void testFrame(String str, int iteration) {
        Helper helper = new Helper(str);
        testFrame2(str, iteration);
        Asserts.assertTrue((helper.string != null) && (this != null) && (helper != null), String.format("%s : some locals are null", getName()));
    }

    private void testFrame2(String str, int iteration) {
        Helper helper = new Helper(str);
        recurse(2, iteration);
        Asserts.assertTrue((helper.string != null) && (this != null) && (helper != null), String.format("%s : some locals are null", getName()));
    }

    private void recurse(int depth, int iteration) {
        if (depth == 0) {
            check(iteration);
        } else {
            Integer s = new Integer(depth);
            recurse(depth - 1, iteration);
            Asserts.assertEQ(s.intValue(), depth, String.format("different values: %s != %s", s.intValue(), depth));
        }
    }

    private void check(int iteration) {
        if (iteration == COMPILE_THRESHOLD) {
            HotSpotStackFrameReference materialized = CompilerToVMHelper.getNextStackFrame(null, new ResolvedJavaMethod[] { MATERIALIZED_RESOLVED }, 0);
            Asserts.assertNotNull(materialized, getName() + " : got null frame for materialized method");
            HotSpotStackFrameReference notMaterialized = CompilerToVMHelper.getNextStackFrame(null, new ResolvedJavaMethod[] { NOT_MATERIALIZED_RESOLVED }, 0);
            Asserts.assertNE(materialized, notMaterialized, "Got same frame pointer for both tested frames");
            Asserts.assertNotNull(notMaterialized, getName() + " : got null frame for not materialized method");
            Asserts.assertTrue(materialized.hasVirtualObjects(), getName() + ": materialized frame has no virtual object before materialization");
            Asserts.assertTrue(notMaterialized.hasVirtualObjects(), getName() + ": notMaterialized frame has no virtual object before materialization");
            CompilerToVMHelper.materializeVirtualObjects(materialized, INVALIDATE);
            Asserts.assertFalse(materialized.hasVirtualObjects(), getName() + " : materialized has virtual object after materialization");
            Asserts.assertTrue(notMaterialized.hasVirtualObjects(), getName() + " : notMaterialized has no virtual object after materialization");
            Asserts.assertEQ(WB.isMethodCompiled(MATERIALIZED_METHOD), !INVALIDATE, getName() + " : materialized method has unexpected compiled status");
            Asserts.assertTrue(WB.isMethodCompiled(NOT_MATERIALIZED_METHOD), getName() + " : not materialized method has unexpected compiled status");
        }
    }

    private class Helper {

        public String string;

        public Helper(String s) {
            this.string = s;
        }
    }
}
