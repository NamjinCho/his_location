package com.example.mpterm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 남지니 on 2016-04-12.
 */

public class Join extends Activity implements View.OnClickListener {
    Button joinbtn;
    EditText ID, Pass, Email, PName;
    RadioButton PBtn, CBtn;
    RadioGroup groups;
    Intent MyLocalIntent;
    private TCPClient mTcpClient;
    static int checker = 0;
    private JSONObject personJson;
    public static String myTok="";
    TokenGenerator tokenGenerator;
    String sender;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String returnedValue = (String)msg.obj;
            join(returnedValue);
        }};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        init();

    }

    public void init() {
        joinbtn = (Button) findViewById(R.id.Join);
        ID = (EditText) findViewById(R.id.ID);
        Pass = (EditText) findViewById(R.id.Pass);
        Email = (EditText) findViewById(R.id.Email);
        PName = (EditText) findViewById(R.id.PName);
        PBtn = (RadioButton) findViewById(R.id.ParentRadio);
        CBtn = (RadioButton) findViewById(R.id.ChildRadio);
        groups = (RadioGroup) findViewById(R.id.Groups);
        PName.setEnabled(false);
        MyLocalIntent = getIntent();
        joinbtn.setOnClickListener(this);
        PBtn.setOnClickListener(this);
        CBtn.setOnClickListener(this);
        personJson = new JSONObject();
        if (PBtn.isChecked())
            PName.setEnabled(true);

        tokenGenerator = new TokenGenerator(this);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == PBtn.getId()) {
            PName.setEnabled(false);
            PName.setFocusable(false);

        }
        if (v.getId() == CBtn.getId())
            PName.setEnabled(true);
        if (v.getId() == joinbtn.getId()) {
            try {
                personJson.put("type", "register");
                if (groups.getCheckedRadioButtonId() == PBtn.getId())
                    personJson.put("userType", PBtn.getText().toString());
                else {
                    personJson.put("userType", CBtn.getText().toString());
                    personJson.put("parentName", PName.getText().toString());
                }

                personJson.put("email", Email.getText().toString());
                personJson.put("username", ID.getText().toString());
                personJson.put("password", Pass.getText().toString());
                personJson.put("token",myTok);
                sender = personJson.toString();
                mTcpClient = new TCPClient(new TCPClient.OnMessageReceived(){
                    public void messageReceived(String message) {
                    }
                });
                mTcpClient.start();
                mTcpClient.setTcpJoin(this,sender);

            } catch (Exception e) {
                Log.e("ERROR", "register json err", e);
            }
        }
    }

    public void join(String msg) {
        if(msg.startsWith("2-1"))
        {
            Toast.makeText(this, "회원가입에 성공했습니다.", 1);
            setResult(Activity.RESULT_OK, getIntent());
         //   checker = 0;
            finish();
        }
        else
            Toast.makeText(this, "입력된 정보가 중복 되었거나 옳바르지 않습니다.", 1);
    }

    public void sendMessage(String sender) {
        mTcpClient.sendMessage(sender);
    }
}
