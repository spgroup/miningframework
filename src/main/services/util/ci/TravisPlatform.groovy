package services.util.ci

import exception.TravisHelperException
import project.Project

import static app.MiningFramework.arguments

class TravisPlatform implements CIPlatform  {
    static private final FILE_NAME = '.travis.yml'
    static private final ENABLE_MAX_RETRIES = 50
    private static final POOLING_FREQUENCY = 10000

    private TravisHelper travisHelper;

    @Override
    void enableProject(Project project) {
        travisHelper = new TravisHelper(arguments.getAccessKey())

        keepTryingToEnableTravisProject(project, ENABLE_MAX_RETRIES)
    }

    private void keepTryingToEnableTravisProject (Project project, int maxNumberOfTries) {
        /* This is a workaround to a limitation in travis api
        * You have to wait and sync multiple times to a project
        * become available
        */
        try {
            configureTravisProject(project)
        } catch (TravisHelperException e) {
            e.printStackTrace()
            travisHelper.syncAndWait()
            if (maxNumberOfTries > 0) {
                keepTryingToEnableTravisProject(project, maxNumberOfTries - 1)
            } else {
                throw new TravisHelperException("Number of sync tries exceeded")
            }
        }
    }

    private void configureTravisProject (Project project) {
        String[] ownerAndName = project.getOwnerAndName()
        Map travisProject = travisHelper.getProject(ownerAndName[0], ownerAndName[1])
        travisHelper.enableTravis(travisProject.id)
        travisHelper.addEnvironmentVariable(travisProject.id, "GITHUB_TOKEN", arguments.getAccessKey())
    }

    @Override
    File getConfigurationFile(Project project) {
        return new File("${project.getPath()}/${FILE_NAME}")
    }

    @Override
    String generateConfiguration(Project project, String identifier, String buildCommand) {
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
    - cd ./MiningBuild
    - tar -zcvf result.tar.gz *
deploy:
  provider: releases
  api_key: \$GITHUB_TOKEN
  file: result.tar.gz
  name: fetchjar-${identifier}
  file_glob: true
  overwrite: false
  skip_cleanup: true
  on:
    all_branches: true 
            """
    }

    @Override
    void waitForBuilds(Project project) {
        if (travisHelper == null) {
            travisHelper = new TravisHelper(arguments.getAccessKey())
        }

        boolean hasPendent = true

        while (hasPendent) {
            List<Object> builds = travisHelper.getBuilds(project)

            hasPendent = builds.stream()
                    .filter(
                            build -> !((String) build.branch).startsWith("untagged"))
                    .any(
                            build -> ((String) build.state) != "finished"
                    ).toBoolean()

            if (hasPendent) {
                sleep(POOLING_FREQUENCY)
            }
        }

    }
}
