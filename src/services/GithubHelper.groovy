package services

import main.project.Project
import java.net.URL
import java.net.UnknownHostException

class GithubHelper {

    private final String API_URL = "https://api.github.com"
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
                def post = new URL(url).openConnection();
                post.setRequestProperty("Authorization", getAuthorizationHeader())
                post.setRequestMethod("POST")
                int responseCode = post.getResponseCode();
                if (responseCode != 202) {
                    throw UnableToForkException("Http request returned ${responseCode}")
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                throw UnableToForkException("Error parsing project remote")
            } catch (IOException e) {
                throw UnableToForkException("Error sending the HTTP request")
            } catch (UnknownHostException e) {
                throw UnableToForkException("Unable to find request Host")
            }
        }
    }

    private String getAuthorizationHeader() {
        return "token ${accessKey}"
    }

}