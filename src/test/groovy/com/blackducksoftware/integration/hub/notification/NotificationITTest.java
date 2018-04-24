package com.blackducksoftware.integration.hub.notification;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.core.HubResponse;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.rest.RestConnectionTestHelper;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.NotificationService;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;
import com.blackducksoftware.integration.hub.service.bucket.HubBucketService;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.log.PrintStreamIntLogger;
import com.blackducksoftware.integration.test.annotation.PerformanceTest;

@Category(PerformanceTest.class)
public class NotificationITTest {
    private static final RestConnectionTestHelper restConnectionTestHelper = new RestConnectionTestHelper();
    private final IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);

    @Test
    public void testSynchHubBucketForNotifications() throws Exception {
        final HubServicesFactory services = restConnectionTestHelper.createHubServicesFactory(logger);
        final NotificationService notificationService = services.createNotificationService();
        final HubBucketService bucketService = services.createHubBucketService();
        final HubBucket synchronousBucket = new HubBucket();

        ZonedDateTime endTime = ZonedDateTime.now();
        endTime = endTime.withZoneSameInstant(ZoneOffset.UTC);
        endTime = endTime.withSecond(0).withNano(0);
        final ZonedDateTime startTime = endTime.minusDays(1);
        final Date startDate = Date.from(startTime.toInstant());
        final Date endDate = Date.from(endTime.toInstant());
        final List<NotificationView> notifications = notificationService.getAllNotifications(startDate, endDate);
        final List<CommonNotificationState> commonNotificationList = notificationService.getCommonNotifications(notifications);
        final List<UriSingleResponse<? extends HubResponse>> uriSingleResponses = notificationService.getAllLinks(commonNotificationList);

        final long synchStart = System.currentTimeMillis();
        bucketService.addToTheBucket(synchronousBucket, uriSingleResponses);
        final long synchEnd = System.currentTimeMillis();
        final long synchDiff = Math.abs(synchEnd - synchStart);
        logger.info("Notification count found: " + notifications.size());
        logger.info("Links found: " + uriSingleResponses.size());
        logger.info(String.format("Synchronous start: %d end: %d diff: %d", synchStart, synchEnd, synchDiff));
    }

    @Test
    public void testAsynchHubBucketForNotifications() throws Exception {
        final HubServicesFactory services = restConnectionTestHelper.createHubServicesFactory(logger);
        final NotificationService notificationService = services.createNotificationService();
        final ThreadFactory threadFactory = Executors.defaultThreadFactory();
        final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory);
        final HubBucketService bucketService = services.createHubBucketService(executorService);
        final HubBucket asyncBucket = new HubBucket();

        ZonedDateTime endTime = ZonedDateTime.now();
        endTime = endTime.withZoneSameInstant(ZoneOffset.UTC);
        endTime = endTime.withSecond(0).withNano(0);
        final ZonedDateTime startTime = endTime.minusDays(1);
        final Date startDate = Date.from(startTime.toInstant());
        final Date endDate = Date.from(endTime.toInstant());
        final List<NotificationView> notifications = notificationService.getAllNotifications(startDate, endDate);
        final List<CommonNotificationState> commonNotificationList = notificationService.getCommonNotifications(notifications);
        final List<UriSingleResponse<? extends HubResponse>> uriSingleResponses = notificationService.getAllLinks(commonNotificationList);

        final long asynchStart = System.currentTimeMillis();
        bucketService.addToTheBucket(asyncBucket, uriSingleResponses);
        final long asynchEnd = System.currentTimeMillis();
        final long asynchDiff = Math.abs(asynchEnd - asynchStart);
        logger.info("Notification count found: " + notifications.size());
        logger.info("Links found: " + uriSingleResponses.size());
        logger.info(String.format("Asynchronous start: %d end: %d diff: %d", asynchStart, asynchEnd, asynchDiff));
    }
}
