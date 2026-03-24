package com.example.devoops;

import androidx.appcompat.app.AppCompatActivity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainActivityTest {

    @Test
    public void givenMainActivityClass_whenInspecting_thenHasExpectedClassName() {
        assertEquals("com.example.devoops.MainActivity", MainActivity.class.getName());
    }

    @Test
    public void givenMainActivityClass_whenInspectingType_thenExtendsAppCompatActivity_primePath1() {
        assertTrue(AppCompatActivity.class.isAssignableFrom(MainActivity.class));
    }

    @Test
    public void givenMainActivityClass_whenInspectingMethods_thenContainsOnCreate_primePath2() throws Exception {
        Method onCreate = MainActivity.class.getDeclaredMethod("onCreate", android.os.Bundle.class);
        assertNotNull(onCreate);
    }

    @Test
    public void givenMainActivityClass_whenInspectingPackage_thenPackageIsExpected_primePath3() {
        assertEquals("com.example.devoops", MainActivity.class.getPackage().getName());
    }

    @Test
    public void givenMainActivityClass_whenReadingSimpleName_thenSimpleNameMatches_primePath4() {
        assertEquals("MainActivity", MainActivity.class.getSimpleName());
    }

    @Test
    public void givenMainActivityClass_whenLoading_thenClassReferenceIsNotNull_primePath5() {
        assertNotNull(MainActivity.class);
    }
}



