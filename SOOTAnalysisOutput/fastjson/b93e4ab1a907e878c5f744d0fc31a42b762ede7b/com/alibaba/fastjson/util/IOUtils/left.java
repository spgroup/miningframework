package com.alibaba.fastjson.util;

import java.io.Closeable;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.MalformedInputException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Properties;
import com.alibaba.fastjson.JSONException;

public class IOUtils {

    public final static String FASTJSON_PROPERTIES = "fastjson.properties";

    public final static String FASTJSON_COMPATIBLEWITHJAVABEAN = "fastjson.compatibleWithJavaBean";

    public final static String FASTJSON_COMPATIBLEWITHFIELDNAME = "fastjson.compatibleWithFieldName";

    public final static Properties DEFAULT_PROPERTIES = new Properties();

    public final static Charset UTF8 = Charset.forName("UTF-8");

    public final static char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    public final static boolean[] firstIdentifierFlags = new boolean[256];

    static {
        for (char c = 0; c < firstIdentifierFlags.length; ++c) {
            if (c >= 'A' && c <= 'Z') {
                firstIdentifierFlags[c] = true;
            } else if (c >= 'a' && c <= 'z') {
                firstIdentifierFlags[c] = true;
            } else if (c == '_' || c == '$') {
                firstIdentifierFlags[c] = true;
            }
        }
    }

    public final static boolean[] identifierFlags = new boolean[256];

    static {
        for (char c = 0; c < identifierFlags.length; ++c) {
            if (c >= 'A' && c <= 'Z') {
                identifierFlags[c] = true;
            } else if (c >= 'a' && c <= 'z') {
                identifierFlags[c] = true;
            } else if (c == '_') {
                identifierFlags[c] = true;
            } else if (c >= '0' && c <= '9') {
                identifierFlags[c] = true;
            }
        }
    }

    static {
        try {
            loadPropertiesFromFile();
        } catch (Throwable e) {
        }
    }

    public static String getStringProperty(String name) {
        String prop = null;
        try {
            prop = System.getProperty(name);
        } catch (SecurityException e) {
        }
        return (prop == null) ? DEFAULT_PROPERTIES.getProperty(name) : prop;
    }

    public static void loadPropertiesFromFile() {
        InputStream imputStream = AccessController.doPrivileged(new PrivilegedAction<InputStream>() {

            public InputStream run() {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                if (cl != null) {
                    return cl.getResourceAsStream(FASTJSON_PROPERTIES);
                } else {
                    return ClassLoader.getSystemResourceAsStream(FASTJSON_PROPERTIES);
                }
            }
        });
        if (null != imputStream) {
            try {
                DEFAULT_PROPERTIES.load(imputStream);
                imputStream.close();
            } catch (java.io.IOException e) {
            }
        }
    }

    public final static byte[] specicalFlags_doubleQuotes = new byte[161];

    public final static byte[] specicalFlags_singleQuotes = new byte[161];

    public final static boolean[] specicalFlags_doubleQuotesFlags = new boolean[161];

    public final static boolean[] specicalFlags_singleQuotesFlags = new boolean[161];

    public final static char[] replaceChars = new char[93];

