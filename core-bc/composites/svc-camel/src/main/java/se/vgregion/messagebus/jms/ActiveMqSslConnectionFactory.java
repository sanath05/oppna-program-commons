package se.vgregion.messagebus.jms;

import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.apache.activemq.broker.SslContext;
import org.apache.activemq.transport.Transport;
import org.apache.activemq.transport.tcp.SslTransportFactory;
import org.apache.activemq.util.JMSExceptionSupport;

import javax.jms.JMSException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

public final class ActiveMqSslConnectionFactory extends ActiveMQSslConnectionFactory {
    private String trustStore;
    private String trustStorePassword;
    private String keyStore;
    private String keyStorePassword;

    protected Transport createTransport() throws JMSException {
        // If the given URI is non-ssl, let superclass handle it.
        if (!brokerURL.getScheme().equals("ssl")) {
            return super.createTransport();
        }

        try {
            if (keyManager == null || trustManager == null) {
                trustManager = getTrustManager();
                keyManager = getKeyManager();
                // secureRandom can be left as null
            }
            SslTransportFactory sslFactory = new SslTransportFactory();
            SslContext ctx = new SslContext(keyManager, trustManager, secureRandom);
            SslContext.setCurrentSslContext(ctx);
            return sslFactory.doConnect(brokerURL);
        } catch (Exception e) {
            throw JMSExceptionSupport.create("Could not create Transport. Reason: " + e, e);
        }
    }

    /**
     * Get {@link TrustManager} array.
     * @return Array of {@link TrustManager}s.
     * @throws Exception Exception
     */
    public TrustManager[] getTrustManager() throws Exception {
        TrustManager[] trustStoreManagers = null;
        KeyStore trustedCertStore = KeyStore.getInstance("jks");

        InputStream tsStream = null;
        try {
            tsStream = getClass().getResourceAsStream(trustStore);

            trustedCertStore.load(tsStream, trustStorePassword.toCharArray());

            TrustManagerFactory tmf =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

            tmf.init(trustedCertStore);
            trustStoreManagers = tmf.getTrustManagers();
            return trustStoreManagers;
        } finally {
            if (tsStream != null) {
                tsStream.close();
            }
        }
    }

    /**
     * Get {@link KeyManager} array.
     * @return Array of {@link KeyManager}s.
     * @throws Exception Exception
     */
    public KeyManager[] getKeyManager() throws Exception {
        KeyManagerFactory kmf =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        KeyStore ks = KeyStore.getInstance("jks");
        KeyManager[] keystoreManagers = null;

        byte[] sslCert = loadClientCredential(keyStore);

        if (sslCert != null && sslCert.length > 0) {
            ByteArrayInputStream bin = new ByteArrayInputStream(sslCert);
            ks.load(bin, keyStorePassword.toCharArray());
            kmf.init(ks, keyStorePassword.toCharArray());
            keystoreManagers = kmf.getKeyManagers();
        }
        return keystoreManagers;
    }

    private byte[] loadClientCredential(String fileName) throws IOException {
        if (fileName == null) {
            return new byte[0];
        }

        InputStream in = null;
        try {
            in = getClass().getResourceAsStream(fileName);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            final int i1 = 512;
            byte[] buf = new byte[i1];
            int i = in.read(buf);
            while (i > 0) {
                out.write(buf, 0, i);
                i = in.read(buf);
            }
            return out.toByteArray();
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

}