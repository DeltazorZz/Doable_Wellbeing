package com.dw.backend.doablewellbeingbackend.persistence.impl;


import com.dw.backend.doablewellbeingbackend.domain.enums.AppointmentStatus;
import com.dw.backend.doablewellbeingbackend.persistence.entity.AppointmentEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID> {
    @Query("""
        SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END
        FROM AppointmentEntity e
        WHERE e.coachId = :coachId
        AND e.status IN ('requested','scheduled') 
        AND (
            (e.startsAt < :endsAt AND e.endsAt > :startsAt)
        )
    """)
    boolean existsOverlap(
            @Param("coachId") UUID coachId,
            @Param("startsAt") OffsetDateTime startsAt,
            @Param("endsAt") OffsetDateTime endsAt
    );

    List<AppointmentEntity> findAllByClientIdOrderByStartsAtDesc(UUID clientId);
    List<AppointmentEntity> findAllByCoachIdOrderByStartsAtDesc(UUID coachId);

    boolean existsByCoachIdAndClientIdAndStatusIn(
            UUID coachId,
            UUID clientId,
            Collection<AppointmentStatus> statuses
    );

    List<AppointmentEntity> findByCoachIdAndStartsAtBetween(
            UUID coachId,
            OffsetDateTime from,
            OffsetDateTime to
    );

    List<AppointmentEntity> findByClientIdOrderByStartsAtDesc(UUID clientId);

    List<AppointmentEntity> findByCoachIdOrderByStartsAtDesc(UUID coachId);

    List<AppointmentEntity> findByCoachIdAndStartsAtBetweenOrderByStartsAtAsc(
            UUID coachId,
            OffsetDateTime from,
            OffsetDateTime to
    );

    @Query("""
    select
        a.id as id,
        a.startsAt as startsAt,
        a.endsAt as endsAt,
        a.status as status,
        a.meetingUrl as meetingUrl,
        a.externalCalendarId as externalCalendarId,
        u.id as clientId,
        concat(u.firstName, ' ', u.lastName) as clientName,
        u.email as clientEmail,
        a.notes as notes
    from AppointmentEntity a
    join UserEntity u on u.id = a.clientId
    where a.coachId = :coachId
      and a.startsAt >= :from
      and a.endsAt <= :to
""")
    List<CoachCalendarProjection> findCoachCalendar(
            UUID coachId,
            OffsetDateTime from,
            OffsetDateTime to
    );


    @Query("""
  select a from AppointmentEntity a
  where a.clientId = :clientId
    and a.startsAt >= :from
    and a.startsAt < :to
    and (a.status = 'requested' or a.status = 'scheduled')
  order by a.startsAt asc
""")
    List<AppointmentEntity> findUpcomingForClient(UUID clientId, OffsetDateTime from, OffsetDateTime to);

    @Query("""
    select a from AppointmentEntity a
    where a.clientId = :clientId
      and a.status = 'completed'
    order by a.startsAt desc
  """)
    List<AppointmentEntity> findCompletedForClient(UUID clientId, PageRequest pageable);
}