package sun.jvm.hotspot.opto;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;

public class Node extends VMObject {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("Node");
        outmaxField = new CIntField(type.getCIntegerField("_outmax"), 0);
        outcntField = new CIntField(type.getCIntegerField("_outcnt"), 0);
        maxField = new CIntField(type.getCIntegerField("_max"), 0);
        cntField = new CIntField(type.getCIntegerField("_cnt"), 0);
        idxField = new CIntField(type.getCIntegerField("_idx"), 0);
        outField = type.getAddressField("_out");
        inField = type.getAddressField("_in");
        nodeType = db.lookupType("Node");
        virtualConstructor = new VirtualBaseConstructor(db, nodeType, "sun.jvm.hotspot.opto", Node.class);
    }

    private static CIntField outmaxField;

    private static CIntField outcntField;

    private static CIntField maxField;

    private static CIntField cntField;

    private static CIntField idxField;

    private static AddressField outField;

    private static AddressField inField;

    private static VirtualBaseConstructor virtualConstructor;

    private static Type nodeType;

    static HashMap nodes = new HashMap();

    static HashMap constructors = new HashMap();

    static abstract class Instantiator {

        abstract Node create(Address addr);
    }

    static public Node create(Address addr) {
        if (addr == null)
            return null;
        Node result = (Node) nodes.get(addr);
        if (result == null) {
            result = (Node) virtualConstructor.instantiateWrapperFor(addr);
            nodes.put(addr, result);
        }
        return result;
    }

    public Node(Address addr) {
        super(addr);
    }

    public int outcnt() {
        return (int) outcntField.getValue(this.getAddress());
    }

    public int req() {
        return (int) cntField.getValue(this.getAddress());
    }

    public int len() {
        return (int) maxField.getValue(this.getAddress());
    }

    public int idx() {
        return (int) idxField.getValue(this.getAddress());
    }

    private Node[] _out;

    private Node[] _in;

    public Node rawOut(int i) {
        if (_out == null) {
            int addressSize = (int) VM.getVM().getAddressSize();
            _out = new Node[outcnt()];
            Address ptr = outField.getValue(this.getAddress());
            for (int j = 0; j < outcnt(); j++) {
                _out[j] = Node.create(ptr.getAddressAt(j * addressSize));
            }
        }
        return _out[i];
    }

    public Node in(int i) {
        if (_in == null) {
            int addressSize = (int) VM.getVM().getAddressSize();
            _in = new Node[len()];
            Address ptr = inField.getValue(this.getAddress());
            for (int j = 0; j < len(); j++) {
                _in[j] = Node.create(ptr.getAddressAt(j * addressSize));
            }
        }
        return _in[i];
    }

    public ArrayList collect(int d, boolean onlyCtrl) {
        int depth = Math.abs(d);
        ArrayList nstack = new ArrayList();
        BitSet set = new BitSet();
        nstack.add(this);
        set.set(idx());
        int begin = 0;
        int end = 0;
        for (int i = 0; i < depth; i++) {
            end = nstack.size();
            for (int j = begin; j < end; j++) {
                Node tp = (Node) nstack.get(j);
                int limit = d > 0 ? tp.len() : tp.outcnt();
                for (int k = 0; k < limit; k++) {
                    Node n = d > 0 ? tp.in(k) : tp.rawOut(k);
                    if (n == null)
                        continue;
                    if (!set.get(n.idx())) {
                        nstack.add(n);
                        set.set(n.idx());
                    }
                }
            }
            begin = end;
        }
        return nstack;
    }

    protected void dumpNodes(Node s, int d, boolean onlyCtrl, PrintStream out) {
        if (s == null)
            return;
        ArrayList nstack = s.collect(d, onlyCtrl);
        int end = nstack.size();
        if (d > 0) {
            for (int j = end - 1; j >= 0; j--) {
                ((Node) nstack.get(j)).dump(out);
            }
        } else {
            for (int j = 0; j < end; j++) {
                ((Node) nstack.get(j)).dump(out);
            }
        }
    }

    public void dump(int depth, PrintStream out) {
        dumpNodes(this, depth, false, out);
    }

    public String Name() {
        Type t = VM.getVM().getTypeDataBase().findDynamicTypeForAddress(getAddress(), nodeType);
        String name = null;
        if (t != null) {
            name = t.toString();
        } else {
            Class c = getClass();
            if (c == Node.class) {
                return "UnknownNode<" + getAddress().getAddressAt(0) + ">";
            }
            name = getClass().getName();
            if (name.startsWith("sun.jvm.hotspot.opto.")) {
                name = name.substring("sun.jvm.hotspot.opto.".length());
            }
        }
        if (name.endsWith("Node")) {
            return name.substring(0, name.length() - 4);
        }
        return name;
    }

    public void dump(PrintStream out) {
        out.print(" ");
        out.print(idx());
        out.print("\t");
        out.print(Name());
        out.print("\t=== ");
        int i = 0;
        for (i = 0; i < req(); i++) {
            Node n = in(i);
            if (n != null) {
                out.print(' ');
                out.print(in(i).idx());
            } else {
                out.print("_");
            }
            out.print(" ");
        }
        if (len() != req()) {
            int prec = 0;
            for (; i < len(); i++) {
                Node n = in(i);
                if (n != null) {
                    if (prec++ == 0) {
                        out.print("| ");
                    }
                    out.print(in(i).idx());
                }
                out.print(" ");
            }
        }
        dumpOut(out);
        dumpSpec(out);
        out.println();
    }

    void dumpOut(PrintStream out) {
        out.print(" [[");
        for (int i = 0; i < outcnt(); i++) {
            Node u = rawOut(i);
            if (u == null) {
                out.print("_ ");
            } else {
                out.print(' ');
                out.print(u.idx());
                out.print(' ');
            }
        }
        out.print("]] ");
    }

    public void dumpSpec(PrintStream out) {
    }
}
