package services.dataCollectors.buildRequester


import static com.xlson.groovycsv.CsvParser.parseCsv

import project.*
import util.FileManager
import static app.MiningFramework.arguments
import util.FileTransformations

class BuildRequesterDynamicSemanticStudy extends BuildRequester {

    static private final MAVEN_BUILD = 'mvn clean compile assembly:single'
    static private final GRADLE_BUILD = './gradlew fatJar'
    static private final BRANCH_NAME_WITH_TRANFORMATIONS = '_build_branch_with_all_dependencies_and_tranformations'
    static private final BRANCH_NAME_WITHOUT_TRANFORMATIONS = '_build_branch_with_all_dependencies_and_no_tranformations'

    public void collectData(Project project, MergeCommit mergeCommit) {
        if (arguments.providedAccessKey()) {
            String[] versions = ["original", "transformed"]
            versions.each{ version ->
                createBuildForEachCommit(project, mergeCommit, version)
            }
            
        }
    }

    private void createBuildForEachCommit(Project project, MergeCommit mergeCommit, String version) {
       String[] branch_option = ["_build_branch_all_dependencies_with_tranformations", "_build_branch_all_dependencies_without_tranformations"]
       findAllCommitsFromMergeScenario(mergeCommit).each { commit ->
            if (version == "original") {
                setupEnvironmentForBranchWithoutTransformations(project, mergeCommit, commit, BRANCH_NAME_WITHOUT_TRANFORMATIONS, version)
            } else if (version == "transformed") {
                setupEnvironmentForBranchWithTransformations(project, mergeCommit, commit, collectCodeTransformationInfoOfMergeCommit(mergeCommit.getSHA()), BRANCH_NAME_WITH_TRANFORMATIONS, version)
            }
       }
    }


    private void setupEnvironmentForBranchWithTransformations(Project project, MergeCommit mergeCommit, String commit, ArrayList<String> codeTransformationInfo, String branch, String version) {

        String branchName = commit.take(5) + branch
        checkoutCommitAndCreateBranch(project, branchName, commit).waitFor()
        FileTransformations transformations = new FileTransformations()

        File configurationFile = ciPlatform.getConfigurationFile(project)
        configurationFile.delete()
        BuildSystem buildSystem = getBuildSystem(project)
        for (code in codeTransformationInfo){
            for (file in FileManager.findLocalFileOfChangedClass(project.getPath(), code[4], mergeCommit.getSHA())){
                transformations.executeCodeTransformations( "${System.getProperty("user.dir")}/${file.toString().replace("[","").replace("]","")}").waitFor()
            }
        }

        sendNewBuildRequest(project, configurationFile, buildSystem, commit, branchName, version)
    }

    private void setupEnvironmentForBranchWithoutTransformations(Project project, MergeCommit mergeCommit, String commit, String branch, String version) {
        String branchName = commit.take(5) + branch
        checkoutCommitAndCreateBranch(project, branchName, commit).waitFor()
        File configurationFile = ciPlatform.getConfigurationFile(project)
        configurationFile.delete()
        BuildSystem buildSystem = getBuildSystem(project)
        sendNewBuildRequest(project, configurationFile, buildSystem, commit, branchName, version)
    }

    protected void sendNewBuildRequest(Project project, File configurationFile, BuildSystem buildSystem, String commit, String branchName, String version){
        configurationFile << ciPlatform.generateConfiguration(project, "${version}-${commit}", getBuildCommand(buildSystem))
        commitChanges(project, "'Trigger build #${commit}'").waitFor()
        pushBranch(project, branchName).waitFor()
        goBackToMaster(project).waitFor()
        println "${project.getName()} - Build requesting finished!"
    }

