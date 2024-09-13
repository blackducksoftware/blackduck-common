package com.blackduck.integration.blackduck.codelocation.signaturescanner.command;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScanCommandQuoteParserTest {
    private ScanCommandQuoteParser parser = new ScanCommandQuoteParser();

    @ParameterizedTest
    @CsvFileSource(resources = "/codelocation/signaturescanner/command/StringsWithMismatchedQuotes.txt")
    public void testCorrectlyIdentifiesUnevenNumberOfQuotes(String string, boolean expected) {
        assertEquals(expected, parser.hasEvenNumberOfNonEscapedQuotes(string));
    }
}
