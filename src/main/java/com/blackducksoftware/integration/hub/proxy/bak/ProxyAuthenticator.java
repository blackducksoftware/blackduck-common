
package com.blackducksoftware.integration.hub.proxy.bak;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import okhttp3.Authenticator;
import okhttp3.Challenge;
import okhttp3.Credentials;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Proxy authenticator which is the same code ripped out of okhttp-digest
 */
public class ProxyAuthenticator implements Authenticator {

    public static final String PROXY_AUTH = "Proxy-Authenticate";

    public static final String PROXY_AUTH_RESP = "Proxy-Authorization";

    public static final String WWW_AUTH = "WWW-Authenticate";

    public static final String WWW_AUTH_RESP = "Authorization";

    private static final char[] HEXADECIMAL = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f'
    };

    private final String username;

    private final String password;

    private String lastNonce;

    private long nounceCount;

    private String realm;

    private String nonce;

    private String qop;

    private boolean stale;

    private String a1;

    private String a2;

    private boolean proxy;

    public ProxyAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    protected void parseChallenge(
            final String buffer, int pos, int len, Map<String, String> params) {
        HeaderValueParser parser = HeaderValueParser.INSTANCE;
        ParserCursor cursor = new ParserCursor(pos, buffer.length());
        CharArrayBuffer buf = new CharArrayBuffer(len);
        buf.append(buffer);

        HeaderElement[] elements = parser.parseElements(buf, cursor);
        if (elements.length == 0) {
            throw new IllegalArgumentException("Authentication challenge is empty");
        }

        for (HeaderElement element : elements) {
            params.put(element.getName(), element.getValue());
        }
    }

    @Override
    public synchronized Request authenticate(Route route, Response response) throws IOException {
        if (route.proxy() != null) {
            setProxy(true);
        }
        if (isBasicAuth(response)) {
            return authenticateBasic(response);
        }
        if (isDigestAuth(response)) {
            return authenticateDigest(response);
        }
        return null;
    }

    private boolean isBasicAuth(Response response) {
        List<Challenge> challenges = response.challenges();
        for (Challenge challenge : challenges) {
            if ("Basic".equalsIgnoreCase(challenge.scheme())) {
                return true;
            }
        }
        return false;
    }

    private boolean isDigestAuth(Response response) {
        String headerKey;
        if (isProxy()) {
            headerKey = PROXY_AUTH;
        } else {
            headerKey = WWW_AUTH;
        }
        return StringUtils.isNotBlank(findDigestHeader(response.headers(), headerKey));
    }

    public synchronized Request authenticateBasic(Response response) throws IOException {
        String headerKey;
        if (isProxy()) {
            headerKey = PROXY_AUTH_RESP;
        } else {
            headerKey = WWW_AUTH_RESP;
        }

        List<Challenge> challenges = response.challenges();
        for (Challenge challenge : challenges) {
            if ("Basic".equalsIgnoreCase(challenge.scheme())) {
                String credential = Credentials.basic(username, password);
                return response.request().newBuilder().header(headerKey, credential).build();
            }

        }
        return null;
    }

    public synchronized Request authenticateDigest(Response response) throws IOException {
        String headerKey;
        if (isProxy()) {
            headerKey = PROXY_AUTH;
        } else {
            headerKey = WWW_AUTH;
        }
        String digestHeader = findDigestHeader(response.headers(), headerKey);
        parseHeader(digestHeader);
        String responseDigest = createDigestHeader(response, digestHeader);
        return response.request().newBuilder().header(PROXY_AUTH_RESP, responseDigest).build();
    }

    public String createDigestHeader(Response response, String digestHeader) {
        MessageDigest digest = DigestUtils.getMd5Digest();

        StringBuilder a1PreHash = new StringBuilder();
        a1PreHash.append(username);
        a1PreHash.append(":");
        a1PreHash.append(realm);
        a1PreHash.append(":");
        a1PreHash.append(password);
        byte[] a1Array = digest.digest(a1PreHash.toString().getBytes(StandardCharsets.UTF_8));
        a1 = encode(a1Array);

        StringBuilder a2PreHash = new StringBuilder();
        a2PreHash.append(response.request().method());
        a2PreHash.append(":");
        a2PreHash.append(response.request().url().toString());
        byte[] a2Array = digest.digest(a2PreHash.toString().getBytes(StandardCharsets.UTF_8));
        a2 = encode(a2Array);

        StringBuilder responsePreHash = new StringBuilder();
        a1PreHash.append(a1);
        a1PreHash.append(":");
        a1PreHash.append(nonce);
        a1PreHash.append(":");
        a1PreHash.append(a2);
        byte[] responseArray = digest.digest(responsePreHash.toString().getBytes(StandardCharsets.UTF_8));
        String responseHash = encode(responseArray);

        StringBuilder responseHeader = new StringBuilder();
        responseHeader.append("Digest");
        responseHeader.append(" username=");
        responseHeader.append("\"" + username + "\",");
        responseHeader.append(" realm=");
        responseHeader.append("\"" + realm + "\",");
        responseHeader.append(" nonce=");
        responseHeader.append("\"" + nonce + "\",");
        responseHeader.append(" uri=");
        responseHeader.append("\"" + response.request().url().toString() + "\",");
        responseHeader.append(" qop=");
        responseHeader.append("\"" + qop + "\",");
        responseHeader.append(" cnonce=");
        responseHeader.append("\"" + createCnonce() + "\",");
        responseHeader.append(" response=");
        responseHeader.append("\"" + responseHash + "\"");

        return responseHeader.toString();
    }

    /**
     * Encodes the 128 bit (16 bytes) MD5 digest into a 32 characters long
     * <CODE>String</CODE> according to RFC 2617.
     *
     * @param binaryData
     *            array containing the digest
     * @return encoded MD5, or <CODE>null</CODE> if encoding failed
     */
    static String encode(final byte[] binaryData) {
        final int n = binaryData.length;
        final char[] buffer = new char[n * 2];
        for (int i = 0; i < n; i++) {
            final int low = (binaryData[i] & 0x0f);
            final int high = ((binaryData[i] & 0xf0) >> 4);
            buffer[i * 2] = HEXADECIMAL[high];
            buffer[(i * 2) + 1] = HEXADECIMAL[low];
        }

        return new String(buffer);
    }

    private String findDigestHeader(Headers headers, String headerKey) {
        final List<String> authHeaders = headers.values(headerKey);
        for (String header : authHeaders) {
            if (header.startsWith("Digest")) {
                return header;
            }
        }
        throw new IllegalArgumentException("unsupported auth scheme: " + authHeaders);
    }

    public void parseHeader(String digestHeader) {
        digestHeader = digestHeader.replace("Digest ", "");

        String[] splitString = digestHeader.split(",");
        for (String split : splitString) {
            String[] subSplit = split.split("=");
            String key = subSplit[0].trim();
            String value = subSplit[1].trim().replace("\"", "");
            if (key.equalsIgnoreCase("realm")) {
                realm = value;
            } else if (key.equalsIgnoreCase("nonce")) {
                nonce = value;
            } else if (key.equalsIgnoreCase("qop")) {
                qop = value;
            } else if (key.equalsIgnoreCase("stale")) {
                stale = Boolean.valueOf(value);
            }
        }

    }

    /**
     * Creates a random cnonce value based on the current time.
     *
     * @return The cnonce value as String.
     */
    public String createCnonce() {
        final SecureRandom rnd = new SecureRandom();
        final byte[] tmp = new byte[8];
        rnd.nextBytes(tmp);
        return encode(tmp);
    }

    public boolean isProxy() {
        return proxy;
    }

    public void setProxy(boolean proxy) {
        this.proxy = proxy;
    }

}
