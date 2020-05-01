package integration

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
@SuiteClasses([SameLineTest.class, MergeConflictCollectorTest.class, CommitFilterTest.class])
public class TestSuite {
    public static Map<String, String> outputMethods;
    public static Map<String, String> outputCommits;

    @BeforeClass
    static void setUp() {

        Arguments args = new Arguments()
        
        args.setInputPath('src/test/integration/input.csv')
        args.setOutputPath('src/test/integration/output')
        args.setKeepProjects()

        Injector injector = Guice.createInjector(new TestModule())
        MiningFramework framework = injector.getInstance(MiningFramework.class)

        framework.setArguments(args)

        FileManager.createOutputFiles(args.getOutputPath(), false)

        ArrayList<Project> projectList = InputParser.getProjectList(args.getInputPath())
        
        framework.setProjectList(projectList)
        framework.start()
        
        Map<String, String> outputMethods = new HashMap<String, String>();
        Map<String, String> outputCommits = new HashMap<String, String>();
        String output = new File('src/test/integration/output/data/results.csv').getText()
        def iterator = parseCsv(output, separator:';')
        for (line in iterator) {
            outputMethods.put(line[3], "${line[4]} ${line[5]} ${line[6]} ${line[7]}")
            outputCommits.put(line[1], line.toString())
        }

        this.outputMethods = outputMethods
        this.outputCommits = outputCommits
    }

    public static getModifiedLines(String method) {
        this.outputMethods.get(method)
    }
}