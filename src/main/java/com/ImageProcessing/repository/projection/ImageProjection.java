package com.ImageProcessing.repository.projection;

import java.time.LocalDateTime;

public interface ImageProjection {
    Long getId();
    String getName();
    byte[] getImageData();
    String getType();
    LocalDateTime getImageDate();
    boolean getDeleted();
}
