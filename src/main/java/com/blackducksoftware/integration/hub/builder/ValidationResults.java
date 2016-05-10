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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

public class ValidationResults<Key, Type> {

	private Type constructedObject;
	private final Map<Key, Map<ValidationResultEnum, List<ValidationResult>>> resultMap;
	private final Set<ValidationResultEnum> status = new LinkedHashSet<ValidationResultEnum>();

	public ValidationResults() {
		resultMap = new HashMap<Key, Map<ValidationResultEnum, List<ValidationResult>>>();
	}

	public void addAllResults(final Map<Key, List<ValidationResult>> results) {
		for (final Entry<Key, List<ValidationResult>> entry : results.entrySet()) {
			for (final ValidationResult result : entry.getValue()) {
				// This will prevent duplication
				addResult(entry.getKey(), result);
			}
		}
	}

	public void addResult(final Key fieldKey, final ValidationResult result) {
		final List<ValidationResult> resultList;
		final Map<ValidationResultEnum, List<ValidationResult>> resultListMap;
		final ValidationResultEnum resultType = result.getResultType();
		if (resultMap.containsKey(fieldKey)) {
			resultListMap = resultMap.get(fieldKey);

		} else {
			resultListMap = new LinkedHashMap<ValidationResultEnum, List<ValidationResult>>();
			resultMap.put(fieldKey, resultListMap);
		}

		if (resultListMap.containsKey(resultType)) {
			resultList = resultListMap.get(result.getResultType());
		} else {
			resultList = new Vector<ValidationResult>();
			resultListMap.put(resultType, resultList);
		}
		status.add(resultType);

		if (!resultList.contains(result)) {
			// This will prevent duplication
			resultList.add(result);
		}
	}

	public Map<Key, List<ValidationResult>> getResultMap() {
		final Map<Key, List<ValidationResult>> map = new HashMap<Key, List<ValidationResult>>();
		for (final Key fieldKey : resultMap.keySet()) {
			map.put(fieldKey, getResultList(fieldKey));
		}
		return map;
	}

	public List<ValidationResult> getResultList(final Key fieldKey) {
		if (resultMap.containsKey(fieldKey)) {
			final List<ValidationResult> resultList = new Vector<ValidationResult>();
			final Map<ValidationResultEnum, List<ValidationResult>> itemList = resultMap.get(fieldKey);
			for (final ValidationResultEnum key : itemList.keySet()) {
				resultList.addAll(itemList.get(key));
			}

			return resultList;
		} else {
			return new Vector<ValidationResult>();
		}
	}

	public List<String> getResultList(final Key fieldKey, final ValidationResultEnum resultEnum) {
		final List<String> resultList = new ArrayList<String>();
		if (resultMap.containsKey(fieldKey)) {
			final Map<ValidationResultEnum, List<ValidationResult>> listMap = resultMap.get(fieldKey);
			if (listMap.containsKey(resultEnum)) {
				final List<ValidationResult> itemList = listMap.get(resultEnum);
				for (final ValidationResult result : itemList) {
					resultList.add(result.getMessage());
				}
			}
		}
		return resultList;
	}

	public String getResultString(final Key fieldKey, final ValidationResultEnum resultEnum) {
		String resultString = "";
		final List<String> resultList = getResultList(fieldKey, resultEnum);
		if (!resultList.isEmpty()) {
			resultString = StringUtils.join(resultList, "\n");
		}
		return resultString;
	}

	public List<Throwable> getResultThrowables(final Key fieldKey, final ValidationResultEnum resultEnum) {
		final List<Throwable> throwables = new ArrayList<Throwable>();

		if (resultMap.containsKey(fieldKey)) {
			final Map<ValidationResultEnum, List<ValidationResult>> listMap = resultMap.get(fieldKey);
			if (listMap.containsKey(resultEnum)) {
				final List<ValidationResult> itemList = listMap.get(resultEnum);
				for (final ValidationResult result : itemList) {
					throwables.add(result.getThrowable());
				}
			}
		}
		return throwables;
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

	public boolean hasErrors(final Key fieldKey) {
		if (resultMap.containsKey(fieldKey)) {
			return resultMap.get(fieldKey).containsKey(ValidationResultEnum.ERROR);
		} else {
			return false;
		}
	}

	public boolean hasWarnings(final Key fieldKey) {
		if (resultMap.containsKey(fieldKey)) {
			return resultMap.get(fieldKey).containsKey(ValidationResultEnum.WARN);
		} else {
			return false;
		}
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

	public boolean isEmpty() {
		return status.isEmpty();
	}
}
