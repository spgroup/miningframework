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
    private String accessKey;
    private JsonSlurper jsonSlurper;

    public GithubHelper (String accessKey) {
        this.accessKey = accessKey
        this.jsonSlurper = new JsonSlurper()
    }

    public void fork (Project project) {
        if (project.isRemote()) {
            try {
                String[] splitedPath = project.getPath().split("/");
                String projectOwner = splitedPath[splitedPath.length - 2]
                String projectName = splitedPath[splitedPath.length - 1]

                String url = "${API_URL}/repos/${projectOwner}/${projectName}/forks"
                String responseMessage = requestToApi(url, "POST").getResponseMessage();
                println responseMessage
                if (responseMessage != "Accepted") {
                    throw new GithubHelperException("Http request returned an error ${responseMessage}")
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new GithubHelperException("Error parsing project remote")
            }
        }
    }

    private getFile (String projectOwner, String projectName, String path) {
        String url = "${API_URL}/repos/${projectOwner}/${projectName}/contents/${path}"
        HttpURLConnection response = requestToApi(url, "GET")
        String responseMessage = response.getResponseMessage()
        if (responseMessage != "OK") {
            throw new GithubHelperException("Http request returned an error ${responseMessage}")
        }
        
        def br = new BufferedReader(new InputStreamReader(response.getInputStream()));
        def result =  jsonSlurper.parseText(br.getText())
        result.content = convertToUTF8(result.content)

        return result
    }

    private String convertToUTF8(String string) {
        return new String(Base64.getMimeDecoder().decode(string))
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