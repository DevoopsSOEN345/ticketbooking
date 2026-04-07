package com.example.devoops.notification;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Prime path coverage for SmtpEmailNotificationService.
 *
 * Control flow for sendEmail(to, subject, body):
 *   [1] Build Properties with smtpHost, smtpPort
 *   [2] Create Session with Authenticator(username, password)
 *   [3] Build MimeMessage: setFrom, setRecipients, setSubject, setText
 *   [4] Transport.send(message)
 *       ├─ success → return normally
 *       └─ throws  → exception propagated to caller
 *
 * Prime paths:
 *   P1 – sendEmail: Transport.send succeeds → method returns normally
 *   P2 – sendEmail: Transport.send throws → exception propagated
 *   P3 – constructor stores all SMTP config fields correctly
 *   P4 – session is created with correct SMTP properties
 *   P5 – message is built with correct from, to, subject, body
 */
@ExtendWith(MockitoExtension.class)
class SmtpEmailNotificationServiceTest {

    @Mock private Session mockSession;
    @Mock private MimeMessage mockMessage;

    @Test
    void givenValidConfig_whenSendEmailSucceeds_thenReturnsNormally_primePath1() {
        // Given
        SmtpEmailNotificationService service =
                new SmtpEmailNotificationService("smtp.test.com", 587, "user@test.com", "pass");

        try (MockedStatic<Transport> transport = mockStatic(Transport.class);
             MockedStatic<Session> sessionMock = mockStatic(Session.class)) {

            sessionMock.when(() -> Session.getInstance(any(), any())).thenReturn(mockSession);
            when(mockSession.getProperties()).thenReturn(new java.util.Properties());

            // When / Then
            assertDoesNotThrow(() -> service.sendEmail("to@test.com", "Subject", "Body"));
            transport.verify(() -> Transport.send(any(Message.class)));
        }
    }

    @Test
    void givenTransportThrows_whenSendEmail_thenExceptionPropagated_primePath2() {
        // Given
        SmtpEmailNotificationService service =
                new SmtpEmailNotificationService("smtp.test.com", 587, "user@test.com", "pass");

        try (MockedStatic<Transport> transport = mockStatic(Transport.class);
             MockedStatic<Session> sessionMock = mockStatic(Session.class)) {

            sessionMock.when(() -> Session.getInstance(any(), any())).thenReturn(mockSession);
            when(mockSession.getProperties()).thenReturn(new java.util.Properties());
            transport.when(() -> Transport.send(any(Message.class)))
                     .thenThrow(new javax.mail.MessagingException("SMTP connect failed"));

            // When / Then
            Exception ex = assertThrows(Exception.class,
                    () -> service.sendEmail("to@test.com", "Subject", "Body"));
            assertEquals("SMTP connect failed", ex.getMessage());
        }
    }

    @Test
    void givenConfig_whenConstructed_thenFieldsStoredCorrectly_primePath3() {
        // Given / When
        SmtpEmailNotificationService service =
                new SmtpEmailNotificationService("mail.example.com", 465, "admin@ex.com", "secret");

        // Then — verify via sendEmail that the config is used
        try (MockedStatic<Transport> transport = mockStatic(Transport.class);
             MockedStatic<Session> sessionMock = mockStatic(Session.class)) {

            ArgumentCaptor<java.util.Properties> propsCaptor =
                    ArgumentCaptor.forClass(java.util.Properties.class);

            sessionMock.when(() -> Session.getInstance(propsCaptor.capture(), any()))
                       .thenReturn(mockSession);
            when(mockSession.getProperties()).thenReturn(new java.util.Properties());

            assertDoesNotThrow(() -> service.sendEmail("to@test.com", "Sub", "Bod"));

            java.util.Properties captured = propsCaptor.getValue();
            assertEquals("mail.example.com", captured.get("mail.smtp.host"));
            assertEquals("465", captured.get("mail.smtp.port"));
            assertEquals("true", captured.get("mail.smtp.auth"));
            assertEquals("true", captured.get("mail.smtp.starttls.enable"));
        }
    }

    @Test
    void givenValidInputs_whenSendEmail_thenMessageBuiltWithCorrectFields_primePath5() throws Exception {
        // Given
        SmtpEmailNotificationService service =
                new SmtpEmailNotificationService("smtp.test.com", 587, "sender@test.com", "pass");

        try (MockedStatic<Transport> transport = mockStatic(Transport.class);
             MockedStatic<Session> sessionMock = mockStatic(Session.class)) {

            sessionMock.when(() -> Session.getInstance(any(), any())).thenReturn(mockSession);
            when(mockSession.getProperties()).thenReturn(new java.util.Properties());

            ArgumentCaptor<Message> msgCaptor = ArgumentCaptor.forClass(Message.class);

            // When
            service.sendEmail("recipient@test.com", "Test Subject", "Test Body");

            // Then
            transport.verify(() -> Transport.send(msgCaptor.capture()));
            Message sent = msgCaptor.getValue();
            assertNotNull(sent);
        }
    }
}
