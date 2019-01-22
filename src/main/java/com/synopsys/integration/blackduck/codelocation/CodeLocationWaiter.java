package com.synopsys.integration.blackduck.codelocation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.jayway.jsonpath.JsonPath;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.NotificationView;
import com.synopsys.integration.blackduck.service.CodeLocationService;
import com.synopsys.integration.blackduck.service.NotificationService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class CodeLocationWaiter {
    private final IntLogger logger;
    private final CodeLocationService codeLocationService;
    private final NotificationService notificationService;

    private final Set<String> foundCodeLocationNames = new HashSet<>();
    private final Map<String, CodeLocationView> codeLocationNamesToViews = new HashMap<>();
    private final Map<String, String> codeLocationUrlsToNames = new HashMap<>();

    public CodeLocationWaiter(IntLogger logger, CodeLocationService codeLocationService, NotificationService notificationService) {
        this.logger = logger;
        this.codeLocationService = codeLocationService;
        this.notificationService = notificationService;
    }

    public CodeLocationWaitResult checkCodeLocationsAddedToBom(NotificationTaskRange notificationTaskRange, Set<String> codeLocationNames, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        boolean allCompleted = false;
        int attemptCount = 1;
        while (!allCompleted && System.currentTimeMillis() - notificationTaskRange.getTaskStartTime() <= timeoutInSeconds * 1000) {
            for (String codeLocationName : codeLocationNames) {
                if (!codeLocationNamesToViews.containsKey(codeLocationName)) {
                    Optional<CodeLocationView> codeLocationView = codeLocationService.getCodeLocationByName(codeLocationName);
                    if (codeLocationView.flatMap(CodeLocationView::getHref).isPresent()) {
                        codeLocationNamesToViews.put(codeLocationName, codeLocationView.get());
                        codeLocationUrlsToNames.put(codeLocationView.get().getHref().get(), codeLocationName);
                    }
                }
            }

            if (codeLocationNamesToViews.size() > 0) {
                logger.debug("At least one code location has been found, now looking for notifications.");
                List<NotificationView> notifications = notificationService
                                                               .getFilteredNotifications(notificationTaskRange.getStartDate(), notificationTaskRange.getEndDate(),
                                                                       Arrays.asList(NotificationType.VERSION_BOM_CODE_LOCATION_BOM_COMPUTED.name()));
                logger.debug(String.format("There were %d notifications found.", notifications.size()));

                for (NotificationView notificationView : notifications) {
                    Optional<String> codeLocationUrl = getCodeLocationUrl(notificationView);
                    if (codeLocationUrl.isPresent() && codeLocationUrlsToNames.containsKey(codeLocationUrl.get())) {
                        foundCodeLocationNames.add(codeLocationUrlsToNames.get(codeLocationUrl.get()));
                    }
                }

                if (foundCodeLocationNames.containsAll(codeLocationNames)) {
                    allCompleted = true;
                } else {
                    attemptCount++;
                    logger.info(String.format("All code locations have not been added to the BOM yet, waiting another 5 seconds (try #%d)...", attemptCount));
                    Thread.sleep(5000);
                }
            }
        }

        if (!allCompleted) {
            return CodeLocationWaitResult.PARTIAL(foundCodeLocationNames, String.format("It was not possible to verify the code locations were added to the BOM within the timeout (%ds) provided.", timeoutInSeconds));
        } else {
            logger.info("All code locations have been added to the BOM.");
            return CodeLocationWaitResult.COMPLETE(foundCodeLocationNames);
        }
    }

    private Optional<String> getCodeLocationUrl(NotificationView notificationView) {
        String codeLocationUrl = JsonPath.read(notificationView.getJson(), "$.content.codeLocation");
        return Optional.ofNullable(codeLocationUrl);
    }

}
