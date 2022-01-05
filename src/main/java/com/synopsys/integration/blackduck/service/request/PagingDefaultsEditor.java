/*
 * blackduck-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.request;

import java.util.function.Consumer;
import java.util.function.Function;

import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;

/**
 * Often, a request to Black Duck will need to have defaults set for paging. If
 * explicit values have not already been set, this will add the defaults to the
 * BlackDuckRequestBuilder.
 */
public class PagingDefaultsEditor implements BlackDuckRequestBuilderEditor {
    @Override
    public void edit(BlackDuckRequestBuilder blackDuckRequestBuilder) {
        Function<String, Boolean> contains = blackDuckRequestBuilder.getQueryParameters()::containsKey;
        conditionallySet(contains, BlackDuckRequestBuilder.LIMIT_PARAMETER, blackDuckRequestBuilder::setLimit, BlackDuckRequestBuilder.DEFAULT_LIMIT);
        conditionallySet(contains, BlackDuckRequestBuilder.OFFSET_PARAMETER, blackDuckRequestBuilder::setOffset, BlackDuckRequestBuilder.DEFAULT_OFFSET);
    }

    public void conditionallySet(Function<String, Boolean> contains, String key, Consumer<Integer> setter, int value) {
        if (!contains.apply(key)) {
            setter.accept(value);
        }
    }

}
