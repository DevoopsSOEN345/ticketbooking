package com.example.devoops.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.lifecycle.MutableLiveData;

import com.example.devoops.InstantExecutorExtension;
import com.example.devoops.models.Event;
import com.example.devoops.models.User;
import com.example.devoops.models.UserRole;
import com.example.devoops.notification.NotificationService;
import com.example.devoops.repository.AuthRepository;
import com.example.devoops.repository.ReservationRepository;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Prime path coverage for {@link NotifyingReservationViewModel} (OCP subclass).
 *
 *   P1  – reserve success + user loaded → sendReservationConfirmation called
 *   P2  – reserve success + user null   → no notification (no crash)
 *   P3  – reserve error                 → no notification, error status
 *   P4  – reserve when not logged in    → no repo call, no notification
 *   P5  – cancel success + user loaded  → sendCancellationConfirmation called
 *   P6  – cancel success + user null    → no notification
 *   P7  – cancel error                  → no notification, error status
 *   P8  – cancel when not in map        → "Reservation not found", no notification
 *   P9  – init loads user from AuthRepository
 *   P10 – init is idempotent (second call ignored)
 */
@ExtendWith({InstantExecutorExtension.class, MockitoExtension.class})
class NotifyingReservationViewModelTest {

    @Mock private ReservationRepository reservationRepo;
    @Mock private DatabaseReference eventsRef;
    @Mock private NotificationService notificationService;
    @Mock private AuthRepository authRepo;

    @Mock private FirebaseDatabase firebaseDatabase;
    @Mock private DatabaseReference dbRoot;
    @Mock private DatabaseReference reservationsDbRef;
    @Mock private Query mockQuery;

    private MockedStatic<FirebaseDatabase> firebaseMock;
    private NotifyingReservationViewModel viewModel;

    private static final String USER_ID  = "user-1";
    private static final String EVENT_ID = "evt-1";
    private static final String RES_ID   = "res-1";

    private final User  testUser  = new User(USER_ID, "Alice", "alice@test.com", "+1234", UserRole.CUSTOMER);
    private final Event testEvent = new Event(EVENT_ID, "Jazz Night", "2026-05-01", "Music", "Montreal", 100);

    @BeforeEach
    void setUp() {
        firebaseMock = Mockito.mockStatic(FirebaseDatabase.class);
        firebaseMock.when(FirebaseDatabase::getInstance).thenReturn(firebaseDatabase);
        lenient().when(firebaseDatabase.getReference()).thenReturn(dbRoot);
        lenient().when(dbRoot.child("reservations")).thenReturn(reservationsDbRef);
        lenient().when(reservationsDbRef.orderByChild("userId")).thenReturn(mockQuery);
        lenient().when(mockQuery.equalTo(anyString())).thenReturn(mockQuery);

        viewModel = new NotifyingReservationViewModel(
                reservationRepo, eventsRef, notificationService, authRepo);
    }

    @AfterEach
    void tearDown() {
        firebaseMock.close();
    }

    @SuppressWarnings("unchecked")
    private void seedReservationMap(String eventId, String reservationId) throws Exception {
        Field field = ReservationViewModel.class.getDeclaredField("eventToReservationMap");
        field.setAccessible(true);
        ((Map<String, String>) field.get(viewModel)).put(eventId, reservationId);
    }

    // ─── RESERVE ──────────────────────────────────────────────────────────────

    @Test
    void givenUserLoaded_whenReserveSucceeds_thenConfirmationSent_primePath1() {
        // Given
        MutableLiveData<User> userLd = new MutableLiveData<>(testUser);
        when(authRepo.getUserById(USER_ID)).thenReturn(userLd);
        viewModel.init(USER_ID);

        // When
        viewModel.reserveEvent(testEvent);
        captureReserveCallback().onSuccess(RES_ID);

        // Then
        verify(notificationService).sendReservationConfirmation(eq(testUser), eq(testEvent), eq(RES_ID));
        assertEquals("Reserved successfully!", viewModel.getStatusMessage().getValue());
    }

    @Test
    void givenUserNull_whenReserveSucceeds_thenNoNotification_primePath2() {
        // Given — LiveData with no value
        MutableLiveData<User> userLd = new MutableLiveData<>();
        when(authRepo.getUserById(USER_ID)).thenReturn(userLd);
        viewModel.init(USER_ID);

        // When
        viewModel.reserveEvent(testEvent);
        captureReserveCallback().onSuccess(RES_ID);

        // Then
        verify(notificationService, never()).sendReservationConfirmation(any(), any(), anyString());
        assertEquals("Reserved successfully!", viewModel.getStatusMessage().getValue());
    }

    @Test
    void givenUserLoaded_whenReserveFails_thenNoNotification_primePath3() {
        // Given
        MutableLiveData<User> userLd = new MutableLiveData<>(testUser);
        when(authRepo.getUserById(USER_ID)).thenReturn(userLd);
        viewModel.init(USER_ID);

        // When
        viewModel.reserveEvent(testEvent);
        captureReserveCallback().onError("No seats");

        // Then
        verify(notificationService, never()).sendReservationConfirmation(any(), any(), anyString());
        assertEquals("No seats", viewModel.getStatusMessage().getValue());
    }

