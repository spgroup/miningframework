package com.oracle.truffle.regex.tregex.parser;

import java.util.ArrayList;
import java.util.Arrays;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.regex.RegexFlags;
import com.oracle.truffle.regex.RegexLanguage;
import com.oracle.truffle.regex.RegexOptions;
import com.oracle.truffle.regex.RegexSource;
import com.oracle.truffle.regex.RegexSyntaxException;
import com.oracle.truffle.regex.charset.CodePointSet;
import com.oracle.truffle.regex.charset.CodePointSetAccumulator;
import com.oracle.truffle.regex.charset.Constants;
import com.oracle.truffle.regex.errors.ErrorMessages;
import com.oracle.truffle.regex.tregex.TRegexOptions;
import com.oracle.truffle.regex.tregex.buffer.CompilationBuffer;
import com.oracle.truffle.regex.tregex.buffer.IntRangesBuffer;
import com.oracle.truffle.regex.tregex.buffer.ObjectArrayBuffer;
import com.oracle.truffle.regex.tregex.parser.Token.Quantifier;
import com.oracle.truffle.regex.tregex.parser.ast.BackReference;
import com.oracle.truffle.regex.tregex.parser.ast.CalcASTPropsVisitor;
import com.oracle.truffle.regex.tregex.parser.ast.CharacterClass;
import com.oracle.truffle.regex.tregex.parser.ast.Group;
import com.oracle.truffle.regex.tregex.parser.ast.LookAheadAssertion;
import com.oracle.truffle.regex.tregex.parser.ast.LookAroundAssertion;
import com.oracle.truffle.regex.tregex.parser.ast.LookBehindAssertion;
import com.oracle.truffle.regex.tregex.parser.ast.PositionAssertion;
import com.oracle.truffle.regex.tregex.parser.ast.QuantifiableTerm;
import com.oracle.truffle.regex.tregex.parser.ast.RegexAST;
import com.oracle.truffle.regex.tregex.parser.ast.RegexASTNode;
import com.oracle.truffle.regex.tregex.parser.ast.RegexASTRootNode;
import com.oracle.truffle.regex.tregex.parser.ast.RegexASTSubtreeRootNode;
import com.oracle.truffle.regex.tregex.parser.ast.Sequence;
import com.oracle.truffle.regex.tregex.parser.ast.Term;
import com.oracle.truffle.regex.tregex.parser.ast.visitors.CopyVisitor;
import com.oracle.truffle.regex.tregex.parser.ast.visitors.DepthFirstTraversalRegexASTVisitor;
import com.oracle.truffle.regex.tregex.parser.ast.visitors.InitIDVisitor;
import com.oracle.truffle.regex.tregex.parser.ast.visitors.MarkLookBehindEntriesVisitor;
import com.oracle.truffle.regex.tregex.parser.ast.visitors.NodeCountVisitor;
import com.oracle.truffle.regex.tregex.parser.ast.visitors.SetSourceSectionVisitor;
import com.oracle.truffle.regex.tregex.parser.flavors.RubyFlavor;
import com.oracle.truffle.regex.tregex.string.Encodings;

public final class RegexParser {

    private final RegexParserGlobals globals;

    private final RegexAST ast;

    private final RegexSource source;

    private final RegexFlags flags;

    private final RegexLexer lexer;

    private final RegexProperties properties;

    private final Counter.ThresholdCounter groupCount;

    private final CopyVisitor copyVisitor;

    private final NodeCountVisitor countVisitor;

    private final SetSourceSectionVisitor setSourceSectionVisitor;

    private Sequence curSequence;

    private Group curGroup;

    private Term curTerm;

    private final CompilationBuffer compilationBuffer;

    @TruffleBoundary
    public RegexParser(RegexLanguage language, RegexSource source, RegexFlags flags, CompilationBuffer compilationBuffer) throws RegexSyntaxException {
        this.globals = language.parserGlobals;
        this.source = source;
        this.flags = flags;
        this.lexer = new RegexLexer(source, flags);
        this.ast = new RegexAST(language, source, flags);
        this.properties = ast.getProperties();
        this.groupCount = ast.getGroupCount();
        this.copyVisitor = new CopyVisitor(ast);
        this.countVisitor = new NodeCountVisitor();
        this.setSourceSectionVisitor = source.getOptions().isDumpAutomata() ? new SetSourceSectionVisitor(ast) : null;
        this.compilationBuffer = compilationBuffer;
    }

    public static Group parseRootLess(RegexLanguage language, String pattern) throws RegexSyntaxException {
        return new RegexParser(language, new RegexSource(pattern, "", RegexOptions.DEFAULT, null), RegexFlags.DEFAULT, new CompilationBuffer(Encodings.UTF_16_RAW)).parse(false);
    }

    @TruffleBoundary
    public RegexAST parse() throws RegexSyntaxException {
        ast.setRoot(parse(true));
        ast.setNamedCaputureGroups(lexer.getNamedCaptureGroups());
        return ast;
    }

