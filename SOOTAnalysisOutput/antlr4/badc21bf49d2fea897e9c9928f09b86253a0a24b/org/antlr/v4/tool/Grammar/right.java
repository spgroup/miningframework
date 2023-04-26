package org.antlr.v4.tool;

import org.antlr.v4.Tool;
import org.antlr.v4.analysis.LeftRecursiveRuleTransformer;
import org.antlr.v4.misc.CharSupport;
import org.antlr.v4.misc.OrderedHashMap;
import org.antlr.v4.misc.Utils;
import org.antlr.v4.parse.ANTLRParser;
import org.antlr.v4.parse.GrammarASTAdaptor;
import org.antlr.v4.parse.GrammarTreeVisitor;
import org.antlr.v4.parse.TokenVocabParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.LexerInterpreter;
import org.antlr.v4.runtime.ParserInterpreter;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ATNSerializer;
import org.antlr.v4.runtime.atn.SemanticContext;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.IntSet;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.tool.ast.ActionAST;
import org.antlr.v4.tool.ast.GrammarAST;
import org.antlr.v4.tool.ast.GrammarASTWithOptions;
import org.antlr.v4.tool.ast.GrammarRootAST;
import org.antlr.v4.tool.ast.PredAST;
import org.antlr.v4.tool.ast.RuleAST;
import org.antlr.v4.tool.ast.TerminalAST;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Grammar implements AttributeResolver {

    public static final String GRAMMAR_FROM_STRING_NAME = "<string>";

    public static final String INVALID_TOKEN_NAME = "<INVALID>";

    public static final String INVALID_RULE_NAME = "<invalid>";

    public static final Set<String> parserOptions = new HashSet<String>();

    static {
        parserOptions.add("superClass");
        parserOptions.add("TokenLabelType");
        parserOptions.add("tokenVocab");
        parserOptions.add("language");
    }

    public static final Set<String> lexerOptions = parserOptions;

    public static final Set<String> ruleOptions = new HashSet<String>();

    public static final Set<String> ParserBlockOptions = new HashSet<String>();

    public static final Set<String> LexerBlockOptions = new HashSet<String>();

    public static final Set<String> ruleRefOptions = new HashSet<String>();

    static {
        ruleRefOptions.add(LeftRecursiveRuleTransformer.PRECEDENCE_OPTION_NAME);
        ruleRefOptions.add(LeftRecursiveRuleTransformer.TOKENINDEX_OPTION_NAME);
    }

    public static final Set<String> tokenOptions = new HashSet<String>();

    static {
        tokenOptions.add("assoc");
        tokenOptions.add(LeftRecursiveRuleTransformer.TOKENINDEX_OPTION_NAME);
    }

    public static final Set<String> actionOptions = new HashSet<String>();

    public static final Set<String> semPredOptions = new HashSet<String>();

    static {
        semPredOptions.add(LeftRecursiveRuleTransformer.PRECEDENCE_OPTION_NAME);
        semPredOptions.add("fail");
    }

    public static final Set<String> doNotCopyOptionsToLexer = new HashSet<String>();

    static {
        doNotCopyOptionsToLexer.add("superClass");
        doNotCopyOptionsToLexer.add("TokenLabelType");
        doNotCopyOptionsToLexer.add("tokenVocab");
    }

    public static final Map<String, AttributeDict> grammarAndLabelRefTypeToScope = new HashMap<String, AttributeDict>();

    static {
        grammarAndLabelRefTypeToScope.put("parser:RULE_LABEL", Rule.predefinedRulePropertiesDict);
        grammarAndLabelRefTypeToScope.put("parser:TOKEN_LABEL", AttributeDict.predefinedTokenDict);
        grammarAndLabelRefTypeToScope.put("combined:RULE_LABEL", Rule.predefinedRulePropertiesDict);
        grammarAndLabelRefTypeToScope.put("combined:TOKEN_LABEL", AttributeDict.predefinedTokenDict);
    }

    public String name;

    public GrammarRootAST ast;

    @NotNull
    public final org.antlr.runtime.TokenStream tokenStream;

    public org.antlr.runtime.TokenStream originalTokenStream;

    public String text;

    public String fileName;

    public LexerGrammar implicitLexer;

    public Grammar originalGrammar;

    public Grammar parent;

    public List<Grammar> importedGrammars;

    public OrderedHashMap<String, Rule> rules = new OrderedHashMap<String, Rule>();

    public List<Rule> indexToRule = new ArrayList<Rule>();

    int ruleNumber = 0;

    int stringLiteralRuleNumber = 0;

    public ATN atn;

    public Map<Integer, Interval> stateToGrammarRegionMap;

    public Map<Integer, DFA> decisionDFAs = new HashMap<Integer, DFA>();

    public List<IntervalSet[]> decisionLOOK;

    @NotNull
    public final Tool tool;

    int maxTokenType = Token.MIN_USER_TOKEN_TYPE - 1;

    public final Map<String, Integer> tokenNameToTypeMap = new LinkedHashMap<String, Integer>();

    public final Map<String, Integer> stringLiteralToTypeMap = new LinkedHashMap<String, Integer>();

    public final List<String> typeToStringLiteralList = new ArrayList<String>();

    public final List<String> typeToTokenList = new ArrayList<String>();

    int maxChannelType = Token.MIN_USER_CHANNEL_VALUE - 1;

    public final Map<String, Integer> channelNameToValueMap = new LinkedHashMap<String, Integer>();

    public final List<String> channelValueToNameList = new ArrayList<String>();

    public Map<String, ActionAST> namedActions = new HashMap<String, ActionAST>();

    public LinkedHashMap<ActionAST, Integer> lexerActions = new LinkedHashMap<ActionAST, Integer>();

    public LinkedHashMap<PredAST, Integer> sempreds = new LinkedHashMap<PredAST, Integer>();

    public LinkedHashMap<Integer, PredAST> indexToPredMap;

    public static final String AUTO_GENERATED_TOKEN_NAME_PREFIX = "T__";

    public Grammar(Tool tool, @NotNull GrammarRootAST ast) {
        if (ast == null) {
            throw new NullPointerException("ast");
        }
        if (ast.tokenStream == null) {
            throw new IllegalArgumentException("ast must have a token stream");
        }
        this.tool = tool;
        this.ast = ast;
        this.name = (ast.getChild(0)).getText();
        this.tokenStream = ast.tokenStream;
        initTokenSymbolTables();
    }

    public Grammar(String grammarText) throws org.antlr.runtime.RecognitionException {
        this(GRAMMAR_FROM_STRING_NAME, grammarText, null);
    }

    public Grammar(String grammarText, LexerGrammar tokenVocabSource) throws org.antlr.runtime.RecognitionException {
        this(GRAMMAR_FROM_STRING_NAME, grammarText, tokenVocabSource, null);
    }

    public Grammar(String grammarText, ANTLRToolListener listener) throws org.antlr.runtime.RecognitionException {
        this(GRAMMAR_FROM_STRING_NAME, grammarText, listener);
    }

    public Grammar(String fileName, String grammarText) throws org.antlr.runtime.RecognitionException {
        this(fileName, grammarText, null);
    }

    public Grammar(String fileName, String grammarText, @Nullable ANTLRToolListener listener) throws org.antlr.runtime.RecognitionException {
        this(fileName, grammarText, null, listener);
    }

    public Grammar(String fileName, String grammarText, Grammar tokenVocabSource, @Nullable ANTLRToolListener listener) throws org.antlr.runtime.RecognitionException {
        this.text = grammarText;
        this.fileName = fileName;
        this.tool = new Tool();
        this.tool.addListener(listener);
        org.antlr.runtime.ANTLRStringStream in = new org.antlr.runtime.ANTLRStringStream(grammarText);
        in.name = fileName;
        this.ast = tool.parse(fileName, in);
        if (ast == null) {
            throw new UnsupportedOperationException();
        }
        if (ast.tokenStream == null) {
            throw new IllegalStateException("expected ast to have a token stream");
        }
        this.tokenStream = ast.tokenStream;
        final Grammar thiz = this;
        org.antlr.runtime.tree.TreeVisitor v = new org.antlr.runtime.tree.TreeVisitor(new GrammarASTAdaptor());
        v.visit(ast, new org.antlr.runtime.tree.TreeVisitorAction() {

            @Override
            public Object pre(Object t) {
                ((GrammarAST) t).g = thiz;
                return t;
            }

            @Override
            public Object post(Object t) {
                return t;
            }
        });
        initTokenSymbolTables();
        if (tokenVocabSource != null) {
            importVocab(tokenVocabSource);
        }
        tool.process(this, false);
    }

    protected void initTokenSymbolTables() {
        tokenNameToTypeMap.put("EOF", Token.EOF);
        typeToTokenList.add(null);
    }

    public void loadImportedGrammars() {
        if (ast == null)
            return;
        GrammarAST i = (GrammarAST) ast.getFirstChildWithType(ANTLRParser.IMPORT);
        if (i == null)
            return;
        importedGrammars = new ArrayList<Grammar>();
        for (Object c : i.getChildren()) {
            GrammarAST t = (GrammarAST) c;
            String importedGrammarName = null;
            if (t.getType() == ANTLRParser.ASSIGN) {
                t = (GrammarAST) t.getChild(1);
                importedGrammarName = t.getText();
            } else if (t.getType() == ANTLRParser.ID) {
                importedGrammarName = t.getText();
            }
            Grammar g;
            try {
                g = tool.loadImportedGrammar(this, t);
            } catch (IOException ioe) {
                tool.errMgr.grammarError(ErrorType.ERROR_READING_IMPORTED_GRAMMAR, importedGrammarName, t.getToken(), importedGrammarName, name);
                continue;
            }
            if (g == null)
                continue;
            g.parent = this;
            importedGrammars.add(g);
            g.loadImportedGrammars();
        }
    }

    public void defineAction(GrammarAST atAST) {
        if (atAST.getChildCount() == 2) {
            String name = atAST.getChild(0).getText();
            namedActions.put(name, (ActionAST) atAST.getChild(1));
        } else {
            String scope = atAST.getChild(0).getText();
            String gtype = getTypeString();
            if (scope.equals(gtype) || (scope.equals("parser") && gtype.equals("combined"))) {
                String name = atAST.getChild(1).getText();
                namedActions.put(name, (ActionAST) atAST.getChild(2));
            }
        }
    }

    public boolean defineRule(@NotNull Rule r) {
        if (rules.get(r.name) != null) {
            return false;
        }
        rules.put(r.name, r);
        r.index = ruleNumber++;
        indexToRule.add(r);
        return true;
    }

    public boolean undefineRule(@NotNull Rule r) {
        if (r.index < 0 || r.index >= indexToRule.size() || indexToRule.get(r.index) != r) {
            return false;
        }
        assert rules.get(r.name) == r;
        rules.remove(r.name);
        indexToRule.remove(r.index);
        for (int i = r.index; i < indexToRule.size(); i++) {
            assert indexToRule.get(i).index == i + 1;
            indexToRule.get(i).index--;
        }
        ruleNumber--;
        return true;
    }

    public Rule getRule(String name) {
        Rule r = rules.get(name);
        if (r != null)
            return r;
        return null;
    }

    public Rule getRule(int index) {
        return indexToRule.get(index);
    }

    public Rule getRule(String grammarName, String ruleName) {
        if (grammarName != null) {
            Grammar g = getImportedGrammar(grammarName);
            if (g == null) {
                return null;
            }
            return g.rules.get(ruleName);
        }
        return getRule(ruleName);
    }

    public List<Grammar> getAllImportedGrammars() {
        if (importedGrammars == null) {
            return null;
        }
        LinkedHashMap<String, Grammar> delegates = new LinkedHashMap<String, Grammar>();
        for (Grammar d : importedGrammars) {
            delegates.put(d.fileName, d);
            List<Grammar> ds = d.getAllImportedGrammars();
            if (ds != null) {
                for (Grammar imported : ds) {
                    delegates.put(imported.fileName, imported);
                }
            }
        }
        return new ArrayList<Grammar>(delegates.values());
    }

    public List<Grammar> getImportedGrammars() {
        return importedGrammars;
    }

    public LexerGrammar getImplicitLexer() {
        return implicitLexer;
    }

    public static Grammar load(String fileName) {
        Tool antlr = new Tool();
        return antlr.loadGrammar(fileName);
    }

    public List<Grammar> getGrammarAncestors() {
        Grammar root = getOutermostGrammar();
        if (this == root)
            return null;
        List<Grammar> grammars = new ArrayList<Grammar>();
        Grammar p = this.parent;
        while (p != null) {
            grammars.add(0, p);
            p = p.parent;
        }
        return grammars;
    }

    public Grammar getOutermostGrammar() {
        if (parent == null)
            return this;
        return parent.getOutermostGrammar();
    }

    public String getRecognizerName() {
        String suffix = "";
        List<Grammar> grammarsFromRootToMe = getOutermostGrammar().getGrammarAncestors();
        String qualifiedName = name;
        if (grammarsFromRootToMe != null) {
            StringBuilder buf = new StringBuilder();
            for (Grammar g : grammarsFromRootToMe) {
                buf.append(g.name);
                buf.append('_');
            }
            buf.append(name);
            qualifiedName = buf.toString();
        }
        if (isCombined() || (isLexer() && implicitLexer != null)) {
            suffix = Grammar.getGrammarTypeToFileNameSuffix(getType());
        }
        return qualifiedName + suffix;
    }

    public String getStringLiteralLexerRuleName(String lit) {
        return AUTO_GENERATED_TOKEN_NAME_PREFIX + stringLiteralRuleNumber++;
    }

    public Grammar getImportedGrammar(String name) {
        for (Grammar g : importedGrammars) {
            if (g.name.equals(name))
                return g;
        }
        return null;
    }

    public int getTokenType(String token) {
        Integer I;
        if (token.charAt(0) == '\'') {
            I = stringLiteralToTypeMap.get(token);
        } else {
            I = tokenNameToTypeMap.get(token);
        }
        int i = (I != null) ? I : Token.INVALID_TYPE;
        return i;
    }

    public String getTokenDisplayName(int ttype) {
        if (isLexer() && ttype >= Lexer.MIN_CHAR_VALUE && ttype <= Lexer.MAX_CHAR_VALUE) {
            return CharSupport.getANTLRCharLiteralForChar(ttype);
        }
        if (ttype == Token.EOF) {
            return "EOF";
        }
        if (ttype == Token.INVALID_TYPE) {
            return INVALID_TOKEN_NAME;
        }
        if (ttype >= 0 && ttype < typeToStringLiteralList.size() && typeToStringLiteralList.get(ttype) != null) {
            return typeToStringLiteralList.get(ttype);
        }
        if (ttype >= 0 && ttype < typeToTokenList.size() && typeToTokenList.get(ttype) != null) {
            return typeToTokenList.get(ttype);
        }
        return String.valueOf(ttype);
    }

    @NotNull
    public String getTokenName(int ttype) {
        if (isLexer() && ttype >= Lexer.MIN_CHAR_VALUE && ttype <= Lexer.MAX_CHAR_VALUE) {
            return CharSupport.getANTLRCharLiteralForChar(ttype);
        }
        if (ttype == Token.EOF) {
            return "EOF";
        }
        if (ttype >= 0 && ttype < typeToTokenList.size() && typeToTokenList.get(ttype) != null) {
            return typeToTokenList.get(ttype);
        }
        return INVALID_TOKEN_NAME;
    }

    public int getChannelValue(String channel) {
        Integer I = channelNameToValueMap.get(channel);
        int i = (I != null) ? I : -1;
        return i;
    }

    public String[] getRuleNames() {
        String[] result = new String[rules.size()];
        Arrays.fill(result, INVALID_RULE_NAME);
        for (Rule rule : rules.values()) {
            result[rule.index] = rule.name;
        }
        return result;
    }

    public String[] getTokenNames() {
        int numTokens = getMaxTokenType();
        String[] tokenNames = new String[numTokens + 1];
        for (int i = 0; i < tokenNames.length; i++) {
            tokenNames[i] = getTokenName(i);
        }
        return tokenNames;
    }

    public String[] getTokenDisplayNames() {
        int numTokens = getMaxTokenType();
        String[] tokenNames = new String[numTokens + 1];
        for (int i = 0; i < tokenNames.length; i++) {
            tokenNames[i] = getTokenDisplayName(i);
        }
        return tokenNames;
    }

    public String getSemanticContextDisplayString(SemanticContext semctx) {
        if (semctx instanceof SemanticContext.Predicate) {
            return getPredicateDisplayString((SemanticContext.Predicate) semctx);
        }
        if (semctx instanceof SemanticContext.AND) {
            SemanticContext.AND and = (SemanticContext.AND) semctx;
            return joinPredicateOperands(and, " and ");
        }
        if (semctx instanceof SemanticContext.OR) {
            SemanticContext.OR or = (SemanticContext.OR) semctx;
            return joinPredicateOperands(or, " or ");
        }
        return semctx.toString();
    }

    public String joinPredicateOperands(SemanticContext.Operator op, String separator) {
        StringBuilder buf = new StringBuilder();
        for (SemanticContext operand : op.getOperands()) {
            if (buf.length() > 0) {
                buf.append(separator);
            }
            buf.append(getSemanticContextDisplayString(operand));
        }
        return buf.toString();
    }

    public LinkedHashMap<Integer, PredAST> getIndexToPredicateMap() {
        LinkedHashMap<Integer, PredAST> indexToPredMap = new LinkedHashMap<Integer, PredAST>();
        for (Rule r : rules.values()) {
            for (ActionAST a : r.actions) {
                if (a instanceof PredAST) {
                    PredAST p = (PredAST) a;
                    indexToPredMap.put(sempreds.get(p), p);
                }
            }
        }
        return indexToPredMap;
    }

    public String getPredicateDisplayString(SemanticContext.Predicate pred) {
        if (indexToPredMap == null) {
            indexToPredMap = getIndexToPredicateMap();
        }
        ActionAST actionAST = indexToPredMap.get(pred.predIndex);
        return actionAST.getText();
    }

    public int getMaxCharValue() {
        return org.antlr.v4.runtime.Lexer.MAX_CHAR_VALUE;
    }

    public IntSet getTokenTypes() {
        if (isLexer()) {
            return getAllCharValues();
        }
        return IntervalSet.of(Token.MIN_USER_TOKEN_TYPE, getMaxTokenType());
    }

    public IntSet getAllCharValues() {
        return IntervalSet.of(Lexer.MIN_CHAR_VALUE, getMaxCharValue());
    }

    public int getMaxTokenType() {
        return typeToTokenList.size() - 1;
    }

    public int getNewTokenType() {
        maxTokenType++;
        return maxTokenType;
    }

    public int getNewChannelNumber() {
        maxChannelType++;
        return maxChannelType;
    }

    public void importTokensFromTokensFile() {
        String vocab = getOptionString("tokenVocab");
        if (vocab != null) {
            TokenVocabParser vparser = new TokenVocabParser(this);
            Map<String, Integer> tokens = vparser.load();
            tool.log("grammar", "tokens=" + tokens);
            for (String t : tokens.keySet()) {
                if (t.charAt(0) == '\'')
                    defineStringLiteral(t, tokens.get(t));
                else
                    defineTokenName(t, tokens.get(t));
            }
        }
    }

    public void importVocab(Grammar importG) {
        for (String tokenName : importG.tokenNameToTypeMap.keySet()) {
            defineTokenName(tokenName, importG.tokenNameToTypeMap.get(tokenName));
        }
        for (String tokenName : importG.stringLiteralToTypeMap.keySet()) {
            defineStringLiteral(tokenName, importG.stringLiteralToTypeMap.get(tokenName));
        }
        for (Map.Entry<String, Integer> channel : importG.channelNameToValueMap.entrySet()) {
            defineChannelName(channel.getKey(), channel.getValue());
        }
        int max = Math.max(this.typeToTokenList.size(), importG.typeToTokenList.size());
        Utils.setSize(typeToTokenList, max);
        for (int ttype = 0; ttype < importG.typeToTokenList.size(); ttype++) {
            maxTokenType = Math.max(maxTokenType, ttype);
            this.typeToTokenList.set(ttype, importG.typeToTokenList.get(ttype));
        }
        max = Math.max(this.channelValueToNameList.size(), importG.channelValueToNameList.size());
        Utils.setSize(channelValueToNameList, max);
        for (int channelValue = 0; channelValue < importG.channelValueToNameList.size(); channelValue++) {
            maxChannelType = Math.max(maxChannelType, channelValue);
            this.channelValueToNameList.set(channelValue, importG.channelValueToNameList.get(channelValue));
        }
    }

    public int defineTokenName(String name) {
        Integer prev = tokenNameToTypeMap.get(name);
        if (prev == null)
            return defineTokenName(name, getNewTokenType());
        return prev;
    }

    public int defineTokenName(String name, int ttype) {
        Integer prev = tokenNameToTypeMap.get(name);
        if (prev != null)
            return prev;
        tokenNameToTypeMap.put(name, ttype);
        setTokenForType(ttype, name);
        maxTokenType = Math.max(maxTokenType, ttype);
        return ttype;
    }

    public int defineStringLiteral(String lit) {
        if (stringLiteralToTypeMap.containsKey(lit)) {
            return stringLiteralToTypeMap.get(lit);
        }
        return defineStringLiteral(lit, getNewTokenType());
    }

    public int defineStringLiteral(String lit, int ttype) {
        if (!stringLiteralToTypeMap.containsKey(lit)) {
            stringLiteralToTypeMap.put(lit, ttype);
            if (ttype >= typeToStringLiteralList.size()) {
                Utils.setSize(typeToStringLiteralList, ttype + 1);
            }
            typeToStringLiteralList.set(ttype, lit);
            setTokenForType(ttype, lit);
            return ttype;
        }
        return Token.INVALID_TYPE;
    }

    public int defineTokenAlias(String name, String lit) {
        int ttype = defineTokenName(name);
        stringLiteralToTypeMap.put(lit, ttype);
        setTokenForType(ttype, name);
        return ttype;
    }

    public void setTokenForType(int ttype, String text) {
        if (ttype == Token.EOF) {
            return;
        }
        if (ttype >= typeToTokenList.size()) {
            Utils.setSize(typeToTokenList, ttype + 1);
        }
        String prevToken = typeToTokenList.get(ttype);
        if (prevToken == null || prevToken.charAt(0) == '\'') {
            typeToTokenList.set(ttype, text);
        }
    }

    public int defineChannelName(String name) {
        Integer prev = channelNameToValueMap.get(name);
        if (prev == null) {
            return defineChannelName(name, getNewChannelNumber());
        }
        return prev;
    }

    public int defineChannelName(String name, int value) {
        Integer prev = channelNameToValueMap.get(name);
        if (prev != null) {
            return prev;
        }
        channelNameToValueMap.put(name, value);
        setChannelNameForValue(value, name);
        maxChannelType = Math.max(maxChannelType, value);
        return value;
    }

    public void setChannelNameForValue(int channelValue, String name) {
        if (channelValue >= channelValueToNameList.size()) {
            Utils.setSize(channelValueToNameList, channelValue + 1);
        }
        String prevChannel = channelValueToNameList.get(channelValue);
        if (prevChannel == null) {
            channelValueToNameList.set(channelValue, name);
        }
    }

    @Override
    public Attribute resolveToAttribute(String x, ActionAST node) {
        return null;
    }

    @Override
    public Attribute resolveToAttribute(String x, String y, ActionAST node) {
        return null;
    }

    @Override
    public boolean resolvesToLabel(String x, ActionAST node) {
        return false;
    }

    @Override
    public boolean resolvesToListLabel(String x, ActionAST node) {
        return false;
    }

    @Override
    public boolean resolvesToToken(String x, ActionAST node) {
        return false;
    }

    @Override
    public boolean resolvesToAttributeDict(String x, ActionAST node) {
        return false;
    }

    public String getDefaultActionScope() {
        switch(getType()) {
            case ANTLRParser.LEXER:
                return "lexer";
            case ANTLRParser.PARSER:
            case ANTLRParser.COMBINED:
                return "parser";
        }
        return null;
    }

    public int getType() {
        if (ast != null)
            return ast.grammarType;
        return 0;
    }

    public org.antlr.runtime.TokenStream getTokenStream() {
        if (ast != null)
            return ast.tokenStream;
        return null;
    }

    public boolean isLexer() {
        return getType() == ANTLRParser.LEXER;
    }

    public boolean isParser() {
        return getType() == ANTLRParser.PARSER;
    }

    public boolean isCombined() {
        return getType() == ANTLRParser.COMBINED;
    }

    public static boolean isTokenName(String id) {
        return Character.isUpperCase(id.charAt(0));
    }

    public String getTypeString() {
        if (ast == null)
            return null;
        return ANTLRParser.tokenNames[getType()].toLowerCase();
    }

    public static String getGrammarTypeToFileNameSuffix(int type) {
        switch(type) {
            case ANTLRParser.LEXER:
                return "Lexer";
            case ANTLRParser.PARSER:
                return "Parser";
            case ANTLRParser.COMBINED:
                return "Parser";
            default:
                return "<invalid>";
        }
    }

    public String getOptionString(String key) {
        return ast.getOptionString(key);
    }

    public static void setNodeOptions(GrammarAST node, GrammarAST options) {
        if (options == null)
            return;
        GrammarASTWithOptions t = (GrammarASTWithOptions) node;
        if (t.getChildCount() == 0 || options.getChildCount() == 0)
            return;
        for (Object o : options.getChildren()) {
            GrammarAST c = (GrammarAST) o;
            if (c.getType() == ANTLRParser.ASSIGN) {
                t.setOption(c.getChild(0).getText(), (GrammarAST) c.getChild(1));
            } else {
                t.setOption(c.getText(), null);
            }
        }
    }

    public static List<Pair<GrammarAST, GrammarAST>> getStringLiteralAliasesFromLexerRules(GrammarRootAST ast) {
        String[] patterns = { "(RULE %name:TOKEN_REF (BLOCK (ALT %lit:STRING_LITERAL)))", "(RULE %name:TOKEN_REF (BLOCK (ALT %lit:STRING_LITERAL ACTION)))", "(RULE %name:TOKEN_REF (BLOCK (ALT %lit:STRING_LITERAL SEMPRED)))", "(RULE %name:TOKEN_REF (BLOCK (LEXER_ALT_ACTION (ALT %lit:STRING_LITERAL) .)))", "(RULE %name:TOKEN_REF (BLOCK (LEXER_ALT_ACTION (ALT %lit:STRING_LITERAL) . .)))", "(RULE %name:TOKEN_REF (BLOCK (LEXER_ALT_ACTION (ALT %lit:STRING_LITERAL) (LEXER_ACTION_CALL . .))))", "(RULE %name:TOKEN_REF (BLOCK (LEXER_ALT_ACTION (ALT %lit:STRING_LITERAL) . (LEXER_ACTION_CALL . .))))", "(RULE %name:TOKEN_REF (BLOCK (LEXER_ALT_ACTION (ALT %lit:STRING_LITERAL) (LEXER_ACTION_CALL . .) .)))" };
        GrammarASTAdaptor adaptor = new GrammarASTAdaptor(ast.token.getInputStream());
        org.antlr.runtime.tree.TreeWizard wiz = new org.antlr.runtime.tree.TreeWizard(adaptor, ANTLRParser.tokenNames);
        List<Pair<GrammarAST, GrammarAST>> lexerRuleToStringLiteral = new ArrayList<Pair<GrammarAST, GrammarAST>>();
        List<GrammarAST> ruleNodes = ast.getNodesWithType(ANTLRParser.RULE);
        if (ruleNodes == null || ruleNodes.isEmpty())
            return null;
        for (GrammarAST r : ruleNodes) {
            org.antlr.runtime.tree.Tree name = r.getChild(0);
            if (name.getType() == ANTLRParser.TOKEN_REF) {
                boolean isLitRule;
                for (String pattern : patterns) {
                    isLitRule = defAlias(r, pattern, wiz, lexerRuleToStringLiteral);
                    if (isLitRule)
                        break;
                }
            }
        }
        return lexerRuleToStringLiteral;
    }

    protected static boolean defAlias(GrammarAST r, String pattern, org.antlr.runtime.tree.TreeWizard wiz, List<Pair<GrammarAST, GrammarAST>> lexerRuleToStringLiteral) {
        HashMap<String, Object> nodes = new HashMap<String, Object>();
        if (wiz.parse(r, pattern, nodes)) {
            GrammarAST litNode = (GrammarAST) nodes.get("lit");
            GrammarAST nameNode = (GrammarAST) nodes.get("name");
            Pair<GrammarAST, GrammarAST> pair = new Pair<GrammarAST, GrammarAST>(nameNode, litNode);
            lexerRuleToStringLiteral.add(pair);
            return true;
        }
        return false;
    }

    public Set<String> getStringLiterals() {
        final Set<String> strings = new LinkedHashSet<String>();
        GrammarTreeVisitor collector = new GrammarTreeVisitor() {

            @Override
            public void stringRef(TerminalAST ref) {
                strings.add(ref.getText());
            }

            @Override
            public ErrorManager getErrorManager() {
                return tool.errMgr;
            }
        };
        collector.visitGrammar(ast);
        return strings;
    }

    public void setLookaheadDFA(int decision, DFA lookaheadDFA) {
        decisionDFAs.put(decision, lookaheadDFA);
    }

    public static Map<Integer, Interval> getStateToGrammarRegionMap(GrammarRootAST ast, IntervalSet grammarTokenTypes) {
        Map<Integer, Interval> stateToGrammarRegionMap = new HashMap<Integer, Interval>();
        if (ast == null)
            return stateToGrammarRegionMap;
        List<GrammarAST> nodes = ast.getNodesWithType(grammarTokenTypes);
        for (GrammarAST n : nodes) {
            if (n.atnState != null) {
                Interval tokenRegion = Interval.of(n.getTokenStartIndex(), n.getTokenStopIndex());
                org.antlr.runtime.tree.Tree ruleNode = null;
                switch(n.getType()) {
                    case ANTLRParser.RULE:
                        ruleNode = n;
                        break;
                    case ANTLRParser.BLOCK:
                    case ANTLRParser.CLOSURE:
                        ruleNode = n.getAncestor(ANTLRParser.RULE);
                        break;
                }
                if (ruleNode instanceof RuleAST) {
                    String ruleName = ((RuleAST) ruleNode).getRuleName();
                    Rule r = ast.g.getRule(ruleName);
                    if (r instanceof LeftRecursiveRule) {
                        RuleAST originalAST = ((LeftRecursiveRule) r).getOriginalAST();
                        tokenRegion = Interval.of(originalAST.getTokenStartIndex(), originalAST.getTokenStopIndex());
                    }
                }
                stateToGrammarRegionMap.put(n.atnState.stateNumber, tokenRegion);
            }
        }
        return stateToGrammarRegionMap;
    }

    public Interval getStateToGrammarRegion(int atnStateNumber) {
        if (stateToGrammarRegionMap == null) {
            stateToGrammarRegionMap = getStateToGrammarRegionMap(ast, null);
        }
        if (stateToGrammarRegionMap == null)
            return Interval.INVALID;
        return stateToGrammarRegionMap.get(atnStateNumber);
    }

    public LexerInterpreter createLexerInterpreter(CharStream input) {
        if (this.isParser()) {
            throw new IllegalStateException("A lexer interpreter can only be created for a lexer or combined grammar.");
        }
        if (this.isCombined()) {
            return implicitLexer.createLexerInterpreter(input);
        }
        char[] serializedAtn = ATNSerializer.getSerializedAsChars(atn);
        ATN deserialized = new ATNDeserializer().deserialize(serializedAtn);
        return new LexerInterpreter(fileName, Arrays.asList(getTokenDisplayNames()), Arrays.asList(getRuleNames()), ((LexerGrammar) this).modes.keySet(), deserialized, input);
    }

    public ParserInterpreter createParserInterpreter(TokenStream tokenStream) {
        if (this.isLexer()) {
            throw new IllegalStateException("A parser interpreter can only be created for a parser or combined grammar.");
        }
        char[] serializedAtn = ATNSerializer.getSerializedAsChars(atn);
        ATN deserialized = new ATNDeserializer().deserialize(serializedAtn);
        return new ParserInterpreter(fileName, Arrays.asList(getTokenDisplayNames()), Arrays.asList(getRuleNames()), deserialized, tokenStream);
    }
}
