package sun.jvm.hotspot.oops;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.memory.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.TypeDataBase;
import sun.jvm.hotspot.utilities.*;
import sun.jvm.hotspot.jdi.JVMTIThreadState;

public class OopUtilities implements JVMTIThreadState {

    private static ByteField coderField;

    private static OopField valueField;

    private static OopField threadGroupParentField;

    private static OopField threadGroupNameField;

    private static IntField threadGroupNThreadsField;

    private static OopField threadGroupThreadsField;

    private static IntField threadGroupNGroupsField;

    private static OopField threadGroupGroupsField;

    private static OopField threadNameField;

    private static OopField threadGroupField;

    private static LongField threadEETopField;

    private static LongField threadTIDField;

    private static IntField threadStatusField;

    private static OopField threadParkBlockerField;

    private static int THREAD_STATUS_NEW;

    private static OopField absOwnSyncOwnerThreadField;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) {
    }

    public static String charArrayToString(TypeArray charArray) {
        if (charArray == null) {
            return null;
        }
        int length = (int) charArray.getLength();
        StringBuffer buf = new StringBuffer(length);
        for (int i = 0; i < length; i++) {
            buf.append(charArray.getCharAt(i));
        }
        return buf.toString();
    }

    public static String byteArrayToString(TypeArray byteArray, byte coder) {
        if (byteArray == null) {
            return null;
        }
        int length = (int) byteArray.getLength() >> coder;
        StringBuffer buf = new StringBuffer(length);
        if (coder == 0) {
            for (int i = 0; i < length; i++) {
                buf.append((char) (byteArray.getByteAt(i) & 0xff));
            }
        } else {
            for (int i = 0; i < length; i++) {
                buf.append(byteArray.getCharAt(i));
            }
        }
        return buf.toString();
    }

    public static String escapeString(String s) {
        StringBuilder sb = null;
        for (int index = 0; index < s.length(); index++) {
            char value = s.charAt(index);
            if (value >= 32 && value < 127 || value == '\'' || value == '\\') {
                if (sb != null) {
                    sb.append(value);
                }
            } else {
                if (sb == null) {
                    sb = new StringBuilder(s.length() * 2);
                    sb.append(s, 0, index);
                }
                sb.append("\\u");
                if (value < 0x10)
                    sb.append("000");
                else if (value < 0x100)
                    sb.append("00");
                else if (value < 0x1000)
                    sb.append("0");
                sb.append(Integer.toHexString(value));
            }
        }
        if (sb != null) {
            return sb.toString();
        }
        return s;
    }

    public static String stringOopToString(Oop stringOop) {
        InstanceKlass k = (InstanceKlass) stringOop.getKlass();
        coderField = (ByteField) k.findField("coder", "B");
        valueField = (OopField) k.findField("value", "[B");
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(coderField != null, "Field \'coder\' of java.lang.String not found");
            Assert.that(valueField != null, "Field \'value\' of java.lang.String not found");
        }
        return byteArrayToString((TypeArray) valueField.getValue(stringOop), coderField.getValue(stringOop));
    }

    public static String stringOopToEscapedString(Oop stringOop) {
        return escapeString(stringOopToString(stringOop));
    }

    private static void initThreadGroupFields() {
        if (threadGroupParentField == null) {
            SystemDictionary sysDict = VM.getVM().getSystemDictionary();
            InstanceKlass k = sysDict.getThreadGroupKlass();
            threadGroupParentField = (OopField) k.findField("parent", "Ljava/lang/ThreadGroup;");
            threadGroupNameField = (OopField) k.findField("name", "Ljava/lang/String;");
            threadGroupNThreadsField = (IntField) k.findField("nthreads", "I");
            threadGroupThreadsField = (OopField) k.findField("threads", "[Ljava/lang/Thread;");
            threadGroupNGroupsField = (IntField) k.findField("ngroups", "I");
            threadGroupGroupsField = (OopField) k.findField("groups", "[Ljava/lang/ThreadGroup;");
            if (Assert.ASSERTS_ENABLED) {
                Assert.that(threadGroupParentField != null && threadGroupNameField != null && threadGroupNThreadsField != null && threadGroupThreadsField != null && threadGroupNGroupsField != null && threadGroupGroupsField != null, "must find all java.lang.ThreadGroup fields");
            }
        }
    }

    public static Oop threadGroupOopGetParent(Oop threadGroupOop) {
        initThreadGroupFields();
        return threadGroupParentField.getValue(threadGroupOop);
    }

    public static String threadGroupOopGetName(Oop threadGroupOop) {
        initThreadGroupFields();
        return stringOopToString(threadGroupNameField.getValue(threadGroupOop));
    }

    public static Oop[] threadGroupOopGetThreads(Oop threadGroupOop) {
        initThreadGroupFields();
        int nthreads = threadGroupNThreadsField.getValue(threadGroupOop);
        Oop[] result = new Oop[nthreads];
        ObjArray threads = (ObjArray) threadGroupThreadsField.getValue(threadGroupOop);
        for (int i = 0; i < nthreads; i++) {
            result[i] = threads.getObjAt(i);
        }
        return result;
    }

    public static Oop[] threadGroupOopGetGroups(Oop threadGroupOop) {
        initThreadGroupFields();
        int ngroups = threadGroupNGroupsField.getValue(threadGroupOop);
        Oop[] result = new Oop[ngroups];
        ObjArray groups = (ObjArray) threadGroupGroupsField.getValue(threadGroupOop);
        for (int i = 0; i < ngroups; i++) {
            result[i] = groups.getObjAt(i);
        }
        return result;
    }

    private static void initThreadFields() {
        if (threadNameField == null) {
            SystemDictionary sysDict = VM.getVM().getSystemDictionary();
            InstanceKlass k = sysDict.getThreadKlass();
            threadNameField = (OopField) k.findField("name", "Ljava/lang/String;");
            threadGroupField = (OopField) k.findField("group", "Ljava/lang/ThreadGroup;");
            threadEETopField = (LongField) k.findField("eetop", "J");
            threadTIDField = (LongField) k.findField("tid", "J");
            threadStatusField = (IntField) k.findField("threadStatus", "I");
            threadParkBlockerField = (OopField) k.findField("parkBlocker", "Ljava/lang/Object;");
            TypeDataBase db = VM.getVM().getTypeDataBase();
            THREAD_STATUS_NEW = db.lookupIntConstant("java_lang_Thread::NEW").intValue();
            if (Assert.ASSERTS_ENABLED) {
                Assert.that(threadNameField != null && threadGroupField != null && threadEETopField != null, "must find all java.lang.Thread fields");
            }
        }
    }

    public static Oop threadOopGetThreadGroup(Oop threadOop) {
        initThreadFields();
        return threadGroupField.getValue(threadOop);
    }

    public static String threadOopGetName(Oop threadOop) {
        initThreadFields();
        return stringOopToString(threadNameField.getValue(threadOop));
    }

    public static JavaThread threadOopGetJavaThread(Oop threadOop) {
        initThreadFields();
        Address addr = threadOop.getHandle().getAddressAt(threadEETopField.getOffset());
        if (addr == null) {
            return null;
        }
        return VM.getVM().getThreads().createJavaThreadWrapper(addr);
    }

    public static long threadOopGetTID(Oop threadOop) {
        initThreadFields();
        if (threadTIDField != null) {
            return threadTIDField.getValue(threadOop);
        } else {
            return 0;
        }
    }

    public static int threadOopGetThreadStatus(Oop threadOop) {
        initThreadFields();
        if (threadStatusField != null) {
            return (int) threadStatusField.getValue(threadOop);
        } else {
            JavaThread thr = threadOopGetJavaThread(threadOop);
            if (thr == null) {
                return THREAD_STATUS_NEW;
            } else {
                return JVMTI_THREAD_STATE_ALIVE;
            }
        }
    }

    public static Oop threadOopGetParkBlocker(Oop threadOop) {
        initThreadFields();
        if (threadParkBlockerField != null) {
            return threadParkBlockerField.getValue(threadOop);
        }
        return null;
    }

    private static void initAbsOwnSyncFields() {
        if (absOwnSyncOwnerThreadField == null) {
            SystemDictionary sysDict = VM.getVM().getSystemDictionary();
            InstanceKlass k = sysDict.getAbstractOwnableSynchronizerKlass();
            absOwnSyncOwnerThreadField = (OopField) k.findField("exclusiveOwnerThread", "Ljava/lang/Thread;");
        }
    }

    public static Oop abstractOwnableSynchronizerGetOwnerThread(Oop oop) {
        initAbsOwnSyncFields();
        if (absOwnSyncOwnerThreadField == null) {
            return null;
        } else {
            return absOwnSyncOwnerThreadField.getValue(oop);
        }
    }
}
