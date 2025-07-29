package com.ImageProcessing.repository.projection;

import java.time.LocalDateTime;

public interface ImageProjection {
    String getName();
    byte[] getImageData();
    String getType();
    LocalDateTime getImageDate();
}