    public void prepareForDFA() {
        if (properties.hasQuantifiers()) {
            UnrollQuantifiersVisitor.unrollQuantifiers(this, ast.getRoot());
        }
        CalcASTPropsVisitor.run(ast);
        ast.createPrefix();
        InitIDVisitor.init(ast);
        if (!properties.hasNonLiteralLookBehindAssertions() && !ast.getRoot().hasBackReferences() && !properties.hasLargeCountedRepetitions()) {
            new MarkLookBehindEntriesVisitor(ast).run();
        }
        checkInnerLiteral();
    }

    private void checkInnerLiteral() {
        if (ast.isLiteralString() || ast.getRoot().startsWithCaret() || ast.getRoot().endsWithDollar() || ast.getRoot().size() != 1 || flags.isSticky()) {
            return;
        }
        ArrayList<Term> terms = ast.getRoot().getFirstAlternative().getTerms();
        int literalStart = -1;
        int literalEnd = -1;
        for (int i = 0; i < terms.size(); i++) {
            Term t = terms.get(i);
            if (t.isCharacterClass() && (t.asCharacterClass().getCharSet().matchesSingleChar() || t.asCharacterClass().getCharSet().matches2CharsWith1BitDifference()) && ast.getEncoding().isFixedCodePointWidth(t.asCharacterClass().getCharSet()) && (ast.getEncoding() == Encodings.UTF_16_RAW || !t.asCharacterClass().getCharSet().intersects(Constants.SURROGATES))) {
                if (literalStart < 0) {
                    literalStart = i;
                }
                literalEnd = i + 1;
            } else if (literalStart >= 0 || t.hasLoops() || t.hasBackReferences()) {
                break;
            } else {
                if (t.getMaxPath() > 4) {
                    return;
                }
            }
        }
        if (literalStart >= 0 && (literalStart > 0 || literalEnd - literalStart > 1)) {
            properties.setInnerLiteral(literalStart, literalEnd);
        }
    }

    public RegexFlags getFlags() {
        return flags;
    }

    private void createGroup(Token token) {
        createGroup(token, true, false, true, null);
    }

    private void createCaptureGroup(Token token) {
        createGroup(token, true, true, true, null);
    }

    private Group createGroup(Token token, boolean addToSeq, boolean capture, RegexASTSubtreeRootNode parent) {
        return createGroup(token, addToSeq, capture, true, parent);
    }

    private Group createGroup(Token token, boolean addToSeq, boolean capture, boolean setEnclosed, RegexASTSubtreeRootNode parent) {
        Group group = capture ? ast.createCaptureGroup(groupCount.inc()) : ast.createGroup();
        if (parent != null) {
            parent.setGroup(group);
        }
        if (addToSeq) {
            addTerm(group);
        }
        ast.addSourceSection(group, token);
        curGroup = group;
        if (setEnclosed) {
            curGroup.setEnclosedCaptureGroupsLow(groupCount.getCount());
        }
        addSequence();
        return group;
    }

    private void addSequence() {
        curSequence = curGroup.addSequence(ast);
        curTerm = null;
    }

    private void popGroup(Token token) throws RegexSyntaxException {
        popGroup(token, true);
    }

    private void popGroup(Token token, boolean setEnclosed) throws RegexSyntaxException {
        if (setEnclosed) {
            curGroup.setEnclosedCaptureGroupsHigh(groupCount.getCount());
        }
        ast.addSourceSection(curGroup, token);
        if (curGroup.getParent().isLookAroundAssertion()) {
            ast.addSourceSection(curGroup.getParent(), token);
        }
        curTerm = curGroup;
        RegexASTNode parent = curGroup.getParent();
        if (parent instanceof RegexASTRootNode) {
            throw syntaxError(ErrorMessages.UNMATCHED_RIGHT_PARENTHESIS);
        }
        if (parent.isLookAroundAssertion()) {
            curSequence = parent.getParent().asSequence();
            curTerm = (Term) parent;
        } else {
            curSequence = (Sequence) parent;
        }
        curGroup = curSequence.getParent();
    }

