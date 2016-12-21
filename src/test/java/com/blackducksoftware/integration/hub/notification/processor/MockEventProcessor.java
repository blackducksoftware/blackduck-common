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
package com.blackducksoftware.integration.hub.notification.processor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilitySourceQualifiedId;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.dataservice.notification.item.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.item.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.item.PolicyViolationClearedContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.item.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.item.VulnerabilityContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;
import com.blackducksoftware.integration.hub.notification.processor.event.PolicyEvent;
import com.blackducksoftware.integration.hub.notification.processor.event.PolicyOverrideEvent;
import com.blackducksoftware.integration.hub.notification.processor.event.VulnerabilityEvent;

public class MockEventProcessor extends NotificationSubProcessor<NotificationEvent<? extends NotificationContentItem>> {
    private final Logger logger = LoggerFactory.getLogger(MockEventProcessor.class);

    public MockEventProcessor(final MapProcessorCache<NotificationEvent<? extends NotificationContentItem>> cache, final MetaService metaService) {
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
        for (final PolicyRule rule : policyViolationContentItem.getPolicyRuleList()) {
            final PolicyEvent event = new PolicyEvent(NotificationCategoryEnum.POLICY_VIOLATION, policyViolationContentItem,
                    rule, getMetaService().getHref(rule));
            getCache().addEvent(event);
        }
    }

    private void handlePolicyOverride(final PolicyOverrideContentItem policyOverrideContentItem) throws HubIntegrationException {
        for (final PolicyRule rule : policyOverrideContentItem.getPolicyRuleList()) {
            final PolicyOverrideEvent event = new PolicyOverrideEvent(NotificationCategoryEnum.POLICY_VIOLATION,
                    policyOverrideContentItem, rule, getMetaService().getHref(rule));
            if (getCache().hasEvent(event.getEventKey())) {
                getCache().removeEvent(event);
            } else {
                event.setCategoryType(NotificationCategoryEnum.POLICY_VIOLATION_OVERRIDE);
                getCache().addEvent(event);
            }
        }
    }

    private void handlePolicyCleared(final PolicyViolationClearedContentItem policyViolationCleared) throws HubIntegrationException {
        for (final PolicyRule rule : policyViolationCleared.getPolicyRuleList()) {
            final PolicyEvent event = new PolicyEvent(NotificationCategoryEnum.POLICY_VIOLATION, policyViolationCleared, rule,
                    getMetaService().getHref(rule));
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

    private VulnerabilityEvent createEvent(VulnerabilityContentItem vulnerabilityContent,
            final Set<String> vulnerabilityIdList) {

        return new VulnerabilityEvent(NotificationCategoryEnum.VULNERABILITY, vulnerabilityContent, vulnerabilityIdList);
    }

    private Set<String> getVulnerabilityIds(final List<VulnerabilitySourceQualifiedId> itemList) {
        final Set<String> set = new HashSet<>();
        for (final VulnerabilitySourceQualifiedId item : itemList) {
            set.add(item.getVulnerabilityId());
        }

        return set;
    }

}
