package com.sun.java.util.jar.pack;

import java.io.*;
import java.util.*;
import com.sun.java.util.jar.pack.ConstantPool.*;

class Attribute implements Comparable, Constants {

    Layout def;

    byte[] bytes;

    Object fixups;

    public String name() {
        return def.name();
    }

    public Layout layout() {
        return def;
    }

    public byte[] bytes() {
        return bytes;
    }

    public int size() {
        return bytes.length;
    }

    public Entry getNameRef() {
        return def.getNameRef();
    }

    private Attribute(Attribute old) {
        this.def = old.def;
        this.bytes = old.bytes;
        this.fixups = old.fixups;
    }

    public Attribute(Layout def, byte[] bytes, Object fixups) {
        this.def = def;
        this.bytes = bytes;
        this.fixups = fixups;
        Fixups.setBytes(fixups, bytes);
    }

    public Attribute(Layout def, byte[] bytes) {
        this(def, bytes, null);
    }

    public Attribute addContent(byte[] bytes, Object fixups) {
        assert (isCanonical());
        if (bytes.length == 0 && fixups == null)
            return this;
        Attribute res = new Attribute(this);
        res.bytes = bytes;
        res.fixups = fixups;
        Fixups.setBytes(fixups, bytes);
        return res;
    }

    public Attribute addContent(byte[] bytes) {
        return addContent(bytes, null);
    }

    public void finishRefs(Index ix) {
        if (fixups != null) {
            Fixups.finishRefs(fixups, bytes, ix);
            fixups = null;
        }
    }

    public boolean isCanonical() {
        return this == def.canon;
    }

    public int compareTo(Object o) {
        Attribute that = (Attribute) o;
        return this.def.compareTo(that.def);
    }

    private static final byte[] noBytes = {};

    private static final Map<List<Attribute>, List<Attribute>> canonLists = new HashMap<>();

    private static final Map<Layout, Attribute> attributes = new HashMap<>();

    private static final Map<Layout, Attribute> standardDefs = new HashMap<>();

    public static List getCanonList(List<Attribute> al) {
        synchronized (canonLists) {
            List<Attribute> cl = canonLists.get(al);
            if (cl == null) {
                cl = new ArrayList<>(al.size());
                cl.addAll(al);
                cl = Collections.unmodifiableList(cl);
                canonLists.put(al, cl);
            }
            return cl;
        }
    }

    public static Attribute find(int ctype, String name, String layout) {
        Layout key = Layout.makeKey(ctype, name, layout);
        synchronized (attributes) {
            Attribute a = attributes.get(key);
            if (a == null) {
                a = new Layout(ctype, name, layout).canonicalInstance();
                attributes.put(key, a);
            }
            return a;
        }
    }

    public static Layout keyForLookup(int ctype, String name) {
        return Layout.makeKey(ctype, name);
    }

    public static Attribute lookup(Map<Layout, Attribute> defs, int ctype, String name) {
        if (defs == null) {
            defs = standardDefs;
        }
        return defs.get(Layout.makeKey(ctype, name));
    }

    public static Attribute define(Map<Layout, Attribute> defs, int ctype, String name, String layout) {
        Attribute a = find(ctype, name, layout);
        defs.put(Layout.makeKey(ctype, name), a);
        return a;
    }

    static {
        Map<Layout, Attribute> sd = standardDefs;
        define(sd, ATTR_CONTEXT_CLASS, "Signature", "RSH");
        define(sd, ATTR_CONTEXT_CLASS, "Synthetic", "");
        define(sd, ATTR_CONTEXT_CLASS, "Deprecated", "");
        define(sd, ATTR_CONTEXT_CLASS, "SourceFile", "RUH");
        define(sd, ATTR_CONTEXT_CLASS, "EnclosingMethod", "RCHRDNH");
        define(sd, ATTR_CONTEXT_CLASS, "InnerClasses", "NH[RCHRCNHRUNHFH]");
        define(sd, ATTR_CONTEXT_FIELD, "Signature", "RSH");
        define(sd, ATTR_CONTEXT_FIELD, "Synthetic", "");
        define(sd, ATTR_CONTEXT_FIELD, "Deprecated", "");
        define(sd, ATTR_CONTEXT_FIELD, "ConstantValue", "KQH");
        define(sd, ATTR_CONTEXT_METHOD, "Signature", "RSH");
        define(sd, ATTR_CONTEXT_METHOD, "Synthetic", "");
        define(sd, ATTR_CONTEXT_METHOD, "Deprecated", "");
        define(sd, ATTR_CONTEXT_METHOD, "Exceptions", "NH[RCH]");
        define(sd, ATTR_CONTEXT_CODE, "StackMapTable", ("[NH[(1)]]" + "[TB" + "(64-127)[(2)]" + "(247)[(1)(2)]" + "(248-251)[(1)]" + "(252)[(1)(2)]" + "(253)[(1)(2)(2)]" + "(254)[(1)(2)(2)(2)]" + "(255)[(1)NH[(2)]NH[(2)]]" + "()[]" + "]" + "[H]" + "[TB(7)[RCH](8)[PH]()[]]"));
        define(sd, ATTR_CONTEXT_CODE, "LineNumberTable", "NH[PHH]");
        define(sd, ATTR_CONTEXT_CODE, "LocalVariableTable", "NH[PHOHRUHRSHH]");
        define(sd, ATTR_CONTEXT_CODE, "LocalVariableTypeTable", "NH[PHOHRUHRSHH]");
    }

