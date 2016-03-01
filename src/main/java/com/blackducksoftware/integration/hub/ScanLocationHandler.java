package com.blackducksoftware.integration.hub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.restlet.Response;
import org.restlet.resource.ClientResource;

import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.response.mapping.AssetReferenceItem;
import com.blackducksoftware.integration.hub.response.mapping.ScanLocationItem;
import com.blackducksoftware.integration.hub.response.mapping.ScanLocationResults;
import com.blackducksoftware.integration.suite.sdk.logging.IntLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @deprecated We dont need this anymore since the CLI now maps scans to versions for us, as of Hub 2.2.0
 *
 */
@Deprecated
public class ScanLocationHandler {

    private final IntLogger logger;

    public ScanLocationHandler(IntLogger logger) {
        this.logger = logger;
    }

    /**
     * Will attempt to retrieve the scanLocation with the current hostname and target path. If this cannot be found then
     * it retry's every 5 seconds for 2 minutes
     *
     * @param resource
     *            ClientResource
     * @param targetPath
     *            String
     * @param versionId
     *            String
     *
     * @throws UnknownHostException
     * @throws MalformedURLException
     * @throws InterruptedException
     * @throws BDRestException
     * @throws HubIntegrationException
     */
    public void getScanLocationIdWithRetry(ClientResource resource, String targetPath, String versionId,
            Map<String, Boolean> scanLocationIds)
            throws UnknownHostException, InterruptedException, BDRestException, HubIntegrationException {

        if (resource == null) {
            throw new IllegalArgumentException("Need to provide a ClientResource in order to get the ScanLocation");
        }
        if (StringUtils.isEmpty(targetPath)) {
            throw new IllegalArgumentException("Need to provide the targetPath of the ScanLocation.");
        }
        if (StringUtils.isEmpty(versionId)) {
            throw new IllegalArgumentException("Need to provide the versionId to make sure the mapping hasn't alredy been done.");
        }
        boolean matchFound = false;
        long start = System.currentTimeMillis();
        int i = 0;
        while (!matchFound) {
            i++;

            logger.info("Attempt # " + i + " to get the Scan Location for : '" + targetPath + "'.");

            matchFound = scanLocationRetrieval(resource, targetPath, versionId, scanLocationIds);
            if (matchFound) {
                break;
            }
            long end = System.currentTimeMillis() - start;
            if (end > 120 * 1000) { // This should check if the loop has been running for 2 minutes. If it has, the
                // exception is thrown.
                throw new BDRestException("Can not find the Scan Location after 2 minutes. Try again later.", resource);
            }
            long minutes = TimeUnit.MILLISECONDS.toMinutes(end);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(end) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(end));

            long milliSeconds = end - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(end));

            String time = String.format("%02dm %02ds %dms", minutes, seconds, milliSeconds);
            logger.info("Scan Location retrieval running for : " + time);
            Thread.sleep(5000); // Check every 5 seconds
        }

    }

    /**
     * Attempts to get the scan location for the Host and Path. Searches throught the results to see if there is an
     * exact match.
     *
     * @param resource
     *            ClientResource
     * @param remoteTargetPath
     *            String
     * @param versionId
     *            String
     * @param scanLocationIds
     *            Map<<String, Boolean>>
     * @return (boolean) MatchFound
     * @throws HubIntegrationException
     */
    private boolean scanLocationRetrieval(ClientResource resource, String remoteTargetPath, String versionId, Map<String, Boolean> scanLocationIds)
            throws HubIntegrationException {
        boolean matchFound = false;
        resource.get();
        String remotePath = remoteTargetPath;
        int responseCode = resource.getResponse().getStatus().getCode();
        try {
            ScanLocationResults results = null;
            if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
                Response resp = resource.getResponse();
                Reader reader = resp.getEntity().getReader();
                BufferedReader bufReader = new BufferedReader(reader);
                StringBuilder sb = new StringBuilder();
                String line = bufReader.readLine();
                while (line != null) {
                    sb.append(line + "\n");
                    line = bufReader.readLine();
                }
                bufReader.close();
                Gson gson = new GsonBuilder().create();
                results = gson.fromJson(sb.toString(), ScanLocationResults.class);

            } else {
                throw new BDRestException("Could not connect to the Hub server with the Given Url and credentials. Error Code: " + responseCode, resource);
            }

            if (results != null && results.getTotalCount() > 0 && results.getItems().size() > 0) {
                // More than one match found
                String path = null;
                // if (results.getItems().size() > 1) {
                for (ScanLocationItem scanMatch : results.getItems()) {

                    path = scanMatch.getPath().trim();

                    // Remove trailing slash from both strings
                    if (path.endsWith("/")) {
                        path = path.substring(0, path.length() - 1);
                    }
                    if (remotePath.endsWith("/")) {
                        remotePath = remotePath.substring(0, remotePath.length() - 1);
                    }
                    logger.debug("Comparing target : '" + remotePath + "' with path : '" + path + "'.");
                    if (remotePath.equals(path)) {
                        logger.debug("MATCHED!");
                        matchFound = true;
                        handleScanLocationMatch(scanLocationIds, scanMatch, remotePath, versionId);
                        break;
                    }
                }
            } else {
                logger.error(
                        "No Scan Location Id could be found for the scan target : '" + remotePath + "'.");
            }
        } catch (IOException e) {
            logger.error(e);
        } catch (BDRestException e) {
            logger.error(e);
        }

        return matchFound;
    }

    /**
     * Checks the scan match to see if it has already been mapped to this version or not.
     *
     * @param scanLocationIds
     *            Map<<String, Boolean>>
     * @param scanMatch
     *            ScanLocationItem
     * @param targetPath
     *            String
     * @param versionId
     *            String
     * @throws HubIntegrationException
     */
    private void handleScanLocationMatch(Map<String, Boolean> scanLocationIds, ScanLocationItem scanMatch, String targetPath, String versionId)
            throws HubIntegrationException {

        if (scanMatch.getAssetReferenceList() != null && !scanMatch.getAssetReferenceList().isEmpty()) {
            boolean scanAlreadyMatched = false;
            for (AssetReferenceItem assetReference : scanMatch.getAssetReferenceList()) {
                if (assetReference.getOwnerEntityKey() != null && StringUtils.isNotBlank(assetReference.getOwnerEntityKey().getEntityId())
                        && assetReference.getOwnerEntityKey().getEntityId().equals(versionId)) {
                    // The owner matches the version we want to map to
                    if (assetReference.getAssetEntityKey() != null && StringUtils.isNotBlank(assetReference.getAssetEntityKey().getEntityId())
                            && assetReference.getAssetEntityKey().getEntityId().equals(scanMatch.getId())) {
                        // The asset Id matches the current scan Id, so this scan is already mapped to the version
                        scanAlreadyMatched = true;
                        break;
                    }
                }
            }
            if (scanAlreadyMatched) {
                String scanId = scanMatch.getId();
                scanLocationIds.put(scanId, true);
                logger.debug("The scan target : '"
                        + targetPath
                        + "' has Scan Location Id: '"
                        + scanId
                        + "'. This is already mapped to the Version with Id: '"
                        + versionId + "'.");
                return;
            } else {
                String scanId = scanMatch.getId();
                logger.debug(
                        "The scan target : '" + targetPath + "' has Scan Location Id: '" + scanId + "'.");
                scanLocationIds.put(scanId, false);
                return;
            }
        }
        String scanId = scanMatch.getId();
        logger.debug(
                "The scan target : '" + targetPath + "' has Scan Location Id: '" + scanId + "'.");
        scanLocationIds.put(scanId, false);

    }

}
