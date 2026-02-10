package com.algorena.users.mapper;

import com.algorena.users.domain.User;
import com.algorena.users.dto.UserDTO;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting User entities to DTOs.
 */
@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getOauthIdentity().getProvider(),
                user.getOauthIdentity().getProviderId()
        );
    }
}
