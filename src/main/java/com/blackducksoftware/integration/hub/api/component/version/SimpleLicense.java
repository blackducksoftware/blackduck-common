package com.blackducksoftware.integration.hub.api.component.version;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.collections4.CollectionUtils;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.google.common.base.Joiner;

public class SimpleLicense {

	private static final String AND = " AND ";
	private static final String OR = " OR ";
    private static final String MAPPING_PENDING = "Mapping Pending";
    private static final String OPEN_PARENTHESIS = "(";
    private static final String CLOSED_PARENTHESIS = ")";
	
	private final ComplexLicense complexLicense;
	
	public SimpleLicense(ComplexLicense complexLicense) {
		this.complexLicense = complexLicense;
	}
	
	public ComplexLicense getComplexLicense() {
		return this.complexLicense;
	}
	
	public String toLicenseText() throws HubIntegrationException {
		return this.toLicenseText(this.complexLicense);
	}
	
	private String toLicenseText(ComplexLicense complexLicense) throws HubIntegrationException {
		if (CollectionUtils.isEmpty(complexLicense.getLicenses())){
			return complexLicense.getName();
		} else {
			String operator = complexLicense.getType() == ComplexLicenseType.CONJUNCTIVE ? AND : OR;

			Collection<String> result = new LinkedList<String>();
			for (ComplexLicense childLicense : complexLicense.getLicenses()){
				result.add(this.toLicenseText(childLicense));
			}
			
            /**
             * AND 'Mapping Pending' => throw a HubIntegrationException
             * OR 'Mapping Pending' => discard 'Mapping Pending' at all
             */
            // result.contains("") is needed for Mapping Pending OR Mapping Pending
            if (result.contains(MAPPING_PENDING) || result.contains("")) {
                if (AND.equals(operator) && result.contains(MAPPING_PENDING)) {
                    throw new HubIntegrationException("Unable to create Simple License String");
                } else {
                	LinkedList<String> removalCollection = new LinkedList<String>();
                	removalCollection.add(MAPPING_PENDING);
                	removalCollection.add("");
                    result.removeAll(removalCollection);
                }
            }
            return result.size() > 1 ? OPEN_PARENTHESIS + Joiner.on(operator).join(result) + CLOSED_PARENTHESIS
                    : Joiner.on(operator).join(result);
		
		}
		
		
	}
}
