package com.synopsys.integration.blackduck.codelocation.binaryscanner;

import java.util.List;

import com.synopsys.integration.blackduck.codelocation.CodeLocationBatchOutput;

public class BinaryScanBatchOutput extends CodeLocationBatchOutput<BinaryScanOutput> {
    public BinaryScanBatchOutput(List<BinaryScanOutput> outputs) {
        super(outputs);
    }

}
