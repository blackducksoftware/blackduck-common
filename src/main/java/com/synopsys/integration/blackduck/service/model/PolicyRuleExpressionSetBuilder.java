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
package com.synopsys.integration.blackduck.service.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.synopsys.integration.blackduck.api.enumeration.PolicyRuleComponentUsageValueSetType;
import com.synopsys.integration.blackduck.api.enumeration.PolicyRuleConditionOperatorType;
import com.synopsys.integration.blackduck.api.enumeration.PolicyRuleConditionType;
import com.synopsys.integration.blackduck.api.enumeration.ReviewStatusType;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionParameter;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionSetView;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionView;
import com.synopsys.integration.blackduck.api.generated.enumeration.LicenseCodeSharingType;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyRuleExpressionSetOperatorType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionDistributionType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionPhaseType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentView;
import com.synopsys.integration.blackduck.api.generated.view.LicenseView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.view.MetaHandler;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.rest.RestConstants;

public class PolicyRuleExpressionSetBuilder {
    private final List<PolicyRuleExpressionView> expressions = new ArrayList<>();
    private final MetaHandler metaHandler;

    public PolicyRuleExpressionSetBuilder(final MetaHandler metaHandler) {
        this.metaHandler = metaHandler;
    }

    public void addProjectCondition(final PolicyRuleConditionOperatorType policyRuleConditionOperator, final ProjectView projectView) throws HubIntegrationException {
        addSingleCondition(policyRuleConditionOperator, PolicyRuleConditionType.PROJECT_NAME, metaHandler.getHref(projectView));
    }

    public void addComponentVersionCondition(final PolicyRuleConditionOperatorType policyRuleConditionOperator, final ComponentVersionView componentVersionView) throws HubIntegrationException {
        addSingleCondition(policyRuleConditionOperator, PolicyRuleConditionType.SINGLE_VERSION, metaHandler.getHref(componentVersionView));
    }

    public void addComponentCondition(final PolicyRuleConditionOperatorType policyRuleConditionOperator, final ComponentView componentView) throws HubIntegrationException {
        addSingleCondition(policyRuleConditionOperator, PolicyRuleConditionType.SINGLE_VERSION, metaHandler.getHref(componentView));
    }

    public void addLicenseCondition(final PolicyRuleConditionOperatorType policyRuleConditionOperator, final LicenseView licenseView) throws HubIntegrationException {
        addSingleCondition(policyRuleConditionOperator, PolicyRuleConditionType.SINGLE_LICENSE, metaHandler.getHref(licenseView));
    }

