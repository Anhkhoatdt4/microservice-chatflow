package com.qlda.authservice.mapper;

import com.qlda.authservice.dto.request.UserCreateRequest;
import com.qlda.authservice.dto.request.UserUpdateRequest;
import com.qlda.authservice.dto.response.UserResponse;
import com.qlda.authservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    User toUserEntity(UserCreateRequest request);
    UserResponse toUserResponse(User user);
    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest userUpdateRequest);
}
