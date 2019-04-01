package com.github.breadmoirai.githubreleaseplugin

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.apache.tika.Tika

class GithubApi {

    static String endpoint = "https://api.github.com"
    public static final MediaType MEDIATYPE_JSON = MediaType.parse("application/json")

    private final Map<String, String> defaultHeaders
    private final Tika tika = new Tika()

    GithubApi(CharSequence authorization) {
        this.defaultHeaders = [
                'Authorization': authorization.toString(),
                'User-Agent'   : 'breadmoirai github-release-gradle-plugin',
                'Accept'       : 'application/vnd.github.v3+json',
                'Content-Type' : 'application/json'
        ]
    }

    public static final OkHttpClient client = new OkHttpClient()

    /**
     * Opens the specified {@code url} and sends an http request after applying the {@code closure}.
     * The response is read and returned. The response body is parsed as JSON and represented as an field
     * in the returned {@link Response}.
     * @param url the api endpoint
     * @param closure a closure that adds any necessary configuration OkHttp Request
     * @return The response containing the status code, status message, response headers, and the body as an object
     */
    Response connect(String url, @DelegatesTo(Request.Builder) Closure closure) {
        client.newCall(new Request.Builder().tap {
            delegate.url url
            defaultHeaders.each { name, value ->
                header name, value
            }
            closure.setDelegate delegate
            closure()
        }.build()).execute().withCloseable { response ->
            new Response(response.code(), response.message(), response.body().string(), response.headers().toMultimap())
        }
    }

    Response findReleaseByTag(CharSequence owner, CharSequence repo, CharSequence tagName) {
        String releaseUrl = "$endpoint/repos/$owner/$repo/releases/tags/$tagName"
        println ':githubRelease CHECKING FOR PREVIOUS RELEASE'
        connect(releaseUrl) {
            get()
        }
    }

    Response findTagByName(CharSequence owner, CharSequence repo, CharSequence tagName) {
        String tagUrl = "$endpoint/repos/$owner/$repo/git/refs/tags/$tagName"
        connect(tagUrl) {
            get()
        }
    }

    Response deleteReleaseByUrl(String url) {
        println ':githubRelease DELETING RELEASE'
        connect(url) {
            delete()
        }
    }

    Response uploadFileToUrl(String url, File asset) {
        println ':githubRelease UPLOADING ' + asset.name
        connect(url) {
            put RequestBody.create(MediaType.parse(tika.detect(asset)), asset)
        }
    }

    Response postRelease(CharSequence owner, CharSequence repo, Map data) {
        String releaseUrl = "$endpoint/repos/$owner/$repo/releases"
        println ':githubRelease CREATING NEW RELEASE'
        connect(releaseUrl) {
            post RequestBody.create(MEDIATYPE_JSON, JsonOutput.toJson(data))
        }
    }

    Response getReleases(CharSequence owner, CharSequence repo) {
        String releaseUrl = "$endpoint/repos/$owner/$repo/releases"
        println ':githubRelease RETRIEVING RELEASES ' + releaseUrl
        connect(releaseUrl) {
            get()
        }
    }

    Response getCommits(CharSequence owner, CharSequence repo) {
        String commitsUrl = "$endpoint/repo/$owner/$repo/commits"
        println ':githubRelease RETRIEVING COMMITS ' + commitsUrl
        connect(commitsUrl) {
            get()
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
