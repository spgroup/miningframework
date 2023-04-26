package org.apache.accumulo.test.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.accumulo.core.client.Accumulo;
import org.apache.accumulo.core.client.AccumuloClient;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.harness.AccumuloClusterHarness;
import org.apache.hadoop.io.Text;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Iterators;

public class DeleteRowsIT extends AccumuloClusterHarness {

    @Override
    protected int defaultTimeoutSeconds() {
        return 5 * 60;
    }

    private static final Logger log = LoggerFactory.getLogger(DeleteRowsIT.class);

    private static final int ROWS_PER_TABLET = 10;

    private static final String[] LETTERS = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };

    static final SortedSet<Text> SPLITS = new TreeSet<>();

    static {
        for (String alpha : LETTERS) {
            SPLITS.add(new Text(alpha));
        }
    }

    static final List<String> ROWS = new ArrayList<>(Arrays.asList(LETTERS));

    static {
        ROWS.add("A");
        ROWS.add("{");
    }

    @Test
    public void testDeleteAllRows() throws Exception {
        try (AccumuloClient c = Accumulo.newClient().from(getClientProps()).build()) {
            String[] tableNames = this.getUniqueNames(20);
            for (String tableName : tableNames) {
                c.tableOperations().create(tableName);
                c.tableOperations().deleteRows(tableName, null, null);
                try (Scanner scanner = c.createScanner(tableName, Authorizations.EMPTY)) {
                    assertEquals(0, Iterators.size(scanner.iterator()));
                }
            }
        }
    }

    @Test
    public void testManyRows() throws Exception {
        try (AccumuloClient c = Accumulo.newClient().from(getClientProps()).build()) {
            int i = 0;
            String tableName = getUniqueNames(1)[0];
            testSplit(c, tableName + i++, "f", "h", "abcdefijklmnopqrstuvwxyz", 260);
            testSplit(c, tableName + i++, "f1", "h", "abcdeff1ijklmnopqrstuvwxyz", 262);
            testSplit(c, tableName + i++, "f", "h1", "abcdefijklmnopqrstuvwxyz", 258);
            testSplit(c, tableName + i++, "f1", "h1", "abcdeff1ijklmnopqrstuvwxyz", 260);
            testSplit(c, tableName + i++, "f", "g", "abcdefhijklmnopqrstuvwxyz", 270);
            testSplit(c, tableName + i++, "f", "f1", "abcdefghijklmnopqrstuvwxyz", 278);
            testSplit(c, tableName + i++, "f1", "g", "abcdeff1hijklmnopqrstuvwxyz", 272);
            testSplit(c, tableName + i++, null, "h", "ijklmnopqrstuvwxyz", 200);
            testSplit(c, tableName + i++, "t", null, "abcdefghijklmnopqrst", 200);
            testSplit(c, tableName + i++, "t0", "t2", "abcdefghijklmnopqrstt0uvwxyz", 278);
            testSplit(c, tableName + i++, null, "A1", "abcdefghijklmnopqrstuvwxyz", 278);
            testSplit(c, tableName + i++, "{1", null, "abcdefghijklmnopqrstuvwxyz{1", 272);
            testSplit(c, tableName + i++, null, null, "", 0);
        }
    }

    private void testSplit(AccumuloClient c, String table, String start, String end, String result, int entries) throws Exception {
        c.tableOperations().create(table);
        try (BatchWriter bw = c.createBatchWriter(table)) {
            for (String row : ROWS) {
                for (int j = 0; j < ROWS_PER_TABLET; j++) {
                    Mutation m = new Mutation(row + j);
                    m.put("cf", "cq", "value");
                    bw.addMutation(m);
                }
            }
            bw.flush();
        }
        c.tableOperations().addSplits(table, SPLITS);
        Text startText = start == null ? null : new Text(start);
        Text endText = end == null ? null : new Text(end);
        c.tableOperations().deleteRows(table, startText, endText);
        Collection<Text> remainingSplits = c.tableOperations().listSplits(table);
        StringBuilder sb = new StringBuilder();
        for (Text split : remainingSplits) sb.append(split);
        assertEquals(result, sb.toString());
        try (Scanner scanner = c.createScanner(table, Authorizations.EMPTY)) {
            int count = 0;
            for (Entry<Key, Value> entry : scanner) {
                Text row = entry.getKey().getRow();
                assertTrue((startText == null || row.compareTo(startText) <= 0) || (endText == null || row.compareTo(endText) > 0));
                assertTrue(startText != null || endText != null);
                count++;
            }
            log.info("Finished table {}", table);
            assertEquals(entries, count);
        }
    }
}
