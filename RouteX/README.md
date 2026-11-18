# RouteX – Iterations 0 & 1 (Spring Boot + React)

## Requirements
- JDK 17+ and Maven 3.8+  (`java -version`, `mvn -version`)
- Node.js 18+ (`node -v`)
- No database install needed: the backend uses an embedded H2 file DB by default.

## Run (two terminals)
**Terminal 1 – backend** (http://localhost:8080)
- Linux/macOS: `./run-backend.sh`
- Windows: double-click `run-backend.bat`  (or `cd backend && mvn spring-boot:run`)

**Terminal 2 – frontend** (http://localhost:5173)
- Linux/macOS: `./run-frontend.sh`
- Windows: double-click `run-frontend.bat`

Open http://localhost:5173

## Default dev admin (local only – change via env vars)
- Email: `admin@routex.local`  Password: `Admin@12345`
- Env overrides: `ROUTEX_ADMIN_EMAIL`, `ROUTEX_ADMIN_PASSWORD`, `ROUTEX_JWT_SECRET` (>=32 chars)

## Tests
`cd backend && mvn test`  (10 integration tests, in-memory H2)

## API docs
http://localhost:8080/swagger-ui.html

## Using MySQL instead
```
DB_ROOT_PASSWORD=pass docker compose up -d
cd backend
DB_PASSWORD=pass mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

## Production note
The default JWT secret and admin password are for development only. Set real values via environment variables before any deployment.
