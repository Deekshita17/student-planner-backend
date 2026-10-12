# 📘 Student Study Planner - Learning Guide & Architecture Notes

Welcome to the **Student Study Planner** learning guide! This guide is written specially for students and beginner engineers to understand every piece of the stack, why it is used, how data flows end-to-end, and how to ace technical vivas or job interviews.

---

## 1. 📖 Concept Glossary (Plain English)

| Term / Technology | What It Is | Why We Use It Here |
|---|---|---|
| **Spring Boot 3** | An opinionated Java framework that sets up Spring applications with minimal manual configuration. | Provides an embedded web server (Tomcat), dependency injection, and production-ready REST controllers out of the box. |
| **Spring Data JPA** | A library that sits on top of Hibernate (JPA provider) to talk to SQL databases. | Eliminates manual SQL boilerplate. Allows querying the database simply by declaring Java interface methods like `findByUserId()`. |
| **Hibernate** | An Object-Relational Mapping (ORM) framework. | Automatically maps Java classes (Entities) into relational MySQL database tables and vice versa. |
| **Maven** | A build automation and dependency management tool for Java projects. | Downloads project libraries (JARs) declared in `pom.xml`, compiles Java code, runs tests, and packages the application. |
| **MySQL** | A robust, widely used relational SQL database engine. | Stores our persistent application data (Users, Subjects, Tasks) across restarts with relational integrity (Foreign Keys). |
| **React** | A component-based JavaScript library for building user interfaces. | Divides the UI into reusable components (`Navbar`, `Modal`, `Alert`, `Dashboard`), automatically re-rendering when data (state) changes. |
| **Vite** | A modern, lightning-fast frontend build tool and development server. | Offers sub-second hot module replacement (HMR) and fast build bundling using Rollup. |
| **React Router** | Client-side routing library for single-page applications (SPAs). | Switches between pages (`/login`, `/dashboard`, `/subjects`, `/tasks`) smoothly in the browser without reloading the webpage. |
| **Axios** | A Promise-based HTTP client for web browsers and Node.js. | Sends asynchronous HTTP requests (`GET`, `POST`, `PUT`, `DELETE`) to our Spring Boot backend. |
| **Axios Interceptor** | A middleware hook that inspects or transforms outgoing requests and incoming responses. | Automatically attaches `Authorization: Bearer <token>` to every request and logs out users if the server responds with `401 Unauthorized`. |
| **Tailwind CSS** | A utility-first CSS framework. | Enables rapid styling directly in JSX using utility classes (`bg-white`, `rounded-2xl`, `p-6`, `shadow-xs`) without writing separate CSS files. |
| **JWT (JSON Web Token)** | A compact, URL-safe cryptographic token representing claims between two parties. | Enables stateless authentication. The client holds the signed token, so the backend doesn't need to store session memory. |
| **BCrypt** | An adaptive cryptographic one-way password hashing algorithm. | Hashes student passwords before saving to MySQL. Passwords cannot be decrypted even if the database is leaked. |
| **DTO (Data Transfer Object)** | A plain Java object used exclusively to transfer data between client and server. | Separates internal database entities from external API contracts, preventing mass-assignment security vulnerabilities. |
| **CORS (Cross-Origin Resource Sharing)** | A browser security mechanism that restricts HTTP requests initiated from a different origin domain/port. | Allows our React frontend on port `5173` to safely communicate with the Spring Boot backend on port `8080`. |

---

## 2. 🔄 How Frontend, Backend, and Database Talk (High-Level Architecture)

