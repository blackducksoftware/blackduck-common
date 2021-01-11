package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

public class ScanCommandQuoteParser {

    public boolean hasEvenNumberOfNonEscapedQuotes(String command) {
        int numberOfNonEscapedQuotes = 0;
        char quote = '"';
        char backslash = '\\';
        char last = 0;
        for (char current : command.toCharArray()) {
            if (current == quote && last != backslash) {
                numberOfNonEscapedQuotes++;
            }
            last = current;
        }

        return (numberOfNonEscapedQuotes % 2) == 0;
    }
}
