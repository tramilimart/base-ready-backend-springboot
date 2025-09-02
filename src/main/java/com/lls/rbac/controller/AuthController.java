package com.lls.rbac.controller;

import com.lls.rbac.dto.ApiResponse;
import com.lls.rbac.dto.LoginRequestDTO;
import com.lls.rbac.dto.RegisterRequestDTO;
import com.lls.rbac.entity.User;
import com.lls.rbac.jwt.JwtUtil;
import com.lls.rbac.repository.UserRepository;
import com.lls.rbac.security.CustomUserDetailsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private static final Logger logger = LogManager.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, UserDetailsService userDetailsService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequestDTO, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getUsername(), loginRequestDTO.getPassword())
            );
            logger.info("authentication: {}", authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails.getUsername());
            logger.info("userDetails: {}", userDetails);

            // Set JWT token as HTTP-only cookie
            Cookie jwtCookie = new Cookie("jwt-token", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false); // Set to true in production with HTTPS
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(2 * 60 * 60); // 2 hours in seconds
            response.addCookie(jwtCookie);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("username", userDetails.getUsername());
            responseBody.put("authorities", userDetails.getAuthorities());

            logger.info("response: {}", responseBody);

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        if (userRepository.existsByUsername(registerRequestDTO.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        if (userRepository.existsByEmail(registerRequestDTO.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        User user = new User();
        user.setUsername(registerRequestDTO.getUsername());
        user.setEmail(registerRequestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));
        user.setFirstName(registerRequestDTO.getFirstName());
        user.setMiddleName(registerRequestDTO.getMiddleName());
        user.setLastName(registerRequestDTO.getLastName());

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    @GetMapping(path = "/validate-token")
    public ResponseEntity<?> checkSession(HttpServletRequest request) {
        try {
            // Read JWT token from cookies
            Cookie[] cookies = request.getCookies();
            String token = null;

            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwt-token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }

            if (token != null) {
                return ApiResponse.body()
                        .success(true)
                        .status(HttpStatus.OK)
                        .build();
            }
            return ApiResponse.body()
                    .success(false)
                    .responseCode("INVALID_TOKEN")
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        } catch (Exception e) {
            return ApiResponse.body()
                    .success(false)
                    .responseCode("INVALID_TOKEN")
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
            // Read JWT token from cookies
            Cookie[] cookies = request.getCookies();
            String token = null;
            
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwt-token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }

            logger.info("getting profile info");
            
            if (token != null) {
                String username = jwtUtil.validateToken(token);
                
                if (username != null) {
                    User user = userRepository.findByUsername(username).orElse(null);
                    if (user != null) {
                        Map<String, Object> profile = new HashMap<>();
                        profile.put("username", user.getUsername());
                        profile.put("email", user.getEmail());
                        profile.put("firstName", user.getFirstName());
                        profile.put("middleName", user.getMiddleName());
                        profile.put("lastName", user.getLastName());

                        UserDetails userAuthority = userDetailsService.loadUserByUsername(username);
                        profile.put("authorities", userAuthority.getAuthorities());
                        return ApiResponse.body()
                                .success(true)
                                .data(profile)
                                .status(HttpStatus.OK)
                                .build();
                    }
                }
            }
            return ApiResponse.body()
                    .success(false)
                    .responseCode("INVALID_TOKEN")
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        } catch (Exception e) {
            return ApiResponse.body()
                    .success(false)
                    .responseCode("INVALID_TOKEN")
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Clear the JWT cookie by setting it to expire immediately
        Cookie jwtCookie = new Cookie("jwt-token", "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // Set to true in production with HTTPS
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // Expire immediately
        response.addCookie(jwtCookie);

        return ApiResponse.body()
                .success(true)
                .status(HttpStatus.OK)
                .build();
    }

}
