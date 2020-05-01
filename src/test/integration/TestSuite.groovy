package integration

import unit.OutputFileTest

import static com.xlson.groovycsv.CsvParser.parseCsv
import com.google.inject.*
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses
import org.junit.BeforeClass

import app.MiningFramework
import project.*
import arguments.*
import util.FileManager


@RunWith(Suite.class)
@SuiteClasses([SameLineTest.class, MergeConflictFilterTest.class, OutputFileTest.class])
public class TestSuite {
    public static Map<String, String> modifiedLines;

    @BeforeClass
    static void setUp() {

        runFramework('src/test/integration/input.csv', 'src/test/integration/output', new TestModule())

        Map<String, String> outputFiles = new HashMap<String, String>();
        String output = new File('src/test/integration/output/data/results.csv').getText()
        def iterator = parseCsv(output, separator:';')
        for (line in iterator) {
            outputFiles.put(line[3], "${line[4]} ${line[5]} ${line[6]} ${line[7]}")
        }

        modifiedLines = outputFiles

        runFramework('src/test/integration/fileTest/projects.csv', 'src/test/integration/fileTest/output', new FileTestModule())
    }


    static void runFramework(String input, String output, def testModule){
        Arguments args = new Arguments()

        args.setInputPath(input)
        args.setOutputPath(output)
        if (testModule instanceof FileTestModule) {
            args.setUntilDate('21/04/2020')
        }
        args.setKeepProjects()
        delDirectory(args.getOutputPath())

        Injector injector = Guice.createInjector(testModule)
        MiningFramework framework = injector.getInstance(MiningFramework.class)

        framework.setArguments(args)

        FileManager.createOutputFiles(args.getOutputPath(), false)

        ArrayList<Project> projectList = InputParser.getProjectList(args.getInputPath())

        framework.setProjectList(projectList)
        framework.start()

    }

    public static getModifiedLines(String method) {
        this.modifiedLines.get(method)
    }


//    Delete the files of the directory
    public static delDirectory(String dir){
        def mainDir = new File(dir)
        assert mainDir.deleteDir()
        assert !mainDir.exists()
    }

}