package test.integration

import org.junit.Test
import static test.Assert.assertEquals

public class SameLineTest {

    @Test
    public void modifySameLineTest() {
        assertEquals(TestSuite.getModifiedLines('modifySameLine()'), "[12] [] [12] []")
    }

    @Test
    public void addSameLineTest() {
        assertEquals(TestSuite.getModifiedLines('addSameLine()'), '[4] [] [4] []')
    }

}