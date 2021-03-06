package com.codecool.codecoolquiz.service;

import com.codecool.codecoolquiz.model.AppUser;
import com.codecool.codecoolquiz.model.RequestResponseBody.UserResponseBody;
import com.codecool.codecoolquiz.model.UserCredentials;
import com.codecool.codecoolquiz.model.exception.EmailAlreadyExistsException;
import com.codecool.codecoolquiz.model.exception.NotFoundException;
import com.codecool.codecoolquiz.model.exception.UsernameAlreadyExistException;
import com.codecool.codecoolquiz.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppUserStorage {

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    AppUserRepository appUserRepository;

    @Autowired
    EmailSenderService emailSenderService;

    public AppUser find(int id) throws NotFoundException {
        return appUserRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found."));
    }

    public UserResponseBody getUserResponseBodyById(int id) {
        AppUser user = find(id);
        return new UserResponseBody(user);
    }

    public void add(AppUser appUser) {
        appUserRepository.save(appUser);
    }

    public AppUser getByName(String name) {
        return appUserRepository.findByUsername(name)
                .orElseThrow(() -> new UsernameNotFoundException("Username is not found"));
    }

    public boolean signUp(UserCredentials userCredentials) throws AuthenticationException {

        String username = userCredentials.getUsername();
        String email = userCredentials.getEmail();

        if (appUserRepository.findByUsername(username).isPresent()) {
            throw new UsernameAlreadyExistException();
        }
        if (appUserRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException();
        }
        appUserRepository.save(AppUser
                .builder()
                .username(username)
                .password(encoder.encode(userCredentials.getPassword()))
                .role("USER")
                .email(email)
                .registrationDate(LocalDate.now())
                .build()
        );
        emailSenderService.sendEmail(email, username);
        return true;
    }

    public List<UserResponseBody> getUsers() {
        return appUserRepository.findAll().stream().map((user)-> new UserResponseBody(user)).collect(Collectors.toList());
    }
}

