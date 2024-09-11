/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.http.transform.subclass;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.manual.view.BomEditNotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.ComponentUnknownVersionNotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.LicenseLimitNotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.NotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.PolicyOverrideNotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.ProjectNotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.ProjectVersionNotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationClearedNotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationNotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.UnknownNotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.VersionBomCodeLocationBomComputedNotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.VulnerabilityNotificationUserView;

public class NotificationUserViewSubclassResolver extends BlackDuckResponseSubclassResolver<NotificationUserView> {
    public NotificationUserViewSubclassResolver(Gson gson) {
        super(gson, NotificationUserView.class);
    }

    @Override
    public Class<? extends BlackDuckResponse> resolveSubclass(JsonElement jsonElement) {
        // Note: If you are supporting new notification types then this class and NotificationViewSubclassResolver may need to be changed.
        SimpleNotificationView simpleNotificationView = gson.fromJson(jsonElement, SimpleNotificationView.class);
        switch (simpleNotificationView.type) {
            case BOM_EDIT:
                return BomEditNotificationUserView.class;
            case COMPONENT_UNKNOWN_VERSION:
                return ComponentUnknownVersionNotificationUserView.class;
            case LICENSE_LIMIT:
                return LicenseLimitNotificationUserView.class;
            case POLICY_OVERRIDE:
                return PolicyOverrideNotificationUserView.class;
            case PROJECT:
                return ProjectNotificationUserView.class;
            case PROJECT_VERSION:
                return ProjectVersionNotificationUserView.class;
            case RULE_VIOLATION_CLEARED:
                return RuleViolationClearedNotificationUserView.class;
            case RULE_VIOLATION:
                return RuleViolationNotificationUserView.class;
            case VERSION_BOM_CODE_LOCATION_BOM_COMPUTED:
                return VersionBomCodeLocationBomComputedNotificationUserView.class;
            case VULNERABILITY:
                return VulnerabilityNotificationUserView.class;
            default:
                return UnknownNotificationUserView.class;
        }
    }

}
