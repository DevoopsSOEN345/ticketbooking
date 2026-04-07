package com.example.devoops.notification;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.telephony.SmsManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

/**
 * Prime path coverage for AndroidSmsNotificationService.
 *
 * Control flow for sendSms(phoneNumber, message):
 *   [1] SmsManager.getDefault()
 *   [2] message.length() > 160 ?
 *       ├─ YES → [3] divideMessage + sendMultipartTextMessage
 *       └─ NO  → [4] sendTextMessage
 *   [5] SmsManager throws → exception propagated
 *
 * Prime paths:
 *   P1 – short message (≤160 chars) → sendTextMessage called
 *   P2 – long message (>160 chars) → divideMessage + sendMultipartTextMessage called
 *   P3 – exactly 160 chars → sendTextMessage (boundary)
 *   P4 – exactly 161 chars → sendMultipartTextMessage (boundary)
 *   P5 – SmsManager.getDefault() returns null → NullPointerException propagated
 *   P6 – sendTextMessage throws → exception propagated
 */
@ExtendWith(MockitoExtension.class)
class AndroidSmsNotificationServiceTest {

    @Mock private SmsManager mockSmsManager;

    private AndroidSmsNotificationService service;
    private MockedStatic<SmsManager> smsManagerMock;

    @BeforeEach
    void setUp() {
        service = new AndroidSmsNotificationService();
        smsManagerMock = Mockito.mockStatic(SmsManager.class);
        smsManagerMock.when(SmsManager::getDefault).thenReturn(mockSmsManager);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        smsManagerMock.close();
    }

    @Test
    void givenShortMessage_whenSendSms_thenSendTextMessageCalled_primePath1() {
        // Given
        String msg = "Your reservation is confirmed.";

        // When
        assertDoesNotThrow(() -> service.sendSms("+15141234567", msg));

        // Then
        verify(mockSmsManager).sendTextMessage(
                eq("+15141234567"), isNull(), eq(msg), isNull(), isNull());
    }

    @Test
    void givenLongMessage_whenSendSms_thenSendMultipartCalled_primePath2() {
        // Given — 200 chars
        String msg = "A".repeat(200);
        ArrayList<String> parts = new ArrayList<>();
        parts.add(msg.substring(0, 160));
        parts.add(msg.substring(160));
        when(mockSmsManager.divideMessage(msg)).thenReturn(parts);

        // When
        assertDoesNotThrow(() -> service.sendSms("+15141234567", msg));

        // Then
        verify(mockSmsManager).divideMessage(msg);
        verify(mockSmsManager).sendMultipartTextMessage(
                eq("+15141234567"), isNull(), eq(parts), isNull(), isNull());
    }

    @Test
    void givenExactly160Chars_whenSendSms_thenSendTextMessageCalled_primePath3() {
        // Given — boundary: exactly 160
        String msg = "B".repeat(160);

        // When
        assertDoesNotThrow(() -> service.sendSms("+15149876543", msg));

        // Then
        verify(mockSmsManager).sendTextMessage(
                eq("+15149876543"), isNull(), eq(msg), isNull(), isNull());
    }

    @Test
    void givenExactly161Chars_whenSendSms_thenSendMultipartCalled_primePath4() {
        // Given — boundary: exactly 161
        String msg = "C".repeat(161);
        ArrayList<String> parts = new ArrayList<>();
        parts.add(msg.substring(0, 160));
        parts.add(msg.substring(160));
        when(mockSmsManager.divideMessage(msg)).thenReturn(parts);

        // When
        assertDoesNotThrow(() -> service.sendSms("+15149876543", msg));

        // Then
        verify(mockSmsManager).divideMessage(msg);
        verify(mockSmsManager).sendMultipartTextMessage(
                eq("+15149876543"), isNull(), eq(parts), isNull(), isNull());
    }

    @Test
    void givenSmsManagerReturnsNull_whenSendSms_thenNullPointerPropagated_primePath5() {
        // Given
        smsManagerMock.when(SmsManager::getDefault).thenReturn(null);

        // When / Then
        assertThrows(NullPointerException.class,
                () -> service.sendSms("+15141234567", "test"));
    }

    @Test
    void givenSendTextMessageThrows_whenSendSms_thenExceptionPropagated_primePath6() {
        // Given
        Mockito.doThrow(new IllegalArgumentException("Invalid number"))
               .when(mockSmsManager)
               .sendTextMessage(eq("+bad"), isNull(), any(), isNull(), isNull());

        // When / Then
        assertThrows(IllegalArgumentException.class,
                () -> service.sendSms("+bad", "test"));
    }
}
