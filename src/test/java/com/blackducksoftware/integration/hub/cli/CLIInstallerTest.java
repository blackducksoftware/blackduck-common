package com.blackducksoftware.integration.hub.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.util.TestLogger;

public class CLIInstallerTest {

    private static Properties testProperties;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void testInit() throws Exception {
        testProperties = new Properties();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream is = classLoader.getResourceAsStream("test.properties");
        try {
            testProperties.load(is);
        } catch (IOException e) {
            System.err.println("reading test.properties failed!");
        }
    }

    private File setupFakeCliStructure() throws Exception {
        folder.create();
        File dirToInstallTo = folder.newFolder();
        File cliInstallDir = new File(dirToInstallTo, CLIInstaller.CLI_UNZIP_DIR);
        File cliUnzipDir = new File(cliInstallDir, "scan.cli");
        File bin = new File(cliUnzipDir, "bin");
        bin.mkdirs();
        File lib = new File(cliUnzipDir, "lib");
        File cache = new File(lib, "cache");
        cache.mkdirs();
        File oneJarFile = new File(cache, "scan.cli.impl-standalone.jar");
        oneJarFile.createNewFile();
        File scanCli = new File(lib, "scan.cli.TEST.jar");
        scanCli.createNewFile();
        return dirToInstallTo;
    }

    private File setupFakeCliStructureWithJre() throws Exception {
        File dirToInstallTo = setupFakeCliStructure();
        File cliInstallDir = new File(dirToInstallTo, CLIInstaller.CLI_UNZIP_DIR);
        File cliUnzipDir = new File(cliInstallDir, "scan.cli");
        File jre = new File(cliUnzipDir, "jre");
        File bin = null;
        if (SystemUtils.IS_OS_MAC_OSX) {
            bin = new File(jre, "Contents");
            bin = new File(bin, "Home");
            bin = new File(bin, "bin");
        } else {
            bin = new File(jre, "bin");
        }
        bin.mkdirs();
        File java = new File(bin, "java");
        java.createNewFile();

        File lib = new File(jre, "lib");
        lib.mkdirs();
        return dirToInstallTo;
    }

