package com.example.crud.service;

import com.example.crud.dto.UserDTO;
import com.example.crud.dto.UserListRequest;
import com.example.crud.repository.UserListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserListService {

    private final UserListRepository userListRepository;
    private final ConfigService configService;

    public UserListService(UserListRepository userListRepository, ConfigService configService) {
        this.userListRepository = userListRepository;
        this.configService = configService;
    }

    public List<UserDTO> getUsersForPage(UserListRequest request, boolean excludeTestData) {
        UserListRequest safeRequest = request != null ? request : new UserListRequest();
        if (configService.isExcludeTestDataEnabled()) {
            return userListRepository.fetchUsersExcludeTest(safeRequest, excludeTestData);
        }
        return userListRepository.fetchUsersLegacy(safeRequest);
    }
}
