package mockability.client.adapters;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by dnwiebe on 7/20/15.
 */
public class HttpServletAdapterTest {

    private HttpServletAdapter subject;
    private MockHttpServletRequest requestWithoutHeadersOrBody;
    private MockHttpServletRequest requestWithHeadersAndBody;
    private MockHttpServletResponse responseWithoutHeadersOrBody;
    private MockHttpServletResponse responseWithHeadersAndBody;

    @Before
    public void setup () throws Exception {
        subject = new HttpServletAdapter ();

        requestWithoutHeadersOrBody = new MockHttpServletRequest ();
        requestWithoutHeadersOrBody.setMethod ("GET");
        requestWithoutHeadersOrBody.setRequestURI ("http://a.b.c:4700/bliggety");
        requestWithHeadersAndBody = new MockHttpServletRequest ();
        requestWithHeadersAndBody.setRequestURI ("http://a.b.c:4700/bloggety");
        requestWithHeadersAndBody.addHeader ("bloopety", "bloppety");
        requestWithHeadersAndBody.addHeader ("flippety", "floppety");
        requestWithHeadersAndBody.setContent ("wibbledy wobbledy woo".getBytes ());

        responseWithoutHeadersOrBody = new MockHttpServletResponse ();
        responseWithoutHeadersOrBody.setStatus (201);
        responseWithHeadersAndBody = new MockHttpServletResponse ();
        responseWithHeadersAndBody.setStatus (503);
        responseWithHeadersAndBody.addHeader ("bloopety", "bloppety");
        responseWithHeadersAndBody.addHeader ("flippety", "floppety");
        responseWithHeadersAndBody.getOutputStream ().write ("wibbledy wobbledy woo".getBytes ());
    }

    @Test
    public void convertsToGetRequestWithoutHeadersOrBody () throws Exception {
        MockHttpServletRequest result = subject.convert ("GET", "http://foppy/clang?glooby=yes&gloppy=no",
                new ArrayList<> (), new byte[] {});

        assertEquals ("GET", result.getMethod ());
        assertEquals ("http://foppy/clang?glooby=yes&gloppy=no", result.getRequestURI ());
        assertEquals (0, countHeaders (result));
        assertEquals (0, result.getContentLength ());
    }

    @Test
    public void convertsToPostRequestWithHeadersAndBody () throws Exception {
        MockHttpServletRequest result = subject.convert ("POST", "http://foppy/clang?glooby=yes&gloppy=no",
                Arrays.asList (
                        new LibraryAdapter.HeaderPair ("bloopety", "bloppety"),
                        new LibraryAdapter.HeaderPair ("flippety", "floppety")
                ), "Zibble is a whimpfer woddle".getBytes ());

        assertEquals ("http://foppy/clang?glooby=yes&gloppy=no", result.getRequestURI ());
        assertEquals ("bloppety", result.getHeaders ("bloopety").nextElement ());
        assertEquals ("floppety", result.getHeaders ("flippety").nextElement ());
        assertEquals (2, countHeaders (result));
        byte[] buf = new byte[100];
        int len = result.getInputStream ().read (buf, 0, buf.length);
        assertEquals ("Zibble is a whimpfer woddle", new String (buf, 0, len));
    }

    @Test
    public void convertsToPutRequest () throws Exception {
        MockHttpServletRequest result = subject.convert ("PUT", "http://x.com", new ArrayList<> (), new byte[] {});

        assertEquals ("PUT", result.getMethod ());
    }

    @Test
    public void convertsToDeleteRequest () throws Exception {
        MockHttpServletRequest result = subject.convert ("DELETE", "http://x.com", new ArrayList<> (), new byte[] {});

        assertEquals ("DELETE", result.getMethod ());
    }

    @Test
    public void convertsToHeadRequest () throws Exception {
        MockHttpServletRequest result = subject.convert ("HEAD", "http://x.com", new ArrayList<> (), new byte[] {});

        assertEquals ("HEAD", result.getMethod ());
    }

    @Test
    public void complainsAboutUnknownMethod () throws Exception {
        try {
            subject.convert ("QUARBLEY", "http://x.com", new ArrayList<> (), new byte[] {});
            fail ();
        }
        catch (IllegalArgumentException e) {
            assertEquals ("Unexpected request method QUARBLEY", e.getMessage ());
        }
    }

    @Test
    public void convertsToResponseWithoutHeadersOrBody () throws Exception {
        MockHttpServletResponse result = subject.convert (202,
                new ArrayList<> (), new byte[] {});

        assertEquals (202, result.getStatus ());
        assertEquals (0, result.getHeaderNames ().size ());
        assertEquals ("", result.getContentAsString ());
    }

    @Test
    public void convertsToResponseWithHeadersAndBody () throws Exception {
        MockHttpServletResponse result = subject.convert (504,
                Arrays.asList (
                        new LibraryAdapter.HeaderPair ("bloopety", "bloppety"),
                        new LibraryAdapter.HeaderPair ("flippety", "floppety")
                ), "Zibble is a whimpfer woddle".getBytes ());

        assertEquals (504, result.getStatus ());
        assertEquals ("bloppety", result.getHeaders ("bloopety").get (0));
        assertEquals (1, result.getHeaders ("bloopety").size ());
        assertEquals ("floppety", result.getHeaders ("flippety").get (0));
        assertEquals (1, result.getHeaders ("flippety").size ());
        assertEquals (2, result.getHeaderNames ().size ());
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        assertEquals ("Zibble is a whimpfer woddle", result.getContentAsString ());
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

    private int countHeaders (MockHttpServletRequest request) {
        int count = 0;
        Enumeration<String> names = request.getHeaderNames ();
        while (names.hasMoreElements ()) {
            String name = names.nextElement ();
            Enumeration<String> values = request.getHeaders (name);
            while (values.hasMoreElements ()) {
                count++;
                values.nextElement ();
            }
        }
        return count;
    }
}