    private void optimizeLookAround() {
        if (curTerm == null || !curTerm.isLookAroundAssertion()) {
            return;
        }
        LookAroundAssertion lookaround = (LookAroundAssertion) curTerm;
        Group group = lookaround.getGroup();
        if (!group.isCapturing()) {
            if ((group.isEmpty() || (group.size() == 1 && group.getFirstAlternative().isEmpty()))) {
                if (lookaround.isNegated()) {
                    replaceCurTermWithDeadNode();
                } else {
                    removeCurTerm();
                }
            } else if (!lookaround.isNegated()) {
                if (group.size() == 1 && group.getFirstAlternative().size() == 1 && group.getFirstAlternative().getFirstTerm().isPositionAssertion()) {
                    removeCurTerm();
                    addTerm(group.getFirstAlternative().getFirstTerm());
                } else {
                    int innerPositionAssertion = -1;
                    for (int i = 0; i < group.size(); i++) {
                        Sequence s = group.getAlternatives().get(i);
                        if (s.size() == 1 && s.getFirstTerm().isPositionAssertion()) {
                            innerPositionAssertion = i;
                            break;
                        }
                    }
                    if (innerPositionAssertion >= 0) {
                        curSequence.removeLastTerm();
                        Sequence removed = group.getAlternatives().remove(innerPositionAssertion);
                        Group wrapGroup = ast.createGroup();
                        wrapGroup.setEnclosedCaptureGroupsLow(group.getEnclosedCaptureGroupsLow());
                        wrapGroup.setEnclosedCaptureGroupsHigh(group.getEnclosedCaptureGroupsHigh());
                        wrapGroup.add(removed);
                        Sequence wrapSeq = wrapGroup.addSequence(ast);
                        wrapSeq.add(lookaround);
                        addTerm(wrapGroup);
                    }
                }
            }
        }
        if (lookaround.isNegated() && group.size() == 1 && group.getFirstAlternative().isSingleCharClass()) {
            lookaround.setNegated(false);
            CharacterClass cc = group.getFirstAlternative().getFirstTerm().asCharacterClass();
            assert !flags.isUnicode() || !source.getOptions().isUTF16ExplodeAstralSymbols() || cc.getCharSet().matchesNothing() || cc.getCharSet().getMax() <= 0xffff;
            assert !group.hasEnclosedCaptureGroups();
            cc.setCharSet(cc.getCharSet().createInverse(ast.getEncoding()));
            curSequence.removeLastTerm();
            Group wrapGroup = ast.createGroup();
            Sequence positionAssertionSeq = wrapGroup.addSequence(ast);
            positionAssertionSeq.add(ast.createPositionAssertion(lookaround.isLookAheadAssertion() ? PositionAssertion.Type.DOLLAR : PositionAssertion.Type.CARET));
            Sequence wrapSeq = wrapGroup.addSequence(ast);
            wrapSeq.add(lookaround);
            addTerm(wrapGroup);
        }
    }

    private void addTerm(Term term) {
        curSequence.add(term);
        curTerm = term;
    }

    private void addLookBehindAssertion(Token token, boolean negate) {
        LookBehindAssertion lookBehind = ast.createLookBehindAssertion(negate);
        ast.addSourceSection(lookBehind, token);
        addTerm(lookBehind);
        createGroup(token, false, false, lookBehind);
    }

    private void addLookAheadAssertion(Token token, boolean negate) {
        LookAheadAssertion lookAhead = ast.createLookAheadAssertion(negate);
        ast.addSourceSection(lookAhead, token);
        addTerm(lookAhead);
        createGroup(token, false, false, lookAhead);
    }

