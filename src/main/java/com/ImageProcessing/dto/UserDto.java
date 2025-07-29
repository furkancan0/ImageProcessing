package com.ImageProcessing.dto;

import com.ImageProcessing.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class UserDto {
    private String name;
    private String email;
    private Role role;
    private LocalDateTime createdDate;
}
