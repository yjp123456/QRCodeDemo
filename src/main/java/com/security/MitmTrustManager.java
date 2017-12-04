package com.security;

import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by jieping_yang on 2017/6/9.
 */

public class MitmTrustManager implements X509TrustManager {
    private X509TrustManager systemTrustManager = null;
    private X509TrustManager selfTrustManager = null;

    private String LOG_TAG = "MitmTrustManager";

    public MitmTrustManager(KeyStore keyStore) {
        systemTrustManager = createTrustManager(keyStore);
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        systemTrustManager.checkServerTrusted(chain, authType);
    }

    private X509TrustManager createTrustManager(KeyStore keyStore) {
        TrustManager[] trustManagers = null;
        try {
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);//store is null will use system cer
            trustManagers = tmf.getTrustManagers();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return (X509TrustManager) trustManagers[0];
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
