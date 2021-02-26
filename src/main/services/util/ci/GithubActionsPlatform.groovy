package services.util.ci

import project.Project

class GithubActionsPlatform implements CIPlatform {
    private static final FILE_NAME = ".github/workflows/build.yaml"
    private static final JAVA_VERSION = "1.8"

    @Override
    void enableProject(Project project) {}

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
            - name: Set up JDK ${JAVA_VERSION}
              uses: actions/setup-java@v1
              with:
                java-version: ${JAVA_VERSION}
            - name: Build
              run: ${buildCommand}
            - name: Generate tar
              run: |
                mkdir MiningBuild
                find . -name '*.jar' -exec cp {} ./MiningBuild \\;
                tar -zcvf result.tar.gz ./MiningBuild/*
            - name: Create release
              id: create_release
              uses: actions/create-release@v1
              env:
                GITHUB_TOKEN: \${{ secrets.GITHUB_TOKEN }}
              with:
                release_name: fetchjar-${identifier}
                draft: false
            - name: Upload jar
              id: upload-release-jar
              uses: actions/upload-release-assets@v1
              env:
                GITHUB_TOKEN: \${{ secrets.GITHUB_TOKEN }}
              with:
                upload_url: \${{ steps.create_release.outputs.upload_url }}
                asset_path: ./result.tar.gz
                asset_name: result.tar.gz
                asset_content_type: application/gzip
"""
    }
}
