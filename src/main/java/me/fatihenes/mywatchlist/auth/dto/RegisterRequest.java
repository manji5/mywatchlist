package me.fatihenes.mywatchlist.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(@NotBlank(message = "Username cannot be blank") @Size(min = 3,
        max = 30,
        message = "Username must be between 3-30 characters") @Pattern(regexp = "^[a-zA-Z0-9_]+$",
                message = "Username can only contain letters, numbers and underscores") String username,

        @NotBlank(message = "Email cannot be blank") @Email(
                message = "Must be a valid email address") String email,

        @NotBlank(message = "Password cannot be blank") @Size(min = 8,
                message = "Password must be at least 8 characters") String password) {

}
