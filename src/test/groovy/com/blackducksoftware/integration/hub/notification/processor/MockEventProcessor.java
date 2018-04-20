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
package com.blackducksoftware.integration.hub.notification.processor;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.ItemTypeEnum;
import com.blackducksoftware.integration.hub.notification.MapProcessorCache;
import com.blackducksoftware.integration.hub.notification.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.notification.NotificationContentItem;
import com.blackducksoftware.integration.hub.notification.NotificationEvent;
import com.blackducksoftware.integration.hub.notification.NotificationEventConstants;
import com.blackducksoftware.integration.hub.notification.NotificationSubProcessor;
import com.blackducksoftware.integration.hub.notification.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.notification.PolicyViolationClearedContentItem;
import com.blackducksoftware.integration.hub.notification.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.notification.VulnerabilityContentItem;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilitySourceQualifiedId;

public class MockEventProcessor extends NotificationSubProcessor {
    private final Logger logger = LoggerFactory.getLogger(MockEventProcessor.class);

    public MockEventProcessor(final MapProcessorCache cache, final MetaHandler metaService) {
        super(cache, metaService);
    }

    @Override
    public void process(final NotificationContentItem notification) {
        // because of inheritance the order of the PolicyViolationContentItem matters it must be last since it is a base
        // class for the other types.
        try {
            if (notification instanceof PolicyOverrideContentItem) {
                final PolicyOverrideContentItem policyOverrideContentItem = (PolicyOverrideContentItem) notification;
                handlePolicyOverride(policyOverrideContentItem);
            } else if (notification instanceof PolicyViolationClearedContentItem) {
                final PolicyViolationClearedContentItem policyViolationCleared = (PolicyViolationClearedContentItem) notification;
                handlePolicyCleared(policyViolationCleared);
            } else if (notification instanceof PolicyViolationContentItem) {
                final PolicyViolationContentItem policyViolationContentItem = (PolicyViolationContentItem) notification;
                handlePolicyViolation(policyViolationContentItem);
            } else if (notification instanceof VulnerabilityContentItem) {
                final VulnerabilityContentItem vulnerabilityContentItem = (VulnerabilityContentItem) notification;
                handleVulnerability(vulnerabilityContentItem);
            }
        } catch (final HubIntegrationException ex) {
            logger.error("error processing event", ex);
        }
    }

    private void handlePolicyViolation(final PolicyViolationContentItem policyViolationContentItem) throws HubIntegrationException {
        for (final PolicyRuleView rule : policyViolationContentItem.getPolicyRuleList()) {
            final String eventKey = generatePolicyEventKey(policyViolationContentItem, rule);
            final Map<String, Object> dataSet = generatePolicyDataSet(policyViolationContentItem, rule);
            final NotificationEvent event = new NotificationEvent(eventKey, NotificationCategoryEnum.POLICY_VIOLATION, dataSet);
            getCache().addEvent(event);
        }
    }

    private void handlePolicyOverride(final PolicyOverrideContentItem policyOverrideContentItem) throws HubIntegrationException {
        for (final PolicyRuleView rule : policyOverrideContentItem.getPolicyRuleList()) {
            final String eventKey = generatePolicyEventKey(policyOverrideContentItem, rule);
            final Map<String, Object> dataSet = generatePolicyOverrideDataSet(policyOverrideContentItem, rule);
            final NotificationEvent event = new NotificationEvent(eventKey, NotificationCategoryEnum.POLICY_VIOLATION,
                    dataSet);
            if (getCache().hasEvent(event.getEventKey())) {
                getCache().removeEvent(event);
            } else {
                event.setCategoryType(NotificationCategoryEnum.POLICY_VIOLATION_OVERRIDE);
                getCache().addEvent(event);
            }
        }
    }

    private void handlePolicyCleared(final PolicyViolationClearedContentItem policyViolationCleared) throws HubIntegrationException {
        for (final PolicyRuleView rule : policyViolationCleared.getPolicyRuleList()) {
            final String eventKey = generatePolicyEventKey(policyViolationCleared, rule);
            final Map<String, Object> dataSet = generatePolicyDataSet(policyViolationCleared, rule);
            final NotificationEvent event = new NotificationEvent(eventKey, NotificationCategoryEnum.POLICY_VIOLATION, dataSet);
            if (getCache().hasEvent(event.getEventKey())) {
                getCache().removeEvent(event);
            } else {
                event.setCategoryType(NotificationCategoryEnum.POLICY_VIOLATION_CLEARED);
                getCache().addEvent(event);
            }
        }
    }

