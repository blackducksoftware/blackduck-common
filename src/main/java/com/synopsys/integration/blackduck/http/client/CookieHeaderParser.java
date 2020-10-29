package com.synopsys.integration.blackduck.http.client;

import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;

public class CookieHeaderParser {
    public static final String SET_COOKIE = "Set-Cookie";
    public static final String AUTHORIZATION_BEARER_PREFIX = "AUTHORIZATION_BEARER=";
    public static final String HEADER_VALUE_SEPARATOR = ";";

    public Optional<String> parseBearerToken(Header[] allHeaders) {
        return Arrays
                   .stream(allHeaders)
                   .filter(header -> SET_COOKIE.equals(header.getName()))
                   .filter(header -> header.getValue().contains(AUTHORIZATION_BEARER_PREFIX))
                   .findFirst()
                   .map(header -> getToken(header.getValue()));
    }

    private String getToken(String headerValue) {
        if (headerValue.contains(HEADER_VALUE_SEPARATOR)) {
            return StringUtils.substringBetween(headerValue, AUTHORIZATION_BEARER_PREFIX, HEADER_VALUE_SEPARATOR);
        } else {
            return StringUtils.substringAfter(headerValue, AUTHORIZATION_BEARER_PREFIX);
        }
    }

}
