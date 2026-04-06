package com.example.devoops.notification;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import android.util.Log;

import com.example.devoops.models.Event;
import com.example.devoops.models.User;
import com.example.devoops.models.UserRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prime path coverage for ConfirmationNotificationService.
 *
 * Control flow graph for sendReservationConfirmation / sendCancellationConfirmation:
 *
 *   START
 *     ├─ user has email? ─YES─► try emailService.send()
 *     │                            ├─ success ─► log success
 *     │                            └─ throws  ─► log error (no crash)
 *     ├─ user has phone? ─YES─► try smsService.send()
 *     │                            ├─ success ─► log success
 *     │                            └─ throws  ─► log error (no crash)
 *     └─ neither? ─────────────► log warning (no crash)
 *   END
 *
 * Prime paths:
 *   P1  – email present, email succeeds; phone present, SMS succeeds
 *   P2  – email present, email succeeds; no phone
 *   P3  – email present, email fails;    phone present, SMS succeeds
 *   P4  – email present, email fails;    no phone
 *   P5  – email present, email succeeds; phone present, SMS fails
 *   P6  – email present, email fails;    phone present, SMS fails
 *   P7  – no email; phone present, SMS succeeds
 *   P8  – no email; phone present, SMS fails
 *   P9  – no email, no phone
 *   P10 – cancellation: email only, succeeds
 *   P11 – cancellation: phone only, succeeds
 *   P12 – cancellation: both channels succeed
 *   P13 – cancellation: email fails, no crash
 *   P14 – reservation email body contains event name
 *   P15 – reservation email body contains event dateTime
 *   P16 – reservation email body contains event location
 *   P17 – reservation email body contains reservationId
 *   P18 – cancellation email subject indicates cancellation
 */
@ExtendWith(MockitoExtension.class)
class ConfirmationNotificationServiceTest {

    @Mock private EmailNotificationService emailService;
    @Mock private SmsNotificationService smsService;

    private ConfirmationNotificationService service;

    private static final String RESERVATION_ID = "res-001";
    private Event event;

    @BeforeEach
    void setUp() {
        service = new ConfirmationNotificationService(emailService, smsService);
        event = new Event("evt1", "Jazz Night", "2026-05-01 20:00", "Music", "Montreal", 100);
    }

    private User userWith(String email, String phone) {
        return new User("u1", "Alice", email, phone, UserRole.CUSTOMER);
    }

    // ─── RESERVATION CONFIRMATION ──────────────────────────────────────────────

    @Test
    void givenEmailAndPhone_whenReservationConfirmed_thenBothChannelsSend_primePath1() throws Exception {
        // Given
        User user = userWith("alice@test.com", "+15141234567");

        try (MockedStatic<Log> log = Mockito.mockStatic(Log.class)) {
            // When
            service.sendReservationConfirmation(user, event, RESERVATION_ID);

            // Then
            verify(emailService).sendEmail(eq("alice@test.com"), anyString(), anyString());
            verify(smsService).sendSms(eq("+15141234567"), anyString());
        }
    }

    @Test
    void givenEmailOnlyUser_whenReservationConfirmed_thenEmailSentAndSmsSkipped_primePath2() throws Exception {
        // Given
        User user = userWith("alice@test.com", null);

        try (MockedStatic<Log> log = Mockito.mockStatic(Log.class)) {
            // When
            service.sendReservationConfirmation(user, event, RESERVATION_ID);

            // Then
            verify(emailService).sendEmail(eq("alice@test.com"), anyString(), anyString());
            verifyNoInteractions(smsService);
        }
    }

    @Test
    void givenEmailFailsAndPhonePresent_whenReservationConfirmed_thenEmailErrorLoggedAndSmsSent_primePath3() throws Exception {
        // Given
        User user = userWith("alice@test.com", "+15141234567");
        doThrow(new Exception("SMTP error")).when(emailService).sendEmail(anyString(), anyString(), anyString());

        try (MockedStatic<Log> log = Mockito.mockStatic(Log.class)) {
            // When
            assertDoesNotThrow(() -> service.sendReservationConfirmation(user, event, RESERVATION_ID));

            // Then
            log.verify(() -> Log.e(anyString(), contains("Email delivery failed"), any(Exception.class)));
            verify(smsService).sendSms(eq("+15141234567"), anyString());
        }
    }

