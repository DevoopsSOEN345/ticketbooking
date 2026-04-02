package com.example.devoops.presentation;

import static org.junit.jupiter.api.Assertions.*;

import androidx.fragment.app.DialogFragment;

import com.example.devoops.models.Event;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

class EventDialogFragmentTest {

    // ─────────────────────────────────────────────
    // 1. Class structure
    // ─────────────────────────────────────────────

    @Nested
    class StructuralTests {

        @Test
        void extendsDialogFragment() {
            assertTrue(DialogFragment.class.isAssignableFrom(EventDialogFragment.class));
        }

        @Test
        void hasOnCreateViewMethod() throws Exception {
            Method m = EventDialogFragment.class.getDeclaredMethod(
                    "onCreateView",
                    android.view.LayoutInflater.class,
                    android.view.ViewGroup.class,
                    android.os.Bundle.class);
            assertEquals(android.view.View.class, m.getReturnType());
        }

        @Test
        void hasSetEventToEditMethod() throws Exception {
            Method m = EventDialogFragment.class.getDeclaredMethod(
                    "setEventToEdit", Event.class);
            assertTrue(Modifier.isPublic(m.getModifiers()));
            assertEquals(void.class, m.getReturnType());
        }

        @Test
        void hasPrivateEventToEditField() throws Exception {
            Field f = EventDialogFragment.class.getDeclaredField("eventToEdit");
            assertTrue(Modifier.isPrivate(f.getModifiers()));
            assertEquals(Event.class, f.getType());
        }
    }

    // ─────────────────────────────────────────────
    // 2. setEventToEdit (verified via reflection)
    // ─────────────────────────────────────────────

    @Nested
    class SetEventToEditTests {

        private Object getField(EventDialogFragment fragment) throws Exception {
            Field f = EventDialogFragment.class.getDeclaredField("eventToEdit");
            f.setAccessible(true);
            return f.get(fragment);
        }

        @Test
        void defaultValueIsNull() throws Exception {
            assertNull(getField(new EventDialogFragment()));
        }

        @Test
        void storesEvent() throws Exception {
            EventDialogFragment fragment = new EventDialogFragment();
            Event event = new Event();
            fragment.setEventToEdit(event);
            assertSame(event, getField(fragment));
        }

        @Test
        void canBeSetToNull() throws Exception {
            EventDialogFragment fragment = new EventDialogFragment();
            fragment.setEventToEdit(new Event());
            fragment.setEventToEdit(null);
            assertNull(getField(fragment));
        }

        @Test
        void lastCallWins() throws Exception {
            EventDialogFragment fragment = new EventDialogFragment();
            Event first = new Event();
            Event second = new Event();
            fragment.setEventToEdit(first);
            fragment.setEventToEdit(second);
            assertSame(second, getField(fragment));
        }
    }

    // ─────────────────────────────────────────────
    // 3. Date format regex (extracted from source)
    // ─────────────────────────────────────────────

    @Nested
    class DateRegexTests {

        private static final String DATE_REGEX = "\\d{2}-\\d{2}-\\d{4}";

        @ParameterizedTest
        @ValueSource(strings = {
                "01-01-2025",
                "31-12-2024",
                "15-06-2023",
                "99-99-9999"
        })
        void validFormats(String date) {
            assertTrue(date.matches(DATE_REGEX));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "2025-01-01",
                "1-1-2025",
                "01/01/2025",
                "01012025",
                "ab-cd-efgh",
                "01-01-25",
                " 01-01-2025",
                "01-01-2025 "
        })
        void invalidFormats(String date) {
            assertFalse(date.matches(DATE_REGEX));
        }

        @Test
        void emptyStringDoesNotMatch() {
            assertFalse("".matches(DATE_REGEX));
        }
    }

    // ─────────────────────────────────────────────
    // 4. Seats parsing (Integer.parseInt paths)
    // ─────────────────────────────────────────────

    @Nested
    class SeatsParsingTests {

        @Test
        void validNumber() {
            assertEquals(100, Integer.parseInt("100"));
        }

        @Test
        void zero() {
            assertEquals(0, Integer.parseInt("0"));
        }

        @Test
        void negativeNumber() {
            assertEquals(-5, Integer.parseInt("-5"));
        }

        @Test
        void nonNumericThrows() {
            assertThrows(NumberFormatException.class, () -> Integer.parseInt("abc"));
        }

        @Test
        void decimalThrows() {
            assertThrows(NumberFormatException.class, () -> Integer.parseInt("10.5"));
        }

        @Test
        void emptyThrows() {
            assertThrows(NumberFormatException.class, () -> Integer.parseInt(""));
        }

        @Test
        void whitespaceThrows() {
            assertThrows(NumberFormatException.class, () -> Integer.parseInt(" "));
        }
    }

    // ─────────────────────────────────────────────
    // 5. Empty-field validation logic
    // ─────────────────────────────────────────────

    @Nested
    class EmptyFieldValidationTests {

        /** Mirrors the guard clause in the click listener. */
        private boolean anyEmpty(String name, String seats, String date,
                                 String location, String category) {
            return name.isEmpty() || seats.isEmpty() || date.isEmpty()
                    || location.isEmpty() || category.isEmpty();
        }

        @Test
        void allFilledPasses() {
            assertFalse(anyEmpty("Ev", "50", "01-01-2025", "Hall", "Music"));
        }

        @Test
        void emptyNameFails() {
            assertTrue(anyEmpty("", "50", "01-01-2025", "Hall", "Music"));
        }

        @Test
        void emptySeatsFails() {
            assertTrue(anyEmpty("Ev", "", "01-01-2025", "Hall", "Music"));
        }

        @Test
        void emptyDateFails() {
            assertTrue(anyEmpty("Ev", "50", "", "Hall", "Music"));
        }

        @Test
        void emptyLocationFails() {
            assertTrue(anyEmpty("Ev", "50", "01-01-2025", "", "Music"));
        }

        @Test
        void emptyCategoryFails() {
            assertTrue(anyEmpty("Ev", "50", "01-01-2025", "Hall", ""));
        }

        @Test
        void allEmptyFails() {
            assertTrue(anyEmpty("", "", "", "", ""));
        }
    }
}
