package com.synopsys.integration.blackduck.version;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.synopsys.integration.blackduck.version.BlackDuckVersion;

import org.junit.jupiter.api.Test;

public class BlackDuckVersionTest {

    @Test
    void test() {
        // Different major
        assertTrue((new BlackDuckVersion(2022, 10, 0)).isAtLeast((new BlackDuckVersion(2021, 1, 0))));
        assertFalse((new BlackDuckVersion(2021, 10, 0)).isAtLeast((new BlackDuckVersion(2022, 1, 0))));

        // Different minor
        assertTrue((new BlackDuckVersion(2022, 2, 0)).isAtLeast((new BlackDuckVersion(2022, 1, 0))));
        assertFalse((new BlackDuckVersion(2022, 1, 0)).isAtLeast((new BlackDuckVersion(2022, 2, 0))));

        // Different patch
        assertTrue((new BlackDuckVersion(2022, 2, 1)).isAtLeast((new BlackDuckVersion(2022, 2, 0))));
        assertFalse((new BlackDuckVersion(2022, 2, 1)).isAtLeast((new BlackDuckVersion(2022, 2, 2))));

        // Same
        assertTrue((new BlackDuckVersion(2022, 2, 1)).isAtLeast((new BlackDuckVersion(2022, 2, 1))));
    }
}
