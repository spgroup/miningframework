package sun.jvm.hotspot.ci;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;

public class ciMetadata extends ciBaseObject {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("ciMetadata");
        metadataField = new MetadataField(type.getAddressField("_metadata"), 0);
    }

    private static MetadataField metadataField;

    public Metadata getMetadata() {
        return metadataField.getValue(getAddress());
    }

    public ciMetadata(Address addr) {
        super(addr);
    }

    public void printOn(PrintStream out) {
        getMetadata().printValueOn(out);
    }

    public String toString() {
        return getMetadata().toString();
    }
}
