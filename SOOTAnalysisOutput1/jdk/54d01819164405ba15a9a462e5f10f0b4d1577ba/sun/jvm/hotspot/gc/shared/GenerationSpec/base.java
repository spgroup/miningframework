package sun.jvm.hotspot.gc.shared;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;

public class GenerationSpec extends VMObject {

    private static CIntegerField nameField;

    private static CIntegerField initSizeField;

    private static CIntegerField maxSizeField;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("GenerationSpec");
        nameField = type.getCIntegerField("_name");
        initSizeField = type.getCIntegerField("_init_size");
        maxSizeField = type.getCIntegerField("_max_size");
    }

    public GenerationSpec(Address addr) {
        super(addr);
    }

    public Generation.Name name() {
        return Generation.nameForEnum((int) nameField.getValue(addr));
    }

    public long initSize() {
        return initSizeField.getValue(addr);
    }

    public long maxSize() {
        return maxSizeField.getValue(addr);
    }
}
