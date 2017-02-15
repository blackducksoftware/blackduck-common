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
package com.blackducksoftware.integration.hub.api.aggregate.bom;

import java.util.Date;

public class ComponentActivityData {
    private int contributorCount12Month;

    private int commitCount12Month;

    private Date lastCommitDate;

    private ComponentActivityTrendEnum trending;

    public int getContributorCount12Month() {
        return contributorCount12Month;
    }

    public int getCommitCount12Month() {
        return commitCount12Month;
    }

    public Date getLastCommitDate() {
        return lastCommitDate;
    }

    public ComponentActivityTrendEnum getTrending() {
        return trending;
    }

}
