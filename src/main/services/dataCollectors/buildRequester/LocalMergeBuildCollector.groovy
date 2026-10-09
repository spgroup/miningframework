package services.dataCollectors.buildRequester

import interfaces.DataCollector
import project.MergeCommit
import project.Project
import util.MergeBuildHelper
import util.ProcessRunner

import static app.MiningFramework.arguments

/**
 * Local alternative to BuildRequester, used when no access key is given: builds the merge commit on this machine with
 * its Gradle wrapper or Maven.
 *
 * @requires: that the merge commit has a gradlew or a pom.xml on its root (and mvn installed, for Maven)
 * @provides: [outputPath]/files/[project]/[merge commit]/original-without-dependencies/merge/build.jar
 */
class LocalMergeBuildCollector implements DataCollector {

    private static final boolean IS_WINDOWS = System.getProperty('os.name').toLowerCase().contains('windows')

    void collectData(Project project, MergeCommit mergeCommit) {
        if (arguments.providedAccessKey()) {
            return // BuildRequester builds it on the CI
        }

        File mergeCommitFolder = checkoutInTemporaryFolder(project, mergeCommit)
        try {
            boolean built = buildProject(mergeCommitFolder) && saveMergeBuild(project, mergeCommit, mergeCommitFolder)
            println "${project.getName()} - merge commit ${mergeCommit.getSHA()} ${built ? 'built' : 'build failed'}"
        } finally {
            removeTemporaryFolder(project, mergeCommitFolder)
        }
    }

    private static File checkoutInTemporaryFolder(Project project, MergeCommit mergeCommit) {
        File folder = File.createTempDir()
        runCommand(project.getPath(), 'git', 'worktree', 'add', '--force', '--detach', folder.absolutePath, mergeCommit.getSHA())
        return folder
    }

    private static void removeTemporaryFolder(Project project, File folder) {
        runCommand(project.getPath(), 'git', 'worktree', 'remove', '--force', folder.absolutePath)
        folder.deleteDir()
    }

    private static boolean buildProject(File projectFolder) {
        File gradleWrapper = new File(projectFolder, IS_WINDOWS ? 'gradlew.bat' : 'gradlew')
        File mavenPom = new File(projectFolder, 'pom.xml')

        if (gradleWrapper.exists()) {
            gradleWrapper.setExecutable(true)
            return runCommand(projectFolder.path, gradleWrapper.absolutePath, 'assemble', '--no-daemon', '-q') == 0
        }
        if (mavenPom.exists()) {
            String maven = IS_WINDOWS ? 'mvn.cmd' : 'mvn'
            return runCommand(projectFolder.path, maven, '-DskipTests=true', '-q', 'package') == 0
        }
        return false
    }

    private static boolean saveMergeBuild(Project project, MergeCommit mergeCommit, File projectFolder) {
        File mergeBuild = MergeBuildHelper.getMergeBuild(arguments.getOutputPath(), project.getName(), mergeCommit.getSHA())
        return MergeBuildHelper.saveMergeBuild(getGeneratedJars(projectFolder), mergeBuild)
    }

    private static List<File> getGeneratedJars(File projectFolder) {
        File[] gradleJars = new File(projectFolder, 'build/libs').listFiles() ?: new File[0]
        File[] mavenJars = new File(projectFolder, 'target').listFiles() ?: new File[0]
        return (gradleJars + mavenJars) as List<File>
    }

    // runs the command, printing its output, and returns its exit code
    private static int runCommand(String directory, String... command) {
        Process process = ProcessRunner.runProcess(directory, command)
        process.inputStream.eachLine { println "    ${it}" }
        return process.waitFor()
    }
}