```
+-------------------------------------------------------------------------+
|                              WEB BROWSER                                |
|                                                                         |
|  +---------------------------+             +--------------------------+ |
|  |     React UI Pages        |  User Click |    Axios HTTP Client     | |
|  | (Dashboard, Tasks, etc.)  | ----------->| (Attaches Bearer JWT)    | |
|  +---------------------------+             +--------------------------+ |
+---------------------------------------------------------|---------------+
                                                          | HTTP REST Request
                                                          | (JSON + Bearer Token)
                                                          v
+-------------------------------------------------------------------------+
|                       SPRING BOOT BACKEND (:8080)                       |
|                                                                         |
|  1. AuthTokenFilter & SecurityFilterChain                               |
|     - Intercepts incoming HTTP request                                  |
|     - Verifies JWT digital signature & extracts user ID                 |
|                                                                         |
|  2. Controller Layer (@RestController)                                  |
|     - Matches route: @PostMapping("/api/tasks")                         |
|     - Validates payload with @Valid (@NotBlank, @NotNull)               |
|                                                                         |
|  3. Service Layer (@Service)                                            |
|     - Executes business rules                                           |
|     - Enforces user data isolation (verifies subject belongs to user)   |
|                                                                         |
|  4. Repository Layer (@Repository)                                      |
|     - Spring Data JPA generates SQL statement                           |
+---------------------------------------------------------|---------------+
                                                          | SQL Query
                                                          | (INSERT / SELECT / UPDATE)
                                                          v
+-------------------------------------------------------------------------+
|                       MySQL DATABASE (:3306)                            |
|                                                                         |
|   Tables: [users] <--- 1:N ---> [subjects] <--- 1:N ---> [tasks]       |
+-------------------------------------------------------------------------+
```

---

## 3. 🚶 Step-by-Step Request Flow: Creating a New Task

Let's follow what happens when a student fills out the "Add Task" modal and clicks **Save Task**:

### Step 1: User Action in React (`Tasks.jsx`)
1. Student enters:
   - Title: `"Complete Chapter 4 Homework"`
   - Subject: `"Mathematics"` (ID: `1`)
   - Due Date: `"2026-10-15"`
   - Priority: `"HIGH"`
2. Form submit event fires:
   ```javascript
   await api.post('/tasks', {
     title: "Complete Chapter 4 Homework",
     subjectId: 1,
     dueDate: "2026-10-15",
     priority: "HIGH"
   });
   ```

### Step 2: Axios Request Interceptor (`axios.js`)
1. The interceptor intercepts the outgoing request.
2. Retrieves `token` from `localStorage.getItem('token')`.
3. Adds HTTP header:
   ```http
   POST /api/tasks HTTP/1.1
   Host: localhost:8080
   Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
   Content-Type: application/json
   ```

### Step 3: Spring Security Filter (`AuthTokenFilter.java`)
1. The request enters the Spring Security filter chain.
2. `AuthTokenFilter.doFilterInternal()` extracts `eyJhbGci...` after `"Bearer "`.
3. `JwtUtils.validateJwtToken()` checks the cryptographic HMAC secret signature and expiration time.
4. `JwtUtils.getUserNameFromJwtToken()` extracts the email (`sarah@example.com`).
5. Loads the user and places the `UserDetailsImpl` object into Spring's `SecurityContextHolder`.

### Step 4: Controller Layer (`TaskController.java`)
1. Route matches `@PostMapping` on `/api/tasks`.
2. Spring validates the `@Valid @RequestBody TaskRequest`.
3. Spring injects `@AuthenticationPrincipal UserDetailsImpl userPrincipal` (giving `userId = 1`).
4. Controller delegates execution to:
   ```java
   taskService.createTask(request, userPrincipal.getId());
   ```

### Step 5: Service Layer (`TaskService.java`)
1. Fetches the authenticated `User` from `UserRepository`.
2. If `subjectId` is provided (`1`), checks `subjectRepository.findByIdAndUserId(1, userId)`.
   > **Security Check**: If subject 1 belongs to another student, a `ResourceNotFoundException` is thrown!
3. Builds the `Task` entity:
   ```java
   Task task = Task.builder()
       .title(request.getTitle())
       .dueDate(request.getDueDate())
       .priority(request.getPriority())
       .completed(false)
       .subject(subject)
       .user(user)
       .build();
   ```
4. Calls `taskRepository.save(task)`.

