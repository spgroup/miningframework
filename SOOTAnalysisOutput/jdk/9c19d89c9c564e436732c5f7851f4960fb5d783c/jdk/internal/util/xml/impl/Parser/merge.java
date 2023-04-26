package jdk.internal.util.xml.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import jdk.internal.org.xml.sax.InputSource;
import jdk.internal.org.xml.sax.SAXException;

public abstract class Parser {

    public static final String FAULT = "";

    protected static final int BUFFSIZE_READER = 512;

    protected static final int BUFFSIZE_PARSER = 128;

    private static final int MAX_ARRAY_SIZE = 1024 << 16;

    public static final char EOS = 0xffff;

    private Pair mNoNS;

    private Pair mXml;

    private Map<String, Input> mEnt;

    private Map<String, Input> mPEnt;

    protected boolean mIsSAlone;

    protected boolean mIsSAloneSet;

    protected boolean mIsNSAware;

    protected int mPh;

    protected static final int PH_BEFORE_DOC = -1;

    protected static final int PH_DOC_START = 0;

    protected static final int PH_MISC_DTD = 1;

    protected static final int PH_DTD = 2;

    protected static final int PH_DTD_MISC = 3;

    protected static final int PH_DOCELM = 4;

    protected static final int PH_DOCELM_MISC = 5;

    protected static final int PH_AFTER_DOC = 6;

    protected int mEvt;

    protected static final int EV_NULL = 0;

    protected static final int EV_ELM = 1;

    protected static final int EV_ELMS = 2;

    protected static final int EV_ELME = 3;

    protected static final int EV_TEXT = 4;

    protected static final int EV_WSPC = 5;

    protected static final int EV_PI = 6;

    protected static final int EV_CDAT = 7;

    protected static final int EV_COMM = 8;

    protected static final int EV_DTD = 9;

    protected static final int EV_ENT = 10;

    private char mESt;

    protected char[] mBuff;

    protected int mBuffIdx;

    protected Pair mPref;

    protected Pair mElm;

    protected Pair mAttL;

    protected Input mDoc;

    protected Input mInp;

    private char[] mChars;

    private int mChLen;

    private int mChIdx;

    protected Attrs mAttrs;

    private String[] mItems;

    private char mAttrIdx;

    private String mUnent;

    private Pair mDltd;

    private static final char[] NONS;

    private static final char[] XML;

    private static final char[] XMLNS;

    static {
        NONS = new char[1];
        NONS[0] = (char) 0;
        XML = new char[4];
        XML[0] = (char) 4;
        XML[1] = 'x';
        XML[2] = 'm';
        XML[3] = 'l';
        XMLNS = new char[6];
        XMLNS[0] = (char) 6;
        XMLNS[1] = 'x';
        XMLNS[2] = 'm';
        XMLNS[3] = 'l';
        XMLNS[4] = 'n';
        XMLNS[5] = 's';
    }

    private static final byte[] asctyp;

    private static final byte[] nmttyp;

    static {
        short i = 0;
        asctyp = new byte[0x80];
        while (i < ' ') {
            asctyp[i++] = (byte) 'z';
        }
        asctyp['\t'] = (byte) ' ';
        asctyp['\r'] = (byte) ' ';
        asctyp['\n'] = (byte) ' ';
        while (i < '0') {
            asctyp[i] = (byte) i++;
        }
        while (i <= '9') {
            asctyp[i++] = (byte) 'd';
        }
        while (i < 'A') {
            asctyp[i] = (byte) i++;
        }
        while (i <= 'Z') {
            asctyp[i++] = (byte) 'A';
        }
        while (i < 'a') {
            asctyp[i] = (byte) i++;
        }
        while (i <= 'z') {
            asctyp[i++] = (byte) 'a';
        }
        while (i < 0x80) {
            asctyp[i] = (byte) i++;
        }
        nmttyp = new byte[0x80];
        for (i = 0; i < '0'; i++) {
            nmttyp[i] = (byte) 0xff;
        }
        while (i <= '9') {
            nmttyp[i++] = (byte) 2;
        }
        while (i < 'A') {
            nmttyp[i++] = (byte) 0xff;
        }
        for (i = '['; i < 'a'; i++) {
            nmttyp[i] = (byte) 0xff;
        }
        for (i = '{'; i < 0x80; i++) {
            nmttyp[i] = (byte) 0xff;
        }
        nmttyp['_'] = 0;
        nmttyp[':'] = 1;
        nmttyp['.'] = 2;
        nmttyp['-'] = 2;
        nmttyp[' '] = 3;
        nmttyp['\t'] = 3;
        nmttyp['\r'] = 3;
        nmttyp['\n'] = 3;
    }

    protected Parser() {
        mPh = PH_BEFORE_DOC;
        mBuff = new char[BUFFSIZE_PARSER];
        mAttrs = new Attrs();
        mPref = pair(mPref);
        mPref.name = "";
        mPref.value = "";
        mPref.chars = NONS;
        mNoNS = mPref;
        mPref = pair(mPref);
        mPref.name = "xml";
        mPref.value = "http://www.w3.org/XML/1998/namespace";
        mPref.chars = XML;
        mXml = mPref;
    }

    protected void init() {
        mUnent = null;
        mElm = null;
        mPref = mXml;
        mAttL = null;
        mPEnt = new HashMap<>();
        mEnt = new HashMap<>();
        mDoc = mInp;
        mChars = mInp.chars;
        mPh = PH_DOC_START;
    }

    protected void cleanup() {
        while (mAttL != null) {
            while (mAttL.list != null) {
                if (mAttL.list.list != null) {
                    del(mAttL.list.list);
                }
                mAttL.list = del(mAttL.list);
            }
            mAttL = del(mAttL);
        }
        while (mElm != null) {
            mElm = del(mElm);
        }
        while (mPref != mXml) {
            mPref = del(mPref);
        }
        while (mInp != null) {
            pop();
        }
        if ((mDoc != null) && (mDoc.src != null)) {
            try {
                mDoc.src.close();
            } catch (IOException ioe) {
            }
        }
        mPEnt = null;
        mEnt = null;
        mDoc = null;
        mPh = PH_AFTER_DOC;
    }

