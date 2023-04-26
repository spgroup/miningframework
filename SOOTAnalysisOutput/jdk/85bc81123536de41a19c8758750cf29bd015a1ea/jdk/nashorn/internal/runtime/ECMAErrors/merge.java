package jdk.nashorn.internal.runtime;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import jdk.nashorn.api.scripting.NashornException;
import jdk.nashorn.internal.codegen.CompilerConstants;
import jdk.nashorn.internal.objects.Global;
import jdk.nashorn.internal.scripts.JS;

public final class ECMAErrors {

    private static final String MESSAGES_RESOURCE = "jdk.nashorn.internal.runtime.resources.Messages";

    private static final ResourceBundle MESSAGES_BUNDLE;

    static {
        MESSAGES_BUNDLE = ResourceBundle.getBundle(MESSAGES_RESOURCE, Locale.getDefault());
    }

    private static final String scriptPackage;

    static {
        String name = JS.class.getName();
        scriptPackage = name.substring(0, name.lastIndexOf('.'));
    }

    private ECMAErrors() {
    }

    private static ECMAException error(final Object thrown, final Throwable cause) {
        return new ECMAException(thrown, cause);
    }

    public static ECMAException asEcmaException(final ParserException e) {
        return asEcmaException(Context.getGlobal(), e);
    }

    public static ECMAException asEcmaException(final Global global, final ParserException e) {
        final JSErrorType errorType = e.getErrorType();
        assert errorType != null : "error type for " + e + " was null";
        final Global globalObj = global;
        final String msg = e.getMessage();
        switch(errorType) {
            case ERROR:
                return error(globalObj.newError(msg), e);
            case EVAL_ERROR:
                return error(globalObj.newEvalError(msg), e);
            case RANGE_ERROR:
                return error(globalObj.newRangeError(msg), e);
            case REFERENCE_ERROR:
                return error(globalObj.newReferenceError(msg), e);
            case SYNTAX_ERROR:
                return error(globalObj.newSyntaxError(msg), e);
            case TYPE_ERROR:
                return error(globalObj.newTypeError(msg), e);
            case URI_ERROR:
                return error(globalObj.newURIError(msg), e);
            default:
                throw new AssertionError(e.getMessage());
        }
    }

    public static ECMAException syntaxError(final String msgId, final String... args) {
        return syntaxError(Context.getGlobal(), msgId, args);
    }

    public static ECMAException syntaxError(final Global global, final String msgId, final String... args) {
        return syntaxError(global, null, msgId, args);
    }

    public static ECMAException syntaxError(final Throwable cause, final String msgId, final String... args) {
        return syntaxError(Context.getGlobal(), cause, msgId, args);
    }

    public static ECMAException syntaxError(final Global global, final Throwable cause, final String msgId, final String... args) {
        final String msg = getMessage("syntax.error." + msgId, args);
        return error(global.newSyntaxError(msg), cause);
    }

    public static ECMAException typeError(final String msgId, final String... args) {
        return typeError(Context.getGlobal(), msgId, args);
    }

    public static ECMAException typeError(final Global global, final String msgId, final String... args) {
        return typeError(global, null, msgId, args);
    }

    public static ECMAException typeError(final Throwable cause, final String msgId, final String... args) {
        return typeError(Context.getGlobal(), cause, msgId, args);
    }

    public static ECMAException typeError(final Global global, final Throwable cause, final String msgId, final String... args) {
        final String msg = getMessage("type.error." + msgId, args);
        return error(global.newTypeError(msg), cause);
    }

    public static ECMAException rangeError(final String msgId, final String... args) {
        return rangeError(Context.getGlobal(), msgId, args);
    }

    public static ECMAException rangeError(final Global global, final String msgId, final String... args) {
        return rangeError(global, null, msgId, args);
    }

    public static ECMAException rangeError(final Throwable cause, final String msgId, final String... args) {
        return rangeError(Context.getGlobal(), cause, msgId, args);
    }

    public static ECMAException rangeError(final Global global, final Throwable cause, final String msgId, final String... args) {
        final String msg = getMessage("range.error." + msgId, args);
        return error(global.newRangeError(msg), cause);
    }

    public static ECMAException referenceError(final String msgId, final String... args) {
        return referenceError(Context.getGlobal(), msgId, args);
    }

    public static ECMAException referenceError(final Global global, final String msgId, final String... args) {
        return referenceError(global, null, msgId, args);
    }

    public static ECMAException referenceError(final Throwable cause, final String msgId, final String... args) {
        return referenceError(Context.getGlobal(), cause, msgId, args);
    }

    public static ECMAException referenceError(final Global global, final Throwable cause, final String msgId, final String... args) {
        final String msg = getMessage("reference.error." + msgId, args);
        return error(global.newReferenceError(msg), cause);
    }

    public static ECMAException uriError(final String msgId, final String... args) {
        return uriError(Context.getGlobal(), msgId, args);
    }

    public static ECMAException uriError(final Global global, final String msgId, final String... args) {
        return uriError(global, null, msgId, args);
    }

    public static ECMAException uriError(final Throwable cause, final String msgId, final String... args) {
        return uriError(Context.getGlobal(), cause, msgId, args);
    }

    public static ECMAException uriError(final Global global, final Throwable cause, final String msgId, final String... args) {
        final String msg = getMessage("uri.error." + msgId, args);
        return error(global.newURIError(msg), cause);
    }

    public static String getMessage(final String msgId, final String... args) {
        try {
            return new MessageFormat(MESSAGES_BUNDLE.getString(msgId)).format(args);
        } catch (final java.util.MissingResourceException e) {
            throw new RuntimeException("no message resource found for message id: " + msgId);
        }
    }

    public static boolean isScriptFrame(final StackTraceElement frame) {
        final String className = frame.getClassName();
        if (className.startsWith(scriptPackage) && !CompilerConstants.isInternalMethodName(frame.getMethodName())) {
            final String source = frame.getFileName();
            return source != null && !source.endsWith(".java") && !source.contains(NashornException.ENGINE_SCRIPT_SOURCE_NAME);
        }
        return false;
    }
}
