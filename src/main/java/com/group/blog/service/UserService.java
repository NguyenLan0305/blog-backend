package com.group.blog.service;

import com.group.blog.dto.request.PasswordChangeRequest;
import com.group.blog.dto.request.UserCreatetionRequest;
import com.group.blog.dto.request.UserUpdateRequest;
import com.group.blog.dto.response.UserResponse;
import com.group.blog.entity.User;
import com.group.blog.enums.Role;
import com.group.blog.exception.AppException;
import com.group.blog.exception.ErrorCode;
import com.group.blog.mapper.UserMapper;
import com.group.blog.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE,makeFinal=true)
public class UserService {

 UserRepository userRepository;
 UserMapper userMapper;
 PasswordEncoder passwordEncoder;

 // 🔥 SỬA Ở ĐÂY 1: Tiêm FollowService vào để lấy thông số Follow
 FollowService followService;

 public UserResponse createUser(UserCreatetionRequest request){
  if(userRepository.existsByUsername(request.getUsername())) throw new AppException(ErrorCode.USER_EXITED);
  User u=userMapper.toUser(request);
  u.setPassword(passwordEncoder.encode(request.getPassword()));
  u.getRoles().add(Role.USER.name());
  User savedUser=userRepository.save(u);
  return userMapper.toUserResponse(savedUser); // User mới tạo mặc định chưa có follow, không cần enrich
 }

 public UserResponse updateUser(UUID id, UserUpdateRequest request){
  User u = userRepository.findById(id)
          .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

  // 1. Vẫn cho Mapper tự cập nhật email, bio...
  userMapper.updateUser(u, request);

  // 2. 🔥 TỰ TAY CẬP NHẬT QUYỀN: Xóa sạch quyền cũ và nạp quyền mới vào
  if (request.getRoles() != null && !request.getRoles().isEmpty()) {
   u.getRoles().clear(); // Rất quan trọng: Xóa quyền USER cũ đi
   u.getRoles().addAll(request.getRoles()); // Đắp quyền ADMIN mới vào
  }

  // 3. Lưu vào Database
  return followService.enrichUserResponse(userRepository.save(u));
 }

 public void deleteUser(UUID id){
  if(!userRepository.existsById(id)) throw new AppException(ErrorCode.USER_NOT_EXITED);
  userRepository.deleteById(id);
 }

 public List<UserResponse> getUsers(){
  // 🔥 SỬA Ở ĐÂY 3
  return userRepository.findAll().stream().map(followService::enrichUserResponse).toList();
 }

 public UserResponse getUser(UUID id){
  // 🔥 SỬA Ở ĐÂY 4
  return followService.enrichUserResponse(userRepository.findById(id).orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXITED)));
 }

 public void changePassword(PasswordChangeRequest request) {
  // 1. Lấy ra username của người ĐANG ĐĂNG NHẬP từ cái Token
  var context = SecurityContextHolder.getContext();
  String currentUsername = context.getAuthentication().getName();

  // 2. Lấy User từ Database lên
  User user = userRepository.findByUsername(currentUsername)
          .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

  // 3. So sánh: Mật khẩu cũ (nhập vào) có khớp với mật khẩu đang lưu trong DB không?
  boolean isMatch = passwordEncoder.matches(request.getOldPassword(), user.getPassword());
  if (!isMatch) {
   throw new AppException(ErrorCode.PASSWORD_INCORRECT);
  }

  // 4. Kiểm tra: Mật khẩu mới và Nhập lại mật khẩu có giống nhau không?
  if (!request.getNewPassword().equals(request.getConfirmPassword())) {
   throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
  }

  // 5. Mọi thứ OK -> Băm (hash) mật khẩu mới và lưu đè lên mật khẩu cũ
  user.setPassword(passwordEncoder.encode(request.getNewPassword()));
  userRepository.save(user);
 }

 // Lấy thông tin cá nhân
 public UserResponse getMyProfile() {
  var context = SecurityContextHolder.getContext();
  String currentUsername = context.getAuthentication().getName();

  User user = userRepository.findByUsername(currentUsername)
          .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

  // 🔥 SỬA Ở ĐÂY 5: Gắn kèm các chỉ số Follower, Following
  return followService.enrichUserResponse(user);
 }

 // Cập nhật thông tin cá nhân
 @Transactional
 public UserResponse updateMyProfile(UserUpdateRequest request) {
  var context = SecurityContextHolder.getContext();
  String currentUsername = context.getAuthentication().getName();

  User user = userRepository.findByUsername(currentUsername)
          .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

  // Cập nhật các trường được phép
  if (request.getEmail() != null) user.setEmail(request.getEmail());
  if (request.getBio() != null) user.setBio(request.getBio());
  if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());

  // 🔥 SỬA Ở ĐÂY 6
  return followService.enrichUserResponse(userRepository.save(user));
 }

 // Lấy Profile public bằng username
 public UserResponse getUserByUsername(String username) {
  User user = userRepository.findByUsername(username)
          .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));

  // 🔥 SỬA Ở ĐÂY 7: Gắn thông số & check xem User đăng nhập đã follow tác giả này chưa
  return followService.enrichUserResponse(user);
 }

 public long countTotalUsers() {
  return userRepository.count();
 }
}