#!/bin/bash
# 一键关闭前后端脚本

# 关闭后端
BACKEND_PID=$(ps aux | grep 'processengine-backend-0.0.1-SNAPSHOT.jar' | grep -v grep | awk '{print $2}')
if [ -n "$BACKEND_PID" ]; then
  kill $BACKEND_PID
  echo "[OK] 后端已关闭，PID: $BACKEND_PID"
else
  echo "[INFO] 未检测到后端进程"
fi

# 关闭前端
FRONTEND_PID=$(ps aux | grep 'npm run dev' | grep -v grep | awk '{print $2}')
if [ -n "$FRONTEND_PID" ]; then
  kill $FRONTEND_PID
  echo "[OK] 前端已关闭，PID: $FRONTEND_PID"
else
  echo "[INFO] 未检测到前端进程"
fi
