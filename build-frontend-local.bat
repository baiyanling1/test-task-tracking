@echo off
echo 开始本地构建前端...

cd frontend

echo 安装依赖...
npm install

echo 构建前端应用...
npm run build

echo 前端构建完成！
echo 构建结果位于: frontend/dist/

pause
