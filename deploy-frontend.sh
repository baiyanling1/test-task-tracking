#!/bin/bash

echo "========================================"
echo "Deploy Frontend Service"
echo "========================================"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running, please start Docker first"
    exit 1
fi

echo "1. Starting frontend deployment..."

echo ""
echo "2. Preparing frontend for deployment..."

echo ""
echo "3. Checking existing frontend service status..."
FRONTEND_RUNNING=$(docker ps --filter "name=test_tracking_frontend" --format "{{.Names}}" | wc -l)

if [ $FRONTEND_RUNNING -gt 0 ]; then
    echo "⚠️  Frontend service is running, stopping and restarting..."
    docker stop test_tracking_frontend
    docker rm test_tracking_frontend
fi

echo ""
echo "4. Building frontend service..."
docker-compose -f docker-compose.prod.yml build frontend

echo ""
echo "5. Starting frontend service..."
docker-compose -f docker-compose.prod.yml up -d frontend

echo ""
echo "6. Waiting for frontend service to start..."
sleep 10

echo ""
echo "7. Checking frontend service status..."
docker-compose -f docker-compose.prod.yml ps frontend

echo ""
echo "8. Viewing frontend service logs..."
docker logs test_tracking_frontend --tail 10

echo ""
echo "9. Testing frontend service..."
sleep 5
curl -f http://localhost:3000 2>/dev/null && echo "✅ Frontend service started successfully!" || echo "❌ Frontend service failed to start"

echo ""
echo "========================================"
echo "🎉 Frontend service deployment completed!"
echo "========================================"
echo ""
echo "🌐 Access URLs:"
echo "   Frontend: http://localhost:3000"
echo "   Backend API: http://localhost:8080"
echo ""
echo "🔧 Management Commands:"
echo "   View logs: docker logs test_tracking_frontend -f"
echo "   Restart service: docker-compose -f docker-compose.prod.yml restart frontend"
echo "   Stop service: docker-compose -f docker-compose.prod.yml stop frontend"
echo ""
echo "📝 Configuration:"
echo "   Application: Vue.js 3"
echo "   Build Tool: Vite"
echo "   UI Framework: Element Plus" 