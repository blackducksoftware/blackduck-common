package com.blackducksoftware.integration.hub;

import org.junit.Test;

public class resttest {

	@Test
	public void testStuff() throws Exception {
		final HubIntRestService service = new HubIntRestService("http://integration-hub.blackducksoftware.com");
		service.setProxyProperties("qasslproxy.blackducksoftware.com", 3130, null, "bds", "blackduck");
		service.setCookies("sysadmin", "blackduck");
	}
}