    static {
        specicalFlags_doubleQuotes['\0'] = 4;
        specicalFlags_doubleQuotes['\1'] = 4;
        specicalFlags_doubleQuotes['\2'] = 4;
        specicalFlags_doubleQuotes['\3'] = 4;
        specicalFlags_doubleQuotes['\4'] = 4;
        specicalFlags_doubleQuotes['\5'] = 4;
        specicalFlags_doubleQuotes['\6'] = 4;
        specicalFlags_doubleQuotes['\7'] = 4;
        specicalFlags_doubleQuotes['\b'] = 1;
        specicalFlags_doubleQuotes['\t'] = 1;
        specicalFlags_doubleQuotes['\n'] = 1;
        specicalFlags_doubleQuotes['\u000B'] = 4;
        specicalFlags_doubleQuotes['\f'] = 1;
        specicalFlags_doubleQuotes['\r'] = 1;
        specicalFlags_doubleQuotes['\"'] = 1;
        specicalFlags_doubleQuotes['\\'] = 1;
        specicalFlags_singleQuotes['\0'] = 4;
        specicalFlags_singleQuotes['\1'] = 4;
        specicalFlags_singleQuotes['\2'] = 4;
        specicalFlags_singleQuotes['\3'] = 4;
        specicalFlags_singleQuotes['\4'] = 4;
        specicalFlags_singleQuotes['\5'] = 4;
        specicalFlags_singleQuotes['\6'] = 4;
        specicalFlags_singleQuotes['\7'] = 4;
        specicalFlags_singleQuotes['\b'] = 1;
        specicalFlags_singleQuotes['\t'] = 1;
        specicalFlags_singleQuotes['\n'] = 1;
        specicalFlags_singleQuotes['\u000B'] = 4;
        specicalFlags_singleQuotes['\f'] = 1;
        specicalFlags_singleQuotes['\r'] = 1;
        specicalFlags_singleQuotes['\\'] = 1;
        specicalFlags_singleQuotes['\''] = 1;
        for (int i = 14; i <= 31; ++i) {
            specicalFlags_doubleQuotes[i] = 4;
            specicalFlags_singleQuotes[i] = 4;
        }
        for (int i = 127; i < 160; ++i) {
            specicalFlags_doubleQuotes[i] = 4;
            specicalFlags_singleQuotes[i] = 4;
        }
        for (int i = 0; i < 161; ++i) {
            specicalFlags_doubleQuotesFlags[i] = specicalFlags_doubleQuotes[i] != 0;
            specicalFlags_singleQuotesFlags[i] = specicalFlags_singleQuotes[i] != 0;
        }
        replaceChars['\0'] = '0';
        replaceChars['\1'] = '1';
        replaceChars['\2'] = '2';
        replaceChars['\3'] = '3';
        replaceChars['\4'] = '4';
        replaceChars['\5'] = '5';
        replaceChars['\6'] = '6';
        replaceChars['\7'] = '7';
        replaceChars['\b'] = 'b';
        replaceChars['\t'] = 't';
        replaceChars['\n'] = 'n';
        replaceChars['\u000B'] = 'v';
        replaceChars['\f'] = 'f';
        replaceChars['\r'] = 'r';
        replaceChars['\"'] = '"';
        replaceChars['\''] = '\'';
        replaceChars['/'] = '/';
        replaceChars['\\'] = '\\';
    }

    public final static char[] ASCII_CHARS = { '0', '0', '0', '1', '0', '2', '0', '3', '0', '4', '0', '5', '0', '6', '0', '7', '0', '8', '0', '9', '0', 'A', '0', 'B', '0', 'C', '0', 'D', '0', 'E', '0', 'F', '1', '0', '1', '1', '1', '2', '1', '3', '1', '4', '1', '5', '1', '6', '1', '7', '1', '8', '1', '9', '1', 'A', '1', 'B', '1', 'C', '1', 'D', '1', 'E', '1', 'F', '2', '0', '2', '1', '2', '2', '2', '3', '2', '4', '2', '5', '2', '6', '2', '7', '2', '8', '2', '9', '2', 'A', '2', 'B', '2', 'C', '2', 'D', '2', 'E', '2', 'F' };

    public static void close(Closeable x) {
        if (x != null) {
            try {
                x.close();
            } catch (Exception e) {
            }
        }
    }

    public static int stringSize(long x) {
        long p = 10;
        for (int i = 1; i < 19; i++) {
            if (x < p)
                return i;
            p = 10 * p;
        }
        return 19;
    }

