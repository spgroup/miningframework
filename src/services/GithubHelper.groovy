package services

import main.project.Project
import java.net.URL
import java.net.UnknownHostException
import java.net.HttpURLConnection
import groovy.json.*
import java.util.Base64


class GithubHelper {

    private final String API_URL = "https://api.github.com"
    private final String RAW_CONTENT_URL = "https://raw.githubusercontent.com"
    private final String COMMIT_MESSAGE = "Update .travis.yml to save builds in github releases"
    private String accessKey;
    private JsonSlurper jsonSlurper;

    public GithubHelper (String accessKey) {
        this.accessKey = accessKey
        this.jsonSlurper = new JsonSlurper()
    }

    public getUser() {
        String url = "${API_URL}/user"
        HttpURLConnection connection = requestToApi(url, "GET")
        
        def resBody = responseToJSON(connection.getInputStream())
        if (connection.getResponseMessage() != "OK") {
            throw new GithubHelperException("Http request returned an error ${responseMessage}")
        }
        return resBody
    }

    public String fork (Project project) {
        if (project.isRemote()) {
            try {
                String[] projectNameAndOwner = project.getOwnerAndName()
                String projectOwner = projectNameAndOwner[0]
                String projectName = projectNameAndOwner[1]

                String url = "${API_URL}/repos/${projectOwner}/${projectName}/forks"
                String responseMessage = requestToApi(url, "POST").getResponseMessage();
                if (responseMessage != "Accepted") {
                    throw new GithubHelperException("Http request returned an error ${responseMessage}")
                }
                return responseMessage
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new GithubHelperException("Error parsing project remote")
            }
        }
    }

    public getFile (String projectOwner, String projectName, String path) {
        String url = getContentApiUrl(projectOwner, projectName, path)
        HttpURLConnection response = requestToApi(url, "GET")
        String responseMessage = response.getResponseMessage()
        if (responseMessage != "OK") {
            throw new GithubHelperException("Http request returned an error ${responseMessage}")
        }
        
        def result = responseToJSON(response.getInputStream())
        result.content = convertToUTF8(result.content)

        return result
    }

    private responseToJSON(InputStream resInputStream) {
        def br = new BufferedReader(new InputStreamReader(resInputStream));
        return jsonSlurper.parseText(br.getText())
    }

    private String convertToUTF8(String string) {
        return new String(Base64.getMimeDecoder().decode(string))
    }

    public updateFile(String projectOwner, String projectName, String path, String fileSha, String content) {
        String url = getContentApiUrl(projectOwner, projectName, path)
        HttpURLConnection connection = requestToApi(url, "PUT")
        connection.setRequestProperty("Content-type", "application/json");
        connection.setDoOutput(true);
                
        def message = [message: COMMIT_MESSAGE, content: convertToBase64(content), sha: fileSha]

            
        PrintStream printStream = new PrintStream(connection.getOutputStream());
        printStream.println(JsonOutput.toJson(message));

        String responseMessage = connection.getResponseMessage()

        if (responseMessage != "OK") {
            throw new GithubHelperException("Http request returned an error ${responseMessage}")
        }
        return responseMessage
    }

    private String convertToBase64(String string) {
        return new String(Base64.getMimeEncoder().encode(string.getBytes("UTF-8")))
    }

    private getContentApiUrl(String projectOwner, String projectName, String path) {
        return "${API_URL}/repos/${projectOwner}/${projectName}/contents/${path}"
    }

    private HttpURLConnection requestToApi(String url, String method) {
        try {
            def request = new URL(url).openConnection();
            if (this.accessKey.length() > 0) {
                request.setRequestProperty("Authorization", getAuthorizationHeader())
            }
            request.setRequestMethod(method)
            return request
        } catch (IOException e) {
            throw new GithubHelperException("Error sending the HTTP request")
        } catch (UnknownHostException e) {
            throw new GithubHelperException("Unable to find request Host")
        }
    }
    
    private String getAuthorizationHeader() {
        return "token ${accessKey}"
    }

}