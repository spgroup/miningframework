package sun.jvm.hotspot.memory;

import java.util.*;
import sun.jvm.hotspot.classfile.ClassLoaderData;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.utilities.*;

public class PlaceholderEntry extends sun.jvm.hotspot.utilities.HashtableEntry {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("PlaceholderEntry");
        loaderDataField = type.getAddressField("_loader_data");
    }

    private static AddressField loaderDataField;

    public Oop loader() {
        return loaderData().getClassLoader();
    }

    public ClassLoaderData loaderData() {
        return ClassLoaderData.instantiateWrapperFor(loaderDataField.getValue(addr));
    }

    public PlaceholderEntry(Address addr) {
        super(addr);
    }

    public Symbol klass() {
        return Symbol.create(literalValue());
    }
}
