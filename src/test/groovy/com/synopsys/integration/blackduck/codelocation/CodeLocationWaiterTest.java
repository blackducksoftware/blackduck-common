package com.synopsys.integration.blackduck.codelocation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.api.generated.component.ResourceMetadata;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.view.NotificationUserView;
import com.synopsys.integration.blackduck.service.CodeLocationService;
import com.synopsys.integration.blackduck.service.NotificationService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;

@ExtendWith(TimingExtension.class)
public class CodeLocationWaiterTest {
    @Test
    public void testAllCodeLocationsFoundImmediately() throws InterruptedException, IntegrationException {
        BufferedIntLogger logger = new BufferedIntLogger();
        CodeLocationService mockCodeLocationService = Mockito.mock(CodeLocationService.class);
        Mockito.when(mockCodeLocationService.getCodeLocationByName("one")).thenReturn(Optional.of(createTestView("one")));
        Mockito.when(mockCodeLocationService.getCodeLocationByName("two")).thenReturn(Optional.of(createTestView("two")));

        NotificationService mockNotificationService = Mockito.mock(NotificationService.class);
        NotificationUserView first = createTestNotification("one");
        NotificationUserView second = createTestNotification("two");
        Mockito.when(mockNotificationService.getFilteredUserNotifications(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyList())).thenReturn(Arrays.asList(first, second));

        NotificationTaskRange notificationTaskRange = createTestRange();
        Set<String> codeLocationNames = new HashSet<>(Arrays.asList("one", "two"));

        CodeLocationWaiter codeLocationWaiter = new CodeLocationWaiter(logger, mockCodeLocationService, mockNotificationService);
        CodeLocationWaitResult codeLocationWaitResult = codeLocationWaiter.checkCodeLocationsAddedToBom(new UserView(), notificationTaskRange, codeLocationNames, 2);
        assertTrue(CodeLocationWaitResult.Status.COMPLETE == codeLocationWaitResult.getStatus());
        assertTrue(codeLocationWaitResult.getCodeLocationNames().contains("one"));
        assertTrue(codeLocationWaitResult.getCodeLocationNames().contains("two"));
        assertFalse(codeLocationWaitResult.getErrorMessage().isPresent());
    }

    @Test
    public void testAllCodeLocationsFoundEventually() throws InterruptedException, IntegrationException {
        BufferedIntLogger logger = new BufferedIntLogger();
        CodeLocationService mockCodeLocationService = Mockito.mock(CodeLocationService.class);
        Mockito.when(mockCodeLocationService.getCodeLocationByName("one")).thenReturn(Optional.of(createTestView("one")));
        Mockito.when(mockCodeLocationService.getCodeLocationByName("two")).thenReturn(Optional.of(createTestView("two")));

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
        Mockito.when(mockNotificationService.getFilteredUserNotifications(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyList())).thenAnswer(eventuallyFindBoth);

        NotificationTaskRange notificationTaskRange = createTestRange();
        Set<String> codeLocationNames = new HashSet<>(Arrays.asList("one", "two"));

        CodeLocationWaiter codeLocationWaiter = new CodeLocationWaiter(logger, mockCodeLocationService, mockNotificationService);
        CodeLocationWaitResult codeLocationWaitResult = codeLocationWaiter.checkCodeLocationsAddedToBom(new UserView(), notificationTaskRange, codeLocationNames, 7);
        assertTrue(CodeLocationWaitResult.Status.COMPLETE == codeLocationWaitResult.getStatus());
        assertTrue(codeLocationWaitResult.getCodeLocationNames().contains("one"));
        assertTrue(codeLocationWaitResult.getCodeLocationNames().contains("two"));
        assertFalse(codeLocationWaitResult.getErrorMessage().isPresent());
    }

    @Test
    public void testSomeCodeLocationsFoundEventually() throws InterruptedException, IntegrationException {
        BufferedIntLogger logger = new BufferedIntLogger();
        CodeLocationService mockCodeLocationService = Mockito.mock(CodeLocationService.class);
        Mockito.when(mockCodeLocationService.getCodeLocationByName("one")).thenReturn(Optional.of(createTestView("one")));
        Mockito.when(mockCodeLocationService.getCodeLocationByName("two")).thenReturn(Optional.empty());

        NotificationService mockNotificationService = Mockito.mock(NotificationService.class);
        NotificationUserView first = createTestNotification("one");
        Mockito.when(mockNotificationService.getFilteredUserNotifications(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyList())).thenReturn(Arrays.asList(first));

        NotificationTaskRange notificationTaskRange = createTestRange();
        Set<String> codeLocationNames = new HashSet<>(Arrays.asList("one", "two"));

        CodeLocationWaiter codeLocationWaiter = new CodeLocationWaiter(logger, mockCodeLocationService, mockNotificationService);
        CodeLocationWaitResult codeLocationWaitResult = codeLocationWaiter.checkCodeLocationsAddedToBom(new UserView(), notificationTaskRange, codeLocationNames, 2);
        assertTrue(CodeLocationWaitResult.Status.PARTIAL == codeLocationWaitResult.getStatus());
        assertTrue(codeLocationWaitResult.getCodeLocationNames().contains("one"));
        assertTrue(codeLocationWaitResult.getErrorMessage().isPresent());
    }

    private NotificationTaskRange createTestRange() {
        long startTime = System.currentTimeMillis();
        LocalDateTime localStartTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneOffset.UTC);
        LocalDateTime threeDaysLater = localStartTime.plusDays(3);

        Date startDate = Date.from(localStartTime.atZone(ZoneOffset.UTC).toInstant());
        Date endDate = Date.from(threeDaysLater.atZone(ZoneOffset.UTC).toInstant());

        return new NotificationTaskRange(startTime, startDate, endDate);
    }

    private CodeLocationView createTestView(String name) {
        ResourceMetadata meta = new ResourceMetadata();
        meta.setHref(hrefFromName(name));

        CodeLocationView codeLocationView = new CodeLocationView();
        codeLocationView.setName(name);
        codeLocationView.setMeta(meta);

        return codeLocationView;
    }

    private NotificationUserView createTestNotification(String name) {
        NotificationUserView notificationView = new NotificationUserView();
        notificationView.setJson("{\"content\": {\"codeLocation\": \"" + hrefFromName(name) + "\"}}");
        return notificationView;
    }

    private String hrefFromName(String name) {
        return name + "href";
    }

}