    private Term translateUnicodeCharClass(Token.CharacterClass token) {
        CodePointSet codePointSet = token.getCodePointSet();
        if (!source.getOptions().isUTF16ExplodeAstralSymbols() || Constants.BMP_WITHOUT_SURROGATES.contains(token.getCodePointSet())) {
            return createCharClass(codePointSet, token, token.wasSingleChar());
        }
        Group group = ast.createGroup();
        group.setEnclosedCaptureGroupsLow(groupCount.getCount());
        group.setEnclosedCaptureGroupsHigh(groupCount.getCount());
        IntRangesBuffer tmp = compilationBuffer.getIntRangesBuffer1();
        CodePointSet bmpRanges = codePointSet.createIntersection(Constants.BMP_WITHOUT_SURROGATES, tmp);
        CodePointSet astralRanges = codePointSet.createIntersection(Constants.ASTRAL_SYMBOLS, tmp);
        CodePointSet loneLeadSurrogateRanges = codePointSet.createIntersection(Constants.LEAD_SURROGATES, tmp);
        CodePointSet loneTrailSurrogateRanges = codePointSet.createIntersection(Constants.TRAIL_SURROGATES, tmp);
        assert astralRanges.matchesSomething() || loneLeadSurrogateRanges.matchesSomething() || loneTrailSurrogateRanges.matchesSomething();
        if (bmpRanges.matchesSomething()) {
            Sequence bmpAlternative = group.addSequence(ast);
            bmpAlternative.add(createCharClass(bmpRanges, token));
        }
        if (loneLeadSurrogateRanges.matchesSomething()) {
            Sequence loneLeadSurrogateAlternative = group.addSequence(ast);
            loneLeadSurrogateAlternative.add(createCharClass(loneLeadSurrogateRanges, token));
            loneLeadSurrogateAlternative.add(globals.noTrailSurrogateAhead.copyRecursive(ast, compilationBuffer));
        }
        if (loneTrailSurrogateRanges.matchesSomething()) {
            Sequence loneTrailSurrogateAlternative = group.addSequence(ast);
            loneTrailSurrogateAlternative.add(globals.noLeadSurrogateBehind.copyRecursive(ast, compilationBuffer));
            loneTrailSurrogateAlternative.add(createCharClass(loneTrailSurrogateRanges, token));
        }
        if (astralRanges.matchesSomething()) {
            CodePointSetAccumulator completeRanges = compilationBuffer.getCodePointSetAccumulator1();
            completeRanges.clear();
            char curLead = Character.highSurrogate(astralRanges.getLo(0));
            CodePointSetAccumulator curTrails = compilationBuffer.getCodePointSetAccumulator2();
            curTrails.clear();
            for (int i = 0; i < astralRanges.size(); i++) {
                char startLead = Character.highSurrogate(astralRanges.getLo(i));
                final char startTrail = Character.lowSurrogate(astralRanges.getLo(i));
                char endLead = Character.highSurrogate(astralRanges.getHi(i));
                final char endTrail = Character.lowSurrogate(astralRanges.getHi(i));
                if (startLead > curLead) {
                    if (!curTrails.isEmpty()) {
                        Sequence finishedAlternative = group.addSequence(ast);
                        finishedAlternative.add(createCharClass(CodePointSet.create(curLead), token));
                        finishedAlternative.add(createCharClass(curTrails.toCodePointSet(), token));
                    }
                    curLead = startLead;
                    curTrails.clear();
                }
                if (startLead == endLead) {
                    curTrails.addRange(startTrail, endTrail);
                } else {
                    if (startTrail != Constants.TRAIL_SURROGATES.getLo(0)) {
                        curTrails.addRange(startTrail, Constants.TRAIL_SURROGATES.getHi(0));
                        assert startLead < Character.MAX_VALUE;
                        startLead = (char) (startLead + 1);
                    }
                    if (!curTrails.isEmpty()) {
                        Sequence finishedAlternative = group.addSequence(ast);
                        finishedAlternative.add(createCharClass(CodePointSet.create(curLead), token));
                        finishedAlternative.add(createCharClass(curTrails.toCodePointSet(), token));
                    }
                    curLead = endLead;
                    curTrails.clear();
                    if (endTrail != Constants.TRAIL_SURROGATES.getHi(0)) {
                        curTrails.addRange(Constants.TRAIL_SURROGATES.getLo(0), endTrail);
                        assert endLead > Character.MIN_VALUE;
                        endLead = (char) (endLead - 1);
                    }
                    if (startLead <= endLead) {
                        completeRanges.addRange(startLead, endLead);
                    }
                }
            }
            if (!curTrails.isEmpty()) {
                Sequence lastAlternative = group.addSequence(ast);
                lastAlternative.add(createCharClass(CodePointSet.create(curLead), token));
                lastAlternative.add(createCharClass(curTrails.toCodePointSet(), token));
            }
            if (!completeRanges.isEmpty()) {
                Sequence completeRangesAlt = ast.createSequence();
                group.insertFirst(completeRangesAlt);
                completeRangesAlt.add(createCharClass(completeRanges.toCodePointSet(), token));
                completeRangesAlt.add(createCharClass(Constants.TRAIL_SURROGATES, token));
            }
        }
        assert !(group.size() == 1 && group.getFirstAlternative().getTerms().size() == 1);
        return group;
    }

    private void addCharClass(Token.CharacterClass token) {
        CodePointSet codePointSet = token.getCodePointSet();
        if (flags.isUnicode()) {
            if (codePointSet.matchesNothing()) {
                addTerm(createCharClass(CodePointSet.getEmpty(), token));
            } else {
                addTerm(translateUnicodeCharClass(token));
            }
        } else {
            addTerm(createCharClass(codePointSet, token, token.wasSingleChar()));
        }
    }

    private CharacterClass createCharClass(CodePointSet charSet, Token token) {
        return createCharClass(charSet, token, false);
    }

    private CharacterClass createCharClass(CodePointSet charSet, Token token, boolean wasSingleChar) {
        CharacterClass characterClass = ast.createCharacterClass(charSet);
        ast.addSourceSection(characterClass, token);
        if (wasSingleChar) {
            characterClass.setWasSingleChar();
        }
        return characterClass;
    }

    private void createOptionalBranch(QuantifiableTerm term, Quantifier quantifier, boolean copy, boolean unroll, int recurse) throws RegexSyntaxException {
        addTerm(copy ? copyVisitor.copy(term) : term);
        curTerm.setExpandedQuantifier(false);
        ((QuantifiableTerm) curTerm).setQuantifier(null);
        curTerm.setEmptyGuard(true);
        createOptional(term, quantifier, true, unroll, recurse - 1);
    }

    private void createOptional(QuantifiableTerm term, Quantifier quantifier, boolean copy, boolean unroll, int recurse) throws RegexSyntaxException {
        if (recurse < 0) {
            return;
        }
        if (copy) {
            createGroup(null, true, false, false, null);
        }
        curGroup.setExpandedQuantifier(unroll);
        curGroup.setQuantifier(quantifier);
        if (term.isGroup()) {
            curGroup.setEnclosedCaptureGroupsLow(term.asGroup().getEnclosedCaptureGroupsLow());
            curGroup.setEnclosedCaptureGroupsHigh(term.asGroup().getEnclosedCaptureGroupsHigh());
        }
        if (quantifier.isGreedy()) {
            createOptionalBranch(term, quantifier, copy, unroll, recurse);
            addSequence();
            curSequence.setExpandedQuantifier(true);
        } else {
            curSequence.setExpandedQuantifier(true);
            addSequence();
            createOptionalBranch(term, quantifier, copy, unroll, recurse);
        }
        popGroup(null, false);
    }

