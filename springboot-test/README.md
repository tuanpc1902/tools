# Spring Boot CRUD Application với MySQL và JdbcTemplate

Ứng dụng Spring Boot CRUD hoàn chỉnh sử dụng **MySQL**, **JdbcTemplate**, và **Stored Procedures** với các test cases chi tiết...

## 📋 Mục Lục

1. [Tổng Quan](#tổng-quan)
2. [Cấu Trúc Project](#cấu-trúc-project)
3. [Các Thành Phần Chính](#các-thành-phần-chính)
4. [CRUD Operations](#crud-operations)
5. [Stored Procedures](#stored-procedures)
6. [Dynamic Queries](#dynamic-queries)
7. [Testing](#testing)
8. [Cách Chạy](#cách-chạy)
9. [API Endpoints](#api-endpoints)

## 🎯 Tổng Quan

Đây là một ứng dụng Spring Boot đầy đủ với:
- **Database**: MySQL với Stored Procedures
- **Data Access**: JdbcTemplate và NamedParameterJdbcTemplate
- **Entity Layer**: User entity (không dùng JPA)
- **Repository Layer**: JdbcTemplate Repository với dynamic queries
- **Service Layer**: Business logic và CRUD operations
- **Controller Layer**: REST API endpoints
- **Testing**: Unit tests, Integration tests với H2 in-memory database
- **User List Page**: Config-driven exclude test data (account_test) và sắp xếp theo level

## 📁 Cấu Trúc Project

```
springboot-test/
├── src/
│   ├── main/
│   │   ├── java/com/example/crud/
│   │   │   ├── CrudApplication.java          # Main application class
│   │   │   ├── entity/
│   │   │   │   └── User.java                 # Entity class (không dùng JPA)
│   │   │   ├── dto/
│   │   │   │   ├── UserDTO.java              # Data Transfer Object
│   │   │   │   └── UserListRequest.java      # Request DTO cho page list
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java       # JdbcTemplate Repository
│   │   │   │   ├── UserListRepository.java   # List page queries (SP giả lap)
│   │   │   │   └── ConfigRepository.java     # app_config access
│   │   │   ├── service/
│   │   │   │   ├── UserService.java          # Service layer
│   │   │   │   ├── UserListService.java      # List page logic
│   │   │   │   └── ConfigService.java        # config_key logic
│   │   │   └── controller/
│   │   │       └── UserController.java       # REST Controller
│   │   └── resources/
│   │       ├── application.properties        # Application configuration
│   │       └── db/
│   │           ├── schema.sql                # Database schema
│   │           └── stored-procedures.sql     # Stored Procedures
│   └── test/
│       ├── java/com/example/crud/
│       │   ├── CrudApplicationTests.java     # Application context test
│       │   ├── service/
│       │   │   ├── UserServiceTest.java      # Unit tests cho Service
│       │   │   └── UserListServiceTest.java  # Unit tests cho list page
│       │   ├── controller/
│       │   │   └── UserControllerTest.java   # Integration tests cho Controller
│       │   ├── repository/
│       │   │   ├── UserRepositoryTest.java   # Integration tests cho Repository
│       │   │   └── UserListRepositoryTest.java # JDBC tests cho list page
│       │   └── integration/
│       │       └── UserIntegrationTest.java   # End-to-End tests
│       └── resources/
│           ├── application-test.properties    # Test configuration
│           └── schema-test.sql               # Test schema (H2)
├── pom.xml                                    # Maven dependencies
├── README.md                                  # Documentation
└── SETUP.md                                   # Setup guide
```

## 🏗️ Các Thành Phần Chính

### 1. Entity Layer (`User.java`)

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String status;
    private String levelCode;
    private Boolean isTest;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**Đặc điểm:**
- Không sử dụng JPA annotations
- Chỉ là POJO (Plain Old Java Object)
- Validation annotations vẫn được giữ lại cho DTO

### 2. Repository Layer (`UserRepository.java`)

**Sử dụng JdbcTemplate và Stored Procedures:**

```java
@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    
    // CREATE với Stored Procedure
    public User createUser(User user) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_create_user");
        // ...
    }
    
    // Dynamic query với NamedParameterJdbcTemplate
    public List<User> findUsersDynamic(String name, String email, String phone) {
        // Build query động dựa trên parameters
    }
}
```

**Đặc điểm:**
- Sử dụng `JdbcTemplate` cho queries đơn giản
- Sử dụng `NamedParameterJdbcTemplate` cho dynamic queries
- Sử dụng `SimpleJdbcCall` để gọi Stored Procedures
- Hỗ trợ cả Stored Procedures và direct SQL queries

### 3. Service Layer (`UserService.java`)

**Các phương thức CRUD:**

- `createUser(UserDTO)` - Tạo mới user (dùng Stored Procedure)
- `createUserWithJdbcTemplate(UserDTO)` - Tạo mới user (dùng JdbcTemplate)
- `getUserById(Long)` - Lấy user theo ID
- `getAllUsers()` - Lấy tất cả users
- `getAllUsersWithPagination(int, int)` - Lấy users với phân trang
- `updateUser(Long, UserDTO)` - Cập nhật user
- `deleteUser(Long)` - Xóa user
- `searchUsersByName(String)` - Tìm kiếm theo tên
- `searchUsersDynamic(String, String, String)` - Tìm kiếm động với nhiều điều kiện

## 🔄 CRUD Operations

### CREATE (Tạo mới)

**Sử dụng Stored Procedure:**
```java
public UserDTO createUser(UserDTO userDTO) {
    if (userRepository.existsByEmail(userDTO.getEmail())) {
        throw new RuntimeException("Email đã tồn tại");
    }
    User user = convertToEntity(userDTO);
    User savedUser = userRepository.createUser(user); // Gọi SP
    return convertToDTO(savedUser);
}
```

**Sử dụng JdbcTemplate:**
```java
public UserDTO createUserWithJdbcTemplate(UserDTO userDTO) {
    User user = convertToEntity(userDTO);
    User savedUser = userRepository.createUserWithJdbcTemplate(user);
    return convertToDTO(savedUser);
}
```

### READ (Đọc)

```java
// Lấy theo ID
public UserDTO getUserById(Long id) {
    User user = userRepository.getUserById(id)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    return convertToDTO(user);
}

// Lấy tất cả với phân trang
public List<UserDTO> getAllUsersWithPagination(int page, int size) {
    List<User> users = userRepository.findAllWithPagination(page, size);
    return users.stream().map(this::convertToDTO).collect(Collectors.toList());
}
```

### UPDATE (Cập nhật)

```java
public UserDTO updateUser(Long id, UserDTO userDTO) {
    if (!userRepository.existsById(id)) {
        throw new RuntimeException("Không tìm thấy user");
    }
    
    User user = convertToEntity(userDTO);
    user.setId(id);
    User updatedUser = userRepository.updateUser(user); // Gọi SP
    return convertToDTO(updatedUser);
}
```

### DELETE (Xóa)

```java
public void deleteUser(Long id) {
    if (!userRepository.existsById(id)) {
        throw new RuntimeException("Không tìm thấy user");
    }
    userRepository.deleteUser(id); // Gọi SP
}
```

## 🗄️ Stored Procedures

### Danh Sách Stored Procedures

1. **sp_create_user** - Tạo mới user
   ```sql
   CALL sp_create_user('Nguyễn Văn A', 'email@example.com', '0123456789', @user_id);
   ```

2. **sp_get_user_by_id** - Lấy user theo ID
   ```sql
   CALL sp_get_user_by_id(1);
   ```

3. **sp_get_all_users** - Lấy tất cả users
   ```sql
   CALL sp_get_all_users();
   ```

4. **sp_update_user** - Cập nhật user
   ```sql
   CALL sp_update_user(1, 'Tên mới', 'email@example.com', '0987654321');
   ```

5. **sp_delete_user** - Xóa user
   ```sql
   CALL sp_delete_user(1);
   ```

6. **sp_search_users** - Tìm kiếm users với nhiều điều kiện
   ```sql
   CALL sp_search_users('Nguyễn', NULL, NULL, 0, 10);
   ```

7. **sp_get_user_count** - Đếm số lượng users
   ```sql
   CALL sp_get_user_count('Nguyễn', NULL, NULL, @total);
   ```

### Tạo Stored Procedures

Chạy file `src/main/resources/db/stored-procedures.sql` trong MySQL:

```bash
mysql -u root -p crud_db < src/main/resources/db/stored-procedures.sql
```

## 🔍 Dynamic Queries

### Tìm Kiếm Động với NamedParameterJdbcTemplate

```java
public List<User> findUsersDynamic(String name, String email, String phone) {
    StringBuilder sql = new StringBuilder(
        "SELECT id, name, email, phone, created_at, updated_at FROM users WHERE 1=1");
    MapSqlParameterSource params = new MapSqlParameterSource();

    if (name != null && !name.isEmpty()) {
        sql.append(" AND LOWER(name) LIKE LOWER(:name)");
        params.addValue("name", "%" + name + "%");
    }

    if (email != null && !email.isEmpty()) {
        sql.append(" AND email LIKE :email");
        params.addValue("email", "%" + email + "%");
    }

    if (phone != null && !phone.isEmpty()) {
        sql.append(" AND phone LIKE :phone");
        params.addValue("phone", "%" + phone + "%");
    }

    return namedParameterJdbcTemplate.query(sql.toString(), params, 
        BeanPropertyRowMapper.newInstance(User.class));
}
```

**Ưu điểm:**
- Query được build động dựa trên parameters
- Tránh SQL injection với named parameters
- Linh động trong việc thêm/bớt điều kiện

## 🧪 Testing

### Vì Sao Cần Test Thật Chi Tiết

- Hành vi phụ thuộc config (`exclude_test_data_enabled`) có thể đổi luồng logic, dễ gây leak dữ liệu test nếu không có test rõ ràng.
- Dữ liệu đầu vào không kiểm soát được (page/size âm, filter rỗng, null request) dễ gây lỗi runtime hoặc trả về sai dữ liệu.
- Luồng “exclude test data” ảnh hưởng trực tiếp dữ liệu hiển thị cho khách hàng, cần test để tránh hiển thị account_test ngoài ý muốn.
- Các tầng (Controller/Service/Repository) có trách nhiệm khác nhau, test chi tiết giúp bắt lỗi sớm ở đúng layer.
- Test unhappy case giúp phát hiện lỗi và lỗ hổng sớm hơn production.

### 1. Unit Tests (`UserServiceTest.java`)

Sử dụng Mockito để mock Repository:

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void testCreateUser_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.createUser(any(User.class))).thenReturn(user);
        // ...
    }
}
```

### 2. Repository Integration Tests (`UserRepositoryTest.java`)

Sử dụng `@JdbcTest` với H2 in-memory database:

```java
@JdbcTest
@Import(UserRepository.class)
@ActiveProfiles("test")
class UserRepositoryTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void testCreateUserWithJdbcTemplate_Success() {
        User savedUser = userRepository.createUserWithJdbcTemplate(user);
        assertNotNull(savedUser.getId());
    }
}
```

### 3. Controller Integration Tests (`UserControllerTest.java`)

Sử dụng `@WebMvcTest` và `MockMvc`:

```java
@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
    
    @Test
    void testCreateUser_Success() throws Exception {
        when(userService.createUser(any(UserDTO.class))).thenReturn(userDTO);
        
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
            .andExpect(status().isCreated());
    }
}
```

### 4. List Page Tests (Exclude Test Data)

Test config_key + excludeTestData flow:
- `UserListServiceTest` dùng `@ExtendWith(MockitoExtension.class)` và `@ParameterizedTest`.
- `UserListRepositoryTest` dùng `@JdbcTest` để test JdbcTemplate query, có account_test.

## 🚀 Cách Chạy

### 1. Yêu Cầu

- Java 17+
- Maven 3.6+
- MySQL 8.0+

### 2. Setup Database

Xem file [SETUP.md](SETUP.md) để biết chi tiết.

```bash
# Tạo database và tables
mysql -u root -p < src/main/resources/db/schema.sql

# Tạo stored procedures
mysql -u root -p crud_db < src/main/resources/db/stored-procedures.sql
```

### 3. Cấu Hình Application

Cập nhật `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/crud_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&characterEncoding=utf8mb4
spring.datasource.username=root
spring.datasource.password=your_password
```

### 4. Build và Chạy

```bash
# Build project
mvn clean install

# Chạy application
mvn spring-boot:run
```

### 5. Chạy Tests

```bash
# Chạy tất cả tests (sử dụng H2 in-memory)
mvn test

# Chạy test cụ thể
mvn test -Dtest=UserServiceTest
```

## 📡 API Endpoints

### CREATE User

```bash
POST http://localhost:8080/api/users
Content-Type: application/json

{
  "name": "Nguyễn Văn A",
  "email": "nguyenvana@example.com",
  "phone": "0123456789"
}
```

### GET User by ID

```bash
GET http://localhost:8080/api/users/1
```

### GET All Users

```bash
GET http://localhost:8080/api/users
```

### GET Users with Pagination

```bash
GET http://localhost:8080/api/users/paginated?page=0&size=10
```

### GET Users Page (Exclude Test Data)

```bash
GET http://localhost:8080/api/users/page?excludeTestData=true&page=0&size=10
```

Logic:
- `exclude_test_data_enabled = true` -> dùng SP mới (16 params) và loại bỏ `account_test` nếu `excludeTestData=true`.
- `exclude_test_data_enabled = false` -> dùng SP cũ (15 params), luôn trả về cả account_test.

### UPDATE User

```bash
PUT http://localhost:8080/api/users/1
Content-Type: application/json

{
  "name": "Nguyễn Văn A Updated",
  "email": "nguyenvana@example.com",
  "phone": "0111111111"
}
```

### DELETE User

```bash
DELETE http://localhost:8080/api/users/1
```

### SEARCH Users by Name

```bash
GET http://localhost:8080/api/users/search?name=Nguyễn
```

### DYNAMIC SEARCH Users

```bash
GET http://localhost:8080/api/users/search-dynamic?name=Nguyễn&email=example.com
```

### COUNT Users

```bash
GET http://localhost:8080/api/users/count
```

## 📚 Kiến Thức Quan Trọng

### 1. JdbcTemplate vs JPA

**JdbcTemplate:**
- ✅ Kiểm soát SQL queries hoàn toàn
- ✅ Performance tốt hơn (ít overhead)
- ✅ Linh động với stored procedures
- ✅ Dễ debug SQL queries
- ❌ Nhiều boilerplate code hơn
- ❌ Phải tự map ResultSet

**JPA:**
- ✅ Ít boilerplate code
- ✅ Tự động map objects
- ✅ Type-safe queries
- ❌ Khó kiểm soát SQL queries
- ❌ Performance overhead
- ❌ Khó làm việc với stored procedures

### 2. Stored Procedures vs Direct SQL

**Stored Procedures:**
- ✅ Logic nghiệp vụ ở database layer
- ✅ Performance tốt (pre-compiled)
- ✅ Bảo mật tốt hơn
- ✅ Tái sử dụng logic
- ❌ Khó maintain
- ❌ Khó test
- ❌ Khó version control

**Direct SQL:**
- ✅ Dễ maintain và test
- ✅ Version control tốt
- ✅ Linh động hơn
- ❌ Logic phân tán
- ❌ Có thể có SQL injection nếu không cẩn thận

### 3. Dynamic Queries

Sử dụng `NamedParameterJdbcTemplate` để:
- Tránh SQL injection
- Build queries động
- Dễ đọc và maintain

## 🎓 Bài Tập Thực Hành

1. Thêm field `age` vào User và cập nhật stored procedures
2. Tạo stored procedure `sp_get_users_by_age_range`
3. Thêm sorting và filtering vào dynamic search
4. Tạo batch insert với JdbcTemplate
5. Thêm transaction management cho multiple operations
6. Tạo stored procedure với OUT parameters
7. Implement caching cho frequently accessed data

## 📝 Ghi Chú

- Database production: MySQL
- Database testing: H2 in-memory (nhanh hơn MySQL)
- Tất cả stored procedures có error handling
- Dynamic queries sử dụng named parameters để tránh SQL injection
- Tests sử dụng H2 với MySQL compatibility mode
- `app_config` chứa `exclude_test_data_enabled` để bật/tắt logic exclude test data

## 🤝 Đóng Góp

Nếu bạn muốn cải thiện project này, hãy:
1. Fork project
2. Tạo feature branch
3. Commit changes
4. Push và tạo Pull Request

---

**Chúc bạn học tập tốt! 🚀**
#   t o o l s 
 
 