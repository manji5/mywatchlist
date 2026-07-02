package me.fatihenes.mywatchlist.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import me.fatihenes.mywatchlist.auth.entity.User;
import me.fatihenes.mywatchlist.auth.repository.UserRepository;
import me.fatihenes.mywatchlist.exception.BadRequestException;
import me.fatihenes.mywatchlist.exception.ResourceConflictException;
import me.fatihenes.mywatchlist.auth.dto.AuthResponse;
import me.fatihenes.mywatchlist.auth.dto.LoginRequest;
import me.fatihenes.mywatchlist.auth.dto.RegisterRequest;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResourceConflictException("This username is already used.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ResourceConflictException("This email is already used.");
        }

        User user = User.builder().username(request.username()).email(request.email())
                .password(passwordEncoder.encode(request.password())).build();

        userRepository.save(user);
        return "User successfully created.";
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Wrong email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadRequestException("Wrong email or password.");
        }

        String jwtToken;

        if (request.rememberMe()) {
            jwtToken = jwtService.generateRememberMeToken(user);
        } else {
            jwtToken = jwtService.generateToken(user);
        }

        return new AuthResponse(jwtToken, user.getUsername(), user.getEmail(), "Login Successful!");
    }

}
