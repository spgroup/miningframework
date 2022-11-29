package org.graalvm.compiler.debug;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.graalvm.compiler.serviceprovider.GraalServices;

public class TTY {

    public static class Filter {

        private LogStream previous;

        private final Thread thread = Thread.currentThread();

        public Filter(String filter, Object object) {
            boolean suppressed = false;
            if (filter != null) {
                String input = object.toString();
                if (filter.startsWith("~")) {
                    suppressed = !Pattern.matches(filter.substring(1), input);
                } else {
                    suppressed = !input.contains(filter);
                }
                if (suppressed) {
                    previous = out();
                    log.set(LogStream.SINK);
                }
            }
        }

        public Filter() {
            previous = out();
            log.set(LogStream.SINK);
        }

        public void remove() {
            assert thread == Thread.currentThread();
            if (previous != null) {
                log.set(previous);
            }
        }
    }

    public static final PrintStream out;

    static {
        TTYStreamProvider p = GraalServices.loadSingle(TTYStreamProvider.class, false);
        out = p == null ? System.out : p.getStream();
    }

    private static final ThreadLocal<LogStream> log = new ThreadLocal<LogStream>() {

        @Override
        protected LogStream initialValue() {
            return new LogStream(out);
        }
    };

    public static boolean isSuppressed() {
        return log.get() == LogStream.SINK;
    }

    public static LogStream out() {
        return log.get();
    }

    public static void print(String s) {
        out().print(s);
    }

    public static void print(int i) {
        out().print(i);
    }

    public static void print(long i) {
        out().print(i);
    }

    public static void print(char c) {
        out().print(c);
    }

    public static void print(boolean b) {
        out().print(b);
    }

    public static void print(double d) {
        out().print(d);
    }

    public static void print(float f) {
        out().print(f);
    }

    public static void println(String s) {
        out().println(s);
    }

    public static void println() {
        out().println();
    }

    public static void println(int i) {
        out().println(i);
    }

    public static void println(long l) {
        out().println(l);
    }

    public static void println(char c) {
        out().println(c);
    }

    public static void println(boolean b) {
        out().println(b);
    }

    public static void println(double d) {
        out().println(d);
    }

    public static void println(float f) {
        out().println(f);
    }

    public static void printf(String format, Object... args) {
        out().printf(format, args);
    }

    public static void println(String format, Object... args) {
        out().printf(format + "%n", args);
    }

    public static void fillTo(int i) {
        out().fillTo(i, ' ');
    }

    public static void printFields(Class<?> javaClass) {
        final String className = javaClass.getSimpleName();
        TTY.println(className + " {");
        for (final Field field : javaClass.getFields()) {
            printField(field, false);
        }
        TTY.println("}");
    }

    public static void printField(final Field field, boolean tabbed) {
        final String fieldName = String.format("%35s", field.getName());
        try {
            String prefix = tabbed ? "" : "    " + fieldName + " = ";
            String postfix = tabbed ? "\t" : "\n";
            if (field.getType() == int.class) {
                TTY.print(prefix + field.getInt(null) + postfix);
            } else if (field.getType() == boolean.class) {
                TTY.print(prefix + field.getBoolean(null) + postfix);
            } else if (field.getType() == float.class) {
                TTY.print(prefix + field.getFloat(null) + postfix);
            } else if (field.getType() == String.class) {
                TTY.print(prefix + field.get(null) + postfix);
            } else if (field.getType() == Map.class) {
                Map<?, ?> m = (Map<?, ?>) field.get(null);
                TTY.print(prefix + printMap(m) + postfix);
            } else {
                TTY.print(prefix + field.get(null) + postfix);
            }
        } catch (IllegalAccessException e) {
        }
    }

    private static String printMap(Map<?, ?> m) {
        StringBuilder sb = new StringBuilder();
        List<String> keys = new ArrayList<>();
        for (Object key : m.keySet()) {
            keys.add((String) key);
        }
        Collections.sort(keys);
        for (String key : keys) {
            sb.append(key);
            sb.append("\t");
            sb.append(m.get(key));
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void flush() {
        out().flush();
    }
}
