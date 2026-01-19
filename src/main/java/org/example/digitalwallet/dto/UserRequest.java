package org.example.digitalwallet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.digitalwallet.model.MembershipStatus;


public record UserRequest(
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Username cannot be blank")
        String username,

        @NotBlank(message = "Password cannot be blank")
        @Size(min = 5, max = 10, message = "Password should be between 5 and 10 characters")
        String password,

        MembershipStatus status
) {
    public UserRequest {
        if (status == null) {
            status = MembershipStatus.FREE;
        }
    }
}
