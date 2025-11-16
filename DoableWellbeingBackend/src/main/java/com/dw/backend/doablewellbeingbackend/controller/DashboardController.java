package com.dw.backend.doablewellbeingbackend.controller;

import com.dw.backend.doablewellbeingbackend.domain.dashboard.DashboardDto;
import com.dw.backend.doablewellbeingbackend.domain.dashboard.LayoutsDto;
import com.dw.backend.doablewellbeingbackend.domain.dashboard.WidgetDto;
import com.dw.backend.doablewellbeingbackend.presistence.entity.UserModuleLayoutsEntity;
import com.dw.backend.doablewellbeingbackend.presistence.entity.UserWidgetEntity;
import com.dw.backend.doablewellbeingbackend.presistence.impl.UserModuleLayoutsRepository;
import com.dw.backend.doablewellbeingbackend.presistence.impl.UserWidgetRepository;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.service.RequestBodyService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserWidgetRepository widgetRepo;
    private final UserModuleLayoutsRepository layoutRepo;
    private final RequestBodyService requestBodyBuilder;

    private UUID currentUserID(Jwt jwt) {
        UUID userID = UUID.fromString(jwt.getSubject());
        return userID;
    }


    @GetMapping
    public DashboardDto get(@AuthenticationPrincipal Jwt jwt) {
        UUID user_id = currentUserID(jwt);
        var layout = layoutRepo.findByUserIdAndName(user_id, "default")
                .orElseGet(() -> {
                    var e = new UserModuleLayoutsEntity();
                    e.setUserId(user_id); e.setName("default");
                    return layoutRepo.save(e);
                });
        var widgets = widgetRepo.findByUserIdAndActiveIsTrueOrderByCreatedAtAsc(user_id)
                .stream().map(w -> new WidgetDto(
                        w.getId(), w.getType(), w.getTitle(), w.getSettings()
                )).toList();

        return new DashboardDto("default",
                new LayoutsDto(
                        layout.getGridLg(), layout.getGridMd(), layout.getGridSm(),
                        layout.getGridXs(), layout.getGridXxs()
                ), widgets);
    }

    @PutMapping
    public DashboardDto put(@AuthenticationPrincipal Jwt jwt, @RequestBody DashboardDto body) {
        UUID user_id = currentUserID(jwt);
        var layout = layoutRepo.findByUserIdAndName(user_id, body.name())
                .orElseGet(() -> {
                    var e = new UserModuleLayoutsEntity();
                    e.setUserId(user_id); e.setName(body.name());
                    return e;
                });
        var l = body.layouts();
        layout.setGridLg(Optional.ofNullable(l.lg()).orElse("[]"));
        layout.setGridMd(Optional.ofNullable(l.md()).orElse("[]"));
        layout.setGridSm(Optional.ofNullable(l.sm()).orElse("[]"));
        layout.setGridXs(Optional.ofNullable(l.xs()).orElse("[]"));
        layout.setGridXxs(Optional.ofNullable(l.xxs()).orElse("[]"));
        layoutRepo.save(layout);

        var existing = widgetRepo.findByUserIdAndActiveIsTrueOrderByCreatedAtAsc(user_id)
                .stream().collect(Collectors.toMap(UserWidgetEntity::getId, Function.identity()));

        Set<UUID> seen = new HashSet<>();
        List<WidgetDto> normalized = new ArrayList<>();
        for(var w : body.widgets()) {
            UserWidgetEntity e = w.id() != null ? existing.get(w.id()) : null;
            if(e == null) e = new UserWidgetEntity();
            e.setUserId(user_id);
            e.setType(w.type());
            e.setTitle(w.title());
            e.setSettings(Optional.ofNullable(w.settings()).orElse("{}"));
            e.setActive(true);
            e = widgetRepo.save(e);
            seen.add(e.getId());
            normalized.add(new WidgetDto(e.getId(), e.getType(), e.getTitle(), e.getSettings()));
        }
        for (var e : existing.values()){
            if (!seen.contains(e.getId())) {
                e.setActive(false);
                widgetRepo.save(e);
            }
        }
        return new DashboardDto(layout.getName(),
                new LayoutsDto(layout.getGridLg(), layout.getGridMd(), layout.getGridSm(),
                        layout.getGridXs(), layout.getGridXxs()),
                normalized);
    }
}
