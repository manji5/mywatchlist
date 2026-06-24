package me.fatihenes.mywatchlist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import me.fatihenes.mywatchlist.auth.entity.User;
import me.fatihenes.mywatchlist.auth.repository.UserRepository;
import me.fatihenes.mywatchlist.exception.ApiErrorResponse;
import me.fatihenes.mywatchlist.exception.BadRequestException;
import me.fatihenes.mywatchlist.exception.ExternalServiceException;
import me.fatihenes.mywatchlist.exception.GlobalExceptionHandler;
import me.fatihenes.mywatchlist.exception.ResourceConflictException;
import me.fatihenes.mywatchlist.exception.ResourceNotFoundException;
import me.fatihenes.mywatchlist.exception.UnauthorizedAccessException;
import me.fatihenes.mywatchlist.media.client.JikanClient;
import me.fatihenes.mywatchlist.media.client.TmdbClient;
import me.fatihenes.mywatchlist.media.dto.JikanAnimeDTO;
import me.fatihenes.mywatchlist.media.dto.MediaResponseDTO;
import me.fatihenes.mywatchlist.media.dto.TmdbDTO;
import me.fatihenes.mywatchlist.media.entity.Media;
import me.fatihenes.mywatchlist.media.entity.MediaType;
import me.fatihenes.mywatchlist.media.entity.WatchStatus;
import me.fatihenes.mywatchlist.media.repository.MediaRepository;
import me.fatihenes.mywatchlist.media.service.WatchlistService;

@ExtendWith(MockitoExtension.class)
class MywatchlistApplicationTests {

	// =========================================================
	// WatchlistService Tests
	// =========================================================
	@Nested
	class WatchlistServiceTests {

		@Mock
		private MediaRepository mediaRepository;

		@Mock
		private UserRepository userRepository;

		@Mock
		private TmdbClient tmdbClient;

		@Mock
		private JikanClient jikanClient;

		@InjectMocks
		private WatchlistService watchlistService;

		// --- saveMovie ---

		@Test
		void saveMovie_ShouldThrowConflict_WhenMovieAlreadyExists() {
			Long movieId = 123L;
			String username = "testuser";

			when(mediaRepository.existsByExternalIdAndTypeAndUserUsername(String.valueOf(movieId),
					MediaType.MOVIE, username)).thenReturn(true);

			assertThrows(ResourceConflictException.class,
					() -> watchlistService.saveMovie(movieId, username));

			verify(mediaRepository, never()).save(any());
			verify(tmdbClient, never()).fetchMovieDetails(anyLong());
		}

		@Test
		void saveMovie_ShouldSaveSuccessfully_WhenValidDataProvided() {
			Long movieId = 550L;
			String username = "testuser";

			User mockUser = new User();
			mockUser.setUsername(username);

			TmdbDTO mockDto = new TmdbDTO();
			mockDto.setId(movieId);
			mockDto.setTitle("Fight Club");
			mockDto.setVoteAverage(8.8);
			mockDto.setRuntime(139);

			when(mediaRepository.existsByExternalIdAndTypeAndUserUsername(String.valueOf(movieId),
					MediaType.MOVIE, username)).thenReturn(false);
			when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
			when(tmdbClient.fetchMovieDetails(movieId)).thenReturn(mockDto);

			watchlistService.saveMovie(movieId, username);

			verify(mediaRepository, times(1)).save(any(Media.class));
		}

		@Test
		void saveMovie_ShouldThrowNotFound_WhenUserDoesNotExist() {
			Long movieId = 550L;
			String username = "ghost";

			when(mediaRepository.existsByExternalIdAndTypeAndUserUsername(String.valueOf(movieId),
					MediaType.MOVIE, username)).thenReturn(false);
			when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

			assertThrows(ResourceNotFoundException.class,
					() -> watchlistService.saveMovie(movieId, username));

			verify(mediaRepository, never()).save(any());
		}

		@Test
		void saveMovie_ShouldUseOriginalTitle_WhenTitleIsNull() {
			Long movieId = 550L;
			String username = "testuser";

			User mockUser = new User();
			mockUser.setUsername(username);

			TmdbDTO mockDto = new TmdbDTO();
			mockDto.setId(movieId);
			mockDto.setTitle(null);
			mockDto.setOriginalTitle("Fight Club Original");

			when(mediaRepository.existsByExternalIdAndTypeAndUserUsername(String.valueOf(movieId),
					MediaType.MOVIE, username)).thenReturn(false);
			when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
			when(tmdbClient.fetchMovieDetails(movieId)).thenReturn(mockDto);

			watchlistService.saveMovie(movieId, username);

			verify(mediaRepository, times(1)).save(any(Media.class));
		}

		// --- saveSeries ---

