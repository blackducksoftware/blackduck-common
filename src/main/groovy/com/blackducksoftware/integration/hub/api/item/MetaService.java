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
import java.util.List;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.meta.MetaAllowEnum;
import com.blackducksoftware.integration.hub.model.HubView;
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

    public static final String CODE_LOCATION_LINK = "codelocations";

    public static final String CODE_LOCATION_BOM_STATUS_LINK = "codelocation";

    public static final String SCANS_LINK = "scans";

    public static final String NOTIFICATIONS_LINK = "notifications";

    public static final String USERS_LINK = "users";

    public static final String GLOBAL_OPTIONS_LINK = "global-options";

    public static final String USER_OPTIONS_LINK = "user-options";

    public static final String TEXT_LINK = "text";
    
    public static final String COMPONENT_ISSUES = "component-issues";

    private final IntLogger logger;

    private final JsonParser jsonParser;

    public MetaService(final IntLogger logger, final JsonParser jsonParser) {
        this.logger = logger;
        this.jsonParser = jsonParser;
    }

    public boolean hasLink(final HubView item, final String linkKey) throws HubIntegrationException {
        final JsonArray linksArray = getLinks(item);
        if (linksArray == null) {
            return false;
        }
        for (final JsonElement linkElement : linksArray) {
            final JsonObject linkObject = linkElement.getAsJsonObject();
            final String rel = linkObject.get("rel").getAsString();
            if (rel.equals(linkKey)) {
                return true;
            }
        }
        return false;
    }

    public String getFirstLink(final HubView item, final String linkKey) throws HubIntegrationException {
        final JsonArray linksArray = getLinks(item);
        if (linksArray == null) {
            throw new HubIntegrationException("Could not find any links for this item : " + item.json);
        }

        final StringBuilder linksAvailable = new StringBuilder();
        linksAvailable.append("Could not find the link '" + linkKey + "', these are the available links : ");
        int i = 0;
        for (final JsonElement linkElement : linksArray) {
            final JsonObject linkObject = linkElement.getAsJsonObject();
            final String rel = linkObject.get("rel").getAsString();
            if (rel.equals(linkKey)) {
                return linkObject.get("href").getAsString();
            }
            if (i > 0) {
                linksAvailable.append(", ");
            }
            linksAvailable.append("'" + rel + "'");
            i++;
        }
        linksAvailable.append(". For Item : " + getHref(item));
        throw new HubIntegrationException(linksAvailable.toString());
    }

    public String getFirstLinkSafely(final HubView item, final String linkKey) {
        try {
            final String link = getFirstLink(item, linkKey);
            return link;
        } catch (final HubIntegrationException e) {
            logger.debug("Link '" + linkKey + "' not found on item");
            return null;
        }
    }

    public List<String> getLinks(final HubView item, final String linkKey) throws HubIntegrationException {
        final JsonArray linksArray = getLinks(item);
        if (linksArray == null) {
            throw new HubIntegrationException("Could not find any links for this item : " + item.json);
        }

        final List<String> links = new ArrayList<>();
        final StringBuilder linksAvailable = new StringBuilder();
        linksAvailable.append("Could not find the link '" + linkKey + "', these are the available links : ");
        int i = 0;
        for (final JsonElement linkElement : linksArray) {
            final JsonObject linkObject = linkElement.getAsJsonObject();
            final String rel = linkObject.get("rel").getAsString();
            final String linkValue = linkObject.get("href").getAsString();
            if (rel.equals(linkKey)) {
                links.add(linkValue);
            }
            if (i > 0) {
                linksAvailable.append(", ");
            }
            linksAvailable.append("'" + rel + "'");
            i++;
        }
        if (!links.isEmpty()) {
            return links;
        }
        linksAvailable.append(". For Item : " + getHref(item));
        throw new HubIntegrationException(linksAvailable.toString());
    }

    private JsonArray getLinks(final HubView item) throws HubIntegrationException {
        final JsonObject metaJson = getMeta(item);
        final JsonElement linksElement = metaJson.get("links");
        if (linksElement == null) {
            if (logger != null) {
                logger.error("Hub Item has no links : " + item.json);
            }
            throw new HubIntegrationException("This Hub item does not have any link information.");
        }
        return linksElement.getAsJsonArray();
    }

    public List<MetaAllowEnum> getAllowedMethods(final HubView item) throws HubIntegrationException {
        final List<MetaAllowEnum> allows = new ArrayList<>();
        final JsonObject metaJson = getMeta(item);
        final JsonElement allowElement = metaJson.get("allow");
        if (allowElement == null) {
            if (logger != null) {
                logger.error("Hub Item has no allow : " + item.json);
            }
            throw new HubIntegrationException("This Hub item does not have any allow information.");
        }
        final JsonArray allowArray = allowElement.getAsJsonArray();
        for (final JsonElement allow : allowArray) {
            allows.add(MetaAllowEnum.valueOf(allow.getAsString()));
        }

        return allows;
    }

    public String getHref(final HubView item) throws HubIntegrationException {
        final JsonObject metaJson = getMeta(item);
        final JsonElement hrefElement = metaJson.get("href");
        if (hrefElement == null) {
            if (logger != null) {
                logger.error("Hub Item has no href : " + item.json);
            }
            throw new HubIntegrationException("This Hub item does not have any href information.");
        }
        return hrefElement.getAsString();
    }

    private JsonObject getMeta(final HubView item) throws HubIntegrationException {
        final String json = item.json;
        final JsonElement element = jsonParser.parse(json);
        final JsonObject jsonObject = element.getAsJsonObject();
        final JsonElement metaElement = jsonObject.get("_meta");
        if (metaElement == null) {
            if (logger != null) {
                logger.error("Hub Item has no meta : " + item.json);
            }
            throw new HubIntegrationException("This Hub item does not have meta information.");
        }
        return metaElement.getAsJsonObject();
    }

}
