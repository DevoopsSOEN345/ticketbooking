package com.example.devoops.repository;

import android.util.Log;

import com.example.devoops.models.User;
import com.example.devoops.models.UserRole;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserRepository {
    private DatabaseReference db;

    // This Constructor is for Testing to do dependency injection
    public UserRepository(DatabaseReference mockedDb) {
        this.db = mockedDb;
    }
    public UserRepository() {
        try {
            this.db = FirebaseDatabase.getInstance().getReference();
        } catch (Exception e) {
            // this is for Unit Tests
        }
    }

    public void createUser(String uid, String email, String phone, String name) {
        DatabaseReference userRef = db.child("users").child(uid);

        User user = new User(uid, name, email, phone, UserRole.CUSTOMER);

        userRef.setValue(user)
                .addOnSuccessListener(aVoid -> Log.d("RTDB", "User saved!"))
                .addOnFailureListener(e -> Log.e("RTDB", "Save failed", e));
    }
}
