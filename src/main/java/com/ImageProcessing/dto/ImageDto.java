package com.ImageProcessing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ImageDto {
    private Long id;
    private String name;
    private String type;
    private Long userId;
    private LocalDateTime imageDate;
}
