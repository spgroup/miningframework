package unit

import arguments.ArgsParser
import arguments.Arguments
import exception.InvalidArgsException
import org.junit.Test

import static util.Assert.*

public class ArgsParserTest {

    ArgsParser argsParser = new ArgsParser()

    @Test
    public void withManyArgument() {
        String[] args = ['-a', 'token',
                         '-i', 'injectors.StaticAnalysisConflictsDetectionModule',
                         '-k',
                         '-p', 'https://github.com/mock-project',
                         '-s', '01/01/2000',
                         '-u', '01/01/2000',
                         '-t', '2',
                         './projects.csv', 'SOOTAnalysisOutput']
        Arguments resultArgs = argsParser.parse(args)

        assertEquals(resultArgs.accessKey,  'token')
        assertEquals(resultArgs.isHelp(),  false)
        assertEquals(resultArgs.injector.toString(),  'class injectors.StaticAnalysisConflictsDetectionModule')
        assertEquals(resultArgs.keepProjects,  true)
        assertEquals(resultArgs.resultsRemoteRepositoryURL,  'https://github.com/mock-project')
        assertEquals(resultArgs.sinceDate,  '01/01/2000')
        assertEquals(resultArgs.numOfThreads,  2)
        assertEquals(resultArgs.untilDate,  '01/01/2000')
        assertEquals(resultArgs.inputPath,  './projects.csv')
        assertEquals(resultArgs.outputPath,  'SOOTAnalysisOutput')
    }

    @Test
    public void defaultArgument() {
        String[] args = ['./projects.csv',]
        Arguments resultArgs = argsParser.parse(args)

        assertEquals(resultArgs.accessKey,  '')
        assertEquals(resultArgs.isHelp(),  false)
        assertEquals(resultArgs.injector.toString(),  'class injectors.StaticAnalysisConflictsDetectionModule')
        assertEquals(resultArgs.keepProjects,  false)
        assertEquals(resultArgs.resultsRemoteRepositoryURL,  '')
        assertEquals(resultArgs.sinceDate,  '')
        assertEquals(resultArgs.numOfThreads,  1)
        assertEquals(resultArgs.untilDate,  '')
        assertEquals(resultArgs.inputPath,  './projects.csv')
        assertEquals(resultArgs.outputPath,  'output')
    }

    @Test
    public void onlyHelpArgument() {
        String[] help = ['--help']
        Arguments resultArgsHelp = argsParser.parse(help)

        assertEquals(resultArgsHelp.isHelp(),  true)

        String[] argh = ['-h']
        Arguments resultArgsH = argsParser.parse(argh)

        assertEquals(resultArgsH.isHelp(),  true)
    }

    @Test
    public void withoutArgs() {
        String[] args = []
        Arguments resultArgs = argsParser.parse(args)

        Arguments argsDefault = new Arguments()
        argsDefault.setHelp()

        assertEquals(argsDefault.getProperties(), resultArgs.getProperties())
    }

    @Test
    public void invalidArgument() {
        String[] args = ['--invalid', 'invalid','./projects.csv']

        try {
            argsParser.parse(args)
        } catch (InvalidArgsException e) {
            assertEquals(e.getMessage(), 'Too many arguments passed');
        }
    }

    @Test
    public void invalidInputFileArgument() {
        String[] args = ['./projects.txt']

        try {
            argsParser.parse(args)
        } catch (InvalidArgsException e) {
            assertEquals(e.getMessage(), 'The input must be a csv file');
        }

    }

    @Test
    public void inputFileArgumentNotExists() {
        String inputFile = './doesNotExist.csv'
        String[] args = [inputFile]

        try {
            argsParser.parse(args)
        } catch (InvalidArgsException e) {
            assertEquals(e.getMessage(), "Could not find input file: ${inputFile}");
        }
    }

    @Test
    public void outputFile() {
        String[] args = ['./projects.csv', 'output/SOOTAnalysisOutput/']
        Arguments resultArgs = argsParser.parse(args)

        assertEquals(resultArgs.outputPath, 'output/SOOTAnalysisOutput')
    }

    @Test
    public void sinceInvalidArgument() {
        String[] argsSince = ['--since', '2000/01/01', './projects.csv', 'SOOTAnalysisOutput']

        try {
            argsParser.parse(argsSince)
        } catch (InvalidArgsException e) {
            assertEquals(e.getMessage(), 'Invalid since date. You must specify it with the format DD/MM/YYYY');
        }

        String[] argS = ['-s', 'since','./projects.csv']

        try {
            argsParser.parse(argS)
        } catch (InvalidArgsException e) {
            assertEquals(e.getMessage(), 'Invalid since date. You must specify it with the format DD/MM/YYYY');
        }
    }

