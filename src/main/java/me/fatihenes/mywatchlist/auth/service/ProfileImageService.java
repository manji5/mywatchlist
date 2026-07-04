package me.fatihenes.mywatchlist.auth.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import me.fatihenes.mywatchlist.auth.entity.User;
import me.fatihenes.mywatchlist.auth.repository.UserRepository;
import me.fatihenes.mywatchlist.exception.ResourceNotFoundException;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

@Service
@RequiredArgsConstructor
public class ProfileImageService {

    private static final List<String> ALLOWED_TYPES =
            List.of("image/jpeg", "image/png", "image/webp");
    private static final int SIZE = 256;
    private static final float QUALITY = 0.75f;
    private static final int AVATAR_COUNT = 20;

    private final UserRepository userRepository;

    @Value("${application.upload.dir}")
    private String uploadDir;

    @Value("${application.upload.base-url}")
    private String baseUrl;

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    public String uploadCustomAvatar(String username, MultipartFile file)
            throws BadRequestException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File cannot be empty.");
        }
        if (file.getContentType() == null || !ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Only JPEG, PNG or WEBP images are allowed.");
        }

        User user = getUser(username);

        try {
            byte[] compressed = compress(file);

            Path dir = Path.of(uploadDir);
            Files.createDirectories(dir);

            String filename = "user-" + user.getId() + ".jpg";
            Files.write(dir.resolve(filename), compressed);

            String url = baseUrl + "/" + filename;
            user.setProfileImageUrl(url);
            userRepository.save(user);

            return url;
        } catch (IOException e) {
            throw new BadRequestException("The image could not be processed.");
        }
    }

    public String selectPresetAvatar(String username, int avatarId) throws BadRequestException {
        if (avatarId < 1 || avatarId > AVATAR_COUNT) {
            throw new BadRequestException("Invalid avatar id.");
        }

        User user = getUser(username);
        String url = "/avatars/avatar-" + avatarId + ".jpg";

        user.setProfileImageUrl(url);
        userRepository.save(user);

        return url;
    }

    public void removeAvatar(String username) throws BadRequestException {
        User user = getUser(username);

        deleteFilesIfCustomUpload(user.getProfileImageUrl());

        user.setProfileImageUrl(null);
        userRepository.save(user);
    }

    public void deleteFilesIfCustomUpload(String existingUrl) throws BadRequestException {
        if (existingUrl == null || existingUrl.isBlank()) {
            return;
        }
        if (!existingUrl.startsWith(baseUrl)) {
            return;
        }

        try {
            String filename = existingUrl.substring(existingUrl.lastIndexOf('/') + 1);
            Path filePath = Path.of(uploadDir, filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new BadRequestException("The image could not be deleted.");
        }
    }

    private byte[] compress(MultipartFile file) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        Thumbnails.of(file.getInputStream()).size(SIZE, SIZE).crop(Positions.CENTER)
                .outputFormat("jpg").outputQuality(QUALITY).toOutputStream(output);

        return output.toByteArray();
    }
}
