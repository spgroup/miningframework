package integration

import unit.OutputFileTest

import static com.xlson.groovycsv.CsvParser.parseCsv
import com.google.inject.*
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses
import org.junit.BeforeClass
import org.junit.Assert

import java.io.FileNotFoundException
import app.MiningFramework
import project.*
import arguments.*
import util.FileManager


@RunWith(Suite.class)
@SuiteClasses([SameLineTest.class, TextualConflictFilterTest.class, CommitFilterTest.class, OutputFileTest.class])
public class TestSuite {
    public static Map<String, String> outputMethods;
    public static Map<String, String> outputCommits;

    @BeforeClass
    static void setUp() {

        runFramework('src/test/integration/input.csv', 'src/test/integration/output', new TestModule())
        
        Map<String, String> outputMethods = new HashMap<String, String>();
        Map<String, String> outputCommits = new HashMap<String, String>();

        String output = "";
        try {
            output = new File('src/test/integration/output/data/results.csv').getText();
        } catch (FileNotFoundException e) {
            Assert.fail("Error: Could not find any scenarios on the results.csv file");
        }

        def iterator = parseCsv(output, separator:';')
        for (line in iterator) {
            outputMethods.put(line[3], "${line[4]} ${line[5]} ${line[6]} ${line[7]}")
            outputCommits.put(line[1], line.toString())
        }

        this.outputMethods = outputMethods
        this.outputCommits = outputCommits

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
        this.outputMethods.get(method)
    }


//    Delete the files of the directory
    public static delDirectory(String dir){
        def mainDir = new File(dir)
        assert mainDir.deleteDir()
        assert !mainDir.exists()
    }

}