package mockability.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import mockability.client.adapters.LibraryAdapter;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by dnwiebe on 7/19/15.
 */
public class MockabilityClientTest {

    private MockabilityClient<String, String> subject;
    private LibraryAdapter<String, String> adapter;
    private HttpClient client;

    private static final Base64 CODEC = new Base64 ();

    private static class TestAdapter implements LibraryAdapter<String, String> {

        @Override
        public String convert(String method, String uri, List<HeaderPair> headers, byte[] body) {
            StringBuilder buf = new StringBuilder ()
                .append(method).append ("|").append (uri).append ("|")
                .append (translateHeaderPairs(headers));
            addBody (buf, body);
            return buf.toString ();
        }

        @Override
        public String convert(int status, List<HeaderPair> headers, byte[] body) {
            StringBuilder buf = new StringBuilder ()
                    .append (status).append ("|")
                    .append (translateHeaderPairs (headers));
            addBody (buf, body);
            return buf.toString ();
        }

        private void addBody (StringBuilder buf, byte[] body) {
            if (body.length > 0) {
                buf
                    .append ("|")
                    .append (new String (body));
            }
        }

        @Override
        public String getRequestMethod(String request) {
            return request.split ("\\|")[0];
        }

        @Override
        public String getRequestUri(String request) {
            return request.split ("\\|")[1];
        }

        @Override
        public List<HeaderPair> getRequestHeaders(String request) {
            String[] components = request.split ("\\|")[2].split ("=");
            return Collections.singletonList (new HeaderPair (components[0], components[1]));
        }

        @Override
        public byte[] getRequestBody(String request) {
            String[] split = request.split ("\\|");
            if (split.length > 3) {
                return split[3].getBytes ();
            }
            else {
                return new byte[]{};
            }
        }

        @Override
        public int getResponseStatus(String response) {
            return Integer.parseInt(response.split("\\|")[0]);
        }

        @Override
        public List<HeaderPair> getResponseHeaders(String response) {
            String[] components = response.split ("\\|")[1].split ("=");
            return Arrays.asList (new HeaderPair (components[0], components[1]));
        }

        @Override
        public byte[] getResponseBody(String response) {
            String[] split = response.split ("\\|");
            if (split.length > 2) {
                return split[2].getBytes ();
            }
            else {
                return new byte[]{};
            }
        }

        private String translateHeaderPairs(List<HeaderPair> pairs) {
            StringBuilder buf = new StringBuilder ();
            for (HeaderPair pair : pairs) {
                if (buf.length () > 0) {buf.append (";");}
                buf.append (pair.name ()).append ("=").append (pair.value ());
            }
            return buf.toString ();
        }
    }

    @Before
    public void setup () {
        adapter = new TestAdapter ();
        client = mock (HttpClient.class);
        subject = new MockabilityClient (adapter, "http://baseUrl:1234");
        subject.client = client;
    }

    @Test
    public void shouldSendSpecificDeleteRequestOnSpecificClear () throws Exception {
        HttpResponse clearResponse = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "");
        clearResponse.addHeader(new BasicHeader("millie", "whump"));
        clearResponse.setEntity(new StringEntity("cleared"));
        when(client.execute(any (HttpHost.class), any (HttpDelete.class))).thenReturn (clearResponse);

        String resultText = subject.clear("GLOMPETY", "/wiggle");

        ArgumentCaptor<HttpHost> hostCaptor = ArgumentCaptor.forClass(HttpHost.class);
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify (client).execute (hostCaptor.capture (), requestCaptor.capture ());

        HttpHost host = hostCaptor.getValue ();
        assertEquals ("baseUrl", host.getHostName ());
        assertEquals (1234, host.getPort ());

        HttpDelete request = (HttpDelete)requestCaptor.getValue ();
        assertEquals ("/mockability/GLOMPETY/wiggle", request.getRequestLine ().getUri ());

