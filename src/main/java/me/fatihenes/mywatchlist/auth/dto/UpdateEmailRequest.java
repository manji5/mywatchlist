package me.fatihenes.mywatchlist.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateEmailRequest(
        @NotBlank(message = "Password cannot be blank") String currentPassword,
        @NotBlank(message = "Email cannot be blank") @Email(
                message = "Must be a valid email address") String newEmail) {

}
