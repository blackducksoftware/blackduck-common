package com.blackduck.integration.blackduck.util;

import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.blackduck.service.model.ProjectNameVersionGuess;
import com.blackduck.integration.blackduck.service.model.ProjectNameVersionGuesser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(TimingExtension.class)
public class ProjectNameVersionGuesserTest {
    @Test
    public void testProjectNameVersionGuess() {
        ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        ProjectNameVersionGuess guess = guesser.guessNameAndVersion("nothing");
        String defaultVersion = guesser.getDefaultVersionGuess();
        assertEquals("nothing", guess.getProjectName());
        assertEquals(defaultVersion, guess.getVersionName());
    }

    @Test
    public void testGuessingSingleHyphen() {
        ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        ProjectNameVersionGuess guess = guesser.guessNameAndVersion("-");
        String defaultVersion = guesser.getDefaultVersionGuess();
        assertEquals("-", guess.getProjectName());
        assertEquals(defaultVersion, guess.getVersionName());
    }

    @Test
    public void testGuessingSinglePeriod() {
        ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        ProjectNameVersionGuess guess = guesser.guessNameAndVersion(".");
        String defaultVersion = guesser.getDefaultVersionGuess();
        assertEquals(".", guess.getProjectName());
        assertEquals(defaultVersion, guess.getVersionName());
    }

    @Test
    public void testGuessingAllHyphens() {
        ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        ProjectNameVersionGuess guess = guesser.guessNameAndVersion("-----");
        String defaultVersion = guesser.getDefaultVersionGuess();
        assertEquals("-----", guess.getProjectName());
        assertEquals(defaultVersion, guess.getVersionName());
    }

    @Test
    public void testGuessingAllPeriods() {
        ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        ProjectNameVersionGuess guess = guesser.guessNameAndVersion(".....");
        String defaultVersion = guesser.getDefaultVersionGuess();
        assertEquals(".....", guess.getProjectName());
        assertEquals(defaultVersion, guess.getVersionName());
    }

    @Test
    public void testLongVersionName() {
        ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        ProjectNameVersionGuess guess = guesser.guessNameAndVersion("valohai_yaml-0.4-py2.py3-none-any");
        assertEquals("valohai_yaml", guess.getProjectName());
        assertEquals("0.4-py2.py3-none-any", guess.getVersionName());
    }

    @Test
    public void testGuessingNormal() {
        ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        ProjectNameVersionGuess guess = guesser.guessNameAndVersion("angular-1.6.1");
        assertEquals("angular", guess.getProjectName());
        assertEquals("1.6.1", guess.getVersionName());
    }

    @Test
    public void testGuessingWithPeriods() {
        ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        ProjectNameVersionGuess guess = guesser.guessNameAndVersion("Microsoft.Net.Http.2.2.29");
        assertEquals("Microsoft.Net.Http", guess.getProjectName());
        assertEquals("2.2.29", guess.getVersionName());
    }

    @Test
    public void testGuessingWithMultipleHyphensInName() {
        ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        ProjectNameVersionGuess guess = guesser.guessNameAndVersion("babel-polyfill-6.22.0");
        assertEquals("babel-polyfill", guess.getProjectName());
        assertEquals("6.22.0", guess.getVersionName());
    }

    @Test
    public void testGuessingWithMultipleHyphensInVersion() {
        ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        ProjectNameVersionGuess guess = guesser.guessNameAndVersion("functools32-3.2.3-2");
        assertEquals("functools32", guess.getProjectName());
        assertEquals("3.2.3-2", guess.getVersionName());
    }

}
