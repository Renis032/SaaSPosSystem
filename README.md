# Renko SaaS POS System

Monorepo for the Renko point-of-sale platform.

## Structure

```text
renko-saas-pos-system/
├── backend/     Spring Boot REST API (Java 21, Maven)
├── frontend/    React + Vite + TypeScript SPA
└── README.md
```

### Backend (`backend/`)

- Controllers, services, repositories, entities, and JWT security
- Runs on port `5000`
- MySQL database: `pos`
- Schema reference: `backend/pos.sql`

### Frontend (`frontend/`)

```text
frontend/src/
├── api/           HTTP calls to the backend
├── components/    Shared UI and layout
├── features/      Domain modules (auth, products, orders, ...)
├── hooks/         Shared React hooks
├── lib/           Utilities (API client, helpers)
├── pages/         Route-level screens
├── routes/        React Router setup
├── stores/        Client state (auth token, etc.)
└── types/         Shared TypeScript types
```

Dev server runs on port `8080` and proxies `/api` and `/auth` to the backend.

Open `http://localhost:8080` for the **API Playground**: forms and buttons for every controller so you can exercise the database end-to-end.

## Prerequisites

- Java 21+
- Maven (or use `./mvnw`)
- MySQL
- Node.js 18+ (20+ recommended)

## Run locally

### 1. Database

Create a MySQL database named `pos` and optionally load `backend/pos.sql`.

Update credentials in `backend/src/main/resources/application.properties` if needed.

### 2. Backend

```bash
cd backend
./mvnw spring-boot:run
```

API: `http://localhost:5000`

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

App: `http://localhost:8080`
