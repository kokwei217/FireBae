package com.example.kokwei217.firebae;

public class WifiFingerprints {
    //    public String x, y, z;
//    public String timestamp, tag;
    public String bssid, rssi;

    public WifiFingerprints(){

    }

    public WifiFingerprints(String bssid, String rssi){
        this.bssid = bssid;
        this.rssi = rssi;
    }


}
