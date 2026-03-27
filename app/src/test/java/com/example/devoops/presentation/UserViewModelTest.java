package com.example.devoops.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import androidx.lifecycle.MutableLiveData;

import org.mockito.junit.jupiter.MockitoExtension;

import com.example.devoops.InstantExecutorExtension;
import com.example.devoops.models.User;
import com.example.devoops.repository.AuthRepository;
import com.example.devoops.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

@ExtendWith({InstantExecutorExtension.class, MockitoExtension.class})
class UserViewModelTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private UserRepository userRepository;

    private UserViewModel viewModel;

    @BeforeEach
    void setUp() {
        // Given
        viewModel = new UserViewModel(authRepository, userRepository);
    }

    @ParameterizedTest
    @CsvSource({
            "'', password123, Jonathan",
            "test@me.com, 12345, Jonathan",
            "test@me.com, password123, ''"
    })
    void givenInvalidSignupInput_whenSignupEmailCalled_thenValidationMessageAndNoRepositoryCall(
            String email,
            String password,
            String name) {
        // When
        viewModel.signupEmail(email, password, name);

        // Then
        assertEquals("Valid email and 6+ char password required", viewModel.status.getValue());
        verifyNoInteractions(authRepository);
        verifyNoInteractions(userRepository);
    }

    @Test
    void givenValidSignupInput_whenSignupSucceeds_thenCreatesUserAndSetsSuccessStatus() {
        // Given
        String email = "valid@concordia.ca";
        String password = "password123";
        String name = "Jonathan";
        String uid = "uid-123";

        // When
        viewModel.signupEmail(email, password, name);
        ArgumentCaptor<AuthRepository.AuthCallback> callbackCaptor =
                ArgumentCaptor.forClass(AuthRepository.AuthCallback.class);
        verify(authRepository).signupEmail(eq(email), eq(password), callbackCaptor.capture());
        callbackCaptor.getValue().onSuccess(uid);

        // Then
        verify(userRepository).createUser(eq(uid), eq(email), eq(null), eq(name));
        assertEquals("Signup success", viewModel.status.getValue());
    }

    @Test
    void givenValidSignupInput_whenSignupFails_thenSetsErrorStatus() {
        // Given
        String error = "Weak password";

        // When
        viewModel.signupEmail("valid@concordia.ca", "password123", "Jonathan");
        ArgumentCaptor<AuthRepository.AuthCallback> callbackCaptor =
                ArgumentCaptor.forClass(AuthRepository.AuthCallback.class);
        verify(authRepository).signupEmail(anyString(), anyString(), callbackCaptor.capture());
        callbackCaptor.getValue().onError(error);

        // Then
        verifyNoInteractions(userRepository);
        assertEquals(error, viewModel.status.getValue());
    }

    @Test
    void givenLoginInput_whenLoginSucceeds_thenSetsLoginSuccessStatus() {
        // Given
        MutableLiveData<User> fakeLiveData = new MutableLiveData<>();
        fakeLiveData.setValue(new User());

        when(authRepository.getUserById(anyString())).thenReturn(fakeLiveData);

        // When
        viewModel.loginEmail("valid@concordia.ca", "password123");

        ArgumentCaptor<AuthRepository.AuthCallback> callbackCaptor =
                ArgumentCaptor.forClass(AuthRepository.AuthCallback.class);

        verify(authRepository).loginEmail(eq("valid@concordia.ca"), eq("password123"), callbackCaptor.capture());

        callbackCaptor.getValue().onSuccess("uid-456");

        // Then
        assertEquals("Login success", viewModel.status.getValue());
    }

    @Test
    void givenLoginInput_whenLoginFails_thenSetsErrorStatus() {
        // Given
        String error = "Invalid credentials";

        // When
        viewModel.loginEmail("wrong@concordia.ca", "wrong");
        ArgumentCaptor<AuthRepository.AuthCallback> callbackCaptor =
                ArgumentCaptor.forClass(AuthRepository.AuthCallback.class);
        verify(authRepository).loginEmail(anyString(), anyString(), callbackCaptor.capture());
        callbackCaptor.getValue().onError(error);

        // Then
        assertEquals(error, viewModel.status.getValue());
    }

    @Test
    void givenVerificationIdField_whenSetAndRead_thenValueIsPreserved() {
        // Given / When
        viewModel.verificationId = "verification-123";

        // Then
        assertEquals("verification-123", viewModel.verificationId);
    }

    @Test
    void givenConstructor_whenViewModelCreated_thenLiveDataIsInitialized() {
        // Then
        assertNotNull(viewModel);
        assertNotNull(viewModel.status);
        assertNotNull(viewModel.getLoggedInUser());
    }
}