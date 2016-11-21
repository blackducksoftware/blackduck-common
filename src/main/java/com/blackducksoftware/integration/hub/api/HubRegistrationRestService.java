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
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_REGISTRATIONS;
import static com.blackducksoftware.integration.hub.api.UrlConstants.SEGMENT_V1;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.restlet.data.Method;

import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.JsonObject;

public class HubRegistrationRestService extends HubRestService {
    private static final List<String> REGISTRATION_SEGMENTS = Arrays.asList(SEGMENT_API, SEGMENT_V1, SEGMENT_REGISTRATIONS);

    public HubRegistrationRestService(RestConnection restConnection) {
        super(restConnection);
    }

    public String getRegistrationId() throws IOException, URISyntaxException, BDRestException {
        final HubRequest registrationRequest = new HubRequest(getRestConnection());
        registrationRequest.setMethod(Method.GET);
        registrationRequest.addUrlSegments(REGISTRATION_SEGMENTS);

        final JsonObject jsonObject = registrationRequest.executeForResponseJson();
        String registrationId = jsonObject.get("registrationId").getAsString();
        return registrationId;
    }

}
