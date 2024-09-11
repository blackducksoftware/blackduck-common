/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation.binaryscanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BinaryScanBatch {
    private final List<BinaryScan> binaryScans = new ArrayList<>();

    public BinaryScanBatch() {
    }

    public BinaryScanBatch(BinaryScan... binaryScans) {
        this.binaryScans.addAll(Arrays.asList(binaryScans));
    }

    public void addBinaryScan(BinaryScan binaryScan) {
        binaryScans.add(binaryScan);
    }

    public List<BinaryScan> getBinaryScans() {
        return binaryScans;
    }

}
