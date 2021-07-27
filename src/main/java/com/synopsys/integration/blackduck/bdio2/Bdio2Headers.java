package com.synopsys.integration.blackduck.bdio2;

public class Bdio2Headers {
    // IDETECT-2756
    public static final String PROJECT_NAME_HEADER = "X-BD-PROJECT-NAME";
    public static final String VERSION_NAME_HEADER = "X-BD-VERSION-NAME";

    public static final String HEADER_CONTENT_TYPE = "Content-type";
    public static final String HEADER_X_BD_MODE = "X-BD-MODE";
    public static final String HEADER_X_BD_DOCUMENT_COUNT = "X-BD-DOCUMENT-COUNT";

    private Bdio2Headers() {}
}
