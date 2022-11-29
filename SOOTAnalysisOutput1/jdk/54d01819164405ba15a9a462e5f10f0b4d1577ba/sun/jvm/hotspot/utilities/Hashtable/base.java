package sun.jvm.hotspot.utilities;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.runtime.*;

public class Hashtable extends BasicHashtable {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("IntptrHashtable");
    }

    protected Class getHashtableEntryClass() {
        return HashtableEntry.class;
    }

    public int hashToIndex(long fullHash) {
        return (int) (fullHash % tableSize());
    }

    public Hashtable(Address addr) {
        super(addr);
    }

    protected static long hashSymbol(byte[] buf) {
        long h = 0;
        int s = 0;
        int len = buf.length;
        while (len-- > 0) {
            h = 31 * h + (0xFFL & buf[s]);
            s++;
        }
        return h & 0xFFFFFFFFL;
    }
}
