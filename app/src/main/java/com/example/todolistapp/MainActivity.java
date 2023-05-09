package com.example.todolistapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.view.Window;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    Button b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check if user was authenticated during the last session and restore the state
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent i = new Intent(this, Dashboard.class);
            startActivity(i);
        }

        b = findViewById(R.id.button);
        // set onClickListener on get started button to open sign up page
        b.setOnClickListener((v) -> {
            Intent i = new Intent(MainActivity.this, SignUp.class);

            // create transition
            Transition fadeTransition = new Fade();

            // duration
            fadeTransition.setDuration(2000);

            // setting transition
            getWindow().setEnterTransition(fadeTransition);

            // start sign up activity
            startActivity(i, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        });
    }
}