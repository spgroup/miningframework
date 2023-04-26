package com.sun.xml.internal.messaging.saaj.packaging.mime.internet;

import java.io.*;
import java.util.*;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException;
import com.sun.xml.internal.messaging.saaj.packaging.mime.util.*;
import com.sun.xml.internal.messaging.saaj.util.SAAJUtil;

public class MimeUtility {

    private MimeUtility() {
    }

    public static final int ALL = -1;

    private static final int BUFFER_SIZE = 1024;

    private static boolean decodeStrict = true;

    private static boolean encodeEolStrict = false;

    private static boolean foldEncodedWords = false;

    private static boolean foldText = true;

    static {
        try {
            String s = SAAJUtil.getSystemProperty("mail.mime.decodetext.strict");
            decodeStrict = s == null || !s.equalsIgnoreCase("false");
            s = SAAJUtil.getSystemProperty("mail.mime.encodeeol.strict");
            encodeEolStrict = s != null && s.equalsIgnoreCase("true");
            s = SAAJUtil.getSystemProperty("mail.mime.foldencodedwords");
            foldEncodedWords = s != null && s.equalsIgnoreCase("true");
            s = SAAJUtil.getSystemProperty("mail.mime.foldtext");
            foldText = s == null || !s.equalsIgnoreCase("false");
        } catch (SecurityException sex) {
        }
    }

    public static String getEncoding(DataSource ds) {
        ContentType cType = null;
        InputStream is = null;
        String encoding = null;
        try {
            cType = new ContentType(ds.getContentType());
            is = ds.getInputStream();
        } catch (Exception ex) {
            return "base64";
        }
        boolean isText = cType.match("text/*");
        int i = checkAscii(is, ALL, !isText);
        switch(i) {
            case ALL_ASCII:
                encoding = "7bit";
                break;
            case MOSTLY_ASCII:
                encoding = "quoted-printable";
                break;
            default:
                encoding = "base64";
                break;
        }
        try {
            is.close();
        } catch (IOException ioex) {
        }
        return encoding;
    }

    public static String getEncoding(DataHandler dh) {
        ContentType cType = null;
        String encoding = null;
        if (dh.getName() != null)
            return getEncoding(dh.getDataSource());
        try {
            cType = new ContentType(dh.getContentType());
        } catch (Exception ex) {
            return "base64";
        }
        if (cType.match("text/*")) {
            AsciiOutputStream aos = new AsciiOutputStream(false, false);
            try {
                dh.writeTo(aos);
            } catch (IOException ex) {
            }
            switch(aos.getAscii()) {
                case ALL_ASCII:
                    encoding = "7bit";
                    break;
                case MOSTLY_ASCII:
                    encoding = "quoted-printable";
                    break;
                default:
                    encoding = "base64";
                    break;
            }
        } else {
            AsciiOutputStream aos = new AsciiOutputStream(true, encodeEolStrict);
            try {
                dh.writeTo(aos);
            } catch (IOException ex) {
            }
            if (aos.getAscii() == ALL_ASCII)
                encoding = "7bit";
            else
                encoding = "base64";
        }
        return encoding;
    }

    public static InputStream decode(InputStream is, String encoding) throws MessagingException {
        if (encoding.equalsIgnoreCase("base64"))
            return new BASE64DecoderStream(is);
        else if (encoding.equalsIgnoreCase("quoted-printable"))
            return new QPDecoderStream(is);
        else if (encoding.equalsIgnoreCase("uuencode") || encoding.equalsIgnoreCase("x-uuencode") || encoding.equalsIgnoreCase("x-uue"))
            return new UUDecoderStream(is);
        else if (encoding.equalsIgnoreCase("binary") || encoding.equalsIgnoreCase("7bit") || encoding.equalsIgnoreCase("8bit"))
            return is;
        else
            throw new MessagingException("Unknown encoding: " + encoding);
    }

    public static OutputStream encode(OutputStream os, String encoding) throws MessagingException {
        if (encoding == null)
            return os;
        else if (encoding.equalsIgnoreCase("base64"))
            return new BASE64EncoderStream(os);
        else if (encoding.equalsIgnoreCase("quoted-printable"))
            return new QPEncoderStream(os);
        else if (encoding.equalsIgnoreCase("uuencode") || encoding.equalsIgnoreCase("x-uuencode") || encoding.equalsIgnoreCase("x-uue"))
            return new UUEncoderStream(os);
        else if (encoding.equalsIgnoreCase("binary") || encoding.equalsIgnoreCase("7bit") || encoding.equalsIgnoreCase("8bit"))
            return os;
        else
            throw new MessagingException("Unknown encoding: " + encoding);
    }

