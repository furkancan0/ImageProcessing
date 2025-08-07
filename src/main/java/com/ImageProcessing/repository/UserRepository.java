package com.ImageProcessing.repository;

import com.ImageProcessing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("""
            SELECT CASE WHEN count(user) > 0 THEN true ELSE false END FROM User user
            WHERE user.id = :userId AND user.privateProfile = true
            """)
    boolean isUserHavePrivateProfile(@Param("userId") Long userId);
}
