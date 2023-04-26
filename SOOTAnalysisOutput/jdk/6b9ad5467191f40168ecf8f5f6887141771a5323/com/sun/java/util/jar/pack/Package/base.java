package com.sun.java.util.jar.pack;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.zip.*;
import java.util.jar.*;
import java.io.*;
import com.sun.java.util.jar.pack.ConstantPool.*;

class Package implements Constants {

    int verbose;

    {
        PropMap pmap = Utils.currentPropMap();
        if (pmap != null)
            verbose = pmap.getInteger(Utils.DEBUG_VERBOSE);
    }

    int magic;

    int package_minver;

    int package_majver;

    int default_modtime = NO_MODTIME;

    int default_options = 0;

    short default_class_majver = -1;

    short default_class_minver = 0;

    short min_class_majver = JAVA_MIN_CLASS_MAJOR_VERSION;

    short min_class_minver = JAVA_MIN_CLASS_MINOR_VERSION;

    short max_class_majver = JAVA6_MAX_CLASS_MAJOR_VERSION;

    short max_class_minver = JAVA6_MAX_CLASS_MINOR_VERSION;

    short observed_max_class_majver = min_class_majver;

    short observed_max_class_minver = min_class_minver;

    ConstantPool.IndexGroup cp = new ConstantPool.IndexGroup();

    Package() {
        magic = JAVA_PACKAGE_MAGIC;
        package_minver = -1;
        package_majver = 0;
    }

    public void reset() {
        cp = new ConstantPool.IndexGroup();
        classes.clear();
        files.clear();
    }

    int getPackageVersion() {
        return (package_majver << 16) + (int) package_minver;
    }

    public static final Attribute.Layout attrCodeEmpty;

    public static final Attribute.Layout attrInnerClassesEmpty;

    public static final Attribute.Layout attrSourceFileSpecial;

    public static final Map attrDefs;

    static {
        HashMap ad = new HashMap(2);
        attrCodeEmpty = Attribute.define(ad, ATTR_CONTEXT_METHOD, "Code", "").layout();
        attrInnerClassesEmpty = Attribute.define(ad, ATTR_CONTEXT_CLASS, "InnerClasses", "").layout();
        attrSourceFileSpecial = Attribute.define(ad, ATTR_CONTEXT_CLASS, "SourceFile", "RUNH").layout();
        attrDefs = Collections.unmodifiableMap(ad);
    }

    int getDefaultClassVersion() {
        return (default_class_majver << 16) + (char) default_class_minver;
    }

    int getHighestClassVersion() {
        int res = 0;
        for (Iterator i = classes.iterator(); i.hasNext(); ) {
            Class cls = (Class) i.next();
            int ver = cls.getVersion();
            if (res < ver)
                res = ver;
        }
        return res;
    }

    void choosePackageVersion() {
        assert (package_majver <= 0);
        int classver = getHighestClassVersion();
        if (classver != 0 && (classver >>> 16) < JAVA6_MAX_CLASS_MAJOR_VERSION) {
            package_majver = JAVA5_PACKAGE_MAJOR_VERSION;
            package_minver = JAVA5_PACKAGE_MINOR_VERSION;
        } else {
            package_majver = JAVA6_PACKAGE_MAJOR_VERSION;
            package_minver = JAVA6_PACKAGE_MINOR_VERSION;
        }
    }

    void checkVersion() throws IOException {
        if (magic != JAVA_PACKAGE_MAGIC) {
            String gotMag = Integer.toHexString(magic);
            String expMag = Integer.toHexString(JAVA_PACKAGE_MAGIC);
            throw new IOException("Unexpected package magic number: got " + gotMag + "; expected " + expMag);
        }
        if ((package_majver != JAVA6_PACKAGE_MAJOR_VERSION && package_majver != JAVA5_PACKAGE_MAJOR_VERSION) || (package_minver != JAVA6_PACKAGE_MINOR_VERSION && package_minver != JAVA5_PACKAGE_MINOR_VERSION)) {
            String gotVer = package_majver + "." + package_minver;
            String expVer = JAVA6_PACKAGE_MAJOR_VERSION + "." + JAVA6_PACKAGE_MINOR_VERSION + " OR " + JAVA5_PACKAGE_MAJOR_VERSION + "." + JAVA5_PACKAGE_MINOR_VERSION;
            throw new IOException("Unexpected package minor version: got " + gotVer + "; expected " + expVer);
        }
    }

