package org.graalvm.compiler.debug;

import static org.graalvm.compiler.debug.DelegatingDebugConfig.Feature.INTERCEPT;
import static org.graalvm.compiler.debug.DelegatingDebugConfig.Feature.LOG_METHOD;
import static java.util.FormattableFlags.LEFT_JUSTIFY;
import static java.util.FormattableFlags.UPPERCASE;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.graalvm.compiler.debug.DelegatingDebugConfig.Level;
import org.graalvm.compiler.debug.internal.CounterImpl;
import org.graalvm.compiler.debug.internal.DebugHistogramImpl;
import org.graalvm.compiler.debug.internal.DebugScope;
import org.graalvm.compiler.debug.internal.MemUseTrackerImpl;
import org.graalvm.compiler.debug.internal.TimerImpl;
import org.graalvm.compiler.debug.internal.method.MethodMetricsImpl;
import org.graalvm.compiler.serviceprovider.GraalServices;
import jdk.vm.ci.meta.ResolvedJavaMethod;

public class Debug {

    private static final Params params = new Params();

    static {
        for (DebugInitializationParticipant p : GraalServices.load(DebugInitializationParticipant.class)) {
            p.apply(params);
        }
    }

    public static class Params {

        public boolean enable;

        public boolean enableMethodFilter;

        public boolean enableUnscopedTimers;

        public boolean enableUnscopedCounters;

        public boolean enableUnscopedMethodMetrics;

        public boolean enableUnscopedMemUseTrackers;

        public boolean interceptCount;

        public boolean interceptTime;

        public boolean interceptMem;
    }

    @SuppressWarnings("all")
    private static boolean initialize() {
        boolean assertionsEnabled = false;
        assert assertionsEnabled = true;
        return assertionsEnabled || params.enable || GraalDebugConfig.Options.ForceDebugEnable.getValue();
    }

    private static final boolean ENABLED = initialize();

    public static boolean isEnabled() {
        return ENABLED;
    }

    public static boolean isDumpEnabledForMethod() {
        if (!ENABLED) {
            return false;
        }
        DebugConfig config = DebugScope.getConfig();
        if (config == null) {
            return false;
        }
        return config.isDumpEnabledForMethod();
    }

    public static final int BASIC_LOG_LEVEL = 1;

    public static final int INFO_LOG_LEVEL = 2;

    public static final int VERBOSE_LOG_LEVEL = 3;

    public static final int DETAILED_LOG_LEVEL = 4;

    public static final int VERY_DETAILED_LOG_LEVEL = 5;

    public static boolean isDumpEnabled(int dumpLevel) {
        return ENABLED && DebugScope.getInstance().isDumpEnabled(dumpLevel);
    }

    public static boolean isVerifyEnabledForMethod() {
        if (!ENABLED) {
            return false;
        }
        DebugConfig config = DebugScope.getConfig();
        if (config == null) {
            return false;
        }
        return config.isVerifyEnabledForMethod();
    }

    public static boolean isVerifyEnabled() {
        return ENABLED && DebugScope.getInstance().isVerifyEnabled();
    }

    public static boolean isCountEnabled() {
        return ENABLED && DebugScope.getInstance().isCountEnabled();
    }

    public static boolean isTimeEnabled() {
        return ENABLED && DebugScope.getInstance().isTimeEnabled();
    }

    public static boolean isMemUseTrackingEnabled() {
        return ENABLED && DebugScope.getInstance().isMemUseTrackingEnabled();
    }

    public static boolean isLogEnabledForMethod() {
        if (!ENABLED) {
            return false;
        }
        DebugConfig config = DebugScope.getConfig();
        if (config == null) {
            return false;
        }
        return config.isLogEnabledForMethod();
    }

    public static boolean isLogEnabled() {
        return isLogEnabled(BASIC_LOG_LEVEL);
    }

    public static boolean isLogEnabled(int logLevel) {
        return ENABLED && DebugScope.getInstance().isLogEnabled(logLevel);
    }

    public static boolean isMethodMeterEnabled() {
        return ENABLED && DebugScope.getInstance().isMethodMeterEnabled();
    }

    @SuppressWarnings("unused")
    public static Runnable decorateDebugRoot(Runnable runnable, String name, DebugConfig config) {
        return runnable;
    }

    @SuppressWarnings("unused")
    public static <T> Callable<T> decorateDebugRoot(Callable<T> callable, String name, DebugConfig config) {
        return callable;
    }

    @SuppressWarnings("unused")
    public static Runnable decorateScope(Runnable runnable, String name, Object... context) {
        return runnable;
    }

    @SuppressWarnings("unused")
    public static <T> Callable<T> decorateScope(Callable<T> callable, String name, Object... context) {
        return callable;
    }

