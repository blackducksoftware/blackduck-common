package com.blackducksoftware.integration.hub.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;

import com.blackducksoftware.integration.hub.project.api.AutoCompleteItem;

public class AutoCompleteItemTest {

    @Test
    public void testAutoCompleteItem() {
        final String value1 = "Value1";
        final String uuid1 = "TestUUID1";
        final String value2 = "Value2";
        final String uuid2 = "TestUUID2";

        AutoCompleteItem item1 = new AutoCompleteItem(value1, uuid1);
        AutoCompleteItem item2 = new AutoCompleteItem(value2, uuid2);
        AutoCompleteItem item3 = new AutoCompleteItem(value1, uuid1);

        assertEquals(value1, item1.getValue());
        assertEquals(value2, item2.getValue());
        assertEquals(uuid1, item1.getUuid());
        assertEquals(uuid2, item2.getUuid());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        EqualsVerifier.forClass(AutoCompleteItem.class).suppress(Warning.STRICT_INHERITANCE).verify();

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        StringBuilder builder = new StringBuilder();
        builder.append("AutoCompleteItem [value=");
        builder.append(item1.getValue());
        builder.append(", uuid=");
        builder.append(item1.getUuid());
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }

}
