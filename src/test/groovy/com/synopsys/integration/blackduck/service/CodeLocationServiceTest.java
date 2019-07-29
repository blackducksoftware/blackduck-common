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
    public static final String CODELOCATION_NAME_ALL_CAPS = "ABC";

    @Test
    public void testCodeLocationCaseSensitivity() throws IntegrationException {
        final BlackDuckService blackDuckService = Mockito.mock(BlackDuckService.class);
        final IntLogger logger = new BufferedIntLogger();

        final CodeLocationService codeLocationService = new CodeLocationService(blackDuckService, logger);

        final List<CodeLocationView> codeLocations = new ArrayList<>();
        final CodeLocationView codeLocationWithLowercaseName = new CodeLocationView();
        codeLocationWithLowercaseName.setName(CODELOCATION_NAME_LOWERCASE);
        codeLocations.add(codeLocationWithLowercaseName);
        Mockito.when(blackDuckService.getAllResponses(Mockito.any(BlackDuckPathMultipleResponses.class), Mockito.any(Request.Builder.class))).thenReturn(codeLocations);

        assertSearchResultFound(codeLocationService, CODELOCATION_NAME_LOWERCASE);
        assertSearchResultFound(codeLocationService, CODELOCATION_NAME_TITLECASE);
        assertSearchResultFound(codeLocationService, CODELOCATION_NAME_ALL_CAPS);

        assertSearchResultNotFound(codeLocationService, CODELOCATION_NAME_LOWERCASE + " ");
        assertSearchResultNotFound(codeLocationService, CODELOCATION_NAME_LOWERCASE + CODELOCATION_NAME_LOWERCASE);
        assertSearchResultNotFound(codeLocationService, CODELOCATION_NAME_TITLECASE + CODELOCATION_NAME_TITLECASE);
        assertSearchResultNotFound(codeLocationService, CODELOCATION_NAME_ALL_CAPS + CODELOCATION_NAME_ALL_CAPS);
    }

    private void assertSearchResultFound(CodeLocationService svc, String nameToSearchFor) throws IntegrationException {
        final Optional<CodeLocationView> searchResult = svc.getCodeLocationByName(nameToSearchFor);
        assertTrue(searchResult.isPresent());
        assertEquals(CODELOCATION_NAME_LOWERCASE, searchResult.get().getName());
    }

    private void assertSearchResultNotFound(CodeLocationService svc, String nameToSearchFor) throws IntegrationException {
        final Optional<CodeLocationView> searchResult = svc.getCodeLocationByName(nameToSearchFor);
        assertFalse(searchResult.isPresent());
    }

}
