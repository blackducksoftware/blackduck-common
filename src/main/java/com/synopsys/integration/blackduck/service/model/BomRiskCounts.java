/**
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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

import static com.synopsys.integration.blackduck.api.generated.enumeration.RiskPriorityType.HIGH;
import static com.synopsys.integration.blackduck.api.generated.enumeration.RiskPriorityType.LOW;
import static com.synopsys.integration.blackduck.api.generated.enumeration.RiskPriorityType.MEDIUM;

import com.synopsys.integration.blackduck.api.generated.component.RiskProfileCountsView;

public class BomRiskCounts {
    private int high;
    private int medium;
    private int low;

    public void add(RiskProfileCountsView countsView) {
        int count = countsView.getCount().intValue();
        if (HIGH == countsView.getCountType()) {
            high += count;
        } else if (MEDIUM == countsView.getCountType()) {
            medium += count;
        } else if (LOW == countsView.getCountType()) {
            low += count;
        }
    }

    public void add(BomRiskCounts bomRiskCounts) {
        if (bomRiskCounts.getHigh() > 0) {
            high++;
        } else if (bomRiskCounts.getMedium() > 0) {
            medium++;
        } else if (bomRiskCounts.getLow() > 0) {
            low++;
        }
    }

    public int getHigh() {
        return high;
    }

    public int getMedium() {
        return medium;
    }

    public int getLow() {
        return low;
    }

}
