package unit

import org.apache.commons.io.FileUtils
import org.junit.Test
import static util.Assert.assertEquals


public class OutputFileTest {

    @Test
    public void compareResults(){
        assertEquals(FileUtils.readFileToString(new File('src/test/integration/fileTest/compare/data/results.csv'), "utf-8"),
                FileUtils.readFileToString(new File('src/test/integration/fileTest/output/data/results.csv'), "utf-8"))
    }

    @Test
    public void compareSootResults(){
        assertEquals(FileUtils.readFileToString(new File('src/test/integration/fileTest/compare/data/soot-results.csv'), "utf-8"),
                FileUtils.readFileToString(new File('src/test/integration/fileTest/output/data/soot-results.csv'), "utf-8"))
    }


    @Test
    public void compareMergeResults(){
        assertEquals(FileUtils.readFileToString(new File('src/test/integration/fileTest/compare/mergeconflicts/results.csv'), "utf-8"),
                FileUtils.readFileToString(new File('src/test/integration/fileTest/output/mergeconflicts/results.csv'), "utf-8"))
    }


    @Test
    public void compareStatisticsResults(){
        assertEquals(FileUtils.readFileToString(new File('src/test/integration/fileTest/compare/statistics/results.csv'), "utf-8"),
                FileUtils.readFileToString(new File('src/test/integration/fileTest/output/statistics/results.csv'), "utf-8"))
    }

}