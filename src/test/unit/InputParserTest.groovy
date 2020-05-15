package unit

import arguments.InputParser
import exception.InvalidArgsException
import org.junit.Test
import project.Project

import static org.junit.jupiter.api.Assertions.assertThrows
import static util.Assert.assertEquals

public class InputParserTest {

    @Test
    public void getProjectListTest() {
        String inputPath = './src/test/assets/projectsTest.csv'
        ArrayList<Project> projectList = InputParser.getProjectList(inputPath)

        assertEquals(projectList.size(), 2)
        assertEquals(projectList.get(0).getPath(), 'https://github.com/jhy/jsoup')
        assertEquals(projectList.get(0).getName(), 'jsoup')
        assertEquals(projectList.get(1).getPath(), 'https://github.com/guilhermejccavalcanti/jFSTMerge')
        assertEquals(projectList.get(1).getName(), 'jFSTMerge')
    }

    @Test
    public void withSeparatorSemicolon() {
        String inputPath = './src/test/assets/separatorSemicolon.csv'
        ArrayList<Project> projectList = new ArrayList<Project>()

        Throwable exception = assertThrows(MissingPropertyException.class, () -> {
            projectList = InputParser.getProjectList(inputPath)
        })

        String expectedMessage = 'path'
        String actualMessage = exception.getMessage()

        assertEquals(expectedMessage, actualMessage)
    }

    @Test
    public void withoutProjectsInputFile() {
        String inputPath = './src/test/assets/withoutProjects.csv'
        ArrayList<Project> projectList = new ArrayList<Project>()

        Throwable exception = assertThrows(InvalidArgsException.class, () -> {
            projectList = InputParser.getProjectList(inputPath)
        })

        String expectedMessage = 'The input file cannot be processed'
        String actualMessage = exception.getMessage()

        assertEquals(expectedMessage, actualMessage)
        assertEquals(projectList.size(), 0)
    }

    @Test
    public void inputFileIsInvalid() {
        String inputPath = './src/test/assets/invalidFile.csv'
        ArrayList<Project> projectList = new ArrayList<Project>()

        Throwable exception = assertThrows(InvalidArgsException.class, () -> {
            projectList = InputParser.getProjectList(inputPath)
        })

        String expectedMessage = 'The input file cannot be processed'
        String actualMessage = exception.getMessage()

        assertEquals(expectedMessage, actualMessage)
        assertEquals(projectList.size(), 0)
    }
}