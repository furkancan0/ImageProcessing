package com.ImageProcessing.repository;

import com.ImageProcessing.dto.RoleEnum;
import com.ImageProcessing.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(RoleEnum name);
}