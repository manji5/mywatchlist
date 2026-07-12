package me.fatihenes.mywatchlist.auth.service;

import org.apache.coyote.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import me.fatihenes.mywatchlist.auth.dto.PublicUserProfileResponse;
import me.fatihenes.mywatchlist.auth.dto.UpdateEmailRequest;
import me.fatihenes.mywatchlist.auth.dto.UpdatePasswordRequest;
import me.fatihenes.mywatchlist.auth.dto.UpdateUsernameRequest;
import me.fatihenes.mywatchlist.auth.dto.UpdateUsernameResponse;
import me.fatihenes.mywatchlist.auth.dto.UserProfileResponse;
import me.fatihenes.mywatchlist.auth.dto.UserStatsDTO;
import me.fatihenes.mywatchlist.auth.entity.User;
import me.fatihenes.mywatchlist.auth.repository.UserRepository;
import me.fatihenes.mywatchlist.exception.ResourceConflictException;
import me.fatihenes.mywatchlist.exception.ResourceNotFoundException;
import me.fatihenes.mywatchlist.media.entity.MediaType;
import me.fatihenes.mywatchlist.media.entity.WatchStatus;
import me.fatihenes.mywatchlist.media.repository.MediaRepository;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;
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

    private UserStatsDTO buildStats(String username) {
        long total = mediaRepository.countByUserUsername(username);
        long movies = mediaRepository.countByUserUsernameAndType(username, MediaType.MOVIE);
        long series = mediaRepository.countByUserUsernameAndType(username, MediaType.TV_SERIES);
        long anime = mediaRepository.countByUserUsernameAndType(username, MediaType.ANIME);
        long completed =
                mediaRepository.countByUserUsernameAndStatus(username, WatchStatus.COMPLETED);
        long watching =
                mediaRepository.countByUserUsernameAndStatus(username, WatchStatus.WATCHING);
        long planned =
                mediaRepository.countByUserUsernameAndStatus(username, WatchStatus.PLAN_TO_WATCH);
        long onHold = mediaRepository.countByUserUsernameAndStatus(username, WatchStatus.ON_HOLD);
        long dropped = mediaRepository.countByUserUsernameAndStatus(username, WatchStatus.DROPPED);

        return new UserStatsDTO(total, movies, series, anime, completed, watching, planned, onHold,
                dropped);
    }

    public UserProfileResponse getProfile(String username) {
        User user = getUser(username);
        return new UserProfileResponse(user.getUsername(), user.getEmail(),
                user.getProfileImageUrl(), buildStats(username));
    }

    public PublicUserProfileResponse getPublicProfile(String username) {
        User user = getUser(username);
        return new PublicUserProfileResponse(user.getUsername(), user.getProfileImageUrl(),
                buildStats(username));
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
