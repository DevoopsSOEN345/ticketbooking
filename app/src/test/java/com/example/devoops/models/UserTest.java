package com.example.devoops.models;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for User model class
 */
public class UserTest {
    
    private User user;
    private static final String TEST_USER_ID = "user123";
    private static final String TEST_NAME = "John Doe";
    private static final String TEST_EMAIL = "john@example.com";
    private static final String TEST_PHONE = "555-1234";

    @Before
    public void setUp() {
        user = new User(TEST_USER_ID, TEST_NAME, TEST_EMAIL, TEST_PHONE, UserRole.CUSTOMER);
    }

    @Test
    public void givenValidConstructorInput_whenUserCreated_thenAllFieldsAreSet_primePath1() {
        assertNotNull(user);
        assertEquals(TEST_USER_ID, user.getUserId());
        assertEquals(TEST_NAME, user.getName());
        assertEquals(TEST_EMAIL, user.getEmail());
        assertEquals(TEST_PHONE, user.getPhoneNumber());
        assertEquals(UserRole.CUSTOMER, user.getRole());
    }

    @Test
    public void givenUser_whenGettingUserId_thenReturnsExpectedValue() {
        assertEquals(TEST_USER_ID, user.getUserId());
    }

    @Test
    public void givenUser_whenGettingName_thenReturnsExpectedValue() {
        assertEquals(TEST_NAME, user.getName());
    }

    @Test
    public void givenUser_whenGettingEmail_thenReturnsExpectedValue() {
        assertEquals(TEST_EMAIL, user.getEmail());
    }

    @Test
    public void givenUser_whenGettingPhoneNumber_thenReturnsExpectedValue() {
        assertEquals(TEST_PHONE, user.getPhoneNumber());
    }

    @Test
    public void givenUser_whenGettingRole_thenReturnsExpectedValue() {
        assertEquals(UserRole.CUSTOMER, user.getRole());
    }

    @Test
    public void givenEmptyConstructor_whenUserCreated_thenInstanceIsNotNull_primePath2() {
        User emptyUser = new User();
        assertNotNull(emptyUser);
    }

    @Test
    public void givenAdminRole_whenUserCreated_thenRoleIsAdmin_primePath3() {
        User admin = new User("admin123", "Admin", "admin@example.com", "555-9999", UserRole.ADMIN);
        assertEquals(UserRole.ADMIN, admin.getRole());
    }

    @Test
    public void givenNullConstructorInput_whenUserCreated_thenNullableFieldsRemainNull_primePath4() {
        User nullUser = new User(null, null, null, null, null);
        assertNull(nullUser.getUserId());
        assertNull(nullUser.getName());
        assertNull(nullUser.getEmail());
    }
}


