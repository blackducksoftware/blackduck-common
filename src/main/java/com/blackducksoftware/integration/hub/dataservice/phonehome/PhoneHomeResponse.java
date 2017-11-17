package com.blackducksoftware.integration.hub.dataservice.phonehome;

import java.util.concurrent.Future;

public class PhoneHomeResponse {
    private final Future<Boolean> phoneHomeTask;

    public PhoneHomeResponse(final Future<Boolean> phoneHomeTask) {
        this.phoneHomeTask = phoneHomeTask;
    }

    public Future<Boolean> getPhoneHomeTask() {
        return phoneHomeTask;
    }

    public void endPhoneHome() {
        if (phoneHomeTask != null) {
            if (!phoneHomeTask.isDone()) {
                phoneHomeTask.cancel(true);
            }
        }
    }

}
