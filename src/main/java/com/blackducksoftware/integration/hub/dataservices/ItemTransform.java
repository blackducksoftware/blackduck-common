package com.blackducksoftware.integration.hub.dataservices;

import com.blackducksoftware.integration.hub.exception.HubItemTransformException;

public interface ItemTransform<R, S> {
	public R transform(S item) throws HubItemTransformException;;

}
