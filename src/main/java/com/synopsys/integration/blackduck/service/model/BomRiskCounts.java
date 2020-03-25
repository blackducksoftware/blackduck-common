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
import com.synopsys.integration.blackduck.api.generated.enumeration.ComponentVersionRiskProfileRiskDataCountsCountTypeType;

import java.math.BigDecimal;
import java.util.function.Consumer;

import static com.synopsys.integration.blackduck.api.generated.enumeration.ComponentVersionRiskProfileRiskDataCountsCountTypeType.*;

public class BomRiskCounts {
    private BigDecimalWrapper high = new BigDecimalWrapper();
    private BigDecimalWrapper medium = new BigDecimalWrapper();
    private BigDecimalWrapper low = new BigDecimalWrapper();

    public void add(ComponentVersionRiskProfileRiskDataCountsView countsView) {
        ComponentVersionRiskProfileRiskDataCountsCountTypeType countType = countsView.getCountType();
        if (HIGH == countType) {
            addTo(high::add, countsView);
        } else if (MEDIUM == countType) {
            addTo(medium::add, countsView);
        } else if (LOW == countType) {
            addTo(low::add, countsView);
        }
    }

    public void add(BomRiskCounts bomRiskCounts) {
        if (bomRiskCounts.high.isGreaterThanZero()) {
            addTo(high::add);
        } else if (bomRiskCounts.medium.isGreaterThanZero()) {
            addTo(medium::add);
        } else if (bomRiskCounts.low.isGreaterThanZero()) {
            addTo(low::add);
        }
    }

    public int getHigh() {
        return high.bigDecimal.intValue();
    }

    public int getMedium() {
        return medium.bigDecimal.intValue();
    }

    public int getLow() {
        return low.bigDecimal.intValue();
    }

    private void addTo(Consumer<BigDecimal> adder, ComponentVersionRiskProfileRiskDataCountsView countsView) {
        adder.accept(countsView.getCount());
    }

    private void addTo(Consumer<BigDecimal> adder) {
        adder.accept(BigDecimal.ONE);
    }

    private class BigDecimalWrapper {
        public BigDecimal bigDecimal = BigDecimal.ZERO;

        public void add(BigDecimal toAdd) {
            this.bigDecimal = this.bigDecimal.add(toAdd);
        }

        public boolean isGreaterThanZero() {
            return bigDecimal.compareTo(BigDecimal.ZERO) > 0;
        }
    }

}