    static {
        String[] mdLayouts = { Attribute.normalizeLayoutString("" + "\n  # parameter_annotations :=" + "\n  [ NB[(1)] ]     # forward call to annotations"), Attribute.normalizeLayoutString("" + "\n  # annotations :=" + "\n  [ NH[(1)] ]     # forward call to annotation" + "\n  " + "\n  # annotation :=" + "\n  [RSH" + "\n    NH[RUH (1)]   # forward call to value" + "\n    ]"), Attribute.normalizeLayoutString("" + "\n  # value :=" + "\n  [TB # Callable 2 encodes one tagged value." + "\n    (\\B,\\C,\\I,\\S,\\Z)[KIH]" + "\n    (\\D)[KDH]" + "\n    (\\F)[KFH]" + "\n    (\\J)[KJH]" + "\n    (\\c)[RSH]" + "\n    (\\e)[RSH RUH]" + "\n    (\\s)[RUH]" + "\n    (\\[)[NH[(0)]] # backward self-call to value" + "\n    (\\@)[RSH NH[RUH (0)]] # backward self-call to value" + "\n    ()[] ]") };
        Map<Layout, Attribute> sd = standardDefs;
        String defaultLayout = mdLayouts[2];
        String annotationsLayout = mdLayouts[1] + mdLayouts[2];
        String paramsLayout = mdLayouts[0] + annotationsLayout;
        for (int ctype = 0; ctype < ATTR_CONTEXT_LIMIT; ctype++) {
            if (ctype == ATTR_CONTEXT_CODE)
                continue;
            define(sd, ctype, "RuntimeVisibleAnnotations", annotationsLayout);
            define(sd, ctype, "RuntimeInvisibleAnnotations", annotationsLayout);
            if (ctype == ATTR_CONTEXT_METHOD) {
                define(sd, ctype, "RuntimeVisibleParameterAnnotations", paramsLayout);
                define(sd, ctype, "RuntimeInvisibleParameterAnnotations", paramsLayout);
                define(sd, ctype, "AnnotationDefault", defaultLayout);
            }
        }
    }

    public static String contextName(int ctype) {
        switch(ctype) {
            case ATTR_CONTEXT_CLASS:
                return "class";
            case ATTR_CONTEXT_FIELD:
                return "field";
            case ATTR_CONTEXT_METHOD:
                return "method";
            case ATTR_CONTEXT_CODE:
                return "code";
        }
        return null;
    }

    public static abstract class Holder {

        protected abstract Entry[] getCPMap();

        protected int flags;

        protected List<Attribute> attributes;

        public int attributeSize() {
            return (attributes == null) ? 0 : attributes.size();
        }

        public void trimToSize() {
            if (attributes == null) {
                return;
            }
            if (attributes.isEmpty()) {
                attributes = null;
                return;
            }
            if (attributes instanceof ArrayList) {
                ArrayList<Attribute> al = (ArrayList<Attribute>) attributes;
                al.trimToSize();
                boolean allCanon = true;
                for (Attribute a : al) {
                    if (!a.isCanonical()) {
                        allCanon = false;
                    }
                    if (a.fixups != null) {
                        assert (!a.isCanonical());
                        a.fixups = Fixups.trimToSize(a.fixups);
                    }
                }
                if (allCanon) {
                    attributes = getCanonList(al);
                }
            }
        }

        public void addAttribute(Attribute a) {
            if (attributes == null)
                attributes = new ArrayList<>(3);
            else if (!(attributes instanceof ArrayList))
                attributes = new ArrayList<>(attributes);
            attributes.add(a);
        }

        public Attribute removeAttribute(Attribute a) {
            if (attributes == null)
                return null;
            if (!attributes.contains(a))
                return null;
            if (!(attributes instanceof ArrayList))
                attributes = new ArrayList<>(attributes);
            attributes.remove(a);
            return a;
        }

        public Attribute getAttribute(int n) {
            return attributes.get(n);
        }

        protected void visitRefs(int mode, Collection<Entry> refs) {
            if (attributes == null)
                return;
            for (Attribute a : attributes) {
                a.visitRefs(this, mode, refs);
            }
        }

        static final List<Attribute> noAttributes = Arrays.asList(new Attribute[0]);

        public List<Attribute> getAttributes() {
            if (attributes == null)
                return noAttributes;
            return attributes;
        }

