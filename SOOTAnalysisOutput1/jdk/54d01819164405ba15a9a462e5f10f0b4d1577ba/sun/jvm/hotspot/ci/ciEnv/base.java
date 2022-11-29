package sun.jvm.hotspot.ci;

import java.io.PrintStream;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.opto.*;
import sun.jvm.hotspot.compiler.CompileTask;
import sun.jvm.hotspot.prims.JvmtiExport;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.GrowableArray;

public class ciEnv extends VMObject {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("ciEnv");
        dependenciesField = type.getAddressField("_dependencies");
        factoryField = type.getAddressField("_factory");
        compilerDataField = type.getAddressField("_compiler_data");
        taskField = type.getAddressField("_task");
        systemDictionaryModificationCounterField = new CIntField(type.getCIntegerField("_system_dictionary_modification_counter"), 0);
    }

    private static AddressField dependenciesField;

    private static AddressField factoryField;

    private static AddressField compilerDataField;

    private static AddressField taskField;

    private static CIntField systemDictionaryModificationCounterField;

    public ciEnv(Address addr) {
        super(addr);
    }

    public Compile compilerData() {
        Address addr = compilerDataField.getValue(this.getAddress());
        if (addr == null) {
            return null;
        }
        return new Compile(addr);
    }

    public ciObjectFactory factory() {
        return new ciObjectFactory(factoryField.getValue(this.getAddress()));
    }

    public CompileTask task() {
        return new CompileTask(taskField.getValue(this.getAddress()));
    }

    public void dumpReplayData(PrintStream out) {
        out.println("JvmtiExport can_access_local_variables " + (JvmtiExport.canAccessLocalVariables() ? '1' : '0'));
        out.println("JvmtiExport can_hotswap_or_post_breakpoint " + (JvmtiExport.canHotswapOrPostBreakpoint() ? '1' : '0'));
        out.println("JvmtiExport can_post_on_exceptions " + (JvmtiExport.canPostOnExceptions() ? '1' : '0'));
        GrowableArray<ciMetadata> objects = factory().objects();
        out.println("# " + objects.length() + " ciObject found");
        for (int i = 0; i < objects.length(); i++) {
            ciMetadata o = objects.at(i);
            out.println("# ciMetadata" + i + " @ " + o);
            o.dumpReplayData(out);
        }
        CompileTask task = task();
        Method method = task.method();
        int entryBci = task.osrBci();
        int compLevel = task.compLevel();
        out.print("compile " + method.nameAsAscii() + " " + entryBci + " " + compLevel);
        Compile compiler = compilerData();
        if (compiler != null) {
            compiler.dumpInlineData(out);
        }
        out.println();
    }
}