    public static OutputStream encode(OutputStream os, String encoding, String filename) throws MessagingException {
        if (encoding == null)
            return os;
        else if (encoding.equalsIgnoreCase("base64"))
            return new BASE64EncoderStream(os);
        else if (encoding.equalsIgnoreCase("quoted-printable"))
            return new QPEncoderStream(os);
        else if (encoding.equalsIgnoreCase("uuencode") || encoding.equalsIgnoreCase("x-uuencode") || encoding.equalsIgnoreCase("x-uue"))
            return new UUEncoderStream(os, filename);
        else if (encoding.equalsIgnoreCase("binary") || encoding.equalsIgnoreCase("7bit") || encoding.equalsIgnoreCase("8bit"))
            return os;
        else
            throw new MessagingException("Unknown encoding: " + encoding);
    }

    public static String encodeText(String text) throws UnsupportedEncodingException {
        return encodeText(text, null, null);
    }

    public static String encodeText(String text, String charset, String encoding) throws UnsupportedEncodingException {
        return encodeWord(text, charset, encoding, false);
    }

    public static String decodeText(String etext) throws UnsupportedEncodingException {
        String lwsp = " \t\n\r";
        StringTokenizer st;
        if (etext.indexOf("=?") == -1)
            return etext;
        st = new StringTokenizer(etext, lwsp, true);
        StringBuffer sb = new StringBuffer();
        StringBuffer wsb = new StringBuffer();
        boolean prevWasEncoded = false;
        while (st.hasMoreTokens()) {
            char c;
            String s = st.nextToken();
            if (((c = s.charAt(0)) == ' ') || (c == '\t') || (c == '\r') || (c == '\n'))
                wsb.append(c);
            else {
                String word;
                try {
                    word = decodeWord(s);
                    if (!prevWasEncoded && wsb.length() > 0) {
                        sb.append(wsb);
                    }
                    prevWasEncoded = true;
                } catch (ParseException pex) {
                    word = s;
                    if (!decodeStrict)
                        word = decodeInnerWords(word);
                    if (wsb.length() > 0)
                        sb.append(wsb);
                    prevWasEncoded = false;
                }
                sb.append(word);
                wsb.setLength(0);
            }
        }
        return sb.toString();
    }

    public static String encodeWord(String word) throws UnsupportedEncodingException {
        return encodeWord(word, null, null);
    }

    public static String encodeWord(String word, String charset, String encoding) throws UnsupportedEncodingException {
        return encodeWord(word, charset, encoding, true);
    }

    private static String encodeWord(String string, String charset, String encoding, boolean encodingWord) throws UnsupportedEncodingException {
        int ascii = checkAscii(string);
        if (ascii == ALL_ASCII)
            return string;
        String jcharset;
        if (charset == null) {
            jcharset = getDefaultJavaCharset();
            charset = getDefaultMIMECharset();
        } else
            jcharset = javaCharset(charset);
        if (encoding == null) {
            if (ascii != MOSTLY_NONASCII)
                encoding = "Q";
            else
                encoding = "B";
        }
        boolean b64;
        if (encoding.equalsIgnoreCase("B"))
            b64 = true;
        else if (encoding.equalsIgnoreCase("Q"))
            b64 = false;
        else
            throw new UnsupportedEncodingException("Unknown transfer encoding: " + encoding);
        StringBuffer outb = new StringBuffer();
        doEncode(string, b64, jcharset, 75 - 7 - charset.length(), "=?" + charset + "?" + encoding + "?", true, encodingWord, outb);
        return outb.toString();
    }

