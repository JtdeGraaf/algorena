package com.algorena.users.application;

import com.algorena.common.exception.BadRequestException;
import com.algorena.common.exception.DataNotFoundException;
import com.algorena.security.CurrentUser;
import com.algorena.users.data.UserRepository;
import com.algorena.users.domain.User;
import com.algorena.users.dto.UpdateUserRequest;
import com.algorena.users.dto.UserDTO;
import com.algorena.users.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final CurrentUser currentUser;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserDTO getCurrentUserDetails() {
        User user = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new DataNotFoundException("User not found with id: " + currentUser.id()));
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional
    public UserDTO updateCurrentUser(UpdateUserRequest request) {
        User user = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new DataNotFoundException("User not found with id: " + currentUser.id()));

        // Check if new username is already taken by another user
        if (request.username() != null
                && userRepository.existsByUsernameAndIdNot(request.username(), currentUser.id())) {
            throw new BadRequestException("Username already taken");
        }

        user.updateProfile(request.username(), request.name());

        user = userRepository.save(user);
        return userMapper.toDTO(user);
    }
}