    public static String currentScope() {
        if (ENABLED) {
            return DebugScope.getInstance().getQualifiedName();
        } else {
            return "";
        }
    }

    public interface Scope extends AutoCloseable {

        @Override
        void close();
    }

    public static Scope scope(Object name, Object[] contextObjects) throws Throwable {
        if (ENABLED) {
            return DebugScope.getInstance().scope(convertFormatArg(name).toString(), null, contextObjects);
        } else {
            return null;
        }
    }

    public static Scope scope(Object name) {
        if (ENABLED) {
            return DebugScope.getInstance().scope(convertFormatArg(name).toString(), null);
        } else {
            return null;
        }
    }

    public static Scope methodMetricsScope(Object name, DebugScope.ExtraInfo metaInfo, boolean newId, Object... context) {
        if (ENABLED) {
            return DebugScope.getInstance().enhanceWithExtraInfo(convertFormatArg(name).toString(), metaInfo, newId, context);
        } else {
            return null;
        }
    }

    public static Scope scope(Object name, Object context) throws Throwable {
        if (ENABLED) {
            return DebugScope.getInstance().scope(convertFormatArg(name).toString(), null, context);
        } else {
            return null;
        }
    }

    public static Scope scope(Object name, Object context1, Object context2) throws Throwable {
        if (ENABLED) {
            return DebugScope.getInstance().scope(convertFormatArg(name).toString(), null, context1, context2);
        } else {
            return null;
        }
    }

    public static Scope scope(Object name, Object context1, Object context2, Object context3) throws Throwable {
        if (ENABLED) {
            return DebugScope.getInstance().scope(convertFormatArg(name).toString(), null, context1, context2, context3);
        } else {
            return null;
        }
    }

    public static Scope sandbox(CharSequence name, DebugConfig config, Object... context) throws Throwable {
        if (ENABLED) {
            DebugConfig sandboxConfig = config == null ? silentConfig() : config;
            return DebugScope.getInstance().scope(name, sandboxConfig, context);
        } else {
            return null;
        }
    }

    public static Scope forceLog() throws Throwable {
        ArrayList<Object> context = new ArrayList<>();
        for (Object obj : context()) {
            context.add(obj);
        }
        return Debug.sandbox("forceLog", new DelegatingDebugConfig().override(Level.LOG, Integer.MAX_VALUE).enable(LOG_METHOD), context.toArray());
    }

    public static DebugConfigScope disableIntercept() {
        return Debug.setConfig(new DelegatingDebugConfig().disable(INTERCEPT));
    }

    public static RuntimeException handle(Throwable exception) {
        if (ENABLED) {
            return DebugScope.getInstance().handle(exception);
        } else {
            if (exception instanceof Error) {
                throw (Error) exception;
            }
            if (exception instanceof RuntimeException) {
                throw (RuntimeException) exception;
            }
            throw new RuntimeException(exception);
        }
    }

    public static void log(String msg) {
        log(BASIC_LOG_LEVEL, msg);
    }

    public static void log(int logLevel, String msg) {
        if (ENABLED) {
            DebugScope.getInstance().log(logLevel, msg);
        }
    }

    public static void log(String format, Object arg) {
        log(BASIC_LOG_LEVEL, format, arg);
    }

    public static void log(int logLevel, String format, Object arg) {
        if (ENABLED) {
            DebugScope.getInstance().log(logLevel, format, arg);
        }
    }

    public static void log(String format, int arg) {
        log(BASIC_LOG_LEVEL, format, arg);
    }

    public static void log(int logLevel, String format, int arg) {
        if (ENABLED) {
            DebugScope.getInstance().log(logLevel, format, arg);
        }
    }

    public static void log(String format, Object arg1, Object arg2) {
        log(BASIC_LOG_LEVEL, format, arg1, arg2);
    }

    public static void log(int logLevel, String format, Object arg1, Object arg2) {
        if (ENABLED) {
            DebugScope.getInstance().log(logLevel, format, arg1, arg2);
        }
    }

    public static void log(String format, int arg1, Object arg2) {
        log(BASIC_LOG_LEVEL, format, arg1, arg2);
    }

    public static void log(int logLevel, String format, int arg1, Object arg2) {
        if (ENABLED) {
            DebugScope.getInstance().log(logLevel, format, arg1, arg2);
        }
    }

    public static void log(String format, Object arg1, int arg2) {
        log(BASIC_LOG_LEVEL, format, arg1, arg2);
    }

    public static void log(int logLevel, String format, Object arg1, int arg2) {
        if (ENABLED) {
            DebugScope.getInstance().log(logLevel, format, arg1, arg2);
        }
    }

