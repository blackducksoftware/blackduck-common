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
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

public class ValidationResult<T> {
	private final ValidationResultEnum resultType;

	private final Map<String, List<String>> messageMap;

	private final T returnObject;

	public ValidationResult(final ValidationResultEnum resultType, final T returnObject) {
		this.resultType = resultType;
		this.returnObject = returnObject;
		messageMap = new HashMap<String, List<String>>();
	}

	public ValidationResultEnum getResultType() {
		return resultType;
	}

	public void addMessage(final String fieldKey, final String message) {

		List<String> messageList;
		if (messageMap.containsKey(fieldKey)) {
			messageList = messageMap.get(fieldKey);
		} else {
			messageList = new Vector<String>();
			messageMap.put(fieldKey, messageList);
		}

		messageList.add(message);
	}

	public T getReturnObject() {
		return returnObject;
	}

	public String getValidationMessages(final String fieldKey) {
		String message = "";

		if (messageMap.containsKey(fieldKey)) {
			final List<String> messageList = messageMap.get(fieldKey);

			message = StringUtils.join(messageList, " ");
		}
		return message;
	}

	public List<String> getValidationMessageList(final String fieldKey) {

		if (messageMap.containsKey(fieldKey)) {
			return messageMap.get(fieldKey);
		} else {
			return new Vector<String>();
		}
	}
}