package sun.jvm.hotspot.utilities;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.utilities.*;

public class CompactHashTable extends VMObject {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("SymbolCompactHashTable");
        baseAddressField = type.getAddressField("_base_address");
        bucketCountField = type.getCIntegerField("_bucket_count");
        tableEndOffsetField = type.getCIntegerField("_table_end_offset");
        bucketsField = type.getAddressField("_buckets");
        uintSize = db.lookupType("juint").getSize();
    }

    private static CIntegerField bucketCountField;

    private static CIntegerField tableEndOffsetField;

    private static AddressField baseAddressField;

    private static AddressField bucketsField;

    private static long uintSize;

    private static int BUCKET_OFFSET_MASK = 0x3FFFFFFF;

    private static int BUCKET_TYPE_SHIFT = 30;

    private static int COMPACT_BUCKET_TYPE = 1;

    public CompactHashTable(Address addr) {
        super(addr);
    }

    private int bucketCount() {
        return (int) bucketCountField.getValue(addr);
    }

    private int tableEndOffset() {
        return (int) tableEndOffsetField.getValue(addr);
    }

    private boolean isCompactBucket(int bucket_info) {
        return (bucket_info >> BUCKET_TYPE_SHIFT) == COMPACT_BUCKET_TYPE;
    }

    private int bucketOffset(int bucket_info) {
        return bucket_info & BUCKET_OFFSET_MASK;
    }

    public Symbol probe(byte[] name, long hash) {
        long symOffset;
        Symbol sym;
        Address baseAddress = baseAddressField.getValue(addr);
        Address bucket = bucketsField.getValue(addr);
        Address bucketEnd = bucket;
        long index = hash % bucketCount();
        int bucketInfo = (int) bucket.getCIntegerAt(index * uintSize, uintSize, true);
        int bucketOffset = bucketOffset(bucketInfo);
        int nextBucketInfo = (int) bucket.getCIntegerAt((index + 1) * uintSize, uintSize, true);
        int nextBucketOffset = bucketOffset(nextBucketInfo);
        bucket = bucket.addOffsetTo(bucketOffset * uintSize);
        if (isCompactBucket(bucketInfo)) {
            symOffset = bucket.getCIntegerAt(0, uintSize, true);
            sym = Symbol.create(baseAddress.addOffsetTo(symOffset));
            if (sym.equals(name)) {
                return sym;
            }
        } else {
            bucketEnd = bucket.addOffsetTo(nextBucketOffset * uintSize);
            while (bucket.lessThan(bucketEnd)) {
                long symHash = bucket.getCIntegerAt(0, uintSize, true);
                if (symHash == hash) {
                    symOffset = bucket.getCIntegerAt(uintSize, uintSize, true);
                    Address symAddr = baseAddress.addOffsetTo(symOffset);
                    sym = Symbol.create(symAddr);
                    if (sym.equals(name)) {
                        return sym;
                    }
                }
                bucket = bucket.addOffsetTo(2 * uintSize);
            }
        }
        return null;
    }
}
