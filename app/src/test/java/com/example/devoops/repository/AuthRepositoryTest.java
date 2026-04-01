package com.example.devoops.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

}


