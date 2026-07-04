# 🎬 MyWatchlist API

A modern REST API built with **Java** and **Spring Boot** that powers **MyWatchlist**, a platform for discovering, organizing, and tracking movies, TV series, and anime.

The API integrates multiple external providers into a single, consistent backend while offering secure authentication, personalized watchlists, ratings, favorites, and user profile management.

---

## ✨ Features

### Authentication

* JWT Authentication
* User Registration
* User Login
* Secure Password Encryption
* Refresh Token Support
* Protected Endpoints

### User Profile

* Get current user information
* Change username
* Change email
* Change password
* Upload custom profile image
* Select preset profile image
* Remove profile image

### Media

* Search Movies
* Search TV Shows
* Search Anime
* Trending Content
* Popular Content
* Detailed Media Information
* Unified Media Response Model

### Personal Library

* Watchlist Management
* Watched History
* Personal Statistics

### External Integrations

* TMDB API
* Jikan API

---

# 🛠 Tech Stack

| Category        | Technologies               |
| --------------- | -------------------------- |
| Language        | Java 21                    |
| Framework       | Spring Boot                |
| Security        | Spring Security, JWT       |
| Database        | PostgreSQL                 |
| ORM             | Spring Data JPA, Hibernate |
| Validation      | Jakarta Validation         |
| Build Tool      | Maven                      |
| API             | RESTful API                |
| Documentation   | OpenAPI / Swagger          |
| Version Control | Git                        |

---

# 📂 Project Structure

```text
src
├── auth
├── config
├── controller
├── dto
├── entity
├── exception
├── mapper
├── repository
├── security
├── service
└── util
```

---

# 🚀 Getting Started

## Clone

```bash
git clone https://github.com/MyWatchlistApp/mywatchlist-backend.git
cd mywatchlist-backend
```

## Configure Environment

Create an `.env` file (or configure your `application.yml`) with the required values.

Example:

```env
DATABASE_URL=
DATABASE_USERNAME=
DATABASE_PASSWORD=

JWT_SECRET=

TMDB_API_KEY=
```

---

## Run

```bash
./mvnw spring-boot:run
```

or

```bash
mvn spring-boot:run
```

---

# 🔐 Authentication

The API uses **JWT Bearer Authentication**.

After logging in, include the access token in every protected request.

```
Authorization: Bearer <your_access_token>
```

---

# 📌 Main Endpoints

## Authentication

```
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
```

## User

```
GET    /api/users/me
PATCH  /api/users/me/username
PATCH  /api/users/me/email
PATCH  /api/users/me/password

POST   /api/users/me/profile-image
PATCH  /api/users/me/profile-image/preset
DELETE /api/users/me/profile-image
```

## Media

```
GET /api/media/search
GET /api/media/{id}
GET /api/media/trending
GET /api/media/popular
```

*(Additional endpoints omitted for brevity.)*

---

# 🌍 External Services

* TMDB (Movies & TV Shows)
* Jikan (Anime)

---

# 📖 API Documentation

Swagger/OpenAPI documentation is available after running the application.

```
http://localhost:8080/swagger-ui/index.html
```

---

# 📄 License

This project is intended for educational and portfolio purposes.
