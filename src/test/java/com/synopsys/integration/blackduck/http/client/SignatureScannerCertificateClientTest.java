package com.synopsys.integration.blackduck.http.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.TimeZone;

import javax.net.ssl.SSLContext;

import org.apache.http.ssl.SSLContexts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.tls.HandshakeCertificates;
import okhttp3.tls.HeldCertificate;

@ExtendWith(TimingExtension.class)
public class SignatureScannerCertificateClientTest {
    private static final BufferedIntLogger LOGGER = new BufferedIntLogger();
    private static final BlackDuckRequestFactory BLACK_DUCK_REQUEST_FACTORY = new BlackDuckRequestFactory();

    private static HeldCertificate LOCALHOST_CERTIFICATE;
    private static String KEYSTORE_FILENAME;
    private static KeyStore DEFAULT_KEYSTORE;
    private static Request BLACK_DUCK_DEFAULT_REQUEST;
    public static HandshakeCertificates SERVER_CERTIFICATE;
    private static MockWebServer DESTINATION_MOCK_SERVER;
    private static String DESTINATION_MOCK_SERVER_URL;
    private static MockWebServer REDIRECTING_MOCK_SERVER;
    private static String REDIRECTING_MOCK_SERVER_URL;

    private static final char[] KEYSTORE_PASSWORD = "changeit".toCharArray();
    private static final String CERTIFICATE_ALIAS = "bd-common-test-cert";
    public static final String SERVER_BODY = "BlackDuck-Common response body";

    @BeforeEach
    void setUp() throws UnknownHostException, IntegrationException {
        String localHost = InetAddress.getByName("localhost").getCanonicalHostName();
        LOCALHOST_CERTIFICATE = new HeldCertificate.Builder().addSubjectAlternativeName(localHost).build();
        SERVER_CERTIFICATE = new HandshakeCertificates.Builder().heldCertificate(LOCALHOST_CERTIFICATE).build();

        DESTINATION_MOCK_SERVER = new MockWebServer();
        DESTINATION_MOCK_SERVER.useHttps(SERVER_CERTIFICATE.sslSocketFactory(), false);
        DESTINATION_MOCK_SERVER.enqueue(new MockResponse().setBody(SERVER_BODY));

        DESTINATION_MOCK_SERVER_URL = DESTINATION_MOCK_SERVER.url("/").toString();
        System.out.println(String.format("Destination MockWebServer started for test at %s", DESTINATION_MOCK_SERVER_URL));

        HttpUrl httpsServer = new HttpUrl(DESTINATION_MOCK_SERVER_URL);

        BLACK_DUCK_DEFAULT_REQUEST = BLACK_DUCK_REQUEST_FACTORY.createCommonGetRequest(httpsServer);

        DEFAULT_KEYSTORE = createEmptyKeyStoreWithCertificate(LOCALHOST_CERTIFICATE.certificate());
    }

    @AfterEach
    void tearDownAfterTest() throws IOException {
        File keyStore = new File(KEYSTORE_FILENAME);
        assertTrue(keyStore.delete(), String.format("Failed removing keystore: %s", KEYSTORE_FILENAME));

        DESTINATION_MOCK_SERVER.shutdown();
        //        REDIRECTING_MOCK_SERVER.shutdown();
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
    public void noProxyTrustFalse() throws IntegrationException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException {
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(DEFAULT_KEYSTORE, (x509Certificates, s) -> false).build();
        SignatureScannerCertificateClient signatureScannerCertificateClient = new SignatureScannerCertificateClient(LOGGER, 10, false, ProxyInfo.NO_PROXY_INFO, sslContext);
        assertSame(sslContext, signatureScannerCertificateClient.getSSLContext());

        Response response = signatureScannerCertificateClient.execute(BLACK_DUCK_DEFAULT_REQUEST);

        assertEquals(200, response.getStatusCode());
        assertEquals(SERVER_BODY, response.getContentString());
        assertNotNull(signatureScannerCertificateClient.getServerCertificate());
        assertEquals(LOCALHOST_CERTIFICATE.certificate(), signatureScannerCertificateClient.getServerCertificate());
    }

    @Test
    public void noProxyTrustTrue() throws IntegrationException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException, InterruptedException {
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(DEFAULT_KEYSTORE, (x509Certificates, s) -> false).build();
        SignatureScannerCertificateClient signatureScannerCertificateClient = new SignatureScannerCertificateClient(LOGGER, 10, false, ProxyInfo.NO_PROXY_INFO, sslContext);
        //assertNotSame(sslContext, signatureScannerCertificateClient.getSSLContext());

        Response response = signatureScannerCertificateClient.execute(BLACK_DUCK_DEFAULT_REQUEST);

        assertEquals(200, response.getStatusCode());
        assertEquals(SERVER_BODY, response.getContentString());
        assertNotNull(signatureScannerCertificateClient.getServerCertificate());
        assertEquals(LOCALHOST_CERTIFICATE.certificate(), signatureScannerCertificateClient.getServerCertificate());
    }
    
}
