package com.example.mpterm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {
    public static String[] ChildNames;
    public TCPClient mTcpClient;
    public JSONObject personJson;
    public boolean onoffFlag = false;
    String State;
    String myID;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            String returnedValue = (String) msg.obj;
            Setting();
        }

    };
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private String sender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //제이슨 및 부모 이름 가져오기
        personJson = new JSONObject();
        myID = getIntent().getStringExtra("ID");
        // this.mTcpClient = Login.mTcpClient;
        //mTcpClient.setTcpMainActivity(this);
        requestChildList();
     //   getSystemService()
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());


        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                 invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

        public void Setting() {
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, ChildNames));


    }
    public void donate(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("기부 감사합니다").setMessage("711 - 12 - 103857(농협) / 조남진 감사합니다!").setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void ViewInformation(View v) {

        ImageView item = (ImageView)findViewById(R.id.informationicon);
        ImageView item2 = (ImageView)findViewById(R.id.click);
        if (onoffFlag) {
            item.setVisibility(View.VISIBLE);
            item2.setVisibility(View.GONE);
        } else {
            item.setVisibility(View.GONE);
            item2.setVisibility(View.VISIBLE);
        }

        onoffFlag = !onoffFlag;
    }

    public void getChildList(String msg) {
        ChildNames = msg.split("/");
        Message msgs = mHandler.obtainMessage(1, "");
        mHandler.sendMessage(msgs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void requestChildList() {
        try {
            personJson.put("type", "getList");
            personJson.put("username", myID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sender = personJson.toString();
        mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
            public void messageReceived(String message) {
            }
        });
        mTcpClient.start();
        mTcpClient.setTcpMainActivity(this, sender);

    }

    public void sendMassage(String sender) {
        mTcpClient.sendMessage(sender);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return true;
    }

    private void selectItem(int position) {

        Intent my = new Intent(this, hislocation.class);
        //my.putExtra("ChildName", ChildNames[position]);
        my.putExtra("ChildName", ChildNames[position]);
        my.putExtra("ID", myID);
        //my.putExtra("State",State);
        startActivity(my);
    }


    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }


}