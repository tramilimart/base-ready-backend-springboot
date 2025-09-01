# RBAC (Role-Based Access Control) Spring Boot Application

This is a Spring Boot application that implements a comprehensive Role-Based Access Control (RBAC) system using JWT authentication.

## Features

- **JWT-based Authentication**: Secure token-based authentication
- **Role-Based Access Control**: Users, Roles, and Permissions with proper relationships
- **Method-Level Security**: Fine-grained access control using `@PreAuthorize` annotations
- **Automatic Data Initialization**: Creates default roles, permissions, and users on startup
- **RESTful API**: Clean REST endpoints for authentication and testing

## Default Data

The application automatically creates the following on startup:

### Roles
- **USER**: Basic user role with limited permissions
- **MODERATOR**: Intermediate role with some admin capabilities
- **ADMIN**: Full administrative access

### Permissions
- **USER_READ/WRITE/DELETE**: User data management
- **ADMIN_READ/WRITE/DELETE**: Admin data management  
- **SYSTEM_READ/WRITE/DELETE**: System-level operations

### Default Users
- **admin/admin123**: Administrator user with full access
- **user/user123**: Regular user with basic permissions

## API Endpoints

### Authentication (Public)
- `POST /session/login` - User login
- `POST /session/register` - User registration
- `GET /session/profile` - Get user profile (requires authentication)

### Test Endpoints
- `GET /api/public/test` - Public endpoint (no authentication)
- `GET /api/user/test` - User endpoint (requires USER role)
- `GET /api/moderator/test` - Moderator endpoint (requires MODERATOR or ADMIN role)
- `GET /api/admin/test` - Admin endpoint (requires ADMIN role)
- `GET /api/profile` - Current user profile
- `POST /api/admin/create-user` - Create user (requires ADMIN role)
- `DELETE /api/admin/delete-user/{userId}` - Delete user (requires ADMIN role)

## Security Configuration

The application uses:
- **JWT Filter**: Processes JWT tokens and sets authentication
- **Custom User Details Service**: Loads user details with roles and permissions
- **Method Security**: `@PreAuthorize` annotations for role-based access control
- **Stateless Sessions**: No server-side session storage

## How to Use

### 1. Start the Application
```bash
mvn spring-boot:run
```

### 2. Login as Admin
```bash
curl -X POST http://localhost:8080/session/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 3. Use the JWT Token
```bash
# Replace YOUR_JWT_TOKEN with the token from login response
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/admin/test
```

### 4. Test Different Roles
```bash
# Test USER role (login as 'user' first)
curl -H "Authorization: Bearer USER_JWT_TOKEN" \
  http://localhost:8080/api/user/test

# Test MODERATOR role
curl -H "Authorization: Bearer MODERATOR_JWT_TOKEN" \
  http://localhost:8080/api/moderator/test
```

## Database Schema

The application uses JPA entities with the following relationships:
- **User** ↔ **Role** (Many-to-Many)
- **Role** ↔ **Permission** (Many-to-Many)

## Configuration

Key configuration files:
- `SecurityConfig.java` - Spring Security configuration
- `JwtFilter.java` - JWT token processing
- `CustomUserDetailsService.java` - User details loading
- `DataInitializationService.java` - Initial data creation

## Dependencies

- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- JWT (jjwt)
- H2 Database (for development)

## Security Notes

- JWT tokens expire after 8 hours
- Passwords are encrypted using BCrypt
- CORS is configured for development
- CSRF is disabled for API endpoints
- All endpoints except `/session/**` and `/api/public/**` require authentication 