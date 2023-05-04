package com.example.todolistapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.Window;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button b = findViewById(R.id.button);
        b.setOnClickListener((v) -> {
            Intent i = new Intent(MainActivity.this, SignUp.class);
            Transition fadeTransition = new Fade();
            fadeTransition.setDuration(2000);
            getWindow().setEnterTransition(fadeTransition);
            startActivity(i, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        });
    }
}