    private static void doEncode(String string, boolean b64, String jcharset, int avail, String prefix, boolean first, boolean encodingWord, StringBuffer buf) throws UnsupportedEncodingException {
        byte[] bytes = string.getBytes(jcharset);
        int len;
        if (b64)
            len = BEncoderStream.encodedLength(bytes);
        else
            len = QEncoderStream.encodedLength(bytes, encodingWord);
        int size;
        if ((len > avail) && ((size = string.length()) > 1)) {
            doEncode(string.substring(0, size / 2), b64, jcharset, avail, prefix, first, encodingWord, buf);
            doEncode(string.substring(size / 2, size), b64, jcharset, avail, prefix, false, encodingWord, buf);
        } else {
            ByteArrayOutputStream os = new ByteArrayOutputStream(BUFFER_SIZE);
            OutputStream eos;
            if (b64)
                eos = new BEncoderStream(os);
            else
                eos = new QEncoderStream(os, encodingWord);
            try {
                eos.write(bytes);
                eos.close();
            } catch (IOException ioex) {
            }
            byte[] encodedBytes = os.toByteArray();
            if (!first)
                if (foldEncodedWords)
                    buf.append("\r\n ");
                else
                    buf.append(" ");
            buf.append(prefix);
            for (int i = 0; i < encodedBytes.length; i++) buf.append((char) encodedBytes[i]);
            buf.append("?=");
        }
    }

