package com.synopsys.integration.blackduck.codelocation.signaturescanner;

import java.util.Set;

import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationData;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class SignatureScannerService extends DataService {
    private final ScanBatchManager scanBatchManager;
    private final CodeLocationCreationService codeLocationCreationService;

    public SignatureScannerService(final BlackDuckService blackDuckService, final IntLogger logger,
            final ScanBatchManager scanBatchManager, final CodeLocationCreationService codeLocationCreationService) {
        super(blackDuckService, logger);
        this.scanBatchManager = scanBatchManager;
        this.codeLocationCreationService = codeLocationCreationService;
    }

    public CodeLocationCreationData<ScanBatchOutput> performSignatureScan(final SignatureScannerCodeLocationCreationRequest scanRequest) throws IntegrationException {
        return codeLocationCreationService.createCodeLocations(scanRequest);
    }

    public CodeLocationCreationData<ScanBatchOutput> performSignatureScan(final ScanBatch scanBatch) throws IntegrationException {
        final SignatureScannerCodeLocationCreationRequest scanRequest = new SignatureScannerCodeLocationCreationRequest(scanBatchManager, scanBatch);

        return performSignatureScan(scanRequest);
    }

    public ScanBatchOutput performSignatureScanAndWait(final SignatureScannerCodeLocationCreationRequest scanRequest, final long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return codeLocationCreationService.createCodeLocationsAndWait(scanRequest, timeoutInSeconds);
    }

    public ScanBatchOutput performSignatureScanAndWait(final ScanBatch scanBatch, final long timeoutInSeconds) throws IntegrationException, InterruptedException {
        final SignatureScannerCodeLocationCreationRequest scanRequest = new SignatureScannerCodeLocationCreationRequest(scanBatchManager, scanBatch);

        return performSignatureScanAndWait(scanRequest, timeoutInSeconds);
    }

    public void waitForBdioUpload(final NotificationTaskRange notificationTaskRange, final Set<String> codeLocationNames, final long timeoutInSeconds) throws IntegrationException, InterruptedException {
        codeLocationCreationService.waitForCodeLocations(notificationTaskRange, codeLocationNames, timeoutInSeconds);
    }

}
