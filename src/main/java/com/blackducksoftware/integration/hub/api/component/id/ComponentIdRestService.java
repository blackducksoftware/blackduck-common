package com.blackducksoftware.integration.hub.api.component.id;

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

public class ComponentIdRestService extends HubItemRestService<ComponentIdItem> {

    private static Type ITEM_TYPE = new TypeToken<ComponentIdItem>() {
    }.getType();

    private static Type ITEM_LIST_TYPE = new TypeToken<List<ComponentIdItem>>() {
    }.getType();

    public ComponentIdRestService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
        super(restConnection, gson, jsonParser, ITEM_TYPE, ITEM_LIST_TYPE);
    }

    public ComponentIdItem getComponent(final String componentURL)
            throws IOException, URISyntaxException, BDRestException {
        final HubRequest componentRequest = new HubRequest(getRestConnection(), getJsonParser());
        componentRequest.setMethod(Method.GET);
        componentRequest.setLimit(1);
        componentRequest.setUrl(componentURL);
        final JsonObject jsonObject = componentRequest.executeForResponseJson();
        final ComponentIdItem component = getItem(jsonObject, ComponentIdItem.class);
        return component;
    }

}