### Step 6: Repository & Database (`TaskRepository.java` -> MySQL)
1. Spring Data JPA / Hibernate translates `save(task)` into SQL:
   ```sql
   INSERT INTO tasks (title, due_date, priority, completed, subject_id, user_id, created_at)
   VALUES ('Complete Chapter 4 Homework', '2026-10-15', 'HIGH', 0, 1, 1, NOW());
   ```
2. MySQL allocates primary key `id = 4` and inserts the row.

### Step 7: Return Journey
1. Service converts the persisted `Task` entity into a clean `TaskResponse` DTO.
2. Controller wraps the DTO in `ResponseEntity.status(HttpStatus.CREATED).body(response)`.
3. Spring converts the Java object into JSON and sends HTTP status `201 Created`.
4. Axios receives the JSON response in React.
5. `Tasks.jsx` closes the modal, triggers `fetchTasks()`, and displays a green success alert!

---

## 4. 🔐 How JWT Authentication Works Step by Step

```
  [ Student ]                  [ React App ]                [ Spring Boot ]               [ MySQL ]
       |                             |                             |                          |
       |-- 1. Enters Email/Password->|                             |                          |
       |                             |-- 2. POST /api/auth/login ->|                          |
       |                             |   {email, password}         |                          |
       |                             |                             |-- 3. Find by email ----->|
       |                             |                             |<-- 4. Return user row ---|
       |                             |                             |                          |
       |                             |                             |-- 5. BCrypt.matches()    |
       |                             |                             |-- 6. Jwts.builder()      |
       |                             |                             |      Signs with Secret   |
       |                             |<-- 7. Returns JWT Token ----|                          |
       |                             |   {token: "eyJhbGci..."}    |                          |
       |                             |-- 8. Stores in localStorage |                          |
       |                             |                             |                          |
       |-- 9. Opens Dashboard ------>|                             |                          |
       |                             |-- 10. GET /api/dashboard -->|                          |
       |                             |    Authorization: Bearer jwt|-- 11. Validates JWT      |
       |                             |                             |-- 12. Query user tasks ->|
       |                             |                             |<-- 13. Return data ------|
       |<-- 14. Displays Dashboard --|<-- 15. Returns JSON -------|                          |
```

---

## 5. ⚠️ Common Errors and How to Fix Them

### 1. CORS Error (`Access to XMLHttpRequest at ... from origin 'http://localhost:5173' has been blocked by CORS policy`)
- **Cause**: The browser enforces security rules preventing a website hosted on port `5173` from requesting data from a server on port `8080` unless the server explicitly grants permission.
- **Fix**: In `WebSecurityConfig.java`, we configure a `CorsConfigurationSource` bean that permits `http://localhost:5173` with all required HTTP methods (`GET, POST, PUT, DELETE, PATCH, OPTIONS`) and exposed headers. Additionally, Vite configures a reverse proxy in `vite.config.js`.

### 2. Database Connection Refused (`Communications link failure / Connection refused: connect`)
- **Cause**: MySQL Server is not running or is listening on a different port/credentials.
- **Fix**:
  1. Verify MySQL is running on port `3306`:
     ```powershell
     Test-NetConnection -ComputerName 127.0.0.1 -Port 3306
     ```
  2. Start MySQL server:
     ```powershell
     & "C:\MySQLExtract\PFiles64\MySQL\MySQL Server 8.0\bin\mysqld.exe" --defaults-file="C:\MySQLData\my.ini" --console
     ```
  3. Ensure `student_planner` database exists:
     ```sql
     CREATE DATABASE IF NOT EXISTS student_planner;
     ```

### 3. Port Already in Use (`Web server failed to start. Port 8080 was already in use`)
- **Cause**: Another program (or a background Spring instance) is occupying port 8080.
- **Fix**:
  1. Find the process PID using the port:
     ```powershell
     Get-NetTCPConnection -LocalPort 8080
     ```
  2. Terminate the process:
     ```powershell
     Stop-Process -Id <PID> -Force
     ```
  3. Or configure a different port in `application.properties`:
     ```properties
     server.port=8081
     ```

