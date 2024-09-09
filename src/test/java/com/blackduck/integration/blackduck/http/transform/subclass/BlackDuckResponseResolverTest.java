package com.blackduck.integration.blackduck.http.transform.subclass;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.blackduck.integration.blackduck.http.transform.subclass.BlackDuckResponseResolver;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;
import com.synopsys.integration.blackduck.api.manual.view.ProjectVersionNotificationView;
import com.synopsys.integration.blackduck.api.manual.view.VulnerabilityNotificationView;

public class BlackDuckResponseResolverTest {
    @Test
    public void testGettingRuntimeClass() throws IOException {
        String projectVersionJson = IOUtils.toString(this.getClass().getResourceAsStream("/json/notifications/project_version.json"), StandardCharsets.UTF_8);
        String vulnerabilityJson = IOUtils.toString(this.getClass().getResourceAsStream("/json/notifications/vulnerability.json"), StandardCharsets.UTF_8);

        BlackDuckResponseResolver blackDuckResponseResolver = new BlackDuckResponseResolver(new Gson());
        NotificationView projectVersionNotificationView = blackDuckResponseResolver.resolve(projectVersionJson, NotificationView.class);
        NotificationView vulnerabilityNotificationView = blackDuckResponseResolver.resolve(vulnerabilityJson, NotificationView.class);

        assertTrue(projectVersionNotificationView instanceof ProjectVersionNotificationView);
        assertTrue(vulnerabilityNotificationView instanceof VulnerabilityNotificationView);
    }

}
