package com.blackducksoftware.integration.hub.api.view;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.blackducksoftware.integration.hub.api.core.HubView;
import com.blackducksoftware.integration.hub.api.enumeration.ScanSummaryStatusType;
import com.blackducksoftware.integration.hub.api.generated.view.CodeLocationView;

public class ScanSummaryView extends HubView {
    public static final Map<String, Type> links = new HashMap<>();

    public static final String CODELOCATION_LINK = "codelocation";

    static {
        links.put(CODELOCATION_LINK, CodeLocationView.class);
    }

    public ScanSummaryStatusType status;
    public String statusMessage;
    public Date createdAt;
    public Date updatedAt;

}
