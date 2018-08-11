package com.example.kokwei217.firebae;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;


@IgnoreExtraProperties
public class User {
    public String tag, timestamp, x, y, z;


    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

}
