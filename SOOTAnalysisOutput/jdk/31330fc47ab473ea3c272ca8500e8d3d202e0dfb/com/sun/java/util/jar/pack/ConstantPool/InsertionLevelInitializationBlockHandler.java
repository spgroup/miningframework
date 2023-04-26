package com.sun.java.util.jar.pack;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import static com.sun.java.util.jar.pack.Constants.*;

abstract class ConstantPool {

    private ConstantPool() {
    }

    static int verbose() {
        return Utils.currentPropMap().getInteger(Utils.DEBUG_VERBOSE);
    }

    public static synchronized Utf8Entry getUtf8Entry(String value) {
        Map<String, Utf8Entry> utf8Entries = Utils.getTLGlobals().getUtf8Entries();
        Utf8Entry e = utf8Entries.get(value);
        if (e == null) {
            e = new Utf8Entry(value);
            utf8Entries.put(e.stringValue(), e);
        }
        return e;
    }

    public static ClassEntry getClassEntry(String name) {
        Map<String, ClassEntry> classEntries = Utils.getTLGlobals().getClassEntries();
        ClassEntry e = classEntries.get(name);
        if (e == null) {
            e = new ClassEntry(getUtf8Entry(name));
            assert (name.equals(e.stringValue()));
            classEntries.put(e.stringValue(), e);
        }
        return e;
    }

    public static LiteralEntry getLiteralEntry(Comparable<?> value) {
        Map<Object, LiteralEntry> literalEntries = Utils.getTLGlobals().getLiteralEntries();
        LiteralEntry e = literalEntries.get(value);
        if (e == null) {
            if (value instanceof String)
                e = new StringEntry(getUtf8Entry((String) value));
            else
                e = new NumberEntry((Number) value);
            literalEntries.put(value, e);
        }
        return e;
    }

    public static StringEntry getStringEntry(String value) {
        return (StringEntry) getLiteralEntry(value);
    }

    public static SignatureEntry getSignatureEntry(String type) {
        Map<String, SignatureEntry> signatureEntries = Utils.getTLGlobals().getSignatureEntries();
        SignatureEntry e = signatureEntries.get(type);
        if (e == null) {
            e = new SignatureEntry(type);
            assert (e.stringValue().equals(type));
            signatureEntries.put(type, e);
        }
        return e;
    }

    public static SignatureEntry getSignatureEntry(Utf8Entry formRef, ClassEntry[] classRefs) {
        return getSignatureEntry(SignatureEntry.stringValueOf(formRef, classRefs));
    }

    public static DescriptorEntry getDescriptorEntry(Utf8Entry nameRef, SignatureEntry typeRef) {
        Map<String, DescriptorEntry> descriptorEntries = Utils.getTLGlobals().getDescriptorEntries();
        String key = DescriptorEntry.stringValueOf(nameRef, typeRef);
        DescriptorEntry e = descriptorEntries.get(key);
        if (e == null) {
            e = new DescriptorEntry(nameRef, typeRef);
            assert (e.stringValue().equals(key)) : (e.stringValue() + " != " + (key));
            descriptorEntries.put(key, e);
        }
        return e;
    }

    public static DescriptorEntry getDescriptorEntry(Utf8Entry nameRef, Utf8Entry typeRef) {
        return getDescriptorEntry(nameRef, getSignatureEntry(typeRef.stringValue()));
    }

    public static MemberEntry getMemberEntry(byte tag, ClassEntry classRef, DescriptorEntry descRef) {
        Map<String, MemberEntry> memberEntries = Utils.getTLGlobals().getMemberEntries();
        String key = MemberEntry.stringValueOf(tag, classRef, descRef);
        MemberEntry e = memberEntries.get(key);
        if (e == null) {
            e = new MemberEntry(tag, classRef, descRef);
            assert (e.stringValue().equals(key)) : (e.stringValue() + " != " + (key));
            memberEntries.put(key, e);
        }
        return e;
    }

    public static MethodHandleEntry getMethodHandleEntry(byte refKind, MemberEntry memRef) {
        Map<String, MethodHandleEntry> methodHandleEntries = Utils.getTLGlobals().getMethodHandleEntries();
        String key = MethodHandleEntry.stringValueOf(refKind, memRef);
        MethodHandleEntry e = methodHandleEntries.get(key);
        if (e == null) {
            e = new MethodHandleEntry(refKind, memRef);
            assert (e.stringValue().equals(key));
            methodHandleEntries.put(key, e);
        }
        return e;
    }

    public static MethodTypeEntry getMethodTypeEntry(SignatureEntry sigRef) {
        Map<String, MethodTypeEntry> methodTypeEntries = Utils.getTLGlobals().getMethodTypeEntries();
        String key = sigRef.stringValue();
        MethodTypeEntry e = methodTypeEntries.get(key);
        if (e == null) {
            e = new MethodTypeEntry(sigRef);
            assert (e.stringValue().equals(key));
            methodTypeEntries.put(key, e);
        }
        return e;
    }

    public static MethodTypeEntry getMethodTypeEntry(Utf8Entry typeRef) {
        return getMethodTypeEntry(getSignatureEntry(typeRef.stringValue()));
    }

    public static InvokeDynamicEntry getInvokeDynamicEntry(BootstrapMethodEntry bssRef, DescriptorEntry descRef) {
        Map<String, InvokeDynamicEntry> invokeDynamicEntries = Utils.getTLGlobals().getInvokeDynamicEntries();
        String key = InvokeDynamicEntry.stringValueOf(bssRef, descRef);
        InvokeDynamicEntry e = invokeDynamicEntries.get(key);
        if (e == null) {
            e = new InvokeDynamicEntry(bssRef, descRef);
            assert (e.stringValue().equals(key));
            invokeDynamicEntries.put(key, e);
        }
        return e;
    }

    public static BootstrapMethodEntry getBootstrapMethodEntry(MethodHandleEntry bsmRef, Entry[] argRefs) {
        Map<String, BootstrapMethodEntry> bootstrapMethodEntries = Utils.getTLGlobals().getBootstrapMethodEntries();
        String key = BootstrapMethodEntry.stringValueOf(bsmRef, argRefs);
        BootstrapMethodEntry e = bootstrapMethodEntries.get(key);
        if (e == null) {
            e = new BootstrapMethodEntry(bsmRef, argRefs);
            assert (e.stringValue().equals(key));
            bootstrapMethodEntries.put(key, e);
        }
        return e;
    }

