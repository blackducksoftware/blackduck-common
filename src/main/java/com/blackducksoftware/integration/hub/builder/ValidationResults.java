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
package com.blackducksoftware.integration.hub.builder;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

public class ValidationResults<Key, Type> {

	private Type constructedObject;
	private final Map<Key, Map<ValidationResultEnum, List<ValidationResult>>> resultMap;
	private final Set<ValidationResultEnum> status = EnumSet.noneOf(ValidationResultEnum.class);

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

	public List<String> getAllResultList(final ValidationResultEnum resultEnum) {
		final List<String> resultList = new ArrayList<String>();

		for (final Entry<Key, Map<ValidationResultEnum, List<ValidationResult>>> entry : resultMap.entrySet()) {
			if (entry.getValue().containsKey(resultEnum)) {
				for (final ValidationResult result : entry.getValue().get(resultEnum)) {
					resultList.add(result.getMessage());
				}
			}
		}
		return resultList;
	}

	public String getAllResultString(final ValidationResultEnum resultEnum) {
		String resultString = "";

		final List<String> resultList = getAllResultList(resultEnum);
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
					if (result.getThrowable() != null) {
						throwables.add(result.getThrowable());
					}
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
