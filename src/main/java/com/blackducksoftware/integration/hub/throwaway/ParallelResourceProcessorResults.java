package com.blackducksoftware.integration.hub.throwaway;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class ParallelResourceProcessorResults<R> {
    private final List<R> results;
    private final List<Exception> exceptions;

    public ParallelResourceProcessorResults(final List<R> results, final List<Exception> exceptionMessages) {
        if (results == null) {
            this.results = new ArrayList<>();
        } else {
            this.results = results;
        }
        this.exceptions = exceptionMessages;
    }

    public List<R> getResults() {
        return results;
    }

    public List<Exception> getExceptions() {
        return exceptions;
    }

    public boolean isError() {
        if ((exceptions != null) && (exceptions.size() > 0)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
    }
}
