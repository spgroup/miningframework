package sun.jvm.hotspot.code;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.*;

public class StubQueue extends VMObject {

    private static AddressField stubBufferField;

    private static CIntegerField bufferLimitField;

    private static CIntegerField queueBeginField;

    private static CIntegerField queueEndField;

    private static CIntegerField numberOfStubsField;

    private Class stubType;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("StubQueue");
        stubBufferField = type.getAddressField("_stub_buffer");
        bufferLimitField = type.getCIntegerField("_buffer_limit");
        queueBeginField = type.getCIntegerField("_queue_begin");
        queueEndField = type.getCIntegerField("_queue_end");
        numberOfStubsField = type.getCIntegerField("_number_of_stubs");
    }

    public StubQueue(Address addr, Class stubType) {
        super(addr);
        this.stubType = stubType;
    }

    public boolean contains(Address pc) {
        if (pc == null)
            return false;
        long offset = pc.minus(getStubBuffer());
        return ((0 <= offset) && (offset < getBufferLimit()));
    }

    public Stub getStubContaining(Address pc) {
        if (contains(pc)) {
            int i = 0;
            for (Stub s = getFirst(); s != null; s = getNext(s)) {
                if (stubContains(s, pc)) {
                    return s;
                }
            }
        }
        return null;
    }

    public boolean stubContains(Stub s, Address pc) {
        return (s.codeBegin().lessThanOrEqual(pc) && s.codeEnd().greaterThan(pc));
    }

    public int getNumberOfStubs() {
        return (int) numberOfStubsField.getValue(addr);
    }

    public Stub getFirst() {
        return ((getNumberOfStubs() > 0) ? getStubAt(getQueueBegin()) : null);
    }

    public Stub getNext(Stub s) {
        long i = getIndexOf(s) + getStubSize(s);
        if (i == getBufferLimit()) {
            i = 0;
        }
        return ((i == getQueueEnd()) ? null : getStubAt(i));
    }

    public Stub getPrev(Stub s) {
        if (getIndexOf(s) == getQueueBegin()) {
            return null;
        }
        Stub temp = getFirst();
        Stub prev = null;
        while (temp != null && getIndexOf(temp) != getIndexOf(s)) {
            prev = temp;
            temp = getNext(temp);
        }
        return prev;
    }

    private long getQueueBegin() {
        return queueBeginField.getValue(addr);
    }

    private long getQueueEnd() {
        return queueEndField.getValue(addr);
    }

    private long getBufferLimit() {
        return bufferLimitField.getValue(addr);
    }

    private Address getStubBuffer() {
        return stubBufferField.getValue(addr);
    }

    private Stub getStubAt(long offset) {
        checkIndex(offset);
        return (Stub) VMObjectFactory.newObject(stubType, getStubBuffer().addOffsetTo(offset));
    }

    private long getIndexOf(Stub s) {
        long i = s.getAddress().minus(getStubBuffer());
        checkIndex(i);
        return i;
    }

    private long getStubSize(Stub s) {
        return s.getSize();
    }

    private void checkIndex(long i) {
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(0 <= i && i < getBufferLimit() && (i % VM.getVM().getAddressSize() == 0), "illegal index");
        }
    }
}
