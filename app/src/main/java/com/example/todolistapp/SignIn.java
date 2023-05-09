package com.example.todolistapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class SignIn extends AppCompatActivity {
    FirebaseAuth auth;
    EditText emailEt, passwordEt;
    String email, password;
    TextView signUpBtn;
    Button b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        signUpBtn = findViewById(R.id.sign_up_button);
        signUpBtn.setOnClickListener((v) -> {
            Intent i = new Intent(SignIn.this, SignUp.class);
            startActivity(i);
        });

        b = findViewById(R.id.getStartedSignInBtn);
        emailEt = findViewById(R.id.email);
        passwordEt = findViewById(R.id.password);

        b.setOnClickListener((v) -> {
            email = emailEt.getText().toString();
            password = passwordEt.getText().toString();

            auth = FirebaseAuth.getInstance();

            // authenticate with email and password
            if (email.length() == 0 || password.length() == 0) {
                if (email.length() == 0) emailEt.setError("Email cannot be empty");
                if (password.length() == 0) passwordEt.setError("Password cannot be empty");
            } else {
                auth.signInWithEmailAndPassword(email.trim(), password.trim()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Intent i = new Intent(SignIn.this, Dashboard.class);
                        startActivity(i);
                    } else {
                        Toast.makeText(SignIn.this, "Error: " + task.getException(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}