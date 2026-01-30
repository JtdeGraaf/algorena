package com.algorena.users.controllers;


import com.algorena.users.application.UserService;
import com.algorena.users.dto.UpdateUserRequest;
import com.algorena.users.dto.UserDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Updates the currently authenticated user's profile.
     *
     * @param request the update request containing username and/or name
     * @return ResponseEntity containing the updated user profile
     */
    @PatchMapping("/me")
    public ResponseEntity<UserDTO> updateCurrentUserProfile(@Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateCurrentUser(request));
    }
}