    public static abstract class Entry implements Comparable<Object> {

        protected final byte tag;

        protected int valueHash;

        protected Entry(byte tag) {
            this.tag = tag;
        }

        public final byte getTag() {
            return tag;
        }

        public final boolean tagEquals(int tag) {
            return getTag() == tag;
        }

        public Entry getRef(int i) {
            return null;
        }

        public boolean eq(Entry that) {
            assert (that != null);
            return this == that || this.equals(that);
        }

        public abstract boolean equals(Object o);

        public final int hashCode() {
            if (valueHash == 0) {
                valueHash = computeValueHash();
                if (valueHash == 0)
                    valueHash = 1;
            }
            return valueHash;
        }

        protected abstract int computeValueHash();

        public abstract int compareTo(Object o);

        protected int superCompareTo(Object o) {
            Entry that = (Entry) o;
            if (this.tag != that.tag) {
                return TAG_ORDER[this.tag] - TAG_ORDER[that.tag];
            }
            return 0;
        }

        public final boolean isDoubleWord() {
            return tag == CONSTANT_Double || tag == CONSTANT_Long;
        }

        public final boolean tagMatches(int matchTag) {
            if (tag == matchTag)
                return true;
            byte[] allowedTags;
            switch(matchTag) {
                case CONSTANT_All:
                    return true;
                case CONSTANT_Signature:
                    return tag == CONSTANT_Utf8;
                case CONSTANT_LoadableValue:
                    allowedTags = LOADABLE_VALUE_TAGS;
                    break;
                case CONSTANT_AnyMember:
                    allowedTags = ANY_MEMBER_TAGS;
                    break;
                case CONSTANT_FieldSpecific:
                    allowedTags = FIELD_SPECIFIC_TAGS;
                    break;
                default:
                    return false;
            }
            for (byte b : allowedTags) {
                if (b == tag)
                    return true;
            }
            return false;
        }

        public String toString() {
            String valuePrint = stringValue();
            if (verbose() > 4) {
                if (valueHash != 0)
                    valuePrint += " hash=" + valueHash;
                valuePrint += " id=" + System.identityHashCode(this);
            }
            return tagName(tag) + "=" + valuePrint;
        }

        public abstract String stringValue();
    }

    public static class Utf8Entry extends Entry {

        final String value;

        Utf8Entry(String value) {
            super(CONSTANT_Utf8);
            this.value = value.intern();
            hashCode();
        }

        protected int computeValueHash() {
            return value.hashCode();
        }

        public boolean equals(Object o) {
            return (o != null && o.getClass() == Utf8Entry.class && ((Utf8Entry) o).value.equals(value));
        }

        public int compareTo(Object o) {
            int x = superCompareTo(o);
            if (x == 0) {
                x = value.compareTo(((Utf8Entry) o).value);
            }
            return x;
        }

        public String stringValue() {
            return value;
        }
    }

    static boolean isMemberTag(byte tag) {
        switch(tag) {
            case CONSTANT_Fieldref:
            case CONSTANT_Methodref:
            case CONSTANT_InterfaceMethodref:
                return true;
        }
        return false;
    }

    static byte numberTagOf(Number value) {
        if (value instanceof Integer)
            return CONSTANT_Integer;
        if (value instanceof Float)
            return CONSTANT_Float;
        if (value instanceof Long)
            return CONSTANT_Long;
        if (value instanceof Double)
            return CONSTANT_Double;
        throw new RuntimeException("bad literal value " + value);
    }

    static boolean isRefKind(byte refKind) {
        return (REF_getField <= refKind && refKind <= REF_invokeInterface);
    }

    public static abstract class LiteralEntry extends Entry {

        protected LiteralEntry(byte tag) {
            super(tag);
        }

        public abstract Comparable<?> literalValue();
    }

    public static class NumberEntry extends LiteralEntry {

        final Number value;

        NumberEntry(Number value) {
            super(numberTagOf(value));
            this.value = value;
            hashCode();
        }

        protected int computeValueHash() {
            return value.hashCode();
        }

        public boolean equals(Object o) {
            return (o != null && o.getClass() == NumberEntry.class && ((NumberEntry) o).value.equals(value));
        }

        public int compareTo(Object o) {
            int x = superCompareTo(o);
            if (x == 0) {
                @SuppressWarnings("unchecked")
                Comparable<Number> compValue = (Comparable<Number>) value;
                x = compValue.compareTo(((NumberEntry) o).value);
            }
            return x;
        }

        public Number numberValue() {
            return value;
        }

        public Comparable<?> literalValue() {
            return (Comparable<?>) value;
        }

        public String stringValue() {
            return value.toString();
        }
    }

    public static class StringEntry extends LiteralEntry {

        final Utf8Entry ref;

        public Entry getRef(int i) {
            return i == 0 ? ref : null;
        }

        StringEntry(Entry ref) {
            super(CONSTANT_String);
            this.ref = (Utf8Entry) ref;
            hashCode();
        }

        protected int computeValueHash() {
            return ref.hashCode() + tag;
        }

        public boolean equals(Object o) {
            return (o != null && o.getClass() == StringEntry.class && ((StringEntry) o).ref.eq(ref));
        }

        public int compareTo(Object o) {
            int x = superCompareTo(o);
            if (x == 0) {
                x = ref.compareTo(((StringEntry) o).ref);
            }
            return x;
        }

        public Comparable<?> literalValue() {
            return ref.stringValue();
        }

        public String stringValue() {
            return ref.stringValue();
        }
    }

    public static class ClassEntry extends Entry {

        final Utf8Entry ref;

        public Entry getRef(int i) {
            return i == 0 ? ref : null;
        }

        protected int computeValueHash() {
            return ref.hashCode() + tag;
        }

        ClassEntry(Entry ref) {
            super(CONSTANT_Class);
            this.ref = (Utf8Entry) ref;
            hashCode();
        }

        public boolean equals(Object o) {
            return (o != null && o.getClass() == ClassEntry.class && ((ClassEntry) o).ref.eq(ref));
        }

