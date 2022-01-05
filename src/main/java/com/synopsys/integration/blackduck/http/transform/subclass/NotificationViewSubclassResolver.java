/*
 * blackduck-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http.transform.subclass;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.manual.view.BomEditNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.ComponentUnknownVersionNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.LicenseLimitNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.blackduck.api.manual.view.PolicyOverrideNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.ProjectNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.ProjectVersionNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationClearedNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.RuleViolationNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.UnknownNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.VersionBomCodeLocationBomComputedNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.VulnerabilityNotificationView;

public class NotificationViewSubclassResolver extends BlackDuckResponseSubclassResolver<NotificationView> {
    public NotificationViewSubclassResolver(Gson gson) {
        super(gson, NotificationView.class);
    }

    @Override
    public Class<? extends BlackDuckResponse> resolveSubclass(JsonElement jsonElement) {
        // Note: If you are supporting new notification types then this class and NotificationUserViewSubclassResolver may need to be changed.
        SimpleNotificationView simpleNotificationView = gson.fromJson(jsonElement, SimpleNotificationView.class);
        switch (simpleNotificationView.type) {
            case BOM_EDIT:
                return BomEditNotificationView.class;
            case COMPONENT_UNKNOWN_VERSION:
                return ComponentUnknownVersionNotificationView.class;
            case LICENSE_LIMIT:
                return LicenseLimitNotificationView.class;
            case POLICY_OVERRIDE:
                return PolicyOverrideNotificationView.class;
            case PROJECT:
                return ProjectNotificationView.class;
            case PROJECT_VERSION:
                return ProjectVersionNotificationView.class;
            case RULE_VIOLATION_CLEARED:
                return RuleViolationClearedNotificationView.class;
            case RULE_VIOLATION:
                return RuleViolationNotificationView.class;
            case VERSION_BOM_CODE_LOCATION_BOM_COMPUTED:
                return VersionBomCodeLocationBomComputedNotificationView.class;
            case VULNERABILITY:
                return VulnerabilityNotificationView.class;
            default:
                return UnknownNotificationView.class;
        }

    }
}
