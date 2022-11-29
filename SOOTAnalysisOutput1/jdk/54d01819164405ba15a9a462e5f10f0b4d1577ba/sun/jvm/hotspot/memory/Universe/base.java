package sun.jvm.hotspot.memory;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.gc.shared.*;
import sun.jvm.hotspot.gc.g1.G1CollectedHeap;
import sun.jvm.hotspot.gc.parallel.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.runtime.*;

public class Universe {

    private static AddressField collectedHeapField;

    private static VirtualConstructor heapConstructor;

    private static sun.jvm.hotspot.types.OopField mainThreadGroupField;

    private static sun.jvm.hotspot.types.OopField systemThreadGroupField;

    private static sun.jvm.hotspot.types.AddressField boolArrayKlassField;

    private static sun.jvm.hotspot.types.AddressField byteArrayKlassField;

    private static sun.jvm.hotspot.types.AddressField charArrayKlassField;

    private static sun.jvm.hotspot.types.AddressField intArrayKlassField;

    private static sun.jvm.hotspot.types.AddressField shortArrayKlassField;

    private static sun.jvm.hotspot.types.AddressField longArrayKlassField;

    private static sun.jvm.hotspot.types.AddressField singleArrayKlassField;

    private static sun.jvm.hotspot.types.AddressField doubleArrayKlassField;

    private static AddressField narrowOopBaseField;

    private static CIntegerField narrowOopShiftField;

    private static AddressField narrowKlassBaseField;

    private static CIntegerField narrowKlassShiftField;

    public enum NARROW_OOP_MODE {

        UnscaledNarrowOop, ZeroBasedNarrowOop, HeapBasedNarrowOop
    }

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("Universe");
        collectedHeapField = type.getAddressField("_collectedHeap");
        heapConstructor = new VirtualConstructor(db);
        heapConstructor.addMapping("GenCollectedHeap", GenCollectedHeap.class);
        heapConstructor.addMapping("ParallelScavengeHeap", ParallelScavengeHeap.class);
        heapConstructor.addMapping("G1CollectedHeap", G1CollectedHeap.class);
        mainThreadGroupField = type.getOopField("_main_thread_group");
        systemThreadGroupField = type.getOopField("_system_thread_group");
        boolArrayKlassField = type.getAddressField("_boolArrayKlassObj");
        byteArrayKlassField = type.getAddressField("_byteArrayKlassObj");
        charArrayKlassField = type.getAddressField("_charArrayKlassObj");
        intArrayKlassField = type.getAddressField("_intArrayKlassObj");
        shortArrayKlassField = type.getAddressField("_shortArrayKlassObj");
        longArrayKlassField = type.getAddressField("_longArrayKlassObj");
        singleArrayKlassField = type.getAddressField("_singleArrayKlassObj");
        doubleArrayKlassField = type.getAddressField("_doubleArrayKlassObj");
        narrowOopBaseField = type.getAddressField("_narrow_oop._base");
        narrowOopShiftField = type.getCIntegerField("_narrow_oop._shift");
        narrowKlassBaseField = type.getAddressField("_narrow_klass._base");
        narrowKlassShiftField = type.getCIntegerField("_narrow_klass._shift");
    }

    public Universe() {
    }

    public static String narrowOopModeToString(NARROW_OOP_MODE mode) {
        switch(mode) {
            case UnscaledNarrowOop:
                return "32-bits Oops";
            case ZeroBasedNarrowOop:
                return "zero based Compressed Oops";
            case HeapBasedNarrowOop:
                return "Compressed Oops with base";
        }
        return "";
    }

    public CollectedHeap heap() {
        return (CollectedHeap) heapConstructor.instantiateWrapperFor(collectedHeapField.getValue());
    }

    public static long getNarrowOopBase() {
        if (narrowOopBaseField.getValue() == null) {
            return 0;
        } else {
            return narrowOopBaseField.getValue().minus(null);
        }
    }

    public static int getNarrowOopShift() {
        return (int) narrowOopShiftField.getValue();
    }

    public static long getNarrowKlassBase() {
        if (narrowKlassBaseField.getValue() == null) {
            return 0;
        } else {
            return narrowKlassBaseField.getValue().minus(null);
        }
    }

    public static int getNarrowKlassShift() {
        return (int) narrowKlassShiftField.getValue();
    }

    public boolean isIn(Address p) {
        return heap().isIn(p);
    }

    public boolean isInReserved(Address p) {
        return heap().isInReserved(p);
    }

    private Oop newOop(OopHandle handle) {
        return VM.getVM().getObjectHeap().newOop(handle);
    }

    public Oop mainThreadGroup() {
        return newOop(mainThreadGroupField.getValue());
    }

    public Oop systemThreadGroup() {
        return newOop(systemThreadGroupField.getValue());
    }

    public void basicTypeClassesDo(SystemDictionary.ClassVisitor visitor) {
        visitor.visit(new TypeArrayKlass(boolArrayKlassField.getValue()));
        visitor.visit(new TypeArrayKlass(byteArrayKlassField.getValue()));
        visitor.visit(new TypeArrayKlass(charArrayKlassField.getValue()));
        visitor.visit(new TypeArrayKlass(intArrayKlassField.getValue()));
        visitor.visit(new TypeArrayKlass(shortArrayKlassField.getValue()));
        visitor.visit(new TypeArrayKlass(longArrayKlassField.getValue()));
        visitor.visit(new TypeArrayKlass(singleArrayKlassField.getValue()));
        visitor.visit(new TypeArrayKlass(doubleArrayKlassField.getValue()));
    }

    public void print() {
        printOn(System.out);
    }

    public void printOn(PrintStream tty) {
        heap().printOn(tty);
    }

    public static boolean elementTypeShouldBeAligned(BasicType type) {
        return type == BasicType.T_DOUBLE || type == BasicType.T_LONG;
    }

    public static boolean fieldTypeShouldBeAligned(BasicType type) {
        return type == BasicType.T_DOUBLE || type == BasicType.T_LONG;
    }
}
