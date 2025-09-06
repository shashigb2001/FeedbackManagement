# Feedback Management System 

- **JDK:** 21
- **Maven modelVersion:** 4.0.0
- **Spring Boot:** 3.5.5
- **DB:** PostgreSQL

1. Create database "feedbackdb" in postgress
2. Update `src/main/resources/application.properties` with your DB username/password if different.
3. run mvn spring-boot:run

### Authentication & RBAC
- login for Student, Faculty, Admin.
- Role-based access to `student`, `faculty`, `admin`.
- Change password.

### Admin
- Create faculty accounts.
- List & delete faculty accounts.
