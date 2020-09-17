package com.synopsys.integration.blackduck.performance;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.exception.IntegrationException;

public class TokenExpireTest {
    private final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();

    @Test
    public void testExpiring() throws IntegrationException, InterruptedException {
        DateTimeFormatter eastern = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withZone(ZoneId.of("GMT-4"));
        BlackDuckServicesFactory blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        ProjectService projectService = blackDuckServicesFactory.createProjectService();

        Instant start = Instant.now();
        while (true) {
            Instant now = Instant.now();
            int projectCount = projectService.getAllProjects().size();
            String time = eastern.format(now);
            System.out.println(String.format("%s: (%s)", time, projectCount));

            //between 1 and 5 minutes
            int minutes = (int)(Math.random() * 5 + 1);
            System.out.println(String.format("...waiting %s minutes...", minutes));
            Thread.sleep(minutes * 60 * 1000);
        }
    }

}
