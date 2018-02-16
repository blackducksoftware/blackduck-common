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
package com.blackducksoftware.integration.hub.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import com.blackducksoftware.integration.IntegrationTest;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.response.CurrentVersionView;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.rest.TestingPropertyKey;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.test.TestLogger;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;

@Category(IntegrationTest.class)
public class CLIInstallerTestIT {
    private static final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final CIEnvironmentVariables ciEnvironmentVariables = new CIEnvironmentVariables();

    private File setupFakeCliStructure() throws Exception {
        folder.create();
        final File dirToInstallTo = folder.newFolder();
        final File cliInstallDir = new File(dirToInstallTo, CLILocation.CLI_UNZIP_DIR);
        final File cliUnzipDir = new File(cliInstallDir, "scan.cli");
        final File bin = new File(cliUnzipDir, "bin");
        bin.mkdirs();
        final File lib = new File(cliUnzipDir, "lib");
        final File cache = new File(lib, "cache");
        cache.mkdirs();
        final File oneJarFile = new File(cache, "scan.cli.impl-standalone.jar");
        oneJarFile.createNewFile();
        final File scanCli = new File(lib, "scan.cli.TEST.jar");
        scanCli.createNewFile();
        return dirToInstallTo;
    }

    private File setupFakeCliStructureWithJre() throws Exception {
        final File dirToInstallTo = setupFakeCliStructure();
        final File cliInstallDir = new File(dirToInstallTo, CLILocation.CLI_UNZIP_DIR);
        final File cliUnzipDir = new File(cliInstallDir, "scan.cli");
        final File jre = new File(cliUnzipDir, "jre");
        File bin = null;
        if (SystemUtils.IS_OS_MAC_OSX) {
            bin = new File(jre, "Contents");
            bin = new File(bin, "Home");
            bin = new File(bin, "bin");
        } else {
            bin = new File(jre, "bin");
        }
        bin.mkdirs();

        final File java;
        if (SystemUtils.IS_OS_WINDOWS) {
            java = new File(bin, "java.exe");
        } else {
            java = new File(bin, "java");
        }

        java.createNewFile();

        final File lib = new File(jre, "lib");
        lib.mkdirs();
        return dirToInstallTo;
    }

