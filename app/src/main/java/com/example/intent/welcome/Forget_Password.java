package com.example.intent.welcome;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.intent.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class Forget_Password extends AppCompatActivity {

    private EditText Forgetemail;
    private Button ResetButton;
    private String email;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        auth = FirebaseAuth.getInstance();
        Forgetemail= findViewById(R.id.forget_email);
        ResetButton= findViewById(R.id.buttonResetPassword);

        ResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validatedata();
            }
        });
    }

    private void validatedata() {
        email = Forgetemail.getText().toString();
        if(email.isEmpty()){
            Forgetemail.setError("Required");
        }
        else {
            forgetpass();
        }
    }

    private void forgetpass() {
        auth.sendPasswordResetEmail(email).
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(Forget_Password.this, "Check Your Email Inbox", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Forget_Password.this,Login.class));
                            finish();
                        }else {
                            Toast.makeText(Forget_Password.this, "Error  : "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}