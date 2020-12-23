package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class ScanCommandArgumentParser {

    public List<String> parse(String command) {
        if (!isParseable(command)) {
            return new ArrayList<>();
        }

        List<String> parsedArguments = new LinkedList<>();
        String[] splitBySpaces = command.split("\\s");
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

    private boolean isParseable(String command) {
        if (StringUtils.isBlank(command)) {
            return false;
        }
        return hasEvenNumberOfQuotes(command);
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
