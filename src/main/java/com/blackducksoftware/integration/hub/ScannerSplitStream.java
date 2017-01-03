/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
 */
package com.blackducksoftware.integration.hub;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.log.IntLogger;

public class ScannerSplitStream extends OutputStream {
    // https://www.cs.cmu.edu/~pattis/15-1XX/common/handouts/ascii.html
    private static final int EOF = -1; // End of file

    private static final int ETX = 3; // End of text, should have no more data

    private static final int EOT = 4; // End of transmission, no more data

    private static final int LF = 10; // Line feed, new line

    private static final int CR = 13; // Carriage return

    private static final String EXCEPTION = "Exception:";

    private static final String FINISHED = "Finished in";

    private static final String ERROR = "ERROR:";

    private static final String WARN = "WARN:";

    private static final String INFO = "INFO:";

    private static final String DEBUG = "DEBUG:";

    private static final String TRACE = "TRACE:";

    private final OutputStream outputFileStream;

    private final IntLogger logger;

    private String output = "";

    private String lineBuffer = "";

    private String currentLine = "";

    private int previousCodePoint = -1;

    public ScannerSplitStream(final IntLogger logger, final OutputStream outputFileStream) {
        this.outputFileStream = outputFileStream;
        this.logger = logger;
    }

    public String getOutput() {
        return output;
    }

    public Boolean hasOutput() {
        return StringUtils.isNotBlank(output);
    }

    @Override
    public void write(final int codePoint) throws IOException {
        outputFileStream.write(codePoint);

        if (EOF == codePoint) {
            throw new EOFException();
        }

        boolean atLineEnd = false;
        if (ETX == codePoint || EOT == codePoint) {
            atLineEnd = true;
        } else if (LF == codePoint && CR != previousCodePoint) {
            atLineEnd = true;
        } else if (LF == codePoint && CR == previousCodePoint) {
            atLineEnd = true;
            // also need to remove the previously consumed CR
            currentLine = currentLine.substring(0, currentLine.length() - 1);
        } else if (LF != codePoint && CR == previousCodePoint) {
            processLine(currentLine);
            currentLine = "";
        }
        previousCodePoint = codePoint;

        if (atLineEnd) {
            processLine(currentLine);
            currentLine = "";
        } else {
            final String stringAscii = new String(Character.toChars(codePoint));
            currentLine += stringAscii;
        }
    }

    private Boolean isLoggableLine(final String line) {
        if (StringUtils.containsIgnoreCase(line, ERROR)) {
            return true;
        }
        if (StringUtils.containsIgnoreCase(line, WARN)) {
            return true;
        }
        if (StringUtils.containsIgnoreCase(line, INFO)) {
            return true;
        }
        if (StringUtils.containsIgnoreCase(line, DEBUG)) {
            return true;
        }
        if (StringUtils.containsIgnoreCase(line, TRACE)) {
            return true;
        }
        if (StringUtils.containsIgnoreCase(line, EXCEPTION)) {
            return true;
        }
        if (StringUtils.containsIgnoreCase(line, FINISHED)) {
            return true;
        }
        return false;
    }

    private void processLine(final String line) throws UnsupportedEncodingException {
        if (lineBuffer.length() == 0) {
            // First log line found, put it in the buffer
            lineBuffer = line;
        } else if (isLoggableLine(line)) {
            // next real log message came in, print the log in the buffer
            // print stored lines
            writeToConsole(lineBuffer);

            // replace with the current line
            lineBuffer = line;
        } else {
            // We assume that each new log starts with the log level, if this
            // line does not contain a log level it
            // must only be a piece of a log
            // needs to be added into the buffer
            final StringBuilder builder = new StringBuilder();
            builder.append(lineBuffer);

            builder.append(System.getProperty("line.separator"));
            builder.append(line);
            lineBuffer = builder.toString();
        }
    }

    @Override
    public void write(final byte[] byteArray) throws IOException {
        outputFileStream.write(byteArray);

        final String currentLine = new String(byteArray, "UTF-8");
        if (currentLine.contains(System.getProperty("line.separator"))) {
            final String[] splitLines = currentLine.split(System.getProperty("line.separator"));

            for (final String line : splitLines) {
                processLine(line);
            }
        } else {
            processLine(currentLine);
        }
    }

    @Override
    public void write(final byte[] byteArray, final int offset, final int length) throws IOException {
        outputFileStream.write(byteArray, offset, length);

        final String currentLine = new String(byteArray, offset, length, "UTF-8");
        if (currentLine.contains(System.getProperty("line.separator"))) {
            final String[] splitLines = currentLine.split(System.getProperty("line.separator"));

            for (final String line : splitLines) {
                processLine(line);
            }
        } else {
            processLine(currentLine);
        }
    }

    @Override
    public void flush() throws IOException {
        outputFileStream.flush();

        // Print whatever is left in the buffer
        writeToConsole(lineBuffer);
        lineBuffer = "";
        // Print whatever is left in the buffer
        if (StringUtils.isNotBlank(currentLine)) {
            writeToConsole(currentLine);
            currentLine = "";
        }
    }

    @Override
    public void close() throws IOException {
        outputFileStream.close();

        // Do not close the listener, will not be able to log to the UI anymore
        // if you do
    }

    private void writeToConsole(final String line) {
        if (StringUtils.containsIgnoreCase(line, DEBUG) || StringUtils.containsIgnoreCase(line, TRACE)) {
            // We dont want to print Debug or Trace logs to the logger
            return;
        }
        final StringBuilder outputBuilder = new StringBuilder();
        outputBuilder.append(output);
        if (StringUtils.containsIgnoreCase(line, EXCEPTION)) {
            // looking for 'Exception in thread' type messages
            outputBuilder.append(line);
            outputBuilder.append(System.getProperty("line.separator"));
            logger.error(line);
        } else if (StringUtils.containsIgnoreCase(line, FINISHED)) {
            outputBuilder.append(line);
            outputBuilder.append(System.getProperty("line.separator"));
            logger.info(line);
        } else if (StringUtils.containsIgnoreCase(line, ERROR)) {
            outputBuilder.append(line);
            outputBuilder.append(System.getProperty("line.separator"));
            logger.error(line);
        } else if (StringUtils.containsIgnoreCase(line, WARN)) {
            outputBuilder.append(line);
            outputBuilder.append(System.getProperty("line.separator"));
            logger.warn(line);
        } else if (StringUtils.containsIgnoreCase(line, INFO)) {
            outputBuilder.append(line);
            outputBuilder.append(System.getProperty("line.separator"));
            logger.info(line);
        }

        output = outputBuilder.toString();
    }

}
