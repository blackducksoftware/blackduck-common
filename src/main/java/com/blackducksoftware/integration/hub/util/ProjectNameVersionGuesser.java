package com.blackducksoftware.integration.hub.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class ProjectNameVersionGuesser {
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public ProjectNameVersionGuess guessNameAndVersion(final String fullString) {
        String guessedName = "";
        String guessedVersion = "";

        if (fullString.contains("-"))

            if (StringUtils.isBlank(guessedName) || StringUtils.isBlank(guessedVersion)) {
            guessedName = fullString;
            guessedVersion = getDefaultVersionGuess();
            }

        return new ProjectNameVersionGuess(guessedName, guessedVersion);
    }

    public String getDefaultVersionGuess() {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
        final String defaultVersionGuess = simpleDateFormat.format(new Date());
        return defaultVersionGuess;
    }

}
