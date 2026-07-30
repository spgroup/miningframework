package project.commitHashesExtraction

import exception.UnexpectedOutputException
import project.Project
import util.ProcessRunner

class GitLogCommitHashesExtractor implements CommitHashesExtractor {
    private String sinceDate
    private String untilDate
    private String path

    private static final EXPECTED_OUTPUT = ~/.*-(.* .*)+/

    GitLogCommitHashesExtractor(String sinceDate, String untilDate) {
        this.sinceDate = sinceDate
        this.untilDate = untilDate
    }

    @Override
    List<CommitHashes> extractCommitHashes(Project project) {
        List<CommitHashes> result = new ArrayList()

        def gitLog = constructAndRunGitLog(project)
        gitLog.getInputStream().eachLine {
            // Each line contains the hash of the commit followed by the hashes of the parents.
            if (it ==~ EXPECTED_OUTPUT) {
                def informations = it.split('-') // <commit hash>-<parents hash>
                def mergeSHA = informations[0]
                def parentsSHA = informations[1].split(' ')
                result.add(new CommitHashes(mergeSHA, parentsSHA))
            } else {
                throw new UnexpectedOutputException('Git log returned an unexpected output. Could not retrieve merge commits.', '<commit hash>-<parents hash>', it)
            }
        }

        return result
    }

   private Process constructAndRunGitLog(Project project) {
        this.path = project.getPath()
        return constructAndRunGitLog(this.sinceDate, this.untilDate)
    }

    private Process constructAndRunGitLog(String sinceDate, String untilDate) {
        ProcessBuilder gitLogBuilder = ProcessRunner.buildProcess(path, 'git', '--no-pager', 'log', '--merges', '--pretty=%H-%p')
        if(!sinceDate.equals(''))
            ProcessRunner.addCommand(gitLogBuilder, "--since=\"${sinceDate}\"")
        if(!untilDate.equals(''))
            ProcessRunner.addCommand(gitLogBuilder, "--until=\"${untilDate}\"")
        return ProcessRunner.startProcess(gitLogBuilder)
    }
}
