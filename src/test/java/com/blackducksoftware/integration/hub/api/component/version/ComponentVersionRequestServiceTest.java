package com.blackducksoftware.integration.hub.api.component.version;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.processor.MockRestConnection;
import com.blackducksoftware.integration.hub.request.HubPagedRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ComponentVersionRequestServiceTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Test
	public void getLicenseInfoValid() throws Exception {
		RestConnection restConnection = new MockRestConnection();
		HubServicesFactory factory = new HubServicesFactory(restConnection);
		ComponentVersionRequestService cvrs = factory.createComponentVersionRequestService();
		
		HubPagedRequest mockHubPagedRequest = Mockito.mock(HubPagedRequest.class);
		Mockito.when(mockHubPagedRequest.executeGetForResponseJson()).thenReturn(
				createJsonComponentVersion(0));
		
		LicenseInfo actualLI = cvrs.getLicenseInfo(mockHubPagedRequest);
		
		List<License> expectedLicenseList = new LinkedList<License>();
		License expectedLicense = new License("Apache License 2.0", "OPEN_SOURCE", "PERMISSIVE", new LinkedList<License>(), "http://int-hub01.dc1.lan:8080/api/licenses/7cae335f-1193-421e-92f1-8802b4243e93");
		expectedLicenseList.add(expectedLicense);
		LicenseInfo expectedLI = new LicenseInfo("CONJUNCTIVE", expectedLicenseList);
		
		assertEquals(expectedLI, actualLI);
	}
	
	@Test
	public void getLicenseInfoInvalidVers() throws Exception {
		expectedException.expect(HubIntegrationException.class);
		
		RestConnection restConnection = new MockRestConnection();
		HubServicesFactory factory = new HubServicesFactory(restConnection);
		ComponentVersionRequestService cvrs = factory.createComponentVersionRequestService();
		
		HubPagedRequest mockHubPagedRequest = Mockito.mock(HubPagedRequest.class);
		Mockito.when(mockHubPagedRequest.executeGetForResponseJson()).thenReturn(
				createJsonComponentVersion(1));
		
		LicenseInfo actualLI = cvrs.getLicenseInfo(mockHubPagedRequest);
	}
	
	@Test
	public void getLicenseInfoInvalidComp() throws Exception {
		expectedException.expect(HubIntegrationException.class);
		
		RestConnection restConnection = new MockRestConnection();
		HubServicesFactory factory = new HubServicesFactory(restConnection);
		ComponentVersionRequestService cvrs = factory.createComponentVersionRequestService();
		
		HubPagedRequest mockHubPagedRequest = Mockito.mock(HubPagedRequest.class);
		Mockito.when(mockHubPagedRequest.executeGetForResponseJson()).thenReturn(
				createJsonComponentVersion(2));
		
		LicenseInfo actualLI = cvrs.getLicenseInfo(mockHubPagedRequest);
	}
	
	@Test
	public void getLicenseInfoInvalidCompAndVers() throws Exception {
		expectedException.expect(HubIntegrationException.class);
		
		RestConnection restConnection = new MockRestConnection();
		HubServicesFactory factory = new HubServicesFactory(restConnection);
		ComponentVersionRequestService cvrs = factory.createComponentVersionRequestService();
		
		HubPagedRequest mockHubPagedRequest = Mockito.mock(HubPagedRequest.class);
		Mockito.when(mockHubPagedRequest.executeGetForResponseJson()).thenReturn(
				createJsonComponentVersion(3));
		
		cvrs.getLicenseInfo(mockHubPagedRequest);
	}
	
	private JsonObject createJsonComponentVersion(int type) {
		String s;
		
		//valid response (example from com.google.guava:guava:20.0)
		if(type == 0){
			s = "{"
					+ "\"versionName\":\"20.0\","
					+ "\"releasedOn\":\"2016-10-28T21:04:15.000Z\","
					+ "\"source\":\"KB\","
					+ "\"license\":{"
						+ "\"type\":\"CONJUNCTIVE\","
						+ "\"licenses\":["
							+ "{"
								+ "\"name\":\"Apache License 2.0\","
								+ "\"ownership\":\"OPEN_SOURCE\","
								+ "\"codeSharing\":\"PERMISSIVE\","
								+ "\"licenses\":[],"
								+ "\"license\":\"http://int-hub01.dc1.lan:8080/api/licenses/7cae335f-1193-421e-92f1-8802b4243e93\""
							+ "}"
						+ "]"
					+ "},"
					+ "\"_meta\":{"
						+ "\"allow\":[\"GET\"],"
						+ "\"href\":\"http://int-hub01.dc1.lan:8080/api/components/2f0a32a0-f5c6-45d7-858c-b8a5d922633d/versions/fc3a0063-ab0d-41bf-a602-55ca75fc2cf0\","
						+ "\"links\":["
							+ "{"
								+ "\"rel\":\"component\","
								+ "\"href\":\"http://int-hub01.dc1.lan:8080/api/components/2f0a32a0-f5c6-45d7-858c-b8a5d922633d\""
							+ "},"
							+ "{"
								+ "\"rel\":\"vulnerabilities\","
								+ "\"href\":\"http://int-hub01.dc1.lan:8080/api/components/2f0a32a0-f5c6-45d7-858c-b8a5d922633d/versions/fc3a0063-ab0d-41bf-a602-55ca75fc2cf0/vulnerabilities\""
							+ "}"
						+ "]"
					+ "}"
				+ "}";
		//valid componentId, invalid versionId
		} else if(type == 1) {
			s = "{"
					+ "\"errorMessage\": \"No data could be found.\","
					+ "\"arguments\": {},"
					+ "\"errors\": null,"
					+ "\"errorCode\": \"{core.rest.no_data_found}\""
				+ "}";
		//valid versionId, invalid componentId	
		} else if(type == 2) {	
			s = "{"
				    + "\"errorMessage\": \"Project for version does not exist\","
				    + "\"arguments\": {},"
				    + "\"errors\": ["
				    	+ "{"
				    		+ "\"errorMessage\": \"Project for version does not exist\","
				    			+ "\"arguments\": {},"
				    			+ "\"errorCode\": \"{central.validation.version_project_id_error}\""
				    		+ "}"
				    + "],"
				    + "\"errorCode\": \"{central.validation.version_project_id_error}\""
				+ "}";
		//invalid componentId & versionId
		} else {
			s = "{"
				    + "\"errorMessage\": \"Could not resolve url due to type mismatch.\","
				    + "\"arguments\": {"
				    + "\"errorMessage\": \"Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; nested exception is java.lang.IllegalArgumentException: Invalid UUID string: asdf\""
				    		+ "},"
				    + "\"errors\": null,"
				    + "\"errorCode\": \"{core.rest.type_mismatch}\""
				+ "}";
		}
		
		final JsonParser parser = new JsonParser();
		final JsonObject jo = parser.parse(s).getAsJsonObject();
		return jo;
	}
}
