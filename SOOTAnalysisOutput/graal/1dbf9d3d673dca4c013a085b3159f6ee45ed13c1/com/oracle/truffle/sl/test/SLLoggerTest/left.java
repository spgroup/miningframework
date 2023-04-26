package com.oracle.truffle.sl.test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SLLoggerTest {

    private static final Source ADD_SL;

    private static final Source MUL_SL;

    static {
        ADD_SL = Source.newBuilder("sl", "function add(a,b) {return a + b;} function main() {return add(1,1);}", "add.sl").buildLiteral();
        MUL_SL = Source.newBuilder("sl", "function mul(a,b) {return a * b;} function main() {return mul(1,1);}", "mul.sl").buildLiteral();
    }

    private TestHandler testHandler;

    private Context currentContext;

    @Before
    public void setUp() {
        testHandler = new TestHandler();
    }

    @After
    public void tearDown() {
        if (currentContext != null) {
            currentContext.close();
            currentContext = null;
        }
    }

    private Context createContext(Map<String, String> options) {
        if (currentContext != null) {
            throw new IllegalStateException("Context already created");
        }
        currentContext = Context.newBuilder("sl").options(options).logHandler(testHandler).build();
        return currentContext;
    }

    @Test
    public void testLoggerNoConfig() {
        final Context context = createContext(Collections.emptyMap());
        executeSlScript(context);
        Assert.assertTrue(functionNames(testHandler.getRecords()).isEmpty());
    }

    @Test
    public void testLoggerSlFunctionLevelFine() {
        final Context context = createContext(createLoggingOptions("sl", "com.oracle.truffle.sl.runtime.SLFunction", "FINE"));
        executeSlScript(context);
        Assert.assertFalse(functionNames(testHandler.getRecords()).isEmpty());
    }

    @Test
    public void testLoggerSlFunctionParentLevelFine() {
        final Context context = createContext(createLoggingOptions("sl", "com.oracle.truffle.sl.runtime", "FINE"));
        executeSlScript(context);
        Assert.assertFalse(functionNames(testHandler.getRecords()).isEmpty());
    }

    @Test
    public void testLoggerSlFunctionSiblingLevelFine() {
        final Context context = createContext(createLoggingOptions("sl", "com.oracle.truffle.sl.runtime.SLContext", "FINE"));
        executeSlScript(context);
        Assert.assertTrue(functionNames(testHandler.getRecords()).isEmpty());
    }

    @Test
    public void testMultipleContextsExclusiveFineLevel() {
        final TestHandler handler1 = new TestHandler();
        try (Context ctx = Context.newBuilder("sl").options(createLoggingOptions("sl", "com.oracle.truffle.sl.runtime.SLFunction", "FINE")).logHandler(handler1).build()) {
            executeSlScript(ctx, ADD_SL, 2);
        }
        final TestHandler handler2 = new TestHandler();
        try (Context ctx = Context.newBuilder("sl").options(createLoggingOptions("sl", "com.oracle.truffle.sl.runtime.SLFunction", "FINE")).logHandler(handler2).build()) {
            executeSlScript(ctx, MUL_SL, 1);
        }
        final TestHandler handler3 = new TestHandler();
        try (Context ctx = Context.newBuilder("sl").options(createLoggingOptions("sl", "com.oracle.truffle.sl.runtime.SLFunction", "FINE")).logHandler(handler3).build()) {
            executeSlScript(ctx, ADD_SL, 2);
        }
        Set<String> functionNames = functionNames(handler1.getRecords());
        Assert.assertTrue(functionNames.contains("add"));
        Assert.assertFalse(functionNames.contains("mul"));
        functionNames = functionNames(handler2.getRecords());
        Assert.assertFalse(functionNames.contains("add"));
        Assert.assertTrue(functionNames.contains("mul"));
        functionNames = functionNames(handler3.getRecords());
        Assert.assertTrue(functionNames.contains("add"));
        Assert.assertFalse(functionNames.contains("mul"));
    }

    @Test
    public void testMultipleContextsExclusiveDifferentLogLevel() {
        final TestHandler handler1 = new TestHandler();
        try (Context ctx = Context.newBuilder("sl").options(createLoggingOptions("sl", "com.oracle.truffle.sl.runtime.SLFunction", "FINE")).logHandler(handler1).build()) {
            executeSlScript(ctx, ADD_SL, 2);
        }
        final TestHandler handler2 = new TestHandler();
        try (Context ctx = Context.newBuilder("sl").logHandler(handler2).build()) {
            executeSlScript(ctx, MUL_SL, 1);
        }
        final TestHandler handler3 = new TestHandler();
        try (Context ctx = Context.newBuilder("sl").options(createLoggingOptions("sl", "com.oracle.truffle.sl.runtime.SLFunction", "FINE")).logHandler(handler3).build()) {
            executeSlScript(ctx, ADD_SL, 2);
        }
        Set<String> functionNames = functionNames(handler1.getRecords());
        Assert.assertTrue(functionNames.contains("add"));
        Assert.assertFalse(functionNames.contains("mul"));
        functionNames = functionNames(handler2.getRecords());
        Assert.assertTrue(functionNames.isEmpty());
        functionNames = functionNames(handler3.getRecords());
        Assert.assertTrue(functionNames.contains("add"));
        Assert.assertFalse(functionNames.contains("mul"));
    }

    @Test
    public void testMultipleContextsNestedFineLevel() {
        final TestHandler handler1 = new TestHandler();
        final TestHandler handler2 = new TestHandler();
        final TestHandler handler3 = new TestHandler();
        try (Context ctx1 = Context.newBuilder("sl").options(createLoggingOptions("sl", "com.oracle.truffle.sl.runtime.SLFunction", "FINE")).logHandler(handler1).build()) {
            try (Context ctx2 = Context.newBuilder("sl").options(createLoggingOptions("sl", "com.oracle.truffle.sl.runtime.SLFunction", "FINE")).logHandler(handler2).build()) {
                try (Context ctx3 = Context.newBuilder("sl").options(createLoggingOptions("sl", "com.oracle.truffle.sl.runtime.SLFunction", "FINE")).logHandler(handler3).build()) {
                    executeSlScript(ctx1, ADD_SL, 2);
                    executeSlScript(ctx2, MUL_SL, 1);
                    executeSlScript(ctx3, ADD_SL, 2);
                }
            }
        }
        Set<String> functionNames = functionNames(handler1.getRecords());
        Assert.assertTrue(functionNames.contains("add"));
        Assert.assertFalse(functionNames.contains("mul"));
        functionNames = functionNames(handler2.getRecords());
        Assert.assertFalse(functionNames.contains("add"));
        Assert.assertTrue(functionNames.contains("mul"));
        functionNames = functionNames(handler3.getRecords());
        Assert.assertTrue(functionNames.contains("add"));
        Assert.assertFalse(functionNames.contains("mul"));
    }

    @Test
    public void testMultipleContextsNestedDifferentLogLevel() {
        final TestHandler handler1 = new TestHandler();
        final TestHandler handler2 = new TestHandler();
        final TestHandler handler3 = new TestHandler();
        try (Context ctx1 = Context.newBuilder("sl").options(createLoggingOptions("sl", "com.oracle.truffle.sl.runtime.SLFunction", "FINE")).logHandler(handler1).build()) {
            try (Context ctx2 = Context.newBuilder("sl").options(createLoggingOptions("sl", "com.oracle.truffle.sl.runtime.SLFunction", "FINE")).logHandler(handler2).build()) {
                try (Context ctx3 = Context.newBuilder("sl").logHandler(handler3).build()) {
                    executeSlScript(ctx1, ADD_SL, 2);
                    executeSlScript(ctx2, MUL_SL, 1);
                    executeSlScript(ctx3, ADD_SL, 2);
                }
            }
        }
        Set<String> functionNames = functionNames(handler1.getRecords());
        Assert.assertTrue(functionNames.contains("add"));
        Assert.assertFalse(functionNames.contains("mul"));
        functionNames = functionNames(handler2.getRecords());
        Assert.assertFalse(functionNames.contains("add"));
        Assert.assertTrue(functionNames.contains("mul"));
        functionNames = functionNames(handler3.getRecords());
        Assert.assertTrue(functionNames.isEmpty());
    }

    private static void executeSlScript(final Context context) {
        executeSlScript(context, ADD_SL, 2);
    }

    private static void executeSlScript(final Context context, final Source src, final int expectedResult) {
        final Value res = context.eval(src);
        Assert.assertTrue(res.isNumber());
        Assert.assertEquals(expectedResult, res.asInt());
    }

    private static Map<String, String> createLoggingOptions(String... kvs) {
        if ((kvs.length % 3) != 0) {
            throw new IllegalArgumentException("Lang, Key, Val length has to be divisible by 3.");
        }
        final Map<String, String> options = new HashMap<>();
        for (int i = 0; i < kvs.length; i += 3) {
            options.put(String.format("log.%s.%s.level", kvs[i], kvs[i + 1]), kvs[i + 2]);
        }
        return options;
    }

    private static Set<String> functionNames(final List<? extends LogRecord> records) {
        return records.stream().filter((lr) -> "sl.com.oracle.truffle.sl.runtime.SLFunction".equals(lr.getLoggerName())).map((lr) -> (String) lr.getParameters()[0]).collect(Collectors.toSet());
    }

    private static final class TestHandler extends Handler {

        private final Queue<LogRecord> records;

        private volatile boolean closed;

        TestHandler() {
            this.records = new ArrayDeque<>();
        }

        @Override
        public void publish(LogRecord record) {
            if (closed) {
                throw new IllegalStateException("Closed handler");
            }
            records.offer(record);
        }

        @Override
        public void flush() {
            if (closed) {
                throw new IllegalStateException("Closed handler");
            }
        }

        public List<? extends LogRecord> getRecords() {
            return new ArrayList<>(records);
        }

        @Override
        public void close() throws SecurityException {
            closed = true;
        }
    }
}
