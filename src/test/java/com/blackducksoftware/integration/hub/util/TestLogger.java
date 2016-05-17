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
