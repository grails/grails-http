package grails.http.client.builder

import grails.http.HttpMethod
import grails.http.client.Configuration
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.DefaultFullHttpRequest
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.multipart.HttpPostBodyUtil
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder
import io.netty.handler.codec.http.multipart.MemoryFileUpload

import java.nio.charset.Charset
/**
 * Represents the HTTP request to be customized
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class HttpRequestBuilder extends HttpMessageBuilder<HttpRequestBuilder>{

    final FullHttpRequest request
    HttpRequest wrapped

    HttpRequestBuilder(final FullHttpRequest request, String encoding) {
        this(request, Charset.forName(encoding))
    }

    HttpRequestBuilder(final FullHttpRequest request, Charset charset) {
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

    /**
     * Builds a form
     * @param formDefinition The form definition
     * @return this object
     */
    HttpRequestBuilder form(@DelegatesTo(FormDataBuilder) Closure formDefinition) {
        if(formDefinition != null) {

            HttpPostRequestEncoder encoder = new HttpPostRequestEncoder(request, false)
            formDefinition.delegate = new FormDataBuilder(encoder, charset)
            formDefinition.call()
            wrapped = encoder.finalizeRequest()
        }
        return this
    }

    /**
     * Builds a multipart form
     * @param formDefinition The form definition
     * @return this object
     */
    HttpRequestBuilder multipart(@DelegatesTo(MultipartBuilder) Closure formDefinition) {
        if(formDefinition != null) {

            HttpPostRequestEncoder encoder = new HttpPostRequestEncoder(request, true)
            formDefinition.delegate = new MultipartBuilder(encoder, charset)
            formDefinition.call()
            wrapped = encoder.finalizeRequest()
        }
        return this
    }

    /**
     * Helps with building multipart requests
     */
    static class MultipartBuilder extends FormDataBuilder {
        static final List TEXT_TYPES = ['xml', 'json', 'txt', 'yml', 'csv', 'html', 'rss', 'hal', 'svg']
        static final Map COMMON_CONTENT_TYPES = [
                xml:'application/xml',
                json:'application/json',
                hal:'application/hal+json',
                rss:'application/rss+xml',
                yml:'application/yml',
                csv:'text/csv',
                css:'text/css',
                html:'text/html',
                txt:'text/plain',
                bin:'application/octet-stream',
                jpg:'image/jpeg',
                gif:'image/gif',
                png:'image/png',
                svg:'image/svg+xml'
        ]

        MultipartBuilder(HttpPostRequestEncoder encoder, Charset charset) {
            super(encoder, charset, true)
        }

        /**
         * Adds a file to the request body for the given name
         *
         * @param name The name within the multpart body
         * @param file The file itself
         * @param contentType The content type of the file. Defaults to `text/plain` for text content if not specified
         * @param isText Whether the file is text or binary
         *
         * @return This builder
         */
        FormDataBuilder file(String name, File file, String contentType = guessContentType(file.name), boolean isText = guessIsText(file.name)) {
            encoder.addBodyFileUpload(name, file, contentType, isText)
            return this
        }

        /**
         * Adds a file to the request body for the given name, filename and bytes
         * @param name The name within the multipart body
         * @param filename The file name
         * @param bytes The bytes of the file
         * @param contentType The content type, defaults to 'application/octet-stream'
         * @return This builder
         */
        FormDataBuilder file(String name, String filename, byte[] bytes, String contentType = guessContentType(filename, true) ) {
            def upload = new MemoryFileUpload(name, filename, contentType, "binary", null, bytes.length)
            upload.setContent( Unpooled.wrappedBuffer(bytes) )
            encoder.addBodyHttpData(upload)
            return this
        }

        /**
         * Adds a file to the request body for the given name, filename and bytes
         * @param name The name within the multipart body
         * @param filename The file name
         * @param bytes The bytes of the file as an input stream
         * @param contentType The content type, defaults to 'application/octet-stream'
         * @return This builder
         */
        FormDataBuilder file(String name, String filename, InputStream bytes, int length, String contentType = guessContentType(filename, true) ) {
            def upload = new MemoryFileUpload(name, filename, contentType, "binary", null, length)
            upload.setContent( bytes )
            encoder.addBodyHttpData(upload)
            return this
        }
        /**
         * Adds a file for the given name, filename and text
         *
         * @param name The name within the multipart body
         * @param filename The name of the file
         * @param body The body of the file as text
         * @param contentType The content type, defaults to `text/plain`
         * @param charset The charset
         * @return This builder
         */
        FormDataBuilder file(String name, String filename, CharSequence body, String contentType = guessContentType(filename), Charset charset = this.charset ) {
            def upload = new MemoryFileUpload(name, filename, contentType, null, charset, body.length())
            upload.setContent( Unpooled.copiedBuffer(body, charset))
            encoder.addBodyHttpData(upload)
            return this
        }

        protected String guessContentType(String filename, boolean binary = false) {
            def i = filename.lastIndexOf('.')
            String contentType = null
            if(i > -1) {
                contentType = COMMON_CONTENT_TYPES.get(filename.substring(i + 1)  )
            }

            if(contentType == null) {
                return binary ? 'application/octet-stream' : 'text/plain'
            }
            else {
                return contentType
            }
        }

        protected boolean guessIsText(String filename) {
            def i = filename.lastIndexOf('.')
            if(i > -1) {
                return TEXT_TYPES.contains( filename.substring(i + 1))
            }
            return false
        }

        @Override
        void setProperty(String property, Object newValue) {
            if(multipart && newValue instanceof File) {
                def f = (File) newValue
                encoder.addBodyFileUpload(property, f, guessContentType(f.name), guessIsText(f.name))
            }
            else {
                super.setProperty(property, newValue)
            }
        }
    }

    static class FormDataBuilder {

        final HttpPostRequestEncoder encoder
        final boolean multipart
        final Charset charset

        FormDataBuilder(HttpPostRequestEncoder encoder, Charset charset, boolean multipart = false) {
            this.encoder = encoder
            this.multipart = multipart
            this.charset = charset
        }

        @Override
        void setProperty(String property, Object newValue) {
            encoder.addBodyAttribute(property, newValue.toString())
        }

        @Override
        @CompileDynamic
        Object invokeMethod(String name, Object args) {
            if(args && args.size() == 1) {
                encoder.addBodyAttribute(name, args[0].toString())
            }
            throw new MissingMethodException(name,getClass(), args)
        }
    }
}
