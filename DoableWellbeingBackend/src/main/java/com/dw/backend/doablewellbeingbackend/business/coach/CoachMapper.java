package com.dw.backend.doablewellbeingbackend.business.coach;

import com.dw.backend.doablewellbeingbackend.domain.coach.CoachSummaryResponse;
import com.dw.backend.doablewellbeingbackend.persistence.entity.CoachEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CoachMapper {

    @Mapping(target = "id", source = "userId")
    @Mapping(target = "displayName", expression = "java(buildDisplayName(coach))")
    CoachSummaryResponse toSummary(CoachEntity coach);

    default String buildDisplayName(CoachEntity coach) {
        if (coach.getUser() == null) {
            return "Unknown coach";
        }

        var user = coach.getUser();


        String first = user.getFirstName();
        String last = user.getLastName();

        if (first == null && last == null) {
            return "Unknown coach";
        }
        if (first == null) {
            return last;
        }
        if (last == null) {
            return first;
        }
        return first + " " + last;
    }
}
