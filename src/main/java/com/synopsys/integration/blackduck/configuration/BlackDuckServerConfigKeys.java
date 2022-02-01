/*
 * blackduck-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.configuration;

import static com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder.*;

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
        common.add(URL_KEY);
        common.add(SOLUTION_NAME_KEY);
        common.add(SOLUTION_VERSION_KEY);
        common.add(TIMEOUT_KEY);
        common.add(PROXY_HOST_KEY);
        common.add(PROXY_PORT_KEY);
        common.add(PROXY_USERNAME_KEY);
        common.add(PROXY_PASSWORD_KEY);
        common.add(PROXY_NTLM_DOMAIN_KEY);
        common.add(PROXY_NTLM_WORKSTATION_KEY);
        common.add(TRUST_CERT_KEY);

        apiToken.addAll(common);
        apiToken.add(API_TOKEN_KEY);

        credentials.addAll(common);
        credentials.add(USERNAME_KEY);
        credentials.add(PASSWORD_KEY);

        all.addAll(common);
        all.add(API_TOKEN_KEY);
        all.add(USERNAME_KEY);
        all.add(PASSWORD_KEY);
    }

}
