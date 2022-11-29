package sun.jvm.hotspot.code;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.memory.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.*;

public class CodeCache {

    private static GrowableArray<CodeHeap> heapArray;

    private static AddressField scavengeRootNMethodsField;

    private static VirtualConstructor virtualConstructor;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("CodeCache");
        Type codeHeapType = db.lookupType("CodeHeap");
        VirtualBaseConstructor heapConstructor = new VirtualBaseConstructor(db, codeHeapType, "sun.jvm.hotspot.memory", CodeHeap.class);
        AddressField heapsField = type.getAddressField("_heaps");
        heapArray = GrowableArray.create(heapsField.getValue(), heapConstructor);
        scavengeRootNMethodsField = type.getAddressField("_scavenge_root_nmethods");
        virtualConstructor = new VirtualConstructor(db);
        virtualConstructor.addMapping("BufferBlob", BufferBlob.class);
        virtualConstructor.addMapping("nmethod", NMethod.class);
        virtualConstructor.addMapping("RuntimeStub", RuntimeStub.class);
        virtualConstructor.addMapping("AdapterBlob", AdapterBlob.class);
        virtualConstructor.addMapping("MethodHandlesAdapterBlob", MethodHandlesAdapterBlob.class);
        virtualConstructor.addMapping("SafepointBlob", SafepointBlob.class);
        virtualConstructor.addMapping("DeoptimizationBlob", DeoptimizationBlob.class);
        if (VM.getVM().isServerCompiler()) {
            virtualConstructor.addMapping("ExceptionBlob", ExceptionBlob.class);
            virtualConstructor.addMapping("UncommonTrapBlob", UncommonTrapBlob.class);
        }
    }

    public NMethod scavengeRootMethods() {
        return (NMethod) VMObjectFactory.newObject(NMethod.class, scavengeRootNMethodsField.getValue());
    }

    public boolean contains(Address p) {
        for (int i = 0; i < heapArray.length(); ++i) {
            if (heapArray.at(i).contains(p)) {
                return true;
            }
        }
        return false;
    }

    public CodeBlob findBlob(Address start) {
        CodeBlob result = findBlobUnsafe(start);
        if (result == null)
            return null;
        if (VM.getVM().isDebugging()) {
            return result;
        }
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(!(result.isZombie() || result.isLockedByVM()), "unsafe access to zombie method");
        }
        return result;
    }

    public CodeBlob findBlobUnsafe(Address start) {
        CodeBlob result = null;
        CodeHeap containing_heap = null;
        for (int i = 0; i < heapArray.length(); ++i) {
            if (heapArray.at(i).contains(start)) {
                containing_heap = heapArray.at(i);
                break;
            }
        }
        if (containing_heap == null) {
            return null;
        }
        try {
            result = (CodeBlob) virtualConstructor.instantiateWrapperFor(containing_heap.findStart(start));
        } catch (WrongTypeException wte) {
            Address cbAddr = null;
            try {
                cbAddr = containing_heap.findStart(start);
            } catch (Exception findEx) {
                findEx.printStackTrace();
            }
            String message = "Couldn't deduce type of CodeBlob ";
            if (cbAddr != null) {
                message = message + "@" + cbAddr + " ";
            }
            message = message + "for PC=" + start;
            throw new RuntimeException(message, wte);
        }
        if (result == null)
            return null;
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(result.blobContains(start) || result.blobContains(start.addOffsetTo(8)), "found wrong CodeBlob");
        }
        return result;
    }

    public NMethod findNMethod(Address start) {
        CodeBlob cb = findBlob(start);
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(cb == null || cb.isNMethod(), "did not find an nmethod");
        }
        return (NMethod) cb;
    }

    public NMethod findNMethodUnsafe(Address start) {
        CodeBlob cb = findBlobUnsafe(start);
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(cb == null || cb.isNMethod(), "did not find an nmethod");
        }
        return (NMethod) cb;
    }

    public CodeBlob createCodeBlobWrapper(Address codeBlobAddr) {
        try {
            return (CodeBlob) virtualConstructor.instantiateWrapperFor(codeBlobAddr);
        } catch (Exception e) {
            String message = "Unable to deduce type of CodeBlob from address " + codeBlobAddr + " (expected type nmethod, RuntimeStub, ";
            if (VM.getVM().isClientCompiler()) {
                message = message + " or ";
            }
            message = message + "SafepointBlob";
            if (VM.getVM().isServerCompiler()) {
                message = message + ", DeoptimizationBlob, or ExceptionBlob";
            }
            message = message + ")";
            throw new RuntimeException(message);
        }
    }

    public void iterate(CodeCacheVisitor visitor) {
        visitor.prologue(lowBound(), highBound());
        for (int i = 0; i < heapArray.length(); ++i) {
            CodeHeap current_heap = heapArray.at(i);
            current_heap.iterate(visitor, this);
        }
        visitor.epilogue();
    }

    private Address lowBound() {
        Address low = heapArray.at(0).begin();
        for (int i = 1; i < heapArray.length(); ++i) {
            if (heapArray.at(i).begin().lessThan(low)) {
                low = heapArray.at(i).begin();
            }
        }
        return low;
    }

    private Address highBound() {
        Address high = heapArray.at(0).end();
        for (int i = 1; i < heapArray.length(); ++i) {
            if (heapArray.at(i).end().greaterThan(high)) {
                high = heapArray.at(i).end();
            }
        }
        return high;
    }
}
