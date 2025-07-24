#!/bin/bash

echo "========================================"
echo "Test Task Tracking - Service Manager"
echo "========================================"

show_menu() {
    echo ""
    echo "Please select an operation:"
    echo "1) View service status"
    echo "2) Quick restart (app + frontend + nginx)"
    echo "3) Deploy all services"
    echo "4) Full rebuild (all services)"
    echo "5) Stop all services"
    echo "6) Start all services"
    echo "7) View service logs"
    echo "8) Clean containers and images"
    echo "9) Health check"
    echo "0) Exit"
    echo ""
    read -p "Enter option [0-9]: " choice
}

show_service_status() {
    echo "========================================"
    echo "Service Status"
    echo "========================================"
    docker-compose -f docker-compose.prod.yml ps
    
    echo ""
    echo "Container resource usage:"
    docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}"
}

quick_restart() {
    echo "========================================"
    echo "Quick Restart Services"
    echo "========================================"
    docker-compose -f docker-compose.prod.yml restart app frontend nginx
    echo "✅ Services restarted"
    sleep 10
    show_service_status
}

deploy_all() {
    echo "========================================"
    echo "Deploy All Services"
    echo "========================================"
    echo "This will deploy all services in the correct order:"
    echo "1. Infrastructure (MySQL + Redis)"
    echo "2. Backend service"
    echo "3. Frontend service"
    echo "4. Nginx proxy"
    echo ""
    read -p "Continue? [y/N]: " confirm
    if [[ $confirm =~ ^[Yy]$ ]]; then
        ./deploy-infrastructure.sh
        ./deploy-backend.sh
        ./deploy-frontend.sh
        ./deploy-nginx.sh
        echo "✅ All services deployed"
    else
        echo "❌ Operation cancelled"
    fi
}

full_rebuild() {
    echo "========================================"
    echo "Full Rebuild All Services"
    echo "========================================"
    read -p "Are you sure? This will stop all services and rebuild [y/N]: " confirm
    if [[ $confirm =~ ^[Yy]$ ]]; then
        docker-compose -f docker-compose.prod.yml down
        docker-compose -f docker-compose.prod.yml up -d --build
        echo "✅ Full rebuild completed"
        sleep 20
        show_service_status
    else
        echo "❌ Operation cancelled"
    fi
}

stop_services() {
    echo "========================================"
    echo "Stop All Services"
    echo "========================================"
    docker-compose -f docker-compose.prod.yml down
    echo "✅ All services stopped"
}

start_services() {
    echo "========================================"
    echo "Start All Services"
    echo "========================================"
    docker-compose -f docker-compose.prod.yml up -d
    echo "✅ All services started"
    sleep 15
    show_service_status
}

show_logs() {
    echo "========================================"
    echo "View Service Logs"
    echo "========================================"
    echo "Select service to view logs:"
    echo "1) Backend service logs"
    echo "2) Frontend service logs"
    echo "3) MySQL logs"
    echo "4) Redis logs"
    echo "5) Nginx logs"
    echo "6) All service logs"
    echo "0) Return to main menu"
    echo ""
    read -p "Enter option [0-6]: " log_choice
    
    case $log_choice in
        1) docker logs test_tracking_app --tail 50 -f ;;
        2) docker logs test_tracking_frontend --tail 50 -f ;;
        3) docker logs test_tracking_mysql --tail 50 -f ;;
        4) docker logs test_tracking_redis --tail 50 -f ;;
        5) docker logs test_tracking_nginx --tail 50 -f ;;
        6) docker-compose -f docker-compose.prod.yml logs -f ;;
        0) return ;;
        *) echo "❌ Invalid option" ;;
    esac
}

cleanup() {
    echo "========================================"
    echo "Clean Containers and Images"
    echo "========================================"
    read -p "Are you sure? This will delete all containers and images [y/N]: " confirm
    if [[ $confirm =~ ^[Yy]$ ]]; then
        echo "Stopping all services..."
        docker-compose -f docker-compose.prod.yml down
        
        echo "Removing all containers..."
        docker rm -f $(docker ps -aq) 2>/dev/null || true
        
        echo "Removing all images..."
        docker rmi -f $(docker images -q) 2>/dev/null || true
        
        echo "Cleaning unused resources..."
        docker system prune -f
        
        echo "✅ Cleanup completed"
    else
        echo "❌ Operation cancelled"
    fi
}

health_check() {
    echo "========================================"
    echo "Health Check"
    echo "========================================"
    
    echo "Checking container status..."
    docker-compose -f docker-compose.prod.yml ps
    
    echo ""
    echo "Checking port listening..."
    netstat -tlnp | grep -E ':(80|3000|8080|3306|6379)' || echo "No relevant ports found"
    
    echo ""
    echo "Testing service connectivity..."
    echo "Backend API: $(curl -f http://localhost:8080/actuator/health 2>/dev/null && echo "✅ OK" || echo "❌ Failed")"
    echo "Frontend service: $(curl -f http://localhost:3000 2>/dev/null && echo "✅ OK" || echo "❌ Failed")"
    echo "Nginx proxy: $(curl -f http://localhost/health 2>/dev/null && echo "✅ OK" || echo "❌ Failed")"
    
    echo ""
    echo "Checking database connection..."
    docker exec test_tracking_mysql mysql -u testuser -pTestUser@2024 -e "SELECT 1;" 2>/dev/null && echo "✅ MySQL connection OK" || echo "❌ MySQL connection failed"
    
    echo ""
    echo "Checking Redis connection..."
    docker exec test_tracking_redis redis-cli ping 2>/dev/null && echo "✅ Redis connection OK" || echo "❌ Redis connection failed"
}

# Main loop
while true; do
    show_menu
    
    case $choice in
        1) show_service_status ;;
        2) quick_restart ;;
        3) deploy_all ;;
        4) full_rebuild ;;
        5) stop_services ;;
        6) start_services ;;
        7) show_logs ;;
        8) cleanup ;;
        9) health_check ;;
        0) echo "Exiting service manager"; exit 0 ;;
        *) echo "❌ Invalid option, please try again" ;;
    esac
    
    echo ""
    read -p "Press Enter to continue..."
done 