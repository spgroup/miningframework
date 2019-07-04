package main.project

import main.util.ProcessRunner
import main.exception.UnexpectedOutputException
import static groovy.io.FileType.DIRECTORIES
import static main.app.MiningFramework.arguments
import static com.xlson.groovycsv.CsvParser.parseCsv
@Grab('com.xlson.groovycsv:groovycsv:1.3')

class Project {
    
    private String name
    private String path
    private boolean remote

    public Project(String name, String path) {
        this.name = name
        this.path = path
        this.remote = path.startsWith('https://github.com/')
    }

    public ArrayList<MergeCommit> getMergeCommits(String sinceDate, String untilDate) {
        ArrayList<MergeCommit> mergeCommits = new ArrayList<MergeCommit>()
        
        Process gitLog = constructAndRunGitLog(sinceDate, untilDate)
        def expectedOutput = ~/.*-(.* .*)+/
        gitLog.getInputStream().eachLine {

            // Each line contains the hash of the commit followed by the hashes of the parents.
            if(it ==~ expectedOutput) {
                
                String[] informations = it.split('-') // <commit hash>-<parents hash>
                String SHA = getSHA(informations)
                String[] parentsSHA = getParentsSHA(informations)
                String ancestorSHA = getCommonAncestor(SHA, parentsSHA)

                MergeCommit mergeCommit = new MergeCommit(SHA, parentsSHA, ancestorSHA)
                mergeCommits.add(mergeCommit)

            } else {
                throw new UnexpectedOutputException('Git log returned an unexpected output. Could not retrieve merge commits.', '<commit hash>-<parents hash>', it)
            }
        }
        
        if(mergeCommits.isEmpty())
            println "No merge commits."
        return mergeCommits
    }

    private String getSHA(String[] informations) {
        return informations[0]
    } 

    private String[] getParentsSHA(String[] informations) {
        return informations[1].split(' ')
    }

    private String getCommonAncestor(mergeCommitSHA, parentsSHA) {
        Process gitMergeBase = constructAndRunGitMergeBase(mergeCommitSHA, parentsSHA)
        def expectedOutput = ~/[0-9a-z]{7,}/
        gitMergeBase.getInputStream().eachLine {
            if (it ==~ expectedOutput)
                return it
            else
                throw new UnexpectedOutputException('Git merge-base returned an unexpected output. Could not retrieve the ancestor commit.', '<commit hash>', it)
        }
    }

    private Process constructAndRunGitMergeBase(String mergeCommitSHA, String[] parentsSHA) {
        ProcessBuilder gitMergeBaseBuilder = ProcessRunner.buildProcess(path, 'git', 'merge-base')
        if (parentsSHA.length > 2)
            ProcessRunner.addCommand(gitMergeBaseBuilder, '--octopus')
        for (parent in parentsSHA)
            ProcessRunner.addCommand(gitMergeBaseBuilder, parent)
        return ProcessRunner.startProcess(gitMergeBaseBuilder)
    }

    private Process constructAndRunGitLog(String sinceDate, String untilDate) {
        ProcessBuilder gitLogBuilder = ProcessRunner.buildProcess(path, 'git', '--no-pager', 'log', '--merges', '--pretty=%H-%p')
        if(!sinceDate.equals(''))
            ProcessRunner.addCommand(gitLogBuilder, "--since=\"${sinceDate}\"")
        if(!untilDate.equals(''))
            ProcessRunner.addCommand(gitLogBuilder, "--until=\"${untilDate}\"")
        return ProcessRunner.startProcess(gitLogBuilder)
    }

    public String getName() {
        return name
    }

    public void setName(String name) {
        this.name = name
    }

    public String getPath() {
        return path
    }   

    public void setPath(String path) {
        this.path = path
    }

    public boolean isRemote() {
        return remote
    }

    public void setRemote(boolean remote) {
        this.remote = remote
    }

    public String[] getOwnerAndName() {
        if (remote) {
            String[] splitedPath = this.path.split("/");
            String projectOwner = splitedPath[splitedPath.length - 2]
            String projectName = splitedPath[splitedPath.length - 1]
            return [projectOwner, projectName]
        }
        return []
    }

     static ArrayList<Project> getProjectList() {
        ArrayList<Project> projectList = new ArrayList<Project>()

        String projectsFile = new File(arguments.getInputPath()).getText()
        def iterator = parseCsv(projectsFile)
        for (line in iterator) {
            String name = line[0]
            String path = line[1]

            boolean relativePath
            try {
                relativePath = line[2].equals("true")
            } catch(ArrayIndexOutOfBoundsException e) {
                relativePath = false
            }

            if(relativePath) 
                projectList.addAll(getProjects(name, path))
            else {
                Project project = new Project(name, path)
                projectList.add(project)
            }
        }

        return projectList
    }

    static ArrayList<Project> getProjects(String directoryName, String directoryPath) {
        ArrayList<Project> projectList = new ArrayList<Project>()

        File directory = new File(directoryPath)
        directory.traverse(type: DIRECTORIES, maxDepth: 0) {
             
             // Checking if it's a git project.
             String filePath = it.toString()
             if(new File("${filePath}/.git").exists()) {
                 Project project = new Project("${directoryName}/${it.getName()}", filePath)
                 projectList.add(project)
             }
        }

        return projectList
    }
}