    private void handleVulnerability(final VulnerabilityContentItem vulnerabilityContentItem) {
        final List<VulnerabilitySourceQualifiedId> addedVulnList = vulnerabilityContentItem.getAddedVulnList();
        final List<VulnerabilitySourceQualifiedId> updatedVulnList = vulnerabilityContentItem.getUpdatedVulnList();
        final List<VulnerabilitySourceQualifiedId> deletedVulnList = vulnerabilityContentItem.getDeletedVulnList();

        if (addedVulnList != null && !addedVulnList.isEmpty()) {
            getCache().addEvent(createEvent(vulnerabilityContentItem, getVulnerabilityIds(addedVulnList)));
        }

        if (updatedVulnList != null && !updatedVulnList.isEmpty()) {
            getCache().addEvent(createEvent(vulnerabilityContentItem, getVulnerabilityIds(updatedVulnList)));
        }

        if (deletedVulnList != null && !deletedVulnList.isEmpty()) {
            getCache().removeEvent(createEvent(vulnerabilityContentItem, getVulnerabilityIds(deletedVulnList)));
        }
    }

    private NotificationEvent createEvent(final VulnerabilityContentItem vulnerabilityContent,
            final Set<String> vulnerabilityIdList) {
        final String eventKey = generateVulnerabilityEventKey(vulnerabilityContent);
        final Map<String, Object> dataSet = generateVulnerabilityDataSet(vulnerabilityContent);
        return new NotificationEvent(eventKey, NotificationCategoryEnum.VULNERABILITY, dataSet);
    }

    private Set<String> getVulnerabilityIds(final List<VulnerabilitySourceQualifiedId> itemList) {
        final Set<String> set = new HashSet<>();
        for (final VulnerabilitySourceQualifiedId item : itemList) {
            set.add(item.vulnerabilityId);
        }

        return set;
    }

    private String generatePolicyEventKey(final PolicyViolationContentItem content, final PolicyRuleView rule) throws HubIntegrationException {
        final StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_ISSUE_TYPE_NAME);
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_ISSUE_TYPE_VALUE_POLICY);
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(NotificationEventConstants.EVENT_KEY_HUB_PROJECT_VERSION_REL_URL_HASHED_NAME);
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(content.getProjectVersion().getUrl()));
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(NotificationEventConstants.EVENT_KEY_HUB_COMPONENT_REL_URL_HASHED_NAME);
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(content.getComponentUrl()));
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(NotificationEventConstants.EVENT_KEY_HUB_COMPONENT_VERSION_REL_URL_HASHED_NAME);
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(content.getComponentVersionUrl()));
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(NotificationEventConstants.EVENT_KEY_HUB_POLICY_RULE_REL_URL_HASHED_NAME);
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(getMetaHandler().getHref(rule)));
        final String key = keyBuilder.toString();
        return key;
    }

    private String generateVulnerabilityEventKey(final VulnerabilityContentItem content) {
        final StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_ISSUE_TYPE_NAME);
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_ISSUE_TYPE_VALUE_VULNERABILITY);
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(NotificationEventConstants.EVENT_KEY_HUB_PROJECT_VERSION_REL_URL_HASHED_NAME);
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(content.getProjectVersion().getUrl()));
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(NotificationEventConstants.EVENT_KEY_HUB_COMPONENT_REL_URL_HASHED_NAME);
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(""); // There is never a component URL
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(NotificationEventConstants.EVENT_KEY_HUB_COMPONENT_VERSION_REL_URL_HASHED_NAME);
        keyBuilder.append(NotificationEventConstants.EVENT_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(content.getComponentVersionUrl()));

        final String key = keyBuilder.toString();
        return key;
    }

    private Map<String, Object> generatePolicyDataSet(final PolicyViolationContentItem content, final PolicyRuleView rule) {
        final Map<String, Object> dataSet = new LinkedHashMap<>(4);
        dataSet.put(ItemTypeEnum.RULE.name(), rule.name);
        dataSet.put(ItemTypeEnum.COMPONENT.name(), content.getComponentName());
        dataSet.put(ItemTypeEnum.VERSION.name(), content.getComponentVersion().versionName);
        return dataSet;
    }

    private Map<String, Object> generatePolicyOverrideDataSet(final PolicyOverrideContentItem content, final PolicyRuleView rule) {
        final Map<String, Object> dataSet = generatePolicyDataSet(content, rule);
        final String person = StringUtils.join(" ", content.getFirstName(), content.getLastName());
        dataSet.put(ItemTypeEnum.PERSON.name(), person);

        return dataSet;
    }

    private Map<String, Object> generateVulnerabilityDataSet(final VulnerabilityContentItem vulnerabilityContent) {
        final Map<String, Object> dataSet = new LinkedHashMap<>();
        dataSet.put(ItemTypeEnum.COMPONENT.name(), vulnerabilityContent.getComponentName());
        dataSet.put(ItemTypeEnum.VERSION.name(), vulnerabilityContent.getComponentVersion().versionName);
        return dataSet;
    }

    @Override
    public String generateEventKey(final Map<String, Object> dataMap) throws HubIntegrationException {
        return ""; // ignore since we create multiple types of events from this processor
    }

    @Override
    public Map<String, Object> generateDataSet(final Map<String, Object> inputData) {
        return Collections.emptyMap();
    }
}
