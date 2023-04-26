package org.hibernate.hql.classic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.engine.JoinSequence;
import org.hibernate.hql.QueryTranslator;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.persister.collection.CollectionPropertyNames;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.sql.InFragment;
import org.hibernate.type.EntityType;
import org.hibernate.type.LiteralType;
import org.hibernate.type.Type;
import org.hibernate.internal.util.ReflectHelper;

public class WhereParser implements Parser {

    private final PathExpressionParser pathExpressionParser;

    {
        pathExpressionParser = new PathExpressionParser();
        pathExpressionParser.setUseThetaStyleJoin(true);
    }

    private static final Set EXPRESSION_TERMINATORS = new HashSet();

    private static final Set EXPRESSION_OPENERS = new HashSet();

    private static final Set BOOLEAN_OPERATORS = new HashSet();

    private static final Map NEGATIONS = new HashMap();

    static {
        EXPRESSION_TERMINATORS.add("and");
        EXPRESSION_TERMINATORS.add("or");
        EXPRESSION_TERMINATORS.add(")");
        EXPRESSION_OPENERS.add("and");
        EXPRESSION_OPENERS.add("or");
        EXPRESSION_OPENERS.add("(");
        BOOLEAN_OPERATORS.add("<");
        BOOLEAN_OPERATORS.add("=");
        BOOLEAN_OPERATORS.add(">");
        BOOLEAN_OPERATORS.add("#");
        BOOLEAN_OPERATORS.add("~");
        BOOLEAN_OPERATORS.add("like");
        BOOLEAN_OPERATORS.add("ilike");
        BOOLEAN_OPERATORS.add("regexp");
        BOOLEAN_OPERATORS.add("rlike");
        BOOLEAN_OPERATORS.add("is");
        BOOLEAN_OPERATORS.add("in");
        BOOLEAN_OPERATORS.add("any");
        BOOLEAN_OPERATORS.add("some");
        BOOLEAN_OPERATORS.add("all");
        BOOLEAN_OPERATORS.add("exists");
        BOOLEAN_OPERATORS.add("between");
        BOOLEAN_OPERATORS.add("<=");
        BOOLEAN_OPERATORS.add(">=");
        BOOLEAN_OPERATORS.add("=>");
        BOOLEAN_OPERATORS.add("=<");
        BOOLEAN_OPERATORS.add("!=");
        BOOLEAN_OPERATORS.add("<>");
        BOOLEAN_OPERATORS.add("!#");
        BOOLEAN_OPERATORS.add("!~");
        BOOLEAN_OPERATORS.add("!<");
        BOOLEAN_OPERATORS.add("!>");
        BOOLEAN_OPERATORS.add("is not");
        BOOLEAN_OPERATORS.add("not like");
        BOOLEAN_OPERATORS.add("not ilike");
        BOOLEAN_OPERATORS.add("not regexp");
        BOOLEAN_OPERATORS.add("not rlike");
        BOOLEAN_OPERATORS.add("not in");
        BOOLEAN_OPERATORS.add("not between");
        BOOLEAN_OPERATORS.add("not exists");
        NEGATIONS.put("and", "or");
        NEGATIONS.put("or", "and");
        NEGATIONS.put("<", ">=");
        NEGATIONS.put("=", "<>");
        NEGATIONS.put(">", "<=");
        NEGATIONS.put("#", "!#");
        NEGATIONS.put("~", "!~");
        NEGATIONS.put("like", "not like");
        NEGATIONS.put("ilike", "not ilike");
        NEGATIONS.put("regexp", "not regexp");
        NEGATIONS.put("rlike", "not rlike");
        NEGATIONS.put("is", "is not");
        NEGATIONS.put("in", "not in");
        NEGATIONS.put("exists", "not exists");
        NEGATIONS.put("between", "not between");
        NEGATIONS.put("<=", ">");
        NEGATIONS.put(">=", "<");
        NEGATIONS.put("=>", "<");
        NEGATIONS.put("=<", ">");
        NEGATIONS.put("!=", "=");
        NEGATIONS.put("<>", "=");
        NEGATIONS.put("!#", "#");
        NEGATIONS.put("!~", "~");
        NEGATIONS.put("!<", "<");
        NEGATIONS.put("!>", ">");
        NEGATIONS.put("is not", "is");
        NEGATIONS.put("not like", "like");
        NEGATIONS.put("not ilike", "ilike");
        NEGATIONS.put("not regexp", "regexp");
        NEGATIONS.put("not rlike", "rlike");
        NEGATIONS.put("not in", "in");
        NEGATIONS.put("not between", "between");
        NEGATIONS.put("not exists", "exists");
    }

    private boolean betweenSpecialCase = false;

    private boolean negated = false;

    private boolean inSubselect = false;

    private int bracketsSinceSelect = 0;

    private StringBuffer subselect;

    private boolean expectingPathContinuation = false;

    private int expectingIndex = 0;

    private LinkedList nots = new LinkedList();

