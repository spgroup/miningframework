package sun.jvm.hotspot.memory;

import java.util.*;
import sun.jvm.hotspot.classfile.ClassLoaderData;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.utilities.*;

public class DictionaryEntry extends sun.jvm.hotspot.utilities.HashtableEntry {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("DictionaryEntry");
        pdSetField = type.getAddressField("_pd_set");
        loaderDataField = type.getAddressField("_loader_data");
    }

    private static AddressField pdSetField;

    private static AddressField loaderDataField;

    public ProtectionDomainEntry pdSet() {
        Address tmp = pdSetField.getValue(addr);
        return (ProtectionDomainEntry) VMObjectFactory.newObject(ProtectionDomainEntry.class, tmp);
    }

    public Oop loader() {
        return loaderData().getClassLoader();
    }

    public ClassLoaderData loaderData() {
        return ClassLoaderData.instantiateWrapperFor(loaderDataField.getValue(addr));
    }

    public Klass klass() {
        return (Klass) Metadata.instantiateWrapperFor(literalValue());
    }

    public DictionaryEntry(Address addr) {
        super(addr);
    }

    public boolean equals(Symbol className, Oop classLoader) {
        InstanceKlass ik = (InstanceKlass) klass();
        Oop loader = loader();
        if (!ik.getName().equals(className)) {
            return false;
        } else {
            return (loader == null) ? (classLoader == null) : (loader.equals(classLoader));
        }
    }

    public boolean isValidProtectionDomain(Oop protectionDomain) {
        if (protectionDomain == null) {
            return true;
        } else {
            return containsProtectionDomain(protectionDomain);
        }
    }

    public boolean containsProtectionDomain(Oop protectionDomain) {
        InstanceKlass ik = (InstanceKlass) klass();
        for (ProtectionDomainEntry current = pdSet(); current != null; current = current.next()) {
            if (protectionDomain.equals(current.protectionDomain())) {
                return true;
            }
        }
        return false;
    }
}
