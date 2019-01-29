package com.synopsys.integration.blackduck.codelocation.binaryscanner;

import com.synopsys.integration.blackduck.codelocation.CodeLocationOutput;
import com.synopsys.integration.blackduck.codelocation.Result;

public class BinaryScanOutput extends CodeLocationOutput {
    public static BinaryScanOutput SUCCESS(String codeLocationName) {
        return new BinaryScanOutput(codeLocationName, Result.SUCCESS, null, null);
    }

    public static BinaryScanOutput FAILURE(String codeLocationName, String errorMessage, Exception exception) {
        return new BinaryScanOutput(codeLocationName, Result.FAILURE, errorMessage, exception);
    }

    private BinaryScanOutput(String codeLocationName, Result result, String errorMessage, Exception exception) {
        super(result, codeLocationName, errorMessage, exception);
    }

}
