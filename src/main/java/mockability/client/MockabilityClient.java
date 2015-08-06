package mockability.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import mockability.client.adapters.LibraryAdapter;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Simplifies interaction with the Mockability server.
 * @param <Q> Class that will represent an HTTP request for this client. Q for "reQuest."
 * @param <S> Class that will represent an HTTP response for this client. S for "reSponse."
 */
public class MockabilityClient<Q, S> {

    private static final Base64 CODEC = new Base64();
    HttpClient client = HttpClientBuilder.create ().build ();
    private LibraryAdapter<Q, S> adapter;
    private HttpHost host;

    /**
     * Create a new MockabilityClient.
     * @param adapter Instance of class that implements the LibraryAdapter interface, adapting MockabilityClient to
     *                some standard representation of HTTP requests and responses.
     * @param baseUrl Base URL of the Mockability server to connect to: for example, "http://localhost:9000".
     */
    public MockabilityClient (LibraryAdapter<Q, S> adapter, String baseUrl) {
        try {
            this.adapter = adapter;
            URI baseUri = new URI(baseUrl);
            host = new HttpHost(baseUri.getHost(), baseUri.getPort());
        }
        catch (Exception e) {
            throw new IllegalStateException (e);
        }
    }

    /**
     * Direct the Mockability server to forget everything it knows about requests and responses from your IP to
     * the supplied URI with the supplied method.
     * @param method HTTP method to clear: for example, "GET" or "POST".
     * @param uri URI to clear: for example, "/library/book/12345?user=sam"
     * @return If unsuccessful, an IllegalStateException with a message that explains the problem;
     *          otherwise, the text/plain body of the 200 response from the Mockability server.
     */
    public String clear (String method, String uri) {
        HttpDelete request = new HttpDelete("/mockability/" + method + ensureInitialSlash (uri));
        return textOrThrow (request);
    }

    /**
     * Direct the Mockability server to forget everything it knows about all requests and responses from your IP.
     * @return If unsuccessful, an IllegalStateException with a message that explains the problem;
     *          otherwise, the text/plain body of the 200 response from the Mockability server.
     */
    public String clear () {
        HttpDelete request = new HttpDelete("/mockability");
        return textOrThrow (request);
    }

    /**
     *  Direct the Mockability server to prepare for a request from your IP address to the supplied URI with the
     *  supplied method, and to respond with the provided response when it arrives. If you call this method more
     *  than once before you start sending the prepared-for requests, the Mockability server will remember your
     *  preparations and send the responses you provide in the order in which you provided them.  These preparations
     *  can be eliminated either by calling the clear() method or by restarting the server.
     * @param method HTTP method to prepare for
     * @param uri URI to prepare for
     * @param response Response to send when the prepared-for request arrives
     * @return If unsuccessful, an IllegalStateException with a message that explains the problem;
     *          otherwise, the text/plain body of the 200 response from the Mockability server.
     */
    public String prepare (String method, String uri, S response) {
        try {
            HttpPost request = new HttpPost("/mockability/" + method + ensureInitialSlash (uri));
            request.addHeader (new BasicHeader ("Content-Type", "application/json"));
            request.setEntity (new StringEntity(responseToJson (response)));
            return textOrThrow (request);
        }
        catch (IllegalStateException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IllegalStateException (e);
        }
    }

    /**
     * Directs the Mockability server to send a list of all the requests it has received from your IP address to
     * the supplied URI with the supplied method.  This list can be cleared either by calling the clear() method or
     * by restarting the server.
     * @param method HTTP method to report
     * @param uri URI to report
     * @return List of HTTP request objects corresponding to the requests seen by the server.
     */
    public List<Q> report (String method, String uri) {
        try {
            HttpGet request = new HttpGet("/mockability/" + method + ensureInitialSlash (uri));
            HttpResponse reportResponse = client.execute (host, request);
            if (reportResponse.getStatusLine ().getStatusCode () != 200) {
                throw new IllegalStateException (new String (extractBody (reportResponse)));
            }
            return inputStreamToRequests(reportResponse.getEntity().getContent());
        }
        catch (IllegalStateException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IllegalStateException (e);
        }
    }