    ArrayList classes = new ArrayList();

    public List getClasses() {
        return classes;
    }

    public class Class extends Attribute.Holder implements Comparable {

        public Package getPackage() {
            return Package.this;
        }

        File file;

        int magic;

        short minver, majver;

        Entry[] cpMap;

        ClassEntry thisClass;

        ClassEntry superClass;

        ClassEntry[] interfaces;

        ArrayList fields;

        ArrayList methods;

        ArrayList innerClasses;

        Class(int flags, ClassEntry thisClass, ClassEntry superClass, ClassEntry[] interfaces) {
            this.magic = JAVA_MAGIC;
            this.minver = default_class_minver;
            this.majver = default_class_majver;
            this.flags = flags;
            this.thisClass = thisClass;
            this.superClass = superClass;
            this.interfaces = interfaces;
            boolean added = classes.add(this);
            assert (added);
        }

        Class(String classFile) {
            initFile(newStub(classFile));
        }

        List getFields() {
            return fields == null ? noFields : fields;
        }

        List getMethods() {
            return methods == null ? noMethods : methods;
        }

        public String getName() {
            return thisClass.stringValue();
        }

        int getVersion() {
            return (majver << 16) + (char) minver;
        }

        String getVersionString() {
            return versionStringOf(majver, minver);
        }

        public int compareTo(Object o) {
            Class that = (Class) o;
            String n0 = this.getName();
            String n1 = that.getName();
            return n0.compareTo(n1);
        }

        String getObviousSourceFile() {
            return Package.getObviousSourceFile(getName());
        }

        private void transformSourceFile(boolean minimize) {
            Attribute olda = getAttribute(attrSourceFileSpecial);
            if (olda == null)
                return;
            String obvious = getObviousSourceFile();
            ArrayList ref = new ArrayList(1);
            olda.visitRefs(this, VRM_PACKAGE, ref);
            Utf8Entry sfName = (Utf8Entry) ref.get(0);
            Attribute a = olda;
            if (sfName == null) {
                if (minimize) {
                    a = Attribute.find(ATTR_CONTEXT_CLASS, "SourceFile", "H");
                    a = a.addContent(new byte[2]);
                } else {
                    byte[] bytes = new byte[2];
                    sfName = getRefString(obvious);
                    Object f = null;
                    f = Fixups.add(f, bytes, 0, Fixups.U2_FORMAT, sfName);
                    a = attrSourceFileSpecial.addContent(bytes, f);
                }
            } else if (obvious.equals(sfName.stringValue())) {
                if (minimize) {
                    a = attrSourceFileSpecial.addContent(new byte[2]);
                } else {
                    assert (false);
                }
            }
            if (a != olda) {
                if (verbose > 2)
                    Utils.log.fine("recoding obvious SourceFile=" + obvious);
                List newAttrs = new ArrayList(getAttributes());
                int where = newAttrs.indexOf(olda);
                newAttrs.set(where, a);
                setAttributes(newAttrs);
            }
        }

        void minimizeSourceFile() {
            transformSourceFile(true);
        }

        void expandSourceFile() {
            transformSourceFile(false);
        }

        protected Entry[] getCPMap() {
            return cpMap;
        }

        protected void setCPMap(Entry[] cpMap) {
            this.cpMap = cpMap;
        }

        boolean hasInnerClasses() {
            return innerClasses != null;
        }

        List getInnerClasses() {
            return innerClasses;
        }

        public void setInnerClasses(Collection ics) {
            innerClasses = (ics == null) ? null : new ArrayList(ics);
            Attribute a = getAttribute(attrInnerClassesEmpty);
            if (innerClasses != null && a == null)
                addAttribute(attrInnerClassesEmpty.canonicalInstance());
            else if (innerClasses == null && a != null)
                removeAttribute(a);
        }

