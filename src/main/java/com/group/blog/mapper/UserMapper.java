package com.group.blog.mapper;

import com.group.blog.dto.request.UserCreatetionRequest;
import com.group.blog.dto.request.UserUpdateRequest;
import com.group.blog.dto.response.UserResponse;
import com.group.blog.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreatetionRequest request);
    UserResponse toUserResponse(User user);
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
