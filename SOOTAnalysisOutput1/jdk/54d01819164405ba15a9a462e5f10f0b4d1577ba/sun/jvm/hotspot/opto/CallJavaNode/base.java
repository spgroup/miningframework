package sun.jvm.hotspot.opto;

import java.util.*;
import java.io.*;
import sun.jvm.hotspot.code.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.ci.*;
import sun.jvm.hotspot.types.*;

public class CallJavaNode extends CallNode {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("CallJavaNode");
        methodField = type.getAddressField("_method");
    }

    private static AddressField methodField;

    public CallJavaNode(Address addr) {
        super(addr);
    }

    public ciMethod method() {
        return (ciMethod) ciObjectFactory.getMetadata(methodField.getValue(getAddress()));
    }

    public void dumpSpec(PrintStream out) {
        if (method() != null) {
            out.print(" " + method().method().externalNameAndSignature());
        }
        super.dumpSpec(out);
    }
}
