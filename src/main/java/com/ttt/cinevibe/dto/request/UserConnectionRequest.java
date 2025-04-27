package com.ttt.cinevibe.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserConnectionRequest {
    
    @NotBlank(message = "Target user ID is required")
    private String targetUserUid;
}