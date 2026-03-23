package com.example.devoops.presentationTest;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.devoops.InstantExecutorExtension;
import com.example.devoops.presentation.UserViewModel;
import com.example.devoops.repository.AuthRepository;
import com.example.devoops.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class, InstantExecutorExtension.class})
public class UserViewModelTest {
    private UserViewModel viewModel;
    @Mock
    private AuthRepository mockAuthRepo;
    @Mock
    private UserRepository mockUserRepo;

    @BeforeEach
    void setup() {
        viewModel = new UserViewModel(mockAuthRepo, mockUserRepo);
    }

    @ParameterizedTest(name = "Path 1: Validation check with email={0}, password={1}")
    @CsvSource({
            "'', 'password123', 'Jonathan'",
            "'test@me.com', '123', 'Jonathan'",
            "'test@me.com', 'password123', ''"
    })
    void signupEmail_Path1_InvalidInput(String email, String password, String name) {
        viewModel.signupEmail(email, password, name);

        assertEquals("Valid email and 6+ char password required", viewModel.status.getValue());

        verifyNoInteractions(mockAuthRepo);
        verifyNoInteractions(mockUserRepo);
    }

    @Test
    void signupEmail_Path2_Success() {
        String email = "test@concordia.ca";
        String pass = "password123";
        String name = "Jonathan";
        String mockUid = "user_abc_123";

        viewModel.signupEmail(email, pass, name);

        ArgumentCaptor<AuthRepository.AuthCallback> captor =
                ArgumentCaptor.forClass(AuthRepository.AuthCallback.class);

        verify(mockAuthRepo).signupEmail(eq(email), eq(pass), captor.capture());

        captor.getValue().onSuccess(mockUid);

        verify(mockUserRepo).createUser(eq(mockUid), eq(email), isNull(), eq(name));
        assertEquals("Signup success", viewModel.status.getValue());
    }

    @Test
    void signupEmail_Path3_Failure() {
        String errorMsg = "Weak password";

        viewModel.signupEmail("test@me.com", "valid_password", "Jonathan");

        ArgumentCaptor<AuthRepository.AuthCallback> captor =
                ArgumentCaptor.forClass(AuthRepository.AuthCallback.class);

        verify(mockAuthRepo).signupEmail(anyString(), anyString(), captor.capture());
        captor.getValue().onError(errorMsg);

        verifyNoInteractions(mockUserRepo);
        assertEquals(errorMsg, viewModel.status.getValue());
    }

    @Test
    void loginEmail_Path1_Success() {
        String email = "test@concordia.ca";
        String pass = "password123";

        viewModel.loginEmail(email, pass);

        ArgumentCaptor<AuthRepository.AuthCallback> captor =
                ArgumentCaptor.forClass(AuthRepository.AuthCallback.class);

        verify(mockAuthRepo).loginEmail(eq(email), eq(pass), captor.capture());
        captor.getValue().onSuccess("mock-uid-456");

        assertEquals("Login success", viewModel.status.getValue());
    }

    @Test
    void loginEmail_Path2_Failure() {
        String errorMsg = "Invalid credentials";

        viewModel.loginEmail("wrong@email.com", "wrongpass");

        ArgumentCaptor<AuthRepository.AuthCallback> captor =
                ArgumentCaptor.forClass(AuthRepository.AuthCallback.class);

        verify(mockAuthRepo).loginEmail(anyString(), anyString(), captor.capture());
        captor.getValue().onError(errorMsg);

        assertEquals(errorMsg, viewModel.status.getValue());
    }
}