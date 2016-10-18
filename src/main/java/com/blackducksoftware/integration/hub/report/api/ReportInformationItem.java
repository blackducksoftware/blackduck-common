/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.hub.report.api;

import com.blackducksoftware.integration.hub.meta.MetaInformation;

@Deprecated
public class ReportInformationItem extends com.blackducksoftware.integration.hub.api.report.ReportInformationItem {
    // Need this package and the objects for backwards compatability
    public ReportInformationItem(final String reportFormat, final String locale, final String fileName,
            final int fileSize, final String createdAt, final String updatedAt, final String finishedAt,
            final String createdBy, final MetaInformation _meta) {
        super(reportFormat, locale, fileName, fileSize, createdAt, updatedAt, finishedAt, createdBy, _meta);
    }

}