        public void setAttributes(List<Attribute> attrList) {
            if (attrList.isEmpty())
                attributes = null;
            else
                attributes = attrList;
        }

        public Attribute getAttribute(String attrName) {
            if (attributes == null)
                return null;
            for (Attribute a : attributes) {
                if (a.name().equals(attrName))
                    return a;
            }
            return null;
        }

        public Attribute getAttribute(Layout attrDef) {
            if (attributes == null)
                return null;
            for (Attribute a : attributes) {
                if (a.layout() == attrDef)
                    return a;
            }
            return null;
        }

        public Attribute removeAttribute(String attrName) {
            return removeAttribute(getAttribute(attrName));
        }

        public Attribute removeAttribute(Layout attrDef) {
            return removeAttribute(getAttribute(attrDef));
        }

        public void strip(String attrName) {
            removeAttribute(getAttribute(attrName));
        }
    }

    public static abstract class ValueStream {

        public int getInt(int bandIndex) {
            throw undef();
        }

        public void putInt(int bandIndex, int value) {
            throw undef();
        }

        public Entry getRef(int bandIndex) {
            throw undef();
        }

        public void putRef(int bandIndex, Entry ref) {
            throw undef();
        }

        public int decodeBCI(int bciCode) {
            throw undef();
        }

        public int encodeBCI(int bci) {
            throw undef();
        }

        public void noteBackCall(int whichCallable) {
        }

        private RuntimeException undef() {
            return new UnsupportedOperationException("ValueStream method");
        }
    }

    static final byte EK_INT = 1;

    static final byte EK_BCI = 2;

    static final byte EK_BCO = 3;

    static final byte EK_FLAG = 4;

    static final byte EK_REPL = 5;

    static final byte EK_REF = 6;

    static final byte EK_UN = 7;

    static final byte EK_CASE = 8;

    static final byte EK_CALL = 9;

    static final byte EK_CBLE = 10;

    static final byte EF_SIGN = 1 << 0;

    static final byte EF_DELTA = 1 << 1;

    static final byte EF_NULL = 1 << 2;

    static final byte EF_BACK = 1 << 3;

    static final int NO_BAND_INDEX = -1;

    public static class Layout implements Comparable {

        int ctype;

        String name;

        boolean hasRefs;

        String layout;

        int bandCount;

        Element[] elems;

        Attribute canon;

        public int ctype() {
            return ctype;
        }

        public String name() {
            return name;
        }

        public String layout() {
            return layout;
        }

        public Attribute canonicalInstance() {
            return canon;
        }

        public Entry getNameRef() {
            return ConstantPool.getUtf8Entry(name());
        }

        public boolean isEmpty() {
            return layout == "";
        }

        public Layout(int ctype, String name, String layout) {
            this.ctype = ctype;
            this.name = name.intern();
            this.layout = layout.intern();
            assert (ctype < ATTR_CONTEXT_LIMIT);
            boolean hasCallables = layout.startsWith("[");
            try {
                if (!hasCallables) {
                    this.elems = tokenizeLayout(this, -1, layout);
                } else {
                    String[] bodies = splitBodies(layout);
                    Element[] elems = new Element[bodies.length];
                    this.elems = elems;
                    for (int i = 0; i < elems.length; i++) {
                        Element ce = this.new Element();
                        ce.kind = EK_CBLE;
                        ce.removeBand();
                        ce.bandIndex = NO_BAND_INDEX;
                        ce.layout = bodies[i];
                        elems[i] = ce;
                    }
                    for (int i = 0; i < elems.length; i++) {
                        Element ce = elems[i];
                        ce.body = tokenizeLayout(this, i, bodies[i]);
                    }
                }
            } catch (StringIndexOutOfBoundsException ee) {
                throw new RuntimeException("Bad attribute layout: " + layout, ee);
            }
            canon = new Attribute(this, noBytes);
        }

        private Layout() {
        }

        static Layout makeKey(int ctype, String name, String layout) {
            Layout def = new Layout();
            def.ctype = ctype;
            def.name = name.intern();
            def.layout = layout.intern();
            assert (ctype < ATTR_CONTEXT_LIMIT);
            return def;
        }

        static Layout makeKey(int ctype, String name) {
            return makeKey(ctype, name, "");
        }

        public Attribute addContent(byte[] bytes, Object fixups) {
            return canon.addContent(bytes, fixups);
        }

        public Attribute addContent(byte[] bytes) {
            return canon.addContent(bytes, null);
        }

        public boolean equals(Object x) {
            return x instanceof Layout && equals((Layout) x);
        }

        public boolean equals(Layout that) {
            return this.name == that.name && this.layout == that.layout && this.ctype == that.ctype;
        }

        public int hashCode() {
            return (((17 + name.hashCode()) * 37 + layout.hashCode()) * 37 + ctype);
        }

        public int compareTo(Object o) {
            Layout that = (Layout) o;
            int r;
            r = this.name.compareTo(that.name);
            if (r != 0)
                return r;
            r = this.layout.compareTo(that.layout);
            if (r != 0)
                return r;
            return this.ctype - that.ctype;
        }