        public List computeGloballyImpliedICs() {
            HashSet cpRefs = new HashSet();
            {
                ArrayList innerClassesSaved = innerClasses;
                innerClasses = null;
                visitRefs(VRM_CLASSIC, cpRefs);
                innerClasses = innerClassesSaved;
            }
            ConstantPool.completeReferencesIn(cpRefs, true);
            HashSet icRefs = new HashSet();
            for (Iterator i = cpRefs.iterator(); i.hasNext(); ) {
                Entry e = (Entry) i.next();
                if (!(e instanceof ClassEntry))
                    continue;
                while (e != null) {
                    InnerClass ic = getGlobalInnerClass(e);
                    if (ic == null)
                        break;
                    if (!icRefs.add(e))
                        break;
                    e = ic.outerClass;
                }
            }
            ArrayList impliedICs = new ArrayList();
            for (Iterator i = allInnerClasses.iterator(); i.hasNext(); ) {
                InnerClass ic = (InnerClass) i.next();
                if (icRefs.contains(ic.thisClass) || ic.outerClass == this.thisClass) {
                    if (verbose > 1)
                        Utils.log.fine("Relevant IC: " + ic);
                    impliedICs.add(ic);
                }
            }
            return impliedICs;
        }

        private List computeICdiff() {
            List impliedICs = computeGloballyImpliedICs();
            List actualICs = getInnerClasses();
            if (actualICs == null)
                actualICs = Collections.EMPTY_LIST;
            if (actualICs.isEmpty()) {
                return impliedICs;
            }
            if (impliedICs.isEmpty()) {
                return actualICs;
            }
            HashSet center = new HashSet(actualICs);
            center.retainAll(new HashSet(impliedICs));
            impliedICs.addAll(actualICs);
            impliedICs.removeAll(center);
            return impliedICs;
        }

        void minimizeLocalICs() {
            List diff = computeICdiff();
            List actualICs = innerClasses;
            List localICs;
            if (diff.isEmpty()) {
                localICs = null;
                if (actualICs != null && actualICs.isEmpty()) {
                    if (verbose > 0)
                        Utils.log.info("Warning: Dropping empty InnerClasses attribute from " + this);
                }
            } else if (actualICs == null) {
                localICs = Collections.EMPTY_LIST;
            } else {
                localICs = diff;
            }
            setInnerClasses(localICs);
            if (verbose > 1 && localICs != null)
                Utils.log.fine("keeping local ICs in " + this + ": " + localICs);
        }

        int expandLocalICs() {
            List localICs = innerClasses;
            List actualICs;
            int changed;
            if (localICs == null) {
                List impliedICs = computeGloballyImpliedICs();
                if (impliedICs.isEmpty()) {
                    actualICs = null;
                    changed = 0;
                } else {
                    actualICs = impliedICs;
                    changed = 1;
                }
            } else if (localICs.isEmpty()) {
                actualICs = null;
                changed = 0;
            } else {
                actualICs = computeICdiff();
                changed = actualICs.containsAll(localICs) ? +1 : -1;
            }
            setInnerClasses(actualICs);
            return changed;
        }

        public abstract class Member extends Attribute.Holder implements Comparable {

            DescriptorEntry descriptor;

            protected Member(int flags, DescriptorEntry descriptor) {
                this.flags = flags;
                this.descriptor = descriptor;
            }

            public Class thisClass() {
                return Class.this;
            }

            public DescriptorEntry getDescriptor() {
                return descriptor;
            }

            public String getName() {
                return descriptor.nameRef.stringValue();
            }

            public String getType() {
                return descriptor.typeRef.stringValue();
            }

            protected Entry[] getCPMap() {
                return cpMap;
            }

            protected void visitRefs(int mode, Collection refs) {
                if (verbose > 2)
                    Utils.log.fine("visitRefs " + this);
                if (mode == VRM_CLASSIC) {
                    refs.add(descriptor.nameRef);
                    refs.add(descriptor.typeRef);
                } else {
                    refs.add(descriptor);
                }
                super.visitRefs(mode, refs);
            }

            public String toString() {
                return Class.this + "." + descriptor.prettyString();
            }
        }

        public class Field extends Member {

            int order;

            public Field(int flags, DescriptorEntry descriptor) {
                super(flags, descriptor);
                assert (!descriptor.isMethod());
                if (fields == null)
                    fields = new ArrayList();
                boolean added = fields.add(this);
                assert (added);
                order = fields.size();
            }

            public byte getLiteralTag() {
                return descriptor.getLiteralTag();
            }

            public int compareTo(Object o) {
                Field that = (Field) o;
                return this.order - that.order;
            }
        }

