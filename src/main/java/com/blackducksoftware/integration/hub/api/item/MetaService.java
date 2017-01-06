/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.api.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.meta.MetaAllowEnum;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.request.HubRequestFactory;
import com.blackducksoftware.integration.log.IntLogger;
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

    public static final String VULNERABILITIES_LINK = "vulnerabilities";

    public static final String VULNERABLE_COMPONENTS_LINK = "vulnerable-components";

    public static final String CONTENT_LINK = "content";

    public static final String DOWNLOAD_LINK = "download";

    public static final String CODE_LOCATION_LINK = "codelocation";

    public static final String SCANS_LINK = "scans";

    public static final String NOTIFICATIONS_LINK = "notifications";

    public static final String USERS_LINK = "users";

    public static final String GLOBAL_OPTIONS_LINK = "global-options";

    public static final String USER_OPTIONS_LINK = "user-options";

    private final IntLogger logger;

    private final JsonParser jsonParser;

    private final HubRequestFactory hubRequestFactory;

    public MetaService(IntLogger logger, JsonParser jsonParser, HubRequestFactory hubRequestFactory) {
        this.logger = logger;
        this.jsonParser = jsonParser;
        this.hubRequestFactory = hubRequestFactory;
    }

    public String getLink(HubItem item, String linkKey) throws HubIntegrationException {
        List<String> linkHrefs = getLinks(item).get(linkKey);
        if (linkHrefs.size() > 1) {
            if (logger != null) {
                logger.error("Hub Item has multiple links for key : " + linkKey + " : " + item.getJson());
            }
            throw new HubIntegrationException("Only expected to get a single link for the key : " + linkKey);
        }
        return linkHrefs.get(0);
    }

    public Map<String, List<String>> getLinks(HubItem item) throws HubIntegrationException {
        Map<String, List<String>> links = new HashMap<>();
        JsonObject metaJson = getMeta(item);
        JsonElement linksElement = metaJson.get("links");
        if (linksElement == null) {
            if (logger != null) {
                logger.error("Hub Item has no links : " + item.getJson());
            }
            throw new HubIntegrationException("This Hub item does not have any link information.");
        }
        JsonArray linkArray = linksElement.getAsJsonArray();
        for (JsonElement linkElement : linkArray) {
            JsonObject linkObject = linkElement.getAsJsonObject();
            String ref = linkObject.get("rel").getAsString();
            String linkHref = linkObject.get("href").getAsString();

            List<String> existingHrefs = links.get(ref);
            if (existingHrefs != null) {
                existingHrefs.add(linkHref);
                links.put(ref, existingHrefs);
            } else {
                List<String> linkHrefs = new ArrayList<>();
                linkHrefs.add(linkHref);
                links.put(ref, linkHrefs);
            }
        }
        return links;
    }

    public List<MetaAllowEnum> getAllowedMethods(HubItem item) throws HubIntegrationException {
        List<MetaAllowEnum> allows = new ArrayList<>();
        JsonObject metaJson = getMeta(item);
        JsonElement allowElement = metaJson.get("allow");
        if (allowElement == null) {
            if (logger != null) {
                logger.error("Hub Item has no allow : " + item.getJson());
            }
            throw new HubIntegrationException("This Hub item does not have any allow information.");
        }
        JsonArray allowArray = allowElement.getAsJsonArray();
        for (JsonElement allow : allowArray) {
            allows.add(MetaAllowEnum.valueOf(allow.getAsString()));
        }

        return allows;
    }

    public String getHref(HubItem item) throws HubIntegrationException {
        JsonObject metaJson = getMeta(item);
        JsonElement hrefElement = metaJson.get("href");
        if (hrefElement == null) {
            if (logger != null) {
                logger.error("Hub Item has no href : " + item.getJson());
            }
            throw new HubIntegrationException("This Hub item does not have any href information.");
        }
        return hrefElement.getAsString();
    }

    private JsonObject getMeta(HubItem item) throws HubIntegrationException {
        String json = item.getJson();
        JsonElement element = jsonParser.parse(json);
        JsonObject jsonObject = element.getAsJsonObject();
        JsonElement metaElement = jsonObject.get("_meta");
        if (metaElement == null) {
            if (logger != null) {
                logger.error("Hub Item has no meta : " + item.getJson());
            }
            throw new HubIntegrationException("This Hub item does not have meta information.");
        }
        return metaElement.getAsJsonObject();
    }

    public void deleteItem(HubItem hubItem) throws HubIntegrationException {
        String itemUrl = getHref(hubItem);
        HubRequest hubRequest = hubRequestFactory.createDeleteRequest(itemUrl);
        hubRequest.executeDelete();
    }

}
