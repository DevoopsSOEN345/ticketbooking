package com.example.devoops.notification;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.example.devoops.models.Event;
import com.example.devoops.models.User;
import com.example.devoops.models.UserRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prime-path focused coverage for ConfirmationNotificationService.
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

    @Test
    void givenNoContact_whenReservationConfirmed_thenNoDeliveryAttempts() {
        User user = userWith(null, null);

        assertDoesNotThrow(() -> service.sendReservationConfirmation(user, event, RESERVATION_ID));

        verifyNoInteractions(emailService);
        verifyNoInteractions(smsService);
    }

    @Test
    void givenEmailOnly_whenReservationConfirmed_thenEmailIsSent() throws Exception {
        User user = userWith("alice@test.com", null);

        service.sendReservationConfirmation(user, event, RESERVATION_ID);

        verify(emailService).sendEmail(eq("alice@test.com"), anyString(), anyString());
        verifyNoInteractions(smsService);
    }

    @Test
    void givenPhoneOnly_whenReservationConfirmed_thenSmsIsSent() throws Exception {
        User user = userWith(null, "+15141234567");

        service.sendReservationConfirmation(user, event, RESERVATION_ID);

        verifyNoInteractions(emailService);
        verify(smsService).sendSms(anyString(), anyString());
    }

    @Test
    void givenEmailAndPhone_whenReservationConfirmed_thenBothChannelsSend() throws Exception {
        User user = userWith("alice@test.com", "+15141234567");

        service.sendReservationConfirmation(user, event, RESERVATION_ID);

        verify(emailService).sendEmail(eq("alice@test.com"), anyString(), anyString());
        verify(smsService).sendSms(eq("+15141234567"), anyString());
    }

    @Test
    void givenEmailFailsAndPhonePresent_whenReservationConfirmed_thenSmsStillSends() throws Exception {
        User user = userWith("alice@test.com", "+15141234567");
        doThrow(new Exception("SMTP error")).when(emailService).sendEmail(anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> service.sendReservationConfirmation(user, event, RESERVATION_ID));

        verify(smsService).sendSms(anyString(), anyString());
    }

    @Test
    void givenSmsFailsAndEmailPresent_whenReservationConfirmed_thenEmailStillSends() throws Exception {
        User user = userWith("alice@test.com", "+15141234567");
        doThrow(new Exception("SMS gateway error")).when(smsService).sendSms(anyString(), anyString());

        assertDoesNotThrow(() -> service.sendReservationConfirmation(user, event, RESERVATION_ID));

        verify(emailService).sendEmail(eq("alice@test.com"), anyString(), anyString());
    }

    @Test
    void givenBothChannelsFail_whenReservationConfirmed_thenMethodDoesNotThrow() throws Exception {
        User user = userWith("alice@test.com", "+15141234567");
        doThrow(new Exception("Email down")).when(emailService).sendEmail(anyString(), anyString(), anyString());
        doThrow(new Exception("SMS down")).when(smsService).sendSms(anyString(), anyString());
        assertDoesNotThrow(() -> service.sendReservationConfirmation(user, event, RESERVATION_ID));
    }

    @Test
    void givenReservationConfirmation_whenEmailSent_thenSubjectAndBodyContainCoreDetails() throws Exception {
        User user = userWith("alice@test.com", null);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        service.sendReservationConfirmation(user, event, RESERVATION_ID);

        verify(emailService).sendEmail(eq("alice@test.com"), subjectCaptor.capture(), bodyCaptor.capture());
        String subject = subjectCaptor.getValue().toLowerCase();
        String body = bodyCaptor.getValue();
        assertTrue(subject.contains("confirm"));
        assertTrue(body.contains("Jazz Night"));
        assertTrue(body.contains("2026-05-01 20:00"));
        assertTrue(body.contains("Montreal"));
        assertTrue(body.contains(RESERVATION_ID));
    }

    @Test
    void givenCancellationConfirmation_whenEmailSent_thenSubjectAndBodyIndicateCancellation() throws Exception {
        User user = userWith("alice@test.com", null);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        service.sendCancellationConfirmation(user, event, RESERVATION_ID);

        verify(emailService).sendEmail(eq("alice@test.com"), subjectCaptor.capture(), bodyCaptor.capture());
        String subject = subjectCaptor.getValue().toLowerCase();
        String body = bodyCaptor.getValue().toLowerCase();
        assertTrue(subject.contains("cancel") || subject.contains("cancellation"));
        assertTrue(body.contains("cancelled"));
    }
}
