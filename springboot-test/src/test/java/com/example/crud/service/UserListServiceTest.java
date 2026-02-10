package com.example.crud.service;

import com.example.crud.dto.UserDTO;
import com.example.crud.dto.UserListRequest;
import com.example.crud.repository.UserListRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// @ExtendWith: bật Mockito cho JUnit 5 để inject mocks.
@ExtendWith(MockitoExtension.class)
@DisplayName("UserListService Unit Tests")
class UserListServiceTest {

    // @Mock: tạo mock cho dependency.
    @Mock
    private UserListRepository userListRepository;

    // @Mock: tạo mock cho dependency.
    @Mock
    private ConfigService configService;

    // @InjectMocks: inject các mock vào class cần test.
    @InjectMocks
    private UserListService userListService;

    // @ParameterizedTest: chạy cùng logic test với nhiều giá trị input.
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Config enabled -> gọi SP mới với excludeTestData")
    void getUsersForPage_ConfigEnabled_UsesNewProcedure(boolean excludeTestData) {
        // Test case: config_key = true thì luôn gọi SP mới, có param excludeTestData.
        UserListRequest request = new UserListRequest();
        when(configService.isExcludeTestDataEnabled()).thenReturn(true);
        when(userListRepository.fetchUsersExcludeTest(any(UserListRequest.class), eq(excludeTestData)))
                .thenReturn(List.of(new UserDTO()));

        List<UserDTO> result = userListService.getUsersForPage(request, excludeTestData);

        // Assert: danh sách trả về đúng số lượng do mock setup.
        assertEquals(1, result.size());
        verify(userListRepository, times(1)).fetchUsersExcludeTest(any(UserListRequest.class), eq(excludeTestData));
        verify(userListRepository, never()).fetchUsersLegacy(any(UserListRequest.class));
    }

    @Test
    @DisplayName("Config disabled -> gọi SP cũ, ignore excludeTestData")
    void getUsersForPage_ConfigDisabled_UsesLegacyProcedure() {
        // Test case: config_key = false thì gọi SP cũ, bỏ qua excludeTestData.
        UserListRequest request = new UserListRequest();
        when(configService.isExcludeTestDataEnabled()).thenReturn(false);
        when(userListRepository.fetchUsersLegacy(any(UserListRequest.class)))
                .thenReturn(List.of(new UserDTO(), new UserDTO()));

        List<UserDTO> result = userListService.getUsersForPage(request, true);

        // Assert: danh sách trả về đúng số lượng do mock setup.
        assertEquals(2, result.size());
        verify(userListRepository, times(1)).fetchUsersLegacy(any(UserListRequest.class));
        verify(userListRepository, never()).fetchUsersExcludeTest(any(UserListRequest.class), anyBoolean());
    }

    @Test
    @DisplayName("Null request -> dùng default request, không NPE")
    void getUsersForPage_NullRequest_Defaults() {
        // Test case: service có thể được gọi từ nhiều nơi, cần an toàn khi request null.
        when(configService.isExcludeTestDataEnabled()).thenReturn(true);
        when(userListRepository.fetchUsersExcludeTest(any(UserListRequest.class), eq(true)))
                .thenReturn(List.of());

        List<UserDTO> result = userListService.getUsersForPage(null, true);

        // Assert: null request vẫn chạy và trả về list rỗng.
        assertEquals(0, result.size());
        verify(userListRepository, times(1)).fetchUsersExcludeTest(any(UserListRequest.class), eq(true));
    }
}
