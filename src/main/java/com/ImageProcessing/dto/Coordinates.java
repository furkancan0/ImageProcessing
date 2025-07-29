package com.ImageProcessing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class Coordinates {
    @Positive
    private int x;
    @Positive
    private int y;
    @Positive
    private int width;
    @Positive
    private int height;
}
