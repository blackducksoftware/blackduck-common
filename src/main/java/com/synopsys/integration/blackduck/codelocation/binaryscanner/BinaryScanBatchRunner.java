package com.synopsys.integration.blackduck.codelocation.binaryscanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.log.IntLogger;

public class BinaryScanBatchRunner {
    private final IntLogger logger;
    private final BlackDuckService blackDuckService;
    private final Optional<ExecutorService> optionalExecutorService;

    public BinaryScanBatchRunner(IntLogger logger, BlackDuckService blackDuckService) {
        this.logger = logger;
        this.blackDuckService = blackDuckService;
        optionalExecutorService = Optional.empty();
    }

    public BinaryScanBatchRunner(IntLogger logger, BlackDuckService blackDuckService, ExecutorService executorService) {
        this.logger = logger;
        this.blackDuckService = blackDuckService;
        optionalExecutorService = Optional.of(executorService);
    }

    public BinaryScanBatchOutput executeUploads(BinaryScanBatch binaryScanBatch) throws BlackDuckIntegrationException {
        logger.info("Starting the binary scan file uploads.");
        BinaryScanBatchOutput binaryScanBatchOutput = uploadFiles(binaryScanBatch);
        logger.info("Completed the binary scan file uploads.");

        return binaryScanBatchOutput;
    }

    private BinaryScanBatchOutput uploadFiles(BinaryScanBatch binaryScanBatch) throws BlackDuckIntegrationException {
        List<BinaryScanOutput> uploadOutputs = new ArrayList<>();

        try {
            List<BinaryScanCallable> callables = createCallables(binaryScanBatch);
            if (optionalExecutorService.isPresent()) {
                ExecutorService executorService = optionalExecutorService.get();
                List<Future<BinaryScanOutput>> submitted = new ArrayList<>();
                for (BinaryScanCallable callable : callables) {
                    submitted.add(executorService.submit(callable));
                }
                for (Future<BinaryScanOutput> future : submitted) {
                    BinaryScanOutput uploadOutput = future.get();
                    uploadOutputs.add(uploadOutput);
                }
            } else {
                for (BinaryScanCallable callable : callables) {
                    uploadOutputs.add(callable.call());
                }
            }
        } catch (Exception e) {
            throw new BlackDuckIntegrationException(String.format("Encountered a problem uploading a binary file: %s", e.getMessage()), e);
        }

        return new BinaryScanBatchOutput(uploadOutputs);
    }

    private List<BinaryScanCallable> createCallables(BinaryScanBatch uploadBatch) {
        List<BinaryScanCallable> callables =
                uploadBatch
                        .getBinaryScans()
                        .stream()
                        .map(binaryScan -> new BinaryScanCallable(blackDuckService, binaryScan))
                        .collect(Collectors.toList());

        return callables;
    }
}
