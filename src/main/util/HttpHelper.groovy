package util

import exception.HttpException
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class HttpHelper {

    public final static String METHOD_GET = "GET"
    public final static String METHOD_POST = "POST"
    public final static String METHOD_PUT = "PUT"

    static Object responseToJSON(InputStream resInputStream) {
        def bufferedReader = new BufferedReader(new InputStreamReader(resInputStream))
        return (new JsonSlurper()).parseText(bufferedReader.getText())
    }

    static jsonToString(Map json) {
        return JsonOutput.toJson(json)
    }

    static void sendJsonBody(HttpURLConnection request, Map body) {
        request.setRequestProperty("Content-type", "application/json")
        request.setDoOutput(true)

        PrintStream printStream = new PrintStream(request.getOutputStream())
        printStream.println(jsonToString(body))
    }

    static HttpURLConnection requestToApi(String url, String method, String token) {
        try {
            def request = new URL(url).openConnection()
            if (token != null && token.length() > 0) {
                request.setRequestProperty("Authorization", getAuthorizationHeader(token))
            }
            request.setRequestMethod(method)
            return request
        } catch (IOException e) {
            throw new HttpException("Error sending the HTTP request: " + e.message)
        } catch (UnknownHostException e) {
            throw new HttpException("Unable to find request Host")
        }
    }

    static HttpURLConnection requestToApi(String url, String method) {
        return HttpHelper.requestToApi(url, method, null);
    }

    static String getAuthorizationHeader(String token) {
        return "token ${token}"
    }

}