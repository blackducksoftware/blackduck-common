package com.blackducksoftware.integration.hub.service;

import com.blackducksoftware.integration.log.IntLogger;

public class DataService {
    HubService hubService;
    IntLogger logger;

    public DataService(final HubService hubService) {
        this.hubService = hubService;
        this.logger = hubService.getRestConnection().logger;
    }

}
