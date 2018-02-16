package com.blackducksoftware.integration.hub.service.model;

import static com.blackducksoftware.integration.hub.RestConstants.QUERY_LIMIT;
import static com.blackducksoftware.integration.hub.RestConstants.QUERY_OFFSET;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.blackducksoftware.integration.hub.request.BodyContent;
import com.blackducksoftware.integration.hub.request.Request;
import com.blackducksoftware.integration.hub.rest.HttpMethod;

public class RequestFactory {
    public static Request.Builder createCommonGetRequestBuilder(final String uri) {
        final Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put(QUERY_OFFSET, String.valueOf(0));
        queryParameters.put(QUERY_LIMIT, String.valueOf(100));
        return new Request.Builder(uri).queryParameters(queryParameters);
    }

    public static Request.Builder createCommonGetRequestBuilder() {
        final Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put(QUERY_OFFSET, String.valueOf(0));
        queryParameters.put(QUERY_LIMIT, String.valueOf(100));
        return new Request.Builder().queryParameters(queryParameters);
    }

    public static Request createCommonGetRequest(final String uri) {
        return createCommonGetRequestBuilder(uri).build();
    }

    public static Request.Builder createCommonPostRequestBuilder(final File bodyContentFile) {
        return new Request.Builder().method(HttpMethod.POST).bodyContent(new BodyContent(bodyContentFile));
    }

    public static Request.Builder createCommonPostRequestBuilder(final Map<String, String> bodyContentMap) {
        return new Request.Builder().method(HttpMethod.POST).bodyContent(new BodyContent(bodyContentMap));
    }

    public static Request.Builder createCommonPostRequestBuilder(final String bodyContent) {
        return new Request.Builder().method(HttpMethod.POST).bodyContent(new BodyContent(bodyContent));
    }

    public static Request.Builder createCommonPostRequestBuilder(final Object bodyContentObject) {
        return new Request.Builder().method(HttpMethod.POST).bodyContent(new BodyContent(bodyContentObject));
    }

}
