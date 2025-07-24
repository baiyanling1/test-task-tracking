#!/bin/bash

echo "========================================"
echo "Deploy Infrastructure Services"
echo "========================================"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running, please start Docker first"
    exit 1
fi

echo "1. Checking existing infrastructure services..."
MYSQL_RUNNING=$(docker ps --filter "name=test_tracking_mysql" --format "{{.Names}}" | wc -l)
REDIS_RUNNING=$(docker ps --filter "name=test_tracking_redis" --format "{{.Names}}" | wc -l)

if [ $MYSQL_RUNNING -gt 0 ]; then
    echo "⚠️  MySQL is running, stopping and restarting..."
    docker stop test_tracking_mysql
    docker rm test_tracking_mysql
fi

if [ $REDIS_RUNNING -gt 0 ]; then
    echo "⚠️  Redis is running, stopping and restarting..."
    docker stop test_tracking_redis
    docker rm test_tracking_redis
fi

echo ""
echo "2. Starting MySQL service..."
docker-compose -f docker-compose.prod.yml up -d mysql

echo "3. Starting Redis service..."
docker-compose -f docker-compose.prod.yml up -d redis

echo ""
echo "4. Waiting for infrastructure services to start..."
sleep 20

echo ""
echo "5. Checking infrastructure services status..."
docker-compose -f docker-compose.prod.yml ps mysql redis

echo ""
echo "6. Testing infrastructure connections..."

# Test MySQL connection
echo "Testing MySQL connection..."
sleep 5
docker exec test_tracking_mysql mysql -u testuser -pTestUser@2024 -e "SELECT 1;" 2>/dev/null && echo "✅ MySQL connection OK" || echo "❌ MySQL connection failed"

# Test Redis connection
echo "Testing Redis connection..."
docker exec test_tracking_redis redis-cli ping 2>/dev/null && echo "✅ Redis connection OK" || echo "❌ Redis connection failed"

echo ""
echo "========================================"
echo "🎉 Infrastructure deployment completed!"
echo "========================================"
echo ""
echo "📊 Service Status:"
echo "   MySQL: localhost:3306"
echo "   Redis: localhost:6379"
echo ""
echo "📝 Database Configuration:"
echo "   Database: test_tracking"
echo "   Username: testuser"
echo "   Password: TestUser@2024"
echo "   Root Password: TestTracking@2024"
echo ""
echo "🔧 Management Commands:"
echo "   View logs: docker logs test_tracking_mysql -f"
echo "   View logs: docker logs test_tracking_redis -f"
echo "   Stop services: docker-compose -f docker-compose.prod.yml stop mysql redis"
echo "   Start services: docker-compose -f docker-compose.prod.yml start mysql redis" 