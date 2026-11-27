package util

import project.Project
import exception.*

class GithubHelper {

    public final String URL = "https://github.com"
    public final String API_URL = "https://api.github.com"
    static final int RELEASES_PER_PAGE = 100 // the most the API returns in a page
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
            String url = getRepoApiUrl(project)
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

    Object getWorkFlowRuns(Project project) {
        def result = null
        if (project.isRemote()) {
            // for now this request only returns at maximum 100 builds
            // TODO: Change this method so that all runs are listed
            String url = getRepoApiUrl(project) + "/actions/runs?per_page=100"
            HttpURLConnection httpConnection = HttpHelper.requestToApi(url, HttpHelper.METHOD_GET, this.accessKey)

            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                result = HttpHelper.responseToJSON(httpConnection.getInputStream())
            } else if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                result = null
            } else {
                throw new GithubHelperException("Http request returned an error " + httpConnection.getResponseMessage())
            }
        }
        return result ? result.workflow_runs : null
    }

    Object fork (Project project) {
        def result = null;
        if (project.isRemote()) {
            try {
                String url = getRepoApiUrl(project) + "/forks"
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

    /**
     * Every release of the repository: the API lists them in pages (30 by default), so it reads 100 at a time until
     * a page that is not full, as scripts/fetch_jars.py did
     */
    List getRepositoryReleases(Project project) {
        String url = getRepoApiUrl(project) + "/releases"
        List releases = []
        for (int page = 1; ; page++) {
            List releasesOfPage = getRepositoryReleasesPage(url, page)
            releases.addAll(releasesOfPage)
            if (releasesOfPage.size() < RELEASES_PER_PAGE) {
                return releases
            }
        }
    }

    protected List getRepositoryReleasesPage(String url, int page) {
        HttpURLConnection connection = HttpHelper.requestToApi("${url}?per_page=${RELEASES_PER_PAGE}&page=${page}", HttpHelper.METHOD_GET, this.accessKey)

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new GithubHelperException("Http request returned an error ${connection.getResponseMessage()}")
        }

        return HttpHelper.responseToJSON(connection.getInputStream()) as List
    }

    Project getFork(Project project) {
        return new Project(project.getName(), "${URL}/${getUser().login}/${project.getOwnerAndName()[1]}")
    }

    private getRepoApiUrl(Project project) {
        String[] projectNameAndOwner = project.getOwnerAndName()
        String projectOwner = projectNameAndOwner[0]
        String projectName = projectNameAndOwner[1]

        return "${API_URL}/repos/${projectOwner}/${projectName}"
    }
}