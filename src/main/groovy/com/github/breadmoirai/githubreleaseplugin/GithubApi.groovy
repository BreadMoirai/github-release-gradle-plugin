package com.github.breadmoirai.githubreleaseplugin

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import javax.net.ssl.HttpsURLConnection
import java.nio.file.Files

class GithubApi {

    public static String endpoint = "https://api.github.com"

    private final Map<String, String> defaultHeaders

    GithubApi(String authorization) {
        this.defaultHeaders = [
                'Authorization': authorization,
                'User-Agent'   : 'breadmoirai github-release-gradle-plugin',
                'Accept'       : 'application/vnd.github.v3+json',
                'Content-Type' : 'application/json'
        ]
    }

    Response openConnection(String url, @DelegatesTo(HttpsURLConnection) Closure closure) {
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

    Response findGithubReleaseByTag(CharSequence owner, CharSequence repo, CharSequence tagName) {
        String releaseUrl = "$endpoint/repos/$owner/$repo/releases/tags/$tagName"
        println ':githubRelease CHECKING FOR PREVIOUS RELEASE ' + releaseUrl
        openConnection(releaseUrl) {
            requestMethod = 'GET'
        }
    }

    Response deleteGithubReleaseByUrl(String url) {
        println 'githubRelease DELETING RELEASE ' + url
        openConnection(url) {
            requestMethod = "DELETE"
        }
    }

    Response uploadFileToUrl(String url, File asset) {
        println ':githubRelease UPLOADING ' + asset.name
        openConnection(url) {
            requestMethod = "PUT"
            setRequestProperty('Content-Type', Files.probeContentType(asset.toPath()))
            doOutput = true
            Files.copy(asset.toPath(), outputStream)
        }
    }

    Response postRelease(String owner, String repo, Map data) {
        String releaseUrl = "$endpoint/repos/$owner/$repo/releases"
        println ':githubRelease CREATING NEW RELEASE'
        openConnection(releaseUrl) {
            requestMethod = "POST"
            doOutput = true
            outputStream.withPrintWriter {
                it.write(JsonOutput.toJson(data))
            }
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