    public static String decodeWord(String eword) throws ParseException, UnsupportedEncodingException {
        if (!eword.startsWith("=?"))
            throw new ParseException();
        int start = 2;
        int pos;
        if ((pos = eword.indexOf('?', start)) == -1)
            throw new ParseException();
        String charset = javaCharset(eword.substring(start, pos));
        start = pos + 1;
        if ((pos = eword.indexOf('?', start)) == -1)
            throw new ParseException();
        String encoding = eword.substring(start, pos);
        start = pos + 1;
        if ((pos = eword.indexOf("?=", start)) == -1)
            throw new ParseException();
        String word = eword.substring(start, pos);
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(ASCIIUtility.getBytes(word));
            InputStream is;
            if (encoding.equalsIgnoreCase("B"))
                is = new BASE64DecoderStream(bis);
            else if (encoding.equalsIgnoreCase("Q"))
                is = new QDecoderStream(bis);
            else
                throw new UnsupportedEncodingException("unknown encoding: " + encoding);
            int count = bis.available();
            byte[] bytes = new byte[count];
            count = is.read(bytes, 0, count);
            String s = new String(bytes, 0, count, charset);
            if (pos + 2 < eword.length()) {
                String rest = eword.substring(pos + 2);
                if (!decodeStrict)
                    rest = decodeInnerWords(rest);
                s += rest;
            }
            return s;
        } catch (UnsupportedEncodingException uex) {
            throw uex;
        } catch (IOException ioex) {
            throw new ParseException();
        } catch (IllegalArgumentException iex) {
            throw new UnsupportedEncodingException();
        }
    }

    private static String decodeInnerWords(String word) throws UnsupportedEncodingException {
        int start = 0, i;
        StringBuffer buf = new StringBuffer();
        while ((i = word.indexOf("=?", start)) >= 0) {
            buf.append(word.substring(start, i));
            int end = word.indexOf("?=", i);
            if (end < 0)
                break;
            String s = word.substring(i, end + 2);
            try {
                s = decodeWord(s);
            } catch (ParseException pex) {
            }
            buf.append(s);
            start = end + 2;
        }
        if (start == 0)
            return word;
        if (start < word.length())
            buf.append(word.substring(start));
        return buf.toString();
    }

    public static String quote(String word, String specials) {
        int len = word.length();
        boolean needQuoting = false;
        for (int i = 0; i < len; i++) {
            char c = word.charAt(i);
            if (c == '"' || c == '\\' || c == '\r' || c == '\n') {
                StringBuffer sb = new StringBuffer(len + 3);
                sb.append('"');
                sb.append(word.substring(0, i));
                int lastc = 0;
                for (int j = i; j < len; j++) {
                    char cc = word.charAt(j);
                    if ((cc == '"') || (cc == '\\') || (cc == '\r') || (cc == '\n'))
                        if (cc == '\n' && lastc == '\r')
                            ;
                        else
                            sb.append('\\');
                    sb.append(cc);
                    lastc = cc;
                }
                sb.append('"');
                return sb.toString();
            } else if (c < 040 || c >= 0177 || specials.indexOf(c) >= 0)
                needQuoting = true;
        }
        if (needQuoting) {
            StringBuffer sb = new StringBuffer(len + 2);
            sb.append('"').append(word).append('"');
            return sb.toString();
        } else
            return word;
    }

    static String fold(int used, String s) {
        if (!foldText)
            return s;
        int end;
        char c;
        for (end = s.length() - 1; end >= 0; end--) {
            c = s.charAt(end);
            if (c != ' ' && c != '\t')
                break;
        }
        if (end != s.length() - 1)
            s = s.substring(0, end + 1);
        if (used + s.length() <= 76)
            return s;
        StringBuffer sb = new StringBuffer(s.length() + 4);
        char lastc = 0;
        while (used + s.length() > 76) {
            int lastspace = -1;
            for (int i = 0; i < s.length(); i++) {
                if (lastspace != -1 && used + i > 76)
                    break;
                c = s.charAt(i);
                if (c == ' ' || c == '\t')
                    if (!(lastc == ' ' || lastc == '\t'))
                        lastspace = i;
                lastc = c;
            }
            if (lastspace == -1) {
                sb.append(s);
                s = "";
                used = 0;
                break;
            }
            sb.append(s.substring(0, lastspace));
            sb.append("\r\n");
            lastc = s.charAt(lastspace);
            sb.append(lastc);
            s = s.substring(lastspace + 1);
            used = 1;
        }
        sb.append(s);
        return sb.toString();
    }

    static String unfold(String s) {
        if (!foldText)
            return s;
        StringBuffer sb = null;
        int i;
        while ((i = indexOfAny(s, "\r\n")) >= 0) {
            int start = i;
            int l = s.length();
            i++;
            if (i < l && s.charAt(i - 1) == '\r' && s.charAt(i) == '\n')
                i++;
            if (start == 0 || s.charAt(start - 1) != '\\') {
                char c;
                if (i < l && ((c = s.charAt(i)) == ' ' || c == '\t')) {
                    i++;
                    while (i < l && ((c = s.charAt(i)) == ' ' || c == '\t')) i++;
                    if (sb == null)
                        sb = new StringBuffer(s.length());
                    if (start != 0) {
                        sb.append(s.substring(0, start));
                        sb.append(' ');
                    }
                    s = s.substring(i);
                    continue;
                }
                if (sb == null)
                    sb = new StringBuffer(s.length());
                sb.append(s.substring(0, i));
                s = s.substring(i);
            } else {
                if (sb == null)
                    sb = new StringBuffer(s.length());
                sb.append(s.substring(0, start - 1));
                sb.append(s.substring(start, i));
                s = s.substring(i);
            }
        }
        if (sb != null) {
            sb.append(s);
            return sb.toString();
        } else
            return s;
    }

    private static int indexOfAny(String s, String any) {
        return indexOfAny(s, any, 0);
    }

    private static int indexOfAny(String s, String any, int start) {
        try {
            int len = s.length();
            for (int i = start; i < len; i++) {
                if (any.indexOf(s.charAt(i)) >= 0)
                    return i;
            }
            return -1;
        } catch (StringIndexOutOfBoundsException e) {
            return -1;
        }
    }

    public static String javaCharset(String charset) {
        if (mime2java == null || charset == null)
            return charset;
        String alias = (String) mime2java.get(charset.toLowerCase());
        return alias == null ? charset : alias;
    }

    public static String mimeCharset(String charset) {
        if (java2mime == null || charset == null)
            return charset;
        String alias = (String) java2mime.get(charset.toLowerCase());
        return alias == null ? charset : alias;
    }

    private static String defaultJavaCharset;

    private static String defaultMIMECharset;

    public static String getDefaultJavaCharset() {
        if (defaultJavaCharset == null) {
            String mimecs = null;
            mimecs = SAAJUtil.getSystemProperty("mail.mime.charset");
            if (mimecs != null && mimecs.length() > 0) {
                defaultJavaCharset = javaCharset(mimecs);
                return defaultJavaCharset;
            }
            try {
                defaultJavaCharset = System.getProperty("file.encoding", "8859_1");
            } catch (SecurityException sex) {
                class NullInputStream extends InputStream {

                    public int read() {
                        return 0;
                    }
                }
                InputStreamReader reader = new InputStreamReader(new NullInputStream());
                defaultJavaCharset = reader.getEncoding();
                if (defaultJavaCharset == null)
                    defaultJavaCharset = "8859_1";
            }
        }
        return defaultJavaCharset;
    }

    static String getDefaultMIMECharset() {
        if (defaultMIMECharset == null) {
            defaultMIMECharset = SAAJUtil.getSystemProperty("mail.mime.charset");
        }
        if (defaultMIMECharset == null)
            defaultMIMECharset = mimeCharset(getDefaultJavaCharset());
        return defaultMIMECharset;
    }

    private static Hashtable mime2java;

    private static Hashtable java2mime;

    static {
        java2mime = new Hashtable(40);
        mime2java = new Hashtable(10);
        try {
            InputStream is = com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility.class.getResourceAsStream("/META-INF/javamail.charset.map");
            if (is != null) {
                is = new LineInputStream(is);
                loadMappings((LineInputStream) is, java2mime);
                loadMappings((LineInputStream) is, mime2java);
            }
        } catch (Exception ex) {
        }
        if (java2mime.isEmpty()) {
            java2mime.put("8859_1", "ISO-8859-1");
            java2mime.put("iso8859_1", "ISO-8859-1");
            java2mime.put("ISO8859-1", "ISO-8859-1");
            java2mime.put("8859_2", "ISO-8859-2");
            java2mime.put("iso8859_2", "ISO-8859-2");
            java2mime.put("ISO8859-2", "ISO-8859-2");
            java2mime.put("8859_3", "ISO-8859-3");
            java2mime.put("iso8859_3", "ISO-8859-3");
            java2mime.put("ISO8859-3", "ISO-8859-3");
            java2mime.put("8859_4", "ISO-8859-4");
            java2mime.put("iso8859_4", "ISO-8859-4");
            java2mime.put("ISO8859-4", "ISO-8859-4");
            java2mime.put("8859_5", "ISO-8859-5");
            java2mime.put("iso8859_5", "ISO-8859-5");
            java2mime.put("ISO8859-5", "ISO-8859-5");
            java2mime.put("8859_6", "ISO-8859-6");
            java2mime.put("iso8859_6", "ISO-8859-6");
            java2mime.put("ISO8859-6", "ISO-8859-6");
            java2mime.put("8859_7", "ISO-8859-7");
            java2mime.put("iso8859_7", "ISO-8859-7");
            java2mime.put("ISO8859-7", "ISO-8859-7");
            java2mime.put("8859_8", "ISO-8859-8");
            java2mime.put("iso8859_8", "ISO-8859-8");
            java2mime.put("ISO8859-8", "ISO-8859-8");
            java2mime.put("8859_9", "ISO-8859-9");
            java2mime.put("iso8859_9", "ISO-8859-9");
            java2mime.put("ISO8859-9", "ISO-8859-9");
            java2mime.put("SJIS", "Shift_JIS");
            java2mime.put("MS932", "Shift_JIS");
            java2mime.put("JIS", "ISO-2022-JP");
            java2mime.put("ISO2022JP", "ISO-2022-JP");
            java2mime.put("EUC_JP", "euc-jp");
            java2mime.put("KOI8_R", "koi8-r");
            java2mime.put("EUC_CN", "euc-cn");
            java2mime.put("EUC_TW", "euc-tw");
            java2mime.put("EUC_KR", "euc-kr");
        }
        if (mime2java.isEmpty()) {
            mime2java.put("iso-2022-cn", "ISO2022CN");
            mime2java.put("iso-2022-kr", "ISO2022KR");
            mime2java.put("utf-8", "UTF8");
            mime2java.put("utf8", "UTF8");
            mime2java.put("ja_jp.iso2022-7", "ISO2022JP");
            mime2java.put("ja_jp.eucjp", "EUCJIS");
            mime2java.put("euc-kr", "KSC5601");
            mime2java.put("euckr", "KSC5601");
            mime2java.put("us-ascii", "ISO-8859-1");
            mime2java.put("x-us-ascii", "ISO-8859-1");
        }
    }

    private static void loadMappings(LineInputStream is, Hashtable table) {
        String currLine;
        while (true) {
            try {
                currLine = is.readLine();
            } catch (IOException ioex) {
                break;
            }
            if (currLine == null)
                break;
            if (currLine.startsWith("--") && currLine.endsWith("--"))
                break;
            if (currLine.trim().length() == 0 || currLine.startsWith("#"))
                continue;
            StringTokenizer tk = new StringTokenizer(currLine, " \t");
            try {
                String key = tk.nextToken();
                String value = tk.nextToken();
                table.put(key.toLowerCase(), value);
            } catch (NoSuchElementException nex) {
            }
        }
    }

    static final int ALL_ASCII = 1;

    static final int MOSTLY_ASCII = 2;

    static final int MOSTLY_NONASCII = 3;

    static int checkAscii(String s) {
        int ascii = 0, non_ascii = 0;
        int l = s.length();
        for (int i = 0; i < l; i++) {
            if (nonascii((int) s.charAt(i)))
                non_ascii++;
            else
                ascii++;
        }
        if (non_ascii == 0)
            return ALL_ASCII;
        if (ascii > non_ascii)
            return MOSTLY_ASCII;
        return MOSTLY_NONASCII;
    }

    static int checkAscii(byte[] b) {
        int ascii = 0, non_ascii = 0;
        for (int i = 0; i < b.length; i++) {
            if (nonascii(b[i] & 0xff))
                non_ascii++;
            else
                ascii++;
        }
        if (non_ascii == 0)
            return ALL_ASCII;
        if (ascii > non_ascii)
            return MOSTLY_ASCII;
        return MOSTLY_NONASCII;
    }

    static int checkAscii(InputStream is, int max, boolean breakOnNonAscii) {
        int ascii = 0, non_ascii = 0;
        int len;
        int block = 4096;
        int linelen = 0;
        boolean longLine = false, badEOL = false;
        boolean checkEOL = encodeEolStrict && breakOnNonAscii;
        byte[] buf = null;
        if (max != 0) {
            block = (max == ALL) ? 4096 : Math.min(max, 4096);
            buf = new byte[block];
        }
        while (max != 0) {
            try {
                if ((len = is.read(buf, 0, block)) == -1)
                    break;
                int lastb = 0;
                for (int i = 0; i < len; i++) {
                    int b = buf[i] & 0xff;
                    if (checkEOL && ((lastb == '\r' && b != '\n') || (lastb != '\r' && b == '\n')))
                        badEOL = true;
                    if (b == '\r' || b == '\n')
                        linelen = 0;
                    else {
                        linelen++;
                        if (linelen > 998)
                            longLine = true;
                    }
                    if (nonascii(b)) {
                        if (breakOnNonAscii)
                            return MOSTLY_NONASCII;
                        else
                            non_ascii++;
                    } else
                        ascii++;
                    lastb = b;
                }
            } catch (IOException ioex) {
                break;
            }
            if (max != ALL)
                max -= len;
        }
        if (max == 0 && breakOnNonAscii)
            return MOSTLY_NONASCII;
        if (non_ascii == 0) {
            if (badEOL)
                return MOSTLY_NONASCII;
            else if (longLine)
                return MOSTLY_ASCII;
            else
                return ALL_ASCII;
        }
        if (ascii > non_ascii)
            return MOSTLY_ASCII;
        return MOSTLY_NONASCII;
    }

    static final boolean nonascii(int b) {
        return b >= 0177 || (b < 040 && b != '\r' && b != '\n' && b != '\t');
    }
}