        public int compareTo(Object o) {
            int x = superCompareTo(o);
            if (x == 0) {
                x = ref.compareTo(((ClassEntry) o).ref);
            }
            return x;
        }

        public String stringValue() {
            return ref.stringValue();
        }
    }

    public static class DescriptorEntry extends Entry {

        final Utf8Entry nameRef;

        final SignatureEntry typeRef;

        public Entry getRef(int i) {
            if (i == 0)
                return nameRef;
            if (i == 1)
                return typeRef;
            return null;
        }

        DescriptorEntry(Entry nameRef, Entry typeRef) {
            super(CONSTANT_NameandType);
            if (typeRef instanceof Utf8Entry) {
                typeRef = getSignatureEntry(typeRef.stringValue());
            }
            this.nameRef = (Utf8Entry) nameRef;
            this.typeRef = (SignatureEntry) typeRef;
            hashCode();
        }

        protected int computeValueHash() {
            int hc2 = typeRef.hashCode();
            return (nameRef.hashCode() + (hc2 << 8)) ^ hc2;
        }

        public boolean equals(Object o) {
            if (o == null || o.getClass() != DescriptorEntry.class) {
                return false;
            }
            DescriptorEntry that = (DescriptorEntry) o;
            return this.nameRef.eq(that.nameRef) && this.typeRef.eq(that.typeRef);
        }

        public int compareTo(Object o) {
            int x = superCompareTo(o);
            if (x == 0) {
                DescriptorEntry that = (DescriptorEntry) o;
                x = this.typeRef.compareTo(that.typeRef);
                if (x == 0)
                    x = this.nameRef.compareTo(that.nameRef);
            }
            return x;
        }

        public String stringValue() {
            return stringValueOf(nameRef, typeRef);
        }

        static String stringValueOf(Entry nameRef, Entry typeRef) {
            return qualifiedStringValue(typeRef, nameRef);
        }

        public String prettyString() {
            return nameRef.stringValue() + typeRef.prettyString();
        }

        public boolean isMethod() {
            return typeRef.isMethod();
        }

        public byte getLiteralTag() {
            return typeRef.getLiteralTag();
        }
    }

    static String qualifiedStringValue(Entry e1, Entry e2) {
        return qualifiedStringValue(e1.stringValue(), e2.stringValue());
    }

    static String qualifiedStringValue(String s1, String s234) {
        assert (s1.indexOf(".") < 0);
        return s1 + "." + s234;
    }

    public static class MemberEntry extends Entry {

        final ClassEntry classRef;

        final DescriptorEntry descRef;

        public Entry getRef(int i) {
            if (i == 0)
                return classRef;
            if (i == 1)
                return descRef;
            return null;
        }

        protected int computeValueHash() {
            int hc2 = descRef.hashCode();
            return (classRef.hashCode() + (hc2 << 8)) ^ hc2;
        }

        MemberEntry(byte tag, ClassEntry classRef, DescriptorEntry descRef) {
            super(tag);
            assert (isMemberTag(tag));
            this.classRef = classRef;
            this.descRef = descRef;
            hashCode();
        }

        public boolean equals(Object o) {
            if (o == null || o.getClass() != MemberEntry.class) {
                return false;
            }
            MemberEntry that = (MemberEntry) o;
            return this.classRef.eq(that.classRef) && this.descRef.eq(that.descRef);
        }

        public int compareTo(Object o) {
            int x = superCompareTo(o);
            if (x == 0) {
                MemberEntry that = (MemberEntry) o;
                if (Utils.SORT_MEMBERS_DESCR_MAJOR)
                    x = this.descRef.compareTo(that.descRef);
                if (x == 0)
                    x = this.classRef.compareTo(that.classRef);
                if (x == 0)
                    x = this.descRef.compareTo(that.descRef);
            }
            return x;
        }

        public String stringValue() {
            return stringValueOf(tag, classRef, descRef);
        }

        static String stringValueOf(byte tag, ClassEntry classRef, DescriptorEntry descRef) {
            assert (isMemberTag(tag));
            String pfx;
            switch(tag) {
                case CONSTANT_Fieldref:
                    pfx = "Field:";
                    break;
                case CONSTANT_Methodref:
                    pfx = "Method:";
                    break;
                case CONSTANT_InterfaceMethodref:
                    pfx = "IMethod:";
                    break;
                default:
                    pfx = tag + "???";
                    break;
            }
            return pfx + qualifiedStringValue(classRef, descRef);
        }

        public boolean isMethod() {
            return descRef.isMethod();
        }
    }

    public static class SignatureEntry extends Entry {

        final Utf8Entry formRef;

        final ClassEntry[] classRefs;

        String value;

        Utf8Entry asUtf8Entry;

        public Entry getRef(int i) {
            if (i == 0)
                return formRef;
            return i - 1 < classRefs.length ? classRefs[i - 1] : null;
        }

        SignatureEntry(String value) {
            super(CONSTANT_Signature);
            value = value.intern();
            this.value = value;
            String[] parts = structureSignature(value);
            formRef = getUtf8Entry(parts[0]);
            classRefs = new ClassEntry[parts.length - 1];
            for (int i = 1; i < parts.length; i++) {
                classRefs[i - 1] = getClassEntry(parts[i]);
            }
            hashCode();
        }

        protected int computeValueHash() {
            stringValue();
            return value.hashCode() + tag;
        }

        public Utf8Entry asUtf8Entry() {
            if (asUtf8Entry == null) {
                asUtf8Entry = getUtf8Entry(stringValue());
            }
            return asUtf8Entry;
        }

        public boolean equals(Object o) {
            return (o != null && o.getClass() == SignatureEntry.class && ((SignatureEntry) o).value.equals(value));
        }

        public int compareTo(Object o) {
            int x = superCompareTo(o);
            if (x == 0) {
                SignatureEntry that = (SignatureEntry) o;
                x = compareSignatures(this.value, that.value);
            }
            return x;
        }

        public String stringValue() {
            if (value == null) {
                value = stringValueOf(formRef, classRefs);
            }
            return value;
        }

        static String stringValueOf(Utf8Entry formRef, ClassEntry[] classRefs) {
            String[] parts = new String[1 + classRefs.length];
            parts[0] = formRef.stringValue();
            for (int i = 1; i < parts.length; i++) {
                parts[i] = classRefs[i - 1].stringValue();
            }
            return flattenSignature(parts).intern();
        }

