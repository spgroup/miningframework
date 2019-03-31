package test

import org.junit.Test

public class SameLineTest {

    @Test
    public void modifySameLineTest() {
        assert TestSuite.getModifiedLines('modifySameLine()') == "[12] [] [12] []"
    }

    @Test
    public void addSameLineTest() {
        assert TestSuite.getModifiedLines('addSameLine()') == '[4] [] [4] []'
    }

    @Test
    public void removeSameLineTest() {
        assert TestSuite.getModifiedLines('removeSameLine()') == '[] [9] [] [9]'
    }
    
}