package com.example.devoops.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.devoops.InstantExecutorExtension;
import com.example.devoops.models.Event;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Prime path coverage for {@link ReservationViewModel} (base class).
 * Tests the reservation logic AND that the template-method hooks are called.
 * Notification behaviour is NOT tested here — see {@link NotifyingReservationViewModelTest}.
 *
 *   P1 – reserveEvent success → statusMessage set, onReservationSuccess hook called
 *   P2 – reserveEvent error → statusMessage set, hook NOT called
 *   P3 – reserveEvent when not logged in → "Not logged in", no repo call
 *   P4 – cancelReservation success → statusMessage set, onCancellationSuccess hook called
 *   P5 – cancelReservation error → statusMessage set, hook NOT called
 *   P6 – cancelReservation when not in map → "Reservation not found"
 *   P7 – init is idempotent
 *   P8 – LiveData fields are initialised after construction
 */
@ExtendWith({InstantExecutorExtension.class, MockitoExtension.class})
class ReservationViewModelTest {

    @Mock private ReservationRepository reservationRepo;
    @Mock private DatabaseReference eventsRef;

    // Firebase static mocks (listenToUserReservations calls FirebaseDatabase.getInstance)
    @Mock private FirebaseDatabase firebaseDatabase;
    @Mock private DatabaseReference dbRoot;
    @Mock private DatabaseReference reservationsDbRef;
    @Mock private Query mockQuery;

    private MockedStatic<FirebaseDatabase> firebaseMock;

    private static final String USER_ID  = "user-1";
    private static final String EVENT_ID = "evt-1";
    private static final String RES_ID   = "res-1";

    private final Event testEvent = new Event(EVENT_ID, "Jazz Night", "2026-05-01", "Music", "Montreal", 100);

    /** Test subclass that records hook calls. */
    private static class HookRecordingViewModel extends ReservationViewModel {
        final AtomicBoolean reserveHookCalled = new AtomicBoolean(false);
        final AtomicBoolean cancelHookCalled = new AtomicBoolean(false);
        final AtomicReference<String> lastReservationId = new AtomicReference<>();
        final AtomicReference<String> lastCancelId = new AtomicReference<>();

        HookRecordingViewModel(ReservationRepository repo, DatabaseReference eventsRef) {
            super(repo, eventsRef);
        }

        @Override
        protected void onReservationSuccess(Event event, String reservationId) {
            reserveHookCalled.set(true);
            lastReservationId.set(reservationId);
        }

        @Override
        protected void onCancellationSuccess(Event event, String reservationId) {
            cancelHookCalled.set(true);
            lastCancelId.set(reservationId);
        }
    }

    private HookRecordingViewModel viewModel;

