package com.example.devoops.notification;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.example.devoops.models.Event;
import com.example.devoops.models.User;
import com.example.devoops.models.UserRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ExecutorService;

/**
 * Prime path coverage for {@link AsyncNotificationService} (Decorator).
 *
 * P1 – sendReservationConfirmation delegates to wrapped service via executor
 * P2 – sendCancellationConfirmation delegates to wrapped service via executor
 * P3 – executor.execute is invoked (task dispatched)
 * P4 – one-arg constructor creates default executor (not null)
 */
@ExtendWith(MockitoExtension.class)
class AsyncNotificationServiceTest {

    @Mock private NotificationService delegate;

    /** Runs tasks on the calling thread for deterministic assertions. */
    private final ExecutorService syncExecutor = new ExecutorService() {
        @Override public void execute(Runnable r) { r.run(); }
        @Override public void shutdown() {}
        @Override public java.util.List<Runnable> shutdownNow() { return java.util.Collections.emptyList(); }
        @Override public boolean isShutdown() { return false; }
        @Override public boolean isTerminated() { return false; }
        @Override public boolean awaitTermination(long t, java.util.concurrent.TimeUnit u) { return true; }
        @Override public <T> java.util.concurrent.Future<T> submit(java.util.concurrent.Callable<T> c) { return null; }
        @Override public <T> java.util.concurrent.Future<T> submit(Runnable r, T v) { return null; }
        @Override public java.util.concurrent.Future<?> submit(Runnable r) { return null; }
        @Override public <T> java.util.List<java.util.concurrent.Future<T>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<T>> c) { return null; }
        @Override public <T> java.util.List<java.util.concurrent.Future<T>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<T>> c, long t, java.util.concurrent.TimeUnit u) { return null; }
        @Override public <T> T invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<T>> c) { return null; }
        @Override public <T> T invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<T>> c, long t, java.util.concurrent.TimeUnit u) { return null; }
    };

    private AsyncNotificationService service;

    private final User user = new User("u1", "Alice", "a@test.com", "+1234", UserRole.CUSTOMER);
    private final Event event = new Event("e1", "Jazz", "2026-05-01", "Music", "MTL", 100);

    @BeforeEach
    void setUp() {
        service = new AsyncNotificationService(delegate, syncExecutor);
    }

    @Test
    void givenDelegate_whenReservationConfirmed_thenDelegateCalled_primePath1() {
        // When
        service.sendReservationConfirmation(user, event, "res-1");

        // Then
        verify(delegate).sendReservationConfirmation(eq(user), eq(event), eq("res-1"));
    }

    @Test
    void givenDelegate_whenCancellationConfirmed_thenDelegateCalled_primePath2() {
        // When
        service.sendCancellationConfirmation(user, event, "res-1");

        // Then
        verify(delegate).sendCancellationConfirmation(eq(user), eq(event), eq("res-1"));
    }

    @Test
    void givenSpyExecutor_whenReservationConfirmed_thenExecutorReceivesTask_primePath3() {
        // Given
        ExecutorService spyExecutor = Mockito.spy(syncExecutor);
        AsyncNotificationService svc = new AsyncNotificationService(delegate, spyExecutor);

        // When
        svc.sendReservationConfirmation(user, event, "res-1");

        // Then
        verify(spyExecutor).execute(any(Runnable.class));
    }

    @Test
    void givenOneArgConstructor_whenCreated_thenServiceIsNotNull_primePath4() {
        // When / Then
        assertNotNull(new AsyncNotificationService(delegate));
    }

    @Test
    void givenDelegate_whenCancellationConfirmed_thenExecutorReceivesTask_primePath5() {
        // Given
        ExecutorService spyExecutor = Mockito.spy(syncExecutor);
        AsyncNotificationService svc = new AsyncNotificationService(delegate, spyExecutor);

        // When
        svc.sendCancellationConfirmation(user, event, "res-1");

        // Then
        verify(spyExecutor).execute(any(Runnable.class));
        verify(delegate).sendCancellationConfirmation(eq(user), eq(event), eq("res-1"));
    }
}
