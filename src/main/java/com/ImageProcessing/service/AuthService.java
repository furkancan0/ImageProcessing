package com.ImageProcessing.service;

import com.ImageProcessing.dto.*;
import com.ImageProcessing.entity.Role;
import com.ImageProcessing.entity.User;
import com.ImageProcessing.exception.ApiRequestException;
import com.ImageProcessing.jwt.JwtService;
import com.ImageProcessing.repository.RoleRepository;
import com.ImageProcessing.repository.UserRepository;
import com.ImageProcessing.util.AuthUtil;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest registerRequest) {
        Role role = roleRepository.findByName(RoleEnum.USER);//New accounts have default user role
        var user = User.builder()
                .name(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .createdDate(LocalDateTime.now())
                .privateProfile(false)
                .role(role)
                .build();
        userRepository.save(user);
        String jwtToken = jwtService.createToken(user, String.valueOf(role.getName()));
        return AuthenticationResponse.builder().accessToken(jwtToken).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiRequestException("USER_NOT_FOUND", HttpStatus.NOT_FOUND));
        String jwtToken = jwtService.createToken(user, String.valueOf(user.getRole().getName()));
        return AuthenticationResponse.builder().accessToken(jwtToken).build();

    }

    public User getCurrentUser() {
        Long userId =  AuthUtil.getAuthenticatedUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiRequestException("USER_NOT_FOUND", HttpStatus.UNAUTHORIZED));

    }

    public boolean isUserHavePrivateProfile(Long userId) {
        return userRepository.isUserHavePrivateProfile(userId);
    }

}