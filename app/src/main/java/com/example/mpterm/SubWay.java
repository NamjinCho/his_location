package com.example.mpterm;

import java.io.Serializable;

/**
 * Created by 남지니 on 2016-05-27.
 */
public class SubWay implements Serializable {
    public String name="";
    public String line = "";
    public String []MAC = new String [100];
    public double Lat=0.0;
    public double Lng=0.0;
    public double Dist = 0.0;
    public int MacLen =-1;

    SubWay()
    {
        name = "";
        line = "";
    }
    public void setName(String msg)
    {
        name = msg;
    }
    public void setLine(String msg)
    {
        line = msg;
    }
    public void setMAC(String mac , int order)
    {
        MAC[order] = mac;
        MacLen = order;
    }
}

