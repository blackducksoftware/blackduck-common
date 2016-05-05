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
package com.blackducksoftware.integration.hub.builder;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class ValidationResults<Key, Type> {

	private Type constructedObject;
	private final Map<Key, List<ValidationResult>> resultMap;
	private final Set<ValidationResultEnum> status = new LinkedHashSet<ValidationResultEnum>();

	public ValidationResults() {
		resultMap = new HashMap<Key, List<ValidationResult>>();
	}

	public void addResult(final Key fieldKey, final ValidationResult result) {
		final List<ValidationResult> resultList;

		if (resultMap.containsKey(fieldKey)) {
			resultList = resultMap.get(fieldKey);
		} else {
			resultList = new Vector<ValidationResult>();
			resultMap.put(fieldKey, resultList);
		}

		status.add(result.getResultType());

		resultList.add(result);
	}

	public Type getConstructedObject() {
		return constructedObject;
	}

	public void setConstructedObject(final Type constructedObject) {
		this.constructedObject = constructedObject;
	}

	public Set<ValidationResultEnum> getValidationStatus() {

		return status;
	}

	public boolean hasErrors() {
		return status.contains(ValidationResultEnum.ERROR);
	}

	public boolean hasWarnings() {
		return status.contains(ValidationResultEnum.WARN);
	}

	public boolean isSuccess() {
		return (status.size() == 1 && status.contains(ValidationResultEnum.OK));
	}
}
