package com.example.crud.service;

import com.example.crud.dto.UserDTO;
import com.example.crud.entity.User;
import com.example.crud.exception.ConflictException;
import com.example.crud.exception.NotFoundException;
import com.example.crud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer - Business logic layer
 * Sử dụng JdbcTemplate Repository với Stored Procedures và dynamic queries
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * CREATE - Tạo mới user sử dụng Stored Procedure
     */
    public UserDTO createUser(UserDTO userDTO) {
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new ConflictException("Email đã tồn tại: " + userDTO.getEmail());
        }

        // Convert DTO sang Entity
        User user = convertToEntity(userDTO);
        
        // Lưu vào database sử dụng Stored Procedure
        User savedUser = userRepository.createUser(user);
        
        // Convert Entity sang DTO và return
        return convertToDTO(savedUser);
    }

    /**
     * CREATE - Tạo mới user sử dụng JdbcTemplate (alternative method)
     */
    public UserDTO createUserWithJdbcTemplate(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new ConflictException("Email đã tồn tại: " + userDTO.getEmail());
        }

        User user = convertToEntity(userDTO);
        User savedUser = userRepository.createUserWithJdbcTemplate(user);
        
        return convertToDTO(savedUser);
    }

    /**
     * READ - Lấy user theo ID sử dụng Stored Procedure
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy user với ID: " + id));
        return convertToDTO(user);
    }

    /**
     * READ ALL - Lấy tất cả users sử dụng Stored Procedure
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.getAllUsers();
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * READ ALL với Pagination
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsersWithPagination(int page, int size) {
        List<User> users = userRepository.findAllWithPagination(page, size);
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * UPDATE - Cập nhật user sử dụng Stored Procedure
     */
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        // Kiểm tra user có tồn tại không
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Không tìm thấy user với ID: " + id);
        }

        // Kiểm tra email mới có trùng với user khác không
        Optional<User> userWithEmail = userRepository.findByEmail(userDTO.getEmail());
        if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(id)) {
            throw new ConflictException("Email đã được sử dụng bởi user khác: " + userDTO.getEmail());
        }

        // Cập nhật thông tin
        User user = convertToEntity(userDTO);
        user.setId(id);
        
        // Lưu vào database sử dụng Stored Procedure
        User updatedUser = userRepository.updateUser(user);
        
        return convertToDTO(updatedUser);
    }

    /**
     * DELETE - Xóa user sử dụng Stored Procedure
     */
    public void deleteUser(Long id) {
        // Kiểm tra user có tồn tại không
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Không tìm thấy user với ID: " + id);
        }
        
        // Xóa user
        userRepository.deleteUser(id);
    }

    /**
     * Tìm users theo tên (dynamic query)
     */
    @Transactional(readOnly = true)
    public List<UserDTO> searchUsersByName(String name) {
        List<User> users = userRepository.findByNameContaining(name);
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Dynamic search - Tìm users với nhiều điều kiện linh động
     */
    @Transactional(readOnly = true)
    public List<UserDTO> searchUsersDynamic(String name, String email, String phone) {
        List<User> users = userRepository.findUsersDynamic(name, email, phone);
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Đếm số lượng users
     */
    @Transactional(readOnly = true)
    public long getUserCount() {
        return userRepository.count();
    }

    /**
     * Helper method: Convert Entity sang DTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setLevelCode(user.getLevelCode());
        dto.setIsTest(user.getIsTest());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setDeletedAt(user.getDeletedAt());
        return dto;
    }

    /**
     * Helper method: Convert DTO sang Entity
     */
    private User convertToEntity(UserDTO dto) {
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        user.setLevelCode(dto.getLevelCode() != null ? dto.getLevelCode() : "Player");
        user.setIsTest(dto.getIsTest() != null && dto.getIsTest());
        return user;
    }
}