    private LinkedList joins = new LinkedList();

    private LinkedList booleanTests = new LinkedList();

    private String getElementName(PathExpressionParser.CollectionElement element, QueryTranslatorImpl q) throws QueryException {
        String name;
        if (element.isOneToMany) {
            name = element.alias;
        } else {
            Type type = element.elementType;
            if (type.isEntityType()) {
                String entityName = ((EntityType) type).getAssociatedEntityName();
                name = pathExpressionParser.continueFromManyToMany(entityName, element.elementColumns, q);
            } else {
                throw new QueryException("illegally dereferenced collection element");
            }
        }
        return name;
    }

    public void token(String token, QueryTranslatorImpl q) throws QueryException {
        String lcToken = token.toLowerCase();
        if (token.equals("[") && !expectingPathContinuation) {
            expectingPathContinuation = false;
            if (expectingIndex == 0)
                throw new QueryException("unexpected [");
            return;
        } else if (token.equals("]")) {
            expectingIndex--;
            expectingPathContinuation = true;
            return;
        }
        if (expectingPathContinuation) {
            boolean pathExpressionContinuesFurther = continuePathExpression(token, q);
            if (pathExpressionContinuesFurther)
                return;
        }
        if (!inSubselect && (lcToken.equals("select") || lcToken.equals("from"))) {
            inSubselect = true;
            subselect = new StringBuffer(20);
        }
        if (inSubselect && token.equals(")")) {
            bracketsSinceSelect--;
            if (bracketsSinceSelect == -1) {
                QueryTranslatorImpl subq = new QueryTranslatorImpl(subselect.toString(), q.getEnabledFilters(), q.getFactory());
                try {
                    subq.compile(q);
                } catch (MappingException me) {
                    throw new QueryException("MappingException occurred compiling subquery", me);
                }
                appendToken(q, subq.getSQLString());
                inSubselect = false;
                bracketsSinceSelect = 0;
            }
        }
        if (inSubselect) {
            if (token.equals("("))
                bracketsSinceSelect++;
            subselect.append(token).append(' ');
            return;
        }
        specialCasesBefore(lcToken);
        if (!betweenSpecialCase && EXPRESSION_TERMINATORS.contains(lcToken)) {
            closeExpression(q, lcToken);
        }
        if (BOOLEAN_OPERATORS.contains(lcToken)) {
            booleanTests.removeLast();
            booleanTests.addLast(Boolean.TRUE);
        }
        if (lcToken.equals("not")) {
            nots.addLast(new Boolean(!((Boolean) nots.removeLast()).booleanValue()));
            negated = !negated;
            return;
        }
        doToken(token, q);
        if (!betweenSpecialCase && EXPRESSION_OPENERS.contains(lcToken)) {
            openExpression(q, lcToken);
        }
        specialCasesAfter(lcToken);
    }

    public void start(QueryTranslatorImpl q) throws QueryException {
        token("(", q);
    }

    public void end(QueryTranslatorImpl q) throws QueryException {
        if (expectingPathContinuation) {
            expectingPathContinuation = false;
            PathExpressionParser.CollectionElement element = pathExpressionParser.lastCollectionElement();
            if (element.elementColumns.length != 1)
                throw new QueryException("path expression ended in composite collection element");
            appendToken(q, element.elementColumns[0]);
            addToCurrentJoin(element);
        }
        token(")", q);
    }

    private void closeExpression(QueryTranslatorImpl q, String lcToken) {
        if (((Boolean) booleanTests.removeLast()).booleanValue()) {
            if (booleanTests.size() > 0) {
                booleanTests.removeLast();
                booleanTests.addLast(Boolean.TRUE);
            }
            appendToken(q, (joins.removeLast()).toString());
        } else {
            StringBuffer join = (StringBuffer) joins.removeLast();
            ((StringBuffer) joins.getLast()).append(join.toString());
        }
        if (((Boolean) nots.removeLast()).booleanValue())
            negated = !negated;
        if (!")".equals(lcToken))
            appendToken(q, ")");
    }

    private void openExpression(QueryTranslatorImpl q, String lcToken) {
        nots.addLast(Boolean.FALSE);
        booleanTests.addLast(Boolean.FALSE);
        joins.addLast(new StringBuffer());
        if (!"(".equals(lcToken))
            appendToken(q, "(");
    }

    private void preprocess(String token, QueryTranslatorImpl q) throws QueryException {
        String[] tokens = StringHelper.split(".", token, true);
        if (tokens.length > 5 && (CollectionPropertyNames.COLLECTION_ELEMENTS.equals(tokens[tokens.length - 1]) || CollectionPropertyNames.COLLECTION_INDICES.equals(tokens[tokens.length - 1]))) {
            pathExpressionParser.start(q);
            for (int i = 0; i < tokens.length - 3; i++) {
                pathExpressionParser.token(tokens[i], q);
            }
            pathExpressionParser.token(null, q);
            pathExpressionParser.end(q);
            addJoin(pathExpressionParser.getWhereJoin(), q);
            pathExpressionParser.ignoreInitialJoin();
        }
    }

