package com.ImageProcessing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadImageResponse {
    private Long id;
    private String message;
}
