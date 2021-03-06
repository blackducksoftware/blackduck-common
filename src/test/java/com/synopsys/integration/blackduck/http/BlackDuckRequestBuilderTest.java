package com.synopsys.integration.blackduck.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;

@ExtendWith(TimingExtension.class)
public class BlackDuckRequestBuilderTest {
    @Test
    public void testFilterWithMultipleValues() throws IntegrationException {
        BlackDuckRequestFilter blackDuckRequestFilter = BlackDuckRequestFilter.createFilterWithMultipleValues("KEY1", Arrays.asList("value1", "value2"));
        BlackDuckRequestBuilder requestBuilder = new BlackDuckRequestBuilder()
                                                     .commonGet()
                                                     .addBlackDuckFilter(blackDuckRequestFilter)
                                                     .setBlackDuckPageDefinition(new BlackDuckPageDefinition(1, 0))
                                                     .url(new HttpUrl("http://www.url.com/api/something"));
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
