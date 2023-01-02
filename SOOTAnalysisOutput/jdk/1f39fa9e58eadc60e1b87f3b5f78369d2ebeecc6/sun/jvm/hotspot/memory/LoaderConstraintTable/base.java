package sun.jvm.hotspot.memory;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.utilities.*;

public class LoaderConstraintTable extends TwoOopHashtable {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("LoaderConstraintTable");
    }

    public LoaderConstraintTable(Address addr) {
        super(addr);
    }

    protected Class getHashtableEntryClass() {
        return LoaderConstraintEntry.class;
    }
}
