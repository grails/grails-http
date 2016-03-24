package grails.http.client

import groovy.transform.CompileStatic
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslProvider
import io.netty.handler.ssl.util.InsecureTrustManagerFactory

import javax.net.ssl.TrustManagerFactory
import java.util.concurrent.ThreadFactory

/**
 * Configuration for the HTTP client
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
trait Configuration {

    /**
     * The encoding to use
     */
    String encoding = "UTF-8"
    /**
     * The number of threads the client should use for requests
     */
    int numOfThreads = 0

    /**
     * The thread factory to use for creating threads
     */
    ThreadFactory threadFactory
    /**
     * The SSL provider to use
     */
    SslProvider sslProvider = SslContext.defaultClientProvider()

    /**
     * The default session cache size
     */
    long sslSessionCacheSize

    /**
     * The SSL timeout period
     */
    long sslSessionTimeout

    /**
     * The default trust manager factory
     */
    TrustManagerFactory sslTrustManagerFactory = InsecureTrustManagerFactory.INSTANCE

    /**
     * The maximum content length the client can consume
     */
    int maxContentLength = Integer.MAX_VALUE

    /**
     * The codec to use
     */
    HttpClientCodec codec = new HttpClientCodec()

    /**
     * Options for the netty channel
     */
    Map<String, Object> channelOptions = [:]

    /**
     * The proxy to use. For authentication specify http.proxyUser and http.proxyPassword system properties
     *
     * Alternatively configure a java.net.ProxySelector
     */
    Proxy proxy

}