    public static void getChars(long i, int index, char[] buf) {
        long q;
        int r;
        int charPos = index;
        char sign = 0;
        if (i < 0) {
            sign = '-';
            i = -i;
        }
        while (i > Integer.MAX_VALUE) {
            q = i / 100;
            r = (int) (i - ((q << 6) + (q << 5) + (q << 2)));
            i = q;
            buf[--charPos] = DigitOnes[r];
            buf[--charPos] = DigitTens[r];
        }
        int q2;
        int i2 = (int) i;
        while (i2 >= 65536) {
            q2 = i2 / 100;
            r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
            i2 = q2;
            buf[--charPos] = DigitOnes[r];
            buf[--charPos] = DigitTens[r];
        }
        for (; ; ) {
            q2 = (i2 * 52429) >>> (16 + 3);
            r = i2 - ((q2 << 3) + (q2 << 1));
            buf[--charPos] = digits[r];
            i2 = q2;
            if (i2 == 0)
                break;
        }
        if (sign != 0) {
            buf[--charPos] = sign;
        }
    }

    public static void getChars(int i, int index, char[] buf) {
        int q, r, p = index;
        char sign = 0;
        if (i < 0) {
            sign = '-';
            i = -i;
        }
        while (i >= 65536) {
            q = i / 100;
            r = i - ((q << 6) + (q << 5) + (q << 2));
            i = q;
            buf[--p] = DigitOnes[r];
            buf[--p] = DigitTens[r];
        }
        for (; ; ) {
            q = (i * 52429) >>> (16 + 3);
            r = i - ((q << 3) + (q << 1));
            buf[--p] = digits[r];
            i = q;
            if (i == 0)
                break;
        }
        if (sign != 0) {
            buf[--p] = sign;
        }
    }

    public static void getChars(byte b, int index, char[] buf) {
        int i = b;
        int q, r;
        int charPos = index;
        char sign = 0;
        if (i < 0) {
            sign = '-';
            i = -i;
        }
        for (; ; ) {
            q = (i * 52429) >>> (16 + 3);
            r = i - ((q << 3) + (q << 1));
            buf[--charPos] = digits[r];
            i = q;
            if (i == 0)
                break;
        }
        if (sign != 0) {
            buf[--charPos] = sign;
        }
    }

    final static char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

    final static char[] DigitTens = { '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9' };

    final static char[] DigitOnes = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

    final static int[] sizeTable = { 9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE };

    public static int stringSize(int x) {
        for (int i = 0; ; i++) {
            if (x <= sizeTable[i]) {
                return i + 1;
            }
        }
    }

    public static void decode(CharsetDecoder charsetDecoder, ByteBuffer byteBuf, CharBuffer charByte) {
        try {
            CoderResult cr = charsetDecoder.decode(byteBuf, charByte, true);
            if (!cr.isUnderflow()) {
                cr.throwException();
            }
            cr = charsetDecoder.flush(charByte);
            if (!cr.isUnderflow()) {
                cr.throwException();
            }
        } catch (CharacterCodingException x) {
            throw new JSONException("utf8 decode error, " + x.getMessage(), x);
        }
    }

    public static boolean firstIdentifier(char ch) {
        return ch < IOUtils.firstIdentifierFlags.length && IOUtils.firstIdentifierFlags[ch];
    }

    public static boolean isIdent(char ch) {
        return ch < identifierFlags.length && identifierFlags[ch];
    }

    public static final char[] CA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    public static final int[] IA = new int[256];

    static {
        Arrays.fill(IA, -1);
        for (int i = 0, iS = CA.length; i < iS; i++) IA[CA[i]] = i;
        IA['='] = 0;
    }

