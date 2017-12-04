package com.example.qr_codescan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.security.Constants;
import com.security.SecurityUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * Created by jieping_yang on 2017/7/11.
 */

public class LoginActivity extends Activity {

    private Button login;
    private EditText userName;
    private EditText pwd;
    private TextView warning;
    private String LOG_TAG = "LoginActivity";
    private Context context;
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            try {
                if (msg.what == 0) {
                    warning.setText("网络异常");
                    warning.setVisibility(View.VISIBLE);
                   /* SecurityUtil.saveOperator(userName.getText().toString().trim(), context);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();*/
                } else {
                    Bundle data = msg.getData();
                    String returnLine = data.getString("data");
                    JSONObject result = null;
                    result = new JSONObject(returnLine);
                    int error_code = result.getInt("error_code");
                    if (error_code == 0) {
                        Constants.cookie = result.getString("cookie");
                        SecurityUtil.saveOperator(userName.getText().toString().trim(), result.getString("shop"), context);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (error_code == 1) {
                        warning.setText("用户名或密码错误");
                        warning.setVisibility(View.VISIBLE);
                    }
                }
                login.setClickable(true);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        context = this;
        login = (Button) findViewById(R.id.login);
        userName = (EditText) findViewById(R.id.userName);
        pwd = (EditText) findViewById(R.id.pwd);
        warning = (TextView) findViewById(R.id.warning);

        login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                warning.setVisibility(View.GONE);
                final String name = userName.getText().toString().trim();
                final String password = pwd.getText().toString().trim();
                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(password)) {
                    login.setClickable(false);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = new JSONObject();
                                data.put("username", name);
                                data.put("password", password);
                                doPost("https://47.93.17.21/login", data);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } else {
                    warning.setText("用户名或密码不能为空");
                    warning.setVisibility(View.VISIBLE);
                }
            }
        });
        final String operator = SecurityUtil.getOperator(context)[0];
        if (Constants.cookie != null && operator != null) {
            new Thread() {
                public void run() {
                    try {
                        JSONObject data = new JSONObject();
                        data.put("username", operator);
                        data.put("cookie", Constants.cookie);
                        doPost("https://47.93.17.21/login", data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }

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
                conn.setSSLSocketFactory(SecurityUtil.getSSLSocket(context));
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
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    String line = "";
                    String returnLine = "";
                    System.out.println("Contents of post request start");

                    while ((line = reader.readLine()) != null) {
                        returnLine += line;
                    }
                    Message message = new Message();
                    message.what = 1;
                    Bundle bundle = new Bundle();
                    bundle.putString("data", returnLine);
                    message.setData(bundle);
                    handler.sendMessage(message);
                } else
                    handler.sendEmptyMessage(0);
                Log.d(LOG_TAG, "response is " + response);
            }
        } catch (Exception e) {
            handler.sendEmptyMessage(0);
            e.printStackTrace();
        }

    }
}