    @SuppressWarnings("fallthrough")
    protected int step() throws Exception {
        mEvt = EV_NULL;
        int st = 0;
        while (mEvt == EV_NULL) {
            char ch = (mChIdx < mChLen) ? mChars[mChIdx++] : getch();
            switch(st) {
                case 0:
                    if (ch != '<') {
                        bkch();
                        mBuffIdx = -1;
                        st = 1;
                        break;
                    }
                    switch(getch()) {
                        case '/':
                            mEvt = EV_ELME;
                            if (mElm == null) {
                                panic(FAULT);
                            }
                            mBuffIdx = -1;
                            bname(mIsNSAware);
                            char[] chars = mElm.chars;
                            if (chars.length == (mBuffIdx + 1)) {
                                for (char i = 1; i <= mBuffIdx; i += 1) {
                                    if (chars[i] != mBuff[i]) {
                                        panic(FAULT);
                                    }
                                }
                            } else {
                                panic(FAULT);
                            }
                            if (wsskip() != '>') {
                                panic(FAULT);
                            }
                            getch();
                            break;
                        case '!':
                            ch = getch();
                            bkch();
                            switch(ch) {
                                case '-':
                                    mEvt = EV_COMM;
                                    comm();
                                    break;
                                case '[':
                                    mEvt = EV_CDAT;
                                    cdat();
                                    break;
                                default:
                                    mEvt = EV_DTD;
                                    dtd();
                                    break;
                            }
                            break;
                        case '?':
                            mEvt = EV_PI;
                            pi();
                            break;
                        default:
                            bkch();
                            mElm = pair(mElm);
                            mElm.chars = qname(mIsNSAware);
                            mElm.name = mElm.local();
                            mElm.id = (mElm.next != null) ? mElm.next.id : 0;
                            mElm.num = 0;
                            Pair elm = find(mAttL, mElm.chars);
                            mElm.list = (elm != null) ? elm.list : null;
                            mAttrIdx = 0;
                            Pair att = pair(null);
                            att.num = 0;
                            attr(att);
                            del(att);
                            mElm.value = (mIsNSAware) ? rslv(mElm.chars) : null;
                            switch(wsskip()) {
                                case '>':
                                    getch();
                                    mEvt = EV_ELMS;
                                    break;
                                case '/':
                                    getch();
                                    if (getch() != '>') {
                                        panic(FAULT);
                                    }
                                    mEvt = EV_ELM;
                                    break;
                                default:
                                    panic(FAULT);
                            }
                            break;
                    }
                    break;
                case 1:
                    switch(ch) {
                        case ' ':
                        case '\t':
                        case '\n':
                            bappend(ch);
                            break;
                        case '\r':
                            if (getch() != '\n') {
                                bkch();
                            }
                            bappend('\n');
                            break;
                        case '<':
                            mEvt = EV_WSPC;
                            bkch();
                            bflash_ws();
                            break;
                        default:
                            bkch();
                            st = 2;
                            break;
                    }
                    break;
                case 2:
                    switch(ch) {
                        case '&':
                            if (mUnent == null) {
                                if ((mUnent = ent('x')) != null) {
                                    mEvt = EV_TEXT;
                                    bkch();
                                    setch('&');
                                    bflash();
                                }
                            } else {
                                mEvt = EV_ENT;
                                skippedEnt(mUnent);
                                mUnent = null;
                            }
                            break;
                        case '<':
                            mEvt = EV_TEXT;
                            bkch();
                            bflash();
                            break;
                        case '\r':
                            if (getch() != '\n') {
                                bkch();
                            }
                            bappend('\n');
                            break;
                        case EOS:
                            panic(FAULT);
                        default:
                            bappend(ch);
                            break;
                    }
                    break;
                default:
                    panic(FAULT);
            }
        }
        return mEvt;
    }

    private void dtd() throws Exception {
        char ch;
        String str = null;
        String name = null;
        Pair psid = null;
        if ("DOCTYPE".equals(name(false)) != true) {
            panic(FAULT);
        }
        mPh = PH_DTD;
        for (short st = 0; st >= 0; ) {
            ch = getch();
            if (ch == EOS) {
                panic(FAULT);
            }
            switch(st) {
                case 0:
                    if (chtyp(ch) != ' ') {
                        bkch();
                        name = name(mIsNSAware);
                        wsskip();
                        st = 1;
                    }
                    break;
                case 1:
                    switch(chtyp(ch)) {
                        case 'A':
                            bkch();
                            psid = pubsys(' ');
                            st = 2;
                            docType(name, psid.name, psid.value);
                            break;
                        case '[':
                            bkch();
                            st = 2;
                            docType(name, null, null);
                            break;
                        case '>':
                            bkch();
                            st = 3;
                            docType(name, null, null);
                            break;
                        default:
                            panic(FAULT);
                    }
                    break;
                case 2:
                    switch(chtyp(ch)) {
                        case '[':
                            dtdsub();
                            st = 3;
                            break;
                        case '>':
                            bkch();
                            st = 3;
                            break;
                        case ' ':
                            break;
                        default:
                            panic(FAULT);
                    }
                    break;
                case 3:
                    switch(chtyp(ch)) {
                        case '>':
                            if (psid != null) {
                                InputSource is = resolveEnt(name, psid.name, psid.value);
                                if (is != null) {
                                    if (mIsSAlone == false) {
                                        bkch();
                                        setch(']');
                                        push(new Input(BUFFSIZE_READER));
                                        setinp(is);
                                        mInp.pubid = psid.name;
                                        mInp.sysid = psid.value;
                                        dtdsub();
                                    } else {
                                        skippedEnt("[dtd]");
                                        if (is.getCharacterStream() != null) {
                                            try {
                                                is.getCharacterStream().close();
                                            } catch (IOException ioe) {
                                            }
                                        }
                                        if (is.getByteStream() != null) {
                                            try {
                                                is.getByteStream().close();
                                            } catch (IOException ioe) {
                                            }
                                        }
                                    }
                                } else {
                                    skippedEnt("[dtd]");
                                }
                                del(psid);
                            }
                            st = -1;
                            break;
                        case ' ':
                            break;
                        default:
                            panic(FAULT);
                    }
                    break;
                default:
                    panic(FAULT);
            }
        }
    }

    private void dtdsub() throws Exception {
        startInternalSub();
        char ch;
        for (short st = 0; st >= 0; ) {
            ch = getch();
            switch(st) {
                case 0:
                    switch(chtyp(ch)) {
                        case '<':
                            ch = getch();
                            switch(ch) {
                                case '?':
                                    pi();
                                    break;
                                case '!':
                                    ch = getch();
                                    bkch();
                                    if (ch == '-') {
                                        comm();
                                        break;
                                    }
                                    bntok();
                                    switch(bkeyword()) {
                                        case 'n':
                                            dtdent();
                                            break;
                                        case 'a':
                                            dtdattl();
                                            break;
                                        case 'e':
                                            dtdelm();
                                            break;
                                        case 'o':
                                            dtdnot();
                                            break;
                                        default:
                                            panic(FAULT);
                                            break;
                                    }
                                    st = 1;
                                    break;
                                default:
                                    panic(FAULT);
                                    break;
                            }
                            break;
                        case '%':
                            pent(' ');
                            break;
                        case ']':
                            st = -1;
                            break;
                        case ' ':
                            break;
                        case 'Z':
                            if (getch() != ']') {
                                panic(FAULT);
                            }
                            st = -1;
                            break;
                        default:
                            panic(FAULT);
                    }
                    break;
                case 1:
                    switch(ch) {
                        case '>':
                            st = 0;
                            break;
                        case ' ':
                        case '\n':
                        case '\r':
                        case '\t':
                            break;
                        default:
                            panic(FAULT);
                            break;
                    }
                    break;
                default:
                    panic(FAULT);
            }
        }
    }

