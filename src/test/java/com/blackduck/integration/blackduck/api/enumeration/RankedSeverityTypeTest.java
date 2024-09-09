package com.blackduck.integration.blackduck.api.enumeration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import com.blackduck.integration.blackduck.TimingExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyRuleSeverityType;

@ExtendWith(TimingExtension.class)
public class RankedSeverityTypeTest {
    @Test
    public void testAllTypesAreRanked() {
        List<PolicyRuleSeverityType> allPolicyRuleSeverityTypes = Arrays.asList(PolicyRuleSeverityType.values());
        List<RankedSeverityType> allRankedSeverityTypes = Arrays.asList(RankedSeverityType.values());

        // if a new severity type is added, we will need to manage our custom ranking
        assertEquals(allPolicyRuleSeverityTypes.size(), allRankedSeverityTypes.size());
    }

}
