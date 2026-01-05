package com.dw.backend.doablewellbeingbackend.it;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

public final class TestSeed {
    private TestSeed() {}

    // Determinisztikus dummy adatok tesztekhez
    private static final String DEFAULT_HASH = "hash";
    private static final byte[] DEFAULT_SALT = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };

    public static void ensureRole(JdbcTemplate jdbc, String roleName) {
        // roles(name unique)
        jdbc.update("""
            insert into roles(name)
            values (?)
            on conflict (name) do nothing
        """, roleName);
    }

    public static void assignRole(JdbcTemplate jdbc, UUID userId, String roleName) {
        // user_roles PK: (user_id, role_id)
        // Fontos: role legyen biztosan seedelve
        ensureRole(jdbc, roleName);

        jdbc.update("""
            insert into user_roles(user_id, role_id)
            select ?, r.id
            from roles r
            where r.name = ?
            on conflict (user_id, role_id) do nothing
        """, userId, roleName);
    }


    public static UUID insertUser(JdbcTemplate jdbc, String email, String passwordHash, byte[] passwordSalt, String firstName, String lastName) {
        // 1) check existing (case-insensitive)
        UUID existingId = jdbc.query(
                "select id from users where lower(email) = lower(?)",
                rs -> rs.next() ? (UUID) rs.getObject("id") : null,
                email
        );

        if (existingId != null) {
            return existingId;
        }

        // 2) insert new
        UUID id = UUID.randomUUID();
        jdbc.update("""
        insert into users(id, email, password_hash, password_salt, first_name, last_name, is_active)
        values (?, ?, ?, ?, ?, ?, true)
        """,
                id, email, passwordHash, passwordSalt, firstName, lastName
        );
        return id;
    }


    /**
     * V1 coaches tábla oszlopai:
     * - user_id uuid pk references users(id)
     * - bio text null
     * - expertise text null
     * - timezone varchar not null
     *
     * NINCS created_at oszlop -> ezért dobtad a hibát.
     */
    public static void insertCoach(JdbcTemplate jdbc, UUID userId) {
        jdbc.update("""
            insert into coaches(user_id, timezone, bio, expertise)
            values (?, ?, ?, ?)
            on conflict (user_id) do nothing
        """, userId, "Europe/London", null, null);

        // role is kell a security-hez
        assignRole(jdbc, userId, "coach");
    }

    /**
     * Nálad a "client" jelenleg role-alapú (AppointmentController: hasAnyRole('user','client')).
     * A V1/V2-ben NINCS clients tábla, szóval itt csak role-t adunk.
     */
    public static void insertClient(JdbcTemplate jdbc, UUID userId) {
        // ha nálad a tesztek "user" role-lal mennek, ezt átírhatod "user"-re
        // vagy akár mindkettőt is adhatod.
        assignRole(jdbc, userId, "user");
        // assignRole(jdbc, userId, "client");
    }

    public static void ensureCoreRoles(JdbcTemplate jdbc) {
        ensureRole(jdbc, "user");
        ensureRole(jdbc, "client");
        ensureRole(jdbc, "coach");
        ensureRole(jdbc, "admin");
    }

    public static void ensureModule(JdbcTemplate jdbc, String code, String name, String description) {
        Integer exists = jdbc.queryForObject(
                "select count(*) from modules where code = ?",
                Integer.class,
                code
        );
        if (exists != null && exists > 0) return;

        jdbc.update("""
        insert into modules (code, name, description)
        values (?, ?, ?)
    """, code, name, description);
    }
}