    public static void log(String format, int arg1, int arg2) {
        log(BASIC_LOG_LEVEL, format, arg1, arg2);
    }

    public static void log(int logLevel, String format, int arg1, int arg2) {
        if (ENABLED) {
            DebugScope.getInstance().log(logLevel, format, arg1, arg2);
        }
    }

    public static void log(String format, Object arg1, Object arg2, Object arg3) {
        log(BASIC_LOG_LEVEL, format, arg1, arg2, arg3);
    }

    public static void log(int logLevel, String format, Object arg1, Object arg2, Object arg3) {
        if (ENABLED) {
            DebugScope.getInstance().log(logLevel, format, arg1, arg2, arg3);
        }
    }

    public static void log(String format, int arg1, int arg2, int arg3) {
        log(BASIC_LOG_LEVEL, format, arg1, arg2, arg3);
    }

    public static void log(int logLevel, String format, int arg1, int arg2, int arg3) {
        if (ENABLED) {
            DebugScope.getInstance().log(logLevel, format, arg1, arg2, arg3);
        }
    }

    public static void log(String format, Object arg1, Object arg2, Object arg3, Object arg4) {
        log(BASIC_LOG_LEVEL, format, arg1, arg2, arg3, arg4);
    }

    public static void log(int logLevel, String format, Object arg1, Object arg2, Object arg3, Object arg4) {
        if (ENABLED) {
            DebugScope.getInstance().log(logLevel, format, arg1, arg2, arg3, arg4);
        }
    }

    public static void log(String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        log(BASIC_LOG_LEVEL, format, arg1, arg2, arg3, arg4, arg5);
    }

    public static void log(int logLevel, String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        if (ENABLED) {
            DebugScope.getInstance().log(logLevel, format, arg1, arg2, arg3, arg4, arg5);
        }
    }

    public static void log(String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
        log(BASIC_LOG_LEVEL, format, arg1, arg2, arg3, arg4, arg5, arg6);
    }

    public static void log(int logLevel, String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
        if (ENABLED) {
            DebugScope.getInstance().log(logLevel, format, arg1, arg2, arg3, arg4, arg5, arg6);
        }
    }

