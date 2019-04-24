package services

import main.project.Project
import java.net.URL
import java.net.UnknownHostException
import java.net.HttpURLConnection

class GithubHelper {

    private final String API_URL = "https://api.github.com"
    private final String RAW_CONTENT_URL = "https://raw.githubusercontent.com"
    private String accessKey;

    public GithubHelper (String accessKey) {
        this.accessKey = accessKey
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

    private String getFileContent (String projectOwner, String projectName, String path) {
        String url = "${RAW_CONTENT_URL}/${projectOwner}/${projectName}/master/${path}"
        HttpURLConnection response = requestToApi(url, "GET")
        String responseMessage = response.getResponseMessage()
        if (responseMessage != "OK") {
            throw new GithubHelperException("Http request returned an error ${responseMessage}")
        }
        
        def br = new BufferedReader(new InputStreamReader(response.getInputStream()));
        println br.getText()
    }

    private HttpURLConnection requestToApi(String url, String method) {
        try {
            def request = new URL(url).openConnection();
            request.setRequestProperty("Authorization", getAuthorizationHeader())
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