        public class Method extends Member {

            Code code;

            public Method(int flags, DescriptorEntry descriptor) {
                super(flags, descriptor);
                assert (descriptor.isMethod());
                if (methods == null)
                    methods = new ArrayList();
                boolean added = methods.add(this);
                assert (added);
            }

            public void trimToSize() {
                super.trimToSize();
                if (code != null)
                    code.trimToSize();
            }

            public int getArgumentSize() {
                int argSize = descriptor.typeRef.computeSize(true);
                int thisSize = Modifier.isStatic(flags) ? 0 : 1;
                return thisSize + argSize;
            }

            public int compareTo(Object o) {
                Method that = (Method) o;
                return this.getDescriptor().compareTo(that.getDescriptor());
            }

            public void strip(String attrName) {
                if (attrName == "Code")
                    code = null;
                if (code != null)
                    code.strip(attrName);
                super.strip(attrName);
            }

            protected void visitRefs(int mode, Collection refs) {
                super.visitRefs(mode, refs);
                if (code != null) {
                    if (mode == VRM_CLASSIC) {
                        refs.add(getRefString("Code"));
                    }
                    code.visitRefs(mode, refs);
                }
            }
        }

        public void trimToSize() {
            super.trimToSize();
            for (int isM = 0; isM <= 1; isM++) {
                ArrayList members = (isM == 0) ? fields : methods;
                if (members == null)
                    continue;
                members.trimToSize();
                for (Iterator i = members.iterator(); i.hasNext(); ) {
                    Member m = (Member) i.next();
                    m.trimToSize();
                }
            }
            if (innerClasses != null) {
                innerClasses.trimToSize();
            }
        }

        public void strip(String attrName) {
            if (attrName == "InnerClass")
                innerClasses = null;
            for (int isM = 0; isM <= 1; isM++) {
                ArrayList members = (isM == 0) ? fields : methods;
                if (members == null)
                    continue;
                for (Iterator i = members.iterator(); i.hasNext(); ) {
                    Member m = (Member) i.next();
                    m.strip(attrName);
                }
            }
            super.strip(attrName);
        }

        protected void visitRefs(int mode, Collection refs) {
            if (verbose > 2)
                Utils.log.fine("visitRefs " + this);
            refs.add(thisClass);
            refs.add(superClass);
            for (int i = 0; i < interfaces.length; i++) {
                refs.add(interfaces[i]);
            }
            for (int isM = 0; isM <= 1; isM++) {
                ArrayList members = (isM == 0) ? fields : methods;
                if (members == null)
                    continue;
                for (Iterator i = members.iterator(); i.hasNext(); ) {
                    Member m = (Member) i.next();
                    boolean ok = false;
                    try {
                        m.visitRefs(mode, refs);
                        ok = true;
                    } finally {
                        if (!ok)
                            Utils.log.warning("Error scanning " + m);
                    }
                }
            }
            visitInnerClassRefs(mode, refs);
            super.visitRefs(mode, refs);
        }

        protected void visitInnerClassRefs(int mode, Collection refs) {
            Package.visitInnerClassRefs(innerClasses, mode, refs);
        }

        void finishReading() {
            trimToSize();
            maybeChooseFileName();
        }

        public void initFile(File file) {
            assert (this.file == null);
            if (file == null) {
                file = newStub(canonicalFileName());
            }
            this.file = file;
            assert (file.isClassStub());
            file.stubClass = this;
            maybeChooseFileName();
        }

        public void maybeChooseFileName() {
            if (thisClass == null) {
                return;
            }
            String canonName = canonicalFileName();
            if (file.nameString.equals("")) {
                file.nameString = canonName;
            }
            if (file.nameString.equals(canonName)) {
                file.name = getRefString("");
                return;
            }
            if (file.name == null) {
                file.name = getRefString(file.nameString);
            }
        }

        public String canonicalFileName() {
            if (thisClass == null)
                return null;
            return thisClass.stringValue() + ".class";
        }

        public java.io.File getFileName(java.io.File parent) {
            String name = file.name.stringValue();
            if (name.equals(""))
                name = canonicalFileName();
            String fname = name.replace('/', java.io.File.separatorChar);
            return new java.io.File(parent, fname);
        }

        public java.io.File getFileName() {
            return getFileName(null);
        }