        assertEquals ("cleared", resultText);
    }

    @Test
    public void shouldSendNonspecificDeleteRequestOnNonspecificClear () throws Exception {
        HttpResponse clearResponse = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "");
        clearResponse.addHeader(new BasicHeader("millie", "whump"));
        clearResponse.setEntity(new StringEntity("cleared"));
        when(client.execute(any (HttpHost.class), any (HttpDelete.class))).thenReturn (clearResponse);

        String resultText = subject.clear();

        ArgumentCaptor<HttpHost> hostCaptor = ArgumentCaptor.forClass(HttpHost.class);
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify (client).execute (hostCaptor.capture (), requestCaptor.capture ());

        HttpHost host = hostCaptor.getValue ();
        assertEquals ("baseUrl", host.getHostName ());
        assertEquals (1234, host.getPort ());

        HttpDelete request = (HttpDelete)requestCaptor.getValue ();
        assertEquals ("/mockability", request.getRequestLine ().getUri ());

        assertEquals ("cleared", resultText);
    }

    @Test
    public void shouldThrowExceptionIfClearFails () throws Exception {
        HttpResponse clearResponse = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 400, "");
        clearResponse.setEntity(new StringEntity("Your mother wears army boots"));
        when(client.execute(any (HttpHost.class), any (HttpDelete.class))).thenReturn (clearResponse);

        try {
            subject.clear("GLOMPETY", "/wiggle");
            fail ();
        }
        catch (IllegalStateException e) {
            assertEquals ("Your mother wears army boots", e.getMessage ());
        }
    }

    @Test
    public void shouldSendPostRequestOnPrepare () throws Exception {
        HttpResponse postResponse = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "");
        postResponse.addHeader(new BasicHeader("millie", "whump"));
        postResponse.setEntity(new StringEntity("prepared"));
        when(client.execute(any (HttpHost.class), any (HttpPost.class))).thenReturn (postResponse);

        String resultText = subject.prepare("GLOMPETY", "/wiggle", "503|gurble=flop|biggety-boo");

        ArgumentCaptor<HttpHost> hostCaptor = ArgumentCaptor.forClass(HttpHost.class);
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify (client).execute (hostCaptor.capture (), requestCaptor.capture ());

        HttpHost host = hostCaptor.getValue ();
        assertEquals ("baseUrl", host.getHostName ());
        assertEquals (1234, host.getPort ());

        HttpPost request = (HttpPost)requestCaptor.getValue();
        assertEquals ("/mockability/GLOMPETY/wiggle", request.getRequestLine().getUri());
        assertEquals ("application/json", request.getHeaders ("Content-Type")[0].getValue ());
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode list = (ArrayNode)mapper.readTree (request.getEntity ().getContent ());
        ObjectNode root = (ObjectNode)list.get (0);
        assertEquals (503, root.get ("status").asInt ());
        ArrayNode headers = (ArrayNode)root.get("headers");
        assertEquals ("gurble", headers.get (0).get ("name").asText());
        assertEquals ("flop", headers.get (0).get ("value").asText ());
        assertEquals (1, headers.size ());
        assertEquals (CODEC.encodeAsString ("biggety-boo".getBytes ()), root.get ("body").asText ());

        assertEquals ("prepared", resultText);
    }

    @Test
    public void shouldThrowExceptionIfPrepareRequestReturnsOtherThan200 () throws Exception {
        HttpResponse postResponse = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 400, "");
        postResponse.addHeader (new BasicHeader ("millie", "whump"));
        postResponse.setEntity (new StringEntity ("I don't like you.  You smell funny."));
        when(client.execute(any (HttpHost.class), any (HttpPost.class))).thenReturn (postResponse);

        try {
            subject.prepare("GLOMPETY", "/wiggle", "503|gurble=flop|biggety-boo");
            fail ();
        }
        catch (IllegalStateException e) {
            assertEquals ("I don't like you.  You smell funny.", e.getMessage ());
        }
    }

    @Test
    public void shouldSendGetRequestAndParseResponseOnReport () throws Exception {
        HttpResponse postResponse = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "");
        postResponse.addHeader(new BasicHeader("millie", "whump"));
        String json = requestsToJson (
                "GLOMPETY|/wiggle|molly=woo|booga-booga, flarpjack",
                "PETYGLOM|/wobble|woo=molly"
        );
        postResponse.setEntity (new StringEntity(json));
        when(client.execute(any (HttpHost.class), any (HttpPost.class))).thenReturn(postResponse);

        List<String> requests = subject.report("GLOMPETY", "/wiggle");

        ArgumentCaptor<HttpHost> hostCaptor = ArgumentCaptor.forClass(HttpHost.class);
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify (client).execute (hostCaptor.capture (), requestCaptor.capture ());

        HttpHost host = hostCaptor.getValue();
        assertEquals ("baseUrl", host.getHostName ());
        assertEquals(1234, host.getPort());

        HttpGet request = (HttpGet)requestCaptor.getValue();
        assertEquals ("/mockability/GLOMPETY/wiggle", request.getRequestLine().getUri());

        assertEquals ("GLOMPETY|/wiggle|molly=woo|booga-booga, flarpjack", requests.get(0));
        assertEquals ("PETYGLOM|/wobble|woo=molly", requests.get(1));
        assertEquals (2, requests.size ());
    }

    private String requestsToJson (String... requests) {
        TestAdapter adapter = new TestAdapter();
        ObjectMapper mapper = new ObjectMapper ();
        ArrayNode root = mapper.createArrayNode ();
        for (String request : requests) {
            root.add (requestToObjectNode (adapter, mapper, request));
        }
        return root.toString ();
    }

    private ObjectNode requestToObjectNode (TestAdapter adapter, ObjectMapper mapper, String request) {
        ObjectNode root = mapper.createObjectNode ();
        root.set ("method", new TextNode(adapter.getRequestMethod (request)));
        root.set ("uri", new TextNode (adapter.getRequestUri(request)));
        root.set ("headers", translateHeaders(mapper, adapter.getRequestHeaders(request)));
        byte[] body = adapter.getRequestBody (request);
        if (body.length > 0) {
            root.set ("body", new TextNode (CODEC.encodeAsString (body)));
        }
        return root;
    }

    private ArrayNode translateHeaders(ObjectMapper mapper, List<LibraryAdapter.HeaderPair> headers) {
        ArrayNode root = mapper.createArrayNode();
        for (LibraryAdapter.HeaderPair pair : headers) {
            ObjectNode header = mapper.createObjectNode();
            header.set ("name", new TextNode (pair.name ()));
            header.set ("value", new TextNode (pair.value ()));
            root.add (header);
        }
        return root;
    }
}
