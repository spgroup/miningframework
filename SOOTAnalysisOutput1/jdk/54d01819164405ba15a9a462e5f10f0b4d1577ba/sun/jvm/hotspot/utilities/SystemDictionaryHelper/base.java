package sun.jvm.hotspot.utilities;

import java.util.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.memory.*;
import sun.jvm.hotspot.runtime.*;

public class SystemDictionaryHelper {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize();
            }
        });
    }

    private static synchronized void initialize() {
        klasses = null;
    }

    private static InstanceKlass[] klasses;

    public static synchronized InstanceKlass[] getAllInstanceKlasses() {
        if (klasses != null) {
            return klasses;
        }
        final Vector tmp = new Vector();
        SystemDictionary dict = VM.getVM().getSystemDictionary();
        dict.classesDo(new SystemDictionary.ClassVisitor() {

            public void visit(Klass k) {
                if (k instanceof InstanceKlass) {
                    InstanceKlass ik = (InstanceKlass) k;
                    tmp.add(ik);
                }
            }
        });
        Object[] tmpArray = tmp.toArray();
        klasses = new InstanceKlass[tmpArray.length];
        System.arraycopy(tmpArray, 0, klasses, 0, tmpArray.length);
        Arrays.sort(klasses, new Comparator() {

            public int compare(Object o1, Object o2) {
                InstanceKlass k1 = (InstanceKlass) o1;
                InstanceKlass k2 = (InstanceKlass) o2;
                Symbol s1 = k1.getName();
                Symbol s2 = k2.getName();
                return s1.asString().compareTo(s2.asString());
            }
        });
        return klasses;
    }

    public static InstanceKlass[] findInstanceKlasses(String namePart) {
        namePart = namePart.replace('.', '/');
        InstanceKlass[] tmpKlasses = getAllInstanceKlasses();
        Vector tmp = new Vector();
        for (int i = 0; i < tmpKlasses.length; i++) {
            String name = tmpKlasses[i].getName().asString();
            if (name.indexOf(namePart) != -1) {
                tmp.add(tmpKlasses[i]);
            }
        }
        Object[] tmpArray = tmp.toArray();
        InstanceKlass[] searchResult = new InstanceKlass[tmpArray.length];
        System.arraycopy(tmpArray, 0, searchResult, 0, tmpArray.length);
        return searchResult;
    }

    public static InstanceKlass findInstanceKlass(String className) {
        className = className.replace('.', '/');
        SystemDictionary sysDict = VM.getVM().getSystemDictionary();
        Klass klass = sysDict.find(className, null, null);
        if (klass != null) {
            return (InstanceKlass) klass;
        }
        klass = sysDict.find(className, sysDict.javaSystemLoader(), null);
        if (klass != null) {
            return (InstanceKlass) klass;
        }
        InstanceKlass[] tmpKlasses = getAllInstanceKlasses();
        int low = 0;
        int high = tmpKlasses.length - 1;
        int mid = -1;
        while (low <= high) {
            mid = (low + high) >> 1;
            InstanceKlass midVal = tmpKlasses[mid];
            int cmp = midVal.getName().asString().compareTo(className);
            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return tmpKlasses[mid];
            }
        }
        return null;
    }
}
