package com.synopsys.integration.blackduck.comprehensive.recipe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

import com.blackducksoftware.bdio2.BdioMetadata;
import com.blackducksoftware.bdio2.model.Project;
import com.blackducksoftware.common.value.ProductList;
import com.synopsys.integration.bdio.graph.MutableDependencyGraph;
import com.synopsys.integration.bdio.graph.MutableMapDependencyGraph;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.blackduck.bdio2.Bdio2Document;
import com.synopsys.integration.blackduck.bdio2.Bdio2Factory;
import com.synopsys.integration.blackduck.bdio2.Bdio2Writer;
import com.synopsys.integration.blackduck.codelocation.bdio2upload.Bdio2UploadService;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;

public class Bdio2UploadRecipeTest {
    private final IntHttpClientTestHelper testHelper = new IntHttpClientTestHelper();

    @Test
    //    @Disabled
    public void uploadBdio2() throws IOException {
        // boilerplate blackduck config
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(testHelper.getIntegrationBlackDuckServerUrl());
        blackDuckServerConfigBuilder.setUsername(testHelper.getTestUsername());
        blackDuckServerConfigBuilder.setPassword(testHelper.getTestPassword());

        BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.build();
        IntLogger logger = new BufferedIntLogger();
        BlackDuckServicesFactory blackDuckServicesFactory = blackDuckServerConfig.createBlackDuckServicesFactory(logger);
        Bdio2UploadService bdio2UploadService = blackDuckServicesFactory.createBdio2UploadService();

        Bdio2Factory bdio2Factory = new Bdio2Factory();

        // create the bdio2 metadata
        String codeLocationName = "bdio2 code location junit";
        ZonedDateTime now = Instant.now().atZone(ZoneId.of("EST5EDT"));
        BdioMetadata bdio2Metadata = bdio2Factory.createBdioMetadata(codeLocationName, now, new ProductList.Builder());

        // create the bdio2 project
        String projectName = "blackduck-common";
        String projectVersionName = "test";
        ExternalIdFactory externalIdFactory = new ExternalIdFactory();
        ExternalId externalId = externalIdFactory.createMavenExternalId("com.synopsys.integration", projectName, projectVersionName);
        Project bdio2Project = bdio2Factory.createProject(externalId, projectName, projectVersionName);

        // create a graph of one dependency
        String commonsLangGroup = "org.apache.commons";
        String commonsLangArtifact = "commons-lang3";
        String commonsLangVersion = "3.11";
        ExternalId commonsLangId = externalIdFactory.createMavenExternalId(commonsLangGroup, commonsLangArtifact, commonsLangVersion);
        Dependency dependency = new Dependency(commonsLangArtifact, commonsLangVersion, commonsLangId);

        MutableDependencyGraph dependencyGraph = new MutableMapDependencyGraph();
        dependencyGraph.addChildToRoot(dependency);

        // now, with metadata, a project, and a graph, we can create a bdio2 document
        Bdio2Document bdio2Document = bdio2Factory.createBdio2Document(bdio2Metadata, bdio2Project, dependencyGraph);

        File bdio2File = new File("/Users/ekerwin/working/bdio2_file.bdio");
        bdio2File.createNewFile();

        Bdio2Writer bdio2Writer = new Bdio2Writer();
        bdio2Writer.writeBdioDocument(new FileOutputStream(bdio2File), bdio2Document);
    }

}
