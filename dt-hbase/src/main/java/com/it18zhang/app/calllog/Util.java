package com.it18zhang.app.calllog;

import java.text.DecimalFormat;

public class Util {
    public static String getRegionNo(String callId, String callTime, int regionNo) {
        int hash = (callId + callTime.substring(0, 6)).hashCode();
        hash = (hash & Integer.MAX_VALUE) % regionNo;
        DecimalFormat df = new DecimalFormat();
        df.applyPattern("00");
        return df.format(hash);
    }
}
