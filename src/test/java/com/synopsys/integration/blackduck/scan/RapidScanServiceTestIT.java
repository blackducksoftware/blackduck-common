package com.synopsys.integration.blackduck.scan;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.VersionSupport;
import com.synopsys.integration.blackduck.api.enumeration.PolicyRuleConditionOperatorType;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.manual.view.DeveloperScanComponentResultView;
import com.synopsys.integration.blackduck.codelocation.upload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.upload.UploadTarget;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.client.IntHttpClientTestHelper;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.BlackDuckRegistrationService;
import com.synopsys.integration.blackduck.service.dataservice.PolicyRuleService;
import com.synopsys.integration.blackduck.service.model.PolicyRuleExpressionSetBuilder;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.NameVersion;

@Tag("integration")
public class RapidScanServiceTestIT {
    public static final String POLICY_RULE_NAME = "junit vulns overall score";

    private static final IntHttpClientTestHelper intHttpClientTestHelper = new IntHttpClientTestHelper();

    private static BlackDuckServicesFactory blackDuckServicesFactory;
    private static BlackDuckApiClient blackDuckApiClient;
    private static PolicyRuleService policyRuleService;
    private static RapidScanService rapidScanService;

    @BeforeAll
    public static void init() throws IntegrationException {
        blackDuckServicesFactory = intHttpClientTestHelper.createBlackDuckServicesFactory();
        blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();
        policyRuleService = blackDuckServicesFactory.createPolicyRuleService();
        rapidScanService = blackDuckServicesFactory.createRapidScanService();

        if (!policyRuleService.getPolicyRuleViewByName(POLICY_RULE_NAME).isPresent()) {
            PolicyRuleView policyRuleView = createVulnerabilityPolicyRule();
            policyRuleService.createPolicyRule(policyRuleView);
        }

        checkVersionForRapidScan(blackDuckServicesFactory);
    }

    @AfterAll
    public static void cleanup() throws IntegrationException {
        Optional<PolicyRuleView> policyRuleView = policyRuleService.getPolicyRuleViewByName(POLICY_RULE_NAME);
        blackDuckApiClient.delete(policyRuleView.get());
    }

    @Test
    public void testScan() throws Exception {
        File bdioFile = new File(getClass().getResource("/bdio/scans/developerScanTest.bdio").getFile());
        NameVersion projectNameVersion = new NameVersion("RapidScanTest", "1.0.0");
        String codeLocationName = String.format("__CodeLocation_%s_%s", projectNameVersion.getName(), projectNameVersion.getVersion());
        UploadTarget uploadTarget = UploadTarget.createDefault(projectNameVersion, codeLocationName, bdioFile);
        int timeout = intHttpClientTestHelper.getBlackDuckServerConfig().getTimeout();
        List<DeveloperScanComponentResultView> results = rapidScanService.performScan(uploadTarget, timeout, 5);
        assertNotNull(results);
        assertTrue(results.size() > 0);
        assertNotNull(results.get(0).getViolatingPolicyNames());
    }

    @Test
    public void testScanBatch() throws Exception {
        File bdioFile = new File(getClass().getResource("/bdio/scans/developerScanTest.bdio").getFile());
        NameVersion projectNameVersion = new NameVersion("RapidScanTest", "1.0.0");
        String codeLocationName = String.format("__CodeLocation_%s_%s", projectNameVersion.getName(), projectNameVersion.getVersion());
        UploadTarget uploadTarget = UploadTarget.createDefault(projectNameVersion, codeLocationName, bdioFile);
        int timeout = intHttpClientTestHelper.getBlackDuckServerConfig().getTimeout();
        List<DeveloperScanComponentResultView> results = rapidScanService.performScan(new UploadBatch(uploadTarget), timeout, 5);
        assertNotNull(results);
        assertTrue(results.size() > 0);
        assertNotNull(results.get(0).getViolatingPolicyNames());
    }

    @Test
    public void testFileMissingHeader() throws Exception {
        try {
            File bdioFile = new File(getClass().getResource("/bdio/scans/developerScanMissingHeader.bdio").getFile());
            NameVersion projectNameVersion = new NameVersion("RapidScanTest", "1.0.0");
            String codeLocationName = String.format("__CodeLocation_%s_%s", projectNameVersion.getName(), projectNameVersion.getVersion());
            UploadTarget uploadTarget = UploadTarget.createDefault(projectNameVersion, codeLocationName, bdioFile);
            int timeout = intHttpClientTestHelper.getBlackDuckServerConfig().getTimeout();
            rapidScanService.performScan(uploadTarget, timeout);
            fail();
        } catch (BlackDuckIntegrationException ex) {
            // pass
        }
    }

    private static void checkVersionForRapidScan(BlackDuckServicesFactory blackDuckServicesFactory) throws IntegrationException {
        BlackDuckRegistrationService blackDuckRegistrationService = blackDuckServicesFactory.createBlackDuckRegistrationService();
        String version = blackDuckRegistrationService.getBlackDuckServerData().getVersion();
        // ejk - let's fail the test successfully! lolz
        Assumptions.assumeTrue(VersionSupport.isVersionOrLater("2021.6.0", version));
    }

    private static PolicyRuleView createVulnerabilityPolicyRule() throws BlackDuckIntegrationException {
        PolicyRuleExpressionSetBuilder builder = new PolicyRuleExpressionSetBuilder();
        builder.addVulnerabilityOverallScoreCondition(PolicyRuleConditionOperatorType.GE, new BigDecimal("0.5"));
        PolicyRuleExpressionView expressionSet = builder.createPolicyRuleExpressionView();

        PolicyRuleView policyRuleView = new PolicyRuleView();
        policyRuleView.setName(POLICY_RULE_NAME);
        policyRuleView.setEnabled(true);
        policyRuleView.setOverridable(true);
        policyRuleView.setExpression(expressionSet);

        return policyRuleView;
    }

}
