/**
 * blackduck-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
import com.synopsys.integration.blackduck.api.generated.enumeration.CustomLicenseRequestCodeSharingType;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyRuleExpressionSetOperatorType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionDistributionType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionPhaseType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentView;
import com.synopsys.integration.blackduck.api.generated.view.LicenseView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.rest.RestConstants;

public class PolicyRuleExpressionSetBuilder {
    private final List<PolicyRuleExpressionView> expressions = new ArrayList<>();

    public void addProjectCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, ProjectView projectView) throws BlackDuckIntegrationException {
        addSingleCondition(policyRuleConditionOperator, PolicyRuleConditionType.PROJECT_NAME, projectView.getHref().orElse(null));
    }

    public void addComponentVersionCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, ComponentVersionView componentVersionView) throws BlackDuckIntegrationException {
        addSingleCondition(policyRuleConditionOperator, PolicyRuleConditionType.SINGLE_VERSION, componentVersionView.getHref().orElse(null));
    }

    public void addComponentCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, ComponentView componentView) throws BlackDuckIntegrationException {
        addSingleCondition(policyRuleConditionOperator, PolicyRuleConditionType.SINGLE_VERSION, componentView.getHref().orElse(null));
    }

    public void addLicenseCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, LicenseView licenseView) throws BlackDuckIntegrationException {
        addSingleCondition(policyRuleConditionOperator, PolicyRuleConditionType.SINGLE_LICENSE, licenseView.getHref().orElse(null));
    }

    public void addReviewStatusCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, ReviewStatusType reviewType) throws BlackDuckIntegrationException {
        addSingleObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.REVIEW_STATUS, reviewType);
    }

    public void addComponentReleaseDateCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, Date date) throws BlackDuckIntegrationException {
        addSingleCondition(policyRuleConditionOperator, PolicyRuleConditionType.RELEASE_DATE, RestConstants.formatDate(date));
    }

    public void addNewerVersionCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, Integer count) throws BlackDuckIntegrationException {
        addSingleObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.NEWER_VERSIONS_COUNT, count);
    }

    public void addHighSeverityVulnerabilityCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, Integer count) throws BlackDuckIntegrationException {
        addSingleObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.HIGH_SEVERITY_VULN_COUNT, count);
    }

    public void addMediumSeverityVulnerabilityCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, Integer count) throws BlackDuckIntegrationException {
        addSingleObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.MEDIUM_SEVERITY_VULN_COUNT, count);
    }

    public void addLowSeverityVulnerabilityCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, Integer count) throws BlackDuckIntegrationException {
        addSingleObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.LOW_SEVERITY_VULN_COUNT, count);
    }

    public void addProjectTierCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, List<Integer> tiers) throws BlackDuckIntegrationException {
        addMultiObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.PROJECT_TIER, tiers);
    }

    public void addPhaseCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, List<ProjectVersionPhaseType> projectVersionPhaseTypes) throws BlackDuckIntegrationException {
        addMultiObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.VERSION_PHASE, projectVersionPhaseTypes);
    }

    public void addDistributionCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, List<ProjectVersionDistributionType> projectVersionDistributionTypes) throws BlackDuckIntegrationException {
        addMultiObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.VERSION_DISTRIBUTION, projectVersionDistributionTypes);
    }

    public void addComponentUsageCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, List<PolicyRuleComponentUsageValueSetType> componentUsageTypes) throws BlackDuckIntegrationException {
        addMultiObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.COMPONENT_USAGE, componentUsageTypes);
    }

    public void addLicenseFamilyCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, List<CustomLicenseRequestCodeSharingType> licenseCodeSharingTypes) throws BlackDuckIntegrationException {
        addMultiObjectCondition(policyRuleConditionOperator, PolicyRuleConditionType.LICENSE_FAMILY, licenseCodeSharingTypes);
    }

    public void addSingleObjectCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, PolicyRuleConditionType policyRuleConditionType, Object object) throws BlackDuckIntegrationException {
        List<String> values = new ArrayList<>(1);
        values.add(object.toString());
        addMultiCondition(policyRuleConditionOperator, PolicyRuleConditionType.PROJECT_NAME, values);
    }

    public void addMultiObjectCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, PolicyRuleConditionType policyRuleConditionType, List<?> objectValues) throws BlackDuckIntegrationException {
        List<String> values = new ArrayList<>(objectValues.size());
        for (Object object : objectValues) {
            values.add(object.toString());
        }
        addMultiCondition(policyRuleConditionOperator, PolicyRuleConditionType.PROJECT_NAME, values);
    }

    public void addSingleCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, PolicyRuleConditionType policyRuleConditionType, String value) throws BlackDuckIntegrationException {
        List<String> values = new ArrayList<>(1);
        values.add(value);
        addMultiCondition(policyRuleConditionOperator, policyRuleConditionType, values);
    }

    public void addMultiCondition(PolicyRuleConditionOperatorType policyRuleConditionOperator, PolicyRuleConditionType policyRuleConditionType, List<String> values) throws BlackDuckIntegrationException {
        PolicyRuleExpressionParameter expressionParameter = new PolicyRuleExpressionParameter();
        expressionParameter.setValues(values);
        PolicyRuleExpressionView expression = new PolicyRuleExpressionView();
        expression.setName(policyRuleConditionType.toString());
        expression.setOperation(policyRuleConditionOperator.toString());
        expression.setParameters(expressionParameter);
        expressions.add(expression);
    }

    public PolicyRuleExpressionSetView createPolicyRuleExpressionSetView() {
        return createPolicyRuleExpressionSetView(PolicyRuleExpressionSetOperatorType.AND);
    }

    public PolicyRuleExpressionSetView createPolicyRuleExpressionSetView(PolicyRuleExpressionSetOperatorType expressionOperatorType) {
        PolicyRuleExpressionSetView expressionSet = new PolicyRuleExpressionSetView();
        expressionSet.setOperator(expressionOperatorType);
        expressionSet.setExpressions(expressions);
        return expressionSet;
    }

}
