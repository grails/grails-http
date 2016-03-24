package grails.http.client

import grails.async.Promise
import grails.http.HttpMethod
import grails.http.HttpStatus
import grails.http.client.cfg.DefaultConfiguration
import grails.http.client.test.TestAsyncHttpBuilder
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpResponseStatus
import spock.lang.Specification
/**
 * Created by graemerocher on 18/03/16.
 */
class AsyncRestBuilderSpec extends Specification {

    void "Test multipart form submission"() {
        given:"A client"
        AsyncHttpBuilder client = new TestAsyncHttpBuilder()

        client.expect {
            uri('/foo/bar')
            method(HttpMethod.POST)
            multipart {
                foo = 'bar'
                file('myFile', 'test.txt', 'Hello world!')
            }
        }.respond {
            ok()
        }

        when:"A form is submitted"
        Promise<HttpClientResponse> p = client.post("http://localhost:8080/foo/bar") {
            multipart {
                foo = "bar"
            }
        }

        def response = p.get()

        then:"The form was submitted correct"
        client.verify()
        response.status == HttpStatus.OK
    }


    void "Test form submission"() {
        given:"A client"
        AsyncHttpBuilder client = new TestAsyncHttpBuilder()

        client.expect {
            uri('/foo/bar')
            method(HttpMethod.POST)
            contentType('application/x-www-form-urlencoded')
            form {
                foo = 'bar'
            }
        }.respond {
            ok()
        }

        when:"A form is submitted"
        Promise<HttpClientResponse> p = client.post("http://localhost:8080/foo/bar") {
            form {
                foo = "bar"
            }
        }

        def response = p.get()

        then:"The form was submitted correct"
        client.verify()
        response.status == HttpStatus.OK
    }

    void 'Test simple GET request with JSON response'() {
        given:"an http client instance"
        AsyncHttpBuilder client = new TestAsyncHttpBuilder()
        client.expect {
            uri '/foo/bar'
            method "GET"
            accept 'application/json'
        }.respond {
            ok()
            json {
                title "Hello"
            }
        }

        when:
        Promise<HttpClientResponse> p = client.get("https://localhost:8080/foo/bar") {
            accept 'application/json'
        }

        then:
        client.verify()


        when:
        final def response = p.get()

        then:
        response.status == HttpStatus.OK
        response.header("Content-Type") == 'application/json'
        response.text != ''
        response.json.title == "Hello"
    }

    void 'Test simple POST request with JSON body and JSON response'() {
        given:"an http client instance"
        TestAsyncHttpBuilder client = new TestAsyncHttpBuilder()
        client.expect {
            uri '/foo/bar'
            method "POST"
            contentType 'application/json'
            json {
                title "Ping"
            }
        }.respond {
            created()
            json {
                title "Pong"
            }
        }

        when:
        Promise<HttpClientResponse> p = client.post("https://localhost:8080/foo/bar") {
            contentType 'application/json'
            json {
                title "Ping"
            }
        }

        then:
        client.verify()


        when:
        final def response = p.get()

        then:
        response.status == HttpStatus.CREATED
        response.header("Content-Type") == 'application/json'
        response.text != ''
        response.json.title == "Pong"
    }

    void 'Test simple POST request with XML body and XMLresponse'() {
        given:"an http client instance"
        TestAsyncHttpBuilder client = new TestAsyncHttpBuilder()
        client.expect {
            uri '/foo/bar'
            method HttpMethod.POST
            contentType 'application/xml'
            xml {
                title "Ping"
            }
        }.respond {
            created()
            xml {
                title "Pong"
            }
        }

        when:
        Promise<HttpClientResponse> p = client.post("https://localhost:8080/foo/bar") {
            xml {
                title "Ping"
            }
        }

        then:
        client.verify()


        when:
        final def response = p.get()

        then:
        response.status == HttpStatus.CREATED
        response.header("Content-Type") == 'application/xml'
        response.text != ''
        response.xml.text() == "Pong"
    }
}
