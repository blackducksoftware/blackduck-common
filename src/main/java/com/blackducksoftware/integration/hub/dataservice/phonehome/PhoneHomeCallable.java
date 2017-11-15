/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.dataservice.phonehome;

import java.util.concurrent.Callable;

import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.phonehome.PhoneHomeClient;
import com.blackducksoftware.integration.phonehome.PhoneHomeRequestBody;

public class PhoneHomeCallable implements Callable<Boolean> {
    private final IntLogger logger;
    private final PhoneHomeClient client;
    private final PhoneHomeRequestBody requestBody;

    public PhoneHomeCallable(final IntLogger logger, final PhoneHomeClient client, final PhoneHomeRequestBody requestBody) {
        this.logger = logger;
        this.client = client;
        this.requestBody = requestBody;
    }

    @Override
    public Boolean call() throws Exception {
        Boolean result = Boolean.FALSE;
        try {
            client.postPhoneHomeRequest(requestBody);
            result = Boolean.TRUE;
        } catch (final Exception ex) {
            logger.debug("Phone home error.", ex);
        }

        return result;
    }
}
