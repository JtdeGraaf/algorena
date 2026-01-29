package com.algorena.users.controllers;


import com.algorena.users.application.UserService;
import com.algorena.users.dto.UserDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserController {
    private final UserService userService;

    /**
     * Retrieves the currently authenticated user's full profile.
     * This is the ONLY endpoint that returns sensitive information like email.
     *
     * @return ResponseEntity containing the current user's complete profile including email
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUserProfile() {
        return ResponseEntity.ok(userService.getCurrentUserDetails());
    }
    
}
