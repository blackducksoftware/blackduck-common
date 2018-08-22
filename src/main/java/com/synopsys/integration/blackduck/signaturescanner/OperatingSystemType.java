package com.synopsys.integration.blackduck.signaturescanner;

import org.apache.commons.lang3.SystemUtils;

import com.synopsys.integration.util.EnumUtils;

public enum OperatingSystemType {
    LINUX,
    MAC,
    WINDOWS;

    public static OperatingSystemType determineFromSystem() {
        if (SystemUtils.IS_OS_MAC) {
            return MAC;
        } else if (SystemUtils.IS_OS_WINDOWS) {
            return WINDOWS;
        } else {
            return LINUX;
        }
    }

    public String prettyPrint() {
        return EnumUtils.prettyPrint(this);
    }

}
