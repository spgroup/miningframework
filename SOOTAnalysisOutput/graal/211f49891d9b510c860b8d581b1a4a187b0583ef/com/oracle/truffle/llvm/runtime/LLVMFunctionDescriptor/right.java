package com.oracle.truffle.llvm.runtime;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.llvm.runtime.interop.LLVMInternalTruffleObject;
import com.oracle.truffle.llvm.runtime.memory.LLVMHandleMemoryBase;

@ExportLibrary(InteropLibrary.class)
@SuppressWarnings("static-method")
public final class LLVMFunctionDescriptor extends LLVMInternalTruffleObject implements Comparable<LLVMFunctionDescriptor> {

    private static final long SULONG_FUNCTION_POINTER_TAG = 0xBADE_FACE_0000_0000L;

    static {
        assert LLVMHandleMemoryBase.isCommonHandleMemory(SULONG_FUNCTION_POINTER_TAG);
        assert !LLVMHandleMemoryBase.isDerefHandleMemory(SULONG_FUNCTION_POINTER_TAG);
    }

    private final LLVMFunction llvmFunction;

    private final LLVMFunctionCode functionCode;

    @CompilationFinal
    private Object nativeWrapper;

    @CompilationFinal
    private long nativePointer;

    private static long tagSulongFunctionPointer(int id) {
        return id | SULONG_FUNCTION_POINTER_TAG;
    }

    public LLVMFunction getLLVMFunction() {
        return llvmFunction;
    }

    public LLVMFunctionCode getFunctionCode() {
        return functionCode;
    }

    public long getNativePointer() {
        return nativePointer;
    }

    public LLVMFunctionDescriptor(LLVMFunction llvmFunction, LLVMFunctionCode functionCode) {
        CompilerAsserts.neverPartOfCompilation();
        this.llvmFunction = llvmFunction;
        this.functionCode = functionCode;
    }

    @Override
    public String toString() {
        return String.format("function@%d '%s'", llvmFunction.getSymbolIndex(true), llvmFunction.getName());
    }

    @Override
    public int compareTo(LLVMFunctionDescriptor o) {
        int otherIndex = o.llvmFunction.getSymbolIndex(true);
        int otherID = o.llvmFunction.getBitcodeID(true);
        int index = llvmFunction.getSymbolIndex(true);
        int id = llvmFunction.getBitcodeID(true);
        if (id == otherID) {
            return Long.compare(index, otherIndex);
        }
        throw new IllegalStateException("Comparing functions from different bitcode files.");
    }

    @ExportMessage
    long asPointer() throws UnsupportedMessageException {
        if (isPointer()) {
            return nativePointer;
        }
        CompilerDirectives.transferToInterpreter();
        throw UnsupportedMessageException.create();
    }

    @ExportMessage
    boolean isPointer() {
        return nativeWrapper != null;
    }

    @ExportMessage
    LLVMFunctionDescriptor toNative() {
        if (nativeWrapper == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            nativeWrapper = functionCode.getFunction().createNativeWrapper(this);
            try {
                nativePointer = InteropLibrary.getFactory().getUncached().asPointer(nativeWrapper);
            } catch (UnsupportedMessageException ex) {
                nativePointer = tagSulongFunctionPointer(llvmFunction.getSymbolIndex(true));
            }
        }
        return this;
    }

    @ExportMessage
    boolean isExecutable() {
        return true;
    }

    @ExportMessage
    static class Execute {

        @Specialization(limit = "5", guards = "self == cachedSelf", assumptions = "singleContextAssumption()")
        static Object doDescriptor(@SuppressWarnings("unused") LLVMFunctionDescriptor self, Object[] args, @Cached("self") @SuppressWarnings("unused") LLVMFunctionDescriptor cachedSelf, @Cached("createCall(cachedSelf)") DirectCallNode call) {
            return call.call(args);
        }

        @Specialization(replaces = "doDescriptor", limit = "5", guards = "self.getFunctionCode() == cachedFunctionCode")
        static Object doCached(@SuppressWarnings("unused") LLVMFunctionDescriptor self, Object[] args, @Cached("self.getFunctionCode()") @SuppressWarnings("unused") LLVMFunctionCode cachedFunctionCode, @Cached("createCall(self)") DirectCallNode call) {
            return call.call(args);
        }

        @Specialization(replaces = "doCached")
        static Object doPolymorphic(LLVMFunctionDescriptor self, Object[] args, @Exclusive @Cached IndirectCallNode call) {
            return call.call(self.getFunctionCode().getForeignCallTarget(self), args);
        }

        protected static DirectCallNode createCall(LLVMFunctionDescriptor self) {
            DirectCallNode callNode = DirectCallNode.create(self.getFunctionCode().getForeignCallTarget(self));
            callNode.forceInlining();
            return callNode;
        }

        protected static Assumption singleContextAssumption() {
            return LLVMLanguage.getLanguage().singleContextAssumption;
        }
    }

    @ExportMessage
    boolean hasMembers() {
        return true;
    }

    @ExportLibrary(InteropLibrary.class)
    static final class FunctionMembers implements TruffleObject {

        @ExportMessage
        boolean hasArrayElements() {
            return true;
        }

        @ExportMessage
        long getArraySize() {
            return 1;
        }

        @ExportMessage
        boolean isArrayElementReadable(long index) {
            return index == 0;
        }

        @ExportMessage
        Object readArrayElement(long index) throws InvalidArrayIndexException {
            if (index == 0) {
                return "bind";
            } else {
                throw InvalidArrayIndexException.create(index);
            }
        }
    }

    @ExportMessage
    Object getMembers(@SuppressWarnings("unused") boolean includeInternal) {
        return new FunctionMembers();
    }

    @SuppressWarnings("static-method")
    @ExportMessage
    boolean isMemberInvocable(String member) {
        return "bind".equals(member);
    }

    @ExportMessage
    Object invokeMember(String member, @SuppressWarnings("unused") Object[] args) throws UnknownIdentifierException {
        if ("bind".equals(member)) {
            return this;
        } else {
            throw UnknownIdentifierException.create(member);
        }
    }

    @ExportMessage
    boolean isInstantiable() {
        return true;
    }

    @ExportMessage
    Object instantiate(Object[] arguments, @Exclusive @Cached IndirectCallNode call) {
        final Object[] newArgs = new Object[arguments.length + 1];
        for (int i = 0; i < arguments.length; i++) {
            newArgs[i + 1] = arguments[i];
        }
        return call.call(functionCode.getForeignConstructorCallTarget(this), newArgs);
    }
}
