package com.ImageProcessing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "images")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String type;//database->image/jpeg

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "file_size")
    private Long fileSize;

    @Lob
    @JdbcTypeCode(java.sql.Types.BINARY)
    @Column(name = "image_data")
    private byte[] imageData;

    @Lob
    @JdbcTypeCode(java.sql.Types.BINARY)
    @Column(name = "thumbnail")
    private byte[] thumbnail;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(name = "date", nullable = false, columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime imageDate = LocalDateTime.now();
}