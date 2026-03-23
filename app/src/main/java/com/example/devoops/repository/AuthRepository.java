package com.example.devoops.repository;

import android.app.Activity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;

import java.util.concurrent.TimeUnit;

public class AuthRepository {

    private FirebaseAuth auth;

    public AuthRepository(){
    }
    //For testing
    public AuthRepository(FirebaseAuth auth) {
        this.auth = auth;
    }
    private FirebaseAuth getAuth() {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

    public interface AuthCallback{
        void onSuccess(String uid);
        void onError(String error);
    }

    //EMAIL
    public void signupEmail(String email, String password, AuthCallback cb) {
        auth = getAuth();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cb.onSuccess(auth.getCurrentUser().getUid());
                    } else {
                        cb.onError(task.getException().getMessage());
                    }
                });
    }

    public void loginEmail(String email, String password, AuthCallback cb) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cb.onSuccess(auth.getCurrentUser().getUid());
                    } else {
                        cb.onError(task.getException().getMessage());
                    }
                });
    }
}
