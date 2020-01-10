package services

import java.io.File 
import groovy.util.NodeBuilder
import main.interfaces.DataCollector
import main.project.*
import main.util.TravisHelper
import main.util.GithubHelper
import main.util.ProcessRunner
import main.exception.TravisHelperException
import main.util.FileManager
import static main.app.MiningFramework.arguments

class BuildRequesterSemanticConflictDynamicImpl extends BuildRequester {

    static protected final MAVEN_BUILD_WITH_ALL_DEPENDENCIES = 'mvn clean compile assembly:single'

    @Override
    public void collectData(Project project, MergeCommit mergeCommit) {
        if (arguments.providedAccessKey()) {
            findAllCommitsFromMergeScenario(mergeCommit).each { commit ->
                String branchName = commit.take(5) + '_build_branch_all_dependencies'
                
                checkoutCommitAndCreateBranch(project, branchName, commit).waitFor()
                
                File travisFile = new File("${project.getPath()}/.travis.yml")
                String[] ownerAndName = getRemoteProjectOwnerAndName(project)
                travisFile.delete()
                BuildSystem buildSystem = getBuildSystem(project)

                if (buildSystem != BuildSystem.None) {
                    travisFile << getNewTravisFile(commit, ownerAndName[0], ownerAndName[1], buildSystem, MAVEN_BUILD_WITH_ALL_DEPENDENCIES)
                    commitChanges(project, "'Trigger build #${commit}'").waitFor()
                    pushBranch(project, branchName).waitFor()
                    goBackToMaster(project).waitFor()
                    println "${project.getName()} - Build requesting finished!"
                }

            }

        }
    }

    private addPluginOnPomFile(Project project){
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

            writeNewMavenFile(mavenFile, finalString)
        }
    }

    private String concatenateNewMavenFileContents(String first_part, String second_part, String third_part) {
        return first_part + second_part + third_part
    }

    private writeNewMavenFile(File mavenFile, String newMavenFileContent) {
        PrintWriter writer = new PrintWriter(mavenFile);
        writer.print("");
        writer.print(newMavenFileContent)
        writer.close();
    }

    @Override
    static protected Process commitChanges(Project project, String message) {
        ProcessRunner.runProcess(project.getPath(), "git", "add", ".travis.yml", "pom.xml").waitFor()

        return ProcessRunner.runProcess(project.getPath(), "git", "commit", "-a", "-m", "${message}")
    }

    @Override
    protected BuildSystem getBuildSystem (Project project) {
        File mavenFile = new File("${project.getPath()}/pom.xml")
        File gradleFile = new File("${project.getPath()}/build.gradle")

        if (mavenFile.exists()) {
            addPluginOnPomFile(project)
            return BuildSystem.Maven
        } else if (gradleFile.exists()) {
            return BuildSystem.Gradle
        } else {
            return BuildSystem.None
        }
    }

    private String[] findAllCommitsFromMergeScenario(MergeCommit mergeCommit){
        return [mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA(), mergeCommit.getRightSHA(), mergeCommit.getSHA()]
    }

}