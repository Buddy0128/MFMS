# MFMS API Documentation

Base URL: `http://localhost:8080/api`

Interactive docs: `/api/swagger-ui.html`

## Authentication

All protected endpoints require header:
```
Authorization: Bearer <jwt_token>
```

### POST /auth/admin/login
```json
{ "phoneNumber": "9999999991", "pin": "1234" }
```

### POST /auth/member/login
```json
{ "phoneNumber": "9876543001" }
```

## Admin Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /admin/dashboard | Admin dashboard stats |
| GET | /admin/members | List/search members |
| GET | /admin/members/{id} | Member details |
| POST | /admin/members | Create member |
| PUT | /admin/members/{id} | Update member |
| PATCH | /admin/members/{id}/disable | Disable member |
| GET | /admin/contributions | List contributions |
| POST | /admin/contributions | Record contribution |
| PATCH | /admin/contributions/{id}/status | Update status |
| GET | /admin/loans | List loans |
| GET | /admin/loans/{id} | Loan details |
| POST | /admin/loans | Issue loan |
| PATCH | /admin/loans/{id}/close | Close loan |
| POST | /admin/payments/principal | Principal payment |
| POST | /admin/payments/interest | Interest payment |
| GET | /admin/borrowers | List external borrowers |
| POST | /admin/borrowers | Create borrower |
| GET | /admin/activity-logs | Activity history |
| GET | /admin/reports/*/excel | Excel exports |
| GET | /admin/reports/fund/pdf | Fund PDF report |

## Member Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /member/dashboard | Member dashboard |
| GET | /member/profile | Member profile |
| GET | /member/contributions | Own contributions |
| GET | /member/loans | Own loans |

## Fund Calculation

```
Available Fund = Total Paid Contributions + Total Interest Collected - Outstanding Principal
Monthly Interest = Outstanding Principal × (Interest Rate / 100)
```

Member rate: 1% | External rate: 5%
