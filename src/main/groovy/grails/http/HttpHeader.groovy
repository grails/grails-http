package grails.http

import groovy.transform.CompileStatic


/**
 * Enum for common HTTP headers
 *
 * @author Graeme Rocher
 * @since 1.0
 */
@CompileStatic
enum HttpHeader implements CharSequence{
    ACCEPT("Accept"),
    ACCEPT_CHARSET("Accept-Charset"),
    ACCEPT_ENCODING("Accept-Encoding"),
    ACCEPT_LANGUAGE("Accept-Language"),
    ACCEPT_RANGES("Accept-Ranges"),
    ACCEPT_PATCH("Accept-Patch"),
    ACCEPT_VERSION("Accept-Version"),
    ACCESS_CONTROL_ALLOW_CREDENTIALS("Access-Control-Allow-Credentials"),
    ACCESS_CONTROL_ALLOW_HEADERS("Access-Control-Allow-Headers"),
    ACCESS_CONTROL_ALLOW_METHODS("Access-Control-Allow-Methods"),
    ACCESS_CONTROL_ALLOW_ORIGIN("Access-Control-Allow-Origin"),
    ACCESS_CONTROL_EXPOSE_HEADERS("Access-Control-Expose-Headers"),
    ACCESS_CONTROL_MAX_AGE("Access-Control-Max-Age"),
    ACCESS_CONTROL_REQUEST_HEADERS( "Access-Control-Request-Headers"),
    ACCESS_CONTROL_REQUEST_METHOD("Access-Control-Request-Method"),
    AGE("Age"),
    ALLOW("Allow"),
    AUTHORIZATION("Authorization"),
    CACHE_CONTROL("Cache-Control"),
    CONNECTION("Connection"),
    CONTENT_BASE("Content-Base"),
    CONTENT_ENCODING("Content-Encoding"),
    CONTENT_LANGUAGE("Content-Language"),
    CONTENT_LENGTH("Content-Length"),
    CONTENT_LOCATION("Content-Location"),
    CONTENT_TRANSFER_ENCODING("Content-Transfer-Encoding"),
    CONTENT_MD5("Content-MD5"),
    CONTENT_RANGE("Content-Range"),
    CONTENT_TYPE("Content-Type"),
    COOKIE("Cookie"),
    DATE("Date"),
    ETAG("ETag"),
    EXPECT("Expect"),
    EXPIRES("Expires"),
    FROM("From"),
    HOST("Host"),
    IF_MATCH("If-Match"),
    IF_MODIFIED_SINCE("If-Modified-Since"),
    IF_NONE_MATCH("If-None-Match"),
    IF_RANGE("If-Range"),
    IF_UNMODIFIED_SINCE("If-Unmodified-Since"),
    LAST_MODIFIED("Last-Modified"),
    LOCATION("Location"),
    MAX_FORWARDS("Max-Forwards"),
    ORIGIN("Origin"),
    PRAGMA("Pragma"),
    PROXY_AUTHENTICATE("Proxy-Authenticate"),
    PROXY_AUTHORIZATION("Proxy-Authorization"),
    RANGE("Range"),
    REFERER("Referer"),
    RETRY_AFTER("Retry-After"),
    SEC_WEBSOCKET_KEY1("Sec-WebSocket-Key1"),
    SEC_WEBSOCKET_KEY2("Sec-WebSocket-Key2"),
    SEC_WEBSOCKET_LOCATION("Sec-WebSocket-Location"),
    SEC_WEBSOCKET_ORIGIN("Sec-WebSocket-Origin"),
    SEC_WEBSOCKET_PROTOCOL("Sec-WebSocket-Protocol"),
    SEC_WEBSOCKET_VERSION("Sec-WebSocket-Version"),
    SEC_WEBSOCKET_KEY("Sec-WebSocket-Key"),
    SEC_WEBSOCKET_ACCEPT("Sec-WebSocket-Accept"),
    SERVER("Server"),
    SET_COOKIE("Set-Cookie"),
    SET_COOKIE2("Set-Cookie2"),
    TRAILER ("Trailer"),
    TRANSFER_ENCODING ("Transfer-Encoding"),
    UPGRADE ("Upgrade"),
    USER_AGENT("User-Agent"),
    VARY("Vary"),
    VIA("Via"),
    WARNING("Warning"),
    WEBSOCKET_LOCATION ("WebSocket-Location"),
    WEBSOCKET_ORIGIN ("WebSocket-Origin"),
    WEBSOCKET_PROTOCOL("WebSocket-Protocol"),
    WWW_AUTHENTICATE("WWW-Authenticate")

    private static final Map<String, HttpHeader> BY_NAME

    static {

        def headers = values()
        Map<String, HttpHeader> byCode= [:]
        for(header in headers) {
            byCode.put(header.text, header)
        }

        BY_NAME = Collections.unmodifiableMap(byCode)
    }


    final String text

    HttpHeader(String text) {
        this.text = text
    }

    @Override
    int length() {
        text.length()
    }

    @Override
    char charAt(int index) {
        return text.charAt(index)
    }

    @Override
    CharSequence subSequence(int start, int end) {
        return text.subSequence(start, end)
    }


    @Override
    String toString() {
        text
    }

    /**
     * Obtain a header instance for the given name
     *
     * @param name The name
     * @return The HttpHeader instance
     */
    static HttpHeader forName(CharSequence name) {
        def nameStr = name.toString()
        def header = BY_NAME.get(nameStr)
        if(header == null) {
            return valueOf(nameStr)
        }
        return header
    }
}
