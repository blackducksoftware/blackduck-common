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
package com.blackducksoftware.integration.hub.api.report;

import java.util.Date;

import com.blackducksoftware.integration.hub.model.view.HubView;

public class ReportView extends HubView {
    private String reportFormat;

    private String locale;

    private String fileName;

    private int fileSize;

    private Date createdAt;

    private Date updatedAt;

    private Date finishedAt;

    private String createdBy;

    public String getReportFormat() {
        return reportFormat;
    }

    public String getLocale() {
        return locale;
    }

    public String getFileName() {
        return fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public Date getFinishedAt() {
        return finishedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

}
