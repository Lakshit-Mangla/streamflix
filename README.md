# Streamflix Backend

A Netflix-inspired streaming platform backend built with **Spring Boot 3**, **Spring Security + JWT**, and **MySQL**. Covers authentication, a searchable movie catalog, watch history, personalized watchlists, ratings, and a hybrid recommendation engine.

## Tech Stack

- Java 17, Spring Boot 3.3
- Spring Security 6 (stateless JWT, access + refresh tokens)
- Spring Data JPA / Hibernate
- MySQL 8
- Bean Validation (`jakarta.validation`)
- springdoc-openapi (Swagger UI)
- Lombok

## Project Structure

```
src/main/java/com/streamflix/
├── config/          SecurityConfig (filter chain, CORS, password encoder)
├── security/         JwtService, JwtAuthFilter, UserDetailsServiceImpl
├── entity/           User, Movie, Genre, WatchHistory, WatchlistItem, Rating
├── repository/       Spring Data JPA repositories (+ Specifications for search)
├── dto/request/      Validated request bodies
├── dto/response/     Response payloads (incl. generic PageResponse<T>, ApiError)
├── service/          Business logic: Auth, Movie, WatchHistory, Watchlist, Rating, Recommendation
├── controller/       REST controllers
├── exception/        Custom exceptions + @RestControllerAdvice global handler
└── util/             EntityMapper (entity -> DTO)
```

## Getting Started

### 1. Prerequisites
- JDK 17+
- Maven 3.9+
- MySQL 8 running locally (or update `application.yml` to point elsewhere)

### 2. Database
The app creates the `streamflix` schema automatically on first run
(`createDatabaseIfNotExist=true`). Set credentials via environment variables,
or edit `src/main/resources/application.yml` directly:

```bash
export DB_USERNAME=root
export DB_PASSWORD=yourpassword
export JWT_SECRET=$(openssl rand -hex 32)   # 256-bit secret for HS256
```

### 3. Run

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. Swagger UI is at
`http://localhost:8080/swagger-ui.html`. A small seed dataset (5 movies,
10 genres) loads automatically via `data.sql`.

### 4. Run tests

```bash
mvn test
```

## Authentication Flow

1. `POST /api/auth/register` or `/api/auth/login` → returns a short-lived
   **access token** (15 min) and a longer-lived **refresh token** (7 days).
2. Send the access token as `Authorization: Bearer <token>` on subsequent
   requests.
3. When the access token expires, call `POST /api/auth/refresh` with the
   refresh token to get a new pair — no need to log in again.

Passwords are hashed with BCrypt (strength 12). The security filter chain is
fully stateless (no sessions); every request is authenticated independently
via the JWT filter.

## API Reference

### Auth (public)
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Create an account |
| POST | `/api/auth/login` | Get access + refresh tokens |
| POST | `/api/auth/refresh` | Exchange a refresh token for a new pair |

### Movies
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/movies` | public | Paginated catalog |
| GET | `/api/movies/{id}` | public | Movie detail |
| GET | `/api/movies/search` | public | Search by `query`, `genre`, `yearFrom`, `yearTo`, `contentType` |
| GET | `/api/movies/trending` | public | Most-viewed titles |
| GET | `/api/movies/top-rated` | public | Highest average rating |
| POST | `/api/movies/admin` | ADMIN | Add a movie |
| PUT | `/api/movies/admin/{id}` | ADMIN | Update a movie |
| DELETE | `/api/movies/admin/{id}` | ADMIN | Remove a movie |

### Watch History
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/watch-history/progress` | Upsert playback progress for a movie |
| GET | `/api/watch-history` | Paginated history, most recent first |
| DELETE | `/api/watch-history/{movieId}` | Remove a history entry |

### Watchlist ("My List")
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/watchlist/{movieId}` | Add a movie |
| DELETE | `/api/watchlist/{movieId}` | Remove a movie |
| GET | `/api/watchlist` | Paginated list |

### Ratings
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/ratings` | Rate a movie 1-5 (upserts, recalculates the movie's average) |
| DELETE | `/api/ratings/{movieId}` | Remove your rating |

### Recommendations
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/recommendations/for-you` | Personalized picks |
| GET | `/api/recommendations/similar/{movieId}` | "Because you watched X" |

All non-public endpoints require `Authorization: Bearer <access-token>`.

## How Recommendations Work

`RecommendationService` implements a small hybrid engine, not a black box:

1. **Genre affinity profile** — built per-user from watch history (completed
   watches weighted 2x, in-progress 1x) and 4-5 star ratings (weighted 3x),
   so explicit signal outweighs passive viewing.
2. **Candidate ranking** — unseen movies sharing those genres are scored by
   `genre-affinity-sum + movie's own average rating`, so a well-loved genre
   match outranks an obscure one.
3. **Cold-start fallback** — brand-new users with no history get trending +
   top-rated titles instead of an empty response.
4. **Top-up** — if genre-based results are thin, trending titles fill the
   remaining slots so `limit` is still respected.
5. **Similar-title mode** — `/similar/{movieId}` finds other titles sharing
   that movie's genres, for "Because you watched X" rails.

This is intentionally content-based rather than collaborative filtering
(no matrix factorization / ALS) — it needs no cold-start user base to work
and stays easy to reason about. A natural next step, if you want to extend
it, is collaborative filtering once you have enough cross-user rating data.

## Example: End-to-End curl Walkthrough

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Ada Lovelace","email":"ada@example.com","password":"supersecret1"}'

# Search movies
curl "http://localhost:8080/api/movies/search?query=ember&genre=Sci-Fi"

# Record watch progress (replace TOKEN)
curl -X POST http://localhost:8080/api/watch-history/progress \
  -H "Authorization: Bearer TOKEN" -H "Content-Type: application/json" \
  -d '{"movieId":1,"progressMinutes":45}'

# Get personalized recommendations
curl "http://localhost:8080/api/recommendations/for-you" -H "Authorization: Bearer TOKEN"
```

## Notes on Design Choices

- **Stateless JWT over sessions** — horizontally scalable, no server-side
  session store needed.
- **Specification-based search** — filters (`query`, `genre`, year range,
  content type) compose independently instead of requiring a combinatorial
  explosion of repository methods.
- **DTOs everywhere** — entities never leak directly into responses, which
  avoids Hibernate lazy-loading serialization issues and keeps the API
  contract stable if the schema changes.
- **Global exception handler** — every error path returns a consistent
  `ApiError` JSON shape instead of raw stack traces.

## Possible Extensions

- Redis caching for `/trending` and `/top-rated` (they're read-heavy and
  don't need to be perfectly real-time)
- Rate limiting on `/api/auth/*` to blunt credential-stuffing attempts
- Collaborative filtering once there's enough cross-user rating volume
- WebSocket or SSE endpoint for "continue watching" sync across devices
- S3-backed video storage instead of a plain `videoUrl` string
