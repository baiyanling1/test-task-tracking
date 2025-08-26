@echo off
echo === 部署到虚拟机 ===

echo 1. 传输文件到虚拟机...
scp -r . root@10.18.40.48:/root/test-tracking/

echo 2. 在虚拟机上执行部署脚本...
ssh root@10.18.40.48 "cd /root/test-tracking && chmod +x deploy-to-vm.sh && ./deploy-to-vm.sh"

echo === 部署完成 ===
pause
