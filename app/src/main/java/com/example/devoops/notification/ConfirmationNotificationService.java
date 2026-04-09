package com.example.devoops.notification;

import android.util.Log;

import com.example.devoops.models.Event;
import com.example.devoops.models.User;

public class ConfirmationNotificationService implements NotificationService {

    private static final String TAG = "ConfirmationService";

    private final EmailNotificationService emailService;
    private final SmsNotificationService smsService;

    public ConfirmationNotificationService(EmailNotificationService emailService,
                                           SmsNotificationService smsService) {
        this.emailService = emailService;
        this.smsService = smsService;
    }

    @Override
    public void sendReservationConfirmation(User user, Event event, String reservationId) {
        String subject = "Reservation Confirmed: " + event.getName();
        String body = buildBody("confirmed", user, event, reservationId);
        dispatch(user, subject, body);
    }

    @Override
    public void sendCancellationConfirmation(User user, Event event, String reservationId) {
        String subject = "Reservation Cancellation: " + event.getName();
        String body = buildBody("cancelled", user, event, reservationId);
        dispatch(user, subject, body);
    }

    private void dispatch(User user, String subject, String body) {
        boolean hasEmail = user.getEmail() != null && !user.getEmail().isEmpty();
        boolean hasPhone = user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty();

        if (!hasEmail && !hasPhone) {
            Log.w(TAG, "No contact info for user: " + user.getUserId());
            return;
        }

        if (hasEmail) {
            try {
                emailService.sendEmail(user.getEmail(), subject, body);
                Log.d(TAG, "Email confirmation sent to: " + user.getEmail());
            } catch (Exception e) {
                Log.e(TAG, "Email delivery failed for: " + user.getEmail(), e);
            }
        }

        if (hasPhone) {
            try {
                smsService.sendSms(user.getPhoneNumber(), body);
                Log.d(TAG, "SMS confirmation sent to: " + user.getPhoneNumber());
            } catch (Exception e) {
                Log.e(TAG, "SMS delivery failed for: " + user.getPhoneNumber(), e);
            }
        }
    }

    private String buildBody(String action, User user, Event event, String reservationId) {
        return "Hello " + user.getName() + ",\n\n"
                + "Your reservation has been " + action + ".\n\n"
                + "Event: " + event.getName() + "\n"
                + "Date & Time: " + event.getDateTime() + "\n"
                + "Location: " + event.getLocation() + "\n"
                + "Reservation ID: " + reservationId + "\n\n"
                + "Thank you for using our service.";
    }
}
