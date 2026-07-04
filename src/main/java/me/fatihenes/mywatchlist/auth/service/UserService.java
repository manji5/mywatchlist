package me.fatihenes.mywatchlist.auth.service;

import org.apache.coyote.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import me.fatihenes.mywatchlist.auth.dto.UpdateEmailRequest;
import me.fatihenes.mywatchlist.auth.dto.UpdatePasswordRequest;
import me.fatihenes.mywatchlist.auth.dto.UpdateUsernameRequest;
import me.fatihenes.mywatchlist.auth.dto.UpdateUsernameResponse;
import me.fatihenes.mywatchlist.auth.dto.UserProfileResponse;
import me.fatihenes.mywatchlist.auth.entity.User;
import me.fatihenes.mywatchlist.auth.repository.UserRepository;
import me.fatihenes.mywatchlist.exception.ResourceConflictException;
import me.fatihenes.mywatchlist.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private void verifyPassword(User user, String rawPassword) throws BadRequestException {
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadRequestException("Current password is incorrect.");
        }
    }

    public UserProfileResponse getProfile(String username) {
        User user = getUser(username);
        return new UserProfileResponse(user.getUsername(), user.getEmail(),
                user.getProfileImageUrl());
    }

    public void updateEmail(String username, UpdateEmailRequest request)
            throws BadRequestException {
        User user = getUser(username);
        verifyPassword(user, request.currentPassword());

        if (!user.getEmail().equalsIgnoreCase(request.newEmail())
                && userRepository.existsByEmail(request.newEmail())) {
            throw new ResourceConflictException("This email is already used.");
        }

        user.setEmail(request.newEmail());
        userRepository.save(user);
    }

    public UpdateUsernameResponse updateUsername(String username, UpdateUsernameRequest request)
            throws BadRequestException {
        User user = getUser(username);
        verifyPassword(user, request.currentPassword());

        if (!user.getUsername().equals(request.newUsername())
                && userRepository.existsByUsername(request.newUsername())) {
            throw new ResourceConflictException("This username is already used.");
        }

        user.setUsername(request.newUsername());
        userRepository.save(user);

        String newToken = jwtService.generateToken(user);
        return new UpdateUsernameResponse(newToken, user.getUsername(),
                "Username successfully updated.");
    }

    public void updatePassword(String username, UpdatePasswordRequest request)
            throws BadRequestException {
        User user = getUser(username);
        verifyPassword(user, request.currentPassword());

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

}
