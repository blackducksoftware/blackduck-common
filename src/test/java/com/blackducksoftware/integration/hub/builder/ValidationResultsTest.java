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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class ValidationResultsTest {
	private static final String KEY_PREFIX = "key-";
	private static final String TEST_MESSAGE_PREFIX = "Test Message ";
	private static final String KEY_2 = "key-2";
	private static final String KEY_1 = "key-1";
	private static final String KEY_0 = "key-0";

	private ValidationResults<String, String> createTestData(final List<ValidationResultEnum> resultTypeList) {
		final ValidationResults<String, String> results = new ValidationResults<String, String>();
		final int count = resultTypeList.size();
		for (int index = 0; index < count; index++) {
			final String key = KEY_PREFIX + index;
			final String message = TEST_MESSAGE_PREFIX + index;

			final ValidationResult result = new ValidationResult(resultTypeList.get(index), message);
			results.addResult(key, result);
		}

		return results;
	}

	@Test
	public void testValidationResultConstructor() {

		final Throwable throwable = new RuntimeException();
		final ValidationResult result = new ValidationResult(ValidationResultEnum.OK, TEST_MESSAGE_PREFIX, throwable);

		assertNotNull(result);
		assertEquals(result.getResultType(), ValidationResultEnum.OK);
		assertEquals(result.getMessage(), TEST_MESSAGE_PREFIX);
		assertEquals(result.getThrowable(), throwable);
	}

	@Test
	public void testValidationResultEquals() {
		EqualsVerifier.forClass(ValidationResult.class).suppress(Warning.STRICT_INHERITANCE).verify();
	}

	@Test
	public void testValidationResultsConstructor() {
		final ValidationResults<Object, Object> result = new ValidationResults<Object, Object>();
		assertNotNull(result);
		assertTrue(result.isEmpty());
		assertFalse(result.isSuccess());
	}

	@Test
	public void testAddResult() {

		final List<ValidationResultEnum> items = new ArrayList<ValidationResultEnum>();
		items.add(ValidationResultEnum.OK);
		items.add(ValidationResultEnum.WARN);
		items.add(ValidationResultEnum.ERROR);

		final ValidationResults<String, String> results = createTestData(items);

		assertNotNull(results);
		assertFalse(results.isEmpty());
		assertFalse(results.isSuccess());
		assertTrue(results.hasErrors());
		assertTrue(results.hasWarnings());
	}

	@Test
	public void testSuccess() {
		final List<ValidationResultEnum> items = new ArrayList<ValidationResultEnum>();
		items.add(ValidationResultEnum.OK);
		items.add(ValidationResultEnum.OK);
		items.add(ValidationResultEnum.OK);

		final ValidationResults<String, String> results = createTestData(items);

		assertNotNull(results);
		assertFalse(results.isEmpty());
		assertTrue(results.isSuccess());
	}

	@Test
	public void testWarnings() {
		final List<ValidationResultEnum> items = new ArrayList<ValidationResultEnum>();
		items.add(ValidationResultEnum.WARN);
		items.add(ValidationResultEnum.WARN);
		items.add(ValidationResultEnum.WARN);

		final ValidationResults<String, String> results = createTestData(items);

		assertNotNull(results);
		assertFalse(results.isEmpty());
		assertFalse(results.isSuccess());
		assertTrue(results.hasWarnings());
	}

	@Test
	public void testErrors() {
		final List<ValidationResultEnum> items = new ArrayList<ValidationResultEnum>();
		items.add(ValidationResultEnum.ERROR);
		items.add(ValidationResultEnum.ERROR);
		items.add(ValidationResultEnum.ERROR);

		final ValidationResults<String, String> results = createTestData(items);

		assertNotNull(results);
		assertFalse(results.isEmpty());
		assertFalse(results.isSuccess());
		assertTrue(results.hasErrors());
	}

	@Test
	public void testGetMap() {
		final List<ValidationResultEnum> items = new ArrayList<ValidationResultEnum>();
		items.add(ValidationResultEnum.OK);
		items.add(ValidationResultEnum.WARN);
		items.add(ValidationResultEnum.ERROR);

		final ValidationResults<String, String> results = createTestData(items);

		assertNotNull(results);
		final Map<String, List<ValidationResult>> map = results.getResultMap();
		assertTrue(map.get(KEY_0).contains(new ValidationResult(ValidationResultEnum.OK, TEST_MESSAGE_PREFIX + "0")));
		assertTrue(map.get(KEY_1).contains(new ValidationResult(ValidationResultEnum.WARN, TEST_MESSAGE_PREFIX + "1")));
		assertTrue(
				map.get(KEY_2).contains(new ValidationResult(ValidationResultEnum.ERROR, TEST_MESSAGE_PREFIX + "2")));
	}

	@Test
	public void testGetResultList() {
		final List<ValidationResultEnum> items = new ArrayList<ValidationResultEnum>();
		items.add(ValidationResultEnum.ERROR);

		final ValidationResults<String, String> results = createTestData(items);

		assertNotNull(results);
		final String anotherMsg = "Test Warning Message";
		results.addResult(KEY_0, new ValidationResult(ValidationResultEnum.WARN, anotherMsg));
		results.addResult(KEY_0, new ValidationResult(ValidationResultEnum.WARN, anotherMsg));

		final List<ValidationResult> resultList = results.getResultList(KEY_0);

		assertEquals(resultList.size(), 2);
		assertEquals(resultList.get(0).getResultType(), ValidationResultEnum.ERROR);
		assertEquals(resultList.get(0).getMessage(), TEST_MESSAGE_PREFIX + "0");
		assertEquals(resultList.get(1).getResultType(), ValidationResultEnum.WARN);
		assertEquals(resultList.get(1).getMessage(), anotherMsg);
	}

	@Test
	public void testGetResultListWithEnum() {

		final List<ValidationResultEnum> items = new ArrayList<ValidationResultEnum>();
		items.add(ValidationResultEnum.ERROR);
		items.add(ValidationResultEnum.OK);
		items.add(ValidationResultEnum.OK);
		items.add(ValidationResultEnum.WARN);
		items.add(ValidationResultEnum.OK);
		final ValidationResults<String, String> results = createTestData(items);

		assertNotNull(results);
		final String anotherMsg = "Test ERROR Message";
		results.addResult(KEY_0, new ValidationResult(ValidationResultEnum.ERROR, anotherMsg));
		results.addResult(KEY_0, new ValidationResult(ValidationResultEnum.ERROR, anotherMsg));
		final String warningMessage = TEST_MESSAGE_PREFIX + "WARNING";
		final String okMessage = TEST_MESSAGE_PREFIX + "OK";
		results.addResult(KEY_1, new ValidationResult(ValidationResultEnum.WARN, warningMessage));
		results.addResult(KEY_1, new ValidationResult(ValidationResultEnum.OK, okMessage));
		final List<String> resultList = results.getResultList(KEY_1, ValidationResultEnum.OK);

		assertEquals(resultList.size(), 2);
		assertEquals(resultList.get(0), TEST_MESSAGE_PREFIX + "1");
		assertEquals(resultList.get(1), okMessage);
	}

	@Test
	public void testGetResultListInvalidKey() {

		final List<ValidationResultEnum> items = new ArrayList<ValidationResultEnum>();
		items.add(ValidationResultEnum.ERROR);
		items.add(ValidationResultEnum.OK);
		items.add(ValidationResultEnum.WARN);
		items.add(ValidationResultEnum.ERROR);

		final ValidationResults<String, String> results = createTestData(items);

		assertNotNull(results);
		final List<ValidationResult> resultList = results.getResultList("key does not exist");

		assertTrue(resultList.isEmpty());
	}

	@Test
	public void testGetResultListEnumInvalidKey() {

		final List<ValidationResultEnum> items = new ArrayList<ValidationResultEnum>();
		items.add(ValidationResultEnum.ERROR);
		items.add(ValidationResultEnum.OK);
		items.add(ValidationResultEnum.WARN);
		items.add(ValidationResultEnum.ERROR);

		final ValidationResults<String, String> results = createTestData(items);

		assertNotNull(results);
		final List<String> resultList = results.getResultList("key does not exist", ValidationResultEnum.OK);

		assertTrue(resultList.isEmpty());
	}

	@Test
	public void testGetResultStringWithEnum() {

		final List<ValidationResultEnum> items = new ArrayList<ValidationResultEnum>();
		items.add(ValidationResultEnum.ERROR);
		items.add(ValidationResultEnum.OK);
		items.add(ValidationResultEnum.OK);
		items.add(ValidationResultEnum.WARN);
		items.add(ValidationResultEnum.OK);
		final ValidationResults<String, String> results = createTestData(items);

		assertNotNull(results);
		final String anotherMsg = "Test ERROR Message";
		results.addResult(KEY_0, new ValidationResult(ValidationResultEnum.ERROR, anotherMsg));
		results.addResult(KEY_0, new ValidationResult(ValidationResultEnum.ERROR, anotherMsg));
		final String warningMessage = TEST_MESSAGE_PREFIX + "WARNING";
		final String okMessage = TEST_MESSAGE_PREFIX + "OK";
		results.addResult(KEY_1, new ValidationResult(ValidationResultEnum.WARN, warningMessage));
		results.addResult(KEY_1, new ValidationResult(ValidationResultEnum.OK, okMessage));
		final String message = results.getResultString(KEY_1, ValidationResultEnum.OK);

		assertTrue(StringUtils.isNotBlank(message));
		assertTrue(StringUtils.contains(message, TEST_MESSAGE_PREFIX + "1"));
		assertTrue(StringUtils.contains(message, okMessage));
	}

	@Test
	public void testGetResultStringEnumInvalidKey() {

		final List<ValidationResultEnum> items = new ArrayList<ValidationResultEnum>();
		items.add(ValidationResultEnum.ERROR);
		items.add(ValidationResultEnum.OK);
		items.add(ValidationResultEnum.WARN);
		items.add(ValidationResultEnum.ERROR);

		final ValidationResults<String, String> results = createTestData(items);

		assertNotNull(results);
		final String message = results.getResultString("key does not exist", ValidationResultEnum.OK);

		assertTrue(StringUtils.isBlank(message));
	}

	@Test
	public void testGetConstructedObject() {

		final List<ValidationResultEnum> items = new ArrayList<ValidationResultEnum>();
		items.add(ValidationResultEnum.ERROR);
		items.add(ValidationResultEnum.OK);
		items.add(ValidationResultEnum.WARN);
		items.add(ValidationResultEnum.ERROR);

		final ValidationResults<String, String> results = createTestData(items);

		assertNotNull(results);
		assertNull(results.getConstructedObject());
		final String testObj = "Test Object";
		results.setConstructedObject(testObj);

		assertEquals(results.getConstructedObject(), testObj);
	}

	@Test
	public void testValidationStatus() {

		List<ValidationResultEnum> items = new ArrayList<ValidationResultEnum>();
		items.add(ValidationResultEnum.ERROR);
		items.add(ValidationResultEnum.ERROR);
		items.add(ValidationResultEnum.ERROR);
		items.add(ValidationResultEnum.ERROR);

		ValidationResults<String, String> results = createTestData(items);

		assertNotNull(results);
		Set<ValidationResultEnum> status = results.getValidationStatus();

		assertEquals(status.size(), 1);

		items = new ArrayList<ValidationResultEnum>();
		items.add(ValidationResultEnum.ERROR);
		items.add(ValidationResultEnum.ERROR);
		items.add(ValidationResultEnum.WARN);
		items.add(ValidationResultEnum.WARN);
		items.add(ValidationResultEnum.WARN);
		items.add(ValidationResultEnum.ERROR);

		results = createTestData(items);
		assertNotNull(results);
		status = results.getValidationStatus();

		assertEquals(status.size(), 2);
		assertTrue(status.contains(ValidationResultEnum.ERROR));
		assertTrue(status.contains(ValidationResultEnum.WARN));
		assertFalse(status.contains(ValidationResultEnum.OK));
	}

	@Test
	public void testHasErrorsWithField() {

		final List<ValidationResultEnum> items = new ArrayList<ValidationResultEnum>();
		items.add(ValidationResultEnum.OK);
		items.add(ValidationResultEnum.OK);
		items.add(ValidationResultEnum.WARN);
		items.add(ValidationResultEnum.WARN);

		final ValidationResults<String, String> results = createTestData(items);
		assertNotNull(results);
		final String anotherMsg = "Test Message";
		results.addResult(KEY_0, new ValidationResult(ValidationResultEnum.OK, anotherMsg));
		results.addResult(KEY_0, new ValidationResult(ValidationResultEnum.WARN, anotherMsg));

		assertFalse(results.hasErrors(KEY_0));
		final String errMsg = "Test ERROR Message";
		results.addResult(KEY_0, new ValidationResult(ValidationResultEnum.ERROR, errMsg));
		results.addResult(KEY_0, new ValidationResult(ValidationResultEnum.ERROR, errMsg));

		assertTrue(results.hasErrors(KEY_0));
	}

	@Test
	public void testHasWarningsWithField() {
		final List<ValidationResultEnum> items = new ArrayList<ValidationResultEnum>();
		items.add(ValidationResultEnum.OK);
		items.add(ValidationResultEnum.OK);
		items.add(ValidationResultEnum.ERROR);
		items.add(ValidationResultEnum.ERROR);

		final ValidationResults<String, String> results = createTestData(items);
		assertNotNull(results);
		final String anotherMsg = "Test Message";
		results.addResult(KEY_0, new ValidationResult(ValidationResultEnum.OK, anotherMsg));
		results.addResult(KEY_0, new ValidationResult(ValidationResultEnum.ERROR, anotherMsg));

		assertFalse(results.hasWarnings(KEY_0));
		final String errMsg = "Test WARN Message";
		results.addResult(KEY_0, new ValidationResult(ValidationResultEnum.WARN, errMsg));
		results.addResult(KEY_0, new ValidationResult(ValidationResultEnum.WARN, errMsg));

		assertTrue(results.hasWarnings(KEY_0));
	}
}
