package com.blackduck.integration.blackduck.api.enumeration;

import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.blackduck.api.generated.enumeration.PolicyRuleSeverityType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
