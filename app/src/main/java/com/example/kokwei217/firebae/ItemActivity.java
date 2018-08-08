package com.example.kokwei217.firebae;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
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

import java.util.ArrayList;

public class ItemActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private EditText remarks_ET;
    private TextView data_TV;
    private int u = 0;
    private int i = 0;
    private int j = 0;
    private static final String TAG = "TEST";
    private StringBuilder sb;
    private ArrayList<String> listOfRoom;
    private ArrayList<WifiFingerprints> wifiFingerprints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        remarks_ET = findViewById(R.id.remarks_item);
        data_TV = findViewById(R.id.data_tv);
        setSupportActionBar(toolbar);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        listOfRoom = new ArrayList<>();
        wifiFingerprints = new ArrayList<>();
        myRef = database.getReference();
        sb = new StringBuilder();
        myRef.addChildEventListener(roomListener);

    }

    public void signOut(View view) {
        auth.signOut();
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void writeDatabase(View view) {
//        myRef.setValue(remarks_ET.getText().toString());
//        String userID = myRef.push().getKey();
////        writeNewUser(userID, "Kok Wei", remarks_ET.getText().toString());
////        Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
        StringBuilder roomSB = new StringBuilder();

        for (String room : listOfRoom) {
            roomSB.append(room + "\n");
        }
        data_TV.setText(roomSB);

    }

    private void writeNewUser(String userId, String name, String email) {
        for (int i = 0; i < 5; i++) {
            myRef.child("F3").child(userId).child("Fingerprints").child(String.valueOf(i)).setValue(30);
        }
    }

    public void readDatabase(View view) {
        listOfRoom = new ArrayList<>();
        wifiFingerprints = new ArrayList<>();
//        Query query = database.getReference("kokwei-1").child("F3B09-5");
//        query.addChildEventListener(childEventListener);
        myRef.addChildEventListener(roomListener);
        sb = new StringBuilder();
        u = 0;
        j = 0;
        Toast.makeText(this, "Read Data", Toast.LENGTH_SHORT).show();
    }

    ChildEventListener roomListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String s) {
            if (snapshot.exists()) {
                try {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Log.e("TAG", "" + dataSnapshot.getKey());
                        listOfRoom.add(dataSnapshot.getKey());
                        if (dataSnapshot.getKey().equals(listOfRoom.get(j))) {
                            dataSnapshot.getRef().addChildEventListener(childEventListener);
                        }
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

    ChildEventListener dimsListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String s) {
            for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                if(dataSnapshot.getKey().equals("dims")){
                    dataSnapshot.getRef().addListenerForSingleValueEvent(fingerprintValueEventListener);
                }
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

    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String s) {
            if (snapshot.exists()) {
                try {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                        Log.e("TAG", "" + dataSnapshot.getValue() + "," + dataSnapshot.getKey());// your name values you will get here
                        if (dataSnapshot.getKey().equals("dims")) {
                            dataSnapshot.getRef().orderByValue().addListenerForSingleValueEvent(fingerprintValueEventListener);
                        }
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

    ValueEventListener fingerprintValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            try {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String bssid = dataSnapshot.getKey();
                    String rssi = String.valueOf(dataSnapshot.getValue());
                    WifiFingerprints fingerprints = new WifiFingerprints(bssid , rssi);
                    wifiFingerprints.add(fingerprints);
                    sb.append(wifiFingerprints.get(u).bssid +  wifiFingerprints.get(u).rssi  +"\n");
                    u++;
                }
                sb.append(u + "," + j);
                data_TV.setText(sb);


            } catch (Exception e) {
                Toast.makeText(ItemActivity.this, "error in", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(TAG, "Failed to read value.", databaseError.toException());
        }
    };
}
