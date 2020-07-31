package com.synopsys.integration.blackduck.http;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.discovery.MediaTypeDiscovery;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TimingExtension.class)
public class RequestFactoryTest {
    @Test
    public void testFilterWithMultipleValues() throws IntegrationException {
        BlackDuckRequestFilter blackDuckRequestFilter = BlackDuckRequestFilter.createFilterWithMultipleValues("KEY1", Arrays.asList(new String[]{"value1", "value2"}));
        RequestFactory requestFactory = new RequestFactory(new MediaTypeDiscovery());
        BlackDuckRequestBuilder requestBuilder = requestFactory.createCommonGetRequestBuilder(new HttpUrl("http://www.url.com/api/something"), Optional.empty(), blackDuckRequestFilter, 1, 0);
        Request request = requestBuilder.build();

        assertTrue(request.getQueryParameters().containsKey("filter"));
        assertTrue(request.getQueryParameters().containsKey("limit"));
        assertTrue(request.getQueryParameters().containsKey("offset"));

        Set<String> filterParameters = request.getQueryParameters().get("filter");
        assertEquals(2, filterParameters.size());
        assertTrue(filterParameters.contains("KEY1:value1"));
        assertTrue(filterParameters.contains("KEY1:value2"));
    }

}
