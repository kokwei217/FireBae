package com.example.kokwei217.firebae;

import android.content.ClipData;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "EmailPassword";

    private TextView email_ET;
    private TextView password_ET;

    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email_ET = findViewById(R.id.email_login);
        password_ET = findViewById(R.id.password_login);
        email_ET.setText("kokwei217@hotmail.com");
        password_ET.setText("xopalx217");

        findViewById(R.id.btn_login).setOnClickListener(this);

        auth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(this, ItemActivity.class);
//            String username = currentUser.getEmail();
//            Toast.makeText(this, username, Toast.LENGTH_SHORT).show();
            startActivity(intent);
            finish();
        }

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_login:
                signIn(email_ET.getText().toString() , password_ET.getText().toString());
                break;
            default:
                break;
        }

    }

    private void signIn(String email , String password){
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = auth.getCurrentUser();
//                            updateUI(user);
                            Intent intent = new Intent(MainActivity.this, ItemActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                        // ...
                    }
                });

    }
}
