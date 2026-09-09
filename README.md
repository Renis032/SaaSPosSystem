# Renko SaaS POS System

Monorepo for the my POS platform: Spring Boot API, React SPA, and an interview-ready demo flow.

## Authorship

- **Backend** — almost entirely written by hand (domain model, controllers, services, security, tenancy, orders, inventory, subscriptions, and related tests).
- **Frontend** — built with AI assistance (landing page, auth/POS/admin UI, playground wiring, styling, and demo tooling), on top of the hand-written API.

## Structure

```text
renko-saas-pos-system/
├── backend/              Spring Boot REST API (Java 21, Maven)
├── frontend/             React + Vite + TypeScript SPA
├── docker-compose.yml    Optional one-command local stack
├── .env.example          Docker / local env defaults
└── README.md
```

### Backend (`backend/`)

- Controllers, services, repositories, entities, JWT security, Flyway migrations
- Store tenancy / RBAC, inventory locking, subscriptions, reports, audit log
- Dev helpers: clear DB + fixed demo seed (`/api/dev/...` on the `dev` profile)
- Runs on port `5000`
- MySQL database: `pos`

### Frontend (`frontend/`)

```text
frontend/src/
├── api/           HTTP calls to the backend
├── components/    Shared UI, auth guards, layout
├── features/      Playground domain sections + full-system TEST
├── hooks/         Shared React hooks
├── lib/           Utilities (API client, roles, receipt PDF, …)
├── pages/         Landing, login/signup, POS, admin, workspace, playground
├── routes/        React Router setup
├── stores/        Client state (auth session, …)
└── types/         Shared TypeScript types
```

Dev server runs on port `8080` and proxies `/api` and `/auth` to the backend.

Open `http://localhost:8080` for the marketing landing page. From there you can reset the demo, sign in by role, use **POS** / **Admin**, or open the **API Playground**.

## Prerequisites

**Local run (Option A):**

- Java 21+
- Maven (or use `backend/./mvnw` — no global Maven install required)
- MySQL 8+ running locally
- Node.js 18+ (20+ recommended)

**Docker run (Option B):**

- Docker + Docker Compose only (MySQL, API, and UI all start from `docker compose up --build`)

## Install and run

There are two ways to run the project:

- **Option A (local)** — you install Java, Node, and MySQL yourself.
- **Option B (Docker)** — Compose starts **MySQL + backend + frontend** for you. You do **not** need a local MySQL install.

### Option A — Local (backend + frontend on your machine)

**1. Clone the repository**

```bash
git clone https://github.com/Renis032/SaaSPosSystem.git
```

**2. Create the database** (local MySQL only — skip this if you use Docker)

In MySQL:

```sql
CREATE DATABASE pos;
```

Default connection (override via env or `backend/.env.example`):

- Host: `localhost`
- Port: `3306`
- Database: `pos`
- User: `root`
- Password: `password`

**3. Install and start the backend**

```bash
cd backend
./mvnw spring-boot:run
```

- API: `http://localhost:5000`
- Profile defaults to `dev` (demo reset endpoints enabled)

Wait until Spring Boot finishes starting before opening the UI.

**4. Install and start the frontend**

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

- App: `http://localhost:8080`
- Vite proxies `/api` and `/auth` to the backend on port `5000`

**5. Open the app**

Visit [http://localhost:8080](http://localhost:8080), click **Reset demo**, then sign in with a demo role (see below).

### Option B — Docker (MySQL included)

Compose starts three services: **MySQL**, **backend**, and **frontend**. The MySQL container creates the `pos` database automatically (`MYSQL_DATABASE=pos`). No separate MySQL install or `CREATE DATABASE` step is required.

From the repo root:

```bash
cp .env.example .env   # optional: edit passwords / ports
docker compose up --build
```

Then open [http://localhost:8080](http://localhost:8080).

| Service | URL |
|---------|-----|
| Frontend | `http://localhost:8080` |
| API | `http://localhost:5000` |
| MySQL | `localhost:3306` (database `pos`, inside Docker) |

Stop with `Ctrl+C`, or `docker compose down` (add `-v` to wipe the MySQL volume and all data).

## Interview demo

1. Open `http://localhost:8080`
2. Click **Reset demo** (dev profile) to seed a fixed store and accounts
3. Sign in and pick a role — password for all demo users: `Demo1234!`

| Role | Email |
|------|--------|
| Owner | `owner@renko.demo` |
| Cashier | `cashier@renko.demo` |
| Manager | `manager@renko.demo` |
| Simple user | `user@renko.demo` |

Suggested walkthrough: Owner admin → Cashier POS (start shift, sell, PDF receipt) → Manager reports.

## CI

GitHub Actions runs selected backend unit tests and `frontend` production build (see `.github/workflows/ci.yml`).
