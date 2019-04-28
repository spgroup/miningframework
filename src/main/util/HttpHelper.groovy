package main.util

import java.net.URL
import java.net.UnknownHostException
import java.net.HttpURLConnection
import groovy.json.*
import java.util.Base64
import main.exception.HttpException

class HttpHelper {

    public static responseToJSON(InputStream resInputStream) {
        def br = new BufferedReader(new InputStreamReader(resInputStream));
        return (new JsonSlurper()).parseText(br.getText())
    }
    
    public static String convertToUTF8(String string) {
        return new String(Base64.getMimeDecoder().decode(string))
    }

    public static String convertToBase64(String string) {
        return new String(Base64.getMimeEncoder().encode(string.getBytes("UTF-8")))
    }

    public static jsonToString(Map json) {
        return JsonOutput.toJson(json)
    }

    public static void sendJsonBody(HttpURLConnection request, Map body) {
        request.setRequestProperty("Content-type", "application/json")
        request.setDoOutput(true)

        PrintStream printStream = new PrintStream(request.getOutputStream())
        printStream.println(jsonToString(body))
    }

    public static HttpURLConnection requestToApi(String url, String method, String token) {
        try {
            def request = new URL(url).openConnection();
            if (token.length() > 0) {
                request.setRequestProperty("Authorization", getAuthorizationHeader(token))
            }
            request.setRequestMethod(method)
            return request
        } catch (IOException e) {
            throw new HttpException("Error sending the HTTP request")
        } catch (UnknownHostException e) {
            throw new HttpException("Unable to find request Host")
        }
    }

    public static HttpURLConnection requestToApi(String url, String method) {
        try {
            def request = new URL(url).openConnection();
            request.setRequestMethod(method)
            return request
        } catch (IOException e) {
            throw new HttpException("Error sending the HTTP request")
        } catch (UnknownHostException e) {
            throw new HttpException("Unable to find request Host")
        }
    }
    
    public static String getAuthorizationHeader(String token) {
        return "token ${token}"
    }

}