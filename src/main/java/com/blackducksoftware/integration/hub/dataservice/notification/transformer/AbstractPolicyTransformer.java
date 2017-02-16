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
package com.blackducksoftware.integration.hub.dataservice.notification.transformer;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.hub.api.component.version.ComponentVersion;
import com.blackducksoftware.integration.hub.api.component.version.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.notification.NotificationItem;
import com.blackducksoftware.integration.hub.api.notification.NotificationRequestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRequestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.api.version.BomComponentVersionPolicyStatus;
import com.blackducksoftware.integration.hub.api.version.VersionBomPolicyRequestService;
import com.blackducksoftware.integration.hub.dataservice.model.ProjectVersion;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.hub.service.HubRequestService;

public abstract class AbstractPolicyTransformer extends AbstractNotificationTransformer {
    private final PolicyNotificationFilter policyFilter;

    /**
     * policyFilter.size() == 0: match no rules
     * policyFilter == null: match all rules
     */
    public AbstractPolicyTransformer(final NotificationRequestService notificationService,
            final ProjectVersionRequestService projectVersionService, final PolicyRequestService policyService,
            final VersionBomPolicyRequestService bomVersionPolicyService, final HubRequestService hubRequestService,
            final PolicyNotificationFilter policyFilter,
            final MetaService metaService) {
        super(notificationService, projectVersionService, policyService, bomVersionPolicyService, hubRequestService,
                metaService);
        this.policyFilter = policyFilter;
    }

    public abstract void handleNotification(final List<ComponentVersionStatus> componentVersionList,
            final ProjectVersion projectVersion, final NotificationItem item,
            final List<NotificationContentItem> templateData) throws HubItemTransformException;

    protected void handleNotificationUsingBomComponentVersionPolicyStatusLink(
            final List<ComponentVersionStatus> componentVersionList, final ProjectVersion projectVersion,
            final NotificationItem item, final List<NotificationContentItem> templateData)
            throws HubItemTransformException {
        for (final ComponentVersionStatus componentVersion : componentVersionList) {
            try {
                final String componentVersionLink = componentVersion.getComponentVersionLink();
                final ComponentVersion fullComponentVersion = getComponentVersion(componentVersionLink);
                if ((componentVersion.getPolicies() == null) || (componentVersion.getPolicies().size() == 0)) {
                    throw new HubItemTransformException("The polices list in the component version status is null or empty");
                }
                final List<String> ruleList = getMatchingRuleUrls(componentVersion.getPolicies());
                if (ruleList != null && !ruleList.isEmpty()) {
                    final List<PolicyRule> policyRuleList = new ArrayList<>();
                    for (final String ruleUrl : ruleList) {
                        final PolicyRule rule = getPolicyRule(ruleUrl);
                        policyRuleList.add(rule);
                    }
                    createContents(projectVersion, componentVersion.getComponentName(), fullComponentVersion,
                            componentVersion.getComponentLink(),
                            componentVersion.getComponentVersionLink(),
                            policyRuleList, item, templateData);
                }

            } catch (final Exception e) {
                throw new HubItemTransformException(e);
            }
        }
    }

    protected List<PolicyRule> getRulesFromUrls(final List<String> ruleUrlsViolated) throws HubIntegrationException {
        if (ruleUrlsViolated == null || ruleUrlsViolated.isEmpty()) {
            return null;
        }
        final List<PolicyRule> rules = new ArrayList<>();
        for (final String ruleUrlViolated : ruleUrlsViolated) {
            final PolicyRule ruleViolated = getPolicyService().getItem(ruleUrlViolated);
            rules.add(ruleViolated);
        }
        return rules;
    }

    protected List<PolicyRule> getMatchingRules(final List<PolicyRule> rulesViolated) throws HubIntegrationException {
        final List<PolicyRule> filteredRules = new ArrayList<>();
        if (policyFilter != null && policyFilter.getRuleLinksToInclude() != null) {
            for (final PolicyRule ruleViolated : rulesViolated) {
                final String ruleHref = getMetaService().getHref(ruleViolated);
                if (policyFilter.getRuleLinksToInclude().contains(ruleHref)) {
                    filteredRules.add(ruleViolated);
                }
            }
        } else {
            return rulesViolated;
        }
        return filteredRules;
    }

    protected PolicyNotificationFilter getPolicyFilter() {
        return policyFilter;
    }

    protected PolicyRule getPolicyRule(final String ruleUrl) throws HubIntegrationException {
        PolicyRule rule;
        rule = getPolicyService().getItem(ruleUrl);
        return rule;
    }

    protected List<String> getMatchingRuleUrls(final List<String> rulesViolated) {
        final List<String> filteredRules = new ArrayList<>();
        if (policyFilter != null && policyFilter.getRuleLinksToInclude() != null) {
            for (final String ruleViolated : rulesViolated) {
                if (policyFilter.getRuleLinksToInclude().contains(ruleViolated)) {
                    filteredRules.add(ruleViolated);
                }
            }
        } else {
            return rulesViolated;
        }
        return filteredRules;
    }

    protected List<String> getRuleUrls(final List<String> rulesViolated) {
        if (rulesViolated == null || rulesViolated.isEmpty()) {
            return null;
        }
        final List<String> matchingRules = new ArrayList<>();
        for (final String ruleViolated : rulesViolated) {
            final String fixedRuleUrl = fixRuleUrl(ruleViolated);
            matchingRules.add(fixedRuleUrl);
        }
        return matchingRules;
    }

    /**
     * In Hub versions prior to 3.2, the rule URLs contained in notifications
     * are internal. To match the configured rule URLs, the "internal" segment
     * of the URL from the notification must be removed. This is the workaround
     * recommended by Rob P. In Hub 3.2 on, these URLs will exclude the
     * "internal" segment.
     *
     * @param origRuleUrl
     * @return
     */
    protected String fixRuleUrl(final String origRuleUrl) {
        String fixedRuleUrl = origRuleUrl;
        if (origRuleUrl.contains("/internal/")) {
            fixedRuleUrl = origRuleUrl.replace("/internal/", "/");
        }
        return fixedRuleUrl;
    }

    protected BomComponentVersionPolicyStatus getBomComponentVersionPolicyStatus(final String policyStatusUrl) throws HubIntegrationException {
        BomComponentVersionPolicyStatus bomComponentVersionPolicyStatus;
        bomComponentVersionPolicyStatus = getBomVersionPolicyService().getItem(policyStatusUrl);

        return bomComponentVersionPolicyStatus;
    }

    public abstract void createContents(final ProjectVersion projectVersion, final String componentName,
            final ComponentVersion componentVersion, String componentUrl, final String componentVersionUrl,
            List<PolicyRule> policyRuleList,
            NotificationItem item, List<NotificationContentItem> templateData) throws URISyntaxException;

}
