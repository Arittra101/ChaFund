# ChaFund — Backend API Documentation

> **Architecture**: Offline-first Android app with server sync.  
> **Currency**: All monetary amounts are stored and transmitted in **paisa** (1 Taka = 100 paisa).  
> **Dates**: All dates are **epoch days** (days since 1970-01-01, Java `LocalDate.toEpochDay()`).  
> **Timestamps**: All `createdAt` / `updatedAt` fields are **Unix milliseconds (epoch ms)**.  
> **Time**: Time-of-day strings use `"HH:mm"` 24-hour format.

---

## Table of Contents

1. [Authentication](#1-authentication)
2. [User](#2-user)
3. [Months](#3-months)
4. [Entries (Income)](#4-entries-income)
5. [Expenses](#5-expenses)
6. [Time Categories](#6-time-categories)
7. [Sync](#7-sync)
8. [Data Models](#8-data-models)
9. [Error Handling](#9-error-handling)
10. [Offline-First Strategy](#10-offline-first-strategy)

---

## Base URL

```
https://api.chafund.com/v1
```

All endpoints require `Content-Type: application/json` unless noted.

---

## 1. Authentication

All protected endpoints require:

```
Authorization: Bearer <access_token>
```

### 1.1 Register

```
POST /auth/register
```

**Request Body**
```json
{
  "name": "Arittra Roy",
  "email": "user@example.com",
  "password": "min8chars"
}
```

**Response `201`**
```json
{
  "user": {
    "id": "uuid",
    "name": "Arittra Roy",
    "email": "user@example.com",
    "createdAt": 1753000000000
  },
  "accessToken": "eyJ...",
  "refreshToken": "eyJ..."
}
```

---

### 1.2 Login

```
POST /auth/login
```

**Request Body**
```json
{
  "email": "user@example.com",
  "password": "min8chars"
}
```

**Response `200`**
```json
{
  "user": {
    "id": "uuid",
    "name": "Arittra Roy",
    "email": "user@example.com"
  },
  "accessToken": "eyJ...",
  "refreshToken": "eyJ..."
}
```

---

### 1.3 Refresh Token

```
POST /auth/refresh
```

**Request Body**
```json
{
  "refreshToken": "eyJ..."
}
```

**Response `200`**
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ..."
}
```

---

### 1.4 Logout

```
POST /auth/logout
```

**Request Body**
```json
{
  "refreshToken": "eyJ..."
}
```

**Response `204`** — No content. Invalidates the refresh token server-side.

---

## 2. User

### 2.1 Get Current User

```
GET /user/me
```

**Response `200`**
```json
{
  "id": "uuid",
  "name": "Arittra Roy",
  "email": "user@example.com",
  "createdAt": 1753000000000
}
```

---

### 2.2 Update Profile

```
PATCH /user/me
```

**Request Body** *(all fields optional)*
```json
{
  "name": "New Name"
}
```

**Response `200`** — Returns updated user object.

---

## 3. Months

A **Month** represents one calendar month of financial tracking. Only one month is marked `isCurrent = true` at any time.

### 3.1 List All Months

```
GET /months
```

**Query Parameters**

| Param | Type | Description |
|---|---|---|
| `includeSummary` | boolean | If `true`, attach income/expense totals to each month. Default `false`. |

**Response `200`**
```json
{
  "months": [
    {
      "id": "uuid",
      "year": 2026,
      "month": 7,
      "label": "July 2026",
      "isCurrent": true,
      "createdAt": 1753000000000,
      "summary": {
        "totalEntriesPaisa": 1500000,
        "totalSpentPaisa": 800000,
        "balancePaisa": 700000
      }
    }
  ]
}
```

> `summary` is only present when `includeSummary=true`.

---

### 3.2 Get Month by ID

```
GET /months/:id
```

**Response `200`**
```json
{
  "id": "uuid",
  "year": 2026,
  "month": 7,
  "label": "July 2026",
  "isCurrent": true,
  "createdAt": 1753000000000
}
```

---

### 3.3 Create or Get Month

The app calls this on resume to ensure the current calendar month exists. Behaves as an upsert.

```
POST /months/ensure
```

**Request Body**
```json
{
  "year": 2026,
  "month": 7
}
```

**Response `200`** — Returns the month (created or existing), with `isCurrent` promoted to `true`.

```json
{
  "id": "uuid",
  "year": 2026,
  "month": 7,
  "label": "July 2026",
  "isCurrent": true,
  "createdAt": 1753000000000
}
```

> The server must atomically unset `isCurrent` on all other months for this user and set it on this one.

---

### 3.4 Delete Past Month

```
DELETE /months/:id
```

> **Constraint**: Cannot delete the month where `isCurrent = true`. Returns `409` if attempted.  
> Deleting a month **cascades** and deletes all its entries and expenses.

**Response `204`** — No content.

---

## 4. Entries (Income)

An **Entry** records income/earnings for a specific day in a month.

### 4.1 List Entries for a Month

```
GET /months/:monthId/entries
```

**Query Parameters**

| Param | Type | Description |
|---|---|---|
| `date` | integer | Filter by epoch day. Optional. |

**Response `200`**
```json
{
  "entries": [
    {
      "id": "uuid",
      "monthId": "uuid",
      "amountPaisa": 50000,
      "ref": "Freelance payment",
      "date": 20289,
      "time": "14:30",
      "createdAt": 1753000000000,
      "updatedAt": 1753000000000
    }
  ]
}
```

---

### 4.2 Create Entry

```
POST /months/:monthId/entries
```

**Request Body**
```json
{
  "amountPaisa": 50000,
  "ref": "Freelance payment",
  "date": 20289,
  "time": "14:30"
}
```

> `ref` is optional. `date` and `time` default to server-side current date/time if omitted.

**Response `201`**
```json
{
  "id": "uuid",
  "monthId": "uuid",
  "amountPaisa": 50000,
  "ref": "Freelance payment",
  "date": 20289,
  "time": "14:30",
  "createdAt": 1753000000000,
  "updatedAt": 1753000000000
}
```

---

### 4.3 Update Entry

```
PATCH /entries/:id
```

**Request Body** *(all fields optional)*
```json
{
  "amountPaisa": 60000,
  "ref": "Updated note"
}
```

**Response `200`** — Returns updated entry.

---

### 4.4 Delete Entry

```
DELETE /entries/:id
```

**Response `204`** — No content.

---

## 5. Expenses

An **Expense** records spending for a specific day, tagged with a **TimeCategory** (Morning, Noon, Afternoon, Evening).

### 5.1 List Expenses for a Month

```
GET /months/:monthId/expenses
```

**Query Parameters**

| Param | Type | Description |
|---|---|---|
| `date` | integer | Filter by epoch day. Optional. |
| `categoryId` | string | Filter by time category UUID. Optional. |

**Response `200`**
```json
{
  "expenses": [
    {
      "id": "uuid",
      "monthId": "uuid",
      "timeCategoryId": "uuid",
      "amountPaisa": 15000,
      "ref": "Lunch",
      "date": 20289,
      "time": "13:15",
      "createdAt": 1753000000000,
      "updatedAt": 1753000000000,
      "category": {
        "id": "uuid",
        "name": "Noon",
        "sortOrder": 2
      }
    }
  ]
}
```

---

### 5.2 Create Expense

```
POST /months/:monthId/expenses
```

**Request Body**
```json
{
  "timeCategoryId": "uuid",
  "amountPaisa": 15000,
  "ref": "Lunch",
  "date": 20289,
  "time": "13:15"
}
```

> `ref` is optional.

**Response `201`** — Returns created expense with embedded `category` object.

---

### 5.3 Update Expense

```
PATCH /expenses/:id
```

**Request Body** *(all fields optional)*
```json
{
  "amountPaisa": 18000,
  "timeCategoryId": "uuid",
  "ref": "Lunch + dessert"
}
```

**Response `200`** — Returns updated expense.

---

### 5.4 Delete Expense

```
DELETE /expenses/:id
```

**Response `204`** — No content.

---

## 6. Time Categories

A **TimeCategory** groups expenses by time of day. Defaults are **Morning, Noon, Afternoon, Evening**.

### 6.1 List Categories

```
GET /categories
```

**Response `200`**
```json
{
  "categories": [
    { "id": "uuid", "name": "Morning",   "sortOrder": 1, "createdAt": 1753000000000 },
    { "id": "uuid", "name": "Noon",      "sortOrder": 2, "createdAt": 1753000000000 },
    { "id": "uuid", "name": "Afternoon", "sortOrder": 3, "createdAt": 1753000000000 },
    { "id": "uuid", "name": "Evening",   "sortOrder": 4, "createdAt": 1753000000000 }
  ]
}
```

---

### 6.2 Create Category

```
POST /categories
```

**Request Body**
```json
{
  "name": "Night",
  "sortOrder": 5
}
```

> Category names must be **unique (case-insensitive)** per user. Returns `409` on duplicate.

**Response `201`**
```json
{
  "id": "uuid",
  "name": "Night",
  "sortOrder": 5,
  "createdAt": 1753000000000
}
```

---

### 6.3 Rename Category

```
PATCH /categories/:id
```

**Request Body**
```json
{
  "name": "Late Night"
}
```

**Response `200`** — Returns updated category.

---

### 6.4 Delete Category

```
DELETE /categories/:id
```

> **Constraint**: Cannot delete a category that has expenses linked to it. Returns `409` with the count of linked expenses.

**Response `204`** — No content.

**Response `409`** (category in use)
```json
{
  "error": "CATEGORY_IN_USE",
  "message": "Category is referenced by 12 expense(s) and cannot be deleted.",
  "linkedCount": 12
}
```

---

## 7. Sync

The app is **offline-first**. Changes made locally while offline are pushed to the server once connectivity is restored. The server also has changes from other sessions (e.g., web app, other devices in future).

### 7.1 Push Local Changes (Upload)

Send all locally created/updated/deleted records since the last sync.

```
POST /sync/push
```

**Request Body**
```json
{
  "lastSyncedAt": 1752000000000,
  "months": {
    "upserted": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "year": 2026,
        "month": 7,
        "label": "July 2026",
        "isCurrent": true,
        "createdAt": 1753000000000
      }
    ],
    "deleted": ["uuid1", "uuid2"]
  },
  "entries": {
    "upserted": [
      {
        "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
        "monthId": "550e8400-e29b-41d4-a716-446655440000",
        "amountPaisa": 50000,
        "ref": "Salary",
        "date": 20289,
        "time": "10:00",
        "createdAt": 1753000000000,
        "updatedAt": 1753100000000
      }
    ],
    "deleted": ["uuid3"]
  },
  "expenses": {
    "upserted": [...],
    "deleted": [...]
  },
  "categories": {
    "upserted": [...],
    "deleted": [...]
  }
}
```

> All `id` fields are **client-generated UUID v4** — the app assigns them at creation time, even offline. The server stores them as-is. No ID mapping is needed.

**Response `200`**
```json
{
  "syncedAt": 1753200000000,
  "conflicts": [
    {
      "type": "entry",
      "id": "uuid",
      "resolution": "server_wins",
      "serverRecord": { ... }
    }
  ]
}
```

---

### 7.2 Pull Server Changes (Download)

Fetch all server-side changes since the last sync timestamp.

```
GET /sync/pull?since=1752000000000
```

**Response `200`**
```json
{
  "syncedAt": 1753200000000,
  "months": {
    "upserted": [...],
    "deleted": ["uuid-of-deleted-month"]
  },
  "entries": {
    "upserted": [...],
    "deleted": []
  },
  "expenses": {
    "upserted": [...],
    "deleted": []
  },
  "categories": {
    "upserted": [...],
    "deleted": []
  }
}
```

---

### 7.3 Conflict Resolution Policy

| Scenario | Resolution |
|---|---|
| Same record edited on device and server | **Last-write-wins** based on `updatedAt` timestamp |
| Record deleted on server, edited on device | **Server delete wins** — local edit is dropped |
| Record deleted on device, edited on server | **Server edit wins** — deletion is cancelled |
| Duplicate category name on upsert | Server rejects — return `409` in `conflicts` array |

---

## 8. Data Models

### Month

| Field | Type | Description |
|---|---|---|
| `id` | string (UUID) | **Client-generated** UUID v4, assigned by the app at creation time |
| `year` | integer | Calendar year, e.g. `2026` |
| `month` | integer | Month number `1–12` |
| `label` | string | Human label, e.g. `"July 2026"` |
| `isCurrent` | boolean | Only one month per user is `true` |
| `createdAt` | integer | Unix ms |

---

### Entry

| Field | Type | Description |
|---|---|---|
| `id` | string (UUID) | **Client-generated** UUID v4, assigned by the app at creation time |
| `monthId` | string (UUID) | Parent month |
| `amountPaisa` | integer | Amount in paisa (positive) |
| `ref` | string \| null | Optional note / reference |
| `date` | integer | Epoch day (LocalDate.toEpochDay()) |
| `time` | string | `"HH:mm"` 24-hour format |
| `createdAt` | integer | Unix ms |
| `updatedAt` | integer | Unix ms |

---

### Expense

| Field | Type | Description |
|---|---|---|
| `id` | string (UUID) | **Client-generated** UUID v4, assigned by the app at creation time |
| `monthId` | string (UUID) | Parent month |
| `timeCategoryId` | string (UUID) | Time-of-day category |
| `amountPaisa` | integer | Amount in paisa (positive) |
| `ref` | string \| null | Optional note / reference |
| `date` | integer | Epoch day |
| `time` | string | `"HH:mm"` 24-hour format |
| `createdAt` | integer | Unix ms |
| `updatedAt` | integer | Unix ms |

---

### TimeCategory

| Field | Type | Description |
|---|---|---|
| `id` | string (UUID) | **Client-generated** UUID v4, assigned by the app at creation time |
| `name` | string | Unique per user, case-insensitive |
| `sortOrder` | integer | Display order (1 = first) |
| `createdAt` | integer | Unix ms |

Default seeded categories per new user:

| Name | sortOrder |
|---|---|
| Morning | 1 |
| Noon | 2 |
| Afternoon | 3 |
| Evening | 4 |

---

### MonthSummary (computed, not stored)

| Field | Type | Description |
|---|---|---|
| `totalEntriesPaisa` | integer | Sum of all entry amounts |
| `totalSpentPaisa` | integer | Sum of all expense amounts |
| `balancePaisa` | integer | `totalEntries - totalSpent` |

---

### DailySummary (computed, not stored)

| Field | Type | Description |
|---|---|---|
| `date` | integer | Epoch day |
| `totalEntriesForDay` | integer | Income for this day in paisa |
| `totalSpentForDay` | integer | Expenses for this day in paisa |
| `balanceAtPoint` | integer | Cumulative balance up to this day |

---

## 9. Error Handling

All error responses use this shape:

```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable description"
}
```

### HTTP Status Codes

| Status | Meaning |
|---|---|
| `200` | Success |
| `201` | Created |
| `204` | No content (delete success) |
| `400` | Bad request / validation error |
| `401` | Unauthenticated — missing or invalid token |
| `403` | Forbidden — authenticated but not authorized |
| `404` | Resource not found |
| `409` | Conflict — e.g. duplicate category name, deleting current month |
| `422` | Unprocessable — business rule violation |
| `500` | Internal server error |

### Error Codes

| Code | Trigger |
|---|---|
| `VALIDATION_ERROR` | Missing/invalid fields in request body |
| `UNAUTHENTICATED` | No or expired access token |
| `NOT_FOUND` | Resource does not exist or belongs to another user |
| `DUPLICATE_CATEGORY` | Category name already exists for this user |
| `CATEGORY_IN_USE` | Attempting to delete a category with linked expenses |
| `CANNOT_DELETE_CURRENT_MONTH` | Attempting to delete the active month |
| `INVALID_MONTH` | monthId does not belong to authenticated user |
| `TOKEN_EXPIRED` | Access token expired — client should refresh |
| `INVALID_REFRESH_TOKEN` | Refresh token invalid or revoked |

---

## 10. Offline-First Strategy

### How the App Works Offline

1. All reads and writes go to the local **Room SQLite** database first.
2. Every mutation is queued as a **pending sync operation** with a local timestamp.
3. On network reconnect, the app calls `POST /sync/push` to upload pending changes.
4. After a successful push, the app calls `GET /sync/pull?since=<lastSyncedAt>` to pull any server-side changes (from other sessions / admin actions).
5. `lastSyncedAt` is persisted locally (DataStore) and updated after every successful sync cycle.

### Server Requirements

- Every write endpoint must record the `updatedAt` timestamp server-side.
- Soft deletes are recommended: mark records `deletedAt = <timestamp>` rather than hard-deleting, so the pull endpoint can return tombstones for the sync window.
- Hard deletes should only occur after a configurable retention window (e.g., 30 days after `deletedAt`).
- Each user's data must be fully isolated — all queries must be scoped to `userId`.

### UUID Strategy

The app currently uses Room auto-increment `Long` IDs. For backend integration, the Room schema should be migrated to use **client-generated UUID strings** as primary keys:

- At record creation time (even offline), the app calls `UUID.randomUUID().toString()` and stores it as the record's `id`.
- This UUID is the permanent identifier — used in Room, in sync payloads, and in all API calls.
- The server stores the UUID as-is and never reassigns it.
- No `idMappings` or two-phase ID resolution is needed.

### Security Rules

- A user may only read/write their own months, entries, expenses, and categories.
- All foreign key references (`monthId`, `timeCategoryId`) must be validated to belong to the authenticated user before accepting a write.
- Tokens should be short-lived (e.g., access token: 15 minutes, refresh token: 30 days).
