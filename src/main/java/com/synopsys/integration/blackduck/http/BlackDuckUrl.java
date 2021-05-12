package com.synopsys.integration.blackduck.http;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.rest.HttpUrl;

public class BlackDuckUrl {
    private final HttpUrl url;

    public BlackDuckUrl(HttpUrl url) {
        this.url = url;
    }

    public String parseId(List<String> searchTerms) {
        String searching = url.string();
        int afterLastTermIndex = -1;
        for (String term : searchTerms) {
            afterLastTermIndex = searching.indexOf(term, afterLastTermIndex) + 1;
        }
        int afterFirstSlashIndex = searching.indexOf('/', afterLastTermIndex) + 1;
        int secondSlashIndex = searching.indexOf('/', afterFirstSlashIndex);
        int end = secondSlashIndex > afterFirstSlashIndex ? secondSlashIndex : searching.length();
        searching = searching.substring(afterFirstSlashIndex, end);
        return searching;
    }

}
