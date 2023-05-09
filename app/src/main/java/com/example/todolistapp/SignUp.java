package com.example.todolistapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUp extends AppCompatActivity {
    EditText nameEt, emailEt, passwordEt, repeatPasswordEt;
    TextView signInBtn;
    private String name, email, password, repeatPassword;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // reference to signIn viewText;
        signInBtn = findViewById(R.id.sign_in_button);
        signInBtn.setOnClickListener(v -> {
            Intent i = new Intent(SignUp.this, SignIn.class);
            startActivity(i);
        });

        // creating transition
        Transition fadeTransition = new Fade();
        fadeTransition.setDuration(2000);
        getWindow().setEnterTransition(fadeTransition);

        // get info from edit texts
        nameEt = findViewById(R.id.name);
        emailEt = findViewById(R.id.email_sign_up);
        passwordEt = findViewById(R.id.password_sign_up);
        repeatPasswordEt = findViewById(R.id.repeat_password);

        // reference to get started button
        Button b = findViewById(R.id.getStartedSignUpBtn);

        // onclick listener
        b.setOnClickListener((v) -> {
            //get all necessary info
            name = nameEt.getText().toString();
            email = emailEt.getText().toString();
            password = passwordEt.getText().toString();
            repeatPassword = repeatPasswordEt.getText().toString();

           if (password.equals(repeatPassword) && name.length() != 0 && email.length() != 0 && password.length() != 0) {
               // create an FirebaseAuth instance
               auth = FirebaseAuth.getInstance();

               // create a user
               auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
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

                       if (user != null) {
                           user.updateProfile(profileUpdates).addOnCompleteListener(task1 -> {
                               if (task1.isSuccessful()) {

                                   //on success putExtras user to dashboard activity and start activity
                                   Toast.makeText(getApplicationContext(), "Name updated", Toast.LENGTH_LONG).show();
                                   startActivity(i);
                               }
                           });
                       }
                   } else {
                       Toast.makeText(
                               getApplicationContext(),
                               "Authentication failed " + task.getException(),
                               Toast.LENGTH_LONG
                       ).show();
                   }
               });
           } else {
               if (!password.equals(repeatPassword)) {
                   passwordEt.setError("Passwords are not equal");
                   repeatPasswordEt.setError("Passwords are not equal");
                   passwordEt.requestFocus();
               }
               if (name.length() == 0) nameEt.setError("Name cannot be empty");
               if (email.length() == 0) emailEt.setError("Email cannot be empty");
               if (password.length() == 0) passwordEt.setError("Password cannot be empty");
               if (repeatPassword.length() == 0) repeatPasswordEt.setError("Please, repeat your password");
           }
        });
    }
}