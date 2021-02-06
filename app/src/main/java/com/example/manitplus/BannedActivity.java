package com.example.manitplus;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class BannedActivity extends AppCompatActivity {

    TextView textView;
    public static String message = "SORRY... YOU HAVE BEEN BANNED FROM THE SERVER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banned);

        textView = findViewById(R.id.textView12);
        textView.setText(message);

    }
}
