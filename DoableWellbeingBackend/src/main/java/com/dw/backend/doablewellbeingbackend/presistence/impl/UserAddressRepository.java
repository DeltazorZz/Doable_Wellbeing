package com.dw.backend.doablewellbeingbackend.presistence.impl;


import com.dw.backend.doablewellbeingbackend.presistence.entity.UserAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserAddressRepository extends JpaRepository<UserAddressEntity, UUID> {

    @Modifying
    @Query("""
      UPDATE UserAddressEntity a SET a.isPrimary=false WHERE a.userId=:uid AND a.isPrimary=true
    """)
    void clearPrimary(@Param("uid") UUID userId);

    // egyszerűsített upsert: most egy új primer sort hozunk létre
    @Modifying
    @Query(value = """
      INSERT INTO user_addresses (id,user_id,line1,line2,line3,city,county,postcode,is_primary,validated,created_at)
      VALUES (gen_random_uuid(), :uid, :l1, :l2, :l3, :city, :county, :pc, true, false, now())
    """, nativeQuery = true)
    void insertPrimary(@Param("uid") UUID userId,
                       @Param("l1") String line1, @Param("l2") String line2, @Param("l3") String line3,
                       @Param("city") String city, @Param("county") String county, @Param("pc") String postcode);

    default void upsertPrimary(UUID userId, String l1, String l2, String l3, String city, String county, String pc) {
        clearPrimary(userId);
        insertPrimary(userId, l1, l2, l3, city, county, pc);
    }

}
