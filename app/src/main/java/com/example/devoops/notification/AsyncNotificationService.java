package com.example.devoops.notification;

import com.example.devoops.models.Event;
import com.example.devoops.models.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Decorator (OCP) — wraps any {@link NotificationService} so that delivery
 * runs on a background thread.  The delegate handles the actual dispatch;
 * this class only adds the threading concern (SRP).
 */
public class AsyncNotificationService implements NotificationService {

    private final NotificationService delegate;
    private final ExecutorService executor;

    public AsyncNotificationService(NotificationService delegate) {
        this(delegate, Executors.newSingleThreadExecutor());
    }

    // For testing — inject a synchronous executor
    public AsyncNotificationService(NotificationService delegate, ExecutorService executor) {
        this.delegate = delegate;
        this.executor = executor;
    }

    @Override
    public void sendReservationConfirmation(User user, Event event, String reservationId) {
        executor.execute(() -> delegate.sendReservationConfirmation(user, event, reservationId));
    }

    @Override
    public void sendCancellationConfirmation(User user, Event event, String reservationId) {
        executor.execute(() -> delegate.sendCancellationConfirmation(user, event, reservationId));
    }
}