### 4. HTTP 401 Unauthorized on Protected Routes
- **Cause**: Missing or expired JWT token in the `Authorization` header.
- **Fix**: The React frontend uses an Axios response interceptor that detects 401 statuses, clears corrupted localStorage data, and redirects the student back to `/login` to acquire a fresh token.

---

## 6. 🎓 10 Viva / Technical Interview Questions & Answers

### Q1: What is the purpose of Spring Boot compared to standard Spring Framework?
> **Answer**: Spring Boot drastically reduces configuration by providing **starter dependencies**, **auto-configuration**, and an **embedded web server (Tomcat)**. In standard Spring, developers had to manually configure XML/Java configuration files, dispatcher servlets, and external application servers.

### Q2: Why do we use JWT instead of traditional HTTP Sessions?
> **Answer**: HTTP Sessions are **stateful**; the server stores session IDs in memory or a database, which does not scale well across multiple servers. JWT is **stateless**; the token contains all user claims digitally signed by the server. The server verifies the token with its secret key without querying session storage.

### Q3: What is the difference between `@RestController` and `@Controller`?
> **Answer**: `@Controller` is traditionally used in MVC applications that render HTML view templates (like Thymeleaf or JSP). `@RestController` is a convenience annotation combining `@Controller` and `@ResponseBody`, ensuring that every method return value is automatically serialized into JSON or XML for REST clients.

### Q4: How does Spring Data JPA execute queries without writing SQL?
> **Answer**: Spring Data JPA uses **method name parsing** and **dynamic proxies**. It analyzes the method name (e.g. `findByUserIdOrderByNameAsc`), determines the entity properties, and generates the appropriate SQL query at runtime.

### Q5: What is the role of DTOs and why shouldn't we return `@Entity` classes directly from controllers?
> **Answer**:
> 1. Returning entities directly causes **over-fetching** (exposing sensitive fields like hashed passwords).
> 2. Direct entities can trigger **infinite JSON recursion** when bidirectional relationships (`@OneToMany` / `@ManyToOne`) exist.
> 3. DTOs decouple the database schema from the public API, preventing **mass-assignment security vulnerabilities**.

### Q6: What does the `@Transactional` annotation do?
> **Answer**: `@Transactional` ensures that a series of database operations either completely succeed or completely roll back in case of an uncaught exception. This maintains data consistency and ACID properties.

### Q7: Why do we hash passwords with BCrypt instead of SHA-256 or MD5?
> **Answer**: MD5 and SHA-256 are fast hash functions, making them vulnerable to brute-force and rainbow table attacks. BCrypt incorporates a **cryptographic salt** and a **configurable work factor (cost)**, making it intentionally slow to compute and resistant to offline dictionary attacks.

### Q8: How does our application ensure that Student A cannot see or edit Student B's tasks?
> **Answer**: Every service method checks the authenticated user's ID extracted from the verified JWT token (`userPrincipal.getId()`). Database queries in `TaskRepository` and `SubjectRepository` explicitly include `WHERE user.id = :userId` (e.g., `findByIdAndUserId(id, userId)`), returning `404 Not Found` if a student attempts to access another user's record.

### Q9: What is the role of React Router and how do Protected Routes work?
> **Answer**: React Router provides client-side routing for Single Page Applications (SPAs). A `ProtectedRoute` component inspects the `AuthContext`; if the student is authenticated, it renders the requested component; otherwise, it redirects them to `/login` using `<Navigate to="/login" replace />`.

### Q10: What are Axios Interceptors and why are they valuable?
> **Answer**: Axios interceptors act as centralized middleware. A **Request Interceptor** automatically injects the `Authorization: Bearer <token>` header to all outgoing requests. A **Response Interceptor** catches global errors like `401 Unauthorized` and cleanly logs the user out without duplicating error-handling code in every component.
