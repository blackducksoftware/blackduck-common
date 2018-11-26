package com.synopsys.integration.blackduck.service;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;

public class SuperHero extends BlackDuckResponse {
    private String name;
    private Address address;
    private String alias;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(final Address address) {
        this.address = address;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(final String alias) {
        this.alias = alias;
    }

}
