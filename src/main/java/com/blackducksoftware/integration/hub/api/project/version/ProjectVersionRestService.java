package com.blackducksoftware.integration.hub.api.project.version;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.List;

import org.restlet.data.Method;

import com.blackducksoftware.integration.hub.api.HubItemRestService;
import com.blackducksoftware.integration.hub.api.HubRequest;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class ProjectVersionRestService extends HubItemRestService<ProjectVersionItem> {
    private static final Type ITEM_TYPE = new TypeToken<ProjectVersionItem>() {
    }.getType();

    private static final Type ITEM_LIST_TYPE = new TypeToken<List<ProjectVersionItem>>() {
    }.getType();

    public ProjectVersionRestService(final RestConnection restConnection, final Gson gson,
            final JsonParser jsonParser) {
        super(restConnection, gson, jsonParser, ITEM_TYPE, ITEM_LIST_TYPE);
    }

    public List<ProjectVersionItem> getAllProjectVersions(final String versionsUrl)
            throws IOException, URISyntaxException, BDRestException {
        final HubRequest projectVersionItemRequest = new HubRequest(getRestConnection(), getJsonParser());
        projectVersionItemRequest.setMethod(Method.GET);
        projectVersionItemRequest.setLimit(100);
        projectVersionItemRequest.setUrl(versionsUrl);

        final JsonObject jsonObject = projectVersionItemRequest.executeForResponseJson();
        final List<ProjectVersionItem> allProjectVersionItems = getAll(jsonObject, projectVersionItemRequest);
        return allProjectVersionItems;
    }

}