    @SuppressWarnings("fallthrough")
    private void dtdent() throws Exception {
        String str = null;
        char[] val = null;
        Input inp = null;
        Pair ids = null;
        char ch;
        for (short st = 0; st >= 0; ) {
            ch = getch();
            switch(st) {
                case 0:
                    switch(chtyp(ch)) {
                        case ' ':
                            break;
                        case '%':
                            ch = getch();
                            bkch();
                            if (chtyp(ch) == ' ') {
                                wsskip();
                                str = name(false);
                                switch(chtyp(wsskip())) {
                                    case 'A':
                                        ids = pubsys(' ');
                                        if (wsskip() == '>') {
                                            if (mPEnt.containsKey(str) == false) {
                                                inp = new Input();
                                                inp.pubid = ids.name;
                                                inp.sysid = ids.value;
                                                mPEnt.put(str, inp);
                                            }
                                        } else {
                                            panic(FAULT);
                                        }
                                        del(ids);
                                        st = -1;
                                        break;
                                    case '\"':
                                    case '\'':
                                        bqstr('d');
                                        val = new char[mBuffIdx + 1];
                                        System.arraycopy(mBuff, 1, val, 1, val.length - 1);
                                        val[0] = ' ';
                                        if (mPEnt.containsKey(str) == false) {
                                            inp = new Input(val);
                                            inp.pubid = mInp.pubid;
                                            inp.sysid = mInp.sysid;
                                            inp.xmlenc = mInp.xmlenc;
                                            inp.xmlver = mInp.xmlver;
                                            mPEnt.put(str, inp);
                                        }
                                        st = -1;
                                        break;
                                    default:
                                        panic(FAULT);
                                        break;
                                }
                            } else {
                                pent(' ');
                            }
                            break;
                        default:
                            bkch();
                            str = name(false);
                            st = 1;
                            break;
                    }
                    break;
                case 1:
                    switch(chtyp(ch)) {
                        case '\"':
                        case '\'':
                            bkch();
                            bqstr('d');
                            if (mEnt.get(str) == null) {
                                val = new char[mBuffIdx];
                                System.arraycopy(mBuff, 1, val, 0, val.length);
                                if (mEnt.containsKey(str) == false) {
                                    inp = new Input(val);
                                    inp.pubid = mInp.pubid;
                                    inp.sysid = mInp.sysid;
                                    inp.xmlenc = mInp.xmlenc;
                                    inp.xmlver = mInp.xmlver;
                                    mEnt.put(str, inp);
                                }
                            }
                            st = -1;
                            break;
                        case 'A':
                            bkch();
                            ids = pubsys(' ');
                            switch(wsskip()) {
                                case '>':
                                    if (mEnt.containsKey(str) == false) {
                                        inp = new Input();
                                        inp.pubid = ids.name;
                                        inp.sysid = ids.value;
                                        mEnt.put(str, inp);
                                    }
                                    break;
                                case 'N':
                                    if ("NDATA".equals(name(false)) == true) {
                                        wsskip();
                                        unparsedEntDecl(str, ids.name, ids.value, name(false));
                                        break;
                                    }
                                default:
                                    panic(FAULT);
                                    break;
                            }
                            del(ids);
                            st = -1;
                            break;
                        case ' ':
                            break;
                        default:
                            panic(FAULT);
                            break;
                    }
                    break;
                default:
                    panic(FAULT);
            }
        }
    }

    @SuppressWarnings("fallthrough")
    private void dtdelm() throws Exception {
        wsskip();
        name(mIsNSAware);
        char ch;
        while (true) {
            ch = getch();
            switch(ch) {
                case '>':
                    bkch();
                    return;
                case EOS:
                    panic(FAULT);
                default:
                    break;
            }
        }
    }

    private void dtdattl() throws Exception {
        char[] elmqn = null;
        Pair elm = null;
        char ch;
        for (short st = 0; st >= 0; ) {
            ch = getch();
            switch(st) {
                case 0:
                    switch(chtyp(ch)) {
                        case 'a':
                        case 'A':
                        case '_':
                        case 'X':
                        case ':':
                            bkch();
                            elmqn = qname(mIsNSAware);
                            elm = find(mAttL, elmqn);
                            if (elm == null) {
                                elm = pair(mAttL);
                                elm.chars = elmqn;
                                mAttL = elm;
                            }
                            st = 1;
                            break;
                        case ' ':
                            break;
                        case '%':
                            pent(' ');
                            break;
                        default:
                            panic(FAULT);
                            break;
                    }
                    break;
                case 1:
                    switch(chtyp(ch)) {
                        case 'a':
                        case 'A':
                        case '_':
                        case 'X':
                        case ':':
                            bkch();
                            dtdatt(elm);
                            if (wsskip() == '>') {
                                return;
                            }
                            break;
                        case ' ':
                            break;
                        case '%':
                            pent(' ');
                            break;
                        default:
                            panic(FAULT);
                            break;
                    }
                    break;
                default:
                    panic(FAULT);
                    break;
            }
        }
    }

    @SuppressWarnings("fallthrough")
    private void dtdatt(Pair elm) throws Exception {
        char[] attqn = null;
        Pair att = null;
        char ch;
        for (short st = 0; st >= 0; ) {
            ch = getch();
            switch(st) {
                case 0:
                    switch(chtyp(ch)) {
                        case 'a':
                        case 'A':
                        case '_':
                        case 'X':
                        case ':':
                            bkch();
                            attqn = qname(mIsNSAware);
                            att = find(elm.list, attqn);
                            if (att == null) {
                                att = pair(elm.list);
                                att.chars = attqn;
                                elm.list = att;
                            } else {
                                att = pair(null);
                                att.chars = attqn;
                                att.id = 'c';
                            }
                            wsskip();
                            st = 1;
                            break;
                        case '%':
                            pent(' ');
                            break;
                        case ' ':
                            break;
                        default:
                            panic(FAULT);
                            break;
                    }
                    break;
                case 1:
                    switch(chtyp(ch)) {
                        case '(':
                            att.id = 'u';
                            st = 2;
                            break;
                        case '%':
                            pent(' ');
                            break;
                        case ' ':
                            break;
                        default:
                            bkch();
                            bntok();
                            att.id = bkeyword();
                            switch(att.id) {
                                case 'o':
                                    if (wsskip() != '(') {
                                        panic(FAULT);
                                    }
                                    ch = getch();
                                    st = 2;
                                    break;
                                case 'i':
                                case 'r':
                                case 'R':
                                case 'n':
                                case 'N':
                                case 't':
                                case 'T':
                                case 'c':
                                    wsskip();
                                    st = 4;
                                    break;
                                default:
                                    panic(FAULT);
                                    break;
                            }
                            break;
                    }
                    break;
                case 2:
                    switch(chtyp(ch)) {
                        case 'a':
                        case 'A':
                        case 'd':
                        case '.':
                        case ':':
                        case '-':
                        case '_':
                        case 'X':
                            bkch();
                            switch(att.id) {
                                case 'u':
                                    bntok();
                                    break;
                                case 'o':
                                    mBuffIdx = -1;
                                    bname(false);
                                    break;
                                default:
                                    panic(FAULT);
                                    break;
                            }
                            wsskip();
                            st = 3;
                            break;
                        case '%':
                            pent(' ');
                            break;
                        case ' ':
                            break;
                        default:
                            panic(FAULT);
                            break;
                    }
                    break;
                case 3:
                    switch(ch) {
                        case ')':
                            wsskip();
                            st = 4;
                            break;
                        case '|':
                            wsskip();
                            switch(att.id) {
                                case 'u':
                                    bntok();
                                    break;
                                case 'o':
                                    mBuffIdx = -1;
                                    bname(false);
                                    break;
                                default:
                                    panic(FAULT);
                                    break;
                            }
                            wsskip();
                            break;
                        case '%':
                            pent(' ');
                            break;
                        default:
                            panic(FAULT);
                            break;
                    }
                    break;
                case 4:
                    switch(ch) {
                        case '#':
                            bntok();
                            switch(bkeyword()) {
                                case 'F':
                                    switch(wsskip()) {
                                        case '\"':
                                        case '\'':
                                            st = 5;
                                            break;
                                        case EOS:
                                            panic(FAULT);
                                        default:
                                            st = -1;
                                            break;
                                    }
                                    break;
                                case 'Q':
                                case 'I':
                                    st = -1;
                                    break;
                                default:
                                    panic(FAULT);
                                    break;
                            }
                            break;
                        case '\"':
                        case '\'':
                            bkch();
                            st = 5;
                            break;
                        case ' ':
                        case '\n':
                        case '\r':
                        case '\t':
                            break;
                        case '%':
                            pent(' ');
                            break;
                        default:
                            bkch();
                            st = -1;
                            break;
                    }
                    break;
                case 5:
                    switch(ch) {
                        case '\"':
                        case '\'':
                            bkch();
                            bqstr('d');
                            att.list = pair(null);
                            att.list.chars = new char[att.chars.length + mBuffIdx + 3];
                            System.arraycopy(att.chars, 1, att.list.chars, 0, att.chars.length - 1);
                            att.list.chars[att.chars.length - 1] = '=';
                            att.list.chars[att.chars.length] = ch;
                            System.arraycopy(mBuff, 1, att.list.chars, att.chars.length + 1, mBuffIdx);
                            att.list.chars[att.chars.length + mBuffIdx + 1] = ch;
                            att.list.chars[att.chars.length + mBuffIdx + 2] = ' ';
                            st = -1;
                            break;
                        default:
                            panic(FAULT);
                            break;
                    }
                    break;
                default:
                    panic(FAULT);
                    break;
            }
        }
    }

