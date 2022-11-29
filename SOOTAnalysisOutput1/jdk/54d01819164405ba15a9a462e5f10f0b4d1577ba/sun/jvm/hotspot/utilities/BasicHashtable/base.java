package sun.jvm.hotspot.utilities;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.runtime.*;

public class BasicHashtable extends VMObject {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("BasicHashtable<mtInternal>");
        tableSizeField = type.getCIntegerField("_table_size");
        bucketsField = type.getAddressField("_buckets");
        bucketSize = db.lookupType("HashtableBucket<mtInternal>").getSize();
    }

    private static CIntegerField tableSizeField;

    private static AddressField bucketsField;

    private static long bucketSize;

    protected int tableSize() {
        return (int) tableSizeField.getValue(addr);
    }

    protected BasicHashtableEntry bucket(int i) {
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(i >= 0 && i < tableSize(), "Invalid bucket id");
        }
        Address tmp = bucketsField.getValue(addr);
        tmp = tmp.addOffsetTo(i * bucketSize);
        HashtableBucket bucket = (HashtableBucket) VMObjectFactory.newObject(HashtableBucket.class, tmp);
        return bucket.getEntry(getHashtableEntryClass());
    }

    protected Class getHashtableEntryClass() {
        return BasicHashtableEntry.class;
    }

    public BasicHashtable(Address addr) {
        super(addr);
    }
}
