package util

import project.Project
import exception.*

class GithubHelper {

    public final String URL = "https://github.com"
    public final String API_URL = "https://api.github.com"
    private String accessKey;

    GithubHelper (String accessKey) {
        this.accessKey = accessKey
    }

    Object getUser() {
        String url = "${API_URL}/user"
        HttpURLConnection connection = HttpHelper.requestToApi(url, HttpHelper.METHOD_GET, this.accessKey)

        Object resBody = HttpHelper.responseToJSON(connection.getInputStream())

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new GithubHelperException("Http request returned an error ${connection.getResponseMessage()}")
        }

        return resBody
    }

    Object getRepository(Project project) {
        def result = null;
        if (project.isRemote()) {
            String[] projectNameAndOwner = project.getOwnerAndName()
            String projectOwner = projectNameAndOwner[0]
            String projectName = projectNameAndOwner[1]

            String url = getRepoApiUrl(projectOwner, projectName)
            HttpURLConnection httpConnection = HttpHelper.requestToApi(url, HttpHelper.METHOD_GET, this.accessKey)

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                result = HttpHelper.responseToJSON(httpConnection.getInputStream());
            } else if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                result = null;
            } else {
                throw new GithubHelperException("Http request returned an error " + httpConnection.getResponseMessage())
            }
        }
        return result
    }

    void enableActions(Project project) {
        if (project.isRemote()) {
            String[] projectOwnerAndName = project.getOwnerAndName()
            String projectOwner = projectOwnerAndName[0]
            String projectName = projectOwnerAndName[1]

            String url = getRepoApiUrl(projectOwner, projectName) + "/enable"
            print(url)
            HttpURLConnection connection = HttpHelper.requestToApi(url, HttpHelper.METHOD_POST, this.accessKey)

            if (connection.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED) {
                throw new GithubHelperException("Http request returned an error ${connection.getResponseMessage()}")
            }
        }
    }


    Object fork (Project project) {
        def result = null;
        if (project.isRemote()) {
            try {
                String[] projectNameAndOwner = project.getOwnerAndName()
                String projectOwner = projectNameAndOwner[0]
                String projectName = projectNameAndOwner[1]

                String url = getRepoApiUrl(projectOwner, projectName) + "/forks"
                HttpURLConnection connection = HttpHelper.requestToApi(url, HttpHelper.METHOD_POST, this.accessKey)

                if (connection.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED) {
                    throw new GithubHelperException("Http request returned an error ${connection.getResponseMessage()}")
                }

                result = HttpHelper.responseToJSON(connection.getInputStream())
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new GithubHelperException("Error parsing project remote")
            }
        }
        return result
    }

    Object getRepositoryReleases(String projectOwner, String projectName) {
        String url = getRepoApiUrl(projectOwner, projectName) + "/releases"   
        HttpURLConnection connection = HttpHelper.requestToApi(url, HttpHelper.METHOD_GET, this.accessKey);

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new GithubHelperException("Http request returned an error ${connection.getResponseMessage()}")
        }

        return HttpHelper.responseToJSON(connection.getInputStream())
    }

    private getRepoApiUrl(String projectOwner, String projectName) {
        return "${API_URL}/repos/${projectOwner}/${projectName}"
    }
}