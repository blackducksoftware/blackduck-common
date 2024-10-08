package com.blackduck.integration.blackduck.codelocation.signaturescanner.command;

import com.blackduck.integration.blackduck.exception.SignatureScannerInputException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ScanCommandArgumentParserTest {
    private ScanCommandArgumentParser parser = new ScanCommandArgumentParser();

    @Test
    public void testParsesArgumentsWithSpaces() throws SignatureScannerInputException {
        String command = "--upload-source --exclude \"/Users/joe/test folder/\"";
        List<String> arguments = parser.parse(command);
        assertTrue(arguments.contains("--exclude"));
        assertTrue(arguments.contains("\"/Users/joe/test folder/\""));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/codelocation/signaturescanner/command/ArgumentsThatShouldCauseExceptions.txt")
    public void testStrangeInputThatShouldTriggerExceptions(String argument) {
        assertThrows(SignatureScannerInputException.class, () -> parser.parse(argument));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/codelocation/signaturescanner/command/ArgumentsThatShouldProcessSuccessfully.txt")
    public void testStrangeInputThatShouldProcessSuccessfully(String argument, int numExpectedPiecesInResult) throws SignatureScannerInputException {
        assertEquals(numExpectedPiecesInResult, parser.parse(argument).size());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/codelocation/signaturescanner/command/ArgumentValuesWithEscapedQuotes.txt")
    public void testParsesArgumentsWithEscapedQuotes(String argValue) throws SignatureScannerInputException {
        String command = String.format("--exclude %s", argValue);
        List<String> arguments = parser.parse(command);
        assertTrue(arguments.contains("--exclude"));
        assertTrue(arguments.contains(argValue));
    }

}
