/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *******************************************************************************/
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
