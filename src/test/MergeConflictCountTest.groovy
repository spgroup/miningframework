package test

@Grab('com.xlson.groovycsv:groovycsv:1.3')

import static com.xlson.groovycsv.CsvParser.parseCsv
import org.junit.Test
import org.junit.BeforeClass

public class MergeConflictCountTest {

    private static Map<String, Integer> mergeConflictsNumber = new HashMap<String, Integer>();  
    private static Map<String, Integer> conflictFilesNumber = new HashMap<String, Integer>();  

    @BeforeClass
    static public void setUp() {
        String statisticsFile = (new File('src/test/output/statistics/results.csv')).getText()

        def iterator = parseCsv(statisticsFile, separator:',')
        for (line in iterator) {
            String commitSHA = line['merge commit']
            conflictFilesNumber.put(commitSHA, line['number of conflicting files'])
            mergeConflictsNumber.put(commitSHA, line['number of merge conflicts'])
        }
    }

    @Test
    public void mergeConflictTest () {
        String commitSHA = 'b610225c36e1eb020b9899be907e06b96a19c1c0'
        assert conflictFilesNumber.get(commitSHA) == '1'
        assert mergeConflictsNumber.get(commitSHA) == '1'
        
    }

    @Test
    public void multipleConflict () {
        String commitSHA = 'ea29fc17bdf20b40208ae39f88fd8aa2b2a5322b'
        assert conflictFilesNumber.get(commitSHA) == '2'
        assert mergeConflictsNumber.get(commitSHA) == '2'
    }

    @Test
    public void multipleConflictInOneFile() {
        String commitSHA = 'c00294fb7135e5bdcd6f7c3417405aea75b66444'
        assert conflictFilesNumber.get(commitSHA) == '1'
        assert mergeConflictsNumber.get(commitSHA) == '2'
    }

    @Test
    public void noMergeConflictTest () {
        String commitSHA = 'ef1a4c9095c04af5eab2e0c54f590c053c48d410'
        assert conflictFilesNumber.get(commitSHA) == '0'
        assert mergeConflictsNumber.get(commitSHA) == '0';
    }
    
}