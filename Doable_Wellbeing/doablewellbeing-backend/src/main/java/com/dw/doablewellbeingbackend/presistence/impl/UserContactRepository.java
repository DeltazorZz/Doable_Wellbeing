package com.dw.doablewellbeingbackend.presistence.impl;

import com.dw.doablewellbeingbackend.presistence.entity.UserContactEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserContactRepository extends JpaRepository<UserContactEntity, UUID> {
    @Modifying
    @Query(value = """
       DELETE FROM user_contacts WHERE user_id=:uid AND type='email' AND is_primary=true
    """, nativeQuery = true)
    void removePrimaryEmail(@Param("uid") UUID userId);

    @Modifying
    @Query(value = """
       INSERT INTO user_contacts (id,user_id,type,value,consent_contact,consent_sms,consent_voicemail,is_primary,created_at)
       VALUES (gen_random_uuid(), :uid,'email',:email,false,false,false,true, now())
       ON CONFLICT (user_id, type, value) DO UPDATE SET is_primary=EXCLUDED.is_primary
    """, nativeQuery = true)
    void insertPrimaryEmail(@Param("uid") UUID userId, @Param("email") String email);

    default void upsertPrimaryEmail(UUID userId, String email) {
        removePrimaryEmail(userId);
        insertPrimaryEmail(userId, email);
    }

    // mobile
    @Modifying
    @Query(value = "DELETE FROM user_contacts WHERE user_id=:uid AND type='mobile' AND is_primary=true", nativeQuery = true)
    void removePrimaryMobile(@Param("uid") UUID userId);

    @Modifying
    @Query(value = """
       INSERT INTO user_contacts (id,user_id,type,value,consent_contact,consent_sms,consent_voicemail,is_primary,created_at)
       VALUES (gen_random_uuid(), :uid,'mobile',:val,false,false,false,true, now())
       ON CONFLICT (user_id, type, value) DO UPDATE SET is_primary=EXCLUDED.is_primary
    """, nativeQuery = true)
    void insertPrimaryMobile(@Param("uid") UUID userId, @Param("val") String value);

    default void upsertPrimaryMobile(UUID userId, String mobile) {
        removePrimaryMobile(userId);
        insertPrimaryMobile(userId, mobile);
    }

    // home phone – hasonló mint a mobile
    @Modifying
    @Query(value = "DELETE FROM user_contacts WHERE user_id=:uid AND type='home_phone' AND is_primary=true", nativeQuery = true)
    void removePrimaryHome(@Param("uid") UUID userId);

    @Modifying
    @Query(value = """
       INSERT INTO user_contacts (id,user_id,type,value,consent_contact,consent_sms,consent_voicemail,is_primary,created_at)
       VALUES (gen_random_uuid(), :uid,'home_phone',:val,false,false,false,true, now())
       ON CONFLICT (user_id, type, value) DO UPDATE SET is_primary=EXCLUDED.is_primary
    """, nativeQuery = true)
    void insertPrimaryHome(@Param("uid") UUID userId, @Param("val") String value);

    default void upsertPrimaryHome(UUID userId, String home) {
        removePrimaryHome(userId);
        insertPrimaryHome(userId, home);
    }
}
