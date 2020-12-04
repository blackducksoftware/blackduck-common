package com.synopsys.integration.blackduck.http.client.cache;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

@Tag("integration")
public class CachingHttpClientTestIT {
    private static final IntHttpClientTestHelper INT_HTTP_CLIENT_TEST_HELPER = new IntHttpClientTestHelper();
    private static BlackDuckServicesFactory blackDuckServicesFactory;
    private static List<ProjectView> projects;

    @BeforeAll
    public static void initializeTest() throws Exception {
        blackDuckServicesFactory = INT_HTTP_CLIENT_TEST_HELPER.createBlackDuckServicesFactory();
        projects = new ArrayList<>();
    }

    @Test
    public void testCache() throws IntegrationException {
        BlackDuckServerConfig blackDuckServerConfig = INT_HTTP_CLIENT_TEST_HELPER.getBlackDuckServerConfig();
        IntLogger intLogger = INT_HTTP_CLIENT_TEST_HELPER.createIntLogger();
        BlackDuckServicesFactory blackDuckServicesFactory = blackDuckServerConfig.createCachedBlackDuckServicesFactory(intLogger);
        ProjectService projectService = blackDuckServicesFactory.createProjectService();
        LocalTime startTimeBeforeCache = LocalTime.now();
        projectService.getAllProjects();
        LocalTime finishedTimeBeforeCache = LocalTime.now();

        LocalTime startTimeAfterCache = LocalTime.now();
        projectService.getAllProjects();
        LocalTime finishedTimeAfterCache = LocalTime.now();

        Duration duration = Duration.between(startTimeBeforeCache, finishedTimeBeforeCache);
        Duration durationAfterCache = Duration.between(startTimeAfterCache, finishedTimeAfterCache);
        assertTrue(0 < duration.compareTo(durationAfterCache));

    }

}
