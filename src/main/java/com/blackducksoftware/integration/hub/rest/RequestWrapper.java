package com.blackducksoftware.integration.hub.rest;

import java.io.File;
import java.util.Map;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.core.HubComponent;
import com.google.gson.JsonObject;

/**
 * For Requests that are NOT GET requests
 */
public class RequestWrapper extends BaseRequestWrapper {
    private String bodyContent;
    private Map<String, String> bodyContentMap;
    private File bodyContentFile;
    private HubComponent hubComponent;
    private JsonObject jsonObject;
    private final HttpMethod method;

    public RequestWrapper(final HttpMethod method) throws IntegrationException {
        if (null == method) {
            throw new IntegrationException("The HttpMethod can not be null");
        }
        if (HttpMethod.GET == method) {
            throw new IntegrationException("The HttpMethod can not be GET");
        }
        this.method = method;
    }

    public RequestWrapper(final HttpMethod method, final String bodyContent) throws IntegrationException {
        this(method);
        this.bodyContent = bodyContent;
    }

    public RequestWrapper(final HttpMethod method, final HubComponent hubComponent) throws IntegrationException {
        this(method);
        this.hubComponent = hubComponent;
    }

    public RequestWrapper(final HttpMethod method, final JsonObject jsonObject) throws IntegrationException {
        this(method);
        this.jsonObject = jsonObject;
    }

    public RequestWrapper(final HttpMethod method, final Map<String, String> bodyContentMap) throws IntegrationException {
        this(method);
        this.bodyContentMap = bodyContentMap;
    }

    public RequestWrapper(final HttpMethod method, final File bodyContentFile) throws IntegrationException {
        this(method);
        this.bodyContentFile = bodyContentFile;
    }

    public HubComponent getHubComponent() {
        return hubComponent;
    }

    public JsonObject getJsonObject() {
        return jsonObject;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getBodyContent() {
        return bodyContent;
    }

    public Map<String, String> getBodyContentMap() {
        return bodyContentMap;
    }

    public File getBodyContentFile() {
        return bodyContentFile;
    }
}
