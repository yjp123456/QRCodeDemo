package com.example.qr_codescan;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.security.Constants;
import com.security.MyUtils;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class MainActivity extends Activity {
    private final static int SCANNIN_GREQUEST_CODE = 1;
    private long exitTime = 0;
    private Context context;
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                Constants.cookie = null;
                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                showAlert("退出失败");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        Button mButton = (Button) findViewById(R.id.button1);
        Button logout = (Button) findViewById(R.id.logout);
        mButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, CaptureActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
            }
        });
        logout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread() {
                    public void run() {
                        try {
                            JSONObject data = new JSONObject();
                            data.put("cookie", Constants.cookie);
                            doPost("https://47.93.17.21/logout", data);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });



    }

    private void showAlert(String content) {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                //.setIcon(R.drawable.ic_launch)
                .setTitle(R.string.alert_title)
                .setMessage(content)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                }).create();
        alertDialog.show();
    }

    public void doPost(String hostURL, JSONObject data) {
        try {
            URL url = new URL(hostURL);
            if (url.getProtocol().toLowerCase().equals("https")) {
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                       /* boolean hostnameVerification = HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, session);
                        Log.d(LOG_TAG, "hostname verify result is " + hostnameVerification);
                        return hostnameVerification;*/
                        return true;
                    }
                });
                conn.setSSLSocketFactory(MyUtils.getSSLSocket(context));
                conn.setConnectTimeout(3 * 1000);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(data.toString());
                wr.flush();
                wr.close();

                int response = conn.getResponseCode();
                if (response == 200) {
                    handler.sendEmptyMessage(0);
                } else
                    handler.sendEmptyMessage(1);
            }
        } catch (Exception e) {
            handler.sendEmptyMessage(1);
            e.printStackTrace();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SCANNIN_GREQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String userId = bundle.getString("result");
                    Intent intent = new Intent(this, UserActivity.class);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
