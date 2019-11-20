package services.S3MHandlersAnalysis.util

import main.app.MiningFramework
import main.project.MergeCommit
import main.project.Project
import main.util.ProcessRunner

import java.nio.file.Path
import java.nio.file.Paths

final class Utils {

    /**
     * Runs a git command, waiting for it to finish.
     * @param repositoryPath
     * @param arguments
     */
    static void runGitCommand(Path repositoryPath, String... arguments) {
        Process gitCommand = ProcessRunner.startProcess(buildGitCommand(repositoryPath, arguments))
        gitCommand.getInputStream().eachLine {
        }
        gitCommand.waitFor()
    }

    private static ProcessBuilder buildGitCommand(Path repositoryPath, String... arguments) {
        ProcessBuilder gitCommand = ProcessRunner.buildProcess(repositoryPath.toString(), 'git')
        gitCommand.command().addAll(arguments.toList())
        return gitCommand
    }

    /**
     * Equivalent to Paths.get(MiningFramework.arguments.getOutputPath())
     * @return a path to the output path given as argument
     */
    static Path getOutputPath() {
        return Paths.get(MiningFramework.arguments.getOutputPath())
    }

    /**
     * @param project
     * @param mergeCommit
     * @return the output path resolved in the project/merge commit directory
     */
    static Path commitFilesPath(Project project, MergeCommit mergeCommit) {
        return getOutputPath().resolve(project.getName()).resolve(mergeCommit.getSHA())
    }

    /**
     * @param list
     * @param separator
     * @return a concatenation of all the string representation of the elements of the list, separated by the separator
     */
    static String toStringList(List list, String separator) {
        if (list.isEmpty())
            return ''

        StringBuilder string = new StringBuilder()

        for (int i = 0; i < list.size() - 1; i++) {
            string.append(list.get(i).toString()).append(separator)
        }
        string.append(list.last().toString())

        return string.toString()
    }

    /**
     * @param link
     * @param name
     * @return a link in the format required by Google Sheets for hyperlinks, using {@code link}
     * as link and {@code name} as its name in the cell
     */
    static String getHyperLink(String link, String name) {
        return "=HYPERLINK(\"${link}\";\"${name}\")"
    }
}
