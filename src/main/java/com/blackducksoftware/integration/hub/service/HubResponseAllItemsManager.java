package com.blackducksoftware.integration.hub.service;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.model.HubResponse;
import com.blackducksoftware.integration.hub.model.HubView;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.request.HubRequestFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.Response;

public class HubResponseAllItemsManager {
    private final HubResponseItemsManager hubResponseItemsManager;
    private final HubRequestFactory hubRequestFactory;
    private final MetaService metaService;
    private final JsonParser jsonParser;

    public HubResponseAllItemsManager(final HubResponseItemsManager hubResponseItemsManager, final HubRequestFactory hubRequestFactory, final MetaService metaService, final JsonParser jsonParser) {
        this.hubResponseItemsManager = hubResponseItemsManager;
        this.hubRequestFactory = hubRequestFactory;
        this.metaService = metaService;
        this.jsonParser = jsonParser;
    }

    public <T extends HubResponse> List<T> getAllItemsFromApi(final String apiSegment, final Class<T> clazz) throws IntegrationException {
        return getAllItemsFromApi(apiSegment, clazz, 100, null);
    }

    public <T extends HubResponse> List<T> getAllItemsFromApi(final String apiSegment, final Class<T> clazz, final String mediaType) throws IntegrationException {
        return getAllItemsFromApi(apiSegment, clazz, 100, mediaType);
    }

    public <T extends HubResponse> List<T> getAllItemsFromApi(final String apiSegment, final Class<T> clazz, final int itemsPerPage) throws IntegrationException {
        return getAllItemsFromApi(apiSegment, clazz, itemsPerPage, null);
    }

    public <T extends HubResponse> List<T> getAllItemsFromApi(final String apiSegment, final Class<T> clazz, final int itemsPerPage, final String mediaType) throws IntegrationException {
        final HubPagedRequest hubPagedRequest = hubRequestFactory.createPagedRequest(itemsPerPage, Arrays.asList(SEGMENT_API, apiSegment));
        return getAllItems(hubPagedRequest, clazz, mediaType);
    }

    public <T extends HubResponse> List<T> getAllItemsFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        return getAllItemsFromLinkSafely(hubView, metaLinkRef, clazz, null);
    }

    public <T extends HubResponse> List<T> getAllItemsFromLinkSafely(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final String mediaType) throws IntegrationException {
        if (!metaService.hasLink(hubView, metaLinkRef)) {
            return Collections.emptyList();
        }

        return getAllItemsFromLink(hubView, metaLinkRef, clazz, mediaType);
    }

    public <T extends HubResponse> List<T> getAllItemsFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz) throws IntegrationException {
        return getAllItemsFromLink(hubView, metaLinkRef, clazz, null);
    }

    public <T extends HubResponse> List<T> getAllItemsFromLink(final HubView hubView, final String metaLinkRef, final Class<T> clazz, final String mediaType) throws IntegrationException {
        final String link = metaService.getFirstLink(hubView, metaLinkRef);
        return getAllItems(link, clazz, mediaType);
    }

    public <T extends HubResponse> List<T> getAllItems(final HubPagedRequest hubPagedRequest, final Class<T> clazz) throws IntegrationException {
        return getAllItems(hubPagedRequest, clazz, null);
    }

    public <T extends HubResponse> List<T> getAllItems(final String url, final Class<T> clazz) throws IntegrationException {
        return getAllItems(url, clazz, null);
    }

    public <T extends HubResponse> List<T> getAllItems(final String url, final Class<T> clazz, final String mediaType) throws IntegrationException {
        final HubPagedRequest hubPagedRequest = hubRequestFactory.createPagedRequest(url);
        return getAllItems(hubPagedRequest, clazz, mediaType);
    }

    public <T extends HubResponse> List<T> getAllItems(final HubPagedRequest hubPagedRequest, final Class<T> clazz, final String mediaType) throws IntegrationException {
        final List<T> allItems = new LinkedList<>();
        int totalCount = 0;
        int currentOffset = hubPagedRequest.offset;
        Response response = null;
        try {
            if (StringUtils.isNotBlank(mediaType)) {
                response = hubPagedRequest.executeGet(mediaType);
            } else {
                response = hubPagedRequest.executeGet();
            }
            final String jsonResponse = response.body().string();

            final JsonObject jsonObject = jsonParser.parse(jsonResponse).getAsJsonObject();
            totalCount = jsonObject.get("totalCount").getAsInt();
            allItems.addAll(hubResponseItemsManager.getItems(jsonObject, clazz));
            while (allItems.size() < totalCount && currentOffset < totalCount) {
                currentOffset += hubPagedRequest.limit;
                hubPagedRequest.offset = currentOffset;
                allItems.addAll(hubResponseItemsManager.getItems(hubPagedRequest, clazz, mediaType));
            }
        } catch (final IOException e) {
            throw new HubIntegrationException(e);
        } finally {
            IOUtils.closeQuietly(response);
        }
        return allItems;
    }

}
