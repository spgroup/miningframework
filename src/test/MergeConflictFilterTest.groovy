package test

@Grab('com.xlson.groovycsv:groovycsv:1.3')

import static com.xlson.groovycsv.CsvParser.parseCsv
import org.junit.Test
import org.junit.BeforeClass
import static test.Assert.assertEquals


public class MergeConflictFilterTest {


    @Test
    public void mergeConflictTest () {
        String commitSHA = 'b610225c36e1eb020b9899be907e06b96a19c1c0'
        assertEquals(TestSuite.modifiedLines.get(commitSHA), null)
        
    }

    @Test
    public void multipleConflict () {
        String commitSHA = 'ea29fc17bdf20b40208ae39f88fd8aa2b2a5322b'
        assertEquals(TestSuite.modifiedLines.get(commitSHA), null)
    }

    @Test
    public void multipleConflictInOneFile() {
        String commitSHA = 'c00294fb7135e5bdcd6f7c3417405aea75b66444'
        assertEquals(TestSuite.modifiedLines.get(commitSHA), null)
    }

    @Test
    public void noMergeConflictTest () {
        String commitSHA = 'ef1a4c9095c04af5eab2e0c54f590c053c48d410'
        assertEquals(TestSuite.modifiedLines.get(commitSHA), null)
    }
    
}