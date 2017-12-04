package com.example.qr_codescan;

import android.app.Application;
import android.content.Context;

import com.security.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jieping_yang on 2017/7/17.
 */

public class MyApplication extends Application {
    Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this.getApplicationContext();
        new Thread(){
            public void run(){
                installCert();
            }
        }.start();

    }

    private void installCert() {
        try {
            InputStream in = context.getAssets().open("certificate.pem");
            String rootPath = context.getFilesDir().getAbsolutePath()+"/cer";
            byte[] buffer = new byte[1024 * 8];
            Constants.CERT_PATH = rootPath+"/certificate.pem";
            File file = new File(rootPath);
            if(!file.exists()){
                file.mkdirs();
                File cer = new File(Constants.CERT_PATH);
                cer.createNewFile();
                FileOutputStream out = new FileOutputStream(cer);
                int len = 0;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                in.close();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
