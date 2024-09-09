package com.blackduck.integration.blackduck.codelocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.blackduck.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.core.ResourceMetadata;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.component.VersionBomCodeLocationBomComputedNotificationContent;
import com.synopsys.integration.blackduck.api.manual.view.NotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.VersionBomCodeLocationBomComputedNotificationUserView;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.dataservice.NotificationService;
import com.blackduck.integration.blackduck.service.dataservice.ProjectService;
import com.blackduck.integration.blackduck.service.model.NotificationTaskRange;
import com.blackduck.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.util.NameVersion;

@ExtendWith(TimingExtension.class)
public class CodeLocationWaiterTest {
    @Test
    public void testAllCodeLocationsFoundImmediately() throws InterruptedException, IntegrationException {
        BufferedIntLogger logger = new BufferedIntLogger();

        MockCodeLocationData mockCodeLocationData = twoCodeLocations();

        NotificationService mockNotificationService = Mockito.mock(NotificationService.class);
        NotificationUserView first = createTestNotification("one");
        NotificationUserView second = createTestNotification("two");
        Mockito.when(mockNotificationService.getAllUserNotifications(Mockito.any(), Mockito.any())).thenReturn(Arrays.asList(first, second));

        NotificationTaskRange notificationTaskRange = createTestRange();
        Set<String> codeLocationNames = new HashSet<>(Arrays.asList("one", "two"));

        CodeLocationWaiter codeLocationWaiter = new CodeLocationWaiter(logger, mockCodeLocationData.mockBlackDuckApiClient, mockCodeLocationData.mockProjectService, mockNotificationService);
        CodeLocationWaitResult codeLocationWaitResult = codeLocationWaiter.checkCodeLocationsAddedToBom(new UserView(), notificationTaskRange, mockCodeLocationData.testProjectAndVersion, codeLocationNames, 2, 0, 5);
        assertTrue(CodeLocationWaitResult.Status.COMPLETE == codeLocationWaitResult.getStatus(), "Status was not COMPLETE but was " + codeLocationWaitResult.getStatus());
        assertTrue(codeLocationWaitResult.getCodeLocationNames().contains("one"));
        assertTrue(codeLocationWaitResult.getCodeLocationNames().contains("two"));
        assertFalse(codeLocationWaitResult.getErrorMessage().isPresent());
    }

