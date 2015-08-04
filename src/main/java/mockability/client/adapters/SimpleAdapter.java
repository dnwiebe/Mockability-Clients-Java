package mockability.client.adapters;

import com.sun.xml.internal.xsom.impl.parser.DelayedRef;

import java.util.List;

/**
 * Created by ga-mlsdiscovery on 8/4/15.
 */
public class SimpleAdapter implements LibraryAdapter<SimpleAdapter.SimpleRequest, SimpleAdapter.SimpleResponse> {

    public static class SimpleRequest {

        private String method;
        private String uri;
        private List<HeaderPair> headers;
        private byte[] body;

        public SimpleRequest(String method, String uri, List<HeaderPair> headers) {
            this(method, uri, headers, new byte[0]);
        }

        public SimpleRequest(String method, String uri, List<HeaderPair> headers, byte[] body) {
            this.method = method;
            this.uri = uri;
            this.headers = headers;
            this.body = body;
        }

        public String getMethod() {
            return method;
        }

        public String getUri() {
            return uri;
        }

        public List<HeaderPair> getHeaders() {
            return headers;
        }

        public byte[] getBody() {
            return body;
        }

    }

    public static class SimpleResponse {

        private int status;
        private List<HeaderPair> headers;
        private byte[] body;

        public SimpleResponse(int status, List<HeaderPair> headers) {
            this(status, headers, new byte[0]);
        }

        public SimpleResponse(int status, List<HeaderPair> headers, byte[] body) {
            this.status = status;
            this.headers = headers;
            this.body = body;
        }


        public int getStatus() {
            return status;
        }

        public List<HeaderPair> getHeaders() {
            return headers;
        }

        public byte[] getBody() {
            return body;
        }
    }

    @Override
    public SimpleRequest convert(String method, String uri, List<HeaderPair> headers, byte[] body) {
        LibraryAdapter.validateMethod (method);
        return new SimpleRequest(method, uri, headers, body);
    }

    @Override
    public SimpleResponse convert(int status, List<HeaderPair> headers, byte[] body) {
        return new SimpleResponse(status, headers, body);
    }

    @Override
    public String getRequestMethod(SimpleRequest request) {
        return request.getMethod();
    }

    @Override
    public String getRequestUri(SimpleRequest request) {
        return request.getUri();
    }

    @Override
    public List<HeaderPair> getRequestHeaders(SimpleRequest request) {
        return request.getHeaders();
    }

    @Override
    public byte[] getRequestBody(SimpleRequest request) {
        return request.getBody();
    }

    @Override
    public int getResponseStatus(SimpleResponse response) {
        return response.getStatus();
    }

    @Override
    public List<HeaderPair> getResponseHeaders(SimpleResponse response) {
        return response.getHeaders();
    }

    @Override
    public byte[] getResponseBody(SimpleResponse response) {
        return response.getBody();
    }
}