    private addPluginOnPomFile(Project project) {
        File mavenFile = new File("${project.getPath()}/pom.xml")
        String finalString = ""
        String pluginAllDependencies = "<plugin>\n\t<artifactId>maven-assembly-plugin</artifactId> \n\t<configuration> \n\t<archive> \n\t<manifest> \n\t\t<mainClass>fully.qualified.MainClass</mainClass> \n\t</manifest> \n\t</archive> \n\t<descriptorRefs> \n\t\t<descriptorRef>jar-with-dependencies</descriptorRef> \n\t</descriptorRefs> \n\t</configuration> \n\t</plugin> \n    </plugins>"
        String pluginAllDependenciesWithoutBuildSection = "\t<build>\n\t\t<plugins>\n\t\t   <plugin>\n\t\t\t<artifactId>maven-assembly-plugin</artifactId> \n\t\t\t<configuration> \n\t\t\t<archive> \n\t\t\t<manifest> \n\t\t\t\t<mainClass>fully.qualified.MainClass</mainClass> \n\t\t\t</manifest> \n\t\t\t</archive> \n\t\t\t<descriptorRefs> \n\t\t\t\t<descriptorRef>jar-with-dependencies</descriptorRef> \n\t\t\t</descriptorRefs> \n\t\t\t</configuration>\n\t\t   </plugin> \n\t\t</plugins>\n\t</build>\n</project>"
        
        if(mavenFile.exists()){
            String contents = new File("${project.getPath()}/pom.xml").text
            def matcher = contents =~ /<build>[\s\S]*<plugins>[\s\S]*<\/build>/
            
            if (matcher.size() == 1){
                String build_section = matcher[0]
                String first_pom_content_part = contents.split(/<build>[\s\S]*<plugins>[\s\S]*<\/build>/)[0]
                String second_pom_content_part = contents.split(/<build>[\s\S]*<plugins>[\s\S]*<\/build>/)[1]

                if (build_section != ""){
                    finalString = concatenateNewMavenFileContents(first_pom_content_part, build_section.replace(/<\/plugins>/, pluginAllDependencies), second_pom_content_part)
                }                
            }else{
                String first_pom_content_part = contents.split(/<\/project>/)[0]
                String second_pom_content_part = contents.split(/<\/project>/)[1]                
                finalString = concatenateNewMavenFileContents(first_pom_content_part, pluginAllDependenciesWithoutBuildSection, second_pom_content_part)
            }

            writeNewBuildManagerFile(mavenFile, finalString)
        }
    }

    private addPluginOnGradleFile(Project project) {
        File gradleFile = new File("${project.getPath()}/build.gradle")
        String pluginAllDependenciesWithoutBuildSection = "apply plugin: \'java\'\ntask fatJar(type: Jar) {\n\tmanifest {\n        attributes \'Implementation-Title\': \'Gradle Jar File Example\',  \n        \t\'Implementation-Version\': version,\n        \t\'Main-Class\': \'com.mkyong.DateUtils\'\n    }\n    baseName = project.name + \'-all\'\n    from { configurations.compile.collect { it.isDirectory() ? it : zipTree(it) } }\n    with jar\n}"
        
        if(gradleFile.exists()){
            String finalString = new File("${project.getPath()}/build.gradle").text
            finalString += pluginAllDependenciesWithoutBuildSection
            
            writeNewBuildManagerFile(gradleFile, finalString)
        }
    }

    private String concatenateNewMavenFileContents(String first_part, String second_part, String third_part) {
        return first_part + second_part + third_part
    }

    private writeNewBuildManagerFile(File buildManagerFile, String newBuildManagerContent) {
        PrintWriter writer = new PrintWriter(buildManagerFile);
        writer.print("");
        writer.print(newBuildManagerContent)
        writer.close();
    }

    protected BuildSystem getBuildSystem (Project project) {
        File mavenFile = new File("${project.getPath()}/pom.xml")
        File gradleFile = new File("${project.getPath()}/build.gradle")

        if (mavenFile.exists()) {
            addPluginOnPomFile(project)
            return BuildSystem.Maven
        } else if (gradleFile.exists()) {
        	addPluginOnGradleFile(project)
            return BuildSystem.Gradle
        } else {
            return BuildSystem.None
        }
    }

    private String[] findAllCommitsFromMergeScenario(MergeCommit mergeCommit) {
        return [mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA(), mergeCommit.getRightSHA(), mergeCommit.getSHA()]
    }

    public ArrayList<String> collectCodeTransformationInfoOfMergeCommit (String mergeCommit){
        ArrayList<String> codeTransformationInfo = new ArrayList<String>();
        try{
            File outputFile = new File("${arguments.getOutputPath()}/data/results.csv")
            def csv_content = outputFile.getText('utf-8')
            def data_iterator = parseCsv(csv_content, separator: ';', readFirstLine: false)
            
            for (line in data_iterator) {
                if (line[1] == mergeCommit){
                    codeTransformationInfo.add([line[1],line[2],line[3],line[4],line[5],line[6],line[7]])
                }
            }
        }catch(Exception e1){
            print(e1)
        }

        return codeTransformationInfo;
    }

}