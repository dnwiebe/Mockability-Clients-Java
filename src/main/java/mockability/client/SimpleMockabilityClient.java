package mockability.client;

import mockability.client.adapters.SimpleAdapter;

import static mockability.client.adapters.SimpleAdapter.*;
/**
 * Created by ga-mlsdiscovery on 8/4/15.
 */

public class SimpleMockabilityClient extends MockabilityClient<SimpleRequest, SimpleResponse> {

    public SimpleMockabilityClient(String baseUrl) {
        super(new SimpleAdapter(), baseUrl);
    }
}
