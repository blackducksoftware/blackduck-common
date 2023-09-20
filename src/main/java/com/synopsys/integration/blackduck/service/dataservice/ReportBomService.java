/*
 * blackduck-common
 *
 * Copyright (c) 2023 Synopsys, Inc.
 * Copyright (c) 2023 Jens Nachtigall
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.dataservice;

import java.util.List;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.manual.view.BomReportView;
import com.synopsys.integration.blackduck.api.manual.view.BomReportContentView;
import com.synopsys.integration.blackduck.api.manual.component.BomReportRequest;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.exception.IntegrationTimeoutException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.wait.ResilientJob;
import com.synopsys.integration.wait.ResilientJobConfig;
import com.synopsys.integration.wait.ResilientJobExecutor;
import com.synopsys.integration.wait.tracker.WaitIntervalTracker;
import com.synopsys.integration.wait.tracker.WaitIntervalTrackerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Blackduck API Data Service implementation to both, request and download Bill of Materials
 * reports from Black Duck Hub when it eventually becomes available.
 */
public class ReportBomService extends DataService {

  private Logger log = LoggerFactory.getLogger(ReportBomService.class);
  private static final int BD_WAIT_AND_RETRY_INTERVAL = 5;

  // Internal class to validate user input arugments.
  private static class BomRequestValidator {

    // as per REST API Docs
    private static List<String> acceptableFormat = List.of("JSON", "RDF", "TAGVALUE", "YAML");
    private static List<String> acceptableType = List.of("SPDX_22", "CYCLONEDX_13", "CYCLONEDX_14");

    /**
     * Validate a user specified format string using acceptableFormat
     * @param format The format string to validate
     * @return The uppercase format string if valid.
     * @throws IllegalArgumentException if tis is not the case
     */
    public static String validateFormat(final String format) throws IllegalArgumentException{
      if (acceptableFormat.contains(format.toUpperCase())) {
        return format.toUpperCase();
      } 
      throw new IllegalArgumentException("Bom Format " + format + "is not among the valid ones: " + String.join(",", acceptableFormat));
    }

    /**
     * Validate a user specified type string using acceptableType
     * @param format The type string to validate
     * @return The uppercase format string if valid.
     * @throws IllegalArgumentException if tis is not the case
     */
    public static String validateType(final String type) throws IllegalArgumentException{
      if (acceptableType.contains(type.toUpperCase())) {
        return type.toUpperCase();
      }
      throw new IllegalArgumentException("Bom Format " + type + "is not among the valid ones: " + String.join(",", acceptableType));
    }
  }

  /**
   * Internal download task for use with the ResilientJobExecutor of the Blackduck API,
   * supporting interrution, timeout and retry count.
   * 
   * Attempts to download a Bill of Material by url/uuid when it becomes avaialble 
   * and maps to a BomReportView, respectively.
   */
  private static class BomDownloadJob implements ResilientJob<BomReportView> {
    private BlackDuckApiClient blackDuckApiClient;
    private String jobName;
    private boolean complete;
    private HttpUrl uri;
    private BomReportView BomReport;
    
    /**
     * Constructor
     * @param blackDuckApiClient An initialized BD API client (which has halready handled OAuth)
     * @param jobName An arbitrary job name (only used for external logging)
     * @param uri The reports URI as returned from the report creation request
     */
    public BomDownloadJob(BlackDuckApiClient blackDuckApiClient, String jobName, HttpUrl uri) {
      this.blackDuckApiClient = blackDuckApiClient;
      this.jobName = jobName;
      this.uri = uri;
      this.complete = false;
    }

    @Override
    public void attemptJob() throws IntegrationException {
      try {
        // Wait while HTTP 412 Precondition failed is returned.
        // for some reason, there will always be a JSON array in the response.
        BomReport = blackDuckApiClient.getResponse(uri.appendRelativeUrl("contents"), BomReportView.class);
        complete = true;
      } catch (IntegrationException e) {
        complete = false;
      }
    }

    @Override
    public boolean wasJobCompleted() {
      return complete;
    }

    @Override
    public BomReportView onTimeout() throws IntegrationTimeoutException {
      throw new IntegrationTimeoutException("Not able to upload BDIO due to timeout.");
    }

    @Override
    public BomReportView onCompletion() {
      return BomReport;
    }

