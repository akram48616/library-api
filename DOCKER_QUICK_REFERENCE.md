# Docker Quick Reference - Library Management System

## 🚀 Quick Start

### Windows
```bash
.\docker-start.bat
# or with tools
.\docker-start.bat --with-tools
```

### macOS/Linux
```bash
chmod +x docker-start.sh
./docker-start.sh
# or with tools
./docker-start.sh --with-tools
```

### Manual (All Platforms)
```bash
docker-compose up -d
# with tools
docker-compose --profile tools up -d
```

---

## 📍 Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| **API** | http://localhost:8080 | N/A |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | N/A |
| **API Docs (JSON)** | http://localhost:8080/api-docs | N/A |
| **Health Check** | http://localhost:8080/actuator/health | N/A |
| **PhpMyAdmin** | http://localhost:8082 | root / root |
| **Redis Commander** | http://localhost:8081 | N/A |

---

## 🛑 Stop Services

```bash
# Stop all services
docker-compose down

# Stop and remove all data (volumes)
docker-compose down -v

# Stop specific service
docker-compose stop app
docker-compose stop mysql
docker-compose stop redis
```

---

## 📊 Database Access

### Using Command Line

**MySQL:**
```bash
docker exec -it library-mysql mysql -u root -proot librarymanagement
```

**Redis:**
```bash
docker exec -it library-redis redis-cli -a redis_pass123
```

### Using GUI Tools

- **PhpMyAdmin:** http://localhost:8082
- **Redis Commander:** http://localhost:8081

---

## 📝 View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f app      # Application logs
docker-compose logs -f mysql    # Database logs
docker-compose logs -f redis    # Cache logs

# Last 100 lines
docker-compose logs --tail 100 app

# Follow new logs only
docker-compose logs -f app
```

---

## 🔄 Restart Services

```bash
# Restart all services
docker-compose restart

# Restart specific service
docker-compose restart app
docker-compose restart mysql
docker-compose restart redis

# Recreate containers (restart with fresh state)
docker-compose up -d
```

---

## 🗄️ Database Operations

### Export Database
```bash
docker exec library-mysql mysqldump -u root -proot librarymanagement > backup.sql
```

### Import Database
```bash
docker exec -i library-mysql mysql -u root -proot librarymanagement < backup.sql
```

### Interactive MySQL Shell
```bash
docker exec -it library-mysql mysql -u root -proot librarymanagement

# Inside MySQL:
SHOW TABLES;
SELECT * FROM authors;
DESC books;
```

---

## 💾 Redis Operations

### Check Redis Status
```bash
docker exec library-redis redis-cli -a redis_pass123 info
```

### View All Keys
```bash
docker exec library-redis redis-cli -a redis_pass123 keys '*'
```

### Clear All Cache
```bash
docker exec library-redis redis-cli -a redis_pass123 FLUSHALL
```

### Interactive Redis Shell
```bash
docker exec -it library-redis redis-cli -a redis_pass123

# Inside Redis:
PING
KEYS *
DBSIZE
FLUSHALL
```

---

## 🔍 Debugging

### Check Container Status
```bash
docker-compose ps
```

### View Container Logs (Errors)
```bash
docker-compose logs --tail 50 app
```

### Execute Command in Container
```bash
docker exec library-app ls -la /app/logs
docker exec library-mysql mysql -u root -proot -e "SHOW VARIABLES LIKE 'datadir';"
```

### Inspect Network
```bash
docker network ls
docker network inspect library-network
```

---

## 🐛 Troubleshooting

### Connection Refused

**MySQL:**
```bash
# Check if running
docker-compose ps mysql

# Check logs
docker-compose logs mysql

# Restart
docker-compose restart mysql
```

**Redis:**
```bash
# Test connection
docker exec library-redis redis-cli -a redis_pass123 PING

# Restart
docker-compose restart redis
```

**App:**
```bash
# Check app logs
docker-compose logs app

# Verify database connection
docker exec library-app curl http://mysql:3306

# Restart app
docker-compose restart app
```

### Port Already in Use

```bash
# Kill process using port (Linux/macOS)
sudo lsof -ti:8080 | xargs kill -9

# Find process (Windows)
netstat -ano | findstr :8080

# Change port in docker-compose.yml:
# Change "8080:8080" to "9090:8080"
```

### Out of Memory

```bash
# Check resource usage
docker stats

# Increase Docker memory limit or reduce cache
# Edit docker-compose.yml or .env
```

---

## 🔧 Environment Variables

Located in `.env` file:

```bash
# MySQL
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=librarymanagement
MYSQL_USER=library_user
MYSQL_PASSWORD=library_pass123

# Redis
REDIS_PASSWORD=redis_pass123

# Spring Boot
SERVER_PORT=8080
JWT_SECRET=LibraryManagementSystemSecretKey2026!!
SPRING_CACHE_TYPE=redis
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

**To override:** Create `.env.local` with custom values

---

## 📦 Docker Operations

### View Images
```bash
docker images | grep library
```

### Remove Images
```bash
docker image rm library-app
docker image prune -a  # Remove unused images
```

### View Volumes
```bash
docker volume ls | grep library
```

### Inspect Volume
```bash
docker volume inspect library_mysql-data
```

### Backup Volume
```bash
docker run --rm -v library_mysql-data:/data \
  -v $(pwd):/backup alpine tar czf /backup/db-backup.tar.gz -C /data .
```

---

## 🛠️ Build & Rebuild

### Build Application Image
```bash
docker-compose build app
```

### Rebuild Without Cache
```bash
docker-compose build --no-cache app
```

### View Build Logs
```bash
docker-compose build --verbose app
```

---

## 📊 Monitoring

### Real-time Resource Usage
```bash
docker stats --no-stream
docker stats  # Continuous monitoring
```

### Container Health Check
```bash
docker-compose ps
# Shows health status in output
```

### Check Application Health
```bash
curl http://localhost:8080/actuator/health
```

---

## 🚀 Production Commands

### Start Production Setup
```bash
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### Stop Production Setup
```bash
docker-compose -f docker-compose.yml -f docker-compose.prod.yml down
```

### View Production Logs
```bash
docker-compose -f docker-compose.yml -f docker-compose.prod.yml logs -f
```

---

## 📚 Useful Links

- [Docker Documentation](https://docs.docker.com)
- [Docker Compose Reference](https://docs.docker.com/compose/compose-file/)
- [Spring Boot Docker](https://spring.io/guides/topicals/spring-boot-docker)
- [MySQL Docker Image](https://hub.docker.com/_/mysql)
- [Redis Docker Image](https://hub.docker.com/_/redis)

---

## 📄 Files Structure

```
LibraryManagementSystem/
├── docker-compose.yml          # Main compose file
├── docker-compose.prod.yml     # Production overrides
├── Dockerfile                   # App image build
├── .dockerignore               # Docker build ignore
├── .env                        # Environment variables
├── docker-start.sh             # macOS/Linux startup
├── docker-start.bat            # Windows startup
├── init-db.sql                 # Database initialization
├── DOCKER_SETUP.md             # Detailed guide
└── DOCKER_QUICK_REFERENCE.md   # This file
```

---

## 💡 Tips

- Always use `.env` for sensitive configuration
- Keep backups of database volumes
- Monitor logs regularly in production
- Use named volumes for persistent data
- Test backups before relying on them
- Update images regularly for security patches
- Use health checks for critical services
- Implement logging centralization

---

**Last Updated:** 2026-08-24
**Version:** 1.0
