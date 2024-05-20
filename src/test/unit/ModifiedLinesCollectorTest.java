package unit;

import org.junit.Test;
import project.MergeCommit;
import project.Project;
import services.dataCollectors.modifiedLinesCollector.ModifiedLinesCollector;

import java.util.Set;

import static util.Assert.assertEquals;


public class ModifiedLinesCollectorTest {


    private final ModifiedLinesCollector modifiedLinesCollector = new ModifiedLinesCollector("../../dependencies/");
    Project project = new Project("project", "test_repositories/SSMTeste");

    @Test
    public void getFilesModifiedByBothParentsTest() {
        MergeCommit mergeCommit = new MergeCommit("2199900a069e7bb82654193f001de183e2dfb99b",
                new String[]{"f051b15e85f4d9db61c9c1f87fd2a50e8182081a",
                        "fc789b8bc7d26a4ce9ded885cf68dd9f9567f3bb"},
                "725d6b39edf282e1ab2922b11a66f1c091381ffe");
        Set<String> mutuallyModifiedFiles = this.modifiedLinesCollector
                .getFilesModifiedByBothParents(project, mergeCommit);
        assertEquals(mutuallyModifiedFiles.size(), 1);
    }
}
