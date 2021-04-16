/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.request;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.blackduck.api.core.response.LinkSingleResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;

public class BlackDuckApiRequestSpec2<T extends BlackDuckResponse> {
    private final Request request;
    private final Class<T> responseClass;

    public static <T extends BlackDuckResponse> BlackDuckApiRequestSingleSpec2<T> fromBlackDuckView(BlackDuckRequestFactory blackDuckRequestFactory, BlackDuckView blackDuckView, LinkSingleResponse<T> linkSingleResponse) {
        HttpUrl url = blackDuckView.getFirstLink(linkSingleResponse.getLink());
        Request request = blackDuckRequestFactory.createCommonGetRequest(url);
        return new BlackDuckApiRequestSingleSpec2<>(request, linkSingleResponse.getResponseClass());
    }

    public BlackDuckApiRequestSpec2(Request request, Class<T> responseClass) {
        this.responseClass = responseClass;
        this.request = request;
    }

    public Request getRequest() {
        return request;
    }

    public Class<T> getResponseClass() {
        return responseClass;
    }

}
