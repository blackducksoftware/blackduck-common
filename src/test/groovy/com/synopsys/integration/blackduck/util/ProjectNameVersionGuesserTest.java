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
package com.synopsys.integration.blackduck.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.service.model.ProjectNameVersionGuess;
import com.synopsys.integration.blackduck.service.model.ProjectNameVersionGuesser;

public class ProjectNameVersionGuesserTest {
    @Test
    public void testProjectNameVersionGuess() {
        final ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        final ProjectNameVersionGuess guess = guesser.guessNameAndVersion("nothing");
        final String defaultVersion = guesser.getDefaultVersionGuess();
        assertEquals("nothing", guess.getProjectName());
        assertEquals(defaultVersion, guess.getVersionName());
    }

    @Test
    public void testGuessingSingleHyphen() {
        final ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        final ProjectNameVersionGuess guess = guesser.guessNameAndVersion("-");
        final String defaultVersion = guesser.getDefaultVersionGuess();
        assertEquals("-", guess.getProjectName());
        assertEquals(defaultVersion, guess.getVersionName());
    }

    @Test
    public void testGuessingSinglePeriod() {
        final ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        final ProjectNameVersionGuess guess = guesser.guessNameAndVersion(".");
        final String defaultVersion = guesser.getDefaultVersionGuess();
        assertEquals(".", guess.getProjectName());
        assertEquals(defaultVersion, guess.getVersionName());
    }

    @Test
    public void testGuessingAllHyphens() {
        final ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        final ProjectNameVersionGuess guess = guesser.guessNameAndVersion("-----");
        final String defaultVersion = guesser.getDefaultVersionGuess();
        assertEquals("-----", guess.getProjectName());
        assertEquals(defaultVersion, guess.getVersionName());
    }

    @Test
    public void testGuessingAllPeriods() {
        final ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        final ProjectNameVersionGuess guess = guesser.guessNameAndVersion(".....");
        final String defaultVersion = guesser.getDefaultVersionGuess();
        assertEquals(".....", guess.getProjectName());
        assertEquals(defaultVersion, guess.getVersionName());
    }

    @Test
    public void testLongVersionName() {
        final ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        final ProjectNameVersionGuess guess = guesser.guessNameAndVersion("valohai_yaml-0.4-py2.py3-none-any");
        assertEquals("valohai_yaml", guess.getProjectName());
        assertEquals("0.4-py2.py3-none-any", guess.getVersionName());
    }

    @Test
    public void testGuessingNormal() {
        final ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        final ProjectNameVersionGuess guess = guesser.guessNameAndVersion("angular-1.6.1");
        assertEquals("angular", guess.getProjectName());
        assertEquals("1.6.1", guess.getVersionName());
    }

    @Test
    public void testGuessingWithPeriods() {
        final ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        final ProjectNameVersionGuess guess = guesser.guessNameAndVersion("Microsoft.Net.Http.2.2.29");
        assertEquals("Microsoft.Net.Http", guess.getProjectName());
        assertEquals("2.2.29", guess.getVersionName());
    }

    @Test
    public void testGuessingWithMultipleHyphensInName() {
        final ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        final ProjectNameVersionGuess guess = guesser.guessNameAndVersion("babel-polyfill-6.22.0");
        assertEquals("babel-polyfill", guess.getProjectName());
        assertEquals("6.22.0", guess.getVersionName());
    }

    @Test
    public void testGuessingWithMultipleHyphensInVersion() {
        final ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        final ProjectNameVersionGuess guess = guesser.guessNameAndVersion("functools32-3.2.3-2");
        assertEquals("functools32", guess.getProjectName());
        assertEquals("3.2.3-2", guess.getVersionName());
    }

}
