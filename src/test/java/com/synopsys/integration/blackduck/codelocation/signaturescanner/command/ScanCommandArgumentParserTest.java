package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ScanCommandArgumentParserTest {

    @Test
    public void testParsesArgumentsWithSpaces() {
        String argumentsWithSpaces = "--upload-source --exclude \"/Users/joe/test folder/\"";
        ScanCommandArgumentParser parser = new ScanCommandArgumentParser();
        List<String> arguments = parser.parse(argumentsWithSpaces);
        assertTrue(arguments.contains("--exclude"));
        assertTrue(arguments.contains("\"/Users/joe/test folder/\""));
    }

    @Test
    public void testReturnsEmpytListWhenNumberOfNonEscapedQuotesIsUneven() {
        ScanCommandArgumentParser parser = new ScanCommandArgumentParser();

        String command1 = "--exclude \"this\" \"";
        Assertions.assertEquals(0, parser.parse(command1).size());

        String command2 = "--exclude \"thi\"s \"";
        Assertions.assertEquals(0, parser.parse(command2).size());

        String command3 = "--exclude \"this\\\" \"";
        Assertions.assertNotEquals(0, parser.parse(command3).size());

        String command4 = "--exclude \"this\\\" thing\"\"";
        Assertions.assertEquals(0, parser.parse(command4).size());
    }

    @Test
    public void testParsesArgumentsWithEscapedQuotes() {
        ScanCommandArgumentParser parser = new ScanCommandArgumentParser();

        String command1 = "--exclude \"/Users/joe/test \\\" folder/\"";
        List<String> arguments1 = parser.parse(command1);
        assertTrue(arguments1.contains("--exclude"));
        assertTrue(arguments1.contains("\"/Users/joe/test \\\" folder/\""));

        String command2 = "--exclude \"/Users/joe/tes\\\"t folder/\"";
        List<String> arguments2 = parser.parse(command2);
        assertTrue(arguments2.contains("--exclude"));
        assertTrue(arguments2.contains("\"/Users/joe/tes\\\"t folder/\""));

        String command3 = "--exclude \"/Users/joe/tes\\\\\"t folder/\"";
        List<String> arguments3 = parser.parse(command3);
        assertTrue(arguments3.contains("--exclude"));
        assertTrue(arguments3.contains("\"/Users/joe/tes\\\\\"t folder/\""));
    }
}
