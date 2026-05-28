package dev.namphamcse.shopsflow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import dev.namphamcse.shopsflow.dto.request.LoginRequest;
import dev.namphamcse.shopsflow.dto.request.RegisterRequest;
import dev.namphamcse.shopsflow.dto.response.AuthResponse;
import dev.namphamcse.shopsflow.entity.User;
import dev.namphamcse.shopsflow.entity.enums.Role;
import dev.namphamcse.shopsflow.exception.DuplicateResourceException;
import dev.namphamcse.shopsflow.repository.UserRepository;
import dev.namphamcse.shopsflow.security.JwtUtil;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepo;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtUtil jwtUtil;
    @Mock
    AuthenticationManager authenticationManager;

    @InjectMocks
    AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Nam");
        registerRequest.setEmail("n@x.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("n@x.com");
        loginRequest.setPassword("password123");
    }


    @Test
    void register_throws_whenEmailAlreadyExists() {
        when(userRepo.existsByEmail("n@x.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> authService.register(registerRequest));

        verify(userRepo, never()).save(any());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void register_hashesPassword_savesUser() {
        when(userRepo.existsByEmail("n@x.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("HASHED");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        authService.register(registerRequest);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("Nam", saved.getName());
        assertEquals("n@x.com", saved.getEmail());
        assertEquals("HASHED", saved.getPassword());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void register_doesNotStoreRawPassword() {
        when(userRepo.existsByEmail("n@x.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("HASHED");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(registerRequest);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(captor.capture());
        assertEquals("HASHED", captor.getValue().getPassword());
    }


    @Test
    void login_authenticates_returnsToken_onValidCredentials() {
        User user = new User("Nam", "n@x.com", "HASHED");
        user.setId(1L);

        when(userRepo.findByEmail("n@x.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(loginRequest);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertEquals("n@x.com", captor.getValue().getPrincipal());
        assertEquals("password123", captor.getValue().getCredentials());

        assertEquals("jwt-token", response.getToken());
        assertNotNull(response.getUser());
        assertEquals(1L, response.getUser().getId());
        assertEquals("Nam", response.getUser().getName());
        assertEquals("n@x.com", response.getUser().getEmail());
        assertEquals(Role.USER, response.getUser().getRole());
    }

    @Test
    void login_throws_whenAuthenticationFails() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad creds"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(loginRequest));

        verify(userRepo, never()).findByEmail(any());
        verify(jwtUtil, never()).generateToken(any());
    }
}
