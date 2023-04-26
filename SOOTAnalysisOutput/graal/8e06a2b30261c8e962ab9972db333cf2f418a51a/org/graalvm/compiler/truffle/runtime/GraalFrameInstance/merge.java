package org.graalvm.compiler.truffle.runtime;

import java.lang.reflect.Method;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import jdk.vm.ci.code.stack.InspectedFrame;

public class GraalFrameInstance implements FrameInstance {

    static final int CALL_TARGET_INDEX = 0;

    static final int FRAME_INDEX = 1;

    static final int OPTIMIZATION_TIER_FRAME_INDEX = 2;

    static final int CALL_NODE_NOTIFY_INDEX = 1;

    static final Method CALL_TARGET_METHOD;

    static final Method CALL_DIRECT;

    static final Method CALL_INLINED;

    static final Method CALL_INLINED_CALL;

    static final Method CALL_INDIRECT;

    static {
        try {
            CALL_DIRECT = OptimizedCallTarget.class.getDeclaredMethod("callDirect", Node.class, Object[].class);
            CALL_INLINED = OptimizedCallTarget.class.getDeclaredMethod("callInlined", Node.class, Object[].class);
            CALL_INLINED_CALL = GraalRuntimeSupport.class.getDeclaredMethod(GraalRuntimeSupport.CALL_INLINED_METHOD_NAME, Node.class, CallTarget.class, Object[].class);
            CALL_INDIRECT = OptimizedCallTarget.class.getDeclaredMethod("callIndirect", Node.class, Object[].class);
            CALL_TARGET_METHOD = OptimizedCallTarget.class.getDeclaredMethod("executeRootNode", VirtualFrame.class, CompilationState.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new InternalError(e);
        }
    }

    private final InspectedFrame callTargetFrame;

    private final InspectedFrame callNodeFrame;

    GraalFrameInstance(InspectedFrame callTargetFrame, InspectedFrame callNodeFrame) {
        this.callTargetFrame = callTargetFrame;
        this.callNodeFrame = callNodeFrame;
    }

    @TruffleBoundary
    protected static Frame getFrameFrom(InspectedFrame inspectedFrame, FrameAccess access) {
        if (access == FrameAccess.READ_WRITE || access == FrameAccess.MATERIALIZE) {
            if (inspectedFrame.isVirtual(FRAME_INDEX)) {
                inspectedFrame.materializeVirtualObjects(false);
            }
        }
        Frame frame = (Frame) inspectedFrame.getLocal(FRAME_INDEX);
        if (access == FrameAccess.MATERIALIZE) {
            frame = frame.materialize();
        }
        return frame;
    }

    @Override
    public Frame getFrame(FrameAccess access) {
        return getFrameFrom(callTargetFrame, access);
    }

    @Override
    public boolean isVirtualFrame() {
        return callTargetFrame.isVirtual(FRAME_INDEX);
    }

    @Override
    public int getCompilationTier() {
        return ((CompilationState) callTargetFrame.getLocal(OPTIMIZATION_TIER_FRAME_INDEX)).getTier();
    }

    @Override
    public boolean isCompilationRoot() {
        return ((CompilationState) callTargetFrame.getLocal(OPTIMIZATION_TIER_FRAME_INDEX)).isCompilationRoot();
    }

    @Override
    public CallTarget getCallTarget() {
        return (CallTarget) callTargetFrame.getLocal(CALL_TARGET_INDEX);
    }

    @Override
    public final Node getCallNode() {
        if (callNodeFrame != null) {
            Object receiver = callNodeFrame.getLocal(CALL_NODE_NOTIFY_INDEX);
            if (receiver instanceof Node) {
                return (Node) receiver;
            }
        }
        return null;
    }
}
