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
package com.blackducksoftware.integration.hub.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.logging.LogLevel;

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
        return StringUtils.join(outputList, System.getProperty("line.separator"));
    }

    public String getErrorOutputString() {
        StringBuilder sb = new StringBuilder();
        if (errorList != null && !errorList.isEmpty()) {
            for (Throwable e : errorList) {
                if (sb.length() > 0) {
                    sb.append(System.getProperty("line.separator"));
                }
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                sb.append(sw.toString());
            }
        }
        return sb.toString();
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