    public static byte[] decodeBase64(char[] chars, int offset, int charsLen) {
        if (charsLen == 0) {
            return new byte[0];
        }
        int sIx = offset, eIx = offset + charsLen - 1;
        while (sIx < eIx && IA[chars[sIx]] < 0) sIx++;
        while (eIx > 0 && IA[chars[eIx]] < 0) eIx--;
        int pad = chars[eIx] == '=' ? (chars[eIx - 1] == '=' ? 2 : 1) : 0;
        int cCnt = eIx - sIx + 1;
        int sepCnt = charsLen > 76 ? (chars[76] == '\r' ? cCnt / 78 : 0) << 1 : 0;
        int len = ((cCnt - sepCnt) * 6 >> 3) - pad;
        byte[] bytes = new byte[len];
        int d = 0;
        for (int cc = 0, eLen = (len / 3) * 3; d < eLen; ) {
            int i = IA[chars[sIx++]] << 18 | IA[chars[sIx++]] << 12 | IA[chars[sIx++]] << 6 | IA[chars[sIx++]];
            bytes[d++] = (byte) (i >> 16);
            bytes[d++] = (byte) (i >> 8);
            bytes[d++] = (byte) i;
            if (sepCnt > 0 && ++cc == 19) {
                sIx += 2;
                cc = 0;
            }
        }
        if (d < len) {
            int i = 0;
            for (int j = 0; sIx <= eIx - pad; j++) i |= IA[chars[sIx++]] << (18 - j * 6);
            for (int r = 16; d < len; r -= 8) bytes[d++] = (byte) (i >> r);
        }
        return bytes;
    }

    public static byte[] decodeBase64(String chars, int offset, int charsLen) {
        if (charsLen == 0) {
            return new byte[0];
        }
        int sIx = offset, eIx = offset + charsLen - 1;
        while (sIx < eIx && IA[chars.charAt(sIx)] < 0) sIx++;
        while (eIx > 0 && IA[chars.charAt(eIx)] < 0) eIx--;
        int pad = chars.charAt(eIx) == '=' ? (chars.charAt(eIx - 1) == '=' ? 2 : 1) : 0;
        int cCnt = eIx - sIx + 1;
        int sepCnt = charsLen > 76 ? (chars.charAt(76) == '\r' ? cCnt / 78 : 0) << 1 : 0;
        int len = ((cCnt - sepCnt) * 6 >> 3) - pad;
        byte[] bytes = new byte[len];
        int d = 0;
        for (int cc = 0, eLen = (len / 3) * 3; d < eLen; ) {
            int i = IA[chars.charAt(sIx++)] << 18 | IA[chars.charAt(sIx++)] << 12 | IA[chars.charAt(sIx++)] << 6 | IA[chars.charAt(sIx++)];
            bytes[d++] = (byte) (i >> 16);
            bytes[d++] = (byte) (i >> 8);
            bytes[d++] = (byte) i;
            if (sepCnt > 0 && ++cc == 19) {
                sIx += 2;
                cc = 0;
            }
        }
        if (d < len) {
            int i = 0;
            for (int j = 0; sIx <= eIx - pad; j++) i |= IA[chars.charAt(sIx++)] << (18 - j * 6);
            for (int r = 16; d < len; r -= 8) bytes[d++] = (byte) (i >> r);
        }
        return bytes;
    }

    private int decode0(String src, int sp, int sl, byte[] bytes) {
        int[] base64 = IA;
        int dp = 0;
        int bits = 0;
        int shiftto = 18;
        while (sp < sl) {
            int b = src.charAt(sp++) & 0xff;
            if ((b = base64[b]) < 0) {
                if (b == -2) {
                    if (shiftto == 6 && (sp == sl || src.charAt(sp++) != '=') || shiftto == 18) {
                        throw new IllegalArgumentException("Input byte array has wrong 4-byte ending unit");
                    }
                    break;
                }
                throw new IllegalArgumentException("Illegal base64 character " + Integer.toString(src.charAt(sp - 1), 16));
            }
            bits |= (b << shiftto);
            shiftto -= 6;
            if (shiftto < 0) {
                bytes[dp++] = (byte) (bits >> 16);
                bytes[dp++] = (byte) (bits >> 8);
                bytes[dp++] = (byte) (bits);
                shiftto = 18;
                bits = 0;
            }
        }
        if (shiftto == 6) {
            bytes[dp++] = (byte) (bits >> 16);
        } else if (shiftto == 0) {
            bytes[dp++] = (byte) (bits >> 16);
            bytes[dp++] = (byte) (bits >> 8);
        } else if (shiftto == 12) {
            throw new IllegalArgumentException("Last unit does not have enough valid bits");
        }
        while (sp < sl) {
            if (base64[src.charAt(sp++) & 0xff] < 0)
                continue;
            throw new IllegalArgumentException("Input byte array has incorrect ending byte at " + sp);
        }
        return dp;
    }