    @Override
    public String getName() {
      return this.jobName;
    }

  }

  /**
   * Constuctor of the BOM Data Service
   * @param blackDuckApiClient An initialized BD API client (which has halready handled OAuth)
   * @param apiDiscovery For the superclass of a Blackduck DataService
   * @param logger For unified logging
   */
  public ReportBomService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, IntLogger logger) {
    super(blackDuckApiClient, apiDiscovery, logger);
  }

  /**
   * Sets up a valid Bom report request to the BDH RESET API (e.g. the POST Payload)
   * @param type The BOM type, e.g. Cyclone or SPDX, @see BomRequestValidator.acceptableType
   * @param format The BOM format, e.g. JSON, tag:value, @see BomRequestValidator.acceptableFormat
   * @return The request obect
   * @throws IllegalArgumentException 
   */
  public BomReportRequest createRequest(String type, String format) throws IllegalArgumentException{
    BomReportRequest request = new BomReportRequest();
    request.setReportType("Bom"); // Bom - static, optional?
    request.setReportFormat(BomRequestValidator.validateFormat(format).toUpperCase()); //JSON
    request.setSbomType(BomRequestValidator.validateType(type).toUpperCase()); // SPDX_22
    return request;
  }

  /**
   * Request a Bom report creation on the BDH
   * @param projectVersion Project version response (wraped as View) carrying project and version uuid, respectively.
   * @param reportRequest A populated / configured request
   * @return An URL to the scheduled report including the uuid of the report.
   * @throws IntegrationException
   */
  public HttpUrl createReport(ProjectVersionView projectVersion, BomReportRequest reportRequest) throws IntegrationException {
    // This is merely queing the report; it will be available upon completion
    HttpUrl versionUrl = projectVersion.getHref();
    log.info("Project Version URL for " + projectVersion.getVersionName() + ": " + versionUrl.toString());

    // The request returns an empty response with HTTP 201 Created and an attribute "Link" in the reponse header.
    // Coincidentially, this is exactly what is returned by post().
    HttpUrl reportUrl = blackDuckApiClient.post(
      versionUrl.appendRelativeUrl("Bom-reports"), reportRequest);

    log.info("Report available from: " + reportUrl.toString());
    
    return reportUrl;
  }

  /**
   * Request a Bom report creation on the BDH
   * @param wrapper A wrapped View response including project and version uuid.
   * @param reportRequest A populated / configured request
   * @return An URL to the scheduled report including the uuid of the report.
   * @throws IntegrationException
   */
  public HttpUrl createReport(ProjectVersionWrapper wrapper, BomReportRequest reportRequest) throws IntegrationException {
    // This is merely queing the report creation
    return createReport(wrapper.getProjectVersionView(), reportRequest);
  }

  /**
   * Await a Bom creation and download the report based on the reportUrl (@see createReport)
   * @param reportUrl The URI identifying the report with it's uuid
   * @param timeout Timeout in seconds
   * @return
   * @throws IntegrationException Something failed along the way (see stacktrace)
   * @throws InterruptedException Interrupted by user
   */
  public BomReportView downloadReports(HttpUrl reportUrl, long timeout) throws IntegrationException, InterruptedException {

    WaitIntervalTracker waitIntervalTracker = WaitIntervalTrackerFactory.createConstant(timeout, BD_WAIT_AND_RETRY_INTERVAL);
    ResilientJobConfig jobConfig = new ResilientJobConfig(logger, System.currentTimeMillis(), waitIntervalTracker);
    BomDownloadJob BomDownloadJob = new BomDownloadJob(blackDuckApiClient, "Awaiting Bom report completion", reportUrl);
    ResilientJobExecutor jobExecutor = new ResilientJobExecutor(jobConfig);

    return jobExecutor.executeJob(BomDownloadJob);
  }

  /**
   * Await a Bom creation and download the report based on the reportUrl (@see createReport)
   * @param reportUrl The URI identifying the report with it's uuid
   * @param timeout Timeout in seconds
   * @param log For unified logging
   * @return
   * @throws IntegrationException Something failed along the way (see stacktrace)
   * @throws InterruptedException Interrupted by user
   */
  public BomReportView downloadReports(HttpUrl reportUrl, long timeout, Logger log) throws IntegrationException, InterruptedException { 
    this.log = log;
    return downloadReports(reportUrl, timeout);
  } 
}
