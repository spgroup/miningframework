package java.lang;

import java.io.ObjectStreamField;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Formatter;
import java.util.Locale;
import java.util.Objects;
import java.util.Spliterator;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import jdk.internal.HotSpotIntrinsicCandidate;

public final class String implements java.io.Serializable, Comparable<String>, CharSequence {

    private final byte[] value;

    private final byte coder;

    private int hash;

    private static final long serialVersionUID = -6849794470754667710L;

    static final boolean COMPACT_STRINGS;

    static {
        COMPACT_STRINGS = true;
    }

    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[0];

    public String() {
        this.value = "".value;
        this.coder = "".coder;
    }

    @HotSpotIntrinsicCandidate
    public String(String original) {
        this.value = original.value;
        this.coder = original.coder;
        this.hash = original.hash;
    }

    public String(char[] value) {
        this(value, 0, value.length, null);
    }

    public String(char[] value, int offset, int count) {
        this(value, offset, count, rangeCheck(value, offset, count));
    }

    private static Void rangeCheck(char[] value, int offset, int count) {
        checkBoundsOffCount(offset, count, value.length);
        return null;
    }

    public String(int[] codePoints, int offset, int count) {
        checkBoundsOffCount(offset, count, codePoints.length);
        if (count == 0) {
            this.value = "".value;
            this.coder = "".coder;
            return;
        }
        if (COMPACT_STRINGS) {
            byte[] val = StringLatin1.toBytes(codePoints, offset, count);
            if (val != null) {
                this.coder = LATIN1;
                this.value = val;
                return;
            }
        }
        this.coder = UTF16;
        this.value = StringUTF16.toBytes(codePoints, offset, count);
    }

    @Deprecated
    public String(byte[] ascii, int hibyte, int offset, int count) {
        checkBoundsOffCount(offset, count, ascii.length);
        if (count == 0) {
            this.value = "".value;
            this.coder = "".coder;
            return;
        }
        if (COMPACT_STRINGS && (byte) hibyte == 0) {
            this.value = Arrays.copyOfRange(ascii, offset, offset + count);
            this.coder = LATIN1;
        } else {
            hibyte <<= 8;
            byte[] val = StringUTF16.newBytesFor(count);
            for (int i = 0; i < count; i++) {
                StringUTF16.putChar(val, i, hibyte | (ascii[offset++] & 0xff));
            }
            this.value = val;
            this.coder = UTF16;
        }
    }

    @Deprecated
    public String(byte[] ascii, int hibyte) {
        this(ascii, hibyte, 0, ascii.length);
    }

    public String(byte[] bytes, int offset, int length, String charsetName) throws UnsupportedEncodingException {
        if (charsetName == null)
            throw new NullPointerException("charsetName");
        checkBoundsOffCount(offset, length, bytes.length);
        StringCoding.Result ret = StringCoding.decode(charsetName, bytes, offset, length);
        this.value = ret.value;
        this.coder = ret.coder;
    }

    public String(byte[] bytes, int offset, int length, Charset charset) {
        if (charset == null)
            throw new NullPointerException("charset");
        checkBoundsOffCount(offset, length, bytes.length);
        StringCoding.Result ret = StringCoding.decode(charset, bytes, offset, length);
        this.value = ret.value;
        this.coder = ret.coder;
    }

    public String(byte[] bytes, String charsetName) throws UnsupportedEncodingException {
        this(bytes, 0, bytes.length, charsetName);
    }

    public String(byte[] bytes, Charset charset) {
        this(bytes, 0, bytes.length, charset);
    }

    public String(byte[] bytes, int offset, int length) {
        checkBoundsOffCount(offset, length, bytes.length);
        StringCoding.Result ret = StringCoding.decode(bytes, offset, length);
        this.value = ret.value;
        this.coder = ret.coder;
    }

