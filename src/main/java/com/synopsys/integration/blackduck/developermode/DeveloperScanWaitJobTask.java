/**
 * blackduck-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.developermode;

import java.io.IOException;

import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.wait.WaitJobTask;

public class DeveloperScanWaitJobTask implements WaitJobTask {
    private BlackDuckPath resultPath;
    private IntLogger logger;
    private BlackDuckApiClient blackDuckApiClient;

    public DeveloperScanWaitJobTask(final IntLogger logger, final BlackDuckApiClient blackDuckApiClient, BlackDuckPath resultPath) {
        this.logger = logger;
        this.blackDuckApiClient = blackDuckApiClient;
        this.resultPath = resultPath;
    }

    @Override
    public boolean isComplete() throws IntegrationException {
        try (Response response = blackDuckApiClient.get(resultPath)) {
            return response.isStatusCodeSuccess();
        } catch (IOException ex) {
            throw new BlackDuckIntegrationException(ex.getMessage(), ex);
        }
    }
}
