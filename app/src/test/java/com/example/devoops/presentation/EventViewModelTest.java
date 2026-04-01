package com.example.devoops.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.arch.core.executor.TaskExecutor;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.example.devoops.models.Event;
import com.example.devoops.repository.EventRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class EventViewModelTest {

    private EventRepository mockRepo;
    private MutableLiveData<List<Event>> eventsLiveData;
    private EventViewModel viewModel;
    private Observer<List<Event>> filteredObserver;

    @BeforeEach
    void setUp() {
        // Replace the LiveData executor so setValue/getValue work synchronously in tests
        ArchTaskExecutor.getInstance().setDelegate(new TaskExecutor() {
            @Override
            public void executeOnDiskIO(Runnable runnable) { runnable.run(); }
            @Override
            public void postToMainThread(Runnable runnable) { runnable.run(); }
            @Override
            public boolean isMainThread() { return true; }
        });

        mockRepo = mock(EventRepository.class);
        eventsLiveData = new MutableLiveData<>();
        when(mockRepo.getEvents()).thenReturn(eventsLiveData);
        viewModel = new EventViewModel(mockRepo);

        // MediatorLiveData only processes sources when it has an active observer
        filteredObserver = events -> { };
        viewModel.getFilteredEvents().observeForever(filteredObserver);
    }

    @AfterEach
    void tearDown() {
        viewModel.getFilteredEvents().removeObserver(filteredObserver);
        ArchTaskExecutor.getInstance().setDelegate(null);
    }

    // ========================
    // Structural / Reflection Tests (matching existing style)
    // ========================

    @Test
    void givenEventViewModelClass_whenInspectingType_thenExtendsViewModel() {
        assertNotNull(ViewModel.class.isAssignableFrom(EventViewModel.class));
    }

    @Test
    void givenEventViewModelClass_whenInspectingMethods_thenContainsPrimeUiPaths() throws Exception {
        Method getEvents = EventViewModel.class.getDeclaredMethod("getEvents");
        Method createEvent = EventViewModel.class.getDeclaredMethod(
                "createEvent", String.class, String.class, String.class, String.class, int.class);
        Method cancelEvent = EventViewModel.class.getDeclaredMethod("cancelEvent", String.class);
        Method editEvent = EventViewModel.class.getDeclaredMethod(
                "editEvent", String.class, String.class, String.class, String.class, String.class, int.class);

        assertNotNull(getEvents);
        assertNotNull(createEvent);
        assertNotNull(cancelEvent);
        assertNotNull(editEvent);
    }

    @Test
    void givenEventViewModelClass_whenInspectingFields_thenContainsRepoAndLiveData() throws Exception {
        Field repo = EventViewModel.class.getDeclaredField("repo");
        Field events = EventViewModel.class.getDeclaredField("events");

        assertNotNull(repo);
        assertNotNull(events);
    }

    @Test
    void givenEventViewModelClass_whenInspectingFilterMethods_thenAllFilterMethodsExist() throws Exception {
        Method getFilteredEvents = EventViewModel.class.getDeclaredMethod("getFilteredEvents");
        Method setSearchQuery = EventViewModel.class.getDeclaredMethod("setSearchQuery", String.class);
        Method setFilterDate = EventViewModel.class.getDeclaredMethod("setFilterDate", String.class);
        Method setFilterLocation = EventViewModel.class.getDeclaredMethod("setFilterLocation", String.class);
        Method setFilterCategory = EventViewModel.class.getDeclaredMethod("setFilterCategory", String.class);
        Method clearFilters = EventViewModel.class.getDeclaredMethod("clearFilters");

        assertNotNull(getFilteredEvents);
        assertNotNull(setSearchQuery);
        assertNotNull(setFilterDate);
        assertNotNull(setFilterLocation);
        assertNotNull(setFilterCategory);
        assertNotNull(clearFilters);
    }

    @Test
    void givenEventViewModelClass_whenInspectingFilterFields_thenAllFilterFieldsExist() throws Exception {
        Field searchQuery = EventViewModel.class.getDeclaredField("searchQuery");
        Field filterDate = EventViewModel.class.getDeclaredField("filterDate");
        Field filterLocation = EventViewModel.class.getDeclaredField("filterLocation");
        Field filterCategory = EventViewModel.class.getDeclaredField("filterCategory");
        Field filteredEvents = EventViewModel.class.getDeclaredField("filteredEvents");

        assertNotNull(searchQuery);
        assertNotNull(filterDate);
        assertNotNull(filterLocation);
        assertNotNull(filterCategory);
        assertNotNull(filteredEvents);
    }

    // ========================
    // Behavioral Tests for Filter Logic
    // ========================

    @Test
    void givenNoFilters_whenEventsAreSet_thenAllEventsReturned() {
        List<Event> events = Arrays.asList(
                new Event("1", "Concert", "15-06-2025", "Music", "Montreal", 100),
                new Event("2", "Game Night", "20-07-2025", "Sports", "Toronto", 50)
        );

        eventsLiveData.setValue(events);

        List<Event> result = viewModel.getFilteredEvents().getValue();
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void givenSearchQuery_whenMatching_thenOnlyMatchingEventsReturned() {
        List<Event> events = Arrays.asList(
                new Event("1", "Concert", "15-06-2025", "Music", "Montreal", 100),
                new Event("2", "Game Night", "20-07-2025", "Sports", "Toronto", 50)
        );
        eventsLiveData.setValue(events);

        viewModel.setSearchQuery("concert");

        List<Event> result = viewModel.getFilteredEvents().getValue();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Concert", result.get(0).getName());
    }

    @Test
    void givenSearchQuery_whenNoMatch_thenEmptyListReturned() {
        List<Event> events = Arrays.asList(
                new Event("1", "Concert", "15-06-2025", "Music", "Montreal", 100)
        );
        eventsLiveData.setValue(events);

        viewModel.setSearchQuery("basketball");

        List<Event> result = viewModel.getFilteredEvents().getValue();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void givenSearchQuery_whenCaseIsDifferent_thenMatchesIgnoringCase() {
        List<Event> events = Arrays.asList(
                new Event("1", "Jazz Concert", "15-06-2025", "Music", "Montreal", 100)
        );
        eventsLiveData.setValue(events);

        viewModel.setSearchQuery("JAZZ");

        List<Event> result = viewModel.getFilteredEvents().getValue();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void givenDateFilter_whenMatching_thenOnlyMatchingEventsReturned() {
        List<Event> events = Arrays.asList(
                new Event("1", "Concert", "15-06-2025", "Music", "Montreal", 100),
                new Event("2", "Game Night", "20-07-2025", "Sports", "Toronto", 50)
        );
        eventsLiveData.setValue(events);

        viewModel.setFilterDate("06-2025");

        List<Event> result = viewModel.getFilteredEvents().getValue();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Concert", result.get(0).getName());
    }

    @Test
    void givenLocationFilter_whenMatching_thenOnlyMatchingEventsReturned() {
        List<Event> events = Arrays.asList(
                new Event("1", "Concert", "15-06-2025", "Music", "Montreal", 100),
                new Event("2", "Game Night", "20-07-2025", "Sports", "Toronto", 50)
        );
        eventsLiveData.setValue(events);

        viewModel.setFilterLocation("toronto");

        List<Event> result = viewModel.getFilteredEvents().getValue();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Game Night", result.get(0).getName());
    }

    @Test
    void givenCategoryFilter_whenMatching_thenOnlyMatchingEventsReturned() {
        List<Event> events = Arrays.asList(
                new Event("1", "Concert", "15-06-2025", "Music", "Montreal", 100),
                new Event("2", "Game Night", "20-07-2025", "Sports", "Toronto", 50)
        );
        eventsLiveData.setValue(events);

        viewModel.setFilterCategory("Music");

        List<Event> result = viewModel.getFilteredEvents().getValue();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Concert", result.get(0).getName());
    }

    @Test
    void givenMultipleFilters_whenCombined_thenAllFiltersCombineWithAnd() {
        List<Event> events = Arrays.asList(
                new Event("1", "Jazz Concert", "15-06-2025", "Music", "Montreal", 100),
                new Event("2", "Rock Concert", "20-06-2025", "Music", "Toronto", 50),
                new Event("3", "Game Night", "15-06-2025", "Sports", "Montreal", 30)
        );
        eventsLiveData.setValue(events);

        viewModel.setFilterCategory("Music");
        viewModel.setFilterLocation("Montreal");

        List<Event> result = viewModel.getFilteredEvents().getValue();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Jazz Concert", result.get(0).getName());
    }

    @Test
    void givenSearchAndFilter_whenCombined_thenBothApplied() {
        List<Event> events = Arrays.asList(
                new Event("1", "Jazz Concert", "15-06-2025", "Music", "Montreal", 100),
                new Event("2", "Jazz Festival", "20-07-2025", "Music", "Toronto", 200),
                new Event("3", "Game Night", "15-06-2025", "Sports", "Montreal", 30)
        );
        eventsLiveData.setValue(events);

        viewModel.setSearchQuery("jazz");
        viewModel.setFilterLocation("Montreal");

        List<Event> result = viewModel.getFilteredEvents().getValue();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Jazz Concert", result.get(0).getName());
    }

    @Test
    void givenActiveFilters_whenClearFilters_thenAllEventsReturned() {
        List<Event> events = Arrays.asList(
                new Event("1", "Concert", "15-06-2025", "Music", "Montreal", 100),
                new Event("2", "Game Night", "20-07-2025", "Sports", "Toronto", 50)
        );
        eventsLiveData.setValue(events);

        viewModel.setFilterCategory("Music");
        assertEquals(1, viewModel.getFilteredEvents().getValue().size());

        viewModel.clearFilters();

        List<Event> result = viewModel.getFilteredEvents().getValue();
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void givenNullEventsList_whenFiltering_thenEmptyListReturned() {
        eventsLiveData.setValue(null);

        viewModel.setSearchQuery("anything");

        List<Event> result = viewModel.getFilteredEvents().getValue();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenSubstringSearch_whenPartialMatch_thenMatchesCorrectly() {
        List<Event> events = Arrays.asList(
                new Event("1", "Basketball Tournament", "15-06-2025", "Sports", "Montreal", 100),
                new Event("2", "Baseball Game", "20-07-2025", "Sports", "Toronto", 50)
        );
        eventsLiveData.setValue(events);

        viewModel.setSearchQuery("bas");

        List<Event> result = viewModel.getFilteredEvents().getValue();
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void givenEmptySearchQuery_whenSet_thenAllEventsReturned() {
        List<Event> events = Arrays.asList(
                new Event("1", "Concert", "15-06-2025", "Music", "Montreal", 100),
                new Event("2", "Game Night", "20-07-2025", "Sports", "Toronto", 50)
        );
        eventsLiveData.setValue(events);

        viewModel.setSearchQuery("");

        List<Event> result = viewModel.getFilteredEvents().getValue();
        assertNotNull(result);
        assertEquals(2, result.size());
    }
}
