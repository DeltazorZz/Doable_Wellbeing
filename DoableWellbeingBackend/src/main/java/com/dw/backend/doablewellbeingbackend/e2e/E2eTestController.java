package com.dw.backend.doablewellbeingbackend.e2e;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Profile("e2e")
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class E2eTestController {

    private final JdbcTemplate jdbc;

    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void reset() {


        jdbc.execute("""
            truncate table
              appointments,
              coach_availabilities,
              dashboard_widgets,
              dashboards,
              user_roles,
              coaches,
              users,
              roles
            restart identity cascade
        """);


        ensureRole("user");
        ensureRole("client");
        ensureRole("coach");
        ensureRole("admin");
    }

    private void ensureRole(String name) {
        jdbc.update("""
            insert into roles(name)
            values (?)
            on conflict (name) do nothing
        """, name);
    }
}
