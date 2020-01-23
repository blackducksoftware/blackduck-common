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
package com.synopsys.integration.blackduck.codelocation;

import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.CodeLocationWaitJobTask;
import com.synopsys.integration.blackduck.service.CodeLocationService;
import com.synopsys.integration.blackduck.service.NotificationService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.wait.WaitJob;

import java.util.Set;

public class CodeLocationWaiter {
    private final IntLogger logger;
    private final CodeLocationService codeLocationService;
    private final NotificationService notificationService;

    public CodeLocationWaiter(IntLogger logger, CodeLocationService codeLocationService, NotificationService notificationService) {
        this.logger = logger;
        this.codeLocationService = codeLocationService;
        this.notificationService = notificationService;
    }

    public CodeLocationWaitResult checkCodeLocationsAddedToBom(UserView userView, NotificationTaskRange notificationTaskRange, Set<String> codeLocationNames, int expectedNotificationCount, long timeoutInSeconds)
            throws IntegrationException, InterruptedException {
        CodeLocationWaitJobTask codeLocationWaitJobTask = new CodeLocationWaitJobTask(logger, codeLocationService, notificationService, userView, notificationTaskRange, codeLocationNames, expectedNotificationCount);

        // if a timeout of 0 is provided and the timeout check is done too quickly, w/o a do/while, no check will be performed
        // regardless of the timeout provided, we always want to check at least once
        boolean allCompleted = codeLocationWaitJobTask.isComplete();
        if (!allCompleted) {
            WaitJob waitJob = WaitJob.create(logger, timeoutInSeconds, notificationTaskRange.getTaskStartTime(), 5, codeLocationWaitJobTask);
            allCompleted = waitJob.waitFor();
        }

        if (!allCompleted) {
            return CodeLocationWaitResult.PARTIAL(codeLocationWaitJobTask.getFoundCodeLocationNames(), String.format("It was not possible to verify the code locations were added to the BOM within the timeout (%ds) provided.", timeoutInSeconds));
        } else {
            logger.info("All code locations have been added to the BOM.");
            return CodeLocationWaitResult.COMPLETE(codeLocationWaitJobTask.getFoundCodeLocationNames());
        }
    }

}
