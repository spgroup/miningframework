package test

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

    @Test
    public void removeSameLineTest() {
        assertEquals(TestSuite.getModifiedLines('removeSameLine()'), '[] [[8, 9]] [] [[8, 9]]')
    }
    
}