package com.sun.codemodel.internal;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JJavaName {

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

    public static boolean isFullyQualifiedClassName(String s) {
        return isJavaPackageName(s);
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

    public static String getPluralForm(String word) {
        boolean allUpper = true;
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            if (ch >= 0x80)
                return word;
            allUpper &= !Character.isLowerCase(ch);
        }
        for (Entry e : TABLE) {
            String r = e.apply(word);
            if (r != null) {
                if (allUpper)
                    r = r.toUpperCase();
                return r;
            }
        }
        return word;
    }

    private static HashSet<String> reservedKeywords = new HashSet<String>();

    static {
        String[] words = new String[] { "abstract", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false", "null", "assert", "enum" };
        for (String w : words) reservedKeywords.add(w);
    }

    private static class Entry {

        private final Pattern pattern;

        private final String replacement;

        public Entry(String pattern, String replacement) {
            this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            this.replacement = replacement;
        }

        String apply(String word) {
            Matcher m = pattern.matcher(word);
            if (m.matches()) {
                StringBuffer buf = new StringBuffer();
                m.appendReplacement(buf, replacement);
                return buf.toString();
            } else {
                return null;
            }
        }
    }

    private static final Entry[] TABLE;

    static {
        String[] source = { "(.*)child", "$1children", "(.+)fe", "$1ves", "(.*)mouse", "$1mise", "(.+)f", "$1ves", "(.+)ch", "$1ches", "(.+)sh", "$1shes", "(.*)tooth", "$1teeth", "(.+)um", "$1a", "(.+)an", "$1en", "(.+)ato", "$1atoes", "(.*)basis", "$1bases", "(.*)axis", "$1axes", "(.+)is", "$1ises", "(.+)ss", "$1sses", "(.+)us", "$1uses", "(.+)s", "$1s", "(.*)foot", "$1feet", "(.+)ix", "$1ixes", "(.+)ex", "$1ices", "(.+)nx", "$1nxes", "(.+)x", "$1xes", "(.+)y", "$1ies", "(.+)", "$1s" };
        TABLE = new Entry[source.length / 2];
        for (int i = 0; i < source.length; i += 2) {
            TABLE[i / 2] = new Entry(source[i], source[i + 1]);
        }
    }
}