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
package com.synopsys.integration.blackduck.codelocation;

import java.util.Set;

import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;

public class CodeLocationCreationData<T> {
    private final NotificationTaskRange notificationTaskRange;
    private final Set<String> codeLocationNames;
    private final T output;

    public CodeLocationCreationData(final NotificationTaskRange notificationTaskRange, final Set<String> codeLocationNames, final T output) {
        this.notificationTaskRange = notificationTaskRange;
        this.codeLocationNames = codeLocationNames;
        this.output = output;
    }

    public NotificationTaskRange getNotificationTaskRange() {
        return notificationTaskRange;
    }

    public Set<String> getCodeLocationNames() {
        return codeLocationNames;
    }

    public T getOutput() {
        return output;
    }

}
