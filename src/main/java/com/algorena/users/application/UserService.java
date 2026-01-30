package com.algorena.users.application;

import com.algorena.users.dto.UpdateUserRequest;
import com.algorena.users.dto.UserDTO;

public interface UserService {
    /**
     * Retrieves the currently authenticated user's details.
     *
     * @return UserDTO containing the current user's id, username, and name
     */
    UserDTO getCurrentUserDetails();

    /**
     * Updates the currently authenticated user's profile.
     *
     * @param request the update request containing username and/or name
     * @return UserDTO containing the updated user details
     */
    UserDTO updateCurrentUser(UpdateUserRequest request);
}