        public String toString() {
            return thisClass.stringValue();
        }
    }

    void addClass(Class c) {
        assert (c.getPackage() == this);
        boolean added = classes.add(c);
        assert (added);
        if (c.file == null)
            c.initFile(null);
        addFile(c.file);
    }

    ArrayList files = new ArrayList();

    public List getFiles() {
        return files;
    }

    public List getClassStubs() {
        ArrayList classStubs = new ArrayList(classes.size());
        for (Iterator i = classes.iterator(); i.hasNext(); ) {
            Class cls = (Class) i.next();
            assert (cls.file.isClassStub());
            classStubs.add(cls.file);
        }
        return classStubs;
    }

    public class File implements Comparable {

        String nameString;

        Utf8Entry name;

        int modtime = NO_MODTIME;

        int options = 0;

        Class stubClass;

        ArrayList prepend = new ArrayList();

        java.io.ByteArrayOutputStream append = new ByteArrayOutputStream();

        File(Utf8Entry name) {
            this.name = name;
            this.nameString = name.stringValue();
        }

        File(String nameString) {
            nameString = fixupFileName(nameString);
            this.name = getRefString(nameString);
            this.nameString = name.stringValue();
        }

        public boolean isDirectory() {
            return nameString.endsWith("/");
        }

        public boolean isClassStub() {
            return (options & FO_IS_CLASS_STUB) != 0;
        }

        public Class getStubClass() {
            assert (isClassStub());
            assert (stubClass != null);
            return stubClass;
        }

        public boolean isTrivialClassStub() {
            return isClassStub() && name.stringValue().equals("") && (modtime == NO_MODTIME || modtime == default_modtime) && (options & ~FO_IS_CLASS_STUB) == 0;
        }

        public boolean equals(Object o) {
            File that = (File) o;
            return that.nameString == this.nameString;
        }

        public int hashCode() {
            return nameString.hashCode();
        }

        public int compareTo(Object o) {
            File that = (File) o;
            return this.nameString.compareTo(that.nameString);
        }

        public String toString() {
            return nameString + "{" + (isClassStub() ? "*" : "") + (BandStructure.testBit(options, FO_DEFLATE_HINT) ? "@" : "") + (modtime == NO_MODTIME ? "" : "M" + modtime) + (getFileLength() == 0 ? "" : "[" + getFileLength() + "]") + "}";
        }

        public java.io.File getFileName() {
            return getFileName(null);
        }

        public java.io.File getFileName(java.io.File parent) {
            String name = this.nameString;
            String fname = name.replace('/', java.io.File.separatorChar);
            return new java.io.File(parent, fname);
        }

        public void addBytes(byte[] bytes) {
            addBytes(bytes, 0, bytes.length);
        }

        public void addBytes(byte[] bytes, int off, int len) {
            if (((append.size() | len) << 2) < 0) {
                prepend.add(append.toByteArray());
                append.reset();
            }
            append.write(bytes, off, len);
        }

        public long getFileLength() {
            long len = 0;
            if (prepend == null && append == null)
                return 0;
            for (Iterator i = prepend.iterator(); i.hasNext(); ) {
                byte[] block = (byte[]) i.next();
                len += block.length;
            }
            len += append.size();
            return len;
        }

        public void writeTo(OutputStream out) throws IOException {
            if (prepend == null && append == null)
                return;
            for (Iterator i = prepend.iterator(); i.hasNext(); ) {
                byte[] block = (byte[]) i.next();
                out.write(block);
            }
            append.writeTo(out);
        }

        public void readFrom(InputStream in) throws IOException {
            byte[] buf = new byte[1 << 16];
            int nr;
            while ((nr = in.read(buf)) > 0) {
                addBytes(buf, 0, nr);
            }
        }

        public InputStream getInputStream() {
            InputStream in = new ByteArrayInputStream(append.toByteArray());
            if (prepend.size() == 0)
                return in;
            ArrayList isa = new ArrayList(prepend.size() + 1);
            for (Iterator i = prepend.iterator(); i.hasNext(); ) {
                byte[] bytes = (byte[]) i.next();
                isa.add(new ByteArrayInputStream(bytes));
            }
            isa.add(in);
            return new SequenceInputStream(Collections.enumeration(isa));
        }

        protected void visitRefs(int mode, Collection refs) {
            assert (name != null);
            refs.add(name);
        }
    }

