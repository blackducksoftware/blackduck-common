/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.api.item;

import java.util.Map;

public class MetaService {

    public static final String PROJECTS_LINK = "projects";

    public static final String PROJECT_LINK = "project";

    public static final String VERSIONS_LINK = "versions";

    public static final String VERSION_LINK = "versions";

    public static final String CANONICAL_VERSION_LINK = "canonicalVersion";

    public static final String VERSION_REPORT_LINK = "versionReport";

    public static final String COMPONENTS_LINK = "components";

    public static final String POLICY_RULES_LINK = "policy-rules";

    public static final String POLICY_RULE_LINK = "policy-rule";

    public static final String POLICY_STATUS_LINK = "policy-status";

    public static final String RISK_PROFILE_LINK = "riskProfile";

    public static final String VULNERABLE_COMPONENTS_LINK = "vulnerable-components";

    public static final String REPORT_CONTENT_LINK = "content";

    public static final String REPORT_DOWNLOAD_LINK = "download";

    public static final String CODE_LOCATION_LINK = "codelocation";

    public static final String USERS_LINK = "users";

    public static String getLink(HubItem item, String linkKey) {
        String json = item.getJson();

        return null;
    }

    public static Map<String, String> getLinks(HubItem item) {

        return null;
    }

}
