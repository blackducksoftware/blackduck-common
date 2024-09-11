/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.configuration;

import java.util.HashSet;
import java.util.Set;

import com.synopsys.integration.builder.BuilderPropertyKey;

public class BlackDuckServerConfigKeys {
    public static final BlackDuckServerConfigKeys KEYS = new BlackDuckServerConfigKeys();

    public final Set<BuilderPropertyKey> common = new HashSet<>();
    public final Set<BuilderPropertyKey> apiToken = new HashSet<>();
    public final Set<BuilderPropertyKey> credentials = new HashSet<>();
    public final Set<BuilderPropertyKey> all = new HashSet<>();

    public BlackDuckServerConfigKeys() {
        common.add(BlackDuckServerConfigBuilder.URL_KEY);
        common.add(BlackDuckServerConfigBuilder.SOLUTION_NAME_KEY);
        common.add(BlackDuckServerConfigBuilder.SOLUTION_VERSION_KEY);
        common.add(BlackDuckServerConfigBuilder.TIMEOUT_KEY);
        common.add(BlackDuckServerConfigBuilder.PROXY_HOST_KEY);
        common.add(BlackDuckServerConfigBuilder.PROXY_PORT_KEY);
        common.add(BlackDuckServerConfigBuilder.PROXY_USERNAME_KEY);
        common.add(BlackDuckServerConfigBuilder.PROXY_PASSWORD_KEY);
        common.add(BlackDuckServerConfigBuilder.PROXY_NTLM_DOMAIN_KEY);
        common.add(BlackDuckServerConfigBuilder.PROXY_NTLM_WORKSTATION_KEY);
        common.add(BlackDuckServerConfigBuilder.TRUST_CERT_KEY);

        apiToken.addAll(common);
        apiToken.add(BlackDuckServerConfigBuilder.API_TOKEN_KEY);

        credentials.addAll(common);
        credentials.add(BlackDuckServerConfigBuilder.USERNAME_KEY);
        credentials.add(BlackDuckServerConfigBuilder.PASSWORD_KEY);

        all.addAll(common);
        all.add(BlackDuckServerConfigBuilder.API_TOKEN_KEY);
        all.add(BlackDuckServerConfigBuilder.USERNAME_KEY);
        all.add(BlackDuckServerConfigBuilder.PASSWORD_KEY);
    }

}
