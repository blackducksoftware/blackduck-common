/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.api.view;

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusEnum;

public class BomComponentPolicyStatusView extends HubItem {
    private PolicyStatusEnum approvalStatus;

    public PolicyStatusEnum getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(final PolicyStatusEnum approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

}
