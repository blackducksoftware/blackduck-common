package com.blackducksoftware.integration.hub;

import java.net.URI;

import org.junit.Test;
import org.restlet.Context;
import org.restlet.Response;
import org.restlet.resource.ClientResource;

public class NotificationDataServiceTest {
	@Test
	public void testGoogle2() throws Exception {
		final String url = "https://www.google.com?gws_rd=ssl";
		final ClientResource resource = new ClientResource(new Context(), new URI(url));
		resource.handle();

		final Response response = resource.getResponse();
		final int responseCode = response.getStatus().getCode();
		System.out.println(responseCode);
	}

}
