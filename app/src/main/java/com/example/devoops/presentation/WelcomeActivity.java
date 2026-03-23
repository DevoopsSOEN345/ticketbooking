package com.example.devoops.presentation;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.devoops.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Go to Sign Up Screen
        findViewById(R.id.btnGoToSignup).setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        // Go to Login Screen
        findViewById(R.id.btnGoToLogin).setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
