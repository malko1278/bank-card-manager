package com.effective.mobile.bank_card_manager.controller;

import com.effective.mobile.bank_card_manager.entity.User;
import com.effective.mobile.bank_card_manager.payload.request.LoginRequest;
import com.effective.mobile.bank_card_manager.payload.request.SignupRequest;
import com.effective.mobile.bank_card_manager.payload.response.JwtResponse;
import com.effective.mobile.bank_card_manager.payload.response.MessageResponse;
import com.effective.mobile.bank_card_manager.repository.UserRepository;
import com.effective.mobile.bank_card_manager.security.jwt.JwtUtils;
import com.effective.mobile.bank_card_manager.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    // Constructor Injection
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
                          PasswordEncoder encoder, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getAuthorities()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка: Имя пользователя уже занято!"));
        }
        // Создание новой учетной записи пользователя
        User user = new User(
                signUpRequest.getUsername(),
                encoder.encode(signUpRequest.getPassword()),
                // Должно быть "USER" или "ADMIN"
                signUpRequest.getRole()
        );
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("Пользователь успешно зарегистрирован!"));
    }
}