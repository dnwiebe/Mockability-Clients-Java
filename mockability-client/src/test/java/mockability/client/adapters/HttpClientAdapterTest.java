package mockability.client.adapters;

import com.sun.javaws.exceptions.InvalidArgumentException;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by dnwiebe on 7/20/15.
 */
public class HttpClientAdapterTest {

    private HttpClientAdapter subject;
    private HttpGet requestWithoutHeadersOrBody;
    private HttpPost requestWithHeadersAndBody;
    private HttpResponse responseWithoutHeadersOrBody;
    private HttpResponse responseWithHeadersAndBody;

    @Before
    public void setup () throws Exception {
        subject = new HttpClientAdapter ();

        requestWithoutHeadersOrBody = new HttpGet (new URI("http://a.b.c:4700/bliggety"));
        requestWithHeadersAndBody = new HttpPost (new URI("http://a.b.c:4700/bloggety"));
        requestWithHeadersAndBody.addHeader (new BasicHeader("bloopety", "bloppety"));
        requestWithHeadersAndBody.addHeader (new BasicHeader("flippety", "floppety"));
        requestWithHeadersAndBody.setEntity (new StringEntity ("wibbledy wobbledy woo"));

        responseWithoutHeadersOrBody = new BasicHttpResponse (new ProtocolVersion ("HTTP", 1, 1), 201, "");
        responseWithHeadersAndBody = new BasicHttpResponse (new ProtocolVersion ("HTTP", 1, 1), 503, "");
        responseWithHeadersAndBody.addHeader (new BasicHeader("bloopety", "bloppety"));
        responseWithHeadersAndBody.addHeader (new BasicHeader("flippety", "floppety"));
        responseWithHeadersAndBody.setEntity(new StringEntity("wibbledy wobbledy woo"));
    }

    @Test
    public void convertsToGetRequestWithoutHeadersOrBody () throws Exception {
        HttpRequestBase result = subject.convert ("GET", "http://foppy/clang?glooby=yes&gloppy=no",
                new ArrayList<> (), new byte[] {});

        assertEquals (HttpGet.class, result.getClass ());
        assertEquals ("GET", result.getMethod ());
        assertEquals ("http://foppy/clang?glooby=yes&gloppy=no", result.getURI ().toString ());
        assertEquals (0, result.getAllHeaders ().length);
        // dunno how to assert on body of non-entity-containing request
    }

    @Test
    public void convertsToPostRequestWithHeadersAndBody () throws Exception {
        HttpPost result = (HttpPost)subject.convert ("POST", "http://foppy/clang?glooby=yes&gloppy=no",
                Arrays.asList (
                        new LibraryAdapter.HeaderPair ("bloopety", "bloppety"),
                        new LibraryAdapter.HeaderPair ("flippety", "floppety")
                ), "Zibble is a whimpfer woddle".getBytes ());

        assertEquals ("http://foppy/clang?glooby=yes&gloppy=no", result.getURI ().toString ());
        assertEquals ("bloppety", result.getHeaders ("bloopety")[0].getValue ());
        assertEquals (1, result.getHeaders ("bloopety").length);
        assertEquals ("floppety", result.getHeaders ("flippety")[0].getValue ());
        assertEquals (1, result.getHeaders ("flippety").length);
        assertEquals (2, result.getAllHeaders ().length);
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        result.getEntity ().writeTo (baos);
        assertEquals ("Zibble is a whimpfer woddle", new String (baos.toByteArray ()));
    }

    @Test
    public void convertsToPutRequest () throws Exception {
        HttpPut result = (HttpPut)subject.convert ("PUT", "http://x.com", new ArrayList<LibraryAdapter.HeaderPair> (), new byte[] {});

        assertEquals ("PUT", result.getMethod ());
    }

    @Test
    public void convertsToDeleteRequest () throws Exception {
        HttpDelete result = (HttpDelete)subject.convert ("DELETE", "http://x.com", new ArrayList<LibraryAdapter.HeaderPair> (), new byte[] {});

        assertEquals ("DELETE", result.getMethod ());
    }

    @Test
    public void convertsToHeadRequest () throws Exception {
        HttpHead result = (HttpHead)subject.convert ("HEAD", "http://x.com", new ArrayList<LibraryAdapter.HeaderPair> (), new byte[] {});

        assertEquals ("HEAD", result.getMethod ());
    }

