package com.synopsys.integration.blackduck.bdio2;

import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.util.NameVersion;

public class Bdio2Headers {
    // IDETECT-2756
    public static final String PROJECT_NAME_HEADER = "X-BD-PROJECT-NAME";
    public static final String VERSION_NAME_HEADER = "X-BD-VERSION-NAME";

    private Bdio2Headers() {}

    public static void applyOptionalMappingHeaders(BlackDuckRequestBuilder builder, NameVersion projectNameVersion, boolean includeProjectMappingHeaders) {
        if (includeProjectMappingHeaders) {
            builder
                .addHeader(Bdio2Headers.PROJECT_NAME_HEADER, projectNameVersion.getName())
                .addHeader(Bdio2Headers.VERSION_NAME_HEADER, projectNameVersion.getVersion());
        }
    }
}
