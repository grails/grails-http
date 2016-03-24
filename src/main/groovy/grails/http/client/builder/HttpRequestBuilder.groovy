package grails.http.client.builder

import grails.http.HttpMethod
import groovy.transform.CompileStatic
import io.netty.handler.codec.http.DefaultFullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames

import java.nio.charset.Charset
/**
 * Represents the HTTP request to be customized
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class HttpRequestBuilder extends HttpMessageBuilder<HttpRequestBuilder>{

    final DefaultFullHttpRequest request

    HttpRequestBuilder(final DefaultFullHttpRequest request, String encoding) {
        this(request, Charset.forName(encoding))
    }

    HttpRequestBuilder(final DefaultFullHttpRequest request, Charset charset) {
        super(request, charset)
        this.request = request
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
        header HttpHeaderNames.AUTHORIZATION, "Basic $encoded".toString()
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
        header HttpHeaderNames.ACCEPT, contentTypes.join(',')
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
        header HttpHeaderNames.AUTHORIZATION, accessToken
        return this
    }
}
