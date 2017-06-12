package com.example.mpterm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

/**
 * Created by 남지니 on 2016-05-29.
 */
public class WIFIScanner {

    private static final String TAG = "WIFIScanner";

    // WifiManager variable

    WifiManager wifimanager;
    private int scanCount = 0;

    String text = "";

    String result = "";
    boolean indoor = false;
    Context context;
    private List<ScanResult> mScanResult; // ScanResult List
    int length;
    SubWay[] mySubs;
    static int index = -1;

    WIFIScanner(Context c, int length, SubWay... subWays) {
        context = c;
        wifimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        this.length = length;
        mySubs = new SubWay[520];
        mySubs = subWays.clone();
        //initWIFIScan(); // start WIFIScan
    }

//첫번째 브로드 캐스트 리시버이다. 이것을 통해 사용하도록한다.

//wifimanager라는 객체는 startScan()이라는 것을 갖고있어서 이것을 이용하면 ,

//스캔을 시작하고, 그 결과를 getWIFIScanResult를 이용하여 글씨로 뿌려준다.

//테스트를 위해 2개의 리시버를 준비하였다.

    public BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override

        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {

                getWIFIScanResult(); // get WIFISCanResult
                wifimanager.startScan(); // for refresh

            } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {

                context.sendBroadcast(new Intent("wifi.ON_NETWORK_STATE_CHANGED"));
            }
        }
    };

    //스캔한뒤에 그결과를 getScanResult()라는 함수로 받아온다.
    public void getWIFIScanResult() {
        mScanResult = wifimanager.getScanResults(); // ScanResult
        // Scan count
        boolean rV = false;
        double dist = 0.0;
        //Log.d("wifi 스캔중","ㅋㅋ");
        for (int i = 0; i < mScanResult.size(); i++) {
            ScanResult result = mScanResult.get(i);
            //이부분이 가장중요하다. SSID에서 AP이름을 확인하고
            //level은 신호세기를 나타낸다. 이것을 이용하여 사용할 것이다.
            int frq = result.frequency;
            //result.BSSID;
            dist = calculateDistance((double) result.level, (double) result.frequency);
            int failer = 1;
            if (index != -1) {
                for (int s = 0; s <= mySubs[index].MacLen; s++) {
                    if (mySubs[index].MAC[s].equals(result.BSSID.toUpperCase()) && (dist < 100)) {
                        indoor = true;
                        failer = 0;
                        // Log.d("asd",indoor+"'");
                    }
                    // Log.d("wifi 스캔중",Login.subWays[ChildMain.Index].MAC[s]);
                }
            }
        }
    }

    public double calculateDistance(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }


//원리는 같다.

//스캔을 시작하며 리시버를 등록하여 준다.

    public void initWIFIScan() {

        // init WIFISCAN
        if (wifimanager.isWifiEnabled() == false)
            wifimanager.setWifiEnabled(true);

        scanCount = 0;

        text = "";

        final IntentFilter filter = new IntentFilter(

                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        context.registerReceiver(mReceiver, filter);

        wifimanager.startScan();

        Log.d(TAG, "initWIFIScan()");

    }


    /**
     * Called when the activity is first created.
     */


}