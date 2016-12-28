package com.blackducksoftware.integration.hub.api.component.version;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.processor.MockRestConnection;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;

public class ComponentVersionRequestServiceTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Test
	public void getLicenseInfoFromRequestTest() throws Exception {
		RestConnection restConnection = new MockRestConnection();
		HubServicesFactory factory = new HubServicesFactory(restConnection);
		ComponentVersionRequestService cvrs = factory.createComponentVersionRequestService();
		
	}
}
