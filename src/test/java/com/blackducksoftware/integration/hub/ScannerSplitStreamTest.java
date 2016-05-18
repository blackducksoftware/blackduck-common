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
package com.blackducksoftware.integration.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.blackducksoftware.integration.hub.util.TestLogger;
import com.blackducksoftware.integration.hub.util.TestOutputStream;

public class ScannerSplitStreamTest {
	private static final String NEW_LINE = System.getProperty("line.separator");

	@Test
	public void testWritingTextWithWindowsLineSeparator() throws IOException {
		final TestLogger logger = new TestLogger();
		final TestOutputStream stream = new TestOutputStream();
		final ScannerSplitStream splitStream = new ScannerSplitStream(logger, stream);

		final String originalLineSeparator = System.getProperty("line.separator");
		try {
			System.setProperty("line.separator", "\r\n");
			final String text = "Exception: one fish\r\nException: two fish";
			final List<Integer> codePoints = getCodePoints(text);

			writeCodePointsToScannerSplitStream(codePoints, splitStream);

			final String output = splitStream.getOutput().trim();
			assertEquals(text, output);
		} finally {
			System.setProperty("line.separator", originalLineSeparator);
			splitStream.close();
		}
	}

	@Test
	public void testWritingTextWithNonWindowsLineSeparator() throws IOException {
		final TestLogger logger = new TestLogger();
		final TestOutputStream stream = new TestOutputStream();
		final ScannerSplitStream splitStream = new ScannerSplitStream(logger, stream);

		final String originalLineSeparator = System.getProperty("line.separator");
		try {
			System.setProperty("line.separator", "\n");
			final String text = "Exception: red fish\nException: blue fish";
			final List<Integer> codePoints = getCodePoints(text);

			writeCodePointsToScannerSplitStream(codePoints, splitStream);

			final String output = splitStream.getOutput().trim();
			assertEquals(text, output);
		} finally {
			System.setProperty("line.separator", originalLineSeparator);
			splitStream.close();
		}
	}

	@Test
	public void testWriteBytes() throws Exception {
		final TestLogger logger = new TestLogger();
		final TestOutputStream stream = new TestOutputStream();
		final ScannerSplitStream splitStream = new ScannerSplitStream(logger, stream);

		final String notLoggable = "Test 1 2 3, test 1 2 3, can you hear me? " + NEW_LINE + " What about now?";

		for (int i = 0; i < notLoggable.length(); i++) {
			splitStream.write(notLoggable.codePointAt(i));
		}
		splitStream.flush();
		// Should write all the bytes to the TestOutputStream
		assertEquals(notLoggable, stream.buffer.toString());

		assertTrue(StringUtils.isBlank(logger.getOutputString()));
		splitStream.close();
	}

	@Test
	public void testWriteBytesLoggable() throws Exception {
		final TestLogger logger = new TestLogger();
		final TestOutputStream stream = new TestOutputStream();
		final ScannerSplitStream splitStream = new ScannerSplitStream(logger, stream);

		final String loggable = "Exception: Test 1 2 3, test 1 2 3, can you hear me?  " + NEW_LINE
				+ " WARN: about to explode " + NEW_LINE + " INFO: breaking news " + NEW_LINE
				+ " Finished in What about now? " + NEW_LINE + " ERROR: something happened";

		final String notLoggable = "DEBUG: found something " + NEW_LINE + " TRACE: reading pg 876";

		List<Integer> codePoints = getCodePoints(loggable);
		writeCodePointsToScannerSplitStream(codePoints, splitStream);

		// Should write all the bytes to the TestOutputStream
		assertEquals(loggable, stream.buffer.toString());
		assertEquals(loggable, logger.getOutputString());
		assertTrue(splitStream.hasOutput());
		assertEquals(loggable, splitStream.getOutput().trim());

		stream.resetBuffer();
		logger.resetAllOutput();

		codePoints = getCodePoints(notLoggable);
		writeCodePointsToScannerSplitStream(codePoints, splitStream);

		// Should write all the bytes to the TestOutputStream
		assertEquals(notLoggable, stream.buffer.toString());

		assertTrue(StringUtils.isBlank(logger.getOutputString()));
		splitStream.close();
	}

