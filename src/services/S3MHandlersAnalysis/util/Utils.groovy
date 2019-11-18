package services.S3MHandlersAnalysis.util

import main.app.MiningFramework
import main.project.MergeCommit
import main.project.Project
import main.util.ProcessRunner

import java.nio.file.Path
import java.nio.file.Paths

final class Utils {

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

    static Path getOutputPath() {
        return Paths.get(MiningFramework.arguments.getOutputPath())
    }

    static Path commitFilesPath(Project project, MergeCommit mergeCommit) {
        return getOutputPath().resolve(project.getName()).resolve(mergeCommit.getSHA())
    }

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

    static String getHyperLink(String link, String name) {
        return "=HYPERLINK(\"${link}\";\"${name}\")"
    }
}
