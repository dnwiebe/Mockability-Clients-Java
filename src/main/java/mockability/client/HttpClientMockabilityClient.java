package mockability.client;

import mockability.client.adapters.HttpClientAdapter;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * Created by dnwiebe on 7/23/15.
 */
public class HttpClientMockabilityClient extends MockabilityClient<HttpRequestBase, HttpResponse> {
    public HttpClientMockabilityClient (HttpClientAdapter adapter, String baseUrl) {
        super (adapter, baseUrl);
    }
}
