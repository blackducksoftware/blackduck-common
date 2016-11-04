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
package com.blackducksoftware.integration.hub.api.project.version;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.blackducksoftware.integration.util.ResourceUtil;
import com.google.gson.Gson;

public class ProjectVersionTest {

    @Test
    public void testPopulatingScanSummaryItemFromJson() throws IOException {
        final InputStream inputStream = ResourceUtil.getResourceAsStream(
                "com/blackducksoftware/integration/hub/api/project/version/ProjectVersionItemJson.txt");
        final String json = IOUtils.toString(inputStream, "UTF-8");

        final Gson gson = new Gson();
        final ProjectVersionItem releaseItem = gson.fromJson(json, ProjectVersionItem.class);
        assertNotNull(releaseItem.getReleasedOn());

        final Calendar createdAt = Calendar.getInstance();
        createdAt.setTime(releaseItem.getReleasedOn());
        assertEquals(3, createdAt.get(Calendar.DAY_OF_MONTH));
        assertEquals(Calendar.NOVEMBER, createdAt.get(Calendar.MONTH));
        assertEquals(2016, createdAt.get(Calendar.YEAR));

    }
}
