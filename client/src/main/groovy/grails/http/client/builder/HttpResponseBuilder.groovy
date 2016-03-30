package grails.http.client.builder

import grails.http.HttpStatus
import groovy.transform.CompileStatic
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpResponseStatus

import java.nio.charset.Charset
/**
 * Builds HTTP responses
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class HttpResponseBuilder extends HttpMessageBuilder<HttpResponseBuilder> {


    final FullHttpResponse response

    HttpResponseBuilder(FullHttpResponse response, Charset charset) {
        super(response, charset)
        this.response = response
    }

    /**
     * Sets an ok status
     *
     * @return This builder
     */

    HttpResponseBuilder ok() {
        this.response.setStatus(HttpResponseStatus.OK)
        return this
    }

    /**
     * Sets an UNAUTHORIZED status
     *
     * @return This builder
     */
    HttpResponseBuilder unauthorized() {
        this.response.setStatus(HttpResponseStatus.UNAUTHORIZED)
        return this
    }

    /**
     * Sets an FORBIDDEN status
     *
     * @return This builder
     */
    HttpResponseBuilder forbidden() {
        this.response.setStatus(HttpResponseStatus.FORBIDDEN)
        return this
    }

    /**
     * Sets a created status
     *
     * @return The created status
     */
    HttpResponseBuilder created() {
        this.response.setStatus(HttpResponseStatus.CREATED)
        return this
    }


    /**
     * Sets not found status
     *
     * @return The not found status
     */
    HttpResponseBuilder notFound() {
        this.response.setStatus(HttpResponseStatus.NOT_FOUND)
        return this
    }

    /**
     * Sets the status of the response
     *
     * @param code The status code
     * @return This builder
     */
    HttpResponseBuilder status(int code) {
        this.response.setStatus(HttpResponseStatus.valueOf(code))
        return this
    }

    /**
     * Sets the status of the response
     *
     * @param code The status code
     * @return This builder
     */
    HttpResponseBuilder status(HttpStatus status) {
        this.response.setStatus(HttpResponseStatus.valueOf(status.code))
        return this
    }
}
