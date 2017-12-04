package com.example.qr_codescan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.adapter.OperationAdapter;
import com.objects.Operation;
import com.objects.User;
import com.security.Constants;
import com.security.MyUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * Created by jieping_yang on 2017/7/10.
 */

public class UserActivity extends Activity {
    private TextView userId;
    private TextView balance;
    private TextView vipView;
    private TextView scoreView;
    private Button back;
    private GridView operations;
    private List<Operation> operationList;
    private OperationAdapter adapter;
    private Context context;
    private User user;
    private static int TYPE_COMSUME_CHARGE = 1;
    private static int TYPE_COMSUME_CASH = 0;
    private static int TYPE_CONSUME_SCORE = 5;
    private static int TYPE_RECHARGE = 3;
    private static int ACTIVE_VIP = 2;
    private static int GET_USER_INFO = 4;
    private Executor executorService;
    private String LOG_TAG = "UserActivity";
    private String shop;
    private String operatorName;
    private String userID;


    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            try {
                if (msg.what == 0) {
                    showAlert("网络错误");
                } else if (msg.what == 1) {
                    showAlert("余额不足");
                } else if (msg.what == 2) {
                    showAlert("积分不足");
                } else {
                    Bundle data = msg.getData();
                    String returnLine = data.getString("data");
                    int type = data.getInt("type");
                    JSONObject result = null;
                    result = new JSONObject(returnLine);
                    int error_code = result.getInt("error_code");
                    if (error_code == 0) {
                        if (type == GET_USER_INFO)
                            result = result.getJSONObject("data");
                        user = new User(result.getString("userId"), result.getDouble("money"), result.getBoolean("isVIP"), result.getInt("score"));
                        if (user.isVIP)
                            vipView.setText("(VIP)");
                        else
                            vipView.setText("(未激活VIP)");
                        balance.setText(result.getDouble("money") + "(RMB)");
                        scoreView.setText(result.getInt("score") + "");
                        if (type != GET_USER_INFO)
                            showAlert("操作成功");
                    } else if (error_code == 2) {
                        if (type == GET_USER_INFO) {
                            Toast.makeText(context.getApplicationContext(), "无效用户", Toast.LENGTH_LONG).show();
                            finish();
                        } else
                            showAlertForReLogin("会话失效，是否重新登录？");
                    } else
                        showAlert("操作失败");

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_user);
        context = this;
        userID = getIntent().getStringExtra("userId");
        if (TextUtils.isEmpty(userID)) {
            Toast.makeText(context.getApplicationContext(), "无效用户", Toast.LENGTH_LONG).show();
            finish();
        }
        userId = (TextView) findViewById(R.id.userId);
        balance = (TextView) findViewById(R.id.balance);
        vipView = (TextView) findViewById(R.id.isVIP);
        scoreView = (TextView) findViewById(R.id.score);
        back = (Button) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        userId.setText(userID);
        balance.setText("0(RMB)");
        scoreView.setText("0");

        operations = (GridView) findViewById(R.id.operation);
        operationList = new ArrayList<Operation>();
        user = new User(userID, 0, false, 0);
        executorService = Executors.newFixedThreadPool(1);
        adapter = new OperationAdapter(this, operationList);
        operations.setAdapter(adapter);
        operations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Operation item = operationList.get(position);
                if (item.type == ACTIVE_VIP) {
                    if (user.isVIP)
                        showAlert("该用户已是会员");
                    else
                        showMyDialog("会员激活", ACTIVE_VIP);
                } else
                    showMyDialog(item.operationName, item.type);
            }
        });

        String[] operator = MyUtils.getOperator(context);
        operatorName = operator[0];
        shop = operator[1];

    }

    @Override
    protected void onResume() {
        initOperationList();
        getUserInfo(userID);
        super.onResume();
    }

    private void getUserInfo(final String userId) {
        executorService.execute(new Runnable() {
            public void run() {
                try {
                    JSONObject data = new JSONObject();
                    data.put("userId", user.userId);
                    data.put("cookie", Constants.cookie);
                    doPost("https://47.93.17.21/getUserInfo", data, GET_USER_INFO);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void doPost(String hostURL, JSONObject data, int type) {
        try {
            URL url = new URL(hostURL);
            if (url.getProtocol().toLowerCase().equals("https")) {
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                int a = Integer.valueOf("90",16);
                conn.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        /*boolean hostnameVerification = HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, session);
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
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                    String line = "";
                    String returnLine = "";
                    System.out.println("Contents of post request start");

                    while ((line = reader.readLine()) != null) {
                        returnLine += line;
                    }
                    Message message = new Message();
                    message.what = 3;
                    Bundle bundle = new Bundle();
                    bundle.putString("data", returnLine);
                    bundle.putInt("type", type);
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

    private void showMyDialog(String title, final int type) {
        LayoutInflater inflater = LayoutInflater.from(UserActivity.this);
        View layout = inflater.inflate(R.layout.dialog, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this).setView(layout).setCancelable(false);

        final AlertDialog dialog = builder.create();
        dialog.show();
        ImageView icon = (ImageView) layout.findViewById(R.id.icon);
        TextView headText = (TextView) layout.findViewById(R.id.header_text);
        final Button ok_btn = (Button) layout.findViewById(R.id.ok_button);
        Button cancel_btn = (Button) layout.findViewById(R.id.cancel_button);
        final EditText moneyView = (EditText) layout.findViewById(R.id.money);
        final EditText phoneView = (EditText) layout.findViewById(R.id.phone);
        final EditText scoreView = (EditText) layout.findViewById(R.id.score);


        moneyView.addTextChangedListener(new TextWatcher() {
                                             @Override
                                             public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                             }

                                             @Override
                                             public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                             }

                                             @Override
                                             public void afterTextChanged(Editable editable) {
                                                 if ((scoreView.getVisibility() == View.VISIBLE && TextUtils.isEmpty(scoreView.getText())) || (moneyView.getVisibility() == View.VISIBLE && TextUtils.isEmpty(moneyView.getText())))
                                                     ok_btn.setEnabled(false);
                                                 else
                                                     ok_btn.setEnabled(true);
                                             }
                                         }
        );
        scoreView.addTextChangedListener(new TextWatcher() {
                                             @Override
                                             public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                             }

                                             @Override
                                             public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                             }

                                             @Override
                                             public void afterTextChanged(Editable editable) {
                                                 if ((scoreView.getVisibility() == View.VISIBLE && TextUtils.isEmpty(scoreView.getText())) || (moneyView.getVisibility() == View.VISIBLE && TextUtils.isEmpty(moneyView.getText())))
                                                     ok_btn.setEnabled(false);
                                                 else
                                                     ok_btn.setEnabled(true);
                                             }
                                         }
        );

        if (type == TYPE_CONSUME_SCORE) {
            scoreView.setVisibility(View.VISIBLE);
        }
        if (type == ACTIVE_VIP) {
            moneyView.setVisibility(View.VISIBLE);
            phoneView.setVisibility(View.VISIBLE);
        }
        if (type == TYPE_COMSUME_CASH || type == TYPE_COMSUME_CHARGE) {
            moneyView.setVisibility(View.VISIBLE);
            scoreView.setVisibility(View.VISIBLE);
        }
        if (type == TYPE_RECHARGE) {
            moneyView.setVisibility(View.VISIBLE);
        }
        if (type == TYPE_COMSUME_CASH || type == TYPE_RECHARGE)
            icon.setBackgroundResource(R.drawable.cash);
        else
            icon.setBackgroundResource(R.drawable.vip);
        headText.setText(title);

        ok_btn.setOnClickListener(new View.OnClickListener()

                                  {
                                      @Override
                                      public void onClick(View view) {
                                          executorService.execute(new Runnable() {
                                              public void run() {
                                                  try {
                                                      String money = moneyView.getText().toString().trim();
                                                      String score = scoreView.getText().toString().trim();
                                                      JSONObject data = new JSONObject();
                                                      data.put("type", type);
                                                      data.put("operator", operatorName);
                                                      data.put("time", new Date().getTime());
                                                      data.put("cookie", Constants.cookie);
                                                      data.put("shop", shop);
                                                      data.put("userId", userID);
                                                      if (moneyView.getVisibility() == View.VISIBLE)
                                                          data.put("money", Double.parseDouble(money));
                                                      if (scoreView.getVisibility() == View.VISIBLE)
                                                          data.put("score", Double.parseDouble(score));
                                                      if (type == TYPE_COMSUME_CHARGE) {
                                                          double moneyValue = Double.parseDouble(money);
                                                          if (moneyValue <= user.money) {
                                                              doPost("https://47.93.17.21/consume", data, TYPE_COMSUME_CHARGE);
                                                          } else
                                                              handler.sendEmptyMessage(1);
                                                      } else if (type == TYPE_RECHARGE) {
                                                          doPost("https://47.93.17.21/charge", data, TYPE_RECHARGE);
                                                      } else if (type == ACTIVE_VIP) {
                                                          data.put("phone", phoneView.getText().toString().trim());
                                                          doPost("https://47.93.17.21/activeVIP", data, ACTIVE_VIP);
                                                      } else if (type == TYPE_COMSUME_CASH) {
                                                          doPost("https://47.93.17.21/consume", data, TYPE_COMSUME_CASH);
                                                      } else if (type == TYPE_CONSUME_SCORE) {
                                                          double scoreValue = Double.parseDouble(score);
                                                          if (scoreValue <= user.score) {
                                                              doPost("https://47.93.17.21/consume", data, TYPE_CONSUME_SCORE);
                                                          } else
                                                              handler.sendEmptyMessage(2);
                                                      }

                                                  } catch (JSONException e) {
                                                      e.printStackTrace();
                                                  }
                                              }
                                          });
                                          dialog.dismiss();

                                      }
                                  }

        );
        cancel_btn.setOnClickListener(new View.OnClickListener()

                                      {
                                          @Override
                                          public void onClick(View view) {
                                              dialog.dismiss();

                                          }
                                      }

        );
    }

    private void showAlertForReLogin(final String content) {
        AlertDialog alertDialog = new AlertDialog.Builder(UserActivity.this)
                //.setIcon(R.drawable.ic_launch)
                .setTitle(R.string.alert_title)
                .setMessage(content)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Constants.cookie = null;
                                Intent intent = new Intent(context, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        }
                )
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.show();
    }

    private void showAlert(String content) {
        AlertDialog alertDialog = new AlertDialog.Builder(UserActivity.this)
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


    private void initOperationList() {
        operationList.clear();
        operationList.add(new Operation("充值", getResources().getDrawable(R.drawable.cash), TYPE_RECHARGE));
        operationList.add(new Operation("会员消费", getResources().getDrawable(R.drawable.vip), TYPE_COMSUME_CHARGE));
        operationList.add(new Operation("现金消费", getResources().getDrawable(R.drawable.cash), TYPE_COMSUME_CASH));
        operationList.add(new Operation("激活会员", getResources().getDrawable(R.drawable.vip), ACTIVE_VIP));
        operationList.add(new Operation("消费积分", getResources().getDrawable(R.drawable.vip), TYPE_CONSUME_SCORE));
        adapter.notifyDataSetChanged();

    }
}
