# Second-hand Marketplace — Advanced Programming Project

A web-based marketplace application where users can register, post advertisements for second-hand items, search and filter listings, chat with sellers, save favorites, and rate sellers after a transaction. An admin panel allows reviewing and moderating advertisements and managing users.

## Team Members
- Parsa Vakili
- Mahdi Nikzad

---

## Technologies Used

**Backend**
- Java 17+
- Spring Boot 4.1.0 (Spring Web, Spring Data JPA, Spring Security)
- JWT (io.jsonwebtoken / jjwt) for authentication
- SQLite (via Hibernate Community Dialect)
- Maven

**Frontend**
- JavaFX *(in progress)*

---

## Prerequisites

- JDK 17 or higher
- Maven
- Git
- (Optional) Postman, for testing API endpoints manually
- (Optional) DB Browser for SQLite, for inspecting the database file

Check your installation:
```
java -version
mvn -version
```

---

## Project Structure

```
secondhand-java-project/
├── backend/     # Spring Boot backend (Java)
├── frontend/    # JavaFX frontend
├── docs/        # API contract, task list, and other documentation
└── README.md
```

---

## Running the Backend

1. Navigate to the backend folder:
   ```
   cd backend
   ```
2. Run the application:
   ```
   mvn spring-boot:run
   ```
3. The server starts on port `8080`. Verify it's running:
   ```
   GET http://localhost:8080/api/health
   ```
   Expected response: `Backend is running`

No manual database setup is required — SQLite creates the database file (`secondhand.db`) automatically on first run, and sample categories/cities are inserted automatically.

---

## Running the Frontend

*(To be completed once the JavaFX frontend is implemented.)*

```
cd frontend
mvn javafx:run
```

Make sure the backend is running first, since the frontend depends on it (`http://localhost:8080`).

---

## Data Storage

This project uses **SQLite** as the persistent storage method. No external database server is required — the database is stored in a single file (`secondhand.db`) inside the `backend` folder, created automatically the first time the backend runs.

- Database file: `backend/secondhand.db`
- Sample data (categories and cities) is seeded automatically via `data.sql` on every startup (using `INSERT OR IGNORE`, so it's safe to restart without duplication errors).
- The database file is excluded from version control (`.gitignore`) — each team member has their own local copy with their own test data.

---

## Test Accounts

The database starts empty. To test the application, register the following accounts (or any accounts you prefer) via `POST /api/auth/register`:

| Role | Username | Password |
|---|---|---|
| Regular user | `testuser1` | `123456` |
| Regular user | `testuser2` | `123456` |
| Admin | `admin1` | `123456` |

> **Note:** New users are always registered with the `USER` role by default. To create an admin account, register a normal user first, then manually change the `role` column to `ADMIN` in the `users` table (e.g. using DB Browser for SQLite) while the backend is stopped.

---

## Implemented Features

### Backend (complete)
- User registration and login with JWT-based authentication
- Password hashing (BCrypt)
- Role-based access control (`USER` / `ADMIN`)
- Advertisement creation, listing (with keyword search, category/city/price filters, and sorting), detail view, editing, deletion, and marking as sold
- Ownership checks: only the advertisement owner can edit, delete, or mark it as sold
- Categories and cities listing
- Favorites: add, list, remove, with duplicate prevention
- Chat: starting a conversation, listing conversations, sending and viewing messages
- Prevents users from messaging themselves about their own advertisement
- Ratings: submitting a 1–5 score with optional comment, average score calculation, duplicate-rating prevention, self-rating prevention
- Admin panel: listing pending advertisements, approving/rejecting them, listing users, blocking/unblocking users
- Image upload for advertisements (multipart upload, stored on disk, served as static resources, ownership-restricted)
- Standardized error responses (`{ "message": ..., "status": ... }`) across all endpoints
- Full API documentation available in [`docs/api-contract.md`](docs/api-contract.md)

### Frontend
*(To be completed as JavaFX screens are implemented — e.g. registration/login, advertisement listing and search, advertisement details, posting/editing ads, favorites, chat, ratings, admin panel.)*

---

## Screenshots

*(To be added once the frontend UI is available.)*

---

## Individual Contributions

**Parsa Vakili:**
I was responsible for designing and implementing the entire Backend of the project. This included setting up the initial Spring Boot project structure with SQLite as the persistence layer, and designing the domain model (User, Advertisement, AdImage, Category, City, Conversation, Message, Favorite, and Rating entities) along with their relationships. I implemented the layered architecture (repository, service, controller, DTO, and exception-handling layers) and built authentication and authorization from scratch, including JWT token generation/validation and Spring Security configuration to protect routes based on ownership and user roles. I implemented all core API endpoints — advertisement CRUD with keyword search, filtering, and sorting; favorites; the chat system (conversations and messages); the rating system with validation rules; the admin panel for reviewing advertisements and managing users; and image upload for advertisements. I manually tested every endpoint (success and error paths) using Postman throughout development, and wrote the API contract (`docs/api-contract.md`) that documents all endpoints for frontend integration, along with the project task list and this README.

**Mahdi Nikzad:**
*(To be completed — describe your role in design, implementation, testing, and documentation.)*

---

## Additional Documentation

- [`docs/api-contract.md`](docs/api-contract.md) — full list of API endpoints with request/response examples
- [`docs/task-list.md`](docs/task-list.md) — detailed backend and frontend task checklist
