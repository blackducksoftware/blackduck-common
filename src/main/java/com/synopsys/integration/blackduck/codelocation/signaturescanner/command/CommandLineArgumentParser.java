package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class CommandLineArgumentParser {

    public List<String> parse(String commandLine) {
        if (StringUtils.isBlank(commandLine)) {
            return new ArrayList<>();
        }

        if (!hasEvenNumberOfQuotes(commandLine)) {
            // uneven number of quotes not parseable;
            return new ArrayList<>();
        }
        List<String> parsedArguments = new LinkedList<>();
        String[] splitBySpaces = commandLine.split("\\s");
        boolean inQuotes = false;
        StringBuilder currentArgument = null;
        for (String text : splitBySpaces) {
            if (text.startsWith("\"") || text.startsWith("'")) {
                inQuotes = true;
                currentArgument = new StringBuilder();
            }
            if (inQuotes) {
                if (currentArgument.length() > 0) {
                    currentArgument.append(" ");
                }
                currentArgument.append(text);
                if (text.endsWith("\"") || text.endsWith("'")) {
                    inQuotes = false;
                    parsedArguments.add(currentArgument.toString());
                }
            } else {
                parsedArguments.add(text);
            }
        }
        return parsedArguments;
    }

    private boolean hasEvenNumberOfQuotes(String commandLine) {
        int pairsOfDoubleQuotes = StringUtils.countMatches(commandLine, "\"") % 2;
        int pairsOfSingleQuotes = StringUtils.countMatches(commandLine, "'") % 2;

        if (pairsOfDoubleQuotes != 0 || pairsOfSingleQuotes != 0) {
            return false;
        }
        return true;
    }
}
