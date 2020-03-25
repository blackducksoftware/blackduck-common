package com.synopsys.integration.blackduck.service.model;

import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionRiskProfileRiskDataCountsView;

import java.math.BigDecimal;
import java.util.function.Consumer;

public class BomRiskCounts {
    private BigDecimalWrapper high = new BigDecimalWrapper();
    private BigDecimalWrapper medium = new BigDecimalWrapper();
    private BigDecimalWrapper low = new BigDecimalWrapper();

    public void add(ComponentVersionRiskProfileRiskDataCountsView countsView) {
        switch (countsView.getCountType()) {
            case HIGH:
                addTo(high::add, countsView);
            case MEDIUM:
                addTo(medium::add, countsView);
            case LOW:
                addTo(low::add, countsView);
        }
    }

    public void add(BomRiskCounts bomRiskCounts) {
        addTo(high::add, bomRiskCounts.high);
        addTo(medium::add, bomRiskCounts.medium);
        addTo(low::add, bomRiskCounts.low);
    }

    private void addTo(Consumer<BigDecimal> adder, ComponentVersionRiskProfileRiskDataCountsView countsView) {
        adder.accept(countsView.getCount());
    }

    private void addTo(Consumer<BigDecimal> adder, BigDecimalWrapper bigDecimalWrapper) {
        adder.accept(bigDecimalWrapper.bigDecimal);
    }

    private class BigDecimalWrapper {
        public BigDecimal bigDecimal = BigDecimal.ZERO;

        public void add(BigDecimal toAdd) {
            this.bigDecimal = this.bigDecimal.add(toAdd);
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

}
