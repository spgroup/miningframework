package project.commitHashesExtraction

import project.Project

import static com.xlson.groovycsv.CsvParser.parseCsv

class ProjectCommitListCommitHashesExtractor implements CommitHashesExtractor {
    private Map<String, List<CommitHashes>> projectCommitHashes = new HashMap<>()

    ProjectCommitListCommitHashesExtractor(File projectCommitHashesFile) {
        for (line in parseCsv(projectCommitHashesFile.getText())) {
            String project = line['project']
            String merge = line['merge']
            String left = line['left']
            String right = line['right']

            def commitHashes = new CommitHashes(merge, left, right)

            if (projectCommitHashes.containsKey(project)) {
                projectCommitHashes.get(project).add(commitHashes)
            } else {
                projectCommitHashes.put(project, new ArrayList<CommitHashes>([commitHashes]))
            }
        }
    }

    @Override
    List<CommitHashes> extractCommitHashes(Project project) {
        return projectCommitHashes.get(project.getName())
    }
}
