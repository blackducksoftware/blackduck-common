/**
 * blackduck-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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

import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionRiskProfileRiskDataCountsView;

import java.math.BigDecimal;

import static com.synopsys.integration.blackduck.api.generated.enumeration.ComponentVersionRiskProfileRiskDataCountsCountTypeType.*;

public class BomRiskCounts {
    private BigDecimal high = BigDecimal.ZERO;
    private BigDecimal medium = BigDecimal.ZERO;
    private BigDecimal low = BigDecimal.ZERO;

    public void add(ComponentVersionRiskProfileRiskDataCountsView countsView) {
        if (HIGH == countsView.getCountType()) {
            high = high.add(countsView.getCount());
        } else if (MEDIUM == countsView.getCountType()) {
            medium = medium.add(countsView.getCount());
        } else if (LOW == countsView.getCountType()) {
            low = low.add(countsView.getCount());
        }
    }

    public void add(BomRiskCounts bomRiskCounts) {
        if (bomRiskCounts.getHigh() > 0) {
            high = high.add(BigDecimal.ONE);
        } else if (bomRiskCounts.getMedium() > 0) {
            medium = medium.add(BigDecimal.ONE);
        } else if (bomRiskCounts.getLow() > 0) {
            low = low.add(BigDecimal.ONE);
        }
    }

    public int getHigh() {
        return high.intValue();
    }

    public int getMedium() {
        return medium.intValue();
    }

    public int getLow() {
        return low.intValue();
    }

}
