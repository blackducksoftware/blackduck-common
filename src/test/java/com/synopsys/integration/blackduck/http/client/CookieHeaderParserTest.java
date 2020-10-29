package com.synopsys.integration.blackduck.http.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.jupiter.api.Test;

public class CookieHeaderParserTest {
    public static final String BEARER_TOKEN = "bearertoken";

    @Test
    public void testParsingSingleCookie() {
        CookieHeaderParser cookieHeaderParser = new CookieHeaderParser();
        Header[] headers = {
            new BasicHeader("Content-type", "application/json")
            , new BasicHeader("Accept", "text/html,text/xml,application/json")
            , new BasicHeader("Connection", "keep-alive")
            , new BasicHeader("keep-alive", "115")
            , new BasicHeader(CookieHeaderParser.SET_COOKIE, CookieHeaderParser.AUTHORIZATION_BEARER_PREFIX + BEARER_TOKEN)
            , new BasicHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")
        };

        assertEquals("bearertoken", cookieHeaderParser.parseBearerToken(headers).get());
    }

    @Test
    public void testParsingSingleCookieWithMultipleValues() {
        CookieHeaderParser cookieHeaderParser = new CookieHeaderParser();
        Header[] headers = {
            new BasicHeader("Content-type", "application/json")
            , new BasicHeader("Accept", "text/html,text/xml,application/json")
            , new BasicHeader("Connection", "keep-alive")
            , new BasicHeader("keep-alive", "115")
            , new BasicHeader(CookieHeaderParser.SET_COOKIE, CookieHeaderParser.AUTHORIZATION_BEARER_PREFIX + BEARER_TOKEN + "; Expires=3000-12-25")
            , new BasicHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")
        };

        assertEquals("bearertoken", cookieHeaderParser.parseBearerToken(headers).get());
    }

    @Test
    public void testParsingBearerTokenAfterUnimportantCookie() {
        CookieHeaderParser cookieHeaderParser = new CookieHeaderParser();
        Header[] headers = {
            new BasicHeader("Content-type", "application/json")
            , new BasicHeader("Accept", "text/html,text/xml,application/json")
            , new BasicHeader(CookieHeaderParser.SET_COOKIE, "not a bearer token")
            , new BasicHeader("Connection", "keep-alive")
            , new BasicHeader("keep-alive", "115")
            , new BasicHeader(CookieHeaderParser.SET_COOKIE, CookieHeaderParser.AUTHORIZATION_BEARER_PREFIX + BEARER_TOKEN)
            , new BasicHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")
        };

        assertEquals("bearertoken", cookieHeaderParser.parseBearerToken(headers).get());
    }

}
