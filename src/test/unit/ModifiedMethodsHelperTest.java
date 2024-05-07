package unit;

import org.junit.Test;
import project.Project;
import services.dataCollectors.modifiedLinesCollector.ModifiedMethod;
import services.dataCollectors.modifiedLinesCollector.ModifiedMethodsHelper;

import java.util.Set;

import static util.Assert.assertEquals;


public class ModifiedMethodsHelperTest {


    private final ModifiedMethodsHelper modifiedMethodsHelper = new ModifiedMethodsHelper("diffj.jar");
    Project project = new Project("project", "D:/Documents/development/UFPE/SSM/Teste/");
    @Test
    public void getAllModifiedMethodsTest() {
       Set<ModifiedMethod> allModifiedMethods = this.modifiedMethodsHelper
               .getAllModifiedMethods(project,
                       "src/main/java/org/example/Main.java",
                       "2199900a069e7bb82654193f001de183e2dfb99b",
                       "725d6b39edf282e1ab2922b11a66f1c091381ffe");
       System.out.println(allModifiedMethods);
        assertEquals(allModifiedMethods.size(),4);
    }
}
