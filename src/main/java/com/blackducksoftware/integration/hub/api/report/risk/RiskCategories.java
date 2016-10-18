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
package com.blackducksoftware.integration.hub.api.report.risk;

public class RiskCategories {

    private final RiskCounts VULNERABILITY;

    private final RiskCounts ACTIVITY;

    private final RiskCounts VERSION;

    private final RiskCounts LICENSE;

    private final RiskCounts OPERATIONAL;

    public RiskCategories(final RiskCounts VULNERABILITY, final RiskCounts ACTIVITY, final RiskCounts VERSION,
            final RiskCounts LICENSE, final RiskCounts OPERATIONAL) {
        this.VULNERABILITY = VULNERABILITY;
        this.ACTIVITY = ACTIVITY;
        this.VERSION = VERSION;
        this.LICENSE = LICENSE;
        this.OPERATIONAL = OPERATIONAL;
    }

    public RiskCounts getVULNERABILITY() {
        return VULNERABILITY;
    }

    public RiskCounts getACTIVITY() {
        return ACTIVITY;
    }

    public RiskCounts getVERSION() {
        return VERSION;
    }

    public RiskCounts getLICENSE() {
        return LICENSE;
    }

    public RiskCounts getOPERATIONAL() {
        return OPERATIONAL;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ACTIVITY == null) ? 0 : ACTIVITY.hashCode());
        result = prime * result + ((LICENSE == null) ? 0 : LICENSE.hashCode());
        result = prime * result + ((OPERATIONAL == null) ? 0 : OPERATIONAL.hashCode());
        result = prime * result + ((VERSION == null) ? 0 : VERSION.hashCode());
        result = prime * result + ((VULNERABILITY == null) ? 0 : VULNERABILITY.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof RiskCategories)) {
            return false;
        }
        final RiskCategories other = (RiskCategories) obj;
        if (ACTIVITY == null) {
            if (other.ACTIVITY != null) {
                return false;
            }
        } else if (!ACTIVITY.equals(other.ACTIVITY)) {
            return false;
        }
        if (LICENSE == null) {
            if (other.LICENSE != null) {
                return false;
            }
        } else if (!LICENSE.equals(other.LICENSE)) {
            return false;
        }
        if (OPERATIONAL == null) {
            if (other.OPERATIONAL != null) {
                return false;
            }
        } else if (!OPERATIONAL.equals(other.OPERATIONAL)) {
            return false;
        }
        if (VERSION == null) {
            if (other.VERSION != null) {
                return false;
            }
        } else if (!VERSION.equals(other.VERSION)) {
            return false;
        }
        if (VULNERABILITY == null) {
            if (other.VULNERABILITY != null) {
                return false;
            }
        } else if (!VULNERABILITY.equals(other.VULNERABILITY)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("RiskCategory [VULNERABILITY=");
        builder.append(VULNERABILITY);
        builder.append(", ACTIVITY=");
        builder.append(ACTIVITY);
        builder.append(", VERSION=");
        builder.append(VERSION);
        builder.append(", LICENSE=");
        builder.append(LICENSE);
        builder.append(", OPERATIONAL=");
        builder.append(OPERATIONAL);
        builder.append("]");
        return builder.toString();
    }

}
