package com.blackducksoftware.integration.hub.dataservices.parallel;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

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
	public void testParallelProcessing() {
		final TestLogger logger = new TestLogger();
		final ParallelResourceProcessor<Number, String> parallelProcessor = new ParallelResourceProcessor<>(logger);
		parallelProcessor.addTransform(String.class, createTransform());
		final List<Number> numberList = createNumberList();
		final List<String> stringList = createStringList(numberList);
		final List<Number> transformedNumberList = parallelProcessor.process(stringList);
		final int count = numberList.size();
		for (int index = 0; index < count; index++) {
			final Number original = numberList.get(index);
			final Number created = transformedNumberList.get(index);
			assertEquals(original, created);
		}
	}
}