    private void expandQuantifier(QuantifiableTerm toExpand, boolean unroll) {
        assert toExpand.hasNotUnrolledQuantifier();
        Token.Quantifier quantifier = toExpand.getQuantifier();
        assert !unroll || toExpand.isUnrollingCandidate();
        curTerm = toExpand;
        curSequence = (Sequence) curTerm.getParent();
        curGroup = curSequence.getParent();
        ObjectArrayBuffer<Term> buf = compilationBuffer.getObjectBuffer1();
        if (unroll && quantifier.getMin() > 0) {
            int size = curSequence.size();
            for (int i = curTerm.getSeqIndex() + 1; i < size; i++) {
                buf.add(curSequence.getLastTerm());
                curSequence.removeLastTerm();
            }
            curTerm.setExpandedQuantifier(true);
            for (int i = 0; i < quantifier.getMin() - 1; i++) {
                addTerm(copyVisitor.copy(curTerm));
                curTerm.setExpandedQuantifier(true);
            }
        } else {
            assert !unroll || quantifier.getMin() == 0;
            toExpand.getParent().asSequence().replace(toExpand.getSeqIndex(), createGroup(null, false, false, false, null));
        }
        createOptional(toExpand, quantifier, unroll && quantifier.getMin() > 0, unroll, !unroll || quantifier.isInfiniteLoop() ? 0 : (quantifier.getMax() - quantifier.getMin()) - 1);
        if (!unroll || quantifier.isInfiniteLoop()) {
            ((Group) curTerm).setLoop(true);
        }
        if (unroll && quantifier.getMin() > 0) {
            for (int i = buf.length() - 1; i >= 0; i--) {
                curSequence.add(buf.get(i));
            }
        }
    }

    private static final class UnrollQuantifiersVisitor extends DepthFirstTraversalRegexASTVisitor {

        private final RegexParser parser;

        private final ShouldUnrollQuantifierVisitor shouldUnrollVisitor = new ShouldUnrollQuantifierVisitor();

        private UnrollQuantifiersVisitor(RegexParser parser) {
            this.parser = parser;
        }

        public static void unrollQuantifiers(RegexParser parser, RegexASTNode runRoot) {
            new UnrollQuantifiersVisitor(parser).run(runRoot);
        }

        @Override
        protected void visit(BackReference backReference) {
            if (backReference.hasNotUnrolledQuantifier()) {
                parser.expandQuantifier(backReference, shouldUnroll(backReference));
            }
        }

        @Override
        protected void visit(CharacterClass characterClass) {
            if (characterClass.hasNotUnrolledQuantifier()) {
                parser.expandQuantifier(characterClass, shouldUnroll(characterClass));
            }
        }

        @Override
        protected void leave(Group group) {
            if (group.hasNotUnrolledQuantifier() && !group.getFirstAlternative().isExpandedQuantifier() && !group.getLastAlternative().isExpandedQuantifier()) {
                parser.expandQuantifier(group, shouldUnroll(group) && shouldUnrollVisitor.shouldUnroll(group));
            }
        }

        private boolean shouldUnroll(QuantifiableTerm term) {
            return term.getQuantifier().isUnrollTrivial() || (parser.ast.getNumberOfNodes() <= TRegexOptions.TRegexMaxParseTreeSizeForDFA && term.isUnrollingCandidate());
        }

        private static final class ShouldUnrollQuantifierVisitor extends DepthFirstTraversalRegexASTVisitor {

            private Group root;

            private boolean result;

            boolean shouldUnroll(Group group) {
                assert group.hasQuantifier();
                result = true;
                root = group;
                run(group);
                return result;
            }

            @Override
            protected void visit(BackReference backReference) {
                result = false;
            }

            @Override
            protected void visit(Group group) {
                if (group != root && group.hasNotUnrolledQuantifier()) {
                    result = false;
                }
            }
        }
    }

    private void substitute(Token token, Group substitution) {
        Group copy = substitution.copyRecursive(ast, compilationBuffer);
        if (source.getOptions().isDumpAutomata()) {
            setSourceSectionVisitor.run(copy, token);
        }
        addTerm(copy);
    }

