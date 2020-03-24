package util

import project.*
import java.net.HttpURLConnection
import exception.TravisHelperException

class TravisHelper {

    private final String API_URL="https://api.travis-ci.org"
    private String token; 
    
    TravisHelper(String accessKey) {
        String url = "${API_URL}/auth/github"
        HttpURLConnection request = HttpHelper.requestToApi(url, "POST")
        def body = [github_token: accessKey]
        
        HttpHelper.sendJsonBody(request, body)

        String responseMessage = request.getResponseMessage()

        if (responseMessage != 'OK') {
            throw new TravisHelperException('An error ocurred getting travis token')
        }
        def result = HttpHelper.responseToJSON(request.getInputStream())

        this.token = result.access_token
    }

    public Map getProject(String owner, String projectName) {
        String url = "${API_URL}/repos/${owner}/${projectName}"
        HttpURLConnection request = HttpHelper.requestToApi(url, "GET", this.token)
        String responseMessage = request.getResponseMessage()

        if (responseMessage != 'OK') {
            throw new TravisHelperException("An error ocurred trying to get ${projectName} project in travis")
        }

        return HttpHelper.responseToJSON(request.getInputStream())
    }

    private Map getUser() {
        String url = "${API_URL}/users"
        HttpURLConnection request = HttpHelper.requestToApi(url, "GET", this.token)
        String responseMessage = request.getResponseMessage()

        if (responseMessage != 'OK') {
            throw new TravisHelperException("An error ocurred trying to get ${projectName} project in travis")
        }

        return HttpHelper.responseToJSON(request.getInputStream())
    }

    public void sync() {
        println "Trying to sync travis account..."
        String url = "${API_URL}/users/sync"
        HttpURLConnection request = HttpHelper.requestToApi(url, "POST", this.token)
        String responseMessage = request.getResponseMessage()

        if (responseMessage != 'OK' && responseMessage != "Conflict") {
            throw new TravisHelperException("An error ocurred trying to sync: ${responseMessage}")
        }
    }

    public void syncAndWait() {
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

    public void enableTravis(Integer travisRepoId)  {
        String url = "${API_URL}/hooks"
        HttpURLConnection request = HttpHelper.requestToApi(url, "PUT", this.token)
        def body = [hook: [ id: travisRepoId, active: true ]]

        HttpHelper.sendJsonBody(request, body)

        String requestMessage = request.getResponseMessage()
        if (requestMessage != 'OK') {
            throw new TravisHelperException("An error ocurred trying to enable travis a project")
        }
    }

    public void addEnvironmentVariable(Integer travisRepoId, String key, String value) {
        String url = "${API_URL}/settings/env_vars?repository_id=${travisRepoId}"
        HttpURLConnection request = HttpHelper.requestToApi(url, "POST", this.token)
        def body = [env_var: [ name: key, value: value, public: false ]]

        HttpHelper.sendJsonBody(request, body)

        String requestMessage = request.getResponseMessage()
        if (requestMessage != 'OK') {
            throw new TravisHelperException("An error ocurred trying to enable travis a project")
        }
    }

}