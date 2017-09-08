/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
 */
package com.blackducksoftware.integration.hub.dataservice.model;

import java.util.HashMap;
import java.util.Map;

import com.blackducksoftware.integration.hub.model.enumeration.RiskCountEnum;
import com.blackducksoftware.integration.hub.model.view.components.RiskCountView;
import com.blackducksoftware.integration.hub.model.view.components.RiskProfileView;

public class RiskProfileCountsModel {
    private Map<RiskCountEnum, Integer> countsMap;

    public RiskProfileCountsModel(final RiskProfileView view) {
        initCountsMap();
        if (view != null) {
            for (final RiskCountView count : view.counts) {
                if (count.countType == RiskCountEnum.HIGH) {
                    countsMap.put(RiskCountEnum.HIGH, count.count);
                } else if (count.countType == RiskCountEnum.MEDIUM) {
                    countsMap.put(RiskCountEnum.MEDIUM, count.count);
                } else if (count.countType == RiskCountEnum.LOW) {
                    countsMap.put(RiskCountEnum.LOW, count.count);
                } else if (count.countType == RiskCountEnum.OK) {
                    countsMap.put(RiskCountEnum.OK, count.count);
                } else if (count.countType == RiskCountEnum.UNKNOWN) {
                    countsMap.put(RiskCountEnum.UNKNOWN, count.count);
                }
            }
        }
    }

    public int getCount(final RiskCountEnum level) {
        return countsMap.get(level);
    }

    private void initCountsMap() {
        countsMap = new HashMap<>();
        for (final RiskCountEnum value : RiskCountEnum.values()) {
            countsMap.put(value, 0);
        }
    }
}