    private Group parse(boolean rootCapture) throws RegexSyntaxException {
        RegexASTRootNode rootParent = ast.createRootNode();
        Group root = createGroup(null, false, rootCapture, rootParent);
        if (source.getOptions().isDumpAutomata()) {
            ast.addSourceSections(root, Arrays.asList(ast.getSource().getSource().createSection(0, 1), ast.getSource().getSource().createSection(ast.getSource().getPattern().length() + 1, 1)));
        }
        Token token = null;
        Token.Kind prevKind;
        while (lexer.hasNext()) {
            prevKind = token == null ? null : token.kind;
            token = lexer.next();
            if (source.getOptions().getFlavor() != RubyFlavor.INSTANCE && token.kind != Token.Kind.quantifier && curTerm != null && curTerm.isBackReference() && curTerm.asBackReference().isNestedOrForwardReference() && !isNestedInLookBehindAssertion(curTerm)) {
                removeCurTerm();
            } else if (token.kind != Token.Kind.quantifier) {
                optimizeLookAround();
            }
            switch(token.kind) {
                case caret:
                    if (prevKind != Token.Kind.caret) {
                        if (flags.isMultiline()) {
                            substitute(token, globals.multiLineCaretSubstitution);
                        } else {
                            PositionAssertion caret = ast.createPositionAssertion(PositionAssertion.Type.CARET);
                            ast.addSourceSection(caret, token);
                            addTerm(caret);
                        }
                    }
                    break;
                case dollar:
                    if (prevKind != Token.Kind.dollar) {
                        if (flags.isMultiline()) {
                            substitute(token, globals.multiLineDollarSubsitution);
                        } else {
                            PositionAssertion dollar = ast.createPositionAssertion(PositionAssertion.Type.DOLLAR);
                            ast.addSourceSection(dollar, token);
                            addTerm(dollar);
                        }
                    }
                    break;
                case wordBoundary:
                    if (prevKind == Token.Kind.wordBoundary) {
                        break;
                    } else if (prevKind == Token.Kind.nonWordBoundary) {
                        replaceCurTermWithDeadNode();
                        break;
                    }
                    if (flags.isUnicode() && flags.isIgnoreCase()) {
                        substitute(token, globals.unicodeIgnoreCaseWordBoundarySubstitution);
                    } else {
                        substitute(token, globals.wordBoundarySubstituion);
                    }
                    break;
                case nonWordBoundary:
                    if (prevKind == Token.Kind.nonWordBoundary) {
                        break;
                    } else if (prevKind == Token.Kind.wordBoundary) {
                        replaceCurTermWithDeadNode();
                        break;
                    }
                    if (flags.isUnicode() && flags.isIgnoreCase()) {
                        substitute(token, globals.unicodeIgnoreCaseNonWordBoundarySubsitution);
                    } else {
                        substitute(token, globals.nonWordBoundarySubstitution);
                    }
                    break;
                case backReference:
                    BackReference backReference = ast.createBackReference(((Token.BackReference) token).getGroupNr());
                    ast.addSourceSection(backReference, token);
                    addTerm(backReference);
                    if (backReference.getGroupNr() >= groupCount.getCount()) {
                        backReference.setForwardReference();
                    } else if (isNestedBackReference(backReference)) {
                        backReference.setNestedBackReference();
                    }
                    break;
                case quantifier:
                    parseQuantifier((Token.Quantifier) token);
                    break;
                case alternation:
                    if (!tryMergeSingleCharClassAlternations()) {
                        addSequence();
                    }
                    break;
                case captureGroupBegin:
                    createCaptureGroup(token);
                    break;
                case nonCaptureGroupBegin:
                    createGroup(token);
                    break;
                case lookAheadAssertionBegin:
                    addLookAheadAssertion(token, ((Token.LookAheadAssertionBegin) token).isNegated());
                    break;
                case lookBehindAssertionBegin:
                    addLookBehindAssertion(token, ((Token.LookBehindAssertionBegin) token).isNegated());
                    break;
                case groupEnd:
                    if (tryMergeSingleCharClassAlternations()) {
                        curGroup.removeLastSequence();
                        ast.getNodeCount().dec();
                    }
                    optimizeGroup();
                    popGroup(token);
                    break;
                case charClass:
                    addCharClass((Token.CharacterClass) token);
                    break;
            }
        }
        optimizeLookAround();
        if (curGroup != root) {
            throw syntaxError(ErrorMessages.UNTERMINATED_GROUP);
        }
        optimizeGroup();
        root.setEnclosedCaptureGroupsHigh(groupCount.getCount());
        return root;
    }

    private static boolean isNestedBackReference(BackReference backReference) {
        RegexASTNode parent = backReference.getParent().getParent();
        while (true) {
            if (parent.asGroup().getGroupNumber() == backReference.getGroupNr()) {
                return true;
            }
            parent = parent.getParent();
            if (parent.isRoot()) {
                return false;
            }
            if (parent.isLookAroundAssertion()) {
                parent = parent.getParent();
            }
            parent = parent.getParent();
        }
    }

    private static boolean isNestedInLookBehindAssertion(Term t) {
        RegexASTSubtreeRootNode parent = t.getSubTreeParent();
        while (parent.isLookAroundAssertion()) {
            if (parent.isLookBehindAssertion()) {
                return true;
            }
            parent = parent.getParent().getSubTreeParent();
        }
        return false;
    }

    private void optimizeGroup() {
        sortAlternatives(curGroup);
        mergeCommonPrefixes(curGroup);
    }

