package project

import util.ProcessRunner
import exception.UnexpectedOutputException

import java.util.Collections
import java.util.Random
import java.util.regex.Pattern
import java.util.regex.Matcher

class Project {
    
    private String name
    private String path
    private String url
    private boolean remote
    static private Pattern REMOTE_REPO_PATTERN = Pattern.compile("((http|https):\\/\\/)?.+.com\\/.+\\/.+")

    Project(String name, String path) {
        this.name = name
        this.path = path
        
        this.remote = checkIfPathIsUrl(path)
        if (this.remote) {
            this.url = this.path
        } else {
            this.url = null
        }
    }

    Project(String path) {
        this.path = path

        this.remote = checkIfPathIsUrl(path)   
    
        this.name = this.getOwnerAndName()[1] 
    }

    private checkIfPathIsUrl (String path) {
        Matcher matcher = REMOTE_REPO_PATTERN.matcher(path)

        return matcher.find()
    }

    List getMergeCommits(String sinceDate, String untilDate) {
        ArrayList<String> skipped = new ArrayList<String>()
        ArrayList<MergeCommit> mergeCommits = new ArrayList<MergeCommit>()
        
        Process gitLog = constructAndRunGitLog(sinceDate, untilDate)
        def expectedOutput = ~/.*-(.* .*)+/
        gitLog.getInputStream().eachLine {

            // Each line contains the hash of the commit followed by the hashes of the parents.
            if(it ==~ expectedOutput) {
                
                String[] informations = it.split('-') // <commit hash>-<parents hash>
                String SHA = getSHA(informations)
                String[] parentsSHA = getParentsSHA(informations)

                try {
                    String ancestorSHA = getCommonAncestor(SHA, parentsSHA)
                    MergeCommit mergeCommit = new MergeCommit(SHA, parentsSHA, ancestorSHA)
                    mergeCommits.add(mergeCommit)
                } catch (UnexpectedOutputException e) {
                    println "Skipping merge commit ${SHA}"
                    println e.message
                    skipped.add(SHA)
                }
            } else {
                throw new UnexpectedOutputException('Git log returned an unexpected output. Could not retrieve merge commits.', '<commit hash>-<parents hash>', it)
            }
        }
        
        if(mergeCommits.isEmpty())
            println "No merge commits."

        return [mergeCommits, skipped]
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

        String actualOutput = gitMergeBase.getText().trim()
        if (actualOutput ==~ expectedOutput)
            return actualOutput
        else
            throw new UnexpectedOutputException('Git merge-base returned an unexpected output. Could not retrieve the ancestor commit.', '<commit hash>', actualOutput)
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

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    String getPath() {
        return path
    }

    void setPath(String path) {
        this.path = path
    }

    boolean isRemote() {
        return remote
    }

    String getRemoteUrl() {
        if (remote && checkIfPathIsUrl(this.path)) {
            return this.path
        } else if (remote) {
            Process gitProcess = ProcessRunner.runProcess(this.path, "git", "config", "--get", "remote.origin.url")
            String gitProcessText = gitProcess.getText()

            return gitProcessText.trim()
        }
        return null
    }

    String[] getOwnerAndName() {
        String remoteUrl = this.getRemoteUrl()
        if (remoteUrl != null) {
            String[] splitPath = this.remoteUrl.split("/");
            String projectName = splitPath[splitPath.length - 1]
            String projectOwner = splitPath[splitPath.length - 2]

            return [projectOwner, projectName]
        } else {
            String[] splitPath = this.path.split("/")
            String projectName = splitPath[splitPath.length - 1]

            return ["local", projectName]
        }
    }

}
