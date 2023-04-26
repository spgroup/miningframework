package org.antlr.v4.codegen;

import org.antlr.v4.Tool;
import org.antlr.v4.codegen.model.RuleFunction;
import org.antlr.v4.codegen.model.SerializedATN;
import org.antlr.v4.misc.CharSupport;
import org.antlr.v4.misc.Utils;
import org.antlr.v4.parse.ANTLRParser;
import org.antlr.v4.runtime.RuntimeMetaData;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.tool.ErrorType;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.Rule;
import org.antlr.v4.tool.ast.GrammarAST;
import org.stringtemplate.v4.NumberRenderer;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.StringRenderer;
import org.stringtemplate.v4.misc.STMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class Target {

    protected final CodeGenerator gen;

    private STGroup templates;

    protected static final Map<Character, String> defaultCharValueEscape;

    static {
        HashMap<Character, String> map = new HashMap<>();
        addEscapedChar(map, '\t', 't');
        addEscapedChar(map, '\b', 'b');
        addEscapedChar(map, '\n', 'n');
        addEscapedChar(map, '\r', 'r');
        addEscapedChar(map, '\f', 'f');
        addEscapedChar(map, '\'');
        addEscapedChar(map, '\"');
        addEscapedChar(map, '\\');
        defaultCharValueEscape = map;
    }

    protected Target(CodeGenerator gen) {
        this.gen = gen;
    }

    public Map<Character, String> getTargetCharValueEscape() {
        return defaultCharValueEscape;
    }

    protected static void addEscapedChar(HashMap<Character, String> map, char key) {
        addEscapedChar(map, key, key);
    }

    protected static void addEscapedChar(HashMap<Character, String> map, char key, char representation) {
        map.put(key, "\\" + representation);
    }

    public String getLanguage() {
        return gen.language;
    }

    public CodeGenerator getCodeGenerator() {
        return gen;
    }

    public String getVersion() {
        return Tool.VERSION;
    }

    public STGroup getTemplates() {
        if (templates == null) {
            String version = getVersion();
            if (version == null || !RuntimeMetaData.getMajorMinorVersion(version).equals(RuntimeMetaData.getMajorMinorVersion(Tool.VERSION))) {
                gen.tool.errMgr.toolError(ErrorType.INCOMPATIBLE_TOOL_AND_TEMPLATES, version, Tool.VERSION, getLanguage());
            }
            templates = loadTemplates();
        }
        return templates;
    }

    protected abstract Set<String> getReservedWords();

    public String escapeIfNeeded(String identifier) {
        return getReservedWords().contains(identifier) ? escapeWord(identifier) : identifier;
    }

    protected String escapeWord(String word) {
        return word + "_";
    }

    protected void genFile(Grammar g, ST outputFileST, String fileName) {
        getCodeGenerator().write(outputFileST, fileName);
    }

    public String getTokenTypeAsTargetLabel(Grammar g, int ttype) {
        String name = g.getTokenName(ttype);
        if (Grammar.INVALID_TOKEN_NAME.equals(name)) {
            return String.valueOf(ttype);
        }
        return name;
    }

    public String[] getTokenTypesAsTargetLabels(Grammar g, int[] ttypes) {
        String[] labels = new String[ttypes.length];
        for (int i = 0; i < ttypes.length; i++) {
            labels[i] = getTokenTypeAsTargetLabel(g, ttypes[i]);
        }
        return labels;
    }

    public String getTargetStringLiteralFromString(String s, boolean quoted) {
        if (s == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        if (quoted) {
            buf.append('"');
        }
        for (int i = 0; i < s.length(); ) {
            int c = s.codePointAt(i);
            String escaped = c <= Character.MAX_VALUE ? getTargetCharValueEscape().get((char) c) : null;
            if (c != '\'' && escaped != null) {
                buf.append(escaped);
            } else if (shouldUseUnicodeEscapeForCodePointInDoubleQuotedString(c)) {
                appendUnicodeEscapedCodePoint(i, buf);
            } else {
                buf.appendCodePoint(c);
            }
            i += Character.charCount(c);
        }
        if (quoted) {
            buf.append('"');
        }
        return buf.toString();
    }

    private void appendUnicodeEscapedCodePoint(int codePoint, StringBuilder sb, boolean escape) {
        if (escape) {
            sb.append("\\");
        }
        appendUnicodeEscapedCodePoint(codePoint, sb);
    }

    protected void appendUnicodeEscapedCodePoint(int codePoint, StringBuilder sb) {
        UnicodeEscapes.appendEscapedCodePoint(sb, codePoint, getLanguage());
    }

    public String getTargetStringLiteralFromString(String s) {
        return getTargetStringLiteralFromString(s, true);
    }

    public String getTargetStringLiteralFromANTLRStringLiteral(CodeGenerator generator, String literal, boolean addQuotes) {
        return getTargetStringLiteralFromANTLRStringLiteral(generator, literal, addQuotes, false);
    }

    public String getTargetStringLiteralFromANTLRStringLiteral(CodeGenerator generator, String literal, boolean addQuotes, boolean escapeSpecial) {
        StringBuilder sb = new StringBuilder();
        if (addQuotes)
            sb.append('"');
        for (int i = 1; i < literal.length() - 1; ) {
            int codePoint = literal.codePointAt(i);
            int toAdvance = Character.charCount(codePoint);
            if (codePoint == '\\') {
                int escapedCodePoint = literal.codePointAt(i + toAdvance);
                toAdvance++;
                switch(escapedCodePoint) {
                    case 'n':
                    case 'r':
                    case 't':
                    case 'b':
                    case 'f':
                    case '\\':
                        if (escapeSpecial && escapedCodePoint != '\\') {
                            sb.append('\\');
                        }
                        sb.append('\\');
                        sb.appendCodePoint(escapedCodePoint);
                        break;
                    case 'u':
                        if (literal.charAt(i + toAdvance) == '{') {
                            while (literal.charAt(i + toAdvance) != '}') {
                                toAdvance++;
                            }
                            toAdvance++;
                        } else {
                            toAdvance += 4;
                        }
                        if (i + toAdvance <= literal.length()) {
                            String fullEscape = literal.substring(i, i + toAdvance);
                            appendUnicodeEscapedCodePoint(CharSupport.getCharValueFromCharInGrammarLiteral(fullEscape), sb, escapeSpecial);
                        }
                        break;
                    default:
                        if (shouldUseUnicodeEscapeForCodePointInDoubleQuotedString(escapedCodePoint)) {
                            appendUnicodeEscapedCodePoint(escapedCodePoint, sb, escapeSpecial);
                        } else {
                            sb.appendCodePoint(escapedCodePoint);
                        }
                        break;
                }
            } else {
                if (codePoint == 0x22) {
                    sb.append("\\\"");
                } else if (shouldUseUnicodeEscapeForCodePointInDoubleQuotedString(codePoint)) {
                    appendUnicodeEscapedCodePoint(codePoint, sb, escapeSpecial);
                } else {
                    sb.appendCodePoint(codePoint);
                }
            }
            i += toAdvance;
        }
        if (addQuotes)
            sb.append('"');
        return sb.toString();
    }

    protected boolean shouldUseUnicodeEscapeForCodePointInDoubleQuotedString(int codePoint) {
        assert codePoint != 0x0A && codePoint != 0x22;
        return codePoint < 0x20 || codePoint == 0x5C || codePoint >= 0x7F;
    }

    public String encodeInt16AsCharEscape(int v) {
        if (v < Character.MIN_VALUE || v > Character.MAX_VALUE) {
            throw new IllegalArgumentException(String.format("Cannot encode the specified value: %d", v));
        }
        if (isATNSerializedAsInts()) {
            return Integer.toString(v);
        }
        char c = (char) v;
        String escaped = getTargetCharValueEscape().get(c);
        if (escaped != null) {
            return escaped;
        }
        switch(Character.getType(c)) {
            case Character.CONTROL:
            case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
                return escapeChar(v);
            default:
                if (v <= 127) {
                    return String.valueOf(c);
                }
                return escapeChar(v);
        }
    }

    protected String escapeChar(int v) {
        return String.format("\\u%04x", v);
    }

    public String getLoopLabel(GrammarAST ast) {
        return "loop" + ast.token.getTokenIndex();
    }

    public String getLoopCounter(GrammarAST ast) {
        return "cnt" + ast.token.getTokenIndex();
    }

    public String getListLabel(String label) {
        ST st = getTemplates().getInstanceOf("ListLabelName");
        st.add("label", label);
        return st.render();
    }

    public String getRuleFunctionContextStructName(Rule r) {
        if (r.g.isLexer()) {
            return getTemplates().getInstanceOf("LexerRuleContext").render();
        }
        return Utils.capitalize(r.name) + getTemplates().getInstanceOf("RuleContextNameSuffix").render();
    }

    public String getAltLabelContextStructName(String label) {
        return Utils.capitalize(label) + getTemplates().getInstanceOf("RuleContextNameSuffix").render();
    }

    public String getRuleFunctionContextStructName(RuleFunction function) {
        Rule r = function.rule;
        if (r.g.isLexer()) {
            return getTemplates().getInstanceOf("LexerRuleContext").render();
        }
        return Utils.capitalize(r.name) + getTemplates().getInstanceOf("RuleContextNameSuffix").render();
    }

    public String getImplicitTokenLabel(String tokenName) {
        ST st = getTemplates().getInstanceOf("ImplicitTokenLabel");
        int ttype = getCodeGenerator().g.getTokenType(tokenName);
        if (tokenName.startsWith("'")) {
            return "s" + ttype;
        }
        String text = getTokenTypeAsTargetLabel(getCodeGenerator().g, ttype);
        st.add("tokenName", text);
        return st.render();
    }

    public String getImplicitSetLabel(String id) {
        ST st = getTemplates().getInstanceOf("ImplicitSetLabel");
        st.add("id", id);
        return st.render();
    }

    public String getImplicitRuleLabel(String ruleName) {
        ST st = getTemplates().getInstanceOf("ImplicitRuleLabel");
        st.add("ruleName", ruleName);
        return st.render();
    }

    public String getElementListName(String name) {
        ST st = getTemplates().getInstanceOf("ElementListName");
        st.add("elemName", getElementName(name));
        return st.render();
    }

    public String getElementName(String name) {
        if (".".equals(name)) {
            return "_wild";
        }
        if (getCodeGenerator().g.getRule(name) != null)
            return name;
        int ttype = getCodeGenerator().g.getTokenType(name);
        if (ttype == Token.INVALID_TYPE)
            return name;
        return getTokenTypeAsTargetLabel(getCodeGenerator().g, ttype);
    }

    public String getRecognizerFileName(boolean header) {
        ST extST = getTemplates().getInstanceOf("codeFileExtension");
        String recognizerName = gen.g.getRecognizerName();
        return recognizerName + extST.render();
    }

    public String getListenerFileName(boolean header) {
        assert gen.g.name != null;
        ST extST = getTemplates().getInstanceOf("codeFileExtension");
        String listenerName = gen.g.name + "Listener";
        return listenerName + extST.render();
    }

    public String getVisitorFileName(boolean header) {
        assert gen.g.name != null;
        ST extST = getTemplates().getInstanceOf("codeFileExtension");
        String listenerName = gen.g.name + "Visitor";
        return listenerName + extST.render();
    }

    public String getBaseListenerFileName(boolean header) {
        assert gen.g.name != null;
        ST extST = getTemplates().getInstanceOf("codeFileExtension");
        String listenerName = gen.g.name + "BaseListener";
        return listenerName + extST.render();
    }

    public String getBaseVisitorFileName(boolean header) {
        assert gen.g.name != null;
        ST extST = getTemplates().getInstanceOf("codeFileExtension");
        String listenerName = gen.g.name + "BaseVisitor";
        return listenerName + extST.render();
    }

    public int getSerializedATNSegmentLimit() {
        return Integer.MAX_VALUE;
    }

    public int getInlineTestSetWordSize() {
        return 64;
    }

    public boolean grammarSymbolCausesIssueInGeneratedCode(GrammarAST idNode) {
        switch(idNode.getParent().getType()) {
            case ANTLRParser.ASSIGN:
                switch(idNode.getParent().getParent().getType()) {
                    case ANTLRParser.ELEMENT_OPTIONS:
                    case ANTLRParser.OPTIONS:
                        return false;
                    default:
                        break;
                }
                break;
            case ANTLRParser.AT:
            case ANTLRParser.ELEMENT_OPTIONS:
                return false;
            case ANTLRParser.LEXER_ACTION_CALL:
                if (idNode.getChildIndex() == 0) {
                    return false;
                }
                break;
            default:
                break;
        }
        return getReservedWords().contains(idNode.getText());
    }

    @Deprecated
    protected boolean visibleGrammarSymbolCausesIssueInGeneratedCode(GrammarAST idNode) {
        return getReservedWords().contains(idNode.getText());
    }

    public boolean templatesExist() {
        return loadTemplatesHelper(false) != null;
    }

    protected STGroup loadTemplates() {
        STGroup result = loadTemplatesHelper(true);
        if (result == null) {
            return null;
        }
        result.registerRenderer(Integer.class, new NumberRenderer());
        result.registerRenderer(String.class, new StringRenderer());
        result.setListener(new STErrorListener() {

            @Override
            public void compileTimeError(STMessage msg) {
                reportError(msg);
            }

            @Override
            public void runTimeError(STMessage msg) {
                reportError(msg);
            }

            @Override
            public void IOError(STMessage msg) {
                reportError(msg);
            }

            @Override
            public void internalError(STMessage msg) {
                reportError(msg);
            }

            private void reportError(STMessage msg) {
                getCodeGenerator().tool.errMgr.toolError(ErrorType.STRING_TEMPLATE_WARNING, msg.cause, msg.toString());
            }
        });
        return result;
    }

    private STGroup loadTemplatesHelper(boolean reportErrorIfFail) {
        String language = getLanguage();
        String groupFileName = CodeGenerator.TEMPLATE_ROOT + "/" + language + "/" + language + STGroup.GROUP_FILE_EXTENSION;
        try {
            return new STGroupFile(groupFileName);
        } catch (IllegalArgumentException iae) {
            if (reportErrorIfFail) {
                gen.tool.errMgr.toolError(ErrorType.MISSING_CODE_GEN_TEMPLATES, iae, getLanguage());
            }
            return null;
        }
    }

    public boolean wantsBaseListener() {
        return true;
    }

    public boolean wantsBaseVisitor() {
        return true;
    }

    public boolean supportsOverloadedMethods() {
        return true;
    }

    public boolean isATNSerializedAsInts() {
        return true;
    }

    public boolean needsHeader() {
        return false;
    }
}
