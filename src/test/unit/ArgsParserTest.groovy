package unit

import arguments.ArgsParser
import arguments.Arguments
import exception.InvalidArgsException
import org.junit.Test

import static org.junit.jupiter.api.Assertions.assertThrows
import static util.Assert.assertEquals

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

        assertEquals(resultArgs.accessKey, 'token')
        assertEquals(resultArgs.isHelp(), false)
        assertEquals(resultArgs.injector.toString(), 'class injectors.StaticAnalysisConflictsDetectionModule')
        assertEquals(resultArgs.keepProjects, true)
        assertEquals(resultArgs.resultsRemoteRepositoryURL, 'https://github.com/mock-project')
        assertEquals(resultArgs.sinceDate, '01/01/2000')
        assertEquals(resultArgs.numOfThreads, 2)
        assertEquals(resultArgs.untilDate, '01/01/2000')
        assertEquals(resultArgs.inputPath, './projects.csv')
        assertEquals(resultArgs.outputPath, 'SOOTAnalysisOutput')
    }

    @Test
    public void defaultArgument() {
        String[] args = ['./projects.csv',]
        Arguments resultArgs = argsParser.parse(args)

        assertEquals(resultArgs.accessKey, '')
        assertEquals(resultArgs.isHelp(), false)
        assertEquals(resultArgs.injector.toString(), 'class injectors.StaticAnalysisConflictsDetectionModule')
        assertEquals(resultArgs.keepProjects, false)
        assertEquals(resultArgs.resultsRemoteRepositoryURL, '')
        assertEquals(resultArgs.sinceDate, '')
        assertEquals(resultArgs.numOfThreads, 1)
        assertEquals(resultArgs.untilDate, '')
        assertEquals(resultArgs.inputPath, './projects.csv')
        assertEquals(resultArgs.outputPath, 'output')
    }

    @Test
    public void onlyHelpArgument() {
        String[] help = ['--help']
        Arguments resultArgsHelp = argsParser.parse(help)

        assertEquals(resultArgsHelp.isHelp(), true)

        String[] argh = ['-h']
        Arguments resultArgsH = argsParser.parse(argh)

        assertEquals(resultArgsH.isHelp(), true)
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
    public void invalidArgumentPassed() {
        String[] args = ['--invalid', 'invalid', './projects.csv']

        Throwable exception = assertThrows(InvalidArgsException.class, () -> {
            argsParser.parse(args)
        })

        String expectedMessage = "Invalid argument passed"
        String actualMessage = exception.getMessage()

        assertEquals(expectedMessage, actualMessage)
    }

    @Test
    public void manyArgumentPassed() {
        String[] args = ['invalid', 'invalid', './projects.csv']

        Throwable exception = assertThrows(InvalidArgsException.class, () -> {
            argsParser.parse(args)
        })

        String expectedMessage = 'Too many arguments passed'
        String actualMessage = exception.getMessage()

        assertEquals(expectedMessage, actualMessage)
    }

    @Test
    public void invalidInputFileArgument() {
        String[] args = ['./projects.txt']

        Throwable exception = assertThrows(InvalidArgsException.class, () -> {
            argsParser.parse(args)
        })

        String expectedMessage = 'The input must be a csv file'
        String actualMessage = exception.getMessage()

        assertEquals(expectedMessage, actualMessage)
    }

    @Test
    public void inputFileArgumentNotExists() {
        String inputFile = './doesNotExist.csv'
        String[] args = [inputFile]

        Throwable exception = assertThrows(InvalidArgsException.class, () -> {
            argsParser.parse(args)
        })

        String expectedMessage = "Could not find input file: ${inputFile}"
        String actualMessage = exception.getMessage()

        assertEquals(expectedMessage, actualMessage)
    }

    @Test
    public void outputFile() {
        String[] args = ['./projects.csv', 'output/SOOTAnalysisOutput/']
        Arguments resultArgs = argsParser.parse(args)

        assertEquals(resultArgs.outputPath, 'output/SOOTAnalysisOutput')
    }

    @Test
    public void sinceInvalidArgument() {
        String expectedMessage = 'Invalid since date. You must specify it with the format DD/MM/YYYY'

        String[] argsSince = ['--since', '2000/01/01', './projects.csv', 'SOOTAnalysisOutput']

        Throwable argsSinceException = assertThrows(InvalidArgsException.class, () -> {
            argsParser.parse(argsSince)
        })

        assertEquals(expectedMessage, argsSinceException.getMessage())

        String[] argS = ['-s', 'since', './projects.csv']

        Throwable argsSException = assertThrows(InvalidArgsException.class, () -> {
            argsParser.parse(argS)
        })

        assertEquals(expectedMessage, argsSException.getMessage())
    }

    @Test
    public void untilInvalidArgument() {
        String expectedMessage = 'Invalid since date. You must specify it with the format DD/MM/YYYY'

        String[] argsUntil = ['--until', '2000/01/01', './projects.csv', 'SOOTAnalysisOutput']

        Throwable argsUntilException = assertThrows(InvalidArgsException.class, () -> {
            argsParser.parse(argsUntil)
        })

        assertEquals(expectedMessage, argsUntilException.getMessage())

        String[] argU = ['-u', 'until', './projects.csv']

        Throwable argsUException = assertThrows(InvalidArgsException.class, () -> {
            argsParser.parse(argU)
        })

        assertEquals(expectedMessage, argsUException.getMessage())
    }

    @Test
    public void injectorInvalidArgument() {
        String expectedMessage = 'Invalid injector class. be sure it is in your classpath'

        String[] argsInjector = ['--injector', 'app/Main.groovy', './projects.csv', 'SOOTAnalysisOutput']

        Throwable argsInjectorException = assertThrows(InvalidArgsException.class, () -> {
            argsParser.parse(argsInjector)
        })

        assertEquals(expectedMessage, argsInjectorException.getMessage())

        String[] argI = ['-i', 'app/Main.groovy', './projects.csv']

        Throwable argsIException = assertThrows(InvalidArgsException.class, () -> {
            argsParser.parse(argI)
        })

        assertEquals(expectedMessage, argsIException.getMessage())
    }

    @Test
    public void pushInexistentRepositoryArgument() {
        String expectedMessage = 'Inexistent remote git repository.'

        String[] argsPush = ['--push', 'https://github.com/spgroup/invalidrepo', './projects.csv', 'SOOTAnalysisOutput']

        Throwable argsPushException = assertThrows(InvalidArgsException.class, () -> {
            argsParser.parse(argsPush)
        })

        assertEquals(expectedMessage, argsPushException.getMessage())

        String[] argP = ['-p', 'https://github.com/spgroup/invalidrepo', './projects.csv']

        Throwable argsPException = assertThrows(InvalidArgsException.class, () -> {
            argsParser.parse(argP)
        })

        assertEquals(expectedMessage, argsPException.getMessage())
    }

    @Test
    public void pushInvalidURLArgument() {
        String expectedMessage = 'Invalid url.'

        String[] argsPush = ['--push', 'invalidrepourl', './projects.csv', 'SOOTAnalysisOutput']

        Throwable argsPushException = assertThrows(InvalidArgsException.class, () -> {
            argsParser.parse(argsPush)
        })

        assertEquals(expectedMessage, argsPushException.getMessage())

        String[] argP = ['-p', 'invalidrepourl', './projects.csv']

        Throwable argsPException = assertThrows(InvalidArgsException.class, () -> {
            argsParser.parse(argP)
        })

        assertEquals(expectedMessage, argsPException.getMessage())
    }

    @Test
    public void repeatedArgument() {
        String[] accessKey = ['--access-key', 'token', '-a', 'token', './projects.csv']
        Arguments resultArgsAccessKey = argsParser.parse(accessKey)

        String[] help = ['--help', '-h', './projects.csv']
        Arguments resultArgsHelp = argsParser.parse(help)

        String[] injector = ['--injector', 'injectors.StaticAnalysisConflictsDetectionModule', '-i', 'injectors.StaticAnalysisConflictsDetectionModule', './projects.csv',]
        Arguments resultArgsInjector = argsParser.parse(injector)

        String[] keepProjects = ['--keep-projects', '-k', './projects.csv']
        Arguments resultArgsKeepProjects = argsParser.parse(keepProjects)

        String[] push = ['--push', 'https://github.com/mock-project', '-p', 'https://github.com/mock-project', './projects.csv']
        Arguments resultArgsPush = argsParser.parse(push)

        String[] since = ['--since', '01/01/2000', '-s', '01/01/2000', './projects.csv']
        Arguments resultArgsSince = argsParser.parse(since)

        String[] threads = ['--threads', 2, '-t', 2, './projects.csv']
        Arguments resultArgsThreads = argsParser.parse(threads)

        String[] until = ['--until', '01/01/2000', '-u', '01/01/2000', './projects.csv', 'SOOTAnalysisOutput']
        Arguments resultArgsUntil = argsParser.parse(until)

        assertEquals(resultArgsAccessKey.accessKey, 'token')
        assertEquals(resultArgsHelp.isHelp(), true)
        assertEquals(resultArgsInjector.injector.toString(), 'class injectors.StaticAnalysisConflictsDetectionModule')
        assertEquals(resultArgsKeepProjects.keepProjects, true)
        assertEquals(resultArgsPush.resultsRemoteRepositoryURL, 'https://github.com/mock-project')
        assertEquals(resultArgsSince.sinceDate, '01/01/2000')
        assertEquals(resultArgsThreads.numOfThreads, 2)
        assertEquals(resultArgsUntil.untilDate, '01/01/2000')
    }
}