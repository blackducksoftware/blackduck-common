package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.io.Files;
import com.synopsys.integration.blackduck.exception.SignatureScannerInputException;
import com.synopsys.integration.exception.IntegrationException;

public class ScanCommandArgumentParserTest {
    private ScanCommandArgumentParser parser = new ScanCommandArgumentParser();

    @Test
    public void testParsesArgumentsWithSpaces() throws IntegrationException {
        String command = "--upload-source --exclude \"/Users/joe/test folder/\"";
        List<String> arguments = parser.parse(command);
        assertTrue(arguments.contains("--exclude"));
        assertTrue(arguments.contains("\"/Users/joe/test folder/\""));
    }

    @Test
    public void testStrangeInputThatShouldTriggerExceptions() throws URISyntaxException, IOException {
        File argumentValuesResourceFile = getResourceFile("/codelocation/signaturescanner/command/ArgumentsThatShouldCauseExceptions.txt");
        for (String argument : Files.readLines(argumentValuesResourceFile, Charset.defaultCharset())) {
            assertTrue(throwsException(argument, parser));
        }
    }

    @Test
    public void testStrangeInputThatShouldProcessSuccessfully() throws IOException, URISyntaxException, SignatureScannerInputException {
        File argumentValuesResourceFile = getResourceFile("/codelocation/signaturescanner/command/ArgumentsThatShouldProcessSuccessfully.txt");
        for (String line : Files.readLines(argumentValuesResourceFile, Charset.defaultCharset())) {
            String[] pieces = line.split(" = ");
            String argument = pieces[0];
            int numberOfPieces = Integer.parseInt(pieces[1]);
            assertEquals(numberOfPieces, parser.parse(argument).size());
        }
    }

    //TODO- look into parameterized tests <-- is it really necessary?
    @Test
    public void testThrowsExceptionWhenNumberOfNonEscapedQuotesIsUneven() throws URISyntaxException, IOException, IntegrationException {
        File argumentValuesResourceFile = getResourceFile("/codelocation/signaturescanner/command/ArgumentValuesWithMismatchedQuotes.txt");
        for (String line : Files.readLines(argumentValuesResourceFile, Charset.defaultCharset())) {
            String[] pieces = line.split(" = ");
            String argValue = pieces[0];
            boolean shouldThrowException = pieces[1].equals("true");
            String command = String.format("--exclude %s", argValue);
            assertEquals(shouldThrowException, throwsException(command, parser));
        }
    }

    // TODO - feasible to pass specific exception into method?
    private boolean throwsException(String command, ScanCommandArgumentParser parser) {
        try {
            parser.parse(command);
        } catch (IntegrationException e) {
            return true;
        }
        return false;
    }

    @Test
    public void testParsesArgumentsWithEscapedQuotes() throws IntegrationException, URISyntaxException, IOException {
        File argumentValuesResourceFile = getResourceFile("/codelocation/signaturescanner/command/ArgumentValuesWithEscapedQuotes.txt");
        for (String argValue : Files.readLines(argumentValuesResourceFile, Charset.defaultCharset())) {
            String command = String.format("--exclude %s", argValue);
            List<String> arguments = parser.parse(command);
            assertTrue(arguments.contains("--exclude"));
            assertTrue(arguments.contains(argValue));
        }
    }

    private File getResourceFile(String resourceName) throws URISyntaxException {
        return new File(ScanCommandArgumentParserTest.class.getResource(resourceName).toURI());
    }
}