	@Test
	public void testWriteByteArray() throws Exception {
		final TestLogger logger = new TestLogger();
		final TestOutputStream stream = new TestOutputStream();
		final ScannerSplitStream splitStream = new ScannerSplitStream(logger, stream);

		final String notLoggable = "Test 1 2 3, test 1 2 3, can you hear me? " + NEW_LINE + " What about now?";

		splitStream.write(notLoggable.getBytes());
		splitStream.flush();
		// Should write all the bytes to the TestOutputStream
		assertEquals(notLoggable, stream.buffer.toString());

		assertTrue(StringUtils.isBlank(logger.getOutputString()));
		splitStream.close();
	}

	@Test
	public void testWriteByteArrayLoggable() throws Exception {
		final TestLogger logger = new TestLogger();
		final TestOutputStream stream = new TestOutputStream();
		final ScannerSplitStream splitStream = new ScannerSplitStream(logger, stream);

		final String loggable = "Exception: Test 1 2 3, test 1 2 3, can you hear me?  " + NEW_LINE
				+ " WARN: about to explode " + NEW_LINE + " INFO: breaking news " + NEW_LINE
				+ " Finished in What about now? " + NEW_LINE + " ERROR: something happened";

		final String notLoggable = "DEBUG: found something " + NEW_LINE + " TRACE: reading pg 876";

		splitStream.write(loggable.getBytes());
		splitStream.flush();

		// Should write all the bytes to the TestOutputStream
		assertEquals(loggable, stream.buffer.toString());
		assertEquals(loggable, logger.getOutputString());
		assertTrue(splitStream.hasOutput());
		assertEquals(loggable, splitStream.getOutput().trim());

		stream.resetBuffer();
		logger.resetAllOutput();

		splitStream.write(notLoggable.getBytes());
		splitStream.flush();
		// Should write all the bytes to the TestOutputStream
		assertEquals(notLoggable, stream.buffer.toString());

		assertTrue(StringUtils.isBlank(logger.getOutputString()));
		splitStream.close();
	}

	@Test
	public void testWriteByteArrayWithOffset() throws Exception {
		final TestLogger logger = new TestLogger();
		final TestOutputStream stream = new TestOutputStream();
		final ScannerSplitStream splitStream = new ScannerSplitStream(logger, stream);

		final String notLoggable = "Test 1 2 3, test 1 2 3, can you hear me? " + NEW_LINE + " What about now?";

		splitStream.write(notLoggable.getBytes(), 0, notLoggable.length());
		splitStream.flush();
		// Should write all the bytes to the TestOutputStream
		assertEquals(notLoggable, stream.buffer.toString());

		assertTrue(StringUtils.isBlank(logger.getOutputString()));
		splitStream.close();
	}

	@Test
	public void testWriteByteArrayWithOffsetLoggable() throws Exception {
		final TestLogger logger = new TestLogger();
		final TestOutputStream stream = new TestOutputStream();
		final ScannerSplitStream splitStream = new ScannerSplitStream(logger, stream);

		final String loggable = "Exception: Test 1 2 3, test 1 2 3, can you hear me?  " + NEW_LINE
				+ " WARN: about to explode " + NEW_LINE + " INFO: breaking news " + NEW_LINE
				+ " Finished in What about now? " + NEW_LINE + " ERROR: something happened";

		final String notLoggable = "DEBUG: found something " + NEW_LINE + " TRACE: reading pg 876";

		splitStream.write(loggable.getBytes(), 0, loggable.length());
		splitStream.flush();

		// Should write all the bytes to the TestOutputStream
		assertEquals(loggable, stream.buffer.toString());
		assertEquals(loggable, logger.getOutputString());
		assertTrue(splitStream.hasOutput());
		assertEquals(loggable, splitStream.getOutput().trim());

		stream.resetBuffer();
		logger.resetAllOutput();

		splitStream.write(notLoggable.getBytes(), 0, notLoggable.length());
		splitStream.flush();
		// Should write all the bytes to the TestOutputStream
		assertEquals(notLoggable, stream.buffer.toString());

		assertTrue(StringUtils.isBlank(logger.getOutputString()));
		splitStream.close();
	}

	private List<Integer> getCodePoints(final String s) {
		final List<Integer> codePoints = new ArrayList<Integer>();
		for (int i = 0; i < s.length(); i++) {
			codePoints.add(s.codePointAt(i));
		}

		return codePoints;
	}

	private void writeCodePointsToScannerSplitStream(final List<Integer> codePoints, final ScannerSplitStream stream)
			throws IOException {
		for (final Integer codePoint : codePoints) {
			stream.write(codePoint);
		}
		stream.flush();
	}

}
