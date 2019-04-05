package test

import org.junit.Test

public class DeletionTest {

    @Test
    public void multipleCommitsTest() {
        assert TestSuite.getModifiedLines('multipleCommits()') == "[34, 35] [] [32] []"
    }

    @Test
    public void deletionBothAndAdditionTest() {
        assert TestSuite.getModifiedLines('deletionBothAndAddition()') == '[] [[11, 11]] [12] [[11, 11]]'
    }

    @Test
    public void deletionBothAndAdditionBothTest() {
        assert TestSuite.getModifiedLines('deletionBothAndAdditionBoth()') == '[17] [] [20] []'
    }

    @Test
    public void deletionAndAdditionBothTest() {
        assert TestSuite.getModifiedLines('deletionAndAdditionBoth()') == '[25] [] [27] []'
    }

    @Test
    public void deletionSingleAndAdditionTest() {
        assert TestSuite.getModifiedLines('deletionSingleAndAddition()') == '[] [[5, 5]] [6] []'
    }
    
}