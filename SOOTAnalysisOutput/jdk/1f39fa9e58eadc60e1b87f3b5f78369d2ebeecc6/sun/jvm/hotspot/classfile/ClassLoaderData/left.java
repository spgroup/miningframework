package sun.jvm.hotspot.classfile;

import java.io.PrintStream;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.memory.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;

public class ClassLoaderData extends VMObject {

    static {
        VM.registerVMInitializedObserver(new java.util.Observer() {

            public void update(java.util.Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("ClassLoaderData");
        classLoaderField = type.getOopField("_class_loader");
        nextField = type.getAddressField("_next");
        klassesField = new MetadataField(type.getAddressField("_klasses"), 0);
        isAnonymousField = new CIntField(type.getCIntegerField("_is_anonymous"), 0);
        dictionaryField = type.getAddressField("_dictionary");
    }

    private static sun.jvm.hotspot.types.OopField classLoaderField;

    private static AddressField nextField;

    private static MetadataField klassesField;

    private static CIntField isAnonymousField;

    private static AddressField dictionaryField;

    public ClassLoaderData(Address addr) {
        super(addr);
    }

    public Dictionary dictionary() {
        Address tmp = dictionaryField.getValue();
        return (Dictionary) VMObjectFactory.newObject(Dictionary.class, tmp);
    }

    public static ClassLoaderData instantiateWrapperFor(Address addr) {
        if (addr == null) {
            return null;
        }
        return new ClassLoaderData(addr);
    }

    public Oop getClassLoader() {
        return VM.getVM().getObjectHeap().newOop(classLoaderField.getValue(getAddress()));
    }

    public boolean getIsAnonymous() {
        return isAnonymousField.getValue(this) != 0;
    }

    public ClassLoaderData next() {
        return instantiateWrapperFor(nextField.getValue(getAddress()));
    }

    public Klass getKlasses() {
        return (Klass) klassesField.getValue(this);
    }

    public Klass find(Symbol className) {
        for (Klass l = getKlasses(); l != null; l = l.getNextLinkKlass()) {
            if (className.equals(l.getName())) {
                return l;
            }
        }
        return null;
    }

    public void classesDo(ClassLoaderDataGraph.ClassVisitor v) {
        for (Klass l = getKlasses(); l != null; l = l.getNextLinkKlass()) {
            v.visit(l);
        }
    }

    public void allEntriesDo(ClassLoaderDataGraph.ClassAndLoaderVisitor v) {
        for (Klass l = getKlasses(); l != null; l = l.getNextLinkKlass()) {
            dictionary().allEntriesDo(v, getClassLoader());
        }
    }
}
