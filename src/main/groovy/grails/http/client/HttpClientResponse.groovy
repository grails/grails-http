package grails.http.client

import grails.http.HttpStatus
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import io.netty.buffer.ByteBufInputStream
import io.netty.handler.codec.http.FullHttpResponse

import java.nio.charset.Charset

/**
 * Represents an HTTP response
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class HttpClientResponse {

    final FullHttpResponse response
    final Charset charset

    HttpClientResponse(FullHttpResponse response) {
        this(response, "UTF-8")
    }

    HttpClientResponse(FullHttpResponse response, String encoding) {
        this(response, Charset.forName(encoding))
    }

    HttpClientResponse(FullHttpResponse response, Charset charset) {
        this.response = response
        this.charset = charset
    }
    /**
     * @return The JSON representation of the response body
     */
    Object getJson() {
        new JsonSlurper().parse(getInputStream(), charset.toString())
    }

    /**
     * Obtain an input stream from the body
     *
     * @return An {@link InputStream}
     */
    InputStream getInputStream() {
        new ByteBufInputStream(response.content())
    }


    /**
     * @return The textual content of the body
     */
    String getText() {
        response.content().toString(charset)
    }

    /**
     * @return The textual content of the body
     */
    String getText(Charset charset) {
        response.content().toString(charset)
    }

    /**
     * Obtain a value of a header
     *
     * @param name The header name
     * @return The value
     */
    String getHeader(String name) {
        response.headers().get(name)
    }

    /**
     * Obtain a value of a header
     *
     * @param name The header name
     * @return The value
     */
    String header(String name) {
        response.headers().get(name)
    }

    /**
     * @return The status code of the response
     */
    int getStatusCode() {
        response.status.code()
    }

    /**
     * @return The returned http status object
     */
    HttpStatus getStatus() {
        HttpStatus.valueOf(response.status.code())
    }
}

