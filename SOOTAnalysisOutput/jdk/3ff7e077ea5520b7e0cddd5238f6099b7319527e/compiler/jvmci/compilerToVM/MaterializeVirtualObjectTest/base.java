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

    private static final Method METHOD;

    private static final ResolvedJavaMethod RESOLVED_METHOD;

    private static final boolean INVALIDATE;

    private static final int COMPILE_THRESHOLD;

    static {
        WB = WhiteBox.getWhiteBox();
        try {
            METHOD = MaterializeVirtualObjectTest.class.getDeclaredMethod("testFrame", String.class, int.class);
        } catch (NoSuchMethodException e) {
            throw new Error("Can't get executable for test method", e);
        }
        RESOLVED_METHOD = CTVMUtilities.getResolvedMethod(METHOD);
        INVALIDATE = Boolean.getBoolean("compiler.jvmci.compilerToVM.MaterializeVirtualObjectTest.invalidate");
        COMPILE_THRESHOLD = WB.getBooleanVMFlag("TieredCompilation") ? CompilerWhiteBoxTest.THRESHOLD : CompilerWhiteBoxTest.THRESHOLD * 2;
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
        return "CASE: invalidate=" + INVALIDATE;
    }

    private void test() {
        System.out.println(getName());
        Asserts.assertFalse(WB.isMethodCompiled(METHOD), getName() + " : method unexpectedly compiled");
        for (int i = 0; i < COMPILE_THRESHOLD; i++) {
            testFrame("someString", i);
        }
        Asserts.assertTrue(WB.isMethodCompiled(METHOD), getName() + "Method unexpectedly not compiled");
        Asserts.assertTrue(WB.getMethodCompilationLevel(METHOD) == 4, getName() + "Method not compiled at level 4");
        testFrame("someString", COMPILE_THRESHOLD);
    }

    private void testFrame(String str, int iteration) {
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
            HotSpotStackFrameReference hsFrame = CompilerToVMHelper.getNextStackFrame(null, new ResolvedJavaMethod[] { RESOLVED_METHOD }, 0);
            Asserts.assertNotNull(hsFrame, getName() + " : got null frame");
            Asserts.assertTrue(WB.isMethodCompiled(METHOD), getName() + "Test method should be compiled");
            Asserts.assertTrue(hsFrame.hasVirtualObjects(), getName() + ": has no virtual object before materialization");
            CompilerToVMHelper.materializeVirtualObjects(hsFrame, INVALIDATE);
            Asserts.assertFalse(hsFrame.hasVirtualObjects(), getName() + " : has virtual object after materialization");
            Asserts.assertEQ(WB.isMethodCompiled(METHOD), !INVALIDATE, getName() + " : unexpected compiled status");
        }
    }

    private class Helper {

        public String string;

        public Helper(String s) {
            this.string = s;
        }
    }
}
