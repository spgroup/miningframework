package test

@Grab('com.google.inject:guice:4.2.2')
@Grab('com.xlson.groovycsv:groovycsv:1.3')

import static com.xlson.groovycsv.CsvParser.parseCsv
import com.google.inject.*
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses
import org.junit.BeforeClass

import main.app.MiningFramework
import main.project.*
import main.arguments.Arguments
import main.util.FileManager
import test.*

@RunWith(Suite.class)
@SuiteClasses([DeletionTest.class, SameLineTest.class, MergeConflictCountTest.class])
public class TestSuite {
    public static Map<String, String> modifiedLines;

    @BeforeClass
    static void setUp() {

        Arguments args = new Arguments()
        
        args.setInputPath('src/test/input.csv')
        args.setOutputPath('src/test/output')

        Injector injector = Guice.createInjector(new TestModule())
        MiningFramework framework = injector.getInstance(MiningFramework.class)

        framework.setArguments(args)

        FileManager.createOutputFiles(args.getOutputPath(), false)

        ArrayList<Project> projectList = Project.getProjectList()
        
        framework.setProjectList(projectList)
        framework.start()


        Map<String, String> outputFiles = new HashMap<String, String>();
        String output = new File('src/test/output/data/results.csv').getText()
        def iterator = parseCsv(output, separator:';')
        for (line in iterator) {
            outputFiles.put(line[3], "${line[4]} ${line[5]} ${line[6]} ${line[7]}")
        }

        modifiedLines = outputFiles
    }

    public static getModifiedLines(String method) {
        this.modifiedLines.get(method)
    }
}