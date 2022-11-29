package sun.jvm.hotspot.runtime;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;

public class JNIid extends VMObject {

    private static MetadataField holder;

    private static AddressField next;

    private static CIntegerField offset;

    private static MetadataField resolvedMethod;

    private static MetadataField resolvedReceiver;

    private ObjectHeap heap;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
    }

    public JNIid(Address addr, ObjectHeap heap) {
        super(addr);
        this.heap = heap;
    }

    public JNIid next() {
        Address nextAddr = next.getValue(addr);
        if (nextAddr == null) {
            return null;
        }
        return new JNIid(nextAddr, heap);
    }

    public Klass holder() {
        return (Klass) holder.getValue(addr);
    }

    public int offset() {
        return (int) offset.getValue(addr);
    }

    public Method method() {
        return ((InstanceKlass) holder()).getMethods().at(offset());
    }

    public Method resolvedMethod() {
        return (Method) resolvedMethod.getValue(addr);
    }

    public Klass resolvedReceiver() {
        return (Klass) resolvedReceiver.getValue(addr);
    }
}
