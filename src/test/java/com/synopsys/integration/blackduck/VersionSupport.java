package com.synopsys.integration.blackduck;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VersionSupport {
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