    @Test
    public void testAllCodeLocationsFoundEventually() throws InterruptedException, IntegrationException {
        BufferedIntLogger logger = new BufferedIntLogger();

        MockCodeLocationData mockCodeLocationData = twoCodeLocations();

        NotificationUserView first = createTestNotification("one");
        NotificationUserView second = createTestNotification("two");

        List<NotificationUserView> initialResponse = Arrays.asList(first);
        List<NotificationUserView> eventualResponse = Arrays.asList(first, second);

        Answer eventuallyFindBoth = new Answer() {
            final long startTime = System.currentTimeMillis();
            final long duration = 5 * 1000;

            @Override
            public Object answer(InvocationOnMock invocation) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - startTime > duration) {
                    return eventualResponse;
                }

                return initialResponse;
            }
        };

        NotificationService mockNotificationService = Mockito.mock(NotificationService.class);
        Mockito.when(mockNotificationService.getAllUserNotifications(Mockito.any(), Mockito.any())).thenAnswer(eventuallyFindBoth);

        NotificationTaskRange notificationTaskRange = createTestRange();
        Set<String> codeLocationNames = new HashSet<>(Arrays.asList("one", "two"));

        CodeLocationWaiter codeLocationWaiter = new CodeLocationWaiter(logger, mockCodeLocationData.mockBlackDuckApiClient, mockCodeLocationData.mockProjectService, mockNotificationService);
        CodeLocationWaitResult codeLocationWaitResult = codeLocationWaiter.checkCodeLocationsAddedToBom(new UserView(), notificationTaskRange, mockCodeLocationData.testProjectAndVersion, codeLocationNames, 2, 7, 5);
        assertTrue(CodeLocationWaitResult.Status.COMPLETE == codeLocationWaitResult.getStatus());
        assertTrue(codeLocationWaitResult.getCodeLocationNames().contains("one"));
        assertTrue(codeLocationWaitResult.getCodeLocationNames().contains("two"));
        assertFalse(codeLocationWaitResult.getErrorMessage().isPresent());
    }

    @Test
    public void testSomeCodeLocationsFoundEventually() throws InterruptedException, IntegrationException {
        BufferedIntLogger logger = new BufferedIntLogger();

        MockCodeLocationData mockCodeLocationData = oneCodeLocation();

        NotificationService mockNotificationService = Mockito.mock(NotificationService.class);
        NotificationUserView first = createTestNotification("one");
        Mockito.when(mockNotificationService.getAllUserNotifications(Mockito.any(), Mockito.any())).thenReturn(Arrays.asList(first));

        NotificationTaskRange notificationTaskRange = createTestRange();
        Set<String> codeLocationNames = new HashSet<>(Arrays.asList("one", "two"));

        CodeLocationWaiter codeLocationWaiter = new CodeLocationWaiter(logger, mockCodeLocationData.mockBlackDuckApiClient, mockCodeLocationData.mockProjectService, mockNotificationService);
        CodeLocationWaitResult codeLocationWaitResult = codeLocationWaiter.checkCodeLocationsAddedToBom(new UserView(), notificationTaskRange, mockCodeLocationData.testProjectAndVersion, codeLocationNames, 2, 7, 5);
        assertTrue(CodeLocationWaitResult.Status.PARTIAL == codeLocationWaitResult.getStatus());
        assertTrue(codeLocationWaitResult.getCodeLocationNames().contains("one"));
        assertTrue(codeLocationWaitResult.getErrorMessage().isPresent());
    }

    @ParameterizedTest
    @CsvSource({
        "20, 30",
        "12, 22"
    })
    public void testTimoutIsObeyed(int timeout, int potentialMaxWait) throws IntegrationException, InterruptedException {
        BufferedIntLogger logger = new BufferedIntLogger();

        ProjectService mockProjectService = Mockito.mock(ProjectService.class);
        Mockito.when(mockProjectService.getProjectVersion(Mockito.any())).thenReturn(Optional.empty());

        NotificationService mockNotificationService = Mockito.mock(NotificationService.class);
        Mockito.when(mockNotificationService.getAllUserNotifications(Mockito.any(), Mockito.any())).thenReturn(Collections.emptyList());

        NotificationTaskRange notificationTaskRange = createTestRange();
        Set<String> codeLocationNames = new HashSet<>(Arrays.asList("one", "two"));

        CodeLocationWaiter codeLocationWaiter = new CodeLocationWaiter(logger, null, mockProjectService, mockNotificationService);
        long testStart = System.currentTimeMillis();
        CodeLocationWaitResult codeLocationWaitResult = codeLocationWaiter.checkCodeLocationsAddedToBom(new UserView(), notificationTaskRange, null, codeLocationNames, 2, timeout, 5);
        long testEnd = System.currentTimeMillis();
        System.out.println((testEnd - testStart) / 1000);
        // it should not timeout BEFORE the timeout, but might take a tiny bit longer.
        assertTrue(timeout <= (testEnd - testStart) / 1000);
        assertTrue(potentialMaxWait > (testEnd - testStart) / 1000);

        assertEquals(CodeLocationWaitResult.Status.PARTIAL, codeLocationWaitResult.getStatus());
        assertNull(logger.getOutputString(LogLevel.ERROR));
        assertNull(logger.getOutputString(LogLevel.WARN));
    }

    private MockCodeLocationData oneCodeLocation() throws IntegrationException {
        List<CodeLocationView> codeLocationViews = new ArrayList<>();
        codeLocationViews.add(createTestView("one"));

        return new MockCodeLocationData(codeLocationViews);
    }

    private MockCodeLocationData twoCodeLocations() throws IntegrationException {
        List<CodeLocationView> codeLocationViews = new ArrayList<>();
        codeLocationViews.add(createTestView("one"));
        codeLocationViews.add(createTestView("two"));

        return new MockCodeLocationData(codeLocationViews);
    }

    private class MockCodeLocationData {
        public ProjectService mockProjectService;
        public BlackDuckApiClient mockBlackDuckApiClient;
        public NameVersion testProjectAndVersion;

        public MockCodeLocationData(List<CodeLocationView> codeLocationViewsToReturn) throws IntegrationException {
            testProjectAndVersion = new NameVersion("testProject", "testProjectVersion");
            ProjectVersionView mockProjectVersionView = Mockito.mock(ProjectVersionView.class);
            ProjectVersionWrapper projectVersionWrapper = new ProjectVersionWrapper(null, mockProjectVersionView);

            mockProjectService = Mockito.mock(ProjectService.class);
            Mockito.when(mockProjectService.getProjectVersion(testProjectAndVersion)).thenReturn(Optional.of(projectVersionWrapper));

            mockBlackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
            Mockito.when(mockBlackDuckApiClient.getAllResponses(mockProjectVersionView.metaCodelocationsLink())).thenReturn(codeLocationViewsToReturn);
        }
    }

    private NotificationTaskRange createTestRange() {
        long startTime = System.currentTimeMillis();
        LocalDateTime localStartTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneOffset.UTC);
        LocalDateTime threeDaysLater = localStartTime.plusDays(3);

        Date startDate = Date.from(localStartTime.atZone(ZoneOffset.UTC).toInstant());
        Date endDate = Date.from(threeDaysLater.atZone(ZoneOffset.UTC).toInstant());

        return new NotificationTaskRange(startTime, startDate, endDate);
    }

    private CodeLocationView createTestView(String name) throws IntegrationException {
        ResourceMetadata meta = new ResourceMetadata();
        meta.setHref(hrefFromName(name));

        CodeLocationView codeLocationView = new CodeLocationView();
        codeLocationView.setName(name);
        codeLocationView.setMeta(meta);

        return codeLocationView;
    }

    private NotificationUserView createTestNotification(String name) throws IntegrationException {
        VersionBomCodeLocationBomComputedNotificationContent content = new VersionBomCodeLocationBomComputedNotificationContent();
        content.setCodeLocation(hrefFromName(name).string());

        VersionBomCodeLocationBomComputedNotificationUserView notificationView = new VersionBomCodeLocationBomComputedNotificationUserView();
        notificationView.setContent(content);

        return notificationView;
    }

    private HttpUrl hrefFromName(String name) throws IntegrationException {
        return new HttpUrl("https://www.blackducksoftware.com/" + name);
    }

}
