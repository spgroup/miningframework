package org.elasticsearch.painless.antlr;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({ "all", "warnings", "unchecked", "unused", "cast" })
class PainlessParser extends Parser {

    static {
        RuntimeMetaData.checkVersion("4.5.1", RuntimeMetaData.VERSION);
    }

    protected static final DFA[] _decisionToDFA;

    protected static final PredictionContextCache _sharedContextCache = new PredictionContextCache();

    public static final int WS = 1, COMMENT = 2, LBRACK = 3, RBRACK = 4, LBRACE = 5, RBRACE = 6, LP = 7, RP = 8, DOT = 9, COMMA = 10, SEMICOLON = 11, IF = 12, ELSE = 13, WHILE = 14, DO = 15, FOR = 16, CONTINUE = 17, BREAK = 18, RETURN = 19, NEW = 20, TRY = 21, CATCH = 22, THROW = 23, BOOLNOT = 24, BWNOT = 25, MUL = 26, DIV = 27, REM = 28, ADD = 29, SUB = 30, LSH = 31, RSH = 32, USH = 33, LT = 34, LTE = 35, GT = 36, GTE = 37, EQ = 38, EQR = 39, NE = 40, NER = 41, BWAND = 42, XOR = 43, BWOR = 44, BOOLAND = 45, BOOLOR = 46, COND = 47, COLON = 48, REF = 49, ARROW = 50, INCR = 51, DECR = 52, ASSIGN = 53, AADD = 54, ASUB = 55, AMUL = 56, ADIV = 57, AREM = 58, AAND = 59, AXOR = 60, AOR = 61, ALSH = 62, ARSH = 63, AUSH = 64, OCTAL = 65, HEX = 66, INTEGER = 67, DECIMAL = 68, STRING = 69, TRUE = 70, FALSE = 71, NULL = 72, TYPE = 73, ID = 74, DOTINTEGER = 75, DOTID = 76;

    public static final int RULE_source = 0, RULE_function = 1, RULE_parameters = 2, RULE_statement = 3, RULE_trailer = 4, RULE_block = 5, RULE_empty = 6, RULE_initializer = 7, RULE_afterthought = 8, RULE_declaration = 9, RULE_decltype = 10, RULE_declvar = 11, RULE_trap = 12, RULE_delimiter = 13, RULE_expression = 14, RULE_unary = 15, RULE_chain = 16, RULE_primary = 17, RULE_secondary = 18, RULE_dot = 19, RULE_brace = 20, RULE_arguments = 21, RULE_argument = 22, RULE_lambda = 23, RULE_lamtype = 24, RULE_funcref = 25;

    public static final String[] ruleNames = { "source", "function", "parameters", "statement", "trailer", "block", "empty", "initializer", "afterthought", "declaration", "decltype", "declvar", "trap", "delimiter", "expression", "unary", "chain", "primary", "secondary", "dot", "brace", "arguments", "argument", "lambda", "lamtype", "funcref" };

    private static final String[] _LITERAL_NAMES = { null, null, null, "'{'", "'}'", "'['", "']'", "'('", "')'", "'.'", "','", "';'", "'if'", "'else'", "'while'", "'do'", "'for'", "'continue'", "'break'", "'return'", "'new'", "'try'", "'catch'", "'throw'", "'!'", "'~'", "'*'", "'/'", "'%'", "'+'", "'-'", "'<<'", "'>>'", "'>>>'", "'<'", "'<='", "'>'", "'>='", "'=='", "'==='", "'!='", "'!=='", "'&'", "'^'", "'|'", "'&&'", "'||'", "'?'", "':'", "'::'", "'->'", "'++'", "'--'", "'='", "'+='", "'-='", "'*='", "'/='", "'%='", "'&='", "'^='", "'|='", "'<<='", "'>>='", "'>>>='", null, null, null, null, null, "'true'", "'false'", "'null'" };

    private static final String[] _SYMBOLIC_NAMES = { null, "WS", "COMMENT", "LBRACK", "RBRACK", "LBRACE", "RBRACE", "LP", "RP", "DOT", "COMMA", "SEMICOLON", "IF", "ELSE", "WHILE", "DO", "FOR", "CONTINUE", "BREAK", "RETURN", "NEW", "TRY", "CATCH", "THROW", "BOOLNOT", "BWNOT", "MUL", "DIV", "REM", "ADD", "SUB", "LSH", "RSH", "USH", "LT", "LTE", "GT", "GTE", "EQ", "EQR", "NE", "NER", "BWAND", "XOR", "BWOR", "BOOLAND", "BOOLOR", "COND", "COLON", "REF", "ARROW", "INCR", "DECR", "ASSIGN", "AADD", "ASUB", "AMUL", "ADIV", "AREM", "AAND", "AXOR", "AOR", "ALSH", "ARSH", "AUSH", "OCTAL", "HEX", "INTEGER", "DECIMAL", "STRING", "TRUE", "FALSE", "NULL", "TYPE", "ID", "DOTINTEGER", "DOTID" };

    public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

    @Deprecated
    public static final String[] tokenNames;

    static {
        tokenNames = new String[_SYMBOLIC_NAMES.length];
        for (int i = 0; i < tokenNames.length; i++) {
            tokenNames[i] = VOCABULARY.getLiteralName(i);
            if (tokenNames[i] == null) {
                tokenNames[i] = VOCABULARY.getSymbolicName(i);
            }
            if (tokenNames[i] == null) {
                tokenNames[i] = "<INVALID>";
            }
        }
    }

    @Override
    @Deprecated
    public String[] getTokenNames() {
        return tokenNames;
    }

    @Override
    public Vocabulary getVocabulary() {
        return VOCABULARY;
    }

    @Override
    public String getGrammarFileName() {
        return "PainlessParser.g4";
    }

    @Override
    public String[] getRuleNames() {
        return ruleNames;
    }

    @Override
    public String getSerializedATN() {
        return _serializedATN;
    }

    @Override
    public ATN getATN() {
        return _ATN;
    }

    public PainlessParser(TokenStream input) {
        super(input);
        _interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
    }

    public static class SourceContext extends ParserRuleContext {

        public TerminalNode EOF() {
            return getToken(PainlessParser.EOF, 0);
        }

        public List<FunctionContext> function() {
            return getRuleContexts(FunctionContext.class);
        }

        public FunctionContext function(int i) {
            return getRuleContext(FunctionContext.class, i);
        }

        public List<StatementContext> statement() {
            return getRuleContexts(StatementContext.class);
        }

        public StatementContext statement(int i) {
            return getRuleContext(StatementContext.class, i);
        }

