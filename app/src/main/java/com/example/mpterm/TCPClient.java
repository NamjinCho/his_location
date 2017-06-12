package com.example.mpterm;

import android.os.Message;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by hong on 2016-05-02.
 */
public class TCPClient extends Thread {

    public static final String SERVERIP = "52.78.18.196"; //your computer IP address 52.78.18.196
    public static final int SERVERPORT = 9000;
    public MessageReciver mR = new MessageReciver();
    JSONObject personJson = new JSONObject();
    BufferedReader in;
    String Sender;
    int flag = -1; // Login = 1 , Main = 2 , His = 3 , Child = 4 , Join = 5 , Updater = 6 , SubwayFinder = 7
    private String serverMessage;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    private Login tcpLogin;
    private Join tcpJoin;
    private enrolledChild tcpEnrollChild;
    private MainActivity tcpMainActivity;
    private hislocation tcpHisLocation;
    private ChildMain tcpChildMain;
    private PrintWriter out;
    private LocationUdater tcpUdater;
    private SubwayFinder tcpFinder;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(String message) {
        if (out != null && !out.checkError()) {
            out.println(message);
            Log.d("보내요" + flag, message);
            out.flush();
        }
    }


    public void stopClient() {
        mRun = false;
    }

    public void run() {

        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);

            Log.e("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, SERVERPORT);
            SocketHandler.setSocket(socket);

            try {

                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(SocketHandler.getSocket().getOutputStream())), true);

                Log.e("TCP Client", "C: Sent.");

                Log.e("TCP Client", "C: Done.");

                //receive the message which the server sends back
                in = new BufferedReader(new InputStreamReader(SocketHandler.getSocket().getInputStream()));

                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    serverMessage = in.readLine();
                    Log.e("100", serverMessage);
                    if (serverMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(serverMessage);
                        mR.messageReceived(serverMessage);
                        if (!serverMessage.startsWith("0-0")) break;
                    }
                    serverMessage = null;

                }
                socket.close();
                Log.e("RESPONSE FROM SERVER " + flag, "S: Received Message: '" + serverMessage + "'");


            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                //socket.close();
            }

        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);

        }

    }

    public void setTcpLogin(Login og, String s) {
        tcpLogin = og;
        Sender = s;
        flag = 1;
    }

    public void setTcpJoin(Join og, String s) {
        tcpJoin = og;
        Sender = s;
        flag = 5;
    }

    public void setTcpEnrollChild(enrolledChild og) {
        tcpEnrollChild = og;
    }

    public void setTcpMainActivity(MainActivity og, String s) {
        tcpMainActivity = og;
        Sender = s;
        flag = 2;
    }

    public void setTcpHisLocation(hislocation og, String s) {
        tcpHisLocation = og;
        Sender = s;
        flag = 3;
    }

    public void setTcpChildMain(ChildMain childMain, String s) {
        tcpChildMain = childMain;
        Sender = s;
        flag = 4;
    }

    public void setTcpUdater(LocationUdater og, String s) {
        tcpUdater = og;
        Sender = s;
        flag = 6;
    }

    public void setSubwayFinder(SubwayFinder subwayFinder, String ss) {
        tcpFinder = subwayFinder;
        Sender = ss;
        flag = 7;
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

    public class MessageReciver {


        public void messageReceived(String message) {
            if (flag == 1) {
                if (message.startsWith("0-0")) {
                    sendMessage(Sender);
                }
                if (message.startsWith("1-1")) {
                    Login.checker = 1;
                    Message msg;
                    msg = tcpLogin.mHandler.obtainMessage(1, message);
                    tcpLogin.mHandler.sendMessage(msg);
                } else if (message.startsWith("1-2")) {
                    Login.checker = 0;
                    Message msg;
                    msg = tcpLogin.mHandler.obtainMessage(1, message);
                    tcpLogin.mHandler.sendMessage(msg);
                } else if (message.startsWith("2-1") || message.startsWith("2-2")) {
                    Message msg;
                    msg = tcpLogin.mHandler.obtainMessage(1, message);
                    tcpLogin.mHandler.sendMessage(msg);
                }

            }
            if (flag == 5) {

                if (message.startsWith("0-0")) {
                    // tcpLogin.RequestSubway();
                    sendMessage(Sender);
                }
                if (message.startsWith("2-1") || message.startsWith("2-2")) {
                    Message msg = tcpJoin.mHandler.obtainMessage(1, message);
                    tcpJoin.mHandler.sendMessage(msg);
                }
            }
            if (flag == 2) {
                if (message.startsWith("0-0")) {
                    Log.d("1차", "1차시도");
                    sendMessage(Sender);
                    Log.d("2차", "2차시도" + Sender);
                }
                if (message.startsWith("3-1"))//child 리스트 받아오기
                {
                    Log.d("3차", "3차시도");
                    tcpMainActivity.getChildList(message.replaceFirst("3-1/", ""));
                    Log.d("4차", "4차시도");
                }
            }
            if (flag == 3) {
                if (message.startsWith("0-0")) sendMessage(Sender);
                if (message.startsWith("4-1")) {
                    Message msg = tcpHisLocation.mHandler.obtainMessage(1, message);
                    tcpHisLocation.mHandler.sendMessage(msg);
                }
                if (message.startsWith("6-1")) {
                    Message msg = tcpHisLocation.mHandler.obtainMessage(1, message);
                    tcpHisLocation.mHandler.sendMessage(msg);
                }
                if (message.startsWith("7-1")) {
                    Message msg = tcpHisLocation.mHandler.obtainMessage(1, message);
                    tcpHisLocation.mHandler.sendMessage(msg);
                }
            }
            if (flag == 4) {
                if (message.startsWith("0-0"))
                    sendMessage(Sender);
                if (message.startsWith("5-1")) {
                    String msg = message;
                    Message msg2 = tcpChildMain.mHandler.obtainMessage(1, msg);
                    tcpChildMain.mHandler.sendMessage(msg2);
                }
                if (message.startsWith("5-2")) {
                    String msg = message;
                    Message msg2 = tcpChildMain.mHandler.obtainMessage(1, msg);
                    tcpChildMain.mHandler.sendMessage(msg2);
                }
            }
            if (flag == 6) {
                if (message.startsWith("0-0"))
                    sendMessage(Sender);
                if (message.startsWith("5-1")) {
                    tcpUdater.setSafeLatLng(message);
                }
            }
            if (flag == 7) {
                if (message.startsWith("0-0"))
                    sendMessage(Sender);
                if (message.startsWith("0-1"))
                    tcpFinder.getFinder(message.replaceFirst("0-1/", ""));
            }
        }
    }
}
