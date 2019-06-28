package main.util

import java.net.HttpURLConnection
import main.project.Project
import main.exception.*

class GithubHelper {

    public final String URL = "https://github.com"
    public final String API_URL = "https://api.github.com"
    public final String RAW_CONTENT_URL = "https://raw.githubusercontent.com"
    private String accessKey;
    
    public GithubHelper (String accessKey) {
        this.accessKey = accessKey
    }

    public getUser() {
        String url = "${API_URL}/user"
        HttpURLConnection connection = HttpHelper.requestToApi(url, "GET", this.accessKey)
        
        def resBody = HttpHelper.responseToJSON(connection.getInputStream())

        if (connection.getResponseMessage() != "OK") {
            throw new GithubHelperException("Http request returned an error ${responseMessage}")
        }
        return resBody
    }

    public Map fork (Project project) {
        if (project.isRemote()) {
            try {
                String[] projectNameAndOwner = project.getOwnerAndName()
                String projectOwner = projectNameAndOwner[0]
                String projectName = projectNameAndOwner[1]

                String url = "${API_URL}/repos/${projectOwner}/${projectName}/forks"
                HttpURLConnection response = HttpHelper.requestToApi(url, "POST", this.accessKey)

                String responseMessage = response.getResponseMessage()
                if (responseMessage != "Accepted") {
                    throw new GithubHelperException("Http request returned an error ${responseMessage}")
                }
                return HttpHelper.responseToJSON(response.getInputStream())
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new GithubHelperException("Error parsing project remote")
            }
        }
    }

    public getFile (String projectOwner, String projectName, String path) {
        String url = getContentApiUrl(projectOwner, projectName, path)
        HttpURLConnection response = HttpHelper.requestToApi(url, "GET", this.accessKey)
        String responseMessage = response.getResponseMessage()
        if (responseMessage != "OK") {
            throw new GithubHelperException("Http request returned an error ${responseMessage}")
        }
        
        def result = HttpHelper.responseToJSON(response.getInputStream())
        result.content = HttpHelper.convertToUTF8(result.content)

        return result
    }

    public updateFile(String projectOwner, String projectName, String path, String fileSha, String content, String branch, String commitMessage) {
        String url = getContentApiUrl(projectOwner, projectName, path)
        HttpURLConnection connection = HttpHelper.requestToApi(url, "PUT", this.accessKey)
        def message = [
            message: commitMessage, 
            content: HttpHelper.convertToBase64(content), 
            sha: fileSha,
            branch: branch
        ]
        
        HttpHelper.sendJsonBody(connection, message)
        String responseMessage = connection.getResponseMessage()

        if (responseMessage != "OK") {
            throw new GithubHelperException("Http request returned an error ${responseMessage}")
        }
        return responseMessage
    }

    private getContentApiUrl(String projectOwner, String projectName, String path) {
        return "${API_URL}/repos/${projectOwner}/${projectName}/contents/${path}"
    }
}