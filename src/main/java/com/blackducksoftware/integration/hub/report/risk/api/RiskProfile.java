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

/**
 * Summary stats of items that fall in each risk priority per risk category
 *
 *
 */
public class RiskProfile {
	private final int numberOfItems;

	private final RiskCategories categories;

	public RiskProfile(
			final int numberOfItems,
			final RiskCategories categories) {
		this.numberOfItems = numberOfItems;
		this.categories = categories;
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
	public RiskCategories getCategories() {
		return categories;
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
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RiskProfile)) {
			return false;
		}
		final RiskProfile other = (RiskProfile) obj;
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
		final StringBuilder builder = new StringBuilder();
		builder.append("RiskProfile [numberOfItems=");
		builder.append(numberOfItems);
		builder.append(", categories=");
		builder.append(categories);
		builder.append("]");
		return builder.toString();
	}

}
