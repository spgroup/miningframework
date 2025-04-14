package project

import exception.UnexpectedOutputException
import org.apache.logging.log4j.LogManager
import project.commitHashesExtraction.CommitHashesExtractor
import util.ProcessRunner

import java.util.regex.Matcher
import java.util.regex.Pattern

class Project {
    private static LOG = LogManager.getLogger(Project.class)
    
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

    List getMergeCommits(CommitHashesExtractor commitHashesExtractor) {
        ArrayList<String> skipped = new ArrayList<String>()
        ArrayList<MergeCommit> mergeCommits = new ArrayList<MergeCommit>()

        commitHashesExtractor.extractCommitHashes(this).each(commitHashes -> {
            def SHA = commitHashes.mergeSha
            def parentsSHA = commitHashes.parents

            try {
                String ancestorSHA = getCommonAncestor(SHA, parentsSHA)
                MergeCommit mergeCommit = new MergeCommit(SHA, parentsSHA, ancestorSHA)
                mergeCommits.add(mergeCommit)
            } catch (UnexpectedOutputException e) {
                println e.message
                try {
                    println "Trying to fetch it"
                    // Let's try fetching it before skipping it
                    constructAndRunGitFetch(SHA)
                    String ancestorSHA = getCommonAncestor(SHA, parentsSHA)
                    MergeCommit mergeCommit = new MergeCommit(SHA, parentsSHA, ancestorSHA)
                    mergeCommits.add(mergeCommit)
                } catch (UnexpectedOutputException ex) {
                    println "Skipping merge commit ${SHA}"
                    println ex.message
                    skipped.add(SHA)
                }
            }
        })

        if(mergeCommits.isEmpty())
            println "No merge commits."
        return [mergeCommits, skipped]
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

    private void constructAndRunGitFetch(String mergeCommitSHA) {
        ProcessBuilder gitMergeBaseBuilder = ProcessRunner.buildProcess(path, 'git', 'fetch', 'origin', mergeCommitSHA)
        def exitCode = ProcessRunner.startProcess(gitMergeBaseBuilder).waitFor()
        if (exitCode > 0) {
            throw new UnexpectedOutputException("Could not fetch merge commit ${mergeCommitSHA} from origin", "Exit code 0", "Exit code ${exitCode}")
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