		@Test
		void saveSeries_ShouldThrowConflict_WhenSeriesAlreadyExists() {
			Long seriesId = 5920L;
			String username = "testuser";

			when(mediaRepository.existsByExternalIdAndTypeAndUserUsername(String.valueOf(seriesId),
					MediaType.TV_SERIES, username)).thenReturn(true);

			assertThrows(ResourceConflictException.class,
					() -> watchlistService.saveSeries(seriesId, username));

			verify(mediaRepository, never()).save(any());
		}

		@Test
		void saveSeries_ShouldSaveSuccessfully_WhenValidDataProvided() {
			Long seriesId = 5920L;
			String username = "testuser";

			User mockUser = new User();
			mockUser.setUsername(username);

			TmdbDTO mockDto = new TmdbDTO();
			mockDto.setId(seriesId);
			mockDto.setTitle("The Mentalist");
			mockDto.setVoteAverage(8.5);
			mockDto.setEpisodes(151);

			when(mediaRepository.existsByExternalIdAndTypeAndUserUsername(String.valueOf(seriesId),
					MediaType.TV_SERIES, username)).thenReturn(false);
			when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
			when(tmdbClient.fetchSeriesDetails(seriesId)).thenReturn(mockDto);

			watchlistService.saveSeries(seriesId, username);

			verify(mediaRepository, times(1)).save(any(Media.class));
		}

		@Test
		void saveSeries_ShouldSetEpisodesZero_WhenEpisodesIsNull() {
			Long seriesId = 5920L;
			String username = "testuser";

			User mockUser = new User();
			mockUser.setUsername(username);

			TmdbDTO mockDto = new TmdbDTO();
			mockDto.setId(seriesId);
			mockDto.setTitle("Unknown Show");
			mockDto.setEpisodes(null);

			when(mediaRepository.existsByExternalIdAndTypeAndUserUsername(String.valueOf(seriesId),
					MediaType.TV_SERIES, username)).thenReturn(false);
			when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
			when(tmdbClient.fetchSeriesDetails(seriesId)).thenReturn(mockDto);

			watchlistService.saveSeries(seriesId, username);

			verify(mediaRepository, times(1)).save(any(Media.class));
		}

		// --- saveAnime ---

		@Test
		void saveAnime_ShouldThrowConflict_WhenAnimeAlreadyExists() {
			Long malId = 1L;
			String username = "testuser";

			when(mediaRepository.existsByExternalIdAndTypeAndUserUsername(String.valueOf(malId),
					MediaType.ANIME, username)).thenReturn(true);

			assertThrows(ResourceConflictException.class,
					() -> watchlistService.saveAnime(malId, username));

			verify(mediaRepository, never()).save(any());
			verify(jikanClient, never()).fetchAnimeDetails(anyLong());
		}

		@Test
		void saveAnime_ShouldSaveSuccessfully_WhenValidDataProvided() {
			Long malId = 1L;
			String username = "testuser";

			User mockUser = new User();
			mockUser.setUsername(username);

			JikanAnimeDTO mockDto = new JikanAnimeDTO();
			mockDto.setMalId(malId);
			mockDto.setTitle("Cowboy Bebop");
			mockDto.setScore(8.75);
			mockDto.setDuration("24 min per ep");
			mockDto.setEpisodes(26);

			when(mediaRepository.existsByExternalIdAndTypeAndUserUsername(String.valueOf(malId),
					MediaType.ANIME, username)).thenReturn(false);
			when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
			when(jikanClient.fetchAnimeDetails(malId)).thenReturn(mockDto);

			watchlistService.saveAnime(malId, username);

			verify(mediaRepository, times(1)).save(any(Media.class));
		}

		@Test
		void saveAnime_ShouldHandleNullDuration_Gracefully() {
			Long malId = 1L;
			String username = "testuser";

			User mockUser = new User();
			mockUser.setUsername(username);

			JikanAnimeDTO mockDto = new JikanAnimeDTO();
			mockDto.setMalId(malId);
			mockDto.setTitle("Cowboy Bebop");
			mockDto.setDuration(null); // null duration
			mockDto.setEpisodes(26);

			when(mediaRepository.existsByExternalIdAndTypeAndUserUsername(String.valueOf(malId),
					MediaType.ANIME, username)).thenReturn(false);
			when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
			when(jikanClient.fetchAnimeDetails(malId)).thenReturn(mockDto);

			watchlistService.saveAnime(malId, username);

			verify(mediaRepository, times(1)).save(any(Media.class));
		}

