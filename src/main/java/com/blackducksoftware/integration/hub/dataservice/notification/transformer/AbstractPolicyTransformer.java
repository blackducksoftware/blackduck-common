/**
 * hub-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.notification.NotificationService;
import com.blackducksoftware.integration.hub.api.policy.PolicyService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionService;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.dataservice.model.ProjectVersionModel;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyNotificationFilter;
import com.blackducksoftware.integration.hub.exception.HubItemTransformException;
import com.blackducksoftware.integration.hub.model.view.BomComponentPolicyStatusView;
import com.blackducksoftware.integration.hub.model.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.model.view.NotificationView;
import com.blackducksoftware.integration.hub.model.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.components.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.log.IntLogger;

public abstract class AbstractPolicyTransformer extends AbstractNotificationTransformer {
    private final PolicyNotificationFilter policyFilter;

    /**
     * policyFilter.size() == 0: match no rules
     * policyFilter == null: match all rules
     */
    public AbstractPolicyTransformer(final HubService hubResponseService, final NotificationService notificationService,
            final ProjectVersionService projectVersionService, final PolicyService policyService,
            final PolicyNotificationFilter policyFilter,
            final MetaHandler metaService) {
        super(hubResponseService, notificationService, projectVersionService, policyService,
                metaService);
        this.policyFilter = policyFilter;
    }

    public AbstractPolicyTransformer(final HubService hubResponseService, final IntLogger logger,
            final NotificationService notificationService,
            final ProjectVersionService projectVersionService, final PolicyService policyService,
            final PolicyNotificationFilter policyFilter,
            final MetaHandler metaService) {
        super(hubResponseService, logger, notificationService, projectVersionService, policyService,
                metaService);
        this.policyFilter = policyFilter;
    }

    public abstract void handleNotification(final List<ComponentVersionStatus> componentVersionList,
            final String projectName, final ProjectVersionView releaseItem, final NotificationView item,
            final List<NotificationContentItem> templateData) throws HubItemTransformException;

    protected List<PolicyRuleView> getRulesFromUrls(final List<String> ruleUrlsViolated) throws IntegrationException {
        if (ruleUrlsViolated == null || ruleUrlsViolated.isEmpty()) {
            return null;
        }
        final List<PolicyRuleView> rules = new ArrayList<>();
        for (final String ruleUrlViolated : ruleUrlsViolated) {
            final PolicyRuleView ruleViolated = getPolicyService().getView(ruleUrlViolated, PolicyRuleView.class);
            rules.add(ruleViolated);
        }
        return rules;
    }

    protected List<PolicyRuleView> getMatchingRules(final List<PolicyRuleView> rulesViolated) throws IntegrationException {
        final List<PolicyRuleView> filteredRules = new ArrayList<>();
        if (policyFilter != null && policyFilter.getRuleLinksToInclude() != null) {
            if (rulesViolated != null) {
                for (final PolicyRuleView ruleViolated : rulesViolated) {
                    final String ruleHref = getMetaService().getHref(ruleViolated);
                    if (policyFilter.getRuleLinksToInclude().contains(ruleHref)) {
                        filteredRules.add(ruleViolated);
                    }
                }
            } else {
                return rulesViolated;
            }
        } else {
            return rulesViolated;
        }
        return filteredRules;
    }

    protected PolicyNotificationFilter getPolicyFilter() {
        return policyFilter;
    }

    protected PolicyRuleView getPolicyRule(final String ruleUrl) throws IntegrationException {
        final PolicyRuleView rule = getPolicyService().getView(ruleUrl, PolicyRuleView.class);
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

    protected BomComponentPolicyStatusView getBomComponentVersionPolicyStatus(final String policyStatusUrl) throws IntegrationException {
        BomComponentPolicyStatusView bomComponentVersionPolicyStatus;
        bomComponentVersionPolicyStatus = getHubResponseService().getView(policyStatusUrl, BomComponentPolicyStatusView.class);

        return bomComponentVersionPolicyStatus;
    }

    public abstract void createContents(final ProjectVersionModel projectVersion, final String componentName,
            final ComponentVersionView componentVersion, final String componentUrl, final String componentVersionUrl,
            List<PolicyRuleView> policyRuleList,
            NotificationView item, List<NotificationContentItem> templateData, final String componentIssueUrl) throws URISyntaxException;
}
