package com.example.devoops.repositoryTest;

import static org.mockito.Mockito.*;
import com.example.devoops.repository.AuthRepository;
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
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthRepositoryTest {
    private AuthRepository authRepository;
    @Mock private FirebaseAuth mockAuth;
    @Mock private FirebaseUser mockUser;
    @Mock private Task<AuthResult> mockTask;
    @Mock private AuthRepository.AuthCallback mockCallback;

    @BeforeEach
    void setup() {
        authRepository = new AuthRepository(mockAuth);
    }

    @Test
    void signupEmail_Path1_Success() {
        String email = "jonathan@concordia.ca";
        String pass = "password123";
        String uid = "user-123";

        when(mockAuth.createUserWithEmailAndPassword(email, pass)).thenReturn(mockTask);
        when(mockTask.isSuccessful()).thenReturn(true);
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn(uid);

        authRepository.signupEmail(email, pass, mockCallback);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<OnCompleteListener<AuthResult>> captor =
                ArgumentCaptor.forClass(OnCompleteListener.class);

        verify(mockTask).addOnCompleteListener(captor.capture());
        captor.getValue().onComplete(mockTask);

        verify(mockCallback).onSuccess(uid);
        verify(mockCallback, never()).onError(anyString());
    }

    @Test
    void signupEmail_Path2_Failure() {
        String error = "Email already in use";
        when(mockAuth.createUserWithEmailAndPassword(anyString(), anyString())).thenReturn(mockTask);
        when(mockTask.isSuccessful()).thenReturn(false);
        when(mockTask.getException()).thenReturn(new Exception(error));

        authRepository.signupEmail("test@me.com", "pass", mockCallback);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<OnCompleteListener<AuthResult>> captor =
                ArgumentCaptor.forClass(OnCompleteListener.class);

        verify(mockTask).addOnCompleteListener(captor.capture());
        captor.getValue().onComplete(mockTask);

        verify(mockCallback).onError(error);
        verify(mockCallback, never()).onSuccess(anyString());
    }
}