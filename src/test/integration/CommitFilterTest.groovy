package integration

import org.junit.Assert
import org.junit.Ignore
import org.junit.Test

class CommitFilterTest {

    @Test
    public void withBothRevisionsChangingDifferentMethods() {
        Assert.assertNull(TestSuite.outputCommits.get("1492151c8d8149c4ba2bc026ddcf692de4682ac7"))
    }

    @Test
    public void changeVisibilityAndBodyInDifferentRevisions() {
        Assert.assertNull(TestSuite.outputCommits.get("1492151c8d8149c4ba2bc026ddcf692de4682ac7"))
    }


}
