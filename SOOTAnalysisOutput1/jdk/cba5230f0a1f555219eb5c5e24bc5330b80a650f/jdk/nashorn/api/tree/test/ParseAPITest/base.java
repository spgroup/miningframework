package jdk.nashorn.api.tree.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import jdk.nashorn.api.tree.Parser;
import jdk.nashorn.api.tree.SimpleTreeVisitorES5_1;
import jdk.nashorn.api.tree.Tree;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ParseAPITest {

    private static final boolean VERBOSE = Boolean.valueOf(System.getProperty("parserapitest.verbose"));

    private static final boolean TEST262 = Boolean.valueOf(System.getProperty("parserapitest.test262"));

    private static final String TEST_BASIC_DIR = System.getProperty("test.basic.dir");

    private static final String TEST_MAPTESTS_DIR = System.getProperty("test.maptests.dir");

    private static final String TEST_SANDBOX_DIR = System.getProperty("test.sandbox.dir");

    private static final String TEST_TRUSTED_DIR = System.getProperty("test.trusted.dir");

    private static final String TEST262_SUITE_DIR = System.getProperty("test262.suite.dir");

    interface TestFilter {

        public boolean exclude(File file, String content);
    }

    private void log(String msg) {
        org.testng.Reporter.log(msg, true);
    }

    private static final String[] options = new String[] { "-scripting", "--const-as-var" };

    @Test
    public void parseAllTests() {
        if (TEST262) {
            parseTestSet(TEST262_SUITE_DIR, new TestFilter() {

                @Override
                public boolean exclude(final File file, final String content) {
                    return content.contains("@negative");
                }
            });
        }
        parseTestSet(TEST_BASIC_DIR, new TestFilter() {

            @Override
            public boolean exclude(final File file, final String content) {
                return file.getParentFile().getName().equals("es6");
            }
        });
        parseTestSet(TEST_MAPTESTS_DIR, null);
        parseTestSet(TEST_SANDBOX_DIR, null);
        parseTestSet(TEST_TRUSTED_DIR, null);
    }

    private void parseTestSet(final String testSet, final TestFilter filter) {
        passed = 0;
        failed = 0;
        skipped = 0;
        final File testSetDir = new File(testSet);
        if (!testSetDir.isDirectory()) {
            log("WARNING: " + testSetDir + " not found or not a directory");
            return;
        }
        log(testSetDir.getAbsolutePath());
        parseJSDirectory(testSetDir, filter);
        log(testSet + " parse API done!");
        log("parse API ok: " + passed);
        log("parse API failed: " + failed);
        log("parse API skipped: " + skipped);
        if (failed != 0) {
            Assert.fail(failed + " tests failed to parse in " + testSetDir.getAbsolutePath());
        }
    }

    private int passed;

    private int failed;

    private int skipped;

    private void parseJSDirectory(final File dir, final TestFilter filter) {
        for (final File f : dir.listFiles()) {
            if (f.isDirectory()) {
                parseJSDirectory(f, filter);
            } else if (f.getName().endsWith(".js")) {
                parseJSFile(f, filter);
            }
        }
    }

    private void parseJSFile(final File file, final TestFilter filter) {
        if (VERBOSE) {
            log("Begin parsing " + file.getAbsolutePath());
        }
        try {
            final char[] buffer = readFully(file);
            final String content = new String(buffer);
            boolean excluded = false;
            if (filter != null) {
                excluded = filter.exclude(file, content);
            }
            if (excluded) {
                if (VERBOSE) {
                    log("Skipping " + file.getAbsolutePath());
                }
                skipped++;
                return;
            }
            final Parser parser = Parser.create(options);
            final Tree tree = parser.parse(file.getAbsolutePath(), content, null);
            tree.accept(new SimpleTreeVisitorES5_1<Void, Void>(), null);
            passed++;
        } catch (final Throwable exp) {
            log("Parse API failed: " + file.getAbsolutePath() + " : " + exp);
            exp.printStackTrace(System.out);
            failed++;
        }
        if (VERBOSE) {
            log("Done parsing via parser API " + file.getAbsolutePath());
        }
    }

    private static char[] byteToCharArray(final byte[] bytes) {
        Charset cs = StandardCharsets.UTF_8;
        int start = 0;
        if (bytes.length > 1 && bytes[0] == (byte) 0xFE && bytes[1] == (byte) 0xFF) {
            start = 2;
            cs = StandardCharsets.UTF_16BE;
        } else if (bytes.length > 1 && bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFE) {
            start = 2;
            cs = StandardCharsets.UTF_16LE;
        } else if (bytes.length > 2 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
            start = 3;
            cs = StandardCharsets.UTF_8;
        } else if (bytes.length > 3 && bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xFE && bytes[2] == 0 && bytes[3] == 0) {
            start = 4;
            cs = Charset.forName("UTF-32LE");
        } else if (bytes.length > 3 && bytes[0] == 0 && bytes[1] == 0 && bytes[2] == (byte) 0xFE && bytes[3] == (byte) 0xFF) {
            start = 4;
            cs = Charset.forName("UTF-32BE");
        }
        return new String(bytes, start, bytes.length - start, cs).toCharArray();
    }

    private static char[] readFully(final File file) throws IOException {
        final byte[] buf = Files.readAllBytes(file.toPath());
        return byteToCharArray(buf);
    }
}