    @Test
    void givenEmailFailsAndNoPhone_whenReservationConfirmed_thenEmailErrorLoggedAndNoSmsSent_primePath4() throws Exception {
        // Given
        User user = userWith("alice@test.com", null);
        doThrow(new Exception("SMTP error")).when(emailService).sendEmail(anyString(), anyString(), anyString());

        try (MockedStatic<Log> log = Mockito.mockStatic(Log.class)) {
            // When
            assertDoesNotThrow(() -> service.sendReservationConfirmation(user, event, RESERVATION_ID));

            // Then
            log.verify(() -> Log.e(anyString(), contains("Email delivery failed"), any(Exception.class)));
            verifyNoInteractions(smsService);
        }
    }

    @Test
    void givenEmailSucceedsAndSmsFails_whenReservationConfirmed_thenSmsErrorLoggedAndNoException_primePath5() throws Exception {
        // Given
        User user = userWith("alice@test.com", "+15141234567");
        doThrow(new Exception("SMS gateway error")).when(smsService).sendSms(anyString(), anyString());

        try (MockedStatic<Log> log = Mockito.mockStatic(Log.class)) {
            // When
            assertDoesNotThrow(() -> service.sendReservationConfirmation(user, event, RESERVATION_ID));

            // Then
            verify(emailService).sendEmail(eq("alice@test.com"), anyString(), anyString());
            log.verify(() -> Log.e(anyString(), contains("SMS delivery failed"), any(Exception.class)));
        }
    }

    @Test
    void givenBothChannelsFail_whenReservationConfirmed_thenBothErrorsLoggedAndNoException_primePath6() throws Exception {
        // Given
        User user = userWith("alice@test.com", "+15141234567");
        doThrow(new Exception("Email down")).when(emailService).sendEmail(anyString(), anyString(), anyString());
        doThrow(new Exception("SMS down")).when(smsService).sendSms(anyString(), anyString());

        try (MockedStatic<Log> log = Mockito.mockStatic(Log.class)) {
            // When
            assertDoesNotThrow(() -> service.sendReservationConfirmation(user, event, RESERVATION_ID));

            // Then
            log.verify(() -> Log.e(anyString(), contains("Email delivery failed"), any(Exception.class)));
            log.verify(() -> Log.e(anyString(), contains("SMS delivery failed"), any(Exception.class)));
        }
    }

    @Test
    void givenPhoneOnlyUser_whenReservationConfirmed_thenSmsSentAndEmailSkipped_primePath7() throws Exception {
        // Given
        User user = userWith(null, "+15141234567");

        try (MockedStatic<Log> log = Mockito.mockStatic(Log.class)) {
            // When
            service.sendReservationConfirmation(user, event, RESERVATION_ID);

            // Then
            verifyNoInteractions(emailService);
            verify(smsService).sendSms(eq("+15141234567"), anyString());
        }
    }

    @Test
    void givenNoEmailAndSmsFails_whenReservationConfirmed_thenSmsErrorLoggedAndNoException_primePath8() throws Exception {
        // Given
        User user = userWith(null, "+15141234567");
        doThrow(new Exception("SMS gateway error")).when(smsService).sendSms(anyString(), anyString());

        try (MockedStatic<Log> log = Mockito.mockStatic(Log.class)) {
            // When
            assertDoesNotThrow(() -> service.sendReservationConfirmation(user, event, RESERVATION_ID));

            // Then
            verifyNoInteractions(emailService);
            log.verify(() -> Log.e(anyString(), contains("SMS delivery failed"), any(Exception.class)));
        }
    }

    @Test
    void givenNoContactInfo_whenReservationConfirmed_thenNoServiceCallsAndWarningLogged_primePath9() {
        // Given
        User user = userWith(null, null);

        try (MockedStatic<Log> log = Mockito.mockStatic(Log.class)) {
            // When
            assertDoesNotThrow(() -> service.sendReservationConfirmation(user, event, RESERVATION_ID));

            // Then
            verifyNoInteractions(emailService);
            verifyNoInteractions(smsService);
            log.verify(() -> Log.w(anyString(), contains("No contact info")));
        }
    }

    // ─── CANCELLATION CONFIRMATION ─────────────────────────────────────────────

    @Test
    void givenEmailOnlyUser_whenCancellationConfirmed_thenEmailSent_primePath10() throws Exception {
        // Given
        User user = userWith("alice@test.com", null);

        try (MockedStatic<Log> log = Mockito.mockStatic(Log.class)) {
            // When
            service.sendCancellationConfirmation(user, event, RESERVATION_ID);

            // Then
            verify(emailService).sendEmail(eq("alice@test.com"), anyString(), anyString());
            verifyNoInteractions(smsService);
        }
    }

