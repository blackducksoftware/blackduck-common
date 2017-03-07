package com.blackducksoftware.integration.hub.util;

import org.junit.Assert;
import org.junit.Test;

public class ProjectNameVersionGuesserTest {
    @Test
    public void testProjectNameVersionGuess() {
        final ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        final ProjectNameVersionGuess guess = guesser.guessNameAndVersion("nothing");
        Assert.assertEquals("nothing", guess.getProjectName());
        Assert.assertEquals("2017-03-06", guess.getVersionName());
    }

    @Test
    public void testGuessingNormal() {
        final ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        final ProjectNameVersionGuess guess = guesser.guessNameAndVersion("angular-1.6.1");
        Assert.assertEquals("angular", guess.getProjectName());
        Assert.assertEquals("1.6.1", guess.getVersionName());
    }

    @Test
    public void testGuessingWithMultipleHyphensInName() {
        final ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        final ProjectNameVersionGuess guess = guesser.guessNameAndVersion("babel-polyfill-6.22.0");
        Assert.assertEquals("babel-polyfill", guess.getProjectName());
        Assert.assertEquals("6.22.0", guess.getVersionName());
    }

    @Test
    public void testGuessingWithMultipleHyphensInVersion() {
        final ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        final ProjectNameVersionGuess guess = guesser.guessNameAndVersion("functools32-3.2.3-2");
        Assert.assertEquals("functools32", guess.getProjectName());
        Assert.assertEquals("3.2.3-2", guess.getVersionName());
    }

    @Test
    public void testGuessingWithPeriodsInName() {
        final ProjectNameVersionGuesser guesser = new ProjectNameVersionGuesser();
        final ProjectNameVersionGuess guess = guesser.guessNameAndVersion("Microsoft.Net.Http.2.2.29");
        Assert.assertEquals("Microsoft.Net.Http", guess.getProjectName());
        Assert.assertEquals("2.2.29", guess.getVersionName());
    }

}