        public String toString() {
            String str = contextName(ctype) + "." + name + "[" + layout + "]";
            assert ((str = stringForDebug()) != null);
            return str;
        }

        private String stringForDebug() {
            return contextName(ctype) + "." + name + Arrays.asList(elems);
        }

        public class Element {

            String layout;

            byte flags;

            byte kind;

            byte len;

            byte refKind;

            int bandIndex;

            int value;

            Element[] body;

            boolean flagTest(byte mask) {
                return (flags & mask) != 0;
            }

            Element() {
                bandIndex = bandCount++;
            }

            void removeBand() {
                --bandCount;
                assert (bandIndex == bandCount);
                bandIndex = NO_BAND_INDEX;
            }

            public boolean hasBand() {
                return bandIndex >= 0;
            }

            public String toString() {
                String str = layout;
                assert ((str = stringForDebug()) != null);
                return str;
            }

            private String stringForDebug() {
                Element[] body = this.body;
                switch(kind) {
                    case EK_CALL:
                        body = null;
                        break;
                    case EK_CASE:
                        if (flagTest(EF_BACK))
                            body = null;
                        break;
                }
                return layout + (!hasBand() ? "" : "#" + bandIndex) + "<" + (flags == 0 ? "" : "" + flags) + kind + len + (refKind == 0 ? "" : "" + refKind) + ">" + (value == 0 ? "" : "(" + value + ")") + (body == null ? "" : "" + Arrays.asList(body));
            }
        }

        public boolean hasCallables() {
            return (elems.length > 0 && elems[0].kind == EK_CBLE);
        }

        static private final Element[] noElems = {};

        public Element[] getCallables() {
            if (hasCallables())
                return elems;
            else
                return noElems;
        }

        public Element[] getEntryPoint() {
            if (hasCallables())
                return elems[0].body;
            else
                return elems;
        }

        public void parse(Holder holder, byte[] bytes, int pos, int len, ValueStream out) {
            int end = parseUsing(getEntryPoint(), holder, bytes, pos, len, out);
            if (end != pos + len)
                throw new InternalError("layout parsed " + (end - pos) + " out of " + len + " bytes");
        }

        public Object unparse(ValueStream in, ByteArrayOutputStream out) {
            Object[] fixups = { null };
            unparseUsing(getEntryPoint(), fixups, in, out);
            return fixups[0];
        }

        public String layoutForPackageMajver(int majver) {
            if (majver <= JAVA5_PACKAGE_MAJOR_VERSION) {
                return expandCaseDashNotation(layout);
            }
            return layout;
        }
    }

    public static class FormatException extends IOException {

        private int ctype;

        private String name;

        String layout;

        public FormatException(String message, int ctype, String name, String layout) {
            super(ATTR_CONTEXT_NAME[ctype] + " attribute \"" + name + "\"" + (message == null ? "" : (": " + message)));
            this.ctype = ctype;
            this.name = name;
            this.layout = layout;
        }

        public FormatException(String message, int ctype, String name) {
            this(message, ctype, name, null);
        }
    }

    void visitRefs(Holder holder, int mode, final Collection refs) {
        if (mode == VRM_CLASSIC) {
            refs.add(getNameRef());
        }
        if (bytes.length == 0)
            return;
        if (!def.hasRefs)
            return;
        if (fixups != null) {
            Fixups.visitRefs(fixups, refs);
            return;
        }
        def.parse(holder, bytes, 0, bytes.length, new ValueStream() {

            public void putInt(int bandIndex, int value) {
            }

            public void putRef(int bandIndex, Entry ref) {
                refs.add(ref);
            }

            public int encodeBCI(int bci) {
                return bci;
            }
        });
    }

    public void parse(Holder holder, byte[] bytes, int pos, int len, ValueStream out) {
        def.parse(holder, bytes, pos, len, out);
    }

    public Object unparse(ValueStream in, ByteArrayOutputStream out) {
        return def.unparse(in, out);
    }

    public String toString() {
        return def + "{" + (bytes == null ? -1 : size()) + "}" + (fixups == null ? "" : fixups.toString());
    }

