import org.junit.Test;

import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;

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

public class OkHttpTest {

    @Test
    public void testConnection() throws Exception {
        HubServerConfigBuilder builder = new HubServerConfigBuilder(true);
        // builder.setHubUrl("http://int-hub01.dc1.lan:8080");
        builder.setHubUrl("https://hub-beta.blackducksoftware.com");
        builder.setUsername("sysadmin");
        builder.setPassword("blackduck");
        builder.setProxyHost("qasslproxy");
        builder.setProxyPort(3129);
        builder.setProxyUsername("bds");
        builder.setProxyPassword("blackduck");
        HubServerConfig config = builder.build();

        CredentialsRestConnection restConnection = new CredentialsRestConnection(config);
        restConnection.connect();

        ProjectRequestService requestService = new ProjectRequestService(restConnection);

        System.out.println(requestService.getAllProjects());

    }

}