    private String textOrThrow (HttpRequest request) {
        try {
            HttpResponse response = client.execute(host, request);
            if (response.getStatusLine ().getStatusCode() != 200) {
                throw new IllegalStateException (new String (extractBody (response)));
            }
            return new String (extractBody (response));
        }
        catch (IllegalStateException e) {
            throw e;
        }
        catch (Exception e) {
            throw new IllegalStateException (e);
        }
    }

    private List<LibraryAdapter.HeaderPair> extractHeaders (HttpResponse response) {
        return translateHeaders (response.getAllHeaders ());
    }

    private List<LibraryAdapter.HeaderPair> extractHeaders (HttpRequest request) {
        return translateHeaders (request.getAllHeaders ());
    }

    private List<LibraryAdapter.HeaderPair> translateHeaders (Header[] headers) {
        List<LibraryAdapter.HeaderPair> pairs = new ArrayList<LibraryAdapter.HeaderPair>();
        for (Header header : headers) {
            pairs.add (new LibraryAdapter.HeaderPair(header.getName (), header.getValue ()));
        }
        return pairs;
    }

    private byte[] extractBody (HttpResponse response) {
        try {
            InputStream istr = response.getEntity().getContent();
            ByteArrayOutputStream ostr = new ByteArrayOutputStream ();
            byte[] buf = new byte[1024];
            while (true) {
                int len = istr.read (buf);
                if (len < 0) {break;}
                ostr.write (buf, 0, len);
            }
            return ostr.toByteArray ();
        }
        catch (Exception e) {
            throw new IllegalStateException (e);
        }
    }

    private String ensureInitialSlash (String uri) {
        if (uri.startsWith ("/")) {
            return uri;
        }
        else {
            return "/" + uri;
        }
    }

    private String responseToJson (S response) throws Exception {
        int status = adapter.getResponseStatus(response);
        List<LibraryAdapter.HeaderPair> headers = adapter.getResponseHeaders (response);
        String body = CODEC.encodeToString(adapter.getResponseBody(response));

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode root = mapper.createArrayNode ();
        ObjectNode responseNode = mapper.createObjectNode ();
        responseNode.set ("status", new IntNode(status));
        responseNode.set ("headers", makeHeaders (headers, mapper));
        responseNode.set ("body", new TextNode (body));
        root.add (responseNode);
        return root.toString ();
    }

    private ArrayNode makeHeaders (List<LibraryAdapter.HeaderPair> headers, ObjectMapper mapper) {
        ArrayNode jsonHeaders = mapper.createArrayNode();
        for (LibraryAdapter.HeaderPair header : headers) {
            ObjectNode jsonHeader = mapper.createObjectNode ();
            jsonHeader.set ("name", new TextNode (header.name ()));
            jsonHeader.set ("value", new TextNode (header.value ()));
            jsonHeaders.add (jsonHeader);
        }
        return jsonHeaders;
    }

    private List<Q> inputStreamToRequests (InputStream istr) throws Exception {
        ObjectMapper mapper = new ObjectMapper ();
        JsonParser parser = mapper.getFactory ().createParser (istr);
        parser.setCodec (mapper);
        ArrayNode root = parser.readValueAsTree();
        List<Q> requests = new ArrayList<Q> ();
        for (Iterator<JsonNode> iter = root.elements (); iter.hasNext ();) {
            ObjectNode request = (ObjectNode)iter.next ();
            requests.add (objectNodeToRequest (request));
        }
        return requests;
    }

    private Q objectNodeToRequest (ObjectNode root) throws Exception {
        String method = root.get ("method").asText ();
        String uri = root.get ("uri").asText ();
        List<LibraryAdapter.HeaderPair> headers = makeHeaderPairs ((ArrayNode) root.get ("headers"));
        JsonNode bodyNode = root.get ("body");
        byte[] body;
        if (bodyNode != null) {
            body = CODEC.decode (root.get ("body").asText ());
        }
        else {
            body = new byte[] {};
        }
        return adapter.convert (method, uri, headers, body);
    }

    private List<LibraryAdapter.HeaderPair> makeHeaderPairs (ArrayNode root) {
        List<LibraryAdapter.HeaderPair> pairs = new ArrayList<LibraryAdapter.HeaderPair> ();
        for (Iterator<JsonNode> iter = root.elements (); iter.hasNext ();) {
            ObjectNode header = (ObjectNode)iter.next ();
            pairs.add (new LibraryAdapter.HeaderPair(header.get ("name").asText (), header.get ("value").asText ()));
        }
        return pairs;
    }
}