class AsciiOutputStream extends OutputStream {

    private boolean breakOnNonAscii;

    private int ascii = 0, non_ascii = 0;

    private int linelen = 0;

    private boolean longLine = false;

    private boolean badEOL = false;

    private boolean checkEOL = false;

    private int lastb = 0;

    private int ret = 0;

    public AsciiOutputStream(boolean breakOnNonAscii, boolean encodeEolStrict) {
        this.breakOnNonAscii = breakOnNonAscii;
        checkEOL = encodeEolStrict && breakOnNonAscii;
    }

    public void write(int b) throws IOException {
        check(b);
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        len += off;
        for (int i = off; i < len; i++) check(b[i]);
    }

    private final void check(int b) throws IOException {
        b &= 0xff;
        if (checkEOL && ((lastb == '\r' && b != '\n') || (lastb != '\r' && b == '\n')))
            badEOL = true;
        if (b == '\r' || b == '\n')
            linelen = 0;
        else {
            linelen++;
            if (linelen > 998)
                longLine = true;
        }
        if (MimeUtility.nonascii(b)) {
            non_ascii++;
            if (breakOnNonAscii) {
                ret = MimeUtility.MOSTLY_NONASCII;
                throw new EOFException();
            }
        } else
            ascii++;
        lastb = b;
    }

    public int getAscii() {
        if (ret != 0)
            return ret;
        if (badEOL)
            return MimeUtility.MOSTLY_NONASCII;
        else if (non_ascii == 0) {
            if (longLine)
                return MimeUtility.MOSTLY_ASCII;
            else
                return MimeUtility.ALL_ASCII;
        }
        if (ascii > non_ascii)
            return MimeUtility.MOSTLY_ASCII;
        return MimeUtility.MOSTLY_NONASCII;
    }
}
