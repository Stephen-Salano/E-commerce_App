package com.example.e_commerce;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.e_commerce.Model.Users;
import com.example.e_commerce.Prevalent.Prevalent;
import com.rey.material.widget.CheckBox;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {

    private EditText inputPhoneNumber, inputPassword;
    private Button loginButton;
    private ProgressDialog loadingBar;
    private String parentDbName = "Users";
    private CheckBox checkBoxRemeberMe;
    private TextView adminLink, notAdminLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        loginButton = findViewById(R.id.login_btn);
        inputPassword = findViewById(R.id.login_password_input);
        inputPhoneNumber = findViewById(R.id.login_phoneNumber_input);
        loadingBar = new ProgressDialog(this);
        // admin links
        adminLink = findViewById(R.id.admin_panel_link);
        notAdminLink = findViewById(R.id.regular_link);
        // Using the Paper library
        checkBoxRemeberMe = findViewById(R.id.rememberMeChk);
        /*
         * Initialize  paper DB / android memory db
         */
        Paper.init(this);

        loginButton.setOnClickListener(v -> loginUser());

        // listening to button click of admin
        adminLink.setOnClickListener(v -> {
            // step 1: change text of the login button when admin is clicked
            loginButton.setText("Login as Admin");
            // step2: Change visibility of the admin Link
            adminLink.setVisibility(View.INVISIBLE);
            // step 3: set visibility of not admin / reguler user link to appear
            notAdminLink.setVisibility(View.VISIBLE);
            // using a different database reference name
            parentDbName = "Admins";
        });
        notAdminLink.setOnClickListener(v -> {
            loginButton.setText("Login");
            notAdminLink.setVisibility(View.INVISIBLE);
            adminLink.setVisibility(View.VISIBLE);
            parentDbName = "Users";
        });
    }

    private void loginUser() {
        String phoneNumber = inputPhoneNumber.getText().toString();
        String password = inputPassword.getText().toString();

        if(TextUtils.isEmpty(phoneNumber)){
            Toast.makeText(this, "Please enter your phone number ", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Logging you into your Account");
            loadingBar.setMessage("Please wait while we're checking the credentials");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            AllowAccessToAccount(phoneNumber, password);
        }
    }

    private void AllowAccessToAccount(String phoneNumber, String password) {
        // before accessing account chek if the user logged in previously
        if (checkBoxRemeberMe.isChecked()){
            // write the phonenumber to android phone mem
            Paper.book().write(Prevalent.userPhoneKey, phoneNumber);
            // write password to android phone memory
            Paper.book().write(Prevalent.userPasswordKey, password);
        }

        final DatabaseReference rootRef;
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(parentDbName).child(phoneNumber).exists()){
                    Users usersData = snapshot.child(parentDbName).child(phoneNumber).getValue(Users.class);
                    if ((usersData != null) && usersData.getPhone().equals(phoneNumber)) {
                        if (usersData.getPassword().equals(password)) {
                            /*
                             checking the name of the database
                             if db == admin -> take to admin panel
                             if db == user -> take to user / HomeActivity
                             */

                            if (parentDbName.equals("Admins")) {
                                Toast.makeText(LoginActivity.this, "Welcome admin", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                // send the user to the admin activity
                                Intent intent = new Intent(LoginActivity.this, AdminCategoryActivity.class);
                                startActivity(intent);
                            } else if (parentDbName.equals("Users")) {
                                Toast.makeText(LoginActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                // send user to HomeActivity
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                Prevalent.currentOnlineUser = usersData;
                                startActivity(intent);
                            }
                        } else {
                            loadingBar.dismiss();
                            Toast.makeText(LoginActivity.this, "Password is incorrect", Toast.LENGTH_SHORT).show();
                        }
                    }
                }else {
                    Toast.makeText(LoginActivity.this, "Account with this phone number does not exist", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}