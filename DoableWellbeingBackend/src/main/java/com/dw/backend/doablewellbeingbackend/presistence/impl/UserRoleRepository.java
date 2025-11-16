package com.dw.backend.doablewellbeingbackend.presistence.impl;

import com.dw.backend.doablewellbeingbackend.presistence.entity.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Integer> {
}