    @Test
    public void testConstructorNull() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("You must provided a directory to install the CLI to.");
        new CLIInstaller(null);
    }

    @Test
    public void testConstructor() throws Exception {
        File dir = folder.newFolder();
        CLIInstaller installer = new CLIInstaller(dir);
        assertEquals(dir, installer.getDirectoryToInstallTo());

        File unzipDir = new File(dir, CLIInstaller.CLI_UNZIP_DIR);
        assertEquals(unzipDir, installer.getCLIInstallDir());

        assertNull(installer.getCLIHome());

        File cliHome = new File(unzipDir, "CLI_HOME");
        assertNull(installer.getCLIHome());

        cliHome.mkdirs();
        assertEquals(cliHome, installer.getCLIHome());
    }

    @Test
    public void testGetCLIDownloadUrl() throws Exception {
        File dir = folder.newFolder();
        CLIInstaller installer = new CLIInstaller(dir);
        TestLogger logger = new TestLogger();
        HubIntRestService service = new HubIntRestService("TestUrl");
        service = Mockito.spy(service);
        Mockito.doReturn("3.0.0").when(service).getHubVersion();
        assertTrue(installer.getCLIDownloadUrl(logger, service).contains("TestUrl/download/"));

        Mockito.doReturn("2.0.0").when(service).getHubVersion();
        assertEquals("TestUrl/download/scan.cli.zip", installer.getCLIDownloadUrl(logger, service));
    }

    @Test
    public void testDeleteRecursively() throws Exception {
        File installDir = setupFakeCliStructure();
        CLIInstaller installer = new CLIInstaller(installDir);
        installer.deleteFilesRecursive(installDir.listFiles());
        assertTrue(installDir.exists());
        assertTrue(installDir.listFiles().length == 0);
    }

    @Test
    public void testGetCLIHome() throws Exception {
        File installDir = setupFakeCliStructure();
        CLIInstaller installer = new CLIInstaller(installDir);
        File file = new File(installDir, CLIInstaller.CLI_UNZIP_DIR);
        file = new File(file, "scan.cli");
        assertEquals(file.getAbsolutePath(), installer.getCLIHome().getAbsolutePath());
        assertTrue(installer.getCLIHome().exists());
    }

    @Test
    public void testGetCLIHomeEmpty() throws Exception {
        File installDir = setupFakeCliStructure();
        CLIInstaller installer = new CLIInstaller(installDir);
        File file = new File(installDir, CLIInstaller.CLI_UNZIP_DIR);
        installer.deleteFilesRecursive(file.listFiles());
        assertNull(installer.getCLIHome());
    }

    @Test
    public void testGetCLIHomeEmptyInstallDir() throws Exception {
        CLIInstaller installer = new CLIInstaller(folder.newFolder());
        assertNull(installer.getCLIHome());
    }

    @Test
    public void testGetOneJarFile() throws Exception {
        File installDir = setupFakeCliStructure();
        CLIInstaller installer = new CLIInstaller(installDir);
        File file = new File(installDir, CLIInstaller.CLI_UNZIP_DIR);
        file = new File(file, "scan.cli");
        file = new File(file, "lib");
        file = new File(file, "cache");
        file = new File(file, "scan.cli.impl-standalone.jar");
        assertEquals(file.getAbsolutePath(), installer.getOneJarFile().getAbsolutePath());
        assertTrue(installer.getOneJarFile().exists());
    }

    @Test
    public void testGetProvidedJavaExec() throws Exception {
        File installDir = setupFakeCliStructureWithJre();
        CLIInstaller installer = new CLIInstaller(installDir);
        File file = new File(installDir, CLIInstaller.CLI_UNZIP_DIR);
        file = new File(file, "scan.cli");
        file = new File(file, "jre");
        if (SystemUtils.IS_OS_MAC_OSX) {
            file = new File(file, "Contents");
            file = new File(file, "Home");
        }
        file = new File(file, "bin");
        file = new File(file, "java");
        assertEquals(file.getAbsolutePath(), installer.getProvidedJavaExec().getAbsolutePath());
        assertTrue(installer.getProvidedJavaExec().exists());
    }

    @Test
    public void testGetProvidedJavaExecJavaDNE() throws Exception {
        File installDir = setupFakeCliStructureWithJre();
        CLIInstaller installer = new CLIInstaller(installDir);
        File file = new File(installDir, CLIInstaller.CLI_UNZIP_DIR);
        file = new File(file, "scan.cli");
        file = new File(file, "jre");
        if (SystemUtils.IS_OS_MAC_OSX) {
            file = new File(file, "Contents");
            file = new File(file, "Home");
        }
        file = new File(file, "bin");
        file = new File(file, "java");
        file.delete();
        assertNull(installer.getProvidedJavaExec());
    }

    @Test
    public void testGetProvidedJavaExecJavaNoBin() throws Exception {
        File installDir = setupFakeCliStructureWithJre();
        CLIInstaller installer = new CLIInstaller(installDir);
        File file = new File(installDir, CLIInstaller.CLI_UNZIP_DIR);
        file = new File(file, "scan.cli");
        file = new File(file, "jre");
        if (SystemUtils.IS_OS_MAC_OSX) {
            file = new File(file, "Contents");
            file = new File(file, "Home");
        }
        installer.deleteFilesRecursive(file.listFiles());
        assertNull(installer.getProvidedJavaExec());
    }

    @Test
    public void testGetProvidedJavaExecJavaNoJreFolder() throws Exception {
        File installDir = setupFakeCliStructure();
        CLIInstaller installer = new CLIInstaller(installDir);
        assertNull(installer.getProvidedJavaExec());
    }

    @Test
    public void testGetProvidedJavaExecJavaNoInstallDir() throws Exception {
        CLIInstaller installer = new CLIInstaller(folder.newFolder());
        assertNull(installer.getProvidedJavaExec());
    }

    @Test
    public void testGetCli() throws Exception {
        File installDir = setupFakeCliStructure();
        CLIInstaller installer = new CLIInstaller(installDir);

        File file = new File(installDir, CLIInstaller.CLI_UNZIP_DIR);
        file = new File(file, "scan.cli");
        file = new File(file, "lib");
        file = new File(file, "scan.cli.TEST.jar");

        File cli = installer.getCLI();
        assertNotNull(cli);
        assertEquals(file.getAbsolutePath(), cli.getAbsolutePath());
    }

    @Test
    public void testGetCliDNE() throws Exception {
        File installDir = setupFakeCliStructure();
        CLIInstaller installer = new CLIInstaller(installDir);

        File lib = new File(installDir, CLIInstaller.CLI_UNZIP_DIR);
        lib = new File(lib, "scan.cli");
        lib = new File(lib, "lib");
        File file = new File(lib, "scan.cli.TEST.jar");
        file.delete();
        File file2 = new File(lib, "test.txt");
        file2.createNewFile();

        // lib without scan cli file
        assertNull(installer.getCLI());

        file2.delete();
        // Now the lib folder is empty
        assertNull(installer.getCLI());
    }

    @Test
    public void testGetCliLibInvalid() throws Exception {
        File installDir = setupFakeCliStructure();
        CLIInstaller installer = new CLIInstaller(installDir);

        File lib = new File(installDir, CLIInstaller.CLI_UNZIP_DIR);
        lib = new File(lib, "scan.cli");
        lib = new File(lib, "lib");
        installer.deleteFilesRecursive(lib.listFiles());

        assertNull(installer.getCLI());

        lib.delete();

        assertNull(installer.getCLI());
    }

    @Test
    public void testGetCliCliHomeEmpty() throws Exception {
        File installDir = setupFakeCliStructure();
        CLIInstaller installer = new CLIInstaller(installDir);

        File file = new File(installDir, CLIInstaller.CLI_UNZIP_DIR);
        file = new File(file, "scan.cli");
        installer.deleteFilesRecursive(file.listFiles());

        assertNull(installer.getCLI());
    }

    @Test
    public void testGetCliCliHomeDNE() throws Exception {
        File installDir = setupFakeCliStructure();
        CLIInstaller installer = new CLIInstaller(installDir);

        File file = new File(installDir, CLIInstaller.CLI_UNZIP_DIR);
        installer.deleteFilesRecursive(file.listFiles());

        assertNull(installer.getCLI());
    }

    @Test
    public void testGetCliExists() throws Exception {
        File installDir = setupFakeCliStructure();
        CLIInstaller installer = new CLIInstaller(installDir);

        File file = new File(installDir, CLIInstaller.CLI_UNZIP_DIR);
        file = new File(file, "scan.cli");
        file = new File(file, "lib");
        file = new File(file, "scan.cli.TEST.jar");

        TestLogger logger = new TestLogger();
        assertTrue(installer.getCLIExists(logger));
        String output = logger.getOutputString();
        assertTrue(output, output.contains("BlackDuck scan directory: "));
        assertTrue(output, output.contains("directories in the BlackDuck scan directory: "));
        assertTrue(output, output.contains("BlackDuck scan lib directory: "));
    }

    @Test
    public void testGetCliExistsDNE() throws Exception {
        File installDir = setupFakeCliStructure();
        CLIInstaller installer = new CLIInstaller(installDir);

        File lib = new File(installDir, CLIInstaller.CLI_UNZIP_DIR);
        lib = new File(lib, "scan.cli");
        lib = new File(lib, "lib");
        File file = new File(lib, "scan.cli.TEST.jar");
        file.delete();
        File file2 = new File(lib, "test.txt");
        file2.createNewFile();

        // lib without scan cli file
        TestLogger logger = new TestLogger();
        assertTrue(!installer.getCLIExists(logger));
        String output = logger.getOutputString();
        assertTrue(output, output.contains("BlackDuck scan directory: "));
        assertTrue(output, output.contains("directories in the BlackDuck scan directory: "));
        assertTrue(output, output.contains("BlackDuck scan lib directory: "));

        file2.delete();
        // Now the lib folder is empty
        logger = new TestLogger();
        assertTrue(!installer.getCLIExists(logger));
        output = logger.getOutputString();
        assertTrue(output, output.contains("BlackDuck scan directory: "));
        assertTrue(output, output.contains("directories in the BlackDuck scan directory: "));
        assertTrue(output, output.contains("BlackDuck scan lib directory: "));
    }

    @Test
    public void testGetCliExistsLibInvalid() throws Exception {
        File installDir = setupFakeCliStructure();
        CLIInstaller installer = new CLIInstaller(installDir);

        File lib = new File(installDir, CLIInstaller.CLI_UNZIP_DIR);
        lib = new File(lib, "scan.cli");
        lib = new File(lib, "lib");
        installer.deleteFilesRecursive(lib.listFiles());

        TestLogger logger = new TestLogger();
        assertTrue(!installer.getCLIExists(logger));
        String output = logger.getOutputString();
        assertTrue(output, output.contains("BlackDuck scan directory: "));
        assertTrue(output, output.contains("directories in the BlackDuck scan directory: "));
        assertTrue(output, output.contains("BlackDuck scan lib directory: "));

        lib.delete();

        logger = new TestLogger();
        assertTrue(!installer.getCLIExists(logger));
        output = logger.getOutputString();
        assertTrue(output, output.contains("BlackDuck scan directory: "));
        assertTrue(output, output.contains("directories in the BlackDuck scan directory: "));
        assertTrue(output, output.contains("Could not find the lib directory of the CLI."));

    }

    @Test
    public void testGetCliExistsCliHomeEmpty() throws Exception {
        File installDir = setupFakeCliStructure();
        CLIInstaller installer = new CLIInstaller(installDir);

        File file = new File(installDir, CLIInstaller.CLI_UNZIP_DIR);
        file = new File(file, "scan.cli");
        installer.deleteFilesRecursive(file.listFiles());

        TestLogger logger = new TestLogger();
        assertTrue(!installer.getCLIExists(logger));
        String output = logger.getOutputString();
        assertTrue(output, output.contains("BlackDuck scan directory: "));
        assertTrue(output, output.contains("directories in the BlackDuck scan directory: 0"));
        assertTrue(output, output.contains("No files found in the BlackDuck scan directory."));
    }

    @Test
    public void testGetCliExistsCliHomeDNE() throws Exception {
        File installDir = setupFakeCliStructure();
        CLIInstaller installer = new CLIInstaller(installDir);

        File file = new File(installDir, CLIInstaller.CLI_UNZIP_DIR);
        installer.deleteFilesRecursive(file.listFiles());

        assertTrue(!installer.getCLIExists(new TestLogger()));
    }

    @Test
    public void testPerformInstallationNullHost() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("You must provided the hostName of the machine this is running on.");
        File installDir = folder.newFolder();
        CLIInstaller installer = new CLIInstaller(installDir);
        TestLogger logger = new TestLogger();
        HubIntRestService service = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        service = Mockito.spy(service);
        installer.performInstallation(logger, service, null);
    }

    @Test
    public void testPerformInstallationUpdatingEmptyHost() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("You must provided the hostName of the machine this is running on.");
        File installDir = folder.newFolder();
        CLIInstaller installer = new CLIInstaller(installDir);
        TestLogger logger = new TestLogger();
        HubIntRestService service = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        service = Mockito.spy(service);
        installer.performInstallation(logger, service, "");
    }

    @Test
    public void testPerformInstallationUpdating() throws Exception {
        File installDir = folder.newFolder();
        CLIInstaller installer = new CLIInstaller(installDir);
        TestLogger logger = new TestLogger();
        HubIntRestService service = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        service = Mockito.spy(service);
        Mockito.doReturn("1.0.0").when(service).getHubVersion();
        installer.performInstallation(logger, service, "TestHost");

        File file = new File(installDir, CLIInstaller.VERSION_FILE_NAME);

        assertTrue(file.exists());
        String storedVersion = IOUtils.toString(new FileInputStream(file));
        assertEquals("1.0.0", storedVersion);
        assertTrue(installer.getCLIExists(logger));
        assertNotNull(installer.getCLI());

        // Upgrade to 2.0.0
        Mockito.doReturn("2.0.0").when(service).getHubVersion();
        installer.performInstallation(logger, service, "TestHost");

        assertTrue(file.exists());
        storedVersion = IOUtils.toString(new FileInputStream(file));
        assertEquals("2.0.0", storedVersion);
        assertTrue(installer.getCLIExists(logger));
        assertNotNull(installer.getCLI());

        // Upgrade to 2.1.0
        Mockito.doReturn("2.1.0").when(service).getHubVersion();
        installer.performInstallation(logger, service, "TestHost");

        assertTrue(file.exists());
        storedVersion = IOUtils.toString(new FileInputStream(file));
        assertEquals("2.1.0", storedVersion);
        assertTrue(installer.getCLIExists(logger));
        assertNotNull(installer.getCLI());

        // Upgrade to 2.1.1
        Mockito.doReturn("2.1.1").when(service).getHubVersion();
        installer.performInstallation(logger, service, "TestHost");

        assertTrue(file.exists());
        storedVersion = IOUtils.toString(new FileInputStream(file));
        assertEquals("2.1.1", storedVersion);
        assertTrue(installer.getCLIExists(logger));
        assertNotNull(installer.getCLI());

        // Downgrade to 2.0.1
        Mockito.doReturn("2.0.1").when(service).getHubVersion();
        installer.performInstallation(logger, service, "TestHost");

        assertTrue(file.exists());
        storedVersion = IOUtils.toString(new FileInputStream(file));
        assertEquals("2.0.1", storedVersion);
        assertTrue(installer.getCLIExists(logger));
        assertNotNull(installer.getCLI());

        // Downgrade to 2.0.0
        Mockito.doReturn("2.0.0").when(service).getHubVersion();
        installer.performInstallation(logger, service, "TestHost");

        assertTrue(file.exists());
        storedVersion = IOUtils.toString(new FileInputStream(file));
        assertEquals("2.0.0", storedVersion);
        assertTrue(installer.getCLIExists(logger));
        assertNotNull(installer.getCLI());

        // Downgrade to 1.0.0
        Mockito.doReturn("1.0.0").when(service).getHubVersion();
        installer.performInstallation(logger, service, "TestHost");

        assertTrue(file.exists());
        storedVersion = IOUtils.toString(new FileInputStream(file));
        assertEquals("1.0.0", storedVersion);
        assertTrue(installer.getCLIExists(logger));
        assertNotNull(installer.getCLI());
    }

    @Test
    public void testPerformInstallation() throws Exception {
        File installDir = folder.newFolder();
        CLIInstaller installer = new CLIInstaller(installDir);
        TestLogger logger = new TestLogger();
        HubIntRestService service = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        service.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));
        installer.performInstallation(logger, service, "TestHost");

        File file = new File(installDir, CLIInstaller.VERSION_FILE_NAME);

        assertTrue(file.exists());

        String output = logger.getOutputString();
        assertTrue(output, output.contains("Unpacking "));
        assertTrue(installer.getCLIExists(logger));
        output = logger.getOutputString();
        assertTrue(output, output.contains("BlackDuck scan directory: "));
        assertTrue(output, output.contains("directories in the BlackDuck scan directory: "));
        assertTrue(output, output.contains("BlackDuck scan lib directory: "));

        assertNotNull(installer.getCLI());
    }

    @Test
    public void testPerformInstallationPassThroughProxy() throws Exception {
        File installDir = folder.newFolder();
        CLIInstaller installer = new CLIInstaller(installDir);
        installer.setProxyHost(testProperties.getProperty("TEST_PROXY_HOST_PASSTHROUGH"));
        installer.setProxyPort(Integer.valueOf(testProperties.getProperty("TEST_PROXY_PORT_PASSTHROUGH")));

        TestLogger logger = new TestLogger();
        HubIntRestService service = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        service.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));
        installer.performInstallation(logger, service, "TestHost");

        File file = new File(installDir, CLIInstaller.VERSION_FILE_NAME);

        assertTrue(file.exists());
        String output = logger.getOutputString();
        assertTrue(output, output.contains("Unpacking "));
        assertTrue(installer.getCLIExists(logger));
        output = logger.getOutputString();
        assertTrue(output, output.contains("BlackDuck scan directory: "));
        assertTrue(output, output.contains("directories in the BlackDuck scan directory: "));
        assertTrue(output, output.contains("BlackDuck scan lib directory: "));

        assertNotNull(installer.getCLI());
    }

    @Test
    public void testPerformInstallationBasicProxy() throws Exception {
        File installDir = folder.newFolder();
        CLIInstaller installer = new CLIInstaller(installDir);
        installer.setProxyHost(testProperties.getProperty("TEST_PROXY_HOST_BASIC"));
        installer.setProxyPort(Integer.valueOf(testProperties.getProperty("TEST_PROXY_PORT_BASIC")));
        installer.setProxyUserName(testProperties.getProperty("TEST_PROXY_USER_BASIC"));
        installer.setProxyPassword(testProperties.getProperty("TEST_PROXY_PASSWORD_BASIC"));

        TestLogger logger = new TestLogger();
        HubIntRestService service = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        service.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));
        installer.performInstallation(logger, service, "TestHost");

        File file = new File(installDir, CLIInstaller.VERSION_FILE_NAME);

        assertTrue(file.exists());
        String output = logger.getOutputString();
        assertTrue(output, output.contains("Unpacking "));
        assertTrue(installer.getCLIExists(logger));
        output = logger.getOutputString();
        assertTrue(output, output.contains("BlackDuck scan directory: "));
        assertTrue(output, output.contains("directories in the BlackDuck scan directory: "));
        assertTrue(output, output.contains("BlackDuck scan lib directory: "));

        assertNotNull(installer.getCLI());
    }

    @Test
    public void testPerformInstallationDigestProxy() throws Exception {
        File installDir = folder.newFolder();
        CLIInstaller installer = new CLIInstaller(installDir);
        installer.setProxyHost(testProperties.getProperty("TEST_PROXY_HOST_DIGEST"));
        installer.setProxyPort(Integer.valueOf(testProperties.getProperty("TEST_PROXY_PORT_DIGEST")));
        installer.setProxyUserName(testProperties.getProperty("TEST_PROXY_USER_DIGEST"));
        installer.setProxyPassword(testProperties.getProperty("TEST_PROXY_PASSWORD_DIGEST"));

        TestLogger logger = new TestLogger();
        HubIntRestService service = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        service.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));
        installer.performInstallation(logger, service, "TestHost");

        File file = new File(installDir, CLIInstaller.VERSION_FILE_NAME);

        assertTrue(file.exists());
        String output = logger.getOutputString();
        assertTrue(output, output.contains("Unpacking "));
        assertTrue(installer.getCLIExists(logger));
        output = logger.getOutputString();
        assertTrue(output, output.contains("BlackDuck scan directory: "));
        assertTrue(output, output.contains("directories in the BlackDuck scan directory: "));
        assertTrue(output, output.contains("BlackDuck scan lib directory: "));

        assertNotNull(installer.getCLI());
    }
}
