package com.example.mpterm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 남지니 on 2016-06-03.
 */
public class LocationUdater extends Service implements LocationListener {
    private static final long MIN_TIME_BW_UPDATES = 1;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 5;
    static int Index = -1;
    JSONObject personJson;
    String myID;
    double lat, lng;
    double oldlat, oldlng;
    TCPClient mTCPClient;
    LocationUdater me = this;
    String Sender;
    Updater updater = null;
    String oldLine;
    String oldStation;
    boolean Mode_S;
    WIFIScanner scanner = null;
    double safeLat = 0, safeLng = 0;
    private SubwayFinder finder=null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
// We want this service to continue running until it is explicitly
// stopped, so return sticky.
        myID = intent.getStringExtra("ID");
        lat = getLocation().getLatitude();
        lng = getLocation().getLongitude();
        personJson = new JSONObject();
        oldLine = null;
        oldStation = null;
        requestSafeLatLng();

        if (updater == null) {
            updater = new Updater();
            updater.start();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            finder = new SubwayFinder(520, lat, lng);
            //finder.start();
        }
        return START_REDELIVER_INTENT;
    }
    @Override
    public void onDestroy() {
        if(updater!=null)
        {
            updater.flag=false;
            if(scanner!=null)
            {
                unregisterReceiver(scanner.mReceiver);
                scanner = null;
            }
            updater=null;
        }
        super.onDestroy();
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
            mTCPClient.setTcpUdater(this, Sender);
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
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                return location;
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

    @Override
    public void onLocationChanged(Location location) {

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

    class Updater extends Thread {
        double mySpeed = 0;
        boolean flag = true;
        int check = 0;

        public void run() {
            try {
                sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (flag) {
                try {
                    sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                oldlat = lat;
                oldlng = lng;
                lat = getLocation().getLatitude();
                lng = getLocation().getLongitude();
                finder.setLatLng(lat, lng);

                requestSafeLatLng();
                try {
                    sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (finder.index != -1) {
                    Log.d("여기는 서비스 제일 가까운역은 : ", finder.mySubs[finder.index].name + " dist :" + finder.mySubs[finder.index].Dist);

                    finder.setLatLng(lat, lng);
                    if (Mode_S == true) {
                        if (scanner == null) {
                            scanner = new WIFIScanner(getApplication(), finder.mySubs.length, finder.mySubs);
                            scanner.initWIFIScan();
                            //scanner.indoor=true;
                        } else {
                            if (scanner.indoor) {
                                if (oldLine == null && oldStation == null) {
                                    oldLine = LineMap.getLine(finder.mySubs[finder.index].line);
                                    oldStation = finder.mySubs[finder.index].name;

                                    //역안으로 들어갔습니다 보낼게
                                    try {
                                        personJson.put("type", "noticeSubway");
                                        personJson.put("username", myID);
                                        personJson.put("message", "지금 " + finder.mySubs[finder.index].name + "역 안으로" + myID + "가 들어갔습니다.");
                                        Sender = personJson.toString();
                                        mTCPClient = new TCPClient(new TCPClient.OnMessageReceived() {
                                            public void messageReceived(String message) {
                                            }
                                        });
                                        mTCPClient.start();
                                        mTCPClient.setTcpUdater(me, Sender);
                                        try {
                                            sleep(5000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }


                                } else {
                                    Log.d("old " + oldStation, "new " + finder.mySubs[finder.index].name);
                                    if (oldLine.equals(LineMap.getLine(finder.mySubs[finder.index].line)) && !finder.mySubs[finder.index].name.equals(oldStation)) {
                                        try {
                                            personJson.put("type", "noticeSubway");
                                            personJson.put("username", myID);
                                            personJson.put("message", "지금 " + myID + " 가 " + oldLine + "으로 " + oldStation + "역에서" +
                                                    finder.mySubs[finder.index].name + "역으로 이동중입니다.");
                                            Sender = personJson.toString();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        mTCPClient = new TCPClient(new TCPClient.OnMessageReceived() {
                                            public void messageReceived(String message) {
                                            }
                                        });
                                        Sender = personJson.toString();
                                        mTCPClient.start();
                                        mTCPClient.setTcpUdater(me, Sender);
                                        oldLine = LineMap.getLine(finder.mySubs[finder.index].line);
                                        oldStation = finder.mySubs[finder.index].name;
                                        try {
                                            sleep(5000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (!oldLine.equals(LineMap.getLine(finder.mySubs[finder.index].line))) {
                                        oldLine = LineMap.getLine(finder.mySubs[finder.index].line);
                                        oldStation = finder.mySubs[finder.index].name;
                                    }
                                }
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

                    }
                    if (finder.mySubs[finder.index].Dist > 0.5) {
                        oldLine = null;
                        oldStation = null;
                    }
                }
                Log.d("dangers", "" + safeLat + " " + safeLng);
                if (safeLat != 0 && safeLng != 0) {
                    Log.d("dangers", CalDistance(safeLat, safeLng) + "");
                    if (CalDistance(safeLat, safeLng) >= 5.0) {
                        try {
                            personJson.put("type", "dangerZone");
                            personJson.put("subType", "alert");
                            personJson.put("username", myID);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Sender = personJson.toString();
                        mTCPClient = new TCPClient(new TCPClient.OnMessageReceived() {
                            public void messageReceived(String message) {
                            }
                        });
                        mTCPClient.start();
                        mTCPClient.setTcpUdater(me, Sender);
                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mySpeed = CalDistance(oldlat, oldlng) * 180;
                try {
                    personJson.put("type", "setLocation");
                    personJson.put("username", myID);
                    personJson.put("lat", lat);
                    personJson.put("lng", lng);
                    personJson.put("speed", mySpeed);
                    Log.d("My Location", "Lat : " + lat + " Lng : " + lng);
                    Sender = personJson.toString();
                    mTCPClient = new TCPClient(new TCPClient.OnMessageReceived() {
                        public void messageReceived(String message) {
                        }
                    });
                    mTCPClient.start();
                    mTCPClient.setTcpUdater(me, Sender);
                    Log.d("보낸다 문도", Sender);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(finder!=null)
            {
                finder.flag=false;
                finder = null;
            }
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
    }
}
