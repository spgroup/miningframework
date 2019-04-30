package main.util

import main.project.*
import java.net.HttpURLConnection
import main.exception.TravisHelperException

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

}