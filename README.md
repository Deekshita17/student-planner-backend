# 🎓 Student Study Planner

A full-stack web application designed for students to organize courses, track homework deadlines, monitor study progress, and prioritize daily tasks. Built end-to-end with **Spring Boot 3** and **React + Vite**.

---

## 🛠️ Tech Stack

### Backend
- **Java 17+** (compiled on JDK 21 / 17 LTS)
- **Spring Boot 3.3.4** (Spring Web, Spring Data JPA, Spring Security, Validation)
- **MySQL 8.0**
- **JJWT (io.jsonwebtoken 0.12.6)** for stateless JWT token authentication
- **Project Lombok** for clean boilerplate reduction
- **Maven** for build automation and dependency management

### Frontend
- **React 18** (Vite build tool)
- **React Router v6 / v7** for single-page client routing
- **Axios** with JWT request & response interceptors
- **Tailwind CSS** for clean, modern, responsive styling
- **Lucide React** for minimalist UI icons

---

## 📂 Project Structure

```
.
├── planner-backend/                   # Spring Boot 3 Backend
│   ├── src/main/java/com/studentplanner/
│   │   ├── controller/               # REST API Controllers (Auth, Subject, Task, Dashboard)
│   │   ├── service/                  # Business Logic layer (Auth, Subject, Task, Dashboard)
│   │   ├── repository/               # Spring Data JPA Repositories (User, Subject, Task)
│   │   ├── entity/                   # JPA Entities (User, Subject, Task, Priority)
│   │   ├── dto/                      # Data Transfer Objects & Request/Response models
│   │   ├── exception/                # Custom Exceptions & GlobalExceptionHandler
│   │   ├── security/                 # JWT Utility, UserDetails, SecurityFilterChain, CORS
│   │   └── StudentPlannerApplication.java # Spring Boot entry point
│   ├── src/main/resources/
│   │   └── application.properties    # MySQL & JWT configuration via environment variables
│   ├── src/test/java/                # Integration and unit tests
│   ├── pom.xml                       # Maven build configuration
│   └── .gitignore
│
├── planner-frontend/                  # React + Vite Frontend
│   ├── src/
│   │   ├── api/                      # Axios client instance with JWT Bearer interceptor
│   │   ├── context/                  # AuthContext providing global login/logout state
│   │   ├── components/               # Navbar, ProtectedRoute, Modal, Alert, LoadingSpinner
│   │   ├── pages/                    # Login, Register, Dashboard, Subjects, Tasks
│   │   ├── App.jsx                   # React Router routing definition
│   │   ├── main.jsx                  # React DOM mount point
│   │   └── index.css                 # Tailwind CSS styles and theme directives
│   ├── index.html                    # Single page HTML entry
│   ├── vite.config.js                # Vite configuration with port 5173 & API reverse proxy
│   ├── tailwind.config.js            # Tailwind color schemes and content rules
│   ├── package.json
│   └── .gitignore
│
├── LEARNING.md                       # Comprehensive beginner learning guide & interview prep
└── README.md                         # Project documentation and setup guide
```

---

## ⚡ Quick Start & Run Commands

### 1. Prerequisites
- **Java 17 or higher** installed (`java -version`)
- **Maven 3.8+** installed (`mvn -version`)
- **Node.js 18+ & npm** installed (`node -v`)
- **MySQL Server 8.0+** running on `localhost:3306`

---

### 2. MySQL Database Setup
Open MySQL command line or MySQL Workbench and run:

```sql
CREATE DATABASE IF NOT EXISTS student_planner;
```

> **Note**: Spring Boot Hibernate is set to `spring.jpa.hibernate.ddl-auto=update`, which will automatically create the `users`, `subjects`, and `tasks` tables upon first run!

---

### 3. Backend Setup & Run

1. Navigate to the backend folder:
   ```bash
   cd planner-backend
   ```

2. (Optional) Customize environment variables or use defaults:
   - `SPRING_DATASOURCE_URL`: `jdbc:mysql://localhost:3306/student_planner`
   - `SPRING_DATASOURCE_USERNAME`: `root`
   - `SPRING_DATASOURCE_PASSWORD`: `` *(empty by default)*
   - `PORT`: `8080`
   - `JWT_SECRET`: `StudentPlannerSecretKeyForJwtSigningMustBeAtLeast256BitsLongDevKey2026!`

3. Compile, run tests, and start the application:
   ```bash
   mvn spring-boot:run
   ```
   *The backend starts at `http://localhost:8080`.*

---

### 4. Frontend Setup & Run

1. Open a new terminal and navigate to the frontend folder:
   ```bash
   cd planner-frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start Vite development server:
   ```bash
   npm run dev
   ```
   *The frontend starts at `http://localhost:5173`.*

---

## 📱 Features Walkthrough

1. **User Authentication (JWT)**:
   - Register a student account with Name, Email, and Password.
   - Secure BCrypt password hashing.
   - JWT tokens generated with 24-hour expiration.
   - Automatic session restore on browser refresh.
2. **Subjects Management**:
   - Create academic subjects (e.g. Mathematics, Computer Science).
   - Assign custom color codes from an interactive 8-preset palette or enter hex values.
   - Edit subject titles and colors.
   - Delete subjects (cascades and cleans up associated tasks).
3. **Tasks Management**:
   - Add tasks with Title, Subject, Due Date, and Priority (`LOW`, `MEDIUM`, `HIGH`).
   - Quick one-click checkbox to toggle completed status.
   - Edit task details.
   - Delete tasks.
4. **Dashboard**:
   - Visual **Completion Progress Bar** calculating overall completion rate.
   - 4 summary cards: Total Tasks, Completed, Pending, Overdue.
   - **Today's Tasks** list showing items due on current date.
   - **Overdue Tasks** list highlighting incomplete tasks past deadline.
5. **Multi-Criteria Task Filtering**:
   - Filter by Subject (All Subjects, or course-specific).
   - Filter by Priority (`HIGH`, `MEDIUM`, `LOW`).
   - Filter by Status (`All`, `Pending / Active`, `Completed`).
   - One-click "Reset Filters" action.
6. **Data Privacy & Isolation**:
   - Each student user strictly sees and modifies only their own subjects and tasks.

---

## 🌐 REST API Endpoints

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register a new student | No |
| `POST` | `/api/auth/login` | Log in and receive JWT token | No |
| `GET` | `/api/auth/me` | Fetch authenticated student profile | Yes |
| `GET` | `/api/subjects` | List all subjects for student | Yes |
| `POST` | `/api/subjects` | Create a new subject course | Yes |
| `PUT` | `/api/subjects/{id}` | Update subject name or color | Yes |
| `DELETE` | `/api/subjects/{id}` | Delete subject and its tasks | Yes |
| `GET` | `/api/tasks` | Get tasks (filter by `subjectId`, `priority`, `completed`) | Yes |
| `POST` | `/api/tasks` | Create a new study task | Yes |
| `PUT` | `/api/tasks/{id}` | Update existing task | Yes |
| `PATCH` | `/api/tasks/{id}/toggle` | Toggle task completed status | Yes |
| `DELETE` | `/api/tasks/{id}` | Delete a task | Yes |
| `GET` | `/api/dashboard` | Get dashboard summary metrics & lists | Yes |

---

## 📚 Deep Dive Learning Materials

For a complete beginner-friendly breakdown of:
- Full request flow diagrams
- Technology explanation glossary
- Common error troubleshooting (CORS, Database, Ports)
- 10 Viva and Interview questions with answers

👉 **See [LEARNING.md](LEARNING.md)**!
