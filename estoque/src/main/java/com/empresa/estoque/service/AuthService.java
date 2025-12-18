package com.empresa.estoque.service;

import com.empresa.estoque.dto.LoginRequestDTO;
import com.empresa.estoque.security.CustomUserDetails;
import com.empresa.estoque.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

//@Service
@RequiredArgsConstructor

public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public String login(LoginRequestDTO dto) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.username(),
                        dto.password()
                )
        );

        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

        return jwtUtil.generateToken(user.getUsername());
    }
}