    @Test
    void givenNotLoggedIn_whenReserve_thenNoRepoCallAndNoNotification_primePath4() {
        // When — init never called
        viewModel.reserveEvent(testEvent);

        // Then
        verify(reservationRepo, never()).createReservation(anyString(), anyString(),
                any(ReservationRepository.ReservationCallback.class));
        verify(notificationService, never()).sendReservationConfirmation(any(), any(), anyString());
        assertEquals("Not logged in", viewModel.getStatusMessage().getValue());
    }

    // ─── CANCEL ───────────────────────────────────────────────────────────────

    @Test
    void givenUserLoaded_whenCancelSucceeds_thenCancellationSent_primePath5() throws Exception {
        // Given
        MutableLiveData<User> userLd = new MutableLiveData<>(testUser);
        when(authRepo.getUserById(USER_ID)).thenReturn(userLd);
        viewModel.init(USER_ID);
        seedReservationMap(EVENT_ID, RES_ID);

        // When
        viewModel.cancelReservation(testEvent);
        captureCancelCallback().onSuccess(RES_ID);

        // Then
        verify(notificationService).sendCancellationConfirmation(eq(testUser), eq(testEvent), eq(RES_ID));
        assertEquals("Reservation cancelled", viewModel.getStatusMessage().getValue());
    }

    @Test
    void givenUserNull_whenCancelSucceeds_thenNoNotification_primePath6() throws Exception {
        // Given
        MutableLiveData<User> userLd = new MutableLiveData<>();
        when(authRepo.getUserById(USER_ID)).thenReturn(userLd);
        viewModel.init(USER_ID);
        seedReservationMap(EVENT_ID, RES_ID);

        // When
        viewModel.cancelReservation(testEvent);
        captureCancelCallback().onSuccess(RES_ID);

        // Then
        verify(notificationService, never()).sendCancellationConfirmation(any(), any(), anyString());
        assertEquals("Reservation cancelled", viewModel.getStatusMessage().getValue());
    }

    @Test
    void givenUserLoaded_whenCancelFails_thenNoNotification_primePath7() throws Exception {
        // Given
        MutableLiveData<User> userLd = new MutableLiveData<>(testUser);
        when(authRepo.getUserById(USER_ID)).thenReturn(userLd);
        viewModel.init(USER_ID);
        seedReservationMap(EVENT_ID, RES_ID);

        // When
        viewModel.cancelReservation(testEvent);
        captureCancelCallback().onError("Cancel failed");

        // Then
        verify(notificationService, never()).sendCancellationConfirmation(any(), any(), anyString());
        assertEquals("Cancel failed", viewModel.getStatusMessage().getValue());
    }

    @Test
    void givenNotInMap_whenCancel_thenNoRepoCallAndNoNotification_primePath8() {
        // Given
        MutableLiveData<User> userLd = new MutableLiveData<>(testUser);
        when(authRepo.getUserById(USER_ID)).thenReturn(userLd);
        viewModel.init(USER_ID);

        // When
        viewModel.cancelReservation(testEvent);

        // Then
        verify(reservationRepo, never()).cancelReservation(anyString(), anyString(),
                any(ReservationRepository.ReservationCallback.class));
        verify(notificationService, never()).sendCancellationConfirmation(any(), any(), anyString());
        assertEquals("Reservation not found", viewModel.getStatusMessage().getValue());
    }

    // ─── INIT ─────────────────────────────────────────────────────────────────

    @Test
    void givenUserId_whenInit_thenLoadsUserFromAuthRepo_primePath9() {
        // Given
        MutableLiveData<User> userLd = new MutableLiveData<>(testUser);
        when(authRepo.getUserById(USER_ID)).thenReturn(userLd);

        // When
        viewModel.init(USER_ID);

        // Then
        verify(authRepo).getUserById(USER_ID);
    }

    @Test
    void givenAlreadyInitialised_whenInitAgain_thenIgnored_primePath10() {
        // Given
        MutableLiveData<User> userLd = new MutableLiveData<>(testUser);
        when(authRepo.getUserById(USER_ID)).thenReturn(userLd);
        viewModel.init(USER_ID);

        // When
        viewModel.init(USER_ID);

        // Then — only called once
        verify(authRepo).getUserById(USER_ID);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private ReservationRepository.ReservationCallback captureReserveCallback() {
        ArgumentCaptor<ReservationRepository.ReservationCallback> c =
                ArgumentCaptor.forClass(ReservationRepository.ReservationCallback.class);
        verify(reservationRepo).createReservation(eq(USER_ID), eq(EVENT_ID), c.capture());
        return c.getValue();
    }

    private ReservationRepository.ReservationCallback captureCancelCallback() {
        ArgumentCaptor<ReservationRepository.ReservationCallback> c =
                ArgumentCaptor.forClass(ReservationRepository.ReservationCallback.class);
        verify(reservationRepo).cancelReservation(eq(RES_ID), eq(EVENT_ID), c.capture());
        return c.getValue();
    }
}
