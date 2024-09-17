package com.blackduck.integration.blackduck;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VersionSupport {
    /*
     * ejk 2021-07-16
     * Black Duck versions now look like this:
     * 2020.10.0, 2020.10.1, etc
     * where the format is year.month.patch.
     *
     * I am *very* confident that the patch number of Black Duck releases will
     * never exceed 28, so...this implementation isn't the dumbest thing we
     * could possibly come up with. :)
     */
    public static final String VERSION_DATE_FORMAT = "yyyy.MM.dd";

    public static boolean isVersionOrLater(String baseVersion, String versionToCheck) {
        SimpleDateFormat sdf = new SimpleDateFormat(VERSION_DATE_FORMAT);
        try {
            Date base = sdf.parse(baseVersion);
            Date toCheck = sdf.parse(versionToCheck);

            return base.equals(toCheck) || base.before(toCheck);
        } catch (ParseException e) {
        }

        return true;
    }

}
