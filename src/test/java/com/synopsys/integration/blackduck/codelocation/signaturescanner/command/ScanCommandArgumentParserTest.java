package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.exception.IntegrationException;

public class ScanCommandArgumentParserTest {

    @Test
    public void testParsesArgumentsWithSpaces() throws IntegrationException {
        String command = "--upload-source --exclude \"/Users/joe/test folder/\"";
        ScanCommandArgumentParser parser = new ScanCommandArgumentParser();
        List<String> arguments = parser.parse(command);
        assertTrue(arguments.contains("--exclude"));
        assertTrue(arguments.contains("\"/Users/joe/test folder/\""));
    }

    @Test
    public void testThrowsExceptionWhenNumberOfNonEscapedQuotesIsUneven() {
        ScanCommandArgumentParser parser = new ScanCommandArgumentParser();
        String command;

        command = "--exclude \"this\" \"";
        Assertions.assertTrue(throwsException(command, parser));

        command = "--exclude \"thi\"s \"";
        Assertions.assertTrue(throwsException(command, parser));

        command = "--exclude \"this\\\" \"";
        Assertions.assertFalse(throwsException(command, parser));

        command = "--exclude \"this\\\" thing\"\"";
        Assertions.assertTrue(throwsException(command, parser));
    }

    private boolean throwsException(String command, ScanCommandArgumentParser parser) {
        try {
            parser.parse(command);
        } catch (IntegrationException e) {
            return true;
        }
        return false;
    }

    @Test
    public void testParsesArgumentsWithEscapedQuotes() throws IntegrationException {
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
