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
package com.blackducksoftware.integration.hub.api.item;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class ParameterizedListType implements ParameterizedType {
    private final Type type;

    public ParameterizedListType(Type type) {
        this.type = type;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return new Type[] { type };
    }

    @Override
    public Type getRawType() {
        return ArrayList.class;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final ParameterizedListType other = (ParameterizedListType) obj;
        if (type == null) {
            if (other.type != null) return false;
        } else if (!type.equals(other.type)) return false;
        return true;
    }

}
