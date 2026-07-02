package me.fatihenes.mywatchlist.media.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.fatihenes.mywatchlist.auth.entity.User;

@Entity
@Table(name = "media",
        indexes = {@Index(name = "idx_media_user_type", columnList = "user_id, type"),
                @Index(name = "idx_media_external_user", columnList = "externalId, type, user_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String externalId; // TMDB or Jikan

    @Column(nullable = false)
    private String title;

    private String originalTitle;

    private String backdropUrl;

    private String posterUrl;

    private Double score;

    private Integer duration;

    private Integer episodes;

    @Column(nullable = false)
    @Builder.Default
    private Integer watchedEpisodes = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WatchStatus status = WatchStatus.PLAN_TO_WATCH;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
}