    public String(byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    public String(StringBuffer buffer) {
        this(buffer.toString());
    }

    public String(StringBuilder builder) {
        this(builder, null);
    }

    String(char[] val, boolean share) {
        this(val, 0, val.length, null);
    }

    public int length() {
        return value.length >> coder();
    }

    public boolean isEmpty() {
        return value.length == 0;
    }

    public char charAt(int index) {
        if (isLatin1()) {
            return StringLatin1.charAt(value, index);
        } else {
            return StringUTF16.charAt(value, index);
        }
    }

    public int codePointAt(int index) {
        if (isLatin1()) {
            checkIndex(index, value.length);
            return value[index] & 0xff;
        }
        int length = value.length >> 1;
        checkIndex(index, length);
        return StringUTF16.codePointAt(value, index, length);
    }

    public int codePointBefore(int index) {
        int i = index - 1;
        if (i < 0 || i >= length()) {
            throw new StringIndexOutOfBoundsException(index);
        }
        if (isLatin1()) {
            return (value[i] & 0xff);
        }
        return StringUTF16.codePointBefore(value, index);
    }

    public int codePointCount(int beginIndex, int endIndex) {
        if (beginIndex < 0 || beginIndex > endIndex || endIndex > length()) {
            throw new IndexOutOfBoundsException();
        }
        if (isLatin1()) {
            return endIndex - beginIndex;
        }
        return StringUTF16.codePointCount(value, beginIndex, endIndex);
    }

    public int offsetByCodePoints(int index, int codePointOffset) {
        if (index < 0 || index > length()) {
            throw new IndexOutOfBoundsException();
        }
        return Character.offsetByCodePoints(this, index, codePointOffset);
    }

    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        checkBoundsBeginEnd(srcBegin, srcEnd, length());
        checkBoundsOffCount(dstBegin, srcEnd - srcBegin, dst.length);
        if (isLatin1()) {
            StringLatin1.getChars(value, srcBegin, srcEnd, dst, dstBegin);
        } else {
            StringUTF16.getChars(value, srcBegin, srcEnd, dst, dstBegin);
        }
    }

    @Deprecated
    public void getBytes(int srcBegin, int srcEnd, byte[] dst, int dstBegin) {
        checkBoundsBeginEnd(srcBegin, srcEnd, length());
        Objects.requireNonNull(dst);
        checkBoundsOffCount(dstBegin, srcEnd - srcBegin, dst.length);
        if (isLatin1()) {
            StringLatin1.getBytes(value, srcBegin, srcEnd, dst, dstBegin);
        } else {
            StringUTF16.getBytes(value, srcBegin, srcEnd, dst, dstBegin);
        }
    }

    public byte[] getBytes(String charsetName) throws UnsupportedEncodingException {
        if (charsetName == null)
            throw new NullPointerException();
        return StringCoding.encode(charsetName, coder(), value);
    }

    public byte[] getBytes(Charset charset) {
        if (charset == null)
            throw new NullPointerException();
        return StringCoding.encode(charset, coder(), value);
    }

