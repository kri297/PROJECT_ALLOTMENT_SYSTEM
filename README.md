# Project Management System

A Java Swing application for managing students, teachers, project requests, and CSV-backed records.

## Features

- Role-based login for Admin, Teacher, and Student
- Full-screen dashboard layout with a top action bar and left summary panel
- In-page workflow screens instead of popup-heavy navigation
- Admin can add/remove users, update passwords, view users, and view confirmed projects
- Teacher can review requests, add students, and inspect assigned students
- Student can browse available areas, choose a teacher, submit a project request, and read mailbox messages
- CSV persistence for users, slots, projects, confirmed projects, messages, and teacher-student mappings
- Password masking for Admin and Teacher views, with an unlock key of `2006`

## Tech Stack

- Java
- Java Swing
- CSV file storage in the `data/` folder

## Prerequisites

- Java Development Kit (JDK) 8 or higher

## Run

### Windows

```batch
run.bat
```

### Linux or macOS

```bash
chmod +x run.sh
./run.sh
```

The scripts compile all `.java` files and launch `LoginSystem`.

## Default Login Data

The app seeds sample CSV data on first run if the `data/` files are missing.

Common sample accounts:

| Role | Username | Password |
|---|---|---|
| Admin | admin | admin@123 |
| Teacher | Rajesh | Rajesh@123 |
| Teacher | Priya | Priya@123 |
| Teacher | Sneha | Sneha@123 |
| Student | Rahul | Rahul@123 |
| Student | Ananya | Ananya@123 |

## Data Files

The application reads and writes these CSV files:

- `data/users.csv`
- `data/slots.csv`
- `data/projects.csv`
- `data/confirmed_projects.csv`
- `data/messages.csv`
- `data/teacher_students.csv`

## Project Structure

```
LoginSystem.java
AdminDashboard.java
TeacherDashboard.java
StudentDashboard.java
DataManager.java
UITheme.java
PasswordGenerator.java
run.bat
run.sh
data/
```

## Notes

- Do not commit compiled `.class` files.
- If you want a clean GitHub repo, keep the source files and CSV files together so the app runs immediately after clone.
- If you do not want to expose sample data, sanitize the CSV files before pushing.

## License

Educational project.
