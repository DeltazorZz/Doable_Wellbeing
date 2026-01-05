package com.dw.backend.doablewellbeingbackend.business.user;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AppUserImplTest {

    // -------------------------------------------------------------------------
    // constructor behavior
    // -------------------------------------------------------------------------

    @Test
    void constructor_clonesPasswordSalt_defensiveCopy() {
        byte[] salt = new byte[]{1, 2, 3};

        AppUserImpl user = new AppUserImpl(
                UUID.randomUUID(),
                "test@example.com",
                "John",
                "Doe",
                "hash",
                salt,
                true,
                false,
                List.of("USER")
        );

        // mutate original array
        salt[0] = 9;

        byte[] stored = user.getPasswordSalt();
        assertNotNull(stored);
        assertEquals(1, stored[0], "internal salt must not change when original array is modified");
    }

    @Test
    void constructor_rolesNull_becomesEmptyImmutableList() {
        AppUserImpl user = new AppUserImpl(
                UUID.randomUUID(),
                "test@example.com",
                "John",
                "Doe",
                "hash",
                null,
                true,
                false,
                null
        );

        assertNotNull(user.getRoleNames());
        assertTrue(user.getRoleNames().isEmpty());
        assertThrows(UnsupportedOperationException.class, () ->
                user.getRoleNames().add("ADMIN")
        );
    }

    @Test
    void constructor_rolesCopiedAndImmutable() {
        List<String> roles = List.of("USER", "COACH");

        AppUserImpl user = new AppUserImpl(
                UUID.randomUUID(),
                "test@example.com",
                "John",
                "Doe",
                "hash",
                null,
                true,
                false,
                roles
        );

        assertEquals(2, user.getRoleNames().size());
        assertTrue(user.getRoleNames().contains("USER"));

        // immutability
        assertThrows(UnsupportedOperationException.class, () ->
                user.getRoleNames().add("ADMIN")
        );
    }

    // -------------------------------------------------------------------------
    // getters defensive behavior
    // -------------------------------------------------------------------------

    @Test
    void getPasswordSalt_returnsClone_notInternalArray() {
        byte[] salt = new byte[]{5, 6, 7};

        AppUserImpl user = new AppUserImpl(
                UUID.randomUUID(),
                "test@example.com",
                "John",
                "Doe",
                "hash",
                salt,
                true,
                false,
                List.of("USER")
        );

        byte[] returned = user.getPasswordSalt();
        returned[0] = 99;

        byte[] again = user.getPasswordSalt();
        assertEquals(5, again[0], "modifying returned salt must not affect internal state");
    }

    @Test
    void getPasswordSalt_null_whenNoSaltProvided() {
        AppUserImpl user = new AppUserImpl(
                UUID.randomUUID(),
                "test@example.com",
                "John",
                "Doe",
                "hash",
                null,
                true,
                false,
                List.of("USER")
        );

        assertNull(user.getPasswordSalt());
    }

    // -------------------------------------------------------------------------
    // basic getters
    // -------------------------------------------------------------------------

    @Test
    void getters_returnProvidedValues() {
        UUID id = UUID.randomUUID();

        AppUserImpl user = new AppUserImpl(
                id,
                "test@example.com",
                "John",
                "Doe",
                "hash123",
                null,
                true,
                false,
                List.of("USER")
        );

        assertEquals(id, user.getId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("hash123", user.getPasswordHash());
        assertTrue(user.isActive());
        assertFalse(user.isDeleted());
    }
}
