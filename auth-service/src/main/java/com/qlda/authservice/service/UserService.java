package com.qlda.authservice.service;

import com.qlda.authservice.constant.PredefinedRole;
import com.qlda.authservice.dto.request.UserCreateRequest;
import com.qlda.authservice.dto.request.UserUpdateRequest;
import com.qlda.authservice.dto.response.UserResponse;
import com.qlda.authservice.entity.Role;
import com.qlda.authservice.entity.User;
import com.qlda.authservice.exception.AppException;
import com.qlda.authservice.exception.ErrorCode;
import com.qlda.authservice.mapper.UserMapper;
import com.qlda.authservice.repository.RoleRepository;
import com.qlda.authservice.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreateRequest userCreateRequest){
        log.info("Start creating user with username: {} , {}", userCreateRequest.getUsername() , userCreateRequest.getEmail());

        try {
            User user = userMapper.toUserEntity(userCreateRequest);
            user.setPassword(passwordEncoder.encode(userCreateRequest.getPassword()));
            HashSet<Role> roles = new HashSet<>();
            roleRepository.findById(PredefinedRole.USER_ROLE).ifPresentOrElse(
                    role -> {
                        roles.add(role);
                        log.debug("Role USER found and added");
                    },
                    () -> {
                        log.error("Role USER not found");
                        throw new AppException(ErrorCode.ROLE_NOT_FOUND);
                    }
            );
            user.setRoles(roles);
            user.setEmail(userCreateRequest.getEmail());
            user.setEnabled(false);
            user = userRepository.save(user);
            UserResponse response = userMapper.toUserResponse(user);
            return response;

        } catch (DataIntegrityViolationException e) {
            log.error("DataIntegrityViolationException when saving user: {}", e.getMessage());
            throw new AppException(ErrorCode.USER_EXISTED);
        } catch (Exception e) {
            log.error("Unexpected error in createUser: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public UserResponse updateUser(String userId, UserUpdateRequest userUpdateRequest){
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_EXISTED));
        log.info("Updating user with ID: {}", userId);
        userMapper.updateUser(user, userUpdateRequest);
        user.setPassword(passwordEncoder.encode(userUpdateRequest.getPassword()));
        var roles = roleRepository.findAllById(userUpdateRequest.getRoles());
        if (roles.isEmpty()) {
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }
        user.setRoles(new HashSet<>(roles));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteUser(String userId) {
        log.info("Deleting user with ID: {}", userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        userRepository.delete(user);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UserResponse> getAllUsers(){
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();
        return users.stream().map(userMapper::toUserResponse).toList();
    }

    @PreAuthorize(("hasAuthority('ADMIN')"))
    public UserResponse getUserById(String userId) {
        log.info("Fetching user with ID: {}", userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

}
