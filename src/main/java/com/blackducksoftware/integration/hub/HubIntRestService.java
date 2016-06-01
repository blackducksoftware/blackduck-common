/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.hub;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.Cookie;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

import com.blackducksoftware.integration.hub.api.VersionComparison;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.MissingPolicyStatusException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.VersionDoesNotExistException;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.policy.api.PolicyStatus;
import com.blackducksoftware.integration.hub.project.api.ProjectItem;
import com.blackducksoftware.integration.hub.report.api.ReportFormatEnum;
import com.blackducksoftware.integration.hub.report.api.ReportInformationItem;
import com.blackducksoftware.integration.hub.report.api.VersionReport;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.scan.api.ScanLocationItem;
import com.blackducksoftware.integration.hub.scan.api.ScanLocationResults;
import com.blackducksoftware.integration.hub.scan.status.ScanStatusToPoll;
import com.blackducksoftware.integration.hub.version.api.ReleaseItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class HubIntRestService {
	private final RestConnection restConnection;

	public HubIntRestService(final RestConnection restConnection) throws URISyntaxException {
		this.restConnection = restConnection;
	}

	/**
	 * @deprecated moved to RestConnection.
	 */
	@Deprecated
	public HubIntRestService(final String baseUrl) throws URISyntaxException {
		restConnection = new RestConnection(baseUrl);
	}

	/**
	 * @deprecated moved to RestConnection.
	 */
	@Deprecated
	public void setTimeout(final int timeout) {
		getRestConnection().setTimeout(timeout);
	}

	/**
	 * @deprecated moved to RestConnection.
	 */
	@Deprecated
	public void setLogger(final IntLogger logger) {
		getRestConnection().setLogger(logger);
	}

	/**
	 * @deprecated moved to RestConnection.
	 */
	@Deprecated
	public String getBaseUrl() {
		return getRestConnection().getBaseUrl();
	}

	public RestConnection getRestConnection() {
		return restConnection;
	}

	/**
	 * The proxy settings get set as System properties. I.E. https.proxyHost,
	 * https.proxyPort, http.proxyHost, http.proxyPort, http.nonProxyHosts
	 *
	 * @deprecated moved to RestConnection.
	 */
	@Deprecated
	public void setProxyProperties(final HubProxyInfo proxyInfo) {
		getRestConnection().setProxyProperties(proxyInfo);
	}

	/**
	 * The proxy settings get set as System properties. I.E. https.proxyHost,
	 * https.proxyPort, http.proxyHost, http.proxyPort, http.nonProxyHosts
	 *
	 * @deprecated moved to RestConnection.
	 */
	@Deprecated
	public void setProxyProperties(final String proxyHost, final int proxyPort, final List<Pattern> noProxyHosts,
			final String proxyUsername, final String proxyPassword) {
		getRestConnection().setProxyProperties(proxyHost, proxyPort, noProxyHosts, proxyUsername, proxyPassword);
	}

	/**
	 * @deprecated moved to RestConnection.
	 */
	@Deprecated
	public ClientResource createClientResource() throws URISyntaxException {
		return getRestConnection().createClientResource();
	}

	/**
	 * @deprecated moved to RestConnection.
	 */
	@Deprecated
	public ClientResource createClientResource(final String providedUrl) throws URISyntaxException {
		return getRestConnection().createClientResource(providedUrl);
	}

	/**
	 * Gets the cookie for the Authorized connection to the Hub server. Returns
	 * the response code from the connection.
	 *
	 * @deprecated moved to RestConnection.
	 */
	@Deprecated
	public int setCookies(final String hubUserName, final String hubPassword) throws HubIntegrationException,
	URISyntaxException, BDRestException {
		return getRestConnection().setCookies(hubUserName, hubPassword);
	}

	/**
	 * @deprecated moved to RestConnection.
	 */
	@Deprecated
	public Series<Cookie> getCookies() {
		return getRestConnection().getCookies();
	}

	/**
	 * Retrieves a list of Hub Projects that may match the hubProjectName
	 *
	 */
	public List<ProjectItem> getProjectMatches(final String projectName) throws IOException, BDRestException,
	URISyntaxException {
		final ClientResource resource = getRestConnection().createClientResource();
		resource.addSegment("api");
		resource.addSegment("projects");
		resource.addQueryParameter("q", "name:" + projectName);
		resource.addQueryParameter("limit", "15");
		resource.setMethod(Method.GET);
		getRestConnection().handleRequest(resource);
		final int responseCode = resource.getResponse().getStatus().getCode();

		if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
			final String response = getRestConnection().readResponseAsString(resource.getResponse());
			final Gson gson = new GsonBuilder().create();
			final JsonParser parser = new JsonParser();
			final JsonObject json = parser.parse(response).getAsJsonObject();
			return gson.fromJson(json.get("items"), new TypeToken<List<ProjectItem>>() {
			}.getType());

		} else {
			throw new BDRestException("There was a problem getting the project matches. Error Code: " + responseCode,
					resource);
		}

	}

	/**
	 * Gets the Project that is specified by the projectName
	 *
	 */
	public ProjectItem getProjectByName(final String projectName) throws IOException, BDRestException,
	URISyntaxException, ProjectDoesNotExistException {
		final ClientResource resource = getRestConnection().createClientResource();
		resource.addSegment("api");
		resource.addSegment("projects");
		resource.addQueryParameter("q", "name:" + projectName);
		resource.addQueryParameter("limit", "15");
		resource.setMethod(Method.GET);
		getRestConnection().handleRequest(resource);
		final int responseCode = resource.getResponse().getStatus().getCode();

		if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
			final String response = getRestConnection().readResponseAsString(resource.getResponse());
			final Gson gson = new GsonBuilder().create();
			final JsonParser parser = new JsonParser();
			final JsonObject json = parser.parse(response).getAsJsonObject();
			final List<ProjectItem> projects = gson.fromJson(json.get("items"), new TypeToken<List<ProjectItem>>() {
			}.getType());

			for (final ProjectItem project : projects) {
				if (project.getName().equals(projectName)) {
					return project;
				}
			}
			throw new ProjectDoesNotExistException("This Project does not exist. Project : " + projectName, resource);
		} else if (responseCode == 404)

		{
			throw new ProjectDoesNotExistException("This Project does not exist. Project : " + projectName, resource);
		} else {
			throw new BDRestException("There was a problem getting a Project by this name. Project : " + projectName,
					resource);
		}
	}

	public ProjectItem getProject(final String projectUrl) throws IOException, BDRestException, URISyntaxException {
		final ClientResource resource = getRestConnection().createClientResource(projectUrl);
		resource.setMethod(Method.GET);
		getRestConnection().handleRequest(resource);
		final int responseCode = resource.getResponse().getStatus().getCode();

		if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
			final String response = getRestConnection().readResponseAsString(resource.getResponse());
			final Gson gson = new GsonBuilder().create();
			return gson.fromJson(response, ProjectItem.class);

		} else {
			throw new BDRestException("There was a problem getting the project. Error Code: " + responseCode, resource);
		}

	}

	public ReleaseItem getProjectVersion(final String versionUrl) throws IOException, BDRestException,
	URISyntaxException {
		final ClientResource resource = getRestConnection().createClientResource(versionUrl);
		resource.setMethod(Method.GET);
		getRestConnection().handleRequest(resource);
		final int responseCode = resource.getResponse().getStatus().getCode();

		if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
			final String response = getRestConnection().readResponseAsString(resource.getResponse());
			final Gson gson = new GsonBuilder().create();
			return gson.fromJson(response, ReleaseItem.class);

		} else {
			throw new BDRestException("There was a problem getting the version. Error Code: " + responseCode, resource);
		}

	}

	/**
	 * Gets the list of Versions for the specified Project
	 *
	 */
	public ReleaseItem getVersion(final ProjectItem project, final String versionName) throws IOException,
	BDRestException, URISyntaxException, VersionDoesNotExistException {
		final List<ReleaseItem> versions = getVersionsForProject(project);
		for (final ReleaseItem version : versions) {
			if (version.getVersionName().equals(versionName)) {
				return version;
			}
		}
		throw new VersionDoesNotExistException("This Version does not exist. Project : " + project.getName()
				+ " Version : " + versionName);
	}

	/**
	 * Gets the list of Versions for the specified Project
	 *
	 */
	public List<ReleaseItem> getVersionsForProject(final ProjectItem project) throws IOException, BDRestException,
	URISyntaxException {
		final ClientResource resource = getRestConnection().createClientResource(
				project.getLink(ProjectItem.VERSION_LINK));
		resource.addQueryParameter("limit", "10000000");
		resource.setMethod(Method.GET);
		getRestConnection().handleRequest(resource);
		final int responseCode = resource.getResponse().getStatus().getCode();

		if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
			final String response = getRestConnection().readResponseAsString(resource.getResponse());

			final Gson gson = new GsonBuilder().create();
			final JsonParser parser = new JsonParser();
			final JsonObject json = parser.parse(response).getAsJsonObject();
			final List<ReleaseItem> versions = gson.fromJson(json.get("items"), new TypeToken<List<ReleaseItem>>() {
			}.getType());

			return versions;

		} else {
			throw new BDRestException("There was a problem getting the versions for this Project. Error Code: "
					+ responseCode, resource);
		}
	}

	/**
	 * Creates a Hub Project with the specified name.
	 *
	 * @return the project URL.
	 */
	public String createHubProject(final String projectName) throws IOException, BDRestException, URISyntaxException {
		final ClientResource resource = getRestConnection().createClientResource();
		resource.addSegment("api");
		resource.addSegment("projects");
		resource.setMethod(Method.POST);

		final ProjectItem newProject = new ProjectItem(projectName, null, null);
		final Gson gson = new GsonBuilder().create();
		final StringRepresentation stringRep = new StringRepresentation(gson.toJson(newProject));
		stringRep.setMediaType(MediaType.APPLICATION_JSON);
		stringRep.setCharacterSet(CharacterSet.UTF_8);
		resource.getRequest().setEntity(stringRep);
		getRestConnection().handleRequest(resource);
		final int responseCode = resource.getResponse().getStatus().getCode();

		if (responseCode == 201) {
			if (resource.getResponse().getAttributes() == null
					|| resource.getResponse().getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS) == null) {
				throw new BDRestException("Could not get the response headers after creating the Project.", resource);
			}
			@SuppressWarnings("unchecked")
			final Series<Header> responseHeaders = (Series<Header>) resource.getResponse().getAttributes()
			.get(HeaderConstants.ATTRIBUTE_HEADERS);
			final Header projectUrl = responseHeaders.getFirst("location", true);

			if (projectUrl == null || StringUtils.isBlank(projectUrl.getValue())) {
				throw new BDRestException("Could not get the project URL from the response headers.", resource);
			}
			return projectUrl.getValue();
		} else {

			throw new BDRestException("There was a problem creating this Hub Project. Error Code: " + responseCode,
					resource);
		}

	}

	/**
	 * Creates a new Version in the Project specified, using the phase and
	 * distribution provided.
	 *
	 * @return the version URL
	 *
	 */
	public String createHubVersion(final ProjectItem project, final String versionName, final String phase,
			final String dist) throws IOException, BDRestException, URISyntaxException {
		final ClientResource resource = getRestConnection().createClientResource(
				project.getLink(ProjectItem.VERSION_LINK));

		final int responseCode;
		final ReleaseItem newRelease = new ReleaseItem(versionName, phase, dist, null, null);

		resource.setMethod(Method.POST);

		final Gson gson = new GsonBuilder().create();
		final StringRepresentation stringRep = new StringRepresentation(gson.toJson(newRelease));
		stringRep.setMediaType(MediaType.APPLICATION_JSON);
		stringRep.setCharacterSet(CharacterSet.UTF_8);
		resource.getRequest().setEntity(stringRep);
		getRestConnection().handleRequest(resource);
		responseCode = resource.getResponse().getStatus().getCode();

		if (responseCode == 201) {
			if (resource.getResponse().getAttributes() == null
					|| resource.getResponse().getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS) == null) {
				throw new BDRestException("Could not get the response headers after creating the Version.", resource);
			}
			@SuppressWarnings("unchecked")
			final Series<Header> responseHeaders = (Series<Header>) resource.getResponse().getAttributes()
			.get(HeaderConstants.ATTRIBUTE_HEADERS);
			final Header versionUrl = responseHeaders.getFirst("location", true);

			if (versionUrl == null || StringUtils.isBlank(versionUrl.getValue())) {
				throw new BDRestException("Could not get the version URL from the response headers.", resource);
			}
			return versionUrl.getValue();
		} else {
			throw new BDRestException(
					"There was a problem creating this Version for the specified Hub Project. Error Code: "
							+ responseCode, resource);
		}

	}

	/**
	 * Retrieves the version of the Hub server
	 */
	public String getHubVersion() throws IOException, BDRestException, URISyntaxException {
		final ClientResource resource = getRestConnection().createClientResource();
		resource.addSegment("api");
		resource.addSegment("v1");
		resource.addSegment("current-version");

		int responseCode = 0;

		resource.setMethod(Method.GET);
		getRestConnection().handleRequest(resource);
		responseCode = resource.getResponse().getStatus().getCode();

		if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
			final Response resp = resource.getResponse();
			return resp.getEntityAsText();
		} else {
			throw new BDRestException("There was a problem getting the version of the Hub server. Error Code: "
					+ responseCode, resource);
		}
	}

	/**
	 * Compares the specified version with the actual version of the Hub server.
	 *
	 */
	public VersionComparison compareWithHubVersion(final String version) throws IOException, BDRestException,
	URISyntaxException {

		final ClientResource resource = getRestConnection().createClientResource();
		resource.addSegment("api");
		resource.addSegment("v1");
		resource.addSegment("current-version-comparison");
		resource.addQueryParameter("version", version);

		int responseCode = 0;

		resource.setMethod(Method.GET);
		getRestConnection().handleRequest(resource);
		responseCode = resource.getResponse().getStatus().getCode();

		if (responseCode == 200 || responseCode == 204 || responseCode == 202) {

			final String response = getRestConnection().readResponseAsString(resource.getResponse());
			final Gson gson = new GsonBuilder().create();
			final VersionComparison comparison = gson.fromJson(response, VersionComparison.class);
			return comparison;
		} else {
			throw new BDRestException(
					"There was a problem comparing the specified version to the version of the Hub server. Error Code: "
							+ responseCode, resource);
		}
	}

	/**
	 * Gets the code locations that match the host and paths provided
	 *
	 * @deprecated with Hub 3.0 should use the status files from the CLI
	 *             instead. The CLI option is --statusWriteDir
	 */
	@Deprecated
	public List<ScanLocationItem> getScanLocations(final String hostname, final List<String> scanTargets)
			throws InterruptedException, BDRestException, HubIntegrationException, URISyntaxException, IOException {
		final List<ScanLocationItem> codeLocations = new ArrayList<ScanLocationItem>();
		ClientResource resource = null;
		for (final String targetPath : scanTargets) {
			String correctedTargetPath = targetPath;

			// Scan paths in the Hub only use '/' not '\'
			if (correctedTargetPath.contains("\\")) {
				correctedTargetPath = correctedTargetPath.replace("\\", "/");
			}
			// and it always starts with a '/'
			if (!correctedTargetPath.startsWith("/")) {
				correctedTargetPath = "/" + correctedTargetPath;
			}

			resource = getRestConnection().createClientResource();
			resource.addSegment("api");
			resource.addSegment("v1");
			resource.addSegment("scanlocations");
			resource.addQueryParameter("host", hostname);
			resource.addQueryParameter("path", correctedTargetPath);

			resource.setMethod(Method.GET);

			getRestConnection().handleRequest(resource);

			final int responseCode = resource.getResponse().getStatus().getCode();

			if (responseCode == 200) {
				final String response = getRestConnection().readResponseAsString(resource.getResponse());
				final ScanLocationResults results = new Gson().fromJson(response, ScanLocationResults.class);
				final ScanLocationItem currentCodeLocation = getScanLocationMatch(hostname, correctedTargetPath,
						results);
				if (currentCodeLocation == null) {
					throw new HubIntegrationException("Could not determine the code location for the Host : "
							+ hostname + " and Path : " + correctedTargetPath);
				}

				codeLocations.add(currentCodeLocation);
			} else {
				throw new BDRestException(
						"There was a problem getting the code locations for the host and paths provided. Error Code: "
								+ responseCode, resource);
			}

		}
		return codeLocations;
	}

	private ScanLocationItem getScanLocationMatch(final String hostname, final String scanTarget,
			final ScanLocationResults results) {
		String targetPath = scanTarget;

		if (targetPath.endsWith("/")) {
			targetPath = targetPath.substring(0, targetPath.length() - 1);
		}

		for (final ScanLocationItem scanMatch : results.getItems()) {

			String path = scanMatch.getPath().trim();

			// Remove trailing slash from both strings
			if (path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
			}
			if (targetPath.equals(path)) {
				return scanMatch;
			}
		}

		return null;
	}

	/**
	 * Generates a new Hub report for the specified version.
	 *
	 * @return the Report URL
	 *
	 */
	public String generateHubReport(final ReleaseItem version, final ReportFormatEnum reportFormat) throws IOException,
	BDRestException, URISyntaxException {
		if (ReportFormatEnum.UNKNOWN == reportFormat) {
			throw new IllegalArgumentException("Can not generate a report of format : " + reportFormat);
		}

		final ClientResource resource = getRestConnection().createClientResource(
				version.getLink(ReleaseItem.VERSION_REPORT_LINK));

		resource.setMethod(Method.POST);

		final JsonObject json = new JsonObject();
		json.addProperty("reportFormat", reportFormat.name());

		final Gson gson = new GsonBuilder().create();
		final StringRepresentation stringRep = new StringRepresentation(gson.toJson(json));
		stringRep.setMediaType(MediaType.APPLICATION_JSON);
		stringRep.setCharacterSet(CharacterSet.UTF_8);
		resource.getRequest().setEntity(stringRep);
		getRestConnection().handleRequest(resource);

		final int responseCode = resource.getResponse().getStatus().getCode();

		if (responseCode == 201) {
			if (resource.getResponse().getAttributes() == null
					|| resource.getResponse().getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS) == null) {
				throw new BDRestException("Could not get the response headers after creating the report.", resource);
			}
			@SuppressWarnings("unchecked")
			final Series<Header> responseHeaders = (Series<Header>) resource.getResponse().getAttributes()
			.get(HeaderConstants.ATTRIBUTE_HEADERS);
			final Header reportUrl = responseHeaders.getFirst("location", true);

			if (reportUrl == null || StringUtils.isBlank(reportUrl.getValue())) {
				throw new BDRestException("Could not get the report URL from the response headers.", resource);
			}

			return reportUrl.getValue();

		} else {
			throw new BDRestException("There was a problem generating a report for this Version. Error Code: "
					+ responseCode, resource);
		}
	}

	public int deleteHubReport(final String reportUrl) throws IOException, BDRestException, URISyntaxException {

		final ClientResource resource = getRestConnection().createClientResource(reportUrl);
		resource.setMethod(Method.DELETE);
		getRestConnection().handleRequest(resource);

		final int responseCode = resource.getResponse().getStatus().getCode();
		if (responseCode != 204) {
			throw new BDRestException("There was a problem deleting this report. Error Code: " + responseCode, resource);
		}
		return responseCode;
	}

	public ReportInformationItem getReportInformation(final String reportUrl) throws IOException, BDRestException,
	URISyntaxException {

		final ClientResource resource = getRestConnection().createClientResource(reportUrl);

		@SuppressWarnings("unchecked")
		Series<Header> requestHeaders = (Series<Header>) resource.getRequestAttributes().get(
				HeaderConstants.ATTRIBUTE_HEADERS);
		if (requestHeaders == null) {
			requestHeaders = new Series<Header>(Header.class);
			resource.getRequestAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, requestHeaders);
		}
		requestHeaders.add(new Header("Accept", MediaType.APPLICATION_JSON.toString()));

		// Restlet 2.3.4 and higher
		// resource.accept(MediaType.APPLICATION_JSON);

		resource.setMethod(Method.GET);

		getRestConnection().handleRequest(resource);
		final int responseCode = resource.getResponse().getStatus().getCode();

		if (responseCode == 200) {
			final String response = getRestConnection().readResponseAsString(resource.getResponse());

			return new Gson().fromJson(response, ReportInformationItem.class);
		} else {
			throw new BDRestException("There was a problem getting the links for the specified report. Error Code: "
					+ responseCode, resource);
		}

	}

	/**
	 * Gets the content of the report
	 *
	 */
	public VersionReport getReportContent(final String reportContentUrl) throws IOException, BDRestException,
	URISyntaxException {

		final ClientResource resource = getRestConnection().createClientResource(reportContentUrl);

		resource.setMethod(Method.GET);

		getRestConnection().handleRequest(resource);
		final int responseCode = resource.getResponse().getStatus().getCode();

		if (responseCode == 200) {
			final String response = getRestConnection().readResponseAsString(resource.getResponse());

			final Gson gson = new GsonBuilder().create();

			final JsonParser parser = new JsonParser();
			final JsonObject json = parser.parse(response).getAsJsonObject();
			final JsonElement content = json.get("reportContent");
			final JsonArray reportConentArray = content.getAsJsonArray();
			final JsonObject reportFile = reportConentArray.get(0).getAsJsonObject();

			final VersionReport report = gson.fromJson(reportFile.get("fileContent"), VersionReport.class);

			return report;
		} else if (responseCode == 412) {
			final String response = getRestConnection().readResponseAsString(resource.getResponse());
			final JsonParser parser = new JsonParser();
			final JsonObject json = parser.parse(response).getAsJsonObject();
			final String errorMessage = json.get("errorMessage").getAsString();
			throw new BDRestException(errorMessage + " Error Code: " + responseCode, resource);
		} else {
			throw new BDRestException("There was a problem getting the content of this Report. Error Code: "
					+ responseCode, resource);
		}

	}

	/**
	 * Generates a new Hub report for the specified version.
	 *
	 */
	public PolicyStatus getPolicyStatus(final String policyStatusUrl) throws IOException, BDRestException,
	URISyntaxException, MissingPolicyStatusException {
		if (StringUtils.isBlank(policyStatusUrl)) {
			throw new IllegalArgumentException("Missing the policy status URL.");
		}
		final ClientResource resource = getRestConnection().createClientResource(policyStatusUrl);

		resource.setMethod(Method.GET);

		getRestConnection().handleRequest(resource);

		final int responseCode = resource.getResponse().getStatus().getCode();

		if (responseCode == 200) {
			final String response = getRestConnection().readResponseAsString(resource.getResponse());

			final Gson gson = new GsonBuilder().create();
			final PolicyStatus status = gson.fromJson(response, PolicyStatus.class);
			return status;
		}
		if (responseCode == 404) {
			throw new MissingPolicyStatusException(
					"There was no policy status found for this version. The BOM may be empty.");
		} else {
			throw new BDRestException("There was a problem getting the policy status. Error Code: " + responseCode,
					resource);
		}
	}

	/**
	 * Gets the content of the scanStatus at the provided url
	 */
	public ScanStatusToPoll checkScanStatus(final String scanStatusUrl) throws IOException, BDRestException,
	URISyntaxException {

		final ClientResource resource = getRestConnection().createClientResource(scanStatusUrl);

		resource.setMethod(Method.GET);

		getRestConnection().handleRequest(resource);
		final int responseCode = resource.getResponse().getStatus().getCode();

		if (responseCode == 200) {
			final String response = getRestConnection().readResponseAsString(resource.getResponse());

			final Gson gson = new GsonBuilder().create();

			final ScanStatusToPoll status = gson.fromJson(response, ScanStatusToPoll.class);
			return status;
		} else {
			throw new BDRestException("There was a problem getting the scan status. Error Code: " + responseCode,
					resource);
		}

	}
}
