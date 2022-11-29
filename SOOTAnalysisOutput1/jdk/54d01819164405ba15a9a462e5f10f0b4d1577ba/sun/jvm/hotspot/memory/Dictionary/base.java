package sun.jvm.hotspot.memory;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.utilities.*;

public class Dictionary extends TwoOopHashtable {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("Dictionary");
    }

    public Dictionary(Address addr) {
        super(addr);
    }

    protected Class getHashtableEntryClass() {
        return DictionaryEntry.class;
    }

    public void classesDo(SystemDictionary.ClassVisitor v) {
        ObjectHeap heap = VM.getVM().getObjectHeap();
        int tblSize = tableSize();
        for (int index = 0; index < tblSize; index++) {
            for (DictionaryEntry probe = (DictionaryEntry) bucket(index); probe != null; probe = (DictionaryEntry) probe.next()) {
                Klass k = probe.klass();
                if (heap.equal(probe.loader(), ((InstanceKlass) k).getClassLoader())) {
                    v.visit(k);
                }
            }
        }
    }

    public void classesDo(SystemDictionary.ClassAndLoaderVisitor v) {
        int tblSize = tableSize();
        for (int index = 0; index < tblSize; index++) {
            for (DictionaryEntry probe = (DictionaryEntry) bucket(index); probe != null; probe = (DictionaryEntry) probe.next()) {
                Klass k = probe.klass();
                v.visit(k, probe.loader());
            }
        }
    }

    public Klass find(int index, long hash, Symbol className, Oop classLoader, Oop protectionDomain) {
        DictionaryEntry entry = getEntry(index, hash, className, classLoader);
        if (entry != null && entry.isValidProtectionDomain(protectionDomain)) {
            return entry.klass();
        }
        return null;
    }

    private DictionaryEntry getEntry(int index, long hash, Symbol className, Oop classLoader) {
        for (DictionaryEntry entry = (DictionaryEntry) bucket(index); entry != null; entry = (DictionaryEntry) entry.next()) {
            if (entry.hash() == hash && entry.equals(className, classLoader)) {
                return entry;
            }
        }
        return null;
    }
}