    private void dtdnot() throws Exception {
        wsskip();
        String name = name(false);
        wsskip();
        Pair ids = pubsys('N');
        notDecl(name, ids.name, ids.value);
        del(ids);
    }

    @SuppressWarnings("fallthrough")
    private void attr(Pair att) throws Exception {
        switch(wsskip()) {
            case '/':
            case '>':
                if ((att.num & 0x2) == 0) {
                    att.num |= 0x2;
                    Input inp = mInp;
                    for (Pair def = mElm.list; def != null; def = def.next) {
                        if (def.list == null) {
                            continue;
                        }
                        Pair act = find(att.next, def.chars);
                        if (act == null) {
                            push(new Input(def.list.chars));
                        }
                    }
                    if (mInp != inp) {
                        attr(att);
                        return;
                    }
                }
                mAttrs.setLength(mAttrIdx);
                mItems = mAttrs.mItems;
                return;
            case EOS:
                panic(FAULT);
            default:
                att.chars = qname(mIsNSAware);
                att.name = att.local();
                String type = atype(att);
                wsskip();
                if (getch() != '=') {
                    panic(FAULT);
                }
                bqstr((char) att.id);
                String val = new String(mBuff, 1, mBuffIdx);
                Pair next = pair(att);
                next.num = (att.num & ~0x1);
                if ((mIsNSAware == false) || (isdecl(att, val) == false)) {
                    mAttrIdx++;
                    attr(next);
                    mAttrIdx--;
                    char idx = (char) (mAttrIdx << 3);
                    mItems[idx + 1] = att.qname();
                    mItems[idx + 2] = (mIsNSAware) ? att.name : "";
                    mItems[idx + 3] = val;
                    mItems[idx + 4] = type;
                    switch(att.num & 0x3) {
                        case 0x0:
                            mItems[idx + 5] = null;
                            break;
                        case 0x1:
                            mItems[idx + 5] = "d";
                            break;
                        default:
                            mItems[idx + 5] = "D";
                            break;
                    }
                    mItems[idx + 0] = (att.chars[0] != 0) ? rslv(att.chars) : "";
                } else {
                    newPrefix();
                    attr(next);
                }
                del(next);
                break;
        }
    }

    private String atype(Pair att) throws Exception {
        Pair attr;
        att.id = 'c';
        if (mElm.list == null || (attr = find(mElm.list, att.chars)) == null) {
            return "CDATA";
        }
        att.num |= 0x1;
        att.id = 'i';
        switch(attr.id) {
            case 'i':
                return "ID";
            case 'r':
                return "IDREF";
            case 'R':
                return "IDREFS";
            case 'n':
                return "ENTITY";
            case 'N':
                return "ENTITIES";
            case 't':
                return "NMTOKEN";
            case 'T':
                return "NMTOKENS";
            case 'u':
                return "NMTOKEN";
            case 'o':
                return "NOTATION";
            case 'c':
                att.id = 'c';
                return "CDATA";
            default:
                panic(FAULT);
        }
        return null;
    }

    @SuppressWarnings("fallthrough")
    private void comm() throws Exception {
        if (mPh == PH_DOC_START) {
            mPh = PH_MISC_DTD;
        }
        char ch;
        mBuffIdx = -1;
        for (short st = 0; st >= 0; ) {
            ch = (mChIdx < mChLen) ? mChars[mChIdx++] : getch();
            if (ch == EOS) {
                panic(FAULT);
            }
            switch(st) {
                case 0:
                    if (ch == '-') {
                        st = 1;
                    } else {
                        panic(FAULT);
                    }
                    break;
                case 1:
                    if (ch == '-') {
                        st = 2;
                    } else {
                        panic(FAULT);
                    }
                    break;
                case 2:
                    switch(ch) {
                        case '-':
                            st = 3;
                            break;
                        default:
                            bappend(ch);
                            break;
                    }
                    break;
                case 3:
                    switch(ch) {
                        case '-':
                            st = 4;
                            break;
                        default:
                            bappend('-');
                            bappend(ch);
                            st = 2;
                            break;
                    }
                    break;
                case 4:
                    if (ch == '>') {
                        comm(mBuff, mBuffIdx + 1);
                        st = -1;
                        break;
                    }
                default:
                    panic(FAULT);
            }
        }
    }

    private void pi() throws Exception {
        char ch;
        String str = null;
        mBuffIdx = -1;
        for (short st = 0; st >= 0; ) {
            ch = getch();
            if (ch == EOS) {
                panic(FAULT);
            }
            switch(st) {
                case 0:
                    switch(chtyp(ch)) {
                        case 'a':
                        case 'A':
                        case '_':
                        case ':':
                        case 'X':
                            bkch();
                            str = name(false);
                            if ((str.isEmpty()) || (mXml.name.equals(str.toLowerCase()) == true)) {
                                panic(FAULT);
                            }
                            if (mPh == PH_DOC_START) {
                                mPh = PH_MISC_DTD;
                            }
                            wsskip();
                            st = 1;
                            mBuffIdx = -1;
                            break;
                        default:
                            panic(FAULT);
                    }
                    break;
                case 1:
                    switch(ch) {
                        case '?':
                            st = 2;
                            break;
                        default:
                            bappend(ch);
                            break;
                    }
                    break;
                case 2:
                    switch(ch) {
                        case '>':
                            pi(str, new String(mBuff, 0, mBuffIdx + 1));
                            st = -1;
                            break;
                        case '?':
                            bappend('?');
                            break;
                        default:
                            bappend('?');
                            bappend(ch);
                            st = 1;
                            break;
                    }
                    break;
                default:
                    panic(FAULT);
            }
        }
    }

