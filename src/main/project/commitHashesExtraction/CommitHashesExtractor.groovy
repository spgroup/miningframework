package project.commitHashesExtraction

import project.Project

import static app.MiningFramework.arguments

interface CommitHashesExtractor {
    class CommitHashes {
        public String mergeSha
        public String[] parents

        CommitHashes(String mergeSha, String... parents) {
            this.mergeSha = mergeSha
            this.parents = parents
        }
    }

    class Factory {
        static CommitHashesExtractor build() {
            if (arguments.projectCommitHashesFile != '') {
                def file = new File(arguments.projectCommitHashesFile)
                if (!file.exists()) {
                    throw new RuntimeException("File ${file.path} not found")
                }
                return new ProjectCommitListCommitHashesExtractor(file)
            }

            return new GitLogCommitHashesExtractor(arguments.sinceDate, arguments.untilDate)
        }
    }

    List<CommitHashes> extractCommitHashes(Project project)
}