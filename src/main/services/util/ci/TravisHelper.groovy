package services.util.ci

import exception.TravisHelperException
import project.Project
import util.HttpHelper

class TravisHelper {

    private final String API_URL="https://api.travis-ci.org"
    private String token; 
    
    TravisHelper(String accessKey) {
        String url = "${API_URL}/auth/github"
        HttpURLConnection connection = HttpHelper.requestToApi(url, HttpHelper.METHOD_POST)
        def body = [github_token: accessKey]
        HttpHelper.sendJsonBody(connection, body)

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new TravisHelperException('An error occurred getting travis token')
        }
        def result = HttpHelper.responseToJSON(connection.getInputStream())

        this.token = result.access_token
    }

    Object getProject(String owner, String projectName) {
        String url = "${API_URL}/repos/${owner}/${projectName}"
        HttpURLConnection connection = HttpHelper.requestToApi(url, HttpHelper.METHOD_GET, this.token)

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new TravisHelperException("An error ocurred trying to get ${projectName} project in travis")
        }

        return HttpHelper.responseToJSON(connection.getInputStream())
    }

    void syncAndWait() {
        sync()
        waitSync()
    }

    private void waitSync() {
        boolean isSyncing = true
        while (isSyncing) {
            Map user = getUser()
            isSyncing = user.is_syncing
            sleep(5000)
        }
    }

    void sync() {
        println "Trying to sync travis account..."
        String url = "${API_URL}/users/sync"
        HttpURLConnection request = HttpHelper.requestToApi(url, HttpHelper.METHOD_POST, this.token)
        String responseMessage = request.getResponseMessage()

        if (responseMessage != HttpURLConnection.HTTP_OK && responseMessage != HttpURLConnection.HTTP_CONFLICT) {
            throw new TravisHelperException("An error ocurred trying to sync: ${responseMessage}")
        }
    }

    private Object getUser() {
        String url = "${API_URL}/users"
        HttpURLConnection connection = HttpHelper.requestToApi(url, HttpHelper.METHOD_GET, this.token)

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new TravisHelperException("An error occurred trying to get user in travis")
        }

        return HttpHelper.responseToJSON(connection.getInputStream())
    }

    void enableTravis(Integer travisRepoId)  {
        String url = "${API_URL}/hooks"
        HttpURLConnection connection = HttpHelper.requestToApi(url, HttpHelper.METHOD_PUT, this.token)
        def body = [hook: [ id: travisRepoId, active: true ]]

        HttpHelper.sendJsonBody(connection, body)

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new TravisHelperException("An error occurred trying to enable travis a project")
        }
    }

    void addEnvironmentVariable(Integer travisRepoId, String key, String value) {
        String url = "${API_URL}/settings/env_vars?repository_id=${travisRepoId}"
        HttpURLConnection connection = HttpHelper.requestToApi(url, HttpHelper.METHOD_POST, this.token)
        def body = [env_var: [ name: key, value: value, public: false ]]

        HttpHelper.sendJsonBody(connection, body)
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new TravisHelperException("An error occurred trying to add env var to travis project")
        }
    }

    Object getBuilds(Project project) {
        String[] projectOwnerAndName = project.getOwnerAndName()
        String url =  "${API_URL}/repo/${projectOwnerAndName[0]}/${projectOwnerAndName[1]}/builds"
        HttpURLConnection connection = HttpHelper.requestToApi(url, HttpHelper.METHOD_GET, this.token)

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new TravisHelperException("An error occurred trying to get builds in travis")
        }

        return HttpHelper.responseToJSON(connection.getInputStream())
    }
}