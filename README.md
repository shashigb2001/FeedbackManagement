# Feedback Management System 

- **JDK:** 21
- **Maven modelVersion:** 4.0.0
- **Spring Boot:** 3.5.5
- **DB:** PostgreSQL

### Authentication & RBAC
- login for Student, Faculty, Admin.
- Role-based access to `student`, `faculty`, `admin`.
- Change password.

### Admin
- Create faculty accounts.
- List & delete faculty accounts.


### Faculty
- Create feedback forms with questions (rating/text).
- Assign forms to students via email.
- List forms created by the faculty.
- View analytics for assigned forms (submission rate, averages, distributions, text answers).

### Student
- View pending feedback assignments.
- Submit answers (ratings and/or text) for assigned forms.
- View completed submissions with their own answers.