    File newStub(String classFileNameString) {
        File stub = new File(classFileNameString);
        stub.options |= FO_IS_CLASS_STUB;
        stub.prepend = null;
        stub.append = null;
        return stub;
    }

    private static String fixupFileName(String name) {
        String fname = name.replace(java.io.File.separatorChar, '/');
        if (fname.startsWith("/")) {
            throw new IllegalArgumentException("absolute file name " + fname);
        }
        return fname;
    }

    void addFile(File file) {
        boolean added = files.add(file);
        assert (added);
    }

    ArrayList allInnerClasses = new ArrayList();

    HashMap allInnerClassesByThis;

    public List getAllInnerClasses() {
        return allInnerClasses;
    }

    public void setAllInnerClasses(Collection ics) {
        assert (ics != allInnerClasses);
        allInnerClasses.clear();
        allInnerClasses.addAll(ics);
        allInnerClassesByThis = new HashMap(allInnerClasses.size());
        for (Iterator i = allInnerClasses.iterator(); i.hasNext(); ) {
            InnerClass ic = (InnerClass) i.next();
            Object pic = allInnerClassesByThis.put(ic.thisClass, ic);
            assert (pic == null);
        }
    }

    public InnerClass getGlobalInnerClass(Entry thisClass) {
        assert (thisClass instanceof ClassEntry);
        return (InnerClass) allInnerClassesByThis.get(thisClass);
    }

    static class InnerClass implements Comparable {

        final ClassEntry thisClass;

        final ClassEntry outerClass;

        final Utf8Entry name;

        final int flags;

        final boolean predictable;

        InnerClass(ClassEntry thisClass, ClassEntry outerClass, Utf8Entry name, int flags) {
            this.thisClass = thisClass;
            this.outerClass = outerClass;
            this.name = name;
            this.flags = flags;
            this.predictable = computePredictable();
        }

        private boolean computePredictable() {
            String[] parse = parseInnerClassName(thisClass.stringValue());
            if (parse == null)
                return false;
            String pkgOuter = parse[0];
            String name = parse[2];
            String haveName = (this.name == null) ? null : this.name.stringValue();
            String haveOuter = (outerClass == null) ? null : outerClass.stringValue();
            boolean predictable = (name == haveName && pkgOuter == haveOuter);
            return predictable;
        }

        public boolean equals(Object o) {
            if (o == null)
                return false;
            InnerClass that = (InnerClass) o;
            return eq(this.thisClass, that.thisClass) && eq(this.outerClass, that.outerClass) && eq(this.name, that.name) && this.flags == that.flags;
        }

        private static boolean eq(Object x, Object y) {
            return (x == null) ? y == null : x.equals(y);
        }

        public int hashCode() {
            return thisClass.hashCode();
        }

        public int compareTo(Object o) {
            InnerClass that = (InnerClass) o;
            return this.thisClass.compareTo(that.thisClass);
        }

        protected void visitRefs(int mode, Collection refs) {
            refs.add(thisClass);
            if (mode == VRM_CLASSIC || !predictable) {
                refs.add(outerClass);
                refs.add(name);
            }
        }

        public String toString() {
            return thisClass.stringValue();
        }
    }

    static private void visitInnerClassRefs(Collection innerClasses, int mode, Collection refs) {
        if (innerClasses == null) {
            return;
        }
        if (mode == VRM_CLASSIC) {
            refs.add(getRefString("InnerClasses"));
        }
        if (innerClasses.size() > 0) {
            for (Iterator i = innerClasses.iterator(); i.hasNext(); ) {
                InnerClass c = (InnerClass) i.next();
                c.visitRefs(mode, refs);
            }
        }
    }

