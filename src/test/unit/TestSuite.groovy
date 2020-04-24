package unit

import app.MiningFramework
import arguments.Arguments
import arguments.InputParser
import com.google.inject.Guice
import com.google.inject.Injector
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses
import project.Project
import unit.fileTest.TestModule
import util.FileManager

@RunWith(Suite.class)
@SuiteClasses([TextualDiffParserTest.class, DiffJParserTest.class, MethodModifiedLinesMatcherTest.class, OutputFileTest.class])
public class TestSuite {

    @BeforeClass
    static void setUp() {

        Arguments args = new Arguments()

        args.setUntilDate('21/04/2020')
        args.setInputPath('src/test/unit/fileTest/projects.csv')
        args.setOutputPath('src/test/unit/fileTest/output')
        delDirectory(args.getOutputPath())

        Injector injector = Guice.createInjector(new TestModule())
        MiningFramework framework = injector.getInstance(MiningFramework.class)

        framework.setArguments(args)

        FileManager.createOutputFiles(args.getOutputPath(), false)

        ArrayList<Project> projectList = InputParser.getProjectList(args.getInputPath())

        framework.setProjectList(projectList)
        framework.start()

    }

//    Delete the files of the directory
    public static delDirectory(String dir){
        def mainDir = new File(dir)
        assert mainDir.deleteDir()
        assert !mainDir.exists()
    }

}