    @Test
    void givenPhoneOnlyUser_whenCancellationConfirmed_thenSmsSent_primePath11() throws Exception {
        // Given
        User user = userWith(null, "+15141234567");

        try (MockedStatic<Log> log = Mockito.mockStatic(Log.class)) {
            // When
            service.sendCancellationConfirmation(user, event, RESERVATION_ID);

            // Then
            verifyNoInteractions(emailService);
            verify(smsService).sendSms(eq("+15141234567"), anyString());
        }
    }

    @Test
    void givenEmailAndPhone_whenCancellationConfirmed_thenBothChannelsSend_primePath12() throws Exception {
        // Given
        User user = userWith("alice@test.com", "+15141234567");

        try (MockedStatic<Log> log = Mockito.mockStatic(Log.class)) {
            // When
            service.sendCancellationConfirmation(user, event, RESERVATION_ID);

            // Then
            verify(emailService).sendEmail(eq("alice@test.com"), anyString(), anyString());
            verify(smsService).sendSms(eq("+15141234567"), anyString());
        }
    }

    @Test
    void givenEmailFails_whenCancellationConfirmed_thenLogsErrorAndDoesNotCrash_primePath13() throws Exception {
        // Given
        User user = userWith("alice@test.com", null);
        doThrow(new Exception("SMTP error")).when(emailService).sendEmail(anyString(), anyString(), anyString());

        try (MockedStatic<Log> log = Mockito.mockStatic(Log.class)) {
            // When
            assertDoesNotThrow(() -> service.sendCancellationConfirmation(user, event, RESERVATION_ID));

            // Then
            log.verify(() -> Log.e(anyString(), contains("Email delivery failed"), any(Exception.class)));
        }
    }

    // ─── MESSAGE CONTENT ───────────────────────────────────────────────────────

    @Test
    void givenReservation_whenEmailSent_thenBodyContainsEventName_primePath14() throws Exception {
        // Given
        User user = userWith("alice@test.com", null);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        try (MockedStatic<Log> log = Mockito.mockStatic(Log.class)) {
            // When
            service.sendReservationConfirmation(user, event, RESERVATION_ID);

            // Then
            verify(emailService).sendEmail(anyString(), anyString(), bodyCaptor.capture());
            assertTrue(bodyCaptor.getValue().contains("Jazz Night"),
                    "Body should contain event name");
        }
    }

    @Test
    void givenReservation_whenEmailSent_thenBodyContainsEventDateTime_primePath15() throws Exception {
        // Given
        User user = userWith("alice@test.com", null);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        try (MockedStatic<Log> log = Mockito.mockStatic(Log.class)) {
            // When
            service.sendReservationConfirmation(user, event, RESERVATION_ID);

            // Then
            verify(emailService).sendEmail(anyString(), anyString(), bodyCaptor.capture());
            assertTrue(bodyCaptor.getValue().contains("2026-05-01 20:00"),
                    "Body should contain event dateTime");
        }
    }

    @Test
    void givenReservation_whenEmailSent_thenBodyContainsEventLocation_primePath16() throws Exception {
        // Given
        User user = userWith("alice@test.com", null);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        try (MockedStatic<Log> log = Mockito.mockStatic(Log.class)) {
            // When
            service.sendReservationConfirmation(user, event, RESERVATION_ID);

            // Then
            verify(emailService).sendEmail(anyString(), anyString(), bodyCaptor.capture());
            assertTrue(bodyCaptor.getValue().contains("Montreal"),
                    "Body should contain event location");
        }
    }

    @Test
    void givenReservation_whenEmailSent_thenBodyContainsReservationId_primePath17() throws Exception {
        // Given
        User user = userWith("alice@test.com", null);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        try (MockedStatic<Log> log = Mockito.mockStatic(Log.class)) {
            // When
            service.sendReservationConfirmation(user, event, RESERVATION_ID);

            // Then
            verify(emailService).sendEmail(anyString(), anyString(), bodyCaptor.capture());
            assertTrue(bodyCaptor.getValue().contains(RESERVATION_ID),
                    "Body should contain reservationId");
        }
    }

    @Test
    void givenCancellation_whenEmailSent_thenSubjectIndicatesCancellation_primePath18() throws Exception {
        // Given
        User user = userWith("alice@test.com", null);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);

        try (MockedStatic<Log> log = Mockito.mockStatic(Log.class)) {
            // When
            service.sendCancellationConfirmation(user, event, RESERVATION_ID);

            // Then
            verify(emailService).sendEmail(anyString(), subjectCaptor.capture(), anyString());
            String subject = subjectCaptor.getValue().toLowerCase();
            assertTrue(subject.contains("cancel") || subject.contains("cancellation"),
                    "Subject should indicate cancellation");
        }
    }
}
