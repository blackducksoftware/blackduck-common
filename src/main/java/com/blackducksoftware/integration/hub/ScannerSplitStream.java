package com.blackducksoftware.integration.hub;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import com.blackducksoftware.integration.suite.sdk.logging.IntLogger;

public class ScannerSplitStream extends OutputStream {

    // https://www.cs.cmu.edu/~pattis/15-1XX/common/handouts/ascii.html

    private static final int EOF = -1; // End of file

    private static final int ETX = 3; // End of text, should have not more data

    private static final int EOT = 4; // End of transmission, no more data

    private static final int LF = 10; // Line feed, new line

    private static final int CR = 13; // Carriage return

    private static final String EXCEPTION = "Exception ";

    private static final String FINISHED = "Finished in";

    private static final String ERROR = "ERROR:";

    private static final String WARN = "WARN:";

    private static final String INFO = "INFO:";

    private static final String DEBUG = "DEBUG:";

    private static final String TRACE = "TRACE:";

    private final StringBuilder outputBuilder = new StringBuilder();

    private final OutputStream outputFileStream;

    private final IntLogger logger;

    private StringBuilder lineBuffer = new StringBuilder();

    private StringBuilder currentLineBuffer = new StringBuilder();

    public ScannerSplitStream(IntLogger logger, OutputStream outputFileStream) {
        this.outputFileStream = outputFileStream;
        this.logger = logger;
    }

    public String getOutput() {
        return outputBuilder.toString();
    }

    public Boolean hasOutput() {
        return outputBuilder != null && outputBuilder.length() > 0;
    }

    @Override
    public void write(int b) throws IOException {
        outputFileStream.write(b);

        String stringAcii = new String(Character.toChars(b));

        String currentLine = currentLineBuffer.toString();
        switch (b) {
        case ETX:
            processLine(currentLine);
            currentLineBuffer = new StringBuilder();
            return;
        case EOT:
            processLine(currentLine);
            currentLineBuffer = new StringBuilder();
            return;
        case LF:
            processLine(currentLine);
            currentLineBuffer = new StringBuilder();
            return;
        case CR:
            processLine(currentLine);
            currentLineBuffer = new StringBuilder();
            return;
        case EOF:
            throw new EOFException();
        default:
            currentLineBuffer.append(stringAcii);
            return;
        }
    }

    private Boolean isLoggableLine(String line) {
        if (line.startsWith(ERROR) || line.contains(ERROR)) {
            return true;
        }
        if (line.startsWith(WARN) || line.contains(WARN)) {
            return true;
        }
        if (line.startsWith(INFO) || line.contains(INFO)) {
            return true;
        }
        if (line.startsWith(DEBUG) || line.contains(DEBUG)) {
            return true;
        }
        if (line.startsWith(TRACE) || line.contains(TRACE)) {
            return true;
        }
        if (line.startsWith(EXCEPTION) || line.contains(EXCEPTION)) {
            return true;
        }
        if (line.startsWith(FINISHED) || line.contains(FINISHED)) {
            return true;
        }
        return false;
    }

    private void processLine(String line) throws UnsupportedEncodingException {
        if (lineBuffer.length() == 0) {
            // First log line found, put it in the buffer

            lineBuffer.append(line);

        } else if (isLoggableLine(line)) {
            // next real log message came in, print the log in the buffer
            // print stored lines
            writeToConsole(lineBuffer.toString());

            // clear and add current line to the buffer
            lineBuffer = new StringBuilder();
            lineBuffer.append(line);

        } else {
            // We assume that each new log starts with the log level, if this line does not contain a log level it
            // must only be a piece of a log

            // needs to be added into the buffer
            lineBuffer.append(System.getProperty("line.separator") + line);

        }
    }

    @Override
    public void write(byte[] byteArray) throws IOException {
        outputFileStream.write(byteArray);

        String currentLine = new String(byteArray, "UTF-8");

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
        writeToConsole(lineBuffer.toString());
    }

    @Override
    public void close() throws IOException {
        outputFileStream.close();

        // Do not close the listener, will not be able to log to the UI anymore if you do
    }

    private void writeToConsole(String line) {

        if (line.contains(EXCEPTION)) {
            // looking for 'Exception in thread' type messages
            outputBuilder.append(line + System.getProperty("line.separator"));
            logger.error(line);
        } else if (line.contains(FINISHED)) {
            outputBuilder.append(line + System.getProperty("line.separator"));
            logger.info(line);
        } else if (line.contains(ERROR)) {
            outputBuilder.append(line + System.getProperty("line.separator"));
            logger.error(line);
        } else if (line.contains(WARN)) {
            outputBuilder.append(line + System.getProperty("line.separator"));
            logger.warn(line);
        } else if (line.contains(INFO)) {
            outputBuilder.append(line + System.getProperty("line.separator"));
            logger.info(line);
        }
    }
}
