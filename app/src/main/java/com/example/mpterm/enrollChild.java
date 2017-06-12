package com.example.mpterm;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 남지니 on 2016-05-06.
 */
public class enrollChild extends Activity {
    JSONObject personJson;
    private TCPClient mTcpClient;
    @Override
    public void onCreate(Bundle icleicle)
    {
        super.onCreate(icleicle);
        setContentView(R.layout.enroll_child);
        personJson = new JSONObject();
        mTcpClient=Login.mTcpClient;
    }
    public void enroll(View v) throws JSONException {

        try {//ChildList Update
            personJson.put("type", "insertChild");
            personJson.put("username", "1");
            personJson.put("childName", "2");
            personJson.put("childEmail", "3");
            if (mTcpClient != null) {
                mTcpClient.sendMessage(personJson.toString());
            }
        } catch (Exception es) {
            Log.e("ERROR", "login json err", es);
        }
        finish();
    }
}
