/**
 * blackduck-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
 */
package com.synopsys.integration.blackduck.service.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionRiskProfileRiskDataCountsView;
import com.synopsys.integration.blackduck.api.generated.enumeration.ComponentVersionRiskProfileRiskDataCountsCountTypeType;
import com.synopsys.integration.blackduck.api.generated.view.RiskProfileView;

public class RiskProfileCounts {
    private final Map<ComponentVersionRiskProfileRiskDataCountsCountTypeType, BigDecimal> countsMap;

    public RiskProfileCounts(final RiskProfileView view) {
        countsMap = new HashMap<>();
        for (final ComponentVersionRiskProfileRiskDataCountsCountTypeType value : ComponentVersionRiskProfileRiskDataCountsCountTypeType.values()) {
            countsMap.put(value, new BigDecimal(0));
        }
        if (view != null) {
            for (final ComponentVersionRiskProfileRiskDataCountsView count : view.getCounts()) {
                countsMap.put(count.getCountType(), count.getCount());
            }
        }
    }

    public BigDecimal getCount(final ComponentVersionRiskProfileRiskDataCountsCountTypeType level) {
        return countsMap.get(level);
    }
}
