package com.example.mpterm;

import java.net.Socket;

/**
 * Created by hong on 2016-05-02.
 */
public class SocketHandler {
    private static Socket socket;

    public static synchronized Socket getSocket(){
        return socket;
    }

    public static synchronized void setSocket(Socket socket){
        SocketHandler.socket = socket;
    }
}
