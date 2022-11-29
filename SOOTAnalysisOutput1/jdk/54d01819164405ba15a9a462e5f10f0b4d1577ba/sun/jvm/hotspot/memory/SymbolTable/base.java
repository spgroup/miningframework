package sun.jvm.hotspot.memory;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.utilities.*;

public class SymbolTable extends sun.jvm.hotspot.utilities.Hashtable {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("SymbolTable");
        theTableField = type.getAddressField("_the_table");
        sharedTableField = type.getAddressField("_shared_table");
    }

    private static AddressField theTableField;

    private static AddressField sharedTableField;

    private CompactHashTable sharedTable;

    public static SymbolTable getTheTable() {
        Address tmp = theTableField.getValue();
        SymbolTable table = (SymbolTable) VMObjectFactory.newObject(SymbolTable.class, tmp);
        Address shared = sharedTableField.getStaticFieldAddress();
        table.sharedTable = (CompactHashTable) VMObjectFactory.newObject(CompactHashTable.class, shared);
        return table;
    }

    public SymbolTable(Address addr) {
        super(addr);
    }

    public Symbol probe(String name) {
        try {
            return probe(toModifiedUTF8Bytes(name));
        } catch (IOException e) {
            return null;
        }
    }

    public Symbol probe(byte[] name) {
        long hashValue = hashSymbol(name);
        for (HashtableEntry e = (HashtableEntry) bucket(hashToIndex(hashValue)); e != null; e = (HashtableEntry) e.next()) {
            if (e.hash() == hashValue) {
                Symbol sym = Symbol.create(e.literalValue());
                if (sym.equals(name)) {
                    return sym;
                }
            }
        }
        return sharedTable.probe(name, hashValue);
    }

    public interface SymbolVisitor {

        public void visit(Symbol sym);
    }

    public void symbolsDo(SymbolVisitor visitor) {
        int numBuckets = tableSize();
        for (int i = 0; i < numBuckets; i++) {
            for (HashtableEntry e = (HashtableEntry) bucket(i); e != null; e = (HashtableEntry) e.next()) {
                visitor.visit(Symbol.create(e.literalValue()));
            }
        }
    }

    private static byte[] toModifiedUTF8Bytes(String name) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(name);
        dos.flush();
        byte[] buf = baos.toByteArray();
        byte[] res = new byte[buf.length - 2];
        System.arraycopy(buf, 2, res, 0, res.length);
        return res;
    }
}
