package com.blackducksoftware.integration.hub.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.restlet.data.Method;

import com.blackducksoftware.integration.hub.api.scan.ScanSummaryItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class ScanSummaryRestService extends HubRestService<ScanSummaryItem> {
	public ScanSummaryRestService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser, new TypeToken<ScanSummaryItem>() {
		}.getType(), new TypeToken<List<ScanSummaryItem>>() {
		}.getType());
	}

	public List<ScanSummaryItem> getAllScanSummaryItems(final String scanSummaryUrl)
			throws IOException, URISyntaxException, BDRestException {
		final HubRequest scanSummaryItemRequest = new HubRequest(getRestConnection(), getJsonParser());
		scanSummaryItemRequest.setMethod(Method.GET);
		scanSummaryItemRequest.setLimit(100);
		scanSummaryItemRequest.setUrl(scanSummaryUrl);

		final JsonObject jsonObject = scanSummaryItemRequest.executeForResponseJson();
		final List<ScanSummaryItem> allScanSummaryItems = getAll(jsonObject, scanSummaryItemRequest);
		return allScanSummaryItems;
	}

}
