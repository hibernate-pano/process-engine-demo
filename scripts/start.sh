#!/bin/bash
# 一键启动前后端脚本

# 启动后端

DIR=$(cd "$(dirname "$0")" && pwd)
cd $DIR/../backend || exit 1
# if [ ! -f "target/processengine-backend-0.0.1-SNAPSHOT.jar" ]; then
echo "[INFO] 后端正在编译..."
mvn clean package -DskipTests || exit 1
# fi
nohup java -jar target/processengine-backend-0.0.1-SNAPSHOT.jar > backend.log 2>&1 &
BACKEND_PID=$!
echo "[OK] 后端已启动，PID: $BACKEND_PID"
cd ..

# 启动前端
cd $DIR/../frontend || exit 1
nohup npm install
nohup npm run dev > frontend.log 2>&1 &
FRONTEND_PID=$!
echo "[OK] 前端已启动，PID: $FRONTEND_PID"
cd ..

# 显示访问地址
sleep 2
echo "----------------------------------------"
echo "前端访问: http://localhost:5173/"
echo "后端API:  http://localhost:8080/api/process-definitions"
echo "----------------------------------------"
