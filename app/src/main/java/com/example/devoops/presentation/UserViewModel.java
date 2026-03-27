package com.example.devoops.presentation;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.devoops.models.User;
import com.example.devoops.models.UserRole;
import com.example.devoops.repository.AuthRepository;
import com.example.devoops.repository.UserRepository;

public class UserViewModel extends ViewModel {

    private AuthRepository authRepo;
    private UserRepository userRepo;
    public MutableLiveData<String> status = new MutableLiveData<>();
    private MutableLiveData<User> loggedInUser = new MutableLiveData<>();

    public String verificationId;
    /*public UserViewModel() {
         // Initialization logic
    }*/



    //Default constructor for production
    public UserViewModel() {
        this.authRepo = new AuthRepository();
        this.userRepo = new UserRepository();
    }

    //Constructor for unit tests — inject mocks to avoid Firebase calls
    public UserViewModel(AuthRepository authRepo, UserRepository userRepo) {
        this.authRepo = authRepo;
        this.userRepo = userRepo;
    }

    // EMAIL SIGNUP
    public void signupEmail(String email, String password, String name) {

        if (email.isEmpty() || password.length() <= 5 || name.isEmpty()) {
            status.setValue("Valid email and 6+ char password required");
            return;
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

    public MutableLiveData<User> getLoggedInUser() {
        return loggedInUser;
    }
    // EMAIL LOGIN
    public void loginEmail(String email, String password) {
        authRepo.loginEmail(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(String uid) {

                status.setValue("Login success");
                authRepo.getUserById(uid).observeForever(user -> {
                    if (user != null) {
                        loggedInUser.setValue(user);
                    } else {
                        Log.d("VMODEL_DEBUG", "Auth succeeded but no DB record for UID: " + uid);
                    }
                });
            }

            @Override
            public void onError(String error) {
                status.setValue(error);
            }
        });
    }

}
