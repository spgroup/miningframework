package sun.jvm.hotspot.memory;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.utilities.*;

public class LoaderConstraintEntry extends sun.jvm.hotspot.utilities.HashtableEntry {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("LoaderConstraintEntry");
        nameField = type.getAddressField("_name");
        numLoadersField = type.getCIntegerField("_num_loaders");
        maxLoadersField = type.getCIntegerField("_max_loaders");
        loadersField = type.getAddressField("_loaders");
    }

    private static AddressField nameField;

    private static CIntegerField numLoadersField;

    private static CIntegerField maxLoadersField;

    private static AddressField loadersField;

    public Symbol name() {
        return Symbol.create(nameField.getValue(addr));
    }

    public int numLoaders() {
        return (int) numLoadersField.getValue(addr);
    }

    public int maxLoaders() {
        return (int) maxLoadersField.getValue(addr);
    }

    public Oop initiatingLoader(int i) {
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(i >= 0 && i < numLoaders(), "invalid index");
        }
        Address loaders = loadersField.getValue(addr);
        OopHandle loader = loaders.addOffsetToAsOopHandle(i * VM.getVM().getOopSize());
        return VM.getVM().getObjectHeap().newOop(loader);
    }

    public LoaderConstraintEntry(Address addr) {
        super(addr);
    }
}
