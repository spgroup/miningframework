package sun.jvm.hotspot.code;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.*;

public class VMRegImpl {

    private static VMReg stack0;

    private static int stack0Val;

    private static Address stack0Addr;

    private static AddressField regNameField;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static void initialize(TypeDataBase db) {
        Type type = db.lookupType("VMRegImpl");
        AddressField stack0Field = type.getAddressField("stack0");
        stack0Addr = stack0Field.getValue();
        stack0Val = (int) stack0Addr.hashCode();
        stack0 = new VMReg(stack0Val);
        regNameField = type.getAddressField("regName[0]");
    }

    public static VMReg getStack0() {
        return stack0;
    }

    public static String getRegisterName(int index) {
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(index >= 0 && index < stack0Val, "invalid index : " + index);
        }
        Address regName = regNameField.getStaticFieldAddress();
        long addrSize = VM.getVM().getAddressSize();
        return CStringUtilities.getString(regName.getAddressAt(index * addrSize));
    }
}
