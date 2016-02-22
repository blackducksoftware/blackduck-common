/*
 * Copyright (C) 2014 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.report.risk.api;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * Summary stats of items that fall in each risk priority per risk category
 *
 *
 */
public class RiskProfile {
    private final int numberOfItems;

    private final Map<RiskCategory, RiskPriorityDistribution> categories;

    public RiskProfile() {
        this(0, Maps.<RiskCategory, RiskPriorityDistribution> newHashMap());
    }

    public RiskProfile(Map<RiskCategory, RiskPriorityDistribution> categories) {
        this(1, categories);
    }

    public RiskProfile(List<RiskCategory> categories) {
        Map<RiskCategory, RiskPriorityDistribution> riskPriorityDistributionMap = Maps.newHashMap();
        for (RiskCategory category : categories) {
            riskPriorityDistributionMap.put(category, new RiskPriorityDistribution());
        }

        numberOfItems = 0;
        this.categories = riskPriorityDistributionMap;
    }

    public RiskProfile(
            int numberOfItems,
            Map<RiskCategory, RiskPriorityDistribution> categories) {
        this.numberOfItems = numberOfItems;
        this.categories = ImmutableMap.copyOf(categories);
    }

    /**
     * total number of items
     *
     * @return
     */
    public int getNumberOfItems() {
        return numberOfItems;
    }

    /**
     * per risk category, the number of items with certain RiskPriority.
     *
     * @return
     */
    public Map<RiskCategory, RiskPriorityDistribution> getCategories() {
        return categories;
    }

    private int getItemCount(Map<RiskCategory, RiskPriorityDistribution> map) {
        int numItems = 0;

        for (Entry<RiskCategory, RiskPriorityDistribution> entry : map.entrySet()) {
            RiskPriorityDistribution distribution = entry.getValue();
            for (int count : distribution.getCounts()) {
                if (count > 0) {
                    numItems++;
                }
            }
        }

        return numItems;
    }

    public RiskPriorityDistribution getRiskPriorityDistribution(RiskCategory riskCategory) {
        if (!getCategories().isEmpty()) {
            return categories.get(riskCategory);
        }
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((categories == null) ? 0 : categories.hashCode());
        result = prime * result + numberOfItems;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RiskProfile other = (RiskProfile) obj;
        if (categories == null) {
            if (other.categories != null) {
                return false;
            }
        } else if (!categories.equals(other.categories)) {
            return false;
        }
        if (numberOfItems != other.numberOfItems) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RiskProfile [numberOfItems=");
        builder.append(numberOfItems);
        builder.append(", categories=");
        builder.append(categories);
        builder.append("]");
        return builder.toString();
    }

}
