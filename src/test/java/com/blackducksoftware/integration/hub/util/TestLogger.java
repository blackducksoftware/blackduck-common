package com.blackducksoftware.integration.hub.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import com.blackducksoftware.integration.suite.sdk.logging.IntLogger;
import com.blackducksoftware.integration.suite.sdk.logging.LogLevel;

public class TestLogger implements IntLogger {

    private ArrayList<String> outputList = new ArrayList<String>();

    private ArrayList<Throwable> errorList = new ArrayList<Throwable>();

    public ArrayList<String> getOutputList() {
        return outputList;
    }

    public ArrayList<Throwable> getErrorList() {
        return errorList;
    }

    public void resetOutputList() {
        outputList = new ArrayList<String>();
    }

    public void resetErrorList() {
        errorList = new ArrayList<Throwable>();
    }

    public void resetAllOutput() {
        resetOutputList();
        resetErrorList();
    }

    public String getOutputString() {
        if (outputList != null && !outputList.isEmpty()) {

            StringBuilder sb = new StringBuilder();
            for (String string : outputList) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(string);
            }
            return sb.toString();
        }
        return "";
    }

    public String getErrorOutputString() {
        if (errorList != null && !errorList.isEmpty()) {

            StringBuilder sb = new StringBuilder();
            for (Throwable e : errorList) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                sb.append(sw.toString());
            }
            return sb.toString();
        }
        return "";
    }

    @Override
    public void debug(String txt) {
        outputList.add(txt);

    }

    @Override
    public void debug(String txt, Throwable e) {
        outputList.add(txt);
        errorList.add(e);
    }

    @Override
    public void error(Throwable e) {
        errorList.add(e);
    }

    @Override
    public void error(String txt) {
        outputList.add(txt);

    }

    @Override
    public void error(String txt, Throwable e) {
        outputList.add(txt);
        errorList.add(e);
    }

    @Override
    public void info(String txt) {
        outputList.add(txt);

    }

    @Override
    public void trace(String txt) {
        outputList.add(txt);

    }

    @Override
    public void trace(String txt, Throwable e) {
        outputList.add(txt);
        errorList.add(e);
    }

    @Override
    public void warn(String txt) {
        outputList.add(txt);

    }

    @Override
    public void setLogLevel(LogLevel level) {

    }

    @Override
    public LogLevel getLogLevel() {
        return null;

    }

}
