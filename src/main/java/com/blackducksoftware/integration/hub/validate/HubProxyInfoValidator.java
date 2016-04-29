package com.blackducksoftware.integration.hub.validate;

import com.blackducksoftware.integration.hub.exception.ValidationException;

public abstract class HubProxyInfoValidator<T> {

	public abstract T handleValidationException(ValidationException e);

	public abstract T handleSuccess();

}