        public int computeSize(boolean countDoublesTwice) {
            String form = formRef.stringValue();
            int min = 0;
            int max = 1;
            if (isMethod()) {
                min = 1;
                max = form.indexOf(')');
            }
            int size = 0;
            for (int i = min; i < max; i++) {
                switch(form.charAt(i)) {
                    case 'D':
                    case 'J':
                        if (countDoublesTwice) {
                            size++;
                        }
                        break;
                    case '[':
                        while (form.charAt(i) == '[') {
                            ++i;
                        }
                        break;
                    case ';':
                        continue;
                    default:
                        assert (0 <= JAVA_SIGNATURE_CHARS.indexOf(form.charAt(i)));
                        break;
                }
                size++;
            }
            return size;
        }

        public boolean isMethod() {
            return formRef.stringValue().charAt(0) == '(';
        }

        public byte getLiteralTag() {
            switch(formRef.stringValue().charAt(0)) {
                case 'I':
                    return CONSTANT_Integer;
                case 'J':
                    return CONSTANT_Long;
                case 'F':
                    return CONSTANT_Float;
                case 'D':
                    return CONSTANT_Double;
                case 'B':
                case 'S':
                case 'C':
                case 'Z':
                    return CONSTANT_Integer;
                case 'L':
                    return CONSTANT_String;
            }
            assert (false);
            return CONSTANT_None;
        }

        public String prettyString() {
            String s;
            if (isMethod()) {
                s = formRef.stringValue();
                s = s.substring(0, 1 + s.indexOf(')'));
            } else {
                s = "/" + formRef.stringValue();
            }
            int i;
            while ((i = s.indexOf(';')) >= 0) {
                s = s.substring(0, i) + s.substring(i + 1);
            }
            return s;
        }
    }

    static int compareSignatures(String s1, String s2) {
        return compareSignatures(s1, s2, null, null);
    }

    static int compareSignatures(String s1, String s2, String[] p1, String[] p2) {
        final int S1_COMES_FIRST = -1;
        final int S2_COMES_FIRST = +1;
        char c1 = s1.charAt(0);
        char c2 = s2.charAt(0);
        if (c1 != '(' && c2 == '(')
            return S1_COMES_FIRST;
        if (c2 != '(' && c1 == '(')
            return S2_COMES_FIRST;
        if (p1 == null)
            p1 = structureSignature(s1);
        if (p2 == null)
            p2 = structureSignature(s2);
        if (p1.length != p2.length)
            return p1.length - p2.length;
        int length = p1.length;
        for (int i = length; --i >= 0; ) {
            int res = p1[i].compareTo(p2[i]);
            if (res != 0)
                return res;
        }
        assert (s1.equals(s2));
        return 0;
    }

