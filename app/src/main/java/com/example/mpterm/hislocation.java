package com.example.mpterm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class hislocation extends FragmentActivity implements OnMapReadyCallback {
    private static final int[] ITEM_DRAWABLES = {R.drawable.ring, R.drawable.route,
            R.drawable.locations};
    double Lat, Lng, CSpeed;
    hislocation me = this;
    Marker loc;
    Polyline line;
    LatLng[] safePoints;
    boolean traceChecker = false;
    String Sender;
    boolean dangerFlag = false;
    SharedPreferences mystate;
    String OnOff;
    private GoogleMap mMap;
    private String ChildName;
    // hislocation me =this;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String returnedValue = (String) msg.obj;
            if (returnedValue.startsWith("4-1"))
                getChildLocation(returnedValue);
            else if (returnedValue.startsWith("6-1")) {
                getTraceLocation(returnedValue.replaceFirst("6-1/", ""));
            } else if (returnedValue.startsWith("delete"))
                traceMarkerDelete();
            else if (returnedValue.startsWith("7-1")) {
                Toast.makeText(getApplicationContext(), "아이의 위치를 1시간 이상 알 수 없습니다.", Toast.LENGTH_LONG).show();
                getChildLocation(returnedValue);
            }
        }

    };
    private TCPClient mTcpClient;
    private JSONObject personJson;
    private taker mTaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hislocation);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // mTcpClient = Login.mTcpClient;
        // mTcpClient.setTcpHisLocation(this, Sender);
        personJson = new JSONObject();
        ChildName = getIntent().getStringExtra("ChildName");
        ArcMenu arcMenu = (ArcMenu) findViewById(R.id.arc_menu);
        initArcMenu(arcMenu, ITEM_DRAWABLES);

        final int itemCount = ITEM_DRAWABLES.length;
        for (int i = 0; i < itemCount; i++) {
            ImageView item = new ImageView(this);
            item.setImageResource(ITEM_DRAWABLES[i]);
        }
        mystate = PreferenceManager.getDefaultSharedPreferences(this);
        OnOff = mystate.getString("OnOff" + getIntent().getStringExtra("ID").toString(), "on");

    }

    private void initArcMenu(ArcMenu menu, int[] itemDrawables) {
        final int itemCount = itemDrawables.length;
        for (int i = 0; i < itemCount; i++) {
            ImageView item = new ImageView(this);
            item.setImageResource(itemDrawables[i]);

            final int position = i;
            menu.addItem(item, new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // Toast.makeText(getApplicationContext(), "position:" + position, Toast.LENGTH_SHORT).show();


                    if (position == 0) { // 키기 보내기  끄기 보내기
                        if (OnOff.equals("on")) {
                            OnOff = "off";
                            Toast.makeText(getApplicationContext(), "더이상 알람을 받지않습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            OnOff = "on";
                            Toast.makeText(getApplicationContext(), "이제부터 알람을 받습니다.", Toast.LENGTH_SHORT).show();

                        }
                        SharedPreferences.Editor editor = mystate.edit();
                        editor.putString("OnOff" + getIntent().getStringExtra("ID").toString(), OnOff);
                        editor.commit();
                        try {
                            personJson.put("type", "gcm");
                            personJson.put("username", getIntent().getStringExtra("ID").toString());
                            Sender = personJson.toString();
                            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                                public void messageReceived(String message) {
                                }
                            });
                            mTcpClient.start();
                            mTcpClient.setTcpHisLocation(me, Sender);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (position == 1) {
                        final Context context = getApplicationContext();
                        AlertDialog.Builder builder = new AlertDialog.Builder(hislocation.this);
                        builder.setTitle("Title");

// Set up the input
                        final EditText input = new EditText(context);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                        input.setInputType(InputType.TYPE_CLASS_NUMBER);
                        input.setTextColor(Color.BLACK);
                        input.setHint("원하는 기간을 입력하세요( 단위: 시간) ");
                        builder.setView(input);

// Set up the buttons
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //Toast.makeText(context, input.getText().toString(),Toast.LENGTH_SHORT).show();
                                if (input.getText().toString().equals("")) {
                                    return;
                                }
                                if (traceChecker == false) {
                                    try {
                                        personJson.put("type", "getTrace");
                                        personJson.put("childName", ChildName);
// Handle item selection
                                        personJson.put("time", Integer.parseInt(input.getText().toString()));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Sender = personJson.toString();
                                    mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                                        public void messageReceived(String message) {
                                        }
                                    });
                                    mTcpClient.start();
                                    mTcpClient.setTcpHisLocation(me, Sender);
                                } else
                                    Toast.makeText(getApplicationContext(), "이미 추적이 진행중입니다!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();

                    }
                    if (position == 2) { // 안전구역
                        if (!dangerFlag) {
                            dangerFlag = true;
                            Toast.makeText(getApplicationContext(), "원하시는 위치를 길게 누르세요!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, Menu.NONE, "1시간동안 위치 보기")
                .setIcon(android.R.drawable.ic_menu_add);
        menu.add(0, 2, Menu.NONE, "5시간동안 위치 보기")
                .setIcon(android.R.drawable.ic_menu_call);
        menu.add(0, 3, Menu.NONE, "10시간동안 위치 보기")
                .setIcon(android.R.drawable.ic_menu_camera);
        menu.add(0, 4, Menu.NONE, "안전구역 등록하기")
                .setIcon(android.R.drawable.ic_menu_camera);
        menu.add(0, 5, Menu.NONE, "안전구역 해지하기")
                .setIcon(android.R.drawable.ic_menu_camera);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().toString().startsWith("안전구역 해")) {
            final Context context = getApplicationContext();
            AlertDialog.Builder builder = new AlertDialog.Builder(hislocation.this);
            builder.setTitle("Title");

// Set up the input
            final TextView input = new TextView(context);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setText("아이의 안전구역을 해제 하시겠습니까?");
            builder.setView(input);

// Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        personJson.put("type", "dangerZone");
                        personJson.put("childName", ChildName);
                        personJson.put("subType", "delete");
                        Log.d("asd", "delete");
                        Sender = personJson.toString();
                        mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                            public void messageReceived(String message) {
                            }
                        });
                        mTcpClient.start();
                        mTcpClient.setTcpHisLocation(me, Sender);

                        Toast.makeText(getApplicationContext(), "안전구역이 해제 되었습니다", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();

        } else if (item.getTitle().toString().startsWith("안전구역 등")) {
            dangerFlag = true;
            Toast.makeText(getApplicationContext(), "원하시는 위치를 길게 누르세요!", Toast.LENGTH_SHORT).show();
        } else {
            if (traceChecker == false) {
                try {
                    personJson.put("type", "getTrace");
                    personJson.put("childName", ChildName);
// Handle item selection
                    if (item.getTitle().toString().startsWith("1시간"))
                        personJson.put("time", 1);
                    else if (item.getTitle().toString().startsWith("5시간"))
                        personJson.put("time", 5);
                    else if (item.getTitle().toString().startsWith("10시간"))
                        personJson.put("time", 10);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Sender = personJson.toString();
                mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                    public void messageReceived(String message) {
                    }
                });
                mTcpClient.start();
                mTcpClient.setTcpHisLocation(this, Sender);
            } else
                Toast.makeText(getApplicationContext(), "이미 추적이 진행중입니다!", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    public void sendMassage(String sender) {
        mTcpClient.sendMessage(sender);
        // Sender=null;
    }

    public void onStop() {
        super.onStop();
        mTaker.tok = false;
    }

    public void onRestart() {
        mTaker=new taker();
        mTaker.start();
        super.onRestart();
    }

    private void traceMarkerDelete() {

        Log.d("?", "??");
        if (line != null)
            line.remove();
        traceChecker = false;
    }

    private void getTraceLocation(String msg) {
        String[] tLatLng = msg.split("/");
        //Log.d(tLatLng[0],"s");
        int order = 1;
        safePoints = new LatLng[tLatLng.length / 2];

        if (tLatLng.length > 1 && !traceChecker) {
            for (int i = 0; i < tLatLng.length; i = i + 2) {
                setChildMap(Double.parseDouble(tLatLng[i]), Double.parseDouble(tLatLng[i + 1]), order);
                order++;
            }

            line = mMap.addPolyline(new PolylineOptions().add(safePoints));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(safePoints[order - 2]));
            traceChecker = true;
            traceDelete tD = new traceDelete();
            tD.start();
        } else if (traceChecker) {
            Toast.makeText(getApplicationContext(), "이미 추적이 진행중입니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    public void setChildMap() {
        LatLng coordinate = new LatLng(Lat, Lng);
        if (loc != null)
            loc.remove();
        loc = mMap.addMarker(new MarkerOptions().position(coordinate).title(ChildName + "는 여기에 있어용!"));
        if (CSpeed > 30)
            loc.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.bus));
        else
            loc.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.walk));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinate));
        //mMap.moveCamera(CameraUpdateFactory.zoomTo((float) 14));

    }

    public void setChildMap(double Lat, double Lng, int order) {

        safePoints[order - 1] = new LatLng(Lat, Lng);
        //mMap.moveCamera(CameraUpdateFactory.zoomTo((float) 14));

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //이부분 서버에서 받아올수 있도록한다.
        setTitle(ChildName + "의 위치는 어디?");
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(Lat, Lng);
        loc = mMap.addMarker(new MarkerOptions().position(sydney).title(ChildName + "는 여기에 있어용!"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.moveCamera(CameraUpdateFactory.zoomTo((float) 14));
        setChildMap();
        mTaker = new taker();
        mTaker.start();
        //requestChildLocation();
        Log.d("test", "???");

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {
                if (dangerFlag) {
                    double slat = latLng.latitude;
                    double slng = latLng.longitude;

                    Log.d("lomg", "lomgClick");

                    try {
                        personJson.put("type", "dangerZone");
                        personJson.put("childName", ChildName);
                        personJson.put("lat", slat);
                        personJson.put("lng", slng);
                        personJson.put("radius", 5000.0);
                        personJson.put("time", 1);
                        personJson.put("subType", "add");
                        Sender = personJson.toString();
                        mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                            public void messageReceived(String message) {
                            }
                        });
                        mTcpClient.start();
                        mTcpClient.setTcpHisLocation(me, Sender);
                        dangerFlag = false;
                        Toast.makeText(getApplicationContext(), "안전구역이 설정 되었습니다", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    final Context context = getApplicationContext();
                    AlertDialog.Builder builder = new AlertDialog.Builder(hislocation.this);
                    builder.setTitle("Title");

// Set up the input
                    final TextView input = new TextView(context);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setText("아이의 안전구역을 해제 하시겠습니까?");
                    builder.setView(input);

// Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                personJson.put("type", "dangerZone");
                                personJson.put("childName", ChildName);
                                personJson.put("subType", "delete");
                                Log.d("asd", "delete");
                                Sender = personJson.toString();
                                mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                                    public void messageReceived(String message) {
                                    }
                                });
                                mTcpClient.start();
                                mTcpClient.setTcpHisLocation(me, Sender);

                                Toast.makeText(getApplicationContext(), "안전구역이 해제 되었습니다", Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();


                }
            }
        });
    }

    public void requestChildLocation() {
        try {
            personJson.put("type", "getLocation");
            personJson.put("username", ChildName);
            Sender = personJson.toString();
            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                public void messageReceived(String message) {
                }
            });
            mTcpClient.start();
            mTcpClient.setTcpHisLocation(this, Sender);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getChildLocation(String msg) {
        String[] SLatLng = msg.split("/");
        Lat = Double.parseDouble(SLatLng[2]);
        Lng = Double.parseDouble(SLatLng[3]);
        CSpeed = Double.parseDouble(SLatLng[4]);
        Log.d("LATLNG CHILD ", Lat + "/" + Lng);
        setChildMap();
    }

    public class taker extends Thread {

        boolean tok = true;

        public void run() {
            requestChildLocation();
            while (tok) {
                try {
                    sleep(15000);
                    requestChildLocation();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public class traceDelete extends Thread {
        public void run() {
            boolean tok = true;
            try {
                while (tok) {
                    sleep(60000);
                    tok = false;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message msg = mHandler.obtainMessage(1, "delete");
            mHandler.sendMessage(msg);
        }
    }
}
