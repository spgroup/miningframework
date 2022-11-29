package sun.jvm.hotspot.oops;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.utilities.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;

abstract public class Metadata extends VMObject {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    public Metadata(Address addr) {
        super(addr);
    }

    private static VirtualBaseConstructor<Metadata> metadataConstructor;

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        metadataConstructor = new VirtualBaseConstructor<Metadata>(db, db.lookupType("Metadata"), null, null);
        metadataConstructor.addMapping("Metadata", Metadata.class);
        metadataConstructor.addMapping("Klass", Klass.class);
        metadataConstructor.addMapping("InstanceKlass", InstanceKlass.class);
        metadataConstructor.addMapping("InstanceMirrorKlass", InstanceMirrorKlass.class);
        metadataConstructor.addMapping("InstanceRefKlass", InstanceRefKlass.class);
        metadataConstructor.addMapping("InstanceClassLoaderKlass", InstanceClassLoaderKlass.class);
        metadataConstructor.addMapping("TypeArrayKlass", TypeArrayKlass.class);
        metadataConstructor.addMapping("ObjArrayKlass", ObjArrayKlass.class);
        metadataConstructor.addMapping("Method", Method.class);
        metadataConstructor.addMapping("MethodData", MethodData.class);
        metadataConstructor.addMapping("ConstMethod", ConstMethod.class);
        metadataConstructor.addMapping("ConstantPool", ConstantPool.class);
        metadataConstructor.addMapping("ConstantPoolCache", ConstantPoolCache.class);
    }

    public static Metadata instantiateWrapperFor(Address addr) {
        return metadataConstructor.instantiateWrapperFor(addr);
    }

    public void iterate(MetadataVisitor visitor) {
        visitor.setObj(this);
        visitor.prologue();
        iterateFields(visitor);
        visitor.epilogue();
    }

    void iterateFields(MetadataVisitor visitor) {
    }

    abstract public void printValueOn(PrintStream tty);

    public void dumpReplayData(PrintStream out) {
        out.println("# Unknown Metadata");
    }
}
