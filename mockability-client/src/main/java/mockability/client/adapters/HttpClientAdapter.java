package mockability.client.adapters;

import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;

import static mockability.utils.Utils.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dnwiebe on 7/19/15.
 */
public class HttpClientAdapter implements LibraryAdapter<HttpRequestBase, HttpResponse> {
    @Override
    public HttpRequestBase convert(String method, String uri, List<HeaderPair> headers, byte[] body) throws Exception {
        HttpRequestBase request = makeRequest (method, uri);
        addHeaders (request, headers);
        if (HttpEntityEnclosingRequest.class.isAssignableFrom (request.getClass ())) {
            HttpEntityEnclosingRequest heeRequest = (HttpEntityEnclosingRequest)request;
            heeRequest.setEntity (new ByteArrayEntity (body));
        }
        return request;
    }

    @Override
    public HttpResponse convert(int status, List<HeaderPair> headers, byte[] body) {
        BasicHttpResponse response = new BasicHttpResponse (new ProtocolVersion ("HTTP", 1, 1), status, "");
        addHeaders (response, headers);
        response.setEntity (new ByteArrayEntity (body));
        return response;
    }

    @Override
    public String getRequestMethod(HttpRequestBase request) {
        return request.getMethod ();
    }

    @Override
    public String getRequestUri(HttpRequestBase request) {
        return request.getURI ().toString ();
    }

    @Override
    public List<HeaderPair> getRequestHeaders(HttpRequestBase request) {
        return extractHeaderPairs (request);
    }

    @Override
    public byte[] getRequestBody(HttpRequestBase request) throws Exception {
        if (HttpEntityEnclosingRequestBase.class.isAssignableFrom (request.getClass ())) {
            return inputStreamToByteArray (((HttpEntityEnclosingRequestBase)request).getEntity ().getContent ());
        }
        return new byte[0];
    }

    @Override
    public int getResponseStatus(HttpResponse response) {
        return response.getStatusLine ().getStatusCode ();
    }

    @Override
    public List<HeaderPair> getResponseHeaders(HttpResponse response) {
        return extractHeaderPairs (response);
    }

    @Override
    public byte[] getResponseBody(HttpResponse response) throws Exception {
        if (response.getEntity () == null) {
            return new byte[] {};
        }
        return inputStreamToByteArray (response.getEntity ().getContent ());
    }

    private void addHeaders (HttpMessage msg, List<HeaderPair> headers) {
        for (HeaderPair header : headers) {
            msg.addHeader (new BasicHeader (header.name (), header.value ()));
        }
    }

    private HttpRequestBase makeRequest (String method, String uri) throws Exception {
        URI realUri = new URI (uri);
        switch (method) {
            case "GET": return new HttpGet (realUri);
            case "POST": return new HttpPost (realUri);
            case "PUT": return new HttpPut (realUri);
            case "DELETE": return new HttpDelete (realUri);
            case "HEAD": return new HttpHead (realUri);
            default: throw new IllegalArgumentException ("Unexpected request method " + method);
        }
    }

    private List<HeaderPair> extractHeaderPairs (HttpMessage msg) {
        List<HeaderPair> pairs = new ArrayList<> ();
        for (Header header : msg.getAllHeaders ()) {
            pairs.add (new HeaderPair (header.getName (), header.getValue ()));
        }
        return pairs;
    }

    private byte[] inputStreamToByteArray (InputStream istr) throws Exception {
        ByteArrayOutputStream ostr = new ByteArrayOutputStream ();
        byte[] buf = new byte[1024];
        while (true) {
            int len = istr.read (buf);
            if (len < 0) {break;}
            ostr.write (buf, 0, len);
        }
        return ostr.toByteArray ();
    }
}