    static int countClassParts(Utf8Entry formRef) {
        int num = 0;
        String s = formRef.stringValue();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == 'L')
                ++num;
        }
        return num;
    }

    static String flattenSignature(String[] parts) {
        String form = parts[0];
        if (parts.length == 1)
            return form;
        int len = form.length();
        for (int i = 1; i < parts.length; i++) {
            len += parts[i].length();
        }
        char[] sig = new char[len];
        int j = 0;
        int k = 1;
        for (int i = 0; i < form.length(); i++) {
            char ch = form.charAt(i);
            sig[j++] = ch;
            if (ch == 'L') {
                String cls = parts[k++];
                cls.getChars(0, cls.length(), sig, j);
                j += cls.length();
            }
        }
        assert (j == len);
        assert (k == parts.length);
        return new String(sig);
    }

    static private int skipClassNameChars(String sig, int i) {
        int len = sig.length();
        for (; i < len; i++) {
            char ch = sig.charAt(i);
            if (ch <= ' ')
                break;
            if (ch >= ';' && ch <= '@')
                break;
        }
        return i;
    }

    static String[] structureSignature(String sig) {
        sig = sig.intern();
        int formLen = 0;
        int nparts = 1;
        for (int i = 0; i < sig.length(); i++) {
            char ch = sig.charAt(i);
            formLen++;
            if (ch == 'L') {
                nparts++;
                int i2 = skipClassNameChars(sig, i + 1);
                i = i2 - 1;
                int i3 = sig.indexOf('<', i + 1);
                if (i3 > 0 && i3 < i2)
                    i = i3 - 1;
            }
        }
        char[] form = new char[formLen];
        if (nparts == 1) {
            String[] parts = { sig };
            return parts;
        }
        String[] parts = new String[nparts];
        int j = 0;
        int k = 1;
        for (int i = 0; i < sig.length(); i++) {
            char ch = sig.charAt(i);
            form[j++] = ch;
            if (ch == 'L') {
                int i2 = skipClassNameChars(sig, i + 1);
                parts[k++] = sig.substring(i + 1, i2);
                i = i2;
                --i;
            }
        }
        assert (j == formLen);
        assert (k == parts.length);
        parts[0] = new String(form);
        return parts;
    }

    public static class MethodHandleEntry extends Entry {

        final int refKind;

        final MemberEntry memRef;

        public Entry getRef(int i) {
            return i == 0 ? memRef : null;
        }

        protected int computeValueHash() {
            int hc2 = refKind;
            return (memRef.hashCode() + (hc2 << 8)) ^ hc2;
        }

        MethodHandleEntry(byte refKind, MemberEntry memRef) {
            super(CONSTANT_MethodHandle);
            assert (isRefKind(refKind));
            this.refKind = refKind;
            this.memRef = memRef;
            hashCode();
        }

        public boolean equals(Object o) {
            if (o == null || o.getClass() != MethodHandleEntry.class) {
                return false;
            }
            MethodHandleEntry that = (MethodHandleEntry) o;
            return this.refKind == that.refKind && this.memRef.eq(that.memRef);
        }

        public int compareTo(Object o) {
            int x = superCompareTo(o);
            if (x == 0) {
                MethodHandleEntry that = (MethodHandleEntry) o;
                if (Utils.SORT_HANDLES_KIND_MAJOR)
                    x = this.refKind - that.refKind;
                if (x == 0)
                    x = this.memRef.compareTo(that.memRef);
                if (x == 0)
                    x = this.refKind - that.refKind;
            }
            return x;
        }

        public static String stringValueOf(int refKind, MemberEntry memRef) {
            return refKindName(refKind) + ":" + memRef.stringValue();
        }

        public String stringValue() {
            return stringValueOf(refKind, memRef);
        }
    }

    public static class MethodTypeEntry extends Entry {

        final SignatureEntry typeRef;

        public Entry getRef(int i) {
            return i == 0 ? typeRef : null;
        }

        protected int computeValueHash() {
            return typeRef.hashCode() + tag;
        }

        MethodTypeEntry(SignatureEntry typeRef) {
            super(CONSTANT_MethodType);
            this.typeRef = typeRef;
            hashCode();
        }

        public boolean equals(Object o) {
            if (o == null || o.getClass() != MethodTypeEntry.class) {
                return false;
            }
            MethodTypeEntry that = (MethodTypeEntry) o;
            return this.typeRef.eq(that.typeRef);
        }

        public int compareTo(Object o) {
            int x = superCompareTo(o);
            if (x == 0) {
                MethodTypeEntry that = (MethodTypeEntry) o;
                x = this.typeRef.compareTo(that.typeRef);
            }
            return x;
        }

        public String stringValue() {
            return typeRef.stringValue();
        }
    }

    public static class InvokeDynamicEntry extends Entry {

        final BootstrapMethodEntry bssRef;

        final DescriptorEntry descRef;

        public Entry getRef(int i) {
            if (i == 0)
                return bssRef;
            if (i == 1)
                return descRef;
            return null;
        }

        protected int computeValueHash() {
            int hc2 = descRef.hashCode();
            return (bssRef.hashCode() + (hc2 << 8)) ^ hc2;
        }

        InvokeDynamicEntry(BootstrapMethodEntry bssRef, DescriptorEntry descRef) {
            super(CONSTANT_InvokeDynamic);
            this.bssRef = bssRef;
            this.descRef = descRef;
            hashCode();
        }

        public boolean equals(Object o) {
            if (o == null || o.getClass() != InvokeDynamicEntry.class) {
                return false;
            }
            InvokeDynamicEntry that = (InvokeDynamicEntry) o;
            return this.bssRef.eq(that.bssRef) && this.descRef.eq(that.descRef);
        }

        public int compareTo(Object o) {
            int x = superCompareTo(o);
            if (x == 0) {
                InvokeDynamicEntry that = (InvokeDynamicEntry) o;
                if (Utils.SORT_INDY_BSS_MAJOR)
                    x = this.bssRef.compareTo(that.bssRef);
                if (x == 0)
                    x = this.descRef.compareTo(that.descRef);
                if (x == 0)
                    x = this.bssRef.compareTo(that.bssRef);
            }
            return x;
        }

        public String stringValue() {
            return stringValueOf(bssRef, descRef);
        }

        static String stringValueOf(BootstrapMethodEntry bssRef, DescriptorEntry descRef) {
            return "Indy:" + bssRef.stringValue() + "." + descRef.stringValue();
        }
    }

    public static class BootstrapMethodEntry extends Entry {

        final MethodHandleEntry bsmRef;

        final Entry[] argRefs;

        public Entry getRef(int i) {
            if (i == 0)
                return bsmRef;
            if (i - 1 < argRefs.length)
                return argRefs[i - 1];
            return null;
        }

        protected int computeValueHash() {
            int hc2 = bsmRef.hashCode();
            return (Arrays.hashCode(argRefs) + (hc2 << 8)) ^ hc2;
        }

        BootstrapMethodEntry(MethodHandleEntry bsmRef, Entry[] argRefs) {
            super(CONSTANT_BootstrapMethod);
            this.bsmRef = bsmRef;
            this.argRefs = argRefs.clone();
            hashCode();
        }

        public boolean equals(Object o) {
            if (o == null || o.getClass() != BootstrapMethodEntry.class) {
                return false;
            }
            BootstrapMethodEntry that = (BootstrapMethodEntry) o;
            return this.bsmRef.eq(that.bsmRef) && Arrays.equals(this.argRefs, that.argRefs);
        }

        public int compareTo(Object o) {
            int x = superCompareTo(o);
            if (x == 0) {
                BootstrapMethodEntry that = (BootstrapMethodEntry) o;
                if (Utils.SORT_BSS_BSM_MAJOR)
                    x = this.bsmRef.compareTo(that.bsmRef);
                if (x == 0)
                    x = compareArgArrays(this.argRefs, that.argRefs);
                if (x == 0)
                    x = this.bsmRef.compareTo(that.bsmRef);
            }
            return x;
        }

        public String stringValue() {
            return stringValueOf(bsmRef, argRefs);
        }

        static String stringValueOf(MethodHandleEntry bsmRef, Entry[] argRefs) {
            StringBuffer sb = new StringBuffer(bsmRef.stringValue());
            char nextSep = '<';
            boolean didOne = false;
            for (Entry argRef : argRefs) {
                sb.append(nextSep).append(argRef.stringValue());
                nextSep = ';';
            }
            if (nextSep == '<')
                sb.append(nextSep);
            sb.append('>');
            return sb.toString();
        }

        static int compareArgArrays(Entry[] a1, Entry[] a2) {
            int x = a1.length - a2.length;
            if (x != 0)
                return x;
            for (int i = 0; i < a1.length; i++) {
                x = a1[i].compareTo(a2[i]);
                if (x != 0)
                    break;
            }
            return x;
        }
    }

    protected static final Entry[] noRefs = {};

    protected static final ClassEntry[] noClassRefs = {};

    public static final class Index extends AbstractList<Entry> {

        protected String debugName;

        protected Entry[] cpMap;

        protected boolean flattenSigs;

        protected Entry[] getMap() {
            return cpMap;
        }

        protected Index(String debugName) {
            this.debugName = debugName;
        }

        protected Index(String debugName, Entry[] cpMap) {
            this(debugName);
            setMap(cpMap);
        }

        protected void setMap(Entry[] cpMap) {
            clearIndex();
            this.cpMap = cpMap;
        }

        protected Index(String debugName, Collection<Entry> cpMapList) {
            this(debugName);
            setMap(cpMapList);
        }

        protected void setMap(Collection<Entry> cpMapList) {
            cpMap = new Entry[cpMapList.size()];
            cpMapList.toArray(cpMap);
            setMap(cpMap);
        }

        public int size() {
            return cpMap.length;
        }

        public Entry get(int i) {
            return cpMap[i];
        }

        public Entry getEntry(int i) {
            return cpMap[i];
        }

        private int findIndexOf(Entry e) {
            if (indexKey == null) {
                initializeIndex();
            }
            int probe = findIndexLocation(e);
            if (indexKey[probe] != e) {
                if (flattenSigs && e.tag == CONSTANT_Signature) {
                    SignatureEntry se = (SignatureEntry) e;
                    return findIndexOf(se.asUtf8Entry());
                }
                return -1;
            }
            int index = indexValue[probe];
            assert (e.equals(cpMap[index]));
            return index;
        }

        public boolean contains(Entry e) {
            return findIndexOf(e) >= 0;
        }

        public int indexOf(Entry e) {
            int index = findIndexOf(e);
            if (index < 0 && verbose() > 0) {
                System.out.println("not found: " + e);
                System.out.println("       in: " + this.dumpString());
                Thread.dumpStack();
            }
            assert (index >= 0);
            return index;
        }

        public int lastIndexOf(Entry e) {
            return indexOf(e);
        }

        public boolean assertIsSorted() {
            for (int i = 1; i < cpMap.length; i++) {
                if (cpMap[i - 1].compareTo(cpMap[i]) > 0) {
                    System.out.println("Not sorted at " + (i - 1) + "/" + i + ": " + this.dumpString());
                    return false;
                }
            }
            return true;
        }

        protected Entry[] indexKey;

        protected int[] indexValue;

        protected void clearIndex() {
            indexKey = null;
            indexValue = null;
        }

        private int findIndexLocation(Entry e) {
            int size = indexKey.length;
            int hash = e.hashCode();
            int probe = hash & (size - 1);
            int stride = ((hash >>> 8) | 1) & (size - 1);
            for (; ; ) {
                Entry e1 = indexKey[probe];
                if (e1 == e || e1 == null)
                    return probe;
                probe += stride;
                if (probe >= size)
                    probe -= size;
            }
        }

        private void initializeIndex() {
            if (verbose() > 2)
                System.out.println("initialize Index " + debugName + " [" + size() + "]");
            int hsize0 = (int) ((cpMap.length + 10) * 1.5);
            int hsize = 1;
            while (hsize < hsize0) {
                hsize <<= 1;
            }
            indexKey = new Entry[hsize];
            indexValue = new int[hsize];
            for (int i = 0; i < cpMap.length; i++) {
                Entry e = cpMap[i];
                if (e == null)
                    continue;
                int probe = findIndexLocation(e);
                assert (indexKey[probe] == null);
                indexKey[probe] = e;
                indexValue[probe] = i;
            }
        }

        public Entry[] toArray(Entry[] a) {
            int sz = size();
            if (a.length < sz)
                return super.toArray(a);
            System.arraycopy(cpMap, 0, a, 0, sz);
            if (a.length > sz)
                a[sz] = null;
            return a;
        }

        public Entry[] toArray() {
            return toArray(new Entry[size()]);
        }

        public Object clone() {
            return new Index(debugName, cpMap.clone());
        }

        public String toString() {
            return "Index " + debugName + " [" + size() + "]";
        }

        public String dumpString() {
            String s = toString();
            s += " {\n";
            for (int i = 0; i < cpMap.length; i++) {
                s += "    " + i + ": " + cpMap[i] + "\n";
            }
            s += "}";
            return s;
        }
    }

    public static Index makeIndex(String debugName, Entry[] cpMap) {
        return new Index(debugName, cpMap);
    }

    public static Index makeIndex(String debugName, Collection<Entry> cpMapList) {
        return new Index(debugName, cpMapList);
    }

    public static void sort(Index ix) {
        ix.clearIndex();
        Arrays.sort(ix.cpMap);
        if (verbose() > 2)
            System.out.println("sorted " + ix.dumpString());
    }

    public static Index[] partition(Index ix, int[] keys) {
        List<List<Entry>> parts = new ArrayList<>();
        Entry[] cpMap = ix.cpMap;
        assert (keys.length == cpMap.length);
        for (int i = 0; i < keys.length; i++) {
            int key = keys[i];
            if (key < 0)
                continue;
            while (key >= parts.size()) {
                parts.add(null);
            }
            List<Entry> part = parts.get(key);
            if (part == null) {
                parts.set(key, part = new ArrayList<>());
            }
            part.add(cpMap[i]);
        }
        Index[] indexes = new Index[parts.size()];
        for (int key = 0; key < indexes.length; key++) {
            List<Entry> part = parts.get(key);
            if (part == null)
                continue;
            indexes[key] = new Index(ix.debugName + "/part#" + key, part);
            assert (indexes[key].indexOf(part.get(0)) == 0);
        }
        return indexes;
    }

    public static Index[] partitionByTag(Index ix) {
        Entry[] cpMap = ix.cpMap;
        int[] keys = new int[cpMap.length];
        for (int i = 0; i < keys.length; i++) {
            Entry e = cpMap[i];
            keys[i] = (e == null) ? -1 : e.tag;
        }
        Index[] byTag = partition(ix, keys);
        for (int tag = 0; tag < byTag.length; tag++) {
            if (byTag[tag] == null)
                continue;
            byTag[tag].debugName = tagName(tag);
        }
        if (byTag.length < CONSTANT_Limit) {
            Index[] longer = new Index[CONSTANT_Limit];
            System.arraycopy(byTag, 0, longer, 0, byTag.length);
            byTag = longer;
        }
        return byTag;
    }

    public static class IndexGroup {

        private Index[] indexByTag = new Index[CONSTANT_Limit];

        private Index[] indexByTagGroup;

        private int[] untypedFirstIndexByTag;

        private int totalSizeQQ;

        private Index[][] indexByTagAndClass;

        private Index makeTagGroupIndex(byte tagGroupTag, byte[] tagsInGroup) {
            if (indexByTagGroup == null)
                indexByTagGroup = new Index[CONSTANT_GroupLimit - CONSTANT_GroupFirst];
            int which = tagGroupTag - CONSTANT_GroupFirst;
            assert (indexByTagGroup[which] == null);
            int fillp = 0;
            Entry[] cpMap = null;
            for (int pass = 1; pass <= 2; pass++) {
                untypedIndexOf(null);
                for (byte tag : tagsInGroup) {
                    Index ix = indexByTag[tag];
                    if (ix == null)
                        continue;
                    int ixLen = ix.cpMap.length;
                    if (ixLen == 0)
                        continue;
                    assert (tagGroupTag == CONSTANT_All ? fillp == untypedFirstIndexByTag[tag] : fillp < untypedFirstIndexByTag[tag]);
                    if (cpMap != null) {
                        assert (cpMap[fillp] == null);
                        assert (cpMap[fillp + ixLen - 1] == null);
                        System.arraycopy(ix.cpMap, 0, cpMap, fillp, ixLen);
                    }
                    fillp += ixLen;
                }
                if (cpMap == null) {
                    assert (pass == 1);
                    cpMap = new Entry[fillp];
                    fillp = 0;
                }
            }
            indexByTagGroup[which] = new Index(tagName(tagGroupTag), cpMap);
            return indexByTagGroup[which];
        }

        public int untypedIndexOf(Entry e) {
            if (untypedFirstIndexByTag == null) {
                untypedFirstIndexByTag = new int[CONSTANT_Limit + 1];
                int fillp = 0;
                for (int i = 0; i < TAGS_IN_ORDER.length; i++) {
                    byte tag = TAGS_IN_ORDER[i];
                    Index ix = indexByTag[tag];
                    if (ix == null)
                        continue;
                    int ixLen = ix.cpMap.length;
                    untypedFirstIndexByTag[tag] = fillp;
                    fillp += ixLen;
                }
                untypedFirstIndexByTag[CONSTANT_Limit] = fillp;
            }
            if (e == null)
                return -1;
            int tag = e.tag;
            Index ix = indexByTag[tag];
            if (ix == null)
                return -1;
            int idx = ix.findIndexOf(e);
            if (idx >= 0)
                idx += untypedFirstIndexByTag[tag];
            return idx;
        }

        public void initIndexByTag(byte tag, Index ix) {
            assert (indexByTag[tag] == null);
            Entry[] cpMap = ix.cpMap;
            for (int i = 0; i < cpMap.length; i++) {
                assert (cpMap[i].tag == tag);
            }
            if (tag == CONSTANT_Utf8) {
                assert (cpMap.length == 0 || cpMap[0].stringValue().equals(""));
            }
            indexByTag[tag] = ix;
            untypedFirstIndexByTag = null;
            indexByTagGroup = null;
            if (indexByTagAndClass != null)
                indexByTagAndClass[tag] = null;
        }

        public Index getIndexByTag(byte tag) {
            if (tag >= CONSTANT_GroupFirst)
                return getIndexByTagGroup(tag);
            Index ix = indexByTag[tag];
            if (ix == null) {
                ix = new Index(tagName(tag), new Entry[0]);
                indexByTag[tag] = ix;
            }
            return ix;
        }

        private Index getIndexByTagGroup(byte tag) {
            if (indexByTagGroup != null) {
                Index ix = indexByTagGroup[tag - CONSTANT_GroupFirst];
                if (ix != null)
                    return ix;
            }
            switch(tag) {
                case CONSTANT_All:
                    return makeTagGroupIndex(CONSTANT_All, TAGS_IN_ORDER);
                case CONSTANT_LoadableValue:
                    return makeTagGroupIndex(CONSTANT_LoadableValue, LOADABLE_VALUE_TAGS);
                case CONSTANT_AnyMember:
                    return makeTagGroupIndex(CONSTANT_AnyMember, ANY_MEMBER_TAGS);
                case CONSTANT_FieldSpecific:
                    return null;
            }
            throw new AssertionError("bad tag group " + tag);
        }

        public Index getMemberIndex(byte tag, ClassEntry classRef) {
            if (classRef == null)
                throw new RuntimeException("missing class reference for " + tagName(tag));
            if (indexByTagAndClass == null)
                indexByTagAndClass = new Index[CONSTANT_Limit][];
            Index allClasses = getIndexByTag(CONSTANT_Class);
            Index[] perClassIndexes = indexByTagAndClass[tag];
            if (perClassIndexes == null) {
                Index allMembers = getIndexByTag(tag);
                int[] whichClasses = new int[allMembers.size()];
                for (int i = 0; i < whichClasses.length; i++) {
                    MemberEntry e = (MemberEntry) allMembers.get(i);
                    int whichClass = allClasses.indexOf(e.classRef);
                    whichClasses[i] = whichClass;
                }
                perClassIndexes = partition(allMembers, whichClasses);
                for (int i = 0; i < perClassIndexes.length; i++) {
                    assert (perClassIndexes[i] == null || perClassIndexes[i].assertIsSorted());
                }
                indexByTagAndClass[tag] = perClassIndexes;
            }
            int whichClass = allClasses.indexOf(classRef);
            return perClassIndexes[whichClass];
        }

        public int getOverloadingIndex(MemberEntry methodRef) {
            Index ix = getMemberIndex(methodRef.tag, methodRef.classRef);
            Utf8Entry nameRef = methodRef.descRef.nameRef;
            int ord = 0;
            for (int i = 0; i < ix.cpMap.length; i++) {
                MemberEntry e = (MemberEntry) ix.cpMap[i];
                if (e.equals(methodRef))
                    return ord;
                if (e.descRef.nameRef.equals(nameRef))
                    ord++;
            }
            throw new RuntimeException("should not reach here");
        }

        public MemberEntry getOverloadingForIndex(byte tag, ClassEntry classRef, String name, int which) {
            assert (name.equals(name.intern()));
            Index ix = getMemberIndex(tag, classRef);
            int ord = 0;
            for (int i = 0; i < ix.cpMap.length; i++) {
                MemberEntry e = (MemberEntry) ix.cpMap[i];
                if (e.descRef.nameRef.stringValue().equals(name)) {
                    if (ord == which)
                        return e;
                    ord++;
                }
            }
            throw new RuntimeException("should not reach here");
        }

        public boolean haveNumbers() {
            for (byte tag : NUMBER_TAGS) {
                if (getIndexByTag(tag).size() > 0)
                    return true;
            }
            return false;
        }

        public boolean haveExtraTags() {
            for (byte tag : EXTRA_TAGS) {
                if (getIndexByTag(tag).size() > 0)
                    return true;
            }
            return false;
        }
    }

    public static void completeReferencesIn(Set<Entry> cpRefs, boolean flattenSigs) {
        completeReferencesIn(cpRefs, flattenSigs, null);
    }

    public static void completeReferencesIn(Set<Entry> cpRefs, boolean flattenSigs, List<BootstrapMethodEntry> bsms) {
        cpRefs.remove(null);
        for (ListIterator<Entry> work = new ArrayList<>(cpRefs).listIterator(cpRefs.size()); work.hasPrevious(); ) {
            Entry e = work.previous();
            work.remove();
            assert (e != null);
            if (flattenSigs && e.tag == CONSTANT_Signature) {
                SignatureEntry se = (SignatureEntry) e;
                Utf8Entry ue = se.asUtf8Entry();
                cpRefs.remove(se);
                cpRefs.add(ue);
                e = ue;
            }
            if (bsms != null && e.tag == CONSTANT_BootstrapMethod) {
                BootstrapMethodEntry bsm = (BootstrapMethodEntry) e;
                cpRefs.remove(bsm);
                if (!bsms.contains(bsm))
                    bsms.add(bsm);
            }
            for (int i = 0; ; i++) {
                Entry re = e.getRef(i);
                if (re == null)
                    break;
                if (cpRefs.add(re))
                    work.add(re);
            }
        }
    }

    static double percent(int num, int den) {
        return (int) ((10000.0 * num) / den + 0.5) / 100.0;
    }

    public static String tagName(int tag) {
        switch(tag) {
            case CONSTANT_Utf8:
                return "Utf8";
            case CONSTANT_Integer:
                return "Integer";
            case CONSTANT_Float:
                return "Float";
            case CONSTANT_Long:
                return "Long";
            case CONSTANT_Double:
                return "Double";
            case CONSTANT_Class:
                return "Class";
            case CONSTANT_String:
                return "String";
            case CONSTANT_Fieldref:
                return "Fieldref";
            case CONSTANT_Methodref:
                return "Methodref";
            case CONSTANT_InterfaceMethodref:
                return "InterfaceMethodref";
            case CONSTANT_NameandType:
                return "NameandType";
            case CONSTANT_MethodHandle:
                return "MethodHandle";
            case CONSTANT_MethodType:
                return "MethodType";
            case CONSTANT_InvokeDynamic:
                return "InvokeDynamic";
            case CONSTANT_All:
                return "**All";
            case CONSTANT_None:
                return "**None";
            case CONSTANT_LoadableValue:
                return "**LoadableValue";
            case CONSTANT_AnyMember:
                return "**AnyMember";
            case CONSTANT_FieldSpecific:
                return "*FieldSpecific";
            case CONSTANT_Signature:
                return "*Signature";
            case CONSTANT_BootstrapMethod:
                return "*BootstrapMethod";
        }
        return "tag#" + tag;
    }

    public static String refKindName(int refKind) {
        switch(refKind) {
            case REF_getField:
                return "getField";
            case REF_getStatic:
                return "getStatic";
            case REF_putField:
                return "putField";
            case REF_putStatic:
                return "putStatic";
            case REF_invokeVirtual:
                return "invokeVirtual";
            case REF_invokeStatic:
                return "invokeStatic";
            case REF_invokeSpecial:
                return "invokeSpecial";
            case REF_newInvokeSpecial:
                return "newInvokeSpecial";
            case REF_invokeInterface:
                return "invokeInterface";
        }
        return "refKind#" + refKind;
    }

    static final byte[] TAGS_IN_ORDER = { CONSTANT_Utf8, CONSTANT_Integer, CONSTANT_Float, CONSTANT_Long, CONSTANT_Double, CONSTANT_String, CONSTANT_Class, CONSTANT_Signature, CONSTANT_NameandType, CONSTANT_Fieldref, CONSTANT_Methodref, CONSTANT_InterfaceMethodref, CONSTANT_MethodHandle, CONSTANT_MethodType, CONSTANT_BootstrapMethod, CONSTANT_InvokeDynamic };

    static final byte[] TAG_ORDER;

    static {
        TAG_ORDER = new byte[CONSTANT_Limit];
        for (int i = 0; i < TAGS_IN_ORDER.length; i++) {
            TAG_ORDER[TAGS_IN_ORDER[i]] = (byte) (i + 1);
        }
    }

    static final byte[] NUMBER_TAGS = { CONSTANT_Integer, CONSTANT_Float, CONSTANT_Long, CONSTANT_Double };

    static final byte[] EXTRA_TAGS = { CONSTANT_MethodHandle, CONSTANT_MethodType, CONSTANT_BootstrapMethod, CONSTANT_InvokeDynamic };

    static final byte[] LOADABLE_VALUE_TAGS = { CONSTANT_Integer, CONSTANT_Float, CONSTANT_Long, CONSTANT_Double, CONSTANT_String, CONSTANT_Class, CONSTANT_MethodHandle, CONSTANT_MethodType };

    static final byte[] ANY_MEMBER_TAGS = { CONSTANT_Fieldref, CONSTANT_Methodref, CONSTANT_InterfaceMethodref };

    static final byte[] FIELD_SPECIFIC_TAGS = { CONSTANT_Integer, CONSTANT_Float, CONSTANT_Long, CONSTANT_Double, CONSTANT_String };

    static {
        assert (verifyTagOrder(TAGS_IN_ORDER) && verifyTagOrder(NUMBER_TAGS) && verifyTagOrder(EXTRA_TAGS) && verifyTagOrder(LOADABLE_VALUE_TAGS) && verifyTagOrder(ANY_MEMBER_TAGS) && verifyTagOrder(FIELD_SPECIFIC_TAGS));
    }

    private static boolean verifyTagOrder(byte[] tags) {
        int prev = -1;
        for (byte tag : tags) {
            int next = TAG_ORDER[tag];
            assert (next > 0) : "tag not found: " + tag;
            assert (TAGS_IN_ORDER[next - 1] == tag) : "tag repeated: " + tag + " => " + next + " => " + TAGS_IN_ORDER[next - 1];
            assert (prev < next) : "tags not in order: " + Arrays.toString(tags) + " at " + tag;
            prev = next;
        }
        return true;
    }
}