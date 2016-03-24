package grails.http.client.builder

import grails.http.HttpHeader
import groovy.json.StreamingJsonBuilder
import groovy.transform.CompileStatic
import groovy.util.slurpersupport.GPathResult
import groovy.xml.StreamingMarkupBuilder
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufOutputStream
import io.netty.handler.codec.http.FullHttpMessage
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaders

import java.nio.charset.Charset

/**
 * Abstract class for building HTTP messages
 *
 * @param <T> The implementing type
 */
@CompileStatic
abstract class HttpMessageBuilder<T> {

    final FullHttpMessage httpMessage
    final Charset charset
    /**
     * The writer for the request body
     */
    @Lazy Writer writer = {
        ByteBuf buf = httpMessage.content()
        buf = buf.retain()
        def bufOutputStream = new ByteBufOutputStream(buf)
        return new OutputStreamWriter(bufOutputStream, charset)
    } ()

    HttpMessageBuilder(FullHttpMessage httpMessage, Charset charset) {
        this.httpMessage = httpMessage
        this.charset = charset
    }
    /**
     * Sets the content type for the request
     *
     * @param contentType The content type
     */
    T contentType(CharSequence contentType) {
        httpMessage.headers().add(HttpHeaderNames.CONTENT_TYPE, contentType)
        return (T)this
    }

    /**
     * Sets a request header
     *
     * @param name The name of the header
     * @param value The value of the header
     * @return This request
     */
    T header(CharSequence name, value) {
        httpMessage.headers().add(name, value)
        return (T)this
    }


    /**
     * Sets a request header
     *
     * @param name The name of the header
     * @param value The value of the header
     * @return This request
     */
    T header(HttpHeader header, value) {
        httpMessage.headers().add(header, value)
        return (T)this
    }
    /**
     * Adds JSON to the body of the request
     * @param callable The callable that defines the JSON
     * @return
     */
    T json(@DelegatesTo(StreamingJsonBuilder) Closure callable) {
        StreamingJsonBuilder builder = prepareJsonBuilder()
        builder.call(callable)
        writer.flush()
        return (T)this
    }

    /**
     * Adds JSON to the body of the request
     * @param array The JSON array
     * @return This request
     */
    T json(List array) {
        StreamingJsonBuilder builder = prepareJsonBuilder()
        builder.call(array)
        writer.flush()
        return (T)this
    }

    /**
     * Adds JSON to the body of the request
     * @param json The JSON as a map
     * @return This request
     */
    T json(Map json) {
        StreamingJsonBuilder builder = prepareJsonBuilder()
        builder.call(json)
        writer.flush()
        return (T)this
    }

    /**
     * Adds JSON to the body of the request
     * @param json The JSON as a map
     * @return This request
     */
    T json(String json) {
        defaultJsonContentType(httpMessage.headers())
        writer.write(json)
        writer.flush()
        return (T)this
    }


    /**
     * Sets the body of the request to the XML defined by the closure. Uses {@link groovy.xml.StreamingMarkupBuilder} to produce the XML
     *
     * @param closure The closure that defines the XML
     * @return This customizer
     */
    T xml(@DelegatesTo(StreamingMarkupBuilder)Closure closure) {
        def b = new StreamingMarkupBuilder()
        Writable markup = (Writable)b.bind(closure)
        markup.writeTo(writer)
        writer.flush()
        defaultContentType(httpMessage.headers(), "application/xml")
        return (T)this
    }

    /**
     * Sets the body of the request to the XML string argument.
     *
     * @param xml The XML to be used as the body of the request
     * @return This customizer
     */
    T xml(String xml) {
        writer.write(xml)
        writer.flush()
        defaultContentType(httpMessage.headers(), "application/xml")
        return (T)this
    }

    /**
     * Sets the body of the request to the XML GPathResult argument.
     *
     * @param xml The XML to be used as the body of the request
     * @return This customizer
     */
    T xml(GPathResult xml) {
        xml.writeTo(writer)
        writer.flush()
        defaultContentType(httpMessage.headers(), "application/xml")
        return (T)this
    }

    protected StreamingJsonBuilder prepareJsonBuilder() {
        def headers = httpMessage.headers()
        defaultJsonContentType(headers)
        StreamingJsonBuilder builder = new StreamingJsonBuilder(writer)
        builder
    }

    protected void defaultJsonContentType(HttpHeaders headers) {
        defaultContentType(headers, "application/json")
    }

    protected void defaultContentType(HttpHeaders headers, String contentType) {
        if (!headers.contains(HttpHeaderNames.CONTENT_TYPE)) {
            headers.add(HttpHeaderNames.CONTENT_TYPE, contentType)
        }
    }
}
