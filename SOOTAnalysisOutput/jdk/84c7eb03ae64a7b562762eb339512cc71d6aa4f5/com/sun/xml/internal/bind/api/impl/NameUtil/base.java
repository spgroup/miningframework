package com.sun.xml.internal.bind.api.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

class NameUtil {

    protected boolean isPunct(char c) {
        return c == '-' || c == '.' || c == ':' || c == '_' || c == '\u00b7' || c == '\u0387' || c == '\u06dd' || c == '\u06de';
    }

    protected static boolean isDigit(char c) {
        return c >= '0' && c <= '9' || Character.isDigit(c);
    }

    protected static boolean isUpper(char c) {
        return c >= 'A' && c <= 'Z' || Character.isUpperCase(c);
    }

    protected static boolean isLower(char c) {
        return c >= 'a' && c <= 'z' || Character.isLowerCase(c);
    }

    protected boolean isLetter(char c) {
        return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || Character.isLetter(c);
    }

    private String toLowerCase(String s) {
        return s.toLowerCase(Locale.ENGLISH);
    }

    private String toUpperCase(char c) {
        return String.valueOf(c).toUpperCase(Locale.ENGLISH);
    }

    private String toUpperCase(String s) {
        return s.toUpperCase(Locale.ENGLISH);
    }

    public String capitalize(String s) {
        if (!isLower(s.charAt(0)))
            return s;
        StringBuilder sb = new StringBuilder(s.length());
        sb.append(toUpperCase(s.charAt(0)));
        sb.append(toLowerCase(s.substring(1)));
        return sb.toString();
    }

    private int nextBreak(String s, int start) {
        int n = s.length();
        char c1 = s.charAt(start);
        int t1 = classify(c1);
        for (int i = start + 1; i < n; i++) {
            int t0 = t1;
            c1 = s.charAt(i);
            t1 = classify(c1);
            switch(actionTable[t0 * 5 + t1]) {
                case ACTION_CHECK_PUNCT:
                    if (isPunct(c1))
                        return i;
                    break;
                case ACTION_CHECK_C2:
                    if (i < n - 1) {
                        char c2 = s.charAt(i + 1);
                        if (isLower(c2))
                            return i;
                    }
                    break;
                case ACTION_BREAK:
                    return i;
            }
        }
        return -1;
    }

    static protected final int UPPER_LETTER = 0;

    static protected final int LOWER_LETTER = 1;

    static protected final int OTHER_LETTER = 2;

    static protected final int DIGIT = 3;

    static protected final int OTHER = 4;

    private static final byte[] actionTable = new byte[5 * 5];

    static private final byte ACTION_CHECK_PUNCT = 0;

    static private final byte ACTION_CHECK_C2 = 1;

    static private final byte ACTION_BREAK = 2;

    static private final byte ACTION_NOBREAK = 3;

    private static byte decideAction(int t0, int t1) {
        if (t0 == OTHER && t1 == OTHER)
            return ACTION_CHECK_PUNCT;
        if (!xor(t0 == DIGIT, t1 == DIGIT))
            return ACTION_BREAK;
        if (t0 == LOWER_LETTER && t1 != LOWER_LETTER)
            return ACTION_BREAK;
        if (!xor(t0 <= OTHER_LETTER, t1 <= OTHER_LETTER))
            return ACTION_BREAK;
        if (!xor(t0 == OTHER_LETTER, t1 == OTHER_LETTER))
            return ACTION_BREAK;
        if (t0 == UPPER_LETTER && t1 == UPPER_LETTER)
            return ACTION_CHECK_C2;
        return ACTION_NOBREAK;
    }

    private static boolean xor(boolean x, boolean y) {
        return (x && y) || (!x && !y);
    }

    static {
        for (int t0 = 0; t0 < 5; t0++) for (int t1 = 0; t1 < 5; t1++) actionTable[t0 * 5 + t1] = decideAction(t0, t1);
    }

