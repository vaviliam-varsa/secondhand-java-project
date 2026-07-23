# API Contract - Second-hand Marketplace Project

This document lists all endpoints provided by the Backend. The Frontend must follow these exact paths, methods, and JSON formats.

**Base URL:** `http://localhost:8080`

**Standard error format (used across all endpoints):**
```json
{ "message": "Error description", "status": 404 }
```

---

## 1. Authentication

### Register
```
POST /api/auth/register
```
**Request Body:**
```json
{
  "fullName": "Ali Rezaei",
  "username": "ali_rezaei",
  "password": "123456",
  "phoneNumber": "09123456789"
}
```
**Response (201 Created):**
```json
{ "message": "User registered successfully" }
```
**Possible errors:** `400` (duplicate username or phone number)

---

### Login
```
POST /api/auth/login
```
**Request Body:**
```json
{
  "username": "ali_rezaei",
  "password": "123456"
}
```
**Response (200 OK):**
```json
{
  "token": "jwt-token-here",
  "userId": 3,
  "username": "ali_rezaei",
  "role": "USER"
}
```
**Possible errors:** `401` (wrong username/password), `403` (user blocked)

---

## 2. Advertisements

### Get list of active advertisements (with search/filter)
```
GET /api/advertisements?keyword=&categoryId=&cityId=&minPrice=&maxPrice=&sort=
```
All parameters are optional. `sort` can be `newest`, `price_asc`, `price_desc`.

**Response (200 OK):**
```json
[
  {
    "id": 5,
    "title": "Laptop Lenovo",
    "price": 18000000,
    "city": "Tehran",
    "category": "Digital Goods",
    "status": "ACTIVE",
    "createdAt": "2026-07-15T10:30:00"
  }
]
```

---

### Get advertisement details
```
GET /api/advertisements/{id}
```
**Response (200 OK):**
```json
{
  "id": 5,
  "title": "Laptop Lenovo",
  "description": "Used laptop in good condition",
  "price": 18000000,
  "city": "Tehran",
  "category": "Digital Goods",
  "status": "ACTIVE",
  "rejectionReason": null,
  "createdAt": "2026-07-15T10:30:00",
  "images": ["images/ad5-1.jpg", "images/ad5-2.jpg"],
  "owner": { "id": 3, "fullName": "Ali Rezaei" }
}
```
`rejectionReason` is `null` unless `status` is `REJECTED`, in which case it holds the reason the admin gave when rejecting the advertisement.

**Possible errors:** `404` (advertisement not found)

---

### Create a new advertisement (requires JWT)
```
POST /api/advertisements
Authorization: Bearer <token>
```
**Request Body:**
```json
{
  "title": "Laptop Lenovo",
  "description": "Used laptop in good condition",
  "price": 18000000,
  "categoryId": 2,
  "cityId": 1
}
```
> Note: `ownerId` is not sent by the client; the Backend derives it from the JWT.

**Response (201 Created):**
```json
{ "id": 12, "message": "Advertisement submitted for review" }
```
The new advertisement is saved with status `PENDING`.

---

### Update advertisement (owner only, requires JWT)
```
PUT /api/advertisements/{id}
Authorization: Bearer <token>
```
**Request Body:**
```json
{
  "title": "Updated title",
  "description": "Updated description",
  "price": 25000000
}
```
**Response (200 OK):**
```json
{ "message": "Advertisement updated successfully" }
```
**Possible errors:** `403` (user is not the owner), `404`

---

### Delete advertisement (owner only, requires JWT)
```
DELETE /api/advertisements/{id}
Authorization: Bearer <token>
```
**Response (200 OK):**
```json
{ "message": "Advertisement deleted successfully" }
```

---

### Mark advertisement as sold (requires JWT)
```
PUT /api/advertisements/{id}/sold
Authorization: Bearer <token>
```
**Response (200 OK):**
```json
{ "message": "Advertisement marked as sold" }
```

---

## 3. Categories and Cities

### Get list of categories
```
GET /api/categories
```
**Response (200 OK):**
```json
[
  { "id": 1, "name": "Digital Goods" },
  { "id": 2, "name": "Home Appliances" }
]
```

### Get list of cities
```
GET /api/cities
```
**Response (200 OK):**
```json
[
  { "id": 1, "name": "Tehran" },
  { "id": 2, "name": "Isfahan" }
]
```

---

## 4. Favorites — requires JWT

### Get user's favorites
```
GET /api/favorites
Authorization: Bearer <token>
```
**Response (200 OK):**
```json
[
  { "id": 8, "advertisement": { "id": 5, "title": "Laptop Lenovo", "price": 18000000 } }
]
```

### Add advertisement to favorites
```
POST /api/favorites
Authorization: Bearer <token>
```
**Request Body:**
```json
{ "advertisementId": 5 }
```
**Response (201 Created):**
```json
{ "message": "Added to favorites" }
```
**Possible errors:** `400` (already added)

