# Docker Setup Guide - Library Management System

## Overview

This guide explains how to run the Library Management System using Docker and Docker Compose.

## Prerequisites

- Docker (version 20.10+)
- Docker Compose (version 2.0+)
- 4GB minimum RAM available for containers

## Quick Start

### 1. **Run All Services (Production-like setup)**

```bash
docker-compose up -d
```

This will start:
- MySQL Database
- Redis Cache
- Spring Boot Application

### 2. **Run with Additional Tools (PhpMyAdmin + Redis Commander)**

```bash
docker-compose --profile tools up -d
```

Additional services:
- PhpMyAdmin (MySQL UI) at http://localhost:8082
- Redis Commander (Redis UI) at http://localhost:8081

### 3. **View Logs**

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f app
docker-compose logs -f mysql
docker-compose logs -f redis
```

### 4. **Stop All Services**

```bash
docker-compose down
```

### 5. **Remove All Data (including volumes)**

```bash
docker-compose down -v
```

---

## Services Details

### **MySQL Database**
- **Image:** mysql:8.0
- **Container:** library-mysql
- **Port:** 3306
- **Default User:** root
- **Default Password:** root
- **Database:** librarymanagement
- **Credentials:**
  - User: `library_user`
  - Password: `library_pass123`

**Access:**
```bash
docker exec -it library-mysql mysql -u root -proot librarymanagement
```

### **Redis Cache**
- **Image:** redis:7.2-alpine
- **Container:** library-redis
- **Port:** 6379
- **Password:** redis_pass123
- **Persistence:** Enabled (AOF)

**Access:**
```bash
docker exec -it library-redis redis-cli -a redis_pass123
```

### **Spring Boot Application**
- **Image:** Built from Dockerfile
- **Container:** library-app
- **Port:** 8080
- **Java Version:** 17
- **Build:** Multi-stage Maven build

**Access:**
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/api-docs
- Health: http://localhost:8080/actuator/health

### **PhpMyAdmin (Optional)**
- **Image:** phpmyadmin:5.2-apache
- **Container:** library-phpmyadmin
- **Port:** 8082
- **URL:** http://localhost:8082
- **Credentials:** root / root

### **Redis Commander (Optional)**
- **Image:** rediscommander/redis-commander
- **Container:** library-redis-commander
- **Port:** 8081
- **URL:** http://localhost:8081

---

## Environment Configuration

### Using .env File

The `.env` file contains default configuration values. To override:

1. Create `.env.local`:
```bash
cp .env .env.local
```

2. Edit `.env.local` with your values

3. Docker Compose will automatically use both `.env` and `.env.local`

### Key Environment Variables

```env
# MySQL
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=librarymanagement
MYSQL_USER=library_user
MYSQL_PASSWORD=library_pass123

# Redis
REDIS_PASSWORD=redis_pass123

# Spring Boot
SERVER_PORT=8080
SPRING_CACHE_TYPE=redis
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

---

## Docker Compose File Structure

```yaml
version: '3.8'
services:
  mysql:           # Database service
  redis:           # Cache service
  redis-commander: # Redis UI (optional)
  phpmyadmin:      # MySQL UI (optional)
  app:             # Spring Boot application

networks:
  library-network: # Custom bridge network

volumes:
  mysql-data:      # MySQL persistent storage
  redis-data:      # Redis persistent storage
```

---

## Building & Customization

### **Rebuild the Application Image**

```bash
docker-compose build --no-cache app
```

### **Build Specific Configuration**

```bash
# Build with custom Dockerfile
docker-compose build --file docker/Dockerfile app

# Build with build arguments
docker-compose build --build-arg JAVA_VERSION=21 app
```

### **Custom Environment**

Create a separate compose file:
```bash
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

---

## Networking

All services communicate via the `library-network` bridge network:

```
┌─────────────────────────────────────────┐
│       library-network (bridge)          │
├─────────────────────────────────────────┤
│  app ←→ mysql:3306                      │
│  app ←→ redis:6379                      │
│  phpmyadmin ←→ mysql:3306               │
│  redis-commander ←→ redis:6379          │
└─────────────────────────────────────────┘
```

**Inter-service communication:**
- From app to MySQL: `jdbc:mysql://mysql:3306/librarymanagement`
- From app to Redis: `redis://redis:6379`

---

## Data Persistence

### Volumes

Both MySQL and Redis use named volumes for data persistence:

```bash
# List volumes
docker volume ls | grep library

# Inspect volume
docker volume inspect library_mysql-data
docker volume inspect library_redis-data

# Backup MySQL data
docker run --rm -v library_mysql-data:/data -v $(pwd):/backup \
  alpine tar czf /backup/mysql-backup.tar.gz -C /data .

# Restore MySQL data
docker run --rm -v library_mysql-data:/data -v $(pwd):/backup \
  alpine tar xzf /backup/mysql-backup.tar.gz -C /data
```

