package com.drawpin.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 80, message = "Name must not exceed 80 characters")
    private String name;

    @Size(max = 300, message = "Bio must not exceed 300 characters")
    private String bio;

    @Size(max = 80, message = "City must not exceed 80 characters")
    private String city;

    @Size(max = 255, message = "Website URL must not exceed 255 characters")
    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})[/\\w .-]*/?$", message = "Invalid website URL format")
    private String website;
}
