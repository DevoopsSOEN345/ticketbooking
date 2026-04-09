package com.example.devoops.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.devoops.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AuthRepository {

    private FirebaseAuth auth;

    public AuthRepository(){
        auth = FirebaseAuth.getInstance();
    }

    // For testing
    public AuthRepository(FirebaseAuth auth) {
        this.auth = auth;
    }

    private FirebaseAuth getAuth() {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

    public interface AuthCallback {
        void onSuccess(String uid);
        void onError(String error);
    }

    // EMAIL
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
                        safeLog("USER_good", "User Good?");
                    } else {
                        cb.onError(task.getException().getMessage());
                    }
                });
    }

    // LOGOUT
    public void signOut() {
        getAuth().signOut();
    }

    private void safeLog(String tag, String msg) {
        try {
            Log.d(tag, msg);
        } catch (Throwable ignored) {
            // no-op for JVM unit tests
        }
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
