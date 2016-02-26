package com.blackducksoftware.integration.hub.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class VersionComparisonTest {

    @Test
    public void testVersionComparison() {
        final String consumerVersion1 = "comsumerVersion1";
        final String producerVersion1 = "producerVersion1";
        final Integer numericResult1 = 1;
        final String operatorResult1 = "operator1";

        final String consumerVersion2 = "comsumerVersion2";
        final String producerVersion2 = "producerVersion2";
        final Integer numericResult2 = 2;
        final String operatorResult2 = "operator2";

        VersionComparison item1 = new VersionComparison(consumerVersion1, producerVersion1, numericResult1, operatorResult1);
        VersionComparison item2 = new VersionComparison(consumerVersion2, producerVersion2, numericResult2, operatorResult2);
        VersionComparison item3 = new VersionComparison(consumerVersion1, producerVersion1, numericResult1, operatorResult1);

        assertEquals(consumerVersion1, item1.getConsumerVersion());
        assertEquals(producerVersion1, item1.getProducerVersion());
        assertEquals(numericResult1, item1.getNumericResult());
        assertEquals(operatorResult1, item1.getOperatorResult());

        assertEquals(consumerVersion2, item2.getConsumerVersion());
        assertEquals(producerVersion2, item2.getProducerVersion());
        assertEquals(numericResult2, item2.getNumericResult());
        assertEquals(operatorResult2, item2.getOperatorResult());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());
    }

}
