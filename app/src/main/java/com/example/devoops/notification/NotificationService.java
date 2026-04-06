package com.example.devoops.notification;

import com.example.devoops.models.Event;
import com.example.devoops.models.User;

public interface NotificationService {
    void sendReservationConfirmation(User user, Event event, String reservationId);
    void sendCancellationConfirmation(User user, Event event, String reservationId);
}