    public void addReviewStatusCondition(final PolicyRuleConditionOperatorType policyRuleConditionOperator, final ReviewStatusType reviewType) throws HubIntegrationException {
        addSingleObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.REVIEW_STATUS, reviewType);
    }

    public void addComponentReleaseDateCondition(final PolicyRuleConditionOperatorType policyRuleConditionOperator, final Date date) throws HubIntegrationException {
        addSingleCondition(policyRuleConditionOperator, PolicyRuleConditionType.RELEASE_DATE, RestConstants.formatDate(date));
    }

    public void addNewerVersionCondition(final PolicyRuleConditionOperatorType policyRuleConditionOperator, final Integer count) throws HubIntegrationException {
        addSingleObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.NEWER_VERSIONS_COUNT, count);
    }

    public void addHighSeverityVulnerabilityCondition(final PolicyRuleConditionOperatorType policyRuleConditionOperator, final Integer count) throws HubIntegrationException {
        addSingleObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.HIGH_SEVERITY_VULN_COUNT, count);
    }

    public void addMediumSeverityVulnerabilityCondition(final PolicyRuleConditionOperatorType policyRuleConditionOperator, final Integer count) throws HubIntegrationException {
        addSingleObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.MEDIUM_SEVERITY_VULN_COUNT, count);
    }

    public void addLowSeverityVulnerabilityCondition(final PolicyRuleConditionOperatorType policyRuleConditionOperator, final Integer count) throws HubIntegrationException {
        addSingleObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.LOW_SEVERITY_VULN_COUNT, count);
    }

    public void addProjectTierCondition(final PolicyRuleConditionOperatorType policyRuleConditionOperator, final List<Integer> tiers) throws HubIntegrationException {
        addMultiObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.PROJECT_TIER, tiers);
    }

    public void addPhaseCondition(final PolicyRuleConditionOperatorType policyRuleConditionOperator, final List<ProjectVersionPhaseType> projectVersionPhaseTypes) throws HubIntegrationException {
        addMultiObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.VERSION_PHASE, projectVersionPhaseTypes);
    }

    public void addDistributionCondition(final PolicyRuleConditionOperatorType policyRuleConditionOperator, final List<ProjectVersionDistributionType> projectVersionDistributionTypes) throws HubIntegrationException {
        addMultiObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.VERSION_DISTRIBUTION, projectVersionDistributionTypes);
    }

    public void addComponentUsageCondition(final PolicyRuleConditionOperatorType policyRuleConditionOperator, final List<PolicyRuleComponentUsageValueSetType> componentUsageTypes) throws HubIntegrationException {
        addMultiObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.COMPONENT_USAGE, componentUsageTypes);
    }

    public void addLicenseFamilyCondition(final PolicyRuleConditionOperatorType policyRuleConditionOperator, final List<LicenseCodeSharingType> licenseCodeSharingTypes) throws HubIntegrationException {
        addMultiObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.LICENSE_FAMILY, licenseCodeSharingTypes);
    }

    public void addSingleObjectCondition(final PolicyRuleConditionOperatorType policyRuleConditionOperator, final PolicyRuleConditionType policyRuleConditionType, final Object object) throws HubIntegrationException {
        final List<String> values = new ArrayList<>(1);
        values.add(object.toString());
        addMultiCondition(policyRuleConditionOperator, PolicyRuleConditionType.PROJECT_NAME, values);
    }

    public void addMultiObjectCondition(final PolicyRuleConditionOperatorType policyRuleConditionOperator, final PolicyRuleConditionType policyRuleConditionType, final List<?> objectValues) throws HubIntegrationException {
        final List<String> values = new ArrayList<>(objectValues.size());
        for (final Object object : objectValues) {
            values.add(object.toString());
        }
        addMultiCondition(policyRuleConditionOperator, PolicyRuleConditionType.PROJECT_NAME, values);
    }

    public void addSingleCondition(final PolicyRuleConditionOperatorType policyRuleConditionOperator, final PolicyRuleConditionType policyRuleConditionType, final String value) throws HubIntegrationException {
        final List<String> values = new ArrayList<>(1);
        values.add(value);
        addMultiCondition(policyRuleConditionOperator, policyRuleConditionType, values);
    }

    public void addMultiCondition(final PolicyRuleConditionOperatorType policyRuleConditionOperator, final PolicyRuleConditionType policyRuleConditionType, final List<String> values) throws HubIntegrationException {
        final PolicyRuleExpressionParameter expressionParameter = new PolicyRuleExpressionParameter();
        expressionParameter.values = values;
        final PolicyRuleExpressionView expression = new PolicyRuleExpressionView();
        expression.name = policyRuleConditionType.toString();
        expression.operation = policyRuleConditionOperator.toString();
        expression.parameters = expressionParameter;
        expressions.add(expression);
    }

    public PolicyRuleExpressionSetView createPolicyRuleExpressionSetView() {
        return createPolicyRuleExpressionSetView(PolicyRuleExpressionSetOperatorType.AND);
    }

    public PolicyRuleExpressionSetView createPolicyRuleExpressionSetView(final PolicyRuleExpressionSetOperatorType expressionOperatorType) {
        final PolicyRuleExpressionSetView expressionSet = new PolicyRuleExpressionSetView();
        expressionSet.operator = expressionOperatorType;
        expressionSet.expressions = expressions;
        return expressionSet;
    }

}
