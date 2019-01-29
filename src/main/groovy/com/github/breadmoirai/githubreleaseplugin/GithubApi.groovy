package com.github.breadmoirai.githubreleaseplugin

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import javax.net.ssl.HttpsURLConnection
import java.nio.file.Files

class GithubApi {

    public static String endpoint = "https://api.github.com"

    private final Map<String, String> defaultHeaders

    GithubApi(CharSequence authorization) {
        this.defaultHeaders = [
                'Authorization': authorization.toString(),
                'User-Agent'   : 'breadmoirai github-release-gradle-plugin',
                'Accept'       : 'application/vnd.github.v3+json',
                'Content-Type' : 'application/json'
        ]
    }

    /**
     * Opens the specified {@code url} and sends an http request after applying the {@code closure}.
     * The response is read and returned. The response body is parsed as JSON and represented as an field
     * in the returned {@link Response}.
     * @param url the api endpoint
     * @param closure a closure that adds any necessary configuration to the url connection such as the requestMethod
     * @return The response containing the status code, status message, response headers, and the body as an object
     */
    Response connect(String url, @DelegatesTo(HttpsURLConnection) Closure closure) {
        (new URL(url).openConnection() as HttpsURLConnection).with { connection ->
            defaultHeaders.forEach { key, value ->
                setRequestProperty key, value
            }
            closure.setDelegate(connection)
            closure()
            def code = responseCode
            if (code >= 400) {
                return new Response(code, responseMessage, errorStream.text, headerFields)
            } else {
                return new Response(code, responseMessage, inputStream.text, headerFields)
            }
        }
    }

    Response findReleaseByTag(CharSequence owner, CharSequence repo, CharSequence tagName) {
        String releaseUrl = "$endpoint/repos/$owner/$repo/releases/tags/$tagName"
        println ':githubRelease CHECKING FOR PREVIOUS RELEASE ' + releaseUrl
        connect(releaseUrl) {
            requestMethod = 'GET'
        }
    }

    Response deleteReleaseByUrl(String url) {
        println 'githubRelease DELETING RELEASE ' + url
        connect(url) {
            requestMethod = "DELETE"
        }
    }

    Response uploadFileToUrl(String url, File asset) {
        println ':githubRelease UPLOADING ' + asset.name
        connect(url) {
            requestMethod = "PUT"
            setRequestProperty('Content-Type', Files.probeContentType(asset.toPath()))
            doOutput = true
            Files.copy(asset.toPath(), outputStream)
        }
    }

    Response postRelease(CharSequence owner, CharSequence repo, Map data) {
        String releaseUrl = "$endpoint/repos/$owner/$repo/releases"
        println ':githubRelease CREATING NEW RELEASE'
        connect(releaseUrl) {
            requestMethod = "POST"
            doOutput = true
            outputStream.withPrintWriter {
                it.write(JsonOutput.toJson(data))
            }
        }
    }
    
    Response getReleases(CharSequence owner, CharSequence repo) {
        String releaseUrl = "$endpoint/repos/$owner/$repo/releases"
        println ':githubRelease RETRIEVING RELEASES ' + releaseUrl
        connect(releaseUrl) {
            requestMethod = "GET"
        }
    }

    Response getCommits(CharSequence owner, CharSequence repo) {
        String commitsUrl = "$endpoint/repo/$owner/$repo/commits"
        println ':githubRelease RETRIEVING COMMITS ' + commitsUrl
        connect(commitsUrl) {
            requestMethod = "GET"
        }
    }

    static class Response {

        final int code
        final String message
        final Object body
        final Map<String, List<String>> headers

        Response(int code, String message, String body, Map<String, List<String>> headers) {
            this.code = code
            this.message = message
            this.body = body ? new JsonSlurper().parseText(body) : ""
            this.headers = headers
        }

    }
}