    public byte[] getBytes() {
        return StringCoding.encode(coder(), value);
    }

    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof String) {
            String aString = (String) anObject;
            if (coder() == aString.coder()) {
                return isLatin1() ? StringLatin1.equals(value, aString.value) : StringUTF16.equals(value, aString.value);
            }
        }
        return false;
    }

    public boolean contentEquals(StringBuffer sb) {
        return contentEquals((CharSequence) sb);
    }

    private boolean nonSyncContentEquals(AbstractStringBuilder sb) {
        int len = length();
        if (len != sb.length()) {
            return false;
        }
        byte[] v1 = value;
        byte[] v2 = sb.getValue();
        if (coder() == sb.getCoder()) {
            int n = v1.length;
            for (int i = 0; i < n; i++) {
                if (v1[i] != v2[i]) {
                    return false;
                }
            }
        } else {
            if (!isLatin1()) {
                return false;
            }
            for (int i = 0; i < len; i++) {
                if ((char) (v1[i] & 0xff) != StringUTF16.getChar(v2, i)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean contentEquals(CharSequence cs) {
        if (cs instanceof AbstractStringBuilder) {
            if (cs instanceof StringBuffer) {
                synchronized (cs) {
                    return nonSyncContentEquals((AbstractStringBuilder) cs);
                }
            } else {
                return nonSyncContentEquals((AbstractStringBuilder) cs);
            }
        }
        if (cs instanceof String) {
            return equals(cs);
        }
        int n = cs.length();
        if (n != length()) {
            return false;
        }
        byte[] val = this.value;
        if (isLatin1()) {
            for (int i = 0; i < n; i++) {
                if ((val[i] & 0xff) != cs.charAt(i)) {
                    return false;
                }
            }
        } else {
            for (int i = 0; i < n; i++) {
                if (StringUTF16.getChar(val, i) != cs.charAt(i)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean equalsIgnoreCase(String anotherString) {
        return (this == anotherString) ? true : (anotherString != null) && (anotherString.length() == length()) && regionMatches(true, 0, anotherString, 0, length());
    }

    public int compareTo(String anotherString) {
        byte[] v1 = value;
        byte[] v2 = anotherString.value;
        if (coder() == anotherString.coder()) {
            return isLatin1() ? StringLatin1.compareTo(v1, v2) : StringUTF16.compareTo(v1, v2);
        }
        return isLatin1() ? StringLatin1.compareToUTF16(v1, v2) : StringUTF16.compareToLatin1(v1, v2);
    }

    public static final Comparator<String> CASE_INSENSITIVE_ORDER = new CaseInsensitiveComparator();

    private static class CaseInsensitiveComparator implements Comparator<String>, java.io.Serializable {

        private static final long serialVersionUID = 8575799808933029326L;

        public int compare(String s1, String s2) {
            byte[] v1 = s1.value;
            byte[] v2 = s2.value;
            int n1 = s1.length();
            int n2 = s2.length();
            boolean s1IsLatin1 = s1.isLatin1();
            boolean s2IsLatin1 = s2.isLatin1();
            int min = Math.min(n1, n2);
            for (int i = 0; i < min; i++) {
                char c1 = s1IsLatin1 ? StringLatin1.getChar(v1, i) : StringUTF16.getChar(v1, i);
                char c2 = s2IsLatin1 ? StringLatin1.getChar(v2, i) : StringUTF16.getChar(v2, i);
                if (c1 != c2) {
                    c1 = Character.toUpperCase(c1);
                    c2 = Character.toUpperCase(c2);
                    if (c1 != c2) {
                        c1 = Character.toLowerCase(c1);
                        c2 = Character.toLowerCase(c2);
                        if (c1 != c2) {
                            return c1 - c2;
                        }
                    }
                }
            }
            return n1 - n2;
        }

        private Object readResolve() {
            return CASE_INSENSITIVE_ORDER;
        }
    }

    public int compareToIgnoreCase(String str) {
        return CASE_INSENSITIVE_ORDER.compare(this, str);
    }

    public boolean regionMatches(int toffset, String other, int ooffset, int len) {
        byte[] tv = value;
        byte[] ov = other.value;
        if ((ooffset < 0) || (toffset < 0) || (toffset > (long) length() - len) || (ooffset > (long) other.length() - len)) {
            return false;
        }
        if (coder() == other.coder()) {
            if (!isLatin1() && (len > 0)) {
                toffset = toffset << 1;
                ooffset = ooffset << 1;
                len = len << 1;
            }
            while (len-- > 0) {
                if (tv[toffset++] != ov[ooffset++]) {
                    return false;
                }
            }
        } else {
            if (coder() == LATIN1) {
                while (len-- > 0) {
                    if (StringLatin1.getChar(tv, toffset++) != StringUTF16.getChar(ov, ooffset++)) {
                        return false;
                    }
                }
            } else {
                while (len-- > 0) {
                    if (StringUTF16.getChar(tv, toffset++) != StringLatin1.getChar(ov, ooffset++)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len) {
        if (!ignoreCase) {
            return regionMatches(toffset, other, ooffset, len);
        }
        if ((ooffset < 0) || (toffset < 0) || (toffset > (long) length() - len) || (ooffset > (long) other.length() - len)) {
            return false;
        }
        byte[] tv = value;
        byte[] ov = other.value;
        if (coder() == other.coder()) {
            return isLatin1() ? StringLatin1.regionMatchesCI(tv, toffset, ov, ooffset, len) : StringUTF16.regionMatchesCI(tv, toffset, ov, ooffset, len);
        }
        return isLatin1() ? StringLatin1.regionMatchesCI_UTF16(tv, toffset, ov, ooffset, len) : StringUTF16.regionMatchesCI_Latin1(tv, toffset, ov, ooffset, len);
    }

    public boolean startsWith(String prefix, int toffset) {
        if (toffset < 0 || toffset > length() - prefix.length()) {
            return false;
        }
        byte[] ta = value;
        byte[] pa = prefix.value;
        int po = 0;
        int pc = pa.length;
        if (coder() == prefix.coder()) {
            int to = isLatin1() ? toffset : toffset << 1;
            while (po < pc) {
                if (ta[to++] != pa[po++]) {
                    return false;
                }
            }
        } else {
            if (isLatin1()) {
                return false;
            }
            while (po < pc) {
                if (StringUTF16.getChar(ta, toffset++) != (pa[po++] & 0xff)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean startsWith(String prefix) {
        return startsWith(prefix, 0);
    }

    public boolean endsWith(String suffix) {
        return startsWith(suffix, length() - suffix.length());
    }

    public int hashCode() {
        if (hash == 0 && value.length > 0) {
            hash = isLatin1() ? StringLatin1.hashCode(value) : StringUTF16.hashCode(value);
        }
        return hash;
    }

    public int indexOf(int ch) {
        return indexOf(ch, 0);
    }

    public int indexOf(int ch, int fromIndex) {
        return isLatin1() ? StringLatin1.indexOf(value, ch, fromIndex) : StringUTF16.indexOf(value, ch, fromIndex);
    }

    public int lastIndexOf(int ch) {
        return lastIndexOf(ch, length() - 1);
    }

    public int lastIndexOf(int ch, int fromIndex) {
        return isLatin1() ? StringLatin1.lastIndexOf(value, ch, fromIndex) : StringUTF16.lastIndexOf(value, ch, fromIndex);
    }

    public int indexOf(String str) {
        if (coder() == str.coder()) {
            return isLatin1() ? StringLatin1.indexOf(value, str.value) : StringUTF16.indexOf(value, str.value);
        }
        if (coder() == LATIN1) {
            return -1;
        }
        return StringUTF16.indexOfLatin1(value, str.value);
    }

    public int indexOf(String str, int fromIndex) {
        return indexOf(value, coder(), length(), str, fromIndex);
    }

    static int indexOf(byte[] src, byte srcCoder, int srcCount, String tgtStr, int fromIndex) {
        byte[] tgt = tgtStr.value;
        byte tgtCoder = tgtStr.coder();
        int tgtCount = tgtStr.length();
        if (fromIndex >= srcCount) {
            return (tgtCount == 0 ? srcCount : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (tgtCount == 0) {
            return fromIndex;
        }
        if (srcCoder == tgtCoder) {
            return srcCoder == LATIN1 ? StringLatin1.indexOf(src, srcCount, tgt, tgtCount, fromIndex) : StringUTF16.indexOf(src, srcCount, tgt, tgtCount, fromIndex);
        }
        if (srcCoder == LATIN1) {
            return -1;
        }
        return StringUTF16.indexOfLatin1(src, srcCount, tgt, tgtCount, fromIndex);
    }

    public int lastIndexOf(String str) {
        return lastIndexOf(str, length());
    }

    public int lastIndexOf(String str, int fromIndex) {
        return lastIndexOf(value, coder(), length(), str, fromIndex);
    }

    static int lastIndexOf(byte[] src, byte srcCoder, int srcCount, String tgtStr, int fromIndex) {
        byte[] tgt = tgtStr.value;
        byte tgtCoder = tgtStr.coder();
        int tgtCount = tgtStr.length();
        int rightIndex = srcCount - tgtCount;
        if (fromIndex < 0) {
            return -1;
        }
        if (fromIndex > rightIndex) {
            fromIndex = rightIndex;
        }
        if (tgtCount == 0) {
            return fromIndex;
        }
        if (srcCoder == tgtCoder) {
            return srcCoder == LATIN1 ? StringLatin1.lastIndexOf(src, srcCount, tgt, tgtCount, fromIndex) : StringUTF16.lastIndexOf(src, srcCount, tgt, tgtCount, fromIndex);
        }
        if (srcCoder == LATIN1) {
            return -1;
        }
        int min = tgtCount - 1;
        int i = min + fromIndex;
        int strLastIndex = tgtCount - 1;
        char strLastChar = (char) (tgt[strLastIndex] & 0xff);
        startSearchForLastChar: while (true) {
            while (i >= min && StringUTF16.getChar(src, i) != strLastChar) {
                i--;
            }
            if (i < min) {
                return -1;
            }
            int j = i - 1;
            int start = j - strLastIndex;
            int k = strLastIndex - 1;
            while (j > start) {
                if (StringUTF16.getChar(src, j--) != (tgt[k--] & 0xff)) {
                    i--;
                    continue startSearchForLastChar;
                }
            }
            return start + 1;
        }
    }

    public String substring(int beginIndex) {
        if (beginIndex < 0) {
            throw new StringIndexOutOfBoundsException(beginIndex);
        }
        int subLen = length() - beginIndex;
        if (subLen < 0) {
            throw new StringIndexOutOfBoundsException(subLen);
        }
        if (beginIndex == 0) {
            return this;
        }
        return isLatin1() ? StringLatin1.newString(value, beginIndex, subLen) : StringUTF16.newString(value, beginIndex, subLen);
    }

    public String substring(int beginIndex, int endIndex) {
        int length = length();
        checkBoundsBeginEnd(beginIndex, endIndex, length);
        int subLen = endIndex - beginIndex;
        if (beginIndex == 0 && endIndex == length) {
            return this;
        }
        return isLatin1() ? StringLatin1.newString(value, beginIndex, subLen) : StringUTF16.newString(value, beginIndex, subLen);
    }

    public CharSequence subSequence(int beginIndex, int endIndex) {
        return this.substring(beginIndex, endIndex);
    }

    public String concat(String str) {
        int olen = str.length();
        if (olen == 0) {
            return this;
        }
        if (coder() == str.coder()) {
            byte[] val = this.value;
            byte[] oval = str.value;
            int len = val.length + oval.length;
            byte[] buf = Arrays.copyOf(val, len);
            System.arraycopy(oval, 0, buf, val.length, oval.length);
            return new String(buf, coder);
        }
        int len = length();
        byte[] buf = StringUTF16.newBytesFor(len + olen);
        getBytes(buf, 0, UTF16);
        str.getBytes(buf, len, UTF16);
        return new String(buf, UTF16);
    }

    public String replace(char oldChar, char newChar) {
        if (oldChar != newChar) {
            String ret = isLatin1() ? StringLatin1.replace(value, oldChar, newChar) : StringUTF16.replace(value, oldChar, newChar);
            if (ret != null) {
                return ret;
            }
        }
        return this;
    }

    public boolean matches(String regex) {
        return Pattern.matches(regex, this);
    }

    public boolean contains(CharSequence s) {
        return indexOf(s.toString()) >= 0;
    }

    public String replaceFirst(String regex, String replacement) {
        return Pattern.compile(regex).matcher(this).replaceFirst(replacement);
    }

    public String replaceAll(String regex, String replacement) {
        return Pattern.compile(regex).matcher(this).replaceAll(replacement);
    }

    public String replace(CharSequence target, CharSequence replacement) {
        String tgtStr = target.toString();
        String replStr = replacement.toString();
        int j = indexOf(tgtStr);
        if (j < 0) {
            return this;
        }
        int tgtLen = tgtStr.length();
        int tgtLen1 = Math.max(tgtLen, 1);
        int thisLen = length();
        int newLenHint = thisLen - tgtLen + replStr.length();
        if (newLenHint < 0) {
            throw new OutOfMemoryError();
        }
        StringBuilder sb = new StringBuilder(newLenHint);
        int i = 0;
        do {
            sb.append(this, i, j).append(replStr);
            i = j + tgtLen;
        } while (j < thisLen && (j = indexOf(tgtStr, j + tgtLen1)) > 0);
        return sb.append(this, i, thisLen).toString();
    }

    public String[] split(String regex, int limit) {
        char ch = 0;
        if (((regex.length() == 1 && ".$|()[{^?*+\\".indexOf(ch = regex.charAt(0)) == -1) || (regex.length() == 2 && regex.charAt(0) == '\\' && (((ch = regex.charAt(1)) - '0') | ('9' - ch)) < 0 && ((ch - 'a') | ('z' - ch)) < 0 && ((ch - 'A') | ('Z' - ch)) < 0)) && (ch < Character.MIN_HIGH_SURROGATE || ch > Character.MAX_LOW_SURROGATE)) {
            int off = 0;
            int next = 0;
            boolean limited = limit > 0;
            ArrayList<String> list = new ArrayList<>();
            while ((next = indexOf(ch, off)) != -1) {
                if (!limited || list.size() < limit - 1) {
                    list.add(substring(off, next));
                    off = next + 1;
                } else {
                    int last = length();
                    list.add(substring(off, last));
                    off = last;
                    break;
                }
            }
            if (off == 0)
                return new String[] { this };
            if (!limited || list.size() < limit)
                list.add(substring(off, length()));
            int resultSize = list.size();
            if (limit == 0) {
                while (resultSize > 0 && list.get(resultSize - 1).length() == 0) {
                    resultSize--;
                }
            }
            String[] result = new String[resultSize];
            return list.subList(0, resultSize).toArray(result);
        }
        return Pattern.compile(regex).split(this, limit);
    }

    public String[] split(String regex) {
        return split(regex, 0);
    }

    public static String join(CharSequence delimiter, CharSequence... elements) {
        Objects.requireNonNull(delimiter);
        Objects.requireNonNull(elements);
        StringJoiner joiner = new StringJoiner(delimiter);
        for (CharSequence cs : elements) {
            joiner.add(cs);
        }
        return joiner.toString();
    }

    public static String join(CharSequence delimiter, Iterable<? extends CharSequence> elements) {
        Objects.requireNonNull(delimiter);
        Objects.requireNonNull(elements);
        StringJoiner joiner = new StringJoiner(delimiter);
        for (CharSequence cs : elements) {
            joiner.add(cs);
        }
        return joiner.toString();
    }

    public String toLowerCase(Locale locale) {
        return isLatin1() ? StringLatin1.toLowerCase(this, value, locale) : StringUTF16.toLowerCase(this, value, locale);
    }

    public String toLowerCase() {
        return toLowerCase(Locale.getDefault());
    }

    public String toUpperCase(Locale locale) {
        return isLatin1() ? StringLatin1.toUpperCase(this, value, locale) : StringUTF16.toUpperCase(this, value, locale);
    }

    public String toUpperCase() {
        return toUpperCase(Locale.getDefault());
    }

    public String trim() {
        String ret = isLatin1() ? StringLatin1.trim(value) : StringUTF16.trim(value);
        return ret == null ? this : ret;
    }

    public String toString() {
        return this;
    }

    @Override
    public IntStream chars() {
        return StreamSupport.intStream(isLatin1() ? new StringLatin1.CharsSpliterator(value, Spliterator.IMMUTABLE) : new StringUTF16.CharsSpliterator(value, Spliterator.IMMUTABLE), false);
    }

    @Override
    public IntStream codePoints() {
        return StreamSupport.intStream(isLatin1() ? new StringLatin1.CharsSpliterator(value, Spliterator.IMMUTABLE) : new StringUTF16.CodePointsSpliterator(value, Spliterator.IMMUTABLE), false);
    }

    public char[] toCharArray() {
        return isLatin1() ? StringLatin1.toChars(value) : StringUTF16.toChars(value);
    }

    public static String format(String format, Object... args) {
        return new Formatter().format(format, args).toString();
    }

    public static String format(Locale l, String format, Object... args) {
        return new Formatter(l).format(format, args).toString();
    }

    public static String valueOf(Object obj) {
        return (obj == null) ? "null" : obj.toString();
    }

    public static String valueOf(char[] data) {
        return new String(data);
    }

    public static String valueOf(char[] data, int offset, int count) {
        return new String(data, offset, count);
    }

    public static String copyValueOf(char[] data, int offset, int count) {
        return new String(data, offset, count);
    }

    public static String copyValueOf(char[] data) {
        return new String(data);
    }

    public static String valueOf(boolean b) {
        return b ? "true" : "false";
    }

    public static String valueOf(char c) {
        if (COMPACT_STRINGS && StringLatin1.canEncode(c)) {
            return new String(StringLatin1.toBytes(c), LATIN1);
        }
        return new String(StringUTF16.toBytes(c), UTF16);
    }

    public static String valueOf(int i) {
        return Integer.toString(i);
    }

    public static String valueOf(long l) {
        return Long.toString(l);
    }

    public static String valueOf(float f) {
        return Float.toString(f);
    }

    public static String valueOf(double d) {
        return Double.toString(d);
    }

    public native String intern();

    void getBytes(byte[] dst, int dstBegin, byte coder) {
        if (coder() == coder) {
            System.arraycopy(value, 0, dst, dstBegin << coder, value.length);
        } else {
            StringLatin1.inflate(value, 0, dst, dstBegin, value.length);
        }
    }

    String(char[] value, int off, int len, Void sig) {
        if (len == 0) {
            this.value = "".value;
            this.coder = "".coder;
            return;
        }
        if (COMPACT_STRINGS) {
            byte[] val = StringUTF16.compress(value, off, len);
            if (val != null) {
                this.value = val;
                this.coder = LATIN1;
                return;
            }
        }
        this.coder = UTF16;
        this.value = StringUTF16.toBytes(value, off, len);
    }

    String(AbstractStringBuilder asb, Void sig) {
        byte[] val = asb.getValue();
        int length = asb.length();
        if (asb.isLatin1()) {
            this.coder = LATIN1;
            this.value = Arrays.copyOfRange(val, 0, length);
        } else {
            if (COMPACT_STRINGS) {
                byte[] buf = StringUTF16.compress(val, 0, length);
                if (buf != null) {
                    this.coder = LATIN1;
                    this.value = buf;
                    return;
                }
            }
            this.coder = UTF16;
            this.value = Arrays.copyOfRange(val, 0, length << 1);
        }
    }

    String(byte[] value, byte coder) {
        this.value = value;
        this.coder = coder;
    }

    byte coder() {
        return COMPACT_STRINGS ? coder : UTF16;
    }

    private boolean isLatin1() {
        return COMPACT_STRINGS && coder == LATIN1;
    }

    static final byte LATIN1 = 0;

    static final byte UTF16 = 1;

    static void checkIndex(int index, int length) {
        if (index < 0 || index >= length) {
            throw new StringIndexOutOfBoundsException("index " + index);
        }
    }

    static void checkOffset(int offset, int length) {
        if (offset < 0 || offset > length) {
            throw new StringIndexOutOfBoundsException("offset " + offset + ",length " + length);
        }
    }

    private static void checkBoundsOffCount(int offset, int count, int length) {
        if (offset < 0 || count < 0 || offset > length - count) {
            throw new StringIndexOutOfBoundsException("offset " + offset + ", count " + count + ", length " + length);
        }
    }

    private static void checkBoundsBeginEnd(int begin, int end, int length) {
        if (begin < 0 || begin > end || end > length) {
            throw new StringIndexOutOfBoundsException("begin " + begin + ", end " + end + ", length " + length);
        }
    }
}