### Remove from favorites
```
DELETE /api/favorites/{id}
Authorization: Bearer <token>
```
**Response (200 OK):**
```json
{ "message": "Removed from favorites" }
```

---

## 5. Chat (Conversations & Messages) — requires JWT

### Get user's list of conversations
```
GET /api/conversations
Authorization: Bearer <token>
```
**Response (200 OK):**
```json
[
  {
    "id": 4,
    "advertisement": { "id": 5, "title": "Laptop Lenovo" },
    "otherUser": { "id": 3, "fullName": "Ali Rezaei" },
    "lastMessage": "Is this still available?",
    "lastMessageAt": "2026-07-19T18:00:00"
  }
]
```

### Get messages of a conversation
```
GET /api/conversations/{id}/messages
Authorization: Bearer <token>
```
**Response (200 OK):**
```json
[
  {
    "id": 20,
    "content": "Is this still available?",
    "sentAt": "2026-07-19T18:00:00",
    "senderId": 3
  }
]
```

### Send a new message (creates a conversation if none exists)
```
POST /api/conversations
Authorization: Bearer <token>
```
**Request Body:**
```json
{
  "advertisementId": 5,
  "content": "Is this still available?"
}
```
**Response (201 Created):**
```json
{ "conversationId": 4, "message": "Message sent" }
```
**Possible errors:** `400` (user cannot message themselves about their own advertisement)

### Send a message in an existing conversation
```
POST /api/conversations/{id}/messages
Authorization: Bearer <token>
```
**Request Body:**
```json
{ "content": "Yes, it's available." }
```
**Response (201 Created):**
```json
{ "message": "Message sent" }
```

---

## 6. Ratings — requires JWT

### Submit a rating for a seller
```
POST /api/ratings
Authorization: Bearer <token>
```
**Request Body:**
```json
{
  "advertisementId": 5,
  "score": 5,
  "comment": "Great seller, fast response!"
}
```
**Response (201 Created):**
```json
{ "message": "Rating submitted" }
```
**Possible errors:** `400` (score out of range 1-5, already rated, or user rating themselves)

### Get a seller's ratings (average + count)
```
GET /api/users/{id}/ratings
```
**Response (200 OK):**
```json
{
  "averageScore": 4.5,
  "totalRatings": 12
}
```

### Get a seller's text reviews
```
GET /api/users/{id}/ratings/comments?limit=3
```
`limit` is optional. If omitted, all text reviews are returned (used for the "see all reviews" page). If provided, only the first `limit` reviews (newest first) are returned, alongside the true total count so the client knows whether to show a "see all" option. Only ratings that include a non-empty comment are returned.

**Response (200 OK):**
```json
{
  "comments": [
    {
      "id": 9,
      "score": 5,
      "comment": "Great seller, fast response!",
      "raterName": "Ali Rezaei",
      "createdAt": "2026-07-19T18:00:00"
    }
  ],
  "totalCount": 7
}
```

---

## 7. Admin Panel — requires JWT with ADMIN role

### Get pending advertisements
```
GET /api/admin/advertisements/pending
Authorization: Bearer <token>
```
**Response (200 OK):**
```json
[
  { "id": 12, "title": "Laptop Lenovo", "owner": { "id": 3, "fullName": "Ali Rezaei" } }
]
```

### Approve advertisement
```
PUT /api/admin/advertisements/{id}/approve
Authorization: Bearer <token>
```
**Response (200 OK):**
```json
{ "message": "Advertisement approved" }
```

### Reject advertisement
```
PUT /api/admin/advertisements/{id}/reject
Authorization: Bearer <token>
```
**Request Body:**
```json
{ "reason": "Inappropriate content" }
```
**Response (200 OK):**
```json
{ "message": "Advertisement rejected" }
```

### Get list of users
```
GET /api/admin/users
Authorization: Bearer <token>
```
Only returns accounts with role `USER` — admin accounts are excluded from this list.

**Response (200 OK):**
```json
[
  { "id": 3, "fullName": "Ali Rezaei", "username": "ali_rezaei", "status": "ACTIVE" }
]
```

### Block a user
```
PUT /api/admin/users/{id}/block
Authorization: Bearer <token>
```
**Response (200 OK):**
```json
{ "message": "User blocked" }
```
**Possible errors:** `400` (target user is an admin — admin accounts cannot be blocked), `404`

### Unblock a user
```
PUT /api/admin/users/{id}/unblock
Authorization: Bearer <token>
```
**Response (200 OK):**
```json
{ "message": "User unblocked" }
```

---

## Standard status codes used in this project

| Code | Meaning |
|------|---------|
| 200 | Request successful |
| 201 | New resource created |
| 400 | Invalid input data |
| 401 | User not authenticated |
| 403 | User not authorized for this operation |
| 404 | Resource not found |
| 500 | Internal server error |

---

**Note for both team members:** This document is a draft. If, while coding, you find that an endpoint needs to change (new field, different path, etc.), update it here and let each other know so both Backend and Frontend stay in sync.