    private void parseQuantifier(Token.Quantifier quantifier) throws RegexSyntaxException {
        if (curTerm == null) {
            throw syntaxError(ErrorMessages.QUANTIFIER_WITHOUT_TARGET);
        }
        if (flags.isUnicode() && curTerm.isLookAheadAssertion()) {
            throw syntaxError(ErrorMessages.QUANTIFIER_ON_LOOKAHEAD_ASSERTION);
        }
        if (curTerm.isLookBehindAssertion()) {
            throw syntaxError(ErrorMessages.QUANTIFIER_ON_LOOKBEHIND_ASSERTION);
        }
        assert curTerm == curSequence.getLastTerm();
        if (quantifier.getMin() == -1) {
            replaceCurTermWithDeadNode();
            return;
        }
        boolean curTermIsZeroWidthGroup = curTerm.isGroup() && curTerm.asGroup().isAlwaysZeroWidth();
        if (quantifier.getMax() == 0 || quantifier.getMin() == 0 && (curTerm.isLookAroundAssertion() || curTermIsZeroWidthGroup || curTerm.isCharacterClass() && curTerm.asCharacterClass().getCharSet().matchesNothing())) {
            removeCurTerm();
            return;
        }
        ast.addSourceSection(curTerm, quantifier);
        if (curTerm.isLookAroundAssertion() || curTermIsZeroWidthGroup) {
            return;
        }
        if (quantifier.getMin() == 1 && quantifier.getMax() == 1) {
            return;
        }
        setQuantifier((QuantifiableTerm) curTerm, quantifier);
        if (curSequence.size() > 1) {
            Term prevTerm = curSequence.getTerms().get(curSequence.size() - 2);
            if (prevTerm.isQuantifiableTerm()) {
                QuantifiableTerm prev = prevTerm.asQuantifiableTerm();
                if (prev.hasQuantifier() && ((QuantifiableTerm) curTerm).equalsSemantic(prev, true)) {
                    removeCurTerm();
                    long min = (long) prev.getQuantifier().getMin() + quantifier.getMin();
                    long max = prev.getQuantifier().isInfiniteLoop() || quantifier.isInfiniteLoop() ? -1 : (long) prev.getQuantifier().getMax() + quantifier.getMax();
                    if (min > Integer.MAX_VALUE) {
                        replaceCurTermWithDeadNode();
                        return;
                    }
                    if (max > Integer.MAX_VALUE) {
                        max = -1;
                    }
                    setQuantifier(prev, Token.createQuantifier((int) min, (int) max, prev.getQuantifier().isGreedy() || quantifier.isGreedy()));
                }
            }
        }
    }

    private void removeCurTerm() {
        ast.getNodeCount().dec(countVisitor.count(curSequence.getLastTerm()));
        curSequence.removeLastTerm();
        curTerm = curSequence.isEmpty() ? null : curSequence.getLastTerm();
    }

    private void replaceCurTermWithDeadNode() {
        removeCurTerm();
        addTerm(createCharClass(CodePointSet.getEmpty(), null));
    }

    private void setQuantifier(QuantifiableTerm term, Token.Quantifier quantifier) {
        term.setQuantifier(quantifier);
        if (!term.isUnrollingCandidate()) {
            properties.setLargeCountedRepetitions();
        }
        properties.setQuantifiers();
    }

    private boolean tryMergeSingleCharClassAlternations() {
        if (curGroup.size() > 1 && curSequence.isSingleCharClass()) {
            assert curSequence == curGroup.getAlternatives().get(curGroup.size() - 1);
            Sequence prevSequence = curGroup.getAlternatives().get(curGroup.size() - 2);
            if (prevSequence.isSingleCharClass()) {
                mergeCharClasses((CharacterClass) prevSequence.getFirstTerm(), (CharacterClass) curSequence.getFirstTerm());
                curSequence.removeLastTerm();
                ast.getNodeCount().dec();
                return true;
            }
        }
        return false;
    }

    private void mergeCharClasses(CharacterClass dst, CharacterClass src) {
        dst.setCharSet(dst.getCharSet().union(src.getCharSet()));
        dst.setWasSingleChar(false);
        ast.addSourceSections(dst, ast.getSourceSections(src));
    }

    private static void sortAlternatives(Group group) {
        if (group.size() < 2) {
            return;
        }
        int begin = 0;
        while (begin + 1 < group.size()) {
            int end = findSingleCharAlternatives(group, begin);
            if (end > begin + 1) {
                group.getAlternatives().subList(begin, end).sort((Sequence a, Sequence b) -> {
                    return a.getFirstTerm().asCharacterClass().getCharSet().getMin() - b.getFirstTerm().asCharacterClass().getCharSet().getMin();
                });
                begin = end;
            } else {
                begin++;
            }
        }
    }

