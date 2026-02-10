package com.example.crud;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration Test cho Spring Boot Application
 * Test xem application có start được không và context có load đúng không
 */
@SpringBootTest
@ActiveProfiles("test")
class CrudApplicationTests {

    @Test
    void contextLoads() {
        // Test xem Spring context có load được không
        // Nếu test này pass nghĩa là:
        // - Application configuration đúng
        // - Tất cả beans được tạo thành công
        // - Database connection OK
    }
}
