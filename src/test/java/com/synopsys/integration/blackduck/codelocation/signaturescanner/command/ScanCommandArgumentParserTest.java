package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

    // create distinct exceptions
    @Test
    public void testStrangeInput() throws IntegrationException {
        String command = "--exclude \"/Users/.../untitled folder/ \"--upload-source";
        assertTrue(throwsException(command, parser));

        String command2 = "\"\"\"\"";
        assertEquals(1, parser.parse(command2).size());

        String command3 = "--exclude \"this \""; // TODO- (when encoding values with a trailing space in resource file, should use unicode /u0020 to ensure nobody misses it
        assertTrue(throwsException(command3, parser));

        String command4 = "--exclude \"this \" abc";
        assertEquals(3, parser.parse(command4).size());
    }

    //TODO- look into parameterized tests (maybe one that takes values from a resource file to enhance readability, make a copy of test first to ensure I don't make mistakes)
    @Test
    public void testThrowsExceptionWhenNumberOfNonEscapedQuotesIsUneven() {
        String command;

        // Input w/ escaped quotes
        command = "--exclude \"this\" \"";
        Assertions.assertTrue(throwsException(command, parser));

        command = "--exclude \"thi\"s \"";
        Assertions.assertTrue(throwsException(command, parser));

        command = "--exclude \"this\\\" \"";
        Assertions.assertFalse(throwsException(command, parser));

        command = "--exclude \"this\\\" thing\"\"";
        Assertions.assertTrue(throwsException(command, parser));

        // Input with trailing space

    }

    // if I create distinct exceptions, have them extend a parent 'ScannerInputException' and dictate that in this method's parameter
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
