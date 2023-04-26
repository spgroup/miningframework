package org.antlr.v4.tool;

import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.tool.ast.ActionAST;
import org.antlr.v4.tool.ast.AltAST;
import org.antlr.v4.tool.ast.GrammarAST;
import org.antlr.v4.tool.ast.PredAST;
import org.antlr.v4.tool.ast.RuleAST;
import org.stringtemplate.v4.misc.MultiMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Rule implements AttributeResolver {

    public static final AttributeDict predefinedRulePropertiesDict = new AttributeDict(AttributeDict.DictType.PREDEFINED_RULE);

    static {
        predefinedRulePropertiesDict.add(new Attribute("parser"));
        predefinedRulePropertiesDict.add(new Attribute("text"));
        predefinedRulePropertiesDict.add(new Attribute("start"));
        predefinedRulePropertiesDict.add(new Attribute("stop"));
        predefinedRulePropertiesDict.add(new Attribute("ctx"));
    }

    public static final Set<String> validLexerCommands = new HashSet<String>();

    static {
        validLexerCommands.add("mode");
        validLexerCommands.add("pushMode");
        validLexerCommands.add("type");
        validLexerCommands.add("channel");
        validLexerCommands.add("popMode");
        validLexerCommands.add("skip");
        validLexerCommands.add("more");
    }

    public final String name;

    public List<GrammarAST> modifiers;

    public RuleAST ast;

    public AttributeDict args;

    public AttributeDict retvals;

    public AttributeDict locals;

    public final Grammar g;

    public final String mode;

    public final boolean caseInsensitive;

    public Map<String, ActionAST> namedActions = new HashMap<String, ActionAST>();

    public List<GrammarAST> exceptions = new ArrayList<GrammarAST>();

    public List<ActionAST> actions = new ArrayList<ActionAST>();

    public ActionAST finallyAction;

    public final int numberOfAlts;

    public boolean isStartRule = true;

    public Alternative[] alt;

    public int index;

    public int actionIndex = -1;

    public Rule(Grammar g, String name, RuleAST ast, int numberOfAlts) {
        this(g, name, ast, numberOfAlts, null, false);
    }

    public Rule(Grammar g, String name, RuleAST ast, int numberOfAlts, String lexerMode, boolean caseInsensitive) {
        this.g = g;
        this.name = name;
        this.ast = ast;
        this.numberOfAlts = numberOfAlts;
        alt = new Alternative[numberOfAlts + 1];
        for (int i = 1; i <= numberOfAlts; i++) alt[i] = new Alternative(this, i);
        this.mode = lexerMode;
        this.caseInsensitive = caseInsensitive;
    }

    public void defineActionInAlt(int currentAlt, ActionAST actionAST) {
        actions.add(actionAST);
        alt[currentAlt].actions.add(actionAST);
        if (g.isLexer()) {
            defineLexerAction(actionAST);
        }
    }

    public void defineLexerAction(ActionAST actionAST) {
        actionIndex = g.lexerActions.size();
        if (g.lexerActions.get(actionAST) == null) {
            g.lexerActions.put(actionAST, actionIndex);
        }
    }

    public void definePredicateInAlt(int currentAlt, PredAST predAST) {
        actions.add(predAST);
        alt[currentAlt].actions.add(predAST);
        if (g.sempreds.get(predAST) == null) {
            g.sempreds.put(predAST, g.sempreds.size());
        }
    }

    public Attribute resolveRetvalOrProperty(String y) {
        if (retvals != null) {
            Attribute a = retvals.get(y);
            if (a != null)
                return a;
        }
        AttributeDict d = getPredefinedScope(LabelType.RULE_LABEL);
        return d.get(y);
    }

    public Set<String> getTokenRefs() {
        Set<String> refs = new HashSet<String>();
        for (int i = 1; i <= numberOfAlts; i++) {
            refs.addAll(alt[i].tokenRefs.keySet());
        }
        return refs;
    }

    public Set<String> getElementLabelNames() {
        Set<String> refs = new HashSet<String>();
        for (int i = 1; i <= numberOfAlts; i++) {
            refs.addAll(alt[i].labelDefs.keySet());
        }
        if (refs.isEmpty())
            return null;
        return refs;
    }

    public MultiMap<String, LabelElementPair> getElementLabelDefs() {
        MultiMap<String, LabelElementPair> defs = new MultiMap<String, LabelElementPair>();
        for (int i = 1; i <= numberOfAlts; i++) {
            for (List<LabelElementPair> pairs : alt[i].labelDefs.values()) {
                for (LabelElementPair p : pairs) {
                    defs.map(p.label.getText(), p);
                }
            }
        }
        return defs;
    }

    public boolean hasAltSpecificContexts() {
        return getAltLabels() != null;
    }

    public int getOriginalNumberOfAlts() {
        return numberOfAlts;
    }

    public Map<String, List<Pair<Integer, AltAST>>> getAltLabels() {
        Map<String, List<Pair<Integer, AltAST>>> labels = new LinkedHashMap<String, List<Pair<Integer, AltAST>>>();
        for (int i = 1; i <= numberOfAlts; i++) {
            GrammarAST altLabel = alt[i].ast.altLabel;
            if (altLabel != null) {
                List<Pair<Integer, AltAST>> list = labels.get(altLabel.getText());
                if (list == null) {
                    list = new ArrayList<Pair<Integer, AltAST>>();
                    labels.put(altLabel.getText(), list);
                }
                list.add(new Pair<Integer, AltAST>(i, alt[i].ast));
            }
        }
        if (labels.isEmpty())
            return null;
        return labels;
    }

    public List<AltAST> getUnlabeledAltASTs() {
        List<AltAST> alts = new ArrayList<AltAST>();
        for (int i = 1; i <= numberOfAlts; i++) {
            GrammarAST altLabel = alt[i].ast.altLabel;
            if (altLabel == null)
                alts.add(alt[i].ast);
        }
        if (alts.isEmpty())
            return null;
        return alts;
    }

    @Override
    public Attribute resolveToAttribute(String x, ActionAST node) {
        if (args != null) {
            Attribute a = args.get(x);
            if (a != null)
                return a;
        }
        if (retvals != null) {
            Attribute a = retvals.get(x);
            if (a != null)
                return a;
        }
        if (locals != null) {
            Attribute a = locals.get(x);
            if (a != null)
                return a;
        }
        AttributeDict properties = getPredefinedScope(LabelType.RULE_LABEL);
        return properties.get(x);
    }

    @Override
    public Attribute resolveToAttribute(String x, String y, ActionAST node) {
        LabelElementPair anyLabelDef = getAnyLabelDef(x);
        if (anyLabelDef != null) {
            if (anyLabelDef.type == LabelType.RULE_LABEL) {
                return g.getRule(anyLabelDef.element.getText()).resolveRetvalOrProperty(y);
            } else {
                AttributeDict scope = getPredefinedScope(anyLabelDef.type);
                if (scope == null) {
                    return null;
                }
                return scope.get(y);
            }
        }
        return null;
    }

    @Override
    public boolean resolvesToLabel(String x, ActionAST node) {
        LabelElementPair anyLabelDef = getAnyLabelDef(x);
        return anyLabelDef != null && (anyLabelDef.type == LabelType.RULE_LABEL || anyLabelDef.type == LabelType.TOKEN_LABEL);
    }

    @Override
    public boolean resolvesToListLabel(String x, ActionAST node) {
        LabelElementPair anyLabelDef = getAnyLabelDef(x);
        return anyLabelDef != null && (anyLabelDef.type == LabelType.RULE_LIST_LABEL || anyLabelDef.type == LabelType.TOKEN_LIST_LABEL);
    }

    @Override
    public boolean resolvesToToken(String x, ActionAST node) {
        LabelElementPair anyLabelDef = getAnyLabelDef(x);
        if (anyLabelDef != null && anyLabelDef.type == LabelType.TOKEN_LABEL)
            return true;
        return false;
    }

    @Override
    public boolean resolvesToAttributeDict(String x, ActionAST node) {
        if (resolvesToToken(x, node))
            return true;
        return false;
    }

    public Rule resolveToRule(String x) {
        if (x.equals(this.name))
            return this;
        LabelElementPair anyLabelDef = getAnyLabelDef(x);
        if (anyLabelDef != null && anyLabelDef.type == LabelType.RULE_LABEL) {
            return g.getRule(anyLabelDef.element.getText());
        }
        return g.getRule(x);
    }

    public LabelElementPair getAnyLabelDef(String x) {
        List<LabelElementPair> labels = getElementLabelDefs().get(x);
        if (labels != null)
            return labels.get(0);
        return null;
    }

    public AttributeDict getPredefinedScope(LabelType ltype) {
        String grammarLabelKey = g.getTypeString() + ":" + ltype;
        return Grammar.grammarAndLabelRefTypeToScope.get(grammarLabelKey);
    }

    public boolean isFragment() {
        if (modifiers == null)
            return false;
        for (GrammarAST a : modifiers) {
            if (a.getText().equals("fragment"))
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Rule)) {
            return false;
        }
        return name.equals(((Rule) obj).name);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("Rule{name=").append(name);
        if (args != null)
            buf.append(", args=").append(args);
        if (retvals != null)
            buf.append(", retvals=").append(retvals);
        buf.append("}");
        return buf.toString();
    }
}