    protected int classify(char c0) {
        switch(Character.getType(c0)) {
            case Character.UPPERCASE_LETTER:
                return UPPER_LETTER;
            case Character.LOWERCASE_LETTER:
                return LOWER_LETTER;
            case Character.TITLECASE_LETTER:
            case Character.MODIFIER_LETTER:
            case Character.OTHER_LETTER:
                return OTHER_LETTER;
            case Character.DECIMAL_DIGIT_NUMBER:
                return DIGIT;
            default:
                return OTHER;
        }
    }

    public List<String> toWordList(String s) {
        ArrayList<String> ss = new ArrayList<String>();
        int n = s.length();
        for (int i = 0; i < n; ) {
            while (i < n) {
                if (!isPunct(s.charAt(i)))
                    break;
                i++;
            }
            if (i >= n)
                break;
            int b = nextBreak(s, i);
            String w = (b == -1) ? s.substring(i) : s.substring(i, b);
            ss.add(escape(capitalize(w)));
            if (b == -1)
                break;
            i = b;
        }
        return ss;
    }

    protected String toMixedCaseName(List<String> ss, boolean startUpper) {
        StringBuilder sb = new StringBuilder();
        if (!ss.isEmpty()) {
            sb.append(startUpper ? ss.get(0) : toLowerCase(ss.get(0)));
            for (int i = 1; i < ss.size(); i++) sb.append(ss.get(i));
        }
        return sb.toString();
    }

    protected String toMixedCaseVariableName(String[] ss, boolean startUpper, boolean cdrUpper) {
        if (cdrUpper)
            for (int i = 1; i < ss.length; i++) ss[i] = capitalize(ss[i]);
        StringBuilder sb = new StringBuilder();
        if (ss.length > 0) {
            sb.append(startUpper ? ss[0] : toLowerCase(ss[0]));
            for (int i = 1; i < ss.length; i++) sb.append(ss[i]);
        }
        return sb.toString();
    }

    public String toConstantName(String s) {
        return toConstantName(toWordList(s));
    }

    public String toConstantName(List<String> ss) {
        StringBuilder sb = new StringBuilder();
        if (!ss.isEmpty()) {
            sb.append(toUpperCase(ss.get(0)));
            for (int i = 1; i < ss.size(); i++) {
                sb.append('_');
                sb.append(toUpperCase(ss.get(i)));
            }
        }
        return sb.toString();
    }

    public static void escape(StringBuilder sb, String s, int start) {
        int n = s.length();
        for (int i = start; i < n; i++) {
            char c = s.charAt(i);
            if (Character.isJavaIdentifierPart(c))
                sb.append(c);
            else {
                sb.append('_');
                if (c <= '\u000f')
                    sb.append("000");
                else if (c <= '\u00ff')
                    sb.append("00");
                else if (c <= '\u0fff')
                    sb.append('0');
                sb.append(Integer.toString(c, 16));
            }
        }
    }

    private static String escape(String s) {
        int n = s.length();
        for (int i = 0; i < n; i++) if (!Character.isJavaIdentifierPart(s.charAt(i))) {
            StringBuilder sb = new StringBuilder(s.substring(0, i));
            escape(sb, s, i);
            return sb.toString();
        }
        return s;
    }

    public static boolean isJavaIdentifier(String s) {
        if (s.length() == 0)
            return false;
        if (reservedKeywords.contains(s))
            return false;
        if (!Character.isJavaIdentifierStart(s.charAt(0)))
            return false;
        for (int i = 1; i < s.length(); i++) if (!Character.isJavaIdentifierPart(s.charAt(i)))
            return false;
        return true;
    }

    public static boolean isJavaPackageName(String s) {
        while (s.length() != 0) {
            int idx = s.indexOf('.');
            if (idx == -1)
                idx = s.length();
            if (!isJavaIdentifier(s.substring(0, idx)))
                return false;
            s = s.substring(idx);
            if (s.length() != 0)
                s = s.substring(1);
        }
        return true;
    }

    private static HashSet<String> reservedKeywords = new HashSet<String>();

    static {
        String[] words = new String[] { "abstract", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false", "null", "assert", "enum" };
        for (String word : words) reservedKeywords.add(word);
    }
}
