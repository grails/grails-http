package grails.http.client.builder

import grails.http.HttpMethod
import groovy.json.StreamingJsonBuilder
import groovy.transform.CompileStatic
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufOutputStream
import io.netty.handler.codec.http.DefaultFullHttpRequest
import io.netty.handler.codec.http.HttpHeaders

import java.nio.charset.Charset

/**
 * Represents the HTTP request to be customized
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class HttpRequestBuilder {

    final DefaultFullHttpRequest request
    final Charset charset
    /**
     * The writer for the request body
     */
    @Lazy Writer writer = {
        ByteBuf buf = request.content()
        buf = buf.retain()
        def bufOutputStream = new ByteBufOutputStream(buf)
        return new OutputStreamWriter(bufOutputStream, charset)
    } ()


    HttpRequestBuilder(final DefaultFullHttpRequest request, String encoding) {
        this(request, Charset.forName(encoding))
    }

    HttpRequestBuilder(final DefaultFullHttpRequest request, Charset charset) {
        this.request = request
        this.charset = charset
    }

    /**
     * Sets the URI of the request
     * @param uri The uri of the request
     * @return the request
     */
    HttpRequestBuilder uri(CharSequence uri) {
        request.setUri(uri.toString())
        return this
    }

    /**
     * Sets the method of the request
     * @param method The HTTP method
     * @return This request
     */
    HttpRequestBuilder method(CharSequence method) {
        request.setMethod(io.netty.handler.codec.http.HttpMethod.valueOf(method.toString()))
        return this
    }

    /**
     * Sets the method of the request
     * @param method The HTTP method
     * @return This request
     */
    HttpRequestBuilder method(HttpMethod method) {
        request.setMethod(io.netty.handler.codec.http.HttpMethod.valueOf(method.name()))
        return this
    }

    /**
     * Used to configure BASIC authentication. Example:
     *
     * <pre><code>
     * builder.put("http://..") {
     *      auth "myuser", "mypassword"
     * }
     * </code></pre>
     *
     * @param username The username
     * @param password The password
     * @return The request
     */
    HttpRequestBuilder auth(String username, String password) {
        String usernameAndPassword = "$username:$password"
        String encoded = new String(Base64.getEncoder().encode(usernameAndPassword.bytes))
        header HttpHeaders.Names.AUTHORIZATION, "Basic $encoded".toString()
        return this
    }


    /**
     * Sets the Accept HTTP header to the given value. Example:
     *
     * <pre><code>
     * restBuilder.get("http://..") {
     *      accept "application/xml"
     * }
     * </code></pre>
     *
     * @param contentTypes The content types
     * @return The customizer
     */
    HttpRequestBuilder accept(CharSequence... contentTypes) {
        header HttpHeaders.Names.ACCEPT, contentTypes.join(',')
        return this
    }

    /**
     * Sets the Authorization HTTP header to the given value. Used typically to pass OAuth access tokens.
     *
     * <pre><code>
     * builder.put("http://..") {
     *      auth myToken
     * }
     * </code></pre>
     *
     * @param accessToken The access token
     * @return The customizer
     */
    HttpRequestBuilder auth(CharSequence accessToken) {
        header HttpHeaders.Names.AUTHORIZATION, accessToken
        return this
    }

    /**
     * Sets the content type for the request
     *
     * @param contentType The content type
     */
    HttpRequestBuilder contentType(CharSequence contentType) {
        request.headers().add(HttpHeaders.Names.CONTENT_TYPE, contentType)
        return this
    }

    /**
     * Sets a request header
     *
     * @param name The name of the header
     * @param value The value of the header
     * @return This request
     */
    HttpRequestBuilder header(String name, value) {
        request.headers().add(name, value)
        return this
    }

    /**
     * Adds JSON to the body of the request
     * @param callable The callable that defines the JSON
     * @return
     */
    HttpRequestBuilder json(@DelegatesTo(StreamingJsonBuilder) Closure callable) {
        StreamingJsonBuilder builder = prepareJsonBuilder()
        builder.call(callable)
        writer.flush()
        return this
    }

    /**
     * Adds JSON to the body of the request
     * @param array The JSON array
     * @return This request
     */
    HttpRequestBuilder json(List array) {
        StreamingJsonBuilder builder = prepareJsonBuilder()
        builder.call(array)
        writer.flush()
        return this
    }

    /**
     * Adds JSON to the body of the request
     * @param json The JSON as a map
     * @return This request
     */
    HttpRequestBuilder json(Map json) {
        StreamingJsonBuilder builder = prepareJsonBuilder()
        builder.call(json)
        writer.flush()
        return this
    }

    protected StreamingJsonBuilder prepareJsonBuilder() {
        def headers = request.headers()
        if (!headers.contains(HttpHeaders.Names.CONTENT_TYPE)) {
            headers.add(HttpHeaders.Names.CONTENT_TYPE, "application/json")
        }
        StreamingJsonBuilder builder = new StreamingJsonBuilder(writer)
        builder
    }


}
