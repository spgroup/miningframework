package integration

import org.junit.Assert
import org.junit.Test

class CommitFilterTest {

    @Test
    public void withBothRevisionsChangingTheSameMethodBody() {
        Assert.assertNotNull(TestSuite.outputCommits.get("9743e3eac3d3f53fd7b0793c1cbbeee34047bf79"))
    }

    @Test
    public void withBothRevisionsChangingDifferentMethods() {
        Assert.assertNull(TestSuite.outputCommits.get("1492151c8d8149c4ba2bc026ddcf692de4682ac7"))
    }

    @Test
    public void changeVisibilityAndBodyInDifferentRevisions() {
        Assert.assertNull(TestSuite.outputCommits.get("1492151c8d8149c4ba2bc026ddcf692de4682ac7"))
    }


}
