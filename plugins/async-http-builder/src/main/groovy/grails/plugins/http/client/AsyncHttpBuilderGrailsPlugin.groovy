package grails.plugins.http.client

import grails.http.client.AsyncHttpBuilder
import grails.plugins.*
import grails.plugins.http.client.cfg.SpringHttpClientConfiguration

class AsyncHttpBuilderGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.0.0 > *"
    def title = "Grails Async HTTP Client" // Headline display name of the plugin
    def author = "Graeme Rocher"
    def authorEmail = "graeme@grails.org"
    def description = '''\
A non-blocking HTTP client for Grails based on Netty
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.github.io/grails-http/latest"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
    def organization = [ name: "Grails", url: "http://www.grails.org/" ]

    // Any additional developers beyond the author specified above.
    def developers = [ [ name: "Graeme Rocher", email: "graeme@grails.org" ]]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "Github", url: "https://github.com/grails/grails-http/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/grails/grails-http" ]

    Closure doWithSpring() { {->
        asyncHttpBuilderConfiguration(SpringHttpClientConfiguration)
        asyncHttpBuilder(AsyncHttpBuilder, ref('asyncHttpBuilderConfiguration'))
    } }
}
