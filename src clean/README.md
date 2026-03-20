# Laundry App - Backend API

Backend REST API untuk aplikasi laundry Android, dibangun dengan **Ktor + Kotlin + PostgreSQL + Exposed ORM**.

## Tech Stack
- **Framework**: Ktor (Kotlin)
- **ORM**: Jetbrains Exposed
- **Database**: PostgreSQL
- **Auth**: JWT + BCrypt
- **DI**: Koin
- **Serialization**: Kotlinx Serialization

## Struktur Proyek

```
src/main/kotlin/
├── Application.kt              # Entry point, konfigurasi Ktor
├── Routing.kt                  # Definisi semua route API
├── dao/
│   ├── OrderDAO.kt             # Exposed DAO untuk order
│   ├── UserDAO.kt
│   └── RefreshTokenDAO.kt
├── data/
│   ├── OrderRequest.kt         # Request body + toEntity()
│   ├── OrderListData.kt        # Response wrapper untuk list order
│   ├── DataResponse.kt         # Generic response wrapper
│   ├── ErrorResponse.kt
│   ├── AppException.kt
│   ├── AuthRequest.kt
│   ├── RefreshTokenRequest.kt
│   └── UserResponse.kt
├── entities/
│   ├── Order.kt                # Data class entity order
│   ├── User.kt
│   └── RefreshToken.kt
├── helpers/
│   ├── ContHelper.kt           # JWT constants
│   ├── DatabaseHelper.kt       # DB connect + auto-create tables
│   ├── MappingHelper.kt        # DAO → Entity mapping + suspendTransaction
│   ├── ServiceHelper.kt        # Auth helper (getAuthUser)
│   ├── ToolsHelper.kt          # BCrypt utils
│   └── ValidatorHelper.kt      # Request validator
├── module/
│   └── AppModule.kt            # Koin DI module
├── repositories/
│   ├── IOrderRepository.kt
│   ├── OrderRepository.kt      # CRUD order ke database
│   ├── IUserRepository.kt
│   ├── UserRepository.kt
│   ├── IRefreshTokenRepository.kt
│   └── RefreshTokenRepository.kt
├── services/
│   ├── OrderService.kt         # Business logic order (CRUD + validasi)
│   ├── AuthService.kt          # Login, Register, RefreshToken, Logout
│   └── UserService.kt          # Profile management
└── tables/
    ├── OrderTable.kt           # Exposed DSL table definition
    ├── UserTable.kt
    └── RefreshTokenTable.kt
```

## Setup

### 1. Buat Database PostgreSQL
```sql
CREATE DATABASE db_laundry;
```

### 2. Konfigurasi .env
```bash
cp .env.example .env
# Edit .env sesuai konfigurasi database kamu
```

### 3. Jalankan
```bash
./gradlew run
```
> Tabel akan dibuat otomatis saat pertama kali dijalankan.

---

## API Endpoints

### Auth (Tidak perlu token)
| Method | Endpoint             | Keterangan        |
|--------|----------------------|-------------------|
| POST   | /auth/register       | Daftar akun baru  |
| POST   | /auth/login          | Login             |
| POST   | /auth/refresh-token  | Refresh JWT token |
| POST   | /auth/logout         | Logout            |

### Orders (Butuh Bearer Token)
| Method | Endpoint         | Keterangan                   |
|--------|------------------|------------------------------|
| GET    | /orders          | Ambil semua order + statistik|
| GET    | /orders?search=X | Cari order by nama/status    |
| POST   | /orders          | Tambah order baru            |
| GET    | /orders/{id}     | Detail order by ID           |
| PUT    | /orders/{id}     | Update order                 |
| DELETE | /orders/{id}     | Hapus order                  |

### Users (Butuh Bearer Token)
| Method | Endpoint          | Keterangan           |
|--------|-------------------|----------------------|
| GET    | /users/me         | Lihat profil saya    |
| PUT    | /users/me         | Update profil        |
| PUT    | /users/me/password| Ganti password       |
| PUT    | /users/me/photo   | Upload foto profil   |

---

## Contoh Request / Response

### POST /orders
**Request:**
```json
{
  "customerName": "David",
  "contactNumber": "08123456789",
  "serviceType": "Dry Cleaning",
  "weightKg": 2.5,
  "pickupDate": "2025-03-16",
  "notes": ""
}
```
**Response:**
```json
{
  "status": "success",
  "message": "Berhasil menambahkan order",
  "data": { "orderId": "uuid-order" }
}
```

### GET /orders
**Response:**
```json
{
  "status": "success",
  "message": "Berhasil mengambil daftar order",
  "data": {
    "orders": [...],
    "totalOrders": 5,
    "newOrders": 1,
    "inProgressOrders": 2,
    "readyForPickupOrders": 1,
    "completedOrders": 1
  }
}
```

---

## Service Types & Harga per kg
| Service       | Harga/kg    |
|---------------|-------------|
| Wash & Fold   | Rp 15.000   |
| Dry Cleaning  | Rp 20.000   |
| Iron Only     | Rp 8.000    |
| Wash & Iron   | Rp 18.000   |

## Status Order
`New` → `Washing` → `In Progress` → `Ready for Pickup` → `Completed`
