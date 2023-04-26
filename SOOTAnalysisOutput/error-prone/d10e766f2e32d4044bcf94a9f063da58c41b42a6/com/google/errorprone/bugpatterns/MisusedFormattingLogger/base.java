package com.google.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.Category.JDK;
import static com.google.errorprone.BugPattern.MaturityLevel.MATURE;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.isArrayType;
import static com.google.errorprone.matchers.Matchers.isDescendantOfMethod;
import static com.google.errorprone.matchers.Matchers.isPrimitiveArrayType;
import static com.google.errorprone.matchers.Matchers.isSubtypeOf;
import static com.google.errorprone.matchers.Matchers.methodSelect;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.TreeMaker;
import edu.umd.cs.findbugs.formatStringChecker.ExtraFormatArgumentsException;
import edu.umd.cs.findbugs.formatStringChecker.Formatter;
import edu.umd.cs.findbugs.formatStringChecker.FormatterException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.FormatFlagsConversionMismatchException;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.Set;
import java.util.regex.Pattern;
import javax.lang.model.type.TypeKind;

@BugPattern(name = "MisusedFormattingLogger", summary = "FormattingLogger uses wrong or mismatched format string", explanation = "FormattingLogger is easily misused. There are several similar but " + "incompatible methods.  Methods ending in \"fmt\" use String.format, but the " + "corresponding methods without that suffix use MessageFormat. Some methods have an " + "optional exception first, and some have it last. Failing to pick " + "the right method will cause logging information to be lost or the log call to " + "fail at runtime -- often during an error condition when you need it most.\n\n" + "There are further gotchas.  For example, MessageFormat strings cannot " + "have unbalanced single quotes (e.g., \"Don't log {0}\" will not format {0} because " + "of the quote in \"Don't\"). The number of format elements must match the number of " + "arguments provided, and for String.format, the types must match as well.  And so on.", category = JDK, maturity = MATURE, severity = ERROR)
public class MisusedFormattingLogger extends BugChecker implements MethodInvocationTreeMatcher {

    @SuppressWarnings("unchecked")
    private static final Matcher<MethodInvocationTree> isFormattingLogger = anyOf(methodSelect(Matchers.methodReceiver(Matchers.isSubtypeOf("com.google.common.logging.FormattingLogger"))), methodSelect(Matchers.methodReceiver(Matchers.isSubtypeOf("com.google.gdata.util.common.logging.FormattingLogger"))));

    private static final Matcher<Tree> isThrowable = isSubtypeOf("java.lang.Throwable");

    @SuppressWarnings("unchecked")
    private static final Matcher<MethodInvocationTree> isThrowableMessage = methodSelect(Matchers.<ExpressionTree>anyOf(isDescendantOfMethod("java.lang.Throwable", "getMessage()"), isDescendantOfMethod("java.lang.Throwable", "toString()")));

    private static final Pattern formattingLoggerMethods = Pattern.compile("^(severe|warning|info|config|fine|finer|finest|log)(fmt)?\\(" + "(java\\.util\\.logging\\.Level,)?" + "(java\\.lang\\.Throwable,)?" + "java\\.lang\\.String,java\\.lang\\.Object\\.\\.\\.\\)");

    private static final int BASE_GROUP = 1, FORMAT_GROUP = 2, LEVELPARAM_GROUP = 3, THROWABLEPARAM_GROUP = 4;

    private static final Pattern messageFormatGroup = Pattern.compile("\\{ *(\\d+).*?\\}");

    private static final Pattern literalRegion = Pattern.compile("'[^']*'?");

    private static final Pattern printfGroup = Pattern.compile("%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])");

    private static final Map<TypeKind, String> BOXED_TYPE_NAMES;

    static {
        Map<TypeKind, String> boxedTypeNames = new EnumMap<TypeKind, String>(TypeKind.class);
        boxedTypeNames.put(TypeKind.BYTE, Byte.class.getName());
        boxedTypeNames.put(TypeKind.SHORT, Short.class.getName());
        boxedTypeNames.put(TypeKind.INT, Integer.class.getName());
        boxedTypeNames.put(TypeKind.LONG, Long.class.getName());
        boxedTypeNames.put(TypeKind.FLOAT, Float.class.getName());
        boxedTypeNames.put(TypeKind.DOUBLE, Double.class.getName());
        boxedTypeNames.put(TypeKind.BOOLEAN, Boolean.class.getName());
        boxedTypeNames.put(TypeKind.CHAR, Character.class.getName());
        boxedTypeNames.put(TypeKind.NULL, Object.class.getName());
        BOXED_TYPE_NAMES = Collections.unmodifiableMap(boxedTypeNames);
    }

