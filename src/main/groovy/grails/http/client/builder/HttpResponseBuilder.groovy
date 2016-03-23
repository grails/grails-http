package grails.http.client.builder

import grails.http.HttpStatus
import grails.http.client.HttpClientResponse
import groovy.json.StreamingJsonBuilder
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufOutputStream
import io.netty.handler.codec.http.HttpHeaders
import io.netty.handler.codec.http.HttpResponseStatus

/**
 * Builds HTTP responses
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@InheritConstructors
@CompileStatic
class HttpResponseBuilder extends HttpClientResponse {

    /**
     * The writer for the response
     */
    @Lazy Writer writer = {
        ByteBuf buf = response.content()
        buf = buf.retain()
        def bufOutputStream = new ByteBufOutputStream(buf)
        return new OutputStreamWriter(bufOutputStream, charset)
    } ()

    /**
     * Sets an ok status
     *
     * @return This builder
     */
    HttpResponseBuilder ok() {
        response.setStatus(HttpResponseStatus.OK)
        return this
    }

    /**
     * Sets an UNAUTHORIZED status
     *
     * @return This builder
     */
    HttpResponseBuilder unauthorized() {
        response.setStatus(HttpResponseStatus.UNAUTHORIZED)
        return this
    }

    /**
     * Sets an FORBIDDEN status
     *
     * @return This builder
     */
    HttpResponseBuilder forbidden() {
        response.setStatus(HttpResponseStatus.FORBIDDEN)
        return this
    }

    /**
     * Sets a created status
     *
     * @return The created status
     */
    HttpResponseBuilder created() {
        response.setStatus(HttpResponseStatus.CREATED)
        return this
    }


    /**
     * Sets not found status
     *
     * @return The not found status
     */
    HttpResponseBuilder notFound() {
        response.setStatus(HttpResponseStatus.NOT_FOUND)
        return this
    }

    /**
     * Sets the status of the response
     *
     * @param code The status code
     * @return This builder
     */
    HttpResponseBuilder status(int code) {
        response.setStatus(HttpResponseStatus.valueOf(code))
        return this
    }

    /**
     * Sets the status of the response
     *
     * @param code The status code
     * @return This builder
     */
    HttpResponseBuilder status(HttpStatus status) {
        response.setStatus(HttpResponseStatus.valueOf(status.code))
        return this
    }

    /**
     * Builds a json body
     *
     * @param callable The closure representing the JSON
     * @return The builder
     */
    HttpResponseBuilder json(@DelegatesTo(StreamingJsonBuilder) Closure callable) {
        def builder = prepareJsonBuilder()
        builder.call callable
        writer.flush()
        return this
    }

    /**
     * Adds JSON to the body of the request
     * @param array The JSON array
     * @return This request
     */
    HttpResponseBuilder json(List array) {
        StreamingJsonBuilder builder = prepareJsonBuilder()
        builder.call(array)
        writer.flush()
        return this
    }

    protected StreamingJsonBuilder prepareJsonBuilder() {
        def headers = response.headers()
        if (!headers.contains(HttpHeaders.Names.CONTENT_TYPE)) {
            headers.add(HttpHeaders.Names.CONTENT_TYPE, "application/json")
        }
        StreamingJsonBuilder builder = new StreamingJsonBuilder(writer)
        builder
    }
}