    static public String normalizeLayoutString(String layout) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0, len = layout.length(); i < len; ) {
            char ch = layout.charAt(i++);
            if (ch <= ' ') {
                continue;
            } else if (ch == '#') {
                int end1 = layout.indexOf('\n', i);
                int end2 = layout.indexOf('\r', i);
                if (end1 < 0)
                    end1 = len;
                if (end2 < 0)
                    end2 = len;
                i = Math.min(end1, end2);
            } else if (ch == '\\') {
                buf.append((int) layout.charAt(i++));
            } else if (ch == '0' && layout.startsWith("0x", i - 1)) {
                int start = i - 1;
                int end = start + 2;
                while (end < len) {
                    int dig = layout.charAt(end);
                    if ((dig >= '0' && dig <= '9') || (dig >= 'a' && dig <= 'f'))
                        ++end;
                    else
                        break;
                }
                if (end > start) {
                    String num = layout.substring(start, end);
                    buf.append(Integer.decode(num));
                    i = end;
                } else {
                    buf.append(ch);
                }
            } else {
                buf.append(ch);
            }
        }
        String result = buf.toString();
        if (false && !result.equals(layout)) {
            Utils.log.info("Normalizing layout string");
            Utils.log.info("    From: " + layout);
            Utils.log.info("    To:   " + result);
        }
        return result;
    }

    static Layout.Element[] tokenizeLayout(Layout self, int curCble, String layout) {
        ArrayList<Layout.Element> col = new ArrayList<>(layout.length());
        tokenizeLayout(self, curCble, layout, col);
        Layout.Element[] res = new Layout.Element[col.size()];
        col.toArray(res);
        return res;
    }

    static void tokenizeLayout(Layout self, int curCble, String layout, ArrayList<Layout.Element> col) {
        boolean prevBCI = false;
        for (int len = layout.length(), i = 0; i < len; ) {
            int start = i;
            int body;
            Layout.Element e = self.new Element();
            byte kind;
            switch(layout.charAt(i++)) {
                case 'B':
                case 'H':
                case 'I':
                case 'V':
                    kind = EK_INT;
                    --i;
                    i = tokenizeUInt(e, layout, i);
                    break;
                case 'S':
                    kind = EK_INT;
                    --i;
                    i = tokenizeSInt(e, layout, i);
                    break;
                case 'P':
                    kind = EK_BCI;
                    if (layout.charAt(i++) == 'O') {
                        e.flags |= EF_DELTA;
                        if (!prevBCI) {
                            i = -i;
                            continue;
                        }
                        i++;
                    }
                    --i;
                    i = tokenizeUInt(e, layout, i);
                    break;
                case 'O':
                    kind = EK_BCO;
                    e.flags |= EF_DELTA;
                    if (!prevBCI) {
                        i = -i;
                        continue;
                    }
                    i = tokenizeSInt(e, layout, i);
                    break;
                case 'F':
                    kind = EK_FLAG;
                    i = tokenizeUInt(e, layout, i);
                    break;
                case 'N':
                    kind = EK_REPL;
                    i = tokenizeUInt(e, layout, i);
                    if (layout.charAt(i++) != '[') {
                        i = -i;
                        continue;
                    }
                    i = skipBody(layout, body = i);
                    e.body = tokenizeLayout(self, curCble, layout.substring(body, i++));
                    break;
                case 'T':
                    kind = EK_UN;
                    i = tokenizeSInt(e, layout, i);
                    ArrayList<Layout.Element> cases = new ArrayList<>();
                    for (; ; ) {
                        if (layout.charAt(i++) != '(') {
                            i = -i;
                            break;
                        }
                        int beg = i;
                        i = layout.indexOf(')', i);
                        String cstr = layout.substring(beg, i++);
                        int cstrlen = cstr.length();
                        if (layout.charAt(i++) != '[') {
                            i = -i;
                            break;
                        }
                        if (layout.charAt(i) == ']')
                            body = i;
                        else
                            i = skipBody(layout, body = i);
                        Layout.Element[] cbody = tokenizeLayout(self, curCble, layout.substring(body, i++));
                        if (cstrlen == 0) {
                            Layout.Element ce = self.new Element();
                            ce.body = cbody;
                            ce.kind = EK_CASE;
                            ce.removeBand();
                            cases.add(ce);
                            break;
                        } else {
                            boolean firstCaseNum = true;
                            for (int cp = 0, endp; ; cp = endp + 1) {
                                endp = cstr.indexOf(',', cp);
                                if (endp < 0)
                                    endp = cstrlen;
                                String cstr1 = cstr.substring(cp, endp);
                                if (cstr1.length() == 0)
                                    cstr1 = "empty";
                                int value0, value1;
                                int dash = findCaseDash(cstr1, 0);
                                if (dash >= 0) {
                                    value0 = parseIntBefore(cstr1, dash);
                                    value1 = parseIntAfter(cstr1, dash);
                                    if (value0 >= value1) {
                                        i = -i;
                                        break;
                                    }
                                } else {
                                    value0 = value1 = Integer.parseInt(cstr1);
                                }
                                for (; ; value0++) {
                                    Layout.Element ce = self.new Element();
                                    ce.body = cbody;
                                    ce.kind = EK_CASE;
                                    ce.removeBand();
                                    if (!firstCaseNum)
                                        ce.flags |= EF_BACK;
                                    firstCaseNum = false;
                                    ce.value = value0;
                                    cases.add(ce);
                                    if (value0 == value1)
                                        break;
                                }
                                if (endp == cstrlen) {
                                    break;
                                }
                            }
                        }
                    }
                    e.body = new Layout.Element[cases.size()];
                    cases.toArray(e.body);
                    e.kind = kind;
                    for (int j = 0; j < e.body.length - 1; j++) {
                        Layout.Element ce = e.body[j];
                        if (matchCase(e, ce.value) != ce) {
                            {
                                i = -i;
                                break;
                            }
                        }
                    }
                    break;
                case '(':
                    kind = EK_CALL;
                    e.removeBand();
                    i = layout.indexOf(')', i);
                    String cstr = layout.substring(start + 1, i++);
                    int offset = Integer.parseInt(cstr);
                    int target = curCble + offset;
                    if (!(offset + "").equals(cstr) || self.elems == null || target < 0 || target >= self.elems.length) {
                        i = -i;
                        continue;
                    }
                    Layout.Element ce = self.elems[target];
                    assert (ce.kind == EK_CBLE);
                    e.value = target;
                    e.body = new Layout.Element[] { ce };
                    if (offset <= 0) {
                        e.flags |= EF_BACK;
                        ce.flags |= EF_BACK;
                    }
                    break;
                case 'K':
                    kind = EK_REF;
                    switch(layout.charAt(i++)) {
                        case 'I':
                            e.refKind = CONSTANT_Integer;
                            break;
                        case 'J':
                            e.refKind = CONSTANT_Long;
                            break;
                        case 'F':
                            e.refKind = CONSTANT_Float;
                            break;
                        case 'D':
                            e.refKind = CONSTANT_Double;
                            break;
                        case 'S':
                            e.refKind = CONSTANT_String;
                            break;
                        case 'Q':
                            e.refKind = CONSTANT_Literal;
                            break;
                        default:
                            {
                                i = -i;
                                continue;
                            }
                    }
                    break;
                case 'R':
                    kind = EK_REF;
                    switch(layout.charAt(i++)) {
                        case 'C':
                            e.refKind = CONSTANT_Class;
                            break;
                        case 'S':
                            e.refKind = CONSTANT_Signature;
                            break;
                        case 'D':
                            e.refKind = CONSTANT_NameandType;
                            break;
                        case 'F':
                            e.refKind = CONSTANT_Fieldref;
                            break;
                        case 'M':
                            e.refKind = CONSTANT_Methodref;
                            break;
                        case 'I':
                            e.refKind = CONSTANT_InterfaceMethodref;
                            break;
                        case 'U':
                            e.refKind = CONSTANT_Utf8;
                            break;
                        case 'Q':
                            e.refKind = CONSTANT_All;
                            break;
                        default:
                            {
                                i = -i;
                                continue;
                            }
                    }
                    break;
                default:
                    {
                        i = -i;
                        continue;
                    }
            }
            if (kind == EK_REF) {
                if (layout.charAt(i++) == 'N') {
                    e.flags |= EF_NULL;
                    i++;
                }
                --i;
                i = tokenizeUInt(e, layout, i);
                self.hasRefs = true;
            }
            prevBCI = (kind == EK_BCI);
            e.kind = kind;
            e.layout = layout.substring(start, i);
            col.add(e);
        }
    }

    static String[] splitBodies(String layout) {
        ArrayList<String> bodies = new ArrayList<>();
        for (int i = 0; i < layout.length(); i++) {
            if (layout.charAt(i++) != '[')
                layout.charAt(-i);
            int body;
            i = skipBody(layout, body = i);
            bodies.add(layout.substring(body, i));
        }
        String[] res = new String[bodies.size()];
        bodies.toArray(res);
        return res;
    }

    static private int skipBody(String layout, int i) {
        assert (layout.charAt(i - 1) == '[');
        if (layout.charAt(i) == ']')
            return -i;
        for (int depth = 1; depth > 0; ) {
            switch(layout.charAt(i++)) {
                case '[':
                    depth++;
                    break;
                case ']':
                    depth--;
                    break;
            }
        }
        --i;
        assert (layout.charAt(i) == ']');
        return i;
    }

    static private int tokenizeUInt(Layout.Element e, String layout, int i) {
        switch(layout.charAt(i++)) {
            case 'V':
                e.len = 0;
                break;
            case 'B':
                e.len = 1;
                break;
            case 'H':
                e.len = 2;
                break;
            case 'I':
                e.len = 4;
                break;
            default:
                return -i;
        }
        return i;
    }

    static private int tokenizeSInt(Layout.Element e, String layout, int i) {
        if (layout.charAt(i) == 'S') {
            e.flags |= EF_SIGN;
            ++i;
        }
        return tokenizeUInt(e, layout, i);
    }

    static private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    static int findCaseDash(String layout, int fromIndex) {
        if (fromIndex <= 0)
            fromIndex = 1;
        int lastDash = layout.length() - 2;
        for (; ; ) {
            int dash = layout.indexOf('-', fromIndex);
            if (dash < 0 || dash > lastDash)
                return -1;
            if (isDigit(layout.charAt(dash - 1))) {
                char afterDash = layout.charAt(dash + 1);
                if (afterDash == '-' && dash + 2 < layout.length())
                    afterDash = layout.charAt(dash + 2);
                if (isDigit(afterDash)) {
                    return dash;
                }
            }
            fromIndex = dash + 1;
        }
    }

    static int parseIntBefore(String layout, int dash) {
        int end = dash;
        int beg = end;
        while (beg > 0 && isDigit(layout.charAt(beg - 1))) {
            --beg;
        }
        if (beg == end)
            return Integer.parseInt("empty");
        if (beg >= 1 && layout.charAt(beg - 1) == '-')
            --beg;
        assert (beg == 0 || !isDigit(layout.charAt(beg - 1)));
        return Integer.parseInt(layout.substring(beg, end));
    }

    static int parseIntAfter(String layout, int dash) {
        int beg = dash + 1;
        int end = beg;
        int limit = layout.length();
        if (end < limit && layout.charAt(end) == '-')
            ++end;
        while (end < limit && isDigit(layout.charAt(end))) {
            ++end;
        }
        if (beg == end)
            return Integer.parseInt("empty");
        return Integer.parseInt(layout.substring(beg, end));
    }

    static String expandCaseDashNotation(String layout) {
        int dash = findCaseDash(layout, 0);
        if (dash < 0)
            return layout;
        StringBuffer result = new StringBuffer(layout.length() * 3);
        int sofar = 0;
        for (; ; ) {
            result.append(layout.substring(sofar, dash));
            sofar = dash + 1;
            int value0 = parseIntBefore(layout, dash);
            int value1 = parseIntAfter(layout, dash);
            assert (value0 < value1);
            result.append(",");
            for (int i = value0 + 1; i < value1; i++) {
                result.append(i);
                result.append(",");
            }
            dash = findCaseDash(layout, sofar);
            if (dash < 0)
                break;
        }
        result.append(layout.substring(sofar));
        return result.toString();
    }

    static {
        assert (expandCaseDashNotation("1-5").equals("1,2,3,4,5"));
        assert (expandCaseDashNotation("-2--1").equals("-2,-1"));
        assert (expandCaseDashNotation("-2-1").equals("-2,-1,0,1"));
        assert (expandCaseDashNotation("-1-0").equals("-1,0"));
    }

    static int parseUsing(Layout.Element[] elems, Holder holder, byte[] bytes, int pos, int len, ValueStream out) {
        int prevBCI = 0;
        int prevRBCI = 0;
        int end = pos + len;
        int[] buf = { 0 };
        for (int i = 0; i < elems.length; i++) {
            Layout.Element e = elems[i];
            int bandIndex = e.bandIndex;
            int value;
            int BCI, RBCI;
            switch(e.kind) {
                case EK_INT:
                    pos = parseInt(e, bytes, pos, buf);
                    value = buf[0];
                    out.putInt(bandIndex, value);
                    break;
                case EK_BCI:
                    pos = parseInt(e, bytes, pos, buf);
                    BCI = buf[0];
                    RBCI = out.encodeBCI(BCI);
                    if (!e.flagTest(EF_DELTA)) {
                        value = RBCI;
                    } else {
                        value = RBCI - prevRBCI;
                    }
                    prevBCI = BCI;
                    prevRBCI = RBCI;
                    out.putInt(bandIndex, value);
                    break;
                case EK_BCO:
                    assert (e.flagTest(EF_DELTA));
                    pos = parseInt(e, bytes, pos, buf);
                    BCI = prevBCI + buf[0];
                    RBCI = out.encodeBCI(BCI);
                    value = RBCI - prevRBCI;
                    prevBCI = BCI;
                    prevRBCI = RBCI;
                    out.putInt(bandIndex, value);
                    break;
                case EK_FLAG:
                    pos = parseInt(e, bytes, pos, buf);
                    value = buf[0];
                    out.putInt(bandIndex, value);
                    break;
                case EK_REPL:
                    pos = parseInt(e, bytes, pos, buf);
                    value = buf[0];
                    out.putInt(bandIndex, value);
                    for (int j = 0; j < value; j++) {
                        pos = parseUsing(e.body, holder, bytes, pos, end - pos, out);
                    }
                    break;
                case EK_UN:
                    pos = parseInt(e, bytes, pos, buf);
                    value = buf[0];
                    out.putInt(bandIndex, value);
                    Layout.Element ce = matchCase(e, value);
                    pos = parseUsing(ce.body, holder, bytes, pos, end - pos, out);
                    break;
                case EK_CALL:
                    assert (e.body.length == 1);
                    assert (e.body[0].kind == EK_CBLE);
                    if (e.flagTest(EF_BACK))
                        out.noteBackCall(e.value);
                    pos = parseUsing(e.body[0].body, holder, bytes, pos, end - pos, out);
                    break;
                case EK_REF:
                    pos = parseInt(e, bytes, pos, buf);
                    int localRef = buf[0];
                    Entry globalRef;
                    if (localRef == 0) {
                        globalRef = null;
                    } else {
                        globalRef = holder.getCPMap()[localRef];
                        if (e.refKind == CONSTANT_Signature && globalRef.getTag() == CONSTANT_Utf8) {
                            String typeName = globalRef.stringValue();
                            globalRef = ConstantPool.getSignatureEntry(typeName);
                        } else if (e.refKind == CONSTANT_Literal) {
                            assert (globalRef.getTag() >= CONSTANT_Integer);
                            assert (globalRef.getTag() <= CONSTANT_String);
                        } else if (e.refKind != CONSTANT_All) {
                            assert (e.refKind == globalRef.getTag());
                        }
                    }
                    out.putRef(bandIndex, globalRef);
                    break;
                default:
                    assert (false);
                    continue;
            }
        }
        return pos;
    }

    static Layout.Element matchCase(Layout.Element e, int value) {
        assert (e.kind == EK_UN);
        int lastj = e.body.length - 1;
        for (int j = 0; j < lastj; j++) {
            Layout.Element ce = e.body[j];
            assert (ce.kind == EK_CASE);
            if (value == ce.value)
                return ce;
        }
        return e.body[lastj];
    }

    static private int parseInt(Layout.Element e, byte[] bytes, int pos, int[] buf) {
        int value = 0;
        int loBits = e.len * 8;
        for (int bitPos = loBits; (bitPos -= 8) >= 0; ) {
            value += (bytes[pos++] & 0xFF) << bitPos;
        }
        if (loBits < 32 && e.flagTest(EF_SIGN)) {
            int hiBits = 32 - loBits;
            value = (value << hiBits) >> hiBits;
        }
        buf[0] = value;
        return pos;
    }

    static void unparseUsing(Layout.Element[] elems, Object[] fixups, ValueStream in, ByteArrayOutputStream out) {
        int prevBCI = 0;
        int prevRBCI = 0;
        for (int i = 0; i < elems.length; i++) {
            Layout.Element e = elems[i];
            int bandIndex = e.bandIndex;
            int value;
            int BCI, RBCI;
            switch(e.kind) {
                case EK_INT:
                    value = in.getInt(bandIndex);
                    unparseInt(e, value, out);
                    break;
                case EK_BCI:
                    value = in.getInt(bandIndex);
                    if (!e.flagTest(EF_DELTA)) {
                        RBCI = value;
                    } else {
                        RBCI = prevRBCI + value;
                    }
                    assert (prevBCI == in.decodeBCI(prevRBCI));
                    BCI = in.decodeBCI(RBCI);
                    unparseInt(e, BCI, out);
                    prevBCI = BCI;
                    prevRBCI = RBCI;
                    break;
                case EK_BCO:
                    value = in.getInt(bandIndex);
                    assert (e.flagTest(EF_DELTA));
                    assert (prevBCI == in.decodeBCI(prevRBCI));
                    RBCI = prevRBCI + value;
                    BCI = in.decodeBCI(RBCI);
                    unparseInt(e, BCI - prevBCI, out);
                    prevBCI = BCI;
                    prevRBCI = RBCI;
                    break;
                case EK_FLAG:
                    value = in.getInt(bandIndex);
                    unparseInt(e, value, out);
                    break;
                case EK_REPL:
                    value = in.getInt(bandIndex);
                    unparseInt(e, value, out);
                    for (int j = 0; j < value; j++) {
                        unparseUsing(e.body, fixups, in, out);
                    }
                    break;
                case EK_UN:
                    value = in.getInt(bandIndex);
                    unparseInt(e, value, out);
                    Layout.Element ce = matchCase(e, value);
                    unparseUsing(ce.body, fixups, in, out);
                    break;
                case EK_CALL:
                    assert (e.body.length == 1);
                    assert (e.body[0].kind == EK_CBLE);
                    unparseUsing(e.body[0].body, fixups, in, out);
                    break;
                case EK_REF:
                    Entry globalRef = in.getRef(bandIndex);
                    int localRef;
                    if (globalRef != null) {
                        fixups[0] = Fixups.add(fixups[0], null, out.size(), Fixups.U2_FORMAT, globalRef);
                        localRef = 0;
                    } else {
                        localRef = 0;
                    }
                    unparseInt(e, localRef, out);
                    break;
                default:
                    assert (false);
                    continue;
            }
        }
    }

    static private void unparseInt(Layout.Element e, int value, ByteArrayOutputStream out) {
        int loBits = e.len * 8;
        if (loBits == 0) {
            return;
        }
        if (loBits < 32) {
            int hiBits = 32 - loBits;
            int codedValue;
            if (e.flagTest(EF_SIGN))
                codedValue = (value << hiBits) >> hiBits;
            else
                codedValue = (value << hiBits) >>> hiBits;
            if (codedValue != value)
                throw new InternalError("cannot code in " + e.len + " bytes: " + value);
        }
        for (int bitPos = loBits; (bitPos -= 8) >= 0; ) {
            out.write((byte) (value >>> bitPos));
        }
    }
}
