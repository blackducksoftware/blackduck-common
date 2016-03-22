package com.blackducksoftware.integration.hub;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.suite.sdk.logging.IntLogger;

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

    public ScannerSplitStream(IntLogger logger, OutputStream outputFileStream) {
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
    public void write(int b) throws IOException {
        outputFileStream.write(b);

        String stringAcii = new String(Character.toChars(b));
        StringBuilder builder = new StringBuilder();
        builder.append(currentLine);
        switch (b) {
        case ETX:
            processLine(currentLine);
            currentLine = "";
            return;
        case EOT:
            processLine(currentLine);
            currentLine = "";
            return;
        case LF:
            processLine(currentLine);
            currentLine = "";
            return;
        case CR:
            processLine(currentLine);
            currentLine = "";
            return;
        case EOF:
            throw new EOFException();
        default:
            builder.append(stringAcii);
            currentLine = builder.toString();
            return;
        }
    }

    private Boolean isLoggableLine(String line) {
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

    private void processLine(String line) throws UnsupportedEncodingException {
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
            // We assume that each new log starts with the log level, if this line does not contain a log level it
            // must only be a piece of a log
            // needs to be added into the buffer
            StringBuilder builder = new StringBuilder();
            builder.append(lineBuffer);

            builder.append(System.getProperty("line.separator"));
            builder.append(line);
            lineBuffer = builder.toString();
        }
    }

    @Override
    public void write(byte[] byteArray) throws IOException {
        outputFileStream.write(byteArray);

        String currentLine = new String(byteArray, "UTF-8");
        logger.info(currentLine);
        if (currentLine.contains(System.getProperty("line.separator"))) {
            String[] splitLines = currentLine.split(System.getProperty("line.separator"));

            for (String line : splitLines) {
                processLine(line);
            }
        } else {
            processLine(currentLine);
        }
    }

    @Override
    public void write(byte[] byteArray, int offset, int length) throws IOException {
        outputFileStream.write(byteArray, offset, length);

        String currentLine = new String(byteArray, offset, length, "UTF-8");
        logger.info(currentLine);
        if (currentLine.contains(System.getProperty("line.separator"))) {
            String[] splitLines = currentLine.split(System.getProperty("line.separator"));

            for (String line : splitLines) {
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

        // Do not close the listener, will not be able to log to the UI anymore if you do
    }

    private void writeToConsole(String line) {
        StringBuilder builder = new StringBuilder();
        builder.append(output);

        if (StringUtils.containsIgnoreCase(line, EXCEPTION)) {
            // looking for 'Exception in thread' type messages
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
            logger.error(line);
        } else if (StringUtils.containsIgnoreCase(line, FINISHED)) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
            logger.info(line);
        } else if (StringUtils.containsIgnoreCase(line, ERROR)) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
            logger.error(line);
        } else if (StringUtils.containsIgnoreCase(line, WARN)) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
            logger.warn(line);
        } else if (StringUtils.containsIgnoreCase(line, INFO)) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
            logger.info(line);
        }

        output = builder.toString();
    }
}
