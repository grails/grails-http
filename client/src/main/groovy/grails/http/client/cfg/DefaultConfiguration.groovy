package grails.http.client.cfg

import grails.http.client.Configuration
import groovy.transform.CompileStatic

/**
 * The default configuration
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
class DefaultConfiguration implements Configuration {

    DefaultConfiguration() {
        def httpProxyHost = System.getProperty("http.proxyHost")
        def httpProxyPort = System.getProperty("http.proxyPort")

        if(httpProxyHost && httpProxyPort) {
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpProxyHost, httpProxyPort.toInteger()))
        }
    }
}
