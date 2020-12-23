package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class ScanCommandArgumentParserTest {

    @Test
    public void testParsesArgumentsWithSpaces() {
        String command = "--upload-source --exclude \"/Users/joe/test folder/\"";
        ScanCommandArgumentParser parser = new ScanCommandArgumentParser();
        List<String> arguments = parser.parse(command);
        assertTrue(arguments.contains("--exclude"));
        assertTrue(arguments.contains("\"/Users/joe/test folder/\""));
    }
}
