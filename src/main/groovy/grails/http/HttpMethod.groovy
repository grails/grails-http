package grails.http

import groovy.transform.CompileStatic

/**
 * An enum containing the valid HTTP methods
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
enum HttpMethod {
    OPTIONS,
    GET,
    POST,
    PUT,
    PATCH,
    DELETE,
    HEAD,
}