package com.sun.org.apache.xml.internal.security.c14n.implementations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

public class NameSpaceSymbTable {

    SymbMap symb;

    int nameSpaces = 0;

    List<SymbMap> level;

    boolean cloned = true;

    static final String XMLNS = "xmlns";

    final static SymbMap initialMap = new SymbMap();

    static {
        NameSpaceSymbEntry ne = new NameSpaceSymbEntry("", null, true, XMLNS);
        ne.lastrendered = "";
        initialMap.put(XMLNS, ne);
    }

    public NameSpaceSymbTable() {
        level = new ArrayList<SymbMap>(10);
        symb = (SymbMap) initialMap.clone();
    }

    public void getUnrenderedNodes(Collection<Attr> result) {
        Iterator<NameSpaceSymbEntry> it = symb.entrySet().iterator();
        while (it.hasNext()) {
            NameSpaceSymbEntry n = it.next();
            if ((!n.rendered) && (n.n != null)) {
                n = (NameSpaceSymbEntry) n.clone();
                needsClone();
                symb.put(n.prefix, n);
                n.lastrendered = n.uri;
                n.rendered = true;
                result.add(n.n);
            }
        }
    }

    public void outputNodePush() {
        nameSpaces++;
        push();
    }

    public void outputNodePop() {
        nameSpaces--;
        pop();
    }

    public void push() {
        level.add(null);
        cloned = false;
    }

    public void pop() {
        int size = level.size() - 1;
        Object ob = level.remove(size);
        if (ob != null) {
            symb = (SymbMap) ob;
            if (size == 0) {
                cloned = false;
            } else
                cloned = (level.get(size - 1) != symb);
        } else {
            cloned = false;
        }
    }

    final void needsClone() {
        if (!cloned) {
            level.set(level.size() - 1, symb);
            symb = (SymbMap) symb.clone();
            cloned = true;
        }
    }

    public Attr getMapping(String prefix) {
        NameSpaceSymbEntry entry = symb.get(prefix);
        if (entry == null) {
            return null;
        }
        if (entry.rendered) {
            return null;
        }
        entry = (NameSpaceSymbEntry) entry.clone();
        needsClone();
        symb.put(prefix, entry);
        entry.rendered = true;
        entry.level = nameSpaces;
        entry.lastrendered = entry.uri;
        return entry.n;
    }

    public Attr getMappingWithoutRendered(String prefix) {
        NameSpaceSymbEntry entry = symb.get(prefix);
        if (entry == null) {
            return null;
        }
        if (entry.rendered) {
            return null;
        }
        return entry.n;
    }

    public boolean addMapping(String prefix, String uri, Attr n) {
        NameSpaceSymbEntry ob = symb.get(prefix);
        if ((ob != null) && uri.equals(ob.uri)) {
            return false;
        }
        NameSpaceSymbEntry ne = new NameSpaceSymbEntry(uri, n, false, prefix);
        needsClone();
        symb.put(prefix, ne);
        if (ob != null) {
            ne.lastrendered = ob.lastrendered;
            if ((ob.lastrendered != null) && (ob.lastrendered.equals(uri))) {
                ne.rendered = true;
            }
        }
        return true;
    }

    public Node addMappingAndRender(String prefix, String uri, Attr n) {
        NameSpaceSymbEntry ob = symb.get(prefix);
        if ((ob != null) && uri.equals(ob.uri)) {
            if (!ob.rendered) {
                ob = (NameSpaceSymbEntry) ob.clone();
                needsClone();
                symb.put(prefix, ob);
                ob.lastrendered = uri;
                ob.rendered = true;
                return ob.n;
            }
            return null;
        }
        NameSpaceSymbEntry ne = new NameSpaceSymbEntry(uri, n, true, prefix);
        ne.lastrendered = uri;
        needsClone();
        symb.put(prefix, ne);
        if (ob != null) {
            if ((ob.lastrendered != null) && (ob.lastrendered.equals(uri))) {
                ne.rendered = true;
                return null;
            }
        }
        return ne.n;
    }

    public int getLevel() {
        return level.size();
    }

    public void removeMapping(String prefix) {
        NameSpaceSymbEntry ob = symb.get(prefix);
        if (ob != null) {
            needsClone();
            symb.put(prefix, null);
        }
    }

    public void removeMappingIfNotRender(String prefix) {
        NameSpaceSymbEntry ob = symb.get(prefix);
        if (ob != null && !ob.rendered) {
            needsClone();
            symb.put(prefix, null);
        }
    }

    public boolean removeMappingIfRender(String prefix) {
        NameSpaceSymbEntry ob = symb.get(prefix);
        if (ob != null && ob.rendered) {
            needsClone();
            symb.put(prefix, null);
        }
        return false;
    }
}

class NameSpaceSymbEntry implements Cloneable {

    NameSpaceSymbEntry(String name, Attr n, boolean rendered, String prefix) {
        this.uri = name;
        this.rendered = rendered;
        this.n = n;
        this.prefix = prefix;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    int level = 0;

    String prefix;

    String uri;

    String lastrendered = null;

    boolean rendered = false;

    Attr n;
}

class SymbMap implements Cloneable {

    int free = 23;

    NameSpaceSymbEntry[] entries;

    String[] keys;

    SymbMap() {
        entries = new NameSpaceSymbEntry[free];
        keys = new String[free];
    }

    void put(String key, NameSpaceSymbEntry value) {
        int index = index(key);
        Object oldKey = keys[index];
        keys[index] = key;
        entries[index] = value;
        if (oldKey == null || !oldKey.equals(key)) {
            if (--free == 0) {
                free = entries.length;
                int newCapacity = free << 2;
                rehash(newCapacity);
            }
        }
    }

    List<NameSpaceSymbEntry> entrySet() {
        List<NameSpaceSymbEntry> a = new ArrayList<NameSpaceSymbEntry>();
        for (int i = 0; i < entries.length; i++) {
            if ((entries[i] != null) && !("".equals(entries[i].uri))) {
                a.add(entries[i]);
            }
        }
        return a;
    }

    protected int index(Object obj) {
        Object[] set = keys;
        int length = set.length;
        int index = (obj.hashCode() & 0x7fffffff) % length;
        Object cur = set[index];
        if (cur == null || (cur.equals(obj))) {
            return index;
        }
        length = length - 1;
        do {
            index = index == length ? 0 : ++index;
            cur = set[index];
        } while (cur != null && (!cur.equals(obj)));
        return index;
    }

    protected void rehash(int newCapacity) {
        int oldCapacity = keys.length;
        String[] oldKeys = keys;
        NameSpaceSymbEntry[] oldVals = entries;
        keys = new String[newCapacity];
        entries = new NameSpaceSymbEntry[newCapacity];
        for (int i = oldCapacity; i-- > 0; ) {
            if (oldKeys[i] != null) {
                String o = oldKeys[i];
                int index = index(o);
                keys[index] = o;
                entries[index] = oldVals[i];
            }
        }
    }

    NameSpaceSymbEntry get(String key) {
        return entries[index(key)];
    }

    protected Object clone() {
        try {
            SymbMap copy = (SymbMap) super.clone();
            copy.entries = new NameSpaceSymbEntry[entries.length];
            System.arraycopy(entries, 0, copy.entries, 0, entries.length);
            copy.keys = new String[keys.length];
            System.arraycopy(keys, 0, copy.keys, 0, keys.length);
            return copy;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
