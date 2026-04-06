package com.example.devoops.notification;

public interface EmailNotificationService {
    void sendEmail(String to, String subject, String body) throws Exception;
}
