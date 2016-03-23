package grails.http.client

import grails.http.client.async.NettyPromise
import grails.http.client.builder.HttpRequestBuilder
import grails.http.client.cfg.DefaultConfiguration
import groovy.transform.CompileStatic
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslHandler
import io.netty.util.concurrent.Promise

import javax.net.ssl.SSLEngine
import java.nio.charset.Charset

/**
 * An asynchronous REST client that leverages Netty for non-blocking IO
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class AsyncHttpBuilder {


    final Configuration configuration
    final Charset charset
    protected final Bootstrap bootstrap
    protected final EventLoopGroup group

    /**
     * Constructs a new AsyncHttpBuilder instance for the given configuration
     *
     * @param configuration The configuration
     */
    AsyncHttpBuilder(Configuration configuration = new DefaultConfiguration()) {
        this.bootstrap = new Bootstrap()
        this.configuration = configuration
        group = new NioEventLoopGroup(configuration.numOfThreads, configuration.threadFactory)
        this.bootstrap.group(group)
                 .channel(NioSocketChannel.class)
                 .option(ChannelOption.SO_KEEPALIVE, true)

        for(entry in configuration.channelOptions) {
            bootstrap.option(ChannelOption.newInstance(entry.key), entry.value)
        }
        this.charset = Charset.forName(configuration.encoding)
        buildSslContext(configuration)
    }


    /**
     * Executes a GET request to the given URI with an optional customizer
     *
     * @param uri The URI
     * @param customizer The customizer
     * @return A Promise to return a {@link HttpClientResponse}
     */
    grails.async.Promise<HttpClientResponse> get(String uri, @DelegatesTo(HttpRequestBuilder) Closure customizer = null) {
        return doRequest(HttpMethod.GET, customizer, uri)
    }

    /**
     * Executes a POST request to the given URI with an optional customizer
     *
     * @param uri The URI
     * @param customizer The customizer
     * @return A Promise to return a {@link HttpClientResponse}
     */
    grails.async.Promise<HttpClientResponse> post(String uri, @DelegatesTo(HttpRequestBuilder) Closure customizer = null) {
        return doRequest(HttpMethod.POST, customizer, uri)
    }

    /**
     * Executes a PUT request to the given URI with an optional customizer
     *
     * @param uri The URI
     * @param customizer The customizer
     * @return A Promise to return a {@link HttpClientResponse}
     */
    grails.async.Promise<HttpClientResponse> put(String uri, @DelegatesTo(HttpRequestBuilder) Closure customizer = null) {
        return doRequest(HttpMethod.PUT, customizer, uri)
    }

    /**
     * Executes a PATCH request to the given URI with an optional customizer
     *
     * @param uri The URI
     * @param customizer The customizer
     * @return A Promise to return a {@link HttpClientResponse}
     */
    grails.async.Promise<HttpClientResponse> patch(String uri, @DelegatesTo(HttpRequestBuilder) Closure customizer = null) {
        return doRequest(HttpMethod.PATCH, customizer, uri)
    }

    /**
     * Executes a DELETE request to the given URI with an optional customizer
     *
     * @param uri The URI
     * @param customizer The customizer
     * @return A Promise to return a {@link HttpClientResponse}
     */
    grails.async.Promise<HttpClientResponse> delete(String uri, @DelegatesTo(HttpRequestBuilder) Closure customizer = null) {
        return doRequest(HttpMethod.DELETE, customizer, uri)
    }

    /**
     * Executes a HEAD request to the given URI with an optional customizer
     *
     * @param uri The URI
     * @param customizer The customizer
     * @return A Promise to return a {@link HttpClientResponse}
     */
    grails.async.Promise<HttpClientResponse> head(String uri, @DelegatesTo(HttpRequestBuilder) Closure customizer = null) {
        return doRequest(HttpMethod.HEAD, customizer, uri)
    }

    /**
     * Executes a OPTIONS request to the given URI with an optional customizer
     *
     * @param uri The URI
     * @param customizer The customizer
     * @return A Promise to return a {@link HttpClientResponse}
     */
    grails.async.Promise<HttpClientResponse> options(String uri, @DelegatesTo(HttpRequestBuilder) Closure customizer = null) {
        return doRequest(HttpMethod.OPTIONS, customizer, uri)
    }

    protected NettyPromise<HttpClientResponse> doRequest(HttpMethod httpMethod, Closure customizer, String uri) {
        final URI uriObject = new URI(uri)

        SslContext sslCtx = buildSslContext(uriObject)
        ChannelFuture channelFuture = doConnect(uriObject, sslCtx)

        Promise promise = newPromise(channelFuture)


        channelFuture.addListener(new HttpConnectionListener(uriObject, promise, customizer, configuration, httpMethod))

        return buildPromise(promise)
    }

    protected Promise newPromise(ChannelFuture channelFuture) {
        Channel channel = channelFuture
                .channel()

        def promise = channel
                .eventLoop()
                .newPromise()
        promise
    }

    protected SslContext buildSslContext(URI uriObject) {
        final SslContext sslCtx
        if (uriObject.scheme == 'https') {
            sslCtx = buildSslContext(configuration)
        } else {
            sslCtx = null
        }
        sslCtx
    }

    protected ChannelFuture doConnect(URI uri, SslContext sslCtx) {
        String host = uri.host
        int port = uri.port > -1 ? uri.port : sslCtx != null ? 443 : 80

        ChannelFuture channelFuture = doConnect(host, port, sslCtx)
        channelFuture
    }

    protected NettyPromise<HttpClientResponse> buildPromise(Promise promise) {
        new NettyPromise<HttpClientResponse>((Promise<HttpClientResponse>) promise)
    }

    /**
     * Creates an initial connection to the given remote host
     *
     * @param host The host
     * @param port The port
     * @param sslCtx The SslContext instance
     *
     * @return A ChannelFuture
     */
    protected ChannelFuture doConnect(String host, int port, SslContext sslCtx) {
        Bootstrap localBootstrap = this.bootstrap.clone()
        localBootstrap.handler(new AsyncHttpClientInitializer(sslCtx, configuration))
        return doConnect(localBootstrap, host, port)
    }

    /**
     * Creates an initial connection with the given bootstrap and remote host
     *
     * @param bootstrap The bootstrap instance
     * @param host The host
     * @param port The port
     * @return The ChannelFuture
     */
    protected ChannelFuture doConnect(Bootstrap bootstrap, String host, int port) {
        ChannelFuture channelFuture = bootstrap.connect(host, port)
        return channelFuture
    }

    /**
     * Builds an {@link SslContext} from the {@link Configuration}
     *
     * @param configuration The configuration instance
     * @return The {@link SslContext} instance
     */
    protected SslContext buildSslContext(Configuration configuration) {
        SslContextBuilder.forClient()
                .sslProvider(configuration.sslProvider)
                .sessionCacheSize(configuration.sslSessionCacheSize)
                .sessionTimeout(configuration.sslSessionTimeout)
                .trustManager(configuration.sslTrustManagerFactory)
                .build()
    }


    @CompileStatic
    static class HttpConnectionListener implements ChannelFutureListener {

        final URI uri
        final Promise finalPromise
        final Closure customizer
        final HttpMethod httpMethod
        final Configuration configuration

        HttpConnectionListener(URI uri, Promise finalPromise, Closure customizer, Configuration configuration, HttpMethod httpMethod = HttpMethod.GET) {
            this.uri = uri
            this.finalPromise = finalPromise
            this.customizer = customizer
            this.httpMethod = httpMethod
            this.configuration = configuration
        }

        @Override
        void operationComplete(ChannelFuture connectFuture) throws Exception {
            def connectionChannel = connectFuture
                                        .channel()


            if(connectFuture.isSuccess()) {

                def req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, httpMethod, this.uri.rawPath)

                if(customizer != null) {
                    def reqCustomizer = new HttpRequestBuilder(req, Charset.forName(configuration.encoding))
                    customizer.delegate = reqCustomizer
                    customizer.call()
                }

                def headers = req.headers()
                headers.set(HttpHeaders.Names.HOST, this.uri.host)
                headers.set(HttpHeaders.Names.CONNECTION, "close")

                def responseListener = new ChannelFutureListener() {
                    @Override
                    void operationComplete(ChannelFuture reqFuture) throws Exception {
                        def requestChannel = reqFuture
                                                .channel()

                        HttpResponseHandler handler = (HttpResponseHandler)requestChannel.pipeline().get(HttpResponseHandler)
                        handler.promise = finalPromise
                    }
                }

                writeHttpRequest(connectionChannel, req)
                    .addListener(responseListener)
            }
            else {
                // handle connection error
                finalPromise.setFailure(connectFuture.cause())
            }
        }

        protected ChannelFuture writeHttpRequest(Channel connectionChannel, DefaultFullHttpRequest req) {
            connectionChannel
                    .writeAndFlush(req)
        }
    }

    @CompileStatic
    static class HttpResponseHandler extends SimpleChannelInboundHandler<HttpObject> {

        final Configuration configuration

        HttpResponseHandler(Configuration configuration) {
            this.configuration = configuration
        }

        Promise promise

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
            if(msg instanceof FullHttpResponse) {
                if(promise == null) {
                    throw new IllegalStateException("Promise not configured")
                }
                FullHttpResponse resp = (FullHttpResponse)msg
                promise.setSuccess(new HttpClientResponse(resp, configuration.encoding))
            }
        }

        @Override
        void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause)
            if(promise == null) {
                throw new IllegalStateException("Promise not configured")
            }

            promise.setFailure(cause)
            ctx.close()
        }
    }

    /**
     * Initializes the HTTP client channel
     */
    @CompileStatic
    static class AsyncHttpClientInitializer extends ChannelInitializer<Channel> {

        final SslContext sslContext
        final Configuration configuration

        AsyncHttpClientInitializer(SslContext sslContext, Configuration configuration) {
            this.sslContext = sslContext
            this.configuration = configuration
        }

        AsyncHttpClientInitializer(Configuration configuration = new DefaultConfiguration()) {
            this.sslContext = null
            this.configuration = configuration
        }

        protected void initChannel(Channel ch) throws Exception {
            def p = ch.pipeline()
            if(sslContext != null) {
                SSLEngine engine = sslContext.newEngine(ch.alloc())
                p.addFirst("ssl", new SslHandler(engine));
            }

            if(configuration.codec != null) {
                p.addLast("codec", configuration.codec )
            }
            p.addLast("aggregator", new HttpObjectAggregator(configuration.maxContentLength))
            p.addLast("response", new HttpResponseHandler(configuration))
        }

    }
}
