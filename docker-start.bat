@echo off
REM docker-start.bat - Start Library Management System services (Windows)

setlocal enabledelayedexpansion

echo.
echo 📦 Starting Library Management System with Docker Compose...
echo.

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker is not running. Please start Docker and try again.
    exit /b 1
)

REM Parse command line arguments
set PROFILE=
if "%1"=="--with-tools" (
    set PROFILE=--profile tools
    echo 🛠️  Starting with additional tools (PhpMyAdmin, Redis Commander)...
)
if "%1"=="-t" (
    set PROFILE=--profile tools
    echo 🛠️  Starting with additional tools (PhpMyAdmin, Redis Commander)...
)

REM Start services
echo 🚀 Starting services...
docker-compose %PROFILE% up -d

REM Wait for services
echo.
echo ⏳ Waiting for services to be ready...
timeout /t 5 /nobreak

REM Check service health
echo.
echo ✅ Checking service health...

REM MySQL health check
docker-compose exec -T mysql mysqladmin ping -u root -proot >nul 2>&1
if errorlevel 1 (
    echo ⚠️  MySQL is starting... ^(this may take a moment^)
) else (
    echo ✓ MySQL is ready
)

REM Redis health check
docker-compose exec -T redis redis-cli -a redis_pass123 ping >nul 2>&1
if errorlevel 1 (
    echo ⚠️  Redis is starting... ^(this may take a moment^)
) else (
    echo ✓ Redis is ready
)

REM Application health check
echo ⏳ Waiting for application to start...
setlocal enabledelayedexpansion
for /l %%i in (1,1,30) do (
    curl -s http://localhost:8080/actuator/health >nul 2>&1
    if errorlevel 0 (
        echo ✓ Application is ready
        goto app_ready
    )
    if %%i equ 30 (
        echo ⚠️  Application is still starting ^(check logs with: docker-compose logs -f app^)
    )
    timeout /t 2 /nobreak >nul
)

:app_ready
echo.
echo ════════════════════════════════════════════════════════════
echo 🎉 Library Management System is running!
echo ════════════════════════════════════════════════════════════
echo.
echo 📍 Services:
echo    API:          http://localhost:8080
echo    Swagger UI:   http://localhost:8080/swagger-ui.html
echo    API Docs:     http://localhost:8080/api-docs
echo    Health:       http://localhost:8080/actuator/health
echo.
echo 📊 Database:
echo    Host:         localhost:3306
echo    Database:     librarymanagement
echo    User:         library_user
echo    Password:     library_pass123
echo.
echo 🗄️  Cache:
echo    Host:         localhost:6379
echo    Password:     redis_pass123
echo.

if not "!PROFILE!"=="" (
    echo 🛠️  Management Tools:
    echo    PhpMyAdmin:   http://localhost:8082
    echo    Redis Cmd:    http://localhost:8081
    echo.
)

echo 📝 Useful commands:
echo    View logs:        docker-compose logs -f
echo    Specific logs:    docker-compose logs -f app
echo    Stop services:    docker-compose down
echo    MySQL shell:      docker exec -it library-mysql mysql -u root -proot
echo    Redis shell:      docker exec -it library-redis redis-cli -a redis_pass123
echo.
echo 📚 More info: cat DOCKER_SETUP.md
echo ════════════════════════════════════════════════════════════
echo.

endlocal
