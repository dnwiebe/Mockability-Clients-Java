package mockability.client.adapters;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by dnwiebe on 7/29/15.
 */
public class HttpServletAdapter implements LibraryAdapter<MockHttpServletRequest, MockHttpServletResponse> {

    @Override
    public MockHttpServletRequest convert (String method, String uri, List<HeaderPair> headers, byte[] body) throws Exception {
        if (!"|HEAD|GET|POST|PUT|DELETE|".contains ("|" + method + "|")) {
            throw new IllegalArgumentException ("Unexpected request method " + method);
        }
        MockHttpServletRequest request = new MockHttpServletRequest ();
        request.setMethod (method);
        request.setRequestURI (uri);
        for (HeaderPair pair : headers) {request.addHeader (pair.name (), pair.value ());}
        request.setContent (body);
        return request;
    }

    @Override
    public MockHttpServletResponse convert (int status, List<HeaderPair> headers, byte[] body) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse ();
        response.setStatus (status);
        for (HeaderPair pair : headers) {response.addHeader (pair.name (), pair.value ());}
        if (body.length > 0) {response.getOutputStream ().write (body);}
        return response;
    }

    @Override
    public String getRequestMethod (MockHttpServletRequest request) {
        return request.getMethod ();
    }

    @Override
    public String getRequestUri (MockHttpServletRequest request) {
        return request.getRequestURI ();
    }

    @Override
    public List<HeaderPair> getRequestHeaders (MockHttpServletRequest request) {
        List<HeaderPair> pairs = new ArrayList<> ();
        Enumeration<String> names = request.getHeaderNames ();
        while (names.hasMoreElements ()) {
            String name = names.nextElement ();
            Enumeration<String> values = request.getHeaders (name);
            while (values.hasMoreElements ()) {
                String value = values.nextElement ();
                pairs.add (new HeaderPair (name, value));
            }
        }
        return pairs;
    }

    @Override
    public byte[] getRequestBody (MockHttpServletRequest request) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        InputStream istr = request.getInputStream ();
        byte[] buf = new byte[1024];
        while (true) {
            int len = istr.read (buf);
            if (len < 0) {break;}
            baos.write (buf, 0, len);
        }
        return baos.toByteArray ();
    }

    @Override
    public int getResponseStatus (MockHttpServletResponse response) {
        return response.getStatus ();
    }

    @Override
    public List<HeaderPair> getResponseHeaders (MockHttpServletResponse response) {
        List<HeaderPair> pairs = new ArrayList<> ();
        Collection<String> names = response.getHeaderNames ();
        for (String name : names) {
            Collection<String> values = response.getHeaders (name);
            for (String value : values) {
                pairs.add (new HeaderPair (name, value));
            }
        }
        return pairs;
    }

    @Override
    public byte[] getResponseBody (MockHttpServletResponse response) throws Exception {
        return response.getContentAsByteArray ();
    }
}
