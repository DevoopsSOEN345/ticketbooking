package com.example.devoops.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.arch.core.executor.TaskExecutor;
import com.example.devoops.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthRepositoryTest {

    @Mock
    private FirebaseAuth firebaseAuth;
    @Mock
    private FirebaseUser firebaseUser;
    @Mock
    private Task<AuthResult> authTask;
    @Mock
    private AuthRepository.AuthCallback callback;

    private AuthRepository repository;

    @BeforeEach
    void setUp() {
        repository = new AuthRepository(firebaseAuth);
    }

    @Test
    void givenSignupRequest_whenFirebaseSucceeds_thenInvokesSuccessCallback_primePath1() {
        // Given
        String email = "test@example.com";
        String password = "password123";
        String uid = "uid-123";
        when(firebaseAuth.createUserWithEmailAndPassword(email, password)).thenReturn(authTask);
        when(authTask.isSuccessful()).thenReturn(true);
        when(firebaseAuth.getCurrentUser()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn(uid);

        // When
        repository.signupEmail(email, password, callback);
        ArgumentCaptor<OnCompleteListener<AuthResult>> listenerCaptor = ArgumentCaptor.forClass(OnCompleteListener.class);
        verify(authTask).addOnCompleteListener(listenerCaptor.capture());
        listenerCaptor.getValue().onComplete(authTask);

        // Then
        verify(callback).onSuccess(uid);
        verify(callback, never()).onError(anyString());
    }

    @Test
    void givenSignupRequest_whenFirebaseFails_thenInvokesErrorCallback_primePath2() {
        // Given
        String errorMessage = "Email already exists";
        when(firebaseAuth.createUserWithEmailAndPassword(anyString(), anyString())).thenReturn(authTask);
        when(authTask.isSuccessful()).thenReturn(false);
        when(authTask.getException()).thenReturn(new Exception(errorMessage));

        // When
        repository.signupEmail("test@example.com", "password123", callback);
        ArgumentCaptor<OnCompleteListener<AuthResult>> listenerCaptor = ArgumentCaptor.forClass(OnCompleteListener.class);
        verify(authTask).addOnCompleteListener(listenerCaptor.capture());
        listenerCaptor.getValue().onComplete(authTask);

        // Then
        verify(callback).onError(errorMessage);
        verify(callback, never()).onSuccess(anyString());
    }

    @Test
    void givenLoginRequest_whenFirebaseSucceeds_thenInvokesSuccessCallback_primePath3() {
        // Given
        String email = "test@example.com";
        String password = "password123";
        String uid = "uid-456";
        when(firebaseAuth.signInWithEmailAndPassword(email, password)).thenReturn(authTask);
        when(authTask.isSuccessful()).thenReturn(true);
        when(firebaseAuth.getCurrentUser()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn(uid);

        // When
        repository.loginEmail(email, password, callback);
        ArgumentCaptor<OnCompleteListener<AuthResult>> listenerCaptor = ArgumentCaptor.forClass(OnCompleteListener.class);
        verify(authTask).addOnCompleteListener(listenerCaptor.capture());
        listenerCaptor.getValue().onComplete(authTask);

        // Then
        verify(callback).onSuccess(uid);
        verify(callback, never()).onError(anyString());
    }

    @Test
    void givenLoginRequest_whenFirebaseFails_thenInvokesErrorCallback_primePath4() {
        // Given
        String errorMessage = "Invalid credentials";
        when(firebaseAuth.signInWithEmailAndPassword(anyString(), anyString())).thenReturn(authTask);
        when(authTask.isSuccessful()).thenReturn(false);
        when(authTask.getException()).thenReturn(new Exception(errorMessage));

        // When
        repository.loginEmail("bad@example.com", "wrong", callback);
        ArgumentCaptor<OnCompleteListener<AuthResult>> listenerCaptor = ArgumentCaptor.forClass(OnCompleteListener.class);
        verify(authTask).addOnCompleteListener(listenerCaptor.capture());
        listenerCaptor.getValue().onComplete(authTask);

        // Then
        verify(callback).onError(errorMessage);
        verify(callback, never()).onSuccess(anyString());
    }

    @Test
    void givenDefaultConstructor_whenCreated_thenRepositoryInstanceIsNotNull() {
        try (MockedStatic<FirebaseAuth> firebaseAuthStatic = mockStatic(FirebaseAuth.class)) {
            firebaseAuthStatic.when(FirebaseAuth::getInstance).thenReturn(firebaseAuth);

            AuthRepository defaultRepository = new AuthRepository();

            assertNotNull(defaultRepository);
            firebaseAuthStatic.verify(FirebaseAuth::getInstance);
        }
    }

    @Test
    void givenDefaultRepository_whenSignupCalled_thenInitializesFirebaseAuthViaGetInstance_primePath5() {
        try (MockedStatic<FirebaseAuth> firebaseAuthStatic = mockStatic(FirebaseAuth.class)) {
            firebaseAuthStatic.when(FirebaseAuth::getInstance).thenReturn(firebaseAuth);

            AuthRepository defaultRepository = new AuthRepository();

            String uid = "uid-lazy-init";
            when(firebaseAuth.createUserWithEmailAndPassword("lazy@test.com", "password123"))
                    .thenReturn(authTask);
            when(authTask.isSuccessful()).thenReturn(true);
            when(firebaseAuth.getCurrentUser()).thenReturn(firebaseUser);
            when(firebaseUser.getUid()).thenReturn(uid);

            defaultRepository.signupEmail("lazy@test.com", "password123", callback);
            ArgumentCaptor<OnCompleteListener<AuthResult>> listenerCaptor =
                    ArgumentCaptor.forClass(OnCompleteListener.class);
            verify(authTask).addOnCompleteListener(listenerCaptor.capture());
            listenerCaptor.getValue().onComplete(authTask);

            firebaseAuthStatic.verify(FirebaseAuth::getInstance);
            verify(callback).onSuccess(uid);
        }
    }

    @Test
    void givenLoginSuccess_whenSafeLogCalled_thenNoExceptionPropagated() {
    // safeLog is called inside loginEmail on success — Log.d will throw
    // in a JVM test environment, exercising the catch branch
    String email = "log@example.com";
    String password = "pass123";
    String uid = "uid-log";
    when(firebaseAuth.signInWithEmailAndPassword(email, password)).thenReturn(authTask);
    when(authTask.isSuccessful()).thenReturn(true);
    when(firebaseAuth.getCurrentUser()).thenReturn(firebaseUser);
    when(firebaseUser.getUid()).thenReturn(uid);

    repository.loginEmail(email, password, callback);
    ArgumentCaptor<OnCompleteListener<AuthResult>> captor =
            ArgumentCaptor.forClass(OnCompleteListener.class);
    verify(authTask).addOnCompleteListener(captor.capture());
    captor.getValue().onComplete(authTask);

    // safeLog ran (and swallowed the Log.d RuntimeException) — login still succeeds
    verify(callback).onSuccess(uid);
    }



    @Test
void givenValidUid_whenGetUserByIdSucceeds_thenLiveDataContainsUser() {
    ArchTaskExecutor.getInstance().setDelegate(new TaskExecutor() {
        @Override public void executeOnDiskIO(Runnable r) { r.run(); }
        @Override public void postToMainThread(Runnable r) { r.run(); }
        @Override public boolean isMainThread() { return true; }
    });

    try (MockedStatic<FirebaseDatabase> dbStatic = mockStatic(FirebaseDatabase.class);
         MockedStatic<Log> logStatic = mockStatic(Log.class)) {

        FirebaseDatabase mockDatabase = mock(FirebaseDatabase.class);
        DatabaseReference mockRootRef = mock(DatabaseReference.class);
        DatabaseReference mockUsersRef = mock(DatabaseReference.class);
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        Task<DataSnapshot> mockTask = mock(Task.class);
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        User expectedUser = mock(User.class);

        dbStatic.when(FirebaseDatabase::getInstance).thenReturn(mockDatabase);
        when(mockDatabase.getReference()).thenReturn(mockRootRef);
        when(mockRootRef.child("users")).thenReturn(mockUsersRef);
        when(mockUsersRef.child("uid-abc")).thenReturn(mockChildRef);
        when(mockChildRef.get()).thenReturn(mockTask);
        when(mockTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<DataSnapshot> listener = invocation.getArgument(0);
            when(mockSnapshot.getValue(User.class)).thenReturn(expectedUser);
            listener.onSuccess(mockSnapshot);
            return mockTask;
        });
        when(mockTask.addOnFailureListener(any())).thenReturn(mockTask);

        LiveData<User> result = repository.getUserById("uid-abc");

        assertNotNull(result);
        assertNotNull(result.getValue());
    } finally {
        ArchTaskExecutor.getInstance().setDelegate(null);
    }
}

    @Test
void givenValidUid_whenGetUserByIdFails_thenLiveDataContainsNull() {
    ArchTaskExecutor.getInstance().setDelegate(new TaskExecutor() {
        @Override public void executeOnDiskIO(Runnable r) { r.run(); }
        @Override public void postToMainThread(Runnable r) { r.run(); }
        @Override public boolean isMainThread() { return true; }
    });

    try (MockedStatic<FirebaseDatabase> dbStatic = mockStatic(FirebaseDatabase.class);
         MockedStatic<Log> logStatic = mockStatic(Log.class)) {

        FirebaseDatabase mockDatabase = mock(FirebaseDatabase.class);
        DatabaseReference mockRootRef = mock(DatabaseReference.class);
        DatabaseReference mockUsersRef = mock(DatabaseReference.class);
        DatabaseReference mockChildRef = mock(DatabaseReference.class);
        Task<DataSnapshot> mockTask = mock(Task.class);

        dbStatic.when(FirebaseDatabase::getInstance).thenReturn(mockDatabase);
        when(mockDatabase.getReference()).thenReturn(mockRootRef);
        when(mockRootRef.child("users")).thenReturn(mockUsersRef);
        when(mockUsersRef.child("uid-fail")).thenReturn(mockChildRef);
        when(mockChildRef.get()).thenReturn(mockTask);
        when(mockTask.addOnSuccessListener(any())).thenReturn(mockTask);
        when(mockTask.addOnFailureListener(any())).thenAnswer(invocation -> {
            OnFailureListener listener = invocation.getArgument(0);
            listener.onFailure(new Exception("Database error"));
            return mockTask;
        });

        LiveData<User> result = repository.getUserById("uid-fail");

        assertNotNull(result);
        assertEquals(null, result.getValue());
    } finally {
        ArchTaskExecutor.getInstance().setDelegate(null);
    }
}

// ignOut — auth already initialized → signOut called directly


@Test
void givenAuthInitialized_whenSignOutCalled_thenFirebaseSignOutInvoked_primePath7() {
    
    repository.signOut();

    verify(firebaseAuth).signOut();
}


// signOut — auth is null → getAuth() re-initializes, then signOut called


@Test
void givenAuthIsNull_whenSignOutCalled_thenReinitializesAndSignsOut_primePath8() throws Exception {
    try (MockedStatic<FirebaseAuth> firebaseAuthStatic = mockStatic(FirebaseAuth.class)) {
        firebaseAuthStatic.when(FirebaseAuth::getInstance).thenReturn(firebaseAuth);

        
        java.lang.reflect.Field authField = AuthRepository.class.getDeclaredField("auth");
        authField.setAccessible(true);
        authField.set(repository, null);

        repository.signOut();

        firebaseAuthStatic.verify(FirebaseAuth::getInstance);
        verify(firebaseAuth).signOut();
    }
}
}


