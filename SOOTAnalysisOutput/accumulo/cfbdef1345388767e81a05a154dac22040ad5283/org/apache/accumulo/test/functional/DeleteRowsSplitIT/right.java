package org.apache.accumulo.test.functional;

import static com.google.common.base.Charsets.UTF_8;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.BatchWriterConfig;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.core.util.UtilWaitThread;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;
import org.junit.Test;

public class DeleteRowsSplitIT extends SimpleMacIT {

    @Override
    protected int defaultTimeoutSeconds() {
        return 4 * 60;
    }

    private static final Logger log = Logger.getLogger(DeleteRowsSplitIT.class);

    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";

    static final SortedSet<Text> SPLITS = new TreeSet<Text>();

    static final List<String> ROWS = new ArrayList<String>();

    static {
        for (byte b : LETTERS.getBytes(UTF_8)) {
            SPLITS.add(new Text(new byte[] { b }));
            ROWS.add(new String(new byte[] { b }, UTF_8));
        }
    }

    @Test
    public void run() throws Exception {
        final String tableName = getUniqueNames(1)[0];
        for (int test = 0; test < 10; test++) {
            log.info("Test " + test);
            getConnector().tableOperations().create(tableName);
            fillTable(tableName);
            final Text start = new Text();
            final Text end = new Text();
            generateRandomRange(start, end);
            final boolean[] fail = { false };
            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        final SortedSet<Text> afterEnd = SPLITS.tailSet(new Text(end.toString() + "\0"));
                        getConnector().tableOperations().addSplits(tableName, afterEnd);
                    } catch (Exception ex) {
                        log.error(ex, ex);
                        synchronized (fail) {
                            fail[0] = true;
                        }
                    }
                }
            };
            t.start();
            UtilWaitThread.sleep(test * 2);
            getConnector().tableOperations().deleteRows(tableName, start, end);
            t.join();
            synchronized (fail) {
                assertTrue(!fail[0]);
            }
            Scanner scanner = getConnector().createScanner(tableName, Authorizations.EMPTY);
            for (Entry<Key, Value> entry : scanner) {
                Text row = entry.getKey().getRow();
                assertTrue(row.compareTo(start) <= 0 || row.compareTo(end) > 0);
            }
            getConnector().tableOperations().delete(tableName);
        }
    }

    private void generateRandomRange(Text start, Text end) {
        List<String> bunch = new ArrayList<String>(ROWS);
        Collections.shuffle(bunch);
        if (bunch.get(0).compareTo((bunch.get(1))) < 0) {
            start.set(bunch.get(0));
            end.set(bunch.get(1));
        } else {
            start.set(bunch.get(1));
            end.set(bunch.get(0));
        }
    }

    private void fillTable(String table) throws Exception {
        BatchWriter bw = getConnector().createBatchWriter(table, new BatchWriterConfig());
        for (String row : ROWS) {
            Mutation m = new Mutation(row);
            m.put("cf", "cq", "value");
            bw.addMutation(m);
        }
        bw.close();
    }
}
