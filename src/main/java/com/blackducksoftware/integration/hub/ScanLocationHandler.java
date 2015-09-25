package com.blackducksoftware.integration.hub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.restlet.Response;
import org.restlet.resource.ClientResource;

import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.suite.sdk.logging.IntLogger;

public class ScanLocationHandler {

    private final IntLogger logger;

    public ScanLocationHandler(IntLogger logger) {
        this.logger = logger;
    }

    /**
     * Will attempt to retrieve the scanLocation with the current hostname and target path. If this cannot be found then
     *
     * @param targetPath
     * @param versionId
     * @return
     * @throws UnknownHostException
     * @throws MalformedURLException
     * @throws InterruptedException
     * @throws BDRestException
     * @throws HubIntegrationException
     */
    public void getScanLocationIdWithRetry(ClientResource resource, String remoteTargetPath, String versionId,
            Map<String, Boolean> scanLocationIds)
            throws UnknownHostException, InterruptedException, BDRestException, HubIntegrationException {

        if (resource == null) {
            throw new IllegalArgumentException("Need to provide a ClientResource in order to get the ScanLocation");
        }
        if (StringUtils.isEmpty(remoteTargetPath)) {
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

            logger.info("Attempt # " + i + " to get the Scan Location for : '" + remoteTargetPath + "'.");

            matchFound = scanLocationRetrieval(resource, remoteTargetPath, versionId, scanLocationIds);
            if (matchFound) {
                break;
            }
            long end = System.currentTimeMillis() - start;
            if (end > 120 * 1000) { // This should check if the loop has been running for 2 minutes. If it has, the
                // exception is thrown.
                throw new BDRestException("Can not find the Scan Location after 2 minutes. Try again later.");
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

    private boolean scanLocationRetrieval(ClientResource resource, String remoteTargetPath, String versionId, Map<String, Boolean> scanLocationIds)
            throws HubIntegrationException {
        boolean matchFound = false;
        resource.get();

        int responseCode = resource.getResponse().getStatus().getCode();
        try {
            HashMap<String, Object> responseMap = null;
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
                logger.info(sb.toString());
                // byte[] mapData = sb.toString().getBytes();
                //
                // // Create HashMap from the Rest response
                // ObjectMapper responseMapper = new ObjectMapper();
                // responseMap = responseMapper.readValue(mapData, HashMap.class);
            } else {
                throw new BDRestException("Could not connect to the Hub server with the Given Url and credentials. Error Code: " + responseCode);
            }

            if (responseMap.containsKey("items") && ((ArrayList<LinkedHashMap<String, Object>>) responseMap.get("items")).size() > 0) {
                ArrayList<LinkedHashMap<String, Object>> scanMatchesList = (ArrayList<LinkedHashMap<String, Object>>) responseMap.get("items");
                // More than one match found
                String path = null;
                if (scanMatchesList.size() > 1) {
                    for (LinkedHashMap<String, Object> scanMatch : scanMatchesList) {

                        path = ((String) scanMatch.get("path")).trim();

                        // Remove trailing slash from both strings
                        if (path.endsWith("/")) {
                            path = path.substring(0, path.length() - 1);
                        }
                        if (remoteTargetPath.endsWith("/")) {
                            remoteTargetPath = remoteTargetPath.substring(0, remoteTargetPath.length() - 1);
                        }

                        logger.debug("Comparing target : '" + remoteTargetPath + "' with path : '" + path + "'.");
                        if (remoteTargetPath.equals(path)) {
                            logger.debug("MATCHED!");
                            matchFound = true;
                            handleScanLocationMatch(scanLocationIds, scanMatch, remoteTargetPath, versionId);
                            break;
                        }
                    }
                } else if (scanMatchesList.size() == 1) {
                    LinkedHashMap<String, Object> scanMatch = scanMatchesList.get(0);
                    path = (String) scanMatch.get("path");

                    // Remove trailing slash from both strings
                    if (path.endsWith("/")) {
                        path = path.substring(0, path.length() - 1);
                    }
                    if (remoteTargetPath.endsWith("/")) {
                        remoteTargetPath = remoteTargetPath.substring(0, remoteTargetPath.length() - 1);
                    }

                    logger.debug("Comparing target : '" + remoteTargetPath + "' with path : '" + path + "'.");
                    // trim the path, this way there should be no whitespaces to intefere with the comparison
                    if (remoteTargetPath.equals(path.trim())) {
                        logger.debug("MATCHED!");
                        matchFound = true;
                        handleScanLocationMatch(scanLocationIds, scanMatch, remoteTargetPath, versionId);
                    }
                }
                // if (scanId != null) {
                // return scanId;
                //
                // } else {
                // if (!alreadyMapped) {
                // // TODO perform retry until the scan location is available
                // listener.getLogger().println(
                // "[ERROR] No Scan Location Id could be found for the scan target : '" + targetPath + "'.");
                //
                // }
                // }
            } else {
                logger.error(
                        "No Scan Location Id could be found for the scan target : '" + remoteTargetPath + "'.");
            }
        } catch (IOException e) {
            logger.error(e);
        } catch (BDRestException e) {
            logger.error(e);
        }

        return matchFound;
    }

    private void handleScanLocationMatch(Map<String, Boolean> scanLocationIds, LinkedHashMap<String, Object> scanMatch, String targetPath, String versionId)
            throws HubIntegrationException {
        if (scanMatch.containsKey("assetReferenceList")) {
            Object assetRefObject = scanMatch.get("assetReferenceList");
            ArrayList<LinkedHashMap<String, Object>> assetReferences = (ArrayList<LinkedHashMap<String, Object>>) assetRefObject;
            if (!assetReferences.isEmpty()) {
                boolean scanAlreadyMatched = false;
                for (LinkedHashMap<String, Object> assetReference : assetReferences) {
                    LinkedHashMap<String, Object> ownerEntity = (LinkedHashMap<String, Object>) assetReference.get("ownerEntityKey");
                    if (!ownerEntity.containsKey("entityId")) {
                        logger.error("Owner entity does not have 'entityId' key");
                        Set<String> keys = ownerEntity.keySet();
                        logger.debug("Owner entity has these keys : ");
                        for (String key : keys) {
                            logger.debug("key = " + key);
                        }
                        throw new HubIntegrationException("The scan has an owner but the owner does not have an 'entityId'");
                    } else {
                        String ownerId = (String) ownerEntity.get("entityId");
                        if (ownerId.equals(versionId)) {
                            scanAlreadyMatched = true;
                            break;
                        }
                    }
                }

                if (scanAlreadyMatched) {
                    String scanId = (String) scanMatch.get("id");
                    scanLocationIds.put(scanId, true);
                    logger.debug("The scan target : '"
                            + targetPath
                            + "' has Scan Location Id: '"
                            + scanId
                            + "'. This is already mapped to the Version with Id: '"
                            + versionId + "'.");
                    return;
                } else {
                    String scanId = (String) scanMatch.get("id");
                    logger.debug(
                            "The scan target : '" + targetPath + "' has Scan Location Id: '" + scanId + "'.");
                    scanLocationIds.put(scanId, false);
                    return;
                }

            }
        }
        String scanId = (String) scanMatch.get("id");
        logger.debug(
                "The scan target : '" + targetPath + "' has Scan Location Id: '" + scanId + "'.");
        scanLocationIds.put(scanId, false);

    }
}
