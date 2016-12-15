/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.notification.processor;

import java.util.Collection;
import java.util.LinkedList;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;

public class ListProcessorCache<T extends NotificationEvent<?>> implements SubProcessorCache<T> {
    private final LinkedList<T> eventList = new LinkedList<>();

    @Override
    public void addEvent(T event) {
        eventList.add(event);
    }

    @Override
    public void removeEvent(T event) {
        eventList.remove(event);
    }

    @Override
    public Collection<T> getEvents() throws HubIntegrationException {
        return eventList;
    }
}
