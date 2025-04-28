package com.ttt.cinevibe.dto.request;



import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    @NotNull(message = "Firebase UID is required")
    private String firebaseUid;

    @NotNull(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotNull(message = "Display name is required")
    @Size(min = 1, max = 50, message = "Display name must be between 1 and 50 characters")
    private String displayName;

    private String profileImageUrl;
}
