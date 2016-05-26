/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