    private static String getFormatterType(Type type) {
        String boxedTypeName = BOXED_TYPE_NAMES.get(type.getKind());
        String typeName = (boxedTypeName != null ? boxedTypeName : type.toString());
        return ("L" + typeName.replace(".", "/") + ";");
    }

    @Override
    public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
        FormatParameters parameters = findFormatParameters(tree, state);
        if (parameters == null) {
            return Description.NO_MATCH;
        }
        if (!isVariadicInvocation(tree, state, parameters)) {
            return Description.NO_MATCH;
        }
        List<? extends ExpressionTree> args = tree.getArguments();
        if (args.get(parameters.getFormatIndex()).getKind() != Kind.STRING_LITERAL) {
            return Description.NO_MATCH;
        }
        try {
            return checkFormatString(tree, state, parameters);
        } catch (UnsupportedOperationException e) {
            return Description.NO_MATCH;
        }
    }

    private Description checkFormatString(MethodInvocationTree tree, VisitorState state, FormatParameters parameters) {
        List<String> errors = new ArrayList<String>();
        int formatIndex = parameters.getFormatIndex();
        List<? extends ExpressionTree> args = tree.getArguments();
        JCLiteral format = (JCLiteral) args.get(formatIndex);
        String formatString = (String) format.getValue();
        List<ExpressionTree> leadingArguments = new ArrayList<ExpressionTree>();
        List<ExpressionTree> formatArguments = new ArrayList<ExpressionTree>();
        for (int i = 0; i < args.size(); i++) {
            if (i < formatIndex) {
                leadingArguments.add(args.get(i));
            } else if (i > formatIndex) {
                formatArguments.add(args.get(i));
            }
        }
        boolean rewriteMethod = changeFormatTypeIfRequired(parameters, formatString);
        if (rewriteMethod) {
            errors.add("uses the wrong method for the format type");
        }
        Exception formatException = null;
        try {
            if (parameters.getType() == FormatType.MESSAGEFORMAT) {
                new MessageFormat(formatString);
            } else if (parameters.getType() == FormatType.PRINTF) {
                verifyPrintf(tree, parameters);
            }
        } catch (FormatterException e) {
            formatException = e;
        } catch (MissingFormatArgumentException e) {
            formatException = e;
        }
        if (formatException != null) {
            String customMessage = "Format string is invalid: " + formatException.getMessage();
            return new Description.Builder(tree, pattern).setMessage(customMessage).build();
        }
        Set<Integer> referencedArguments = getReferencedArguments(parameters.getType(), formatString);
        if (referencesUnspecifiedArguments(referencedArguments, formatArguments.size())) {
            return describeMatch(tree, SuggestedFix.NO_FIX);
        }
        if (parameters.getType() == FormatType.MESSAGEFORMAT && referencedArguments.size() < formatArguments.size()) {
            String quotedString = formatString.replace("'", "''");
            if (hasQuotedArguments(formatString)) {
                Set<Integer> updatedReferences = getReferencedArguments(parameters.getType(), quotedString);
                if (updatedReferences.size() > referencedArguments.size()) {
                    formatString = quotedString;
                    errors.add("has arguments masked by single quotes");
                    referencedArguments = updatedReferences;
                }
            }
            if (hasUnmatchedQuotes(formatString)) {
                formatString = quotedString;
                errors.add("has unmatched single quotes");
            }
        }
        if (parameters.getAllowExceptionReordering() && referencedArguments.size() < formatArguments.size() && max(referencedArguments) < formatArguments.size() - 1) {
            ExpressionTree last = formatArguments.get(formatArguments.size() - 1);
            if (isThrowable.matches(last, state)) {
                leadingArguments.add(last);
                formatArguments.remove(formatArguments.size() - 1);
                errors.add("ignores the passed exception");
            } else if (last instanceof MethodInvocationTree && isThrowableMessage.matches((MethodInvocationTree) last, state)) {
                ExpressionTree target = getInvocationTarget((MethodInvocationTree) last);
                if (target != null) {
                    leadingArguments.add(target);
                    formatArguments.remove(formatArguments.size() - 1);
                    errors.add("ignores the passed exception message");
                }
            }
        }
        if (referencedArguments.size() < formatArguments.size()) {
            List<String> additionalArgs = new ArrayList<String>();
            for (int i = 0; i < formatArguments.size(); i++) {
                String arg = parameters.getType() == FormatType.MESSAGEFORMAT ? "{" + i + "}" : "%s";
                if (!referencedArguments.contains(i)) {
                    additionalArgs.add(arg);
                }
            }
            formatString = formatString + " (" + join(", ", additionalArgs) + ")";
            errors.add("ignores some parameters");
        }
        if (errors.size() > 0) {
            List<String> newParameters = new ArrayList<String>();
            for (ExpressionTree t : leadingArguments) {
                newParameters.add(t.toString());
            }
            newParameters.add(makeLiteral(state, formatString));
            for (ExpressionTree t : formatArguments) {
                newParameters.add(t.toString());
            }
            int methodStart = state.getEndPosition((JCTree) getInvocationTarget(tree));
            int parameterEnd = state.getEndPosition((JCTree) args.get(args.size() - 1));
            Fix fix;
            if (methodStart >= 0 && parameterEnd >= 0) {
                String replacement = "." + parameters.getMethodName() + "(" + join(", ", newParameters);
                fix = SuggestedFix.replace(methodStart, parameterEnd, replacement);
            } else {
                fix = SuggestedFix.NO_FIX;
            }
            return new Description.Builder(tree, pattern).setMessage("This call " + join(", ", errors)).setFix(fix).build();
        }
        return Description.NO_MATCH;
    }

    private boolean isVariadicInvocation(MethodInvocationTree tree, VisitorState state, FormatParameters params) {
        List<? extends ExpressionTree> arguments = tree.getArguments();
        if (arguments.size() == params.getFormatIndex() + 2) {
            ExpressionTree lastArgument = arguments.get(arguments.size() - 1);
            return !isArrayType().matches(lastArgument, state) || isPrimitiveArrayType().matches(lastArgument, state);
        }
        return true;
    }

    private ExpressionTree getInvocationTarget(MethodInvocationTree invocation) {
        if (invocation.getMethodSelect() instanceof MemberSelectTree) {
            return ((MemberSelectTree) invocation.getMethodSelect()).getExpression();
        }
        return null;
    }

    private void verifyPrintf(MethodInvocationTree tree, FormatParameters parameters) throws FormatFlagsConversionMismatchException, IllegalFormatException, FormatterException {
        List<? extends ExpressionTree> args = tree.getArguments();
        JCLiteral format = (JCLiteral) args.get(parameters.getFormatIndex());
        String formatString = (String) format.getValue();
        List<String> argTypes = new ArrayList<String>();
        for (int i = parameters.getFormatIndex() + 1; i < args.size(); ++i) {
            Type type = ((JCExpression) args.get(i)).type;
            argTypes.add(getFormatterType(type));
        }
        try {
            Formatter.check(formatString, argTypes.toArray(new String[0]));
        } catch (ExtraFormatArgumentsException e) {
            return;
        }
    }

    private boolean referencesUnspecifiedArguments(Set<Integer> usedReferences, int argumentCount) {
        for (int i : usedReferences) {
            if (i > argumentCount) {
                return true;
            }
        }
        return false;
    }

    private static String join(String separator, Iterable<String> strings) {
        StringBuilder builder = new StringBuilder();
        for (String s : strings) {
            builder.append(s).append(separator);
        }
        return builder.length() == 0 ? "" : builder.substring(0, builder.length() - separator.length());
    }

    private Set<Integer> getReferencedArguments(FormatType type, String string) {
        if (type == FormatType.PRINTF) {
            return getReferencedArgumentsP(string);
        } else if (type == FormatType.MESSAGEFORMAT) {
            return getReferencedArgumentsM(string);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private Set<Integer> getReferencedArgumentsP(String str) {
        java.util.regex.Matcher matcher = printfGroup.matcher(str);
        Set<Integer> set = new HashSet<Integer>();
        int i = 0;
        while (matcher.find()) {
            if (!matcher.group().endsWith("n") && !matcher.group().endsWith("%")) {
                if (matcher.group(1) != null) {
                    set.add(Integer.parseInt(matcher.group(1).replaceAll("\\$", "")));
                } else {
                    set.add(i);
                    i++;
                }
            }
        }
        return set;
    }

    private Set<Integer> getReferencedArgumentsM(String str) {
        str = str.replaceAll("''", "");
        str = literalRegion.matcher(str).replaceAll("");
        java.util.regex.Matcher matcher = messageFormatGroup.matcher(str);
        Set<Integer> references = new HashSet<Integer>();
        while (matcher.find()) {
            references.add(Integer.parseInt(matcher.group(1)));
        }
        return references;
    }

    private int max(Set<Integer> set) {
        int max = -1;
        for (int i : set) {
            max = Math.max(i, max);
        }
        return max;
    }

    private boolean hasQuotedArguments(String string) {
        string = string.replaceAll("''", "");
        java.util.regex.Matcher match = literalRegion.matcher(string);
        while (match.find()) {
            if (messageFormatGroup.matcher(match.group()).find()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasUnmatchedQuotes(String string) {
        return string.replaceAll("[^']", "").length() % 2 == 1;
    }

    private FormatParameters findFormatParameters(MethodInvocationTree tree, VisitorState state) {
        FormatParameters parameters = new FormatParameters();
        if (isFormattingLogger.matches(tree, state)) {
            String methodName = ASTHelpers.getSymbol(tree).toString();
            java.util.regex.Matcher matcher = formattingLoggerMethods.matcher(methodName);
            if (matcher.matches()) {
                int formatIndex = 0;
                if (matcher.group(LEVELPARAM_GROUP) != null) {
                    formatIndex++;
                }
                if (matcher.group(THROWABLEPARAM_GROUP) != null) {
                    formatIndex++;
                } else {
                    parameters.setAllowExceptionReordering(true);
                }
                parameters.setFormatIndex(formatIndex);
                if (matcher.group(FORMAT_GROUP) == null) {
                    parameters.setType(FormatType.MESSAGEFORMAT);
                } else {
                    parameters.setType(FormatType.PRINTF);
                }
                parameters.setMethodBase(matcher.group(BASE_GROUP));
                return parameters;
            }
        }
        return null;
    }

    private String makeLiteral(VisitorState state, String value) {
        return TreeMaker.instance(state.context).Literal(value).toString().replaceAll("\\\\'", "'");
    }

    private boolean changeFormatTypeIfRequired(FormatParameters parameters, String formatString) {
        if (parameters.getType() == FormatType.MESSAGEFORMAT && !mayBeMessageFormat(formatString) && mayBePrintfFormat(formatString)) {
            parameters.setType(FormatType.PRINTF);
            return true;
        }
        if (parameters.getType() == FormatType.PRINTF && !mayBePrintfFormat(formatString) && mayBeMessageFormat(formatString)) {
            parameters.setType(FormatType.MESSAGEFORMAT);
            return true;
        }
        return false;
    }

    private boolean mayBePrintfFormat(String formatString) {
        return printfGroup.matcher(formatString).find();
    }

    private boolean mayBeMessageFormat(String formatString) {
        return messageFormatGroup.matcher(formatString).find();
    }

    private static class FormatParameters {

        private FormatType type;

        private int formatIndex;

        private String methodBase;

        private boolean allowExceptionReordering;

        int getFormatIndex() {
            return formatIndex;
        }

        FormatParameters setFormatIndex(int formatIndex) {
            this.formatIndex = formatIndex;
            return this;
        }

        boolean getAllowExceptionReordering() {
            return allowExceptionReordering;
        }

        FormatParameters setAllowExceptionReordering(boolean allow) {
            allowExceptionReordering = allow;
            return this;
        }

        FormatType getType() {
            return type;
        }

        FormatParameters setType(FormatType type) {
            this.type = type;
            return this;
        }

        String getMethodBase() {
            return methodBase;
        }

        FormatParameters setMethodBase(String methodBase) {
            this.methodBase = methodBase;
            return this;
        }

        String getMethodName() {
            return getMethodBase() + (type == FormatType.PRINTF ? "fmt" : "");
        }
    }

    static enum FormatType {

        PRINTF, MESSAGEFORMAT
    }
}
