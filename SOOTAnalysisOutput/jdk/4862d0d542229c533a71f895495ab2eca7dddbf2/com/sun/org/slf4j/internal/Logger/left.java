package com.sun.org.slf4j.internal;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;

public class Logger {

    private static final StackWalker WALKER;

    static {
        final PrivilegedAction<StackWalker> action = () -> StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        WALKER = AccessController.doPrivileged(action);
    }

    private final java.util.logging.Logger impl;

    public Logger(String name) {
        impl = java.util.logging.Logger.getLogger(name);
    }

    public boolean isDebugEnabled() {
        return impl.isLoggable(Level.FINE);
    }

    public boolean isTraceEnabled() {
        return impl.isLoggable(Level.FINE);
    }

    public void debug(String s) {
        log0(Level.FINE, s);
    }

    public void debug(String s, Throwable e) {
        log0(Level.FINE, s, e);
    }

    public void debug(String s, Object... o) {
        log0(Level.FINE, s, o);
    }

    public void trace(String s) {
        log0(Level.FINE, s);
    }

    public void error(String s) {
        log0(Level.SEVERE, s);
    }

    public void error(String s, Throwable e) {
        log0(Level.SEVERE, s, e);
    }

    public void error(String s, Object... o) {
        log0(Level.SEVERE, s, o);
    }

    public void warn(String s) {
        log0(Level.WARNING, s);
    }

    public void warn(String s, Throwable e) {
        log0(Level.WARNING, s, e);
    }

    private void log0(Level level, String s) {
        if (impl.isLoggable(level)) {
            var sf = WALKER.walk(f -> f.skip(2).findFirst()).get();
            impl.logp(Level.FINE, sf.getClassName(), sf.getMethodName(), s);
        }
    }

    public void log0(Level level, String s, Throwable e) {
        if (impl.isLoggable(level)) {
            var sf = WALKER.walk(f -> f.skip(2).findFirst()).get();
            impl.logp(Level.FINE, sf.getClassName(), sf.getMethodName(), s, e);
        }
    }

    public void log0(Level level, String s, Object... o) {
        if (impl.isLoggable(level)) {
            var sf = WALKER.walk(f -> f.skip(2).findFirst()).get();
            impl.logp(Level.FINE, sf.getClassName(), sf.getMethodName(), addIndex(s), o);
        }
    }

    private static String addIndex(String s) {
        int start = 0;
        int index = 0;
        StringBuilder sb = new StringBuilder();
        while (true) {
            int pos = s.indexOf("{}", start);
            if (pos < 0) {
                break;
            }
            sb.append(s, start, pos + 1).append(index++);
            start = pos + 1;
        }
        if (index == 0) {
            return s;
        } else {
            sb.append(s, start, s.length());
            return sb.toString();
        }
    }
}
