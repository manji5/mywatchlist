package me.fatihenes.mywatchlist.media.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import me.fatihenes.mywatchlist.media.entity.Media;
import me.fatihenes.mywatchlist.media.entity.MediaType;

public interface MediaRepository extends JpaRepository<Media, Long> {
    boolean existsByExternalIdAndTypeAndUserUsername(String externalId, MediaType mediaType,
            String username);

    Optional<Media> findByExternalIdAndTypeAndUserUsername(String externalId, MediaType type,
            String username);

    Page<Media> findAllByUserUsername(String username, Pageable pageable);
}
