package com.blackducksoftware.integration.hub;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.hub.util.TestLogger;

public class HubIntRestServiceTest {

    private static Properties testProperties;

    @BeforeClass
    public static void testInit() {
        testProperties = new Properties();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream is = classLoader.getResourceAsStream("test.properties");
        try {
            testProperties.load(is);
        } catch (IOException e) {
            System.err.println("reading test.properties failed!");
        }
        // p.load(new FileReader(new File("test.properties")));
        System.out.println(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        System.out.println(testProperties.getProperty("TEST_USERNAME"));
        System.out.println(testProperties.getProperty("TEST_PASSWORD"));

    }

    @Test
    public void test() throws Exception {

        TestLogger logger = new TestLogger();

        HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        restService.setLogger(logger);
        restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));

        List<String> paths = new ArrayList<String>();
        paths.add("/Users/jrichard/Documents/Jenkins/Jenkins-Hub-Git/int-hub-jenkins/test-workspace/workspace");
        Map<String, Boolean> response = null;
        try {
            response = restService.getScanLocationIds("jrichardMac", paths,
                    "5003bbc7-fc7a-4ba5-9070-d2c3a260b7b8");

            restService.mapScansToProjectVersion(response, "5003bbc7-fc7a-4ba5-9070-d2c3a260b7b8");
        } finally {
            System.out.println(logger.getOutputString());
        }

    }
}
