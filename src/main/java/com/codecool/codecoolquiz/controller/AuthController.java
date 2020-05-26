package com.codecool.codecoolquiz.controller;

import com.codecool.codecoolquiz.model.AppUser;
import com.codecool.codecoolquiz.model.RequestResponseBody.SignInResponseBody;
import com.codecool.codecoolquiz.model.UserCredentials;
import com.codecool.codecoolquiz.model.exception.EmailAlreadyExistException;
import com.codecool.codecoolquiz.model.exception.SignOutException;
import com.codecool.codecoolquiz.model.exception.SignUpException;
import com.codecool.codecoolquiz.model.exception.UsernameAlreadyExistException;
import com.codecool.codecoolquiz.security.JwtTokenServices;
import com.codecool.codecoolquiz.service.AppUserStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${jwt.expiration.minutes:60}")
    private long cookieMaxAgeMinutes;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenServices jwtTokenServices;

    @Autowired
    AppUserStorage appUserStorage;

    @PostMapping("/sign-up")
    public ResponseEntity signUp(@RequestBody UserCredentials userCredentials) {
        try {
            appUserStorage.signUp(userCredentials);
            return ResponseEntity.ok().body(userCredentials.getUsername());
        } catch (EmailAlreadyExistException e) {
            return ResponseEntity.status(409).body(SignUpException.EMAIL);
        } catch (UsernameAlreadyExistException e) {
            return ResponseEntity.status(409).body(SignUpException.USERNAME);
        }
    }

    @PostMapping("/sign-in")
    public ResponseEntity signIn(@RequestBody UserCredentials data, HttpServletResponse response) {
        try {
            String username = data.getUsername();
            // authenticationManager.authenticate calls loadUserByUsername in CustomUserDetailsService
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, data.getPassword()));
            List<String> roles = authentication.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            String token = jwtTokenServices.generateToken(authentication);
            addTokenToCookie(response, token);
            AppUser user = appUserStorage.getByName(username);
            SignInResponseBody signInBody = new SignInResponseBody(username, roles, user.getId(), cookieMaxAgeMinutes);
            return ResponseEntity.ok().body(signInBody);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(403).build();
        }
    }

    private void addTokenToCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("token", token)
                .domain("localhost") // should be parameterized
                .sameSite("Strict")  // CSRF
//                .secure(true)
                .maxAge(Duration.ofMinutes(cookieMaxAgeMinutes))
                .httpOnly(true)      // XSS
                .path("/")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void eraseCookie(HttpServletResponse response, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            cookie.setPath("/");
            cookie.setMaxAge(0);
            cookie.setValue("");
            response.addCookie(cookie);
        }
    }

    @PostMapping("/sign-out")
    public ResponseEntity signOut(HttpServletResponse response, HttpServletRequest request) {
        try {
            eraseCookie(response, request);
            return ResponseEntity.status(200).build();
        } catch (SignOutException e) {
            return ResponseEntity.status(403).build();
        }
    }
}
