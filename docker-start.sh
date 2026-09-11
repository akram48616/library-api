#!/bin/bash
# docker-start.sh - Start all Library Management System services

set -e

echo "📦 Starting Library Management System with Docker Compose..."
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install it first."
    exit 1
fi

# Parse command line arguments
PROFILE=""
if [ "$1" == "--with-tools" ] || [ "$1" == "-t" ]; then
    PROFILE="--profile tools"
    echo "🛠️  Starting with additional tools (PhpMyAdmin, Redis Commander)..."
fi

# Start services
echo "🚀 Starting services..."
docker-compose $PROFILE up -d

# Wait for services to be healthy
echo ""
echo "⏳ Waiting for services to be ready..."
sleep 5

# Check service health
echo ""
echo "✅ Checking service health..."

# MySQL
if docker-compose exec -T mysql mysqladmin ping -u root -proot > /dev/null 2>&1; then
    echo "✓ MySQL is ready"
else
    echo "⚠️  MySQL is starting... (this may take a moment)"
fi

# Redis
if docker-compose exec -T redis redis-cli -a redis_pass123 ping > /dev/null 2>&1; then
    echo "✓ Redis is ready"
else
    echo "⚠️  Redis is starting... (this may take a moment)"
fi

# Application
echo "⏳ Waiting for application to start..."
for i in {1..30}; do
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✓ Application is ready"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "⚠️  Application is still starting (check logs with: docker-compose logs -f app)"
    fi
    sleep 2
done

echo ""
echo "════════════════════════════════════════════════════════════"
echo "🎉 Library Management System is running!"
echo "════════════════════════════════════════════════════════════"
echo ""
echo "📍 Services:"
echo "   API:          http://localhost:8080"
echo "   Swagger UI:   http://localhost:8080/swagger-ui.html"
echo "   API Docs:     http://localhost:8080/api-docs"
echo "   Health:       http://localhost:8080/actuator/health"
echo ""
echo "📊 Database:"
echo "   Host:         localhost:3306"
echo "   Database:     librarymanagement"
echo "   User:         library_user"
echo "   Password:     library_pass123"
echo ""
echo "🗄️  Cache:"
echo "   Host:         localhost:6379"
echo "   Password:     redis_pass123"
echo ""

if [ ! -z "$PROFILE" ]; then
    echo "🛠️  Management Tools:"
    echo "   PhpMyAdmin:   http://localhost:8082"
    echo "   Redis Cmd:    http://localhost:8081"
    echo ""
fi

echo "📝 Useful commands:"
echo "   View logs:        docker-compose logs -f"
echo "   Specific logs:    docker-compose logs -f app"
echo "   Stop services:    docker-compose down"
echo "   MySQL shell:      docker exec -it library-mysql mysql -u root -proot"
echo "   Redis shell:      docker exec -it library-redis redis-cli -a redis_pass123"
echo ""
echo "📚 More info: cat DOCKER_SETUP.md"
echo "════════════════════════════════════════════════════════════"