    static String[] parseInnerClassName(String n) {
        String pkgOuter, number, name;
        int dollar1, dollar2;
        int nlen = n.length();
        int pkglen = lastIndexOf(SLASH_MIN, SLASH_MAX, n, n.length()) + 1;
        dollar2 = lastIndexOf(DOLLAR_MIN, DOLLAR_MAX, n, n.length());
        if (dollar2 < pkglen)
            return null;
        if (isDigitString(n, dollar2 + 1, nlen)) {
            number = n.substring(dollar2 + 1, nlen);
            name = null;
            dollar1 = dollar2;
        } else if ((dollar1 = lastIndexOf(DOLLAR_MIN, DOLLAR_MAX, n, dollar2 - 1)) > pkglen && isDigitString(n, dollar1 + 1, dollar2)) {
            number = n.substring(dollar1 + 1, dollar2);
            name = n.substring(dollar2 + 1, nlen).intern();
        } else {
            dollar1 = dollar2;
            number = null;
            name = n.substring(dollar2 + 1, nlen).intern();
        }
        if (number == null)
            pkgOuter = n.substring(0, dollar1).intern();
        else
            pkgOuter = null;
        return new String[] { pkgOuter, number, name };
    }

    private static final int SLASH_MIN = '.';

    private static final int SLASH_MAX = '/';

    private static final int DOLLAR_MIN = 0;

    private static final int DOLLAR_MAX = '-';

    static {
        assert (lastIndexOf(DOLLAR_MIN, DOLLAR_MAX, "x$$y$", 4) == 2);
        assert (lastIndexOf(SLASH_MIN, SLASH_MAX, "x//y/", 4) == 2);
    }

