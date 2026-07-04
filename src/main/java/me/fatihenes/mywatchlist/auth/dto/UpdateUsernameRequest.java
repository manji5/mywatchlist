package me.fatihenes.mywatchlist.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUsernameRequest(@NotBlank String currentPassword,
        @NotBlank @Size(min = 3, max = 30,
                message = "Username must be between 3-30 characters") @Pattern(
                        regexp = "^[a-zA-Z0-9_]+$") String newUsername) {

}