    private void cdat() throws Exception {
        char ch;
        mBuffIdx = -1;
        for (short st = 0; st >= 0; ) {
            ch = getch();
            if (ch == EOS) {
                panic(FAULT);
            }
            switch(st) {
                case 0:
                    if (ch == '[') {
                        st = 1;
                    } else {
                        panic(FAULT);
                    }
                    break;
                case 1:
                    if (chtyp(ch) == 'A') {
                        bappend(ch);
                    } else {
                        if ("CDATA".equals(new String(mBuff, 0, mBuffIdx + 1)) != true) {
                            panic(FAULT);
                        }
                        bkch();
                        st = 2;
                    }
                    break;
                case 2:
                    if (ch != '[') {
                        panic(FAULT);
                    }
                    mBuffIdx = -1;
                    st = 3;
                    break;
                case 3:
                    if (ch != ']') {
                        bappend(ch);
                    } else {
                        st = 4;
                    }
                    break;
                case 4:
                    if (ch != ']') {
                        bappend(']');
                        bappend(ch);
                        st = 3;
                    } else {
                        st = 5;
                    }
                    break;
                case 5:
                    switch(ch) {
                        case ']':
                            bappend(']');
                            break;
                        case '>':
                            bflash();
                            st = -1;
                            break;
                        default:
                            bappend(']');
                            bappend(']');
                            bappend(ch);
                            st = 3;
                            break;
                    }
                    break;
                default:
                    panic(FAULT);
            }
        }
    }

    protected String name(boolean ns) throws Exception {
        mBuffIdx = -1;
        bname(ns);
        return new String(mBuff, 1, mBuffIdx);
    }

    protected char[] qname(boolean ns) throws Exception {
        mBuffIdx = -1;
        bname(ns);
        char[] chars = new char[mBuffIdx + 1];
        System.arraycopy(mBuff, 0, chars, 0, mBuffIdx + 1);
        return chars;
    }

    private void pubsys(Input inp) throws Exception {
        Pair pair = pubsys(' ');
        inp.pubid = pair.name;
        inp.sysid = pair.value;
        del(pair);
    }

    @SuppressWarnings("fallthrough")
    private Pair pubsys(char flag) throws Exception {
        Pair ids = pair(null);
        String str = name(false);
        if ("PUBLIC".equals(str) == true) {
            bqstr('i');
            ids.name = new String(mBuff, 1, mBuffIdx);
            switch(wsskip()) {
                case '\"':
                case '\'':
                    bqstr(' ');
                    ids.value = new String(mBuff, 1, mBuffIdx);
                    break;
                case EOS:
                    panic(FAULT);
                default:
                    if (flag != 'N') {
                        panic(FAULT);
                    }
                    ids.value = null;
                    break;
            }
            return ids;
        } else if ("SYSTEM".equals(str) == true) {
            ids.name = null;
            bqstr(' ');
            ids.value = new String(mBuff, 1, mBuffIdx);
            return ids;
        }
        panic(FAULT);
        return null;
    }

    protected String eqstr(char flag) throws Exception {
        if (flag == '=') {
            wsskip();
            if (getch() != '=') {
                panic(FAULT);
            }
        }
        bqstr((flag == '=') ? '-' : flag);
        return new String(mBuff, 1, mBuffIdx);
    }

    @SuppressWarnings("fallthrough")
    private String ent(char flag) throws Exception {
        char ch;
        int idx = mBuffIdx + 1;
        Input inp = null;
        String str = null;
        mESt = 0x100;
        bappend('&');
        for (short st = 0; st >= 0; ) {
            ch = (mChIdx < mChLen) ? mChars[mChIdx++] : getch();
            switch(st) {
                case 0:
                case 1:
                    switch(chtyp(ch)) {
                        case 'd':
                        case '.':
                        case '-':
                            if (st != 1) {
                                panic(FAULT);
                            }
                        case 'a':
                        case 'A':
                        case '_':
                        case 'X':
                            bappend(ch);
                            eappend(ch);
                            st = 1;
                            break;
                        case ':':
                            if (mIsNSAware != false) {
                                panic(FAULT);
                            }
                            bappend(ch);
                            eappend(ch);
                            st = 1;
                            break;
                        case ';':
                            if (mESt < 0x100) {
                                mBuffIdx = idx - 1;
                                bappend(mESt);
                                st = -1;
                                break;
                            } else if (mPh == PH_DTD) {
                                bappend(';');
                                st = -1;
                                break;
                            }
                            str = new String(mBuff, idx + 1, mBuffIdx - idx);
                            inp = mEnt.get(str);
                            mBuffIdx = idx - 1;
                            if (inp != null) {
                                if (inp.chars == null) {
                                    InputSource is = resolveEnt(str, inp.pubid, inp.sysid);
                                    if (is != null) {
                                        push(new Input(BUFFSIZE_READER));
                                        setinp(is);
                                        mInp.pubid = inp.pubid;
                                        mInp.sysid = inp.sysid;
                                        str = null;
                                    } else {
                                        if (flag != 'x') {
                                            panic(FAULT);
                                        }
                                    }
                                } else {
                                    push(inp);
                                    str = null;
                                }
                            } else {
                                if (flag != 'x') {
                                    panic(FAULT);
                                }
                            }
                            st = -1;
                            break;
                        case '#':
                            if (st != 0) {
                                panic(FAULT);
                            }
                            st = 2;
                            break;
                        default:
                            panic(FAULT);
                    }
                    break;
                case 2:
                    switch(chtyp(ch)) {
                        case 'd':
                            bappend(ch);
                            break;
                        case ';':
                            try {
                                int i = Integer.parseInt(new String(mBuff, idx + 1, mBuffIdx - idx), 10);
                                mBuffIdx = idx - 1;
                                for (char character : Character.toChars(i)) {
                                    if (character == ' ' || mInp.next != null) {
                                        bappend(character, flag);
                                    } else {
                                        bappend(character);
                                    }
                                }
                            } catch (NumberFormatException nfe) {
                                panic(FAULT);
                            }
                            st = -1;
                            break;
                        case 'a':
                            if ((mBuffIdx == idx) && (ch == 'x')) {
                                st = 3;
                                break;
                            }
                        default:
                            panic(FAULT);
                    }
                    break;
                case 3:
                    switch(chtyp(ch)) {
                        case 'A':
                        case 'a':
                        case 'd':
                            bappend(ch);
                            break;
                        case ';':
                            try {
                                int i = Integer.parseInt(new String(mBuff, idx + 1, mBuffIdx - idx), 16);
                                mBuffIdx = idx - 1;
                                for (char character : Character.toChars(i)) {
                                    if (character == ' ' || mInp.next != null) {
                                        bappend(character, flag);
                                    } else {
                                        bappend(character);
                                    }
                                }
                            } catch (NumberFormatException nfe) {
                                panic(FAULT);
                            }
                            st = -1;
                            break;
                        default:
                            panic(FAULT);
                    }
                    break;
                default:
                    panic(FAULT);
            }
        }
        return str;
    }

    @SuppressWarnings("fallthrough")
    private void pent(char flag) throws Exception {
        char ch;
        int idx = mBuffIdx + 1;
        Input inp = null;
        String str = null;
        bappend('%');
        if (mPh != PH_DTD) {
            return;
        }
        bname(false);
        str = new String(mBuff, idx + 2, mBuffIdx - idx - 1);
        if (getch() != ';') {
            panic(FAULT);
        }
        inp = mPEnt.get(str);
        mBuffIdx = idx - 1;
        if (inp != null) {
            if (inp.chars == null) {
                InputSource is = resolveEnt(str, inp.pubid, inp.sysid);
                if (is != null) {
                    if (flag != '-') {
                        bappend(' ');
                    }
                    push(new Input(BUFFSIZE_READER));
                    setinp(is);
                    mInp.pubid = inp.pubid;
                    mInp.sysid = inp.sysid;
                } else {
                    skippedEnt("%" + str);
                }
            } else {
                if (flag == '-') {
                    inp.chIdx = 1;
                } else {
                    bappend(' ');
                    inp.chIdx = 0;
                }
                push(inp);
            }
        } else {
            skippedEnt("%" + str);
        }
    }

