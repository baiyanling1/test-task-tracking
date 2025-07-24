#!/bin/bash

echo "========================================"
echo "测试任务跟踪系统 - 服务管理"
echo "========================================"

show_menu() {
    echo ""
    echo "请选择操作："
    echo "1) 查看服务状态"
    echo "2) 快速重启（应用+前端+Nginx）"
    echo "3) 智能部署（检测变更）"
    echo "4) 完整重建（所有服务）"
    echo "5) 停止所有服务"
    echo "6) 启动所有服务"
    echo "7) 查看服务日志"
    echo "8) 清理容器和镜像"
    echo "9) 健康检查"
    echo "0) 退出"
    echo ""
    read -p "请输入选项 [0-9]: " choice
}

show_service_status() {
    echo "========================================"
    echo "服务状态"
    echo "========================================"
    docker-compose -f docker-compose.prod.yml ps
    
    echo ""
    echo "容器资源使用情况："
    docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}"
}

quick_restart() {
    echo "========================================"
    echo "快速重启服务"
    echo "========================================"
    docker-compose -f docker-compose.prod.yml restart app frontend nginx
    echo "✅ 服务重启完成"
    sleep 10
    show_service_status
}

smart_deploy() {
    echo "========================================"
    echo "智能部署"
    echo "========================================"
    ./智能部署.sh
}

full_rebuild() {
    echo "========================================"
    echo "完整重建所有服务"
    echo "========================================"
    read -p "确定要重建所有服务吗？这将停止所有服务并重新构建 [y/N]: " confirm
    if [[ $confirm =~ ^[Yy]$ ]]; then
        docker-compose -f docker-compose.prod.yml down
        docker-compose -f docker-compose.prod.yml up -d --build
        echo "✅ 完整重建完成"
        sleep 20
        show_service_status
    else
        echo "❌ 操作已取消"
    fi
}

stop_services() {
    echo "========================================"
    echo "停止所有服务"
    echo "========================================"
    docker-compose -f docker-compose.prod.yml down
    echo "✅ 所有服务已停止"
}

start_services() {
    echo "========================================"
    echo "启动所有服务"
    echo "========================================"
    docker-compose -f docker-compose.prod.yml up -d
    echo "✅ 所有服务已启动"
    sleep 15
    show_service_status
}

show_logs() {
    echo "========================================"
    echo "查看服务日志"
    echo "========================================"
    echo "选择要查看的服务："
    echo "1) 应用服务日志"
    echo "2) 前端服务日志"
    echo "3) MySQL日志"
    echo "4) Redis日志"
    echo "5) Nginx日志"
    echo "6) 所有服务日志"
    echo "0) 返回主菜单"
    echo ""
    read -p "请输入选项 [0-6]: " log_choice
    
    case $log_choice in
        1) docker logs test_tracking_app --tail 50 -f ;;
        2) docker logs test_tracking_frontend --tail 50 -f ;;
        3) docker logs test_tracking_mysql --tail 50 -f ;;
        4) docker logs test_tracking_redis --tail 50 -f ;;
        5) docker logs test_tracking_nginx --tail 50 -f ;;
        6) docker-compose -f docker-compose.prod.yml logs -f ;;
        0) return ;;
        *) echo "❌ 无效选项" ;;
    esac
}

cleanup() {
    echo "========================================"
    echo "清理容器和镜像"
    echo "========================================"
    read -p "确定要清理吗？这将删除所有容器和镜像 [y/N]: " confirm
    if [[ $confirm =~ ^[Yy]$ ]]; then
        echo "停止所有服务..."
        docker-compose -f docker-compose.prod.yml down
        
        echo "删除所有容器..."
        docker rm -f $(docker ps -aq) 2>/dev/null || true
        
        echo "删除所有镜像..."
        docker rmi -f $(docker images -q) 2>/dev/null || true
        
        echo "清理未使用的资源..."
        docker system prune -f
        
        echo "✅ 清理完成"
    else
        echo "❌ 操作已取消"
    fi
}

health_check() {
    echo "========================================"
    echo "健康检查"
    echo "========================================"
    
    echo "检查容器状态..."
    docker-compose -f docker-compose.prod.yml ps
    
    echo ""
    echo "检查端口监听..."
    netstat -tlnp | grep -E ':(80|3000|8080|3306|6379)' || echo "未找到相关端口监听"
    
    echo ""
    echo "测试服务连通性..."
    echo "后端API: $(curl -f http://localhost:8080/actuator/health 2>/dev/null && echo "✅ 正常" || echo "❌ 异常")"
    echo "前端服务: $(curl -f http://localhost:3000 2>/dev/null && echo "✅ 正常" || echo "❌ 异常")"
    echo "Nginx代理: $(curl -f http://localhost/health 2>/dev/null && echo "✅ 正常" || echo "❌ 异常")"
    
    echo ""
    echo "检查数据库连接..."
    docker exec test_tracking_mysql mysql -u testuser -pTestUser@2024 -e "SELECT 1;" 2>/dev/null && echo "✅ MySQL连接正常" || echo "❌ MySQL连接异常"
    
    echo ""
    echo "检查Redis连接..."
    docker exec test_tracking_redis redis-cli ping 2>/dev/null && echo "✅ Redis连接正常" || echo "❌ Redis连接异常"
}

# 主循环
while true; do
    show_menu
    
    case $choice in
        1) show_service_status ;;
        2) quick_restart ;;
        3) smart_deploy ;;
        4) full_rebuild ;;
        5) stop_services ;;
        6) start_services ;;
        7) show_logs ;;
        8) cleanup ;;
        9) health_check ;;
        0) echo "退出服务管理"; exit 0 ;;
        *) echo "❌ 无效选项，请重新选择" ;;
    esac
    
    echo ""
    read -p "按回车键继续..."
done 