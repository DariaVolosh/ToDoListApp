package com.example.todolistapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUp extends AppCompatActivity {
    EditText nameEt, emailEt, passwordEt;
    private String name, email, password;
    private Button b;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Transition fadeTransition = new Fade();
        fadeTransition.setDuration(2000);
        getWindow().setEnterTransition(fadeTransition);

        nameEt = findViewById(R.id.name);
        emailEt = findViewById(R.id.email);
        passwordEt = findViewById(R.id.password);

        b = findViewById(R.id.button);

        b.setOnClickListener((v) -> {
            //get all necessary info
            name = nameEt.getText().toString();
            email = emailEt.getText().toString();
            password = passwordEt.getText().toString();

            // create an FirebaseAuth instance
            auth = FirebaseAuth.getInstance();

            // create a user
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this,
                    task -> {
                        // on success show a message and proceed
                        if (task.isSuccessful()) {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "You were signed in successfully!",
                                    Toast.LENGTH_LONG
                            ).show();

                            // create an intent to dashboard activity
                            Intent i = new Intent(SignUp.this, Dashboard.class);

                            // getting current user
                            FirebaseUser user = auth.getCurrentUser();

                            //update user's name
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name).build();

                            user.updateProfile(profileUpdates).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {

                                    //on success putExtras user to dashboard activity and start activity
                                    Toast.makeText(getApplicationContext(), "Name updated", Toast.LENGTH_LONG).show();
                                    startActivity(i);
                                }
                            });
                        } else {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Authentication failed " + task.getException(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });
    }
}