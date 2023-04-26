package org.antlr.v4.codegen.target;

import org.antlr.v4.codegen.CodeGenerator;
import org.antlr.v4.codegen.Target;
import org.antlr.v4.tool.Grammar;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.StringRenderer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SwiftTarget extends Target {

    private static final ThreadLocal<STGroup> targetTemplates = new ThreadLocal<>();

    protected static final Map<Character, String> targetCharValueEscape;

    static {
        HashMap<Character, String> map = new HashMap<>();
        addEscapedChar(map, '\0', '0');
        addEscapedChar(map, '\\');
        addEscapedChar(map, '\t', 't');
        addEscapedChar(map, '\n', 'n');
        addEscapedChar(map, '\r', 'r');
        addEscapedChar(map, '\"');
        addEscapedChar(map, '\'');
        targetCharValueEscape = map;
    }

    protected static final HashSet<String> reservedWords = new HashSet<>(Arrays.asList("associatedtype", "class", "deinit", "enum", "extension", "func", "import", "init", "inout", "internal", "let", "operator", "private", "protocol", "public", "static", "struct", "subscript", "typealias", "var", "break", "case", "continue", "default", "defer", "do", "else", "fallthrough", "for", "guard", "if", "in", "repeat", "return", "switch", "where", "while", "as", "catch", "dynamicType", "false", "is", "nil", "rethrows", "super", "self", "Self", "throw", "throws", "true", "try", "__COLUMN__", "__FILE__", "__FUNCTION__", "__LINE__", "#column", "#file", "#function", "#line", "_", "#available", "#else", "#elseif", "#endif", "#if", "#selector", "associativity", "convenience", "dynamic", "didSet", "final", "get", "infix", "indirect", "lazy", "left", "mutating", "none", "nonmutating", "optional", "override", "postfix", "precedence", "prefix", "Protocol", "required", "right", "set", "Type", "unowned", "weak", "willSet", "rule", "parserRule"));

    public SwiftTarget(CodeGenerator gen) {
        super(gen);
    }

    @Override
    public Map<Character, String> getTargetCharValueEscape() {
        return targetCharValueEscape;
    }

    @Override
    protected Set<String> getReservedWords() {
        return reservedWords;
    }

    @Override
    protected String escapeWord(String word) {
        return "`" + word + "`";
    }

    @Override
    protected void genFile(Grammar g, ST outputFileST, String fileName) {
        super.genFile(g, outputFileST, fileName);
    }

    @Override
    protected STGroup loadTemplates() {
        STGroup result = targetTemplates.get();
        if (result == null) {
            result = super.loadTemplates();
            result.registerRenderer(String.class, new SwiftStringRenderer(), true);
            targetTemplates.set(result);
        }
        return result;
    }

    protected static class SwiftStringRenderer extends StringRenderer {

        @Override
        public String toString(Object o, String formatString, Locale locale) {
            if ("java-escape".equals(formatString)) {
                return ((String) o).replace("\\u", "\\u005Cu");
            }
            return super.toString(o, formatString, locale);
        }
    }

    @Override
    public boolean isATNSerializedAsInts() {
        return true;
    }

    @Override
    protected String escapeChar(int v) {
        return String.format("\\u{%X}", v);
    }
}