package mockability.client.adapters;

import org.junit.Before;
import org.junit.Test;
import java.util.*;

import static org.junit.Assert.*;
import static mockability.client.adapters.LibraryAdapter.*;
import static mockability.client.adapters.SimpleAdapter.*;

/**
 * Created by dnwiebe on 7/20/15.
 */
public class SimpleAdapterTest {

    private SimpleAdapter subject;
    private SimpleRequest requestWithoutHeadersOrBody;
    private SimpleRequest requestWithHeadersAndBody;
    private SimpleResponse responseWithoutHeadersOrBody;
    private SimpleResponse responseWithHeadersAndBody;

    @Before
    public void setup () {
        subject = new SimpleAdapter ();

        requestWithoutHeadersOrBody = new SimpleRequest ("GET", "http://a.b.c:4700/bliggety", Collections.emptyList());
        requestWithHeadersAndBody = new SimpleRequest ("POST", "http://a.b.c:4700/bloggety", Arrays.asList (
                new HeaderPair("bloopety", "bloppety"),
                new HeaderPair("flippety", "floppety")
        ), "wibbledy wobbledy woo".getBytes());

        responseWithoutHeadersOrBody = new SimpleResponse (201, Collections.emptyList());
        responseWithHeadersAndBody = new SimpleResponse (503, Arrays.asList (
                new HeaderPair("bloopety", "bloppety"),
                new HeaderPair("flippety", "floppety")
        ), "wibbledy wobbledy woo".getBytes ());
    }

    @Test
    public void convertsToGetRequestWithoutHeadersOrBody () {
        SimpleRequest result = subject.convert ("GET", "http://foppy/clang?glooby=yes&gloppy=no",
                new ArrayList<> (), new byte[] {});

        assertEquals ("GET", result.getMethod());
        assertEquals ("http://foppy/clang?glooby=yes&gloppy=no", result.getUri());
        assertEquals (true, result.getHeaders ().isEmpty ());
    }

    @Test
    public void convertsToPostRequestWithHeadersAndBody () {
        SimpleRequest result = subject.convert ("POST", "http://foppy/clang?glooby=yes&gloppy=no",
                Arrays.asList (
                        new HeaderPair ("bloopety", "bloppety"),
                        new HeaderPair ("flippety", "floppety")
                ), "Zibble is a whimpfer woddle".getBytes ());

        assertEquals ("http://foppy/clang?glooby=yes&gloppy=no", result.getUri ());
        HeaderPair header = result.getHeaders ().get (0);
        assertEquals ("bloopety", header.name ());
        assertEquals ("bloppety", header.value ());
        header = result.getHeaders ().get (1);
        assertEquals ("flippety", header.name ());
        assertEquals ("floppety", header.value ());
        assertEquals (2, result.getHeaders ().size ());
        assertEquals ("Zibble is a whimpfer woddle", new String (result.getBody ()));
    }

    @Test
    public void convertsToPutRequest () {
        SimpleRequest result = subject.convert ("PUT", "http://x.com", new ArrayList<> (), new byte[] {});

        assertEquals ("PUT", result.getMethod ());
    }

    @Test
    public void convertsToDeleteRequest () {
        SimpleRequest result = subject.convert ("DELETE", "http://x.com", new ArrayList<> (), new byte[] {});

        assertEquals ("DELETE", result.getMethod ());
    }

    @Test
    public void convertsToHeadRequest () {
        SimpleRequest result = subject.convert ("HEAD", "http://x.com", new ArrayList<> (), new byte[] {});

        assertEquals ("HEAD", result.getMethod ());
    }

    @Test
    public void complainsAboutUnknownMethod () {
        try {
            subject.convert ("QUARBLEY", "http://x.com", new ArrayList<> (), new byte[] {});
            fail ();
        }
        catch (IllegalArgumentException e) {
            assertEquals ("Unexpected request method QUARBLEY", e.getMessage ());
        }
    }

    @Test
    public void convertsToResponseWithoutHeadersOrBody () {
        SimpleResponse result = subject.convert (202,
                new ArrayList<> (), new byte[] {});

        assertEquals (202, result.getStatus());
        assertEquals (true, result.getHeaders().isEmpty());
        assertEquals (0, result.getBody ().length);
    }

    @Test
    public void convertsToResponseWithHeadersAndBody () {
        SimpleResponse result = subject.convert (504,
                Arrays.asList (
                        new LibraryAdapter.HeaderPair ("bloopety", "bloppety"),
                        new LibraryAdapter.HeaderPair ("flippety", "floppety")
                ), "Zibble is a whimpfer woddle".getBytes ());

        assertEquals (504, result.getStatus ());
        HeaderPair header = result.getHeaders ().get (0);
        assertEquals ("bloopety", header.name ());
        assertEquals ("bloppety", header.value ());
        header = result.getHeaders ().get (1);
        assertEquals ("flippety", header.name ());
        assertEquals ("floppety", header.value ());
        assertEquals (2, result.getHeaders ().size ());
        assertEquals ("Zibble is a whimpfer woddle", new String (result.getBody ()));
    }

    @Test
    public void isolatesRequestMethod () {
        assertEquals ("GET", subject.getRequestMethod(requestWithoutHeadersOrBody));
    }

    @Test
    public void isolatesRequestUri () {
        assertEquals ("http://a.b.c:4700/bliggety", subject.getRequestUri(requestWithoutHeadersOrBody));
    }

    @Test
    public void isolatesEmptyRequestHeaders () {
        assertEquals (0, subject.getRequestHeaders(requestWithoutHeadersOrBody).size());
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
    public void isolatesEmptyRequestBody () {
        assertEquals(0, subject.getRequestBody(requestWithoutHeadersOrBody).length);
    }

    @Test
    public void isolatesNonEmptyRequestBody () {
        byte[] body = subject.getRequestBody (requestWithHeadersAndBody);

        assertArrayEquals("wibbledy wobbledy woo".getBytes(), body);
    }

    @Test
    public void isolatesResponseStatus () {
        assertEquals (201, subject.getResponseStatus(responseWithoutHeadersOrBody));
    }

    @Test
    public void isolatesEmptyResponseHeaders () {
        assertEquals (0, subject.getResponseHeaders(responseWithoutHeadersOrBody).size());
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
    public void isolatesEmptyResponseBody () {
        assertEquals(0, subject.getResponseBody(responseWithoutHeadersOrBody).length);
    }

    @Test
    public void isolatesNonEmptyResponseBody () {
        byte[] body = subject.getResponseBody (responseWithHeadersAndBody);

        assertArrayEquals ("wibbledy wobbledy woo".getBytes (), body);
    }
}
