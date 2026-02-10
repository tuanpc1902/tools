package com.example.crud.controller;

import com.example.crud.dto.UserDTO;
import com.example.crud.dto.AddressDTO;
import com.example.crud.dto.UserProfileDTO;
import com.example.crud.dto.UserListRequest;
import com.example.crud.service.AddressService;
import com.example.crud.service.RoleService;
import com.example.crud.service.UserListService;
import com.example.crud.service.UserProfileService;
import com.example.crud.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller - API layer
 * Xử lý HTTP requests và responses
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserProfileService userProfileService;
    private final AddressService addressService;
    private final RoleService roleService;
    private final UserListService userListService;

    @Autowired
    public UserController(UserService userService,
                          UserProfileService userProfileService,
                          AddressService addressService,
                          RoleService roleService,
                          UserListService userListService) {
        this.userService = userService;
        this.userProfileService = userProfileService;
        this.addressService = addressService;
        this.roleService = roleService;
        this.userListService = userListService;
    }

    /**
     * CREATE - POST /api/users
     * Tạo mới user
     */
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        UserDTO createdUser = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * READ - GET /api/users/{id}
     * Lấy user theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * READ ALL - GET /api/users
     * Lấy tất cả users
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * UPDATE - PUT /api/users/{id}
     * Cập nhật user
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * DELETE - DELETE /api/users/{id}
     * Xóa user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * SEARCH - GET /api/users/search?name={name}
     * Tìm kiếm users theo tên
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String name) {
        List<UserDTO> users = userService.searchUsersByName(name);
        return ResponseEntity.ok(users);
    }

    /**
     * DYNAMIC SEARCH - GET /api/users/search-dynamic?name={name}&email={email}&phone={phone}
     * Tìm kiếm users với nhiều điều kiện linh động
     */
    @GetMapping("/search-dynamic")
    public ResponseEntity<List<UserDTO>> searchUsersDynamic(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone) {
        List<UserDTO> users = userService.searchUsersDynamic(name, email, phone);
        return ResponseEntity.ok(users);
    }

    /**
     * PAGINATION - GET /api/users/paginated?page={page}&size={size}
     * Lấy users với phân trang
     */
    @GetMapping("/paginated")
    public ResponseEntity<List<UserDTO>> getUsersWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<UserDTO> users = userService.getAllUsersWithPagination(page, size);
        return ResponseEntity.ok(users);
    }

    /**
     * LIST PAGE - GET /api/users/page?excludeTestData={true|false}
     * Lấy user theo config_key và loại bỏ dữ liệu test nếu được bật.
     */
    @GetMapping("/page")
    public ResponseEntity<List<UserDTO>> getUsersPage(
            @ModelAttribute UserListRequest request,
            @RequestParam(defaultValue = "false") boolean excludeTestData) {
        return ResponseEntity.ok(userListService.getUsersForPage(request, excludeTestData));
    }

    /**
     * COUNT - GET /api/users/count
     * Đếm số lượng users
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getUserCount() {
        long count = userService.getUserCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userProfileService.getProfile(id));
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<UserProfileDTO> upsertProfile(
            @PathVariable Long id,
            @Valid @RequestBody UserProfileDTO profileDTO) {
        return ResponseEntity.ok(userProfileService.upsertProfile(id, profileDTO));
    }

    @GetMapping("/{id}/addresses")
    public ResponseEntity<List<AddressDTO>> getAddresses(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.getAddresses(id));
    }

    @PostMapping("/{id}/addresses")
    public ResponseEntity<AddressDTO> addAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressDTO addressDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.addAddress(id, addressDTO));
    }

    @PutMapping("/{id}/addresses/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(
            @PathVariable Long id,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressDTO addressDTO) {
        return ResponseEntity.ok(addressService.updateAddress(id, addressId, addressDTO));
    }

    @DeleteMapping("/{id}/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long id,
            @PathVariable Long addressId) {
        addressService.deleteAddress(id, addressId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/roles/{roleCode}")
    public ResponseEntity<Void> assignRole(
            @PathVariable Long id,
            @PathVariable String roleCode) {
        roleService.assignRoleToUser(id, roleCode);
        return ResponseEntity.noContent().build();
    }
}
