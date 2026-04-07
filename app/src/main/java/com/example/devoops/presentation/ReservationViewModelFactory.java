package com.example.devoops.presentation;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.devoops.BuildConfig;
import com.example.devoops.notification.AndroidSmsNotificationService;
import com.example.devoops.notification.AsyncNotificationService;
import com.example.devoops.notification.ConfirmationNotificationService;
import com.example.devoops.notification.NotificationService;
import com.example.devoops.notification.SmtpEmailNotificationService;
import com.example.devoops.repository.AuthRepository;

/**
 * Factory (DIP) — assembles the {@link NotifyingReservationViewModel}
 * with its concrete dependencies so that the Activity only depends on
 * the abstract {@link ReservationViewModel}.
 */
public class ReservationViewModelFactory implements ViewModelProvider.Factory {

    private final NotificationService notificationService;
    private final AuthRepository authRepo;

    /** Production factory — reads SMTP config from BuildConfig (local.properties). */
    public ReservationViewModelFactory() {
        NotificationService syncService = new ConfirmationNotificationService(
                new SmtpEmailNotificationService(
                        BuildConfig.SMTP_HOST,
                        BuildConfig.SMTP_PORT,
                        BuildConfig.SMTP_USERNAME,
                        BuildConfig.SMTP_PASSWORD),
                new AndroidSmsNotificationService());
        this.notificationService = new AsyncNotificationService(syncService);
        this.authRepo = new AuthRepository();
    }

    /** Test factory — inject mocks. */
    public ReservationViewModelFactory(NotificationService notificationService,
                                       AuthRepository authRepo) {
        this.notificationService = notificationService;
        this.authRepo = authRepo;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(NotifyingReservationViewModel.class)) {
            return (T) new NotifyingReservationViewModel(notificationService, authRepo);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
