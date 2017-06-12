package com.example.mpterm;

/**
 * Created by 남지니 on 2016-06-18.
 */
public class LineMap {
    public static String getLine(String key) {
        if (key.equals("1")) {
            return "1호선";
        } else if (key.equals("2")) {
            return "2호선";
        } else if (key.equals("3")) {
            return "3호선";
        } else if (key.equals("4")) {
            return "4호선";
        } else if (key.equals("5")) {
            return "5호선";
        } else if (key.equals("6")) {
            return "6호선";
        } else if (key.equals("7")) {
            return "7호선";
        } else if (key.equals("8")) {
            return "8호선";
        } else if (key.equals("9")) {
            return "9호선";
        } else if (key.equals("A")) {
            return "공항철도";
        } else if (key.equals("SU")) {
            return "수인선";
        } else if (key.equals("B")) {
            return "분당선";
        } else if (key.equals("K")) {
            return "경의 중앙선";
        } else if (key.equals("S")) {
            return "신분당선";
        } else if (key.equals("G")) {
            return "경춘선";
        } else {
            return "인천 1호선";
        }
    }
}
