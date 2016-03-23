package grails.http.client

import grails.async.Promise
import grails.http.HttpStatus
import grails.http.client.test.TestAsyncHttpBuilder
import io.netty.handler.codec.http.HttpResponseStatus
import spock.lang.Specification
/**
 * Created by graemerocher on 18/03/16.
 */
class AsyncRestBuilderSpec extends Specification {


    void 'Test simple GET request'() {
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

    void 'Test simple POST request'() {
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
}
