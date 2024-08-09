package com.example.intent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.intent.welcome.Login;
import com.example.intent.welcome.Register;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Thread thread = new Thread(){

            public void run(){
                try {
                    sleep( 3000);

                }
                catch (Exception e){
                    e.fillInStackTrace();

                }
                finally {
                    Intent intent = new Intent(Splash.this , Login.class);
                    startActivity(intent);
                    finish();

                }

            }
        };thread.start();
    }
}