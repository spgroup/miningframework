package services.util.ci

import project.Project
import util.GithubHelper

import static app.MiningFramework.arguments

class GithubActionsPlatform implements CIPlatform {
    private static final FILE_NAME = ".github/workflows/build.yaml"
    private static final JAVA_VERSION = "11"
    private static final POOLING_FREQUENCY = 10000
    private GithubHelper githubHelper

    @Override
    void enableProject(Project project) {
        // github doesn't require that the project be enabled
    }

    @Override
    File getConfigurationFile(Project project) {
        return new File("${project.getPath()}/${FILE_NAME}")
    }

    @Override
    String generateConfiguration(Project project, String identifier, String buildCommand) {
        return """
name: Java Build

on: [push]

jobs:
    build:
        runs-on: ubuntu-latest
        
        steps:
            - uses: actions/checkout@v2
            
            # Try building with Java 8
            - name: Set up JDK 8
              id: setupJava8
              uses: actions/setup-java@v1
              with:
                java-version: 8
    
            - name: Build with Java 8
              id: buildJava8
              run: ${buildCommand}
              continue-on-error: true

            # If Java 8 fails, try Java 11
            - name: Set up JDK 11
              if: steps.buildJava8.outcome == 'failure'
              id: setupJava11
              uses: actions/setup-java@v1
              with:
                java-version: 11
    
            - name: Build with Java 11
              if: steps.buildJava8.outcome == 'failure'
              id: buildJava11
              run: ${buildCommand}
              continue-on-error: true
            
            # If Java 8 AND Java 11 both fail, try Java 17
            - name: Set up JDK 17
              if: steps.buildJava8.outcome == 'failure' && steps.buildJava11.outcome == 'failure'
              id: setupJava17
              uses: actions/setup-java@v1
              with:
                java-version: 17
    
            - name: Build with Java 17
              if: steps.buildJava8.outcome == 'failure' && steps.buildJava11.outcome == 'failure'
              id: buildJava17
              run: ${buildCommand}

            - name: Generate tar
              run: |
                mkdir MiningBuild
                find . -name '*.jar' -exec cp {} . \\;
                tar -zcvf result.tar.gz *.jar
            - name: Create release
              id: create_release
              uses: actions/create-release@v1
              env:
                GITHUB_TOKEN: \${{ secrets.GITHUB_TOKEN }}
              with:
                tag_name: build-\${{ github.sha }}
                release_name: fetchjar-\${{ github.sha }}
                draft: false
            - name: Upload jar
              id: upload-release-jar
              uses: actions/upload-release-asset@v1
              env:
                GITHUB_TOKEN: \${{ secrets.GITHUB_TOKEN }}
              with:
                upload_url: \${{ steps.create_release.outputs.upload_url }}
                asset_path: ./result.tar.gz
                asset_name: result.tar.gz
                asset_content_type: application/gzip
"""
    }

    @Override
    void waitForBuilds(Project project) {
        this.githubHelper = new GithubHelper(arguments.getAccessKey())

        boolean hasPendent = true
        while (hasPendent) {
            List<Object> workflows = this.githubHelper.getWorkFlowRuns(project)

            hasPendent = workflows.stream().any(
                    workflow -> workflow.conclusion == null
            ).toBoolean()

            if (hasPendent) {
                sleep(POOLING_FREQUENCY)
            }
        }
    }
}
