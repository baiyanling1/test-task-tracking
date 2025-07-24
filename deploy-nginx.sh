#!/bin/bash

echo "========================================"
echo "Deploy Nginx Proxy"
echo "========================================"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running, please start Docker first"
    exit 1
fi

echo "1. Checking existing Nginx service status..."
NGINX_RUNNING=$(docker ps --filter "name=test_tracking_nginx" --format "{{.Names}}" | wc -l)

if [ $NGINX_RUNNING -gt 0 ]; then
    echo "⚠️  Nginx service is running, stopping and restarting..."
    docker stop test_tracking_nginx
    docker rm test_tracking_nginx
fi

echo ""
echo "2. Starting Nginx proxy service..."
docker-compose -f docker-compose.prod.yml up -d nginx

echo ""
echo "3. Waiting for Nginx service to start..."
sleep 10

echo ""
echo "4. Checking Nginx service status..."
docker-compose -f docker-compose.prod.yml ps nginx

echo ""
echo "5. Viewing Nginx service logs..."
docker logs test_tracking_nginx --tail 10

echo ""
echo "6. Testing Nginx proxy..."
sleep 5
curl -f http://localhost/health 2>/dev/null && echo "✅ Nginx proxy started successfully!" || echo "❌ Nginx proxy failed to start"

echo ""
echo "========================================"
echo "🎉 Nginx proxy deployment completed!"
echo "========================================"
echo ""
echo "🌐 Access URLs:"
echo "   Main Proxy: http://localhost"
echo "   Health Check: http://localhost/health"
echo ""
echo "🔧 Management Commands:"
echo "   View logs: docker logs test_tracking_nginx -f"
echo "   Restart service: docker-compose -f docker-compose.prod.yml restart nginx"
echo "   Stop service: docker-compose -f docker-compose.prod.yml stop nginx"
echo ""
echo "📝 Configuration:"
echo "   Config file: nginx/nginx.conf"
echo "   SSL certificates: nginx/ssl/ (if configured)" 