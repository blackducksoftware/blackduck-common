package com.synopsys.integration.blackduck.service.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.rest.request.Request;

@ExtendWith(TimingExtension.class)
public class RequestFactoryTest {
    @Test
    public void testFilterWithMultipleValues() {
        BlackDuckRequestFilter blackDuckRequestFilter = BlackDuckRequestFilter.createFilterWithMultipleValues("KEY1", Arrays.asList(new String[] { "value1", "value2" }));
        Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder("http://www.url.com/api/something", RequestFactory.DEFAULT_MEDIA_TYPE, Optional.empty(), blackDuckRequestFilter, 1, 0);
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
