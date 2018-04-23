package com.blackducksoftware.integration.hub.notification.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentView;
import com.blackducksoftware.integration.hub.api.generated.view.IssueView;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.util.Stringable;

public class NotificationContentDetail extends Stringable {
    private final String projectName;
    private final String projectVersionName;
    private final Optional<UriSingleResponse<ProjectVersionView>> projectVersion;

    private final Optional<String> componentName;
    private final Optional<UriSingleResponse<ComponentView>> component;

    private final Optional<String> componentVersionName;
    private final Optional<UriSingleResponse<ComponentVersionView>> componentVersion;

    private final Optional<String> policyName;
    private final Optional<UriSingleResponse<PolicyRuleView>> policy;

    private final Optional<String> componentVersionOriginName;
    private final Optional<UriSingleResponse<IssueView>> componentIssue;

    public static NotificationContentDetail createPolicyDetailWithComponentOnly(final String projectName, final String projectVersionName, final String projectVersionUri, final String componentName, final String componentUri,
            final String policyName, final String policyUri) {
        return new NotificationContentDetail(projectName, projectVersionName, projectVersion(projectVersionUri), Optional.of(componentName), component(componentUri), Optional.empty(), Optional.empty(), Optional.of(policyName),
                policy(policyUri), Optional.empty(), Optional.empty());
    }

    public static NotificationContentDetail createPolicyDetailWithComponentVersion(final String projectName, final String projectVersionName, final String projectVersionUri, final String componentName, final String componentVersionName,
            final String componentVersionUri, final String policyName, final String policyUri) {
        return new NotificationContentDetail(projectName, projectVersionName, projectVersion(projectVersionUri), Optional.of(componentName), Optional.empty(), Optional.of(componentVersionName), componentVersion(componentVersionUri),
                Optional.of(policyName), policy(policyUri), Optional.empty(), Optional.empty());
    }

    public static NotificationContentDetail createVulnerabilityDetail(final String projectName, final String projectVersionName, final String projectVersionUri, final String componentName, final String componentVersionName,
            final String componentVersionUri, final String componentVersionOriginName, final String componentIssueUri) {
        return new NotificationContentDetail(projectName, projectVersionName, projectVersion(projectVersionUri), Optional.of(componentName), Optional.empty(), Optional.of(componentVersionName), componentVersion(componentVersionUri),
                Optional.empty(), Optional.empty(), Optional.of(componentVersionOriginName), componentIssue(componentIssueUri));
    }

    private NotificationContentDetail(final String projectName, final String projectVersionName, final Optional<UriSingleResponse<ProjectVersionView>> projectVersion, final Optional<String> componentName,
            final Optional<UriSingleResponse<ComponentView>> component, final Optional<String> componentVersionName, final Optional<UriSingleResponse<ComponentVersionView>> componentVersion, final Optional<String> policyName,
            final Optional<UriSingleResponse<PolicyRuleView>> policy, final Optional<String> componentVersionOriginName, final Optional<UriSingleResponse<IssueView>> componentIssue) {
        this.projectName = projectName;
        this.projectVersionName = projectVersionName;
        this.projectVersion = projectVersion;
        this.componentName = componentName;
        this.component = component;
        this.componentVersionName = componentVersionName;
        this.componentVersion = componentVersion;
        this.policyName = policyName;
        this.policy = policy;
        this.componentVersionOriginName = componentVersionOriginName;
        this.componentIssue = componentIssue;
    }

    public boolean hasComponentVersion() {
        return componentVersion.isPresent();
    }

    public boolean hasOnlyComponent() {
        return component.isPresent();
    }

    public boolean hasPolicy() {
        return policy.isPresent();
    }

    public boolean hasVulnerability() {
        return componentIssue.isPresent();
    }

    public List<UriSingleResponse<? extends HubResponse>> getPresentLinks() {
        final List<UriSingleResponse<? extends HubResponse>> presentLinks = new ArrayList<>();
        if (projectVersion.isPresent()) {
            presentLinks.add(projectVersion.get());
        }
        if (component.isPresent()) {
            presentLinks.add(component.get());
        }
        if (componentVersion.isPresent()) {
            presentLinks.add(componentVersion.get());
        }
        if (policy.isPresent()) {
            presentLinks.add(policy.get());
        }
        if (componentIssue.isPresent()) {
            presentLinks.add(componentIssue.get());
        }
        return presentLinks;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersionName() {
        return projectVersionName;
    }

    public Optional<UriSingleResponse<ProjectVersionView>> getProjectVersion() {
        return projectVersion;
    }

    public Optional<String> getComponentName() {
        return componentName;
    }

    public Optional<UriSingleResponse<ComponentView>> getComponent() {
        return component;
    }

    public Optional<String> getComponentVersionName() {
        return componentVersionName;
    }

    public Optional<UriSingleResponse<ComponentVersionView>> getComponentVersion() {
        return componentVersion;
    }

    public Optional<String> getPolicyName() {
        return policyName;
    }

    public Optional<UriSingleResponse<PolicyRuleView>> getPolicy() {
        return policy;
    }

    public Optional<String> getComponentVersionOriginName() {
        return componentVersionOriginName;
    }

    public Optional<UriSingleResponse<IssueView>> getComponentIssue() {
        return componentIssue;
    }

    // private methods to assist in static building NotificationContentDetail instances
    private static Optional<UriSingleResponse<ProjectVersionView>> projectVersion(final String projectVersionUri) {
        return optional(projectVersionUri, ProjectVersionView.class);
    }

    private static Optional<UriSingleResponse<ComponentView>> component(final String componentUri) {
        return optional(componentUri, ComponentView.class);
    }

    private static Optional<UriSingleResponse<ComponentVersionView>> componentVersion(final String componentVersionUri) {
        return optional(componentVersionUri, ComponentVersionView.class);
    }

    private static Optional<UriSingleResponse<PolicyRuleView>> policy(final String policyUri) {
        return optional(policyUri, PolicyRuleView.class);
    }

    private static Optional<UriSingleResponse<IssueView>> componentIssue(final String componentIssueUri) {
        return optional(componentIssueUri, IssueView.class);
    }

    private static <T extends HubResponse> Optional<UriSingleResponse<T>> optional(final String uri, final Class<T> responseClass) {
        if (StringUtils.isBlank(uri)) {
            return Optional.empty();
        }
        return Optional.of(new UriSingleResponse<T>(uri, responseClass));
    }

}
