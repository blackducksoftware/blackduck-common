/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.api;

import static com.blackducksoftware.integration.hub.api.UrlConstants.QUERY_LIMIT;
import static com.blackducksoftware.integration.hub.api.UrlConstants.QUERY_OFFSET;

import com.blackducksoftware.integration.hub.rest.RestConnection;

/**
 * This should be used to communicate to the Hub REST endpoints that can return many items across pages of data. These
 * are the endpoints that have the limit and offset query parameters.
 */
public class HubPagedRequest extends HubRequest {
    private int limit = 10;

    private int offset = 0;

    public HubPagedRequest(final RestConnection restConnection) {
        super(restConnection);
    }

    @Override
    public void populateQueryParameters() {
        // if limit is not provided, the default is 10
        if (limit <= 0) {
            limit = 10;
        }
        addQueryParameter(QUERY_LIMIT, String.valueOf(limit));

        // if offset is not provided, the default is 0
        addQueryParameter(QUERY_OFFSET, String.valueOf(offset));
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

}
