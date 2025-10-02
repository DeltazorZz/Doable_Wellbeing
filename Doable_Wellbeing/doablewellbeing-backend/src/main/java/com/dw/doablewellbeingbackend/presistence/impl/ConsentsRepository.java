package com.dw.doablewellbeingbackend.presistence.impl;

import com.dw.doablewellbeingbackend.domain.selfreferral.CreateSelfReferralRequest;
import com.dw.doablewellbeingbackend.presistence.entity.ConsentEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConsentsRepository extends JpaRepository<ConsentEntity, UUID> {

    @Modifying
    @Query(value = """
      INSERT INTO consents (id,user_id,type,granted_at)
      VALUES (gen_random_uuid(), :uid, :type, now())
    """, nativeQuery = true)
    void grant(@Param("uid") UUID userId, @Param("type") String type);

    @Modifying
    @Query(value = """
      UPDATE consents SET revoked_at = now()
      WHERE user_id=:uid AND type=:type AND revoked_at IS NULL
    """, nativeQuery = true)
    void revoke(@Param("uid") UUID userId, @Param("type") String type);

    default void syncCommunicationConsents(UUID userId, CreateSelfReferralRequest r) {
        sync(userId, "mobile_contact", r.getConsentMobileContact());
        sync(userId, "sms_reminders", r.getConsentSmsReminders());
        sync(userId, "mobile_voicemail", r.getConsentMobileVoicemail());
        sync(userId, "home_voicemail", r.getConsentHomeVoicemail());
        sync(userId, "email_contact", r.getConsentEmailContact());
    }
    private void sync(UUID uid, String type, boolean nowValue) {
        if (nowValue) grant(uid, type); else revoke(uid, type);
    }

}
