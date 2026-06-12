package com.example.inventoryorder.api;

import com.example.inventoryorder.api.dto.AuthDtos.LoginRequest;
import com.example.inventoryorder.api.dto.AuthDtos.RegisterRequest;
import com.example.inventoryorder.api.dto.AuthDtos.TokenResponse;
import com.example.inventoryorder.domain.AppUser;
import com.example.inventoryorder.repository.UserRepository;
import com.example.inventoryorder.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserRepository users,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    TokenResponse login(@Valid @RequestBody LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        return new TokenResponse(jwtService.generateToken((UserDetails) authentication.getPrincipal()));
    }

    @PostMapping("/register")
    TokenResponse register(@Valid @RequestBody RegisterRequest request) {
        if (users.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        users.save(new AppUser(request.username(), passwordEncoder.encode(request.password()), Set.of("ROLE_USER")));
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(request.username())
                .password("")
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                .build();
        return new TokenResponse(jwtService.generateToken(userDetails));
    }
}
