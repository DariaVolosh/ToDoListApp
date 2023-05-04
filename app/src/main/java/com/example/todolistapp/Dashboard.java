package com.example.todolistapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

public class Dashboard extends AppCompatActivity {
    // get user from previous activity
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    TextView welcomeMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // reference to a welcome message view text
        welcomeMessage = findViewById(R.id.welcome_message);

        // get the name of the user
        String name = user.getDisplayName();

        // set the welcome message
        welcomeMessage.setText("Welcome, " + name);
    }
}