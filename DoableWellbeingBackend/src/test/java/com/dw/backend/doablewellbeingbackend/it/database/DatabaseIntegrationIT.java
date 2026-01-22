package com.dw.backend.doablewellbeingbackend.it.database;

import com.dw.backend.doablewellbeingbackend.it.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseIntegrationIT extends IntegrationTestBase {

    @Autowired JdbcTemplate jdbc;

    @Test
    void flyway_migrations_applied_and_no_failed_migrations() {
        Integer total = jdbc.queryForObject(
                "select count(*) from flyway_schema_history",
                Integer.class
        );
        assertThat(total).isNotNull();
        assertThat(total).isGreaterThan(0);

        Integer failed = jdbc.queryForObject(
                "select count(*) from flyway_schema_history where success = false",
                Integer.class
        );
        assertThat(failed).isEqualTo(0);
    }

    @Test
    void core_tables_exist() {
        assertThat(tableExists("users")).isTrue();
        assertThat(tableExists("roles")).isTrue();
        assertThat(tableExists("user_roles")).isTrue();
        assertThat(tableExists("coaches")).isTrue();
        assertThat(tableExists("appointments")).isTrue();
    }

    @Test
    void critical_indexes_exist() {
        assertThat(indexExists("ux_users_email_lower")).isTrue();
        assertThat(indexExists("ix_appt_coach_start")).isTrue();
        assertThat(indexExists("ix_appt_client_start")).isTrue();
        assertThat(indexExists("ix_appt_coach_status_start")).isTrue();
    }

    @Test
    void users_email_is_unique_case_insensitive() {
        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();

        insertUser(u1, "Test@Example.com", "A", "B");
        assertThatThrownBy(() -> insertUser(u2, "test@example.com", "C", "D"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void appointments_check_constraint_ends_after_starts_is_enforced() {
        UUID coachId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        insertUser(coachId, "coach_it_db@test.com", "Coach", "Db");
        insertUser(clientId, "client_it_db@test.com", "Client", "Db");

        jdbc.update(
                "insert into coaches(user_id, bio, expertise, timezone) values (?, ?, ?, ?)",
                coachId, "bio", "expertise", "UTC"
        );

        OffsetDateTime start = OffsetDateTime.now().plusDays(2).withSecond(0).withNano(0);
        OffsetDateTime endSameOrBefore = start;

        assertThatThrownBy(() -> jdbc.update(
                "insert into appointments(id, coach_id, client_id, starts_at, ends_at, status) values (?, ?, ?, ?, ?, 'scheduled')",
                UUID.randomUUID(), coachId, clientId, start, endSameOrBefore
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deleting_coach_user_is_restricted_when_appointment_exists() {
        UUID coachId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        insertUser(coachId, "coach_delete_block@test.com", "Coach", "Block");
        insertUser(clientId, "client_delete_block@test.com", "Client", "Block");

        jdbc.update(
                "insert into coaches(user_id, bio, expertise, timezone) values (?, ?, ?, ?)",
                coachId, "bio", "expertise", "UTC"
        );

        OffsetDateTime start = OffsetDateTime.now().plusDays(3).withSecond(0).withNano(0);
        OffsetDateTime end = start.plusMinutes(60);

        jdbc.update(
                "insert into appointments(id, coach_id, client_id, starts_at, ends_at, status) values (?, ?, ?, ?, ?, 'scheduled')",
                UUID.randomUUID(), coachId, clientId, start, end
        );

        // deleting coach user would cascade delete coaches row, but appointments has FK to coaches with ON DELETE RESTRICT
        assertThatThrownBy(() -> jdbc.update("delete from users where id = ?", coachId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // ----------------------------
    // helpers
    // ----------------------------
    private boolean tableExists(String tableName) {
        // looks in current schema search_path (usually public)
        Boolean exists = jdbc.queryForObject(
                "select to_regclass(?) is not null",
                Boolean.class,
                tableName
        );
        return exists != null && exists;
    }

    private boolean indexExists(String indexName) {
        Boolean exists = jdbc.queryForObject(
                "select exists (select 1 from pg_indexes where indexname = ?)",
                Boolean.class,
                indexName
        );
        return exists != null && exists;
    }

    private void insertUser(UUID id, String email, String firstName, String lastName) {
        // users.password_salt is bytea NOT NULL, users.password_hash NOT NULL
        // keep it minimal for schema-level tests
        jdbc.update("""
            insert into users(
                id, email, password_hash, password_salt, first_name, last_name,
                is_active, is_deleted, created_at, deleted_at
            ) values (
                ?, ?, ?, decode('01','hex'), ?, ?,
                true, false, now(), null
            )
        """, id, email, "x", firstName, lastName);
    }
}
