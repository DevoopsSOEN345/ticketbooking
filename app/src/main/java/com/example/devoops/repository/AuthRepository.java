package com.example.devoops.repository;

import android.app.Activity;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.devoops.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class AuthRepository {

    private FirebaseAuth auth;

    public AuthRepository(){
        auth = FirebaseAuth.getInstance();
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
        auth = getAuth();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cb.onSuccess(auth.getCurrentUser().getUid());
                        Log.d("USER_good", "User Good?");

                    } else {
                        cb.onError(task.getException().getMessage());
                    }
                });
    }
    public LiveData<User> getUserById(String uid) {
        MutableLiveData<User> liveData = new MutableLiveData<>();
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        db.child("users").child(uid).get()
                .addOnSuccessListener(snapshot -> {
                    User user = snapshot.getValue(User.class);
                    liveData.setValue(user);
                    Log.d("USER_FETCH", "Data found for: " + uid);
                })
                .addOnFailureListener(e -> {
                    Log.e("USER_FETCH", "Database error: " + e.getMessage());
                    liveData.setValue(null);
                });

        return liveData;
    }
}
