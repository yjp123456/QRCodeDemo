package com.security;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * Created by jieping_yang on 2017/7/11.
 */

public class MyUtils {
    public static final String SHARED_PREFERENCES = "shared_preferences";

    public static SSLSocketFactory getSSLSocket(Context context) {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            MitmTrustManager myTrustManager = new MitmTrustManager(getTrustStore(context));
            sslContext.init(null, new TrustManager[]{myTrustManager}, new SecureRandom());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (sslContext == null)
            return null;
        return sslContext.getSocketFactory();
    }

    public static KeyStore getTrustStore(Context context) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        if (null != context) {
            addRootCertToTrustStore(context, trustStore);
        }
        return trustStore;
    }

    private static void addRootCertToTrustStore(Context context, KeyStore trustStore) {
        String caFilePath = Constants.CERT_PATH;
        File file = new File(caFilePath);
        if (file.exists()) {
            try {
                FileInputStream in = new FileInputStream(file);
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                trustStore.setCertificateEntry("ca", certificateFactory.generateCertificate(in));

                if (in != null) {
                    in.close();
                }
                //Log.d(TAG, "add root certificate to trust store successfully!");
            } catch (Exception e) {

            }

        }
    }

    public static String[] getOperator(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String[] data = new String[2];
        data[0] = preferences.getString("userName", null);
        data[1] = preferences.getString("shop", null);
        return data;
    }

    public static void saveOperator(String userName, String shop, Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userName", userName);
        editor.putString("shop", shop);
        editor.commit();
    }
}
