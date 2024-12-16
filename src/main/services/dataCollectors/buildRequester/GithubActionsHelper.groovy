package services.dataCollectors.buildRequester

import project.Project

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class GithubActionsHelper {
    static void createGitHubActionsFile(Project project) {
        def contents = """
name: Mining Framework Check
on: [push]
jobs:
    test:
        runs-on: ubuntu-latest
        strategy:
            matrix:
                java-version: [8, 11, 17]
            fail-fast: false
        steps:
            - uses: actions/checkout@v4
            - name: Set up Java \${{ matrix.java-version }}
              uses: actions/setup-java@v1
              with:
                java-version: \${{ matrix.java-version }}
                distribution: 'corretto'
            - name: Run tests
              uses: nick-fields/retry@v3
              with:
                timeout_minutes: 30
                max_attempts: 5
                command: mvn clean test -Dcheckstyle.skip=true
        """

        def githubActionsDirectory = Paths.get(project.getPath()).resolve(".github/workflows")
        Files.createDirectories(githubActionsDirectory)
        Files.write(githubActionsDirectory.resolve("mining_framework.yaml"), contents.getBytes(Charset.defaultCharset()),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)
    }
}
