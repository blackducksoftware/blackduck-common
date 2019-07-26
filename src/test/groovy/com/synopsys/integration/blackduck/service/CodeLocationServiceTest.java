package com.synopsys.integration.blackduck.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.rest.request.Request;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.blackduck.api.core.BlackDuckPathMultipleResponses;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;

public class CodeLocationServiceTest {

    public static final String CODELOCATION_NAME_LOWERCASE = "abc";
    public static final String CODELOCATION_NAME_TITLECASE = "Abc";

    @Test
    public void testCodeLocationCaseSensitivity() throws IntegrationException {

        final BlackDuckService blackDuckService = Mockito.mock(BlackDuckService.class);
        final IntLogger logger = new BufferedIntLogger();

        final CodeLocationService svc = new CodeLocationService(blackDuckService, logger);

        final List<CodeLocationView> codeLocations = new ArrayList<>();
        final CodeLocationView mockedCodeLocationWithLowercaseName = Mockito.mock(CodeLocationView.class);
        Mockito.when(mockedCodeLocationWithLowercaseName.getName()).thenReturn(CODELOCATION_NAME_LOWERCASE);
        codeLocations.add(mockedCodeLocationWithLowercaseName);
        Mockito.doReturn(codeLocations).when(blackDuckService).getAllResponses(Mockito.any(BlackDuckPathMultipleResponses.class), Mockito.any(Request.Builder.class));

        final Optional<CodeLocationView> searchForSameCaseResult = svc.getCodeLocationByName(CODELOCATION_NAME_LOWERCASE);
        assertTrue(searchForSameCaseResult.isPresent());
        assertEquals(mockedCodeLocationWithLowercaseName, searchForSameCaseResult.get());

        // Verify that codelocation name check is case sensitive
        final Optional<CodeLocationView> searchForDifferentCaseResult = svc.getCodeLocationByName(CODELOCATION_NAME_TITLECASE);
        assertFalse(searchForDifferentCaseResult.isPresent());
    }
}