    private void mergeCommonPrefixes(Group group) {
        if (group.size() < 2) {
            return;
        }
        ArrayList<Sequence> newAlternatives = null;
        int lastEnd = 0;
        int begin = 0;
        while (begin + 1 < group.size()) {
            int end = findMatchingAlternatives(group, begin);
            if (end < 0) {
                begin++;
            } else {
                if (newAlternatives == null) {
                    newAlternatives = new ArrayList<>();
                }
                for (int i = lastEnd; i < begin; i++) {
                    newAlternatives.add(group.getAlternatives().get(i));
                }
                lastEnd = end;
                int prefixSize = 1;
                while (alternativesAreEqualAt(group, begin, end, prefixSize)) {
                    prefixSize++;
                }
                Sequence prefixSeq = ast.createSequence();
                Group innerGroup = ast.createGroup();
                int enclosedCGLo = Integer.MAX_VALUE;
                int enclosedCGHi = Integer.MIN_VALUE;
                boolean emptyAlt = false;
                for (int i = begin; i < end; i++) {
                    Sequence s = group.getAlternatives().get(i);
                    assert s.size() >= prefixSize;
                    for (int j = 0; j < prefixSize; j++) {
                        Term t = s.getTerms().get(j);
                        if (i == begin) {
                            prefixSeq.add(t);
                        } else {
                            ast.addSourceSections(prefixSeq.getTerms().get(j), ast.getSourceSections(t));
                            ast.getNodeCount().dec(countVisitor.count(t));
                        }
                    }
                    if (i > begin && s.size() - prefixSize == 1 && s.getLastTerm().isCharacterClass() && !s.getLastTerm().asCharacterClass().hasQuantifier() && innerGroup.getLastAlternative().isSingleCharClass()) {
                        mergeCharClasses(innerGroup.getLastAlternative().getFirstTerm().asCharacterClass(), s.getLastTerm().asCharacterClass());
                    } else {
                        if (prefixSize == s.size()) {
                            if (!emptyAlt) {
                                innerGroup.addSequence(ast);
                            }
                            emptyAlt = true;
                        } else {
                            Sequence copy = innerGroup.addSequence(ast);
                            for (int j = prefixSize; j < s.size(); j++) {
                                Term t = s.getTerms().get(j);
                                copy.add(t);
                                if (t.isGroup()) {
                                    Group g = t.asGroup();
                                    if (g.getEnclosedCaptureGroupsLow() != g.getEnclosedCaptureGroupsHigh()) {
                                        enclosedCGLo = Math.min(enclosedCGLo, g.getEnclosedCaptureGroupsLow());
                                        enclosedCGHi = Math.max(enclosedCGHi, g.getEnclosedCaptureGroupsHigh());
                                    }
                                    if (g.isCapturing()) {
                                        enclosedCGLo = Math.min(enclosedCGLo, g.getGroupNumber());
                                        enclosedCGHi = Math.max(enclosedCGHi, g.getGroupNumber() + 1);
                                    }
                                }
                            }
                        }
                    }
                }
                if (enclosedCGLo != Integer.MAX_VALUE) {
                    innerGroup.setEnclosedCaptureGroupsLow(enclosedCGLo);
                    innerGroup.setEnclosedCaptureGroupsHigh(enclosedCGHi);
                }
                if (!innerGroup.isEmpty() && !(innerGroup.size() == 1 && innerGroup.getFirstAlternative().isEmpty())) {
                    mergeCommonPrefixes(innerGroup);
                    prefixSeq.add(innerGroup);
                }
                newAlternatives.add(prefixSeq);
                begin = end;
            }
        }
        if (newAlternatives != null) {
            for (int i = lastEnd; i < group.size(); i++) {
                newAlternatives.add(group.getAlternatives().get(i));
            }
            group.setAlternatives(newAlternatives);
        }
    }

    private static boolean alternativesAreEqualAt(Group group, int altBegin, int altEnd, int iTerm) {
        if (group.getAlternatives().get(altBegin).size() <= iTerm) {
            return false;
        }
        Term cmp = group.getAlternatives().get(altBegin).getTerms().get(iTerm);
        for (int i = altBegin + 1; i < altEnd; i++) {
            Sequence s = group.getAlternatives().get(i);
            if (s.size() <= iTerm) {
                return false;
            }
            if (!s.getTerms().get(iTerm).equalsSemantic(cmp)) {
                return false;
            }
        }
        return true;
    }

    private static int findMatchingAlternatives(Group group, int begin) {
        if (group.getAlternatives().get(begin).isEmpty()) {
            return -1;
        }
        Term cmp = group.getAlternatives().get(begin).getFirstTerm();
        int ret = -1;
        for (int i = begin + 1; i < group.size(); i++) {
            Sequence s = group.getAlternatives().get(i);
            if (!s.isEmpty() && cmp.equalsSemantic(s.getFirstTerm())) {
                ret = i + 1;
            } else {
                return ret;
            }
        }
        return ret;
    }

    private static int findSingleCharAlternatives(Group group, int begin) {
        int ret = -1;
        for (int i = begin; i < group.size(); i++) {
            Sequence s = group.getAlternatives().get(i);
            if (s.isEmpty() || !s.getFirstTerm().isCharacterClass() || !s.getFirstTerm().asCharacterClass().wasSingleChar()) {
                return ret;
            }
            ret = i + 1;
        }
        return ret;
    }

    private RegexSyntaxException syntaxError(String msg) {
        return RegexSyntaxException.createPattern(source, msg, lexer.getLastTokenPosition());
    }
}
