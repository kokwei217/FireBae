package com.example.kokwei217.firebae;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class ItemActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    // Wifi from phone
    WifiManager wifiManager;
    List<ScanResult> scanResults;
    Runnable runnable;
    Handler handler;
    ArrayList<String> bssidList;
    ArrayList<Integer> rssiList;

    TextView data_TV;
    int u = 0;
    int j = 0;
    int count = 0;

    //Wifi Data from database
    StringBuilder sb;
    ArrayList<String> listOfRoom;
    ArrayList<WifiFingerprints> wifiFingerprints;
    HashMap<String, Integer> wifiHashMap;
    ArrayList<HashMap<String, Integer>> arrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        data_TV = findViewById(R.id.data_tv);

        listOfRoom = new ArrayList<>();
        wifiFingerprints = new ArrayList<>();
        sb = new StringBuilder();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        initializeWifi();
        myRef.addChildEventListener(childEventListener);
    }

    public void initializeWifi() {
        handler = new Handler();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        scanWifi();
        runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 4000);
                scanWifi();
            }
        };
        handler.post(runnable);
    }

    public void scanWifi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
                Toast.makeText(this, "Enabling Wifi", Toast.LENGTH_SHORT).show();
            } else {
                wifiManager.startScan();
                scanResults = wifiManager.getScanResults();
                rssiList = new ArrayList<>();
                bssidList = new ArrayList<>();
                for (int i = 0; i < scanResults.size(); i++) {
                    rssiList.add(scanResults.get(i).level);
                    bssidList.add(scanResults.get(i).BSSID);
                }
            }
        }
    }

    public void signOut(View view) {
//        auth.signOut();
//        finish();
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
        wifiHashMap = new HashMap<>();
        wifiHashMap.put("house3", 10);
        wifiHashMap.put("mcd", 40);
        sb = new StringBuilder();
        Iterator iterator = wifiHashMap.entrySet().iterator();

        for (HashMap.Entry entry : wifiHashMap.entrySet()) {
            sb.append(entry.getKey() + ":" + entry.getValue() + "\n");
            data_TV.setText(sb);
        }
//        while (iterator.hasNext()){
//            HashMap.Entry entry = (HashMap.Entry)iterator.next();
//            sb.append(entry.getKey() + ":" + entry.getValue() +"\n");
//            data_TV.setText(sb);
//        }
    }

    public void listLocation(View view) {
        StringBuilder roomSB = new StringBuilder();
        for (String room : listOfRoom) {
            roomSB.append(room + "\n");
        }
        data_TV.setText(roomSB);
    }

    public void readDatabase(View view) {
        listOfRoom = new ArrayList<>();
        wifiFingerprints = new ArrayList<>();
        scanWifi();
//        Query query = database.getReference("kokwei-1").child("F3B09-5");
        myRef.addChildEventListener(childEventListener);
        sb = new StringBuilder();
        u = 0;
        j = 0;
        count = 0;
    }

    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String s) {
            if (snapshot.exists()) {
                try {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        listOfRoom.add(dataSnapshot.getKey());
                        dataSnapshot.getRef().addListenerForSingleValueEvent(valueEventListener);
                        j++;
                    }
                } catch (Exception e) {
                    Toast.makeText(ItemActivity.this, "error in", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                Log.e("TAG", " non existent");
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (final DataSnapshot dataSnapshot : snapshot.getChildren()) {
                //At the building level
                int correctPair = 0;
                String tag = String.valueOf(dataSnapshot.child("tag").getValue());
                sb.append(tag + "\n");

                //Iterates down the array off local Mac Address list
                for(int i =0; i < rssiList.size(); i++) {
                    int rssiLocal = rssiList.get(i);
                    String mac = bssidList.get(i);

                    //If local mac address matches with database
                    if (dataSnapshot.child("dims").child(mac).exists()) {
                        String rssiRemote = String.valueOf(dataSnapshot.child("dims").child(mac).getValue());
                        int rssiDiff = Math.abs(Integer.parseInt(rssiRemote) - rssiLocal);
                        if (rssiDiff <= 10){
                            correctPair++;
                            sb.append(mac + ": " +  rssiDiff + ",   " +"\n");
                            data_TV.setText(sb);
                        }
                    }
                    else if (rssiLocal >-80 && !dataSnapshot.child("dims").child(mac).exists()){
                        int rssiDiff = Math.abs(rssiLocal);
                        sb.append(mac + ":  " + rssiDiff + " doest not exist here " + "\n");
                    }
                }
                sb.append (String.valueOf(correctPair) + "\n\n");
                data_TV.setText(sb);
//                    dataSnapshot.child("dims").getRef().addListenerForSingleValueEvent(new ValueEventListener() {
//
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dimsData) {
//                            for (int i = 0; i < rssiList.size(); i++) {
//                                for (DataSnapshot dims : dimsData.getChildren()) {
//
//                                    String bssid = dims.getKey();
//                                    String rssi = String.valueOf(dims.getValue());
//                                    String tag = String.valueOf(dataSnapshot.child("tag").getValue());
//                                    WifiFingerprints fingerprints = new WifiFingerprints(bssid, rssi);
//                                    wifiFingerprints.add(fingerprints);
//
//                                    if (bssidList.get(i).equals(wifiFingerprints.get(u).bssid)) {
//                                        int rssiValue = Integer.parseInt(wifiFingerprints.get(u).rssi);
//                                        int rssiDiff = rssiValue - rssiList.get(i);
//                                        if (rssiList.get(i) >= -80 && (rssiDiff >= -10 && rssiDiff <= 10)) {
//                                            count++;
//                                            sb.append(bssidList.get(i) + ", " + rssiDiff + ", " + tag + "\n");
//                                        } else if (rssiList.get(i) < -80 && (rssiDiff >= -10 && rssiDiff <= 10)) {
//                                            count++;
//                                            sb.append(bssidList.get(i) + ", " + rssiDiff + ", " + tag + ", Weak Signal" + "\n");
//                                        }
//                                    }
//                                    u++;
//                                }
//                            }
//                            data_TV.setText(sb + String.valueOf(count));
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                        }
//                    });
            }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
}