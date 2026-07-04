package me.fatihenes.mywatchlist.auth.controller;

import org.apache.coyote.BadRequestException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.fatihenes.mywatchlist.auth.dto.ProfileImageResponse;
import me.fatihenes.mywatchlist.auth.dto.UpdateAvatarRequest;
import me.fatihenes.mywatchlist.auth.dto.UpdateEmailRequest;
import me.fatihenes.mywatchlist.auth.dto.UpdatePasswordRequest;
import me.fatihenes.mywatchlist.auth.dto.UpdateUsernameRequest;
import me.fatihenes.mywatchlist.auth.dto.UpdateUsernameResponse;
import me.fatihenes.mywatchlist.auth.dto.UserProfileResponse;
import me.fatihenes.mywatchlist.auth.service.ProfileImageService;
import me.fatihenes.mywatchlist.auth.service.UserService;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ProfileImageService profileImageService;

    private String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile() {
        return ResponseEntity.ok(userService.getProfile(currentUsername()));
    }

    @PatchMapping("/email")
    public ResponseEntity<String> updateEmail(@Valid @RequestBody UpdateEmailRequest request)
            throws BadRequestException {
        userService.updateEmail(currentUsername(), request);
        return ResponseEntity.ok("Email successfully updated.");
    }

    @PatchMapping("/username")
    public ResponseEntity<UpdateUsernameResponse> updateUsername(
            @Valid @RequestBody UpdateUsernameRequest request) throws BadRequestException {
        return ResponseEntity.ok(userService.updateUsername(currentUsername(), request));
    }

    @PatchMapping("/password")
    public ResponseEntity<String> updatePassword(@Valid @RequestBody UpdatePasswordRequest request)
            throws BadRequestException {
        userService.updatePassword(currentUsername(), request);
        return ResponseEntity.ok("Password successfully updated.");
    }

    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileImageResponse> uploadCustomAvatar(
            @RequestParam("file") MultipartFile file) throws BadRequestException {
        String url = profileImageService.uploadCustomAvatar(currentUsername(), file);
        return ResponseEntity.ok(new ProfileImageResponse(url));
    }

    @PatchMapping("/profile-image/preset")
    public ResponseEntity<ProfileImageResponse> selectPresetAvatar(
            @Valid @RequestBody UpdateAvatarRequest request) throws BadRequestException {
        String url = profileImageService.selectPresetAvatar(currentUsername(), request.avatarId());
        return ResponseEntity.ok(new ProfileImageResponse(url));
    }

    @DeleteMapping("/profile-image")
    public ResponseEntity<String> removeAvatar() throws BadRequestException {
        profileImageService.removeAvatar(currentUsername());
        return ResponseEntity.ok("Profile image removed.");
    }

}
