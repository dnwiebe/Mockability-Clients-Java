package mockability.client.adapters;

import java.util.List;

/**
 * Created by dnwiebe on 7/19/15.
 */

/**
 * A class that implements this interface serves to adapt MockabilityClient to accept and return objects of
 * specific classes that represent HTTP requests and responses.
 * @param <Q> Class that will represent an HTTP request. Q for "reQuest."
 * @param <S> Class that will represent an HTTP response. S for "reSponse."
 */
public interface LibraryAdapter<Q, S> {

    /**
     * Class of immutable objects that each hold the name and value of a single HTTP header.
     */
    class HeaderPair {
        private String _name;
        private String _value;

        /**
         * Create a new HeaderPair with name and value.
         * @param name Name of the new header
         * @param value Value of the new header
         */
        public HeaderPair (String name, String value) {
            _name = name;
            _value = value;
        }

        /**
         * @return Name of the header
         */
        public String name () {return _name;}

        /**
         * @return Value of the header
         */
        public String value () {return _value;}
    }

    /**
     * Combine a collection of information about an HTTP request into an object representing that request.
     * @param method HTTP method, such as "GET" or "PUT".
     * @param uri HTTP URI, usually beginning with a slash.  Do not include scheme, hostname, or port.
     * @param headers List of headers for the request.
     * @param body Body of the request, not Base64-encoded. Chunking and streaming are not supported.
     * @return An object of the specified HTTP Request type, populated with the supplied information.
     * @throws Exception The conversion method may throw whatever exception it needs to throw.
     */
    Q convert (String method, String uri, List<HeaderPair> headers, byte[] body) throws Exception;

    /**
     * Combine a collection of information about an HTTP response into an object representing that response.
     * @param status Status code, such as 200 or 404.
     * @param headers List of headers for the response.
     * @param body Body of the response, not Base64-encoded. Chunking and streaming are not supported.
     * @return An object of the specified HTTP Response type, populated with the supplied information.
     * @throws Exception The conversion method may throw whatever exception it needs to throw.
     */
    S convert (int status, List<HeaderPair> headers, byte[] body) throws Exception;

    /**
     * Returns just the method from the supplied request.
     * @param request
     * @return
     */
    String getRequestMethod (Q request);

    /**
     * Returns just the URI (beginning with the slash after the hostname/port) from the supplied request.
     * @param request
     * @return
     */
    String getRequestUri (Q request);

    /**
     * Returns just the headers from the supplied request.
     * @param request
     * @return
     */
    List<HeaderPair> getRequestHeaders (Q request);

    /**
     * Returns just the body from the supplied request.
     * @param request
     * @return
     * @throws Exception Sometimes getting the body into a byte array can throw an exception.
     */
    byte[] getRequestBody (Q request) throws Exception;


    /**
     * Returns just the status code from the supplied response.
     * @param response
     * @return
     */
    int getResponseStatus (S response);

    /**
     * Returns just the headers from the supplied response.
     * @param response
     * @return
     */
    List<HeaderPair> getResponseHeaders (S response);

    /**
     * Returns just the body from the supplied response.
     * @param response
     * @return
     * @throws Exception Sometimes getting the body into a byte array can throw an exception.
     */
    byte[] getResponseBody (S response) throws Exception;

    /**
     * Used mostly internally, this static method throws an exception if the HTTP method supplied is not one of the
     * recognized ones.
     * @param method
     */
    static void validateMethod (String method) {
        if (!"|HEAD|GET|POST|PUT|DELETE|".contains ("|" + method + "|")) {
            throw new IllegalArgumentException ("Unexpected request method " + method);
        }
    }
}
