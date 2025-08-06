package com.ImageProcessing.repository;

import com.ImageProcessing.entity.Image;
import com.ImageProcessing.entity.User;
import com.ImageProcessing.repository.projection.ImageProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image,Long> {
    @Query("SELECT s FROM Image s WHERE s.id = :imageId")
    Optional<ImageProjection> getImageById(@Param("imageId") Long imageId);

    @Query("""
            SELECT image FROM Image image
            WHERE image.user.id = :id
            ORDER BY image.imageDate DESC
            """)
    Page<ImageProjection> getAuthorImages(@Param("userId") Long id, Pageable pageable);

    @Query("SELECT image FROM Image image WHERE UPPER(image.name) " +
            "LIKE UPPER(CONCAT('%',:query,'%')) " +
            "AND image.user.id = :id " +
            "ORDER BY image.imageDate ASC")
    Page<ImageProjection> searchUserImages(@Param("userId") Long id, String query, Pageable pageable);

    @Query("SELECT image FROM Image image " +
            "WHERE (coalesce(:types, null) IS NULL OR split_part(image.type, '/', 2) IN :types) " +
            "AND (coalesce(:start, null) IS NULL OR image.imageDate BETWEEN :start AND :end) " +
            "ORDER BY image.imageDate ASC")
    List<ImageProjection> getImagesByFilterParams(
            List<String> types,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("""
            SELECT image FROM Image image
            WHERE image.user.id = :userId
            ORDER BY image.imageDate DESC
            """)
    Page<ImageProjection> getUserImages(@Param("user") Long userId, Pageable pageable);

    @Query("""
            SELECT image FROM Image image
            WHERE image.user.id = :userId
            AND image.id = :imageId
            """)
    Optional<Image> getImageByUserId(@Param("userId") Long userId, @Param("tweetId") Long imageId);
}