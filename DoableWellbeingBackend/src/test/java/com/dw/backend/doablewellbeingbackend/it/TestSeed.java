package com.dw.backend.doablewellbeingbackend.it;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

public final class TestSeed {
    private TestSeed() {}


    private static final String DEFAULT_HASH = "hash";
    private static final byte[] DEFAULT_SALT = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };

    public static void ensureRole(JdbcTemplate jdbc, String roleName) {

        jdbc.update("""
            insert into roles(name)
            values (?)
            on conflict (name) do nothing
        """, roleName);
    }

    public static void assignRole(JdbcTemplate jdbc, UUID userId, String roleName) {

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

        UUID existingId = jdbc.query(
                "select id from users where lower(email) = lower(?)",
                rs -> rs.next() ? (UUID) rs.getObject("id") : null,
                email
        );

        if (existingId != null) {
            return existingId;
        }


        UUID id = UUID.randomUUID();
        jdbc.update("""
        insert into users(id, email, password_hash, password_salt, first_name, last_name, is_active)
        values (?, ?, ?, ?, ?, ?, true)
        """,
                id, email, passwordHash, passwordSalt, firstName, lastName
        );
        return id;
    }



    public static void insertCoach(JdbcTemplate jdbc, UUID userId) {
        jdbc.update("""
            insert into coaches(user_id, timezone, bio, expertise)
            values (?, ?, ?, ?)
            on conflict (user_id) do nothing
        """, userId, "Europe/London", null, null);


        assignRole(jdbc, userId, "coach");
    }


    public static void insertClient(JdbcTemplate jdbc, UUID userId) {

        assignRole(jdbc, userId, "user");

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
