package mockability.client;

import mockability.client.adapters.HttpServletAdapter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Created by dnwiebe on 7/23/15.
 */
public class HttpServletMockabilityClient extends MockabilityClient<MockHttpServletRequest, MockHttpServletResponse> {
    public HttpServletMockabilityClient (HttpServletAdapter adapter, String baseUrl) {
        super (adapter, baseUrl);
    }
}
