package com.example.devoops.repositoryTest;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.devoops.models.User;
import com.example.devoops.repository.UserRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DatabaseReference;
import android.util.Log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {
    private UserRepository userRepository;
    @Mock private DatabaseReference mockDb;
    @Mock private DatabaseReference mockUsersChild;
    @Mock private DatabaseReference mockUidChild;
    @Mock private Task<Void> mockTask;

    @BeforeEach
    void setup() {
        lenient().when(mockDb.child("users")).thenReturn(mockUsersChild);
        lenient().when(mockUsersChild.child(anyString())).thenReturn(mockUidChild);
        lenient().when(mockUidChild.setValue(any())).thenReturn(mockTask);
        lenient().when(mockTask.addOnSuccessListener(any())).thenReturn(mockTask);
        lenient().when(mockTask.addOnFailureListener(any())).thenReturn(mockTask);

        userRepository = new UserRepository(mockDb);
    }

    @Test
    void createUser_Path1_Success() {
        try (MockedStatic<Log> mockedLog = mockStatic(Log.class)) {
            userRepository.createUser("123", "test@me.com", "555", "Jonathan");

            @SuppressWarnings("unchecked")
            ArgumentCaptor<OnSuccessListener<Void>> captor = ArgumentCaptor.forClass(OnSuccessListener.class);
            verify(mockTask).addOnSuccessListener(captor.capture());

            captor.getValue().onSuccess(null);

            mockedLog.verify(() -> Log.d(eq("RTDB"), eq("User saved!")));
            verify(mockUsersChild).child("123");
            verify(mockUidChild).setValue(any(User.class));
        }
    }

    @Test
    void createUser_Path2_Failure() {
        try (MockedStatic<Log> mockedLog = mockStatic(Log.class)) {
            userRepository.createUser("123", "test@me.com", "555", "Jon");

            ArgumentCaptor<OnFailureListener> captor = ArgumentCaptor.forClass(OnFailureListener.class);
            verify(mockTask).addOnFailureListener(captor.capture());

            Exception testException = new Exception("Database Error");
            captor.getValue().onFailure(testException);

            mockedLog.verify(() -> Log.e(eq("RTDB"), anyString(), eq(testException)));
        }
    }
}