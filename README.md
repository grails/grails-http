# Grails HTTP Utilities

This project contains HTTP utility classes generally useful across both client and server projects.
 
Initially there is an Asynchronous HTTP client available that is built on Netty and designed as replacement for the synchronous RestBuilder project for Grails.
 
The client is however usable outside of Grails and for any general purpose.

* [![Build Status](https://travis-ci.org/grails/grails-http.svg?branch=master)](https://travis-ci.org/grails/grails-http)

## Example

    import grails.http.client.*
    import grails.async.*
    
    AsyncHttpBuilder client = new AsyncHttpBuilder()
    Promise<HttpClientResponse> p = client.post("https://localhost:8080/foo/bar") {
        contentType 'application/json'
        json {
            title "Ping"
        }
    }
    p.onComplete { HttpClientResponse resp ->
        assert resp.json.title == 'Pong'
    }
        

## Installation

To use the `AsyncHttpBuilder` class outside of Grails use the dependency directly:

    compile "org.grails:http-client:VERSION"
    
Where `VERSION` is the version you wish to use. For usage within Grails there is a plugin:
    
    compile "org.grails.plugins:async-http-builder:VERSION"

For more information see the documentation:

* [Snapshot Documentation](http://grails.github.io/grails-http/snapshot/)