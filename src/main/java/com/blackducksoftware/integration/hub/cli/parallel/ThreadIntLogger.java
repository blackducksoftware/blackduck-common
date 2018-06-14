/**
 * hub-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.cli.parallel;

import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.LogLevel;

public class ThreadIntLogger extends IntLogger {

    private final IntLogger intLogger;

    public ThreadIntLogger(final IntLogger intLogger) {
        this.intLogger = intLogger;
    }

    @Override
    public void alwaysLog(final String txt) {
        intLogger.alwaysLog(textWithThreadName(txt));
    }

    @Override
    public void info(final String txt) {
        intLogger.info(textWithThreadName(txt));
    }

    @Override
    public void error(final Throwable t) {
        intLogger.error(t);
    }

    @Override
    public void error(final String txt, final Throwable t) {
        intLogger.error(textWithThreadName(txt), t);
    }

    @Override
    public void error(final String txt) {
        intLogger.error(textWithThreadName(txt));
    }

    @Override
    public void warn(final String txt) {
        intLogger.warn(textWithThreadName(txt));
    }

    @Override
    public void trace(final String txt) {
        intLogger.trace(textWithThreadName(txt));
    }

    @Override
    public void trace(final String txt, final Throwable t) {
        intLogger.trace(textWithThreadName(txt), t);
    }

    @Override
    public void debug(final String txt) {
        intLogger.debug(textWithThreadName(txt));
    }

    @Override
    public void debug(final String txt, final Throwable t) {
        intLogger.debug(textWithThreadName(txt), t);
    }

    @Override
    public void setLogLevel(final LogLevel logLevel) {
        intLogger.setLogLevel(logLevel);
    }

    @Override
    public LogLevel getLogLevel() {
        return intLogger.getLogLevel();
    }

    private String textWithThreadName(final String text) {
        return String.format("[%s] %s", Thread.currentThread().getName(), text);
    }

}
