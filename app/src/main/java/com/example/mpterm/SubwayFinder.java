package com.example.mpterm;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 남지니 on 2016-05-28.
 */
public class SubwayFinder extends Thread {

    SubWay[] mySubs;
    int myLength;
    double Lng, Lat;
    boolean flag;
    public int index =-1;
    TCPClient mTcpClient;
    String sender;
    JSONObject personJson;

    SubwayFinder(int length, double lat, double lng) {
        myLength = length;
        Lat = lat;
        Lng = lng;
        flag = true;
        index = -1;
        mySubs = new SubWay[520];
        personJson = new JSONObject();
        try {
            personJson.put("type","start");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sender = personJson.toString();
        mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
            public void messageReceived(String message) {
            }
        });
        mTcpClient.start();
        mTcpClient.setSubwayFinder(this,sender);
    }



    public void run() {
        while (flag) {
            try {
                double min = 100000000;
                for (int i = 0; i < myLength; i++) {
                    mySubs[i].Dist = CalDistance(Lat, Lng, mySubs[i].Lat, mySubs[i].Lng);
                    if (mySubs[i].Dist <= min) {
                        min = mySubs[i].Dist;
                        index = i;
                        WIFIScanner.index = i;
                    }
                }
                sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setLatLng(double lat, double lng) {
        Lat = lat;
        Lng = lng;
    }

    public double CalDistance(double lat1, double lng1, double lat2, double lng2) {
        double lat_do1, lat_do2, lat_do, lng_do1, lng_do2, lng_do;
        double lat_min1, lat_min2, lat_min, lng_min1, lng_min2, lng_min;
        double lat_sec1, lat_sec2, lat_sec, lng_sec, lng_sec2, lng_sec1;

        lat_do1 = Math.floor(lat1);
        lat_do2 = Math.floor(lat2);
        lng_do1 = Math.floor(lng1);
        lng_do2 = Math.floor(lng2);
        lat_min1 = Math.floor((lat1 - lat_do1) * 60);
        lat_min2 = Math.floor((lat2 - lat_do2) * 60);
        lng_min1 = Math.floor((lng1 - lng_do1) * 60);
        lng_min2 = Math.floor((lng2 - lng_do2) * 60);
        lat_sec1 = Math.floor((((lat1 - lat_do1) * 60) - lat_min1) * 60);
        lat_sec2 = Math.floor((((lat2 - lat_do2) * 60) - lat_min2) * 60);
        lng_sec1 = Math.floor((((lng1 - lng_do1) * 60) - lng_min1) * 60);
        lng_sec2 = Math.floor((((lng2 - lng_do2) * 60) - lng_min2) * 60);
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

    public void sendMassage(String sender) {
        mTcpClient.sendMessage(sender);
    }

    public void getFinder(String msg) {
        String [] message;
        message = msg.split("/");
        for(int i=0;i <message.length;i=i+4)
        {
            // Log.d("asd", message[i]);
            mySubs[i/4] = new SubWay();
            mySubs[i/4].setName(message[i]);
            mySubs[i/4].setLine(message[i + 1]);
            mySubs[i/4].Lat = Double.parseDouble(message[i+2]);
            mySubs[i/4].Lng = Double.parseDouble(message[i+3]);
        }
        this.start();
    }
}
