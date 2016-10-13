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
package com.blackducksoftware.integration.hub.dataservices.parallel;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.Test;

import com.blackducksoftware.integration.hub.dataservices.ItemTransform;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.test.TestLogger;

public class ParallelResourceProcessorTest {

	private List<Number> createNumberList() {
		final List<Number> numberList = new ArrayList<>();
		for (int index = 1; index <= 9; index++) {
			numberList.add(new Integer(index));
		}
		return numberList;
	}

	private List<String> createStringList(final List<Number> numberList) {
		final int count = numberList.size();
		final List<String> stringList = new ArrayList<>(count);
		List<String> subStringList = new ArrayList<>(3);
		for (int index = 0; index < count; index++) {
			final Integer integer = new Integer(index + 1);
			subStringList.add(integer.toString());
			if (integer % 3 == 0) {
				final String commaSeparated = StringUtils.join(subStringList, ",");
				stringList.add(commaSeparated);
				subStringList = new ArrayList<>(3);
			}
		}
		return stringList;
	}

	private ItemTransform<List<Number>, String> createTransform() {
		return new ItemTransform<List<Number>, String>() {

			@Override
			public List<Number> transform(final String item) throws HubItemTransformException {
				final String[] numberArray = StringUtils.split(item, ",");

				final List<Number> numberList = new ArrayList<>(numberArray.length);
				for (final String numString : numberArray) {
					final Integer value = NumberUtils.createInteger(numString);
					numberList.add(value);
				}
				return numberList;
			}
		};
	}

	@Test
	public void testConstructor() {
		final TestLogger logger = new TestLogger();
		final ThreadFactory threadFactory = Executors.defaultThreadFactory();
		final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
				threadFactory);
		final ExecutorCompletionService<List<Number>> completionService = new ExecutorCompletionService<>(
				executorService);
		final ParallelResourceProcessor<Number, String> parallelProcessor = new ParallelResourceProcessor<>(logger,
				executorService, completionService);
		parallelProcessor.addTransform(String.class, createTransform());
		final List<Number> numberList = createNumberList();
		final List<String> stringList = createStringList(numberList);
		final List<Number> transformedNumberList = parallelProcessor.process(stringList);
		// NOTE: order is not guaranteed by the processor
		assertTrue(transformedNumberList.containsAll(numberList));
	}

	@Test
	public void testParallelProcessing() {
		final TestLogger logger = new TestLogger();
		final ParallelResourceProcessor<Number, String> parallelProcessor = new ParallelResourceProcessor<>(logger);
		parallelProcessor.addTransform(String.class, createTransform());
		final List<Number> numberList = createNumberList();
		final List<String> stringList = createStringList(numberList);
		final List<Number> transformedNumberList = parallelProcessor.process(stringList);
		// NOTE: order is not guaranteed by the processor
		assertTrue(transformedNumberList.containsAll(numberList));
	}
}
