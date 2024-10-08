/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.http;

import com.blackduck.integration.rest.HttpUrl;

import java.util.List;

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