		@Test
		void saveAnime_ShouldHandleUnknownDuration_Gracefully() {
			Long malId = 1L;
			String username = "testuser";

			User mockUser = new User();
			mockUser.setUsername(username);

			JikanAnimeDTO mockDto = new JikanAnimeDTO();
			mockDto.setMalId(malId);
			mockDto.setTitle("Cowboy Bebop");
			mockDto.setDuration("Unknown"); // "Unknown" duration
			mockDto.setEpisodes(26);

			when(mediaRepository.existsByExternalIdAndTypeAndUserUsername(String.valueOf(malId),
					MediaType.ANIME, username)).thenReturn(false);
			when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
			when(jikanClient.fetchAnimeDetails(malId)).thenReturn(mockDto);

			watchlistService.saveAnime(malId, username);

			verify(mediaRepository, times(1)).save(any(Media.class));
		}

		// --- updateWatchStatus ---

		@Test
		void updateWatchStatus_ShouldThrowNotFound_WhenMediaDoesNotExist() {
			when(mediaRepository.findById(99L)).thenReturn(Optional.empty());

			assertThrows(ResourceNotFoundException.class, () -> watchlistService
					.updateWatchStatus(99L, WatchStatus.COMPLETED, "testuser"));
		}

		@Test
		void updateWatchStatus_ShouldThrowBadRequest_WhenCompletedButZeroEpisodes() {
			User user = new User();
			user.setUsername("testuser");

			Media media = Media.builder().id(1L).title("Some Show").episodes(0).user(user).build();

			when(mediaRepository.findById(1L)).thenReturn(Optional.of(media));

			assertThrows(BadRequestException.class, () -> watchlistService.updateWatchStatus(1L,
					WatchStatus.COMPLETED, "testuser"));

			verify(mediaRepository, never()).save(any());
		}

		@Test
		void updateWatchStatus_ShouldThrowUnauthorized_WhenUserIsNotOwner() {
			User owner = new User();
			owner.setUsername("owner");

			Media media =
					Media.builder().id(1L).title("Some Show").episodes(12).user(owner).build();

			when(mediaRepository.findById(1L)).thenReturn(Optional.of(media));

			assertThrows(UnauthorizedAccessException.class, () -> watchlistService
					.updateWatchStatus(1L, WatchStatus.COMPLETED, "attacker"));

			verify(mediaRepository, never()).save(any());
		}

		@Test
		void updateWatchStatus_ShouldUpdateSuccessfully_WhenValidRequest() {
			User user = new User();
			user.setUsername("testuser");

			Media media = Media.builder().id(1L).title("Some Show").episodes(12)
					.status(WatchStatus.PLAN_TO_WATCH).user(user).build();

			when(mediaRepository.findById(1L)).thenReturn(Optional.of(media));

			watchlistService.updateWatchStatus(1L, WatchStatus.COMPLETED, "testuser");

			assertThat(media.getStatus()).isEqualTo(WatchStatus.COMPLETED);
			verify(mediaRepository, times(1)).save(media);
		}

		// --- deleteMedia ---

		@Test
		void deleteMedia_ShouldThrowNotFound_WhenMediaDoesNotExist() {
			when(mediaRepository.findById(99L)).thenReturn(Optional.empty());

			assertThrows(ResourceNotFoundException.class,
					() -> watchlistService.deleteMedia(99L, "testuser"));
		}

		@Test
		void deleteMedia_ShouldThrowUnauthorized_WhenUserIsNotOwner() {
			User owner = new User();
			owner.setUsername("owner");

			Media media = Media.builder().id(1L).user(owner).build();

			when(mediaRepository.findById(1L)).thenReturn(Optional.of(media));

			assertThrows(UnauthorizedAccessException.class,
					() -> watchlistService.deleteMedia(1L, "attacker"));

			verify(mediaRepository, never()).delete(any());
		}

		@Test
		void deleteMedia_ShouldDeleteSuccessfully_WhenUserIsOwner() {
			User user = new User();
			user.setUsername("testuser");

			Media media = Media.builder().id(1L).user(user).build();

			when(mediaRepository.findById(1L)).thenReturn(Optional.of(media));

			watchlistService.deleteMedia(1L, "testuser");

			verify(mediaRepository, times(1)).delete(media);
		}

		// --- getWatchlistByUsername ---

		@Test
		void getWatchlistByUsername_ShouldReturnMappedDTOs() {
			User user = new User();
			user.setUsername("testuser");

			Media media = Media.builder().id(1L).externalId("550").title("Fight Club").score(8.8)
					.duration(139).episodes(1).type(MediaType.MOVIE).status(WatchStatus.COMPLETED)
					.user(user).build();

			Page<Media> mediaPage = new PageImpl<>(List.of(media));

			when(mediaRepository.findAllByUserUsername(anyString(), any(Pageable.class)))
					.thenReturn(mediaPage);

			Page<MediaResponseDTO> result =
					watchlistService.getWatchlistByUsername("testuser", 0, 10);

			assertThat(result.getContent()).hasSize(1);
			assertThat(result.getContent().get(0).title()).isEqualTo("Fight Club");
			assertThat(result.getContent().get(0).status()).isEqualTo(WatchStatus.COMPLETED);
		}

