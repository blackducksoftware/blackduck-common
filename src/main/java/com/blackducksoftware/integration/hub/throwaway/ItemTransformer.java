package com.blackducksoftware.integration.hub.throwaway;

import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;

public interface ItemTransformer<R, S> {
    public List<R> transform(S item) throws IntegrationException;

}
