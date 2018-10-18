package com.synopsys.integration.blackduck.service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.NotificationView;
import com.synopsys.integration.blackduck.exception.DoesNotExistException;
import com.synopsys.integration.blackduck.exception.HubTimeoutExceededException;
import com.synopsys.integration.blackduck.signaturescanner.ScanJob;
import com.synopsys.integration.blackduck.signaturescanner.ScanJobManager;
import com.synopsys.integration.blackduck.signaturescanner.ScanJobOutput;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class ScanService extends DataService {
    private final JsonParser jsonParser;
    private final Gson gson;
    private final CodeLocationService codeLocationService;
    private final NotificationService notificationService;
    private final ScanJobManager scanJobManager;

    public ScanService(final HubService hubService, final IntLogger logger, final JsonParser jsonParser, final Gson gson, final CodeLocationService codeLocationService, final NotificationService notificationService,
            final ScanJobManager scanJobManager) {
        super(hubService, logger);
        this.jsonParser = jsonParser;
        this.gson = gson;
        this.codeLocationService = codeLocationService;
        this.notificationService = notificationService;
        this.scanJobManager = scanJobManager;
    }

    // this will only work with pre-named code locations
    public void performScans(final ScanJob scanJob, final long postScanTimeoutInSeconds) throws IntegrationException, IOException, InterruptedException {
        final Set<String> codeLocationNames = scanJob.getScanTargets()
                                                      .stream()
                                                      .map(scanTarget -> scanTarget.getCodeLocationName())
                                                      .filter(s -> StringUtils.isNotBlank(s))
                                                      .collect(Collectors.toSet());
        if (codeLocationNames.size() != scanJob.getScanTargets().size()) {
            throw new IntegrationException("All scanTargets need to have a non-blank name.");
        }

        final long startTime = System.currentTimeMillis();
        final LocalDateTime localStartTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneOffset.UTC);
        final LocalDateTime twentyFourHoursLater = localStartTime.plusDays(1);

        final Date startDate = notificationService.getLatestNotificationDate();
        final Date endDate = Date.from(twentyFourHoursLater.atZone(ZoneOffset.UTC).toInstant());

        final ScanJobOutput scanJobOutput = scanJobManager.executeScans(scanJob);

        boolean allCompleted = false;
        int attemptCount = 1;
        while (!allCompleted && System.currentTimeMillis() - startTime <= postScanTimeoutInSeconds * 1000) {
            final List<CodeLocationView> codeLocations = new ArrayList<>();
            for (final String codeLocationName : codeLocationNames) {
                try {
                    final CodeLocationView codeLocationView = codeLocationService.getCodeLocationByName(codeLocationName);
                    codeLocations.add(codeLocationView);
                } catch (final DoesNotExistException ignoreBecauseItIsExpected) {
                    // ignored - the code locations may not exist yet, hence the searching and timeout
                }
            }

            if (codeLocations.size() == codeLocationNames.size()) {
                logger.debug("All code locations have been found, now looking for notifications.");
                final Set<String> codeLocationUrlsToFind = codeLocations
                                                                   .stream()
                                                                   .map(codeLocation -> codeLocation._meta.href)
                                                                   .collect(Collectors.toSet());
                final Set<String> codeLocationUrls = new HashSet<>();
                final List<NotificationView> notifications = notificationService.getFilteredNotifications(startDate, endDate, Arrays.asList(NotificationType.VERSION_BOM_CODE_LOCATION_BOM_COMPUTED.name()));
                logger.debug(String.format("There were %d notifications found.", notifications.size()));

                for (final NotificationView notificationView : notifications) {
                    final Optional<String> codeLocationUrl = getCodeLocationUrl(notificationView);
                    if (codeLocationUrl.isPresent()) {
                        codeLocationUrls.add(codeLocationUrl.get());
                    }
                }

                if (codeLocationUrls.containsAll(codeLocationUrlsToFind)) {
                    allCompleted = true;
                } else {
                    attemptCount++;
                    logger.info(String.format("All scans have not been added to the BOM yet, waiting another 5 seconds (try #%d)...", attemptCount));
                    Thread.sleep(5000);
                }
            }
        }

        if (!allCompleted) {
            throw new HubTimeoutExceededException(String.format("It was not possible to verify the scans were added to the BOM within the timeout (%ds) provided.", postScanTimeoutInSeconds));
        } else {
            logger.info("All scans have been added to the BOM.");
        }
    }

    private Optional<String> getCodeLocationUrl(final NotificationView notificationView) {
        try {
            final JsonElement jsonElement = jsonParser.parse(notificationView.json);
            if (jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has("content")) {
                final JsonElement content = jsonElement.getAsJsonObject().get("content");
                if (content.isJsonObject() && content.getAsJsonObject().has("codeLocation")) {
                    final JsonElement codeLocation = content.getAsJsonObject().get("codeLocation");
                    final String codeLocationUrl = codeLocation.getAsString();
                    if (StringUtils.isNotBlank(codeLocationUrl)) {
                        return Optional.of(codeLocationUrl);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Error processing the json - it might not be possible to verify if the code location is added to the BOM.");
            logger.error(e);
        }

        return Optional.empty();
    }

}
