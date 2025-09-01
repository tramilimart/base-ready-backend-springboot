package com.lls.rbac.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JwtFilter extends OncePerRequestFilter {
    private static final Logger logger = LogManager.getLogger(JwtFilter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private UserDetailsService userDetailsService;

    private JwtUtil jwtUtil;

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            // Read JWT token from cookies instead of Authorization header
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwt-token".equals(cookie.getName())) {
                        String token = cookie.getValue();
                        String username = jwtUtil.validateToken(token);
                        
                        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                            
                            if (userDetails != null) {
                                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                                SecurityContextHolder.getContext().setAuthentication(auth);
                                logger.info("User authenticated: {} with authorities: {}", username, userDetails.getAuthorities());
                            }
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error processing JWT token from cookie: {}", e.getMessage());
            // Don't set authentication, let the request continue without authentication
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/") || path.startsWith("/user/") || path.startsWith("/session/") || 
               path.startsWith("/swagger-ui/") || path.startsWith("/api-docs/") ||
               path.equals("/swagger-ui.html");
    }
}