    public static void log(String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
        log(BASIC_LOG_LEVEL, format, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
    }

    public static void log(String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
        log(BASIC_LOG_LEVEL, format, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
    }

    public static void log(int logLevel, String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
        if (ENABLED) {
            DebugScope.getInstance().log(logLevel, format, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
        }
    }

    public static void log(int logLevel, String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8) {
        if (ENABLED) {
            DebugScope.getInstance().log(logLevel, format, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8);
        }
    }

    public static void log(String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) {
        log(BASIC_LOG_LEVEL, format, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
    }

    public static void log(int logLevel, String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9) {
        if (ENABLED) {
            DebugScope.getInstance().log(logLevel, format, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
        }
    }

    public static void log(String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10) {
        log(BASIC_LOG_LEVEL, format, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10);
    }

    public static void log(int logLevel, String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7, Object arg8, Object arg9, Object arg10) {
        if (ENABLED) {
            DebugScope.getInstance().log(logLevel, format, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10);
        }
    }

    public static void logv(String format, Object... args) {
        logv(BASIC_LOG_LEVEL, format, args);
    }

    public static void logv(int logLevel, String format, Object... args) {
        if (!ENABLED) {
            throw new InternalError("Use of Debug.logv() must be guarded by a test of Debug.isEnabled()");
        }
        DebugScope.getInstance().log(logLevel, format, args);
    }

    @Deprecated
    public static void log(String format, Object[] args) {
        assert false : "shouldn't use this";
        log(BASIC_LOG_LEVEL, format, args);
    }

    @Deprecated
    public static void log(int logLevel, String format, Object[] args) {
        assert false : "shouldn't use this";
        logv(logLevel, format, args);
    }

    public static void dump(int dumpLevel, Object object, String msg) {
        if (ENABLED && DebugScope.getInstance().isDumpEnabled(dumpLevel)) {
            DebugScope.getInstance().dump(dumpLevel, object, msg);
        }
    }

    public static void dump(int dumpLevel, Object object, String format, Object arg) {
        if (ENABLED && DebugScope.getInstance().isDumpEnabled(dumpLevel)) {
            DebugScope.getInstance().dump(dumpLevel, object, format, arg);
        }
    }

    public static void dump(int dumpLevel, Object object, String format, Object arg1, Object arg2) {
        if (ENABLED && DebugScope.getInstance().isDumpEnabled(dumpLevel)) {
            DebugScope.getInstance().dump(dumpLevel, object, format, arg1, arg2);
        }
    }

    public static void dump(int dumpLevel, Object object, String format, Object arg1, Object arg2, Object arg3) {
        if (ENABLED && DebugScope.getInstance().isDumpEnabled(dumpLevel)) {
            DebugScope.getInstance().dump(dumpLevel, object, format, arg1, arg2, arg3);
        }
    }

    @Deprecated
    public static void dump(int dumpLevel, Object object, String format, Object[] args) {
        assert false : "shouldn't use this";
        if (ENABLED && DebugScope.getInstance().isDumpEnabled(dumpLevel)) {
            DebugScope.getInstance().dump(dumpLevel, object, format, args);
        }
    }

    public static void verify(Object object, String message) {
        if (ENABLED && DebugScope.getInstance().isVerifyEnabled()) {
            DebugScope.getInstance().verify(object, message);
        }
    }

    public static void verify(Object object, String format, Object arg) {
        if (ENABLED && DebugScope.getInstance().isVerifyEnabled()) {
            DebugScope.getInstance().verify(object, format, arg);
        }
    }

    @Deprecated
    public static void verify(Object object, String format, Object[] args) {
        assert false : "shouldn't use this";
        if (ENABLED && DebugScope.getInstance().isVerifyEnabled()) {
            DebugScope.getInstance().verify(object, format, args);
        }
    }

    public static Indent indent() {
        if (ENABLED) {
            DebugScope scope = DebugScope.getInstance();
            return scope.pushIndentLogger();
        }
        return null;
    }

    public static Indent logAndIndent(String msg) {
        return logAndIndent(BASIC_LOG_LEVEL, msg);
    }

    public static Indent logAndIndent(int logLevel, String msg) {
        if (ENABLED && Debug.isLogEnabled(logLevel)) {
            return logvAndIndentInternal(logLevel, msg);
        }
        return null;
    }

    public static Indent logAndIndent(String format, Object arg) {
        return logAndIndent(BASIC_LOG_LEVEL, format, arg);
    }

    public static Indent logAndIndent(int logLevel, String format, Object arg) {
        if (ENABLED && Debug.isLogEnabled(logLevel)) {
            return logvAndIndentInternal(logLevel, format, arg);
        }
        return null;
    }

    public static Indent logAndIndent(String format, int arg) {
        return logAndIndent(BASIC_LOG_LEVEL, format, arg);
    }

    public static Indent logAndIndent(int logLevel, String format, int arg) {
        if (ENABLED && Debug.isLogEnabled(logLevel)) {
            return logvAndIndentInternal(logLevel, format, arg);
        }
        return null;
    }

    public static Indent logAndIndent(String format, int arg1, Object arg2) {
        return logAndIndent(BASIC_LOG_LEVEL, format, arg1, arg2);
    }

    public static Indent logAndIndent(int logLevel, String format, int arg1, Object arg2) {
        if (ENABLED && Debug.isLogEnabled(logLevel)) {
            return logvAndIndentInternal(logLevel, format, arg1, arg2);
        }
        return null;
    }

    public static Indent logAndIndent(String format, Object arg1, int arg2) {
        return logAndIndent(BASIC_LOG_LEVEL, format, arg1, arg2);
    }

    public static Indent logAndIndent(int logLevel, String format, Object arg1, int arg2) {
        if (ENABLED && Debug.isLogEnabled(logLevel)) {
            return logvAndIndentInternal(logLevel, format, arg1, arg2);
        }
        return null;
    }

    public static Indent logAndIndent(String format, int arg1, int arg2) {
        return logAndIndent(BASIC_LOG_LEVEL, format, arg1, arg2);
    }

    public static Indent logAndIndent(int logLevel, String format, int arg1, int arg2) {
        if (ENABLED && Debug.isLogEnabled(logLevel)) {
            return logvAndIndentInternal(logLevel, format, arg1, arg2);
        }
        return null;
    }

    public static Indent logAndIndent(String format, Object arg1, Object arg2) {
        return logAndIndent(BASIC_LOG_LEVEL, format, arg1, arg2);
    }

    public static Indent logAndIndent(int logLevel, String format, Object arg1, Object arg2) {
        if (ENABLED && Debug.isLogEnabled(logLevel)) {
            return logvAndIndentInternal(logLevel, format, arg1, arg2);
        }
        return null;
    }

    public static Indent logAndIndent(String format, Object arg1, Object arg2, Object arg3) {
        return logAndIndent(BASIC_LOG_LEVEL, format, arg1, arg2, arg3);
    }

    public static Indent logAndIndent(int logLevel, String format, Object arg1, Object arg2, Object arg3) {
        if (ENABLED && Debug.isLogEnabled(logLevel)) {
            return logvAndIndentInternal(logLevel, format, arg1, arg2, arg3);
        }
        return null;
    }

    public static Indent logAndIndent(String format, int arg1, int arg2, int arg3) {
        return logAndIndent(BASIC_LOG_LEVEL, format, arg1, arg2, arg3);
    }

    public static Indent logAndIndent(int logLevel, String format, int arg1, int arg2, int arg3) {
        if (ENABLED && Debug.isLogEnabled(logLevel)) {
            return logvAndIndentInternal(logLevel, format, arg1, arg2, arg3);
        }
        return null;
    }

    public static Indent logAndIndent(String format, Object arg1, int arg2, int arg3) {
        return logAndIndent(BASIC_LOG_LEVEL, format, arg1, arg2, arg3);
    }

    public static Indent logAndIndent(int logLevel, String format, Object arg1, int arg2, int arg3) {
        if (ENABLED && Debug.isLogEnabled(logLevel)) {
            return logvAndIndentInternal(logLevel, format, arg1, arg2, arg3);
        }
        return null;
    }

    public static Indent logAndIndent(String format, Object arg1, Object arg2, Object arg3, Object arg4) {
        return logAndIndent(BASIC_LOG_LEVEL, format, arg1, arg2, arg3, arg4);
    }

    public static Indent logAndIndent(int logLevel, String format, Object arg1, Object arg2, Object arg3, Object arg4) {
        if (ENABLED && Debug.isLogEnabled(logLevel)) {
            return logvAndIndentInternal(logLevel, format, arg1, arg2, arg3, arg4);
        }
        return null;
    }

    public static Indent logAndIndent(String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        return logAndIndent(BASIC_LOG_LEVEL, format, arg1, arg2, arg3, arg4, arg5);
    }

    public static Indent logAndIndent(int logLevel, String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        if (ENABLED && Debug.isLogEnabled(logLevel)) {
            return logvAndIndentInternal(logLevel, format, arg1, arg2, arg3, arg4, arg5);
        }
        return null;
    }

    public static Indent logAndIndent(String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
        return logAndIndent(BASIC_LOG_LEVEL, format, arg1, arg2, arg3, arg4, arg5, arg6);
    }

    public static Indent logAndIndent(int logLevel, String format, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
        if (ENABLED && Debug.isLogEnabled(logLevel)) {
            return logvAndIndentInternal(logLevel, format, arg1, arg2, arg3, arg4, arg5, arg6);
        }
        return null;
    }

    public static Indent logvAndIndent(int logLevel, String format, Object... args) {
        if (ENABLED) {
            if (Debug.isLogEnabled(logLevel)) {
                return logvAndIndentInternal(logLevel, format, args);
            }
            return null;
        }
        throw new InternalError("Use of Debug.logvAndIndent() must be guarded by a test of Debug.isEnabled()");
    }

    private static Indent logvAndIndentInternal(int logLevel, String format, Object... args) {
        assert ENABLED && Debug.isLogEnabled(logLevel) : "must have checked Debug.isLogEnabled()";
        DebugScope scope = DebugScope.getInstance();
        scope.log(logLevel, format, args);
        return scope.pushIndentLogger();
    }

    @Deprecated
    public static void logAndIndent(String format, Object[] args) {
        assert false : "shouldn't use this";
        logAndIndent(BASIC_LOG_LEVEL, format, args);
    }

    @Deprecated
    public static void logAndIndent(int logLevel, String format, Object[] args) {
        assert false : "shouldn't use this";
        logvAndIndent(logLevel, format, args);
    }

    public static Iterable<Object> context() {
        if (ENABLED) {
            return DebugScope.getInstance().getCurrentContext();
        } else {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> contextSnapshot(Class<T> clazz) {
        if (ENABLED) {
            List<T> result = new ArrayList<>();
            for (Object o : context()) {
                if (clazz.isInstance(o)) {
                    result.add((T) o);
                }
            }
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T contextLookup(Class<T> clazz) {
        if (ENABLED) {
            for (Object o : context()) {
                if (clazz.isInstance(o)) {
                    return ((T) o);
                }
            }
        }
        return null;
    }

    public static DebugMemUseTracker memUseTracker(CharSequence name) {
        if (!isUnconditionalMemUseTrackingEnabled && !ENABLED) {
            return VOID_MEM_USE_TRACKER;
        }
        return createMemUseTracker("%s", name, null);
    }

    public static DebugMemUseTracker memUseTracker(String format, Object arg) {
        if (!isUnconditionalMemUseTrackingEnabled && !ENABLED) {
            return VOID_MEM_USE_TRACKER;
        }
        return createMemUseTracker(format, arg, null);
    }

    public static DebugMemUseTracker memUseTracker(String format, Object arg1, Object arg2) {
        if (!isUnconditionalMemUseTrackingEnabled && !ENABLED) {
            return VOID_MEM_USE_TRACKER;
        }
        return createMemUseTracker(format, arg1, arg2);
    }

    private static DebugMemUseTracker createMemUseTracker(String format, Object arg1, Object arg2) {
        String name = formatDebugName(format, arg1, arg2);
        return DebugValueFactory.createMemUseTracker(name, !isUnconditionalMemUseTrackingEnabled);
    }

    public static DebugCounter counter(CharSequence name) {
        if (!areUnconditionalCountersEnabled() && !ENABLED) {
            return VOID_COUNTER;
        }
        return createCounter("%s", name, null);
    }

    public static DebugMethodMetrics methodMetrics(ResolvedJavaMethod method) {
        if (isMethodMeterEnabled() && method != null) {
            return MethodMetricsImpl.getMethodMetrics(method);
        }
        return VOID_MM;
    }

    public static String applyFormattingFlagsAndWidth(String s, int flags, int width) {
        if (flags == 0 && width < 0) {
            return s;
        }
        StringBuilder sb = new StringBuilder(s);
        int len = sb.length();
        if (len < width) {
            for (int i = 0; i < width - len; i++) {
                if ((flags & LEFT_JUSTIFY) == LEFT_JUSTIFY) {
                    sb.append(' ');
                } else {
                    sb.insert(0, ' ');
                }
            }
        }
        String res = sb.toString();
        if ((flags & UPPERCASE) == UPPERCASE) {
            res = res.toUpperCase();
        }
        return res;
    }

    public static DebugCounter counter(String format, Object arg) {
        if (!areUnconditionalCountersEnabled() && !ENABLED) {
            return VOID_COUNTER;
        }
        return createCounter(format, arg, null);
    }

    public static DebugCounter counter(String format, Object arg1, Object arg2) {
        if (!areUnconditionalCountersEnabled() && !ENABLED) {
            return VOID_COUNTER;
        }
        return createCounter(format, arg1, arg2);
    }

    private static DebugCounter createCounter(String format, Object arg1, Object arg2) {
        String name = formatDebugName(format, arg1, arg2);
        boolean conditional = enabledCounters == null || !findMatch(enabledCounters, enabledCountersSubstrings, name);
        if (!ENABLED && conditional) {
            return VOID_COUNTER;
        }
        return DebugValueFactory.createCounter(name, conditional);
    }

    public static DebugConfigScope setConfig(DebugConfig config) {
        if (ENABLED) {
            return new DebugConfigScope(config);
        } else {
            return null;
        }
    }

    public static DebugHistogram createHistogram(String name) {
        return new DebugHistogramImpl(name);
    }

    public static DebugConfig silentConfig() {
        return fixedConfig(0, 0, false, false, false, false, false, Collections.<DebugDumpHandler>emptyList(), Collections.<DebugVerifyHandler>emptyList(), null);
    }

    public static DebugConfig fixedConfig(final int logLevel, final int dumpLevel, final boolean isCountEnabled, final boolean isMemUseTrackingEnabled, final boolean isTimerEnabled, final boolean isVerifyEnabled, final boolean isMMEnabled, final Collection<DebugDumpHandler> dumpHandlers, final Collection<DebugVerifyHandler> verifyHandlers, final PrintStream output) {
        return new DebugConfig() {

            @Override
            public int getLogLevel() {
                return logLevel;
            }

            @Override
            public boolean isLogEnabledForMethod() {
                return logLevel > 0;
            }

            @Override
            public boolean isCountEnabled() {
                return isCountEnabled;
            }

            @Override
            public boolean isMemUseTrackingEnabled() {
                return isMemUseTrackingEnabled;
            }

            @Override
            public int getDumpLevel() {
                return dumpLevel;
            }

            @Override
            public boolean isDumpEnabledForMethod() {
                return dumpLevel > 0;
            }

            @Override
            public boolean isVerifyEnabled() {
                return isVerifyEnabled;
            }

            @Override
            public boolean isVerifyEnabledForMethod() {
                return isVerifyEnabled;
            }

            @Override
            public boolean isMethodMeterEnabled() {
                return isMMEnabled;
            }

            @Override
            public boolean isTimeEnabled() {
                return isTimerEnabled;
            }

            @Override
            public RuntimeException interceptException(Throwable e) {
                return null;
            }

            @Override
            public Collection<DebugDumpHandler> dumpHandlers() {
                return dumpHandlers;
            }

            @Override
            public Collection<DebugVerifyHandler> verifyHandlers() {
                return verifyHandlers;
            }

            @Override
            public PrintStream output() {
                return output;
            }

            @Override
            public void addToContext(Object o) {
            }

            @Override
            public void removeFromContext(Object o) {
            }
        };
    }

    private static final DebugCounter VOID_COUNTER = new DebugCounter() {

        @Override
        public void increment() {
        }

        @Override
        public void add(long value) {
        }

        @Override
        public void setConditional(boolean flag) {
            throw new InternalError("Cannot make void counter conditional");
        }

        @Override
        public boolean isConditional() {
            return false;
        }

        @Override
        public long getCurrentValue() {
            return 0L;
        }
    };

    private static final DebugMethodMetrics VOID_MM = new DebugMethodMetrics() {

        @Override
        public void addToMetric(long value, String metricName) {
        }

        @Override
        public void addToMetric(long value, String format, Object arg1) {
        }

        @Override
        public void addToMetric(long value, String format, Object arg1, Object arg2) {
        }

        @Override
        public void addToMetric(long value, String format, Object arg1, Object arg2, Object arg3) {
        }

        @Override
        public void incrementMetric(String metricName) {
        }

        @Override
        public void incrementMetric(String format, Object arg1) {
        }

        @Override
        public void incrementMetric(String format, Object arg1, Object arg2) {
        }

        @Override
        public void incrementMetric(String format, Object arg1, Object arg2, Object arg3) {
        }

        @Override
        public long getCurrentMetricValue(String metricName) {
            return 0;
        }

        @Override
        public long getCurrentMetricValue(String format, Object arg1) {
            return 0;
        }

        @Override
        public long getCurrentMetricValue(String format, Object arg1, Object arg2) {
            return 0;
        }

        @Override
        public long getCurrentMetricValue(String format, Object arg1, Object arg2, Object arg3) {
            return 0;
        }

        @Override
        public ResolvedJavaMethod getMethod() {
            return null;
        }
    };

    private static final DebugMemUseTracker VOID_MEM_USE_TRACKER = new DebugMemUseTracker() {

        @Override
        public DebugCloseable start() {
            return DebugCloseable.VOID_CLOSEABLE;
        }

        @Override
        public long getCurrentValue() {
            return 0;
        }
    };

    public static final String ENABLE_TIMER_PROPERTY_NAME_PREFIX = "graaldebug.timer.";

    public static final String ENABLE_COUNTER_PROPERTY_NAME_PREFIX = "graaldebug.counter.";

    private static final Set<String> enabledCounters;

    private static final Set<String> enabledTimers;

    private static final Set<String> enabledCountersSubstrings = new HashSet<>();

    private static final Set<String> enabledTimersSubstrings = new HashSet<>();

    private static final boolean isUnconditionalMemUseTrackingEnabled;

    static {
        Set<String> counters = new HashSet<>();
        Set<String> timers = new HashSet<>();
        parseCounterAndTimerSystemProperties(counters, timers, enabledCountersSubstrings, enabledTimersSubstrings);
        counters = counters.isEmpty() && enabledCountersSubstrings.isEmpty() ? null : counters;
        timers = timers.isEmpty() && enabledTimersSubstrings.isEmpty() ? null : timers;
        if (counters == null && params.enableUnscopedCounters && !params.enableMethodFilter) {
            counters = Collections.emptySet();
        }
        if (timers == null && params.enableUnscopedTimers && !params.enableMethodFilter) {
            timers = Collections.emptySet();
        }
        enabledCounters = counters;
        enabledTimers = timers;
        isUnconditionalMemUseTrackingEnabled = params.enableUnscopedMemUseTrackers;
        DebugValueFactory = initDebugValueFactory();
    }

    private static DebugValueFactory initDebugValueFactory() {
        return new DebugValueFactory() {

            @Override
            public DebugTimer createTimer(String name, boolean conditional) {
                return new TimerImpl(name, conditional, params.interceptTime);
            }

            @Override
            public DebugCounter createCounter(String name, boolean conditional) {
                return CounterImpl.create(name, conditional, params.interceptCount);
            }

            @Override
            public DebugMethodMetrics createMethodMetrics(ResolvedJavaMethod method) {
                return MethodMetricsImpl.getMethodMetrics(method);
            }

            @Override
            public DebugMemUseTracker createMemUseTracker(String name, boolean conditional) {
                return new MemUseTrackerImpl(name, conditional, params.interceptMem);
            }
        };
    }

    private static DebugValueFactory DebugValueFactory;

    public static void setDebugValueFactory(DebugValueFactory factory) {
        Objects.requireNonNull(factory);
        DebugValueFactory = factory;
    }

    public static DebugValueFactory getDebugValueFactory() {
        return DebugValueFactory;
    }

    private static boolean findMatch(Set<String> haystack, Set<String> haystackSubstrings, String needle) {
        if (haystack.isEmpty() && haystackSubstrings.isEmpty()) {
            return true;
        }
        if (haystack.contains(needle)) {
            return true;
        }
        if (!haystackSubstrings.isEmpty()) {
            for (String h : haystackSubstrings) {
                if (needle.startsWith(h)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean areUnconditionalTimersEnabled() {
        return enabledTimers != null;
    }

    public static boolean areUnconditionalCountersEnabled() {
        return enabledCounters != null;
    }

    public static boolean isMethodFilteringEnabled() {
        return params.enableMethodFilter;
    }

    public static boolean areUnconditionalMethodMetricsEnabled() {
        return params.enableUnscopedMethodMetrics;
    }

    protected static void parseCounterAndTimerSystemProperties(Set<String> counters, Set<String> timers, Set<String> countersSubstrings, Set<String> timersSubstrings) {
        do {
            try {
                for (Map.Entry<Object, Object> e : System.getProperties().entrySet()) {
                    String name = e.getKey().toString();
                    if (name.startsWith(ENABLE_COUNTER_PROPERTY_NAME_PREFIX) && Boolean.parseBoolean(e.getValue().toString())) {
                        if (name.endsWith("*")) {
                            countersSubstrings.add(name.substring(ENABLE_COUNTER_PROPERTY_NAME_PREFIX.length(), name.length() - 1));
                        } else {
                            counters.add(name.substring(ENABLE_COUNTER_PROPERTY_NAME_PREFIX.length()));
                        }
                    }
                    if (name.startsWith(ENABLE_TIMER_PROPERTY_NAME_PREFIX) && Boolean.parseBoolean(e.getValue().toString())) {
                        if (name.endsWith("*")) {
                            timersSubstrings.add(name.substring(ENABLE_TIMER_PROPERTY_NAME_PREFIX.length(), name.length() - 1));
                        } else {
                            timers.add(name.substring(ENABLE_TIMER_PROPERTY_NAME_PREFIX.length()));
                        }
                    }
                }
                return;
            } catch (ConcurrentModificationException e) {
            }
        } while (true);
    }

    public static DebugTimer timer(CharSequence name) {
        if (!areUnconditionalTimersEnabled() && !ENABLED) {
            return VOID_TIMER;
        }
        return createTimer("%s", name, null);
    }

    public static DebugTimer timer(String format, Object arg) {
        if (!areUnconditionalTimersEnabled() && !ENABLED) {
            return VOID_TIMER;
        }
        return createTimer(format, arg, null);
    }

    public static DebugTimer timer(String format, Object arg1, Object arg2) {
        if (!areUnconditionalTimersEnabled() && !ENABLED) {
            return VOID_TIMER;
        }
        return createTimer(format, arg1, arg2);
    }

    private static final ClassValue<String> formattedClassName = new ClassValue<String>() {

        @Override
        protected String computeValue(Class<?> c) {
            final String simpleName = c.getSimpleName();
            Class<?> enclosingClass = c.getEnclosingClass();
            if (enclosingClass != null) {
                String prefix = "";
                while (enclosingClass != null) {
                    prefix = enclosingClass.getSimpleName() + "_" + prefix;
                    enclosingClass = enclosingClass.getEnclosingClass();
                }
                return prefix + simpleName;
            } else {
                return simpleName;
            }
        }
    };

    public static Object convertFormatArg(Object arg) {
        if (arg instanceof Class) {
            return formattedClassName.get((Class<?>) arg);
        }
        return arg;
    }

    private static String formatDebugName(String format, Object arg1, Object arg2) {
        return String.format(format, convertFormatArg(arg1), convertFormatArg(arg2));
    }

    private static DebugTimer createTimer(String format, Object arg1, Object arg2) {
        String name = formatDebugName(format, arg1, arg2);
        boolean conditional = enabledTimers == null || !findMatch(enabledTimers, enabledTimersSubstrings, name);
        if (!ENABLED && conditional) {
            return VOID_TIMER;
        }
        return DebugValueFactory.createTimer(name, conditional);
    }

    private static final DebugTimer VOID_TIMER = new DebugTimer() {

        @Override
        public DebugCloseable start() {
            return DebugCloseable.VOID_CLOSEABLE;
        }

        @Override
        public void setConditional(boolean flag) {
            throw new InternalError("Cannot make void timer conditional");
        }

        @Override
        public boolean isConditional() {
            return false;
        }

        @Override
        public long getCurrentValue() {
            return 0L;
        }

        @Override
        public TimeUnit getTimeUnit() {
            return null;
        }
    };
}
