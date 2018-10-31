/**
 * hub-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.synopsys.integration.blackduck.exception.ScanFailedException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.IntEnvironmentVariables;

public class ScanCommandRunner {
    private final IntLogger logger;
    private final IntEnvironmentVariables intEnvironmentVariables;
    private final ScanPathsUtility scanPathsUtility;
    private final Optional<ExecutorService> optionalExecutorService;

    public ScanCommandRunner(final IntLogger logger, final IntEnvironmentVariables intEnvironmentVariables, final ScanPathsUtility scanPathsUtility) {
        this.logger = logger;
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.scanPathsUtility = scanPathsUtility;
        optionalExecutorService = Optional.empty();
    }

    public ScanCommandRunner(final IntLogger logger, final IntEnvironmentVariables intEnvironmentVariables, final ScanPathsUtility scanPathsUtility, final ExecutorService executorService) {
        this.logger = logger;
        this.intEnvironmentVariables = intEnvironmentVariables;
        this.scanPathsUtility = scanPathsUtility;
        optionalExecutorService = Optional.of(executorService);
    }

    public List<ScanCommandOutput> executeScans(final List<ScanCommand> scanCommands, final boolean cleanupOutput) throws ScanFailedException {
        logger.info("Starting the Black Duck Signature Scan commands.");
        final List<ScanCommandOutput> scanCommandOutputs = executeCommands(scanCommands, cleanupOutput);
        logger.info("Completed the Black Duck Signature Scan commands.");

        return scanCommandOutputs;
    }

    private List<ScanCommandOutput> executeCommands(final List<ScanCommand> scanCommands, final boolean cleanupOutput) throws ScanFailedException {
        final List<ScanCommandOutput> scanCommandOutputs = new ArrayList<>();

        try {
            final List<ScanCommandCallable> callables = createCallables(scanCommands, cleanupOutput);
            if (optionalExecutorService.isPresent()) {
                final ExecutorService executorService = optionalExecutorService.get();
                final List<Future<ScanCommandOutput>> submitted = new ArrayList<>();
                for (final ScanCommandCallable callable : callables) {
                    submitted.add(executorService.submit(callable));
                }
                for (final Future<ScanCommandOutput> future : submitted) {
                    final ScanCommandOutput scanCommandOutput = future.get();
                    if (scanCommandOutput != null) {
                        scanCommandOutputs.add(scanCommandOutput);
                    }
                }
            } else {
                for (final ScanCommandCallable callable : callables) {
                    scanCommandOutputs.add(callable.call());
                }
            }
        } catch (final Exception e) {
            throw new ScanFailedException(String.format("Encountered a problem waiting for a scan to finish. %s", e.getMessage()), e);
        }

        return scanCommandOutputs;
    }

    private List<ScanCommandCallable> createCallables(final List<ScanCommand> scanCommands, final boolean cleanupOutput) {
        final List<ScanCommandCallable> callables = scanCommands.stream().map(scanCommand -> new ScanCommandCallable(logger, scanPathsUtility, intEnvironmentVariables, scanCommand, cleanupOutput)).collect(Collectors.toList());

        return callables;
    }

}
