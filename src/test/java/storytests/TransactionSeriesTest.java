package storytests;

import mockability.client.HttpClientMockabilityClient;
import mockability.client.adapters.HttpClientAdapter;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.Test;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicHttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by dnwiebe on 7/19/15.
 */
public class TransactionSeriesTest {

    private HttpClientMockabilityClient subject;

    @Before
    public void setup () {
        subject = new HttpClientMockabilityClient ("http://localhost:9000");
    }

    @Test
    public void setUpThree_triggerTwo_clearOne () throws Exception {
        BasicHttpResponse firstResponse = new BasicHttpResponse (new ProtocolVersion("HTTP", 1, 1), 201, "");
        firstResponse.addHeader("Content-Length", "37");
        firstResponse.addHeader ("Content-Type", "text/html");
        firstResponse.setEntity (new StringEntity ("<html><body>Response #1</body></html>"));

        BasicHttpResponse secondResponse = new BasicHttpResponse (new ProtocolVersion("HTTP", 1, 1), 405, "");
        secondResponse.addHeader("Content-Length", "55");
        secondResponse.addHeader ("Content-Type", "text/html");
        secondResponse.setEntity (new StringEntity ("Last programmed response; you won't be getting any more"));

        BasicHttpResponse thirdResponse = new BasicHttpResponse (new ProtocolVersion("HTTP", 1, 1), 503, "");
        thirdResponse.addHeader("Content-Length", "25");
        thirdResponse.addHeader("Content-Type", "text/html");
        thirdResponse.setEntity(new StringEntity("No one will ever see this"));

        URI uri = new URI ("/blibbety?type=silly&definition=mouth+noise");

        HttpGet firstRequest = new HttpGet ();
        firstRequest.setURI (uri);
        firstRequest.addHeader("X-Record", "First request");

        HttpGet secondRequest = new HttpGet ();
        secondRequest.setURI (uri);
        secondRequest.addHeader("X-Record", "Second request");

        HttpGet thirdRequest = new HttpGet ();
        thirdRequest.setURI(uri);
        thirdRequest.addHeader("X-Record", "Rejected");

        subject.clear("GET", uri.toString());
        subject.prepare("GET", uri.toString(), firstResponse);
        subject.prepare("GET", uri.toString (), secondResponse);
        subject.prepare ("GET", uri.toString (), thirdResponse);

        HttpClient client = HttpClientBuilder.create ()
            .build ();
        HttpHost host = new HttpHost("localhost", 9000);
        HttpResponse firstResponseActual = client.execute (host, firstRequest);
        compare (firstResponse, firstResponseActual);
        HttpResponse secondResponseActual = client.execute(host, secondRequest);
        compare (secondResponse, secondResponseActual);

        List<HttpRequestBase> requests = subject.report("GET", uri.toString());
        compare (firstRequest, requests.get (0));
        compare (secondRequest, requests.get (1));
        assertEquals (2, requests.size ());

        subject.clear("GET", uri.toString());

        HttpResponse thirdResponseActual = client.execute (host, thirdRequest);
        assertEquals (499, thirdResponseActual.getStatusLine ().getStatusCode ());

        try {
            subject.report ("GET", uri.toString ());
            fail ();
        }
        catch (IllegalStateException e) {
            assertEquals (
                "\nReport was demanded for:\n" +
                "127.0.0.1: GET '/blibbety?type=silly&definition=mouth+noise'\n\n" +
                "Reports are prepared only for:\n" +
                "No reports were prepared.\n",
                e.getMessage ()
            );
        }
    }

    private void compare (HttpRequestBase expected, HttpRequestBase actual) {
        assertEquals (expected.getMethod (), actual.getMethod ());
        assertEquals (expected.getURI ().toString (), actual.getURI ().toString ());
        compareHeaders (expected, actual);
        if (HttpEntityEnclosingRequestBase.class.isAssignableFrom (expected.getClass ())) {
            compareEntities (((HttpEntityEnclosingRequest)expected).getEntity (),
                    ((HttpEntityEnclosingRequest)actual).getEntity ());
        }
    }

    private void compare (HttpResponse expected, HttpResponse actual) {
        compareEntities (expected.getEntity (), actual.getEntity ());
        assertEquals (expected.getStatusLine ().getStatusCode (), actual.getStatusLine ().getStatusCode ());
        compareHeaders (expected, actual);
    }

    private void compareHeaders (HttpMessage expected, HttpMessage actual) {
        Map<String, List<String>> expectedHeaders = headersToMap (expected);
        Map<String, List<String>> actualHeaders = headersToMap (expected);
        for (Map.Entry<String, List<String>> entry : expectedHeaders.entrySet ()) {
            for (String value : entry.getValue ()) {
                assertTrue ("Actual header set " + actualHeaders + " didn't contain expected header " + entry,
                        (actualHeaders.containsKey (entry.getKey ()) && actualHeaders.get (entry.getKey ()).contains (value)));
            }
        }
    }

    private Map<String, List<String>> headersToMap (HttpMessage msg) {
        Map<String, List<String>> map = new HashMap<> ();
        for (Header header : msg.getAllHeaders ()) {
            List<String> values = map.get (header.getName ());
            if (values == null) {
                values = new ArrayList<> ();
                map.put (header.getName (), values);
            }
            values.add (header.getValue ());
        }
        return map;
    }

    private void compareEntities (HttpEntity expected, HttpEntity actual) {
        String expectedString = entityToString (expected);
        String actualString = entityToString (actual);
        assertEquals (expectedString, actualString);
    }

    private String entityToString (HttpEntity entity) {
        try {
            InputStream istr = entity.getContent ();
            ByteArrayOutputStream baos = new ByteArrayOutputStream ();
            byte[] buf = new byte[1024];
            while (true) {
                int len = istr.read (buf);
                if (len < 0) {break;}
                baos.write (buf, 0, len);
            }
            return new String (baos.toByteArray ());
        }
        catch (Exception e) {
            throw new IllegalStateException (e);
        }
    }
}
