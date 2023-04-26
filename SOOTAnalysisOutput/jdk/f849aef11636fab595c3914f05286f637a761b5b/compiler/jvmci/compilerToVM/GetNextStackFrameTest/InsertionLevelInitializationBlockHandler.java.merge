package compiler.jvmci.compilerToVM;

import compiler.jvmci.common.CTVMUtilities;
import java.lang.reflect.Method;
import jdk.vm.ci.hotspot.CompilerToVMHelper;
import jdk.vm.ci.hotspot.HotSpotStackFrameReference;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import jdk.test.lib.Asserts;

public class GetNextStackFrameTest {

    private static final int RECURSION_AMOUNT = 3;

    private static final ResolvedJavaMethod REC_FRAME_METHOD;

    private static final ResolvedJavaMethod FRAME1_METHOD;

    private static final ResolvedJavaMethod FRAME2_METHOD;

    private static final ResolvedJavaMethod FRAME3_METHOD;

    private static final ResolvedJavaMethod FRAME4_METHOD;

    private static final ResolvedJavaMethod RUN_METHOD;

    static {
        Method method;
        try {
            Class<?> aClass = GetNextStackFrameTest.class;
            method = aClass.getDeclaredMethod("recursiveFrame", int.class);
            REC_FRAME_METHOD = CTVMUtilities.getResolvedMethod(method);
            method = aClass.getDeclaredMethod("frame1");
            FRAME1_METHOD = CTVMUtilities.getResolvedMethod(method);
            method = aClass.getDeclaredMethod("frame2");
            FRAME2_METHOD = CTVMUtilities.getResolvedMethod(method);
            method = aClass.getDeclaredMethod("frame3");
            FRAME3_METHOD = CTVMUtilities.getResolvedMethod(method);
            method = aClass.getDeclaredMethod("frame4");
            FRAME4_METHOD = CTVMUtilities.getResolvedMethod(method);
            method = Thread.class.getDeclaredMethod("run");
            RUN_METHOD = CTVMUtilities.getResolvedMethod(Thread.class, method);
        } catch (NoSuchMethodException e) {
            throw new Error("TEST BUG: can't find a test method : " + e, e);
        }
    }

    public static void main(String[] args) {
        new GetNextStackFrameTest().test();
    }

    private void test() {
        Thread thread = new Thread(() -> recursiveFrame(RECURSION_AMOUNT));
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new Error("Interrupted while waiting to join", e);
        }
    }

    private void recursiveFrame(int recursionAmount) {
        if (--recursionAmount != 0) {
            recursiveFrame(recursionAmount);
        } else {
            frame1();
        }
    }

    private void frame1() {
        frame2();
    }

    private void frame2() {
        frame3();
    }

    private void frame3() {
        frame4();
    }

    private void frame4() {
        check();
    }

    private void check() {
        findFirst();
        walkThrough();
        skipAll();
        findNextSkipped();
        findYourself();
    }

    private void findFirst() {
        checkNextFrameFor(null, new ResolvedJavaMethod[] { FRAME2_METHOD, FRAME3_METHOD, FRAME4_METHOD }, FRAME4_METHOD, 0);
    }

    private void walkThrough() {
        HotSpotStackFrameReference nextStackFrame = checkNextFrameFor(null, new ResolvedJavaMethod[] { FRAME4_METHOD }, FRAME4_METHOD, 0);
        nextStackFrame = checkNextFrameFor(nextStackFrame, new ResolvedJavaMethod[] { FRAME3_METHOD, FRAME2_METHOD }, FRAME3_METHOD, 0);
        nextStackFrame = checkNextFrameFor(nextStackFrame, new ResolvedJavaMethod[] { FRAME1_METHOD }, FRAME1_METHOD, 0);
        nextStackFrame = checkNextFrameFor(nextStackFrame, new ResolvedJavaMethod[] { REC_FRAME_METHOD }, REC_FRAME_METHOD, RECURSION_AMOUNT - 1);
        nextStackFrame = checkNextFrameFor(nextStackFrame, new ResolvedJavaMethod[] { RUN_METHOD }, RUN_METHOD, 0);
        nextStackFrame = CompilerToVMHelper.getNextStackFrame(nextStackFrame, null, 0);
        Asserts.assertNull(nextStackFrame, "Found stack frame after Thread::run");
    }

    private void skipAll() {
        int initialSkip = Thread.currentThread().getStackTrace().length + 2;
        HotSpotStackFrameReference nextStackFrame = CompilerToVMHelper.getNextStackFrame(null, null, initialSkip);
        Asserts.assertNull(nextStackFrame, "Unexpected frame");
    }

    private void findNextSkipped() {
        HotSpotStackFrameReference nextStackFrame = CompilerToVMHelper.getNextStackFrame(null, new ResolvedJavaMethod[] { FRAME4_METHOD }, 0);
        checkNextFrameFor(nextStackFrame, null, FRAME2_METHOD, 1);
    }

    private void findYourself() {
        Method method;
        Class<?> aClass = CompilerToVMHelper.CompilerToVMClass();
        try {
            method = aClass.getDeclaredMethod("getNextStackFrame", HotSpotStackFrameReference.class, ResolvedJavaMethod[].class, int.class);
        } catch (NoSuchMethodException e) {
            throw new Error("TEST BUG: can't find getNextStackFrame : " + e, e);
        }
        ResolvedJavaMethod self = CTVMUtilities.getResolvedMethod(aClass, method);
        checkNextFrameFor(null, null, self, 0);
    }

    private HotSpotStackFrameReference checkNextFrameFor(HotSpotStackFrameReference currentFrame, ResolvedJavaMethod[] searchMethods, ResolvedJavaMethod expected, int skip) {
        HotSpotStackFrameReference nextStackFrame = CompilerToVMHelper.getNextStackFrame(currentFrame, searchMethods, skip);
        Asserts.assertNotNull(nextStackFrame);
        Asserts.assertTrue(nextStackFrame.isMethod(expected), "Unexpected next frame: " + nextStackFrame + " from current frame: " + currentFrame);
        return nextStackFrame;
    }
}