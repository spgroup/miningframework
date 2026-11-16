package unit

import org.apache.commons.io.FileUtils
import org.junit.Test
import static util.Assert.assertEquals
import java.nio.charset.StandardCharsets

public class OutputFileTest {

    @Test
    public void compareResults(){
        String expected = FileUtils.readFileToString(
                new File("src/test/integration/fileTest/compare/data/result.csv"),
                StandardCharsets.UTF_8
        ).replace("\r\n", "\n");

        String actual = FileUtils.readFileToString(
                new File("src/test/integration/fileTest/output/data/results.csv"),
                StandardCharsets.UTF_8
        ).replace("\r\n", "\n");

        assertEquals(expected, actual);
    }

    @Test
    public void compareSootResults(){
        String expected = FileUtils.readFileToString(
                new File("src/test/integration/fileTest/compare/data/soot-results.csv"),
                StandardCharsets.UTF_8
        ).replace("\r\n", "\n");

        String actual = FileUtils.readFileToString(
                new File("src/test/integration/fileTest/output/data/soot-results.csv"),
                StandardCharsets.UTF_8
        ).replace("\r\n", "\n");

        assertEquals(expected, actual);
    }

    @Test
    public void compareStatisticsResults() throws Exception {
        String expected = FileUtils.readFileToString(
                new File("src/test/integration/fileTest/compare/statistics/result.csv"),
                StandardCharsets.UTF_8
        ).replace("\r\n", "\n");

        String actual = FileUtils.readFileToString(
                new File("src/test/integration/fileTest/output/statistics/results.csv"),
                StandardCharsets.UTF_8
        ).replace("\r\n", "\n");

        assertEquals(expected, actual);
    }

}