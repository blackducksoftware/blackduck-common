package com.blackducksoftware.integration.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.blackducksoftware.integration.hub.util.TestLogger;
import com.blackducksoftware.integration.hub.util.TestOutputStream;

public class ScannerSplitStreamTest {
    @Test
    public void testWriteBytes() throws Exception {
        TestLogger logger = new TestLogger();
        TestOutputStream stream = new TestOutputStream();

        ScannerSplitStream splitStream = new ScannerSplitStream(logger, stream);

        final String notLoggable = "Test 1 2 3, test 1 2 3, can you hear me? \n What about now?";

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

        TestLogger logger = new TestLogger();
        TestOutputStream stream = new TestOutputStream();

        ScannerSplitStream splitStream = new ScannerSplitStream(logger, stream);

        final String loggable = "Exception: Test 1 2 3, test 1 2 3, can you hear me? "
                + " \n WARN: about to explode \n INFO: breaking news \n Finished in What about now? \n ERROR: something happened";

        final String notLoggable = "DEBUG: found something \n TRACE: reading pg 876";

        for (int i = 0; i < loggable.length(); i++) {
            splitStream.write(loggable.codePointAt(i));
        }
        splitStream.flush();

        // Should write all the bytes to the TestOutputStream
        assertEquals(loggable, stream.buffer.toString());
        assertEquals(loggable, logger.getOutputString());
        assertTrue(splitStream.hasOutput());
        assertEquals(loggable, splitStream.getOutput().trim());

        stream.resetBuffer();
        logger.resetAllOutput();

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
    public void testWriteByteArray() throws Exception {
        TestLogger logger = new TestLogger();
        TestOutputStream stream = new TestOutputStream();

        ScannerSplitStream splitStream = new ScannerSplitStream(logger, stream);

        final String notLoggable = "Test 1 2 3, test 1 2 3, can you hear me? \n What about now?";

        splitStream.write(notLoggable.getBytes());
        splitStream.flush();
        // Should write all the bytes to the TestOutputStream
        assertEquals(notLoggable, stream.buffer.toString());

        assertTrue(StringUtils.isBlank(logger.getOutputString()));
        splitStream.close();
    }

    @Test
    public void testWriteByteArrayLoggable() throws Exception {

        TestLogger logger = new TestLogger();
        TestOutputStream stream = new TestOutputStream();

        ScannerSplitStream splitStream = new ScannerSplitStream(logger, stream);

        final String loggable = "Exception: Test 1 2 3, test 1 2 3, can you hear me? "
                + " \n WARN: about to explode \n INFO: breaking news \n Finished in What about now? \n ERROR: something happened";

        final String notLoggable = "DEBUG: found something \n TRACE: reading pg 876";

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

        TestLogger logger = new TestLogger();
        TestOutputStream stream = new TestOutputStream();

        ScannerSplitStream splitStream = new ScannerSplitStream(logger, stream);

        final String notLoggable = "Test 1 2 3, test 1 2 3, can you hear me? \n What about now?";

        splitStream.write(notLoggable.getBytes(), 0, notLoggable.length());
        splitStream.flush();
        // Should write all the bytes to the TestOutputStream
        assertEquals(notLoggable, stream.buffer.toString());

        assertTrue(StringUtils.isBlank(logger.getOutputString()));
        splitStream.close();
    }

    @Test
    public void testWriteByteArrayWithOffsetLoggable() throws Exception {

        TestLogger logger = new TestLogger();
        TestOutputStream stream = new TestOutputStream();

        ScannerSplitStream splitStream = new ScannerSplitStream(logger, stream);

        final String loggable = "Exception: Test 1 2 3, test 1 2 3, can you hear me? "
                + " \n WARN: about to explode \n INFO: breaking news \n Finished in What about now? \n ERROR: something happened";

        final String notLoggable = "DEBUG: found something \n TRACE: reading pg 876";

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
}
