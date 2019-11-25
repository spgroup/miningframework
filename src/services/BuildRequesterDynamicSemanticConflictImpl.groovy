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

class BuildRequesterDynamicSemanticConflictImpl extends BuildRequester {

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
                    travisFile << getNewTravisFile(commit, ownerAndName[0], ownerAndName[1], buildSystem)
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
                String first_pom = contents.split(/<build>[\s\S]*<plugins>[\s\S]*<\/build>/)[0]
                String second_pom = contents.split(/<build>[\s\S]*<plugins>[\s\S]*<\/build>/)[1]

                if (build_section != ""){
                    finalString += first_pom
                    finalString += build_section.replace(/<\/plugins>/, pluginAllDependencies)
                    finalString += second_pom
                }                
            }else{
                String first_pom = contents.split(/<\/project>/)[0]
                String second_pom = contents.split(/<\/project>/)[1]
                
                finalString += first_pom
                finalString += pluginAllDependenciesWithoutBuildSection
                finalString += second_pom
            }
        }

        PrintWriter writer = new PrintWriter(mavenFile);
        writer.print("");
        writer.print(finalString)
        writer.close();
        
        /*finalString = ""
        pluginAllDependencies = "<plugin>\n\t<artifactId>maven-assembly-plugin</artifactId> \n\t<configuration> \n\t<archive> \n\t<manifest> \n\t\t<mainClass>fully.qualified.MainClass</mainClass> \n\t</manifest> \n\t</archive> \n\t<descriptorRefs> \n\t\t<descriptorRef>jar-with-dependencies</descriptorRef> \n\t</descriptorRefs> \n\t</configuration> \n\t</plugin> \n    </plugins>"
        if(mavenFile.exists(project)){
            contents = ""
            readPom = open("${project.getPath()}/pom.xml", "r")
            if f.mode == 'r':
                contents =f.read()
            if (re.search("<build>[\s\S]*<plugins>[\s\S]*<\/build>",contents)):
                p = re.compile("<build>[\\s\\S]*<plugins>[\\s\\S]*<\\/build>")
                build = p.search(contents)
                if (len(build.groups())) == 0:
                    first_pom = re.split("<build>[\\s\\S]*<plugins>[\\s\\S]*<\\/build>", contents)[0]
                    second_pom = re.split("<build>[\\s\\S]*<plugins>[\\s\\S]*<\\/build>",contents)[1]
                if (re.search("<\\/plugins>[\\s\\S]*<\\/build>", build.group(0))):
                    finalString += first
                    finalString += re.sub("<\\/plugins>", pluginAllDependencies, build.group(0))
                    finalString += second
                else:
                    finalString += first
                    finalString += "<build><plugins><plugin>"+pluginAllDependencies+"<\\build>"
                    finalString +=  second
                
                with open("${project.getPath()}/pom.xml", "w") as f:
                    f.write(finalString)
        }*/
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

    @Override
    static protected getNewTravisFile(String commitSha, String owner, String projectName, BuildSystem buildSystem) {
        String buildCommand = "";
        if (buildSystem == BuildSystem.Maven) {
            buildCommand = MAVEN_BUILD_WITH_ALL_DEPENDENCIES
        } else if (buildSystem == BuildSystem.Gradle) {
            buildCommand = GRADLE_BUILD
        }
    
        String trimmedProjectName = projectName.replace('\n', '')
        return """
sudo: required
language: java

jdk:
  - openjdk8

script:
  - ${buildCommand}

before_deploy:
    - mkdir MiningBuild
    - find . -name '*.jar' -exec cp {} ./MiningBuild \\;
    - cd /home/travis/build/${owner}/${trimmedProjectName}/MiningBuild
    - tar -zcvf result.tar.gz *
deploy:
  provider: releases
  api_key:
    secure: \$GITHUB_TOKEN
  file: result.tar.gz
  name: fetchjar-${commitSha}
  file_glob: true
  overwrite: true
  skip_cleanup: true
  on:
    all_branches: true 
            """
    }

    private String[] findAllCommitsFromMergeScenario(MergeCommit mergeCommit){
        return [mergeCommit.getAncestorSHA(), mergeCommit.getLeftSHA(), mergeCommit.getRightSHA(), mergeCommit.getSHA()]
    }

}