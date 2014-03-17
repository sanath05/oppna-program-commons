package se.vgregion.crypto.xml;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertTrue;

/**
 * @author Patrik Bergström
 */
public class XmlSignerTest {

    @Test
    public void testSignAndVerifyXml() throws Exception {

        String xml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><rootElement><content>theContent</content></rootElement>";

        XmlSigner signer = new XmlSigner();

        /*File file = new File("C:\\java\\workspace\\secrets\\certifikat\\portalen.vgregion.se.p12");
        FileInputStream fis = new FileInputStream(file);

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(fis, "asdf".toCharArray());
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)  ks.getEntry("portalen.vgregion.se", new KeyStore.PasswordProtection("asdf".toCharArray()));*/

        // Load the KeyStore and get the signing key and certificate.
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(this.getClass().getClassLoader().getResourceAsStream("teststore.jks"), "changeit".toCharArray());
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry
                ("test_alias", new KeyStore.PasswordProtection("changeit".toCharArray()));

        String signedXml1 = signer.sign(xml1, privateKeyEntry);

        System.out.println(signedXml1);

        System.out.println(new String(Base64.encodeBase64(signedXml1.getBytes("UTF-8")), "UTF-8"));

        X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X509")
                .generateCertificate(this.getClass().getClassLoader().getResourceAsStream("testcert.pem"));

//        X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X509")
//                .generateCertificate(new FileInputStream("C:\\Users\\patber.KNOWIT\\Documents\\secure\\" +
//                        "portalen.vgregion.se-cert-pkcs10\\portalen.vgregion.se_cert.pem"));


        boolean verify1 = signer.verify(signedXml1, certificate);

        assertTrue(verify1);
    }

}