    @BeforeEach
    void setUp() {
        firebaseMock = Mockito.mockStatic(FirebaseDatabase.class);
        firebaseMock.when(FirebaseDatabase::getInstance).thenReturn(firebaseDatabase);
        lenient().when(firebaseDatabase.getReference()).thenReturn(dbRoot);
        lenient().when(dbRoot.child("reservations")).thenReturn(reservationsDbRef);
        lenient().when(reservationsDbRef.orderByChild("userId")).thenReturn(mockQuery);
        lenient().when(mockQuery.equalTo(anyString())).thenReturn(mockQuery);

        viewModel = new HookRecordingViewModel(reservationRepo, eventsRef);
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
    void givenLoggedIn_whenReserveSucceeds_thenHookCalledWithReservationId_primePath1() {
        // Given
        viewModel.init(USER_ID);
        viewModel.reserveEvent(testEvent);

        ArgumentCaptor<ReservationRepository.ReservationCallback> captor =
                ArgumentCaptor.forClass(ReservationRepository.ReservationCallback.class);
        verify(reservationRepo).createReservation(eq(USER_ID), eq(EVENT_ID), captor.capture());

        // When
        captor.getValue().onSuccess(RES_ID);

        // Then
        assertEquals("Reserved successfully!", viewModel.getStatusMessage().getValue());
        assertTrue(viewModel.reserveHookCalled.get());
        assertEquals(RES_ID, viewModel.lastReservationId.get());
    }

    @Test
    void givenLoggedIn_whenReserveFails_thenHookNotCalled_primePath2() {
        // Given
        viewModel.init(USER_ID);
        viewModel.reserveEvent(testEvent);

        ArgumentCaptor<ReservationRepository.ReservationCallback> captor =
                ArgumentCaptor.forClass(ReservationRepository.ReservationCallback.class);
        verify(reservationRepo).createReservation(eq(USER_ID), eq(EVENT_ID), captor.capture());

        // When
        captor.getValue().onError("No seats available");

        // Then
        assertEquals("No seats available", viewModel.getStatusMessage().getValue());
        assertTrue(!viewModel.reserveHookCalled.get());
    }

    @Test
    void givenNotLoggedIn_whenReserve_thenNoRepoCallAndNoHook_primePath3() {
        // Given — init never called

        // When
        viewModel.reserveEvent(testEvent);

        // Then
        verify(reservationRepo, never()).createReservation(anyString(), anyString(),
                Mockito.any(ReservationRepository.ReservationCallback.class));
        assertEquals("Not logged in", viewModel.getStatusMessage().getValue());
        assertTrue(!viewModel.reserveHookCalled.get());
    }

    // ─── CANCEL ───────────────────────────────────────────────────────────────

    @Test
    void givenReservationInMap_whenCancelSucceeds_thenHookCalledWithId_primePath4() throws Exception {
        // Given
        viewModel.init(USER_ID);
        seedReservationMap(EVENT_ID, RES_ID);

        viewModel.cancelReservation(testEvent);
        ArgumentCaptor<ReservationRepository.ReservationCallback> captor =
                ArgumentCaptor.forClass(ReservationRepository.ReservationCallback.class);
        verify(reservationRepo).cancelReservation(eq(RES_ID), eq(EVENT_ID), captor.capture());

        // When
        captor.getValue().onSuccess(RES_ID);

        // Then
        assertEquals("Reservation cancelled", viewModel.getStatusMessage().getValue());
        assertTrue(viewModel.cancelHookCalled.get());
        assertEquals(RES_ID, viewModel.lastCancelId.get());
    }

    @Test
    void givenReservationInMap_whenCancelFails_thenHookNotCalled_primePath5() throws Exception {
        // Given
        viewModel.init(USER_ID);
        seedReservationMap(EVENT_ID, RES_ID);

        viewModel.cancelReservation(testEvent);
        ArgumentCaptor<ReservationRepository.ReservationCallback> captor =
                ArgumentCaptor.forClass(ReservationRepository.ReservationCallback.class);
        verify(reservationRepo).cancelReservation(eq(RES_ID), eq(EVENT_ID), captor.capture());

        // When
        captor.getValue().onError("Cancel failed");

        // Then
        assertEquals("Cancel failed", viewModel.getStatusMessage().getValue());
        assertTrue(!viewModel.cancelHookCalled.get());
    }

    @Test
    void givenReservationNotInMap_whenCancel_thenNoRepoCallAndNoHook_primePath6() {
        // Given
        viewModel.init(USER_ID);

        // When
        viewModel.cancelReservation(testEvent);

        // Then
        verify(reservationRepo, never()).cancelReservation(anyString(), anyString(),
                Mockito.any(ReservationRepository.ReservationCallback.class));
        assertEquals("Reservation not found", viewModel.getStatusMessage().getValue());
        assertTrue(!viewModel.cancelHookCalled.get());
    }

    // ─── INIT ─────────────────────────────────────────────────────────────────

    @Test
    void givenAlreadyInitialised_whenInitAgain_thenIgnored_primePath7() {
        // Given
        viewModel.init(USER_ID);

        // When — second call
        viewModel.init("other-user");

        // Then — reserve still uses first userId
        viewModel.reserveEvent(testEvent);
        ArgumentCaptor<ReservationRepository.ReservationCallback> captor =
                ArgumentCaptor.forClass(ReservationRepository.ReservationCallback.class);
        verify(reservationRepo).createReservation(eq(USER_ID), eq(EVENT_ID), captor.capture());
    }

    @Test
    void givenViewModel_whenCreated_thenLiveDataFieldsInitialised_primePath8() {
        assertNotNull(viewModel.getReservedEvents());
        assertNotNull(viewModel.getReservedEventIds());
        assertNotNull(viewModel.getStatusMessage());
    }
}
