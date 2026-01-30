package com.algorena.users.application;

import com.algorena.users.dto.UserDTO;

public interface UserService {
    /**
     * Retrieves the currently authenticated user's details.
     *
     * @return UserDTO containing the current user's id, username, and name
     */
    UserDTO getCurrentUserDetails();
}