    public static byte[] decodeBase64(String s) {
        int sLen = s.length();
        if (sLen == 0) {
            return new byte[0];
        }
        int sIx = 0, eIx = sLen - 1;
        while (sIx < eIx && IA[s.charAt(sIx) & 0xff] < 0) sIx++;
        while (eIx > 0 && IA[s.charAt(eIx) & 0xff] < 0) eIx--;
        int pad = s.charAt(eIx) == '=' ? (s.charAt(eIx - 1) == '=' ? 2 : 1) : 0;
        int cCnt = eIx - sIx + 1;
        int sepCnt = sLen > 76 ? (s.charAt(76) == '\r' ? cCnt / 78 : 0) << 1 : 0;
        int len = ((cCnt - sepCnt) * 6 >> 3) - pad;
        byte[] dArr = new byte[len];
        int d = 0;
        for (int cc = 0, eLen = (len / 3) * 3; d < eLen; ) {
            int i = IA[s.charAt(sIx++)] << 18 | IA[s.charAt(sIx++)] << 12 | IA[s.charAt(sIx++)] << 6 | IA[s.charAt(sIx++)];
            dArr[d++] = (byte) (i >> 16);
            dArr[d++] = (byte) (i >> 8);
            dArr[d++] = (byte) i;
            if (sepCnt > 0 && ++cc == 19) {
                sIx += 2;
                cc = 0;
            }
        }
        if (d < len) {
            int i = 0;
            for (int j = 0; sIx <= eIx - pad; j++) i |= IA[s.charAt(sIx++)] << (18 - j * 6);
            for (int r = 16; d < len; r -= 8) dArr[d++] = (byte) (i >> r);
        }
        return dArr;
    }

    public static int encodeUTF8(char[] chars, int offset, int len, byte[] bytes) {
        int sl = offset + len;
        int dp = 0;
        int dlASCII = dp + Math.min(len, bytes.length);
        while (dp < dlASCII && chars[offset] < '\u0080') {
            bytes[dp++] = (byte) chars[offset++];
        }
        while (offset < sl) {
            char c = chars[offset++];
            if (c < 0x80) {
                bytes[dp++] = (byte) c;
            } else if (c < 0x800) {
                bytes[dp++] = (byte) (0xc0 | (c >> 6));
                bytes[dp++] = (byte) (0x80 | (c & 0x3f));
            } else if (c >= '\uD800' && c < ('\uDFFF' + 1)) {
                final int uc;
                int ip = offset - 1;
                if (c >= '\uD800' && c < ('\uDBFF' + 1)) {
                    if (sl - ip < 2) {
                        uc = -1;
                    } else {
                        char d = chars[ip + 1];
                        if (d >= '\uDC00' && d < ('\uDFFF' + 1)) {
                            uc = ((c << 10) + d) + (0x010000 - ('\uD800' << 10) - '\uDC00');
                        } else {
                            throw new JSONException("encodeUTF8 error", new MalformedInputException(1));
                        }
                    }
                } else {
                    if (c >= '\uDC00' && c < ('\uDFFF' + 1)) {
                        throw new JSONException("encodeUTF8 error", new MalformedInputException(1));
                    } else {
                        uc = c;
                    }
                }
                if (uc < 0) {
                    bytes[dp++] = (byte) '?';
                } else {
                    bytes[dp++] = (byte) (0xf0 | ((uc >> 18)));
                    bytes[dp++] = (byte) (0x80 | ((uc >> 12) & 0x3f));
                    bytes[dp++] = (byte) (0x80 | ((uc >> 6) & 0x3f));
                    bytes[dp++] = (byte) (0x80 | (uc & 0x3f));
                    offset++;
                }
            } else {
                bytes[dp++] = (byte) (0xe0 | ((c >> 12)));
                bytes[dp++] = (byte) (0x80 | ((c >> 6) & 0x3f));
                bytes[dp++] = (byte) (0x80 | (c & 0x3f));
            }
        }
        return dp;
    }

