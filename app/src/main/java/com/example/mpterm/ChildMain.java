package com.example.mpterm;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

public class ChildMain extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private static final long MIN_TIME_BW_UPDATES = 1;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    static int Index = -1;
    public Uploader upTh;
    public String myID;
    public SubwayFinder finder;
    public String Sender;
    //    LocationGetter locationGetter;
    Location myLocation;
    double lat = 0, lng = 0, old_lat = 0, old_lng = 0;
    double safeLat = 0, safeLng = 0;
    CircleOptions circleOptions;
    Marker myLMark;
    JSONObject personJson;
    TCPClient mTCPClient;
    double mySpeed = 0;
    boolean Mode_S = false;
    WIFIScanner scanner = null;
    Circle circle;
    ChildMain me = this;
    //지하철 이동 알려주기 위한 변수들
    String oldLine;
    String oldStation;
    private GoogleMap mMap;
    //
    Handler mHandler = new Handler() {
        @Override


        public void handleMessage(Message msg) {
            String returnedValue = (String) msg.obj;
            Log.d("aㅁㄴㅇ", returnedValue);
            if (returnedValue.startsWith("Indoor"))
                myMap();
            else if (returnedValue.startsWith("setMyLocation")) {
                myMap();
            } else if (returnedValue.startsWith("5-1"))
                setSafeLatLng(returnedValue);
            else if (returnedValue.startsWith("5-2"))
                deleteDangerZone();
        }
    };
    //  private String Sender;

    private void deleteDangerZone() {
        if (circle != null)
            circle.remove();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //turnGPSOn();
        setContentView(R.layout.activity_child_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // mTCPClient = Login.mTcpClient;
        personJson = new JSONObject();
        //myLocation = getLocation();
        upTh = new Uploader(this);
        lat = getLocation().getLatitude();
        lng = getLocation().getLongitude();
        myID = getIntent().getStringExtra("ID");
        oldLine = null;
        oldStation = null;
        upTh.start();

        //finder.start();
        Intent intent = new Intent(getBaseContext(), LocationUdater.class);
        intent.putExtra("ID", myID);
        startService(intent);
        //setMap();
        Log.d("Test", "lat : " + lat + " lng : " + lng);
        //lat=0;//lng=0;
    }
    public void onRestart() {
        super.onRestart();
        upTh = new Uploader(this);
        upTh.start();
    }

    public void onStop() {
        super.onStop();
        upTh.Tstop();
        upTh = null;
        // finder.stop();
    }

    public void onDestroy() {
        //finder.flag = false;
        if (upTh != null)
            upTh.Tstop();
        super.onDestroy();
    }

    public void Emergency(View v) {
        try {
            personJson.put("type", "emergency");
            personJson.put("username", myID);
            Sender = personJson.toString();
            Log.d("asd", Sender);
            mTCPClient = new TCPClient(new TCPClient.OnMessageReceived() {
                public void messageReceived(String message) {
                }
            });
            mTCPClient.start();
            mTCPClient.setTcpChildMain(this, Sender);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onResume() {
        super.onResume();
    }

    public void sendMassage(String sender) {
        mTCPClient.sendMessage(sender);
        // Sender=null;
    }

    public Location getLocation() {
        Location location = null;
        LocationManager locationManager = null;
        boolean isGPSEnabled, isNetworkEnabled;
        Context mContext = this;
        try {
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isGPSEnabled == false && isNetworkEnabled == false) {

            } else {

                if (isGPSEnabled) {
                    if (location == null) {
                        Log.d("11","11");
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        if (locationManager != null) {
                            Log.d("11","14");
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            //location = locationManager.getLastKnownLocation()
                            Log.d("11","12");
                            if (location != null) {
                                Log.d("11","13");
                                return location;
                            }
                            else if(location==null)
                            {
                                Log.d("11","15");
                            }
                        }
                    }
                }
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            return location;
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(lat, lng);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Here Is My Locatation"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.moveCamera(CameraUpdateFactory.zoomTo((float) 14));
        //  mMap.addCircle(circleOptions);
        myLMark = mMap.addMarker(new MarkerOptions().position(sydney).title("Start"));

        myLMark.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.walk));

        Log.d("Test", "on Map lat : " + lat + " lng : " + lng);
        //  requestSafeLatLng();
    }

    public void requestSafeLatLng() {
        try {
            personJson.put("type", "dangerZone");
            personJson.put("subType", "check");
            personJson.put("username", myID);
            Sender = personJson.toString();
            Log.d("asd", Sender);
            mTCPClient = new TCPClient(new TCPClient.OnMessageReceived() {
                public void messageReceived(String message) {
                }
            });
            mTCPClient.start();
            mTCPClient.setTcpChildMain(this, Sender);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void setSafeLatLng(String msg) {
        String[] token = msg.split("/");
        safeLat = Double.parseDouble(token[1]);
        safeLng = Double.parseDouble(token[2]);
        double radius = Double.parseDouble(token[4]);
        Log.d("safe", safeLat + " : " + safeLng + " : " + radius);
        circleOptions = new CircleOptions().center(new LatLng(safeLat, safeLng)).radius(radius);
        if (circle == null)
            circle = mMap.addCircle(circleOptions);
        else {
            circle.remove();
            circle = mMap.addCircle(circleOptions);
        }
    }


    public void myMap() {

        LatLng coordinate = new LatLng(lat, lng);

        CameraUpdate update = CameraUpdateFactory.newLatLng(coordinate);
        mMap.moveCamera(update);
        if (myLMark != null)
            myLMark.remove();
        myLMark = mMap.addMarker(new MarkerOptions().position(coordinate).title("my Location1"));
        if (mySpeed > 30) {
            myLMark.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.bus));
        } else {
            myLMark.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.walk));
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        if (location != null) {
            mySpeed = location.getSpeed();

        }

        LatLng coordinate = new LatLng(lat, lng);

        CameraUpdate update = CameraUpdateFactory.newLatLng(coordinate);
        mMap.moveCamera(update);
        if (myLMark != null)
            myLMark.remove();
        myLMark = mMap.addMarker(new MarkerOptions().position(coordinate).title("my Location1"));
        if (mySpeed > 30) {
            myLMark.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.bus));
        } else {
            myLMark.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.walk));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    public double CalDistance(double safeLat, double safeLng) {
        double lat_do1, lat_do2, lat_do, lng_do1, lng_do2, lng_do;
        double lat_min1, lat_min2, lat_min, lng_min1, lng_min2, lng_min;
        double lat_sec1, lat_sec2, lat_sec, lng_sec, lng_sec2, lng_sec1;

        lat_do1 = Math.floor(lat);
        lat_do2 = Math.floor(safeLat);
        lng_do1 = Math.floor(lng);
        lng_do2 = Math.floor(safeLng);
        lat_min1 = Math.floor((lat - lat_do1) * 60);
        lat_min2 = Math.floor((safeLat - lat_do2) * 60);
        lng_min1 = Math.floor((lng - lng_do1) * 60);
        lng_min2 = Math.floor((safeLng - lng_do2) * 60);
        lat_sec1 = Math.floor((((lat - lat_do1) * 60) - lat_min1) * 60);
        lat_sec2 = Math.floor((((safeLat - lat_do2) * 60) - lat_min2) * 60);
        lng_sec1 = Math.floor((((lng - lng_do1) * 60) - lng_min1) * 60);
        lng_sec2 = Math.floor((((safeLng - lng_do2) * 60) - lng_min2) * 60);
        lat_do = lat_do1 - lat_do2;
        lng_do = lng_do1 - lng_do2;
        lat_min = lat_min1 - lat_min2;
        lng_min = lng_min1 - lng_min2;
        lat_sec = lat_sec1 - lat_sec2;
        lng_sec = lng_sec1 - lng_sec2;

        lat_do = lat_do * 111 + lat_min * 1.85 + lat_sec * 0.031;
        lng_do = lng_do * 88.8 + lng_min * 1.48 + lng_sec * 0.025;
        double result = Math.pow((Math.pow(lat_do, 2) + Math.pow(lng_do, 2)), 0.5);
        return result;
    }

    public class Uploader extends Thread {

        int start = 0;
        int timeChecker = 0;
        int alert = 0;
        Context context;

        Uploader(Context c) {
            context = c;
        }

        public void run() {
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finder = new SubwayFinder(520, lat, lng);
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                while (start == 0) {
                    timeChecker++;
                    Thread.sleep(10000);
                    old_lat = lat;
                    old_lng = lng;
                    lat = getLocation().getLatitude();
                    lng = getLocation().getLongitude();
                    mySpeed = CalDistance(old_lat, old_lng) * 360;//속도 구하기
                    Log.d("Speed", mySpeed + "");
                    timeChecker++;
                    requestSafeLatLng();

                    if (finder != null && finder.index != -1) {
                        finder.setLatLng(lat, lng);
                        Log.d("제일 가까운역은 : ", finder.mySubs[finder.index].name + " dist :" + finder.mySubs[finder.index].Dist);
                        if (Mode_S == true) {
                            if (scanner == null) {
                                scanner = new WIFIScanner(context, finder.mySubs.length, finder.mySubs);
                                //scanner.indoor=true;
                                scanner.initWIFIScan();
                            } else {
                                Log.d("asd", scanner.indoor + "");
                                if (scanner.indoor) {
                                    lat = finder.mySubs[finder.index].Lat;
                                    lng = finder.mySubs[finder.index].Lng;
                                }

                                if (finder.mySubs[finder.index].Dist > 0.1) {
                                    Mode_S = false;
                                    unregisterReceiver(scanner.mReceiver);
                                    scanner = null;
                                }
                            }
                        }
                        if (Mode_S == false && finder.mySubs[finder.index].Dist <= 0.1) {
                            Mode_S = true;
                            xmlPasser xP = new xmlPasser(finder.mySubs[finder.index].name, finder.mySubs[finder.index]);
                            xP.start();
                        } else if (Mode_S == false) {
                            Message msg = mHandler.obtainMessage(1, "Indoor");
                            mHandler.sendMessage(msg);

                            if (finder.mySubs[finder.index].Dist > 0.5) {
                                oldLine = null;
                                oldStation = null;
                            }
                        }
                    }


                    Message msgs = mHandler.obtainMessage(1, "setMyLocation");
                    mHandler.sendMessage(msgs);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (finder != null) {
                finder.flag = false;
                finder = null;
            }
        }

        public void Tstop() {
            start = 1;
        }
    }
}
