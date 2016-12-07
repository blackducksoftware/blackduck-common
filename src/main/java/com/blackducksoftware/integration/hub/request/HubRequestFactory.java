/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.request;

import java.util.List;

import com.blackducksoftware.integration.hub.rest.RestConnection;

public class HubRequestFactory {
    private final RestConnection restConnection;

    public HubRequestFactory(RestConnection restConnection) {
        this.restConnection = restConnection;
    }

    public HubRequest createGetRequest(List<String> urlSegments) {
        final HubRequest hubRequest = new HubRequest(restConnection);
        hubRequest.addUrlSegments(urlSegments);
        return hubRequest;
    }

    public HubRequest createGetRequest(String url) {
        final HubRequest hubRequest = new HubRequest(restConnection);
        hubRequest.setUrl(url);
        return hubRequest;
    }

    public HubPagedRequest createGetPagedRequest(List<String> urlSegments) {
        final HubPagedRequest hubPagedRequest = new HubPagedRequest(restConnection);
        hubPagedRequest.setLimit(100);
        hubPagedRequest.addUrlSegments(urlSegments);
        return hubPagedRequest;
    }

    public HubPagedRequest createGetPagedRequest(int itemsPerPage, List<String> urlSegments) {
        final HubPagedRequest hubPagedRequest = new HubPagedRequest(restConnection);
        hubPagedRequest.setLimit(itemsPerPage);
        hubPagedRequest.addUrlSegments(urlSegments);
        return hubPagedRequest;
    }

    public HubPagedRequest createGetPagedRequest(List<String> urlSegments, String q) {
        final HubPagedRequest hubPagedRequest = new HubPagedRequest(restConnection);
        hubPagedRequest.setLimit(100);
        hubPagedRequest.addUrlSegments(urlSegments);
        hubPagedRequest.setQ(q);
        return hubPagedRequest;
    }

    public HubPagedRequest createGetPagedRequest(int itemsPerPage, List<String> urlSegments, String q) {
        final HubPagedRequest hubPagedRequest = new HubPagedRequest(restConnection);
        hubPagedRequest.setLimit(itemsPerPage);
        hubPagedRequest.addUrlSegments(urlSegments);
        hubPagedRequest.setQ(q);
        return hubPagedRequest;
    }

    public HubPagedRequest createGetPagedRequest(String url) {
        final HubPagedRequest hubPagedRequest = new HubPagedRequest(restConnection);
        hubPagedRequest.setLimit(100);
        hubPagedRequest.setUrl(url);
        return hubPagedRequest;
    }

    public HubPagedRequest createGetPagedRequest(int itemsPerPage, String url) {
        final HubPagedRequest hubPagedRequest = new HubPagedRequest(restConnection);
        hubPagedRequest.setLimit(itemsPerPage);
        hubPagedRequest.setUrl(url);
        return hubPagedRequest;
    }

    public HubPagedRequest createGetPagedRequest(String url, String q) {
        final HubPagedRequest hubPagedRequest = new HubPagedRequest(restConnection);
        hubPagedRequest.setLimit(100);
        hubPagedRequest.setUrl(url);
        hubPagedRequest.setQ(q);
        return hubPagedRequest;
    }

    public HubPagedRequest createGetPagedRequest(int itemsPerPage, String url, String q) {
        final HubPagedRequest hubPagedRequest = new HubPagedRequest(restConnection);
        hubPagedRequest.setLimit(itemsPerPage);
        hubPagedRequest.setUrl(url);
        hubPagedRequest.setQ(q);
        return hubPagedRequest;
    }

    public HubRequest createPostRequest(List<String> urlSegments) {
        final HubRequest hubRequest = new HubRequest(restConnection);
        hubRequest.addUrlSegments(urlSegments);
        return hubRequest;
    }

    public HubRequest createPostRequest(String url) {
        final HubRequest hubRequest = new HubRequest(restConnection);
        hubRequest.setUrl(url);
        return hubRequest;
    }

    public HubRequest createDeleteRequest(String url) {
        final HubRequest hubRequest = new HubRequest(restConnection);
        hubRequest.setUrl(url);
        return hubRequest;
    }

}
