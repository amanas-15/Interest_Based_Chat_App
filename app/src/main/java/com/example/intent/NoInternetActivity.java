package com.example.intent;



import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class NoInternetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);

        Button retryButton = findViewById(R.id.button_retry);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected()) {
                    // If internet connection is available, close this activity
                    finish();
                    // Start all activities
                    startAllActivities(v.getContext());
                } else {
                    Toast.makeText(NoInternetActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Method to check internet connectivity
    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    // Method to start all activities
    private void startAllActivities(Context context) {
        // Implement logic to start all activities of your application here
        // For example:
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    // Method to start this activity if internet is not detected
    public static void start(Context context) {
        Intent intent = new Intent(context, NoInternetActivity.class);
        context.startActivity(intent);
    }
}
