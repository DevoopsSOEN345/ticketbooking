package com.example.devoops.notification;

public interface SmsNotificationService {
    void sendSms(String phoneNumber, String message) throws Exception;
}
