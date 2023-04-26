package com.oracle.truffle.llvm.runtime;

import java.util.HashMap;
import java.util.Map;
import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.llvm.runtime.LLVMFunctionCodeFactory.ResolveFunctionNodeGen;
import com.oracle.truffle.llvm.runtime.debug.type.LLVMSourceFunctionType;
import com.oracle.truffle.llvm.runtime.except.LLVMLinkerException;
import com.oracle.truffle.llvm.runtime.interop.LLVMForeignCallNode;
import com.oracle.truffle.llvm.runtime.interop.LLVMForeignConstructorCallNode;
import com.oracle.truffle.llvm.runtime.interop.LLVMForeignFunctionCallNode;
import com.oracle.truffle.llvm.runtime.interop.LLVMForeignIntrinsicCallNode;
import com.oracle.truffle.llvm.runtime.interop.access.LLVMInteropType;
import com.oracle.truffle.llvm.runtime.memory.LLVMHandleMemoryBase;
import com.oracle.truffle.llvm.runtime.nodes.api.LLVMExpressionNode;
import com.oracle.truffle.llvm.runtime.nodes.api.LLVMNode;
import com.oracle.truffle.llvm.runtime.pointer.LLVMNativePointer;
import com.oracle.truffle.llvm.runtime.types.FunctionType;
import com.oracle.truffle.llvm.runtime.types.Type;

public final class LLVMFunctionCode {

    private static final long SULONG_FUNCTION_POINTER_TAG = 0xBADE_FACE_0000_0000L;

    static {
        assert LLVMHandleMemoryBase.isCommonHandleMemory(SULONG_FUNCTION_POINTER_TAG);
        assert !LLVMHandleMemoryBase.isDerefHandleMemory(SULONG_FUNCTION_POINTER_TAG);
    }

    @CompilationFinal
    private Function functionFinal;

    private Function functionDynamic;

    @CompilationFinal
    private Assumption assumption;

    private final LLVMFunction llvmFunction;

    public LLVMFunctionCode(LLVMFunction llvmFunction) {
        this.llvmFunction = llvmFunction;
        this.functionFinal = this.functionDynamic = llvmFunction.getFunction();
        this.assumption = Truffle.getRuntime().createAssumption();
    }

    private static long tagSulongFunctionPointer(int id) {
        return id | SULONG_FUNCTION_POINTER_TAG;
    }

    public static final class Intrinsic {

        private final String intrinsicName;

        private final Map<FunctionType, RootCallTarget> overloadingMap;

        private final LLVMIntrinsicProvider provider;

        private final NodeFactory nodeFactory;

        public Intrinsic(LLVMIntrinsicProvider provider, String name, NodeFactory nodeFactory) {
            this.intrinsicName = name;
            this.overloadingMap = new HashMap<>();
            this.provider = provider;
            this.nodeFactory = nodeFactory;
        }

        public RootCallTarget cachedCallTarget(FunctionType type) {
            if (exists(type)) {
                return get(type);
            } else {
                return generateTarget(type);
            }
        }

        public LLVMExpressionNode createIntrinsicNode(LLVMExpressionNode[] arguments, Type[] argTypes) {
            return provider.generateIntrinsicNode(intrinsicName, arguments, argTypes, nodeFactory);
        }

        @TruffleBoundary
        private boolean exists(FunctionType type) {
            return overloadingMap.containsKey(type);
        }

        @TruffleBoundary
        private RootCallTarget get(FunctionType type) {
            return overloadingMap.get(type);
        }

