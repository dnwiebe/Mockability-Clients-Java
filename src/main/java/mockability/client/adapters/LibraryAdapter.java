package mockability.client.adapters;

import java.util.List;

/**
 * Created by dnwiebe on 7/19/15.
 */
public interface LibraryAdapter<Q, S> {
    class HeaderPair {
        private String _name;
        private String _value;
        public HeaderPair (String name, String value) {
            _name = name;
            _value = value;
        }
        public String name () {return _name;}
        public String value () {return _value;}
    }

    Q convert (String method, String uri, List<HeaderPair> headers, byte[] body) throws Exception;
    S convert (int status, List<HeaderPair> headers, byte[] body) throws Exception;

    String getRequestMethod (Q request);
    String getRequestUri (Q request);
    List<HeaderPair> getRequestHeaders (Q request);
    byte[] getRequestBody (Q request) throws Exception;

    int getResponseStatus (S response);
    List<HeaderPair> getResponseHeaders (S response);
    byte[] getResponseBody (S response) throws Exception;

    static void validateMethod (String method) {
        if (!"|HEAD|GET|POST|PUT|DELETE|".contains ("|" + method + "|")) {
            throw new IllegalArgumentException ("Unexpected request method " + method);
        }
    }
}