    @Test
    public void complainsAboutUnknownMethod () throws Exception {
        try {
            subject.convert ("QUARBLEY", "http://x.com", new ArrayList<LibraryAdapter.HeaderPair> (), new byte[] {});
            fail ();
        }
        catch (IllegalArgumentException e) {
            assertEquals ("Unexpected request method QUARBLEY", e.getMessage ());
        }
    }

    @Test
    public void convertsToResponseWithoutHeadersOrBody () throws Exception {
        HttpResponse result = subject.convert (202,
                new ArrayList<> (), new byte[] {});

        assertEquals (202, result.getStatusLine ().getStatusCode ());
        assertEquals (0, result.getAllHeaders ().length);
        InputStream istr = result.getEntity ().getContent ();
        assertEquals (-1, istr.read ());
    }

    @Test
    public void convertsToResponseWithHeadersAndBody () throws Exception {
        HttpResponse result = subject.convert (504,
                Arrays.asList (
                        new LibraryAdapter.HeaderPair ("bloopety", "bloppety"),
                        new LibraryAdapter.HeaderPair ("flippety", "floppety")
                ), "Zibble is a whimpfer woddle".getBytes ());

        assertEquals (504, result.getStatusLine ().getStatusCode ());
        assertEquals ("bloppety", result.getHeaders ("bloopety")[0].getValue ());
        assertEquals (1, result.getHeaders ("bloopety").length);
        assertEquals ("floppety", result.getHeaders ("flippety")[0].getValue ());
        assertEquals (1, result.getHeaders ("flippety").length);
        assertEquals (2, result.getAllHeaders ().length);
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        result.getEntity ().writeTo (baos);
        assertEquals ("Zibble is a whimpfer woddle", new String (baos.toByteArray ()));
    }

    @Test
    public void isolatesRequestMethod () {
        assertEquals ("GET", subject.getRequestMethod (requestWithoutHeadersOrBody));
    }

    @Test
    public void isolatesRequestUri () {
        assertEquals ("http://a.b.c:4700/bliggety", subject.getRequestUri (requestWithoutHeadersOrBody));
    }

    @Test
    public void isolatesEmptyRequestHeaders () {
        assertEquals (0, subject.getRequestHeaders (requestWithoutHeadersOrBody).size ());
    }

    @Test
    public void isolatesNonEmptyRequestHeaders () {
        List<LibraryAdapter.HeaderPair> pairs = subject.getRequestHeaders (requestWithHeadersAndBody);

        assertEquals ("bloopety", pairs.get (0).name ());
        assertEquals ("bloppety", pairs.get (0).value ());
        assertEquals ("flippety", pairs.get (1).name ());
        assertEquals ("floppety", pairs.get (1).value ());
    }

    @Test
    public void isolatesEmptyRequestBody () throws Exception {
        assertEquals (0, subject.getRequestBody (requestWithoutHeadersOrBody).length);
    }

    @Test
    public void isolatesNonEmptyRequestBody () throws Exception {
        byte[] body = subject.getRequestBody (requestWithHeadersAndBody);

        assertArrayEquals ("wibbledy wobbledy woo".getBytes (), body);
    }

    @Test
    public void isolatesResponseStatus () throws Exception {
        assertEquals (201, subject.getResponseStatus (responseWithoutHeadersOrBody));
    }

    @Test
    public void isolatesEmptyResponseHeaders () {
        assertEquals (0, subject.getResponseHeaders (responseWithoutHeadersOrBody).size ());
    }

    @Test
    public void isolatesNonEmptyResponseHeaders () {
        List<LibraryAdapter.HeaderPair> pairs = subject.getResponseHeaders (responseWithHeadersAndBody);

        assertEquals ("bloopety", pairs.get (0).name ());
        assertEquals ("bloppety", pairs.get (0).value ());
        assertEquals ("flippety", pairs.get (1).name ());
        assertEquals ("floppety", pairs.get (1).value ());
    }

    @Test
    public void isolatesEmptyResponseBody () throws Exception {
        assertEquals (0, subject.getResponseBody (responseWithoutHeadersOrBody).length);
    }

    @Test
    public void isolatesNonEmptyResponseBody () throws Exception {
        byte[] body = subject.getResponseBody (responseWithHeadersAndBody);

        assertArrayEquals ("wibbledy wobbledy woo".getBytes (), body);
    }
}
