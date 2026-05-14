package dev.namphamcse.shopsflow.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dev.namphamcse.shopsflow.dto.request.LoginRequest;
import dev.namphamcse.shopsflow.dto.request.RegisterRequest;
import dev.namphamcse.shopsflow.dto.response.AuthResponse;
import dev.namphamcse.shopsflow.entity.User;
import dev.namphamcse.shopsflow.exception.DuplicateResourceException;
import dev.namphamcse.shopsflow.repository.UserRepository;
import dev.namphamcse.shopsflow.security.JwtUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }
        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token);
    }
}
