#!/bin/bash

echo "========================================"
echo "Deploy Backend Service"
echo "========================================"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running, please start Docker first"
    exit 1
fi

echo "1. Checking infrastructure services..."
MYSQL_RUNNING=$(docker ps --filter "name=test_tracking_mysql" --format "{{.Names}}" | wc -l)
REDIS_RUNNING=$(docker ps --filter "name=test_tracking_redis" --format "{{.Names}}" | wc -l)

if [ $MYSQL_RUNNING -eq 0 ]; then
    echo "⚠️  MySQL is not running, deploying infrastructure first..."
    ./deploy-infrastructure.sh
    if [ $? -ne 0 ]; then
        echo "❌ Infrastructure deployment failed"
        exit 1
    fi
else
    echo "✅ MySQL is running"
fi

if [ $REDIS_RUNNING -eq 0 ]; then
    echo "⚠️  Redis is not running, deploying infrastructure first..."
    ./deploy-infrastructure.sh
    if [ $? -ne 0 ]; then
        echo "❌ Infrastructure deployment failed"
        exit 1
    fi
else
    echo "✅ Redis is running"
fi

echo ""
echo "2. Checking existing backend service status..."
BACKEND_RUNNING=$(docker ps --filter "name=test_tracking_app" --format "{{.Names}}" | wc -l)

if [ $BACKEND_RUNNING -gt 0 ]; then
    echo "⚠️  Backend service is running, stopping and restarting..."
    docker stop test_tracking_app
    docker rm test_tracking_app
fi

echo ""
echo "3. Building backend service..."
docker-compose -f docker-compose.prod.yml build app

echo ""
echo "4. Starting backend service..."
docker-compose -f docker-compose.prod.yml up -d app

echo ""
echo "5. Waiting for backend service to start..."
sleep 15

echo ""
echo "6. Checking backend service status..."
docker-compose -f docker-compose.prod.yml ps app

echo ""
echo "7. Viewing backend service logs..."
docker logs test_tracking_app --tail 20

echo ""
echo "8. Testing backend service..."
sleep 5
curl -f http://localhost:8080/api/health 2>/dev/null && echo "✅ Backend service started successfully!" || echo "❌ Backend service failed to start"

echo ""
echo "========================================"
echo "🎉 Backend service deployment completed!"
echo "========================================"
echo ""
echo "🌐 Access URLs:"
echo "   Backend API: http://localhost:8080"
echo "   Health Check: http://localhost:8080/api/health"
echo ""
echo "🔧 Management Commands:"
echo "   View logs: docker logs test_tracking_app -f"
echo "   Restart service: docker-compose -f docker-compose.prod.yml restart app"
echo "   Stop service: docker-compose -f docker-compose.prod.yml stop app"
echo ""
echo "📝 Configuration:"
echo "   Application: Spring Boot"
echo "   Database: MySQL"
echo "   Cache: Redis" 