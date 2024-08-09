package com.example.intent.welcome;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.intent.MainActivity;
import com.example.intent.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity {

    private EditText userEmail, userPassword, userConfirmPassword;
    private Button createAccountButton;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    private TextView signInButton;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        userEmail = findViewById(R.id.register_email);
        userPassword = findViewById(R.id.register_password);
        userConfirmPassword = findViewById(R.id.register_confirm_password);
        createAccountButton = findViewById(R.id.register_create_account_button);
        signInButton = findViewById(R.id.register_signin);
        progressBar = findViewById(R.id.progressBar);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password, confirmPassword;
                email = userEmail.getText().toString().trim().toLowerCase();
                password = userPassword.getText().toString().trim();
                confirmPassword = userConfirmPassword.getText().toString().trim();

                // Check email format
                if (!isValidEmailFormat(email)) {
                    Toast.makeText(Register.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                // Check password constraints
                if (!isValidPassword(password)) {
                    Toast.makeText(Register.this, "Password should start with an uppercase letter, have at least 6 characters, and contain at least one special symbol", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                // Check if passwords match
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(Register.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
                                    startActivity(intent);
                                    finish();
                                    Toast.makeText(Register.this, "Authentication Successful.", Toast.LENGTH_SHORT).show();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(Register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

            private boolean isValidEmailFormat(String email) {
                String emailRegex = "^[a-zA-Z][a-zA-Z0-9._]*@[a-zA-Z0-9]+(\\.[a-zA-Z]{2,})+$";
                return email.matches(emailRegex);
            }

            private boolean isValidPassword(String password) {
                String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{6,}$";
                return password.matches(passwordRegex);
            }
        });


    }
    }