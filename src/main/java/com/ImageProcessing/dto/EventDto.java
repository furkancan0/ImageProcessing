package com.ImageProcessing.dto;

import lombok.Data;

@Data
public class EventDto {
    private int eventId;
    private String eventName;
    private String eventDescription;
    private Integer height;
    private Integer width;
}
