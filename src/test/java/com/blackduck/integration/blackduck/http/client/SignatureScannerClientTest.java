package com.blackduck.integration.blackduck.http.client;

import com.blackduck.integration.blackduck.TimingExtension;
import com.blackduck.integration.blackduck.http.BlackDuckRequestBuilder;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.BufferedIntLogger;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.blackduck.integration.rest.request.Request;
import com.blackduck.integration.rest.response.Response;
import com.google.gson.Gson;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.tls.HandshakeCertificates;
import okhttp3.tls.HeldCertificate;
import org.apache.http.ssl.SSLContexts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Optional;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TimingExtension.class)
public class SignatureScannerClientTest {
    private static final BufferedIntLogger LOGGER = new BufferedIntLogger();
    private static final Gson gson = new Gson();

    private static final char[] KEYSTORE_PASSWORD = "changeit".toCharArray();
    private static final String CERTIFICATE_ALIAS = "bd-common-test-cert";
    private static final String SERVER_BODY_RESPONSE1 = "BlackDuck-Common response body #1";
    private static final String SERVER_BODY_RESPONSE2 = "BlackDuck-Common response body #2";

    private static HeldCertificate LOCALHOST_CERTIFICATE;
    private static String KEYSTORE_FILENAME;
    private static KeyStore DEFAULT_KEYSTORE;
    private static Request BLACK_DUCK_DEFAULT_REQUEST;
    private static MockWebServer DESTINATION_MOCK_SERVER;

    @BeforeEach
    void setUp() throws IOException, IntegrationException {
        String localHost = InetAddress.getByName("localhost").getCanonicalHostName();
        LOCALHOST_CERTIFICATE = new HeldCertificate.Builder().addSubjectAlternativeName(localHost).build();
        HandshakeCertificates serverCertificate = new HandshakeCertificates.Builder().heldCertificate(LOCALHOST_CERTIFICATE).build();

        DESTINATION_MOCK_SERVER = new MockWebServer();
        DESTINATION_MOCK_SERVER.useHttps(serverCertificate.sslSocketFactory(), false);
        DESTINATION_MOCK_SERVER.enqueue(new MockResponse().setBody(SERVER_BODY_RESPONSE1));
        DESTINATION_MOCK_SERVER.enqueue(new MockResponse().setBody(SERVER_BODY_RESPONSE2));

        String destinationMockServerUrl = DESTINATION_MOCK_SERVER.url("/").toString();
        System.out.println(String.format("Destination MockWebServer started for test at %s", destinationMockServerUrl));

        HttpUrl httpsServer = new HttpUrl(destinationMockServerUrl);

        BLACK_DUCK_DEFAULT_REQUEST = new BlackDuckRequestBuilder()
                                         .commonGet()
                                         .url(httpsServer)
                                         .build();

        DEFAULT_KEYSTORE = createEmptyKeyStoreWithCertificate(LOCALHOST_CERTIFICATE.certificate());
    }

    @AfterEach
    void tearDownAfterTest() throws IOException {
        File keyStore = new File(KEYSTORE_FILENAME);
        assertTrue(keyStore.delete(), String.format("Failed removing keystore: %s", KEYSTORE_FILENAME));

        DESTINATION_MOCK_SERVER.shutdown();
    }

    private static KeyStore createEmptyKeyStoreWithCertificate(X509Certificate x509Certificate) throws IntegrationException {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        long secondsSinceEpoch = calendar.getTimeInMillis() / 1000L;

        KEYSTORE_FILENAME = "bdcommon.keystore." + secondsSinceEpoch;

        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, KEYSTORE_PASSWORD);

            FileOutputStream fileOutputStream = new FileOutputStream(KEYSTORE_FILENAME);
            keyStore.setCertificateEntry(CERTIFICATE_ALIAS, x509Certificate);
            keyStore.store(fileOutputStream, KEYSTORE_PASSWORD);
            fileOutputStream.close();

            assertEquals(1, keyStore.size(), "Keystore size should equal 1");
            assertTrue(keyStore.containsAlias(CERTIFICATE_ALIAS), String.format("%s does not contain expected alias %s", KEYSTORE_FILENAME, CERTIFICATE_ALIAS));

            System.out.println(String.format("Keystore file created for testing: %s", KEYSTORE_FILENAME));
        } catch (Exception e) {
            throw new IntegrationException("Encountered error while creating empty key store", e);
        }

        return keyStore;
    }

    @Test
    public void getCertWithContext() throws IntegrationException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, IOException {
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(DEFAULT_KEYSTORE, (x509Certificates, s) -> true).build();
        SignatureScannerClient signatureScannerClient = new SignatureScannerClient(LOGGER, gson, 10, ProxyInfo.NO_PROXY_INFO, sslContext);

        Optional<Response> optionalResponse = signatureScannerClient.executeGetRequestIfModifiedSince(BLACK_DUCK_DEFAULT_REQUEST, -5L);
        Response response = optionalResponse.orElse(null);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals(SERVER_BODY_RESPONSE2, response.getContentString());
        assertNotNull(signatureScannerClient.getServerCertificate());
        assertEquals(LOCALHOST_CERTIFICATE.certificate(), signatureScannerClient.getServerCertificate());
    }

    @Test
    public void getCertTrustTrue() throws IntegrationException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, IOException {
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(DEFAULT_KEYSTORE, (x509Certificates, s) -> true).build();
        SignatureScannerClient signatureScannerClient = new SignatureScannerClient(LOGGER, gson, 10, true, ProxyInfo.NO_PROXY_INFO);

        Optional<Response> optionalResponse = signatureScannerClient.executeGetRequestIfModifiedSince(BLACK_DUCK_DEFAULT_REQUEST, -5L);
        Response response = optionalResponse.orElse(null);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals(SERVER_BODY_RESPONSE2, response.getContentString());
        assertNotNull(signatureScannerClient.getServerCertificate());
        assertEquals(LOCALHOST_CERTIFICATE.certificate(), signatureScannerClient.getServerCertificate());
    }

}
