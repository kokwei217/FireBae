package com.example.kokwei217.firebae;

import android.Manifest;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    TextView data_TV, location_TV;
    int u = 0;
    int j = 0;
    int count = 0;
    Boolean scanFlag = true;

    //Wifi Data from database
    StringBuilder sb;
    ArrayList<String> listOfRoom;
    ArrayList<WifiFingerprints> wifiFingerprints;
    HashMap<String, Integer> wifiHashMap;
    HashMap<String, Double> hashmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        data_TV = findViewById(R.id.data_tv);
        location_TV = findViewById(R.id.location_tv);

        listOfRoom = new ArrayList<>();
        wifiFingerprints = new ArrayList<>();
        sb = new StringBuilder();
        hashmap = new HashMap<>();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        initializeWifi();
        myRef.addChildEventListener(childEventListener);
    }

    public void initializeWifi() {
        if (scanFlag) {
            handler = new Handler();
            wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            scanWifi();
            runnable = new Runnable() {
                @Override
                public void run() {
                    handler.postDelayed(this, 4000);
                    scanWifi();
//                    readData();
                }
            };
            handler.post(runnable);
        } else {
            handler.removeCallbacks(runnable);
        }
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

    public void checkLocation(View view) {
//        auth.signOut();
//        finish();
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
        whereAmI();
    }

    public void whereAmI() {
        Map.Entry<String, Double> min = null;
        for (Map.Entry<String, Double> entry : hashmap.entrySet()) {
            if (min == null || min.getValue() > entry.getValue()) {
                min = entry;
            }
        }
        location_TV.setText(min.getKey());
    }

    public void flag(View view) {
//        StringBuilder roomSB = new StringBuilder();
//        for (String room : listOfRoom) {
//            roomSB.append(room + "\n");
//        }
//        data_TV.setText(roomSB);
        scanFlag = !scanFlag;
        Toast.makeText(this, "State Changed", Toast.LENGTH_SHORT).show();
        initializeWifi();

    }

    public void readDatabase(View view) {
        readData();
    }

    public void readData() {
        hashmap = new HashMap<>();
        listOfRoom = new ArrayList<>();
        wifiFingerprints = new ArrayList<>();
        scanWifi();
//        Query query = database.getReference("kokwei-1").child("F3B09-5");
        myRef.addChildEventListener(childEventListener);
        sb = new StringBuilder();
        u = 0;

    }

    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String s) {
            if (snapshot.exists()) {
                try {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        listOfRoom.add(dataSnapshot.getKey());
                        dataSnapshot.getRef().addListenerForSingleValueEvent(valueEventListener);
                    }
                } catch (Exception e) {
                    Toast.makeText(ItemActivity.this, "error", Toast.LENGTH_SHORT).show();
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
            //for each snapshot in the main building level
            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                double score = 0;
                int correctPair = 0;
                int count = 0;
                String tag = String.valueOf(dataSnapshot.child("tag").getValue());
                sb.append(tag + "\n");


                // for each snapshot of building, get each snapshot of dims
                for (DataSnapshot dims : dataSnapshot.child("dims").getChildren()) {
                    String macDatabase = dims.getKey();
                    int rssiRemote = Integer.parseInt(String.valueOf(dims.getValue()));
                    if (rssiRemote > -80 && !bssidList.contains(macDatabase)) {
                        count++;
                        int rssiDiff = Math.abs(rssiRemote);
                        score = (score + rssiDiff);
                        sb.append(macDatabase + ":  " + rssiDiff + " not in local " + "\n");

                    }
                }

                //Iterates down the array off local Mac Address list
                for (int i = 0; i < rssiList.size(); i++) {
                    int rssiLocal = rssiList.get(i);
                    String mac = bssidList.get(i);

                    //If local mac address matches with database, find difference in rssi
                    if (dataSnapshot.child("dims").child(mac).exists()) {
                        String rssiRemote = String.valueOf(dataSnapshot.child("dims").child(mac).getValue());
                        int rssiDiff = Math.abs(Integer.parseInt(rssiRemote) - rssiLocal);
                        correctPair++;
                        count++;
                        score = (score + rssiDiff);
                        sb.append(mac + ": " + rssiDiff + ",   " + "\n");
                        data_TV.setText(sb);

                    } else if (!dataSnapshot.child("dims").child(mac).exists()) {
                        int rssiDiff;
                        if (rssiLocal <=-90){
                            rssiDiff = (Math.abs(rssiLocal) - 40);
                        } else if (rssiLocal <= -80 ){
                            rssiDiff = (Math.abs(rssiLocal) - 20);
                        }
                        else{
                            rssiDiff = Math.abs(rssiLocal);
                        }
                        score = (score + rssiDiff);
                        count++;
                        sb.append(mac + ":  " + rssiDiff + " not in database " + "\n");
                    }
                }
                hashmap.put(tag, score);
                sb.append(String.valueOf(correctPair) + ",  " + score + "\n\n");
                data_TV.setText(sb);
            }



            if (hashmap.size() == listOfRoom.size()) {
                whereAmI();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
}