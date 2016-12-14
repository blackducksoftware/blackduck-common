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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.meta.MetaAllowEnum;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

    public static final String CONTENT_LINK = "content";

    public static final String DOWNLOAD_LINK = "download";

    public static final String CODE_LOCATION_LINK = "codelocation";

    public static final String NOTIFICATIONS_LINK = "notifications";

    public static final String USERS_LINK = "users";

    public static final String GLOBAL_OPTIONS_LINK = "global-options";

    public static final String USER_OPTIONS_LINK = "user-options";

    private static final JsonParser jsonParser = new JsonParser();

    public static String getLink(HubItem item, String linkKey) throws HubIntegrationException {
        return getLinks(item).get(linkKey);
    }

    public static Map<String, String> getLinks(HubItem item) throws HubIntegrationException {
        Map<String, String> links = new HashMap<>();
        String json = item.getJson();
        JsonElement element = jsonParser.parse(json);
        JsonObject jsonObject = element.getAsJsonObject();
        JsonElement metaElement = jsonObject.get("_meta");
        if (metaElement == null) {
            throw new HubIntegrationException("This Hub item does not have meta information.");
        }
        JsonObject metaJson = metaElement.getAsJsonObject();
        JsonElement linksElement = metaJson.get("links");
        if (linksElement == null) {
            throw new HubIntegrationException("This Hub item does not have any link information.");
        }
        JsonArray linkArray = linksElement.getAsJsonArray();
        for (JsonElement linkElement : linkArray) {
            JsonObject linkObject = linkElement.getAsJsonObject();
            links.put(linkObject.get("rel").getAsString(), linkObject.get("href").getAsString());
        }
        return links;
    }

    public static List<MetaAllowEnum> getAllowedMethods(HubItem item) throws HubIntegrationException {
        List<MetaAllowEnum> allows = new ArrayList<>();
        String json = item.getJson();
        JsonElement element = jsonParser.parse(json);
        JsonObject jsonObject = element.getAsJsonObject();
        JsonElement metaElement = jsonObject.get("_meta");
        if (metaElement == null) {
            throw new HubIntegrationException("This Hub item does not have meta information.");
        }
        JsonObject metaJson = metaElement.getAsJsonObject();
        JsonElement allowElement = metaJson.get("allow");
        if (allowElement == null) {
            throw new HubIntegrationException("This Hub item does not have any allow information.");
        }
        JsonArray allowArray = allowElement.getAsJsonArray();
        for (JsonElement allow : allowArray) {
            allows.add(MetaAllowEnum.valueOf(allow.getAsString()));
        }

        return allows;
    }

    public static String getHref(HubItem item) throws HubIntegrationException {
        List<MetaAllowEnum> allows = new ArrayList<>();
        String json = item.getJson();
        JsonElement element = jsonParser.parse(json);
        JsonObject jsonObject = element.getAsJsonObject();
        JsonElement metaElement = jsonObject.get("_meta");
        if (metaElement == null) {
            throw new HubIntegrationException("This Hub item does not have meta information.");
        }
        JsonObject metaJson = metaElement.getAsJsonObject();
        JsonElement hrefElement = metaJson.get("href");
        if (hrefElement == null) {
            throw new HubIntegrationException("This Hub item does not have any href information.");
        }
        return hrefElement.getAsString();
    }

}
