package com.example.devoops.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.util.Log;

import com.example.devoops.models.User;
import com.example.devoops.models.UserRole;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private DatabaseReference rootReference;
    @Mock
    private DatabaseReference usersReference;
    @Mock
    private DatabaseReference userReference;
    @Mock
    private Task<Void> writeTask;

    private UserRepository repository;

    @BeforeEach
    void setUp() {
        lenient().when(rootReference.child("users")).thenReturn(usersReference);
        lenient().when(usersReference.child(anyString())).thenReturn(userReference);
        lenient().when(userReference.setValue(any())).thenReturn(writeTask);
        lenient().when(writeTask.addOnSuccessListener(any())).thenReturn(writeTask);
        lenient().when(writeTask.addOnFailureListener(any())).thenReturn(writeTask);
        repository = new UserRepository(rootReference);
    }

    @Test
    void givenCreateUserRequest_whenWriteSucceeds_thenLogsSuccess_primePath1() {
        // Given
        String uid = "user-1";

        // When
        try (MockedStatic<Log> logMock = mockStatic(Log.class)) {
            repository.createUser(uid, "u1@example.com", "111", "User One");
            ArgumentCaptor<OnSuccessListener<Void>> successCaptor = ArgumentCaptor.forClass(OnSuccessListener.class);
            verify(writeTask).addOnSuccessListener(successCaptor.capture());
            successCaptor.getValue().onSuccess(null);

            // Then
            verify(usersReference).child(uid);
            verify(userReference).setValue(any(User.class));
            logMock.verify(() -> Log.d(eq("RTDB"), eq("User saved!")));
        }
    }

    @Test
    void givenCreateUserRequest_whenWriteFails_thenLogsFailure_primePath2() {
        // Given
        Exception exception = new Exception("Database failure");

        // When
        try (MockedStatic<Log> logMock = mockStatic(Log.class)) {
            repository.createUser("user-2", "u2@example.com", "222", "User Two");
            ArgumentCaptor<OnFailureListener> failureCaptor = ArgumentCaptor.forClass(OnFailureListener.class);
            verify(writeTask).addOnFailureListener(failureCaptor.capture());
            failureCaptor.getValue().onFailure(exception);

            // Then
            logMock.verify(() -> Log.e(eq("RTDB"), eq("Save failed"), eq(exception)));
        }
    }

    @Test
    void givenCreateUserRequest_whenPersistingUser_thenStoredModelMatchesInput() {
        // Given
        String uid = "user-3";
        String email = "u3@example.com";
        String phone = "333";
        String name = "User Three";

        // When
        repository.createUser(uid, email, phone, name);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userReference).setValue(userCaptor.capture());

        // Then
        User captured = userCaptor.getValue();
        assertNotNull(captured);
        assertEquals(uid, captured.getUserId());
        assertEquals(email, captured.getEmail());
        assertEquals(phone, captured.getPhoneNumber());
        assertEquals(name, captured.getName());
        assertEquals(UserRole.CUSTOMER, captured.getRole());
    }

    @Test
    void givenMultipleCreateUserRequests_whenPersisting_thenWritesEveryUser() {
        // Given / When
        repository.createUser("u1", "a@test.com", "111", "A");
        repository.createUser("u2", "b@test.com", "222", "B");

        // Then
        verify(usersReference, times(2)).child(anyString());
        verify(userReference, times(2)).setValue(any(User.class));
    }

    @Test
    void givenDefaultConstructor_whenCreated_thenRepositoryInstanceIsNotNull() {
        // Given / When
        UserRepository defaultRepository = new UserRepository();

        // Then
        assertNotNull(defaultRepository);
    }

    @Test
    void givenFirebaseDatabaseAvailable_whenDefaultConstructorCalled_thenInitializesReference_primePath5() {
        // Given
        FirebaseDatabase firebaseDatabase = org.mockito.Mockito.mock(FirebaseDatabase.class);
        when(firebaseDatabase.getReference()).thenReturn(rootReference);

        // When
        try (MockedStatic<FirebaseDatabase> databaseStatic = mockStatic(FirebaseDatabase.class)) {
            databaseStatic.when(FirebaseDatabase::getInstance).thenReturn(firebaseDatabase);
            UserRepository defaultRepository = new UserRepository();

            // Then
            assertNotNull(defaultRepository);
            databaseStatic.verify(FirebaseDatabase::getInstance);
            verify(firebaseDatabase).getReference();
        }
    }

    @Test
    void givenFirebaseDatabaseThrows_whenDefaultConstructorCalled_thenConstructorHandlesException_primePath6() {
        // When
        try (MockedStatic<FirebaseDatabase> databaseStatic = mockStatic(FirebaseDatabase.class)) {
            databaseStatic.when(FirebaseDatabase::getInstance).thenThrow(new RuntimeException("boom"));
            UserRepository defaultRepository = new UserRepository();

            // Then
            assertNotNull(defaultRepository);
            databaseStatic.verify(FirebaseDatabase::getInstance);
        }
    }
}