    private boolean isdecl(Pair name, String value) {
        if (name.chars[0] == 0) {
            if ("xmlns".equals(name.name) == true) {
                mPref = pair(mPref);
                mPref.list = mElm;
                mPref.value = value;
                mPref.name = "";
                mPref.chars = NONS;
                mElm.num++;
                return true;
            }
        } else {
            if (name.eqpref(XMLNS) == true) {
                int len = name.name.length();
                mPref = pair(mPref);
                mPref.list = mElm;
                mPref.value = value;
                mPref.name = name.name;
                mPref.chars = new char[len + 1];
                mPref.chars[0] = (char) (len + 1);
                name.name.getChars(0, len, mPref.chars, 1);
                mElm.num++;
                return true;
            }
        }
        return false;
    }

    private String rslv(char[] qname) throws Exception {
        for (Pair pref = mPref; pref != null; pref = pref.next) {
            if (pref.eqpref(qname) == true) {
                return pref.value;
            }
        }
        if (qname[0] == 1) {
            for (Pair pref = mPref; pref != null; pref = pref.next) {
                if (pref.chars[0] == 0) {
                    return pref.value;
                }
            }
        }
        panic(FAULT);
        return null;
    }

    protected char wsskip() throws IOException {
        char ch;
        while (true) {
            ch = (mChIdx < mChLen) ? mChars[mChIdx++] : getch();
            if (ch < 0x80) {
                if (nmttyp[ch] != 3) {
                    break;
                }
            } else {
                break;
            }
        }
        mChIdx--;
        return ch;
    }

    protected abstract void docType(String name, String pubid, String sysid) throws SAXException;

    public abstract void startInternalSub() throws SAXException;

    protected abstract void comm(char[] text, int length);

    protected abstract void pi(String target, String body) throws Exception;

    protected abstract void newPrefix() throws Exception;

    protected abstract void skippedEnt(String name) throws Exception;

    protected abstract InputSource resolveEnt(String name, String pubid, String sysid) throws Exception;

    protected abstract void notDecl(String name, String pubid, String sysid) throws Exception;

    protected abstract void unparsedEntDecl(String name, String pubid, String sysid, String notation) throws Exception;

    protected abstract void panic(String msg) throws Exception;

    private void bname(boolean ns) throws Exception {
        char ch;
        char type;
        mBuffIdx++;
        int bqname = mBuffIdx;
        int bcolon = bqname;
        int bchidx = bqname + 1;
        int bstart = bchidx;
        int cstart = mChIdx;
        short st = (short) ((ns == true) ? 0 : 2);
        while (true) {
            if (mChIdx >= mChLen) {
                bcopy(cstart, bstart);
                getch();
                mChIdx--;
                cstart = mChIdx;
                bstart = bchidx;
            }
            ch = mChars[mChIdx++];
            type = (char) 0;
            if (ch < 0x80) {
                type = (char) nmttyp[ch];
            } else if (ch == EOS) {
                panic(FAULT);
            }
            switch(st) {
                case 0:
                case 2:
                    switch(type) {
                        case 0:
                            bchidx++;
                            st++;
                            break;
                        case 1:
                            mChIdx--;
                            st++;
                            break;
                        default:
                            panic(FAULT);
                    }
                    break;
                case 1:
                case 3:
                    switch(type) {
                        case 0:
                        case 2:
                            bchidx++;
                            break;
                        case 1:
                            bchidx++;
                            if (ns == true) {
                                if (bcolon != bqname) {
                                    panic(FAULT);
                                }
                                bcolon = bchidx - 1;
                                if (st == 1) {
                                    st = 2;
                                }
                            }
                            break;
                        default:
                            mChIdx--;
                            bcopy(cstart, bstart);
                            mBuff[bqname] = (char) (bcolon - bqname);
                            return;
                    }
                    break;
                default:
                    panic(FAULT);
            }
        }
    }

    @SuppressWarnings("fallthrough")
    private void bntok() throws Exception {
        char ch;
        mBuffIdx = -1;
        bappend((char) 0);
        while (true) {
            ch = getch();
            switch(chtyp(ch)) {
                case 'a':
                case 'A':
                case 'd':
                case '.':
                case ':':
                case '-':
                case '_':
                case 'X':
                    bappend(ch);
                    break;
                case 'Z':
                    panic(FAULT);
                default:
                    bkch();
                    return;
            }
        }
    }