    @Test
    public void testConstructorNullLogger() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("You must provided a logger.");
        new CLILocation(null, null);
    }

    @Test
    public void testConstructorNull() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("You must provided a directory to install the CLI to.");
        new CLILocation(new TestLogger(), null);
    }

    @Test
    public void testConstructor() throws Exception {
        final File dir = folder.newFolder();
        final CLILocation cliLocation = new CLILocation(new TestLogger(), dir);

        final File unzipDir = new File(dir, CLILocation.CLI_UNZIP_DIR);
        assertEquals(unzipDir, cliLocation.getCLIInstallDir());

        assertNull(cliLocation.getCLIHome());

        final File cliHome = new File(unzipDir, "CLI_HOME");
        assertNull(cliLocation.getCLIHome());

        cliHome.mkdirs();
        assertEquals(cliHome, cliLocation.getCLIHome());
    }

    @Test
    public void testDeleteRecursively() throws Exception {
        final File installDir = setupFakeCliStructure();
        deleteFilesRecursive(installDir.listFiles());
        assertTrue(installDir.exists());
        assertTrue(installDir.listFiles().length == 0);
    }

    @Test
    public void testGetCLIHome() throws Exception {
        final File installDir = setupFakeCliStructure();
        final CLILocation cliLocation = new CLILocation(new TestLogger(), installDir);
        File file = new File(installDir, CLILocation.CLI_UNZIP_DIR);
        file = new File(file, "scan.cli");
        assertEquals(file.getAbsolutePath(), cliLocation.getCLIHome().getAbsolutePath());
        assertTrue(cliLocation.getCLIHome().exists());
    }

    @Test
    public void testGetCLIHomeEmpty() throws Exception {
        final File installDir = setupFakeCliStructure();
        final CLILocation cliLocation = new CLILocation(new TestLogger(), installDir);
        final File file = new File(installDir, CLILocation.CLI_UNZIP_DIR);
        deleteFilesRecursive(file.listFiles());
        assertNull(cliLocation.getCLIHome());
    }

    @Test
    public void testGetCLIHomeEmptyInstallDir() throws Exception {
        final CLILocation cliLocation = new CLILocation(new TestLogger(), folder.newFolder());
        assertNull(cliLocation.getCLIHome());
    }

    @Test
    public void testGetOneJarFile() throws Exception {
        final File installDir = setupFakeCliStructure();
        final CLILocation cliLocation = new CLILocation(new TestLogger(), installDir);
        File file = new File(installDir, CLILocation.CLI_UNZIP_DIR);
        file = new File(file, "scan.cli");
        file = new File(file, "lib");
        file = new File(file, "cache");
        file = new File(file, "scan.cli.impl-standalone.jar");
        assertEquals(file.getAbsolutePath(), cliLocation.getOneJarFile().getAbsolutePath());
        assertTrue(cliLocation.getOneJarFile().exists());
    }

    @Test
    public void testGetProvidedJavaExec() throws Exception {
        final File installDir = setupFakeCliStructureWithJre();
        final CLILocation cliLocation = new CLILocation(new TestLogger(), installDir);
        File file = new File(installDir, CLILocation.CLI_UNZIP_DIR);
        file = new File(file, "scan.cli");
        file = new File(file, "jre");
        if (SystemUtils.IS_OS_MAC_OSX) {
            file = new File(file, "Contents");
            file = new File(file, "Home");
        }
        file = new File(file, "bin");

        if (SystemUtils.IS_OS_WINDOWS) {
            file = new File(file, "java.exe");
        } else {
            file = new File(file, "java");
        }

        assertEquals(file.getAbsolutePath(), cliLocation.getProvidedJavaExec().getAbsolutePath());
        assertTrue(cliLocation.getProvidedJavaExec().exists());
    }

    @Test
    public void testGetProvidedJavaExecJavaDNE() throws Exception {
        final File installDir = setupFakeCliStructureWithJre();
        final CLILocation cliLocation = new CLILocation(new TestLogger(), installDir);
        File file = new File(installDir, CLILocation.CLI_UNZIP_DIR);
        file = new File(file, "scan.cli");
        file = new File(file, "jre");
        if (SystemUtils.IS_OS_MAC_OSX) {
            file = new File(file, "Contents");
            file = new File(file, "Home");
        }
        file = new File(file, "bin");
        if (SystemUtils.IS_OS_WINDOWS) {
            file = new File(file, "java.exe");
        } else {
            file = new File(file, "java");
        }
        file.delete();
        assertNull(cliLocation.getProvidedJavaExec());
    }

    @Test
    public void testGetProvidedJavaExecJavaNoBin() throws Exception {
        final File installDir = setupFakeCliStructureWithJre();
        final CLILocation cliLocation = new CLILocation(new TestLogger(), installDir);
        File file = new File(installDir, CLILocation.CLI_UNZIP_DIR);
        file = new File(file, "scan.cli");
        file = new File(file, "jre");
        if (SystemUtils.IS_OS_MAC_OSX) {
            file = new File(file, "Contents");
            file = new File(file, "Home");
        }
        deleteFilesRecursive(file.listFiles());
        assertNull(cliLocation.getProvidedJavaExec());
    }

    @Test
    public void testGetProvidedJavaExecJavaNoJreFolder() throws Exception {
        final File installDir = setupFakeCliStructure();
        final CLILocation cliLocation = new CLILocation(new TestLogger(), installDir);
        assertNull(cliLocation.getProvidedJavaExec());
    }

    @Test
    public void testGetProvidedJavaExecJavaNoInstallDir() throws Exception {
        final CLILocation cliLocation = new CLILocation(new TestLogger(), folder.newFolder());
        assertNull(cliLocation.getProvidedJavaExec());
    }

    @Test
    public void testGetCLI() throws Exception {
        final TestLogger logger = new TestLogger();
        final File installDir = setupFakeCliStructure();
        final CLILocation cliLocation = new CLILocation(logger, installDir);

        File file = new File(installDir, CLILocation.CLI_UNZIP_DIR);
        file = new File(file, "scan.cli");
        file = new File(file, "lib");
        file = new File(file, "scan.cli.TEST.jar");

        final File cli = cliLocation.getCLI(logger);
        assertNotNull(cli);
        assertEquals(file.getAbsolutePath(), cli.getAbsolutePath());
    }

    @Test
    public void testGetCliDNE() throws Exception {
        final TestLogger logger = new TestLogger();
        final File installDir = setupFakeCliStructure();
        final CLILocation cliLocation = new CLILocation(logger, installDir);

        File lib = new File(installDir, CLILocation.CLI_UNZIP_DIR);
        lib = new File(lib, "scan.cli");
        lib = new File(lib, "lib");
        final File file = new File(lib, "scan.cli.TEST.jar");
        file.delete();
        final File file2 = new File(lib, "test.txt");
        file2.createNewFile();

        // lib without scan cli file
        assertNull(cliLocation.getCLI(logger));

        file2.delete();
        // Now the lib folder is empty
        assertNull(cliLocation.getCLI(logger));
    }

    @Test
    public void testGetCliLibInvalid() throws Exception {
        final TestLogger logger = new TestLogger();
        final File installDir = setupFakeCliStructure();
        final CLILocation cliLocation = new CLILocation(logger, installDir);

        File lib = new File(installDir, CLILocation.CLI_UNZIP_DIR);
        lib = new File(lib, "scan.cli");
        lib = new File(lib, "lib");
        deleteFilesRecursive(lib.listFiles());

        assertNull(cliLocation.getCLI(logger));

        lib.delete();

        assertNull(cliLocation.getCLI(logger));
    }

    @Test
    public void testGetCliCliHomeEmpty() throws Exception {
        final TestLogger logger = new TestLogger();
        final File installDir = setupFakeCliStructure();
        final CLILocation cliLocation = new CLILocation(logger, installDir);

        File file = new File(installDir, CLILocation.CLI_UNZIP_DIR);
        file = new File(file, "scan.cli");
        deleteFilesRecursive(file.listFiles());

        assertNull(cliLocation.getCLI(logger));
    }

    @Test
    public void testGetCliCliHomeDNE() throws Exception {
        final TestLogger logger = new TestLogger();
        final File installDir = setupFakeCliStructure();
        final CLILocation cliLocation = new CLILocation(logger, installDir);

        final File file = new File(installDir, CLILocation.CLI_UNZIP_DIR);
        deleteFilesRecursive(file.listFiles());

        assertNull(cliLocation.getCLI(logger));
    }

    @Test
    public void testGetCliExists() throws Exception {
        final TestLogger logger = new TestLogger();
        final File installDir = setupFakeCliStructure();
        final CLILocation cliLocation = new CLILocation(logger, installDir);

        File file = new File(installDir, CLILocation.CLI_UNZIP_DIR);
        file = new File(file, "scan.cli");
        file = new File(file, "lib");
        file = new File(file, "scan.cli.TEST.jar");

        assertTrue(cliLocation.getCLIExists(logger));
        final String output = logger.getOutputString();
        assertTrue(output, output.contains("BlackDuck scan directory: "));
        assertTrue(output, output.contains("directories in the BlackDuck scan directory: "));
        assertTrue(output, output.contains("BlackDuck scan lib directory: "));
    }

    @Test
    public void testGetCliExistsDNE() throws Exception {
        TestLogger logger = new TestLogger();
        final File installDir = setupFakeCliStructure();
        final CLILocation cliLocation = new CLILocation(logger, installDir);

        File lib = new File(installDir, CLILocation.CLI_UNZIP_DIR);
        lib = new File(lib, "scan.cli");
        lib = new File(lib, "lib");
        final File file = new File(lib, "scan.cli.TEST.jar");
        file.delete();
        final File file2 = new File(lib, "test.txt");
        file2.createNewFile();

        // lib without scan cli file
        assertTrue(!cliLocation.getCLIExists(logger));
        String output = logger.getOutputString();
        assertTrue(output, output.contains("BlackDuck scan directory: "));
        assertTrue(output, output.contains("directories in the BlackDuck scan directory: "));
        assertTrue(output, output.contains("BlackDuck scan lib directory: "));

        file2.delete();
        // Now the lib folder is empty
        logger = new TestLogger();
        assertTrue(!cliLocation.getCLIExists(logger));
        output = logger.getOutputString();
        assertTrue(output, output.contains("BlackDuck scan directory: "));
        assertTrue(output, output.contains("directories in the BlackDuck scan directory: "));
        assertTrue(output, output.contains("BlackDuck scan lib directory: "));
    }

    @Test
    public void testGetCliExistsLibInvalid() throws Exception {
        TestLogger logger = new TestLogger();
        final File installDir = setupFakeCliStructure();
        final CLILocation cliLocation = new CLILocation(logger, installDir);

        File lib = new File(installDir, CLILocation.CLI_UNZIP_DIR);
        lib = new File(lib, "scan.cli");
        lib = new File(lib, "lib");
        deleteFilesRecursive(lib.listFiles());

        assertTrue(!cliLocation.getCLIExists(logger));
        String output = logger.getOutputString();
        assertTrue(output, output.contains("BlackDuck scan directory: "));
        assertTrue(output, output.contains("directories in the BlackDuck scan directory: "));
        assertTrue(output, output.contains("BlackDuck scan lib directory: "));

        lib.delete();

        logger = new TestLogger();
        assertTrue(!cliLocation.getCLIExists(logger));
        output = logger.getOutputString();
        assertTrue(output, output.contains("BlackDuck scan directory: "));
        assertTrue(output, output.contains("directories in the BlackDuck scan directory: "));
        assertTrue(output, output.contains("Could not find the lib directory of the CLI."));

    }

    @Test
    public void testGetCliExistsCliHomeEmpty() throws Exception {
        final TestLogger logger = new TestLogger();
        final File installDir = setupFakeCliStructure();
        final CLILocation cliLocation = new CLILocation(logger, installDir);

        File file = new File(installDir, CLILocation.CLI_UNZIP_DIR);
        file = new File(file, "scan.cli");
        deleteFilesRecursive(file.listFiles());

        assertTrue(!cliLocation.getCLIExists(logger));
        final String output = logger.getOutputString();
        assertTrue(output, output.contains("BlackDuck scan directory: "));
        assertTrue(output, output.contains("No files found in the BlackDuck scan directory."));
    }

    @Test
    public void testGetCliExistsCliHomeDNE() throws Exception {
        final TestLogger logger = new TestLogger();
        final File installDir = setupFakeCliStructure();
        final CLILocation cliLocation = new CLILocation(logger, installDir);

        final File file = new File(installDir, CLILocation.CLI_UNZIP_DIR);
        deleteFilesRecursive(file.listFiles());

        assertTrue(!cliLocation.getCLIExists(new TestLogger()));
    }

    @Test
    public void testPerformInstallationNullHost() throws Exception {
        exception.expect(IllegalArgumentException.class);
        final File installDir = folder.newFolder();

        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final URL hubUrl = hubServicesFactory.getRestConnection().baseUrl;

        final CLIDownloadUtility cliDownloadService = hubServicesFactory.createCliDownloadUtility();
        final CurrentVersionView currentVersion = hubServicesFactory.createHubService().getResponseFromPath(ApiDiscovery.CURRENT_VERSION_LINK_RESPONSE);

        cliDownloadService.performInstallation(installDir, ciEnvironmentVariables, hubUrl.toString(), currentVersion.version, null);
    }

    @Test
    public void testPerformInstallationUpdatingEmptyHost() throws Exception {
        exception.expect(IllegalArgumentException.class);
        final File installDir = folder.newFolder();

        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory();
        final URL hubUrl = hubServicesFactory.getRestConnection().baseUrl;

        final CurrentVersionView currentVersion = hubServicesFactory.createHubService().getResponseFromPath(ApiDiscovery.CURRENT_VERSION_LINK_RESPONSE);
        final CLIDownloadUtility cliDownloadService = hubServicesFactory.createCliDownloadUtility();
        final String hubVersion = currentVersion.version;

        cliDownloadService.performInstallation(installDir, ciEnvironmentVariables, hubUrl.toString(), hubVersion, "");
    }

    @Test
    public void testPerformInstallation() throws Exception {
        final TestLogger logger = new TestLogger();
        final File installDir = folder.newFolder();
        final CLILocation cliLocation = new CLILocation(logger, installDir);
        final HubServicesFactory hubServicesFactory = restConnectionTestHelper.createHubServicesFactory(logger);
        final URL hubUrl = hubServicesFactory.getRestConnection().baseUrl;

        final CurrentVersionView currentVersion = hubServicesFactory.createHubService().getResponseFromPath(ApiDiscovery.CURRENT_VERSION_LINK_RESPONSE);
        final CLIDownloadUtility cliDownloadService = hubServicesFactory.createCliDownloadUtility();
        final String hubVersion = currentVersion.version;
        cliDownloadService.performInstallation(installDir, ciEnvironmentVariables, hubUrl.toString(), hubVersion, "TestHost");

        final File file = new File(installDir, CLILocation.VERSION_FILE_NAME);

        assertTrue(file.exists());

        String output = logger.getOutputString();
        assertTrue(output, output.contains("Unpacking "));
        assertTrue(cliLocation.getCLIExists(hubServicesFactory.getRestConnection().logger));
        output = ((TestLogger) hubServicesFactory.getRestConnection().logger).getOutputString();
        assertTrue(output, output.contains("BlackDuck scan directory: "));
        assertTrue(output, output.contains("directories in the BlackDuck scan directory: "));
        assertTrue(output, output.contains("BlackDuck scan lib directory: "));

        assertNotNull(cliLocation.getCLI(hubServicesFactory.getRestConnection().logger));
    }

    @Test
    public void testPerformInstallationPassThroughProxy() throws Exception {
        final TestLogger logger = new TestLogger();
        final File installDir = folder.newFolder();
        final CLILocation cliLocation = new CLILocation(logger, installDir);

        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        builder.setHubUrl(restConnectionTestHelper.getProperty("TEST_HUB_SERVER_URL"));
        builder.setUsername(restConnectionTestHelper.getProperty("TEST_USERNAME"));
        builder.setPassword(restConnectionTestHelper.getProperty("TEST_PASSWORD"));
        builder.setTimeout(restConnectionTestHelper.getProperty("TEST_HUB_TIMEOUT"));
        builder.setProxyHost(restConnectionTestHelper.getProperty("TEST_PROXY_HOST_PASSTHROUGH"));
        builder.setProxyPort(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_PASSTHROUGH"));
        builder.setAlwaysTrustServerCertificate(Boolean.valueOf(restConnectionTestHelper.getProperty(TestingPropertyKey.TEST_TRUST_HTTPS_CERT)));

        final CredentialsRestConnection restConnection = restConnectionTestHelper.getRestConnection(builder.build());
        restConnection.logger = logger;

        final HubServicesFactory hubServicesFactory = new HubServicesFactory(restConnection);
        final URL hubUrl = restConnection.baseUrl;

        final CurrentVersionView currentVersion = hubServicesFactory.createHubService().getResponseFromPath(ApiDiscovery.CURRENT_VERSION_LINK_RESPONSE);
        final String hubVersion = currentVersion.version;

        final CLIDownloadUtility cliDownloadService = hubServicesFactory.createCliDownloadUtility();
        cliDownloadService.performInstallation(installDir, ciEnvironmentVariables, hubUrl.toString(), hubVersion, "TestHost");

        final File file = new File(installDir, CLILocation.VERSION_FILE_NAME);

        assertTrue(file.exists());
        String output = logger.getOutputString();
        assertTrue(output, output.contains("Unpacking "));
        assertTrue(cliLocation.getCLIExists(logger));
        output = logger.getOutputString();
        assertTrue(output, output.contains("BlackDuck scan directory: "));
        assertTrue(output, output.contains("directories in the BlackDuck scan directory: "));
        assertTrue(output, output.contains("BlackDuck scan lib directory: "));

        assertNotNull(cliLocation.getCLI(logger));
    }

    @Test
    public void testPerformInstallationBasicProxy() throws Exception {
        final TestLogger logger = new TestLogger();
        final File installDir = folder.newFolder();
        final CLILocation cliLocation = new CLILocation(logger, installDir);

        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        builder.setHubUrl(restConnectionTestHelper.getProperty("TEST_HUB_SERVER_URL"));
        builder.setUsername(restConnectionTestHelper.getProperty("TEST_USERNAME"));
        builder.setPassword(restConnectionTestHelper.getProperty("TEST_PASSWORD"));
        builder.setTimeout(restConnectionTestHelper.getProperty("TEST_HUB_TIMEOUT"));
        builder.setProxyHost(restConnectionTestHelper.getProperty("TEST_PROXY_HOST_BASIC"));
        builder.setProxyPort(restConnectionTestHelper.getProperty("TEST_PROXY_PORT_BASIC"));
        builder.setProxyUsername(restConnectionTestHelper.getProperty("TEST_PROXY_USER_BASIC"));
        builder.setProxyPassword(restConnectionTestHelper.getProperty("TEST_PROXY_PASSWORD_BASIC"));
        builder.setAlwaysTrustServerCertificate(Boolean.valueOf(restConnectionTestHelper.getProperty(TestingPropertyKey.TEST_TRUST_HTTPS_CERT)));

        final CredentialsRestConnection restConnection = restConnectionTestHelper.getRestConnection(builder.build());
        restConnection.logger = logger;

        final HubServicesFactory hubServicesFactory = new HubServicesFactory(restConnection);
        final URL hubUrl = restConnection.baseUrl;

        final CurrentVersionView currentVersion = hubServicesFactory.createHubService().getResponseFromPath(ApiDiscovery.CURRENT_VERSION_LINK_RESPONSE);
        final String hubVersion = currentVersion.version;

        final CLIDownloadUtility cliDownloadService = hubServicesFactory.createCliDownloadUtility();
        cliDownloadService.performInstallation(installDir, ciEnvironmentVariables, hubUrl.toString(), hubVersion, "TestHost");

        final File file = new File(installDir, CLILocation.VERSION_FILE_NAME);

        assertTrue(file.exists());
        String output = logger.getOutputString();
        assertTrue(output, output.contains("Unpacking "));
        assertTrue(cliLocation.getCLIExists(logger));
        output = logger.getOutputString();
        assertTrue(output, output.contains("BlackDuck scan directory: "));
        assertTrue(output, output.contains("directories in the BlackDuck scan directory: "));
        assertTrue(output, output.contains("BlackDuck scan lib directory: "));

        assertNotNull(cliLocation.getCLI(logger));
    }

    private void deleteFilesRecursive(final File[] files) throws IOException {
        for (final File file : files) {
            FileUtils.deleteQuietly(file);
        }
    }

}
