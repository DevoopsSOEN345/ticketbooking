package com.example.devoops.presentation;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.devoops.R;
import com.example.devoops.models.User;
import com.example.devoops.models.UserRole;

public class LoginActivity extends AppCompatActivity {
    private UserViewModel viewModel;
    private EditText etEmail, etPassword, etPhone, etOtp;
    private Button btnLoginEmail, btnSendOtp, btnVerifyOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(UserViewModel.class);
        initViews();

        viewModel.getLoggedInUser().observe(this, user -> {
            if (user != null) {
                Log.d("NAV_DEBUG", "User role: " + user.getRole());
                navigate(user);
            }
        });

        // 1. Email Login
        btnLoginEmail.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String pass = etPassword.getText().toString();
            Log.d("BUTTON_CLICK", "Login button was pressed!");
            viewModel.loginEmail(email, pass);
        });

        // 2. Phone Step 1: Send OTP
        btnSendOtp.setOnClickListener(v -> {
            String phone = etPhone.getText().toString();
            //viewModel.sendOTP(phone, this);
        });

        // 3. Phone Step 2: Verify OTP
        btnVerifyOtp.setOnClickListener(v -> {
            String code = etOtp.getText().toString();
            String phone = etPhone.getText().toString();
            //viewModel.verifyOTP(code, phone);
        });

        // 4. Observe Status Messages
        viewModel.status.observe(this, s -> {
            Toast.makeText(this, s, Toast.LENGTH_SHORT).show();

            if (s.equals("OTP sent")) {
                etOtp.setVisibility(View.VISIBLE);
                btnVerifyOtp.setVisibility(View.VISIBLE);
            }
        });
    }
    private void navigate(User user) {
        Intent intent;

        if (user.getRole() == UserRole.ADMIN) {
            intent = new Intent(this, AdminMainActivity.class);
        } else {
            intent = new Intent(this, CustomerMainActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        etOtp = findViewById(R.id.etOtp);
        btnLoginEmail = findViewById(R.id.btnLoginEmail);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
    }
}