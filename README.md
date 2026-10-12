# JobBridge — React + Spring Boot

A separated job recruitment portal with a React/Vite frontend, Spring Boot REST API, and MySQL database. Includes sample job listings, seeker registration/login, company accounts, job posting, applications, and application status updates.

## Requirements
- Java 17 or newer
- Maven 3.9+
- Node.js 20+ and npm
- MySQL 8+

## 1. Create the database
From this project folder:
```bash
mysql -u root -p < backend/schema.sql
mysql -u root -p < backend/seed_demo.sql
```
Enter the MySQL password when prompted. Do not type your password as a terminal command.

## 2. Run Spring Boot backend
In Terminal 1:
```bash
cd backend
export DB_USER=root
export DB_PASSWORD='YOUR_MYSQL_PASSWORD'
# Optional if your MySQL uses a different URL:
# export DB_URL='jdbc:mysql://127.0.0.1:3306/jobbridge?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
mvn spring-boot:run
```
API: http://127.0.0.1:8080/api/health

## 3. Run React frontend
In Terminal 2:
```bash
cd frontend
npm install
npm run dev
```
Open the exact URL Vite prints, normally http://127.0.0.1:5173.

## Demo company accounts
Run `seed_demo.sql` to create sample companies. For each sample company, the demo password is `password` (change these credentials before sharing the project).

- `recruitment@techspire.demo`
- `careers@cloudnest.demo`
- `jobs@brightpath.demo`

## Features
- Responsive React UI with search/filter and sample listings
- Register and log in as a seeker or company
- Companies can post jobs and review/update applications
- Job seekers can apply and view their applications
- Spring Boot REST API and MySQL persistence

## API endpoints
- `GET /api/health`
- `GET /api/jobs?q=developer`
- `POST /api/register`
- `POST /api/login`
- `POST /api/jobs`
- `POST /api/applications`
- `GET /api/applications?userId=1&role=seeker`
- `PATCH /api/applications/{id}/status`

## Important security note
This is a local academic demo. The login endpoint returns a user record but does not issue a session/JWT, so the UI keeps the selected account in client memory. Before production, add Spring Security, server-side authorization, JWT/session management, validation, rate limiting, HTTPS, and secure secrets. Change the sample password hashes/accounts before using outside local development.