    @Test
    public void untilInvalidArgument() {
        String[] argsUntil = ['--until', '2000/01/01', './projects.csv', 'SOOTAnalysisOutput']

        try {
            argsParser.parse(argsUntil)
        } catch (InvalidArgsException e) {
            assertEquals(e.getMessage(), 'Invalid since date. You must specify it with the format DD/MM/YYYY');
        }

        String[] argU = ['-u', 'until','./projects.csv']

        try {
            argsParser.parse(argU)
        } catch (InvalidArgsException e) {
            assertEquals(e.getMessage(), 'Invalid since date. You must specify it with the format DD/MM/YYYY');
        }
    }

    @Test
    public void injectorInvalidArgument() {
        String[] argsInjector = ['--injector', 'app/Main.groovy', './projects.csv', 'SOOTAnalysisOutput']

        try {
            argsParser.parse(argsInjector)
        } catch (InvalidArgsException e) {
            assertEquals(e.getMessage(), 'Invalid injector class. be sure it is in your classpath');
        }

        String[] argI = ['-i', 'app/Main.groovy', './projects.csv']

        try {
            argsParser.parse(argI)
        } catch (InvalidArgsException e) {
            assertEquals(e.getMessage(), 'Invalid injector class. be sure it is in your classpath');
        }
    }

    @Test
    public void pushInexistentRepositoryArgument() {
        String[] argsPush = ['--push', 'https://github.com/spgroup/invalidrepo', './projects.csv', 'SOOTAnalysisOutput']

        try {
            argsParser.parse(argsPush)
        } catch (InvalidArgsException e) {
            assertEquals(e.getMessage(), 'Inexistent remote git repository.');
        }

        String[] argP = ['-p', 'https://github.com/spgroup/invalidrepo','./projects.csv']

        try {
            argsParser.parse(argP)
        } catch (InvalidArgsException e) {
            assertEquals(e.getMessage(), 'Inexistent remote git repository.');
        }
    }

    @Test
    public void pushInvalidURLArgument() {
        String[] argsPush = ['--push', 'invalidrepourl', './projects.csv', 'SOOTAnalysisOutput']

        try {
            argsParser.parse(argsPush)
        } catch (InvalidArgsException e) {
            assertEquals(e.getMessage(), 'Invalid url.');
        }

        String[] argP = ['-p', 'invalidrepourl','./projects.csv']

        try {
            argsParser.parse(argP)
        } catch (InvalidArgsException e) {
            assertEquals(e.getMessage(), 'Invalid url.');
        }
    }

    @Test
    public void repeatedArgument() {
        String[] accessKey = ['--access-key', 'token', '-a', 'token',  './projects.csv']
        Arguments resultArgsAccessKey =  argsParser.parse(accessKey)

        String[] help = ['--help', '-h', './projects.csv']
        Arguments resultArgsHelp =  argsParser.parse(help)

        String[] injector = ['--injector', 'injectors.StaticAnalysisConflictsDetectionModule', '-i', 'injectors.StaticAnalysisConflictsDetectionModule',  './projects.csv',]
        Arguments resultArgsInjector =  argsParser.parse(injector)

        String[] keepProjects = ['--keep-projects', '-k',  './projects.csv']
        Arguments resultArgsKeepProjects =  argsParser.parse(keepProjects)

        String[] push = ['--push', 'https://github.com/mock-project', '-p', 'https://github.com/mock-project',  './projects.csv']
        Arguments resultArgsPush =  argsParser.parse(push)

        String[] since = ['--since', '01/01/2000', '-s', '01/01/2000',  './projects.csv']
        Arguments resultArgsSince =  argsParser.parse(since)

        String[] threads = ['--threads', 2, '-t', 2,  './projects.csv']
        Arguments resultArgsThreads =  argsParser.parse(threads)

        String[] until = ['--until', '01/01/2000', '-u', '01/01/2000',  './projects.csv', 'SOOTAnalysisOutput']
        Arguments resultArgsUntil =  argsParser.parse(until)

        assertEquals(resultArgsAccessKey.accessKey,  'token')
        assertEquals(resultArgsHelp.isHelp(),  true)
        assertEquals(resultArgsInjector.injector.toString(),  'class injectors.StaticAnalysisConflictsDetectionModule')
        assertEquals(resultArgsKeepProjects.keepProjects,  true)
        assertEquals(resultArgsPush.resultsRemoteRepositoryURL,  'https://github.com/mock-project')
        assertEquals(resultArgsSince.sinceDate,  '01/01/2000')
        assertEquals(resultArgsThreads.numOfThreads,  2)
        assertEquals(resultArgsUntil.untilDate,  '01/01/2000')
    }
}