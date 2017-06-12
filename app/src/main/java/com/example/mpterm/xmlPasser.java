package com.example.mpterm;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by 남지니 on 2016-05-28.
 */
public class xmlPasser extends Thread {

    String Station;
    SubWay mySubway;
    xmlPasser(String Station , SubWay subs)
    {
        this.Station = Station;
        mySubway = subs;
      }
    public void run() {
        StringBuilder sBuffer = new StringBuilder("http://swopenAPI.seoul.go.kr/api/subway/4c43754342736b6137306d536d7870/xml/stationWifi/0/100/");
        try {
            sBuffer.append(URLEncoder.encode(Station, "UTF-8"));
            URL url = new URL(sBuffer.toString());
            // XmlPullParser 생성
            XmlPullParserFactory xpf = XmlPullParserFactory.newInstance();
            XmlPullParser xp = xpf.newPullParser();
            // XML 파일 읽어오기
            xp.setInput(url.openStream(), "UTF-8");
            int parserEvent = xp.getEventType();
            String tag = null;
            int i=0;
            // XML 문서가 끝날때 까지 반복 작업합니다.
            while (parserEvent != XmlPullParser.END_DOCUMENT) {
                boolean flag = false;
                switch (parserEvent) {
                    // 시작태그들을 만났을 때 진행합니다.
                    case XmlPullParser.START_TAG:
                        tag = xp.getName();
                        // 태그명이 "PeopleVersion" 일 때
                        break;
                    // 종료태그들을 만났을 때 진행합니다.
                    case XmlPullParser.END_TAG:
                        tag = xp.getName();
                        // 종료태그 후에도 TEXT 이벤트가 발생하니 tag에 null 대입해서 중복 실행을 방지합니다.
                        tag = null;
                        break;
                    // 시작, 종료 태그 사이의 text를 만났을 때 진행합니다.
                    case XmlPullParser.TEXT:
                        if (tag.startsWith("mac")) {
                            String text = xp.getText();
                            Log.d(tag, text+"\n"+i);
                            mySubway.setMAC(text,i);
                            Log.d("잘저장 됌",mySubway.MAC[i]);
                            i++;
                        }
                        if (tag.startsWith("가천대")) {
                            String text = xp.getText();
                            Log.d(tag, text);
                        }
                        break;
                }
                parserEvent = xp.next();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

    }
}
