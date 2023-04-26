package org.apache.accumulo.test.functional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.accumulo.core.Constants;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.BatchWriterConfig;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;

public class DeleteRowsTest extends FunctionalTest {

    private static final Logger log = Logger.getLogger(DeleteRowsTest.class);

    private static final int ROWS_PER_TABLET = 10;

    private static final String[] LETTERS = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };

    static final SortedSet<Text> SPLITS = new TreeSet<Text>();

    static {
        for (String alpha : LETTERS) {
            SPLITS.add(new Text(alpha));
        }
    }

    static final List<String> ROWS = new ArrayList<String>(Arrays.asList(LETTERS));

    static {
        ROWS.add("A");
        ROWS.add("{");
    }

    @Override
    public void cleanup() throws Exception {
    }

    @Override
    public Map<String, String> getInitialConfig() {
        return Collections.emptyMap();
    }

    @Override
    public List<TableSetup> getTablesToCreate() {
        return Collections.emptyList();
    }

    @Override
    public void run() throws Exception {
        int i = 0;
        testSplit("test" + i++, "f", "h", "abcdefijklmnopqrstuvwxyz", 260);
        testSplit("test" + i++, "f1", "h", "abcdeff1ijklmnopqrstuvwxyz", 262);
        testSplit("test" + i++, "f", "h1", "abcdefijklmnopqrstuvwxyz", 258);
        testSplit("test" + i++, "f1", "h1", "abcdeff1ijklmnopqrstuvwxyz", 260);
        testSplit("test" + i++, "f", "g", "abcdefhijklmnopqrstuvwxyz", 270);
        testSplit("test" + i++, "f", "f1", "abcdefghijklmnopqrstuvwxyz", 278);
        testSplit("test" + i++, "f1", "g", "abcdeff1hijklmnopqrstuvwxyz", 272);
        testSplit("test" + i++, null, "h", "ijklmnopqrstuvwxyz", 200);
        testSplit("test" + i++, "t", null, "abcdefghijklmnopqrst", 200);
        testSplit("test" + i++, "t0", "t2", "abcdefghijklmnopqrstt0uvwxyz", 278);
        testSplit("test" + i++, null, "A1", "abcdefghijklmnopqrstuvwxyz", 278);
        testSplit("test" + i++, "{1", null, "abcdefghijklmnopqrstuvwxyz{1", 272);
        testSplit("test" + i++, null, null, "", 0);
    }

    private void testSplit(String table, String start, String end, String result, int entries) throws Exception {
        this.getConnector().tableOperations().create(table);
        BatchWriter bw = this.getConnector().createBatchWriter(table, new BatchWriterConfig());
        for (String row : ROWS) {
            for (int j = 0; j < ROWS_PER_TABLET; j++) {
                Mutation m = new Mutation(row + j);
                m.put("cf", "cq", "value");
                bw.addMutation(m);
            }
        }
        bw.flush();
        bw.close();
        this.getConnector().tableOperations().addSplits(table, SPLITS);
        Text startText = start == null ? null : new Text(start);
        Text endText = end == null ? null : new Text(end);
        this.getConnector().tableOperations().deleteRows(table, startText, endText);
        Collection<Text> remainingSplits = this.getConnector().tableOperations().listSplits(table);
        StringBuilder sb = new StringBuilder();
        for (Text split : remainingSplits) sb.append(split.toString());
        assertEquals(result, sb.toString());
        Scanner scanner = this.getConnector().createScanner(table, Constants.NO_AUTHS);
        int count = 0;
        for (Entry<Key, Value> entry : scanner) {
            Text row = entry.getKey().getRow();
            assertTrue((startText == null || row.compareTo(startText) <= 0) || (endText == null || row.compareTo(endText) > 0));
            assertTrue(startText != null || endText != null);
            count++;
        }
        log.info("Finished table " + table);
        assertEquals(entries, count);
    }

    private void assertEquals(int expected, int value) {
        if (expected != value)
            throw new RuntimeException("Test failed, expected " + expected + " != " + value);
    }

    private void assertTrue(boolean b) {
        if (!b)
            throw new RuntimeException("test failed, false value");
    }

    private void assertEquals(String expected, String value) {
        if (!expected.equals(value))
            throw new RuntimeException("expected " + expected + " != " + value);
    }
}