package com.example.manitplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.OneSignal;

public class SplashScreen extends AppCompatActivity {

    DatabaseReference root = FirebaseDatabase.getInstance().getReference();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        //OneSignal Initialization....

        OneSignal.initWithContext(this);
        OneSignal.setAppId(MainActivity.OneSignalAppId);


        loading = findViewById(R.id.progressBar);
        loading.setVisibility(View.INVISIBLE);

        Handler handler = new android.os.Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loading.setVisibility(View.VISIBLE);
            }
        }, 1000);


        if (user == null) {
            //Login Screen
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else
            {
            //Reload User
            user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (user == null) {
                        //Login Screen
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (user.isEmailVerified())
                        OpenApp();
                    else
                        Toast.makeText(getApplicationContext(), "Please Verify Your Email!!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void OpenApp() {

        /*
        Log.i("yoyo2",MainActivity.ScholarNo);
        Log.i("yoyo2",MainActivity.Name);
        Log.i("yoyo2",MainActivity.Branch);
        Log.i("yoyo2",MainActivity.Year);
        Log.i("yoyo2",MainActivity.Section);
        Log.i("yoyo2",MainActivity.Hostel);
        Log.i("yoyo2",MainActivity.PlayerId);
        */


        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();

    }
}