### Logs Volume

Application logs are stored in `./logs`:
```bash
ls -la ./logs/
tail -f ./logs/library.log
```

---

## Health Checks

All services include health checks:

```bash
# Check service health
docker-compose ps

# Manual health check
curl http://localhost:8080/actuator/health
docker exec library-mysql mysqladmin ping -u root -proot
docker exec library-redis redis-cli -a redis_pass123 ping
```

---

## Common Tasks

### **Import SQL Script**

```bash
docker exec -i library-mysql mysql -u root -proot librarymanagement < backup.sql
```

### **Export Database**

```bash
docker exec library-mysql mysqldump -u root -proot librarymanagement > backup.sql
```

### **Access MySQL Shell**

```bash
docker exec -it library-mysql mysql -u root -proot
```

### **Access Redis Shell**

```bash
docker exec -it library-redis redis-cli -a redis_pass123
```

### **View MySQL Slow Queries**

```bash
docker exec library-mysql tail -f /var/log/mysql/slow.log
```

### **Restart Specific Service**

```bash
docker-compose restart app
```

### **Scale Services (if applicable)**

```bash
# Note: app needs unique port mapping for multiple instances
docker-compose up -d --scale app=3
```

---

## Troubleshooting

### **Connection Refused Error**

```bash
# Check if services are running
docker-compose ps

# Restart services
docker-compose down
docker-compose up -d

# Check network
docker network inspect library-network
```

### **MySQL Connection Timeout**

```bash
# Check MySQL logs
docker-compose logs mysql

# Verify MySQL is healthy
docker exec library-mysql mysqladmin ping -u root -proot

# Restart MySQL
docker-compose restart mysql
```

### **Redis Connection Refused**

```bash
# Test Redis connection
docker exec library-redis redis-cli -a redis_pass123 ping

# Check Redis logs
docker-compose logs redis

# Verify Redis password
docker-compose exec redis redis-cli -a redis_pass123 CONFIG GET requirepass
```

### **App Can't Connect to Database**

```bash
# Check if app is using correct host
docker exec library-app env | grep DATASOURCE

# Test connectivity from app
docker exec library-app curl http://mysql:3306

# Check MySQL is accepting connections
docker exec library-mysql netstat -tlnp | grep 3306
```

### **Out of Memory**

```bash
# Reduce cache TTL in .env
SPRING_CACHE_REDIS_TIME_TO_LIVE=300000

# Clear unused Docker data
docker system prune -a
```

### **Port Already in Use**

```bash
# Find process using port
lsof -i :8080      # macOS/Linux
netstat -ano | findstr :8080  # Windows

# Change port in docker-compose.yml or .env
SERVER_PORT=9090
```

---

## Performance Optimization

### **MySQL Optimization**

Edit `docker-compose.yml` command section:
```yaml
command: >
  --default-authentication-plugin=mysql_native_password
  --max_connections=1000
  --query_cache_type=1
  --query_cache_size=256M
```

### **Redis Optimization**

```yaml
command: >
  redis-server
  --requirepass redis_pass123
  --appendonly yes
  --maxmemory 512mb
  --maxmemory-policy allkeys-lru
```

### **JVM Optimization**

In `Dockerfile`, modify ENTRYPOINT:
```dockerfile
ENTRYPOINT ["java", \
  "-Xmx512m", \
  "-Xms256m", \
  "-XX:+UseG1GC", \
  "-jar", "app.jar"]
```

---

## Production Deployment

For production deployment:

1. **Use environment-specific compose files:**
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```

2. **Enable security:**
   - Change default passwords
   - Use secret management (Docker Secrets)
   - Enable authentication for all services

3. **Monitor and logging:**
   - Set up centralized logging
   - Use Prometheus + Grafana
   - Monitor container metrics

4. **Backup strategy:**
   - Regular MySQL backups
   - Redis persistence verification
   - Volume snapshots

5. **Resource limits:**
   ```yaml
   services:
     app:
       deploy:
         resources:
           limits:
             cpus: '1'
             memory: 1G
           reservations:
             cpus: '0.5'
             memory: 512M
   ```

---

## CI/CD Integration

### **GitHub Actions Example**

```yaml
name: Docker Build & Push
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: docker/setup-buildx-action@v2
      - uses: docker/build-push-action@v4
        with:
          push: true
          tags: myrepo/library-app:latest
```

---

## Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [MySQL Docker Image](https://hub.docker.com/_/mysql)
- [Redis Docker Image](https://hub.docker.com/_/redis)
- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker)

---

## Support

For issues or questions:
1. Check logs: `docker-compose logs -f`
2. Review this guide's troubleshooting section
3. Visit [Docker Community](https://forums.docker.com/)
