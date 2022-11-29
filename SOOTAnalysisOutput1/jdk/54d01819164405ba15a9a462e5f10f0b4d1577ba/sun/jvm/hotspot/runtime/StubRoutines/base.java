package sun.jvm.hotspot.runtime;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.types.*;

public class StubRoutines {

    private static AddressField callStubReturnAddressField;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("StubRoutines");
        callStubReturnAddressField = type.getAddressField("_call_stub_return_address");
    }

    public StubRoutines() {
    }

    public boolean returnsToCallStub(Address returnPC) {
        Address addr = callStubReturnAddressField.getValue();
        if (addr == null) {
            return (addr == returnPC);
        } else {
            return (addr.equals(returnPC));
        }
    }
}