    private static int lastIndexOf(int chMin, int chMax, String str, int pos) {
        for (int i = pos; --i >= 0; ) {
            int ch = str.charAt(i);
            if (ch >= chMin && ch <= chMax) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isDigitString(String x, int beg, int end) {
        if (beg == end)
            return false;
        for (int i = beg; i < end; i++) {
            char ch = x.charAt(i);
            if (!(ch >= '0' && ch <= '9'))
                return false;
        }
        return true;
    }

    static String getObviousSourceFile(String className) {
        String n = className;
        int pkglen = lastIndexOf(SLASH_MIN, SLASH_MAX, n, n.length()) + 1;
        n = n.substring(pkglen);
        int cutoff = n.length();
        for (; ; ) {
            int dollar2 = lastIndexOf(DOLLAR_MIN, DOLLAR_MAX, n, cutoff - 1);
            if (dollar2 < 0)
                break;
            cutoff = dollar2;
            if (cutoff == 0)
                break;
        }
        String obvious = n.substring(0, cutoff) + ".java";
        return obvious;
    }

    static Utf8Entry getRefString(String s) {
        return ConstantPool.getUtf8Entry(s);
    }

    static LiteralEntry getRefLiteral(Comparable s) {
        return ConstantPool.getLiteralEntry(s);
    }

    void stripAttributeKind(String what) {
        if (verbose > 0)
            Utils.log.info("Stripping " + what.toLowerCase() + " data and attributes...");
        if (what == "Debug") {
            strip("SourceFile");
            strip("LineNumberTable");
            strip("LocalVariableTable");
            strip("LocalVariableTypeTable");
        }
        if (what == "Compile") {
            strip("Deprecated");
            strip("Synthetic");
        }
        if (what == "Exceptions") {
            strip("Exceptions");
        }
        if (what == "Constant") {
            stripConstantFields();
        }
    }

    public void trimToSize() {
        classes.trimToSize();
        for (Iterator i = classes.iterator(); i.hasNext(); ) {
            Class c = (Class) i.next();
            c.trimToSize();
        }
        files.trimToSize();
    }

    public void strip(String attrName) {
        for (Iterator i = classes.iterator(); i.hasNext(); ) {
            Class c = (Class) i.next();
            c.strip(attrName);
        }
    }

    public static String versionStringOf(int majver, int minver) {
        return majver + "." + minver;
    }

    public static String versionStringOf(int version) {
        return versionStringOf(version >>> 16, (char) version);
    }

    public void stripConstantFields() {
        for (Iterator i = classes.iterator(); i.hasNext(); ) {
            Class c = (Class) i.next();
            for (Iterator j = c.fields.iterator(); j.hasNext(); ) {
                Class.Field f = (Class.Field) j.next();
                if (Modifier.isFinal(f.flags) && Modifier.isStatic(f.flags) && f.getAttribute("ConstantValue") != null && !f.getName().startsWith("serial")) {
                    if (verbose > 2) {
                        Utils.log.fine(">> Strip " + this + " ConstantValue");
                        j.remove();
                    }
                }
            }
        }
    }

    protected void visitRefs(int mode, Collection refs) {
        for (Iterator i = classes.iterator(); i.hasNext(); ) {
            Class c = (Class) i.next();
            c.visitRefs(mode, refs);
        }
        if (mode != VRM_CLASSIC) {
            for (Iterator i = files.iterator(); i.hasNext(); ) {
                File f = (File) i.next();
                f.visitRefs(mode, refs);
            }
            visitInnerClassRefs(allInnerClasses, mode, refs);
        }
    }

    void reorderFiles(boolean keepClassOrder, boolean stripDirectories) {
        if (!keepClassOrder) {
            Collections.sort(classes);
        }
        List stubs = getClassStubs();
        for (Iterator i = files.iterator(); i.hasNext(); ) {
            File file = (File) i.next();
            if (file.isClassStub() || (stripDirectories && file.isDirectory())) {
                i.remove();
            }
        }
        Collections.sort(files, new Comparator() {

            public int compare(Object o0, Object o1) {
                File r0 = (File) o0;
                File r1 = (File) o1;
                String f0 = r0.nameString;
                String f1 = r1.nameString;
                if (f0.equals(f1))
                    return 0;
                if (JarFile.MANIFEST_NAME.equals(f0))
                    return 0 - 1;
                if (JarFile.MANIFEST_NAME.equals(f1))
                    return 1 - 0;
                String n0 = f0.substring(1 + f0.lastIndexOf('/'));
                String n1 = f1.substring(1 + f1.lastIndexOf('/'));
                String x0 = n0.substring(1 + n0.lastIndexOf('.'));
                String x1 = n1.substring(1 + n1.lastIndexOf('.'));
                int r;
                r = x0.compareTo(x1);
                if (r != 0)
                    return r;
                r = f0.compareTo(f1);
                return r;
            }
        });
        files.addAll(stubs);
    }

    void trimStubs() {
        for (ListIterator i = files.listIterator(files.size()); i.hasPrevious(); ) {
            File file = (File) i.previous();
            if (!file.isTrivialClassStub()) {
                if (verbose > 1)
                    Utils.log.fine("Keeping last non-trivial " + file);
                break;
            }
            if (verbose > 2)
                Utils.log.fine("Removing trivial " + file);
            i.remove();
        }
        if (verbose > 0) {
            Utils.log.info("Transmitting " + files.size() + " files, including per-file data for " + getClassStubs().size() + " classes out of " + classes.size());
        }
    }

    void buildGlobalConstantPool(Set requiredEntries) {
        if (verbose > 1)
            Utils.log.fine("Checking for unused CP entries");
        requiredEntries.add(getRefString(""));
        visitRefs(VRM_PACKAGE, requiredEntries);
        ConstantPool.completeReferencesIn(requiredEntries, false);
        if (verbose > 1)
            Utils.log.fine("Sorting CP entries");
        Index cpAllU = ConstantPool.makeIndex("unsorted", requiredEntries);
        Index[] byTagU = ConstantPool.partitionByTag(cpAllU);
        for (int i = 0; i < ConstantPool.TAGS_IN_ORDER.length; i++) {
            byte tag = ConstantPool.TAGS_IN_ORDER[i];
            Index ix = byTagU[tag];
            if (ix == null)
                continue;
            ConstantPool.sort(ix);
            cp.initIndexByTag(tag, ix);
            byTagU[tag] = null;
        }
        for (int i = 0; i < byTagU.length; i++) {
            assert (byTagU[i] == null);
        }
        for (int i = 0; i < ConstantPool.TAGS_IN_ORDER.length; i++) {
            byte tag = ConstantPool.TAGS_IN_ORDER[i];
            Index ix = cp.getIndexByTag(tag);
            assert (ix.assertIsSorted());
            if (verbose > 2)
                Utils.log.fine(ix.dumpString());
        }
    }

    void ensureAllClassFiles() {
        HashSet fileSet = new HashSet(files);
        for (Iterator i = classes.iterator(); i.hasNext(); ) {
            Class cls = (Class) i.next();
            if (!fileSet.contains(cls.file))
                files.add(cls.file);
        }
    }

    static final List noObjects = Arrays.asList(new Object[0]);

    static final List noFields = Arrays.asList(new Class.Field[0]);

    static final List noMethods = Arrays.asList(new Class.Method[0]);

    static final List noInnerClasses = Arrays.asList(new InnerClass[0]);
}
