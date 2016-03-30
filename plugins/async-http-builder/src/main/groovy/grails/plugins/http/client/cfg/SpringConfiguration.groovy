package grails.plugins.http.client.cfg

import grails.http.client.cfg.DefaultConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Extended impl that allows the configuration to be modified via properties
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@ConfigurationProperties('grails.http.client')
class SpringConfiguration extends DefaultConfiguration {
}