    public static int decodeUTF8(byte[] sa, int sp, int len, char[] da) {
        final int sl = sp + len;
        int dp = 0;
        int dlASCII = Math.min(len, da.length);
        while (dp < dlASCII && sa[sp] >= 0) da[dp++] = (char) sa[sp++];
        while (sp < sl) {
            int b1 = sa[sp++];
            if (b1 >= 0) {
                da[dp++] = (char) b1;
            } else if ((b1 >> 5) == -2 && (b1 & 0x1e) != 0) {
                if (sp < sl) {
                    int b2 = sa[sp++];
                    if ((b2 & 0xc0) != 0x80) {
                        return -1;
                    } else {
                        da[dp++] = (char) (((b1 << 6) ^ b2) ^ (((byte) 0xC0 << 6) ^ ((byte) 0x80 << 0)));
                    }
                    continue;
                }
                return -1;
            } else if ((b1 >> 4) == -2) {
                if (sp + 1 < sl) {
                    int b2 = sa[sp++];
                    int b3 = sa[sp++];
                    if ((b1 == (byte) 0xe0 && (b2 & 0xe0) == 0x80) || (b2 & 0xc0) != 0x80 || (b3 & 0xc0) != 0x80) {
                        return -1;
                    } else {
                        char c = (char) ((b1 << 12) ^ (b2 << 6) ^ (b3 ^ (((byte) 0xE0 << 12) ^ ((byte) 0x80 << 6) ^ ((byte) 0x80 << 0))));
                        boolean isSurrogate = c >= Character.MIN_SURROGATE && c < (Character.MAX_SURROGATE + 1);
                        if (isSurrogate) {
                            return -1;
                        } else {
                            da[dp++] = c;
                        }
                    }
                    continue;
                }
                return -1;
            } else if ((b1 >> 3) == -2) {
                if (sp + 2 < sl) {
                    int b2 = sa[sp++];
                    int b3 = sa[sp++];
                    int b4 = sa[sp++];
                    int uc = ((b1 << 18) ^ (b2 << 12) ^ (b3 << 6) ^ (b4 ^ (((byte) 0xF0 << 18) ^ ((byte) 0x80 << 12) ^ ((byte) 0x80 << 6) ^ ((byte) 0x80 << 0))));
                    if (((b2 & 0xc0) != 0x80 || (b3 & 0xc0) != 0x80 || (b4 & 0xc0) != 0x80) || !Character.isSupplementaryCodePoint(uc)) {
                        return -1;
                    } else {
                        da[dp++] = (char) ((uc >>> 10) + (Character.MIN_HIGH_SURROGATE - (Character.MIN_SUPPLEMENTARY_CODE_POINT >>> 10)));
                        da[dp++] = (char) ((uc & 0x3ff) + Character.MIN_LOW_SURROGATE);
                    }
                    continue;
                }
                return -1;
            } else {
                return -1;
            }
        }
        return dp;
    }

    public static String readAll(Reader reader) {
        StringBuilder buf = new StringBuilder();
        try {
            char[] chars = new char[2048];
            for (; ; ) {
                int len = reader.read(chars, 0, chars.length);
                if (len < 0) {
                    break;
                }
                buf.append(chars, 0, len);
            }
        } catch (Exception ex) {
            throw new JSONException("read string from reader error", ex);
        }
        return buf.toString();
    }

    public static boolean isValidJsonpQueryParam(String value) {
        if (value == null || value.length() == 0) {
            return false;
        }
        for (int i = 0, len = value.length(); i < len; ++i) {
            char ch = value.charAt(i);
            if (ch != '.' && !IOUtils.isIdent(ch)) {
                return false;
            }
        }
        return true;
    }
}
