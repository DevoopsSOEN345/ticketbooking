package com.example.devoops.presentation;

import android.app.Activity;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.devoops.repository.AuthRepository;
import com.example.devoops.repository.UserRepository;

public class UserViewModel extends ViewModel {

    private AuthRepository authRepo;
    private UserRepository userRepo;
    public MutableLiveData<String> status = new MutableLiveData<>();

    public String verificationId;

    public UserViewModel(AuthRepository authRepo, UserRepository userRepo) {
        this.authRepo = authRepo;
        this.userRepo = userRepo;
    }

    // EMAIL SIGNUP
    public void signupEmail(String email, String password, String name) {
        if (email.isEmpty() || password.length() <= 5 || name.isEmpty()) {
            status.setValue("Valid email and 6+ char password required");
            return; // Path 1
        }

        authRepo.signupEmail(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(String uid) {
                userRepo.createUser(uid, email, null, name);
                status.setValue("Signup success");
            }

            @Override
            public void onError(String error) {
                status.setValue(error);
            }
        });
    }

    // EMAIL LOGIN
    public void loginEmail(String email, String password) {
        authRepo.loginEmail(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(String uid) {
                status.setValue("Login success");
            }

            @Override
            public void onError(String error) {
                status.setValue(error);
            }
        });
    }
}