    private void doPathExpression(String token, QueryTranslatorImpl q) throws QueryException {
        preprocess(token, q);
        StringTokenizer tokens = new StringTokenizer(token, ".", true);
        pathExpressionParser.start(q);
        while (tokens.hasMoreTokens()) {
            pathExpressionParser.token(tokens.nextToken(), q);
        }
        pathExpressionParser.end(q);
        if (pathExpressionParser.isCollectionValued()) {
            openExpression(q, "");
            appendToken(q, pathExpressionParser.getCollectionSubquery(q.getEnabledFilters()));
            closeExpression(q, "");
            q.addQuerySpaces(q.getCollectionPersister(pathExpressionParser.getCollectionRole()).getCollectionSpaces());
        } else {
            if (pathExpressionParser.isExpectingCollectionIndex()) {
                expectingIndex++;
            } else {
                addJoin(pathExpressionParser.getWhereJoin(), q);
                appendToken(q, pathExpressionParser.getWhereColumn());
            }
        }
    }

    private void addJoin(JoinSequence joinSequence, QueryTranslatorImpl q) throws QueryException {
        q.addFromJoinOnly(pathExpressionParser.getName(), joinSequence);
        try {
            addToCurrentJoin(joinSequence.toJoinFragment(q.getEnabledFilters(), true).toWhereFragmentString());
        } catch (MappingException me) {
            throw new QueryException(me);
        }
    }

    private void doToken(String token, QueryTranslatorImpl q) throws QueryException {
        if (q.isName(StringHelper.root(token))) {
            doPathExpression(q.unalias(token), q);
        } else if (token.startsWith(ParserHelper.HQL_VARIABLE_PREFIX)) {
            q.addNamedParameter(token.substring(1));
            appendToken(q, "?");
        } else {
            Queryable persister = q.getEntityPersisterUsingImports(token);
            if (persister != null) {
                final String discrim = persister.getDiscriminatorSQLValue();
                if (InFragment.NULL.equals(discrim) || InFragment.NOT_NULL.equals(discrim)) {
                    throw new QueryException("subclass test not allowed for null or not null discriminator");
                } else {
                    appendToken(q, discrim);
                }
            } else {
                Object constant;
                if (token.indexOf('.') > -1 && (constant = ReflectHelper.getConstantValue(token)) != null) {
                    Type type;
                    try {
                        type = q.getFactory().getTypeResolver().heuristicType(constant.getClass().getName());
                    } catch (MappingException me) {
                        throw new QueryException(me);
                    }
                    if (type == null)
                        throw new QueryException(QueryTranslator.ERROR_CANNOT_DETERMINE_TYPE + token);
                    try {
                        appendToken(q, ((LiteralType) type).objectToSQLString(constant, q.getFactory().getDialect()));
                    } catch (Exception e) {
                        throw new QueryException(QueryTranslator.ERROR_CANNOT_FORMAT_LITERAL + token, e);
                    }
                } else {
                    String negatedToken = negated ? (String) NEGATIONS.get(token.toLowerCase()) : null;
                    if (negatedToken != null && (!betweenSpecialCase || !"or".equals(negatedToken))) {
                        appendToken(q, negatedToken);
                    } else {
                        appendToken(q, token);
                    }
                }
            }
        }
    }

    private void addToCurrentJoin(String sql) {
        ((StringBuffer) joins.getLast()).append(sql);
    }

    private void addToCurrentJoin(PathExpressionParser.CollectionElement ce) throws QueryException {
        try {
            addToCurrentJoin(ce.joinSequence.toJoinFragment().toWhereFragmentString() + ce.indexValue.toString());
        } catch (MappingException me) {
            throw new QueryException(me);
        }
    }

    private void specialCasesBefore(String lcToken) {
        if (lcToken.equals("between") || lcToken.equals("not between")) {
            betweenSpecialCase = true;
        }
    }

    private void specialCasesAfter(String lcToken) {
        if (betweenSpecialCase && lcToken.equals("and")) {
            betweenSpecialCase = false;
        }
    }

    void appendToken(QueryTranslatorImpl q, String token) {
        if (expectingIndex > 0) {
            pathExpressionParser.setLastCollectionElementIndexValue(token);
        } else {
            q.appendWhereToken(token);
        }
    }

    private boolean continuePathExpression(String token, QueryTranslatorImpl q) throws QueryException {
        expectingPathContinuation = false;
        PathExpressionParser.CollectionElement element = pathExpressionParser.lastCollectionElement();
        if (token.startsWith(".")) {
            doPathExpression(getElementName(element, q) + token, q);
            addToCurrentJoin(element);
            return true;
        } else {
            if (element.elementColumns.length != 1) {
                throw new QueryException("path expression ended in composite collection element");
            }
            appendToken(q, element.elementColumns[0]);
            addToCurrentJoin(element);
            return false;
        }
    }
}
