package com.algorena.users.application;

import com.algorena.common.exception.DataNotFoundException;
import com.algorena.security.CurrentUser;
import com.algorena.users.data.UserRepository;
import com.algorena.users.domain.User;
import com.algorena.users.dto.UserDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    /**
     * Retrieves the currently authenticated user's details.
     *
     * @return UserDTO containing the current user's id, username, and name
     */
    public UserDTO getCurrentUserDetails() {
        User user = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new DataNotFoundException("User not found with id: " + currentUser.id()));
        return new UserDTO(user.getId(), user.getUsername(), user.getName());
    }
}

