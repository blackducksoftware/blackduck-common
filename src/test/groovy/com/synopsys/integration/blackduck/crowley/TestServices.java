package com.synopsys.integration.blackduck.crowley;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.util.IntEnvironmentVariables;

public class TestServices {
    public static void main(String[] args) throws IntegrationException {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newBuilder();
        BlackDuckServerConfig.newBuilder();
        blackDuckServerConfigBuilder.setUrl("https://int-hub03.dc1.lan");
        blackDuckServerConfigBuilder.setUsername("sysadmin");
        blackDuckServerConfigBuilder.setPassword("blackduck");
        blackDuckServerConfigBuilder.setTrustCert(true);

        BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.build();

        IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
        BlackDuckServicesFactory blackDuckServicesFactory = blackDuckServerConfig.createBlackDuckServicesFactory(logger);
        ProjectService projectService = blackDuckServicesFactory.createProjectService();
        List<ProjectView> allProjects = projectService.getAllProjects();

        // With for loop
        for (ProjectView project : allProjects) {
            List<ProjectVersionView> allVersions = projectService.getAllProjectVersions(project); // I added this to ProjectService
            int numVersions = allVersions.size();
            String projectTier = project.getProjectTier() != null ? String.valueOf(project.getProjectTier()) : "sub amazing";
            System.out.println(project.getName() + " " + projectTier + " " + numVersions);
            for (ProjectVersionView version : allVersions) {
                System.out.println("\t" + version.getVersionName());
            }
        }

        // with stream...
    }

}
