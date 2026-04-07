package com.example.devoops.presentation;

import androidx.lifecycle.LiveData;

import com.example.devoops.models.Event;
import com.example.devoops.models.User;
import com.example.devoops.notification.NotificationService;
import com.example.devoops.repository.AuthRepository;
import com.example.devoops.repository.ReservationRepository;
import com.google.firebase.database.DatabaseReference;

/**
 * Extension of {@link ReservationViewModel} that adds email / SMS confirmation
 * via the Template Method hooks (OCP — no modification to the base class).
 *
 * <p>Responsibilities added by this subclass (SRP):
 * <ul>
 *   <li>Load the current {@link User} from {@link AuthRepository}</li>
 *   <li>Delegate to {@link NotificationService} on reservation / cancellation success</li>
 * </ul>
 */
public class NotifyingReservationViewModel extends ReservationViewModel {

    private final NotificationService notificationService;
    private final AuthRepository authRepo;
    private LiveData<User> currentUserLiveData;

    /** Production constructor — called by {@link ReservationViewModelFactory}. */
    public NotifyingReservationViewModel(NotificationService notificationService,
                                         AuthRepository authRepo) {
        super();
        this.notificationService = notificationService;
        this.authRepo = authRepo;
    }

    /** Test constructor — inject mocked repository and refs. */
    public NotifyingReservationViewModel(ReservationRepository reservationRepo,
                                         DatabaseReference eventsRef,
                                         NotificationService notificationService,
                                         AuthRepository authRepo) {
        super(reservationRepo, eventsRef);
        this.notificationService = notificationService;
        this.authRepo = authRepo;
    }

    @Override
    public void init(String userId) {
        if (isInitialized()) {
            return;
        }
        super.init(userId);
        currentUserLiveData = authRepo.getUserById(userId);
    }

    @Override
    protected void onReservationSuccess(Event event, String reservationId) {
        User user = currentUserLiveData != null ? currentUserLiveData.getValue() : null;
        if (user != null) {
            notificationService.sendReservationConfirmation(user, event, reservationId);
        }
    }

    @Override
    protected void onCancellationSuccess(Event event, String reservationId) {
        User user = currentUserLiveData != null ? currentUserLiveData.getValue() : null;
        if (user != null) {
            notificationService.sendCancellationConfirmation(user, event, reservationId);
        }
    }
}
