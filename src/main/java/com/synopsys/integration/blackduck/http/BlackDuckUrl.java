/*
 * blackduck-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.rest.HttpUrl;

/**
 * A url of the form:
 * https://{black_duck_hostname}/api/{searchTerm1}/{searchTerm1Id}/
 * {searchTerm2}/{searchTerm2Id}/{searchTerm3}/{searchTerm3Id}...
 */
public class BlackDuckUrl {
    private final HttpUrl url;

    public BlackDuckUrl(HttpUrl url) {
        this.url = url;
    }

    public String parseId(List<BlackDuckUrlSearchTerm> searchTerms) {
        String searching = url.string();
        int afterLastTermIndex = -1;
        for (BlackDuckUrlSearchTerm searchTerm : searchTerms) {
            afterLastTermIndex = searching.indexOf(searchTerm.getTerm(), afterLastTermIndex) + 1;
        }
        int afterFirstSlashIndex = searching.indexOf('/', afterLastTermIndex) + 1;
        int secondSlashIndex = searching.indexOf('/', afterFirstSlashIndex);
        int end = secondSlashIndex > afterFirstSlashIndex ? secondSlashIndex : searching.length();
        searching = searching.substring(afterFirstSlashIndex, end);
        return searching;
    }

}
