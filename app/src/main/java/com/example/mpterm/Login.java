package com.example.mpterm;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends Activity implements View.OnClickListener {

    public static TCPClient mTcpClient;
    static public SubWay[] subWays = new SubWay[520];
    public static String myTok="";
    static int checker = 0;
    TokenGenerator tokenGenerator;
    String MyID;
    String MyPass;
    int autoLogin;
    Bundle results = new Bundle();
    JSONObject personJson = new JSONObject();
    //RadioGroup radioGroup;
    //RadioButton childRadio, parentRadio;
    boolean who = false; // true = parent / false = child
    SharedPreferences shpf;
    String utype;
    Login myLogin;
    String sender;
    ImageButton PB, CB;
    TextView text1;
    TextView text2;
    TextView text_c,text_c2,text_c3,text_c4;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String returnedValue = (String) msg.obj;
            if(returnedValue.startsWith("1-1"))
                login();
            else if(returnedValue.startsWith("2-1"))
                Toast.makeText(getApplicationContext(),"회원가입이 되셨습니다.",Toast.LENGTH_SHORT).show();
            else if(returnedValue.startsWith("2-2"))
                Toast.makeText(getApplicationContext(),"회원가입에 실패하셨습니다.",Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        shpf = getSharedPreferences("AutoLogin", MODE_WORLD_WRITEABLE);
        init();
    }

    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void init() {
        PB = (ImageButton) findViewById(R.id.imageButton);
        CB = (ImageButton) findViewById(R.id.imageButton2);
        PB.setOnClickListener(this);
        CB.setOnClickListener(this);
        autoLogin = shpf.getInt("autoLogin", 0);
        myLogin = this;
        turnGPSOn();
    }

    public void login() {
        if (checker == 1) {//TCP쪽에서 로그인성공이면 체커는 1
            Intent data;
            if (who == true) {
                data = new Intent(this, MainActivity.class);
                data.putExtra("ID", MyID);
            } else {
                data = new Intent(this, ChildMain.class);
                data.putExtra("ID", MyID);
            }
            SharedPreferences.Editor editor = shpf.edit();
            editor.putString("ID", MyID);
            editor.putString("Pass", MyPass);
            editor.putString("userType", utype);
            editor.putInt("autoLogin", 1);
            editor.putInt("Subs", 0);
            editor.commit();
            startActivity(data);
        } else
            Toast.makeText(getApplicationContext(), "로그인정보가 옳바르지 않습니다.", 1);
        checker = 0;

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == PB.getId()) {
            who = true;

            final Custom_Dialog dialog = new Custom_Dialog(this, R.style.AlertDialogCustom);

            dialog.setContentView(R.layout.dialog_parent);

            //final TextView text1 = (TextView) dialog.findViewById(R.id.txtUsername);
            //   final
            Button btn_ok = (Button) dialog.findViewById(R.id.login);
            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        text1 = (TextView) dialog.findViewById(R.id.txtUsername);
                        text2 = (TextView) dialog.findViewById(R.id.txtPassword);
                        personJson.put("type", "login");
                        personJson.put("username", text1.getText().toString());
                        personJson.put("password", text2.getText().toString());
                        personJson.put("userType", "Parent");
                        MyID = text1.getText().toString();
                        //sends the message to the server
                        sender = personJson.toString();
                        mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                            public void messageReceived(String message) {
                            }
                        });
                        mTcpClient.start();
                        mTcpClient.setTcpLogin(Login.this, sender);
                    } catch (Exception e) {
                        Log.e("ERROR", "login json err", e);
                    }
                }
            });
            Button btn_can = (Button) dialog.findViewById(R.id.cancel);
            btn_can.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });
            Button btn_reg = (Button) dialog.findViewById(R.id.register);
            btn_reg.setOnClickListener(new View.OnClickListener() { // 회원가입 창 띄우기
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                    who = true;

                    final Custom_Dialog dialog2 = new Custom_Dialog(Login.this, R.style.AlertDialogCustom);

                    dialog2.setContentView(R.layout.dialog_parent_register);
                    tokenGenerator = new TokenGenerator(getApplicationContext());
                    Button btn_ok_p = (Button) dialog2.findViewById(R.id.p_register);
                    btn_ok_p.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                text_c = (TextView) dialog2.findViewById(R.id.txtUsername_p);
                                text_c2 = (TextView) dialog2.findViewById(R.id.txtPassword_p);
                                text_c3 = (TextView) dialog2.findViewById(R.id.txtEmail_p);
                                personJson.put("type", "register");
                                personJson.put("username", text_c.getText().toString());
                                personJson.put("password", text_c2.getText().toString());
                                personJson.put("email", text_c3.getText().toString());
                                personJson.put("userType", "Parent");
                                personJson.put("token", myTok);
                                MyID = text_c.getText().toString();
                                //sends the message to the server
                                sender = personJson.toString();
                                mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                                    public void messageReceived(String message) {
                                    }
                                });
                                mTcpClient.start();
                                mTcpClient.setTcpLogin(Login.this, sender);

                            } catch (Exception e) {
                                Log.e("ERROR", "login json err", e);
                            }
                        }
                    });
                    Button btn_can_p = (Button) dialog2.findViewById(R.id.p_cancel);
                    btn_can_p.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog2.cancel();
                        }
                    });

                    dialog2.show();
                }
            });

            dialog.show();
        } else if (v.getId() == CB.getId()) {
            who = false;
            final Custom_Dialog dialog_2 = new Custom_Dialog(this, R.style.AlertDialogCustom);

            dialog_2.setContentView(R.layout.dialog_child);

            Button btn_ok = (Button) dialog_2.findViewById(R.id.login);
            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {

                        text1 = (TextView)dialog_2.findViewById(R.id.txtUsername_);
                        text2 = (TextView)dialog_2.findViewById(R.id.txtPassword_);
                        personJson.put("type", "login");
                        personJson.put("username", text1.getText().toString());
                        personJson.put("password", text2.getText().toString());
                        personJson.put("userType", "Child");
                        MyID = text1.getText().toString();
                        //sends the message to the server
                        sender = personJson.toString();
                        mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                            public void messageReceived(String message) {
                            }
                        });
                        mTcpClient.start();
                        mTcpClient.setTcpLogin(Login.this, sender);

                    } catch (Exception e) {
                        Log.e("ERROR", "login json err", e);
                    }
                }
            });
            Button btn_can = (Button) dialog_2.findViewById(R.id.cancel);
            btn_can.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog_2.cancel();
                }
            });
            Button btn_reg = (Button) dialog_2.findViewById(R.id.register);
            btn_reg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog_2.cancel();
                    who = true;

                    final Custom_Dialog dialog_22 = new Custom_Dialog(Login.this, R.style.AlertDialogCustom);

                    dialog_22.setContentView(R.layout.dialog_child_register);
                    tokenGenerator = new TokenGenerator(getApplicationContext());
                    Button btn_ok_c = (Button) dialog_22.findViewById(R.id.c_register);
                    btn_ok_c.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {

                                text_c = (TextView)dialog_22.findViewById(R.id.txtUsername_c);
                                text_c2 = (TextView)dialog_22.findViewById(R.id.txtPassword_c);
                                text_c3 = (TextView)dialog_22.findViewById(R.id.txtEmail_c);
                                text_c4 = (TextView)dialog_22.findViewById(R.id.txtPname_c);
                                personJson.put("type", "register");
                                personJson.put("username", text_c.getText().toString());
                                personJson.put("password", text_c2.getText().toString());
                                personJson.put("email", text_c3.getText().toString());
                                personJson.put("parentName", text_c4.getText().toString());
                                personJson.put("userType", "Child");
                                personJson.put("token",myTok);
                                MyID = text_c.getText().toString();
                                //sends the message to the server
                                sender = personJson.toString();
                                mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                                    public void messageReceived(String message) {
                                    }
                                });
                                mTcpClient.start();
                                mTcpClient.setTcpLogin(Login.this, sender);

                            } catch (Exception e) {
                                Log.e("ERROR", "login json err", e);
                            }
                        }
                    });
                    Button btn_can_c = (Button) dialog_22.findViewById(R.id.c_cancel);
                    btn_can_c.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog_22.cancel();
                        }
                    });

                    dialog_22.show();
                }
            });

            dialog_2.show();
        } else {
            Intent intent = new Intent(Login.this, Join.class);
            intent.putExtras(results);
            startActivityForResult(intent, 222);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        super.onActivityResult(requestCode, resultCode, Data);
    }

    private void turnGPSOn(){
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(!provider.contains("gps")){ //if gps is disabled
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }
    public void RequestSubway() {
        if (shpf.getInt("Subs", 0) == 0) {
            try {
                personJson.put("type", "start");
                sender = personJson.toString();
                mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                    public void messageReceived(String message) {
                    }
                });
                mTcpClient.start();
                mTcpClient.setTcpLogin(this, sender);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void getSubway(String msg) {
        String[] message;
        message = msg.split("/");
        for (int i = 0; i < message.length; i = i + 4) {
            // Log.d("asd", message[i]);
            subWays[i / 4] = new SubWay();
            subWays[i / 4].setName(message[i]);
            subWays[i / 4].setLine(message[i + 1]);
            subWays[i / 4].Lat = Double.parseDouble(message[i + 2]);
            subWays[i / 4].Lng = Double.parseDouble(message[i + 3]);
        }
    }

    public void sendMessage(String sender) {
        mTcpClient.sendMessage(sender);
    }

    public class connectTask extends AsyncTask<String, String, TCPClient> {
        @Override
        protected TCPClient doInBackground(String... message) {

            //we create a TCPClient object and
            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {


                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            mTcpClient.setTcpLogin(myLogin, sender);
            mTcpClient.run();
            return null;
        }
    }
}