package com.synopsys.integration.hub.service.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;

import com.synopsys.integration.rest.request.Request;

public class RequestFactoryTest {
    @Test
    public void testFilterWithMultipleValues() {
        final HubFilter hubFilter = HubFilter.createFilterWithMultipleValues("KEY1", Arrays.asList(new String[] { "value1", "value2" }));
        final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder("http://www.url.com/api/something", Optional.empty(), hubFilter, 1, 0);
        final Request request = requestBuilder.build();

        assertTrue(request.getQueryParameters().containsKey("filter"));
        assertTrue(request.getQueryParameters().containsKey("limit"));
        assertTrue(request.getQueryParameters().containsKey("offset"));

        final Set<String> filterParameters = request.getQueryParameters().get("filter");
        assertEquals(2, filterParameters.size());
        assertTrue(filterParameters.contains("KEY1:value1"));
        assertTrue(filterParameters.contains("KEY1:value2"));
    }

}
