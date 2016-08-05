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
package com.blackducksoftware.integration.hub.item;

import java.util.List;

/**
 * A list of items as returned by the Hub.
 *
 * @author sbillings
 *
 */
public class HubItems {
	private int totalCount;
	private List<HubItem> items;

	public int getTotalCount() {
		return totalCount;
	}

	public List<HubItem> getItems() {
		return items;
	}

	public void setTotalCount(final int totalCount) {
		this.totalCount = totalCount;
	}

	public void setItems(final List<HubItem> items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return "HubItemList [totalCount=" + totalCount + ", items=" + items + "]";
	}

}
