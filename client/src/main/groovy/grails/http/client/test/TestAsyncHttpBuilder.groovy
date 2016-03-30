package grails.http.client.test

import grails.http.client.AsyncHttpBuilder
import grails.http.client.Configuration
import grails.http.client.HttpClientResponse
import grails.http.client.async.NettyPromise
import grails.http.client.builder.HttpRequestBuilder
import grails.http.client.builder.HttpResponseBuilder
import grails.http.client.cfg.DefaultConfiguration
import groovy.transform.CompileStatic
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.DefaultChannelPromise
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.http.*

/**
 * Allows for mocking and testing usages of {@link AsyncHttpBuilder} in application code
 *
 * <p>Below is an example:
 * <pre class="code">
 *   TestAsyncHttpBuilder client = new TestAsyncHttpBuilder()
 *   client.expect {
 *       uri '/foo/bar'
 *       method "POST"
 *       contentType 'application/json'
 *       json {
 *           title "Ping"
 *       }
 *   }.respond {
 *       created()
 *       json {
 *           title "Pong"
 *       }
 *   }
 *
 *    Promise<HttpClientResponse> p = client.post("https://localhost:8080/foo/bar") {
 *        contentType 'application/json'
 *        json {
 *            title "Ping"
 *        }
 *    }
 *
 *    assert client.verify()
 * </pre>
 *
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class TestAsyncHttpBuilder extends AsyncHttpBuilder {

    final EmbeddedChannel mockChannel
    final LinkedList<HttpRequest> expectedRequests = []
    final LinkedList<HttpResponse> expectedResponses = []

    TestAsyncHttpBuilder(Configuration configuration = new DefaultConfiguration(codecClass: (Class)null)) {
        super(configuration)
        mockChannel = new EmbeddedChannel(new AsyncHttpClientInitializer(configuration), new HttpResponseHandler(configuration))
    }

    @Override
    protected ChannelFuture doConnect(Bootstrap bootstrap, String host, int port) {
        mockChannel.outboundMessages().clear()
        mockChannel.inboundMessages().clear()
        def p = new DefaultChannelPromise(mockChannel)
        p.setSuccess()
        return p
    }

    @Override
    protected NettyPromise<HttpClientResponse> buildPromise(io.netty.util.concurrent.Promise promise) {
        def p = super.buildPromise(promise)
        writeResponse()
        return p
    }

    protected void writeResponse() {
        if(!expectedResponses.isEmpty()) {
            def httpResponse = expectedResponses.removeFirst()
            mockChannel.writeInbound(httpResponse)
        }
    }

    /**
     * Adds a new request expectation
     *
     * @param expectedRequest The expected request
     * @return A MockResponseBuilder
     */
    MockResponseBuilder expect(@DelegatesTo(HttpRequestBuilder) Closure expectedRequest) {
        def req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/")
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder(req, charset)
        expectedRequest.delegate = requestBuilder
        expectedRequest.call()


        expectedRequests.add(requestBuilder.wrapped ?: req)

        return new MockResponseBuilder(this)
    }

    /**
     * Adds a response to be produced
     *
     * @param callable The callable that defines the response
     * @return This instance
     */
    TestAsyncHttpBuilder respond(@DelegatesTo(HttpResponseBuilder) Closure callable) {
        new MockResponseBuilder(this).respond callable
        return this
    }

    /**
     * Verifies the expected requests were produced
     */
    boolean verify() {

        def outboundMessages = mockChannel.outboundMessages()
        def expectedTotal = expectedRequests.size()
        def actualTotal = outboundMessages.size()
        if(actualTotal != expectedTotal) {
            assert expectedTotal == actualTotal : "Expected $expectedTotal requests, but $actualTotal were executed"
        }
        int i = 0
        for(object in outboundMessages) {
            if(object instanceof HttpRequest) {

                HttpRequest expectedRequest = expectedRequests.get(i++)
                HttpRequest actualRequest = (HttpRequest)object

                verifyRequest(expectedRequest, actualRequest)
            }
            else {
                assert false : "Found non-request object among outbound messages"
            }
        }
        outboundMessages.clear()
        return true
    }

    protected void verifyRequest(HttpRequest expected, HttpRequest actual) {
        def expectedUri = expected.uri()
        def actualUri = actual.uri()

        assert expectedUri == actualUri: "Expected URI [$expectedUri] does not match actual URI [$actualUri]"

        def expectedMethod = expected.method()
        def actualMethod = actual.method()

        assert expectedMethod == actualMethod: "Expected method [$expectedMethod] does not match actual method [$actualMethod]"
        def expectedHeaders = expected.headers()
        def actualHeaders = actual.headers()
        for (header in expectedHeaders) {

            def headerName = header.key
            def expectedHeaderValue = header.value
            def actualHeaderValue = actualHeaders.get(headerName)
            if(expectedHeaderValue.startsWith('multipart/')) {
                // need to ignore the changeable boundary definition
                assert actualHeaderValue.startsWith("multipart/") : "expected a multipart request"
            }
            else {
                assert "$headerName: $expectedHeaderValue" == "$headerName: $actualHeaderValue"
            }
        }

        if((expected instanceof FullHttpRequest) && (actual instanceof FullHttpRequest)) {
            def expectedBody = expected.content()
            def actualBody = actual.content()
            if( expectedBody.hasArray() && !actualBody.hasArray() ) {
                assert false : "Expected content ${expectedBody.toString(charset)} but got none"
            }
            else {
                assert expectedBody.toString(charset) == actualBody.toString(charset)
            }
        }

    }
    /**
     * Allows construction of a mock response
     */
    static class MockResponseBuilder {
        final TestAsyncHttpBuilder parent

        MockResponseBuilder(TestAsyncHttpBuilder parent) {
            this.parent = parent
        }

        TestAsyncHttpBuilder respond(@DelegatesTo(HttpResponseBuilder) Closure callable) {
            def res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
            callable.delegate = new HttpResponseBuilder(res, parent.charset)
            callable.call()
            parent.expectedResponses.add(res)
            return parent
        }
    }
}
