package com.example.devoops.models;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for UserRole enum
 */
public class UserRoleTest {

    @Test
    public void givenEnumValues_whenFetched_thenContainsCustomerAndAdmin_primePath1() {
        UserRole[] roles = UserRole.values();
        assertEquals(2, roles.length);
        assertTrue(contains(roles, UserRole.CUSTOMER));
        assertTrue(contains(roles, UserRole.ADMIN));
    }

    @Test
    public void givenCustomerRole_whenRead_thenNameIsCustomer() {
        UserRole role = UserRole.CUSTOMER;
        assertNotNull(role);
        assertEquals("CUSTOMER", role.name());
    }

    @Test
    public void givenAdminRole_whenRead_thenNameIsAdmin() {
        UserRole role = UserRole.ADMIN;
        assertNotNull(role);
        assertEquals("ADMIN", role.name());
    }

    @Test
    public void givenRoleName_whenValueOfCalled_thenReturnsMatchingEnum_primePath2() {
        UserRole customer = UserRole.valueOf("CUSTOMER");
        UserRole admin = UserRole.valueOf("ADMIN");
        
        assertEquals(UserRole.CUSTOMER, customer);
        assertEquals(UserRole.ADMIN, admin);
    }

    @Test
    public void givenTwoRoles_whenCompared_thenEqualityMatchesExpected_primePath3() {
        assertTrue(UserRole.CUSTOMER.equals(UserRole.CUSTOMER));
        assertFalse(UserRole.CUSTOMER.equals(UserRole.ADMIN));
    }

    private boolean contains(UserRole[] roles, UserRole target) {
        for (UserRole role : roles) {
            if (role.equals(target)) {
                return true;
            }
        }
        return false;
    }
}


