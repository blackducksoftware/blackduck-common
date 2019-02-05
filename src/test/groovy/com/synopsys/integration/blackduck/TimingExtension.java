package com.synopsys.integration.blackduck;

import java.lang.reflect.Method;
import java.util.Date;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.synopsys.integration.rest.RestConstants;

public class TimingExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
    private static final String START_TIME = "start time";

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        long currentTime = System.currentTimeMillis();
        getStore(context).put(TimingExtension.START_TIME, currentTime);
        Class<?> testClass = context.getRequiredTestClass();
        Method testMethod = context.getRequiredTestMethod();
        String currentTimeString = RestConstants.formatDate(new Date(currentTime));

        System.out.println(String.format("%s - %s:%s starting...", currentTimeString, testClass.getName(), testMethod.getName()));
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        long currentTime = System.currentTimeMillis();
        Class<?> testClass = context.getRequiredTestClass();
        Method testMethod = context.getRequiredTestMethod();
        String currentTimeString = RestConstants.formatDate(new Date(currentTime));
        long startTime = getStore(context).remove(TimingExtension.START_TIME, long.class);
        long duration = currentTime - startTime;

        System.out.println(String.format("%s - %s:%s took %s ms.", currentTimeString, testClass.getName(), testMethod.getName(), duration));
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
    }

}
