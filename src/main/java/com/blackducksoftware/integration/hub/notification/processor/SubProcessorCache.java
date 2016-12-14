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

import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;

public interface SubProcessorCache<T extends NotificationEvent<?>> {

    public void addEvent(final T event);

    public void removeEvent(final T event);

    public Collection<T> getEvents();
}
