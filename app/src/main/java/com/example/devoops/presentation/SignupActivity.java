package com.example.devoops.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.devoops.R;

public class SignupActivity extends AppCompatActivity {

    private UserViewModel viewModel;
    private EditText etEmail, etPassword, etPhone, etOtp, etName;
    private Button btnEmailSignup, btnSendOtp, btnVerifyOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        viewModel = new ViewModelProvider(this).get(UserViewModel.class);
        initViews();

        btnEmailSignup.setOnClickListener(v -> {
            viewModel.signupEmail(
                    etEmail.getText().toString().trim(),
                    etPassword.getText().toString().trim(),
                    etName.getText().toString().trim()
            );
        });


        viewModel.status.observe(this, statusMsg -> {
            Toast.makeText(this, statusMsg, Toast.LENGTH_SHORT).show();


            if (statusMsg.contains("success")) {
                // Navigate to the main part of your app
                // startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });
    }

    private void initViews() {
        etEmail = findViewById(R.id.etSignupEmail);
        etName = findViewById(R.id.etName);
        etPassword = findViewById(R.id.etSignupPassword);
        etOtp = findViewById(R.id.etSignupOTP);
        btnEmailSignup = findViewById(R.id.btnSignupEmail);
        btnVerifyOtp = findViewById(R.id.btnVerifySignupOTP);
    }
}