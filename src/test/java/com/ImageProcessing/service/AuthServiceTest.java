package com.ImageProcessing.service;

import com.ImageProcessing.ServiceTestHelper;
import com.ImageProcessing.dto.AuthenticationRequest;
import com.ImageProcessing.dto.AuthenticationResponse;
import com.ImageProcessing.dto.RegisterRequest;
import com.ImageProcessing.dto.RoleEnum;
import com.ImageProcessing.entity.Role;
import com.ImageProcessing.entity.User;
import com.ImageProcessing.exception.ApiRequestException;
import com.ImageProcessing.jwt.JwtService;
import com.ImageProcessing.repository.RoleRepository;
import com.ImageProcessing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.ImageProcessing.ServiceTestHelper.pageable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    public void setUp() {
    }

    @Test
    void register() {
        RegisterRequest registerRequest = new RegisterRequest("testUser", "test@example.com", "password");
        Role mockRole = new Role(1L, RoleEnum.USER,"user");
        User user = User.builder()
                .name("testUser")
                .email("test@example.com")
                .password("hashedPassword")
                .role(mockRole)
                .createdDate(LocalDateTime.now())
                .build();

        when(roleRepository.findByName(RoleEnum.USER)).thenReturn(mockRole);
        when(passwordEncoder.encode("password")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.createToken(any(User.class), any())).thenReturn("mock_jwt");
        assertEquals("mock_jwt", authService.register(registerRequest).getAccessToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void authenticate() {
        AuthenticationRequest request = new AuthenticationRequest("test@example.com", "password");

        Role mockRole = new Role(1L, RoleEnum.USER,"user");
        User user = User.builder()
                .email("test@example.com")
                .password("password")
                .role(mockRole)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtService.createToken(user, "USER")).thenReturn("jwt_token");
        assertEquals("jwt_token", authService.authenticate(request).getAccessToken());
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void testAuthenticate_UserNotFound() {
        AuthenticationRequest request = new AuthenticationRequest("notfound@example.com", "password");

        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        ApiRequestException exception = assertThrows(ApiRequestException.class, () -> {
            authService.authenticate(request);
        });

        assertEquals("USER_NOT_FOUND", exception.getMessage());
    }


    @Test
    void getCurrentUser() {
        User applicationUser = new User();
        applicationUser.setId(1L);
        Authentication authentication = new UsernamePasswordAuthenticationToken(applicationUser, null, List.of());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        when(userRepository.findById(1L)).thenReturn(Optional.of(applicationUser));
        assertEquals(applicationUser, authService.getCurrentUser());
        verify(userRepository, times(1)).findById(applicationUser.getId());
    }

    @Test
    void isUserHavePrivateProfile() {
        when(userRepository.isUserHavePrivateProfile(2L)).thenReturn(false);

        boolean result = authService.isUserHavePrivateProfile(2L);

        assertTrue(result);
    }

    @Test
    void isUserHavePrivateProfile_ReturnsFalseIfPrivate() {
        when(userRepository.isUserHavePrivateProfile(2L)).thenReturn(true);

        boolean result = authService.isUserHavePrivateProfile(2L);

        assertFalse(result);
    }
}