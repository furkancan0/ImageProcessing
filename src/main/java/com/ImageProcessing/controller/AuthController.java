package com.ImageProcessing.controller;

import com.ImageProcessing.dto.AuthenticationRequest;
import com.ImageProcessing.dto.AuthenticationResponse;
import com.ImageProcessing.dto.RegisterRequest;
import com.ImageProcessing.dto.UserDto;
import com.ImageProcessing.entity.User;
import com.ImageProcessing.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth/")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest registerRequest
    ) {
        AuthenticationResponse authResponse = authService.register(registerRequest);
        return  ResponseEntity.ok(authResponse);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @Transactional(readOnly = true)
    @GetMapping("/me")
    public ResponseEntity<UserDto> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        UserDto userDto = UserDto.builder().role(user.getRole()).email(user.getEmail()).name(user.getName())
                .createdDate(user.getCreatedDate()).build();
        return ResponseEntity.ok(userDto);
    }

}