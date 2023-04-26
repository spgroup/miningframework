package org.antlr.v4.tool;

import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.Tree;
import org.antlr.runtime.tree.TreeVisitor;
import org.antlr.runtime.tree.TreeVisitorAction;
import org.antlr.runtime.tree.TreeWizard;
import org.antlr.v4.Tool;
import org.antlr.v4.misc.CharSupport;
import org.antlr.v4.misc.OrderedHashMap;
import org.antlr.v4.misc.Utils;
import org.antlr.v4.parse.ANTLRParser;
import org.antlr.v4.parse.GrammarASTAdaptor;
import org.antlr.v4.parse.GrammarTreeVisitor;
import org.antlr.v4.parse.TokenVocabParser;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.IntSet;
import org.antlr.v4.runtime.misc.IntegerList;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.tool.ast.ActionAST;
import org.antlr.v4.tool.ast.GrammarAST;
import org.antlr.v4.tool.ast.GrammarASTWithOptions;
import org.antlr.v4.tool.ast.GrammarRootAST;
import org.antlr.v4.tool.ast.PredAST;
import org.antlr.v4.tool.ast.TerminalAST;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Grammar implements AttributeResolver {

    public static final String GRAMMAR_FROM_STRING_NAME = "<string>";

    public static final Set<String> parserOptions = new HashSet<String>() {

        {
            add("superClass");
            add("TokenLabelType");
            add("tokenVocab");
            add("language");
        }
    };

    public static final Set<String> lexerOptions = parserOptions;

    public static final Set<String> ruleOptions = new HashSet<String>() {

        {
        }
    };

    public static final Set<String> ParserBlockOptions = new HashSet<String>() {

        {
        }
    };

    public static final Set<String> LexerBlockOptions = new HashSet<String>() {

        {
        }
    };

    public static final Set<String> tokenOptions = new HashSet<String>() {

        {
            add("assoc");
        }
    };

    public static final Set<String> actionOptions = new HashSet<String>() {

        {
        }
    };

    public static final Set<String> semPredOptions = new HashSet<String>() {

        {
            add("fail");
        }
    };

    public static final Set doNotCopyOptionsToLexer = new HashSet() {

        {
            add("superClass");
            add("TokenLabelType");
            add("tokenVocab");
        }
    };

    public static Map<String, AttributeDict> grammarAndLabelRefTypeToScope = new HashMap<String, AttributeDict>() {

        {
            put("parser:RULE_LABEL", Rule.predefinedRulePropertiesDict);
            put("parser:TOKEN_LABEL", AttributeDict.predefinedTokenDict);
            put("combined:RULE_LABEL", Rule.predefinedRulePropertiesDict);
            put("combined:TOKEN_LABEL", AttributeDict.predefinedTokenDict);
        }
    };

    public String name;

    public GrammarRootAST ast;

    public TokenStream tokenStream;

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

    public Map<Integer, DFA> decisionDFAs = new HashMap<Integer, DFA>();

    public List<IntervalSet[]> decisionLOOK;

    @NotNull
    public final Tool tool;

    int maxTokenType = Token.MIN_USER_TOKEN_TYPE - 1;

    public Map<String, Integer> tokenNameToTypeMap = new LinkedHashMap<String, Integer>();

    public Map<String, Integer> stringLiteralToTypeMap = new LinkedHashMap<String, Integer>();

    public List<String> typeToStringLiteralList = new ArrayList<String>();

    public List<String> typeToTokenList = new ArrayList<String>();

    public Map<String, ActionAST> namedActions = new HashMap<String, ActionAST>();

    public LinkedHashMap<ActionAST, Integer> lexerActions = new LinkedHashMap<ActionAST, Integer>();

    public LinkedHashMap<PredAST, Integer> sempreds = new LinkedHashMap<PredAST, Integer>();

    public static final String AUTO_GENERATED_TOKEN_NAME_PREFIX = "T__";

    public Grammar(Tool tool, GrammarRootAST ast) {
        if (ast == null)
            throw new IllegalArgumentException("can't pass null tree");
        this.tool = tool;
        this.ast = ast;
        this.name = (ast.getChild(0)).getText();
        initTokenSymbolTables();
    }

    public Grammar(String grammarText) throws org.antlr.runtime.RecognitionException {
        this(GRAMMAR_FROM_STRING_NAME, grammarText, null);
    }

    public Grammar(String grammarText, ANTLRToolListener listener) throws org.antlr.runtime.RecognitionException {
        this(GRAMMAR_FROM_STRING_NAME, grammarText, listener);
    }

    public Grammar(String fileName, String grammarText) throws org.antlr.runtime.RecognitionException {
        this(fileName, grammarText, null);
    }

    public Grammar(String fileName, String grammarText, @Nullable ANTLRToolListener listener) throws org.antlr.runtime.RecognitionException {
        this.text = grammarText;
        this.fileName = fileName;
        this.tool = new Tool();
        this.tool.addListener(listener);
        org.antlr.runtime.ANTLRStringStream in = new org.antlr.runtime.ANTLRStringStream(grammarText);
        in.name = fileName;
        this.ast = tool.load(fileName, in);
        if (ast == null)
            return;
        final Grammar thiz = this;
        TreeVisitor v = new TreeVisitor(new GrammarASTAdaptor());
        v.visit(ast, new TreeVisitorAction() {

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
        tool.process(this, false);
    }

    protected void initTokenSymbolTables() {
        tokenNameToTypeMap.put("EOF", Token.EOF);
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
                importedGrammarName = t.getChild(1).getText();
                tool.log("grammar", "import " + importedGrammarName);
            } else if (t.getType() == ANTLRParser.ID) {
                importedGrammarName = t.getText();
                tool.log("grammar", "import " + t.getText());
            }
            Grammar g;
            try {
                g = tool.loadImportedGrammar(this, importedGrammarName);
            } catch (IOException ioe) {
                tool.errMgr.toolError(ErrorType.CANNOT_FIND_IMPORTED_GRAMMAR, ioe, importedGrammarName);
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

    public void defineRule(Rule r) {
        if (rules.get(r.name) != null)
            return;
        rules.put(r.name, r);
        r.index = ruleNumber++;
        indexToRule.add(r);
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
        if (importedGrammars == null)
            return null;
        List<Grammar> delegates = new ArrayList<Grammar>();
        for (Grammar d : importedGrammars) {
            delegates.add(d);
            List<Grammar> ds = d.getAllImportedGrammars();
            if (ds != null)
                delegates.addAll(ds);
        }
        return delegates;
    }

    public List<Grammar> getImportedGrammars() {
        return importedGrammars;
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
        String tokenName;
        if (isLexer() && ttype >= Lexer.MIN_CHAR_VALUE && ttype <= Lexer.MAX_CHAR_VALUE) {
            return CharSupport.getANTLRCharLiteralForChar(ttype);
        } else if (ttype == Token.EOF) {
            tokenName = "EOF";
        } else {
            if (ttype > 0 && ttype < typeToTokenList.size()) {
                tokenName = typeToTokenList.get(ttype);
                if (tokenName != null && tokenName.startsWith(AUTO_GENERATED_TOKEN_NAME_PREFIX) && ttype < typeToStringLiteralList.size() && typeToStringLiteralList.get(ttype) != null) {
                    tokenName = typeToStringLiteralList.get(ttype);
                }
            } else {
                tokenName = String.valueOf(ttype);
            }
        }
        return tokenName;
    }

    public List<String> getTokenDisplayNames(IntegerList types) {
        List<String> names = new ArrayList<String>();
        for (int t : types.toArray()) names.add(getTokenDisplayName(t));
        return names;
    }

    public String[] getTokenNames() {
        int numTokens = getMaxTokenType();
        String[] tokenNames = new String[numTokens + 1];
        for (String tokenName : tokenNameToTypeMap.keySet()) {
            Integer ttype = tokenNameToTypeMap.get(tokenName);
            if (tokenName != null && tokenName.startsWith(AUTO_GENERATED_TOKEN_NAME_PREFIX)) {
                tokenName = typeToStringLiteralList.get(ttype);
            }
            if (ttype > 0)
                tokenNames[ttype] = tokenName;
        }
        return tokenNames;
    }

    public String[] getTokenDisplayNames() {
        int numTokens = getMaxTokenType();
        String[] tokenNames = new String[numTokens + 1];
        for (String t : tokenNameToTypeMap.keySet()) {
            Integer ttype = tokenNameToTypeMap.get(t);
            if (ttype > 0)
                tokenNames[ttype] = t;
        }
        for (String t : stringLiteralToTypeMap.keySet()) {
            Integer ttype = stringLiteralToTypeMap.get(t);
            if (ttype > 0)
                tokenNames[ttype] = t;
        }
        return tokenNames;
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

    public void importTokensFromTokensFile() {
        String vocab = getOptionString("tokenVocab");
        if (vocab != null) {
            TokenVocabParser vparser = new TokenVocabParser(tool, vocab);
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
        int max = Math.max(this.typeToTokenList.size(), importG.typeToTokenList.size());
        Utils.setSize(typeToTokenList, max);
        for (int ttype = 0; ttype < importG.typeToTokenList.size(); ttype++) {
            maxTokenType = Math.max(maxTokenType, ttype);
            this.typeToTokenList.set(ttype, importG.typeToTokenList.get(ttype));
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
        if (ttype >= typeToTokenList.size()) {
            Utils.setSize(typeToTokenList, ttype + 1);
        }
        String prevToken = typeToTokenList.get(ttype);
        if (prevToken == null || prevToken.charAt(0) == '\'') {
            typeToTokenList.set(ttype, text);
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
        GrammarASTWithOptions t = (GrammarASTWithOptions) node;
        if (t.getChildCount() == 0)
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
        TreeWizard wiz = new TreeWizard(adaptor, ANTLRParser.tokenNames);
        List<Pair<GrammarAST, GrammarAST>> lexerRuleToStringLiteral = new ArrayList<Pair<GrammarAST, GrammarAST>>();
        List<GrammarAST> ruleNodes = ast.getNodesWithType(ANTLRParser.RULE);
        if (ruleNodes == null || ruleNodes.isEmpty())
            return null;
        for (GrammarAST r : ruleNodes) {
            Tree name = r.getChild(0);
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

    protected static boolean defAlias(GrammarAST r, String pattern, TreeWizard wiz, List<Pair<GrammarAST, GrammarAST>> lexerRuleToStringLiteral) {
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
        final Set<String> strings = new HashSet<String>();
        GrammarTreeVisitor collector = new GrammarTreeVisitor() {

            @Override
            public void stringRef(TerminalAST ref) {
                strings.add(ref.getText());
            }
        };
        collector.visitGrammar(ast);
        return strings;
    }

    public void setLookaheadDFA(int decision, DFA lookaheadDFA) {
        decisionDFAs.put(decision, lookaheadDFA);
    }
}
