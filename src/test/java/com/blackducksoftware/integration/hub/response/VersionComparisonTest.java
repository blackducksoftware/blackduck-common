package com.blackducksoftware.integration.hub.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.VersionComparison;

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

        EqualsVerifier.forClass(VersionComparison.class).suppress(Warning.STRICT_INHERITANCE).verify();

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        StringBuilder builder = new StringBuilder();
        builder.append("VersionComparison [consumerVersion=");
        builder.append(item1.getConsumerVersion());
        builder.append(", producerVersion=");
        builder.append(item1.getProducerVersion());
        builder.append(", numericResult=");
        builder.append(item1.getNumericResult());
        builder.append(", operatorResult=");
        builder.append(item1.getOperatorResult());
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }

}
