# Sadhashiv Mitra Mandal Ghodbari Fund Management System

A production-ready web application for digital mandal fund management — replacing physical register-based systems.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React 18, Vite, TypeScript, Tailwind CSS, React Query, Axios |
| Backend | Java 17, Spring Boot 3.2, Spring Security, JWT, JPA |
| Database | MySQL 8+ |
| Docs | Swagger UI at `/api/swagger-ui.html` |

## Project Structure

```
mfms/
├── backend/          # Spring Boot REST API
├── frontend/         # React SPA
├── database/         # SQL schema & migrations
├── docker-compose.yml
└── README.md
```

## Quick Start (Local)

### Prerequisites
- Java 17+
- Node.js 18+
- MySQL 8+ (or Docker)

### 1. Database

```bash
mysql -u root -p < database/schema.sql
```

Use this local MySQL password when prompted:

```text
localhost@123#
```

Or use Docker:

```bash
docker-compose up mysql -d
```

### 2. Backend

**Local MySQL-backed dev profile:**

```bash
cd backend
mvn package -DskipTests
# PowerShell:
$env:SPRING_PROFILES_ACTIVE="dev"
java -jar target/mfms-backend-1.0.0.jar
```

API runs at `http://localhost:8080/api`

Swagger: `http://localhost:8080/api/swagger-ui.html`

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

App runs at `http://localhost:5173`

## Demo Credentials

### Admin Login (Phone + 4-digit PIN)
| Admin | Phone | PIN |
|-------|-------|-----|
| Admin One | 9999999991 | 1234 |
| Admin Two | 9999999992 | 5678 |

### Member Login (Phone only)
| Member | Phone |
|--------|-------|
| Ramesh Patel | 9876543001 |
| Suresh Shah | 9876543002 |

## Seed Data

On first startup, the backend creates the two admin accounts. Optional demo financial data can be enabled with `SEED_DEMO_DATA=true`, which seeds:
- 2 Admin accounts
- 20 Members
- 5 External Borrowers
- 10 Loans (5 member + 5 external)
- 6 months of contribution history
- Interest payment records

## Business Rules

- **Contributions**: ₹1,000/month per member (PAID/PENDING)
- **Member loans**: 1% monthly interest on outstanding principal
- **External loans**: 5% monthly interest on outstanding principal
- **Available Fund** = Total Contributions + Interest Collected − Outstanding Loans
- No repayment deadlines, penalties, or late fees

## API Endpoints

| Module | Base Path |
|--------|-----------|
| Auth | `/api/auth/*` |
| Admin Dashboard | `/api/admin/dashboard` |
| Members | `/api/admin/members` |
| Contributions | `/api/admin/contributions` |
| Loans | `/api/admin/loans` |
| Payments | `/api/admin/payments` |
| Borrowers | `/api/admin/borrowers` |
| Reports | `/api/admin/reports` |
| Activity Logs | `/api/admin/activity-logs` |
| Member Portal | `/api/member/*` |

## Docker (Full Stack)

```bash
docker-compose up --build
```

- Frontend: http://localhost:5173
- Backend: http://localhost:8080/api
- MySQL: localhost:3306

## Deployment

### Frontend → Vercel

1. Push repo to GitHub
2. Import project in Vercel, set root to `frontend`
3. Environment variable: `VITE_API_URL=https://your-backend.onrender.com/api`
4. Deploy

### Backend → Render

1. Create Web Service, root directory: `backend`
2. Build: `mvn clean package -DskipTests`
3. Start: `java -jar target/mfms-backend-1.0.0.jar`
4. Environment variables:
   - `DATABASE_URL` — Railway MySQL JDBC URL
   - `DATABASE_USERNAME`
   - `DATABASE_PASSWORD`
   - `JWT_SECRET` — strong 256-bit secret
   - `PORT=8080`

### Database → Railway MySQL

1. Create MySQL service on Railway
2. Run `database/schema.sql` via Railway console
3. Copy connection URL to Render env vars

## Environment Variables

### Backend (`application.yml` overrides)

| Variable | Default | Description |
|----------|---------|-------------|
| DATABASE_URL | jdbc:mysql://localhost:3306/mfms | JDBC connection |
| DATABASE_USERNAME | root | DB user |
| DATABASE_PASSWORD | localhost@123# | DB password |
| JWT_SECRET | (dev default) | JWT signing key |
| JWT_EXPIRATION | 86400000 | Token TTL (ms) |
| PORT | 8080 | Server port |

### Frontend

| Variable | Default | Description |
|----------|---------|-------------|
| VITE_API_URL | /api | Backend API base URL |

## License

Proprietary - Sadhashiv Mitra Mandal Ghodbari