    private char bkeyword() throws Exception {
        String str = new String(mBuff, 1, mBuffIdx);
        switch(str.length()) {
            case 2:
                return ("ID".equals(str) == true) ? 'i' : '?';
            case 5:
                switch(mBuff[1]) {
                    case 'I':
                        return ("IDREF".equals(str) == true) ? 'r' : '?';
                    case 'C':
                        return ("CDATA".equals(str) == true) ? 'c' : '?';
                    case 'F':
                        return ("FIXED".equals(str) == true) ? 'F' : '?';
                    default:
                        break;
                }
                break;
            case 6:
                switch(mBuff[1]) {
                    case 'I':
                        return ("IDREFS".equals(str) == true) ? 'R' : '?';
                    case 'E':
                        return ("ENTITY".equals(str) == true) ? 'n' : '?';
                    default:
                        break;
                }
                break;
            case 7:
                switch(mBuff[1]) {
                    case 'I':
                        return ("IMPLIED".equals(str) == true) ? 'I' : '?';
                    case 'N':
                        return ("NMTOKEN".equals(str) == true) ? 't' : '?';
                    case 'A':
                        return ("ATTLIST".equals(str) == true) ? 'a' : '?';
                    case 'E':
                        return ("ELEMENT".equals(str) == true) ? 'e' : '?';
                    default:
                        break;
                }
                break;
            case 8:
                switch(mBuff[2]) {
                    case 'N':
                        return ("ENTITIES".equals(str) == true) ? 'N' : '?';
                    case 'M':
                        return ("NMTOKENS".equals(str) == true) ? 'T' : '?';
                    case 'O':
                        return ("NOTATION".equals(str) == true) ? 'o' : '?';
                    case 'E':
                        return ("REQUIRED".equals(str) == true) ? 'Q' : '?';
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        return '?';
    }

    @SuppressWarnings("fallthrough")
    private void bqstr(char flag) throws Exception {
        Input inp = mInp;
        mBuffIdx = -1;
        bappend((char) 0);
        char ch;
        for (short st = 0; st >= 0; ) {
            ch = (mChIdx < mChLen) ? mChars[mChIdx++] : getch();
            switch(st) {
                case 0:
                    switch(ch) {
                        case ' ':
                        case '\n':
                        case '\r':
                        case '\t':
                            break;
                        case '\'':
                            st = 2;
                            break;
                        case '\"':
                            st = 3;
                            break;
                        default:
                            panic(FAULT);
                            break;
                    }
                    break;
                case 2:
                case 3:
                    switch(ch) {
                        case '\'':
                            if ((st == 2) && (mInp == inp)) {
                                st = -1;
                            } else {
                                bappend(ch);
                            }
                            break;
                        case '\"':
                            if ((st == 3) && (mInp == inp)) {
                                st = -1;
                            } else {
                                bappend(ch);
                            }
                            break;
                        case '&':
                            if (flag != 'd') {
                                ent(flag);
                            } else {
                                bappend(ch);
                            }
                            break;
                        case '%':
                            if (flag == 'd') {
                                pent('-');
                            } else {
                                bappend(ch);
                            }
                            break;
                        case '<':
                            if ((flag == '-') || (flag == 'd')) {
                                bappend(ch);
                            } else {
                                panic(FAULT);
                            }
                            break;
                        case EOS:
                            panic(FAULT);
                        case '\r':
                            if (flag != ' ' && mInp.next == null) {
                                if (getch() != '\n') {
                                    bkch();
                                }
                                ch = '\n';
                            }
                        default:
                            bappend(ch, flag);
                            break;
                    }
                    break;
                default:
                    panic(FAULT);
            }
        }
        if ((flag == 'i') && (mBuff[mBuffIdx] == ' ')) {
            mBuffIdx -= 1;
        }
    }

    protected abstract void bflash() throws Exception;

    protected abstract void bflash_ws() throws Exception;

    private void bappend(char ch, char mode) throws Exception {
        switch(mode) {
            case 'i':
                switch(ch) {
                    case ' ':
                    case '\n':
                    case '\r':
                    case '\t':
                        if ((mBuffIdx > 0) && (mBuff[mBuffIdx] != ' ')) {
                            bappend(' ');
                        }
                        return;
                    default:
                        break;
                }
                break;
            case 'c':
                switch(ch) {
                    case '\n':
                    case '\r':
                    case '\t':
                        ch = ' ';
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        mBuffIdx++;
        if (mBuffIdx < mBuff.length) {
            mBuff[mBuffIdx] = ch;
        } else {
            mBuffIdx--;
            bappend(ch);
        }
    }

    private void bappend(char ch) throws Exception {
        ensureCapacity(++mBuffIdx);
        mBuff[mBuffIdx] = ch;
    }

    private void bcopy(int cidx, int bidx) throws Exception {
        int length = mChIdx - cidx;
        ensureCapacity(bidx + length + 1);
        System.arraycopy(mChars, cidx, mBuff, bidx, length);
        mBuffIdx += length;
    }

    @SuppressWarnings("fallthrough")
    private void eappend(char ch) {
        switch(mESt) {
            case 0x100:
                switch(ch) {
                    case 'l':
                        mESt = 0x101;
                        break;
                    case 'g':
                        mESt = 0x102;
                        break;
                    case 'a':
                        mESt = 0x103;
                        break;
                    case 'q':
                        mESt = 0x107;
                        break;
                    default:
                        mESt = 0x200;
                        break;
                }
                break;
            case 0x101:
                mESt = (ch == 't') ? '<' : (char) 0x200;
                break;
            case 0x102:
                mESt = (ch == 't') ? '>' : (char) 0x200;
                break;
            case 0x103:
                switch(ch) {
                    case 'm':
                        mESt = 0x104;
                        break;
                    case 'p':
                        mESt = 0x105;
                        break;
                    default:
                        mESt = 0x200;
                        break;
                }
                break;
            case 0x104:
                mESt = (ch == 'p') ? '&' : (char) 0x200;
                break;
            case 0x105:
                mESt = (ch == 'o') ? (char) 0x106 : (char) 0x200;
                break;
            case 0x106:
                mESt = (ch == 's') ? '\'' : (char) 0x200;
                break;
            case 0x107:
                mESt = (ch == 'u') ? (char) 0x108 : (char) 0x200;
                break;
            case 0x108:
                mESt = (ch == 'o') ? (char) 0x109 : (char) 0x200;
                break;
            case 0x109:
                mESt = (ch == 't') ? '\"' : (char) 0x200;
                break;
            case '<':
            case '>':
            case '&':
            case '\'':
            case '\"':
                mESt = 0x200;
            default:
                break;
        }
    }

    protected void setinp(InputSource is) throws Exception {
        Reader reader = null;
        mChIdx = 0;
        mChLen = 0;
        mChars = mInp.chars;
        mInp.src = null;
        if (mPh < PH_DOC_START) {
            mIsSAlone = false;
        }
        mIsSAloneSet = false;
        if (is.getCharacterStream() != null) {
            reader = is.getCharacterStream();
            xml(reader);
        } else if (is.getByteStream() != null) {
            String expenc;
            if (is.getEncoding() != null) {
                expenc = is.getEncoding().toUpperCase();
                if (expenc.equals("UTF-16")) {
                    reader = bom(is.getByteStream(), 'U');
                } else {
                    reader = enc(expenc, is.getByteStream());
                }
                xml(reader);
            } else {
                reader = bom(is.getByteStream(), ' ');
                if (reader == null) {
                    reader = utf16(is.getByteStream());
                }
                if (reader == null) {
                    reader = enc("UTF-8", is.getByteStream());
                    expenc = xml(reader);
                    if (!expenc.equals("UTF-8")) {
                        if (expenc.startsWith("UTF-16")) {
                            panic(FAULT);
                        }
                        reader = enc(expenc, is.getByteStream());
                    }
                } else {
                    xml(reader);
                }
            }
        } else {
            panic(FAULT);
        }
        mInp.src = reader;
        mInp.pubid = is.getPublicId();
        mInp.sysid = is.getSystemId();
    }

    private Reader bom(InputStream is, char hint) throws Exception {
        int val = is.read();
        switch(val) {
            case 0xef:
                if (hint == 'U') {
                    panic(FAULT);
                }
                if (is.read() != 0xbb) {
                    panic(FAULT);
                }
                if (is.read() != 0xbf) {
                    panic(FAULT);
                }
                return new ReaderUTF8(is);
            case 0xfe:
                if (is.read() != 0xff) {
                    panic(FAULT);
                }
                return new ReaderUTF16(is, 'b');
            case 0xff:
                if (is.read() != 0xfe) {
                    panic(FAULT);
                }
                return new ReaderUTF16(is, 'l');
            case -1:
                mChars[mChIdx++] = EOS;
                return new ReaderUTF8(is);
            default:
                if (hint == 'U') {
                    panic(FAULT);
                }
                switch(val & 0xf0) {
                    case 0xc0:
                    case 0xd0:
                        mChars[mChIdx++] = (char) (((val & 0x1f) << 6) | (is.read() & 0x3f));
                        break;
                    case 0xe0:
                        mChars[mChIdx++] = (char) (((val & 0x0f) << 12) | ((is.read() & 0x3f) << 6) | (is.read() & 0x3f));
                        break;
                    case 0xf0:
                        throw new UnsupportedEncodingException();
                    default:
                        mChars[mChIdx++] = (char) val;
                        break;
                }
                return null;
        }
    }

    private Reader utf16(InputStream is) throws Exception {
        if (mChIdx != 0) {
            byte b0 = (byte) mChars[0];
            if (b0 == 0x00 || b0 == 0x3C) {
                int b1 = is.read();
                int b2 = is.read();
                int b3 = is.read();
                if (b0 == 0x00 && b1 == 0x3C && b2 == 0x00 && b3 == 0x3F) {
                    mChars[0] = (char) (b1);
                    mChars[mChIdx++] = (char) (b3);
                    return new ReaderUTF16(is, 'b');
                } else if (b0 == 0x3C && b1 == 0x00 && b2 == 0x3F && b3 == 0x00) {
                    mChars[0] = (char) (b0);
                    mChars[mChIdx++] = (char) (b2);
                    return new ReaderUTF16(is, 'l');
                } else {
                    mChars[0] = (char) (b0);
                    mChars[mChIdx++] = (char) (b1);
                    mChars[mChIdx++] = (char) (b2);
                    mChars[mChIdx++] = (char) (b3);
                }
            }
        }
        return null;
    }

    private String xml(Reader reader) throws Exception {
        String str = null;
        String enc = "UTF-8";
        char ch;
        int val;
        short st = 0;
        int byteRead = mChIdx;
        while (st >= 0 && mChIdx < mChars.length) {
            if (st < byteRead) {
                ch = mChars[st];
            } else {
                ch = ((val = reader.read()) >= 0) ? (char) val : EOS;
                mChars[mChIdx++] = ch;
            }
            switch(st) {
                case 0:
                    switch(ch) {
                        case '<':
                            st = 1;
                            break;
                        case 0xfeff:
                            ch = ((val = reader.read()) >= 0) ? (char) val : EOS;
                            mChars[mChIdx - 1] = ch;
                            st = (short) ((ch == '<') ? 1 : -1);
                            break;
                        default:
                            st = -1;
                            break;
                    }
                    break;
                case 1:
                    st = (short) ((ch == '?') ? 2 : -1);
                    break;
                case 2:
                    st = (short) ((ch == 'x') ? 3 : -1);
                    break;
                case 3:
                    st = (short) ((ch == 'm') ? 4 : -1);
                    break;
                case 4:
                    st = (short) ((ch == 'l') ? 5 : -1);
                    break;
                case 5:
                    switch(ch) {
                        case ' ':
                        case '\t':
                        case '\r':
                        case '\n':
                            st = 6;
                            break;
                        default:
                            st = -1;
                            break;
                    }
                    break;
                case 6:
                    switch(ch) {
                        case '?':
                            st = 7;
                            break;
                        case EOS:
                            st = -2;
                            break;
                        default:
                            break;
                    }
                    break;
                case 7:
                    switch(ch) {
                        case '>':
                        case EOS:
                            st = -2;
                            break;
                        default:
                            st = 6;
                            break;
                    }
                    break;
                default:
                    panic(FAULT);
                    break;
            }
        }
        mChLen = mChIdx;
        mChIdx = 0;
        if (st == -1) {
            return enc;
        }
        mChIdx = 5;
        for (st = 0; st >= 0; ) {
            ch = getch();
            switch(st) {
                case 0:
                    if (chtyp(ch) != ' ') {
                        bkch();
                        st = 1;
                    }
                    break;
                case 1:
                case 2:
                case 3:
                    switch(chtyp(ch)) {
                        case 'a':
                        case 'A':
                        case '_':
                            bkch();
                            str = name(false).toLowerCase();
                            if ("version".equals(str) == true) {
                                if (st != 1) {
                                    panic(FAULT);
                                }
                                if ("1.0".equals(eqstr('=')) != true) {
                                    panic(FAULT);
                                }
                                mInp.xmlver = 0x0100;
                                st = 2;
                            } else if ("encoding".equals(str) == true) {
                                if (st != 2) {
                                    panic(FAULT);
                                }
                                mInp.xmlenc = eqstr('=').toUpperCase();
                                enc = mInp.xmlenc;
                                st = 3;
                            } else if ("standalone".equals(str) == true) {
                                if ((st == 1) || (mPh >= PH_DOC_START)) {
                                    panic(FAULT);
                                }
                                str = eqstr('=').toLowerCase();
                                if (str.equals("yes") == true) {
                                    mIsSAlone = true;
                                } else if (str.equals("no") == true) {
                                    mIsSAlone = false;
                                } else {
                                    panic(FAULT);
                                }
                                mIsSAloneSet = true;
                                st = 4;
                            } else {
                                panic(FAULT);
                            }
                            break;
                        case ' ':
                            break;
                        case '?':
                            if (st == 1) {
                                panic(FAULT);
                            }
                            bkch();
                            st = 4;
                            break;
                        default:
                            panic(FAULT);
                    }
                    break;
                case 4:
                    switch(chtyp(ch)) {
                        case '?':
                            if (getch() != '>') {
                                panic(FAULT);
                            }
                            if (mPh <= PH_DOC_START) {
                                mPh = PH_MISC_DTD;
                            }
                            st = -1;
                            break;
                        case ' ':
                            break;
                        default:
                            panic(FAULT);
                    }
                    break;
                default:
                    panic(FAULT);
            }
        }
        return enc;
    }

    private Reader enc(String name, InputStream is) throws UnsupportedEncodingException {
        if (name.equals("UTF-8")) {
            return new ReaderUTF8(is);
        } else if (name.equals("UTF-16LE")) {
            return new ReaderUTF16(is, 'l');
        } else if (name.equals("UTF-16BE")) {
            return new ReaderUTF16(is, 'b');
        } else {
            return new InputStreamReader(is, name);
        }
    }

    protected void push(Input inp) {
        mInp.chLen = mChLen;
        mInp.chIdx = mChIdx;
        inp.next = mInp;
        mInp = inp;
        mChars = inp.chars;
        mChLen = inp.chLen;
        mChIdx = inp.chIdx;
    }

    protected void pop() {
        if (mInp.src != null) {
            try {
                mInp.src.close();
            } catch (IOException ioe) {
            }
            mInp.src = null;
        }
        mInp = mInp.next;
        if (mInp != null) {
            mChars = mInp.chars;
            mChLen = mInp.chLen;
            mChIdx = mInp.chIdx;
        } else {
            mChars = null;
            mChLen = 0;
            mChIdx = 0;
        }
    }

    protected char chtyp(char ch) {
        if (ch < 0x80) {
            return (char) asctyp[ch];
        }
        return (ch != EOS) ? 'X' : 'Z';
    }

    protected char getch() throws IOException {
        if (mChIdx >= mChLen) {
            if (mInp.src == null) {
                pop();
                return getch();
            }
            int Num = mInp.src.read(mChars, 0, mChars.length);
            if (Num < 0) {
                if (mInp != mDoc) {
                    pop();
                    return getch();
                } else {
                    mChars[0] = EOS;
                    mChLen = 1;
                }
            } else {
                mChLen = Num;
            }
            mChIdx = 0;
        }
        return mChars[mChIdx++];
    }

    protected void bkch() throws Exception {
        if (mChIdx <= 0) {
            panic(FAULT);
        }
        mChIdx--;
    }

    protected void setch(char ch) {
        mChars[mChIdx] = ch;
    }

    protected Pair find(Pair chain, char[] qname) {
        for (Pair pair = chain; pair != null; pair = pair.next) {
            if (pair.eqname(qname) == true) {
                return pair;
            }
        }
        return null;
    }

    protected Pair pair(Pair next) {
        Pair pair;
        if (mDltd != null) {
            pair = mDltd;
            mDltd = pair.next;
        } else {
            pair = new Pair();
        }
        pair.next = next;
        return pair;
    }

    protected Pair del(Pair pair) {
        Pair next = pair.next;
        pair.name = null;
        pair.value = null;
        pair.chars = null;
        pair.list = null;
        pair.next = mDltd;
        mDltd = pair;
        return next;
    }

    private void ensureCapacity(int minCapacity) throws Exception {
        if (mBuff == null) {
            int newCapacity = minCapacity > BUFFSIZE_PARSER ? minCapacity + BUFFSIZE_PARSER : BUFFSIZE_PARSER;
            mBuff = new char[newCapacity];
            return;
        }
        if (mBuff.length <= minCapacity) {
            int size = mBuff.length << 1;
            int newCapacity = size > minCapacity ? size : minCapacity + BUFFSIZE_PARSER;
            if (newCapacity < 0 || newCapacity > MAX_ARRAY_SIZE) {
                panic(FAULT);
            }
            mBuff = Arrays.copyOf(mBuff, newCapacity);
        }
    }
}
