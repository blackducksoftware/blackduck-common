package com.synopsys.integration.blackduck.signaturescanner;

import java.util.List;

import com.synopsys.integration.blackduck.signaturescanner.command.ScanCommandOutput;

public class ScanJobOutput {
    private final List<ScanCommandOutput> scanCommandOutputs;

    public ScanJobOutput(final List<ScanCommandOutput> scanCommandOutputs) {
        this.scanCommandOutputs = scanCommandOutputs;
    }

    public List<ScanCommandOutput> getScanCommandOutputs() {
        return scanCommandOutputs;
    }

}
