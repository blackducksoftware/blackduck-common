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
package com.blackducksoftware.integration.hub.dataservice.notification.model;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.notification.NotificationContentItem;
import com.blackducksoftware.integration.hub.service.model.ProjectVersionModel;

public class NotificationContentItemTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testEqualVersioned() {
        final Date createdAt = new Date();
        final ProjectVersionModel projectVersion = new ProjectVersionModel();
        projectVersion.setProjectName("projectName");
        projectVersion.setProjectVersionName("projectVersionName");
        final String componentName = "comp1";
        final ComponentVersionView componentVersion = Mockito.mock(ComponentVersionView.class);
        componentVersion.versionName = "componentVersionName";
        final String componentVersionUrl = "http://some.url.com";
        final String componentIssueUrl = "";

        final NotificationContentItem notif1 = new NotificationContentItem(createdAt, projectVersion,
                componentName,
                componentVersion,
                componentVersionUrl,
                componentIssueUrl);

        final NotificationContentItem notif2 = new NotificationContentItem(createdAt, projectVersion,
                componentName,
                componentVersion,
                componentVersionUrl,
                componentIssueUrl);

        assertEquals(0, notif1.compareTo(notif2));
    }

    @Test
    public void testEqualVersionless() {
        final Date createdAt = new Date();
        final ProjectVersionModel projectVersion = new ProjectVersionModel();
        projectVersion.setProjectName("projectName");
        projectVersion.setProjectVersionName("projectVersionName");
        final String componentName = "comp1";
        final ComponentVersionView componentVersion = null;
        final String componentVersionUrl = null;
        final String componentIssueUrl = "";

        final NotificationContentItem notif1 = new NotificationContentItem(createdAt, projectVersion,
                componentName,
                componentVersion,
                componentVersionUrl,
                componentIssueUrl);

        final NotificationContentItem notif2 = new NotificationContentItem(createdAt, projectVersion,
                componentName,
                componentVersion,
                componentVersionUrl,
                componentIssueUrl);

        assertEquals(0, notif1.compareTo(notif2));
    }

    @Test
    public void testUnequalVersionless() {
        final Date createdAt = new Date();
        final ProjectVersionModel projectVersion1 = new ProjectVersionModel();
        projectVersion1.setProjectName("projectName1");
        projectVersion1.setProjectVersionName("projectVersionName");
        final ProjectVersionModel projectVersion2 = new ProjectVersionModel();
        projectVersion2.setProjectName("projectName2");
        projectVersion2.setProjectVersionName("projectVersionName");
        final String componentName = "comp1";
        final ComponentVersionView componentVersion = null;
        final String componentVersionUrl = null;
        final String componentIssueUrl = "";

        final NotificationContentItem notif1 = new NotificationContentItem(createdAt, projectVersion1,
                componentName,
                componentVersion,
                componentVersionUrl,
                componentIssueUrl);

        final NotificationContentItem notif2 = new NotificationContentItem(createdAt, projectVersion2,
                componentName,
                componentVersion,
                componentVersionUrl,
                componentIssueUrl);

        assertEquals(-1, notif1.compareTo(notif2));
    }
}
