/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.model;

import com.blackduck.integration.blackduck.api.generated.component.RiskProfileCountsView;
import com.blackduck.integration.blackduck.api.generated.enumeration.RiskPriorityType;
import com.blackduck.integration.blackduck.api.generated.view.RiskProfileView;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class RiskProfileCounts {
    private final Map<RiskPriorityType, BigDecimal> countsMap;

    public RiskProfileCounts(RiskProfileView view) {
        countsMap = new HashMap<>();
        for (RiskPriorityType value : RiskPriorityType.values()) {
            countsMap.put(value, new BigDecimal(0));
        }
        if (view != null) {
            for (RiskProfileCountsView count : view.getCounts()) {
                countsMap.put(count.getCountType(), count.getCount());
            }
        }
    }

    public BigDecimal getCount(RiskPriorityType level) {
        return countsMap.get(level);
    }

}