        public SourceContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_source;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitSource(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final SourceContext source() throws RecognitionException {
        SourceContext _localctx = new SourceContext(_ctx, getState());
        enterRule(_localctx, 0, RULE_source);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(55);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 0, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(52);
                                function();
                            }
                        }
                    }
                    setState(57);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 0, _ctx);
                }
                setState(61);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 1, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(58);
                                statement();
                            }
                        }
                    }
                    setState(63);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 1, _ctx);
                }
                setState(64);
                match(EOF);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class FunctionContext extends ParserRuleContext {

        public DecltypeContext decltype() {
            return getRuleContext(DecltypeContext.class, 0);
        }

        public TerminalNode ID() {
            return getToken(PainlessParser.ID, 0);
        }

        public ParametersContext parameters() {
            return getRuleContext(ParametersContext.class, 0);
        }

        public BlockContext block() {
            return getRuleContext(BlockContext.class, 0);
        }

        public FunctionContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_function;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitFunction(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final FunctionContext function() throws RecognitionException {
        FunctionContext _localctx = new FunctionContext(_ctx, getState());
        enterRule(_localctx, 2, RULE_function);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(66);
                decltype();
                setState(67);
                match(ID);
                setState(68);
                parameters();
                setState(69);
                block();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class ParametersContext extends ParserRuleContext {

        public TerminalNode LP() {
            return getToken(PainlessParser.LP, 0);
        }

        public TerminalNode RP() {
            return getToken(PainlessParser.RP, 0);
        }

        public List<DecltypeContext> decltype() {
            return getRuleContexts(DecltypeContext.class);
        }

        public DecltypeContext decltype(int i) {
            return getRuleContext(DecltypeContext.class, i);
        }

        public List<TerminalNode> ID() {
            return getTokens(PainlessParser.ID);
        }

        public TerminalNode ID(int i) {
            return getToken(PainlessParser.ID, i);
        }

        public List<TerminalNode> COMMA() {
            return getTokens(PainlessParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(PainlessParser.COMMA, i);
        }

        public ParametersContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_parameters;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitParameters(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final ParametersContext parameters() throws RecognitionException {
        ParametersContext _localctx = new ParametersContext(_ctx, getState());
        enterRule(_localctx, 4, RULE_parameters);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(71);
                match(LP);
                setState(83);
                _la = _input.LA(1);
                if (_la == TYPE) {
                    {
                        setState(72);
                        decltype();
                        setState(73);
                        match(ID);
                        setState(80);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                {
                                    setState(74);
                                    match(COMMA);
                                    setState(75);
                                    decltype();
                                    setState(76);
                                    match(ID);
                                }
                            }
                            setState(82);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                    }
                }
                setState(85);
                match(RP);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class StatementContext extends ParserRuleContext {

        public StatementContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_statement;
        }

        public StatementContext() {
        }

        public void copyFrom(StatementContext ctx) {
            super.copyFrom(ctx);
        }
    }

    public static class DeclContext extends StatementContext {

        public DeclarationContext declaration() {
            return getRuleContext(DeclarationContext.class, 0);
        }

        public DelimiterContext delimiter() {
            return getRuleContext(DelimiterContext.class, 0);
        }

        public DeclContext(StatementContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitDecl(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class BreakContext extends StatementContext {

        public TerminalNode BREAK() {
            return getToken(PainlessParser.BREAK, 0);
        }

        public DelimiterContext delimiter() {
            return getRuleContext(DelimiterContext.class, 0);
        }

        public BreakContext(StatementContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitBreak(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class ThrowContext extends StatementContext {

        public TerminalNode THROW() {
            return getToken(PainlessParser.THROW, 0);
        }

        public ExpressionContext expression() {
            return getRuleContext(ExpressionContext.class, 0);
        }

        public DelimiterContext delimiter() {
            return getRuleContext(DelimiterContext.class, 0);
        }

        public ThrowContext(StatementContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitThrow(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class ContinueContext extends StatementContext {

        public TerminalNode CONTINUE() {
            return getToken(PainlessParser.CONTINUE, 0);
        }

        public DelimiterContext delimiter() {
            return getRuleContext(DelimiterContext.class, 0);
        }

        public ContinueContext(StatementContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitContinue(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class ForContext extends StatementContext {

        public TerminalNode FOR() {
            return getToken(PainlessParser.FOR, 0);
        }

        public TerminalNode LP() {
            return getToken(PainlessParser.LP, 0);
        }

        public List<TerminalNode> SEMICOLON() {
            return getTokens(PainlessParser.SEMICOLON);
        }

        public TerminalNode SEMICOLON(int i) {
            return getToken(PainlessParser.SEMICOLON, i);
        }

        public TerminalNode RP() {
            return getToken(PainlessParser.RP, 0);
        }

        public TrailerContext trailer() {
            return getRuleContext(TrailerContext.class, 0);
        }

        public EmptyContext empty() {
            return getRuleContext(EmptyContext.class, 0);
        }

        public InitializerContext initializer() {
            return getRuleContext(InitializerContext.class, 0);
        }

        public ExpressionContext expression() {
            return getRuleContext(ExpressionContext.class, 0);
        }

        public AfterthoughtContext afterthought() {
            return getRuleContext(AfterthoughtContext.class, 0);
        }

        public ForContext(StatementContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitFor(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class TryContext extends StatementContext {

        public TerminalNode TRY() {
            return getToken(PainlessParser.TRY, 0);
        }

        public BlockContext block() {
            return getRuleContext(BlockContext.class, 0);
        }

        public List<TrapContext> trap() {
            return getRuleContexts(TrapContext.class);
        }

        public TrapContext trap(int i) {
            return getRuleContext(TrapContext.class, i);
        }

        public TryContext(StatementContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitTry(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class ExprContext extends StatementContext {

        public ExpressionContext expression() {
            return getRuleContext(ExpressionContext.class, 0);
        }

        public DelimiterContext delimiter() {
            return getRuleContext(DelimiterContext.class, 0);
        }

        public ExprContext(StatementContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitExpr(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class DoContext extends StatementContext {

        public TerminalNode DO() {
            return getToken(PainlessParser.DO, 0);
        }

        public BlockContext block() {
            return getRuleContext(BlockContext.class, 0);
        }

        public TerminalNode WHILE() {
            return getToken(PainlessParser.WHILE, 0);
        }

        public TerminalNode LP() {
            return getToken(PainlessParser.LP, 0);
        }

        public ExpressionContext expression() {
            return getRuleContext(ExpressionContext.class, 0);
        }

        public TerminalNode RP() {
            return getToken(PainlessParser.RP, 0);
        }

        public DelimiterContext delimiter() {
            return getRuleContext(DelimiterContext.class, 0);
        }

        public DoContext(StatementContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitDo(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class WhileContext extends StatementContext {

        public TerminalNode WHILE() {
            return getToken(PainlessParser.WHILE, 0);
        }

        public TerminalNode LP() {
            return getToken(PainlessParser.LP, 0);
        }

        public ExpressionContext expression() {
            return getRuleContext(ExpressionContext.class, 0);
        }

        public TerminalNode RP() {
            return getToken(PainlessParser.RP, 0);
        }

        public TrailerContext trailer() {
            return getRuleContext(TrailerContext.class, 0);
        }

        public EmptyContext empty() {
            return getRuleContext(EmptyContext.class, 0);
        }

        public WhileContext(StatementContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitWhile(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class IfContext extends StatementContext {

        public TerminalNode IF() {
            return getToken(PainlessParser.IF, 0);
        }

        public TerminalNode LP() {
            return getToken(PainlessParser.LP, 0);
        }

        public ExpressionContext expression() {
            return getRuleContext(ExpressionContext.class, 0);
        }

        public TerminalNode RP() {
            return getToken(PainlessParser.RP, 0);
        }

        public List<TrailerContext> trailer() {
            return getRuleContexts(TrailerContext.class);
        }

        public TrailerContext trailer(int i) {
            return getRuleContext(TrailerContext.class, i);
        }

        public TerminalNode ELSE() {
            return getToken(PainlessParser.ELSE, 0);
        }

        public IfContext(StatementContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitIf(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class EachContext extends StatementContext {

        public TerminalNode FOR() {
            return getToken(PainlessParser.FOR, 0);
        }

        public TerminalNode LP() {
            return getToken(PainlessParser.LP, 0);
        }

        public DecltypeContext decltype() {
            return getRuleContext(DecltypeContext.class, 0);
        }

        public TerminalNode ID() {
            return getToken(PainlessParser.ID, 0);
        }

        public TerminalNode COLON() {
            return getToken(PainlessParser.COLON, 0);
        }

        public ExpressionContext expression() {
            return getRuleContext(ExpressionContext.class, 0);
        }

        public TerminalNode RP() {
            return getToken(PainlessParser.RP, 0);
        }

        public TrailerContext trailer() {
            return getRuleContext(TrailerContext.class, 0);
        }

        public EachContext(StatementContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitEach(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class ReturnContext extends StatementContext {

        public TerminalNode RETURN() {
            return getToken(PainlessParser.RETURN, 0);
        }

        public ExpressionContext expression() {
            return getRuleContext(ExpressionContext.class, 0);
        }

        public DelimiterContext delimiter() {
            return getRuleContext(DelimiterContext.class, 0);
        }

        public ReturnContext(StatementContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitReturn(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final StatementContext statement() throws RecognitionException {
        StatementContext _localctx = new StatementContext(_ctx, getState());
        enterRule(_localctx, 6, RULE_statement);
        try {
            int _alt;
            setState(165);
            switch(getInterpreter().adaptivePredict(_input, 11, _ctx)) {
                case 1:
                    _localctx = new IfContext(_localctx);
                    enterOuterAlt(_localctx, 1);
                    {
                        setState(87);
                        match(IF);
                        setState(88);
                        match(LP);
                        setState(89);
                        expression(0);
                        setState(90);
                        match(RP);
                        setState(91);
                        trailer();
                        setState(95);
                        switch(getInterpreter().adaptivePredict(_input, 4, _ctx)) {
                            case 1:
                                {
                                    setState(92);
                                    match(ELSE);
                                    setState(93);
                                    trailer();
                                }
                                break;
                            case 2:
                                {
                                    setState(94);
                                    if (!(_input.LA(1) != ELSE))
                                        throw new FailedPredicateException(this, " _input.LA(1) != ELSE ");
                                }
                                break;
                        }
                    }
                    break;
                case 2:
                    _localctx = new WhileContext(_localctx);
                    enterOuterAlt(_localctx, 2);
                    {
                        setState(97);
                        match(WHILE);
                        setState(98);
                        match(LP);
                        setState(99);
                        expression(0);
                        setState(100);
                        match(RP);
                        setState(103);
                        switch(getInterpreter().adaptivePredict(_input, 5, _ctx)) {
                            case 1:
                                {
                                    setState(101);
                                    trailer();
                                }
                                break;
                            case 2:
                                {
                                    setState(102);
                                    empty();
                                }
                                break;
                        }
                    }
                    break;
                case 3:
                    _localctx = new DoContext(_localctx);
                    enterOuterAlt(_localctx, 3);
                    {
                        setState(105);
                        match(DO);
                        setState(106);
                        block();
                        setState(107);
                        match(WHILE);
                        setState(108);
                        match(LP);
                        setState(109);
                        expression(0);
                        setState(110);
                        match(RP);
                        setState(111);
                        delimiter();
                    }
                    break;
                case 4:
                    _localctx = new ForContext(_localctx);
                    enterOuterAlt(_localctx, 4);
                    {
                        setState(113);
                        match(FOR);
                        setState(114);
                        match(LP);
                        setState(116);
                        switch(getInterpreter().adaptivePredict(_input, 6, _ctx)) {
                            case 1:
                                {
                                    setState(115);
                                    initializer();
                                }
                                break;
                        }
                        setState(118);
                        match(SEMICOLON);
                        setState(120);
                        switch(getInterpreter().adaptivePredict(_input, 7, _ctx)) {
                            case 1:
                                {
                                    setState(119);
                                    expression(0);
                                }
                                break;
                        }
                        setState(122);
                        match(SEMICOLON);
                        setState(124);
                        switch(getInterpreter().adaptivePredict(_input, 8, _ctx)) {
                            case 1:
                                {
                                    setState(123);
                                    afterthought();
                                }
                                break;
                        }
                        setState(126);
                        match(RP);
                        setState(129);
                        switch(getInterpreter().adaptivePredict(_input, 9, _ctx)) {
                            case 1:
                                {
                                    setState(127);
                                    trailer();
                                }
                                break;
                            case 2:
                                {
                                    setState(128);
                                    empty();
                                }
                                break;
                        }
                    }
                    break;
                case 5:
                    _localctx = new EachContext(_localctx);
                    enterOuterAlt(_localctx, 5);
                    {
                        setState(131);
                        match(FOR);
                        setState(132);
                        match(LP);
                        setState(133);
                        decltype();
                        setState(134);
                        match(ID);
                        setState(135);
                        match(COLON);
                        setState(136);
                        expression(0);
                        setState(137);
                        match(RP);
                        setState(138);
                        trailer();
                    }
                    break;
                case 6:
                    _localctx = new DeclContext(_localctx);
                    enterOuterAlt(_localctx, 6);
                    {
                        setState(140);
                        declaration();
                        setState(141);
                        delimiter();
                    }
                    break;
                case 7:
                    _localctx = new ContinueContext(_localctx);
                    enterOuterAlt(_localctx, 7);
                    {
                        setState(143);
                        match(CONTINUE);
                        setState(144);
                        delimiter();
                    }
                    break;
                case 8:
                    _localctx = new BreakContext(_localctx);
                    enterOuterAlt(_localctx, 8);
                    {
                        setState(145);
                        match(BREAK);
                        setState(146);
                        delimiter();
                    }
                    break;
                case 9:
                    _localctx = new ReturnContext(_localctx);
                    enterOuterAlt(_localctx, 9);
                    {
                        setState(147);
                        match(RETURN);
                        setState(148);
                        expression(0);
                        setState(149);
                        delimiter();
                    }
                    break;
                case 10:
                    _localctx = new TryContext(_localctx);
                    enterOuterAlt(_localctx, 10);
                    {
                        setState(151);
                        match(TRY);
                        setState(152);
                        block();
                        setState(154);
                        _errHandler.sync(this);
                        _alt = 1;
                        do {
                            switch(_alt) {
                                case 1:
                                    {
                                        {
                                            setState(153);
                                            trap();
                                        }
                                    }
                                    break;
                                default:
                                    throw new NoViableAltException(this);
                            }
                            setState(156);
                            _errHandler.sync(this);
                            _alt = getInterpreter().adaptivePredict(_input, 10, _ctx);
                        } while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER);
                    }
                    break;
                case 11:
                    _localctx = new ThrowContext(_localctx);
                    enterOuterAlt(_localctx, 11);
                    {
                        setState(158);
                        match(THROW);
                        setState(159);
                        expression(0);
                        setState(160);
                        delimiter();
                    }
                    break;
                case 12:
                    _localctx = new ExprContext(_localctx);
                    enterOuterAlt(_localctx, 12);
                    {
                        setState(162);
                        expression(0);
                        setState(163);
                        delimiter();
                    }
                    break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class TrailerContext extends ParserRuleContext {

        public BlockContext block() {
            return getRuleContext(BlockContext.class, 0);
        }

        public StatementContext statement() {
            return getRuleContext(StatementContext.class, 0);
        }

        public TrailerContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_trailer;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitTrailer(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final TrailerContext trailer() throws RecognitionException {
        TrailerContext _localctx = new TrailerContext(_ctx, getState());
        enterRule(_localctx, 8, RULE_trailer);
        try {
            setState(169);
            switch(getInterpreter().adaptivePredict(_input, 12, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                    {
                        setState(167);
                        block();
                    }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                    {
                        setState(168);
                        statement();
                    }
                    break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class BlockContext extends ParserRuleContext {

        public TerminalNode LBRACK() {
            return getToken(PainlessParser.LBRACK, 0);
        }

        public TerminalNode RBRACK() {
            return getToken(PainlessParser.RBRACK, 0);
        }

        public List<StatementContext> statement() {
            return getRuleContexts(StatementContext.class);
        }

        public StatementContext statement(int i) {
            return getRuleContext(StatementContext.class, i);
        }

        public BlockContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_block;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitBlock(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final BlockContext block() throws RecognitionException {
        BlockContext _localctx = new BlockContext(_ctx, getState());
        enterRule(_localctx, 10, RULE_block);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(171);
                match(LBRACK);
                setState(175);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 13, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(172);
                                statement();
                            }
                        }
                    }
                    setState(177);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 13, _ctx);
                }
                setState(178);
                match(RBRACK);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class EmptyContext extends ParserRuleContext {

        public TerminalNode SEMICOLON() {
            return getToken(PainlessParser.SEMICOLON, 0);
        }

        public EmptyContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_empty;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitEmpty(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final EmptyContext empty() throws RecognitionException {
        EmptyContext _localctx = new EmptyContext(_ctx, getState());
        enterRule(_localctx, 12, RULE_empty);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(180);
                match(SEMICOLON);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class InitializerContext extends ParserRuleContext {

        public DeclarationContext declaration() {
            return getRuleContext(DeclarationContext.class, 0);
        }

        public ExpressionContext expression() {
            return getRuleContext(ExpressionContext.class, 0);
        }

        public InitializerContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_initializer;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitInitializer(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final InitializerContext initializer() throws RecognitionException {
        InitializerContext _localctx = new InitializerContext(_ctx, getState());
        enterRule(_localctx, 14, RULE_initializer);
        try {
            setState(184);
            switch(getInterpreter().adaptivePredict(_input, 14, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                    {
                        setState(182);
                        declaration();
                    }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                    {
                        setState(183);
                        expression(0);
                    }
                    break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class AfterthoughtContext extends ParserRuleContext {

        public ExpressionContext expression() {
            return getRuleContext(ExpressionContext.class, 0);
        }

        public AfterthoughtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_afterthought;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitAfterthought(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final AfterthoughtContext afterthought() throws RecognitionException {
        AfterthoughtContext _localctx = new AfterthoughtContext(_ctx, getState());
        enterRule(_localctx, 16, RULE_afterthought);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(186);
                expression(0);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class DeclarationContext extends ParserRuleContext {

        public DecltypeContext decltype() {
            return getRuleContext(DecltypeContext.class, 0);
        }

        public List<DeclvarContext> declvar() {
            return getRuleContexts(DeclvarContext.class);
        }

        public DeclvarContext declvar(int i) {
            return getRuleContext(DeclvarContext.class, i);
        }

        public List<TerminalNode> COMMA() {
            return getTokens(PainlessParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(PainlessParser.COMMA, i);
        }

        public DeclarationContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_declaration;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitDeclaration(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final DeclarationContext declaration() throws RecognitionException {
        DeclarationContext _localctx = new DeclarationContext(_ctx, getState());
        enterRule(_localctx, 18, RULE_declaration);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(188);
                decltype();
                setState(189);
                declvar();
                setState(194);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(190);
                            match(COMMA);
                            setState(191);
                            declvar();
                        }
                    }
                    setState(196);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class DecltypeContext extends ParserRuleContext {

        public TerminalNode TYPE() {
            return getToken(PainlessParser.TYPE, 0);
        }

        public List<TerminalNode> LBRACE() {
            return getTokens(PainlessParser.LBRACE);
        }

        public TerminalNode LBRACE(int i) {
            return getToken(PainlessParser.LBRACE, i);
        }

        public List<TerminalNode> RBRACE() {
            return getTokens(PainlessParser.RBRACE);
        }

        public TerminalNode RBRACE(int i) {
            return getToken(PainlessParser.RBRACE, i);
        }

        public DecltypeContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_decltype;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitDecltype(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final DecltypeContext decltype() throws RecognitionException {
        DecltypeContext _localctx = new DecltypeContext(_ctx, getState());
        enterRule(_localctx, 20, RULE_decltype);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(197);
                match(TYPE);
                setState(202);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == LBRACE) {
                    {
                        {
                            setState(198);
                            match(LBRACE);
                            setState(199);
                            match(RBRACE);
                        }
                    }
                    setState(204);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class DeclvarContext extends ParserRuleContext {

        public TerminalNode ID() {
            return getToken(PainlessParser.ID, 0);
        }

        public TerminalNode ASSIGN() {
            return getToken(PainlessParser.ASSIGN, 0);
        }

        public ExpressionContext expression() {
            return getRuleContext(ExpressionContext.class, 0);
        }

        public DeclvarContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_declvar;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitDeclvar(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final DeclvarContext declvar() throws RecognitionException {
        DeclvarContext _localctx = new DeclvarContext(_ctx, getState());
        enterRule(_localctx, 22, RULE_declvar);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(205);
                match(ID);
                setState(208);
                _la = _input.LA(1);
                if (_la == ASSIGN) {
                    {
                        setState(206);
                        match(ASSIGN);
                        setState(207);
                        expression(0);
                    }
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class TrapContext extends ParserRuleContext {

        public TerminalNode CATCH() {
            return getToken(PainlessParser.CATCH, 0);
        }

        public TerminalNode LP() {
            return getToken(PainlessParser.LP, 0);
        }

        public TerminalNode TYPE() {
            return getToken(PainlessParser.TYPE, 0);
        }

        public TerminalNode ID() {
            return getToken(PainlessParser.ID, 0);
        }

        public TerminalNode RP() {
            return getToken(PainlessParser.RP, 0);
        }

        public BlockContext block() {
            return getRuleContext(BlockContext.class, 0);
        }

        public TrapContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_trap;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitTrap(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final TrapContext trap() throws RecognitionException {
        TrapContext _localctx = new TrapContext(_ctx, getState());
        enterRule(_localctx, 24, RULE_trap);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(210);
                match(CATCH);
                setState(211);
                match(LP);
                setState(212);
                match(TYPE);
                setState(213);
                match(ID);
                setState(214);
                match(RP);
                setState(215);
                block();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class DelimiterContext extends ParserRuleContext {

        public TerminalNode SEMICOLON() {
            return getToken(PainlessParser.SEMICOLON, 0);
        }

        public TerminalNode EOF() {
            return getToken(PainlessParser.EOF, 0);
        }

        public DelimiterContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_delimiter;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitDelimiter(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final DelimiterContext delimiter() throws RecognitionException {
        DelimiterContext _localctx = new DelimiterContext(_ctx, getState());
        enterRule(_localctx, 26, RULE_delimiter);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(217);
                _la = _input.LA(1);
                if (!(_la == EOF || _la == SEMICOLON)) {
                    _errHandler.recoverInline(this);
                } else {
                    consume();
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class ExpressionContext extends ParserRuleContext {

        public boolean s = true;

        public ExpressionContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_expression;
        }

        public ExpressionContext() {
        }

        public void copyFrom(ExpressionContext ctx) {
            super.copyFrom(ctx);
            this.s = ctx.s;
        }
    }

    public static class SingleContext extends ExpressionContext {

        public UnaryContext u;

        public UnaryContext unary() {
            return getRuleContext(UnaryContext.class, 0);
        }

        public SingleContext(ExpressionContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitSingle(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class CompContext extends ExpressionContext {

        public List<ExpressionContext> expression() {
            return getRuleContexts(ExpressionContext.class);
        }

        public ExpressionContext expression(int i) {
            return getRuleContext(ExpressionContext.class, i);
        }

        public TerminalNode LT() {
            return getToken(PainlessParser.LT, 0);
        }

        public TerminalNode LTE() {
            return getToken(PainlessParser.LTE, 0);
        }

        public TerminalNode GT() {
            return getToken(PainlessParser.GT, 0);
        }

        public TerminalNode GTE() {
            return getToken(PainlessParser.GTE, 0);
        }

        public TerminalNode EQ() {
            return getToken(PainlessParser.EQ, 0);
        }

        public TerminalNode EQR() {
            return getToken(PainlessParser.EQR, 0);
        }

        public TerminalNode NE() {
            return getToken(PainlessParser.NE, 0);
        }

        public TerminalNode NER() {
            return getToken(PainlessParser.NER, 0);
        }

        public CompContext(ExpressionContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitComp(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class BoolContext extends ExpressionContext {

        public List<ExpressionContext> expression() {
            return getRuleContexts(ExpressionContext.class);
        }

        public ExpressionContext expression(int i) {
            return getRuleContext(ExpressionContext.class, i);
        }

        public TerminalNode BOOLAND() {
            return getToken(PainlessParser.BOOLAND, 0);
        }

        public TerminalNode BOOLOR() {
            return getToken(PainlessParser.BOOLOR, 0);
        }

        public BoolContext(ExpressionContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitBool(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class ConditionalContext extends ExpressionContext {

        public ExpressionContext e0;

        public ExpressionContext e1;

        public List<ExpressionContext> expression() {
            return getRuleContexts(ExpressionContext.class);
        }

        public ExpressionContext expression(int i) {
            return getRuleContext(ExpressionContext.class, i);
        }

        public TerminalNode COND() {
            return getToken(PainlessParser.COND, 0);
        }

        public TerminalNode COLON() {
            return getToken(PainlessParser.COLON, 0);
        }

        public ConditionalContext(ExpressionContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitConditional(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class AssignmentContext extends ExpressionContext {

        public ChainContext chain() {
            return getRuleContext(ChainContext.class, 0);
        }

        public ExpressionContext expression() {
            return getRuleContext(ExpressionContext.class, 0);
        }

        public TerminalNode ASSIGN() {
            return getToken(PainlessParser.ASSIGN, 0);
        }

        public TerminalNode AADD() {
            return getToken(PainlessParser.AADD, 0);
        }

        public TerminalNode ASUB() {
            return getToken(PainlessParser.ASUB, 0);
        }

        public TerminalNode AMUL() {
            return getToken(PainlessParser.AMUL, 0);
        }

        public TerminalNode ADIV() {
            return getToken(PainlessParser.ADIV, 0);
        }

        public TerminalNode AREM() {
            return getToken(PainlessParser.AREM, 0);
        }

        public TerminalNode AAND() {
            return getToken(PainlessParser.AAND, 0);
        }

        public TerminalNode AXOR() {
            return getToken(PainlessParser.AXOR, 0);
        }

        public TerminalNode AOR() {
            return getToken(PainlessParser.AOR, 0);
        }

        public TerminalNode ALSH() {
            return getToken(PainlessParser.ALSH, 0);
        }

        public TerminalNode ARSH() {
            return getToken(PainlessParser.ARSH, 0);
        }

        public TerminalNode AUSH() {
            return getToken(PainlessParser.AUSH, 0);
        }

        public AssignmentContext(ExpressionContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitAssignment(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class BinaryContext extends ExpressionContext {

        public List<ExpressionContext> expression() {
            return getRuleContexts(ExpressionContext.class);
        }

        public ExpressionContext expression(int i) {
            return getRuleContext(ExpressionContext.class, i);
        }

        public TerminalNode MUL() {
            return getToken(PainlessParser.MUL, 0);
        }

        public TerminalNode DIV() {
            return getToken(PainlessParser.DIV, 0);
        }

        public TerminalNode REM() {
            return getToken(PainlessParser.REM, 0);
        }

        public TerminalNode ADD() {
            return getToken(PainlessParser.ADD, 0);
        }

        public TerminalNode SUB() {
            return getToken(PainlessParser.SUB, 0);
        }

        public TerminalNode LSH() {
            return getToken(PainlessParser.LSH, 0);
        }

        public TerminalNode RSH() {
            return getToken(PainlessParser.RSH, 0);
        }

        public TerminalNode USH() {
            return getToken(PainlessParser.USH, 0);
        }

        public TerminalNode BWAND() {
            return getToken(PainlessParser.BWAND, 0);
        }

        public TerminalNode XOR() {
            return getToken(PainlessParser.XOR, 0);
        }

        public TerminalNode BWOR() {
            return getToken(PainlessParser.BWOR, 0);
        }

        public BinaryContext(ExpressionContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitBinary(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final ExpressionContext expression() throws RecognitionException {
        return expression(0);
    }

    private ExpressionContext expression(int _p) throws RecognitionException {
        ParserRuleContext _parentctx = _ctx;
        int _parentState = getState();
        ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
        ExpressionContext _prevctx = _localctx;
        int _startState = 28;
        enterRecursionRule(_localctx, 28, RULE_expression, _p);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(228);
                switch(getInterpreter().adaptivePredict(_input, 18, _ctx)) {
                    case 1:
                        {
                            _localctx = new AssignmentContext(_localctx);
                            _ctx = _localctx;
                            _prevctx = _localctx;
                            setState(220);
                            chain(true);
                            setState(221);
                            _la = _input.LA(1);
                            if (!(((((_la - 53)) & ~0x3f) == 0 && ((1L << (_la - 53)) & ((1L << (ASSIGN - 53)) | (1L << (AADD - 53)) | (1L << (ASUB - 53)) | (1L << (AMUL - 53)) | (1L << (ADIV - 53)) | (1L << (AREM - 53)) | (1L << (AAND - 53)) | (1L << (AXOR - 53)) | (1L << (AOR - 53)) | (1L << (ALSH - 53)) | (1L << (ARSH - 53)) | (1L << (AUSH - 53)))) != 0))) {
                                _errHandler.recoverInline(this);
                            } else {
                                consume();
                            }
                            setState(222);
                            expression(1);
                            ((AssignmentContext) _localctx).s = false;
                        }
                        break;
                    case 2:
                        {
                            _localctx = new SingleContext(_localctx);
                            _ctx = _localctx;
                            _prevctx = _localctx;
                            setState(225);
                            ((SingleContext) _localctx).u = unary(false);
                            ((SingleContext) _localctx).s = ((SingleContext) _localctx).u.s;
                        }
                        break;
                }
                _ctx.stop = _input.LT(-1);
                setState(289);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 20, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        if (_parseListeners != null)
                            triggerExitRuleEvent();
                        _prevctx = _localctx;
                        {
                            setState(287);
                            switch(getInterpreter().adaptivePredict(_input, 19, _ctx)) {
                                case 1:
                                    {
                                        _localctx = new BinaryContext(new ExpressionContext(_parentctx, _parentState));
                                        pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                        setState(230);
                                        if (!(precpred(_ctx, 12)))
                                            throw new FailedPredicateException(this, "precpred(_ctx, 12)");
                                        setState(231);
                                        _la = _input.LA(1);
                                        if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << MUL) | (1L << DIV) | (1L << REM))) != 0))) {
                                            _errHandler.recoverInline(this);
                                        } else {
                                            consume();
                                        }
                                        setState(232);
                                        expression(13);
                                        ((BinaryContext) _localctx).s = false;
                                    }
                                    break;
                                case 2:
                                    {
                                        _localctx = new BinaryContext(new ExpressionContext(_parentctx, _parentState));
                                        pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                        setState(235);
                                        if (!(precpred(_ctx, 11)))
                                            throw new FailedPredicateException(this, "precpred(_ctx, 11)");
                                        setState(236);
                                        _la = _input.LA(1);
                                        if (!(_la == ADD || _la == SUB)) {
                                            _errHandler.recoverInline(this);
                                        } else {
                                            consume();
                                        }
                                        setState(237);
                                        expression(12);
                                        ((BinaryContext) _localctx).s = false;
                                    }
                                    break;
                                case 3:
                                    {
                                        _localctx = new BinaryContext(new ExpressionContext(_parentctx, _parentState));
                                        pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                        setState(240);
                                        if (!(precpred(_ctx, 10)))
                                            throw new FailedPredicateException(this, "precpred(_ctx, 10)");
                                        setState(241);
                                        _la = _input.LA(1);
                                        if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LSH) | (1L << RSH) | (1L << USH))) != 0))) {
                                            _errHandler.recoverInline(this);
                                        } else {
                                            consume();
                                        }
                                        setState(242);
                                        expression(11);
                                        ((BinaryContext) _localctx).s = false;
                                    }
                                    break;
                                case 4:
                                    {
                                        _localctx = new CompContext(new ExpressionContext(_parentctx, _parentState));
                                        pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                        setState(245);
                                        if (!(precpred(_ctx, 9)))
                                            throw new FailedPredicateException(this, "precpred(_ctx, 9)");
                                        setState(246);
                                        _la = _input.LA(1);
                                        if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LT) | (1L << LTE) | (1L << GT) | (1L << GTE))) != 0))) {
                                            _errHandler.recoverInline(this);
                                        } else {
                                            consume();
                                        }
                                        setState(247);
                                        expression(10);
                                        ((CompContext) _localctx).s = false;
                                    }
                                    break;
                                case 5:
                                    {
                                        _localctx = new CompContext(new ExpressionContext(_parentctx, _parentState));
                                        pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                        setState(250);
                                        if (!(precpred(_ctx, 8)))
                                            throw new FailedPredicateException(this, "precpred(_ctx, 8)");
                                        setState(251);
                                        _la = _input.LA(1);
                                        if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << EQ) | (1L << EQR) | (1L << NE) | (1L << NER))) != 0))) {
                                            _errHandler.recoverInline(this);
                                        } else {
                                            consume();
                                        }
                                        setState(252);
                                        expression(9);
                                        ((CompContext) _localctx).s = false;
                                    }
                                    break;
                                case 6:
                                    {
                                        _localctx = new BinaryContext(new ExpressionContext(_parentctx, _parentState));
                                        pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                        setState(255);
                                        if (!(precpred(_ctx, 7)))
                                            throw new FailedPredicateException(this, "precpred(_ctx, 7)");
                                        setState(256);
                                        match(BWAND);
                                        setState(257);
                                        expression(8);
                                        ((BinaryContext) _localctx).s = false;
                                    }
                                    break;
                                case 7:
                                    {
                                        _localctx = new BinaryContext(new ExpressionContext(_parentctx, _parentState));
                                        pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                        setState(260);
                                        if (!(precpred(_ctx, 6)))
                                            throw new FailedPredicateException(this, "precpred(_ctx, 6)");
                                        setState(261);
                                        match(XOR);
                                        setState(262);
                                        expression(7);
                                        ((BinaryContext) _localctx).s = false;
                                    }
                                    break;
                                case 8:
                                    {
                                        _localctx = new BinaryContext(new ExpressionContext(_parentctx, _parentState));
                                        pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                        setState(265);
                                        if (!(precpred(_ctx, 5)))
                                            throw new FailedPredicateException(this, "precpred(_ctx, 5)");
                                        setState(266);
                                        match(BWOR);
                                        setState(267);
                                        expression(6);
                                        ((BinaryContext) _localctx).s = false;
                                    }
                                    break;
                                case 9:
                                    {
                                        _localctx = new BoolContext(new ExpressionContext(_parentctx, _parentState));
                                        pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                        setState(270);
                                        if (!(precpred(_ctx, 4)))
                                            throw new FailedPredicateException(this, "precpred(_ctx, 4)");
                                        setState(271);
                                        match(BOOLAND);
                                        setState(272);
                                        expression(5);
                                        ((BoolContext) _localctx).s = false;
                                    }
                                    break;
                                case 10:
                                    {
                                        _localctx = new BoolContext(new ExpressionContext(_parentctx, _parentState));
                                        pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                        setState(275);
                                        if (!(precpred(_ctx, 3)))
                                            throw new FailedPredicateException(this, "precpred(_ctx, 3)");
                                        setState(276);
                                        match(BOOLOR);
                                        setState(277);
                                        expression(4);
                                        ((BoolContext) _localctx).s = false;
                                    }
                                    break;
                                case 11:
                                    {
                                        _localctx = new ConditionalContext(new ExpressionContext(_parentctx, _parentState));
                                        pushNewRecursionContext(_localctx, _startState, RULE_expression);
                                        setState(280);
                                        if (!(precpred(_ctx, 2)))
                                            throw new FailedPredicateException(this, "precpred(_ctx, 2)");
                                        setState(281);
                                        match(COND);
                                        setState(282);
                                        ((ConditionalContext) _localctx).e0 = expression(0);
                                        setState(283);
                                        match(COLON);
                                        setState(284);
                                        ((ConditionalContext) _localctx).e1 = expression(2);
                                        ((ConditionalContext) _localctx).s = ((ConditionalContext) _localctx).e0.s && ((ConditionalContext) _localctx).e1.s;
                                    }
                                    break;
                            }
                        }
                    }
                    setState(291);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 20, _ctx);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            unrollRecursionContexts(_parentctx);
        }
        return _localctx;
    }

    public static class UnaryContext extends ParserRuleContext {

        public boolean c;

        public boolean s = true;

        public UnaryContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public UnaryContext(ParserRuleContext parent, int invokingState, boolean c) {
            super(parent, invokingState);
            this.c = c;
        }

        @Override
        public int getRuleIndex() {
            return RULE_unary;
        }

        public UnaryContext() {
        }

        public void copyFrom(UnaryContext ctx) {
            super.copyFrom(ctx);
            this.c = ctx.c;
            this.s = ctx.s;
        }
    }

    public static class CastContext extends UnaryContext {

        public TerminalNode LP() {
            return getToken(PainlessParser.LP, 0);
        }

        public DecltypeContext decltype() {
            return getRuleContext(DecltypeContext.class, 0);
        }

        public TerminalNode RP() {
            return getToken(PainlessParser.RP, 0);
        }

        public UnaryContext unary() {
            return getRuleContext(UnaryContext.class, 0);
        }

        public CastContext(UnaryContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitCast(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class PreContext extends UnaryContext {

        public ChainContext chain() {
            return getRuleContext(ChainContext.class, 0);
        }

        public TerminalNode INCR() {
            return getToken(PainlessParser.INCR, 0);
        }

        public TerminalNode DECR() {
            return getToken(PainlessParser.DECR, 0);
        }

        public PreContext(UnaryContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitPre(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class ReadContext extends UnaryContext {

        public ChainContext chain() {
            return getRuleContext(ChainContext.class, 0);
        }

        public ReadContext(UnaryContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitRead(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class PostContext extends UnaryContext {

        public ChainContext chain() {
            return getRuleContext(ChainContext.class, 0);
        }

        public TerminalNode INCR() {
            return getToken(PainlessParser.INCR, 0);
        }

        public TerminalNode DECR() {
            return getToken(PainlessParser.DECR, 0);
        }

        public PostContext(UnaryContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitPost(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class NullContext extends UnaryContext {

        public TerminalNode NULL() {
            return getToken(PainlessParser.NULL, 0);
        }

        public NullContext(UnaryContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitNull(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class TrueContext extends UnaryContext {

        public TerminalNode TRUE() {
            return getToken(PainlessParser.TRUE, 0);
        }

        public TrueContext(UnaryContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitTrue(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class FalseContext extends UnaryContext {

        public TerminalNode FALSE() {
            return getToken(PainlessParser.FALSE, 0);
        }

        public FalseContext(UnaryContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitFalse(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class NumericContext extends UnaryContext {

        public TerminalNode OCTAL() {
            return getToken(PainlessParser.OCTAL, 0);
        }

        public TerminalNode HEX() {
            return getToken(PainlessParser.HEX, 0);
        }

        public TerminalNode INTEGER() {
            return getToken(PainlessParser.INTEGER, 0);
        }

        public TerminalNode DECIMAL() {
            return getToken(PainlessParser.DECIMAL, 0);
        }

        public NumericContext(UnaryContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitNumeric(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class OperatorContext extends UnaryContext {

        public UnaryContext unary() {
            return getRuleContext(UnaryContext.class, 0);
        }

        public TerminalNode BOOLNOT() {
            return getToken(PainlessParser.BOOLNOT, 0);
        }

        public TerminalNode BWNOT() {
            return getToken(PainlessParser.BWNOT, 0);
        }

        public TerminalNode ADD() {
            return getToken(PainlessParser.ADD, 0);
        }

        public TerminalNode SUB() {
            return getToken(PainlessParser.SUB, 0);
        }

        public OperatorContext(UnaryContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitOperator(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final UnaryContext unary(boolean c) throws RecognitionException {
        UnaryContext _localctx = new UnaryContext(_ctx, getState(), c);
        enterRule(_localctx, 30, RULE_unary);
        int _la;
        try {
            setState(321);
            switch(getInterpreter().adaptivePredict(_input, 21, _ctx)) {
                case 1:
                    _localctx = new PreContext(_localctx);
                    enterOuterAlt(_localctx, 1);
                    {
                        setState(292);
                        if (!(!_localctx.c))
                            throw new FailedPredicateException(this, " !$c ");
                        setState(293);
                        _la = _input.LA(1);
                        if (!(_la == INCR || _la == DECR)) {
                            _errHandler.recoverInline(this);
                        } else {
                            consume();
                        }
                        setState(294);
                        chain(true);
                    }
                    break;
                case 2:
                    _localctx = new PostContext(_localctx);
                    enterOuterAlt(_localctx, 2);
                    {
                        setState(295);
                        if (!(!_localctx.c))
                            throw new FailedPredicateException(this, " !$c ");
                        setState(296);
                        chain(true);
                        setState(297);
                        _la = _input.LA(1);
                        if (!(_la == INCR || _la == DECR)) {
                            _errHandler.recoverInline(this);
                        } else {
                            consume();
                        }
                    }
                    break;
                case 3:
                    _localctx = new ReadContext(_localctx);
                    enterOuterAlt(_localctx, 3);
                    {
                        setState(299);
                        if (!(!_localctx.c))
                            throw new FailedPredicateException(this, " !$c ");
                        setState(300);
                        chain(false);
                    }
                    break;
                case 4:
                    _localctx = new NumericContext(_localctx);
                    enterOuterAlt(_localctx, 4);
                    {
                        setState(301);
                        if (!(!_localctx.c))
                            throw new FailedPredicateException(this, " !$c ");
                        setState(302);
                        _la = _input.LA(1);
                        if (!(((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & ((1L << (OCTAL - 65)) | (1L << (HEX - 65)) | (1L << (INTEGER - 65)) | (1L << (DECIMAL - 65)))) != 0))) {
                            _errHandler.recoverInline(this);
                        } else {
                            consume();
                        }
                        ((NumericContext) _localctx).s = false;
                    }
                    break;
                case 5:
                    _localctx = new TrueContext(_localctx);
                    enterOuterAlt(_localctx, 5);
                    {
                        setState(304);
                        if (!(!_localctx.c))
                            throw new FailedPredicateException(this, " !$c ");
                        setState(305);
                        match(TRUE);
                        ((TrueContext) _localctx).s = false;
                    }
                    break;
                case 6:
                    _localctx = new FalseContext(_localctx);
                    enterOuterAlt(_localctx, 6);
                    {
                        setState(307);
                        if (!(!_localctx.c))
                            throw new FailedPredicateException(this, " !$c ");
                        setState(308);
                        match(FALSE);
                        ((FalseContext) _localctx).s = false;
                    }
                    break;
                case 7:
                    _localctx = new NullContext(_localctx);
                    enterOuterAlt(_localctx, 7);
                    {
                        setState(310);
                        if (!(!_localctx.c))
                            throw new FailedPredicateException(this, " !$c ");
                        setState(311);
                        match(NULL);
                        ((NullContext) _localctx).s = false;
                    }
                    break;
                case 8:
                    _localctx = new OperatorContext(_localctx);
                    enterOuterAlt(_localctx, 8);
                    {
                        setState(313);
                        if (!(!_localctx.c))
                            throw new FailedPredicateException(this, " !$c ");
                        setState(314);
                        _la = _input.LA(1);
                        if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BOOLNOT) | (1L << BWNOT) | (1L << ADD) | (1L << SUB))) != 0))) {
                            _errHandler.recoverInline(this);
                        } else {
                            consume();
                        }
                        setState(315);
                        unary(false);
                    }
                    break;
                case 9:
                    _localctx = new CastContext(_localctx);
                    enterOuterAlt(_localctx, 9);
                    {
                        setState(316);
                        match(LP);
                        setState(317);
                        decltype();
                        setState(318);
                        match(RP);
                        setState(319);
                        unary(_localctx.c);
                    }
                    break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class ChainContext extends ParserRuleContext {

        public boolean c;

        public ChainContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public ChainContext(ParserRuleContext parent, int invokingState, boolean c) {
            super(parent, invokingState);
            this.c = c;
        }

        @Override
        public int getRuleIndex() {
            return RULE_chain;
        }

        public ChainContext() {
        }

        public void copyFrom(ChainContext ctx) {
            super.copyFrom(ctx);
            this.c = ctx.c;
        }
    }

    public static class StaticContext extends ChainContext {

        public DecltypeContext decltype() {
            return getRuleContext(DecltypeContext.class, 0);
        }

        public DotContext dot() {
            return getRuleContext(DotContext.class, 0);
        }

        public List<SecondaryContext> secondary() {
            return getRuleContexts(SecondaryContext.class);
        }

        public SecondaryContext secondary(int i) {
            return getRuleContext(SecondaryContext.class, i);
        }

        public StaticContext(ChainContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitStatic(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class DynamicContext extends ChainContext {

        public PrimaryContext p;

        public PrimaryContext primary() {
            return getRuleContext(PrimaryContext.class, 0);
        }

        public List<SecondaryContext> secondary() {
            return getRuleContexts(SecondaryContext.class);
        }

        public SecondaryContext secondary(int i) {
            return getRuleContext(SecondaryContext.class, i);
        }

        public DynamicContext(ChainContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitDynamic(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class NewarrayContext extends ChainContext {

        public TerminalNode NEW() {
            return getToken(PainlessParser.NEW, 0);
        }

        public TerminalNode TYPE() {
            return getToken(PainlessParser.TYPE, 0);
        }

        public List<TerminalNode> LBRACE() {
            return getTokens(PainlessParser.LBRACE);
        }

        public TerminalNode LBRACE(int i) {
            return getToken(PainlessParser.LBRACE, i);
        }

        public List<ExpressionContext> expression() {
            return getRuleContexts(ExpressionContext.class);
        }

        public ExpressionContext expression(int i) {
            return getRuleContext(ExpressionContext.class, i);
        }

        public List<TerminalNode> RBRACE() {
            return getTokens(PainlessParser.RBRACE);
        }

        public TerminalNode RBRACE(int i) {
            return getToken(PainlessParser.RBRACE, i);
        }

        public DotContext dot() {
            return getRuleContext(DotContext.class, 0);
        }

        public List<SecondaryContext> secondary() {
            return getRuleContexts(SecondaryContext.class);
        }

        public SecondaryContext secondary(int i) {
            return getRuleContext(SecondaryContext.class, i);
        }

        public NewarrayContext(ChainContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitNewarray(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final ChainContext chain(boolean c) throws RecognitionException {
        ChainContext _localctx = new ChainContext(_ctx, getState(), c);
        enterRule(_localctx, 32, RULE_chain);
        try {
            int _alt;
            setState(357);
            switch(getInterpreter().adaptivePredict(_input, 27, _ctx)) {
                case 1:
                    _localctx = new DynamicContext(_localctx);
                    enterOuterAlt(_localctx, 1);
                    {
                        setState(323);
                        ((DynamicContext) _localctx).p = primary(_localctx.c);
                        setState(327);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 22, _ctx);
                        while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                            if (_alt == 1) {
                                {
                                    {
                                        setState(324);
                                        secondary(((DynamicContext) _localctx).p.s);
                                    }
                                }
                            }
                            setState(329);
                            _errHandler.sync(this);
                            _alt = getInterpreter().adaptivePredict(_input, 22, _ctx);
                        }
                    }
                    break;
                case 2:
                    _localctx = new StaticContext(_localctx);
                    enterOuterAlt(_localctx, 2);
                    {
                        setState(330);
                        decltype();
                        setState(331);
                        dot();
                        setState(335);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 23, _ctx);
                        while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                            if (_alt == 1) {
                                {
                                    {
                                        setState(332);
                                        secondary(true);
                                    }
                                }
                            }
                            setState(337);
                            _errHandler.sync(this);
                            _alt = getInterpreter().adaptivePredict(_input, 23, _ctx);
                        }
                    }
                    break;
                case 3:
                    _localctx = new NewarrayContext(_localctx);
                    enterOuterAlt(_localctx, 3);
                    {
                        setState(338);
                        match(NEW);
                        setState(339);
                        match(TYPE);
                        setState(344);
                        _errHandler.sync(this);
                        _alt = 1;
                        do {
                            switch(_alt) {
                                case 1:
                                    {
                                        {
                                            setState(340);
                                            match(LBRACE);
                                            setState(341);
                                            expression(0);
                                            setState(342);
                                            match(RBRACE);
                                        }
                                    }
                                    break;
                                default:
                                    throw new NoViableAltException(this);
                            }
                            setState(346);
                            _errHandler.sync(this);
                            _alt = getInterpreter().adaptivePredict(_input, 24, _ctx);
                        } while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER);
                        setState(355);
                        switch(getInterpreter().adaptivePredict(_input, 26, _ctx)) {
                            case 1:
                                {
                                    setState(348);
                                    dot();
                                    setState(352);
                                    _errHandler.sync(this);
                                    _alt = getInterpreter().adaptivePredict(_input, 25, _ctx);
                                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                                        if (_alt == 1) {
                                            {
                                                {
                                                    setState(349);
                                                    secondary(true);
                                                }
                                            }
                                        }
                                        setState(354);
                                        _errHandler.sync(this);
                                        _alt = getInterpreter().adaptivePredict(_input, 25, _ctx);
                                    }
                                }
                                break;
                        }
                    }
                    break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class PrimaryContext extends ParserRuleContext {

        public boolean c;

        public boolean s = true;

        public PrimaryContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public PrimaryContext(ParserRuleContext parent, int invokingState, boolean c) {
            super(parent, invokingState);
            this.c = c;
        }

        @Override
        public int getRuleIndex() {
            return RULE_primary;
        }

        public PrimaryContext() {
        }

        public void copyFrom(PrimaryContext ctx) {
            super.copyFrom(ctx);
            this.c = ctx.c;
            this.s = ctx.s;
        }
    }

    public static class StringContext extends PrimaryContext {

        public TerminalNode STRING() {
            return getToken(PainlessParser.STRING, 0);
        }

        public StringContext(PrimaryContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitString(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class CalllocalContext extends PrimaryContext {

        public TerminalNode ID() {
            return getToken(PainlessParser.ID, 0);
        }

        public ArgumentsContext arguments() {
            return getRuleContext(ArgumentsContext.class, 0);
        }

        public CalllocalContext(PrimaryContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitCalllocal(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class VariableContext extends PrimaryContext {

        public TerminalNode ID() {
            return getToken(PainlessParser.ID, 0);
        }

        public VariableContext(PrimaryContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitVariable(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class ExprprecContext extends PrimaryContext {

        public ExpressionContext e;

        public TerminalNode LP() {
            return getToken(PainlessParser.LP, 0);
        }

        public TerminalNode RP() {
            return getToken(PainlessParser.RP, 0);
        }

        public ExpressionContext expression() {
            return getRuleContext(ExpressionContext.class, 0);
        }

        public ExprprecContext(PrimaryContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitExprprec(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class NewobjectContext extends PrimaryContext {

        public TerminalNode NEW() {
            return getToken(PainlessParser.NEW, 0);
        }

        public TerminalNode TYPE() {
            return getToken(PainlessParser.TYPE, 0);
        }

        public ArgumentsContext arguments() {
            return getRuleContext(ArgumentsContext.class, 0);
        }

        public NewobjectContext(PrimaryContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitNewobject(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class ChainprecContext extends PrimaryContext {

        public TerminalNode LP() {
            return getToken(PainlessParser.LP, 0);
        }

        public UnaryContext unary() {
            return getRuleContext(UnaryContext.class, 0);
        }

        public TerminalNode RP() {
            return getToken(PainlessParser.RP, 0);
        }

        public ChainprecContext(PrimaryContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitChainprec(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final PrimaryContext primary(boolean c) throws RecognitionException {
        PrimaryContext _localctx = new PrimaryContext(_ctx, getState(), c);
        enterRule(_localctx, 34, RULE_primary);
        try {
            setState(377);
            switch(getInterpreter().adaptivePredict(_input, 28, _ctx)) {
                case 1:
                    _localctx = new ExprprecContext(_localctx);
                    enterOuterAlt(_localctx, 1);
                    {
                        setState(359);
                        if (!(!_localctx.c))
                            throw new FailedPredicateException(this, " !$c ");
                        setState(360);
                        match(LP);
                        setState(361);
                        ((ExprprecContext) _localctx).e = expression(0);
                        setState(362);
                        match(RP);
                        ((ExprprecContext) _localctx).s = ((ExprprecContext) _localctx).e.s;
                    }
                    break;
                case 2:
                    _localctx = new ChainprecContext(_localctx);
                    enterOuterAlt(_localctx, 2);
                    {
                        setState(365);
                        if (!(_localctx.c))
                            throw new FailedPredicateException(this, " $c ");
                        setState(366);
                        match(LP);
                        setState(367);
                        unary(true);
                        setState(368);
                        match(RP);
                    }
                    break;
                case 3:
                    _localctx = new StringContext(_localctx);
                    enterOuterAlt(_localctx, 3);
                    {
                        setState(370);
                        match(STRING);
                    }
                    break;
                case 4:
                    _localctx = new VariableContext(_localctx);
                    enterOuterAlt(_localctx, 4);
                    {
                        setState(371);
                        match(ID);
                    }
                    break;
                case 5:
                    _localctx = new CalllocalContext(_localctx);
                    enterOuterAlt(_localctx, 5);
                    {
                        setState(372);
                        match(ID);
                        setState(373);
                        arguments();
                    }
                    break;
                case 6:
                    _localctx = new NewobjectContext(_localctx);
                    enterOuterAlt(_localctx, 6);
                    {
                        setState(374);
                        match(NEW);
                        setState(375);
                        match(TYPE);
                        setState(376);
                        arguments();
                    }
                    break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class SecondaryContext extends ParserRuleContext {

        public boolean s;

        public DotContext dot() {
            return getRuleContext(DotContext.class, 0);
        }

        public BraceContext brace() {
            return getRuleContext(BraceContext.class, 0);
        }

        public SecondaryContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public SecondaryContext(ParserRuleContext parent, int invokingState, boolean s) {
            super(parent, invokingState);
            this.s = s;
        }

        @Override
        public int getRuleIndex() {
            return RULE_secondary;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitSecondary(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final SecondaryContext secondary(boolean s) throws RecognitionException {
        SecondaryContext _localctx = new SecondaryContext(_ctx, getState(), s);
        enterRule(_localctx, 36, RULE_secondary);
        try {
            setState(383);
            switch(getInterpreter().adaptivePredict(_input, 29, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                    {
                        setState(379);
                        if (!(_localctx.s))
                            throw new FailedPredicateException(this, " $s ");
                        setState(380);
                        dot();
                    }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                    {
                        setState(381);
                        if (!(_localctx.s))
                            throw new FailedPredicateException(this, " $s ");
                        setState(382);
                        brace();
                    }
                    break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class DotContext extends ParserRuleContext {

        public DotContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_dot;
        }

        public DotContext() {
        }

        public void copyFrom(DotContext ctx) {
            super.copyFrom(ctx);
        }
    }

    public static class CallinvokeContext extends DotContext {

        public TerminalNode DOT() {
            return getToken(PainlessParser.DOT, 0);
        }

        public TerminalNode DOTID() {
            return getToken(PainlessParser.DOTID, 0);
        }

        public ArgumentsContext arguments() {
            return getRuleContext(ArgumentsContext.class, 0);
        }

        public CallinvokeContext(DotContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitCallinvoke(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public static class FieldaccessContext extends DotContext {

        public TerminalNode DOT() {
            return getToken(PainlessParser.DOT, 0);
        }

        public TerminalNode DOTID() {
            return getToken(PainlessParser.DOTID, 0);
        }

        public TerminalNode DOTINTEGER() {
            return getToken(PainlessParser.DOTINTEGER, 0);
        }

        public FieldaccessContext(DotContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitFieldaccess(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final DotContext dot() throws RecognitionException {
        DotContext _localctx = new DotContext(_ctx, getState());
        enterRule(_localctx, 38, RULE_dot);
        int _la;
        try {
            setState(390);
            switch(getInterpreter().adaptivePredict(_input, 30, _ctx)) {
                case 1:
                    _localctx = new CallinvokeContext(_localctx);
                    enterOuterAlt(_localctx, 1);
                    {
                        setState(385);
                        match(DOT);
                        setState(386);
                        match(DOTID);
                        setState(387);
                        arguments();
                    }
                    break;
                case 2:
                    _localctx = new FieldaccessContext(_localctx);
                    enterOuterAlt(_localctx, 2);
                    {
                        setState(388);
                        match(DOT);
                        setState(389);
                        _la = _input.LA(1);
                        if (!(_la == DOTINTEGER || _la == DOTID)) {
                            _errHandler.recoverInline(this);
                        } else {
                            consume();
                        }
                    }
                    break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class BraceContext extends ParserRuleContext {

        public BraceContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_brace;
        }

        public BraceContext() {
        }

        public void copyFrom(BraceContext ctx) {
            super.copyFrom(ctx);
        }
    }

    public static class BraceaccessContext extends BraceContext {

        public TerminalNode LBRACE() {
            return getToken(PainlessParser.LBRACE, 0);
        }

        public ExpressionContext expression() {
            return getRuleContext(ExpressionContext.class, 0);
        }

        public TerminalNode RBRACE() {
            return getToken(PainlessParser.RBRACE, 0);
        }

        public BraceaccessContext(BraceContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitBraceaccess(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final BraceContext brace() throws RecognitionException {
        BraceContext _localctx = new BraceContext(_ctx, getState());
        enterRule(_localctx, 40, RULE_brace);
        try {
            _localctx = new BraceaccessContext(_localctx);
            enterOuterAlt(_localctx, 1);
            {
                setState(392);
                match(LBRACE);
                setState(393);
                expression(0);
                setState(394);
                match(RBRACE);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class ArgumentsContext extends ParserRuleContext {

        public TerminalNode LP() {
            return getToken(PainlessParser.LP, 0);
        }

        public TerminalNode RP() {
            return getToken(PainlessParser.RP, 0);
        }

        public List<ArgumentContext> argument() {
            return getRuleContexts(ArgumentContext.class);
        }

        public ArgumentContext argument(int i) {
            return getRuleContext(ArgumentContext.class, i);
        }

        public List<TerminalNode> COMMA() {
            return getTokens(PainlessParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(PainlessParser.COMMA, i);
        }

        public ArgumentsContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_arguments;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitArguments(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final ArgumentsContext arguments() throws RecognitionException {
        ArgumentsContext _localctx = new ArgumentsContext(_ctx, getState());
        enterRule(_localctx, 42, RULE_arguments);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                {
                    setState(396);
                    match(LP);
                    setState(405);
                    switch(getInterpreter().adaptivePredict(_input, 32, _ctx)) {
                        case 1:
                            {
                                setState(397);
                                argument();
                                setState(402);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                while (_la == COMMA) {
                                    {
                                        {
                                            setState(398);
                                            match(COMMA);
                                            setState(399);
                                            argument();
                                        }
                                    }
                                    setState(404);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                }
                            }
                            break;
                    }
                    setState(407);
                    match(RP);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class ArgumentContext extends ParserRuleContext {

        public ExpressionContext expression() {
            return getRuleContext(ExpressionContext.class, 0);
        }

        public LambdaContext lambda() {
            return getRuleContext(LambdaContext.class, 0);
        }

        public FuncrefContext funcref() {
            return getRuleContext(FuncrefContext.class, 0);
        }

        public ArgumentContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_argument;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitArgument(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final ArgumentContext argument() throws RecognitionException {
        ArgumentContext _localctx = new ArgumentContext(_ctx, getState());
        enterRule(_localctx, 44, RULE_argument);
        try {
            setState(412);
            switch(getInterpreter().adaptivePredict(_input, 33, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                    {
                        setState(409);
                        expression(0);
                    }
                    break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                    {
                        setState(410);
                        lambda();
                    }
                    break;
                case 3:
                    enterOuterAlt(_localctx, 3);
                    {
                        setState(411);
                        funcref();
                    }
                    break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class LambdaContext extends ParserRuleContext {

        public TerminalNode ARROW() {
            return getToken(PainlessParser.ARROW, 0);
        }

        public BlockContext block() {
            return getRuleContext(BlockContext.class, 0);
        }

        public List<LamtypeContext> lamtype() {
            return getRuleContexts(LamtypeContext.class);
        }

        public LamtypeContext lamtype(int i) {
            return getRuleContext(LamtypeContext.class, i);
        }

        public TerminalNode LP() {
            return getToken(PainlessParser.LP, 0);
        }

        public TerminalNode RP() {
            return getToken(PainlessParser.RP, 0);
        }

        public List<TerminalNode> COMMA() {
            return getTokens(PainlessParser.COMMA);
        }

        public TerminalNode COMMA(int i) {
            return getToken(PainlessParser.COMMA, i);
        }

        public LambdaContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_lambda;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitLambda(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final LambdaContext lambda() throws RecognitionException {
        LambdaContext _localctx = new LambdaContext(_ctx, getState());
        enterRule(_localctx, 46, RULE_lambda);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(427);
                switch(_input.LA(1)) {
                    case TYPE:
                    case ID:
                        {
                            setState(414);
                            lamtype();
                        }
                        break;
                    case LP:
                        {
                            setState(415);
                            match(LP);
                            setState(424);
                            _la = _input.LA(1);
                            if (_la == TYPE || _la == ID) {
                                {
                                    setState(416);
                                    lamtype();
                                    setState(421);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                    while (_la == COMMA) {
                                        {
                                            {
                                                setState(417);
                                                match(COMMA);
                                                setState(418);
                                                lamtype();
                                            }
                                        }
                                        setState(423);
                                        _errHandler.sync(this);
                                        _la = _input.LA(1);
                                    }
                                }
                            }
                            setState(426);
                            match(RP);
                        }
                        break;
                    default:
                        throw new NoViableAltException(this);
                }
                setState(429);
                match(ARROW);
                setState(430);
                block();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class LamtypeContext extends ParserRuleContext {

        public TerminalNode ID() {
            return getToken(PainlessParser.ID, 0);
        }

        public DecltypeContext decltype() {
            return getRuleContext(DecltypeContext.class, 0);
        }

        public LamtypeContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_lamtype;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitLamtype(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final LamtypeContext lamtype() throws RecognitionException {
        LamtypeContext _localctx = new LamtypeContext(_ctx, getState());
        enterRule(_localctx, 48, RULE_lamtype);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(433);
                _la = _input.LA(1);
                if (_la == TYPE) {
                    {
                        setState(432);
                        decltype();
                    }
                }
                setState(435);
                match(ID);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public static class FuncrefContext extends ParserRuleContext {

        public TerminalNode TYPE() {
            return getToken(PainlessParser.TYPE, 0);
        }

        public TerminalNode REF() {
            return getToken(PainlessParser.REF, 0);
        }

        public List<TerminalNode> ID() {
            return getTokens(PainlessParser.ID);
        }

        public TerminalNode ID(int i) {
            return getToken(PainlessParser.ID, i);
        }

        public TerminalNode NEW() {
            return getToken(PainlessParser.NEW, 0);
        }

        public FuncrefContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_funcref;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof PainlessParserVisitor)
                return ((PainlessParserVisitor<? extends T>) visitor).visitFuncref(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final FuncrefContext funcref() throws RecognitionException {
        FuncrefContext _localctx = new FuncrefContext(_ctx, getState());
        enterRule(_localctx, 50, RULE_funcref);
        int _la;
        try {
            setState(443);
            switch(_input.LA(1)) {
                case TYPE:
                    enterOuterAlt(_localctx, 1);
                    {
                        setState(437);
                        match(TYPE);
                        setState(438);
                        match(REF);
                        setState(439);
                        _la = _input.LA(1);
                        if (!(_la == NEW || _la == ID)) {
                            _errHandler.recoverInline(this);
                        } else {
                            consume();
                        }
                    }
                    break;
                case ID:
                    enterOuterAlt(_localctx, 2);
                    {
                        setState(440);
                        match(ID);
                        setState(441);
                        match(REF);
                        setState(442);
                        match(ID);
                    }
                    break;
                default:
                    throw new NoViableAltException(this);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
        switch(ruleIndex) {
            case 3:
                return statement_sempred((StatementContext) _localctx, predIndex);
            case 14:
                return expression_sempred((ExpressionContext) _localctx, predIndex);
            case 15:
                return unary_sempred((UnaryContext) _localctx, predIndex);
            case 17:
                return primary_sempred((PrimaryContext) _localctx, predIndex);
            case 18:
                return secondary_sempred((SecondaryContext) _localctx, predIndex);
        }
        return true;
    }

    private boolean statement_sempred(StatementContext _localctx, int predIndex) {
        switch(predIndex) {
            case 0:
                return _input.LA(1) != ELSE;
        }
        return true;
    }

    private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
        switch(predIndex) {
            case 1:
                return precpred(_ctx, 12);
            case 2:
                return precpred(_ctx, 11);
            case 3:
                return precpred(_ctx, 10);
            case 4:
                return precpred(_ctx, 9);
            case 5:
                return precpred(_ctx, 8);
            case 6:
                return precpred(_ctx, 7);
            case 7:
                return precpred(_ctx, 6);
            case 8:
                return precpred(_ctx, 5);
            case 9:
                return precpred(_ctx, 4);
            case 10:
                return precpred(_ctx, 3);
            case 11:
                return precpred(_ctx, 2);
        }
        return true;
    }

    private boolean unary_sempred(UnaryContext _localctx, int predIndex) {
        switch(predIndex) {
            case 12:
                return !_localctx.c;
            case 13:
                return !_localctx.c;
            case 14:
                return !_localctx.c;
            case 15:
                return !_localctx.c;
            case 16:
                return !_localctx.c;
            case 17:
                return !_localctx.c;
            case 18:
                return !_localctx.c;
            case 19:
                return !_localctx.c;
        }
        return true;
    }

    private boolean primary_sempred(PrimaryContext _localctx, int predIndex) {
        switch(predIndex) {
            case 20:
                return !_localctx.c;
            case 21:
                return _localctx.c;
        }
        return true;
    }

    private boolean secondary_sempred(SecondaryContext _localctx, int predIndex) {
        switch(predIndex) {
            case 22:
                return _localctx.s;
            case 23:
                return _localctx.s;
        }
        return true;
    }

    public static final String _serializedATN = "\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3N\u01c0\4\2\t\2\4" + "\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t" + "\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22" + "\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31" + "\4\32\t\32\4\33\t\33\3\2\7\28\n\2\f\2\16\2;\13\2\3\2\7\2>\n\2\f\2\16\2" + "A\13\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\7\4Q\n" + "\4\f\4\16\4T\13\4\5\4V\n\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\5\5" + "b\n\5\3\5\3\5\3\5\3\5\3\5\3\5\5\5j\n\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5" + "\3\5\3\5\3\5\5\5w\n\5\3\5\3\5\5\5{\n\5\3\5\3\5\5\5\177\n\5\3\5\3\5\3\5" + "\5\5\u0084\n\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5" + "\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\6\5\u009d\n\5\r\5\16\5\u009e\3\5" + "\3\5\3\5\3\5\3\5\3\5\3\5\5\5\u00a8\n\5\3\6\3\6\5\6\u00ac\n\6\3\7\3\7\7" + "\7\u00b0\n\7\f\7\16\7\u00b3\13\7\3\7\3\7\3\b\3\b\3\t\3\t\5\t\u00bb\n\t" + "\3\n\3\n\3\13\3\13\3\13\3\13\7\13\u00c3\n\13\f\13\16\13\u00c6\13\13\3" + "\f\3\f\3\f\7\f\u00cb\n\f\f\f\16\f\u00ce\13\f\3\r\3\r\3\r\5\r\u00d3\n\r" + "\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\17\3\17\3\20\3\20\3\20\3\20\3\20" + "\3\20\3\20\3\20\3\20\5\20\u00e7\n\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20" + "\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20" + "\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20" + "\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20" + "\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\7\20\u0122\n\20\f\20\16\20\u0125" + "\13\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21" + "\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21" + "\3\21\3\21\5\21\u0144\n\21\3\22\3\22\7\22\u0148\n\22\f\22\16\22\u014b" + "\13\22\3\22\3\22\3\22\7\22\u0150\n\22\f\22\16\22\u0153\13\22\3\22\3\22" + "\3\22\3\22\3\22\3\22\6\22\u015b\n\22\r\22\16\22\u015c\3\22\3\22\7\22\u0161" + "\n\22\f\22\16\22\u0164\13\22\5\22\u0166\n\22\5\22\u0168\n\22\3\23\3\23" + "\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23" + "\3\23\3\23\5\23\u017c\n\23\3\24\3\24\3\24\3\24\5\24\u0182\n\24\3\25\3" + "\25\3\25\3\25\3\25\5\25\u0189\n\25\3\26\3\26\3\26\3\26\3\27\3\27\3\27" + "\3\27\7\27\u0193\n\27\f\27\16\27\u0196\13\27\5\27\u0198\n\27\3\27\3\27" + "\3\30\3\30\3\30\5\30\u019f\n\30\3\31\3\31\3\31\3\31\3\31\7\31\u01a6\n" + "\31\f\31\16\31\u01a9\13\31\5\31\u01ab\n\31\3\31\5\31\u01ae\n\31\3\31\3" + "\31\3\31\3\32\5\32\u01b4\n\32\3\32\3\32\3\33\3\33\3\33\3\33\3\33\3\33" + "\5\33\u01be\n\33\3\33\2\3\36\34\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36" + " \"$&(*,.\60\62\64\2\16\3\3\r\r\3\2\67B\3\2\34\36\3\2\37 \3\2!#\3\2$\'" + "\3\2(+\3\2\65\66\3\2CF\4\2\32\33\37 \3\2MN\4\2\26\26LL\u01ec\29\3\2\2" + "\2\4D\3\2\2\2\6I\3\2\2\2\b\u00a7\3\2\2\2\n\u00ab\3\2\2\2\f\u00ad\3\2\2" + "\2\16\u00b6\3\2\2\2\20\u00ba\3\2\2\2\22\u00bc\3\2\2\2\24\u00be\3\2\2\2" + "\26\u00c7\3\2\2\2\30\u00cf\3\2\2\2\32\u00d4\3\2\2\2\34\u00db\3\2\2\2\36" + "\u00e6\3\2\2\2 \u0143\3\2\2\2\"\u0167\3\2\2\2$\u017b\3\2\2\2&\u0181\3" + "\2\2\2(\u0188\3\2\2\2*\u018a\3\2\2\2,\u018e\3\2\2\2.\u019e\3\2\2\2\60" + "\u01ad\3\2\2\2\62\u01b3\3\2\2\2\64\u01bd\3\2\2\2\668\5\4\3\2\67\66\3\2" + "\2\28;\3\2\2\29\67\3\2\2\29:\3\2\2\2:?\3\2\2\2;9\3\2\2\2<>\5\b\5\2=<\3" + "\2\2\2>A\3\2\2\2?=\3\2\2\2?@\3\2\2\2@B\3\2\2\2A?\3\2\2\2BC\7\2\2\3C\3" + "\3\2\2\2DE\5\26\f\2EF\7L\2\2FG\5\6\4\2GH\5\f\7\2H\5\3\2\2\2IU\7\t\2\2" + "JK\5\26\f\2KR\7L\2\2LM\7\f\2\2MN\5\26\f\2NO\7L\2\2OQ\3\2\2\2PL\3\2\2\2" + "QT\3\2\2\2RP\3\2\2\2RS\3\2\2\2SV\3\2\2\2TR\3\2\2\2UJ\3\2\2\2UV\3\2\2\2" + "VW\3\2\2\2WX\7\n\2\2X\7\3\2\2\2YZ\7\16\2\2Z[\7\t\2\2[\\\5\36\20\2\\]\7" + "\n\2\2]a\5\n\6\2^_\7\17\2\2_b\5\n\6\2`b\6\5\2\2a^\3\2\2\2a`\3\2\2\2b\u00a8" + "\3\2\2\2cd\7\20\2\2de\7\t\2\2ef\5\36\20\2fi\7\n\2\2gj\5\n\6\2hj\5\16\b" + "\2ig\3\2\2\2ih\3\2\2\2j\u00a8\3\2\2\2kl\7\21\2\2lm\5\f\7\2mn\7\20\2\2" + "no\7\t\2\2op\5\36\20\2pq\7\n\2\2qr\5\34\17\2r\u00a8\3\2\2\2st\7\22\2\2" + "tv\7\t\2\2uw\5\20\t\2vu\3\2\2\2vw\3\2\2\2wx\3\2\2\2xz\7\r\2\2y{\5\36\20" + "\2zy\3\2\2\2z{\3\2\2\2{|\3\2\2\2|~\7\r\2\2}\177\5\22\n\2~}\3\2\2\2~\177" + "\3\2\2\2\177\u0080\3\2\2\2\u0080\u0083\7\n\2\2\u0081\u0084\5\n\6\2\u0082" + "\u0084\5\16\b\2\u0083\u0081\3\2\2\2\u0083\u0082\3\2\2\2\u0084\u00a8\3" + "\2\2\2\u0085\u0086\7\22\2\2\u0086\u0087\7\t\2\2\u0087\u0088\5\26\f\2\u0088" + "\u0089\7L\2\2\u0089\u008a\7\62\2\2\u008a\u008b\5\36\20\2\u008b\u008c\7" + "\n\2\2\u008c\u008d\5\n\6\2\u008d\u00a8\3\2\2\2\u008e\u008f\5\24\13\2\u008f" + "\u0090\5\34\17\2\u0090\u00a8\3\2\2\2\u0091\u0092\7\23\2\2\u0092\u00a8" + "\5\34\17\2\u0093\u0094\7\24\2\2\u0094\u00a8\5\34\17\2\u0095\u0096\7\25" + "\2\2\u0096\u0097\5\36\20\2\u0097\u0098\5\34\17\2\u0098\u00a8\3\2\2\2\u0099" + "\u009a\7\27\2\2\u009a\u009c\5\f\7\2\u009b\u009d\5\32\16\2\u009c\u009b" + "\3\2\2\2\u009d\u009e\3\2\2\2\u009e\u009c\3\2\2\2\u009e\u009f\3\2\2\2\u009f" + "\u00a8\3\2\2\2\u00a0\u00a1\7\31\2\2\u00a1\u00a2\5\36\20\2\u00a2\u00a3" + "\5\34\17\2\u00a3\u00a8\3\2\2\2\u00a4\u00a5\5\36\20\2\u00a5\u00a6\5\34" + "\17\2\u00a6\u00a8\3\2\2\2\u00a7Y\3\2\2\2\u00a7c\3\2\2\2\u00a7k\3\2\2\2" + "\u00a7s\3\2\2\2\u00a7\u0085\3\2\2\2\u00a7\u008e\3\2\2\2\u00a7\u0091\3" + "\2\2\2\u00a7\u0093\3\2\2\2\u00a7\u0095\3\2\2\2\u00a7\u0099\3\2\2\2\u00a7" + "\u00a0\3\2\2\2\u00a7\u00a4\3\2\2\2\u00a8\t\3\2\2\2\u00a9\u00ac\5\f\7\2" + "\u00aa\u00ac\5\b\5\2\u00ab\u00a9\3\2\2\2\u00ab\u00aa\3\2\2\2\u00ac\13" + "\3\2\2\2\u00ad\u00b1\7\5\2\2\u00ae\u00b0\5\b\5\2\u00af\u00ae\3\2\2\2\u00b0" + "\u00b3\3\2\2\2\u00b1\u00af\3\2\2\2\u00b1\u00b2\3\2\2\2\u00b2\u00b4\3\2" + "\2\2\u00b3\u00b1\3\2\2\2\u00b4\u00b5\7\6\2\2\u00b5\r\3\2\2\2\u00b6\u00b7" + "\7\r\2\2\u00b7\17\3\2\2\2\u00b8\u00bb\5\24\13\2\u00b9\u00bb\5\36\20\2" + "\u00ba\u00b8\3\2\2\2\u00ba\u00b9\3\2\2\2\u00bb\21\3\2\2\2\u00bc\u00bd" + "\5\36\20\2\u00bd\23\3\2\2\2\u00be\u00bf\5\26\f\2\u00bf\u00c4\5\30\r\2" + "\u00c0\u00c1\7\f\2\2\u00c1\u00c3\5\30\r\2\u00c2\u00c0\3\2\2\2\u00c3\u00c6" + "\3\2\2\2\u00c4\u00c2\3\2\2\2\u00c4\u00c5\3\2\2\2\u00c5\25\3\2\2\2\u00c6" + "\u00c4\3\2\2\2\u00c7\u00cc\7K\2\2\u00c8\u00c9\7\7\2\2\u00c9\u00cb\7\b" + "\2\2\u00ca\u00c8\3\2\2\2\u00cb\u00ce\3\2\2\2\u00cc\u00ca\3\2\2\2\u00cc" + "\u00cd\3\2\2\2\u00cd\27\3\2\2\2\u00ce\u00cc\3\2\2\2\u00cf\u00d2\7L\2\2" + "\u00d0\u00d1\7\67\2\2\u00d1\u00d3\5\36\20\2\u00d2\u00d0\3\2\2\2\u00d2" + "\u00d3\3\2\2\2\u00d3\31\3\2\2\2\u00d4\u00d5\7\30\2\2\u00d5\u00d6\7\t\2" + "\2\u00d6\u00d7\7K\2\2\u00d7\u00d8\7L\2\2\u00d8\u00d9\7\n\2\2\u00d9\u00da" + "\5\f\7\2\u00da\33\3\2\2\2\u00db\u00dc\t\2\2\2\u00dc\35\3\2\2\2\u00dd\u00de" + "\b\20\1\2\u00de\u00df\5\"\22\2\u00df\u00e0\t\3\2\2\u00e0\u00e1\5\36\20" + "\3\u00e1\u00e2\b\20\1\2\u00e2\u00e7\3\2\2\2\u00e3\u00e4\5 \21\2\u00e4" + "\u00e5\b\20\1\2\u00e5\u00e7\3\2\2\2\u00e6\u00dd\3\2\2\2\u00e6\u00e3\3" + "\2\2\2\u00e7\u0123\3\2\2\2\u00e8\u00e9\f\16\2\2\u00e9\u00ea\t\4\2\2\u00ea" + "\u00eb\5\36\20\17\u00eb\u00ec\b\20\1\2\u00ec\u0122\3\2\2\2\u00ed\u00ee" + "\f\r\2\2\u00ee\u00ef\t\5\2\2\u00ef\u00f0\5\36\20\16\u00f0\u00f1\b\20\1" + "\2\u00f1\u0122\3\2\2\2\u00f2\u00f3\f\f\2\2\u00f3\u00f4\t\6\2\2\u00f4\u00f5" + "\5\36\20\r\u00f5\u00f6\b\20\1\2\u00f6\u0122\3\2\2\2\u00f7\u00f8\f\13\2" + "\2\u00f8\u00f9\t\7\2\2\u00f9\u00fa\5\36\20\f\u00fa\u00fb\b\20\1\2\u00fb" + "\u0122\3\2\2\2\u00fc\u00fd\f\n\2\2\u00fd\u00fe\t\b\2\2\u00fe\u00ff\5\36" + "\20\13\u00ff\u0100\b\20\1\2\u0100\u0122\3\2\2\2\u0101\u0102\f\t\2\2\u0102" + "\u0103\7,\2\2\u0103\u0104\5\36\20\n\u0104\u0105\b\20\1\2\u0105\u0122\3" + "\2\2\2\u0106\u0107\f\b\2\2\u0107\u0108\7-\2\2\u0108\u0109\5\36\20\t\u0109" + "\u010a\b\20\1\2\u010a\u0122\3\2\2\2\u010b\u010c\f\7\2\2\u010c\u010d\7" + ".\2\2\u010d\u010e\5\36\20\b\u010e\u010f\b\20\1\2\u010f\u0122\3\2\2\2\u0110" + "\u0111\f\6\2\2\u0111\u0112\7/\2\2\u0112\u0113\5\36\20\7\u0113\u0114\b" + "\20\1\2\u0114\u0122\3\2\2\2\u0115\u0116\f\5\2\2\u0116\u0117\7\60\2\2\u0117" + "\u0118\5\36\20\6\u0118\u0119\b\20\1\2\u0119\u0122\3\2\2\2\u011a\u011b" + "\f\4\2\2\u011b\u011c\7\61\2\2\u011c\u011d\5\36\20\2\u011d\u011e\7\62\2" + "\2\u011e\u011f\5\36\20\4\u011f\u0120\b\20\1\2\u0120\u0122\3\2\2\2\u0121" + "\u00e8\3\2\2\2\u0121\u00ed\3\2\2\2\u0121\u00f2\3\2\2\2\u0121\u00f7\3\2" + "\2\2\u0121\u00fc\3\2\2\2\u0121\u0101\3\2\2\2\u0121\u0106\3\2\2\2\u0121" + "\u010b\3\2\2\2\u0121\u0110\3\2\2\2\u0121\u0115\3\2\2\2\u0121\u011a\3\2" + "\2\2\u0122\u0125\3\2\2\2\u0123\u0121\3\2\2\2\u0123\u0124\3\2\2\2\u0124" + "\37\3\2\2\2\u0125\u0123\3\2\2\2\u0126\u0127\6\21\16\3\u0127\u0128\t\t" + "\2\2\u0128\u0144\5\"\22\2\u0129\u012a\6\21\17\3\u012a\u012b\5\"\22\2\u012b" + "\u012c\t\t\2\2\u012c\u0144\3\2\2\2\u012d\u012e\6\21\20\3\u012e\u0144\5" + "\"\22\2\u012f\u0130\6\21\21\3\u0130\u0131\t\n\2\2\u0131\u0144\b\21\1\2" + "\u0132\u0133\6\21\22\3\u0133\u0134\7H\2\2\u0134\u0144\b\21\1\2\u0135\u0136" + "\6\21\23\3\u0136\u0137\7I\2\2\u0137\u0144\b\21\1\2\u0138\u0139\6\21\24" + "\3\u0139\u013a\7J\2\2\u013a\u0144\b\21\1\2\u013b\u013c\6\21\25\3\u013c" + "\u013d\t\13\2\2\u013d\u0144\5 \21\2\u013e\u013f\7\t\2\2\u013f\u0140\5" + "\26\f\2\u0140\u0141\7\n\2\2\u0141\u0142\5 \21\2\u0142\u0144\3\2\2\2\u0143" + "\u0126\3\2\2\2\u0143\u0129\3\2\2\2\u0143\u012d\3\2\2\2\u0143\u012f\3\2" + "\2\2\u0143\u0132\3\2\2\2\u0143\u0135\3\2\2\2\u0143\u0138\3\2\2\2\u0143" + "\u013b\3\2\2\2\u0143\u013e\3\2\2\2\u0144!\3\2\2\2\u0145\u0149\5$\23\2" + "\u0146\u0148\5&\24\2\u0147\u0146\3\2\2\2\u0148\u014b\3\2\2\2\u0149\u0147" + "\3\2\2\2\u0149\u014a\3\2\2\2\u014a\u0168\3\2\2\2\u014b\u0149\3\2\2\2\u014c" + "\u014d\5\26\f\2\u014d\u0151\5(\25\2\u014e\u0150\5&\24\2\u014f\u014e\3" + "\2\2\2\u0150\u0153\3\2\2\2\u0151\u014f\3\2\2\2\u0151\u0152\3\2\2\2\u0152" + "\u0168\3\2\2\2\u0153\u0151\3\2\2\2\u0154\u0155\7\26\2\2\u0155\u015a\7" + "K\2\2\u0156\u0157\7\7\2\2\u0157\u0158\5\36\20\2\u0158\u0159\7\b\2\2\u0159" + "\u015b\3\2\2\2\u015a\u0156\3\2\2\2\u015b\u015c\3\2\2\2\u015c\u015a\3\2" + "\2\2\u015c\u015d\3\2\2\2\u015d\u0165\3\2\2\2\u015e\u0162\5(\25\2\u015f" + "\u0161\5&\24\2\u0160\u015f\3\2\2\2\u0161\u0164\3\2\2\2\u0162\u0160\3\2" + "\2\2\u0162\u0163\3\2\2\2\u0163\u0166\3\2\2\2\u0164\u0162\3\2\2\2\u0165" + "\u015e\3\2\2\2\u0165\u0166\3\2\2\2\u0166\u0168\3\2\2\2\u0167\u0145\3\2" + "\2\2\u0167\u014c\3\2\2\2\u0167\u0154\3\2\2\2\u0168#\3\2\2\2\u0169\u016a" + "\6\23\26\3\u016a\u016b\7\t\2\2\u016b\u016c\5\36\20\2\u016c\u016d\7\n\2" + "\2\u016d\u016e\b\23\1\2\u016e\u017c\3\2\2\2\u016f\u0170\6\23\27\3\u0170" + "\u0171\7\t\2\2\u0171\u0172\5 \21\2\u0172\u0173\7\n\2\2\u0173\u017c\3\2" + "\2\2\u0174\u017c\7G\2\2\u0175\u017c\7L\2\2\u0176\u0177\7L\2\2\u0177\u017c" + "\5,\27\2\u0178\u0179\7\26\2\2\u0179\u017a\7K\2\2\u017a\u017c\5,\27\2\u017b" + "\u0169\3\2\2\2\u017b\u016f\3\2\2\2\u017b\u0174\3\2\2\2\u017b\u0175\3\2" + "\2\2\u017b\u0176\3\2\2\2\u017b\u0178\3\2\2\2\u017c%\3\2\2\2\u017d\u017e" + "\6\24\30\3\u017e\u0182\5(\25\2\u017f\u0180\6\24\31\3\u0180\u0182\5*\26" + "\2\u0181\u017d\3\2\2\2\u0181\u017f\3\2\2\2\u0182\'\3\2\2\2\u0183\u0184" + "\7\13\2\2\u0184\u0185\7N\2\2\u0185\u0189\5,\27\2\u0186\u0187\7\13\2\2" + "\u0187\u0189\t\f\2\2\u0188\u0183\3\2\2\2\u0188\u0186\3\2\2\2\u0189)\3" + "\2\2\2\u018a\u018b\7\7\2\2\u018b\u018c\5\36\20\2\u018c\u018d\7\b\2\2\u018d" + "+\3\2\2\2\u018e\u0197\7\t\2\2\u018f\u0194\5.\30\2\u0190\u0191\7\f\2\2" + "\u0191\u0193\5.\30\2\u0192\u0190\3\2\2\2\u0193\u0196\3\2\2\2\u0194\u0192" + "\3\2\2\2\u0194\u0195\3\2\2\2\u0195\u0198\3\2\2\2\u0196\u0194\3\2\2\2\u0197" + "\u018f\3\2\2\2\u0197\u0198\3\2\2\2\u0198\u0199\3\2\2\2\u0199\u019a\7\n" + "\2\2\u019a-\3\2\2\2\u019b\u019f\5\36\20\2\u019c\u019f\5\60\31\2\u019d" + "\u019f\5\64\33\2\u019e\u019b\3\2\2\2\u019e\u019c\3\2\2\2\u019e\u019d\3" + "\2\2\2\u019f/\3\2\2\2\u01a0\u01ae\5\62\32\2\u01a1\u01aa\7\t\2\2\u01a2" + "\u01a7\5\62\32\2\u01a3\u01a4\7\f\2\2\u01a4\u01a6\5\62\32\2\u01a5\u01a3" + "\3\2\2\2\u01a6\u01a9\3\2\2\2\u01a7\u01a5\3\2\2\2\u01a7\u01a8\3\2\2\2\u01a8" + "\u01ab\3\2\2\2\u01a9\u01a7\3\2\2\2\u01aa\u01a2\3\2\2\2\u01aa\u01ab\3\2" + "\2\2\u01ab\u01ac\3\2\2\2\u01ac\u01ae\7\n\2\2\u01ad\u01a0\3\2\2\2\u01ad" + "\u01a1\3\2\2\2\u01ae\u01af\3\2\2\2\u01af\u01b0\7\64\2\2\u01b0\u01b1\5" + "\f\7\2\u01b1\61\3\2\2\2\u01b2\u01b4\5\26\f\2\u01b3\u01b2\3\2\2\2\u01b3" + "\u01b4\3\2\2\2\u01b4\u01b5\3\2\2\2\u01b5\u01b6\7L\2\2\u01b6\63\3\2\2\2" + "\u01b7\u01b8\7K\2\2\u01b8\u01b9\7\63\2\2\u01b9\u01be\t\r\2\2\u01ba\u01bb" + "\7L\2\2\u01bb\u01bc\7\63\2\2\u01bc\u01be\7L\2\2\u01bd\u01b7\3\2\2\2\u01bd" + "\u01ba\3\2\2\2\u01be\65\3\2\2\2)9?RUaivz~\u0083\u009e\u00a7\u00ab\u00b1" + "\u00ba\u00c4\u00cc\u00d2\u00e6\u0121\u0123\u0143\u0149\u0151\u015c\u0162" + "\u0165\u0167\u017b\u0181\u0188\u0194\u0197\u019e\u01a7\u01aa\u01ad\u01b3" + "\u01bd";

    public static final ATN _ATN = new ATNDeserializer().deserialize(_serializedATN.toCharArray());

    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}
