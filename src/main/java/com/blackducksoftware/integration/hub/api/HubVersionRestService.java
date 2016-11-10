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
package com.blackducksoftware.integration.hub.api;

import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_API;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_CURRENT_VERSION;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_V1;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.restlet.data.Method;

import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.JsonParser;

public class HubVersionRestService extends HubRestService {
    private static final List<String> CURRENT_VERSION_SEGMENTS = Arrays.asList(SEGMENT_API, SEGMENT_V1, SEGMENT_CURRENT_VERSION);

    public HubVersionRestService(RestConnection restConnection) {
        super(restConnection);
    }

    public String getHubVersion() throws IOException, ResourceDoesNotExistException, URISyntaxException, BDRestException {
        HubRequest hubVersionRequest = new HubRequest(getRestConnection(), new JsonParser());
        hubVersionRequest.setMethod(Method.GET);
        hubVersionRequest.addUrlSegments(CURRENT_VERSION_SEGMENTS);

        String hubVersionWithPossibleSurroundingQuotes = hubVersionRequest.executeForResponseString();
        String hubVersion = hubVersionWithPossibleSurroundingQuotes.replace("\"", "");

        return hubVersion;
    }

}
