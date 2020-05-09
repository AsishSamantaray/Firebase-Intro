package com.asish.firebaseintro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;

    private ProgressDialog progressDialog;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Message");

        mAuth = FirebaseAuth.getInstance();

        // Store Data in Firebase
        databaseReference.setValue("Hello World!!");

        // Retrieve data from firebase..
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String data = dataSnapshot.getValue(String.class);
                Toast.makeText(MainActivity.this, data, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        authStateListener = mAuth -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                Log.d(TAG, "User Logged in..");
                Log.d(TAG, "User Email: " + user.getEmail());
                Toast.makeText(MainActivity.this, "User Email: " + user.getEmail(), Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "User Log out..");
            }
        };

        btnLogin.setOnClickListener((View view) -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(MainActivity.this, "Email is Required..", Toast.LENGTH_LONG).show();
                etEmail.requestFocus();
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(MainActivity.this, "Valid Email is Required..", Toast.LENGTH_LONG).show();
                etEmail.requestFocus();
            }
            if (password.isEmpty() || password.length() < 6) {
                Toast.makeText(MainActivity.this, "6 Character password is Required..", Toast.LENGTH_LONG).show();
                etPassword.requestFocus();
            }

            login(email, password);

        });

    }

    private void login(String email, String password) {
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity.this, task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Login Successful..", Toast.LENGTH_LONG).show();
                    } else {
                        if (task.getException() != null) {
                            Toast.makeText(MainActivity.this, "Login Failed.." + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }
}
