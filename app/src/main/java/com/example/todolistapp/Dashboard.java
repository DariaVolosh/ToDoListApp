package com.example.todolistapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class Dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // get user from previous activity
    FirebaseUser user;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    Map<String, Boolean> tasks = new HashMap<>();

    TextView welcomeMessage;
    ImageView addPhoto;
    CircleImageView main_photo, navigation_photo;
    LinearLayout l;
    private DrawerLayout drawerLayout;
    TextView navigation_name;

    // launcher to launch ACTION_PICK intent to pick an image from the gallery
    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // getting Uri of selected photo
                        Uri imageURI = result.getData() != null ? result.getData().getData() : null;

                        // getting reference to the layout
                        l = findViewById(R.id.photo_and_welcome_message);

                        // getting reference to file location in the firebase storage
                        StorageReference fileRef = storageReference.child("users/" + user.getUid()+ "/profile.jpg");

                        Dialog popup = new Dialog(Dashboard.this);
                        popup.setContentView(R.layout.image_uploading_dialog_window);
                        popup.show();

                        // putting image to the location
                        //on success listener
                        if (imageURI != null) {
                            fileRef.putFile(imageURI).addOnSuccessListener(taskSnapshot -> {

                                //toast to inform that the image was uploaded
                                Toast.makeText(Dashboard.this, "Image uploaded", Toast.LENGTH_LONG).show();
                                l.removeView(addPhoto);
                                main_photo.setVisibility(View.VISIBLE);

                                // upload main photo
                                main_photo.setImageURI(imageURI);
                                navigation_photo.setImageURI(imageURI);
                                popup.dismiss();


                                // on failure listener
                            }).addOnFailureListener(e -> {

                                // toast to inform that the image was not uploaded
                                Toast.makeText(Dashboard.this, "Image was not uploaded: " + e,
                                        Toast.LENGTH_LONG).show();
                                popup.dismiss();
                            });
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // reference to a welcome message view text
        welcomeMessage = findViewById(R.id.welcome_message);

        // get the name of the user
        String name = user.getDisplayName();

        // set the welcome message
        welcomeMessage.setText(this.getResources().getString(R.string.welcome_text, name));

        // setting image
        main_photo = findViewById(R.id.circleImageView);

        l = findViewById(R.id.photo_and_welcome_message);

        // reference to storage
        storageReference = FirebaseStorage.getInstance().getReference();


        //get a reference to a toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);

        //setting a toolbar
        setSupportActionBar(toolbar);

        // set navigationItemSelectedListener
        drawerLayout = findViewById(R.id.drawer);
        NavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);

        // creating a drawer toggle
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open_nav, R.string.close_nav);

        //add a drawer toggle
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // add photo and name to navigation view
        View header = navigationView.getHeaderView(0);
        navigation_photo = header.findViewById(R.id.main_photo_nav_header);
        navigation_name = header.findViewById(R.id.name_nav_header);

        navigation_name.setText(user.getDisplayName());
        StorageReference child = storageReference.child("users/" + user.getUid()+ "/profile.jpg");
        child.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    // upload image from storage
                    main_photo.setVisibility(View.VISIBLE);
                    Picasso.get().load(uri).into(main_photo);
                    l.removeView(addPhoto);

                    Picasso.get().load(uri).into(navigation_photo);

                });
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // reading database to check if tasks exist
        databaseReference.child(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                tasks = (Map<String, Boolean>) task.getResult().getValue();
                if (tasks == null) tasks = new HashMap<>();

                for (String key : tasks.keySet()) {
                    CheckBox userTask = addTask(key);
                    userTask.setChecked(Boolean.TRUE.equals(tasks.get(key)));
                }
            } else {
                Toast.makeText(Dashboard.this, "Failed to load tasks",Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.delete_tasks_button).setOnClickListener(v -> {
            LinearLayout l = findViewById(R.id.tasks_layout);
            Dialog popup = new Dialog(Dashboard.this);
            popup.setContentView(R.layout.delete_tasks_popup);
            popup.show();

            popup.findViewById(R.id.delete_done_tasks).setOnClickListener(v2 -> {
                popup.dismiss();
                for (int i = 0; i < l.getChildCount(); i++) {
                    View view = l.getChildAt(i);
                    if (view instanceof LinearLayout) continue;
                    if (((CheckBox) view).isChecked()) {
                        tasks.remove(((CheckBox) view).getText().toString());
                        l.removeView(view);
                        databaseReference.child(user.getUid()).setValue(tasks);
                        i--;
                    }
                }
            });

            popup.findViewById(R.id.delete_all_tasks).setOnClickListener(v2 -> {
                popup.dismiss();
                for (int i = 0; i < l.getChildCount(); i++) {
                    View view = l.getChildAt(i);
                    if (view instanceof LinearLayout) continue;
                    l.removeView(view);
                    i--;
                }

                tasks = new HashMap<>();
                databaseReference.child(user.getUid()).setValue(tasks);
            });
        });

       findViewById(R.id.add_task_button).setOnClickListener(v -> {
           Dialog popup = new Dialog(Dashboard.this);
           popup.setContentView(R.layout.add_task_popup);
           popup.show();

           EditText taskET = popup.findViewById(R.id.enter_task_edit_text);

           popup.findViewById(R.id.add_task_popup_button).setOnClickListener(v2 -> {
               if (taskET.getText().toString().length() != 0) {
                   CheckBox task = addTask(taskET.getText().toString());
                   tasks.put(task.getText().toString(), false);
                   databaseReference.child(user.getUid()).setValue(tasks);
               } else taskET.setError("Task cannot be empty");
           });
       });
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Dashboard.this, SignIn.class);
        startActivity(intent);
    }

    private CheckBox addTask(String value) {
        LinearLayout tasksLayout = findViewById(R.id.tasks_layout);
        AppCompatCheckBox task = new AppCompatCheckBox(Dashboard.this);
        task.setText(value);
        task.setTextSize(12);
        Typeface font = ResourcesCompat.getFont(Dashboard.this, R.font.poppins_regular);
        task.setTypeface(font);
        task.setAlpha(0.75f);
        task.setButtonTintList(ContextCompat.getColorStateList(Dashboard.this, R.color.azure));
        task.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tasks.put(value, isChecked);
            databaseReference.child(user.getUid()).setValue(tasks);
        });
        tasksLayout.addView(task);
        return task;
    }

    // this listener listens for a menu items pressing
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_photo:
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                launcher.launch(i);
                break;
            case R.id.change_name:
                Dialog popup = new Dialog(Dashboard.this);
                popup.setContentView(R.layout.change_name_popup);
                popup.show();

                Button changeButton = popup.findViewById(R.id.change_name_button);
                changeButton.setOnClickListener(v -> {
                    EditText changeNameET = popup.findViewById(R.id.change_name_edit_text);
                    String newName = changeNameET.getText().toString();

                    if (newName.length() == 0) changeNameET.setError("New name cannot be empty");
                    else if (newName.equals(user.getDisplayName())) changeNameET.setError("New name cannot be the same as the old one");
                    else {
                        UserProfileChangeRequest changes = new UserProfileChangeRequest.Builder()
                                .setDisplayName(newName).build();
                        user.updateProfile(changes).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                popup.dismiss();
                                welcomeMessage.setText(this.getResources().getString(R.string.welcome_text, user.getDisplayName()));
                                navigation_name.setText(user.getDisplayName());

                            } else {
                                changeNameET.setError(Objects.requireNonNull(task.getException()).toString());
                            }
                        });
                    }
                });
                break;

            case R.id.change_email:
                popup = new Dialog(Dashboard.this);
                popup.setContentView(R.layout.sign_in_again_popup);
                popup.show();

                Button authenticateButton = popup.findViewById(R.id.authenticate_button);
                authenticateButton.setOnClickListener(v -> {
                    EditText email = popup.findViewById(R.id.reauthenticate_email);
                    String emailString = email.getText().toString();

                    EditText password = popup.findViewById(R.id.reauthenticate_password);
                    String passwordString = password.getText().toString();

                    if (emailString.length() == 0 || passwordString.length() == 0) {
                        if (emailString.length() == 0) email.setError("Email cannot be empty");
                        if (passwordString.length() == 0) password.setError("Password cannot be empty");
                    } else {
                        AuthCredential authCredential = EmailAuthProvider.getCredential(emailString, passwordString);
                        user.reauthenticate(authCredential).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                popup.dismiss();
                                popup.setContentView(R.layout.change_email_popup);
                                popup.show();

                                Button changeEmailButton = popup.findViewById(R.id.change_email_button);

                                changeEmailButton.setOnClickListener(v2 -> {
                                    EditText changeEmailET = popup.findViewById(R.id.change_email_edit_text);
                                    String newEmail = changeEmailET.getText().toString();

                                    if (newEmail.length() == 0) changeEmailET.setError("New email cannot be empty");
                                    else if (newEmail.equals(user.getEmail())) changeEmailET.setError("New email cannot be the same as the old one");
                                    else {
                                        user.updateEmail(newEmail).addOnCompleteListener(task2 -> {
                                            if (task.isSuccessful()) {
                                                popup.dismiss();
                                                signOut();
                                            }
                                            else changeEmailET.setError(Objects.requireNonNull(task.getException()).toString());
                                        });
                                    }
                                });
                            } else {
                                password.setError(Objects.requireNonNull(task.getException()).toString());
                            }
                        });
                    }
                });
                break;
            case R.id.change_password:
                popup = new Dialog(Dashboard.this);
                popup.setContentView(R.layout.sign_in_again_popup);
                popup.show();

                authenticateButton = popup.findViewById(R.id.authenticate_button);
                authenticateButton.setOnClickListener(v -> {
                    EditText email = popup.findViewById(R.id.reauthenticate_email);
                    String emailString = email.getText().toString();

                    EditText password = popup.findViewById(R.id.reauthenticate_password);
                    String passwordString = password.getText().toString();

                    if (emailString.length() == 0 || passwordString.length() == 0) {
                        if (emailString.length() == 0) email.setError("Email cannot be empty");
                        if (passwordString.length() == 0) password.setError("Password cannot be empty");
                    } else {
                        AuthCredential authCredential = EmailAuthProvider.getCredential(emailString, passwordString);
                        user.reauthenticate(authCredential).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                popup.dismiss();
                                popup.setContentView(R.layout.change_password_popup);
                                popup.show();

                                Button changePasswordButton = popup.findViewById(R.id.change_password_button);

                                changePasswordButton.setOnClickListener(v2 -> {
                                    EditText changePasswordET = popup.findViewById(R.id.change_password_edit_text);
                                    String newPassword = changePasswordET.getText().toString();

                                    if (newPassword.length() == 0) changePasswordET.setError("New password cannot be empty");
                                    else {
                                        user.updatePassword(newPassword).addOnCompleteListener(task2 -> {
                                            if (task.isSuccessful()) {
                                                popup.dismiss();
                                                signOut();
                                            }
                                            else changePasswordET.setError(Objects.requireNonNull(task.getException()).toString());
                                        });
                                    }
                                });
                            } else {
                                password.setError(Objects.requireNonNull(task.getException()).toString());
                            }
                        });
                    }
                });
                break;
            case R.id.delete_account:
                popup = new Dialog(Dashboard.this);
                popup.setContentView(R.layout.sign_in_again_popup);
                popup.show();

                authenticateButton = popup.findViewById(R.id.authenticate_button);
                authenticateButton.setOnClickListener(v -> {
                    EditText email = popup.findViewById(R.id.reauthenticate_email);
                    String emailString = email.getText().toString();

                    EditText password = popup.findViewById(R.id.reauthenticate_password);
                    String passwordString = password.getText().toString();

                    if (emailString.length() == 0 || passwordString.length() == 0) {
                        if (emailString.length() == 0) email.setError("Email cannot be empty");
                        if (passwordString.length() == 0) password.setError("Password cannot be empty");
                    } else {
                        AuthCredential authCredential = EmailAuthProvider.getCredential(emailString, passwordString);
                        user.reauthenticate(authCredential).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                popup.dismiss();
                                popup.setContentView(R.layout.delete_account_popup);
                                popup.show();

                                Button deleteAccountButton = popup.findViewById(R.id.delete_account_button);
                                deleteAccountButton.setOnClickListener(v2 -> user.delete().addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        popup.dismiss();
                                        FirebaseAuth.getInstance().signOut();
                                        Intent intent = new Intent(Dashboard.this, SignIn.class);
                                        startActivity(intent);
                                    }
                                }));
                            } else {
                                password.setError(Objects.requireNonNull(task.getException()).toString());
                            }
                        });
                    }
                });
                break;
            case R.id.sign_out:
                signOut();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        Dialog signOut = new Dialog(this);
        signOut.setContentView(R.layout.sign_out_popup);
        signOut.show();

        signOut.findViewById(R.id.sign_out_button).setOnClickListener(v -> signOut());
    }
}