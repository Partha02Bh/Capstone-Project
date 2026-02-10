package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    // You will need to create a simple CustomUserDetailsService later,
    // for now, we will Mock this or wire it if you have Spring Security UserDetails
    // implemented.
    // I'll assume we are creating a standard flow.
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // 1. Look for "Bearer <token>"
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
                System.out.println("--> JWT Filter: Extracted Username: " + username);
            } catch (Exception e) {
                System.out.println("--> JWT Filter: Token extraction failed: " + e.getMessage());
            }
        } else {
            // Only log if header is missing on protected endpoints, otherwise it spams
            if (request.getRequestURI().startsWith("/api/accounts")) {
                System.out.println("--> JWT Filter: Authorization header missing or invalid for protected path: "
                        + request.getRequestURI());
            }
        }

        // 2. If we found a user but they aren't logged in yet...
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 3. Validate the token
            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 4. Log them in!
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("--> JWT Filter: Authentication Successful for user: " + username
                        + " with authorities: " + userDetails.getAuthorities());
            } else {
                System.out.println("--> JWT Filter: Token Validation Failed for user: " + username);
            }
        }
        chain.doFilter(request, response);
    }
}