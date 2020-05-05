package integration

import org.junit.BeforeClass
import org.junit.Test
import org.junit.Assert
import static com.xlson.groovycsv.CsvParser.parseCsv


public class MergeConflictCollectorTest {

    private static Map<String, Boolean> mergeCommitHasConflict = new HashMap<String, Boolean>()

    @BeforeClass
    static void setup() {
        File mergeConflictsFile = new File('src/test/integration/output/mergeconflicts/results.csv')
        String mergeConflictsFileText = mergeConflictsFile.getText()

        def csvIterator = parseCsv(mergeConflictsFileText, separator:';')
        for (row in csvIterator) {
            mergeCommitHasConflict.put(row[1] as String, row[2] == "true")
        }
    }

    @Test
    void mergeConflictTest () {
        String commitSHA = 'b610225c36e1eb020b9899be907e06b96a19c1c0'
        Assert.assertTrue(mergeCommitHasConflict.get(commitSHA))
    }

    @Test
    void multipleConflict () {
        String commitSHA = 'ea29fc17bdf20b40208ae39f88fd8aa2b2a5322b'
        Assert.assertTrue(mergeCommitHasConflict.get(commitSHA))
    }

    @Test
    void multipleConflictInOneFile() {
        String commitSHA = 'c00294fb7135e5bdcd6f7c3417405aea75b66444'
        Assert.assertTrue(mergeCommitHasConflict.get(commitSHA))
    }

    @Test
    void noMergeConflictTest () {
        String commitSHA = 'ef1a4c9095c04af5eab2e0c54f590c053c48d410'
        Assert.assertFalse(mergeCommitHasConflict.get(commitSHA))
    }
    
}