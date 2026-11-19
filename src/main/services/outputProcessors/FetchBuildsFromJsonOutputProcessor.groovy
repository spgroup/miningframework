package services.outputProcessors

import arguments.InputParser
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import interfaces.OutputProcessor
import project.Project
import util.GithubHelper
import util.MergeBuildHelper
import util.ProcessRunner

import static app.MiningFramework.arguments

/**
 * Same as FetchBuildsOutputProcessor (scripts/fetch_jars.py), for the scenarios of the modified lines JSON: downloads
 * the merge build that the CI released (fetchjar-[merge commit]) on the fork of each project. Without access key,
 * LocalMergeBuildCollector builds it instead.
 *
 * @requires: the access key and [outputPath]/data/results-with-build-information.json
 * @provides: the jars of the merge build of each scenario (original-without-dependencies/merge/*.jar) and its hasBuild (json)
 * and has_build (csv) updated
 */
class FetchBuildsFromJsonOutputProcessor implements OutputProcessor {

    private static final int MERGE_COMMIT_COLUMN = 1
    private static final int HAS_BUILD_COLUMN = 5

    void processOutput() {
        if (!arguments.providedAccessKey()) {
            return // LocalMergeBuildCollector already built the merge commits
        }

        GithubHelper githubHelper = new GithubHelper(arguments.getAccessKey())
        Map<String, List> releasesOfEachProject = getReleasesOfEachProject(githubHelper)

        List<Map> scenarios = readScenariosFromJson()
        for (Map scenario : scenarios) {
            fetchMergeBuild(scenario, releasesOfEachProject)
        }
        saveScenariosToJson(scenarios)
        updateHasBuildColumnOfCsv(scenarios)
    }

    // the CI builds run on the fork of the project, made by ForkAndEnableCIProcessor
    private static Map<String, List> getReleasesOfEachProject(GithubHelper githubHelper) {
        Map<String, List> releasesOfEachProject = [:]
        for (Project project : InputParser.getProjectList(arguments.getInputPath())) {
            if (project.isRemote()) {
                Project fork = githubHelper.getFork(project)
                releasesOfEachProject[project.getName()] = githubHelper.getRepositoryReleases(fork) as List
            }
        }
        return releasesOfEachProject
    }

    private static void fetchMergeBuild(Map scenario, Map<String, List> releasesOfEachProject) {
        File mergeFolder = MergeBuildHelper.getMergeBuildFolder(arguments.getOutputPath(), scenario.project as String, scenario.commits.merge as String)
        def mergeRelease = findMergeRelease(scenario, releasesOfEachProject)
        if (mergeRelease != null) {
            downloadMergeBuild(mergeRelease, mergeFolder)
        }

        scenario.hasBuild = MergeBuildHelper.hasMergeBuild(mergeFolder)
        println "${scenario.project} - merge commit ${scenario.commits.merge} ${scenario.hasBuild ? 'build fetched' : 'has no build'}"
    }

    private static Object findMergeRelease(Map scenario, Map<String, List> releasesOfEachProject) {
        List releases = releasesOfEachProject[scenario.project] ?: []
        return releases.find { release -> release.name == "fetchjar-${scenario.commits.merge}" && release.assets }
    }

    // every jar of the build, with its name, as scripts/fetch_jars.py: Scenario.getClassPath picks the one of the scenario
    private static void downloadMergeBuild(Object mergeRelease, File mergeFolder) {
        File downloadFolder = File.createTempDir()
        try {
            File compressedJars = downloadReleaseFile(mergeRelease, downloadFolder)
            extractTarGz(compressedJars)
            MergeBuildHelper.saveMergeBuild(downloadFolder.listFiles() as List<File>, mergeFolder)
        } finally {
            downloadFolder.deleteDir()
        }
    }

    // the CI workflow releases the jars of the build in a result.tar.gz
    private static File downloadReleaseFile(Object release, File downloadFolder) {
        File releaseFile = new File(downloadFolder, 'result.tar.gz')
        releaseFile.bytes = new URL(release.assets[0].browser_download_url as String).bytes
        return releaseFile
    }

    private static void extractTarGz(File tarGz) {
        ProcessRunner.runProcess(tarGz.parent, 'tar', '-xzf', tarGz.name).waitFor()
    }

    private static File getScenariosJson() {
        return new File(arguments.getOutputPath(), 'data/results-with-build-information.json')
    }

    private static List<Map> readScenariosFromJson() {
        return new JsonSlurper().parse(getScenariosJson()) as List<Map>
    }

    private static void saveScenariosToJson(List<Map> scenarios) {
        getScenariosJson().text = JsonOutput.prettyPrint(JsonOutput.toJson(scenarios))
    }

    private static void updateHasBuildColumnOfCsv(List<Map> scenarios) {
        File csv = new File(arguments.getOutputPath(), 'data/results-with-build-information.csv')
        if (!csv.exists()) {
            return
        }

        Map<String, Boolean> hasBuildOfEachMergeCommit = scenarios.collectEntries { [(it.commits.merge): it.hasBuild] }
        List<String> lines = csv.readLines()
        List<String> updatedLines = [lines[0]] // the header
        for (String line : lines.drop(1)) {
            updatedLines << setHasBuildColumn(line, hasBuildOfEachMergeCommit)
        }
        csv.text = updatedLines.join('\n') + '\n'
    }

    private static String setHasBuildColumn(String line, Map<String, Boolean> hasBuildOfEachMergeCommit) {
        List<String> columns = line.split(';', -1) as List<String>
        String mergeCommit = columns[MERGE_COMMIT_COLUMN]
        columns[HAS_BUILD_COLUMN] = String.valueOf(hasBuildOfEachMergeCommit[mergeCommit] ?: false)
        return columns.join(';')
    }
}
