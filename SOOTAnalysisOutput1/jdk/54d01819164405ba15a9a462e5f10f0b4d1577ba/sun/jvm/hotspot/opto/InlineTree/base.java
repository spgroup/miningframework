package sun.jvm.hotspot.opto;

import java.util.*;
import java.io.PrintStream;
import sun.jvm.hotspot.ci.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.utilities.GrowableArray;
import sun.jvm.hotspot.types.*;

public class InlineTree extends VMObject {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("InlineTree");
        callerJvmsField = type.getAddressField("_caller_jvms");
        methodField = type.getAddressField("_method");
        callerTreeField = type.getAddressField("_caller_tree");
        subtreesField = type.getAddressField("_subtrees");
    }

    private static AddressField callerJvmsField;

    private static AddressField methodField;

    private static AddressField callerTreeField;

    private static AddressField subtreesField;

    private static StaticBaseConstructor inlineTreeConstructor = new StaticBaseConstructor<InlineTree>(InlineTree.class);

    public InlineTree(Address addr) {
        super(addr);
    }

    public InlineTree callerTree() {
        Address addr = callerTreeField.getValue(getAddress());
        if (addr == null)
            return null;
        return new InlineTree(addr);
    }

    public ciMethod method() {
        return (ciMethod) ciObjectFactory.getMetadata(methodField.getValue(getAddress()));
    }

    public JVMState callerJvms() {
        return JVMState.create(callerJvmsField.getValue(getAddress()));
    }

    public int callerBci() {
        JVMState jvms = callerJvms();
        return (jvms != null) ? jvms.bci() : -1;
    }

    public GrowableArray<InlineTree> subtrees() {
        Address addr = getAddress().addOffsetTo(subtreesField.getOffset());
        return GrowableArray.create(addr, inlineTreeConstructor);
    }

    public int inlineLevel() {
        JVMState jvms = callerJvms();
        return (jvms != null) ? jvms.depth() : 0;
    }

    public void printImpl(PrintStream st, int indent) {
        for (int i = 0; i < indent; i++) st.print(" ");
        st.printf(" @ %d ", callerBci());
        method().printShortName(st);
        st.println();
        GrowableArray<InlineTree> subt = subtrees();
        for (int i = 0; i < subt.length(); i++) {
            subt.at(i).printImpl(st, indent + 2);
        }
    }

    public void print(PrintStream st) {
        printImpl(st, 2);
    }

    public int count() {
        int result = 1;
        GrowableArray<InlineTree> subt = subtrees();
        for (int i = 0; i < subt.length(); i++) {
            result += subt.at(i).count();
        }
        return result;
    }

    public void dumpReplayData(PrintStream out) {
        out.printf(" %d %d ", inlineLevel(), callerBci());
        Method method = (Method) method().getMetadata();
        Klass holder = method.getMethodHolder();
        out.print(holder.getName().asString() + " " + OopUtilities.escapeString(method.getName().asString()) + " " + method.getSignature().asString());
        GrowableArray<InlineTree> subt = subtrees();
        for (int i = 0; i < subt.length(); i++) {
            subt.at(i).dumpReplayData(out);
        }
    }
}