		@Test
		void getWatchlistByUsername_ShouldReturnEmptyPage_WhenNoMedia() {
			when(mediaRepository.findAllByUserUsername(anyString(), any(Pageable.class)))
					.thenReturn(Page.empty());

			Page<MediaResponseDTO> result =
					watchlistService.getWatchlistByUsername("testuser", 0, 10);

			assertThat(result.getContent()).isEmpty();
		}
	}

	// =========================================================
	// GlobalExceptionHandler Tests
	// =========================================================
	@Nested
	class GlobalExceptionHandlerTests {

		private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

		@Test
		void handleConflict_ShouldReturn409_WithCorrectBody() {
			ResourceConflictException ex = new ResourceConflictException("Already exists");

			ResponseEntity<ApiErrorResponse> response = handler.handleConflict(ex);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
			assertThat(response.getBody().status()).isEqualTo(409);
			assertThat(response.getBody().error()).isEqualTo("Conflict");
			assertThat(response.getBody().message()).isEqualTo("Already exists");
			assertThat(response.getBody().fieldErrors()).isNull();
			assertThat(response.getBody().timestamp()).isNotNull();
		}

		@Test
		void handleNotFound_ShouldReturn404_WithCorrectBody() {
			ResourceNotFoundException ex = new ResourceNotFoundException("User not found");

			ResponseEntity<ApiErrorResponse> response = handler.handleNotFound(ex);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
			assertThat(response.getBody().status()).isEqualTo(404);
			assertThat(response.getBody().message()).isEqualTo("User not found");
			assertThat(response.getBody().fieldErrors()).isNull();
		}

		@Test
		void handleUnauthorized_ShouldReturn403_WithCorrectBody() {
			UnauthorizedAccessException ex = new UnauthorizedAccessException("Access denied");

			ResponseEntity<ApiErrorResponse> response = handler.handleUnautherizedException(ex);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
			assertThat(response.getBody().status()).isEqualTo(403);
			assertThat(response.getBody().message()).isEqualTo("Access denied");
		}

		@Test
		void handleBadRequest_ShouldReturn400_WithCorrectBody() {
			BadRequestException ex = new BadRequestException("Invalid input");

			ResponseEntity<ApiErrorResponse> response = handler.handleBadRequest(ex);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertThat(response.getBody().status()).isEqualTo(400);
			assertThat(response.getBody().message()).isEqualTo("Invalid input");
		}

		@Test
		void handleExternalService_ShouldReturn502_WithCorrectBody() {
			ExternalServiceException ex = new ExternalServiceException("TMDB unavailable");

			ResponseEntity<ApiErrorResponse> response = handler.handleExternalService(ex);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
			assertThat(response.getBody().status()).isEqualTo(502);
			assertThat(response.getBody().message()).isEqualTo("TMDB unavailable");
		}

		@Test
		void handleGeneralException_ShouldReturn500_WithGenericMessage() {
			RuntimeException ex = new RuntimeException("Something broke");

			ResponseEntity<ApiErrorResponse> response = handler.handleGeneralException(ex);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
			assertThat(response.getBody().status()).isEqualTo(500);
			assertThat(response.getBody().message()).doesNotContain("Something broke");
		}

		@Test
		void handleValidation_ShouldReturn400_WithFieldErrors_AndNullMessage() {
			MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
			BindingResult bindingResult = mock(BindingResult.class);
			FieldError fieldError = new FieldError("obj", "email", "cannot be blank");

			when(ex.getBindingResult()).thenReturn(bindingResult);
			when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

			ResponseEntity<ApiErrorResponse> response = handler.handleValidationExceptions(ex);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertThat(response.getBody().fieldErrors()).containsEntry("email", "cannot be blank");
			assertThat(response.getBody().message()).isNull();
			assertThat(response.getBody().fieldErrors()).isNotNull();
		}

		@Test
		void handleValidation_ShouldCaptureMultipleFieldErrors() {
			MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
			BindingResult bindingResult = mock(BindingResult.class);

			List<FieldError> errors = List.of(new FieldError("obj", "email", "cannot be blank"),
					new FieldError("obj", "username", "cannot be null"),
					new FieldError("obj", "password", "must be at least 8 characters"));

			when(ex.getBindingResult()).thenReturn(bindingResult);
			when(bindingResult.getFieldErrors()).thenReturn(errors);

			ResponseEntity<ApiErrorResponse> response = handler.handleValidationExceptions(ex);

			assertThat(response.getBody().fieldErrors()).hasSize(3);
			assertThat(response.getBody().fieldErrors()).containsKeys("email", "username",
					"password");
		}
	}
}
