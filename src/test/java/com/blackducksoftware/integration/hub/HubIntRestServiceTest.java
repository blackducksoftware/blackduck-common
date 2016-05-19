package com.blackducksoftware.integration.hub;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;

public class HubIntRestServiceTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void test() throws ResourceDoesNotExistException,
	    URISyntaxException, IOException {
	HubIntRestService hub = new HubIntRestService("testBaseUrl");

	// hub.getFromAbsoluteUrl(ProjectVersionItem.class,
	// "testProjectVersionItemUrl");
    }

}