        private RootCallTarget generateTarget(FunctionType type) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            RootCallTarget newTarget = provider.generateIntrinsicTarget(intrinsicName, type.getArgumentTypes(), nodeFactory);
            assert newTarget != null;
            overloadingMap.put(type, newTarget);
            return newTarget;
        }
    }

    public interface LazyToTruffleConverter {

        RootCallTarget convert();

        LLVMSourceFunctionType getSourceType();
    }

    public abstract static class Function {

        void resolve(@SuppressWarnings("unused") LLVMFunctionCode descriptor) {
            CompilerAsserts.neverPartOfCompilation();
        }

        abstract Object createNativeWrapper(LLVMFunctionDescriptor descriptor);

        LLVMSourceFunctionType getSourceType() {
            return null;
        }
    }

    @GenerateUncached
    public abstract static class ResolveFunctionNode extends LLVMNode {

        abstract Function execute(Function function, LLVMFunctionCode descriptor);

        @Specialization
        @TruffleBoundary
        Function doLazyLLVMIRFunction(LazyLLVMIRFunction function, LLVMFunctionCode descriptor) {
            function.resolve(descriptor);
            return descriptor.getFunction();
        }

        @Specialization
        @TruffleBoundary
        Function doUnresolvedFunction(UnresolvedFunction function, LLVMFunctionCode descriptor) {
            function.resolve(descriptor);
            return descriptor.getFunction();
        }

        private static boolean resolveDoesNothing(Function function, LLVMFunctionCode descriptor) {
            function.resolve(descriptor);
            return descriptor.getFunction() == function;
        }

        @Fallback
        Function doOther(Function function, LLVMFunctionCode descriptor) {
            assert resolveDoesNothing(function, descriptor);
            return function;
        }
    }

    abstract static class ManagedFunction extends Function {

        @Override
        Object createNativeWrapper(LLVMFunctionDescriptor descriptor) {
            CompilerAsserts.neverPartOfCompilation();
            LLVMContext context = LLVMLanguage.getContext();
            Object wrapper = null;
            LLVMNativePointer pointer = null;
            NativeContextExtension nativeContextExtension = context.getContextExtensionOrNull(NativeContextExtension.class);
            if (nativeContextExtension != null) {
                wrapper = nativeContextExtension.createNativeWrapper(descriptor.getLLVMFunction(), descriptor.getFunctionCode());
                if (wrapper != null) {
                    try {
                        pointer = LLVMNativePointer.create(InteropLibrary.getFactory().getUncached().asPointer(wrapper));
                    } catch (UnsupportedMessageException e) {
                        CompilerDirectives.transferToInterpreter();
                        throw new AssertionError(e);
                    }
                }
            }
            if (wrapper == null) {
                pointer = LLVMNativePointer.create(tagSulongFunctionPointer(descriptor.getLLVMFunction().getSymbolIndex(false)));
                wrapper = pointer;
            }
            context.registerFunctionPointer(pointer, descriptor);
            return wrapper;
        }
    }

    public static final class LazyLLVMIRFunction extends ManagedFunction {

        private final LazyToTruffleConverter converter;

        public LazyLLVMIRFunction(LazyToTruffleConverter converter) {
            this.converter = converter;
        }

        @Override
        void resolve(LLVMFunctionCode descriptor) {
            final RootCallTarget callTarget = converter.convert();
            final LLVMSourceFunctionType sourceType = converter.getSourceType();
            descriptor.setFunction(new LLVMIRFunction(callTarget, sourceType));
        }

        @Override
        LLVMSourceFunctionType getSourceType() {
            return converter.getSourceType();
        }
    }

    public static final class LLVMIRFunction extends ManagedFunction {

        private final RootCallTarget callTarget;

        private final LLVMSourceFunctionType sourceType;

        public LLVMIRFunction(RootCallTarget callTarget, LLVMSourceFunctionType sourceType) {
            this.callTarget = callTarget;
            this.sourceType = sourceType;
        }

        @Override
        LLVMSourceFunctionType getSourceType() {
            return sourceType;
        }
    }

    public static final class UnresolvedFunction extends Function {

        @Override
        void resolve(LLVMFunctionCode functionCode) {
            throw new LLVMLinkerException(String.format("Unresolved external function %s cannot be found.", functionCode.getLLVMFunction().getName()));
        }

        @Override
        Object createNativeWrapper(LLVMFunctionDescriptor descriptor) {
            CompilerAsserts.neverPartOfCompilation();
            resolve(descriptor.getFunctionCode());
            return descriptor.getFunctionCode().getFunction().createNativeWrapper(descriptor);
        }
    }

    public static final class IntrinsicFunction extends LLVMFunctionCode.ManagedFunction {

        private final LLVMFunctionCode.Intrinsic intrinsic;

        private final LLVMSourceFunctionType sourceType;

        public IntrinsicFunction(LLVMFunctionCode.Intrinsic intrinsic, LLVMSourceFunctionType sourceType) {
            this.intrinsic = intrinsic;
            this.sourceType = sourceType;
        }

        @Override
        LLVMSourceFunctionType getSourceType() {
            return this.sourceType;
        }
    }

    public static final class NativeFunction extends LLVMFunctionCode.Function {

        private final Object nativeFunction;

        public NativeFunction(Object nativeFunction) {
            this.nativeFunction = nativeFunction;
        }

        @Override
        Object createNativeWrapper(LLVMFunctionDescriptor descriptor) {
            return nativeFunction;
        }
    }

    public void resolveIfLazyLLVMIRFunction() {
        CompilerAsserts.neverPartOfCompilation();
        if (getFunction() instanceof LLVMFunctionCode.LazyLLVMIRFunction) {
            getFunction().resolve(this);
            assert getFunction() instanceof LLVMFunctionCode.LLVMIRFunction;
        }
    }

    public boolean isLLVMIRFunction() {
        final LLVMFunctionCode.Function currentFunction = getFunction();
        return currentFunction instanceof LLVMFunctionCode.LLVMIRFunction || currentFunction instanceof LLVMFunctionCode.LazyLLVMIRFunction;
    }

    public boolean isIntrinsicFunctionSlowPath() {
        CompilerAsserts.neverPartOfCompilation();
        return isIntrinsicFunction(ResolveFunctionNodeGen.getUncached());
    }

    public boolean isIntrinsicFunction(LLVMFunctionCode.ResolveFunctionNode resolve) {
        return resolve.execute(getFunction(), this) instanceof LLVMFunctionCode.IntrinsicFunction;
    }

    public boolean isNativeFunctionSlowPath() {
        CompilerAsserts.neverPartOfCompilation();
        return isNativeFunction(ResolveFunctionNodeGen.getUncached());
    }

    public boolean isNativeFunction(LLVMFunctionCode.ResolveFunctionNode resolve) {
        return resolve.execute(getFunction(), this) instanceof LLVMFunctionCode.NativeFunction;
    }

    public boolean isDefined() {
        return !(getFunction() instanceof LLVMFunctionCode.UnresolvedFunction);
    }

    public void define(LLVMIntrinsicProvider intrinsicProvider, NodeFactory nodeFactory) {
        Intrinsic intrinsification = new Intrinsic(intrinsicProvider, llvmFunction.getName(), nodeFactory);
        define(new IntrinsicFunction(intrinsification, getFunction().getSourceType()));
    }

    public void define(Function newFunction) {
        setFunction(newFunction);
    }

    public RootCallTarget getLLVMIRFunctionSlowPath() {
        CompilerAsserts.neverPartOfCompilation();
        return getLLVMIRFunction(ResolveFunctionNodeGen.getUncached());
    }

    public RootCallTarget getLLVMIRFunction(ResolveFunctionNode resolve) {
        Function fn = resolve.execute(getFunction(), this);
        return ((LLVMIRFunction) fn).callTarget;
    }

    public Intrinsic getIntrinsicSlowPath() {
        CompilerAsserts.neverPartOfCompilation();
        return getIntrinsic(ResolveFunctionNodeGen.getUncached());
    }

    public Intrinsic getIntrinsic(ResolveFunctionNode resolve) {
        Function fn = resolve.execute(getFunction(), this);
        return ((IntrinsicFunction) fn).intrinsic;
    }

    public Object getNativeFunctionSlowPath() {
        CompilerAsserts.neverPartOfCompilation();
        return getNativeFunction(ResolveFunctionNodeGen.getUncached());
    }

    public Object getNativeFunction(ResolveFunctionNode resolve) {
        Function fn = resolve.execute(getFunction(), this);
        Object nativeFunction = ((NativeFunction) fn).nativeFunction;
        if (nativeFunction == null) {
            CompilerDirectives.transferToInterpreter();
            throw new LLVMLinkerException("Native function " + fn.toString() + " not found");
        }
        return nativeFunction;
    }

    private CallTarget foreignFunctionCallTarget;

    private CallTarget foreignConstructorCallTarget;

    CallTarget getForeignCallTarget(LLVMFunctionDescriptor functionDescriptor) {
        if (foreignFunctionCallTarget == null) {
            CompilerDirectives.transferToInterpreter();
            LLVMLanguage language = LLVMLanguage.getLanguage();
            LLVMSourceFunctionType sourceType = getFunction().getSourceType();
            LLVMInteropType interopType = language.getInteropType(sourceType);
            RootNode foreignCall;
            if (isIntrinsicFunctionSlowPath()) {
                FunctionType type = functionDescriptor.getLLVMFunction().getType();
                foreignCall = LLVMForeignIntrinsicCallNode.create(language, getIntrinsicSlowPath(), type, (LLVMInteropType.Function) interopType);
            } else {
                foreignCall = LLVMForeignFunctionCallNode.create(language, functionDescriptor, interopType, sourceType);
            }
            foreignFunctionCallTarget = LLVMLanguage.createCallTarget(foreignCall);
            assert foreignFunctionCallTarget != null;
        }
        return foreignFunctionCallTarget;
    }

    CallTarget getForeignConstructorCallTarget(LLVMFunctionDescriptor functionDescriptor) {
        if (foreignConstructorCallTarget == null) {
            CompilerDirectives.transferToInterpreter();
            LLVMLanguage language = LLVMLanguage.getLanguage();
            LLVMSourceFunctionType sourceType = getFunction().getSourceType();
            LLVMInteropType interopType = language.getInteropType(sourceType);
            LLVMInteropType extractedType = ((LLVMInteropType.Function) interopType).getParameter(0);
            if (extractedType instanceof LLVMInteropType.Value) {
                LLVMInteropType.Structured structured = ((LLVMInteropType.Value) extractedType).baseType;
                LLVMForeignCallNode foreignCall = LLVMForeignConstructorCallNode.create(language, functionDescriptor, interopType, sourceType, structured);
                foreignConstructorCallTarget = LLVMLanguage.createCallTarget(foreignCall);
            }
            assert foreignConstructorCallTarget != null;
        }
        return foreignConstructorCallTarget;
    }

    private void setFunction(Function newFunction) {
        this.functionDynamic = this.functionFinal = newFunction;
        this.assumption.invalidate();
        this.assumption = Truffle.getRuntime().createAssumption();
    }

    public Function getFunction() {
        if (CompilerDirectives.isPartialEvaluationConstant(this)) {
            if (!assumption.isValid()) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return functionDynamic;
            }
            return functionFinal;
        } else {
            return functionDynamic;
        }
    }

    public LLVMFunction getLLVMFunction() {
        return llvmFunction;
    }
}
