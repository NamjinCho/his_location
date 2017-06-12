package com.example.mpterm;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONObject;

public class enrolledChild extends ListActivity {

    JSONObject personJson;
    TCPClient mTcpClient;
    String myID;
    String State;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ---no need to call this---
        //setContentView(R.layout.main);
        State =getIntent().getStringExtra("State");
       // String []myList = new String [MainActivity.ChildNames.length-1];

        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, MainActivity.ChildNames));
        personJson = new JSONObject();
     //   mTcpClient=Login.mTcpClient;
        myID=getIntent().getStringExtra("ID");

    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        Intent his = new Intent(this, hislocation.class);
        //서버에서 아이의 위치 좌표를 가져옴~
            his.putExtra("ChildName", MainActivity.ChildNames[position]);
            startActivity(his);
            finish();
    }
}
